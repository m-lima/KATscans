package no.uib.inf252.katscan.project;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.tree.MutableTreeNode;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.OperationAbortedException;
import no.uib.inf252.katscan.project.displayable.Displayable;
import net.infonode.docking.View;
import no.uib.inf252.katscan.view.katview.Histogram;
import no.uib.inf252.katscan.project.displayable.TransferFunctionNode;
import no.uib.inf252.katscan.view.katview.TransferFunctionEditor;
import no.uib.inf252.katscan.view.katview.opengl.CompositeRenderer;
import no.uib.inf252.katscan.view.katview.opengl.MaximumRenderer;
import no.uib.inf252.katscan.view.katview.opengl.SliceNavigator;

/**
 *
 * @author Marcelo Lima
 */
public class KatViewNode extends KatNode implements DockingWindowListener {

    public enum Type {
        COMPOSITE("Composite Renderer", 'C', true),
        MAXIMUM("Maximum Renderer", 'M', false),
        SLICE("Slice Navigator", 'S', true),
        EDITOR("Editor", 'E', true),
        HISTOGRAM("Histogram", 'H', false);

        private final String name;
        private final char mnemonic;
        private final boolean transferFunctionNeeded;

        private Type(String name, char mnemonic, boolean transferFunctionNeeded) {
            this.name = name;
            this.mnemonic = mnemonic;
            this.transferFunctionNeeded = transferFunctionNeeded;
        }

        public String getName() {
            return name;
        }

        public char getMnemonic() {
            return mnemonic;
        }

        public boolean isTransferFunctionNeeded() {
            return transferFunctionNeeded;
        }

    }

    public static KatViewNode buildKatView(Type type, Displayable displayable) {
        if (type == null || displayable == null) {
            throw new IllegalArgumentException();
        }
        
        switch (type) {
            case COMPOSITE:
                //TODO Transfer functions should be a child of displayable and SliceNavigator and CompositeRenderer children of TransferFunction
                return new KatViewNode(type, displayable, new CompositeRenderer((TransferFunctionNode) displayable));
            case MAXIMUM:
                return new KatViewNode(type, displayable, new MaximumRenderer(displayable));
            case SLICE:
                return new KatViewNode(type, displayable, new SliceNavigator((TransferFunctionNode) displayable));
            case EDITOR:
                return new KatViewNode(type, displayable, new TransferFunctionEditor((TransferFunctionNode) displayable));
            case HISTOGRAM:
                return new KatViewNode(type, displayable, new Histogram(displayable));
            default:
                return null;
        }
    }
    
    private final View view;
    private final Type type;

    private KatViewNode(Type type, Displayable displayable, Component component) {
        super(type.getName());
        if (displayable == null || component == null) {
            throw new NullPointerException();
        }
        
        this.type = type;
        this.view = new View(type.getName() + " - " + displayable.getName(), null, component);
        view.addListener(this);
    }

    public View getView() {
        return view;
    }
    
    public void removeWindow() {
        view.close();
    }

    public boolean isTransferFunctionNeeded() {
        return type.transferFunctionNeeded;
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

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 17 * hash + Objects.hashCode(view);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return view.equals(((KatViewNode)obj).view);
        }
        return false;
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
