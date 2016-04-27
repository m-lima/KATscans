package no.uib.inf252.katscan.project.displayable;

import java.io.Serializable;
import no.uib.inf252.katscan.data.VoxelMatrix;

/**
 *
 * @author Marcelo Lima
 */
public class Structure extends SubGroup implements Serializable {

    public Structure() {
        super("Structure");
    }

//    @Override
//    public short[] getData() {
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

}
