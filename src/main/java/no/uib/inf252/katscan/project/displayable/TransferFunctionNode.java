package no.uib.inf252.katscan.project.displayable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import no.uib.inf252.katscan.data.VoxelMatrix;
import no.uib.inf252.katscan.util.TransferFunction;
import no.uib.inf252.katscan.util.TransferFunction.Type;

/**
 *
 * @author Marcelo Lima
 */
public class TransferFunctionNode extends SubGroup implements ActionListener {
    
    private transient final TransferFunction transferFunction;

    public TransferFunctionNode(Type type) {
        super("Transfer Function");
        transferFunction = new TransferFunction(type);
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
    public TransferFunction getTransferFunction() {
        return transferFunction;
    }

    @Override
    protected JMenuItem[] getExtraMenus() {
        Type[] types = TransferFunction.Type.values();
        JMenuItem[] extraMenus = new JMenuItem[types.length];
        
        for (int i = 0; i < types.length; i++) {
            Type type = types[i];
            JMenuItem item = new JMenuItem(type.getMakeText(), type.getMnemonic());
            item.addActionListener(this);
            extraMenus[i] = item;
        }
        
        return extraMenus;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JMenuItem item  = (JMenuItem) e.getSource();
        String text = item.getText();
        
        TransferFunction.Type[] types = TransferFunction.Type.values();
        for (TransferFunction.Type type : types) {
            if (type.getMakeText().equals(text)) {
                transferFunction.setType(type);
                return;
            }
        }
    }

}
