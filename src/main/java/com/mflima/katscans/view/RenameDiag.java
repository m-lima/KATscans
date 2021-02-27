package com.mflima.katscans.view;

import com.mflima.katscans.Init;
import com.mflima.katscans.project.KatNode;
import com.mflima.katscans.project.ProjectHandler;
import com.mflima.katscans.view.component.ValidatableBorder;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** @author Marcelo Lima */
public class RenameDiag extends JDialog {

  private final KatNode node;
  private final ValidatableBorder txtFileBorder;

  public static void promptRename(KatNode node) {
    new RenameDiag(node).setVisible(true);
  }

  /** Creates new form RenameDiag */
  private RenameDiag(KatNode node) {
    super(Init.getFrameReference(), true);
    setUndecorated(true);

    initComponents();
    pnlMain.setBorder(new LineBorder(MainFrame.THEME_COLOR));
    setLocationRelativeTo(Init.getFrameReference());

    this.node = node;
    txtFileBorder = new ValidatableBorder();
    txtName.setBorder(txtFileBorder);
    txtName.setText(node.getName());

    txtName
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void insertUpdate(DocumentEvent e) {
                validateName();
              }

              @Override
              public void removeUpdate(DocumentEvent e) {
                validateName();
              }

              @Override
              public void changedUpdate(DocumentEvent e) {
                validateName();
              }
            });

    getRootPane()
        .getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
    getRootPane()
        .getActionMap()
        .put(
            "cancel",
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                dispose();
              }
            });
  }

  private boolean validateName() {
    String name = txtName.getText().trim();

    boolean valid = !name.isEmpty();
    btnOk.setEnabled(valid);
    txtFileBorder.setValid(valid);

    if (valid) {
      txtName.setBackground(UIManager.getColor("TextField.background"));
    } else {
      txtName.setBackground(ValidatableBorder.INVALID_COLOR);
    }

    return valid;
  }

  private void initComponents() {
    JLabel lblName = new JLabel();
    JButton btnCancel = new JButton();

    pnlMain = new JPanel();
    txtName = new JTextField();
    btnOk = new JButton();

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    lblName.setText("Name:");

    txtName.addActionListener(this::txtNameActionPerformed);

    btnCancel.setMnemonic('C');
    btnCancel.setText("Cancel");
    btnCancel.addActionListener(this::btnCancelActionPerformed);

    btnOk.setMnemonic('O');
    btnOk.setText("Ok");
    btnOk.addActionListener(this::btnOkActionPerformed);

    GroupLayout pnlMainLayout = new GroupLayout(pnlMain);
    pnlMain.setLayout(pnlMainLayout);
    pnlMainLayout.setHorizontalGroup(
        pnlMainLayout
            .createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(
                pnlMainLayout
                    .createSequentialGroup()
                    .addContainerGap()
                    .addGroup(
                        pnlMainLayout
                            .createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(
                                pnlMainLayout
                                    .createSequentialGroup()
                                    .addComponent(lblName)
                                    .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(txtName)
                            .addGroup(
                                GroupLayout.Alignment.TRAILING,
                                pnlMainLayout
                                    .createSequentialGroup()
                                    .addGap(0, 124, Short.MAX_VALUE)
                                    .addComponent(btnOk)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnCancel)))
                    .addContainerGap()));

    pnlMainLayout.linkSize(SwingConstants.HORIZONTAL, btnCancel, btnOk);

    pnlMainLayout.setVerticalGroup(
        pnlMainLayout
            .createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(
                pnlMainLayout
                    .createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblName)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(
                        txtName,
                        GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(
                        LayoutStyle.ComponentPlacement.RELATED,
                        GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE)
                    .addGroup(
                        pnlMainLayout
                            .createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(btnCancel)
                            .addComponent(btnOk))
                    .addContainerGap()));

    getContentPane().add(pnlMain, BorderLayout.CENTER);

    pack();
  }

  private void txtNameActionPerformed(ActionEvent evt) {
    btnOkActionPerformed(evt);
  }

  private void btnCancelActionPerformed(ActionEvent evt) {
    dispose();
  }

  private void btnOkActionPerformed(ActionEvent evt) {
    if (validateName()) {
      node.setName(txtName.getText());
      ProjectHandler.getInstance().nodeChanged(node);
      dispose();
    }
  }

  private JButton btnOk;
  private JPanel pnlMain;
  private JTextField txtName;
}
