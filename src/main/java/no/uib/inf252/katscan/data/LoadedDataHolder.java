package no.uib.inf252.katscan.data;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import no.uib.inf252.katscan.event.DataHolderListener;
import no.uib.inf252.katscan.data.io.DatLoadSaveHandler;

/**
 * A singleton class which holds all loaded data to be shared across views
 *
 * @author Marcelo Lima
 */
public class LoadedDataHolder {

    private HashMap<String, VoxelMatrix> voxelMatrices;
    private EventListenerList listenerList;

    private LoadedDataHolder() {
        voxelMatrices = new HashMap<>();
        listenerList = new EventListenerList();
    }
    
    public boolean load(String name, File file) {
        if (name == null || file == null || name.isEmpty()) {
            return false;
        }
        
        if (voxelMatrices.containsKey(name)) {
            return false;
        }
        
        boolean success = true;
        
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            DatLoadSaveHandler loadSave = new DatLoadSaveHandler();
            voxelMatrices.put(name, loadSave.loadData(stream));
        } catch (FileNotFoundException ex) {
            success = false;
            Logger.getLogger(LoadedDataHolder.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                Logger.getLogger(LoadedDataHolder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (success) {
            fireDataLoaded(name, file.getAbsolutePath());
        }
        return success;
    }
    
    public boolean unload(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        if (voxelMatrices.remove(name) != null) {
            fireDataRemoved(name);
            System.gc();
            return true;
        }
        
        return false;
    }
    
    public void unloadAll() {
        Set<String> datasets = voxelMatrices.keySet();
        for (String dataset : datasets) {
            unload(dataset);
        }
    }
    
    public Set<String> getDatasetNames() {
        return voxelMatrices.keySet();
    }
    
    public VoxelMatrix getDataset(String name) {
        return voxelMatrices.get(name);
    }
    
    public synchronized void addDataHolderListener(DataHolderListener listener) {
        if (listener == null) {
            return;
        }
        
        listenerList.add(DataHolderListener.class, listener);
    }
    
    public synchronized void removeDataHolderListener(DataHolderListener listener) {
        if (listener == null) {
            return;
        }
        
        listenerList.remove(DataHolderListener.class, listener);
    }

    private void fireDataLoaded(final String name, final String file) {
        DataHolderListener[] listeners = listenerList.getListeners(DataHolderListener.class);

        for (final DataHolderListener listener : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.dataAdded(name, file);
                }
            });
        }
    }

    private void fireDataRemoved(final String name) {
        DataHolderListener[] listeners = listenerList.getListeners(DataHolderListener.class);

        for (final DataHolderListener listener : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.dataRemoved(name);
                }
            });
        }
    }

    public static LoadedDataHolder getInstance() {
        return LoadedDataHolderHolder.INSTANCE;
    }

    private static class LoadedDataHolderHolder {
        private static final LoadedDataHolder INSTANCE = new LoadedDataHolder();
    }
 }
