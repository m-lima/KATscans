package no.uib.inf252.katscan.view.katview.opengl;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import javax.swing.SwingUtilities;
import no.uib.inf252.katscan.project.displayable.Displayable;
import no.uib.inf252.katscan.util.TransferFunction;

/**
 *
 * @author Marcelo Lima
 */
public class SurfaceRenderer extends VolumeRenderer implements MouseMotionListener, MouseListener {

    private static final int TEXTURE_COLOR_LOCAL = 0;
    private static final int TEXTURE_COLOR = TEXTURE_COUNT_PARENT + TEXTURE_COLOR_LOCAL;
    
    private final int[] colorLocation = new int[1];
    private static byte[] colors;
    
    private float threshold;
    private boolean thresholdDirty;
    
    private int lastY;
    
    public SurfaceRenderer(Displayable displayable) throws GLException {
        super(displayable, "surfaceCaster");
        
        initializeColors();
        
        addMouseListener(this);
        addMouseMotionListener(this);
        threshold = 0.2f;
    }

    @Override
    protected void preDraw(GLAutoDrawable drawable) {
        if (thresholdDirty) {
            GL2 gl2 = drawable.getGL().getGL2();
            gl2.glUseProgram(programName);
            int location = gl2.glGetUniformLocation(programName, "threshold");
            gl2.glUniform1f(location, threshold);
            
            thresholdDirty = false;
            checkError(gl2, "Inject threshold");
        }
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        super.init(drawable); 
        thresholdDirty = true;
        
        GL2 gl2 = drawable.getGL().getGL2();
        
        gl2.glGenTextures(1, colorLocation, TEXTURE_COLOR_LOCAL);
        gl2.glActiveTexture(GL2.GL_TEXTURE0 + TEXTURE_COLOR);
        gl2.glBindTexture(GL2.GL_TEXTURE_1D, colorLocation[0]);
        gl2.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
        gl2.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
        gl2.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_BORDER);
        
        gl2.glTexImage1D(GL2.GL_TEXTURE_1D, 0, GL2.GL_RGBA, TransferFunction.TEXTURE_SIZE, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_INT_8_8_8_8_REV, ByteBuffer.wrap(colors));
        
        gl2.glUseProgram(programName);
            
        int location = gl2.glGetUniformLocation(programName, "colors");
        gl2.glUniform1i(location, TEXTURE_COLOR);
        
        checkError(gl2, "Inject colors");
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        super.dispose(drawable);
        GL2 gl2 = drawable.getGL().getGL2();
        
        gl2.glDeleteTextures(colorLocation.length, colorLocation, 0);
        checkError(gl2, "Dispose Surface Renderer");
    }
    
    private synchronized void initializeColors() {
        BufferedImage colorImage = new BufferedImage(2048, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = (Graphics2D) colorImage.getGraphics();
        
//        g2d.setPaint(new LinearGradientPaint(0f, 0f, 2048f, 0f,
//                new float[] {0f, 1200f / 4096f, 1250f / 4096f, 1300f / 4096f, 2048f / 4096f, 1f},
//                new Color[] {new Color(255, 220, 180), new Color(255, 220, 180), Color.RED, new Color(255, 220, 180), Color.WHITE, Color.BLUE}));
        g2d.setPaint(displayable.getTransferFunction().getPaint());
        
        g2d.drawLine(0, 0, 2048, 0);
        g2d.dispose();
        colors = (byte[]) colorImage.getRaster().getDataElements(0, 0, 2048, 1, null);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        final int modifiers = e.getModifiers();
        if ((modifiers & ~(MouseEvent.CTRL_MASK | MouseEvent.BUTTON1_MASK)) > 0) {
            return;
        }
        
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (e.isControlDown()) {
                float deltaY = (e.getY() - lastY) / 10000f;
                lastY = e.getY();
                
                if (deltaY < 0f && threshold == 0f) {
                    return;
                }
                
                if (deltaY > 0f && threshold == 1f) {
                    return;
                }
                
                threshold += deltaY;
                if (threshold < 0f) {
                    threshold = 0f;
                } else if (threshold > 1f) {
                    threshold = 1f;
                }
                
                thresholdDirty = true;
                repaint();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        lastY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

}