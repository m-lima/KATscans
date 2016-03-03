package no.uib.inf252.katscan.io;

import no.uib.inf252.katscan.data.VoxelMatrix;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Marcelo Lima
 */
public interface LoadSaveHandler {

    public VoxelMatrix loadData(InputStream stream);
    public void saveData(OutputStream stream, VoxelMatrix object);
}
