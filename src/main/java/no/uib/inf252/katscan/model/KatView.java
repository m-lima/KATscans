package no.uib.inf252.katscan.model;

import javax.swing.tree.MutableTreeNode;
import net.infonode.docking.View;

/**
 *
 * @author Marcelo Lima
 */
public class KatView extends KatNode{
    
    private View view;
    
    public View getView() {
        return view;
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public Displayable getParent() {
        return (Displayable) super.getParent();
    }

    @Override
    public void setParent(MutableTreeNode newParent) {
        if (newParent instanceof Displayable) {
            super.setParent(newParent);
        } else {
            throw new ClassCastException("KatView nodes can only be children of Displayable nodes.");
        }
    }

}
