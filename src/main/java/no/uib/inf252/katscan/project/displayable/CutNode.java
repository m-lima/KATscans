package no.uib.inf252.katscan.project.displayable;

import java.io.Serializable;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import no.uib.inf252.katscan.data.VoxelMatrix;
import no.uib.inf252.katscan.util.TransferFunction;

/**
 *
 * @author Marcelo Lima
 */
public class CutNode extends SubGroup implements Serializable {

    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private int minZ;
    private int maxZ;

    public CutNode() {
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
    public TransferFunction getTransferFunction() {
        return getParent().getTransferFunction();
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public ImageIcon getIcon() {
        return new ImageIcon(getClass().getResource("/icons/tree/cut.png"));
    }

    @Override
    protected JMenuItem[] getExtraMenus() {
        return null;
    }
    
}
