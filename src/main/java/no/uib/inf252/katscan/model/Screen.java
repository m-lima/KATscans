package no.uib.inf252.katscan.model;

import com.jogamp.opengl.math.FloatUtil;
import java.awt.EventQueue;
import no.uib.inf252.katscan.event.ScreenListener;

/**
 *
 * @author Marcelo Lima
 */
public class Screen extends KatModel<Screen> {
    
    private final float[] projectionMatrix;

    private boolean orthographic;
    private float fov;
    private float stepFactor;

    public Screen() {
        projectionMatrix = new float[16];

        orthographic = false;
        fov = FloatUtil.QUARTER_PI;
        stepFactor = 1f;
    }

    @Override
    protected Screen newInstance() {
        return new Screen();
    }

    @Override
    public void assimilate(Screen katModel) {
        orthographic = katModel.orthographic;
        fov = katModel.fov;
        stepFactor = katModel.stepFactor;
        
        fireOrthographicValueChanged();
        fireRepaint();
    }
    
    @Override
    public void reset() {
        orthographic = false;
        fov = FloatUtil.QUARTER_PI;
        stepFactor = 1f;
        
        fireOrthographicValueChanged();
        fireStepValueChanged();
        fireRepaint();
    }

    public boolean isOrthographic() {
        return orthographic;
    }

    public float getStepFactor() {
        return stepFactor;
    }

    public float[] getProjectionMatrix() {
        return projectionMatrix;
    }
    
    public void changeStepValue(float delta) {
        if (delta < 0 && stepFactor == 1f) {
            return;
        }

        stepFactor += delta;

        if (stepFactor < 1f) {
            stepFactor = 1f;
        }
        
        fireStepValueChanged();
        fireRepaint();
    }
    
    public void toggleOrthographic() {
        orthographic = !orthographic;
        fireOrthographicValueChanged();
        fireRepaint();
    }

    public void changeFOV(float delta, Camera camera, float width, float height) {
        if (delta < 0) {
            if (fov == 0.5f) {
                return;
            }
        } else {
            if (fov == FloatUtil.PI) {
                return;
            }
        }
        
        fov += delta;
        if (fov > FloatUtil.PI) {
            fov = FloatUtil.PI;
        } else if (fov < 0.5f) {
            fov = 0.5f;
        }
        
        updateProjection(camera, width, height);
    }
    
    public void updateProjection(Camera camera, float width, float height) {
        width /= height;
        if (orthographic) {
            float top = FloatUtil.tan(fov / 2f) * camera.getZoom();
            float bottom = -1.0f * top;
            float left = width * bottom;
            float right = width * top;
            FloatUtil.makeOrtho(projectionMatrix, 0, true, left, right, bottom, top, 0.1f, 50f);
        } else {
            FloatUtil.makePerspective(projectionMatrix, 0, true, fov, width, 0.1f, 50f);
        }
        
        fireProjectionValueChanged();
        fireRepaint();
    }
    
    private void fireOrthographicValueChanged() {
        ScreenListener[] listeners = listenerList.getListeners(ScreenListener.class);

        for (final ScreenListener listener : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.orthographicValueChanged();
                }
            });
        }
    }
    
    private void fireProjectionValueChanged() {
        ScreenListener[] listeners = listenerList.getListeners(ScreenListener.class);

        for (final ScreenListener listener : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.projectionValueChanged();
                }
            });
        }
    }
    
    private void fireStepValueChanged() {
        ScreenListener[] listeners = listenerList.getListeners(ScreenListener.class);

        for (final ScreenListener listener : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.stepValueChanged();
                }
            });
        }
    }
    
}
