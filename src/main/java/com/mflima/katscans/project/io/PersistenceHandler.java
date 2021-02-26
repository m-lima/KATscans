package com.mflima.katscans.project.io;

import com.mflima.katscans.Init;
import com.mflima.katscans.data.io.LoadSaveHandler;
import com.mflima.katscans.project.ProjectHandler;
import com.mflima.katscans.project.ProjectNode;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/** @author Marcelo Lima */
public class PersistenceHandler {

  private static final String KAT_EXTENSION = "kat";
  private static final String LAST_LOAD = "lastProjLoad.lsl";
  private static final File AUTO_SAVE = new File("autosave." + KAT_EXTENSION);
  private static final FileFilter FILE_FILTER =
      new FileNameExtensionFilter("KAT project file", KAT_EXTENSION);

  private File lastFile;

  private PersistenceHandler() {}

  public void autoSave() {
    doSave(AUTO_SAVE, false);
  }

  public void save() {
    if (lastFile == null || !lastFile.exists() || !lastFile.canWrite()) {
      saveAs();
      return;
    }

    doSave(lastFile, true);
  }

  public void saveAs() {
    File newFile = showFileDialog(false);

    if (newFile == null) {
      return;
    }

    doSave(newFile, true);
  }

  private void doSave(File file, boolean userRequest) {
    String path = file.getAbsolutePath();
    if (!path.endsWith("." + KAT_EXTENSION)) {
      file = new File(path + "." + KAT_EXTENSION);
    }

    try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
      out.writeObject(ProjectHandler.getInstance().getRoot());
      if (userRequest) {
        saveLastLoad(file);
        lastFile = file;
      }
    } catch (IOException ex) {
      Logger.getLogger(PersistenceHandler.class.getName()).log(Level.SEVERE, null, ex);
      if (userRequest) {
        JOptionPane.showMessageDialog(
            Init.getFrameReference(), "Could not save file", "Save", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  public boolean load() {
    File newFile = showFileDialog(true);

    if (newFile == null) {
      return false;
    }

    if (!newFile.exists() || !newFile.canRead()) {
      JOptionPane.showMessageDialog(
          Init.getFrameReference(), "Could not load file", "Load", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(newFile))) {
      ProjectNode project = (ProjectNode) in.readObject();
      project.treeRecentlyLoaded();
      ProjectHandler.getInstance().setRoot(project);

      if (!newFile.getAbsolutePath().equals(AUTO_SAVE.getAbsolutePath())) {
        lastFile = newFile;
      }
      return true;
    } catch (IOException | ClassNotFoundException | ClassCastException ex) {
      Logger.getLogger(PersistenceHandler.class.getName()).log(Level.SEVERE, null, ex);
      JOptionPane.showMessageDialog(
          Init.getFrameReference(), "Could not load file", "Load", JOptionPane.ERROR_MESSAGE);
    }

    return false;
  }

  public File getLastLoad() {
    try (BufferedReader reader = new BufferedReader(new FileReader(LAST_LOAD))) {
      String path = reader.readLine();
      return new File(path);
    } catch (FileNotFoundException ignored) {
    } catch (IOException ex) {
      Logger.getLogger(LoadSaveHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  private void saveLastLoad(File lastFile) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(LAST_LOAD))) {
      writer.write(lastFile.getPath());
      writer.flush();
    } catch (IOException ex) {
      Logger.getLogger(LoadSaveHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private File showFileDialog(boolean load) {
    File currentFile = getLastLoad();
    JFileChooser fileChooser = buildFileChooser();
    fileChooser.setSelectedFile(currentFile);
    fileChooser.setMultiSelectionEnabled(false);
    fileChooser.setFileFilter(FILE_FILTER);
    int option;

    if (load) {
      option = fileChooser.showOpenDialog(Init.getFrameReference());
    } else {
      option = fileChooser.showSaveDialog(Init.getFrameReference());
    }

    if (option != JFileChooser.APPROVE_OPTION) {
      return null;
    }

    return fileChooser.getSelectedFile();
  }

  private JFileChooser buildFileChooser() {
    JFileChooser fileChooser = new JFileChooser();
    Component[] components =
        ((JPanel) ((JPanel) fileChooser.getComponents()[0]).getComponents()[0]).getComponents();
    for (Component component : components) {
      if (component instanceof JButton) {
        ((JButton) component).setBorder(null);
      } else if (component instanceof JToggleButton) {
        ((JToggleButton) component).setBorder(null);
      }
    }
    return fileChooser;
  }

  public static PersistenceHandler getInstance() {
    return PersistenceHandlerHolder.INSTANCE;
  }

  private static class PersistenceHandlerHolder {

    private static final PersistenceHandler INSTANCE = new PersistenceHandler();
  }
}
