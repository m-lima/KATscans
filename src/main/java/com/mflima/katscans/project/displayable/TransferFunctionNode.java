package com.mflima.katscans.project.displayable;

import com.mflima.katscans.model.TransferFunction;
import com.mflima.katscans.model.TransferFunction.Type;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

/** @author Marcelo Lima */
public class TransferFunctionNode extends SubGroup implements ActionListener {

  private final TransferFunction transferFunction;

  public TransferFunctionNode(Type type) {
    super(String.format("Transfer Function - %s", type.getText()));
    transferFunction = new TransferFunction(type);
  }

  @Override
  protected TransferFunctionNode internalCopy() {
    TransferFunctionNode newNode = new TransferFunctionNode(Type.SLOPE);
    newNode.transferFunction.assimilate(transferFunction);
    return newNode;
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
    JMenuItem item = (JMenuItem) e.getSource();
    String text = item.getText();

    TransferFunction.Type[] types = TransferFunction.Type.values();
    for (TransferFunction.Type type : types) {
      if (type.getMakeText().equals(text)) {
        transferFunction.setType(type);
        return;
      }
    }
  }

  @Override
  public ImageIcon getIcon() {
    return new ImageIcon(getClass().getResource("/icons/tree/transfer.png"));
  }
}
