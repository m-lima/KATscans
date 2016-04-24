package no.uib.inf252.katscan.event;

import java.util.EventListener;
import no.uib.inf252.katscan.project.KatViewNode;

/**
 *
 * @author Marcelo Lima
 */
public interface KatViewListener extends EventListener {
    
    public void viewAddRequested(KatViewNode view);
    public void viewAdded(KatViewNode view);
    public void viewRemoved(KatViewNode view);

}
