package no.uib.inf252.katscan.model;

import no.uib.inf252.katscan.model.displayable.Displayable;
import java.io.File;
import java.io.Serializable;
import javax.swing.tree.MutableTreeNode;
import no.uib.inf252.katscan.data.LoadedData;
import no.uib.inf252.katscan.data.VoxelMatrix;

/**
 *
 * @author Marcelo Lima
 */
public class DataFile extends Displayable implements Serializable {
    
    private File file;

    public DataFile(File file) {
        super(file.getName());
        setUserObject(file);
    }

    public void setUserObject(File file) {
        if (file == null || !file.isFile()) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " nodes cannot be build with invalid file paths.");
        }
        
        setName(file.getName());
        this.file = file;
    }
    
    public void setUserObject(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " nodes cannot be build with invalid file paths.");
        }
        
        setUserObject(new File(path));
    }

    @Override
    public void setUserObject(Object object) {
        if (object == null) {
            throw new NullPointerException("The " + getClass().getSimpleName() + " object cannot be null.");
        }
        
        if (object instanceof File) {
            setUserObject((File) object);
        } else {
            setUserObject(object.toString());
        }
    }

    @Override
    public VoxelMatrix getMatrix() {
        return LoadedData.getInstance().getDataset(getName());
    }
    
//    @Override
//    public short[] getData() {
//        VoxelMatrix dataset = LoadedData.getInstance().getDataset(getName());
//        return dataset.getData();
//    }

    @Override
    public int[] getHistogram() {
        VoxelMatrix dataset = LoadedData.getInstance().getDataset(getName());
        return dataset.getHistogram();
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public Project getParent() {
        return (Project) super.getParent();
    }

    @Override
    public void setParent(MutableTreeNode newParent) {
        if (newParent instanceof Project) {
            super.setParent(newParent);
        } else {
            throw new IllegalArgumentException("Can only have " + Project.class.getSimpleName() + " nodes as parents of " + getClass().getSimpleName() + " nodes.");
        }
    }

    @Override
    public void insert(MutableTreeNode child, int index) {
        if (child instanceof Project || child instanceof DataFile) {
            throw new IllegalArgumentException("Cannot add " + getClass().getSimpleName() + " nodes or " + Project.class.getSimpleName() + "nodes to " + getClass().getSimpleName() + " nodes.");
        }
        super.insert(child, index);
    }
    
}
