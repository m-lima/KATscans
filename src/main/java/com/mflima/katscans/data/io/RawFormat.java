package com.mflima.katscans.data.io;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.mflima.katscans.data.VoxelMatrix;

/** @author Marcelo Lima */
class RawFormat implements LoadSaveFormat {

  private static final int FORMAT_MAX_VALUE = 255;

  private static final FileFilter FILE_FILTER =
      new FileNameExtensionFilter("Raw volume data", "raw");

  @Override
  public String getName() {
    return "Raw";
  }

  @Override
  public char getMnemonic() {
    return 'R';
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
    byteBuffer.order(ByteOrder.BIG_ENDIAN);
    int sizeZ, sizeY, sizeX;

    if (stream.read(byteBuffer.array()) > 0) {
      sizeX = byteBuffer.getShort();
      sizeY = byteBuffer.getShort();
      sizeZ = byteBuffer.getShort();
    } else {
      throw new StreamCorruptedException("Could not read dat header from the stream");
    }

    return new FormatHeader(sizeX, sizeY, sizeZ, FORMAT_MAX_VALUE);
  }

  @Override
  public VoxelMatrix loadData(InputStream stream, LoadSaveOptions options) throws IOException {
    ByteBuffer byteBuffer = ByteBuffer.allocate(6);
    byteBuffer.order(ByteOrder.BIG_ENDIAN);
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

    byteBuffer = ByteBuffer.allocate(sizeX);
    byteBuffer.order(ByteOrder.BIG_ENDIAN);
    short[] grid = matrix.getData();

    for (int z = 0; z < optionSizeZ; z++) {
      for (int y = 0; y < sizeY; y++) {
        if (stream.read(byteBuffer.array()) < sizeX) {
          throw new IOException("Expected data, but could not be read");
        }

        if (y >= optionSizeY) {
          continue;
        }

        byte[] byteArray = byteBuffer.array();
        for (int i = 0; i < byteArray.length; i++) {
          grid[
                  ((optionSizeZ - 1) - z) * optionSizeY * optionSizeX
                      + ((optionSizeY - 1) - y) * optionSizeX
                      + i] =
              byteArray[i];
        }
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
    byteBuffer.order(ByteOrder.BIG_ENDIAN);
    byteBuffer.putShort((short) sizeX);
    byteBuffer.putShort((short) sizeY);
    byteBuffer.putShort((short) sizeZ);

    short[] gridData = grid.getData();

    stream.write(byteBuffer.array());
    stream.flush();
    byteBuffer = ByteBuffer.allocate(sizeX * 2);
    byteBuffer.order(ByteOrder.BIG_ENDIAN);

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
