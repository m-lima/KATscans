package no.uib.inf252.katscan.view.katview.opengl;

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
import java.awt.Graphics2D;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import no.uib.inf252.katscan.data.VoxelMatrix;
import no.uib.inf252.katscan.event.TransferFunctionListener;
import no.uib.inf252.katscan.project.displayable.Displayable;
import no.uib.inf252.katscan.project.displayable.TransferFunctionNode;
import no.uib.inf252.katscan.util.TransferFunction;
import no.uib.inf252.katscan.view.katview.KatView;

/**
 *
 * @author Marcelo Lima
 */
public class SliceNavigator extends GLJPanel implements KatView, GLEventListener, MouseWheelListener, TransferFunctionListener {

    private IntBuffer buffer;
    
    private final Displayable displayable;
    private boolean textureLoaded;
    
    private final int[] textureLocation = new int[1];
    private boolean transferFunctionDirty;
    
    private final String SHADERS_ROOT = "/shaders";
    private final String SHADERS_NAME = "slicer";
    
    private float[] vertices = new float[]{0f,0f,0f, 0f,1f,0f, 1f,1f,0f, 1f,0f,0f};
    private short[] indices = new short[]{0, 1, 2, 0, 2, 3};
    private int programName;
    private float sliceMax;
    private int slice;

    public SliceNavigator(TransferFunctionNode displayable) throws GLException {
        super(new GLCapabilities(GLProfile.get(GLProfile.GL4)));
        addGLEventListener(this);
        
        this.displayable = displayable;
        
        addMouseWheelListener(this);
        
        //TODO Remove listener when done
        displayable.getTransferFunction().addTransferFunctionListener(this);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        buffer = IntBuffer.allocate(3);
        GL4 gl4 = drawable.getGL().getGL4();
        
        gl4.glGenBuffers(2, buffer);
        buffer.position(2);
        
        gl4.glBindBuffer(GL.GL_ARRAY_BUFFER, buffer.get(0));
        gl4.glBufferData(GL.GL_ARRAY_BUFFER, vertices.length * Float.BYTES, FloatBuffer.wrap(vertices), GL.GL_STATIC_DRAW);
        
        gl4.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, buffer.get(1));
        gl4.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, indices.length * Short.BYTES, ShortBuffer.wrap(indices), GL.GL_STATIC_DRAW);
        
        checkError(gl4, "Create Buffers");
        
        
        VoxelMatrix voxelMatrix = displayable.getMatrix();
        textureLoaded = voxelMatrix != null;
        if (textureLoaded) {
            sliceMax = voxelMatrix.getSizeZ();
            slice = (int) (sliceMax / 2f);

            short[] texture = voxelMatrix.getData();
            
            gl4.glGenTextures(1, buffer);
            buffer.position(3);
            gl4.glActiveTexture(GL4.GL_TEXTURE0);
            gl4.glBindTexture(GL4.GL_TEXTURE_3D, buffer.get(2));
            gl4.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
            gl4.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
            gl4.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_WRAP_R, GL4.GL_CLAMP_TO_EDGE);
            gl4.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_WRAP_S, GL4.GL_CLAMP_TO_EDGE);
            gl4.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_WRAP_T, GL4.GL_CLAMP_TO_EDGE);

            gl4.glTexImage3D(GL4.GL_TEXTURE_3D, 0, GL4.GL_RED, voxelMatrix.getSizeX(), voxelMatrix.getSizeY(), voxelMatrix.getSizeZ(), 0, GL4.GL_RED, GL4.GL_UNSIGNED_SHORT, ShortBuffer.wrap(texture));

            checkError(gl4, "Create Texture");
        
            gl4.glGenTextures(1, textureLocation, 0);
            gl4.glBindTexture(GL4.GL_TEXTURE_1D, textureLocation[0]);
            gl4.glTexParameteri(GL4.GL_TEXTURE_1D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
            gl4.glTexParameteri(GL4.GL_TEXTURE_1D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
            gl4.glTexParameteri(GL4.GL_TEXTURE_1D, GL4.GL_TEXTURE_WRAP_R, GL4.GL_CLAMP_TO_BORDER);
        
            checkError(gl4, "Create Transfer Function");
            transferFunctionDirty = true;
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
        int location = gl4.glGetUniformLocation(programName, "slice");
        gl4.glUniform1f(location, slice / sliceMax);
        
        checkError(gl4, "Create Shaders");
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        GL4 gl4 = drawable.getGL().getGL4();
        
        gl4.glDeleteProgram(programName);
        buffer.position(0);
        gl4.glDeleteBuffers(2, buffer);
        if (textureLoaded) {
            buffer.position(2);
            gl4.glDeleteTextures(1, buffer);
        }
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL4 gl4 = drawable.getGL().getGL4();
        gl4.glUseProgram(programName);
        
        int location = gl4.glGetUniformLocation(programName, "slice");
        gl4.glUniform1f(location, slice / sliceMax);
        
        gl4.glBindBuffer(GL.GL_ARRAY_BUFFER, buffer.get(0));        
        gl4.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, buffer.get(1));
        gl4.glEnableVertexAttribArray(0);
        gl4.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        
        gl4.glActiveTexture(GL4.GL_TEXTURE0);
        gl4.glBindTexture(GL4.GL_TEXTURE_3D, buffer.get(2));
        
        if (transferFunctionDirty) {
            BufferedImage transferImage = new BufferedImage(TransferFunction.TEXTURE_SIZE, 1, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g2d = (Graphics2D) transferImage.getGraphics();
            g2d.setPaint(getDisplayable().getTransferFunction().getPaint(0f, TransferFunction.TEXTURE_SIZE));
            g2d.drawLine(0, 0, TransferFunction.TEXTURE_SIZE, 0);
            g2d.dispose();

            byte[] dataElements = (byte[]) transferImage.getRaster().getDataElements(0, 0, TransferFunction.TEXTURE_SIZE, 1, null);
            gl4.glTexImage1D(GL4.GL_TEXTURE_1D, 0, GL4.GL_RGBA, TransferFunction.TEXTURE_SIZE, 0, GL4.GL_RGBA, GL4.GL_UNSIGNED_INT_8_8_8_8_REV, ByteBuffer.wrap(dataElements));
            transferFunctionDirty = false;
        }
        
        gl4.glDrawElements(GL.GL_TRIANGLES, indices.length, GL.GL_UNSIGNED_SHORT, 0);
    }

    private TransferFunctionNode getDisplayable() {
        return (TransferFunctionNode) displayable;
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL4 gl4 = drawable.getGL().getGL4();
        
        gl4.glUseProgram(programName);
        int location = gl4.glGetUniformLocation(programName, "screenSize");
        gl4.glUniform2f(location, width, height);
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

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        slice += e.getWheelRotation();
        if (slice < 0) {
            slice = 0;
        } else {
            if (slice >= sliceMax) {
                slice = ((int) sliceMax) - 1;
            }
        }        
        repaint();
    }

    @Override
    public void pointCountChanged() {
        transferFunctionDirty = true;
        repaint();
    }

    @Override
    public void pointValueChanged() {
        transferFunctionDirty = true;
        repaint();
    }

}
