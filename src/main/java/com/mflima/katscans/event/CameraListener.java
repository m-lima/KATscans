package com.mflima.katscans.event;

/**
 * @author Marcelo Lima
 */
public interface CameraListener extends KatModelListener {

  public void viewValueChanged();

  public void zoomValueChanged();

}
