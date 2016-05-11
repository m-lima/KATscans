package com.mflima.katscans.util;

import com.mflima.katscans.model.Screen;
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
import com.mflima.katscans.model.Camera;
import com.mflima.katscans.model.Cut;
import com.mflima.katscans.model.Light;
import com.mflima.katscans.model.Rotation;
import com.mflima.katscans.project.displayable.Displayable;
import com.mflima.katscans.view.katview.opengl.VolumeRenderer;

/**
 *
 * @author Marcelo Lima
 */
public class KatViewHandler implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, FocusListener {

    private static final float CUT_RATIO = -0.001f;

    transient private final float[] initialLightPosition;

    transient private final float[] initialPosition;
    transient private final float[] currentPosition;
    transient private final float[] axis;

    transient private final Quaternion initialRotation;

    transient private int xPos;
    transient private int yPos;
    transient private float yPosOld;
    transient private float xPosOld;

    transient private boolean xDown;
    transient private boolean yDown;
    transient private boolean zDown;

    transient private JPopupMenu popupMenu;
    transient private JMenuItem menuOrtho;
    
    transient private final Cut cut;
    transient private final Light light;
    transient private final Rotation rotation;
    transient private final Camera camera;
    transient private final Screen screen;

    public KatViewHandler(VolumeRenderer renderer, Displayable displayable, Screen screen) {
        initialLightPosition = VectorUtil.normalizeVec3(new float[]{-2f, 2f, 5f});
        
        initialPosition = new float[3];
        currentPosition = new float[3];
        axis = new float[3];
        initialRotation = new Quaternion();
        
        cut = displayable.getCut();
        light = displayable.getLight();
        rotation = displayable.getRotation();
        camera = displayable.getCamera();
        this.screen = screen;
        
        renderer.addMouseListener(this);
        renderer.addMouseMotionListener(this);
        renderer.addMouseWheelListener(this);
        renderer.addKeyListener(this);
        renderer.addFocusListener(this);
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
        if (screen.isOrthographic()) {
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
                    camera.reset();
                    light.reset();
                    cut.reset();
                    screen.reset();
                } else if (e.getSource() == menuOrtho) {
                    toggleOrthographic();
                } else if (e.getSource() == structure) {
                    owner.createStructure(xPos, yPos, 1f);
                }
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
        structure.addActionListener(listener);

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
            xPos = e.getX();
            yPos = e.getY();
            popupMenu.show(e.getComponent(), xPos, yPos);
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
            float[] eyePosition = camera.getEyePosition();
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
                screen.changeStepValue((e.getY() - yPos) * 0.025f);
                yPos = e.getY();
            } else {
                getSurfaceVector(e.getX(), e.getY(), renderer.getWidth(), renderer.getHeight(), currentPosition);

                float angle = FloatUtil.acos(VectorUtil.dotVec3(initialPosition, currentPosition));
                VectorUtil.crossVec3(axis, initialPosition, currentPosition);
                VectorUtil.normalizeVec3(axis);

                if (e.isShiftDown()) {
                    if (!renderer.isIlluminated()) {
                        return;
                    }
                    
                    float[] tempMatrix = renderer.getTempMatrix();
                    
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
                    float[] tempMatrix = renderer.getTempMatrix();
                    
                    initialRotation.toMatrix(tempMatrix, 0);
                    initialRotation.conjugate().rotateVector(axis, 0, axis, 0);
                    initialRotation.setFromMatrix(tempMatrix, 0);

                    rotation.rotate(initialRotation, angle, axis);
                }
            }
        } else if (SwingUtilities.isMiddleMouseButton(e)) {
            float deltaY = e.getY() - yPos;

            if ((modifiers & MouseEvent.ALT_DOWN_MASK) > 0) {
                screen.changeFOV(deltaY * FloatUtil.PI / (180f * 16f), camera, renderer.getWidth(), renderer.getHeight());
            } else if (e.isShiftDown()) {
                cut.changeSlice(deltaY * -0.0025f);
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
                camera.changeZoom(deltaY / (4f * 16f));
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
                camera.setEyePosition(xPosOld + (xPos - e.getX()) / (float) renderer.getWidth(),  yPosOld - (yPos - e.getY()) / (float) renderer.getHeight());
            }
        }
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
        if (e.isAltDown()) {
            screen.changeFOV(e.getWheelRotation() * FloatUtil.PI / 180f, camera, component.getWidth(), component.getHeight());
        } else if (e.isShiftDown()) {
            cut.changeSlice(e.getWheelRotation() * -0.05f);
        } else {
            camera.changeZoom(e.getWheelRotation() / 4f);
        }
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
            toggleOrthographic();
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

    private void toggleOrthographic() {
        screen.toggleOrthographic();
        if (menuOrtho != null) {
            if (screen.isOrthographic()) {
                menuOrtho.setText("Perspective");
                menuOrtho.setIcon(new ImageIcon(getClass().getResource("/icons/perspective.png")));
            } else {
                menuOrtho.setText("Orthographic");
                menuOrtho.setIcon(new ImageIcon(getClass().getResource("/icons/ortho.png")));
            }
        }
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
