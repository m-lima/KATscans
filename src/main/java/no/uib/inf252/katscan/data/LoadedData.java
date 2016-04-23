package no.uib.inf252.katscan.data;

import java.awt.EventQueue;
import java.io.File;
import java.util.HashMap;
import java.util.Set;
import javax.swing.event.EventListenerList;
import no.uib.inf252.katscan.event.DataHolderListener;

/**
 * A singleton class which holds all loaded data to be shared across views
 *
 * @author Marcelo Lima
 */
public class LoadedData {

    private final HashMap<String, VoxelMatrix> voxelMatrices;
    private final EventListenerList listenerList;

    private LoadedData() {
        voxelMatrices = new HashMap<>();
        listenerList = new EventListenerList();
    }
    
    public boolean load(String name, File file, VoxelMatrix voxelMatrix) {
        if (name == null || voxelMatrix == null || name.isEmpty()) {
            return false;
        }
        
        if (voxelMatrices.containsKey(name)) {
            return false;
        }
        
        voxelMatrices.put(name, voxelMatrix);
        fireDataLoaded(name, file.getAbsolutePath());
        
        return true;
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

    public static LoadedData getInstance() {
        return LoadedDataHolder.INSTANCE;
    }

    private static class LoadedDataHolder {
        private static final LoadedData INSTANCE = new LoadedData();
    }
 }
