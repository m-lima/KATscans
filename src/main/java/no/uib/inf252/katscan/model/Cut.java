package no.uib.inf252.katscan.model;

import java.io.Serializable;
import javax.swing.tree.MutableTreeNode;

/**
 *
 * @author Marcelo Lima
 */
public class Cut extends SubGroup implements Serializable {

    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private int minZ;
    private int maxZ;
    
    @Override
    public short[] getData() {
        short[] data = getParent().getData();
        for (int i = minZ; i < maxZ; i++) {
            for (int j = minY; j < maxY; j++) {
                for (int k = minX; k < maxX; k++) {
                }
            }
        }
        return getParent().getData();
    }

    @Override
    public short[] getHistogram() {
        return getParent().getHistogram();
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }
    
}
