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
import no.uib.inf252.katscan.view.dataset.DatasetBrowserPopups;

/**
 *
 * @author Marcelo Lima
 */
public class LoadSaveHandler {
    
    private static final String LAST_LOAD = "lastLoad.kat";
    
    public enum Format {
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

    private LoadSaveHandler() {
    }
    
    public void load(Format format) {
        String path = getLastLoad(format);
        
        JFileChooser fileChooser = buildFileChooser();
        fileChooser.setSelectedFile(new File(path));
        fileChooser.setFileFilter(format.getFormat().getFileFilter());
        int option = fileChooser.showOpenDialog(Init.getFrameReference());
        
        if (option != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        File file = fileChooser.getSelectedFile();
        
        try {
            VoxelMatrix voxelMatrix = format.getFormat().loadData(new FileInputStream(file));
            LoadedData.getInstance().load(file.getName(), file, voxelMatrix);
            saveLastLoad(format, file);        
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LoadSaveHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getLastLoad(Format format) {
        String path = "";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(LAST_LOAD));
            while ((path = reader.readLine()) != null) {
                if (path.startsWith(format.name())) {
                    path = path.substring(format.name().length() + 1);
                    break;
                }
            }
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            Logger.getLogger(DatasetBrowserPopups.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    Logger.getLogger(DatasetBrowserPopups.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return path;
    }
    
    private void saveLastLoad(Format format, File lastFile) {
        ArrayList<String> fullFile = new ArrayList<>();

        String path;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(LAST_LOAD));
            while ((path = reader.readLine()) != null) {
                if (path.startsWith(format.name())) {
                    continue;
                }
                fullFile.add(path);
            }
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            Logger.getLogger(DatasetBrowserPopups.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    Logger.getLogger(DatasetBrowserPopups.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(LAST_LOAD));
            writer.write(format.name());
            writer.write(':');
            writer.write(lastFile.getPath());
            for (String string : fullFile) {
                writer.newLine();
                writer.write(string);
            }
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(DatasetBrowserPopups.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    Logger.getLogger(LoadSaveHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
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

    public static LoadSaveHandler getInstance() {
        return LoadSaveHandlerHolder.INSTANCE;
    }

    private static class LoadSaveHandlerHolder {
        private static final LoadSaveHandler INSTANCE = new LoadSaveHandler();
    }
 }
