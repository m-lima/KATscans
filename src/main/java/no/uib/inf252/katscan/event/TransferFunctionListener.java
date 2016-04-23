package no.uib.inf252.katscan.event;

import java.util.EventListener;

/**
 *
 * @author Marcelo Lima
 */
public interface TransferFunctionListener extends EventListener {
    
    public void pointCountChanged();
    public void pointValueChanged();

}
