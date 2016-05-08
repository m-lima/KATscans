package no.uib.inf252.katscan.model;

import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Quaternion;
import java.awt.EventQueue;
import java.io.Serializable;
import no.uib.inf252.katscan.event.RotationListener;

/**
 *
 * @author Marcelo Lima
 */
public class Rotation extends KatModel<Rotation> implements Serializable {
    
    private final float[] modelMatrix;    
    private final Quaternion currentRotation;

    private boolean reuseModel;

    public Rotation() {
        modelMatrix = new float[16];
        currentRotation = new Quaternion();

        reuseModel = false;
    }
    
    @Override
    protected Rotation newInstance() {
        return new Rotation();
    }

    @Override
    public void assimilate(Rotation katModel) {
        System.arraycopy(katModel.modelMatrix, 0, this.modelMatrix, 0, modelMatrix.length);

        this.currentRotation.set(katModel.currentRotation);
        this.reuseModel = katModel.reuseModel;

        fireRotationChanged();
        fireRepaint();
    }

    public synchronized float[] getModelMatrix() {
        if (reuseModel) {
            return modelMatrix;
        } else {
            reuseModel = true;
            return currentRotation.toMatrix(modelMatrix, 0);
        }
    }
    
    public void rotate(Quaternion reference, float angle, float[] axis) {
        currentRotation.set(reference);
        currentRotation.rotateByAngleNormalAxis(angle, axis[0], axis[1], axis[2]);
        fireRotationChanged();
        fireRepaint();
    }

    public Quaternion getCurrentRotation() {
        return currentRotation;
    }
    
    public void top() {
        currentRotation.setIdentity();
        currentRotation.rotateByAngleX(FloatUtil.HALF_PI);
        fireRotationChanged();
        fireRepaint();
    }
    
    public void bottom() {
        currentRotation.setIdentity();
        currentRotation.rotateByAngleX(-FloatUtil.HALF_PI);
        fireRotationChanged();
        fireRepaint();
    }
    
    public void front() {
        currentRotation.setIdentity();
        fireRotationChanged();
        fireRepaint();
    }
    
    public void back() {
        currentRotation.setIdentity();
        currentRotation.rotateByAngleX(FloatUtil.PI);
        currentRotation.rotateByAngleZ(FloatUtil.PI);
        fireRotationChanged();
        fireRepaint();
    }
    
    public void right() {
        currentRotation.setIdentity();
        currentRotation.rotateByAngleY(-FloatUtil.HALF_PI);
        fireRotationChanged();
        fireRepaint();
    }
    
    public void left() {
        currentRotation.setIdentity();
        currentRotation.rotateByAngleY(+FloatUtil.HALF_PI);
        fireRotationChanged();
        fireRepaint();
    }
    
    private void fireRotationChanged() {
        reuseModel = false;
        RotationListener[] listeners = listenerList.getListeners(RotationListener.class);

        for (final RotationListener listener : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.rotationValueChanged();
                }
            });
        }
    }
    
}
