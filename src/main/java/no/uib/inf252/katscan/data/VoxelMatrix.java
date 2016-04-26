package no.uib.inf252.katscan.data;

import java.io.Serializable;
import no.uib.inf252.katscan.data.io.LoadSaveOptions;

/**
 * @author Marcelo Lima
 */
public class VoxelMatrix implements Serializable {

    private final int sizeX, sizeY, sizeZ;
    private final short[] grid;
    private final int[] histogram;
    private final float[] ratio;
    private final int maxFormatValue;
    private short maxValue;

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

        this.maxFormatValue = options.getMaxValue();
        histogram = new int[this.maxFormatValue];

        int maxSize = Math.max(sizeX, Math.max(sizeY, sizeZ));
        ratio = new float[]{sizeX * options.getRatioX() / maxSize,
                            sizeY * options.getRatioY() / maxSize,
                            sizeZ * options.getRatioZ() / maxSize};
    }

    //TODO test this
    public void updateValues(LoadSaveOptions options) {
        if (options.isNormalizeValues()) {
            double normRatio = options.getMaxValue() - options.getMinValue();
            normRatio /= maxFormatValue;

            for (int i = 0; i < grid.length; i++) {
                int value = (int) ((grid[i] & 0xFFFF) * normRatio);
                value += options.getMinValue();

                grid[i] = (short) value;

                if (maxValue < value) {
                    maxValue = (short) value;
                }

                if (value > 1) {
                    histogram[value & 0xFFFF]++;
                }
            }
        } else {
            for (int i = 0; i < grid.length; i++) {
                int value = grid[i] & 0xFFFF;

                if (value < options.getMinValue()) {
                    if (options.isCutMinValue()) {
                        value = 0;
                    } else {
                        value = options.getMinValue();
                    }
                    grid[i] = (short) value;
                } else if (value > options.getMaxValue()) {
                    if (options.isCutMinValue()) {
                        value = 0;
                    } else {
                        value = options.getMaxValue();
                    }
                    grid[i] = (short) value;

                }

                if (maxValue < value) {
                    maxValue = (short) value;
                }

                if (value > 1) {
                    histogram[value & 0xFFFF]++;
                }
            }
        }
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
        return histogram;
    }

    public short getMaxValue() {
        return maxValue;
    }

    public float[] getRatio() {
        return ratio;
    }

    public void setValue(int x, int y, int z, short value) {
        grid[z * sizeY * sizeX + y * sizeX + x] = value;
    }

    public short getValue(int x, int y, int z) {
        return grid[z * sizeY * sizeX + y * sizeX + x];
    }

    public short[] getData() {
        return grid;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        //TODO Watchout!!
        hash = 89 * hash + System.identityHashCode(this.grid);
//        hash = 89 * hash + Arrays.hashCode(this.grid);
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

        //TODO Watchout!!
        if (this.grid != other.grid) {
            return false;
        }
//        if (!Arrays.equals(this.grid, other.grid)) {
//            return false;
//        }
        return true;
    }

}
