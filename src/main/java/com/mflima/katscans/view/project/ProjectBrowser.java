package com.mflima.katscans.view.project;

import com.mflima.katscans.project.KatNode;
import com.mflima.katscans.project.ProjectHandler;
import com.mflima.katscans.view.component.DraggableTree;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/** @author Marcelo Lima */
public class ProjectBrowser extends JPanel {

  /** Creates new form DatasetBrowser */
  public ProjectBrowser() {
    initComponents();

    treDatasets.setModel(ProjectHandler.getInstance());
    treDatasets.setCellRenderer(new ProjectBrowserRenderer());
    treDatasets.setShowsRootHandles(false);
    treDatasets.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    treDatasets.setSelectionRow(0);

    treDatasets.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
              showPopup(e.getX(), e.getY());
            }
          }
        });

    treDatasets.addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyTyped(KeyEvent e) {
            if (e.getKeyChar() == ' ') {
              TreePath node = treDatasets.getSelectionPath();
              if (node == null) {
                return;
              }

              Rectangle pathBounds = treDatasets.getPathBounds(node);
              Point point =
                  pathBounds == null
                      ? new Point()
                      : new Point(
                          pathBounds.x + pathBounds.width / 2,
                          pathBounds.y + pathBounds.height / 2);
              showPopup(point.x, point.y);
            }
          }
        });
  }

  public void focusTree() {
    treDatasets.requestFocusInWindow();
  }

  private void showPopup(int x, int y) {
    TreePath path = treDatasets.getPathForLocation(x, y);
    if (path == null) {
      return;
    }

    KatNode node = (KatNode) path.getLastPathComponent();
    JPopupMenu popupMenu = node.getPopupMenu();
    if (popupMenu != null) {
      popupMenu.show(treDatasets, x, y);
    }
  }

  private void initComponents() {

    JPanel pnlMain = new JPanel();
    JScrollPane scrDatasets = new JScrollPane();

    treDatasets = new DraggableTree();

    setLayout(new BorderLayout());

    treDatasets.setModel(null);
    scrDatasets.setViewportView(treDatasets);

    GroupLayout pnlMainLayout = new GroupLayout(pnlMain);
    pnlMain.setLayout(pnlMainLayout);
    pnlMainLayout.setHorizontalGroup(
        pnlMainLayout
            .createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(
                pnlMainLayout
                    .createSequentialGroup()
                    .addContainerGap()
                    .addComponent(scrDatasets, GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
                    .addContainerGap()));
    pnlMainLayout.setVerticalGroup(
        pnlMainLayout
            .createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(
                pnlMainLayout
                    .createSequentialGroup()
                    .addContainerGap()
                    .addComponent(scrDatasets, GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                    .addContainerGap()));

    add(pnlMain, BorderLayout.CENTER);
  }

  private DraggableTree treDatasets;
}
