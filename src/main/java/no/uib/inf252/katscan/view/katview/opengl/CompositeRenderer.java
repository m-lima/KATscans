package no.uib.inf252.katscan.view.katview.opengl;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import no.uib.inf252.katscan.event.LightListener;
import no.uib.inf252.katscan.event.TransferFunctionListener;
import no.uib.inf252.katscan.model.Light;
import no.uib.inf252.katscan.project.displayable.Displayable;
import no.uib.inf252.katscan.model.TransferFunction;
import no.uib.inf252.katscan.util.Normal;

/**
 *
 * @author Marcelo Lima
 */
public class CompositeRenderer extends VolumeRenderer implements TransferFunctionListener, LightListener {
    
    private static final int LIGHT_DIRTY = 1 << 0;
    private static final int NORMAL_DIRTY = 1 << 1;
    private static final int TRANSFER_FUNCTION_DIRTY = 1 << 2;
    
    private static final int TEXTURE_TRANSFER_LOCAL = 0;
    private static final int TEXTURE_TRANSFER = TEXTURE_COUNT_PARENT + TEXTURE_TRANSFER_LOCAL;
    
    private final int[] textureLocation = new int[1];
    
    private int dirtyValues;
    private final Normal normal;
    private final Light light;
    
    public CompositeRenderer(Displayable displayable) throws GLException {
        super(displayable, "compoCaster", 0.5f);
        light = displayable.getLight();
        normal = new Normal();
    }

    @Override
    public boolean isIlluminated() {
        return true;
    }

    @Override
    protected void preDraw(GLAutoDrawable drawable) {
        GL2 gl2 = drawable.getGL().getGL2();
        
        if ((dirtyValues & (LIGHT_DIRTY | NORMAL_DIRTY | TRANSFER_FUNCTION_DIRTY)) != 0) {
            int uniformLocation;
            if ((dirtyValues & TRANSFER_FUNCTION_DIRTY) > 0) {
                updateTransferFunction(gl2);
            }

            if ((dirtyValues & NORMAL_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "normalMatrix");
                gl2.glUniformMatrix3fv(uniformLocation, 1, false, normal.getNormalMatrix(), 0);
            }
            
            if ((dirtyValues & LIGHT_DIRTY) > 0) {
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "lightPos");
                float[] lightPos = light.getLightPosition();
                gl2.glUniform3fv(uniformLocation, 1, lightPos, 0);
                uniformLocation = gl2.glGetUniformLocation(mainProgram, "lightPosFront");
                gl2.glUniform3f(uniformLocation, -lightPos[0], -lightPos[1], -lightPos[2]);
            }
            
            dirtyValues = 0;
        }
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        super.init(drawable); 
        
        GL2 gl2 = drawable.getGL().getGL2();
        dirtyValues = LIGHT_DIRTY | NORMAL_DIRTY | TRANSFER_FUNCTION_DIRTY;
        normal.updateMatrices(camera, rotation, tempMatrix);
        
        gl2.glGenTextures(1, textureLocation, TEXTURE_TRANSFER_LOCAL);
        gl2.glActiveTexture(GL2.GL_TEXTURE0 + TEXTURE_TRANSFER);
        gl2.glBindTexture(GL2.GL_TEXTURE_1D, textureLocation[TEXTURE_TRANSFER_LOCAL]);
        gl2.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
        gl2.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
        gl2.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_BORDER);
        
        int location = gl2.glGetUniformLocation(mainProgram, "transferFunction");
        gl2.glUniform1i(location, TEXTURE_TRANSFER);
        
        checkError(gl2, "Create transfer function");
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        super.dispose(drawable);
        GL2 gl2 = drawable.getGL().getGL2();
        
        gl2.glDeleteTextures(textureLocation.length, textureLocation, 0);
        checkError(gl2, "Dispose Composite Renderer");
    }
    
    private void updateTransferFunction(GL2 gl2) {
        BufferedImage transferImage = new BufferedImage(TransferFunction.TEXTURE_SIZE, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = (Graphics2D) transferImage.getGraphics();
        g2d.setPaint(displayable.getTransferFunction().getPaint());
        g2d.drawLine(0, 0, TransferFunction.TEXTURE_SIZE, 0);
        g2d.dispose();
        
        byte[] dataElements = (byte[]) transferImage.getRaster().getDataElements(0, 0, TransferFunction.TEXTURE_SIZE, 1, null);
        gl2.glActiveTexture(GL2.GL_TEXTURE0 + TEXTURE_TRANSFER);
        gl2.glBindTexture(GL2.GL_TEXTURE_1D, textureLocation[0]);
        gl2.glTexImage1D(GL2.GL_TEXTURE_1D, 0, GL2.GL_RGBA, TransferFunction.TEXTURE_SIZE, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_INT_8_8_8_8_REV, ByteBuffer.wrap(dataElements));
        
        checkError(gl2, "Update transfer function");
    }

    @Override
    public void pointCountChanged() {
        dirtyValues |= TRANSFER_FUNCTION_DIRTY;
        repaint();
    }

    @Override
    public void pointValueChanged() {
        dirtyValues |= TRANSFER_FUNCTION_DIRTY;
        repaint();
    }

    @Override
    public void lightValueChanged() {
        dirtyValues |= LIGHT_DIRTY;
        repaint();
    }

    @Override
    public void rotationValueChanged() {
        super.rotationValueChanged();
        dirtyValues |= NORMAL_DIRTY;
        normal.updateMatrices(camera, rotation, tempMatrix);
    }

    @Override
    public void viewValueChanged() {
        super.viewValueChanged();
        dirtyValues |= NORMAL_DIRTY;
        normal.updateMatrices(camera, rotation, tempMatrix);
    }

}