package no.uib.inf252.katscan.event;

import java.util.EventListener;
import no.uib.inf252.katscan.model.KatView;

/**
 *
 * @author Marcelo Lima
 */
public interface KatViewListener extends EventListener {
    
    public void viewAddRequested(KatView view);
    public void viewAdded(KatView view);
    public void viewRemoved(KatView view);

}
