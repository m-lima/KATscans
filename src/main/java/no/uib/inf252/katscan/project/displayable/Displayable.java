package no.uib.inf252.katscan.project.displayable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Objects;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import no.uib.inf252.katscan.data.VoxelMatrix;
import no.uib.inf252.katscan.project.KatNode;
import no.uib.inf252.katscan.project.KatViewNode;
import no.uib.inf252.katscan.project.ProjectHandler;
import no.uib.inf252.katscan.view.katview.KatView.Type;

/**
 *
 * @author Marcelo Lima
 */
public abstract class Displayable extends KatNode implements ActionListener {
    
    private static final String REMOVE = "Remove";
    private static final String TRANSFER = "Tranfer Function";
    private static final String CUT = "Cut";
    private static final String STRUCTURE = "Structure";

    public abstract VoxelMatrix getMatrix();
    public abstract int[] getHistogram();

    public Displayable(String name) {
        super(name);
    }

    @Override
    protected JMenu getMainMenu() {
        JMenu menu = new JMenu(getName());
        
        Type[] types = Type.values();
        for (final Type type : types) {
            if (typeAcceptable(type)) {
                JMenuItem item = new JMenuItem(type.getName(), type.getMnemonic());
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        KatViewNode view = KatViewNode.buildKatView(type, Displayable.this);
                        ProjectHandler.getInstance().insertNodeInto(view, Displayable.this, getChildCount());
                    }
                });
                menu.add(item);
            }
        }
        if (types.length > 0) {
            menu.addSeparator();
        }
        
        JMenuItem cutMenu = new JMenuItem(CUT, 'U');
        JMenuItem structureMenu = new JMenuItem(STRUCTURE, 'R');
        JMenuItem removeMenu = new JMenuItem(REMOVE, 'R');
        
        cutMenu.addActionListener(this);
        structureMenu.addActionListener(this);
        removeMenu.addActionListener(this);
        
        JMenuItem[] extraMenus = getExtraMenus();
        if (extraMenus != null) {
            for (JMenuItem extraMenu : extraMenus) {
                menu.add(extraMenu);
            }
        }
        
        menu.add(cutMenu);
        menu.add(structureMenu);
        menu.addSeparator();
        menu.add(removeMenu);
        
        return menu;
    }

    public JMenu getMenu() {
        return null;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 27 * hash + Objects.hashCode(getMatrix());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return getMatrix().equals(((Displayable)obj).getMatrix());
        }
        return false;
    }
    
    protected void remove() {
        //TODO Remove reference to ProjectHandler
        ProjectHandler.getInstance().removeNodeFromParent(this);
        Enumeration<KatNode> children = children();
        while(children.hasMoreElements()) {
            KatNode child = children.nextElement();
            if (child instanceof Displayable) {
                ((Displayable) child).remove();
            } else if (child instanceof KatViewNode) {
                ((KatViewNode) child).getView().close();
            }
        }
    }
    
    protected boolean typeAcceptable(Type type) {
        return !type.isTransferFunctionNeeded();
    }
    
    protected JMenuItem[] getExtraMenus() {
        JMenuItem transfer = new JMenuItem(TRANSFER, 'T');
        transfer.addActionListener(this);
        
        return new JMenuItem[] {transfer};
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JMenuItem item  = (JMenuItem) e.getSource();
        String text = item.getText();
        switch(text) {
            case REMOVE:
                remove();
                break;
            case TRANSFER:
                ProjectHandler.getInstance().insertNodeInto(new TransferFunctionNode(), this, getChildCount());
                break;
            case CUT:
                ProjectHandler.getInstance().insertNodeInto(new Cut(), this, getChildCount());
                break;
            case STRUCTURE:
                ProjectHandler.getInstance().insertNodeInto(new Structure(), this, getChildCount());
                break;
        }
    }

}
