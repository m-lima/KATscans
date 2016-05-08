package no.uib.inf252.katscan.project.displayable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.tree.MutableTreeNode;
import no.uib.inf252.katscan.data.VoxelMatrix;
import no.uib.inf252.katscan.model.Camera;
import no.uib.inf252.katscan.model.Rotation;
import no.uib.inf252.katscan.model.Cut;
import no.uib.inf252.katscan.model.Light;
import no.uib.inf252.katscan.project.KatNode;
import no.uib.inf252.katscan.project.KatViewNode;
import no.uib.inf252.katscan.project.ProjectHandler;
import no.uib.inf252.katscan.model.TransferFunction;
import no.uib.inf252.katscan.view.RenameDiag;
import no.uib.inf252.katscan.view.katview.KatView.Type;

/**
 *
 * @author Marcelo Lima
 */
public abstract class Displayable extends KatNode {
    
    private static final String TRANSFER = "Override Tranfer Function";
    private static final String CUT = "Override Cut";
    private static final String LIGHT = "Override Light";
    private static final String ROTATION = "Override Rotation";
    private static final String REMOVE = "Remove";
    private static final String RENAME = "Rename";

    public VoxelMatrix getMatrix() {
        return ((Displayable)getParent()).getMatrix();
    }
    
    public TransferFunction getTransferFunction() {
        return ((Displayable)getParent()).getTransferFunction();
    }
    
    public Cut getCut() {
        return ((Displayable)getParent()).getCut();
    }
    
    public Rotation getRotation() {
        return ((Displayable)getParent()).getRotation();
    }
    
    public Light getLight() {
        return ((Displayable)getParent()).getLight();
    }
    
    public Camera getCamera() {
        return ((Displayable)getParent()).getCamera();
    }

    public Displayable(String name) {
        super(name);
    }

    @Override
    protected JMenu getMainMenu() {
        JMenu menu = new JMenu(getName());
        
        Type[] types = Type.values();
        for (final Type type : types) {
            JMenuItem item = new JMenuItem(type.getText(), type.getMnemonic());
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    KatViewNode view = KatViewNode.buildKatView(type);
                    ProjectHandler.getInstance().insertNodeInto(view, Displayable.this, getChildCount());
                }
            });
            menu.add(item);
        }
        if (types.length > 0) {
            menu.addSeparator();
        }
        
        JMenuItem[] extraMenus = getExtraMenus();
        if (extraMenus != null && extraMenus.length > 0) {
            for (JMenuItem extraMenu : extraMenus) {
                menu.add(extraMenu);
            }
            menu.addSeparator();
        }
        
        JMenu transferMenu = new JMenu(TRANSFER);
        transferMenu.setMnemonic('T');
        
        JMenuItem cutMenu = new JMenuItem(CUT, 'U');
        JMenuItem rotationMenu = new JMenuItem(ROTATION, 'R');
        JMenuItem lightMenu = new JMenuItem(LIGHT, 'L');
        JMenuItem removeMenu = new JMenuItem(REMOVE, 'E');
        JMenuItem renameMenu = new JMenuItem(RENAME, 'N');
        
        MenuListener listener = new MenuListener();
        
        TransferFunction.Type[] transferTypes = TransferFunction.Type.values();
        for (TransferFunction.Type type : transferTypes) {
            JMenuItem subMenu = new JMenuItem(type.getText(), type.getMnemonic());
            subMenu.addActionListener(listener);
            transferMenu.add(subMenu);
        }
        
        transferMenu.addActionListener(listener);
        cutMenu.addActionListener(listener);
        rotationMenu.addActionListener(listener);
        lightMenu.addActionListener(listener);
        removeMenu.addActionListener(listener);
        renameMenu.addActionListener(listener);
        
        menu.add(transferMenu);
        menu.add(cutMenu);
        menu.add(rotationMenu);
        menu.add(lightMenu);
        menu.addSeparator();
        menu.add(removeMenu);
        menu.add(renameMenu);
        
        return menu;
    }
    
    protected abstract JMenuItem[] getExtraMenus();

    public void remove() {
        //TODO Remove reference to ProjectHandler
        ProjectHandler.getInstance().removeNodeFromParent(this);
        
        Enumeration<KatNode> children = children();
        ArrayList<KatNode> childrenList = new ArrayList<>();
        while(children.hasMoreElements()) {
            childrenList.add(children.nextElement());
        }
        
        for (KatNode katNode : childrenList) {
            if (katNode instanceof Displayable) {
                ((Displayable) katNode).remove();
            } else if (katNode instanceof KatViewNode) {
                ((KatViewNode) katNode).getView().close();
            }
        }
    }

    @Override
    public void setParent(MutableTreeNode newParent) {
        super.setParent(newParent);
        
        Enumeration<KatNode> children = children();
        while (children.hasMoreElements()) {
            children.nextElement().setParent(this);
        }
    }
    
    private class MenuListener implements ActionListener {
    
        @Override
        public void actionPerformed(ActionEvent e) {
            JMenuItem item  = (JMenuItem) e.getSource();
            String text = item.getText();
            switch(text) {
                case REMOVE:
                    remove();
                    break;
                case RENAME:
                    RenameDiag.promptRename(Displayable.this);
                    break;
                case CUT:
                    ProjectHandler.getInstance().insertNodeInto(new CutNode(), Displayable.this, getChildCount());
                    break;
                case LIGHT:
                    ProjectHandler.getInstance().insertNodeInto(new LightNode(), Displayable.this, getChildCount());
                    break;
                case ROTATION:
                    ProjectHandler.getInstance().insertNodeInto(new RotationNode(), Displayable.this, getChildCount());
                    break;
                default:
                    TransferFunction.Type[] types = TransferFunction.Type.values();
                    for (TransferFunction.Type type : types) {
                        if (type.getText().equals(text)) {
                            ProjectHandler.getInstance().insertNodeInto(new TransferFunctionNode(type), Displayable.this, getChildCount());
                            return;
                        }
                    }
            }
        }
        
    }

}
