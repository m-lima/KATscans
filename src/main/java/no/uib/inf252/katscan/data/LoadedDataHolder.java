package no.uib.inf252.katscan.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.inf252.katscan.io.DatLoadSaveHandler;

/**
 * A singleton class which holds all loaded data to be shared across views
 *
 * @author Marcelo Lima
 */
public class LoadedDataHolder {

    private VoxelMatrix voxelMatrix;

    private LoadedDataHolder() {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(new File("misc/sinusveins-256x256x166.dat"));
            DatLoadSaveHandler loadSave = new DatLoadSaveHandler();
            voxelMatrix = loadSave.loadData(stream);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LoadedDataHolder.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                Logger.getLogger(LoadedDataHolder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public VoxelMatrix getVoxelMatrix() {
        return voxelMatrix;
    }

    public static LoadedDataHolder getInstance() {
        return LoadedDataHolderHolder.INSTANCE;
    }

    private static class LoadedDataHolderHolder {
        private static final LoadedDataHolder INSTANCE = new LoadedDataHolder();
    }
 }
