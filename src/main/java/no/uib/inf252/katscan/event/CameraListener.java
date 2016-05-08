package no.uib.inf252.katscan.event;

/**
 *
 * @author Marcelo Lima
 */
public interface CameraListener extends KatModelListener {
    
    public void viewValueChanged();
    public void zoomValueChanged();

}
