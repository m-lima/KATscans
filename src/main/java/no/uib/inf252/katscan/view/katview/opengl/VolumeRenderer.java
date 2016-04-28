package no.uib.inf252.katscan.view.katview.opengl;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import javax.swing.Timer;
import no.uib.inf252.katscan.data.VoxelMatrix;
import no.uib.inf252.katscan.project.displayable.Displayable;
import no.uib.inf252.katscan.util.DisplayObject;
import no.uib.inf252.katscan.util.TrackBall;
import no.uib.inf252.katscan.view.katview.KatView;

/**
 *
 * @author Marcelo Lima
 */
public abstract class VolumeRenderer extends GLJPanel implements KatView, GLEventListener {
    
    private static final String SHADERS_ROOT = "/shaders";
    private final String shaderName;
    
    private static final int VERTICES = 0;
    private static final int INDICES = 1;

    private static final int VOLUME = 0;
    
    private final int[] bufferLocation;
    private final int[] textureLocation;
    
    protected final Displayable displayable;
    
    private final TrackBall trackBall;
    private final DisplayObject displayObject;
    
    protected int programName;
    private boolean textureLoaded;
    
    private int numSample;
    
    private Timer threadLOD;
    private boolean highLOD;

    VolumeRenderer(Displayable displayable, String shaderName) throws GLException {
        super(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
        addGLEventListener(this);
        
        bufferLocation = new int[2];
        textureLocation = new int[1];
        
        this.shaderName = shaderName;
        this.displayable = displayable;

        trackBall = new TrackBall(2 * displayable.getMatrix().getRatio()[2]);
        trackBall.installTrackBall(this);

        displayObject = DisplayObject.getObject(DisplayObject.Type.CUBE);
        
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
        trackBall.markAllDirty();
        
        highLOD = true;
        
        GL2 gl2 = drawable.getGL().getGL2();
        
        gl2.glGenBuffers(bufferLocation.length, bufferLocation, 0);
        
        float[] vertices = displayObject.getVertices();
        gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferLocation[VERTICES]);
        gl2.glBufferData(GL2.GL_ARRAY_BUFFER, vertices.length * Float.BYTES, FloatBuffer.wrap(vertices), GL2.GL_STATIC_DRAW);
        
        short[] indices = displayObject.getIndices();
        gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferLocation[INDICES]);
        gl2.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, indices.length * Short.BYTES, ShortBuffer.wrap(indices), GL2.GL_STATIC_DRAW);
        
        checkError(gl2, "Create Buffers");
        
        VoxelMatrix voxelMatrix = displayable.getMatrix();
        textureLoaded = voxelMatrix != null;
        if (textureLoaded) {
            short[] texture = voxelMatrix.getData();
            
            numSample = (int) Math.cbrt(texture.length);
            
            gl2.glGenTextures(1, textureLocation, 0);
            gl2.glActiveTexture(GL2.GL_TEXTURE0 + VOLUME);
            gl2.glBindTexture(GL2.GL_TEXTURE_3D, textureLocation[VOLUME]);
            gl2.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
            gl2.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
            gl2.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_BORDER);
            gl2.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
            gl2.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_BORDER);

            gl2.glTexImage3D(GL2.GL_TEXTURE_3D, 0, GL2.GL_RED, voxelMatrix.getSizeX(), voxelMatrix.getSizeY(), voxelMatrix.getSizeZ(), 0, GL2.GL_RED, GL2.GL_UNSIGNED_SHORT, ShortBuffer.wrap(texture));

            checkError(gl2, "Create Texture");
        }
        
        ShaderCode vertShader = ShaderCode.create(gl2, GL2.GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT,
                null, shaderName, true);
        ShaderCode fragShader = ShaderCode.create(gl2, GL2.GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT,
                null, shaderName, true);

        ShaderProgram shaderProgram = new ShaderProgram();
        shaderProgram.add(vertShader);
        shaderProgram.add(fragShader);
        shaderProgram.init(gl2);

        programName = shaderProgram.program();
        shaderProgram.link(gl2, System.out);
        
        gl2.glUseProgram(programName);
        
        gl2.glBindFragDataLocation(programName, 0, "fragColor");
        gl2.glBindAttribLocation(programName, 0, "position");
        
        if (textureLoaded) {
            int location = gl2.glGetUniformLocation(programName, "numSamples");
            gl2.glUniform1i(location, numSample);
            
            location = gl2.glGetUniformLocation(programName, "ratio");
            gl2.glUniform3fv(location, 1, voxelMatrix.getRatio(), 0);
            
            location = gl2.glGetUniformLocation(programName, "volumeTexture");
            gl2.glUniform1i(location, 0);
        }

        checkError(gl2, "Create Shaders");
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        GL2 gl2 = drawable.getGL().getGL2();
        
        gl2.glDeleteProgram(programName);
        gl2.glDeleteBuffers(bufferLocation.length, bufferLocation, 0);
        
        if (textureLoaded) {
            gl2.glDeleteTextures(textureLocation.length, textureLocation, 0);
        }
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl2 = drawable.getGL().getGL2();
//        gl4.glClearColor(0.234375f, 0.24609375f, 0.25390625f,1.0f);
//        gl4.glClearColor(0.2f,0.2f,0.2f,1.0f);
        gl2.glClearColor(0f,0f,0f,1.0f);
        gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        
//        gl4.glEnable(GL2.GL_DEPTH_TEST);
        gl2.glEnable(GL2.GL_CULL_FACE);
        gl2.glCullFace(GL2.GL_BACK);
        gl2.glEnable(GL2.GL_BLEND);
        gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
//        gl2.glBlendFunc(GL2.GL_ONE_MINUS_DST_ALPHA, GL2.GL_ONE);
        
        gl2.glUseProgram(programName);
        
        int uniformLocation;
        if (highLOD) {
            uniformLocation = gl2.glGetUniformLocation(programName, "lodMultiplier");
            gl2.glUniform1i(uniformLocation, 16);
        }
        
        int dirtyValues = trackBall.getDirtyValues();
        if ((dirtyValues & (TrackBall.PROJECTION_DIRTY | TrackBall.VIEW_DIRTY | TrackBall.MODEL_DIRTY | TrackBall.ORTHO_DIRTY)) != 0) {

            if ((dirtyValues & TrackBall.PROJECTION_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(programName, "projection");
                gl2.glUniformMatrix4fv(uniformLocation, 1, false, trackBall.getProjectionMatrix(), 0);
                trackBall.clearDirtyValues(TrackBall.PROJECTION_DIRTY);
            }

            if ((dirtyValues & TrackBall.VIEW_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(programName, "view");
                gl2.glUniformMatrix4fv(uniformLocation, 1, false, trackBall.getViewMatrix(), 0);                
                uniformLocation = gl2.glGetUniformLocation(programName, "eyePos");
                gl2.glUniform3fv(uniformLocation, 1, trackBall.getEyePosition(), 0);
                trackBall.clearDirtyValues(TrackBall.ZOOM_DIRTY);
                trackBall.clearDirtyValues(TrackBall.ZOOM_DIRTY);
                trackBall.clearDirtyValues(TrackBall.FOV_DIRTY);
            }

            if ((dirtyValues & TrackBall.MODEL_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(programName, "model");
                gl2.glUniformMatrix4fv(uniformLocation, 1, false, trackBall.getModelMatrix(), 0);
                trackBall.clearDirtyValues(TrackBall.MODEL_DIRTY);
            }

            if ((dirtyValues & TrackBall.ORTHO_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(programName, "orthographic");
                gl2.glUniform1i(uniformLocation, trackBall.isOrthographic() ? 1 : 0);
                trackBall.clearDirtyValues(TrackBall.ORTHO_DIRTY);
            }
        }
        
        preDraw(drawable);

        gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferLocation[VERTICES]);        
        gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferLocation[INDICES]);
        gl2.glEnableVertexAttribArray(0);
        gl2.glVertexAttribPointer(0, 3, GL2.GL_FLOAT, false, 0, 0);
        
        //TODO randomize starting point
        gl2.glDrawElements(GL2.GL_TRIANGLES, displayObject.getIndices().length, GL2.GL_UNSIGNED_SHORT, 0);
        
        if (highLOD) {
            uniformLocation = gl2.glGetUniformLocation(programName, "lodMultiplier");
            gl2.glUniform1i(uniformLocation, 1);
            highLOD = false;
        } else {
            threadLOD.restart();
        }
        
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        trackBall.updateProjection(width, height);
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
}