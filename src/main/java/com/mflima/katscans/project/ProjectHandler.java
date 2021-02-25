package com.mflima.katscans.project;

import com.mflima.katscans.project.displayable.DataFileNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * @author Marcelo Lima
 */
public class ProjectHandler extends DefaultTreeModel {

  public boolean insertDataFile(DataFileNode node) {
    ProjectNode projectNode = getRoot();
    if (projectNode.isParentOf(node)) {
      return false;
    }
    insertNodeInto(node, getRoot(), getRoot().getChildCount());
    return true;
  }

  @Override
  public ProjectNode getRoot() {
    return (ProjectNode) super.getRoot();
  }

  public static ProjectHandler getInstance() {
    return ProjectHandlerHolder.INSTANCE;
  }

  private ProjectHandler() {
    super(new ProjectNode());
  }

  private static class ProjectHandlerHolder {

    private static final ProjectHandler INSTANCE = new ProjectHandler();
  }
}
