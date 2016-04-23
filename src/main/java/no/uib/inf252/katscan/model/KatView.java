package no.uib.inf252.katscan.model;

import java.awt.Component;
import java.util.Objects;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import no.uib.inf252.katscan.model.displayable.Displayable;
import net.infonode.docking.View;
import no.uib.inf252.katscan.model.KatNode;
import no.uib.inf252.katscan.util.TransferFunction;
import no.uib.inf252.katscan.view.Histogram;
import no.uib.inf252.katscan.view.KatViewHandler;
import no.uib.inf252.katscan.view.opengl.CompositeRenderer;
import no.uib.inf252.katscan.view.opengl.MaximumRenderer;
import no.uib.inf252.katscan.view.opengl.SliceNavigator;

/**
 *
 * @author Marcelo Lima
 */
public class KatView extends KatNode {

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

    public static KatView buildKatView(Type type, Displayable displayable) {
        if (type == null || displayable == null) {
            throw new IllegalArgumentException();
        }
        
        switch (type) {
            case COMPOSITE:
                return new KatView(type, displayable, new CompositeRenderer(displayable, new TransferFunction()));
            case MAXIMUM:
                return new KatView(type, displayable, new MaximumRenderer(displayable));
            case SLICE:
                return new KatView(type, displayable, new SliceNavigator(displayable));
            case HISTOGRAM:
                return new KatView(type, displayable, new Histogram(displayable));
            default:
                return null;
        }
    }
    
    private final View view;
    private final Type type;

    private KatView(Type type, Displayable displayable, Component component) {
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
            return view.equals(((KatView)obj).view);
        }
        return false;
    }
    
}
