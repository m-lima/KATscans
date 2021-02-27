package com.mflima.katscans.view;

import com.mflima.katscans.Init;
import com.mflima.katscans.data.io.FormatHeader;
import com.mflima.katscans.data.io.LoadSaveFormat.Format;
import com.mflima.katscans.data.io.LoadSaveHandler;
import com.mflima.katscans.data.io.LoadSaveOptions;
import com.mflima.katscans.view.component.ValidatableBorder;
import com.mflima.katscans.view.component.image.LoadingPanel;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** @author Marcelo Lima */
public class LoadDiag extends JDialog {

  private final ValidatableBorder txtFileBorder;
  private final LoadSaveHandler loadHandler;
  private File file;
  private FormatHeader header;
  private float oldRatioX = 1f;
  private float oldRatioY = 1f;
  private float oldRatioZ = 1f;

  private boolean loading;

  public LoadDiag(Format format) {
    super(
        Init.getFrameReference(),
        String.format("Load %s file", format.getFormat().getName()),
        true);
    loadHandler = new LoadSaveHandler(format);

    initComponents();
    ((SpinnerNumberModel) spnMax.getModel()).setMaximum(format.getFormat().getMaxValue());

    setLocationRelativeTo(Init.getFrameReference());
    setGlassPane(new LoadingPanel(true));

    txtFileBorder = new ValidatableBorder();
    setupTxtFile();
    loading = false;

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
                if (!loading) {
                  dispose();
                }
              }
            });
  }

  private void setupTxtFile() {

    txtFile.setDragEnabled(true);
    txtFile.setBorder(txtFileBorder);
    txtFile.setTransferHandler(
        new TransferHandler() {

          @Override
          public boolean canImport(TransferHandler.TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
          }

          @Override
          public boolean importData(TransferHandler.TransferSupport support) {
            DataFlavor dataFlavor = support.getDataFlavors()[0];
            try {
              if (dataFlavor.equals(DataFlavor.javaFileListFlavor)) {
                Transferable transferable = support.getTransferable();

                @SuppressWarnings("rawtypes")
                List data = (List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                File file = (File) data.get(0);
                txtFile.setText(file.getPath());
                return true;
              } else {
                Transferable transferable = support.getTransferable();
                String incoming = (String) transferable.getTransferData(DataFlavor.stringFlavor);

                if (!incoming.isEmpty()) {
                  txtFile.replaceSelection(incoming);
                  return true;
                }
              }
            } catch (Throwable th) {
              return false;
            }
            return false;
          }
        });

    txtFile
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void insertUpdate(DocumentEvent e) {
                validateFile();
              }

              @Override
              public void removeUpdate(DocumentEvent e) {
                validateFile();
              }

              @Override
              public void changedUpdate(DocumentEvent e) {
                validateFile();
              }
            });

    txtFile.setText(loadHandler.getLastLoad());
  }

  private void validateFile() {
    String path = txtFile.getText();

    if (path.startsWith("file://")) {
      path = txtFile.getText().substring(7);
      txtFile.setText(path);
      return;
    }

    file = new File(path);
    setValid(file.isFile() && file.canRead());
  }

  private void extractFileName() {
    if (file == null) {
      return;
    }

    String fullName = file.getName();
    int index = fullName.lastIndexOf('.');
    if (index > 0) {
      fullName = fullName.substring(0, index);
    }

    txtName.setText(fullName);
  }

  private void extractValues() {
    if (file == null) {
      return;
    }

    if (header == null) {
      return;
    }

    ((SpinnerNumberModel) spnSizeX.getModel()).setMaximum(header.getSizeX());
    ((SpinnerNumberModel) spnSizeY.getModel()).setMaximum(header.getSizeY());
    ((SpinnerNumberModel) spnSizeZ.getModel()).setMaximum(header.getSizeZ());

    spnSizeX.setValue(header.getSizeX());
    spnSizeY.setValue(header.getSizeY());
    spnSizeZ.setValue(header.getSizeZ());

    spnMin.setValue(header.getMin());
    spnMax.setValue(header.getMax());

    oldRatioX = (float) header.getRatioX();
    oldRatioY = (float) header.getRatioY();
    oldRatioZ = (float) header.getRatioZ();
    updateRatios(false);
  }

  private void setValid(boolean valid) {
    btnOk.setEnabled(valid);
    txtName.setEnabled(valid);
    spnMin.setEnabled(valid);
    spnMax.setEnabled(valid);
    lblSizeX.setEnabled(valid);
    lblSizeY.setEnabled(valid);
    lblSizeZ.setEnabled(valid);
    chkNormSize.setEnabled(valid);
    chkNormValues.setEnabled(valid);
    txtFileBorder.setValid(valid);
    spnSizeX.setEnabled(valid);
    spnSizeY.setEnabled(valid);
    spnSizeZ.setEnabled(valid);

    if (valid) {
      spnRatioX.setEnabled(!chkNormSize.isSelected());
      spnRatioY.setEnabled(!chkNormSize.isSelected());
      spnRatioZ.setEnabled(!chkNormSize.isSelected());

      txtFile.setBackground(UIManager.getColor("TextField.background"));
      header = loadHandler.getHeader(file);

      extractFileName();
      extractValues();
    } else {
      spnRatioX.setEnabled(false);
      spnRatioY.setEnabled(false);
      spnRatioZ.setEnabled(false);

      txtFile.setBackground(ValidatableBorder.INVALID_COLOR);

      header = null;
      file = null;
    }
  }

  private void load() {
    if (loading) {
      return;
    }

    getGlassPane().setVisible(true);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    loading = true;

    new Thread("Data Loader") {
      @Override
      public void run() {
        LoadSaveOptions options =
            new LoadSaveOptions(
                getSpinnerValue(spnSizeX).intValue(),
                getSpinnerValue(spnSizeY).intValue(),
                getSpinnerValue(spnSizeZ).intValue(),
                getSpinnerValue(spnRatioX).floatValue(),
                getSpinnerValue(spnRatioY).floatValue(),
                getSpinnerValue(spnRatioZ).floatValue(),
                getSpinnerValue(spnMin).intValue(),
                getSpinnerValue(spnMax).intValue(),
                ((Number) ((SpinnerNumberModel) spnMax.getModel()).getMaximum()).intValue(),
                chkNormValues.isSelected());
        try {
          loadHandler.load(txtName.getText(), options, file);
        } catch (Throwable th) {
          Logger.getLogger(LoadDiag.class.getName()).log(Level.SEVERE, null, th);
          setDefaultCloseOperation(DISPOSE_ON_CLOSE);
          getGlassPane().setVisible(false);
          loading = false;
          return;
        }
        dispose();
      }
    }.start();
  }

  private void updateRatios(boolean saveOldValues) {
    if (chkNormSize.isSelected()) {
      if (saveOldValues) {
        oldRatioX = getSpinnerValue(spnRatioX).floatValue();
        oldRatioY = getSpinnerValue(spnRatioY).floatValue();
        oldRatioZ = getSpinnerValue(spnRatioZ).floatValue();
      }

      if (header != null) {
        float maxSize = Math.max(header.getSizeX(), Math.max(header.getSizeY(), header.getSizeZ()));
        spnRatioX.setValue(maxSize / header.getSizeX());
        spnRatioY.setValue(maxSize / header.getSizeY());
        spnRatioZ.setValue(maxSize / header.getSizeZ());
      }
    } else {
      spnRatioX.setValue(oldRatioX);
      spnRatioY.setValue(oldRatioY);
      spnRatioZ.setValue(oldRatioZ);
    }
  }

  private Number getSpinnerValue(JSpinner spinner) {
    return (Number) spinner.getValue();
  }

  private void initComponents() {

    JPanel pnlMain = new JPanel();
    JPanel pnlMainOther = new JPanel();
    JLabel lblName = new JLabel();
    JPanel pnlValues = new JPanel();
    JLabel lblMin = new JLabel();
    JLabel lblMax = new JLabel();
    JPanel pnlMainSize = new JPanel();
    JPanel pnlRatio = new JPanel();
    JLabel lnlRatioX = new JLabel();
    JLabel lnlRatioY = new JLabel();
    JLabel lnlRatioZ = new JLabel();

    txtFile = new JTextField();
    btnOk = new JButton();
    txtName = new JTextField();
    spnMin = new JSpinner();
    spnMax = new JSpinner();
    chkNormValues = new JCheckBox();
    chkNormSize = new JCheckBox();
    spnRatioX = new JSpinner();
    spnRatioY = new JSpinner();
    spnRatioZ = new JSpinner();
    lblSizeX = new JLabel();
    lblSizeY = new JLabel();
    lblSizeZ = new JLabel();
    spnSizeX = new JSpinner();
    spnSizeY = new JSpinner();
    spnSizeZ = new JSpinner();

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    txtFile.addActionListener(this::txtFileActionPerformed);

    JButton btnSearch = new JButton();
    btnSearch.setIcon(new ImageIcon(getClass().getResource("/icons/search.png")));
    btnSearch.setBorder(null);
    btnSearch.setBorderPainted(false);
    btnSearch.setContentAreaFilled(false);
    btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnSearch.addActionListener(this::btnSearchActionPerformed);

    JButton btnCancel = new JButton();
    btnCancel.setMnemonic('C');
    btnCancel.setText("Cancel");
    btnCancel.addActionListener(this::btnCancelActionPerformed);

    btnOk.setMnemonic('O');
    btnOk.setText("Ok");
    btnOk.addActionListener(this::btnOkActionPerformed);

    pnlMain.setLayout(new GridLayout(1, 1));

    lblName.setText("Display name:");

    pnlValues.setBorder(BorderFactory.createTitledBorder("Values"));

    lblMin.setText("Minimum");

    spnMin.setModel(new SpinnerNumberModel(1, 0, null, 1));
    spnMin.addChangeListener(this::spnMinStateChanged);

    lblMax.setText("Maximum");

    spnMax.setModel(new SpinnerNumberModel());
    spnMax.addChangeListener(this::spnMaxStateChanged);

    chkNormValues.setSelected(true);
    chkNormValues.setText("Normalize values");

    GroupLayout pnlValuesLayout = new GroupLayout(pnlValues);
    pnlValues.setLayout(pnlValuesLayout);
    pnlValuesLayout.setHorizontalGroup(
        pnlValuesLayout
            .createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(
                pnlValuesLayout
                    .createSequentialGroup()
                    .addContainerGap()
                    .addGroup(
                        pnlValuesLayout
                            .createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(
                                lblMin,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                            .addComponent(
                                lblMax,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                            .addComponent(spnMin)
                            .addGroup(
                                pnlValuesLayout
                                    .createSequentialGroup()
                                    .addComponent(chkNormValues)
                                    .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(spnMax))
                    .addContainerGap()));
    pnlValuesLayout.setVerticalGroup(
        pnlValuesLayout
            .createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(
                pnlValuesLayout
                    .createSequentialGroup()
                    .addGap(8, 8, 8)
                    .addComponent(lblMin)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(
                        spnMin,
                        GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(lblMax)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(
                        spnMax,
                        GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                    .addComponent(chkNormValues)
                    .addContainerGap()));

    GroupLayout pnlMainOtherLayout = new GroupLayout(pnlMainOther);
    pnlMainOther.setLayout(pnlMainOtherLayout);
    pnlMainOtherLayout.setHorizontalGroup(
        pnlMainOtherLayout
            .createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(
                pnlMainOtherLayout
                    .createSequentialGroup()
                    .addGroup(
                        pnlMainOtherLayout
                            .createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(
                                pnlValues,
                                GroupLayout.Alignment.TRAILING,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                            .addComponent(txtName, GroupLayout.Alignment.TRAILING)
                            .addGroup(
                                pnlMainOtherLayout
                                    .createSequentialGroup()
                                    .addComponent(lblName)
                                    .addGap(0, 0, Short.MAX_VALUE)))
                    .addContainerGap()));
    pnlMainOtherLayout.setVerticalGroup(
        pnlMainOtherLayout
            .createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(
                pnlMainOtherLayout
                    .createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblName)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(
                        txtName,
                        GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(
                        pnlValues,
                        GroupLayout.DEFAULT_SIZE,
                        GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE)));

    pnlMain.add(pnlMainOther);

    pnlMainSize.setLayout(new GridLayout(0, 1));

    pnlRatio.setBorder(BorderFactory.createTitledBorder("Size"));

    chkNormSize.setText("Normalize size");
    chkNormSize.addActionListener(this::chkNormSizeActionPerformed);

    lnlRatioX.setHorizontalAlignment(SwingConstants.TRAILING);
    lnlRatioX.setText("X Ratio:");

    spnRatioX.setModel(new SpinnerNumberModel(1.0f, 1.0f, null, 0.1f));

    lnlRatioY.setHorizontalAlignment(SwingConstants.TRAILING);
    lnlRatioY.setText("Y Ratio:");

    spnRatioY.setModel(new SpinnerNumberModel(1.0f, 1.0f, null, 0.1f));

    lnlRatioZ.setHorizontalAlignment(SwingConstants.TRAILING);
    lnlRatioZ.setText("Z Ratio:");

    spnRatioZ.setModel(new SpinnerNumberModel(1.0f, 1.0f, null, 0.1f));

    lblSizeX.setText("X:");

    lblSizeY.setText("Y:");

    lblSizeZ.setText("Z:");

    spnSizeX.setModel(new SpinnerNumberModel(2, 1, null, 1));

    spnSizeY.setModel(new SpinnerNumberModel(2, 1, null, 1));

    spnSizeZ.setModel(new SpinnerNumberModel(2, 1, null, 1));

    GroupLayout pnlRatioLayout = new GroupLayout(pnlRatio);
    pnlRatio.setLayout(pnlRatioLayout);
    pnlRatioLayout.setHorizontalGroup(
        pnlRatioLayout
            .createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(
                pnlRatioLayout
                    .createSequentialGroup()
                    .addContainerGap()
                    .addGroup(
                        pnlRatioLayout
                            .createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(
                                pnlRatioLayout
                                    .createSequentialGroup()
                                    .addComponent(
                                        lnlRatioX,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(spnRatioX))
                            .addGroup(
                                pnlRatioLayout
                                    .createSequentialGroup()
                                    .addComponent(
                                        lnlRatioY,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(spnRatioY))
                            .addComponent(
                                chkNormSize,
                                GroupLayout.Alignment.TRAILING,
                                GroupLayout.DEFAULT_SIZE,
                                131,
                                Short.MAX_VALUE)
                            .addGroup(
                                pnlRatioLayout
                                    .createSequentialGroup()
                                    .addComponent(
                                        lnlRatioZ,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(spnRatioZ))
                            .addGroup(
                                pnlRatioLayout
                                    .createSequentialGroup()
                                    .addComponent(lblSizeZ)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(spnSizeZ))
                            .addGroup(
                                pnlRatioLayout
                                    .createSequentialGroup()
                                    .addGroup(
                                        pnlRatioLayout
                                            .createParallelGroup(
                                                GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(
                                                lblSizeY,
                                                GroupLayout.Alignment.LEADING,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                            .addComponent(
                                                lblSizeX,
                                                GroupLayout.Alignment.LEADING,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(
                                        pnlRatioLayout
                                            .createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addComponent(spnSizeX)
                                            .addComponent(spnSizeY))))
                    .addContainerGap()));

    pnlRatioLayout.linkSize(SwingConstants.HORIZONTAL, lnlRatioX, lnlRatioY, lnlRatioZ);

    pnlRatioLayout.setVerticalGroup(
        pnlRatioLayout
            .createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(
                pnlRatioLayout
                    .createSequentialGroup()
                    .addContainerGap()
                    .addGroup(
                        pnlRatioLayout
                            .createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(lblSizeX)
                            .addComponent(
                                spnSizeX,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(
                        pnlRatioLayout
                            .createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(lblSizeY)
                            .addComponent(
                                spnSizeY,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(
                        pnlRatioLayout
                            .createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(lblSizeZ)
                            .addComponent(
                                spnSizeZ,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(chkNormSize)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(
                        pnlRatioLayout
                            .createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(lnlRatioX)
                            .addComponent(
                                spnRatioX,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(
                        pnlRatioLayout
                            .createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(lnlRatioY)
                            .addComponent(
                                spnRatioY,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(
                        pnlRatioLayout
                            .createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(lnlRatioZ)
                            .addComponent(
                                spnRatioZ,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

    pnlMainSize.add(pnlRatio);

    pnlMain.add(pnlMainSize);

    GroupLayout layout = new GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout
            .createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(
                layout
                    .createSequentialGroup()
                    .addContainerGap()
                    .addGroup(
                        layout
                            .createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(
                                pnlMain,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                            .addGroup(
                                GroupLayout.Alignment.TRAILING,
                                layout
                                    .createSequentialGroup()
                                    .addGap(0, 0, Short.MAX_VALUE)
                                    .addComponent(
                                        btnOk,
                                        GroupLayout.PREFERRED_SIZE,
                                        46,
                                        GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnCancel))
                            .addGroup(
                                layout
                                    .createSequentialGroup()
                                    .addComponent(txtFile)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnSearch)))
                    .addContainerGap()));

    layout.linkSize(SwingConstants.HORIZONTAL, btnCancel, btnOk);

    layout.setVerticalGroup(
        layout
            .createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(
                layout
                    .createSequentialGroup()
                    .addContainerGap()
                    .addGroup(
                        layout
                            .createParallelGroup(GroupLayout.Alignment.LEADING, false)
                            .addComponent(
                                btnSearch,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                            .addComponent(txtFile))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(
                        pnlMain,
                        GroupLayout.DEFAULT_SIZE,
                        GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(
                        layout
                            .createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(btnCancel)
                            .addComponent(btnOk))
                    .addContainerGap()));

    pack();
  }

  private void btnSearchActionPerformed(ActionEvent evt) {
    File newFile = loadHandler.showLoadDialog(file);
    if (newFile != null) {
      txtFile.setText(newFile.getPath());
    }
  }

  private void btnCancelActionPerformed(ActionEvent evt) {
    dispose();
  }

  private void btnOkActionPerformed(ActionEvent evt) {
    load();
  }

  private void txtFileActionPerformed(ActionEvent evt) {
    if (file != null) {
      load();
    }
  }

  private void chkNormSizeActionPerformed(ActionEvent evt) {
    spnRatioX.setEnabled(!chkNormSize.isSelected());
    spnRatioY.setEnabled(!chkNormSize.isSelected());
    spnRatioZ.setEnabled(!chkNormSize.isSelected());

    updateRatios(true);
  }

  private void spnMaxStateChanged(ChangeEvent evt) {
    int minValue = getSpinnerValue(spnMin).intValue() + 1;
    if (getSpinnerValue(spnMax).intValue() < minValue) {
      spnMax.setValue(minValue);
    }
  }

  private void spnMinStateChanged(ChangeEvent evt) {
    int maxValue = getSpinnerValue(spnMax).intValue() - 1;
    if (getSpinnerValue(spnMin).intValue() > maxValue) {
      spnMin.setValue(maxValue);
    }
  }

  private JButton btnOk;
  private JCheckBox chkNormSize;
  private JCheckBox chkNormValues;
  private JLabel lblSizeX;
  private JLabel lblSizeY;
  private JLabel lblSizeZ;
  private JSpinner spnMax;
  private JSpinner spnMin;
  private JSpinner spnRatioX;
  private JSpinner spnRatioY;
  private JSpinner spnRatioZ;
  private JSpinner spnSizeX;
  private JSpinner spnSizeY;
  private JSpinner spnSizeZ;
  private JTextField txtFile;
  private JTextField txtName;
}
