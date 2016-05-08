package no.uib.inf252.katscan.event;

import no.uib.inf252.katscan.model.Rotation;

/**
 *
 * @author Marcelo Lima
 */
public interface RotationListener extends KatModelListener {
    
    public void rotationValueChanged();

}
