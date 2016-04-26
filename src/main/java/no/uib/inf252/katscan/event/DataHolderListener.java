package no.uib.inf252.katscan.event;

import java.io.File;
import java.util.EventListener;

/**
 *
 * @author Marcelo Lima
 */
public interface DataHolderListener extends EventListener {

    public void dataAdded(String name, File file);
    public void dataRemoved(String name);
    
}
