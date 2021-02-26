package com.mflima.katscans.project;

import com.mflima.katscans.event.CameraListener;
import com.mflima.katscans.event.CutListener;
import com.mflima.katscans.event.LightListener;
import com.mflima.katscans.event.RotationListener;
import com.mflima.katscans.event.TransferFunctionListener;
import com.mflima.katscans.project.displayable.Displayable;
import com.mflima.katscans.view.component.image.LoadingPanel;
import com.mflima.katscans.view.katview.KatView;
import com.mflima.katscans.view.katview.KatView.Type;
import java.awt.Component;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.tree.MutableTreeNode;
import net.infonode.docking.View;

/** @author Marcelo Lima */
public class KatViewNode extends KatNode {

  private Type type;
  private transient View view;
  private transient boolean newView;

  public static KatViewNode buildKatView(Type type) {
    if (type == null) {
      throw new IllegalArgumentException();
    }

    try {
      return new KatViewNode(type);
    } catch (Exception ex) {
      Logger.getLogger(KatViewNode.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
  }

  private KatViewNode(Type type) {
    super(type.getText());
    newView = true;
    this.view =
        new View(String.format("%s - Loading", type.getText()), null, new LoadingPanel(false));
    this.type = type;
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    type = (Type) in.readObject();
    this.view =
        new View(String.format("%s - Loading", type.getText()), null, new LoadingPanel(false));
    newView = true;
  }

  public View getView() {
    return view;
  }

  public void removeWindow() {
    view.close();
  }

  @Override
  protected KatViewNode internalCopy() {
    return new KatViewNode(type);
  }

  @Override
  public Displayable getParent() {
    return (Displayable) super.getParent();
  }

  @Override
  protected JMenu getMainMenu() {
    return null;
  }

  @Override
  public boolean getAllowsChildren() {
    return false;
  }

  public boolean isNewView() {
    return newView;
  }

  public void setNewView(boolean newView) {
    this.newView = newView;
  }

  public void stopListening(Displayable displayable, Component katView) {
    if (displayable != null && katView != null) {
      if (katView instanceof CameraListener) {
        displayable.getCamera().removeKatModelListener((CameraListener) katView);
      }

      if (katView instanceof CutListener) {
        displayable.getCut().removeKatModelListener((CutListener) katView);
      }

      if (katView instanceof LightListener) {
        displayable.getLight().removeKatModelListener((LightListener) katView);
      }

      if (katView instanceof RotationListener) {
        displayable.getRotation().removeKatModelListener((RotationListener) katView);
      }

      if (katView instanceof TransferFunctionListener) {
        displayable
            .getTransferFunction()
            .removeKatModelListener((TransferFunctionListener) katView);
      }
    }
  }

  @Override
  public void setParent(MutableTreeNode newParent) {
    if (!(newParent instanceof Displayable)) {
      throw new IllegalArgumentException(
          String.format(
              "Can only have %s nodes as parents of %s nodes.",
              Displayable.class.getSimpleName(), getClass().getSimpleName()));
    }

    final Displayable displayable = (Displayable) newParent;
    final Component oldComponent = view.getComponent();
    final Displayable oldParent = getParent();

    view.setComponent(new LoadingPanel(false));
    view.getViewProperties().setTitle(String.format("%s - Loading", type.getText()));

    super.setParent(displayable);

    new Thread("View launching thread") {
      @Override
      public void run() {
        try {
          stopListening(oldParent, oldComponent);

          Map<String, Object> properties = null;
          if (oldComponent instanceof KatView) {
            properties = ((KatView) oldComponent).packProperties();
          }

          Component katView = type.getConstructor().newInstance(displayable);

          if (katView instanceof CameraListener) {
            displayable.getCamera().addKatModelListener((CameraListener) katView);
          }

          if (katView instanceof CutListener) {
            displayable.getCut().addKatModelListener((CutListener) katView);
          }

          if (katView instanceof LightListener) {
            displayable.getLight().addKatModelListener((LightListener) katView);
          }

          if (katView instanceof RotationListener) {
            displayable.getRotation().addKatModelListener((RotationListener) katView);
          }

          if (katView instanceof TransferFunctionListener) {
            displayable
                .getTransferFunction()
                .addKatModelListener((TransferFunctionListener) katView);
          }

          ((KatView) katView).loadProperties(properties);
          view.getViewProperties()
              .setTitle(String.format("%s - %s", type.getText(), displayable.getName()));
          view.setComponent(katView);
        } catch (InstantiationException
            | IllegalAccessException
            | IllegalArgumentException
            | InvocationTargetException ex) {
          Logger.getLogger(KatViewNode.class.getName()).log(Level.SEVERE, null, ex);
          view.close();
        }
      }
    }.start();
  }

  public Type getType() {
    return type;
  }

  @Override
  public ImageIcon getIcon() {
    return new ImageIcon(getClass().getResource("/icons/tree/view.png"));
  }
}
