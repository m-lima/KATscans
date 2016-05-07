package no.uib.inf252.katscan.project.displayable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.tree.MutableTreeNode;
import no.uib.inf252.katscan.Init;
import no.uib.inf252.katscan.data.io.LoadSaveFormat;
import no.uib.inf252.katscan.data.io.LoadSaveOptions;
import no.uib.inf252.katscan.model.VoxelMatrix;
import no.uib.inf252.katscan.model.TransferFunction;
import no.uib.inf252.katscan.project.ProjectNode;

/**
 *
 * @author Marcelo Lima
 */
public class DataFileNode extends Displayable implements Serializable {
    
    private final File file;
    private final LoadSaveFormat.Format format;
    private final LoadSaveOptions options;
    private final TransferFunction transferFunction;
    private transient VoxelMatrix matrix;
    
    public DataFileNode(String name, File file, LoadSaveFormat.Format format, LoadSaveOptions options, VoxelMatrix matrix) {
        super(name);
        this.file = file;
        this.matrix = matrix;
        this.format = format;
        this.options = options;
        transferFunction = new TransferFunction(TransferFunction.Type.SLOPE);
    }

    @Override
    protected DataFileNode internalCopy() {
        return new DataFileNode(getName(), new File(file.getAbsolutePath()), format, options, matrix.copy());
    }

    @Override
    public VoxelMatrix getMatrix() {
        if (matrix == null) {
            try {
                matrix = format.getFormat().loadData(new FileInputStream(file), options);
            } catch (IOException ex) {
                Logger.getLogger(DataFileNode.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(Init.getFrameReference(), "Could not load data", "Load", JOptionPane.ERROR_MESSAGE);
                remove();
            }
        }
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
