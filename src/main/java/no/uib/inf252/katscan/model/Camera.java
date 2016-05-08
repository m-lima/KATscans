package no.uib.inf252.katscan.model;

import com.jogamp.opengl.math.FloatUtil;
import java.awt.EventQueue;
import java.io.Serializable;
import no.uib.inf252.katscan.event.CameraListener;

/**
 *
 * @author Marcelo Lima
 */
public class Camera extends KatModel<Camera> implements Serializable {

    private static final float[] UP_VECTOR = new float[]{0f, 1f, 0f};

    private final float[] eyePosition;
    private final float[] targetPosition;
    
    private final float[] viewMatrix;
    private final float[] tempMatrix;
    private boolean reuseView;
    
    private float initialZoom;

    public Camera() {
        eyePosition = new float[] {0f, 0f, 5f};
        targetPosition = new float[] {0f, 0f, -50f};
        
        viewMatrix = new float[16];
        tempMatrix = new float[16];
        reuseView = false;
        
        this.initialZoom = 5f;
    }

    @Override
    protected Camera newInstance() {
        return new Camera();
    }

    @Override
    public void assimilate(Camera camera) {
        System.arraycopy(camera.eyePosition, 0, this.eyePosition, 0, eyePosition.length);
        System.arraycopy(camera.targetPosition, 0, this.targetPosition, 0, targetPosition.length);
        
        System.arraycopy(camera.viewMatrix, 0, this.viewMatrix, 0, viewMatrix.length);
        this.reuseView = camera.reuseView;
        
        fireZoomValueChanged();
        fireViewValueChanged();
        fireRepaint();
    }

    @Override
    public void reset() {
        eyePosition[0] = 0f;
        eyePosition[1] = 0f;
        eyePosition[2] = initialZoom;
        targetPosition[0] = 0f;
        targetPosition[1] = 0f;
        targetPosition[2] = -50f;
        
        reuseView = false;
        
        fireViewValueChanged();
        fireRepaint();
    }

    public void setInitialZoom(float initialZoom) {
        this.initialZoom = initialZoom;
    }

    public float[] getViewMatrix() {
        if (reuseView) {
            return viewMatrix;
        } else {
            reuseView = true;
            return FloatUtil.makeLookAt(viewMatrix, 0, eyePosition, 0, targetPosition, 0, UP_VECTOR, 0, tempMatrix);
        }
    }

    public float[] getEyePosition() {
        return eyePosition;
    }

    public float getZoom() {
        return eyePosition[2];
    }

    public float getInitialZoom() {
        return initialZoom;
    }
    
    public void changeZoom(float delta) {
        eyePosition[2] += delta;
        
        fireZoomValueChanged();
        fireViewValueChanged();
        fireRepaint();
    }
    
    public void setEyePosition(float x, float y) {
        eyePosition[0] = x;
        targetPosition[0] = x;
        
        eyePosition[1] = y;
        targetPosition[1] = y;
        
        fireViewValueChanged();
        fireRepaint();
    }
    
    private void fireViewValueChanged() {
        reuseView = false;
        CameraListener[] listeners = listenerList.getListeners(CameraListener.class);

        for (final CameraListener listener : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.viewValueChanged();
                }
            });
        }
    }
    
    private void fireZoomValueChanged() {
        reuseView = false;
        CameraListener[] listeners = listenerList.getListeners(CameraListener.class);

        for (final CameraListener listener : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.zoomValueChanged();
                }
            });
        }
    }
    
}
