package no.uib.inf252.katscan.project;

import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.View;
import no.uib.inf252.katscan.event.TransferFunctionListener;
import no.uib.inf252.katscan.project.displayable.Displayable;
import no.uib.inf252.katscan.view.katview.KatView;
import no.uib.inf252.katscan.view.katview.KatView.Type;

/**
 *
 * @author Marcelo Lima
 */
public class KatViewNode extends KatNode implements DockingWindowListener {
    
    private final Type type;
    private final KatView katView;

    public static KatViewNode buildKatView(Type type, Displayable displayable) {
        if (type == null || displayable == null) {
            throw new IllegalArgumentException();
        }
        
        try {
            return new KatViewNode(type, displayable, type.getConstructor().newInstance(displayable));
        } catch (Exception ex) {
            Logger.getLogger(KatViewNode.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    private final View view;

    private KatViewNode(Type type, Displayable displayable, Component component) {
        super(type.getText());
        if (displayable == null || component == null) {
            throw new NullPointerException();
        }
        
        this.view = new View(type.getText() + " - " + displayable.getName(), null, component);
        view.addListener(this);
        this.type = type;
        this.katView = (KatView) component;
    }

    public View getView() {
        return view;
    }
    
    public void removeWindow() {
        view.close();
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

    public Type getType() {
        return type;
    }

    public KatView getKatView() {
        return katView;
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
        
        //TODO Fix this (Remove TransferFunctionListener)
        //FIXME Fix this (Remove TransferFunctionListener)
        if (getView() instanceof TransferFunctionListener) {
            getParent().getTransferFunction().removeTransferFunctionListener((TransferFunctionListener) getView());
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
