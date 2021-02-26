package com.mflima.katscans.event;

/**
 * @author Marcelo Lima
 */
public interface TransferFunctionListener extends KatModelListener {
  void pointCountChanged();
  void pointValueChanged();
}
