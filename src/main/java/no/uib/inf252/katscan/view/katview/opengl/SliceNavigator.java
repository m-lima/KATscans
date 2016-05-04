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
import java.awt.Graphics2D;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import no.uib.inf252.katscan.data.VoxelMatrix;
import no.uib.inf252.katscan.event.TransferFunctionListener;
import no.uib.inf252.katscan.project.displayable.Displayable;
import no.uib.inf252.katscan.util.TrackBall;
import no.uib.inf252.katscan.util.TransferFunction;
import no.uib.inf252.katscan.view.katview.KatView;

/**
 *
 * @author Marcelo Lima
 */
public class SliceNavigator extends GLJPanel implements KatView, GLEventListener, MouseWheelListener, TransferFunctionListener {
    
    private static final int VERTICES = 0;
    private static final int INDICES = 1;

    private static final int VOLUME = 0;
    private static final int TRANSFER = 1;

    private final Displayable displayable;
    private boolean textureLoaded;
    
    private final int[] bufferLocation;
    private final int[] textureLocation;
    private boolean transferFunctionDirty;
    
    private final String SHADERS_ROOT = "/shaders";
    private final String SHADERS_NAME = "slicer";
    
    private float[] vertices = new float[]{0f,0f,0f, 0f,1f,0f, 1f,1f,0f, 1f,0f,0f};
    private short[] indices = new short[]{0, 1, 2, 0, 2, 3};
    private int programName;
    private float sliceMax;
    private int slice;

    public SliceNavigator(Displayable displayable) throws GLException {
        super(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
        addGLEventListener(this);
        
        bufferLocation = new int[2];
        textureLocation = new int[2];
        
        this.displayable = displayable;
        
        addMouseWheelListener(this);
        
        displayable.getTransferFunction().addTransferFunctionListener(this);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl2 = drawable.getGL().getGL2();
        
        gl2.glGenBuffers(bufferLocation.length, bufferLocation, 0);
        
        gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferLocation[VERTICES]);
        gl2.glBufferData(GL2.GL_ARRAY_BUFFER, vertices.length * Float.BYTES, FloatBuffer.wrap(vertices), GL2.GL_STATIC_DRAW);
        
        gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferLocation[INDICES]);
        gl2.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, indices.length * Short.BYTES, ShortBuffer.wrap(indices), GL2.GL_STATIC_DRAW);
        
        checkError(gl2, "Create Buffers");
        
        
        VoxelMatrix voxelMatrix = displayable.getMatrix();
        textureLoaded = voxelMatrix != null;
        if (textureLoaded) {
            sliceMax = voxelMatrix.getSizeZ();
            slice = (int) (sliceMax / 2f);

            short[] texture = voxelMatrix.getData();
            
            gl2.glGenTextures(2, textureLocation, 0);
            gl2.glActiveTexture(GL2.GL_TEXTURE0 + VOLUME);
            gl2.glBindTexture(GL2.GL_TEXTURE_3D, textureLocation[VOLUME]);
            gl2.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
            gl2.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
            gl2.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_EDGE);
            gl2.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
            gl2.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);

            gl2.glTexImage3D(GL2.GL_TEXTURE_3D, 0, GL2.GL_RED, voxelMatrix.getSizeX(), voxelMatrix.getSizeY(), voxelMatrix.getSizeZ(), 0, GL2.GL_RED, GL2.GL_UNSIGNED_SHORT, ShortBuffer.wrap(texture));

            checkError(gl2, "Create Texture");
        
            gl2.glActiveTexture(GL2.GL_TEXTURE0 + TRANSFER);
            gl2.glBindTexture(GL2.GL_TEXTURE_1D, textureLocation[TRANSFER]);
            gl2.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
            gl2.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
            gl2.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_BORDER);
        
            checkError(gl2, "Create Transfer Function");
            transferFunctionDirty = true;
        }
        
        ShaderCode vertShader = ShaderCode.create(gl2, GL2.GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT,
                null, SHADERS_NAME, true);
        ShaderCode fragShader = ShaderCode.create(gl2, GL2.GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT,
                null, SHADERS_NAME, true);

        ShaderProgram shaderProgram = new ShaderProgram();
        shaderProgram.add(vertShader);
        shaderProgram.add(fragShader);
        shaderProgram.init(gl2);
        
        programName = shaderProgram.program();
        shaderProgram.link(gl2, System.out);
        
        gl2.glUseProgram(programName);
        
        gl2.glBindFragDataLocation(programName, 0, "fragColor");
        gl2.glBindAttribLocation(programName, 0, "position");
        
        int location = gl2.glGetUniformLocation(programName, "slice");
        gl2.glUniform1f(location, slice / sliceMax);
        
        if (textureLoaded) {
            location = gl2.glGetUniformLocation(programName, "volumeTexture");
            gl2.glUniform1i(location, VOLUME);
            
            location = gl2.glGetUniformLocation(programName, "transferFunction");
            gl2.glUniform1i(location, TRANSFER);
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
        gl2.glUseProgram(programName);
        
        int location = gl2.glGetUniformLocation(programName, "slice");
        gl2.glUniform1f(location, slice / sliceMax);
        
        gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferLocation[VERTICES]);        
        gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferLocation[INDICES]);
        gl2.glEnableVertexAttribArray(0);
        gl2.glVertexAttribPointer(0, 3, GL2.GL_FLOAT, false, 0, 0);
        
        if (transferFunctionDirty) {
            BufferedImage transferImage = new BufferedImage(TransferFunction.TEXTURE_SIZE, 1, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g2d = (Graphics2D) transferImage.getGraphics();
            g2d.setPaint(displayable.getTransferFunction().getPaint());
            g2d.drawLine(0, 0, TransferFunction.TEXTURE_SIZE, 0);
            g2d.dispose();

            byte[] dataElements = (byte[]) transferImage.getRaster().getDataElements(0, 0, TransferFunction.TEXTURE_SIZE, 1, null);
            gl2.glActiveTexture(GL2.GL_TEXTURE0 + TRANSFER);
            gl2.glBindTexture(GL2.GL_TEXTURE_1D, textureLocation[TRANSFER]);
            gl2.glTexImage1D(GL2.GL_TEXTURE_1D, 0, GL2.GL_RGBA, TransferFunction.TEXTURE_SIZE, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_INT_8_8_8_8_REV, ByteBuffer.wrap(dataElements));
            transferFunctionDirty = false;
        }
        
        gl2.glDrawElements(GL2.GL_TRIANGLES, indices.length, GL2.GL_UNSIGNED_SHORT, 0);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl2 = drawable.getGL().getGL2();
        
        gl2.glUseProgram(programName);
        int location = gl2.glGetUniformLocation(programName, "screenSize");
        gl2.glUniform2i(location, width, height);
    }
    
    private void checkError(GL2 gl, String location) {

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

    @Override
    public TrackBall getTrackBall() {
        return null;
    }

    @Override
    public void setTrackBall(TrackBall trackBall) {}

}
