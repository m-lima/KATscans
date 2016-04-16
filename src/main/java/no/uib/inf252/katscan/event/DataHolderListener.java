package no.uib.inf252.katscan.event;

import java.util.EventListener;

/**
 *
 * @author Marcelo Lima
 */
public interface DataHolderListener extends EventListener {

    public void dataAdded(String name, String file);
    public void dataRemoved(String name);
    
}
