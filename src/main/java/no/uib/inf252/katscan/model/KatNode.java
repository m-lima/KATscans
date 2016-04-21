package no.uib.inf252.katscan.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 *
 * @author Marcelo Lima
 */
public abstract class KatNode implements MutableTreeNode, Serializable {

    private KatNode parent;
    private String name;
    private ArrayList<KatNode> children;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("The " + getClass().getSimpleName() + " name cannot be null or empty");
        }
        this.name = name.trim();
    }

    @Override
    public KatNode getParent() {
        return parent;
    }

    @Override
    public void setParent(MutableTreeNode newParent) {
        this.parent = (KatNode) newParent;
    }

    @Override
    public void setUserObject(Object object) {
        if (object == null) {
            throw new NullPointerException("The " + getClass().getSimpleName() + " object cannot be null.");
        }
        String name = object.toString();
        setName(name);
    }

    @Override
    public KatNode getChildAt(int childIndex) {
        if (children == null) {
            return null;
        }
        return children.get(childIndex);
    }

    @Override
    public int getChildCount() {
        if (children == null) {
            return 0;
        }
        return children.size();
    }

    @Override
    public int getIndex(TreeNode node) {
        if (children == null) {
            return -1;
        }
        return children.indexOf(node);
    }

    @Override
    public boolean isLeaf() {
        if (children == null) {
            return true;
        }
        return children.isEmpty();
    }

    @Override
    public Enumeration children() {
        if (children == null) {
            return Collections.emptyEnumeration();
        }
        return Collections.enumeration(children);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void insert(MutableTreeNode child, int index) {
        if (children == null) {
            children = new ArrayList<>();
        }
        if (child == null) {
            throw new NullPointerException("Cannot add null child to " + getClass().getSimpleName());
        }
        children.add(index, (KatNode) child);
    }

    @Override
    public void remove(int index) {
        if (children != null) {
            children.remove(index);
            if (children.isEmpty()) {
                children = null;
            }
        }
    }

    @Override
    public void remove(MutableTreeNode node) {
        if (children != null) {
            children.remove(node);
            if (children.isEmpty()) {
                children = null;
            }
        }
    }

    @Override
    public void removeFromParent() {
        if (children != null) {
            parent.remove(this);
            if (children.isEmpty()) {
                children = null;
            }
        }
    }

}
