package no.uib.inf252.katscan.model;

import java.awt.EventQueue;
import java.io.Serializable;
import no.uib.inf252.katscan.event.CutListener;

/**
 *
 * @author Marcelo Lima
 */
public class Cut extends KatModel<Cut> implements Serializable {
    
    private final float[] minValues;
    private final float[] maxValues;

    public Cut() {
        minValues = new float[]{0f, 0f, 0f};
        maxValues = new float[]{1f, 1f, 1f};
    }

    @Override
    protected Cut newInstance() {
        return new Cut();
    }

    @Override
    public void assimilate(Cut katModel) {
        System.arraycopy(katModel.minValues, 0, this.minValues, 0, minValues.length);
        System.arraycopy(katModel.maxValues, 0, this.maxValues, 0, maxValues.length);
        
        fireMinValueChanged();
        fireMaxValueChanged();
        fireRepaint();
    }

    public float[] getMinValues() {
        return minValues;
    }

    public float[] getMaxValues() {
        return maxValues;
    }

    public void changeMinX(float delta, boolean paired) {
        if (paired) {
            float diff = maxValues[0] - minValues[0];
            setValue(false, delta, 0, 0f, maxValues[0], paired);
            maxValues[0] = minValues[0] + diff;
            fireMaxValueChanged();
            fireRepaint();
        } else {
            setValue(false, delta, 0, 0f, maxValues[0], paired);
        }
    }

    public void changeMinY(float delta, boolean paired) {
        if (paired) {
            float diff = maxValues[1] - minValues[1];
            setValue(false, delta, 1, 0f, maxValues[1], paired);
            maxValues[1] = minValues[1] + diff;
            fireMaxValueChanged();
            fireRepaint();
        } else {
            setValue(false, delta, 1, 0f, maxValues[1], paired);
        }
    }

    public void changeMinZ(float delta, boolean paired) {
        if (paired) {
            float diff = maxValues[2] - minValues[2];
            setValue(false, delta, 2, 0f, maxValues[2], paired);
            maxValues[2] = minValues[2] + diff;
            fireMaxValueChanged();
            fireRepaint();
        } else {
            setValue(false, delta, 2, 0f, maxValues[2], paired);
        }
    }

    public void changeMaxX(float delta, boolean paired) {
        if (paired) {
            float diff = maxValues[0] - minValues[0];
            setValue(true, delta, 0, minValues[0], 1f, paired);
            minValues[0] = maxValues[0] - diff;
            fireMinValueChanged();
            fireRepaint();
        } else {
            setValue(true, delta, 0, minValues[0], 1f, paired);
        }
    }

    public void changeMaxY(float delta, boolean paired) {
        if (paired) {
            float diff = maxValues[1] - minValues[1];
            setValue(true, delta, 1, minValues[1], 1f, paired);
            minValues[1] = maxValues[1] - diff;
            fireMinValueChanged();
            fireRepaint();
        } else {
            setValue(true, delta, 1, minValues[1], 1f, paired);
        }
    }

    public void changeMaxZ(float delta, boolean paired) {
        if (paired) {
            float diff = maxValues[2] - minValues[2];
            setValue(true, delta, 2, minValues[2], 1f, paired);
            minValues[2] = maxValues[2] - diff;
            fireMinValueChanged();
            fireRepaint();
        } else {
            setValue(true, delta, 2, minValues[2], 1f, paired);
        }
    }

    private void setValue(boolean changeMax, float delta, int index, float min, float max, boolean paired) {
        float[] vector = changeMax ? maxValues : minValues;
        float value = vector[index];
        if (delta > 0) {
            if (value == max) {
                return;
            }
            
            value += delta;
            if (value > max) {
                value = max;
            }
        } else {
            if (value == min) {
                return;
            }
            
            value += delta;
            if (value < min) {
                value = min;
            }
        }
        
        vector[index] = value;
        
        if (changeMax) {
            fireMaxValueChanged();
        } else {
            fireMinValueChanged();
        }
        
        if (!paired) {
            fireRepaint();
        }
    }
    
    private void fireMinValueChanged() {
        CutListener[] listeners = listenerList.getListeners(CutListener.class);

        for (final CutListener listener : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.minValueChanged();
                }
            });
        }
    }
    
    private void fireMaxValueChanged() {
        CutListener[] listeners = listenerList.getListeners(CutListener.class);

        for (final CutListener listener : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.maxValueChanged();
                }
            });
        }
    }
    
}
