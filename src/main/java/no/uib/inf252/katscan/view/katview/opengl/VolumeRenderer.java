package no.uib.inf252.katscan.view.katview.opengl;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.math.FloatUtil;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import no.uib.inf252.katscan.data.VoxelMatrix;
import no.uib.inf252.katscan.project.displayable.Displayable;
import no.uib.inf252.katscan.util.DisplayObject;
import no.uib.inf252.katscan.util.MatrixUtil;
import no.uib.inf252.katscan.util.TrackBall;
import no.uib.inf252.katscan.view.katview.KatView;

/**
 *
 * @author Marcelo Lima
 */
public abstract class VolumeRenderer extends GLJPanel implements KatView, GLEventListener {

    private static final String PROPERTY_TRACKBALL = "TrackBall";
    
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
    protected static final int TEXTURE_COUNT_PARENT = 2;

    private static final int FRAME_BUFFER_FRONT = 0;

    private final int[] shaderLocation;
    private final int[] bufferLocation;
    private final int[] textureLocation;
    private final int[] frameBuffer;

    private int indicesCount;

    protected final Displayable displayable;

    private final TrackBall trackBall;

    private int raycastingProgram;
    protected int mainProgram;
    
    private int numSample;

    private Timer threadLOD;
    private boolean highLOD;

    VolumeRenderer(Displayable displayable, String shaderName) throws GLException {
        super(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
        addGLEventListener(this);

        shaderLocation = new int[4];
        bufferLocation = new int[3];
        textureLocation = new int[2];
        frameBuffer = new int[1];

        this.shaderName = shaderName;
        this.displayable = displayable;

        trackBall = new TrackBall(2 * displayable.getMatrix().getRatio()[2]);
        trackBall.installTrackBall(this);

        numSample = 256;

        threadLOD = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                highLOD = true;
                repaint();
            }
        });
        threadLOD.setRepeats(false);
    }

    abstract protected void preDraw(GLAutoDrawable drawable);

    @Override
    public void init(GLAutoDrawable drawable) {
        VoxelMatrix voxelMatrix = displayable.getMatrix();
        if (voxelMatrix == null) {
            throw new GLException("Could not load the volume data");
        }

        trackBall.markAllDirty();
        highLOD = true;

        GL2 gl2 = drawable.getGL().getGL2();
        loadVertices(gl2);
        loadTexture(gl2, voxelMatrix);
        loadFrameBuffer(gl2);
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

    private void loadFrameBuffer(GL2 gl2) {
        gl2.glGenTextures(1, textureLocation, TEXTURE_FRAME_BUFFER);
        gl2.glActiveTexture(GL2.GL_TEXTURE0 + TEXTURE_FRAME_BUFFER);
        gl2.glBindTexture(GL2.GL_TEXTURE_2D, textureLocation[TEXTURE_FRAME_BUFFER]);

        gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
        gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
        gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
        gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_BORDER);

        gl2.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGB, getWidth(), getHeight(), 0, GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, null);

        gl2.glGenFramebuffers(1, frameBuffer, FRAME_BUFFER_FRONT);
        gl2.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBuffer[FRAME_BUFFER_FRONT]);
        gl2.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0, GL2.GL_TEXTURE_2D, textureLocation[TEXTURE_FRAME_BUFFER], 0);

        if (gl2.glCheckFramebufferStatus(GL2.GL_FRAMEBUFFER) != GL2.GL_FRAMEBUFFER_COMPLETE) {
            throw new GLException("Failed to load frame buffer");
        }

        checkError(gl2, "Load FrameBuffer");
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
        try (BufferedReader reader = new BufferedReader(new FileReader(getClass().getResource(SHADERS_ROOT + codeFile).getFile()))) {
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
        int uniformLocation;
        GL2 gl2 = drawable.getGL().getGL2();

        gl2.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBuffer[FRAME_BUFFER_FRONT]);
        gl2.glViewport(0, 0, getWidth(), getHeight());
        initializeRender(gl2);
        gl2.glUseProgram(raycastingProgram);
        
        checkAndLoadRaycastingUpdates(gl2);
        draw(gl2, false);
        gl2.glFlush();
        gl2.glFinish();

        gl2.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
        gl2.glViewport(0, 0, getWidth(), getHeight());
        initializeRender(gl2);
        gl2.glUseProgram(mainProgram);

        if (highLOD) {
            uniformLocation = gl2.glGetUniformLocation(mainProgram, "lodMultiplier");
            gl2.glUniform1i(uniformLocation, 16);
        }

        checkAndLoadMainUpdates(gl2);
        preDraw(drawable);
        checkError(gl2, "Pre draw");
        draw(gl2, true);
        gl2.glFlush();
        gl2.glFinish();

        if (highLOD) {
            uniformLocation = gl2.glGetUniformLocation(mainProgram, "lodMultiplier");
            gl2.glUniform1i(uniformLocation, 1);
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
        int dirtyValues = trackBall.getDirtyValues();
        if ((dirtyValues & (TrackBall.PROJECTION_DIRTY | TrackBall.VIEW_DIRTY | TrackBall.MODEL_DIRTY)) != 0) {

            if ((dirtyValues & TrackBall.PROJECTION_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(raycastingProgram, "projection");
                gl2.glUniformMatrix4fv(uniformLocation, 1, false, trackBall.getProjectionMatrix(), 0);
            }

            if ((dirtyValues & TrackBall.VIEW_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(raycastingProgram, "view");
                gl2.glUniformMatrix4fv(uniformLocation, 1, false, trackBall.getViewMatrix(), 0);
            }

            if ((dirtyValues & TrackBall.MODEL_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(raycastingProgram, "model");
                gl2.glUniformMatrix4fv(uniformLocation, 1, false, trackBall.getModelMatrix(), 0);
            }

            checkError(gl2, "Update raycasting dirty values");
        }
    }

    private void checkAndLoadMainUpdates(GL2 gl2) {
        int uniformLocation;
        int dirtyValues = trackBall.getDirtyValues();
        if ((dirtyValues & (TrackBall.PROJECTION_DIRTY | TrackBall.VIEW_DIRTY | TrackBall.MODEL_DIRTY | TrackBall.ORTHO_DIRTY | TrackBall.SLICE_DIRTY | TrackBall.LIGHT_DIRTY)) != 0) {

            if ((dirtyValues & (TrackBall.VIEW_DIRTY | TrackBall.MODEL_DIRTY)) > 0) {
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "normalMatrix");
                gl2.glUniformMatrix3fv(uniformLocation, 1, false, trackBall.getNormalMatrix(), 0);
            }

            if ((dirtyValues & TrackBall.PROJECTION_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "projection");
                gl2.glUniformMatrix4fv(uniformLocation, 1, false, trackBall.getProjectionMatrix(), 0);
                trackBall.clearDirtyValues(TrackBall.PROJECTION_DIRTY);
            }

            if ((dirtyValues & TrackBall.VIEW_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "view");
                gl2.glUniformMatrix4fv(uniformLocation, 1, false, trackBall.getViewMatrix(), 0);
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "eyePos");
                gl2.glUniform3fv(uniformLocation, 1, trackBall.getEyePosition(), 0);

                trackBall.clearDirtyValues(TrackBall.VIEW_DIRTY);
                trackBall.clearDirtyValues(TrackBall.ZOOM_DIRTY);
                trackBall.clearDirtyValues(TrackBall.FOV_DIRTY);
            }

            if ((dirtyValues & TrackBall.MODEL_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "model");
                gl2.glUniformMatrix4fv(uniformLocation, 1, false, trackBall.getModelMatrix(), 0);
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "invModel");
                gl2.glUniformMatrix3fv(uniformLocation, 1, false, MatrixUtil.getMatrix3(MatrixUtil.getInverse(trackBall.getModelMatrix())), 0);
                trackBall.clearDirtyValues(TrackBall.MODEL_DIRTY);
            }

            if ((dirtyValues & TrackBall.ORTHO_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "orthographic");
                gl2.glUniform1i(uniformLocation, trackBall.isOrthographic() ? 1 : 0);
                trackBall.clearDirtyValues(TrackBall.ORTHO_DIRTY);
            }
            
            if ((dirtyValues & TrackBall.SLICE_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "slice");
                float slice = trackBall.getSlice();
                gl2.glUniform1f(uniformLocation, slice < 0f ? 0f : slice);
                trackBall.clearDirtyValues(TrackBall.SLICE_DIRTY);
            }
            
            if ((dirtyValues & TrackBall.LIGHT_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "lightPos");
                float[] lightPos = trackBall.getLightPosition();
                gl2.glUniform3fv(uniformLocation, 1, lightPos, 0);
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "lightPosFront");
                gl2.glUniform3f(uniformLocation, -lightPos[0], -lightPos[1], -lightPos[2]);
                trackBall.clearDirtyValues(TrackBall.LIGHT_DIRTY);
            }
            
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
        trackBall.updateProjection(width, height);

        GL2 gl2 = drawable.getGL().getGL2();
        gl2.glActiveTexture(GL2.GL_TEXTURE0 + TEXTURE_FRAME_BUFFER);
        gl2.glBindTexture(GL2.GL_TEXTURE_2D, textureLocation[TEXTURE_FRAME_BUFFER]);
        gl2.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGB, width, height, 0, GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, null);
        
        gl2.glUseProgram(mainProgram);
        int location = gl2.glGetUniformLocation(mainProgram, "screenSize");
        gl2.glUniform2i(location, width, height);
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

    @Override
    public Map<String, Object> packProperties() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_TRACKBALL, trackBall);
        return properties;
    }

    @Override
    public void loadProperties(Map<String, Object> properties) {
        if (properties == null || properties.isEmpty()) {
            return;
        }
        
        TrackBall newTrackBall = (TrackBall) properties.get(PROPERTY_TRACKBALL);
        if (newTrackBall == null) {
            return;
        }
        
        this.trackBall.assimilate(newTrackBall);
        repaint();
    }

}
