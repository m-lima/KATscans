package com.mflima.katscans.event;

/**
 * @author Marcelo Lima
 */
public interface CutListener extends KatModelListener {
  void minValueChanged();
  void maxValueChanged();
  void sliceValueChanged();
}
