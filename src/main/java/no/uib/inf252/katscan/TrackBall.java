package no.uib.inf252.katscan;

import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.math.VectorUtil;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;

/**
 *
 * @author Marcelo Lima
 */
public class TrackBall implements MouseListener, MouseMotionListener, MouseWheelListener {
    
    private float zoom;
    private final float[] initialPosition;
    private final float[] currentPosition;
    private final float[] axis;
    private final Quaternion initialRotation;
    private final Quaternion currentRotation;
    private final float[] rotationMatrix;

    public TrackBall() {
        zoom = 0.5f;
        initialPosition = new float[3];
        currentPosition = new float[3];
        axis = new float[3];
        initialRotation = new Quaternion();
        currentRotation = new Quaternion();
        rotationMatrix = new float[16];
        initialRotation.toMatrix(rotationMatrix, 0);
    }

    public Quaternion getCurrentRotation() {
        return currentRotation;
    }
    
    public float[] getCurrentRotationMatrix() {
        return rotationMatrix;
    }

    public float getZoom() {
        return zoom;
    }
    
    private void getSurfaceVector(MouseEvent e, double width, double height, float[] point) {
//        float radius = (float) Math.sqrt(width * width + height * height);
        point[0] = e.getX();
        point[1] = e.getY();
        
        point[0] -= width;
        point[1] -= height;
        
//        point[0] /= radius;
//        point[1] /= -radius;
        
        point[0] /= width;
        point[1] /= -height;
        
        float length = point[0] * point[0] + point[1] * point[1];
        if (length >= 1f) {
            point[2] = 0f;
        } else {
            point[2] = (float) -Math.sqrt(1f - length);
        }
        
        VectorUtil.normalizeVec3(point);
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Component component = e.getComponent();
        if (e.getButton() == MouseEvent.BUTTON3) {
            currentRotation.setIdentity();
            currentRotation.toMatrix(rotationMatrix, 0);
            component.repaint();
        } else {
            initialRotation.set(currentRotation);
            double width = component.getWidth() / 2.0;
            double height = component.getHeight() / 2.0;
            getSurfaceVector(e, width, height, initialPosition);
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
        double width = component.getWidth() / 2.0;
        double height = component.getHeight() / 2.0;
        getSurfaceVector(e, width, height, currentPosition);
        
        VectorUtil.crossVec3(axis, initialPosition, currentPosition);
        VectorUtil.normalizeVec3(axis);
        float angle = (float) Math.acos(VectorUtil.dotVec3(initialPosition, currentPosition));
        
        currentRotation.set(initialRotation);
        currentRotation.rotateByAngleNormalAxis(angle, axis[0], axis[1], axis[2]);
        currentRotation.toMatrix(rotationMatrix, 0);
        component.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        zoom += e.getWheelRotation() / 100f;
        e.getComponent().repaint();
    }
    
}
