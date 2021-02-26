package com.mflima.katscans.project.displayable;

import javax.swing.tree.MutableTreeNode;
import com.mflima.katscans.project.ProjectNode;

/** @author Marcelo Lima */
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
      throw new IllegalArgumentException(
          String.format(
              "Can only have %s nodes as parent of %s nodes.",
              Displayable.class.getSimpleName(),
              getClass().getSimpleName()));
    }
  }

  @Override
  public void insert(MutableTreeNode child, int index) {
    if (child instanceof ProjectNode || child instanceof DataFileNode) {
      throw new IllegalArgumentException(
          String.format(
              "Cannot add %s nodes or %s nodes to %s nodes.",
              DataFileNode.class.getSimpleName(),
              ProjectNode.class.getSimpleName(),
              getClass().getSimpleName()));
    }
    super.insert(child, index);
  }

  @Override
  public boolean getAllowsChildren() {
    return true;
  }
}
