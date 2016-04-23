package no.uib.inf252.katscan.model.displayable;

import java.io.Serializable;
import no.uib.inf252.katscan.data.VoxelMatrix;

/**
 *
 * @author Marcelo Lima
 */
public class Cut extends SubGroup implements Serializable {

    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private int minZ;
    private int maxZ;

    public Cut() {
        super("Cut");
    }
    
//    @Override
//    public short[] getData() {
//        for (int i = minZ; i < maxZ; i++) {
//            for (int j = minY; j < maxY; j++) {
//                for (int k = minX; k < maxX; k++) {
//                }
//            }
//        }
//        return getParent().getData();
//    }

    @Override
    public VoxelMatrix getMatrix() {
        return getParent().getMatrix();
    }

    @Override
    public int[] getHistogram() {
        return getParent().getHistogram();
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }
    
}
