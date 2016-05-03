package no.uib.inf252.katscan.event;

/**
 *
 * @author Marcelo Lima
 */
public interface CutListener {
    
    public void cutUpdated(int minX, int maxX,
                           int minY, int maxY,
                           int minZ, int maxZ);

}
