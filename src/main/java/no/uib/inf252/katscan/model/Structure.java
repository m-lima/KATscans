package no.uib.inf252.katscan.model;

import java.io.Serializable;

/**
 *
 * @author Marcelo Lima
 */
public class Structure extends SubGroup implements Serializable {

    @Override
    public short[] getData() {
        return getParent().getData();
    }

    @Override
    public short[] getHistogram() {
        return getParent().getHistogram();
    }

}
