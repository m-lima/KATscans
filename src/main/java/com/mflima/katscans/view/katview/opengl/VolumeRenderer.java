package com.mflima.katscans.view.katview.opengl;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import com.mflima.katscans.data.VoxelMatrix;
import com.mflima.katscans.event.CameraListener;
import com.mflima.katscans.event.CutListener;
import com.mflima.katscans.event.RotationListener;
import com.mflima.katscans.event.ScreenListener;
import com.mflima.katscans.model.Camera;
import com.mflima.katscans.model.Cut;
import com.mflima.katscans.model.Rotation;
import com.mflima.katscans.project.displayable.Displayable;
import com.mflima.katscans.util.DisplayObject;
import com.mflima.katscans.util.MatrixUtil;
import com.mflima.katscans.util.KatViewHandler;
import com.mflima.katscans.model.Screen;
import com.mflima.katscans.view.katview.KatView;

/**
 *
 * @author Marcelo Lima
 */
public abstract class VolumeRenderer extends GLJPanel implements KatView, GLEventListener, RotationListener, CameraListener, CutListener, ScreenListener {    
    
    private static final int MODEL_DIRTY = 1 << 0;
    private static final int VIEW_DIRTY = 1 << 1;
    private static final int PROJECTION_DIRTY = 1 << 2;
    private static final int ORTHO_DIRTY = 1 << 3;
    private static final int SLICE_DIRTY = 1 << 4;
    private static final int MIN_DIRTY = 1 << 5;
    private static final int MAX_DIRTY = 1 << 6;
    private static final int STEP_DIRTY = 1 << 7;
    private static final int TRACKED_FLAGS =
                PROJECTION_DIRTY |
                VIEW_DIRTY |
                MODEL_DIRTY |
                ORTHO_DIRTY |
                SLICE_DIRTY |
                MIN_DIRTY |
                MAX_DIRTY |
                STEP_DIRTY;
    
    private static final String PROPERTY_SCREEN = "Screen";
    
    private static final String SHADERS_ROOT = "/shaders/";
    private static final String SHADER_RAYCASTER_NAME = "raycaster";
    private static final String SHADER_MAIN_NAME = "maincaster";
    private final String shaderName;

    private static final int SHADER_RAYCASTER_VERTEX = 0;
    private static final int SHADER_RAYCASTER_FRAG = 1;
    private static final int SHADER_MAIN_VERTEX = 2;
    private static final int SHADER_MAIN_FRAG = 3;

    private static final int BUFFER_VERTICES = 0;
    private static final int BUFFER_INDICES = 1;
    private static final int BUFFER_INDICES_REV = 2;

    private static final int TEXTURE_VOLUME = 0;
    private static final int TEXTURE_FRAME_BUFFER = 1;
    private static final int TEXTURE_RESO_BUFFER = 2;
    protected static final int TEXTURE_COUNT_PARENT = 3;

    private static final int FRAME_BUFFER_FRONT = 0;
    private static final int FRAME_BUFFER_RESO = 1;

    private final int[] shaderLocation;
    private final int[] bufferLocation;
    private final int[] textureLocation;
    private final int[] frameBuffer;

    transient protected final float[] tempMatrix;

    private int indicesCount;

    protected final Displayable displayable;
    protected final Cut cut;
    protected final Camera camera;
    protected final Rotation rotation;
    protected final Screen screen;

    private int raycastingProgram;
    protected int mainProgram;
    
    private int numSample;

    private Timer threadLOD;
    private boolean highLOD;
    private final float lodFactor;
    private int lodWidth;
    private int lodHeight;
    
    private int dirtyValues;
    
    VolumeRenderer(Displayable displayable, String shaderName, float lodFactor) throws GLException {
        super(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
        addGLEventListener(this);

        shaderLocation = new int[4];
        bufferLocation = new int[3];
        textureLocation = new int[3];
        frameBuffer = new int[2];

        this.lodFactor = lodFactor;
        this.shaderName = shaderName;
        
        this.displayable = displayable;
        this.cut = displayable.getCut();
        this.camera = displayable.getCamera();
        this.rotation = displayable.getRotation();
        
        screen = new Screen();
        screen.addKatModelListener(this);

        new KatViewHandler(this, displayable, screen);

        tempMatrix = new float[16];
        numSample = 256;

        threadLOD = new Timer(750, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                highLOD = true;
                repaint();
            }
        });
        threadLOD.setRepeats(false);
    }

    protected abstract void preDraw(GLAutoDrawable drawable);
    
    public abstract boolean isIlluminated();
    public void createStructure(int x, int y, float threshold) {}

    @Override
    public void init(GLAutoDrawable drawable) {
        VoxelMatrix voxelMatrix = displayable.getMatrix();
        if (voxelMatrix == null) {
            throw new GLException("Could not load the volume data");
        }

        dirtyValues = TRACKED_FLAGS;
        highLOD = true;

        GL2 gl2 = drawable.getGL().getGL2();
        loadVertices(gl2);
        loadTexture(gl2, voxelMatrix);
        loadFrameBuffers(gl2);
        loadPrograms(gl2);
        loadRaycastingInitialUniforms(gl2, voxelMatrix);
        loadMainInitialUniforms(gl2, voxelMatrix);
    }

    private void loadVertices(GL2 gl2) {
        gl2.glGenBuffers(bufferLocation.length, bufferLocation, 0);
        DisplayObject displayObject = DisplayObject.getObject(DisplayObject.Type.CUBE);
        indicesCount = displayObject.getIndicesCW().length;

        float[] vertices = displayObject.getVertices();
        gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferLocation[BUFFER_VERTICES]);
        gl2.glBufferData(GL2.GL_ARRAY_BUFFER, vertices.length * Float.BYTES, FloatBuffer.wrap(vertices), GL2.GL_STATIC_DRAW);

        short[] indices = displayObject.getIndicesCW();
        gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferLocation[BUFFER_INDICES]);
        gl2.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, indices.length * Short.BYTES, ShortBuffer.wrap(indices), GL2.GL_STATIC_DRAW);

        indices = displayObject.getIndicesCCW();
        gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferLocation[BUFFER_INDICES_REV]);
        gl2.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, indices.length * Short.BYTES, ShortBuffer.wrap(indices), GL2.GL_STATIC_DRAW);

        checkError(gl2, "Load vertices");
    }

    private void loadTexture(GL2 gl2, VoxelMatrix voxelMatrix) throws RuntimeException {
        short[] texture = voxelMatrix.getData();

        numSample = (int) Math.sqrt(voxelMatrix.getSizeX() * voxelMatrix.getSizeX()
                + voxelMatrix.getSizeY() * voxelMatrix.getSizeY()
                + voxelMatrix.getSizeZ() * voxelMatrix.getSizeZ());

        gl2.glGenTextures(1, textureLocation, TEXTURE_VOLUME);
        gl2.glActiveTexture(GL2.GL_TEXTURE0 + TEXTURE_VOLUME);
        gl2.glBindTexture(GL2.GL_TEXTURE_3D, textureLocation[TEXTURE_VOLUME]);
        gl2.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
        gl2.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
        gl2.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_BORDER);
        gl2.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
        gl2.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_BORDER);
        gl2.glTexImage3D(GL2.GL_TEXTURE_3D, 0, GL2.GL_RED, voxelMatrix.getSizeX(), voxelMatrix.getSizeY(), voxelMatrix.getSizeZ(), 0, GL2.GL_RED, GL2.GL_UNSIGNED_SHORT, ShortBuffer.wrap(texture));
        checkError(gl2, "Create Texture");
    }

    private void loadFrameBuffers(GL2 gl2) {
        lodWidth = (int) (getWidth() * lodFactor);
        lodHeight = (int) (getHeight() * lodFactor);

        gl2.glGenTextures(1, textureLocation, TEXTURE_FRAME_BUFFER);
        gl2.glActiveTexture(GL2.GL_TEXTURE0 + TEXTURE_FRAME_BUFFER);
        gl2.glBindTexture(GL2.GL_TEXTURE_2D, textureLocation[TEXTURE_FRAME_BUFFER]);

        gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
        gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
        gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
        gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_BORDER);
        gl2.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGB, lodWidth, lodHeight, 0, GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, null);

        gl2.glGenFramebuffers(1, frameBuffer, FRAME_BUFFER_FRONT);
        gl2.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBuffer[FRAME_BUFFER_FRONT]);
        gl2.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0, GL2.GL_TEXTURE_2D, textureLocation[TEXTURE_FRAME_BUFFER], 0);

        if (gl2.glCheckFramebufferStatus(GL2.GL_FRAMEBUFFER) != GL2.GL_FRAMEBUFFER_COMPLETE) {
            throw new GLException("Failed to load front frame buffer");
        }

        checkError(gl2, "Load front frame buffer");
        
        gl2.glGenTextures(1, textureLocation, TEXTURE_RESO_BUFFER);
        gl2.glActiveTexture(GL2.GL_TEXTURE0 + TEXTURE_RESO_BUFFER);
        gl2.glBindTexture(GL2.GL_TEXTURE_2D, textureLocation[TEXTURE_RESO_BUFFER]);

        gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
        gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
        gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
        gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_BORDER);

        gl2.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGB, lodWidth, lodHeight, 0, GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, null);

        gl2.glGenFramebuffers(1, frameBuffer, FRAME_BUFFER_RESO);
        gl2.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBuffer[FRAME_BUFFER_RESO]);
        gl2.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0, GL2.GL_TEXTURE_2D, textureLocation[TEXTURE_RESO_BUFFER], 0);

        if (gl2.glCheckFramebufferStatus(GL2.GL_FRAMEBUFFER) != GL2.GL_FRAMEBUFFER_COMPLETE) {
            throw new GLException("Failed to load resolution frame buffer");
        }

        checkError(gl2, "Load resolution frame buffer");
    }

    private void loadPrograms(GL2 gl2) throws GLException {
        loadAndCompileShader(SHADER_RAYCASTER_NAME, SHADER_RAYCASTER_VERTEX, GL2.GL_VERTEX_SHADER, gl2);
        loadAndCompileShader(SHADER_RAYCASTER_NAME, SHADER_RAYCASTER_FRAG, GL2.GL_FRAGMENT_SHADER, gl2);
        loadAndCompileShader(SHADER_MAIN_NAME, SHADER_MAIN_VERTEX, GL2.GL_VERTEX_SHADER, gl2);
        loadAndCompileShader(shaderName, SHADER_MAIN_FRAG, GL2.GL_FRAGMENT_SHADER, gl2);

        raycastingProgram = gl2.glCreateProgram();
        if (raycastingProgram == 0) {
            throw new GLException("Could not create raycasting program");
        }

        mainProgram = gl2.glCreateProgram();
        if (mainProgram == 0) {
            throw new GLException("Could not create main program");
        }

        gl2.glAttachShader(raycastingProgram, shaderLocation[SHADER_RAYCASTER_VERTEX]);
        gl2.glAttachShader(raycastingProgram, shaderLocation[SHADER_RAYCASTER_FRAG]);
        gl2.glLinkProgram(raycastingProgram);
        gl2.glDetachShader(raycastingProgram, shaderLocation[SHADER_RAYCASTER_VERTEX]);
        gl2.glDetachShader(raycastingProgram, shaderLocation[SHADER_RAYCASTER_FRAG]);
        checkLink(gl2, raycastingProgram);
        checkError(gl2, "Link raycasting program");
        
        gl2.glAttachShader(mainProgram, shaderLocation[SHADER_MAIN_VERTEX]);
        gl2.glAttachShader(mainProgram, shaderLocation[SHADER_MAIN_FRAG]);
        gl2.glLinkProgram(mainProgram);
        gl2.glDetachShader(mainProgram, shaderLocation[SHADER_MAIN_VERTEX]);
        gl2.glDetachShader(mainProgram, shaderLocation[SHADER_MAIN_FRAG]);
        checkLink(gl2, mainProgram);
        checkError(gl2, "Link main program");
        
        for (int shader : shaderLocation) {
            gl2.glDeleteShader(shader);
        }
    }

    private void loadAndCompileShader(String codeFile, int location, int type, GL2 gl2) throws GLException {
        String[] shaderCode = new String[1];
        StringBuilder codeBuilder = new StringBuilder();
        codeFile += type == GL2.GL_VERTEX_SHADER ? ".vp" : ".fp";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(SHADERS_ROOT + codeFile)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                codeBuilder.append(line);
                codeBuilder.append('\n');
            }
        } catch (IOException ex) {
            Logger.getLogger(VolumeRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }

        shaderCode[0] = codeBuilder.toString();
        if (shaderCode[0] == null || shaderCode[0].isEmpty()) {
            throw new GLException("Could not read shader");
        }

        int shader = gl2.glCreateShader(type);
        gl2.glShaderSource(shader, 1, shaderCode, new int[]{shaderCode[0].length()}, 0);
        gl2.glCompileShader(shader);
        shaderLocation[location] = shader;
        checkCompile(gl2, shader, codeFile);
        checkError(gl2, "Load and compile shader");
    }

    private void loadRaycastingInitialUniforms(GL2 gl2, VoxelMatrix voxelMatrix) {
        gl2.glUseProgram(raycastingProgram);

        gl2.glBindFragDataLocation(raycastingProgram, 0, "fragColor");
        gl2.glBindAttribLocation(raycastingProgram, 0, "position");

        int location = gl2.glGetUniformLocation(raycastingProgram, "ratio");
        gl2.glUniform3fv(location, 1, voxelMatrix.getRatio(), 0);

        checkError(gl2, "Load initial raycasting uniforms");
    }

    private void loadMainInitialUniforms(GL2 gl2, VoxelMatrix voxelMatrix) {
        gl2.glUseProgram(mainProgram);

        gl2.glBindFragDataLocation(mainProgram, 0, "fragColor");
        gl2.glBindAttribLocation(mainProgram, 0, "position");

        int location = gl2.glGetUniformLocation(mainProgram, "numSamples");
        gl2.glUniform1i(location, numSample);

        location = gl2.glGetUniformLocation(mainProgram, "ratio");
        gl2.glUniform3fv(location, 1, voxelMatrix.getRatio(), 0);

        location = gl2.glGetUniformLocation(mainProgram, "volumeTexture");
        gl2.glUniform1i(location, TEXTURE_VOLUME);

        location = gl2.glGetUniformLocation(mainProgram, "raycastTexture");
        gl2.glUniform1i(location, TEXTURE_FRAME_BUFFER);

        checkError(gl2, "Load initial main uniforms");
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl2 = drawable.getGL().getGL2();
        
        if (highLOD) {
            updateFrameBuffersSize(gl2, getWidth(), getHeight());
        }

        gl2.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBuffer[FRAME_BUFFER_FRONT]);
        gl2.glViewport(0, 0, lodWidth, lodHeight);
        initializeRender(gl2);
        gl2.glUseProgram(raycastingProgram);
        
        checkAndLoadRaycastingUpdates(gl2);
        draw(gl2, false);
        gl2.glFinish();

        gl2.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBuffer[FRAME_BUFFER_RESO]);
        gl2.glViewport(0, 0, lodWidth, lodHeight);
        initializeRender(gl2);
        gl2.glUseProgram(mainProgram);

        checkAndLoadMainUpdates(gl2);
        preDraw(drawable);
        checkError(gl2, "Pre draw");
        draw(gl2, true);
        gl2.glFinish();
        
        gl2.glBindFramebuffer(GL2.GL_DRAW_FRAMEBUFFER, 0);
        gl2.glViewport(0, 0, getWidth(), getHeight());
        initializeRender(gl2);
        gl2.glBlitFramebuffer(0, 0, lodWidth, lodHeight, 0, 0, getWidth(), getHeight(), GL2.GL_COLOR_BUFFER_BIT, GL2.GL_NEAREST);

        if (highLOD) {
            updateFrameBuffersSize(gl2, (int) (getWidth() * lodFactor), (int) (getHeight() * lodFactor));
            highLOD = false;
        } else {
            threadLOD.restart();
        }
    }

    private void initializeRender(GL2 gl2) {
//        gl4.glClearColor(0.234375f, 0.24609375f, 0.25390625f,1.0f);
//        gl4.glClearColor(0.2f,0.2f,0.2f,1.0f);
        gl2.glClearColor(0f, 0f, 0f, 1.0f);
        gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);

//        gl4.glEnable(GL2.GL_DEPTH_TEST);
        gl2.glEnable(GL2.GL_CULL_FACE);
        gl2.glCullFace(GL2.GL_BACK);
        gl2.glEnable(GL2.GL_BLEND);
        gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
//        gl2.glBlendFunc(GL2.GL_ONE_MINUS_DST_ALPHA, GL2.GL_ONE);

        checkError(gl2, "Initialize render");
    }
    
    private void checkAndLoadRaycastingUpdates(GL2 gl2) {
        int uniformLocation;
        if ((dirtyValues & (PROJECTION_DIRTY | VIEW_DIRTY | MODEL_DIRTY)) != 0) {

            if ((dirtyValues & PROJECTION_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(raycastingProgram, "projection");
                gl2.glUniformMatrix4fv(uniformLocation, 1, false, screen.getProjectionMatrix(), 0);
            }

            if ((dirtyValues & VIEW_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(raycastingProgram, "view");
                gl2.glUniformMatrix4fv(uniformLocation, 1, false, camera.getViewMatrix(), 0);
            }

            if ((dirtyValues & MODEL_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(raycastingProgram, "model");
                gl2.glUniformMatrix4fv(uniformLocation, 1, false, rotation.getModelMatrix(), 0);
            }

            checkError(gl2, "Update raycasting dirty values");
        }
    }

    protected void checkAndLoadMainUpdates(GL2 gl2) {
        int uniformLocation;
        if ((dirtyValues & (TRACKED_FLAGS)) != 0) {

            if ((dirtyValues & PROJECTION_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "projection");
                gl2.glUniformMatrix4fv(uniformLocation, 1, false, screen.getProjectionMatrix(), 0);
            }

            if ((dirtyValues & VIEW_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "view");
                gl2.glUniformMatrix4fv(uniformLocation, 1, false, camera.getViewMatrix(), 0);
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "eyePos");
                gl2.glUniform3fv(uniformLocation, 1, camera.getEyePosition(), 0);
            }

            if ((dirtyValues & MODEL_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "model");
                gl2.glUniformMatrix4fv(uniformLocation, 1, false, rotation.getModelMatrix(), 0);
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "invModel");
                gl2.glUniformMatrix3fv(uniformLocation, 1, false, MatrixUtil.getMatrix3(MatrixUtil.getInverse(rotation.getModelMatrix())), 0);
            }

            if ((dirtyValues & ORTHO_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "orthographic");
                gl2.glUniform1i(uniformLocation, screen.isOrthographic() ? 1 : 0);
            }
            
            if ((dirtyValues & SLICE_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "slice");
                float slice = cut.getSlice() + camera.getZoom() - camera.getInitialZoom();
                gl2.glUniform1f(uniformLocation, slice < 0f ? 0f : slice);
            }
            
            if ((dirtyValues & MIN_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "minValues");
                gl2.glUniform3fv(uniformLocation, 1, cut.getMinValues(), 0);
            }
            
            if ((dirtyValues & MAX_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "maxValues");
                gl2.glUniform3fv(uniformLocation, 1, cut.getMaxValues(), 0);
            }
            
            if ((dirtyValues & STEP_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "stepFactor");
                gl2.glUniform1f(uniformLocation, screen.getStepFactor());
            }
            
            dirtyValues = 0;
            checkError(gl2, "Update main dirty values");
        }
    }

    private void draw(GL2 gl2, boolean back) {
        gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferLocation[BUFFER_VERTICES]);
        gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferLocation[back ? BUFFER_INDICES_REV : BUFFER_INDICES]);
        gl2.glEnableVertexAttribArray(0);
        gl2.glVertexAttribPointer(0, 3, GL2.GL_FLOAT, false, 0, 0);

        gl2.glDrawElements(GL2.GL_TRIANGLES, indicesCount, GL2.GL_UNSIGNED_SHORT, 0);

        checkError(gl2, "Draw");
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        screen.updateProjection(camera, width, height);

        GL2 gl2 = drawable.getGL().getGL2();
        updateFrameBuffersSize(gl2, (int) (width * lodFactor), (int) (height * lodFactor));
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        GL2 gl2 = drawable.getGL().getGL2();

        gl2.glUseProgram(0);
        gl2.glDeleteProgram(raycastingProgram);
        gl2.glDeleteProgram(mainProgram);
        
        gl2.glDeleteBuffers(bufferLocation.length, bufferLocation, 0);
        gl2.glDeleteFramebuffers(frameBuffer.length, frameBuffer, 0);

        gl2.glDeleteTextures(textureLocation.length, textureLocation, 0);
        checkError(gl2, "Dispose");
    }

    protected void checkError(GL2 gl, String location) {

        int error = gl.glGetError();
        if (error != GL2.GL_NO_ERROR) {
            String errorString;
            switch (error) {
                case GL2.GL_INVALID_ENUM:
                    errorString = "GL_INVALID_ENUM";
                    break;
                case GL2.GL_INVALID_VALUE:
                    errorString = "GL_INVALID_VALUE";
                    break;
                case GL2.GL_INVALID_OPERATION:
                    errorString = "GL_INVALID_OPERATION";
                    break;
                case GL2.GL_INVALID_FRAMEBUFFER_OPERATION:
                    errorString = "GL_INVALID_FRAMEBUFFER_OPERATION";
                    break;
                case GL2.GL_OUT_OF_MEMORY:
                    errorString = "GL_OUT_OF_MEMORY";
                    break;
                default:
                    errorString = "UNKNOWN";
                    break;
            }
            System.out.println(getClass().getSimpleName() + " :: OpenGL Error(" + errorString + "): " + location);
            throw new Error();
        }
    }
    
    private void updateFrameBuffersSize(GL2 gl2, int width, int height) {
        lodWidth = width;
        lodHeight = height;
        
        gl2.glActiveTexture(GL2.GL_TEXTURE0 + TEXTURE_FRAME_BUFFER);
        gl2.glBindTexture(GL2.GL_TEXTURE_2D, textureLocation[TEXTURE_FRAME_BUFFER]);
        gl2.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGB, lodWidth, lodHeight, 0, GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, null);
        
        gl2.glActiveTexture(GL2.GL_TEXTURE0 + TEXTURE_RESO_BUFFER);
        gl2.glBindTexture(GL2.GL_TEXTURE_2D, textureLocation[TEXTURE_RESO_BUFFER]);
        gl2.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGB, lodWidth, lodHeight, 0, GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, null);
        
        gl2.glUseProgram(mainProgram);
        int location = gl2.glGetUniformLocation(mainProgram, "screenSize");
        gl2.glUniform2i(location, width, height);     

        checkError(gl2, "Update frame buffer textures");   
    }

    protected boolean checkCompile(GL2 gl2, int shader, String shaderName) {
        int[] returnValue = new int[1];
        gl2.glGetShaderiv(shader, GL2.GL_COMPILE_STATUS, returnValue, 0);

        if (returnValue[0] == GL2.GL_FALSE) {
            gl2.glGetShaderiv(shader, GL2.GL_INFO_LOG_LENGTH, returnValue, 0);

            if (returnValue[0] > 0) {
                IntBuffer written = IntBuffer.allocate(1);
                ByteBuffer log = ByteBuffer.allocate(returnValue[0]);
                gl2.glGetShaderInfoLog(shader, returnValue[0], written, log);
                byte[] logArray = log.array();
                
                System.err.println("Compilation error on " + shaderName);
                for (byte letter : logArray) {
                    System.err.print((char) letter);
                }
                System.err.println();
            }
            return false;
        }
        return true;
    }

    protected boolean checkLink(GL2 gl2, int program) {
        int[] returnValue = new int[1];
        gl2.glGetProgramiv(program, GL2.GL_LINK_STATUS, returnValue, 0);
        
        if (returnValue[0] == GL2.GL_FALSE) {
            gl2.glGetShaderiv(program, GL2.GL_INFO_LOG_LENGTH, returnValue, 0);
            
            if (returnValue[0] > 0) {
                IntBuffer written = IntBuffer.allocate(1);
                ByteBuffer log = ByteBuffer.allocate(returnValue[0]);
                gl2.glGetProgramInfoLog(program, returnValue[0], written, log);
                byte[] logArray = log.array();
                System.err.println("Link error on " + (program == raycastingProgram ? "raycasting" : "main") + " program");
                for (byte letter : logArray) {
                    System.err.print((char) letter);
                }
                System.err.println();
            }
            return false;
        }
        return true;
    }

    public float[] getTempMatrix() {
        return tempMatrix;
    }
    
    @Override
    public void rotationValueChanged() {
        dirtyValues |= MODEL_DIRTY;
        repaint();
    }

    @Override
    public void viewValueChanged() {
        dirtyValues |= VIEW_DIRTY;
        repaint();
    }

    @Override
    public void zoomValueChanged() {
        dirtyValues |= SLICE_DIRTY;
        if (screen.isOrthographic()) {
            screen.updateProjection(camera, getWidth(), getHeight());
        }
        repaint();
    }

    @Override
    public void orthographicValueChanged() {
        dirtyValues |= ORTHO_DIRTY;
        screen.updateProjection(camera, getWidth(), getHeight());
        repaint();
    }

    @Override
    public void minValueChanged() {
        dirtyValues |= MIN_DIRTY;
        repaint();
    }

    @Override
    public void maxValueChanged() {
        dirtyValues |= MAX_DIRTY;
        repaint();
    }

    @Override
    public void sliceValueChanged() {
        dirtyValues |= SLICE_DIRTY;
        repaint();
    }

    @Override
    public void projectionValueChanged() {
        dirtyValues |= PROJECTION_DIRTY;
        repaint();
    }

    @Override
    public void stepValueChanged() {
        dirtyValues |= STEP_DIRTY;
        repaint();
    }

    @Override
    public Map<String, Object> packProperties() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_SCREEN, screen);
        return properties;
    }

    @Override
    public void loadProperties(Map<String, Object> properties) {
        if (properties == null || properties.isEmpty()) {
            return;
        }
        
        Screen newScreen = (Screen) properties.get(PROPERTY_SCREEN);
        if (newScreen == null) {
            return;
        }
        
        this.screen.assimilate(newScreen);
    }

}
