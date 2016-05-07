package no.uib.inf252.katscan.project;

import java.io.File;
import java.io.Serializable;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.tree.MutableTreeNode;
import no.uib.inf252.katscan.model.VoxelMatrix;
import no.uib.inf252.katscan.project.displayable.Displayable;
import no.uib.inf252.katscan.model.TransferFunction;

/**
 *
 * @author Marcelo Lima
 */
public class DataFileNode extends Displayable implements Serializable {
    
    private final File file;
    private final transient VoxelMatrix matrix;
    private final transient TransferFunction transferFunction;
    
    public DataFileNode(String name, File file, VoxelMatrix matrix) {
        super(name);
        this.file = file;
        this.matrix = matrix;
        transferFunction = new TransferFunction(TransferFunction.Type.SLOPE);
    }

    @Override
    protected DataFileNode internalCopy() {
        return new DataFileNode(getName(), new File(file.getAbsolutePath()), matrix.copy());
    }

    @Override
    public VoxelMatrix getMatrix() {
        return matrix;
    }

    @Override
    public TransferFunction getTransferFunction() {
        return transferFunction;
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public ProjectNode getParent() {
        return (ProjectNode) super.getParent();
    }

    @Override
    public void setParent(MutableTreeNode newParent) {
        if (newParent instanceof ProjectNode) {
            super.setParent(newParent);
        } else {
            throw new IllegalArgumentException("Can only have " + ProjectNode.class.getSimpleName() + " nodes as parents of " + getClass().getSimpleName() + " nodes.");
        }
    }

    @Override
    public void insert(MutableTreeNode child, int index) {
        if (child instanceof ProjectNode || child instanceof DataFileNode) {
            throw new IllegalArgumentException("Cannot add " + getClass().getSimpleName() + " nodes or " + ProjectNode.class.getSimpleName() + "nodes to " + getClass().getSimpleName() + " nodes.");
        }
        super.insert(child, index);
    }

    @Override
    public ImageIcon getIcon() {
        return new ImageIcon(getClass().getResource("/icons/tree/file.png"));
    }

    @Override
    protected JMenuItem[] getExtraMenus() {
        return null;
    }
    
}
