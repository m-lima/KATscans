package no.uib.inf252.katscan.data.io;

import java.io.IOException;
import no.uib.inf252.katscan.data.VoxelMatrix;

import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.filechooser.FileFilter;

/**
 * @author Marcelo Lima
 */
public interface LoadSaveFormat {

    public VoxelMatrix loadData(InputStream stream, LoadSaveOptions options) throws IOException;
    public FormatHeader getHeader(InputStream stream) throws IOException;
    public void saveData(OutputStream stream, VoxelMatrix object) throws IOException;
    public String getName();
    public char getMnemonic();
    public FileFilter getFileFilter();
}
