package com.mflima.katscans.util;

import com.jogamp.opengl.math.FloatUtil;
import com.mflima.katscans.model.Camera;
import com.mflima.katscans.model.Rotation;

/**
 *
 * @author Marcelo Lima
 */
public class Normal {
    
    private final float[] normalMatrix;

    public Normal() {
        normalMatrix = new float[16];
    }
    
    public float[] getNormalMatrix() {
        return normalMatrix;
    }
    
    public void updateMatrices(Camera camera, Rotation rotation, float[] tempMatrix) {
        FloatUtil.multMatrix(camera.getViewMatrix(), rotation.getModelMatrix(), normalMatrix);
        FloatUtil.invertMatrix(normalMatrix, normalMatrix);
        FloatUtil.transposeMatrix(normalMatrix, tempMatrix);
        MatrixUtil.getMatrix3(tempMatrix, normalMatrix);
    }

}
