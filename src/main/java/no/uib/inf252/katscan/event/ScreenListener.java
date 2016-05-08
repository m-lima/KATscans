package no.uib.inf252.katscan.event;

import no.uib.inf252.katscan.model.Screen;

/**
 *
 * @author Marcelo Lima
 */
public interface ScreenListener extends KatModelListener {
    
    public void orthographicValueChanged();
    public void projectionValueChanged();
    public void stepValueChanged();

}
