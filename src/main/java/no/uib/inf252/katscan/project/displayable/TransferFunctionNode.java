package no.uib.inf252.katscan.project.displayable;

import javax.swing.JMenuItem;
import javax.swing.tree.MutableTreeNode;
import no.uib.inf252.katscan.data.VoxelMatrix;
import no.uib.inf252.katscan.project.KatViewNode;
import no.uib.inf252.katscan.util.TransferFunction;
import no.uib.inf252.katscan.view.katview.KatView.Type;

/**
 *
 * @author Marcelo Lima
 */
public class TransferFunctionNode extends Displayable {
    
    private final TransferFunction transferFunction;

    public TransferFunctionNode() {
        super("Transfer Function");
        transferFunction = new TransferFunction();
    }

    public TransferFunction getTransferFunction() {
        return transferFunction;
    }
    
    @Override
    public VoxelMatrix getMatrix() {
        return getParent().getMatrix();
    }

    @Override
    public int[] getHistogram() {
        return getParent().getHistogram();
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
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
            throw new IllegalArgumentException("Can only have " + Displayable.class.getSimpleName() + " nodes as parents of " + getClass().getSimpleName() + " nodes.");
        }
    }

    @Override
    public void insert(MutableTreeNode child, int index) {
        if (!(child instanceof KatViewNode)) {
            throw new IllegalArgumentException("Can only have " + KatViewNode.class.getName() + " nodes as children of " + getClass().getSimpleName() + " nodes.");
        }
        if (((KatViewNode)child).isTransferFunctionNeeded()) {
            super.insert(child, index);
        } else {
            throw new IllegalArgumentException("Can only have " + KatViewNode.class.getName() + " nodes that use transfer functions as children of " + getClass().getSimpleName() + " nodes.");
        }
    }
    
    @Override
    protected boolean typeAcceptable(Type type) {
        return type.isTransferFunctionNeeded();
    }
    
    protected JMenuItem[] getExtraMenus() {
        return null;
    }

}
