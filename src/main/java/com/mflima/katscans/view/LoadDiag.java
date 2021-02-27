package com.mflima.katscans.view;

import com.mflima.katscans.Init;
import com.mflima.katscans.data.io.FormatHeader;
import com.mflima.katscans.data.io.LoadSaveFormat.Format;
import com.mflima.katscans.data.io.LoadSaveHandler;
import com.mflima.katscans.data.io.LoadSaveOptions;
import com.mflima.katscans.view.component.ValidatableBorder;
import com.mflima.katscans.view.component.image.LoadingPanel;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
public class LoadDiag extends javax.swing.JDialog {

  private final ValidatableBorder txtFileBorder;
  private final LoadSaveHandler loadHandler;
  private File file;
  private FormatHeader header;
  private float oldRatioX = 1f;
  private float oldRatioY = 1f;
  private float oldRatioZ = 1f;

  private boolean loading;

  /** Creates new form LoadDataFileDiag */
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

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT
   * modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    txtFile = new JTextField();
    btnSearch = new JButton();
    btnCancel = new JButton();
    btnOk = new JButton();
    pnlMain = new JPanel();
    pnlMainOther = new JPanel();
    lblName = new JLabel();
    txtName = new JTextField();
    pnlValues = new JPanel();
    lblMin = new JLabel();
    spnMin = new JSpinner();
    lblMax = new JLabel();
    spnMax = new JSpinner();
    chkNormValues = new JCheckBox();
    pnlMainSize = new JPanel();
    pnlRatio = new JPanel();
    chkNormSize = new JCheckBox();
    lnlRatioX = new JLabel();
    spnRatioX = new JSpinner();
    lnlRatioY = new JLabel();
    spnRatioY = new JSpinner();
    lnlRatioZ = new JLabel();
    spnRatioZ = new JSpinner();
    lblSizeX = new JLabel();
    lblSizeY = new JLabel();
    lblSizeZ = new JLabel();
    spnSizeX = new JSpinner();
    spnSizeY = new JSpinner();
    spnSizeZ = new JSpinner();

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    txtFile.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            txtFileActionPerformed(evt);
          }
        });

    btnSearch.setIcon(
        new javax.swing.ImageIcon(getClass().getResource("/icons/search.png"))); // NOI18N
    btnSearch.setBorder(null);
    btnSearch.setBorderPainted(false);
    btnSearch.setContentAreaFilled(false);
    btnSearch.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    btnSearch.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnSearchActionPerformed(evt);
          }
        });

    btnCancel.setMnemonic('C');
    btnCancel.setText("Cancel");
    btnCancel.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnCancelActionPerformed(evt);
          }
        });

    btnOk.setMnemonic('O');
    btnOk.setText("Ok");
    btnOk.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnOkActionPerformed(evt);
          }
        });

    pnlMain.setLayout(new java.awt.GridLayout(1, 1));

    lblName.setText("Display name:");

    pnlValues.setBorder(javax.swing.BorderFactory.createTitledBorder("Values"));

    lblMin.setText("Minimum");

    spnMin.setModel(new javax.swing.SpinnerNumberModel(1, 0, null, 1));
    spnMin.addChangeListener(
        new javax.swing.event.ChangeListener() {
          public void stateChanged(javax.swing.event.ChangeEvent evt) {
            spnMinStateChanged(evt);
          }
        });

    lblMax.setText("Maximum");

    spnMax.setModel(new javax.swing.SpinnerNumberModel());
    spnMax.addChangeListener(
        new javax.swing.event.ChangeListener() {
          public void stateChanged(javax.swing.event.ChangeEvent evt) {
            spnMaxStateChanged(evt);
          }
        });

    chkNormValues.setSelected(true);
    chkNormValues.setText("Normalize values");

    javax.swing.GroupLayout pnlValuesLayout = new javax.swing.GroupLayout(pnlValues);
    pnlValues.setLayout(pnlValuesLayout);
    pnlValuesLayout.setHorizontalGroup(
        pnlValuesLayout
            .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(
                pnlValuesLayout
                    .createSequentialGroup()
                    .addContainerGap()
                    .addGroup(
                        pnlValuesLayout
                            .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(
                                lblMin,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                            .addComponent(
                                lblMax,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
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
            .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(
                pnlValuesLayout
                    .createSequentialGroup()
                    .addGap(8, 8, 8)
                    .addComponent(lblMin)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(
                        spnMin,
                        javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(lblMax)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(
                        spnMax,
                        javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(
                        javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                    .addComponent(chkNormValues)
                    .addContainerGap()));

    javax.swing.GroupLayout pnlMainOtherLayout = new javax.swing.GroupLayout(pnlMainOther);
    pnlMainOther.setLayout(pnlMainOtherLayout);
    pnlMainOtherLayout.setHorizontalGroup(
        pnlMainOtherLayout
            .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(
                pnlMainOtherLayout
                    .createSequentialGroup()
                    .addGroup(
                        pnlMainOtherLayout
                            .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(
                                pnlValues,
                                javax.swing.GroupLayout.Alignment.TRAILING,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                            .addComponent(txtName, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(
                                pnlMainOtherLayout
                                    .createSequentialGroup()
                                    .addComponent(lblName)
                                    .addGap(0, 0, Short.MAX_VALUE)))
                    .addContainerGap()));
    pnlMainOtherLayout.setVerticalGroup(
        pnlMainOtherLayout
            .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(
                pnlMainOtherLayout
                    .createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblName)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(
                        txtName,
                        javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(
                        pnlValues,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE)));

    pnlMain.add(pnlMainOther);

    pnlMainSize.setLayout(new java.awt.GridLayout(0, 1));

    pnlRatio.setBorder(javax.swing.BorderFactory.createTitledBorder("Size"));

    chkNormSize.setText("Normalize size");
    chkNormSize.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            chkNormSizeActionPerformed(evt);
          }
        });

    lnlRatioX.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    lnlRatioX.setText("X Ratio:");

    spnRatioX.setModel(new javax.swing.SpinnerNumberModel(1.0f, 1.0f, null, 0.1f));

    lnlRatioY.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    lnlRatioY.setText("Y Ratio:");

    spnRatioY.setModel(new javax.swing.SpinnerNumberModel(1.0f, 1.0f, null, 0.1f));

    lnlRatioZ.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    lnlRatioZ.setText("Z Ratio:");

    spnRatioZ.setModel(new javax.swing.SpinnerNumberModel(1.0f, 1.0f, null, 0.1f));

    lblSizeX.setText("X:");

    lblSizeY.setText("Y:");

    lblSizeZ.setText("Z:");

    spnSizeX.setModel(new javax.swing.SpinnerNumberModel(2, 1, null, 1));

    spnSizeY.setModel(new javax.swing.SpinnerNumberModel(2, 1, null, 1));

    spnSizeZ.setModel(new javax.swing.SpinnerNumberModel(2, 1, null, 1));

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

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JButton btnCancel;
  private JButton btnOk;
  private JButton btnSearch;
  private JCheckBox chkNormSize;
  private JCheckBox chkNormValues;
  private JLabel lblMax;
  private JLabel lblMin;
  private JLabel lblName;
  private JLabel lblSizeX;
  private JLabel lblSizeY;
  private JLabel lblSizeZ;
  private JLabel lnlRatioX;
  private JLabel lnlRatioY;
  private JLabel lnlRatioZ;
  private JPanel pnlMain;
  private JPanel pnlMainOther;
  private JPanel pnlMainSize;
  private JPanel pnlRatio;
  private JPanel pnlValues;
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
  // End of variables declaration//GEN-END:variables
}
