package no.uib.inf252.katscan.data.io;

/**
 *
 * @author Marcelo Lima
 */
public class LoadSaveOptions {

    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    
    private final float ratioX;
    private final float ratioY;
    private final float ratioZ;
    
    private final int minValue;
    private final int maxValue;
    private final boolean cutMinValue;
    private final boolean cutMaxValue;
    private final boolean normalizeValues;

    public LoadSaveOptions(int sizeX, int sizeY, int sizeZ, float ratioX, float ratioY, float ratioZ, int minValue, int maxValue, boolean cutMinValue, boolean cutMaxValue, boolean normalizeValues) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.ratioX = ratioX;
        this.ratioY = ratioY;
        this.ratioZ = ratioZ;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.cutMinValue = cutMinValue;
        this.cutMaxValue = cutMaxValue;
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

    public boolean isCutMinValue() {
        return cutMinValue;
    }

    public boolean isCutMaxValue() {
        return cutMaxValue;
    }

    public boolean isNormalizeValues() {
        return normalizeValues;
    }

}
