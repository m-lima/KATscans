package no.uib.inf252.katscan.view.window;

import no.uib.inf252.katscan.view.component.Histogram;

/**
 *
 * @author Marcelo Lima
 */
public class HistogramView extends KatWindow {
    
    HistogramView(String name) {
        super("Histogram", name, null, new Histogram(name));
    }

}
