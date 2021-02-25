package com.mflima.katscans.data.io;

import java.io.Serializable;

/**
 * @author Marcelo Lima
 */
public class LoadSaveOptions implements Serializable {

  private final int sizeX;
  private final int sizeY;
  private final int sizeZ;

  private final float ratioX;
  private final float ratioY;
  private final float ratioZ;

  private final int minValue;
  private final int maxValue;
  private final int maxFormatValue;
  private final boolean normalizeValues;

  public LoadSaveOptions(int sizeX, int sizeY, int sizeZ, float ratioX, float ratioY, float ratioZ,
      int minValue, int maxValue, int maxFormatValue, boolean normalizeValues) {
    this.sizeX = sizeX;
    this.sizeY = sizeY;
    this.sizeZ = sizeZ;
    this.ratioX = ratioX;
    this.ratioY = ratioY;
    this.ratioZ = ratioZ;
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.maxFormatValue = maxFormatValue;
    this.normalizeValues = normalizeValues;
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

  public float getRatioX() {
    return ratioX;
  }

  public float getRatioY() {
    return ratioY;
  }

  public float getRatioZ() {
    return ratioZ;
  }

  public int getMinValue() {
    return minValue;
  }

  public int getMaxValue() {
    return maxValue;
  }

  public int getMaxFormatValue() {
    return maxFormatValue;
  }

  public boolean isNormalizeValues() {
    return normalizeValues;
  }

}
