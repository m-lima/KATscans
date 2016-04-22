package no.uib.inf252.katscan.view.opengl;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLException;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

/**
 *
 * @author Marcelo Lima
 */
public class CompositeRenderer extends VolumeRenderer {
    
    private final BufferedImage transferFunction;
    private final int[] textureLocation = new int[1];
    
    public CompositeRenderer(String name) throws GLException {
        super(name, "raycaster");
        
        transferFunction = new BufferedImage(1024, 1, BufferedImage.TYPE_4BYTE_ABGR);
        int rgb = new Color(0, 255, 0, 64).getRGB();
        for (int i = 0; i < 5; i++) {
            transferFunction.setRGB(15 + i, 0, rgb);
        }
        rgb = new Color(255, 255, 255, 64).getRGB();
        for (int i = 60; i < 80; i++) {
            transferFunction.setRGB(i, 0, rgb);
        }
    }

    @Override
    protected void preDraw(GLAutoDrawable drawable) {
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
        
        byte[] dataElements = (byte[]) transferFunction.getRaster().getDataElements(0, 0, transferFunction.getWidth(), 1, null);
        gl4.glTexImage1D(GL4.GL_TEXTURE_1D, 0, GL4.GL_RGBA, transferFunction.getWidth(), 0, GL4.GL_RGBA, GL4.GL_UNSIGNED_BYTE, ByteBuffer.wrap(dataElements));
        checkError(gl4, "Create Transfer Function");
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        super.dispose(drawable);
        GL4 gl4 = drawable.getGL().getGL4();
        
        gl4.glDeleteTextures(0, textureLocation, 0);
    }
    
}