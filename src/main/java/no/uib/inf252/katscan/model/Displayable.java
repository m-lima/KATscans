package no.uib.inf252.katscan.model;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

/**
 *
 * @author Marcelo Lima
 */
public abstract class Displayable extends KatNode {

    public abstract short[] getData();
    public abstract short[] getHistogram();
    
    public JPopupMenu getPopupMenu() {
        return null;
    }
    
    public JMenu getMenu() {
        return null;
    }

}
