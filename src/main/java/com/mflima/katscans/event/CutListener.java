package com.mflima.katscans.event;

/**
 *
 * @author Marcelo Lima
 */
public interface CutListener extends KatModelListener {
    
    public void minValueChanged();
    public void maxValueChanged();
    public void sliceValueChanged();

}
