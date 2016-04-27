package no.uib.inf252.katscan.project.displayable;

import javax.swing.tree.MutableTreeNode;
import no.uib.inf252.katscan.project.DataFileNode;
import no.uib.inf252.katscan.project.ProjectNode;

/**
 *
 * @author Marcelo Lima
 */
public abstract class SubGroup extends Displayable {

    public SubGroup(String name) {
        super(name);
    }

    @Override
    public Displayable getParent() {
        return (Displayable) super.getParent();
    }

    @Override
    public void setParent(MutableTreeNode newParent) {
        if (newParent instanceof Displayable) {
            super.setParent(newParent);
        } else {
            throw new IllegalArgumentException("Can only have " + Displayable.class.getSimpleName() + " nodes as parents of " + getClass().getSimpleName() + " nodes.");
        }
    }

    @Override
    public void insert(MutableTreeNode child, int index) {
        if (child instanceof ProjectNode || child instanceof DataFileNode) {
            throw new IllegalArgumentException("Cannot add " + DataFileNode.class.getSimpleName() + " nodes or " + ProjectNode.class.getSimpleName() + "nodes to " + getClass().getSimpleName() + " nodes.");
        }
        super.insert(child, index);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

}
