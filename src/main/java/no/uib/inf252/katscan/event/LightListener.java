package no.uib.inf252.katscan.event;

import no.uib.inf252.katscan.model.Light;

/**
 *
 * @author Marcelo Lima
 */
public interface LightListener extends KatModelListener {
    
    public void lightValueChanged();

}
