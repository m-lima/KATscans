package com.mflima.katscans.project.displayable;

import java.io.Serializable;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.tree.MutableTreeNode;
import com.mflima.katscans.model.Camera;

/**
 * @author Marcelo Lima
 */
public class CameraNode extends SubGroup implements Serializable {

  private final Camera camera;

  public CameraNode() {
    this(5f);
  }

  public CameraNode(float initialZoom) {
    super("Camera");

    camera = new Camera(initialZoom);
  }

  @Override
  protected CameraNode internalCopy() {
    CameraNode newNode = new CameraNode();
    newNode.camera.assimilate(camera);
    return newNode;
  }

  @Override
  public void setParent(MutableTreeNode newParent) {
    super.setParent(newParent);
    camera.setInitialZoom(2 * ((Displayable) newParent).getMatrix().getRatio()[2]);
  }

  @Override
  public Camera getCamera() {
    return camera;
  }

  @Override
  public ImageIcon getIcon() {
    return new ImageIcon(getClass().getResource("/icons/tree/camera.png"));
  }

  @Override
  protected JMenuItem[] getExtraMenus() {
    return null;
  }

}
