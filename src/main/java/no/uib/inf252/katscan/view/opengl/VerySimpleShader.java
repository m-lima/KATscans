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
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author Marcelo Lima
 */
public class VerySimpleShader extends GLJPanel implements GLEventListener {

    private IntBuffer buffer;
    
    private final String SHADERS_ROOT = "/shaders";
    private final String SHADERS_NAME = "simple";
    
    private float[] vertices = new float[]{0f,0f,0f, 0f,1f,0f, 1f,1f,0f, 0f,0f,0f, 1f,1f,0f, 1f,0f,0f};
    private int programName;

    public VerySimpleShader() throws GLException {
        super(new GLCapabilities(GLProfile.get(GLProfile.GL4)));
        addGLEventListener(this);
        
        buffer = IntBuffer.allocate(1);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL4 gl4 = drawable.getGL().getGL4();
        
        gl4.glGenBuffers(1, buffer);
        gl4.glBindBuffer(GL.GL_ARRAY_BUFFER, buffer.get(0));
        gl4.glBufferData(GL.GL_ARRAY_BUFFER, vertices.length * Float.BYTES, FloatBuffer.wrap(vertices), GL.GL_STATIC_DRAW);
        
        checkError(gl4, "Create Buffers");
        
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
        

        checkError(gl4, "Create Shaders");
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL4 gl4 = drawable.getGL().getGL4();
        gl4.glUseProgram(programName);
        
        gl4.glEnableVertexAttribArray(0);
        gl4.glBindBuffer(GL.GL_ARRAY_BUFFER, buffer.get(0));
        gl4.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        
        gl4.glDrawArrays(GL.GL_TRIANGLES, 0, 6);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
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
