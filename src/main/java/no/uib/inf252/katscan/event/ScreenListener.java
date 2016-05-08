package no.uib.inf252.katscan.event;

/**
 *
 * @author Marcelo Lima
 */
public interface ScreenListener extends KatModelListener {
    
    public void orthographicValueChanged();
    public void projectionValueChanged();
    public void stepValueChanged();

}
