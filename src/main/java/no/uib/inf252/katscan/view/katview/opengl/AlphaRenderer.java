package no.uib.inf252.katscan.view.katview.opengl;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import no.uib.inf252.katscan.event.TransferFunctionListener;
import no.uib.inf252.katscan.project.displayable.TransferFunctionNode;
import no.uib.inf252.katscan.util.TransferFunction;

/**
 *
 * @author Marcelo Lima
 */
public class AlphaRenderer extends VolumeRenderer implements TransferFunctionListener {
    
    private final int[] textureLocation = new int[1];
    private boolean transferFunctionDirty;
    
    public AlphaRenderer(TransferFunctionNode displayable) throws GLException {
        super(displayable, "alphaCaster");
        //TODO Remove listener when done
        displayable.getTransferFunction().addTransferFunctionListener(this);
    }

    @Override
    protected void preDraw(GLAutoDrawable drawable) {
        if (transferFunctionDirty) {
            updateTransferFunction(drawable.getGL().getGL2());
            transferFunctionDirty = false;
        }
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        super.init(drawable); 
        
        GL2 gl2 = drawable.getGL().getGL2();
        
        gl2.glGenTextures(1, textureLocation, 0);
        gl2.glActiveTexture(GL2.GL_TEXTURE1);
        gl2.glBindTexture(GL2.GL_TEXTURE_1D, textureLocation[0]);
        gl2.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
        gl2.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
        gl2.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_BORDER);
        transferFunctionDirty = true;
        
        gl2.glUseProgram(programName);
            
        int location = gl2.glGetUniformLocation(programName, "transferFunction");
        gl2.glUniform1i(location, 1);
        
        checkError(gl2, "Create Transfer Function");
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        super.dispose(drawable);
        GL2 gl2 = drawable.getGL().getGL2();
        
        gl2.glDeleteTextures(1, textureLocation, 0);
    }
    
    private void updateTransferFunction(GL2 gl2) {
        BufferedImage transferImage = new BufferedImage(TransferFunction.TEXTURE_SIZE, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = (Graphics2D) transferImage.getGraphics();
        g2d.setPaint(getDisplayable().getTransferFunction().getPaint(0f, TransferFunction.TEXTURE_SIZE));
        g2d.drawLine(0, 0, TransferFunction.TEXTURE_SIZE, 0);
        g2d.dispose();
        
        byte[] dataElements = (byte[]) transferImage.getRaster().getDataElements(0, 0, TransferFunction.TEXTURE_SIZE, 1, null);
        gl2.glActiveTexture(GL2.GL_TEXTURE1);
        gl2.glBindTexture(GL2.GL_TEXTURE_1D, textureLocation[0]);
        gl2.glTexImage1D(GL2.GL_TEXTURE_1D, 0, GL2.GL_RGBA, TransferFunction.TEXTURE_SIZE, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_INT_8_8_8_8_REV, ByteBuffer.wrap(dataElements));
    }

    private TransferFunctionNode getDisplayable() {
        return (TransferFunctionNode) displayable;
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