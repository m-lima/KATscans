package com.mflima.katscans.project.displayable;

import java.io.Serializable;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import com.mflima.katscans.model.Cut;

/** @author Marcelo Lima */
public class CutNode extends SubGroup implements Serializable {

  private final Cut cut;

  public CutNode() {
    super("Cut");
    cut = new Cut();
  }

  @Override
  protected CutNode internalCopy() {
    CutNode newNode = new CutNode();
    newNode.cut.assimilate(cut);
    return newNode;
  }

  @Override
  public Cut getCut() {
    return cut;
  }

  @Override
  public ImageIcon getIcon() {
    return new ImageIcon(getClass().getResource("/icons/tree/cut.png"));
  }

  @Override
  protected JMenuItem[] getExtraMenus() {
    return null;
  }
}
