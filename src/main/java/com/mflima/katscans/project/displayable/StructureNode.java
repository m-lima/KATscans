package com.mflima.katscans.project.displayable;

import java.io.Serializable;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

/** @author Marcelo Lima */
public class StructureNode extends SubGroup implements Serializable {

  public StructureNode() {
    super("Structure");
  }

  @Override
  protected StructureNode internalCopy() {
    return new StructureNode();
  }

  @Override
  protected JMenuItem[] getExtraMenus() {
    return null;
  }

  @Override
  public ImageIcon getIcon() {
    return new ImageIcon(getClass().getResource("/icons/tree/structure.png"));
  }
}
