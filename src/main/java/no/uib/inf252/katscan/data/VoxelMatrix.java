package no.uib.inf252.katscan.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Marcelo Lima
 */
public class VoxelMatrix implements Serializable {

    public enum Axis {
        X, Y, Z;
    }

    private short[][][] grid;
    private int[] histogram;
    private int maxValue;
    private boolean dirtyHistogram;

    public VoxelMatrix(int sizeZ, int sizeY, int sizeX) {
        if (sizeZ <= 0) throw new IllegalArgumentException("The size must be larger than zero, but Z was " + sizeZ);
        if (sizeY <= 0) throw new IllegalArgumentException("The size must be larger than zero, but Y was " + sizeY);
        if (sizeX <= 0) throw new IllegalArgumentException("The size must be larger than zero, but X was " + sizeX);

        grid = new short[sizeZ][sizeY][sizeX];
        histogram = new int[65536];
        dirtyHistogram = true;
    }

    /**
     * Gets the length of a particular axis in the matrix.
     *
     * @param axis Axis of which the length is being requested.
     * @return The length of the particular axis.
     */
    public int getLength(Axis axis) {
        if (axis == null) throw new NullPointerException("The axis was not specified.");

        switch (axis) {
            case Z:
                return grid.length;
            case Y:
                return grid[0].length;
            case X:
                return grid[0][0].length;
        }

        return -1;
    }

    /**
     * Gets a particular value in the matrix.
     *
     * @param z The Z coordinate.
     * @param y The Y coordinate.
     * @param x The X coordinate.
     * @return The value in the particular position.
     */
    public short getValue(int z, int y, int x) {
        checkBounds(z, y, x);

        return grid[z][y][x];
    }

    public short[] getRow(int z, int y) {
        checkBounds(z, y);

        dirtyHistogram = true;
        return grid[z][y];
    }

    public void setRow(int z, int y, short[] values) {
        checkBounds(z, y);

        if (values.length != grid[0][0].length) throw new IllegalArgumentException("The row does not match the matrix size. Expected " + grid[0][0] + " and got " + values.length);

        dirtyHistogram = true;
        System.arraycopy(values, 0, grid[z][y], 0, values.length);
    }

    /**
     * Sets a particular value in the matrix.
     *
     * @param z The Z coordinate.
     * @param y The Y coordinate.
     * @param x The X coordinate.
     * @param value The value in the particular position.
     */
    public void setValue(int z, int y, int x, short value) {
        checkBounds(z, y, x);

        if (histogram[grid[z][y][x]] > 0) {
            histogram[grid[z][y][x]]--;
        }
        grid[z][y][x] = value;
        histogram[grid[z][y][x]]++;
    }

    public int[] getHistogram() {
        if (dirtyHistogram) {
            updateHistogram();
        }
        int[] histogramReturn = new int[65536];
        System.arraycopy(histogram, 0, histogramReturn, 0, 65536);
        return histogramReturn;
    }

    public int getMaxValue() {
        if (dirtyHistogram) {
            updateHistogram();
        }
        return maxValue;
    }
    
    private void updateHistogram() {
        histogram = new int[65536];
        maxValue = 0;
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                for (int k = 0; k < grid[i][j].length; k++) {
                    histogram[grid[i][j][k]]++;
                    maxValue = Math.max(maxValue, grid[i][j][k]);
                }
            }
        }
        
        dirtyHistogram = false;
    }

    private void checkBounds(int z) {
        checkBounds(z, 1, 1);
    }

    private void checkBounds(int z, int y) {
        checkBounds(z, y, 1);
    }

    private void checkBounds(int z, int y, int x) {
        if (z < 0) throw new IllegalArgumentException("The coordinate must be larger or equal to zero, but Z was " + z);
        if (y < 0) throw new IllegalArgumentException("The coordinate must be larger or equal to zero, but Y was " + y);
        if (x < 0) throw new IllegalArgumentException("The coordinate must be larger or equal to zero, but X was " + x);

        if (z >= grid.length)
            throw new IllegalArgumentException("The coordinate must less than " + grid.length + ", but Z was " + z);
        if (y >= grid[0].length)
            throw new IllegalArgumentException("The coordinate must less than " + grid[0].length + ", but Y was " + y);
        if (x >= grid[0][0].length)
            throw new IllegalArgumentException("The coordinate must less than " + grid[0][0].length + ", but X was " + x);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Arrays.deepHashCode(this.grid);
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
        
        if(other.getLength(Axis.X) != getLength(Axis.X)
        || other.getLength(Axis.Y) != getLength(Axis.Y)
        || other.getLength(Axis.Z) != getLength(Axis.Z)){
            return false;
        }
        
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                for (int k = 0; k < grid[0][0].length; k++) {
                    if (grid[i][j][k] != other.grid[i][j][k]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public short[] asArray() {
        return asArray(null);
    }
    
    public short[] asArray(short[] values) {
        int totalSize = getLength(Axis.X) * getLength(Axis.Y) * getLength(Axis.Z);
        if (values == null) {
            values = new short[getLength(Axis.X) * getLength(Axis.Y) * getLength(Axis.Z)];
        }
        
        if (values.length < totalSize) {
            values = Arrays.copyOf(values, totalSize);
        }
        
        for (int i = 0; i < grid.length; i++) {
            short[][] slices = grid[i];
            for (int j = 0; j < slices.length; j++) {
                short[] slice = slices[j];
                System.arraycopy(slice, 0, values, ((i * slices.length) + j) * getLength(Axis.X), slice.length);
            }
        }
        
        return values;
    }
}
