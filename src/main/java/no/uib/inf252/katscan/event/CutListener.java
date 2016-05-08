package no.uib.inf252.katscan.event;

/**
 *
 * @author Marcelo Lima
 */
public interface CutListener extends KatModelListener {
    
    public void minValueChanged();
    public void maxValueChanged();
    public void sliceValueChanged();

}
