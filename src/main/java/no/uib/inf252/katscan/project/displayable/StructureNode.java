package no.uib.inf252.katscan.project.displayable;

import java.io.Serializable;
import javax.swing.JMenuItem;
import no.uib.inf252.katscan.data.VoxelMatrix;
import no.uib.inf252.katscan.util.TransferFunction;

/**
 *
 * @author Marcelo Lima
 */
public class StructureNode extends SubGroup implements Serializable {

    public StructureNode() {
        super("Structure");
    }

    @Override
    public VoxelMatrix getMatrix() {
        return getParent().getMatrix();
    }

    @Override
    public int[] getHistogram() {
        return getParent().getHistogram();
    }

    @Override
    public TransferFunction getTransferFunction() {
        return getParent().getTransferFunction();
    }

    @Override
    protected JMenuItem[] getExtraMenus() {
        return null;
    }

}