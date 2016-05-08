package no.uib.inf252.katscan.util;

import com.jogamp.opengl.math.FloatUtil;
import no.uib.inf252.katscan.model.Camera;
import no.uib.inf252.katscan.model.Rotation;

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
