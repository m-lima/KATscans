package com.mflima.katscans.view;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import com.mflima.katscans.Init;
import com.mflima.katscans.data.io.FormatHeader;
import com.mflima.katscans.data.io.LoadSaveFormat.Format;
import com.mflima.katscans.data.io.LoadSaveHandler;
import com.mflima.katscans.data.io.LoadSaveOptions;
import com.mflima.katscans.view.component.image.LoadingPanel;
import com.mflima.katscans.view.component.ValidatableBorder;

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

                if (incoming != null && !incoming.isEmpty()) {
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

    txtFile = new javax.swing.JTextField();
    btnSearch = new javax.swing.JButton();
    btnCancel = new javax.swing.JButton();
    btnOk = new javax.swing.JButton();
    pnlMain = new javax.swing.JPanel();
    pnlMainOther = new javax.swing.JPanel();
    lblName = new javax.swing.JLabel();
    txtName = new javax.swing.JTextField();
    pnlValues = new javax.swing.JPanel();
    lblMin = new javax.swing.JLabel();
    spnMin = new javax.swing.JSpinner();
    lblMax = new javax.swing.JLabel();
    spnMax = new javax.swing.JSpinner();
    chkNormValues = new javax.swing.JCheckBox();
    pnlMainSize = new javax.swing.JPanel();
    pnlRatio = new javax.swing.JPanel();
    chkNormSize = new javax.swing.JCheckBox();
    lnlRatioX = new javax.swing.JLabel();
    spnRatioX = new javax.swing.JSpinner();
    lnlRatioY = new javax.swing.JLabel();
    spnRatioY = new javax.swing.JSpinner();
    lnlRatioZ = new javax.swing.JLabel();
    spnRatioZ = new javax.swing.JSpinner();
    lblSizeX = new javax.swing.JLabel();
    lblSizeY = new javax.swing.JLabel();
    lblSizeZ = new javax.swing.JLabel();
    spnSizeX = new javax.swing.JSpinner();
    spnSizeY = new javax.swing.JSpinner();
    spnSizeZ = new javax.swing.JSpinner();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

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

    javax.swing.GroupLayout pnlRatioLayout = new javax.swing.GroupLayout(pnlRatio);
    pnlRatio.setLayout(pnlRatioLayout);
    pnlRatioLayout.setHorizontalGroup(
        pnlRatioLayout
            .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(
                pnlRatioLayout
                    .createSequentialGroup()
                    .addContainerGap()
                    .addGroup(
                        pnlRatioLayout
                            .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(
                                pnlRatioLayout
                                    .createSequentialGroup()
                                    .addComponent(
                                        lnlRatioX,
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE)
                                    .addPreferredGap(
                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(spnRatioX))
                            .addGroup(
                                pnlRatioLayout
                                    .createSequentialGroup()
                                    .addComponent(
                                        lnlRatioY,
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE)
                                    .addPreferredGap(
                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(spnRatioY))
                            .addComponent(
                                chkNormSize,
                                javax.swing.GroupLayout.Alignment.TRAILING,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                131,
                                Short.MAX_VALUE)
                            .addGroup(
                                pnlRatioLayout
                                    .createSequentialGroup()
                                    .addComponent(
                                        lnlRatioZ,
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE)
                                    .addPreferredGap(
                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(spnRatioZ))
                            .addGroup(
                                pnlRatioLayout
                                    .createSequentialGroup()
                                    .addComponent(lblSizeZ)
                                    .addPreferredGap(
                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(spnSizeZ))
                            .addGroup(
                                pnlRatioLayout
                                    .createSequentialGroup()
                                    .addGroup(
                                        pnlRatioLayout
                                            .createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(
                                                lblSizeY,
                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                            .addComponent(
                                                lblSizeX,
                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE))
                                    .addPreferredGap(
                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(
                                        pnlRatioLayout
                                            .createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(spnSizeX)
                                            .addComponent(spnSizeY))))
                    .addContainerGap()));

    pnlRatioLayout.linkSize(
        javax.swing.SwingConstants.HORIZONTAL,
        new java.awt.Component[] {lnlRatioX, lnlRatioY, lnlRatioZ});

    pnlRatioLayout.setVerticalGroup(
        pnlRatioLayout
            .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(
                pnlRatioLayout
                    .createSequentialGroup()
                    .addContainerGap()
                    .addGroup(
                        pnlRatioLayout
                            .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblSizeX)
                            .addComponent(
                                spnSizeX,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(
                        pnlRatioLayout
                            .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblSizeY)
                            .addComponent(
                                spnSizeY,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(
                        pnlRatioLayout
                            .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblSizeZ)
                            .addComponent(
                                spnSizeZ,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(chkNormSize)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(
                        pnlRatioLayout
                            .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lnlRatioX)
                            .addComponent(
                                spnRatioX,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(
                        pnlRatioLayout
                            .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lnlRatioY)
                            .addComponent(
                                spnRatioY,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(
                        pnlRatioLayout
                            .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lnlRatioZ)
                            .addComponent(
                                spnRatioZ,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

    pnlMainSize.add(pnlRatio);

    pnlMain.add(pnlMainSize);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout
            .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(
                layout
                    .createSequentialGroup()
                    .addContainerGap()
                    .addGroup(
                        layout
                            .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(
                                pnlMain,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                            .addGroup(
                                javax.swing.GroupLayout.Alignment.TRAILING,
                                layout
                                    .createSequentialGroup()
                                    .addGap(0, 0, Short.MAX_VALUE)
                                    .addComponent(
                                        btnOk,
                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                        46,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(
                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnCancel))
                            .addGroup(
                                layout
                                    .createSequentialGroup()
                                    .addComponent(txtFile)
                                    .addPreferredGap(
                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnSearch)))
                    .addContainerGap()));

    layout.linkSize(
        javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnCancel, btnOk});

    layout.setVerticalGroup(
        layout
            .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(
                layout
                    .createSequentialGroup()
                    .addContainerGap()
                    .addGroup(
                        layout
                            .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(
                                btnSearch,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                            .addComponent(txtFile))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(
                        pnlMain,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(
                        layout
                            .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnCancel)
                            .addComponent(btnOk))
                    .addContainerGap()));

    pack();
  } // </editor-fold>//GEN-END:initComponents

  private void btnSearchActionPerformed(
      java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnSearchActionPerformed
    File newFile = loadHandler.showLoadDialog(file);
    if (newFile != null) {
      txtFile.setText(newFile.getPath());
    }
  } // GEN-LAST:event_btnSearchActionPerformed

  private void btnCancelActionPerformed(
      java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnCancelActionPerformed
    dispose();
  } // GEN-LAST:event_btnCancelActionPerformed

  private void btnOkActionPerformed(
      java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnOkActionPerformed
    load();
  } // GEN-LAST:event_btnOkActionPerformed

  private void txtFileActionPerformed(
      java.awt.event.ActionEvent evt) { // GEN-FIRST:event_txtFileActionPerformed
    if (file != null) {
      load();
    }
  } // GEN-LAST:event_txtFileActionPerformed

  private void chkNormSizeActionPerformed(
      java.awt.event.ActionEvent evt) { // GEN-FIRST:event_chkNormSizeActionPerformed
    spnRatioX.setEnabled(!chkNormSize.isSelected());
    spnRatioY.setEnabled(!chkNormSize.isSelected());
    spnRatioZ.setEnabled(!chkNormSize.isSelected());

    updateRatios(true);
  } // GEN-LAST:event_chkNormSizeActionPerformed

  private void spnMaxStateChanged(
      javax.swing.event.ChangeEvent evt) { // GEN-FIRST:event_spnMaxStateChanged
    int minValue = getSpinnerValue(spnMin).intValue() + 1;
    if (getSpinnerValue(spnMax).intValue() < minValue) {
      spnMax.setValue(minValue);
    }
  } // GEN-LAST:event_spnMaxStateChanged

  private void spnMinStateChanged(
      javax.swing.event.ChangeEvent evt) { // GEN-FIRST:event_spnMinStateChanged
    int maxValue = getSpinnerValue(spnMax).intValue() - 1;
    if (getSpinnerValue(spnMin).intValue() > maxValue) {
      spnMin.setValue(maxValue);
    }
  } // GEN-LAST:event_spnMinStateChanged

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btnCancel;
  private javax.swing.JButton btnOk;
  private javax.swing.JButton btnSearch;
  private javax.swing.JCheckBox chkNormSize;
  private javax.swing.JCheckBox chkNormValues;
  private javax.swing.JLabel lblMax;
  private javax.swing.JLabel lblMin;
  private javax.swing.JLabel lblName;
  private javax.swing.JLabel lblSizeX;
  private javax.swing.JLabel lblSizeY;
  private javax.swing.JLabel lblSizeZ;
  private javax.swing.JLabel lnlRatioX;
  private javax.swing.JLabel lnlRatioY;
  private javax.swing.JLabel lnlRatioZ;
  private javax.swing.JPanel pnlMain;
  private javax.swing.JPanel pnlMainOther;
  private javax.swing.JPanel pnlMainSize;
  private javax.swing.JPanel pnlRatio;
  private javax.swing.JPanel pnlValues;
  private javax.swing.JSpinner spnMax;
  private javax.swing.JSpinner spnMin;
  private javax.swing.JSpinner spnRatioX;
  private javax.swing.JSpinner spnRatioY;
  private javax.swing.JSpinner spnRatioZ;
  private javax.swing.JSpinner spnSizeX;
  private javax.swing.JSpinner spnSizeY;
  private javax.swing.JSpinner spnSizeZ;
  private javax.swing.JTextField txtFile;
  private javax.swing.JTextField txtName;
  // End of variables declaration//GEN-END:variables
}
