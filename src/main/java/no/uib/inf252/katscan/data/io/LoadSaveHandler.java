package no.uib.inf252.katscan.data.io;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import no.uib.inf252.katscan.Init;
import no.uib.inf252.katscan.data.LoadedData;
import no.uib.inf252.katscan.data.VoxelMatrix;
import no.uib.inf252.katscan.util.FileAwareInputStream;

/**
 *
 * @author Marcelo Lima
 */
public class LoadSaveHandler {
    
    private static final String LAST_LOAD = "lastLoad.kat";
    
    public static enum Format {
        DAT(new DatFormat()),
        RAW(new RawFormat());
        
        private final LoadSaveFormat format;

        private Format(LoadSaveFormat format) {
            this.format = format;
        }

        public LoadSaveFormat getFormat() {
            return format;
        }
    }
    
    private final Format format;
    
    public LoadSaveHandler(Format format) {
        this.format = format;
    }

    public boolean load(String name, LoadSaveOptions options, File file) {        
        try (FileAwareInputStream input = new FileAwareInputStream(file)) {
            VoxelMatrix voxelMatrix = format.getFormat().loadData(input, options);
            if (LoadedData.getInstance().load(name, file, voxelMatrix)) {
                saveLastLoad(file);
                return true;
            }
        } catch (IOException ex) {
            Logger.getLogger(LoadSaveHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    public FormatHeader getHeader(File file) {
        try (FileAwareInputStream input = new FileAwareInputStream(file)) {
            return format.getFormat().getHeader(input);
        } catch (IOException ex) {
            Logger.getLogger(LoadSaveHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getLastLoad() {
        String path = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(LAST_LOAD))) {
            while ((path = reader.readLine()) != null) {
                if (path.startsWith(format.name())) {
                    path = path.substring(format.name().length() + 1);
                    break;
                }
            }
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            Logger.getLogger(LoadSaveHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return path;
    }
    
    private void saveLastLoad(File lastFile) {
        ArrayList<String> fullFile = new ArrayList<>();

        String path;
        try (BufferedReader reader = new BufferedReader(new FileReader(LAST_LOAD))) {
            while ((path = reader.readLine()) != null) {
                if (path.startsWith(format.name())) {
                    continue;
                }
                fullFile.add(path);
            }
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            Logger.getLogger(LoadSaveHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LAST_LOAD))) {
            writer.write(format.name());
            writer.write(':');
            writer.write(lastFile.getPath());
            for (String string : fullFile) {
                writer.newLine();
                writer.write(string);
            }
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(LoadSaveHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public File showLoadDialog(File currentFile) {
        if (currentFile == null) {
            currentFile = new File(getLastLoad());
        }
        
        JFileChooser fileChooser = buildFileChooser();
        fileChooser.setSelectedFile(currentFile);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(format.getFormat().getFileFilter());
        int option = fileChooser.showOpenDialog(Init.getFrameReference());
        
        if (option != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        
        return fileChooser.getSelectedFile();
    }

    private JFileChooser buildFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        Component[] components = ((JPanel)((JPanel)fileChooser.getComponents()[0]).getComponents()[0]).getComponents();
        for (Component component : components) {
            if (component instanceof JButton) {
                ((JButton) component).setBorder(null);
            } else if (component instanceof JToggleButton) {
                ((JToggleButton) component).setBorder(null);
            }
        }
        return fileChooser;
    }

 }
