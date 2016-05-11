package no.uib.inf252.katscan.data.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.filechooser.FileFilter;
import no.uib.inf252.katscan.data.VoxelMatrix;

/**
 * @author Marcelo Lima
 */
public interface LoadSaveFormat {
    
    public static enum Format {
        DAT(new DatFormat()),
        DCM(new DcmFormat()),
        RAW(new RawFormat());
        
        private final LoadSaveFormat format;

        private Format(LoadSaveFormat format) {
            this.format = format;
        }

        public LoadSaveFormat getFormat() {
            return format;
        }
    }

    public VoxelMatrix loadData(InputStream stream, LoadSaveOptions options) throws IOException;
    public FormatHeader getHeader(InputStream stream) throws IOException;
    public void saveData(OutputStream stream, VoxelMatrix object) throws IOException;
    public String getName();
    public char getMnemonic();
    public FileFilter getFileFilter();
    public int getMaxValue();
}
