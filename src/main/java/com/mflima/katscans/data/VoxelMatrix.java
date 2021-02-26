package com.mflima.katscans.data;

import com.mflima.katscans.data.io.LoadSaveOptions;

/**
 * @author Marcelo Lima
 */
public class VoxelMatrix {

  private final int sizeX, sizeY, sizeZ;
  private final short[] grid;
  private final int[] histogram;
  private final float[] ratio;
  private int minValue;
  private int maxValue;
  private final int maxFormatValue;
  private final boolean normalized;

  private boolean initialized;

  private VoxelMatrix(VoxelMatrix matrix) {
    if (!matrix.initialized) {
      throw new RuntimeException(VoxelMatrix.class.getName() + " has not been initialized yet.");
    }

    sizeX = matrix.sizeX;
    sizeY = matrix.sizeY;
    sizeZ = matrix.sizeZ;

    minValue = matrix.minValue;
    maxValue = matrix.maxValue;
    maxFormatValue = matrix.maxFormatValue;
    normalized = matrix.normalized;

    grid = new short[matrix.grid.length];
    histogram = new int[matrix.histogram.length];
    ratio = new float[matrix.ratio.length];

    System.arraycopy(matrix.grid, 0, grid, 0, grid.length);
    System.arraycopy(matrix.histogram, 0, histogram, 0, histogram.length);
    System.arraycopy(matrix.ratio, 0, ratio, 0, ratio.length);

    initialized = true;
  }

  public VoxelMatrix(LoadSaveOptions options) {
    sizeX = options.getSizeX();
    sizeY = options.getSizeY();
    sizeZ = options.getSizeZ();

    if (sizeZ <= 0) {
      throw new IllegalArgumentException("The size must be larger than zero, but Z was " + sizeZ);
    }
    if (sizeY <= 0) {
      throw new IllegalArgumentException("The size must be larger than zero, but Y was " + sizeY);
    }
    if (sizeX <= 0) {
      throw new IllegalArgumentException("The size must be larger than zero, but X was " + sizeX);
    }

    grid = new short[sizeZ * sizeY * sizeX];

    minValue = options.getMinValue();
    maxValue = options.getMaxValue();
    maxFormatValue = options.getMaxFormatValue();
    normalized = options.isNormalizeValues();

    histogram = new int[maxValue];

    int maxSize = Math.max(sizeX, Math.max(sizeY, sizeZ));
    ratio = new float[]{sizeX * options.getRatioX() / maxSize,
        sizeY * options.getRatioY() / maxSize,
        sizeZ * options.getRatioZ() / maxSize};

    initialized = false;
  }

  public void initialize() {
    for (int i = 0; i < grid.length; i++) {
      int value = grid[i] & 0xFFFF;

      if (value < minValue) {
        value = 0;
      } else if (value >= maxValue) {
        value = 0;
      }

      grid[i] = (short) value;

      histogram[value & 0xFFFF]++;
    }

    if (!normalized) {
      minValue = 0;
      maxValue = maxFormatValue;
    }

    double normRatio = 65535d / (maxValue - minValue);

    for (int i = 0; i < grid.length; i++) {
      int value = grid[i] & 0xFFFF;
      value -= minValue;
      if (value < 0) {
        value = 0;
      }
      value *= normRatio;

      grid[i] = (short) value;
    }

    initialized = true;
  }

  public VoxelMatrix copy() {
    return new VoxelMatrix(this);
  }

  public int getSizeX() {
    return sizeX;
  }

  public int getSizeY() {
    return sizeY;
  }

  public int getSizeZ() {
    return sizeZ;
  }

  public int[] getHistogram() {
    if (!initialized) {
      throw new RuntimeException(VoxelMatrix.class.getName() + " has not been initialized yet.");
    }
    return histogram;
  }

  public float[] getRatio() {
    return ratio;
  }

  public short getValue(int x, int y, int z) {
    return grid[z * sizeY * sizeX + y * sizeX + x];
  }

  public short[] getData() {
    return grid;
  }

  public int getMinValue() {
    if (!initialized) {
      throw new RuntimeException(VoxelMatrix.class.getName() + " has not been initialized yet.");
    }
    return minValue;
  }

  public int getMaxValue() {
    if (!initialized) {
      throw new RuntimeException(VoxelMatrix.class.getName() + " has not been initialized yet.");
    }
    return maxValue;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 89 * hash + System.identityHashCode(this.grid);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final VoxelMatrix other = (VoxelMatrix) obj;

    return this.grid == other.grid;
  }

}
