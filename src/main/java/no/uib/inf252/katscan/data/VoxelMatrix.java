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
    private int minValue;
    private int maxValue;
    private final int maxFormatValue;
    private final boolean normalized;
//    private short maxValue;
    
    private boolean initialized;

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
        
        histogram = new int[maxValue - minValue];

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

            if (value >= 0) {
                histogram[value & 0xFFFF]++;
            }
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

    public void setValue(int x, int y, int z, short value) {
        grid[z * sizeY * sizeX + y * sizeX + x] = value;
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
