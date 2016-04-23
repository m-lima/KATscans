package no.uib.inf252.katscan.event;

import java.util.EventListener;
import no.uib.inf252.katscan.model.Project;

/**
 *
 * @author Marcelo Lima
 */
public interface DatasetBrowserListener extends EventListener {
    
    public void treeChanged(Project root);

}
