package no.uib.inf252.katscan.util;

import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.math.VectorUtil;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

/**
 *
 * @author Marcelo Lima
 */
public class TrackBall implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
    
    public static final int MODEL_DIRTY = 0b1;
    public static final int VIEW_DIRTY = 0b10;
    public static final int PROJECTION_DIRTY = 0b100;
    public static final int ZOOM_DIRTY = 0b1000;
    public static final int ORTHO_DIRTY = 0b10000;
    public static final int FOV_DIRTY = 0b100000;
    public static final int MOVEMENT_DIRTY = 0b1000000;
    
    private static final float[] UP_VECTOR = new float[] {0f, 1f, 0f};
    
    private final float[] eyePosition;
    private final float[] targetPosition;
    
    private final float[] initialPosition;
    private final float[] currentPosition;
    private final float[] axis;
    private final Quaternion initialRotation;
    private final Quaternion currentRotation;
    private final float[] translation;
    private boolean moving;
    
    private int xPos;
    private int yPos;
    private float yPosOld;
    private float xPosOld;

    private final float[] tempMatrix;
    
    private final float[] modelMatrix;
    private final float[] viewMatrix;
    private final float[] projection;
    
    private boolean reuseModel;
    private boolean reuseView;
    
    private boolean orthographic;
    private float fov;
    private final float initialZoom;
    
    private int dirtyValues;
    
    private JPopupMenu popupMenu;
    private JMenuItem menuOrtho;
    
    public TrackBall(float initialZoom) {
        eyePosition = new float[] {0f, 0f, initialZoom};
        targetPosition = new float[] {0f, 0f, 0f};
        initialPosition = new float[3];
        currentPosition = new float[3];
        axis = new float[3];
        initialRotation = new Quaternion();
        currentRotation = new Quaternion();
        translation = new float[] {0f, 0f, 0f};
        moving = false;
        
        tempMatrix = new float[16];
        
        modelMatrix = new float[16];
        viewMatrix = new float[16];
        projection = new float[16];
        
        orthographic = false;
        fov = FloatUtil.QUARTER_PI;
        this.initialZoom = initialZoom;
        
        markAllDirty();
    }
    
    public void installTrackBall(Component component) {
        component.addMouseListener(this);
        component.addMouseMotionListener(this);
        component.addMouseWheelListener(this);
        component.addKeyListener(this);
    }
    
    public void markAllDirty() {
        dirtyValues = MODEL_DIRTY | VIEW_DIRTY | PROJECTION_DIRTY | ZOOM_DIRTY | ORTHO_DIRTY | FOV_DIRTY | MOVEMENT_DIRTY;
        reuseModel = false;
        reuseView = false;
    }

    public Quaternion getCurrentRotation() {
        return currentRotation;
    }
    
    public float[] getModelMatrix() {
        if (reuseModel) {
            return modelMatrix;
        } else {
            reuseModel = true;
            FloatUtil.makeTranslation(modelMatrix, true, translation[0], translation[1], translation[2]);
            return FloatUtil.multMatrix(modelMatrix, currentRotation.toMatrix(tempMatrix, 0));
        }
    }
    
    public float[] getViewMatrix() {
        if (reuseView) {
            return viewMatrix;
        } else {
            reuseView = true;
            return FloatUtil.makeLookAt(viewMatrix, 0, eyePosition, 0, targetPosition, 0, UP_VECTOR, 0, tempMatrix);
        }
    }
    
    public float[] getProjectionMatrix() {
        return projection;
    }

    public float[] getEyePosition() {
        return eyePosition;
    }
    
    public float getZoom() {
        return eyePosition[2];
    }

    public boolean isOrthographic() {
        return orthographic;
    }

    public int getDirtyValues() {
        return dirtyValues;
    }

    public float getFOV() {
        return fov;
    }

    public boolean isMoving() {
        return moving;
    }
    
    public void clearDirtyValues() {
        clearDirtyValues(MODEL_DIRTY | VIEW_DIRTY | PROJECTION_DIRTY | ZOOM_DIRTY | ORTHO_DIRTY | FOV_DIRTY | MOVEMENT_DIRTY);
    }
    
    public void clearDirtyValues(int values) {
        if (values < 0 || values > (MODEL_DIRTY | VIEW_DIRTY | PROJECTION_DIRTY | ZOOM_DIRTY | ORTHO_DIRTY | FOV_DIRTY | MOVEMENT_DIRTY)) {
            throw new IllegalArgumentException("Invalid flags: " + Integer.toBinaryString(values) + "(" + values + ")");
        }
        
        dirtyValues &= ~values;
    }
    
    public void updateProjection(int width, int height) {
        float aspect = width;
        aspect /= height;
        if (orthographic) {
            float top = FloatUtil.tan(fov/2f) * eyePosition[2];
            float bottom =  -1.0f * top;
            float left   = aspect * bottom;
            float right  = aspect * top;
            FloatUtil.makeOrtho(projection, 0, true, left, right, bottom, top, 0.1f, 50f);
        } else {
            FloatUtil.makePerspective(projection, 0, true, fov, aspect, 0.1f, 50f);
        }
        
        dirtyValues |= PROJECTION_DIRTY;
    }
    
    private void getSurfaceVector(int x, int y, double width, double height, float[] point) {
        width /= 2d;
        height /= 2d;

        point[0] = x;
        point[1] = y;
        
        point[0] -= width;
        point[1] -= height;
        
        point[0] /= width;
        point[1] /= -height;
        
        float length = point[0] * point[0] + point[1] * point[1];
        if (length >= 1f) {
            point[2] = 0f;
        } else {
            point[2] = FloatUtil.sqrt(1f - length);
        }
        
        VectorUtil.normalizeVec3(point);
    }
    
    private void buildPopup(final Component owner) {
        popupMenu = new JPopupMenu();
        final JMenuItem top = new JMenuItem("Top", new ImageIcon(getClass().getResource("/icons/top.png")));
        final JMenuItem bottom = new JMenuItem("Bottom", new ImageIcon(getClass().getResource("/icons/bottom.png")));
        final JMenuItem front = new JMenuItem("Front", new ImageIcon(getClass().getResource("/icons/front.png")));
        final JMenuItem back = new JMenuItem("Back", new ImageIcon(getClass().getResource("/icons/back.png")));
        final JMenuItem right = new JMenuItem("Right", new ImageIcon(getClass().getResource("/icons/right.png")));
        final JMenuItem left = new JMenuItem("Left", new ImageIcon(getClass().getResource("/icons/left.png")));
        final JMenuItem reset = new JMenuItem("Reset");
        menuOrtho = new JMenuItem(orthographic ? "Perspective" : "Orthographic");
        
        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentRotation.setIdentity();
                if (e.getSource() == top) {
                    currentRotation.rotateByAngleX(+FloatUtil.HALF_PI);
                } else if (e.getSource() == bottom) {
                    currentRotation.rotateByAngleX(-FloatUtil.HALF_PI);
                } else if (e.getSource() == back) {
                    currentRotation.rotateByAngleZ(FloatUtil.PI);
                } else if (e.getSource() == right) {
                    currentRotation.rotateByAngleY(-FloatUtil.HALF_PI);
                } else if (e.getSource() == left) {
                    currentRotation.rotateByAngleY(+FloatUtil.HALF_PI);
                } else if (e.getSource() == reset) {
                    currentRotation.setIdentity();
                    eyePosition[0] = 0f;
                    eyePosition[1] = 0f;
                    eyePosition[2] = initialZoom;
                    targetPosition[0] = 0f;
                    targetPosition[1] = 0f;
                    targetPosition[2] = 0f;
                    moving = false;

                    dirtyValues |= VIEW_DIRTY | ZOOM_DIRTY;
                    reuseView = false;
                } else if (e.getSource() == menuOrtho) {
                    toggleOrthographic(owner);
                    return;
                }
                dirtyValues |= MODEL_DIRTY | MOVEMENT_DIRTY;
                reuseModel = false;
                owner.repaint();
            }
        };
        
        top.addActionListener(listener);
        bottom.addActionListener(listener);
        front.addActionListener(listener);
        back.addActionListener(listener);
        right.addActionListener(listener);
        left.addActionListener(listener);
        reset.addActionListener(listener);
        menuOrtho.addActionListener(listener);
        
        popupMenu.add(top);
        popupMenu.add(bottom);
        popupMenu.add(front);
        popupMenu.add(back);
        popupMenu.add(right);
        popupMenu.add(left);
        popupMenu.addSeparator();
        popupMenu.add(reset);
        popupMenu.addSeparator();
        popupMenu.add(menuOrtho);
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            if (popupMenu == null) {
                buildPopup(e.getComponent());
            }
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            xPos = e.getX();
            yPos = e.getY();
            xPosOld = eyePosition[0];
            yPosOld = eyePosition[1];
        } else if (SwingUtilities.isMiddleMouseButton(e)) {
            yPos = e.getY();
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            Component component = e.getComponent();
            if (SwingUtilities.isMiddleMouseButton(e)) {
            } else if (SwingUtilities.isLeftMouseButton(e)) {
                initialRotation.set(currentRotation);
                getSurfaceVector(e.getX(), e.getY(), component.getWidth(), component.getHeight(), initialPosition);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        e.getComponent().requestFocusInWindow();
        if (moving) {
            moving = false;
            dirtyValues |= MOVEMENT_DIRTY;
            e.getComponent().repaint();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        final int modifiers = e.getModifiers();
        
        if ((modifiers & ~(
                MouseEvent.SHIFT_MASK |
                MouseEvent.BUTTON1_MASK |
                MouseEvent.BUTTON2_MASK |
                MouseEvent.BUTTON3_MASK)) > 0) {
            return;
        }
        
        Component component = e.getComponent();
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (e.isShiftDown()) {
                return;
            }
            
            getSurfaceVector(e.getX(), e.getY(), component.getWidth(), component.getHeight(), currentPosition);

            float angle = FloatUtil.acos(VectorUtil.dotVec3(initialPosition, currentPosition));
            
            VectorUtil.crossVec3(axis, initialPosition, currentPosition);
            VectorUtil.normalizeVec3(axis);
            initialRotation.toMatrix(tempMatrix, 0);
            initialRotation.conjugate().rotateVector(axis, 0, axis, 0);
            initialRotation.setFromMatrix(tempMatrix, 0);

            currentRotation.set(initialRotation);
            currentRotation.rotateByAngleNormalAxis(angle, axis[0], axis[1], axis[2]);
            
            moving = true;
            
            dirtyValues |= MODEL_DIRTY | MOVEMENT_DIRTY;
            reuseModel = false;
        } else if (SwingUtilities.isMiddleMouseButton(e)){
            int deltaY = e.getY() - yPos;
            
            moving = true;
            dirtyValues |= MOVEMENT_DIRTY;
            
            if (e.isShiftDown()) {
                fov += deltaY * FloatUtil.PI / (180f * 16f);

                if (fov > FloatUtil.PI) {
                    fov = FloatUtil.PI;
                } else if (fov < 0.5f) {
                    fov = 0.5f;
                }

                updateProjection(component.getWidth(), component.getHeight());
            } else {
                eyePosition[2] += deltaY / (4f * 16f);
                if (orthographic) {
                    updateProjection(component.getWidth(), component.getHeight());
                }

                dirtyValues |= ZOOM_DIRTY | VIEW_DIRTY;
                reuseView = false;
            }
            yPos = e.getY();
            
        } else if (SwingUtilities.isRightMouseButton(e)){
            eyePosition[0] = xPosOld + (xPos - e.getX()) * (eyePosition[2] - 1f) / component.getWidth();
            targetPosition[0] = eyePosition[0];
            
            eyePosition[1] = yPosOld - (yPos - e.getY()) * (eyePosition[2] - 1f) / component.getHeight();
            targetPosition[1] = eyePosition[1];
            
            moving = true;
            
            dirtyValues |= VIEW_DIRTY | MOVEMENT_DIRTY;
            reuseView = false;
        }
        component.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        final int modifiers = e.getModifiers();
        if ((modifiers & ~(MouseEvent.SHIFT_MASK)) > 0) {
            return;
        }
        
        Component component = e.getComponent();
        component.requestFocus();
        if (e.isShiftDown()) {
            fov += e.getWheelRotation() * FloatUtil.PI / 180f;
            
            if (fov > FloatUtil.PI) {
                fov = FloatUtil.PI;
            } else if (fov < 0.5f) {
                fov = 0.5f;
            }
            
            updateProjection(component.getWidth(), component.getHeight());
        } else {
            eyePosition[2] += e.getWheelRotation() / 4f;
            if (orthographic) {
                updateProjection(component.getWidth(), component.getHeight());
            }
            
            dirtyValues |= ZOOM_DIRTY | VIEW_DIRTY;
            reuseView = false;
        }
        
        component.repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            toggleOrthographic(e.getComponent());
        }
    }
    
    private void toggleOrthographic(Component component) {
        orthographic = !orthographic;
        if (menuOrtho != null) {
            menuOrtho.setText(orthographic ? "Perspective" : "Orthographic");
        }
        updateProjection(component.getWidth(), component.getHeight());
        dirtyValues |= ORTHO_DIRTY;
        component.repaint();
    }
    
}
