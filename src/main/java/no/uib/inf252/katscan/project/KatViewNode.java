package no.uib.inf252.katscan.project;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.tree.MutableTreeNode;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.View;
import no.uib.inf252.katscan.event.TransferFunctionListener;
import no.uib.inf252.katscan.project.displayable.Displayable;
import no.uib.inf252.katscan.view.component.image.LoadingPanel;
import no.uib.inf252.katscan.view.katview.KatView;
import no.uib.inf252.katscan.view.katview.KatView.Type;

/**
 *
 * @author Marcelo Lima
 */
public class KatViewNode extends KatNode implements DockingWindowListener {
    
    private final Type type;
    private final View view;
    private boolean newView;

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
        view.addListener(this);
        this.type = type;
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

    @Override
    public void setParent(MutableTreeNode newParent) {
        if (!(newParent instanceof Displayable)) {
            throw new IllegalArgumentException("Can only have " + Displayable.class.getSimpleName() + " nodes as parents of " + getClass().getSimpleName() + " nodes.");
        }
        
        final Displayable displayable = (Displayable) newParent;
        final Component component = view.getComponent();
        
        view.setComponent(new LoadingPanel(false));
        view.getViewProperties().setTitle(type.getText() + " - Loading");
        
        super.setParent(displayable);
        
        new Thread("View launching thread") {
            @Override
            public void run() {
                try {
                    if (component instanceof TransferFunctionListener) {
                        Displayable parent = getParent();
                        if (parent != null) {
                            parent.getTransferFunction().removeTransferFunctionListener((TransferFunctionListener) component);
                        }
                    }
        
                    Map<String, Object> properties = null;
                    if (component instanceof KatView) {
                        properties = ((KatView) component).packProperties();
                    }

                    Component katView = type.getConstructor().newInstance(displayable);
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

    @Override
    public void windowAdded(DockingWindow addedToWindow, DockingWindow addedWindow) {}

    @Override
    public void windowRemoved(DockingWindow removedFromWindow, DockingWindow removedWindow) {}

    @Override
    public void windowShown(DockingWindow window) {}

    @Override
    public void windowHidden(DockingWindow window) {}

    @Override
    public void viewFocusChanged(View previouslyFocusedView, View focusedView) {}

    @Override
    public void windowClosing(DockingWindow window) throws OperationAbortedException {}

    @Override
    public void windowClosed(DockingWindow window) {
        ProjectHandler.getInstance().removeNodeFromParent(this);
        
        if (getView().getComponent() instanceof TransferFunctionListener) {
            getParent().getTransferFunction().removeTransferFunctionListener((TransferFunctionListener) getView().getComponent());
        }
    }

    @Override
    public void windowUndocking(DockingWindow window) throws OperationAbortedException {}

    @Override
    public void windowUndocked(DockingWindow window) {}

    @Override
    public void windowDocking(DockingWindow window) throws OperationAbortedException {}

    @Override
    public void windowDocked(DockingWindow window) {}

    @Override
    public void windowMinimizing(DockingWindow window) throws OperationAbortedException {}

    @Override
    public void windowMinimized(DockingWindow window) {}

    @Override
    public void windowMaximizing(DockingWindow window) throws OperationAbortedException {}

    @Override
    public void windowMaximized(DockingWindow window) {}

    @Override
    public void windowRestoring(DockingWindow window) throws OperationAbortedException {}

    @Override
    public void windowRestored(DockingWindow window) {}
    
}
