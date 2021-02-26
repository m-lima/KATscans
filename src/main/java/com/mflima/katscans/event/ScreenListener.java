package com.mflima.katscans.event;

/** @author Marcelo Lima */
public interface ScreenListener extends KatModelListener {
  void orthographicValueChanged();

  void projectionValueChanged();

  void stepValueChanged();
}
