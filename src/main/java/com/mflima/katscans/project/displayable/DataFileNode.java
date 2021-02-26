package com.mflima.katscans.project.displayable;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.tree.MutableTreeNode;
import com.mflima.katscans.Init;
import com.mflima.katscans.data.io.LoadSaveFormat;
import com.mflima.katscans.data.io.LoadSaveHandler;
import com.mflima.katscans.data.io.LoadSaveOptions;
import com.mflima.katscans.data.VoxelMatrix;
import com.mflima.katscans.model.Camera;
import com.mflima.katscans.model.Rotation;
import com.mflima.katscans.model.Cut;
import com.mflima.katscans.model.Light;
import com.mflima.katscans.model.TransferFunction;
import com.mflima.katscans.project.ProjectNode;

/** @author Marcelo Lima */
public class DataFileNode extends Displayable implements Serializable {

  private File file;
  private final LoadSaveFormat.Format format;
  private final LoadSaveOptions options;
  private transient VoxelMatrix matrix;

  private final TransferFunction transferFunction;
  private final Cut cut;
  private final Rotation rotation;
  private final Light light;
  private final Camera camera;

  public DataFileNode(
      String name,
      File file,
      LoadSaveFormat.Format format,
      LoadSaveOptions options,
      VoxelMatrix matrix) {
    super(name);
    this.file = file;
    this.matrix = matrix;
    this.format = format;
    this.options = options;

    transferFunction = new TransferFunction(TransferFunction.Type.SLOPE);
    cut = new Cut();
    rotation = new Rotation();
    light = new Light();
    camera = new Camera(2 * matrix.getRatio()[2]);
  }

  @Override
  protected DataFileNode internalCopy() {
    DataFileNode newNode =
        new DataFileNode(
            getName(), new File(file.getAbsolutePath()), format, options, matrix.copy());
    newNode.transferFunction.assimilate(transferFunction);
    newNode.cut.assimilate(cut);
    newNode.rotation.assimilate(rotation);
    newNode.light.assimilate(light);

    return newNode;
  }

  @Override
  public VoxelMatrix getMatrix() {
    if (matrix == null) {
      while (file == null || !file.exists() || !file.canRead()) {
        JOptionPane.showMessageDialog(
            Init.getFrameReference(),
            "Could not load data from file",
            "Load",
            JOptionPane.ERROR_MESSAGE);
        file = new LoadSaveHandler(format).showLoadDialog(file);
        if (file == null) {
          remove();
        }
      }

      try {
        matrix = format.getFormat().loadData(new FileInputStream(file), options);
      } catch (Exception ex) {
        Logger.getLogger(DataFileNode.class.getName()).log(Level.SEVERE, null, ex);
        JOptionPane.showMessageDialog(
            Init.getFrameReference(),
            String.format("Could not load data from %s", file.getPath()),
            "Load",
            JOptionPane.ERROR_MESSAGE);
        remove();
      }
    }
    return matrix;
  }

  @Override
  public TransferFunction getTransferFunction() {
    return transferFunction;
  }

  @Override
  public Cut getCut() {
    return cut;
  }

  @Override
  public Rotation getRotation() {
    return rotation;
  }

  @Override
  public Light getLight() {
    return light;
  }

  @Override
  public Camera getCamera() {
    return camera;
  }

  @Override
  public boolean getAllowsChildren() {
    return true;
  }

  @Override
  public ProjectNode getParent() {
    return (ProjectNode) super.getParent();
  }

  @Override
  public void setParent(MutableTreeNode newParent) {
    if (newParent instanceof ProjectNode) {
      super.setParent(newParent);
    } else {
      throw new IllegalArgumentException(
          String.format(
              "Can only have %s nodes as parent of %s nodes.",
              ProjectNode.class.getSimpleName(), getClass().getSimpleName()));
    }
  }

  @Override
  public void insert(MutableTreeNode child, int index) {
    if (child instanceof ProjectNode || child instanceof DataFileNode) {
      throw new IllegalArgumentException(
          String.format(
              "Cannot add %s nodes or %s nodes to %s nodes.",
              getClass().getSimpleName(),
              ProjectNode.class.getSimpleName(),
              getClass().getSimpleName()));
    }
    super.insert(child, index);
  }

  @Override
  public ImageIcon getIcon() {
    return new ImageIcon(getClass().getResource("/icons/tree/file.png"));
  }

  @Override
  protected JMenuItem[] getExtraMenus() {
    return null;
  }
}
