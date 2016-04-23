package no.uib.inf252.katscan.view.opengl;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import no.uib.inf252.katscan.event.TransferFunctionListener;
import no.uib.inf252.katscan.model.displayable.Displayable;
import no.uib.inf252.katscan.util.TransferFunction;

/**
 *
 * @author Marcelo Lima
 */
public class CompositeRenderer extends VolumeRenderer implements TransferFunctionListener {
    
    private final TransferFunction transferFunction;
    private final int[] textureLocation = new int[1];
    private boolean transferFunctionDirty;
    
    public CompositeRenderer(Displayable displayable, TransferFunction transferFunction) throws GLException {
        super(displayable, "raycaster");
        this.transferFunction = transferFunction;
        this.transferFunction.addTransferFunctionListener(this);
    }

    @Override
    protected void preDraw(GLAutoDrawable drawable) {
        if (transferFunctionDirty) {
            updateTransferFunction(drawable.getGL().getGL4());
            transferFunctionDirty = false;
        }
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        super.init(drawable); 
        
        GL4 gl4 = drawable.getGL().getGL4();
        
        gl4.glGenTextures(1, textureLocation, 0);
        gl4.glBindTexture(GL4.GL_TEXTURE_1D, textureLocation[0]);
        gl4.glTexParameteri(GL4.GL_TEXTURE_1D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
        gl4.glTexParameteri(GL4.GL_TEXTURE_1D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
        gl4.glTexParameteri(GL4.GL_TEXTURE_1D, GL4.GL_TEXTURE_WRAP_R, GL4.GL_CLAMP_TO_BORDER);
        transferFunctionDirty = true;
        
        checkError(gl4, "Create Transfer Function");
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        super.dispose(drawable);
        GL4 gl4 = drawable.getGL().getGL4();
        
        gl4.glDeleteTextures(0, textureLocation, 0);
    }
    
    private void updateTransferFunction(GL4 gl4) {
        BufferedImage transferImage = new BufferedImage(1024, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = (Graphics2D) transferImage.getGraphics();
        g2d.setPaint(transferFunction.getPaint(transferImage.getWidth()));
        g2d.drawLine(0, 0, transferImage.getWidth(), 0);
        g2d.dispose();
        
        byte[] dataElements = (byte[]) transferImage.getRaster().getDataElements(0, 0, transferImage.getWidth(), 1, null);
        gl4.glTexImage1D(GL4.GL_TEXTURE_1D, 0, GL4.GL_RGBA, transferImage.getWidth(), 0, GL4.GL_RGBA, GL4.GL_UNSIGNED_BYTE, ByteBuffer.wrap(dataElements));
    }

    @Override
    public void pointCountChanged() {
        transferFunctionDirty = true;
    }

    @Override
    public void pointValueChanged() {
        transferFunctionDirty = true;
    }
    
}