package no.uib.inf252.katscan.io;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Marcelo Lima
 */
public interface LoadSaveHandler {

    public Object loadData(InputStream stream);
    public void saveData(OutputStream stream, Object object);
}
