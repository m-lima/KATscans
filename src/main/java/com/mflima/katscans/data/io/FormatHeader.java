package com.mflima.katscans.data.io;

/**
 *
 * @author Marcelo Lima
 */
public class FormatHeader {
    
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final double ratioX;
    private final double ratioY;
    private final double ratioZ;

    public FormatHeader(int sizeX, int sizeY, int sizeZ, double ratioX, double ratioY, double ratioZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.ratioX = ratioX;
        this.ratioY = ratioY;
        this.ratioZ = ratioZ;
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

}
