package com.mflima.katscans.project;

import com.mflima.katscans.project.displayable.DataFileNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.tree.MutableTreeNode;
import com.mflima.katscans.data.io.LoadSaveFormat;
import com.mflima.katscans.view.LoadDiag;
import com.mflima.katscans.view.RenameDiag;

/** @author Marcelo Lima */
public class ProjectNode extends KatNode {

  private static final ProjectMenuListener LISTENER = new ProjectMenuListener();
  private static final String CLEAR_ALL = "Clear all";
  private static final String RENAME = "Rename";

  public ProjectNode() {
    super("New project");
    setParent(null);
  }

  @Override
  protected KatNode internalCopy() {
    throw new UnsupportedOperationException(
        String.format("%s nodes cannot be copied", getClass().getSimpleName()));
  }

  @Override
  protected JMenu getMainMenu() {
    JMenu menu = new JMenu("Datasets");
    menu.setMnemonic('D');
    menu.add(getLoadDataset());
    menu.add(getClearDatasets());
    menu.addSeparator();
    JMenuItem item = new JMenuItem(RENAME, 'R');
    item.setIcon(new ImageIcon(ProjectNode.class.getResource("/icons/edit.png")));

    item.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            RenameDiag.promptRename(ProjectNode.this);
          }
        });

    menu.add(item);
    return menu;
  }

  private JMenu getLoadDataset() {
    JMenu loadMenu = new JMenu("Load");
    loadMenu.setMnemonic('L');
    loadMenu.setIcon(new ImageIcon(ProjectNode.class.getResource("/icons/open.png")));

    LoadSaveFormat.Format[] formats = LoadSaveFormat.Format.values();
    for (LoadSaveFormat.Format format : formats) {
      JMenuItem menuItem =
          new JMenuItem(format.getFormat().getName(), format.getFormat().getMnemonic());
      menuItem.addActionListener(LISTENER);
      loadMenu.add(menuItem);
    }

    return loadMenu;
  }

  private JMenuItem getClearDatasets() {
    JMenuItem clearDatasets = new JMenuItem(CLEAR_ALL, 'C');
    clearDatasets.setIcon(new ImageIcon(ProjectNode.class.getResource("/icons/closeData.png")));
    clearDatasets.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {

            Enumeration<KatNode> children = children();
            ArrayList<DataFileNode> childrenList = new ArrayList<>();
            while (children.hasMoreElements()) {
              childrenList.add((DataFileNode) children.nextElement());
            }

            for (DataFileNode dataNode : childrenList) {
              dataNode.remove();
            }
          }
        });
    return clearDatasets;
  }

  @Override
  public boolean getAllowsChildren() {
    return true;
  }

  @Override
  public void insert(MutableTreeNode child, int index) {
    if (child instanceof DataFileNode) {
      super.insert(child, index);
    } else {
      throw new IllegalArgumentException(
          String.format(
              "Can only add %s nodes to Project nodes.", DataFileNode.class.getSimpleName()));
    }
  }

  @Override
  public DataFileNode getChildAt(int childIndex) {
    return (DataFileNode) super.getChildAt(childIndex);
  }

  @Override
  public void setParent(MutableTreeNode newParent) {
    if (newParent == null) {
      return;
    }
    throw new IllegalArgumentException(getClass().getSimpleName() + " nodes may not have parents.");
  }

  @Override
  public ImageIcon getIcon() {
    return new ImageIcon(getClass().getResource("/icons/tree/project.png"));
  }

  private static class ProjectMenuListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      JMenuItem menuItem = (JMenuItem) e.getSource();

      String formatName = menuItem.getText();
      LoadSaveFormat.Format[] formats = LoadSaveFormat.Format.values();
      for (LoadSaveFormat.Format format : formats) {
        if (format.getFormat().getName().equals(formatName)) {
          new LoadDiag(format).setVisible(true);
          return;
        }
      }
    }
  }
}
