package no.uib.inf252.katscan.view.katview.opengl;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLException;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Map;
import javax.swing.SwingUtilities;
import no.uib.inf252.katscan.event.TransferFunctionListener;
import no.uib.inf252.katscan.project.displayable.Displayable;
import no.uib.inf252.katscan.project.displayable.StructureNode;
import no.uib.inf252.katscan.util.TransferFunction;

/**
 *
 * @author Marcelo Lima
 */
public class SurfaceRenderer extends VolumeRenderer implements MouseMotionListener, MouseListener, TransferFunctionListener {

    private static final String PROPERTY_THRESHOLD_LO = "Threshold Low";
    private static final String PROPERTY_THRESHOLD_HI = "Threshold High";
    
    private static final int TEXTURE_COLOR_LOCAL = 0;
    private static final int TEXTURE_COLOR = TEXTURE_COUNT_PARENT + TEXTURE_COLOR_LOCAL;
    
    private final int[] colorLocation = new int[1];
    private static byte[] colors;
    
    private float thresholdLo;
    private float thresholdHi;
    private boolean thresholdLoDirty;
    private boolean thresholdHiDirty;
    
    private int lastY;
    
    private boolean colorsDirty;
    
    public SurfaceRenderer(Displayable displayable) throws GLException {
        super(displayable, "surfaceCaster");
        
        updateColors();
        
        addMouseListener(this);
        addMouseMotionListener(this);
        displayable.getTransferFunction().addTransferFunctionListener(this);
        thresholdLo = 0.2f;
        thresholdHi = 0.5f;
    }

    @Override
    public boolean acceptsStructure() {
        return true;
    }

    @Override
    public void createStructure(int x, int y, float threshold) {
        super.createStructure(x, y, threshold); //To change body of generated methods, choose Tools | Templates.
        StructureNode structure = new StructureNode();
        
    }

    @Override
    protected void preDraw(GLAutoDrawable drawable) {
        GL2 gl2 = drawable.getGL().getGL2();
        if (thresholdLoDirty) {
            int location = gl2.glGetUniformLocation(mainProgram, "thresholdLo");
            gl2.glUniform1f(location, thresholdLo);
            
            thresholdLoDirty = false;
            checkError(gl2, "Inject low threshold");
        }
        
        if (thresholdHiDirty) {
            int location = gl2.glGetUniformLocation(mainProgram, "thresholdHi");
            gl2.glUniform1f(location, thresholdHi);
            
            thresholdHiDirty = false;
            checkError(gl2, "Inject high threshold");
        }
        
        if (colorsDirty) {
            gl2.glActiveTexture(GL2.GL_TEXTURE0 + TEXTURE_COLOR);
            gl2.glBindTexture(GL2.GL_TEXTURE_1D, colorLocation[0]);
            gl2.glTexImage1D(GL2.GL_TEXTURE_1D, 0, GL2.GL_RGBA, TransferFunction.TEXTURE_SIZE, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_INT_8_8_8_8_REV, ByteBuffer.wrap(colors));
        }
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        super.init(drawable); 
        thresholdLoDirty = true;
        thresholdHiDirty = true;
        
        GL2 gl2 = drawable.getGL().getGL2();
        
        gl2.glGenTextures(1, colorLocation, TEXTURE_COLOR_LOCAL);
        gl2.glActiveTexture(GL2.GL_TEXTURE0 + TEXTURE_COLOR);
        gl2.glBindTexture(GL2.GL_TEXTURE_1D, colorLocation[0]);
        gl2.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
        gl2.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
        gl2.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_BORDER);
        
        gl2.glTexImage1D(GL2.GL_TEXTURE_1D, 0, GL2.GL_RGBA, TransferFunction.TEXTURE_SIZE, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_INT_8_8_8_8_REV, ByteBuffer.wrap(colors));
        
        int location = gl2.glGetUniformLocation(mainProgram, "colors");
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
    
    private synchronized void updateColors() {
        BufferedImage colorImage = new BufferedImage(2048, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = (Graphics2D) colorImage.getGraphics();
        
        g2d.setPaint(displayable.getTransferFunction().getPaint());
        
        g2d.drawLine(0, 0, 2048, 0);
        g2d.dispose();
        colors = (byte[]) colorImage.getRaster().getDataElements(0, 0, 2048, 1, null);
        colorsDirty = true;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        final int modifiers = e.getModifiersEx();
        if ((modifiers & ~(MouseEvent.CTRL_DOWN_MASK |
                           MouseEvent.BUTTON1_DOWN_MASK |
                           MouseEvent.BUTTON2_DOWN_MASK |
                           MouseEvent.BUTTON3_DOWN_MASK)) > 0) {
            return;
        }
        
        if (e.isControlDown()) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                float deltaY = (e.getY() - lastY) / 10000f;
                lastY = e.getY();
                
                if (deltaY < 0f && thresholdLo == 0f) {
                    return;
                }
                
                if (deltaY > 0f && thresholdLo == thresholdHi) {
                    return;
                }
                
                thresholdLo += deltaY;
                if (thresholdLo < 0f) {
                    thresholdLo = 0f;
                } else if (thresholdLo > thresholdHi) {
                    thresholdLo = thresholdHi;
                }
                
                thresholdLoDirty = true;
                repaint();
            } else if (SwingUtilities.isRightMouseButton(e)) {
                float deltaY = (e.getY() - lastY) / -10000f;
                lastY = e.getY();
                
                if (deltaY < 0f && thresholdHi == thresholdLo) {
                    return;
                }
                
                if (deltaY > 0f && thresholdHi == 1f) {
                    return;
                }
                
                thresholdHi += deltaY;
                if (thresholdHi < thresholdLo) {
                    thresholdHi = thresholdLo;
                } else if (thresholdHi > 1f) {
                    thresholdHi = 1f;
                }
                
                thresholdHiDirty = true;
                repaint();
            } else if (SwingUtilities.isMiddleMouseButton(e)) {
                float deltaY = (e.getY() - lastY) / -10000f;
                lastY = e.getY();
                float diff = thresholdHi - thresholdLo;
                
                if (deltaY < 0f) {
                    if (thresholdLo == 0f) {
                        return;
                    }
                    
                    thresholdLo += deltaY;
                    if (thresholdLo < 0f) {
                        thresholdLo = 0f;
                    }
                    thresholdHi = thresholdLo + diff;
                } else {
                    if (thresholdHi == 1f) {
                        return;
                    }
                    
                    thresholdHi += deltaY;
                    if (thresholdHi > 1f) {
                        thresholdHi = 1f;
                    }
                    thresholdLo = thresholdHi - diff;
                }
                
                thresholdLoDirty = true;
                thresholdHiDirty = true;
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

    @Override
    public void pointCountChanged() {
        updateColors();
        repaint();
    }

    @Override
    public void pointValueChanged() {
        updateColors();
        repaint();
    }

    @Override
    public Map<String, Object> packProperties() {
        Map<String, Object> properties = super.packProperties();
        properties.put(PROPERTY_THRESHOLD_LO, thresholdLo);
        properties.put(PROPERTY_THRESHOLD_HI, thresholdHi);
        return properties;
    }

    @Override
    public void loadProperties(Map<String, Object> properties) {
        super.loadProperties(properties);
        if (properties == null || properties.isEmpty()) {
            return;
        }
        
        Float newThreshold = (Float) properties.get(PROPERTY_THRESHOLD_LO);
        if (newThreshold != null) {
            this.thresholdLo = newThreshold;
            thresholdLoDirty = true;
        }
        
        newThreshold = (Float) properties.get(PROPERTY_THRESHOLD_HI);
        if (newThreshold != null) {
            this.thresholdHi = newThreshold;
            thresholdHiDirty = true;
        }
        
        repaint();
    }

}