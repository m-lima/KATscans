package com.mflima.katscans.data.io;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.mflima.katscans.data.VoxelMatrix;
import com.mflima.katscans.util.FileAwareInputStream;

/** @author Marcelo Lima */
class DatFormat implements LoadSaveFormat {

  private static final int FORMAT_MAX_VALUE = 4096;

  private static final FileFilter FILE_FILTER =
      new FileNameExtensionFilter("Dat volume data", "dat");

  @Override
  public String getName() {
    return "Dat";
  }

  @Override
  public char getMnemonic() {
    return 'D';
  }

  @Override
  public FileFilter getFileFilter() {
    return FILE_FILTER;
  }

  @Override
  public int getMaxValue() {
    return FORMAT_MAX_VALUE;
  }

  @Override
  public FormatHeader getHeader(InputStream stream) throws IOException {
    ByteBuffer byteBuffer = ByteBuffer.allocate(6);
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    int sizeZ, sizeY, sizeX;
    double ratioX = 1d;
    double ratioY = 1d;
    double ratioZ = 1d;

    if (stream.read(byteBuffer.array()) > 0) {
      sizeX = byteBuffer.getShort();
      sizeY = byteBuffer.getShort();
      sizeZ = byteBuffer.getShort();
    } else {
      throw new StreamCorruptedException("Could not read dat header from the stream");
    }

    if (stream instanceof FileAwareInputStream) {
      File file = ((FileAwareInputStream) stream).getFile();

      String path = file.getAbsolutePath();
      int index = path.lastIndexOf(".dat");
      if (index > 0) {
        path = path.substring(0, index) + ".ini";
      } else {
        Logger.getLogger(DatFormat.class.getName())
            .log(Level.WARNING, "Could not find ini for " + path);
      }

      try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
        String line;
        for (int i = 0; i < 4; i++) {
          line = reader.readLine();
          if (i == 0 && !line.startsWith("[DatFile]")) {
            break;
          }

          if (line.startsWith("oldDat Spacing ")) {
            char axis = line.charAt("oldDat Spacing ".length());
            if (axis == 'X' || axis == 'x') {
              ratioX = Double.parseDouble(line.substring("oldDat Spacing X=".length()));
            } else if (axis == 'Y' || axis == 'y') {
              ratioY = Double.parseDouble(line.substring("oldDat Spacing Y=".length()));
            } else if (axis == 'Z' || axis == 'z') {
              ratioZ = Double.parseDouble(line.substring("oldDat Spacing Z=".length()));
            }
          }
        }
      } catch (IOException ex) {
        Logger.getLogger(DatFormat.class.getName()).log(Level.SEVERE, null, ex);
      }

    } else {
      Logger.getLogger(DatFormat.class.getName())
          .log(Level.WARNING, "Could not find ini given stream.");
    }

    return new FormatHeader(sizeX, sizeY, sizeZ, ratioX, ratioY, ratioZ, FORMAT_MAX_VALUE);
  }

  @Override
  public VoxelMatrix loadData(InputStream stream, LoadSaveOptions options) throws IOException {
    ByteBuffer byteBuffer = ByteBuffer.allocate(6);
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    int sizeY, sizeX;
    int optionSizeX = options.getSizeX();
    int optionSizeY = options.getSizeY();
    int optionSizeZ = options.getSizeZ();

    if (stream.read(byteBuffer.array()) > 0) {
      sizeX = byteBuffer.getShort();
      sizeY = byteBuffer.getShort();
    } else {
      throw new StreamCorruptedException("Could not read dat header from the stream");
    }

    VoxelMatrix matrix = new VoxelMatrix(options);

    ShortBuffer shortBuffer;
    byteBuffer = ByteBuffer.allocate(sizeX * 2);
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    short[] grid = matrix.getData();

    for (int z = 0; z < optionSizeZ; z++) {
      for (int y = 0; y < sizeY; y++) {
        if (stream.read(byteBuffer.array()) < sizeX * 2) {
          throw new IOException("Expected data, but could not be read");
        }

        if (y >= optionSizeY) {
          continue;
        }

        shortBuffer = byteBuffer.asShortBuffer();
        shortBuffer.get(
            grid,
            z * optionSizeY * optionSizeX + ((optionSizeY - 1) - y) * optionSizeX,
            optionSizeX);
      }
    }

    matrix.initialize();
    return matrix;
  }

  @Override
  public void saveData(OutputStream stream, VoxelMatrix grid) throws IOException {
    int sizeX = grid.getSizeX();
    int sizeY = grid.getSizeY();
    int sizeZ = grid.getSizeZ();

    ByteBuffer byteBuffer = ByteBuffer.allocate(6);
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    byteBuffer.putShort((short) sizeX);
    byteBuffer.putShort((short) sizeY);
    byteBuffer.putShort((short) sizeZ);

    short[] gridData = grid.getData();

    stream.write(byteBuffer.array());
    stream.flush();
    byteBuffer = ByteBuffer.allocate(sizeX * 2);
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

    for (int z = 0; z < sizeZ; z++) {
      for (int y = 0; y < sizeY; y++) {
        ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
        shortBuffer.put(gridData, z * sizeY * sizeX + ((sizeY - 1) - y) * sizeX, sizeX);
        stream.write(byteBuffer.array());
        stream.flush();
      }
    }
  }
}
