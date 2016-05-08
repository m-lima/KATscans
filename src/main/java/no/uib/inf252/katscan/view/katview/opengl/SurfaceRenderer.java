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
public class SurfaceRenderer extends VolumeRenderer implements MouseMotionListener, MouseListener, TransferFunctionListener, LightListener {
    
    private static final int LIGHT_DIRTY = 1 << 0;
    private static final int NORMAL_DIRTY = 1 << 1;
    private static final int COLOR_DIRTY = 1 << 2;
    private static final int THRESHOLD_HI_DIRTY = 1 << 3;
    private static final int THRESHOLD_LO_DIRTY = 1 << 4;
    private static final int TRACKED_FLAGS =
            LIGHT_DIRTY |
            NORMAL_DIRTY |
            COLOR_DIRTY |
            THRESHOLD_HI_DIRTY |
            THRESHOLD_LO_DIRTY;

    private static final String PROPERTY_THRESHOLD_LO = "Threshold Low";
    private static final String PROPERTY_THRESHOLD_HI = "Threshold High";
    
    private static final int TEXTURE_COLOR_LOCAL = 0;
    private static final int TEXTURE_COLOR = TEXTURE_COUNT_PARENT + TEXTURE_COLOR_LOCAL;
    
    private final int[] colorLocation = new int[1];
    private static byte[] colors;
    
    private float thresholdLo;
    private float thresholdHi;
//    private boolean thresholdLoDirty;
//    private boolean thresholdHiDirty;
//    private boolean colorsDirty;
    
    private int lastY;
    
    private int dirtyValues;
    private final Normal normal;
    private final Light light;
    
    public SurfaceRenderer(Displayable displayable) throws GLException {
        super(displayable, "surfCaster", 1f);
        
        updateColors();
        
        addMouseListener(this);
        addMouseMotionListener(this);
        thresholdLo = 0.2f;
        thresholdHi = 0.5f;
        
        light = displayable.getLight();
        normal = new Normal();
    }

    @Override
    public boolean isIlluminated() {
        return true;
    }

    @Override
    public void createStructure(int x, int y, float threshold) {
        
    }

    @Override
    protected void preDraw(GLAutoDrawable drawable) {
        GL2 gl2 = drawable.getGL().getGL2();
        
        if ((dirtyValues & (TRACKED_FLAGS)) > 0) {
            int uniformLocation;
            if ((dirtyValues & THRESHOLD_LO_DIRTY) > 0) {
                int location = gl2.glGetUniformLocation(mainProgram, "thresholdLo");
                gl2.glUniform1f(location, thresholdLo);
            }

            if ((dirtyValues & THRESHOLD_HI_DIRTY) > 0) {
                int location = gl2.glGetUniformLocation(mainProgram, "thresholdHi");
                gl2.glUniform1f(location, thresholdHi);
            }

            if ((dirtyValues & COLOR_DIRTY) > 0) {
                gl2.glActiveTexture(GL2.GL_TEXTURE0 + TEXTURE_COLOR);
                gl2.glBindTexture(GL2.GL_TEXTURE_1D, colorLocation[0]);
                gl2.glTexImage1D(GL2.GL_TEXTURE_1D, 0, GL2.GL_RGBA, TransferFunction.TEXTURE_SIZE, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_INT_8_8_8_8_REV, ByteBuffer.wrap(colors));
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
        dirtyValues = TRACKED_FLAGS;
        normal.updateMatrices(camera, rotation, tempMatrix);
        
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
        dirtyValues |= COLOR_DIRTY;
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
                
                dirtyValues |= THRESHOLD_LO_DIRTY;
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
                
                dirtyValues |= THRESHOLD_HI_DIRTY;
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
                
                dirtyValues |= THRESHOLD_LO_DIRTY | THRESHOLD_HI_DIRTY;
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
            dirtyValues |= THRESHOLD_LO_DIRTY;
        }
        
        newThreshold = (Float) properties.get(PROPERTY_THRESHOLD_HI);
        if (newThreshold != null) {
            this.thresholdHi = newThreshold;
            dirtyValues |= THRESHOLD_HI_DIRTY;
        }
        
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