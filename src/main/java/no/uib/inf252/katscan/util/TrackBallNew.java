package no.uib.inf252.katscan.util;

import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.math.VectorUtil;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
import no.uib.inf252.katscan.model.Cut;
import no.uib.inf252.katscan.model.Light;
import no.uib.inf252.katscan.model.Rotation;
import no.uib.inf252.katscan.project.displayable.Displayable;
import no.uib.inf252.katscan.view.katview.opengl.VolumeRenderer;

/**
 *
 * @author Marcelo Lima
 */
public class TrackBallNew implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, FocusListener {

//    public static final int MODEL_DIRTY = 1 << 0;
//    public static final int VIEW_DIRTY = 1 << 1;
//    public static final int PROJECTION_DIRTY = 1 << 2;
//    public static final int ZOOM_DIRTY = 1 << 3;
//    public static final int ORTHO_DIRTY = 1 << 4;
//    public static final int FOV_DIRTY = 1 << 5;
//    public static final int SLICE_DIRTY = 1 << 6;
//    public static final int LIGHT_DIRTY = 1 << 7;
//    public static final int MIN_DIRTY = 1 << 8;
//    public static final int MAX_DIRTY = 1 << 9;
//    public static final int STEP_DIRTY = 1 << 10;
    
    private static final float CUT_RATIO = -0.001f;

    private static final float[] UP_VECTOR = new float[]{0f, 1f, 0f};

    private final float[] eyePosition;
    private final float[] targetPosition;
    transient private final float[] initialLightPosition;

    transient private final float[] initialPosition;
    transient private final float[] currentPosition;
    transient private final float[] axis;

    transient private final Quaternion initialRotation;

    transient private int xPos;
    transient private int yPos;
    transient private float yPosOld;
    transient private float xPosOld;

    transient private final float[] tempMatrix;

    private final float[] viewMatrix;
    private final float[] projectionMatrix;
    private final float[] normalMatrix;

    private boolean reuseView;
    private boolean reuseNormal;

    private boolean orthographic;
    private float fov;
    private float initialZoom;
    private float slice;

//    private int dirtyValues;

    transient private boolean xDown;
    transient private boolean yDown;
    transient private boolean zDown;

    private float stepFactor;

    transient private JPopupMenu popupMenu;
    transient private JMenuItem menuOrtho;
    
    transient private Displayable displayable;
    transient private Cut cut;
    transient private Light light;
    transient private Rotation rotation;

    public TrackBallNew(float initialZoom) {
        eyePosition = new float[]{0f, 0f, initialZoom};
        targetPosition = new float[]{0f, 0f, -50f};
        initialLightPosition = VectorUtil.normalizeVec3(new float[]{-2f, 2f, 5f});
        
        initialPosition = new float[3];
        currentPosition = new float[3];
        axis = new float[3];
        initialRotation = new Quaternion();

        tempMatrix = new float[16];

        viewMatrix = new float[16];
        projectionMatrix = new float[16];
        normalMatrix = new float[16];

        orthographic = false;
        fov = FloatUtil.QUARTER_PI;
        slice = 0f;
        this.initialZoom = initialZoom;
        stepFactor = 1f;

        reuseView = false;
        reuseNormal = false;
        
        markAllDirty();
    }
    
    private TrackBallNew(TrackBallNew trackBall) {
        this(trackBall.initialZoom);
        System.arraycopy(trackBall.eyePosition, 0, this.eyePosition, 0, eyePosition.length);
        System.arraycopy(trackBall.targetPosition, 0, this.targetPosition, 0, targetPosition.length);

        System.arraycopy(trackBall.viewMatrix, 0, this.viewMatrix, 0, viewMatrix.length);
        System.arraycopy(trackBall.projectionMatrix, 0, this.projectionMatrix, 0, projectionMatrix.length);
        System.arraycopy(trackBall.normalMatrix, 0, this.normalMatrix, 0, normalMatrix.length);

        this.orthographic = trackBall.orthographic;
        this.fov = trackBall.fov;
        this.initialZoom = trackBall.initialZoom;
        this.slice = trackBall.slice;
        this.stepFactor = trackBall.stepFactor;

        this.reuseView = trackBall.reuseView;
        if (!trackBall.reuseNormal) {
            updateMatrices();
        }

        markAllDirty();
    }
    
    //TODO Remove "assimilate"
    public void assimilate(TrackBallNew trackBall) {
        System.arraycopy(trackBall.eyePosition, 0, this.eyePosition, 0, eyePosition.length);
        System.arraycopy(trackBall.targetPosition, 0, this.targetPosition, 0, targetPosition.length);

        System.arraycopy(trackBall.viewMatrix, 0, this.viewMatrix, 0, viewMatrix.length);
        System.arraycopy(trackBall.projectionMatrix, 0, this.projectionMatrix, 0, projectionMatrix.length);
        System.arraycopy(trackBall.normalMatrix, 0, this.normalMatrix, 0, normalMatrix.length);

        this.orthographic = trackBall.orthographic;
        this.fov = trackBall.fov;
        this.initialZoom = trackBall.initialZoom;
        this.slice = trackBall.slice;
        this.stepFactor = trackBall.stepFactor;

        this.reuseView = trackBall.reuseView;
        if (!trackBall.reuseNormal) {
            updateMatrices();
        }

        markAllDirty();
    }
    
    public TrackBallNew copy() {
        return new TrackBallNew(this);
    }

    public void installTrackBall(Component component) {
        component.addMouseListener(this);
        component.addMouseMotionListener(this);
        component.addMouseWheelListener(this);
        component.addKeyListener(this);
        component.addFocusListener(this);
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
        return projectionMatrix;
    }

    public float[] getNormalMatrix() {
        if (reuseNormal) {
            return normalMatrix;
        } else {
            reuseNormal = true;
            FloatUtil.multMatrix(getViewMatrix(), rotation.getModelMatrix(), normalMatrix);
            FloatUtil.invertMatrix(normalMatrix, normalMatrix);
            FloatUtil.transposeMatrix(normalMatrix, tempMatrix);
            MatrixUtil.getMatrix3(tempMatrix, normalMatrix);
            return normalMatrix;
        }
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

    public float getSlice() {
        return slice;
    }

    public float getStepFactor() {
        return stepFactor;
    }

    public void markAllDirty() {
        dirtyValues = getAllDirtyFlags();
        reuseView = false;
    }

    public void clearDirtyValues() {
        clearDirtyValues(getAllDirtyFlags());
    }

    public void clearDirtyValues(int values) {
        if (values < 0 || values > getAllDirtyFlags()) {
            throw new IllegalArgumentException("Invalid flags: " + Integer.toBinaryString(values) + "(" + values + ")");
        }

        dirtyValues &= ~values;
    }

    public void updateProjection(int width, int height) {
        float aspect = width;
        aspect /= height;
        if (orthographic) {
            float top = FloatUtil.tan(fov / 2f) * eyePosition[2];
            float bottom = -1.0f * top;
            float left = aspect * bottom;
            float right = aspect * top;
            FloatUtil.makeOrtho(projectionMatrix, 0, true, left, right, bottom, top, 0.1f, 50f);
        } else {
            FloatUtil.makePerspective(projectionMatrix, 0, true, fov, aspect, 0.1f, 50f);
        }

        dirtyValues |= PROJECTION_DIRTY;
    }

    private void updateMatrices() {
        reuseNormal = false;
        getNormalMatrix();
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

    private void buildPopup(final VolumeRenderer owner) {
        popupMenu = new JPopupMenu();
        final JMenuItem top = new JMenuItem("Top", new ImageIcon(getClass().getResource("/icons/top.png")));
        final JMenuItem bottom = new JMenuItem("Bottom", new ImageIcon(getClass().getResource("/icons/bottom.png")));
        final JMenuItem front = new JMenuItem("Front", new ImageIcon(getClass().getResource("/icons/front.png")));
        final JMenuItem back = new JMenuItem("Back", new ImageIcon(getClass().getResource("/icons/back.png")));
        final JMenuItem right = new JMenuItem("Right", new ImageIcon(getClass().getResource("/icons/right.png")));
        final JMenuItem left = new JMenuItem("Left", new ImageIcon(getClass().getResource("/icons/left.png")));
        final JMenuItem reset = new JMenuItem("Reset", new ImageIcon(getClass().getResource("/icons/reset.png")));
        if (orthographic) {
            menuOrtho = new JMenuItem("Perspective", new ImageIcon(getClass().getResource("/icons/perspective.png")));
        } else {
            menuOrtho = new JMenuItem("Orthographic", new ImageIcon(getClass().getResource("/icons/ortho.png")));
        }
        final JMenuItem structure = new JMenuItem("Create structure", new ImageIcon(getClass().getResource("/icons/tree/structure.png")));

        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == top) {
                    rotation.top();
                } else if (e.getSource() == bottom) {
                    rotation.bottom();
                } else if (e.getSource() == back) {
                    rotation.back();
                } else if (e.getSource() == right) {
                    rotation.right();
                } else if (e.getSource() == left) {
                    rotation.left();
                } else if (e.getSource() == reset) {
                    rotation.reset();

                    eyePosition[0] = 0f;
                    eyePosition[1] = 0f;
                    eyePosition[2] = initialZoom;

                    light.reset();
                    
                    cut.reset();

                    targetPosition[0] = 0f;
                    targetPosition[1] = 0f;
                    targetPosition[2] = -50f;

                    fov = FloatUtil.QUARTER_PI;
                    slice = 0f;
                    updateProjection(owner.getWidth(), owner.getHeight());

                    markAllDirty();
                    reuseView = false;
                    updateMatrices();
                } else if (e.getSource() == menuOrtho) {
                    toggleOrthographic(owner);
                    return;
                } else if (e.getSource() == structure) {
                    owner.createStructure(popupMenu.getX(), popupMenu.getY(), 1f);
                }
                updateMatrices();
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
        popupMenu.addSeparator();
        popupMenu.add(structure);
    }
    
    private void setValue(float[] vector, float delta, int index, float min, float max) {
        float value = vector[index];
        if (delta > 0) {
            if (value == max) {
                return;
            }
            
            value += delta;
            if (value > max) {
                value = max;
            }
        } else {
            if (value == min) {
                return;
            }
            
            value += delta;
            if (value < min) {
                value = min;
            }
        }
        
        vector[index] = value;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            if (popupMenu == null) {
                buildPopup((VolumeRenderer) e.getComponent());
            }
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        final int modifiers = e.getModifiersEx();

        if ((modifiers & ~(MouseEvent.SHIFT_DOWN_MASK
                | MouseEvent.ALT_DOWN_MASK
                | MouseEvent.BUTTON1_DOWN_MASK
                | MouseEvent.BUTTON2_DOWN_MASK
                | MouseEvent.BUTTON3_DOWN_MASK)) > 0) {
            return;
        }

        xPos = e.getX();
        yPos = e.getY();
        if (SwingUtilities.isRightMouseButton(e)) {
            xPosOld = eyePosition[0];
            yPosOld = eyePosition[1];
        } else if (SwingUtilities.isMiddleMouseButton(e)) {
            yPos = e.getY();
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            Component component = e.getComponent();
            getSurfaceVector(e.getX(), e.getY(), component.getWidth(), component.getHeight(), initialPosition);
            initialRotation.set(rotation.getCurrentRotation());
            System.arraycopy(light.getLightPosition(), 0, initialLightPosition, 0, initialLightPosition.length);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        e.getComponent().requestFocusInWindow();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        final int modifiers = e.getModifiersEx();

        if ((modifiers & ~(MouseEvent.SHIFT_DOWN_MASK
                | MouseEvent.ALT_DOWN_MASK
                | MouseEvent.BUTTON1_DOWN_MASK
                | MouseEvent.BUTTON2_DOWN_MASK
                | MouseEvent.BUTTON3_DOWN_MASK)) > 0) {
            return;
        }
        
        if (e.isShiftDown() && (modifiers & MouseEvent.ALT_DOWN_MASK) > 0) {
            return;
        }
        
        VolumeRenderer renderer = (VolumeRenderer) e.getComponent();

        if (SwingUtilities.isLeftMouseButton(e)) {
            if (e.isAltDown()) {
                float deltaY = (e.getY() - yPos) * 0.025f;
                
                if (deltaY < 1f && stepFactor == 1f) {
                    return;
                }
                
                stepFactor += deltaY;
                
                if (stepFactor < 1f) {
                    stepFactor = 1f;
                }
                
                yPos = e.getY();
                dirtyValues |= STEP_DIRTY;                
            } else {
                getSurfaceVector(e.getX(), e.getY(), renderer.getWidth(), renderer.getHeight(), currentPosition);

                float angle = FloatUtil.acos(VectorUtil.dotVec3(initialPosition, currentPosition));
                VectorUtil.crossVec3(axis, initialPosition, currentPosition);
                VectorUtil.normalizeVec3(axis);

                if (e.isShiftDown()) {
                    if (!renderer.isIlluminated()) {
                        return;
                    }
                    
                    System.arraycopy(initialLightPosition, 0, tempMatrix, 0, initialLightPosition.length);
                    initialRotation.setIdentity().rotateByAngleNormalAxis(angle, axis[0], axis[1], axis[2]);
                    initialRotation.rotateVector(tempMatrix, 0, tempMatrix, 0);
                    light.setLightPostion(tempMatrix);
                } else if (xDown || yDown || zDown) {
                    float deltaY = (e.getY() - yPos) * CUT_RATIO;
                    if (xDown) {
                        cut.changeMinX(deltaY, false);
                    }

                    if (yDown) {
                        cut.changeMinY(deltaY, false);
                    }

                    if (zDown) {
                        cut.changeMinZ(deltaY, false);
                    }

                    yPos = e.getY();
                } else {
                    initialRotation.toMatrix(tempMatrix, 0);
                    initialRotation.conjugate().rotateVector(axis, 0, axis, 0);
                    initialRotation.setFromMatrix(tempMatrix, 0);

                    rotation.rotate(initialRotation, angle, axis);

                    updateMatrices();
                }
            }
        } else if (SwingUtilities.isMiddleMouseButton(e)) {
            float deltaY = e.getY() - yPos;

            if ((modifiers & MouseEvent.ALT_DOWN_MASK) > 0) {
                fov += deltaY * FloatUtil.PI / (180f * 16f);

                if (fov > FloatUtil.PI) {
                    fov = FloatUtil.PI;
                } else if (fov < 0.5f) {
                    fov = 0.5f;
                }

                updateProjection(renderer.getWidth(), renderer.getHeight());
            } else if (e.isShiftDown()) {
                slice -= deltaY * 0.0025f;
                if (slice <= 0f) {
                    slice = 0f;
                }

                dirtyValues |= SLICE_DIRTY;
            } else if (xDown || yDown || zDown) {
                deltaY = (e.getY() - yPos) * CUT_RATIO;
                if (xDown) {
                    if (deltaY < 0) {
                        cut.changeMinX(deltaY, true);
                    } else {
                        cut.changeMaxX(deltaY, true);
                    }
                }

                if (yDown) {
                    if (deltaY < 0) {
                        cut.changeMinY(deltaY, true);
                    } else {
                        cut.changeMaxY(deltaY, true);
                    }
                }

                if (zDown) {
                    if (deltaY < 0) {
                        cut.changeMinZ(deltaY, true);
                    } else {
                        cut.changeMaxZ(deltaY, true);
                    }
                }
                
            } else {
                eyePosition[2] += deltaY / (4f * 16f);
                if (orthographic) {
                    updateProjection(renderer.getWidth(), renderer.getHeight());
                }

                slice += deltaY / (4f * 16f);

                dirtyValues |= ZOOM_DIRTY | VIEW_DIRTY | SLICE_DIRTY;
                reuseView = false;
                updateMatrices();
            }
            yPos = e.getY();

        } else if (SwingUtilities.isRightMouseButton(e)) {
            if (e.isShiftDown() || e.isAltDown()) {
                return;
            }
            
            if (xDown || yDown || zDown) {
                float deltaY = (e.getY() - yPos) * CUT_RATIO;
                if (xDown) {
                    cut.changeMaxX(deltaY, false);
                }

                if (yDown) {
                    cut.changeMaxY(deltaY, false);
                }

                if (zDown) {
                    cut.changeMaxZ(deltaY, false);
                }
                
                yPos = e.getY();
            } else {

                eyePosition[0] = xPosOld + (xPos - e.getX()) / (float) renderer.getWidth();
                targetPosition[0] = eyePosition[0];

                eyePosition[1] = yPosOld - (yPos - e.getY()) / (float) renderer.getHeight();
                targetPosition[1] = eyePosition[1];

                dirtyValues |= VIEW_DIRTY;
                reuseView = false;
                updateMatrices();
            }
        }
        renderer.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        final int modifiers = e.getModifiers();
        if ((modifiers & ~(MouseEvent.SHIFT_MASK | MouseEvent.ALT_MASK)) > 0) {
            return;
        }

        Component component = e.getComponent();
        component.requestFocus();
        if (e.isAltDown()) {
            fov += e.getWheelRotation() * FloatUtil.PI / 180f;

            if (fov > FloatUtil.PI) {
                fov = FloatUtil.PI;
            } else if (fov < 0.5f) {
                fov = 0.5f;
            }

            updateProjection(component.getWidth(), component.getHeight());
        } else if (e.isShiftDown()) {
            slice -= e.getWheelRotation() * 0.05f;
            if (slice <= 0f) {
                slice = 0f;
            }

            dirtyValues |= SLICE_DIRTY;
        } else {
            eyePosition[2] += e.getWheelRotation() / 4f;
            if (orthographic) {
                updateProjection(component.getWidth(), component.getHeight());
            }

            slice += e.getWheelRotation() / 4f;

            dirtyValues |= ZOOM_DIRTY | VIEW_DIRTY | SLICE_DIRTY;
            reuseView = false;
            updateMatrices();
        }

        component.repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_X) {
            xDown = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_Y) {
            yDown = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_Z) {
            zDown = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            toggleOrthographic(e.getComponent());
        }

        if (e.getKeyCode() == KeyEvent.VK_X) {
            xDown = false;
        }

        if (e.getKeyCode() == KeyEvent.VK_Y) {
            yDown = false;
        }

        if (e.getKeyCode() == KeyEvent.VK_Z) {
            zDown = false;
        }
    }

    private void toggleOrthographic(Component component) {
        orthographic = !orthographic;
        if (menuOrtho != null) {
            if (orthographic) {
                menuOrtho.setText("Perspective");
                menuOrtho.setIcon(new ImageIcon(getClass().getResource("/icons/perspective.png")));
            } else {
                menuOrtho.setText("Orthographic");
                menuOrtho.setIcon(new ImageIcon(getClass().getResource("/icons/ortho.png")));
            }
        }
        updateProjection(component.getWidth(), component.getHeight());
        dirtyValues |= ORTHO_DIRTY;
        component.repaint();
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
        xDown = false;
        yDown = false;
        zDown = false;
    }

}
