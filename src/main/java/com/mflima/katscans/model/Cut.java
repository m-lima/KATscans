package com.mflima.katscans.model;

import com.mflima.katscans.event.CutListener;
import com.mflima.katscans.event.KatModelListener;
import java.awt.EventQueue;
import java.io.Serializable;

/** @author Marcelo Lima */
public class Cut extends KatModel<Cut> implements Serializable {

  private final float[] minValues;
  private final float[] maxValues;
  private float slice;

  public Cut() {
    minValues = new float[] {0f, 0f, 0f};
    maxValues = new float[] {1f, 1f, 1f};

    slice = 0f;
  }

  @Override
  protected Cut newInstance() {
    return new Cut();
  }

  @Override
  public void assimilate(Cut katModel) {
    System.arraycopy(katModel.minValues, 0, this.minValues, 0, minValues.length);
    System.arraycopy(katModel.maxValues, 0, this.maxValues, 0, maxValues.length);

    this.slice = katModel.slice;

    fireMinValueChanged();
    fireMaxValueChanged();
    fireSliceValueChanged();
  }

  public float[] getMinValues() {
    return minValues;
  }

  public float[] getMaxValues() {
    return maxValues;
  }

  public float getSlice() {
    return slice;
  }

  public void changeSlice(float delta) {
    if (delta < 0 && slice == 0f) {
      return;
    }

    slice += delta;
    if (slice < 0f) {
      slice = 0f;
    }

    fireSliceValueChanged();
  }

  public void changeMinX(float delta, boolean paired) {
    if (paired) {
      float diff = maxValues[0] - minValues[0];
      setValue(false, delta, 0, 0f, maxValues[0]);
      maxValues[0] = minValues[0] + diff;
      fireMaxValueChanged();
    } else {
      setValue(false, delta, 0, 0f, maxValues[0]);
    }
  }

  public void changeMinY(float delta, boolean paired) {
    if (paired) {
      float diff = maxValues[1] - minValues[1];
      setValue(false, delta, 1, 0f, maxValues[1]);
      maxValues[1] = minValues[1] + diff;
      fireMaxValueChanged();
    } else {
      setValue(false, delta, 1, 0f, maxValues[1]);
    }
  }

  public void changeMinZ(float delta, boolean paired) {
    if (paired) {
      float diff = maxValues[2] - minValues[2];
      setValue(false, delta, 2, 0f, maxValues[2]);
      maxValues[2] = minValues[2] + diff;
      fireMaxValueChanged();
    } else {
      setValue(false, delta, 2, 0f, maxValues[2]);
    }
  }

  public void changeMaxX(float delta, boolean paired) {
    if (paired) {
      float diff = maxValues[0] - minValues[0];
      setValue(true, delta, 0, minValues[0], 1f);
      minValues[0] = maxValues[0] - diff;
      fireMinValueChanged();
    } else {
      setValue(true, delta, 0, minValues[0], 1f);
    }
  }

  public void changeMaxY(float delta, boolean paired) {
    if (paired) {
      float diff = maxValues[1] - minValues[1];
      setValue(true, delta, 1, minValues[1], 1f);
      minValues[1] = maxValues[1] - diff;
      fireMinValueChanged();
    } else {
      setValue(true, delta, 1, minValues[1], 1f);
    }
  }

  public void changeMaxZ(float delta, boolean paired) {
    if (paired) {
      float diff = maxValues[2] - minValues[2];
      setValue(true, delta, 2, minValues[2], 1f);
      minValues[2] = maxValues[2] - diff;
      fireMinValueChanged();
    } else {
      setValue(true, delta, 2, minValues[2], 1f);
    }
  }

  private void setValue(boolean changeMax, float delta, int index, float min, float max) {
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
  }

  private void fireMinValueChanged() {
    KatModelListener[] listeners = listenerList.getListeners(KatModelListener.class);

    for (final KatModelListener listener : listeners) {
      EventQueue.invokeLater(((CutListener) listener)::minValueChanged);
    }
  }

  private void fireMaxValueChanged() {
    KatModelListener[] listeners = listenerList.getListeners(KatModelListener.class);

    for (final KatModelListener listener : listeners) {
      EventQueue.invokeLater(((CutListener) listener)::maxValueChanged);
    }
  }

  private void fireSliceValueChanged() {
    KatModelListener[] listeners = listenerList.getListeners(KatModelListener.class);

    for (final KatModelListener listener : listeners) {
      EventQueue.invokeLater(((CutListener) listener)::sliceValueChanged);
    }
  }
}
