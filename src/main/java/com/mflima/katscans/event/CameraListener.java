package com.mflima.katscans.event;

/** @author Marcelo Lima */
public interface CameraListener extends KatModelListener {
  void viewValueChanged();

  void zoomValueChanged();
}
