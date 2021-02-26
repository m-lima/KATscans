package com.mflima.katscans.project;

import com.mflima.katscans.view.component.DraggableTree;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/** @author Marcelo Lima */
public abstract class KatNode implements MutableTreeNode, Serializable, Transferable {

  private KatNode parent;
  private String name;
  private ArrayList<KatNode> children;

  public KatNode(String name) {
    setName(name);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException(
          String.format("The %s name cannot be null or empty.", getClass().getSimpleName()));
    }
    this.name = name.trim();
  }

  protected abstract JMenu getMainMenu();

  protected abstract KatNode internalCopy();

  public final void treeRecentlyLoaded() {
    if (children == null) {
      return;
    }

    for (KatNode katNode : children) {
      if (katNode instanceof KatViewNode) {
        katNode.setParent(this);
      } else {
        katNode.treeRecentlyLoaded();
      }
    }
  }

  public final KatNode copy() {
    KatNode copy = internalCopy();

    if (children != null) {
      for (int i = 0; i < children.size(); i++) {
        copy.insert(children.get(i).copy(), i);
      }
    }

    return copy;
  }

  private ArrayList<JMenu> getChildrenMenu() {
    ArrayList<JMenu> childrenMenu = new ArrayList<>();
    if (children != null) {
      for (KatNode child : children) {
        final JMenu menu = child.getMenu(true);
        if (menu != null) {
          childrenMenu.add(menu);
        }
      }
    }
    return childrenMenu;
  }

  public final JPopupMenu getPopupMenu() {
    JMenu menu = getMenu(false);
    if (menu != null) {
      return menu.getPopupMenu();
    }
    return null;
  }

  public final JMenu getMenu(boolean includeChildren) {
    JMenu nodeMenu = getMainMenu();

    if (nodeMenu == null) {
      return null;
    }

    if (includeChildren) {
      ArrayList<JMenu> childrenMenu = getChildrenMenu();

      if (!childrenMenu.isEmpty()) {
        nodeMenu.add(new JPopupMenu.Separator(), 0);
      }

      for (int i = childrenMenu.size() - 1; i >= 0; i--) {
        JMenu childMenu = childrenMenu.get(i);

        if (i < 10) {
          char mnemonic = i == 9 ? '0' : (char) (i + '1');
          childMenu.setText(String.format("[%c] %s", mnemonic, childMenu.getText()));
          childMenu.setMnemonic(mnemonic);
        }

        nodeMenu.add(childMenu, 0);
      }
    }

    return nodeMenu;
  }

  public ImageIcon getIcon() {
    return null;
  }

  public boolean isParentOf(KatNode node) {
    if (children == null) {
      return false;
    }
    return children.contains(node);
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
      throw new NullPointerException(
          String.format("The %s object cannot be null.", getClass().getSimpleName()));
    }
    String newName = object.toString();
    setName(newName);
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
    //noinspection SuspiciousMethodCalls
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
  public Enumeration<KatNode> children() {
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
    if (child == null) {
      throw new NullPointerException(
          String.format("Cannot add null child to %s", getClass().getSimpleName()));
    }
    if (index < 0) {
      throw new ArrayIndexOutOfBoundsException(index);
    }

    if (children == null) {
      children = new ArrayList<>();
    }
    children.add(index, (KatNode) child);
    child.setParent(this);
  }

  @Override
  public void remove(int index) {
    if (children == null || index < 0) {
      return;
    }

    children.remove(index);
    if (children.isEmpty()) {
      children = null;
    }
  }

  @Override
  public void remove(MutableTreeNode node) {
    if (children != null) {
      children.removeIf(node::equals);
      if (children.isEmpty()) {
        children = null;
      }
    }
  }

  @Override
  public void removeFromParent() {
    parent.remove(this);
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {DraggableTree.LOCAL_OBJECT_FLAVOR, DraggableTree.LOCAL_OBJECT_FLAVOR};
  }

  @Override
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    for (DataFlavor transferDataFlavor : getTransferDataFlavors()) {
      if (!flavor.equals(transferDataFlavor)) {
        return false;
      }
    }

    return true;
  }

  @Override
  public KatNode getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
    if (isDataFlavorSupported(flavor)) {
      return this;
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
  }
}
