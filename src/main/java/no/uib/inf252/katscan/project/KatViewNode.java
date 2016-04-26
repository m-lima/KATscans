package no.uib.inf252.katscan.project;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import no.uib.inf252.katscan.project.displayable.Displayable;
import net.infonode.docking.View;
import no.uib.inf252.katscan.util.TransferFunction;
import no.uib.inf252.katscan.view.katview.Histogram;
import no.uib.inf252.katscan.data.KatViewHandler;
import no.uib.inf252.katscan.view.katview.TransferFunctionEditor;
import no.uib.inf252.katscan.view.katview.opengl.CompositeRenderer;
import no.uib.inf252.katscan.view.katview.opengl.MaximumRenderer;
import no.uib.inf252.katscan.view.katview.opengl.SliceNavigator;

/**
 *
 * @author Marcelo Lima
 */
public class KatViewNode extends KatNode {

    public enum Type {
        COMPOSITE("Composite Renderer", 'C'),
        MAXIMUM("Maximum Renderer", 'M'),
        SLICE("Slice Navigator", 'S'),
        HISTOGRAM("Histogram", 'H');

        private final String name;
        private final char mnemonic;

        private Type(String name, char mnemonic) {
            this.name = name;
            this.mnemonic = mnemonic;
        }

        public String getName() {
            return name;
        }

        public char getMnemonic() {
            return mnemonic;
        }

    }

    public static KatViewNode buildKatView(Type type, Displayable displayable) {
        if (type == null || displayable == null) {
            throw new IllegalArgumentException();
        }
        
        switch (type) {
            case COMPOSITE:
                //TODO Transfer functions should be a child of displayable and SliceNavigator and CompositeRenderer children of TransferFunction
                return new KatViewNode(type, displayable, new CompositeRenderer(displayable, new TransferFunction(displayable.getMatrix().getMaxValue())));
            case MAXIMUM:
                return new KatViewNode(type, displayable, new MaximumRenderer(displayable));
            case SLICE:
                return new KatViewNode(type, displayable, new SliceNavigator(displayable));
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
        setParent(displayable);
        view.addListener(KatViewHandler.getInstance());
    }

    public View getView() {
        return view;
    }
    
    @Override
    public Displayable getParent() {
        return (Displayable) super.getParent();
    }

    @Override
    protected JMenu getMainMenu() {
        if (type == Type.COMPOSITE) {
            JMenu menu = new JMenu(getName());
            JMenuItem item = new JMenuItem("Tranfer function", 'T');
            
            //TODO Sooooo wrong!
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Histogram histogram = new TransferFunctionEditor(getParent(), ((CompositeRenderer)getView().getComponent()).getTransferFunction());
                    KatViewNode view = new KatViewNode(type, getParent(), histogram);
                    KatViewHandler.getInstance().requestAddView(view);
                }
            });
            
            menu.add(item);
            return menu;
        }
        return null;
    }

    @Override
    public boolean getAllowsChildren() {
        return type == Type.COMPOSITE;
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
    
}
