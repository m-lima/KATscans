package com.mflima.katscans.data.io;

import com.mflima.katscans.data.VoxelMatrix;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.filechooser.FileFilter;

/** @author Marcelo Lima */
public interface LoadSaveFormat {

  enum Format {
    DAT(new DatFormat()),
    DCM(new DcmFormat()),
    RAW(new RawFormat()),
    GRID(new GridFormat());

    private final LoadSaveFormat format;

    Format(LoadSaveFormat format) {
      this.format = format;
    }

    public LoadSaveFormat getFormat() {
      return format;
    }
  }

  VoxelMatrix loadData(InputStream stream, LoadSaveOptions options) throws IOException;

  FormatHeader getHeader(InputStream stream) throws IOException;

  void saveData(OutputStream stream, VoxelMatrix object) throws IOException;

  String getName();

  char getMnemonic();

  FileFilter getFileFilter();

  int getMaxValue();
}
