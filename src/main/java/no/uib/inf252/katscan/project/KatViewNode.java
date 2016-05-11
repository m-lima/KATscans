package no.uib.inf252.katscan.project;

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
import no.uib.inf252.katscan.event.CameraListener;
import no.uib.inf252.katscan.event.CutListener;
import no.uib.inf252.katscan.event.LightListener;
import no.uib.inf252.katscan.event.RotationListener;
import no.uib.inf252.katscan.event.TransferFunctionListener;
import no.uib.inf252.katscan.project.displayable.Displayable;
import no.uib.inf252.katscan.view.component.image.LoadingPanel;
import no.uib.inf252.katscan.view.katview.KatView;
import no.uib.inf252.katscan.view.katview.KatView.Type;

/**
 *
 * @author Marcelo Lima
 */
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
        this.view = new View(type.getText() + " - Loading", null, new LoadingPanel(false));
        this.type = type;
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        type = (Type) in.readObject();
        this.view = new View(type.getText() + " - Loading", null, new LoadingPanel(false));
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
                displayable.getTransferFunction().removeKatModelListener((TransferFunctionListener) katView);
            }
        }
    }

    @Override
    public void setParent(MutableTreeNode newParent) {
        if (!(newParent instanceof Displayable)) {
            throw new IllegalArgumentException("Can only have " + Displayable.class.getSimpleName() + " nodes as parents of " + getClass().getSimpleName() + " nodes.");
        }
        
        final Displayable displayable = (Displayable) newParent;
        final Component oldComponent = view.getComponent();
        final Displayable oldParent = getParent();
        
        view.setComponent(new LoadingPanel(false));
        view.getViewProperties().setTitle(type.getText() + " - Loading");
        
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
                    
                    if (displayable != null) {
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
                            displayable.getTransferFunction().addKatModelListener((TransferFunctionListener) katView);
                        }
                    }
                    
                    ((KatView)katView).loadProperties(properties);
                    view.getViewProperties().setTitle(type.getText() + " - " + displayable.getName());
                    view.setComponent(katView);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
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
