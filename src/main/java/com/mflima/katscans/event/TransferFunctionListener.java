package com.mflima.katscans.event;

/**
 * @author Marcelo Lima
 */
public interface TransferFunctionListener extends KatModelListener {

  public void pointCountChanged();

  public void pointValueChanged();

}
