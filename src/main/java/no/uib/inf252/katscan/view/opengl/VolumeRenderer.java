package no.uib.inf252.katscan.view.opengl;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_INVALID_ENUM;
import static com.jogamp.opengl.GL.GL_INVALID_FRAMEBUFFER_OPERATION;
import static com.jogamp.opengl.GL.GL_INVALID_OPERATION;
import static com.jogamp.opengl.GL.GL_INVALID_VALUE;
import static com.jogamp.opengl.GL.GL_NO_ERROR;
import static com.jogamp.opengl.GL.GL_OUT_OF_MEMORY;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Level;
import no.uib.inf252.katscan.TrackBall;
import no.uib.inf252.katscan.data.LoadedDataHolder;
import no.uib.inf252.katscan.data.VoxelMatrix;
import no.uib.inf252.katscan.io.DatLoadSaveHandler;

/**
 *
 * @author Marcelo Lima
 */
public class VolumeRenderer extends GLJPanel implements GLEventListener {
    
    private IntBuffer buffers;
    
    private static class BUFFER {
        private static final int VERTICES = 0;
        private static final int INDICES = 1;
        private static final int TEXTURE = 2;
        
        private static final int VBO_LENGTH = 2;
        private static final int TEXTURE_LENGTH = 1;
        private static final int TOTAL_LENGTH = 3;
    }
    
    private TrackBall trackBall;
    
    private final String SHADERS_ROOT = "/shaders";
    private final String SHADERS_NAME = "simpleVolume";
    
    private float[] vertices = new float[] {
	0.0f, 0.0f, 0.0f,
	0.0f, 0.0f, 1.0f,
	0.0f, 1.0f, 0.0f,
	0.0f, 1.0f, 1.0f,
	1.0f, 0.0f, 0.0f,
	1.0f, 0.0f, 1.0f,
	1.0f, 1.0f, 0.0f,
	1.0f, 1.0f, 1.0f
    };
    private short[] indices = new short[]{
	1,5,7,
	7,3,1,
	0,2,6,
        6,4,0,
	0,1,3,
	3,2,0,
	7,5,4,
	4,6,7,
	2,3,7,
	7,6,2,
	1,0,4,
	4,5,1
    };
    private int programName;
    private boolean textureLoaded;

    public VolumeRenderer() throws GLException {
        super(new GLCapabilities(GLProfile.get(GLProfile.GL4)));
        addGLEventListener(this);

        trackBall = new TrackBall();
        
        buffers = IntBuffer.allocate(BUFFER.TOTAL_LENGTH);
        addMouseWheelListener(trackBall);
        addMouseListener(trackBall);
        addMouseMotionListener(trackBall);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL4 gl4 = drawable.getGL().getGL4();
        
        gl4.glGenBuffers(BUFFER.VBO_LENGTH, buffers);
        buffers.position(BUFFER.VBO_LENGTH);
        
        gl4.glBindBuffer(GL.GL_ARRAY_BUFFER, buffers.get(BUFFER.VERTICES));
        gl4.glBufferData(GL.GL_ARRAY_BUFFER, vertices.length * Float.BYTES, FloatBuffer.wrap(vertices), GL.GL_STATIC_DRAW);
        
        gl4.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, buffers.get(BUFFER.INDICES));
        gl4.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, indices.length * Short.BYTES, ShortBuffer.wrap(indices), GL.GL_STATIC_DRAW);
        
        checkError(gl4, "Create Buffers");
        
        VoxelMatrix voxelMatrix = LoadedDataHolder.getInstance().getVoxelMatrix();
        textureLoaded = voxelMatrix != null;
        if (textureLoaded) {
            short[] texture = voxelMatrix.asArray();

            gl4.glGenTextures(BUFFER.TEXTURE_LENGTH, buffers);
            buffers.position(BUFFER.TOTAL_LENGTH);
            gl4.glActiveTexture(GL4.GL_TEXTURE0);
            gl4.glBindTexture(GL4.GL_TEXTURE_3D, buffers.get(BUFFER.TEXTURE));
            gl4.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
            gl4.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
            gl4.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_WRAP_R, GL4.GL_CLAMP_TO_EDGE);
            gl4.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_WRAP_S, GL4.GL_CLAMP_TO_EDGE);
            gl4.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_WRAP_T, GL4.GL_CLAMP_TO_EDGE);

            gl4.glTexImage3D(GL4.GL_TEXTURE_3D, 0, GL4.GL_RED, voxelMatrix.getLength(VoxelMatrix.Axis.X), voxelMatrix.getLength(VoxelMatrix.Axis.Y), voxelMatrix.getLength(VoxelMatrix.Axis.Z), 0, GL4.GL_RED, GL4.GL_SHORT, ShortBuffer.wrap(texture));

            checkError(gl4, "Create Texture");
        }
        
        ShaderCode vertShader = ShaderCode.create(gl4, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT,
                null, SHADERS_NAME, true);
        ShaderCode fragShader = ShaderCode.create(gl4, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT,
                null, SHADERS_NAME, true);

        ShaderProgram shaderProgram = new ShaderProgram();
        shaderProgram.add(vertShader);
        shaderProgram.add(fragShader);

        shaderProgram.init(gl4);
        
        programName = shaderProgram.program();

        shaderProgram.link(gl4, System.out);
        
        gl4.glUseProgram(programName);

        checkError(gl4, "Create Shaders");
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        GL4 gl4 = drawable.getGL().getGL4();
        
        gl4.glDeleteProgram(programName);
        buffers.position(0);
        gl4.glDeleteBuffers(BUFFER.VBO_LENGTH, buffers);
        if (textureLoaded) {
            buffers.position(BUFFER.VBO_LENGTH);
            gl4.glDeleteTextures(BUFFER.TEXTURE_LENGTH, buffers);
        }
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL4 gl4 = drawable.getGL().getGL4();
        gl4.glClearColor(0.2f,0.2f,0.2f,1.0f);
        gl4.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        
        gl4.glEnable(GL.GL_CULL_FACE);
        gl4.glCullFace(GL.GL_FRONT_FACE);
        
        gl4.glUseProgram(programName);
        
        int location = gl4.glGetUniformLocation(programName, "MVP");
        gl4.glUniformMatrix4fv(location, 1, false, trackBall.getCurrentRotationMatrix(), 0);
        
        location = gl4.glGetUniformLocation(programName, "zoom");
        gl4.glUniform1f(location, trackBall.getZoom());
        
        gl4.glBindBuffer(GL.GL_ARRAY_BUFFER, buffers.get(BUFFER.VERTICES));        
        gl4.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, buffers.get(BUFFER.INDICES));
        gl4.glEnableVertexAttribArray(0);
        gl4.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        
        if (textureLoaded) {
            gl4.glActiveTexture(GL4.GL_TEXTURE0);
            gl4.glBindTexture(GL4.GL_TEXTURE_3D, buffers.get(BUFFER.TEXTURE));
        }
        
        gl4.glDrawElements(GL.GL_TRIANGLES, indices.length, GL.GL_UNSIGNED_SHORT, 0);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL4 gl4 = drawable.getGL().getGL4();
        
        gl4.glUseProgram(programName);
//        int location = gl4.glGetUniformLocation(programName, "screenSize");
//        gl4.glUniform2f(location, width, height);
    }
    
    private void checkError(GL gl, String location) {

        int error = gl.glGetError();
        if (error != GL_NO_ERROR) {
            String errorString;
            switch (error) {
                case GL_INVALID_ENUM:
                    errorString = "GL_INVALID_ENUM";
                    break;
                case GL_INVALID_VALUE:
                    errorString = "GL_INVALID_VALUE";
                    break;
                case GL_INVALID_OPERATION:
                    errorString = "GL_INVALID_OPERATION";
                    break;
                case GL_INVALID_FRAMEBUFFER_OPERATION:
                    errorString = "GL_INVALID_FRAMEBUFFER_OPERATION";
                    break;
                case GL_OUT_OF_MEMORY:
                    errorString = "GL_OUT_OF_MEMORY";
                    break;
                default:
                    errorString = "UNKNOWN";
                    break;
            }
            System.out.println("OpenGL Error(" + errorString + "): " + location);
            throw new Error();
        }
    }
}
