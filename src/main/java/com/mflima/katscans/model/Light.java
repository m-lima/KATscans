package com.mflima.katscans.model;

import com.jogamp.opengl.math.VectorUtil;
import com.mflima.katscans.event.KatModelListener;
import com.mflima.katscans.event.LightListener;
import java.awt.EventQueue;
import java.io.Serializable;

/** @author Marcelo Lima */
public class Light extends KatModel<Light> implements Serializable {

  private final float[] lightPosition;

  public Light() {
    lightPosition = VectorUtil.normalizeVec3(new float[] {-2f, 2f, 5f});
  }

  public float[] getLightPosition() {
    return lightPosition;
  }

  public void setLightPostion(float[] light) {
    lightPosition[0] = light[0];
    lightPosition[1] = light[1];
    lightPosition[2] = light[2];
    fireLightValueChanged();
  }

  @Override
  protected Light newInstance() {
    return new Light();
  }

  @Override
  public void assimilate(Light katModel) {
    System.arraycopy(katModel.lightPosition, 0, this.lightPosition, 0, lightPosition.length);
    fireLightValueChanged();
  }

  private void fireLightValueChanged() {
    KatModelListener[] listeners = listenerList.getListeners(KatModelListener.class);

    for (final KatModelListener listener : listeners) {
      EventQueue.invokeLater(((LightListener) listener)::lightValueChanged);
    }
  }
}
