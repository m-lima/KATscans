package com.mflima.katscans.model;

import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Quaternion;
import java.awt.EventQueue;
import java.io.Serializable;
import com.mflima.katscans.event.KatModelListener;
import com.mflima.katscans.event.RotationListener;

/**
 *
 * @author Marcelo Lima
 */
public class Rotation extends KatModel<Rotation> implements Serializable {
    
    private final float[] modelMatrix;    
    
    //TODO persist
    private transient Quaternion currentRotation;

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

        if (this.currentRotation == null) {
            currentRotation = new Quaternion();
        }
        this.currentRotation.set(katModel.currentRotation);
        this.reuseModel = katModel.reuseModel;

        fireRotationChanged();
    }

    public synchronized float[] getModelMatrix() {
        if (reuseModel) {
            return modelMatrix;
        } else {
            reuseModel = true;
            if (currentRotation == null) {
                currentRotation = new Quaternion();
            }
            return currentRotation.toMatrix(modelMatrix, 0);
        }
    }
    
    public void rotate(Quaternion reference, float angle, float[] axis) {
        if (currentRotation == null) {
            currentRotation = new Quaternion();
        }
        currentRotation.set(reference);
        currentRotation.rotateByAngleNormalAxis(angle, axis[0], axis[1], axis[2]);
        fireRotationChanged();
    }

    public Quaternion getCurrentRotation() {
        if (currentRotation == null) {
            currentRotation = new Quaternion();
        }
        return currentRotation;
    }
    
    public void top() {
        if (currentRotation == null) {
            currentRotation = new Quaternion();
        } else {
            currentRotation.setIdentity();
        }
        currentRotation.rotateByAngleX(FloatUtil.HALF_PI);
        fireRotationChanged();
    }
    
    public void bottom() {
        if (currentRotation == null) {
            currentRotation = new Quaternion();
        } else {
            currentRotation.setIdentity();
        }
        currentRotation.rotateByAngleX(-FloatUtil.HALF_PI);
        fireRotationChanged();
    }
    
    public void front() {
        if (currentRotation == null) {
            currentRotation = new Quaternion();
        } else {
            currentRotation.setIdentity();
        }
        fireRotationChanged();
    }
    
    public void back() {
        if (currentRotation == null) {
            currentRotation = new Quaternion();
        } else {
            currentRotation.setIdentity();
        }
        currentRotation.rotateByAngleX(FloatUtil.PI);
        currentRotation.rotateByAngleZ(FloatUtil.PI);
        fireRotationChanged();
    }
    
    public void right() {
        if (currentRotation == null) {
            currentRotation = new Quaternion();
        } else {
            currentRotation.setIdentity();
        }
        currentRotation.rotateByAngleY(-FloatUtil.HALF_PI);
        fireRotationChanged();
    }
    
    public void left() {
        if (currentRotation == null) {
            currentRotation = new Quaternion();
        } else {
            currentRotation.setIdentity();
        }
        currentRotation.rotateByAngleY(+FloatUtil.HALF_PI);
        fireRotationChanged();
    }
    
    private void fireRotationChanged() {
        reuseModel = false;
        KatModelListener[] listeners = listenerList.getListeners(KatModelListener.class);

        for (final KatModelListener listener : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ((RotationListener)listener).rotationValueChanged();
                }
            });
        }
    }
    
}
