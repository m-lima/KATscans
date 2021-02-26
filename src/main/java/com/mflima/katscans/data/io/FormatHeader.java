package com.mflima.katscans.data.io;

/** @author Marcelo Lima */
public class FormatHeader {

  private final int sizeX;
  private final int sizeY;
  private final int sizeZ;
  private final double ratioX;
  private final double ratioY;
  private final double ratioZ;
  private final int min;
  private final int max;

  public FormatHeader(int sizeX, int sizeY, int sizeZ, int max) {
    this(sizeX, sizeY, sizeZ, 1d, 1d, 1d, 0, max);
  }

  public FormatHeader(
      int sizeX, int sizeY, int sizeZ, double ratioX, double ratioY, double ratioZ, int max) {
    this(sizeX, sizeY, sizeZ, ratioX, ratioY, ratioZ, 0, max);
  }

  public FormatHeader(
      int sizeX,
      int sizeY,
      int sizeZ,
      double ratioX,
      double ratioY,
      double ratioZ,
      int min,
      int max) {
    this.sizeX = sizeX;
    this.sizeY = sizeY;
    this.sizeZ = sizeZ;
    this.ratioX = ratioX;
    this.ratioY = ratioY;
    this.ratioZ = ratioZ;
    this.min = min;
    this.max = max;
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

  public double getRatioX() {
    return ratioX;
  }

  public double getRatioY() {
    return ratioY;
  }

  public double getRatioZ() {
    return ratioZ;
  }

  public int getMin() {
    return min;
  }

  public int getMax() {
    return max;
  }
}
