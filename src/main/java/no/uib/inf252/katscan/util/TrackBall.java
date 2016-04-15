package no.uib.inf252.katscan.util;

import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.math.VectorUtil;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
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
    
    private static final float[] UP_VECTOR = new float[] {0f, 1f, 0f};
    
    private final float[] eyePosition;
    private final float[] targetPosition;
    
    private final float[] initialPosition;
    private final float[] currentPosition;
    private final float[] axis;
    private final Quaternion initialRotation;
    private final Quaternion currentRotation;
    
    private int xPos;
    private int yPos;
    private float yPosOld;
    private float xPosOld;

    private final float[] tempMatrix;
    private final float[] tempMatrix2;
    
    private final float[] projection;
    private boolean orthographic;
    private float fov;
    
    private int dirtyValues;
    
    public TrackBall() {
        eyePosition = new float[] {0f, 0f, 5f};
        targetPosition = new float[] {0f, 0f, 0f};
        initialPosition = new float[3];
        currentPosition = new float[3];
        axis = new float[3];
        initialRotation = new Quaternion();
        currentRotation = new Quaternion();
        
        tempMatrix = new float[16];
        tempMatrix2 = new float[16];
        
        projection = new float[16];
        orthographic = false;
        fov = FloatUtil.QUARTER_PI;
        
        dirtyValues = MODEL_DIRTY | VIEW_DIRTY | PROJECTION_DIRTY | ZOOM_DIRTY | ORTHO_DIRTY | FOV_DIRTY;
    }
    
    public void markAllDirty() {
        dirtyValues = MODEL_DIRTY | VIEW_DIRTY | PROJECTION_DIRTY | ZOOM_DIRTY | ORTHO_DIRTY | FOV_DIRTY;
    }

    public Quaternion getCurrentRotation() {
        return currentRotation;
    }
    
    public float[] getModelMatrix() {
//        float[] toMatrix = currentRotation.toMatrix(tempMatrix, 0);
//        Matrix4 m = new Matrix4();
//        m.translate(3, 3, 0);
//        m.multMatrix(toMatrix);
//        return m.getMatrix();
        return currentRotation.toMatrix(tempMatrix, 0);
    }
    
    public float[] getModelMatrix3() {
        int k = 0;
        currentRotation.toMatrix(tempMatrix, 0);
        for (int i = 0; i < 11; i++) {
            if ((i + 1) % 4 == 0) {
                continue;
            }
            tempMatrix2[k++] = tempMatrix[i];
        }
        return tempMatrix2;
    }

    public float[] getViewMatrix() {
        return FloatUtil.makeLookAt(tempMatrix, 0, eyePosition, 0, targetPosition, 0, UP_VECTOR, 0, tempMatrix2);
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
    
    public void clearDirtyValues() {
        clearDirtyValues(MODEL_DIRTY | VIEW_DIRTY | PROJECTION_DIRTY | ZOOM_DIRTY | ORTHO_DIRTY | FOV_DIRTY);
    }
    
    public void clearDirtyValues(int values) {
        if (values < 0 || values > (MODEL_DIRTY | VIEW_DIRTY | PROJECTION_DIRTY | ZOOM_DIRTY | ORTHO_DIRTY | FOV_DIRTY)) {
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
            FloatUtil.makeOrtho(projection, 0, true, left, right, bottom, top, 0.1f, 100f);
        } else {
            FloatUtil.makePerspective(projection, 0, true, fov, aspect, 0.1f, 100f);
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
    
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            xPos = e.getX();
            yPos = e.getY();
            xPosOld = eyePosition[0];
            yPosOld = eyePosition[1];
        } else {
            Component component = e.getComponent();
            if (SwingUtilities.isMiddleMouseButton(e)) {
                currentRotation.setIdentity();
                eyePosition[0] = 0f;
                eyePosition[1] = 0f;
                eyePosition[2] = 5f;
                targetPosition[0] = 0f;
                targetPosition[1] = 0f;
                targetPosition[2] = 0f;
        
                dirtyValues |= VIEW_DIRTY | MODEL_DIRTY | ZOOM_DIRTY;
        
                component.repaint();
            } else if (SwingUtilities.isLeftMouseButton(e)) {
                initialRotation.set(currentRotation);
                getSurfaceVector(e.getX(), e.getY(), component.getWidth(), component.getHeight(), initialPosition);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Component component = e.getComponent();
        if (SwingUtilities.isLeftMouseButton(e)) {
            getSurfaceVector(e.getX(), e.getY(), component.getWidth(), component.getHeight(), currentPosition);

            float angle = FloatUtil.acos(VectorUtil.dotVec3(initialPosition, currentPosition));
            
            VectorUtil.crossVec3(axis, initialPosition, currentPosition);
            VectorUtil.normalizeVec3(axis);
            initialRotation.toMatrix(tempMatrix, 0);
            initialRotation.conjugate().rotateVector(axis, 0, axis, 0);
            initialRotation.setFromMatrix(tempMatrix, 0);

            currentRotation.set(initialRotation);
            currentRotation.rotateByAngleNormalAxis(angle, axis[0], axis[1], axis[2]);
            
            dirtyValues |= MODEL_DIRTY;
        } else if (SwingUtilities.isRightMouseButton(e)){
            eyePosition[0] = xPosOld + (xPos - e.getX()) * (eyePosition[2] - 1f) / component.getWidth();
            targetPosition[0] = eyePosition[0];
            
            eyePosition[1] = yPosOld - (yPos - e.getY()) * (eyePosition[2] - 1f) / component.getHeight();
            targetPosition[1] = eyePosition[1];
            
            dirtyValues |= VIEW_DIRTY;
        }
        component.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        Component component = e.getComponent();
        if (e.isShiftDown()) {
            fov += e.getWheelRotation() * FloatUtil.PI / 180f;
            
            if (fov > FloatUtil.PI) {
                fov = FloatUtil.PI;
            } else if (fov < 1f) {
                fov = 1f;
            }
            
            updateProjection(component.getWidth(), component.getHeight());
        } else {
            eyePosition[2] += e.getWheelRotation() / 4f;
            if (orthographic) {
                updateProjection(component.getWidth(), component.getHeight());
            }
            
            dirtyValues |= ZOOM_DIRTY | VIEW_DIRTY;
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
            orthographic = !orthographic;
            Component component = e.getComponent();
            updateProjection(component.getWidth(), component.getHeight());
            dirtyValues |= ORTHO_DIRTY;
            
            component.repaint();
        }
    }
    
}
