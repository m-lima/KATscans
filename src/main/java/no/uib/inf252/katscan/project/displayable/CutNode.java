package no.uib.inf252.katscan.project.displayable;

import java.io.Serializable;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import no.uib.inf252.katscan.data.VoxelMatrix;
import no.uib.inf252.katscan.event.CutListener;
import no.uib.inf252.katscan.project.KatNode;
import no.uib.inf252.katscan.model.TransferFunction;

/**
 *
 * @author Marcelo Lima
 */
public class CutNode extends SubGroup implements Serializable, CutListener {

    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private int minZ;
    private int maxZ;
    
    private VoxelMatrix cutMatrix;

    public CutNode() {
        super("Cut");
    }

    @Override
    protected KatNode internalCopy() {
        return new CutNode();
    }
    
    @Override
    public VoxelMatrix getMatrix() {
        if (cutMatrix == null) {
            VoxelMatrix matrix = getParent().getMatrix();
            cutMatrix = new VoxelMatrix(matrix, (int) (matrix.getSizeX() * 0.25), (int) (matrix.getSizeX() * 0.75),
                                                (int) (matrix.getSizeY() * 0.25), (int) (matrix.getSizeY() * 0.75),
                                                (int) (matrix.getSizeZ() * 0.25), (int) (matrix.getSizeZ() * 0.75));
                                        
        }
        return cutMatrix;
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

    @Override
    protected void updateData() {
        cutMatrix = null;
    }

    @Override
    public void cutUpdated(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
    }
    
}
