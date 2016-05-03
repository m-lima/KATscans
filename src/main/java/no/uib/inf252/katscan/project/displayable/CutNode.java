package no.uib.inf252.katscan.project.displayable;

import java.io.Serializable;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import no.uib.inf252.katscan.data.VoxelMatrix;
import no.uib.inf252.katscan.data.io.LoadSaveOptions;
import no.uib.inf252.katscan.event.CutListener;
import no.uib.inf252.katscan.util.TransferFunction;

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
        cutMatrix = new VoxelMatrix(getParent().getMatrix());
    }
    
    @Override
    public VoxelMatrix getMatrix() {
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

    //TODO Implement cut
    @Override
    public void cutUpdated(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
    }
    
}
