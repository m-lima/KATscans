package no.uib.inf252.katscan.model;

import javax.swing.tree.MutableTreeNode;

/**
 *
 * @author Marcelo Lima
 */
public class Project extends KatNode {
    
    public Project() {
        setParent(null);
        setName("New project");
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public void insert(MutableTreeNode child, int index) {
        if (child instanceof DataFile) {
            super.insert(child, index);
        } else {
            throw new IllegalArgumentException("Can only add " + DataFile.class.getSimpleName() + " nodes to Project nodes.");
        }
    }

    @Override
    public DataFile getChildAt(int childIndex) {
        return (DataFile) super.getChildAt(childIndex);
    }

    @Override
    public void setParent(MutableTreeNode newParent) {
        if (newParent == null) {
            return;
        }
        throw new IllegalArgumentException(getClass().getSimpleName() + " nodes may not have parents.");
    }

}
