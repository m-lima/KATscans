package no.uib.inf252.katscan.model;

/**
 *
 * @author Marcelo Lima
 */
public class PersistenceHandler {

    private PersistenceHandler() {
    }
    
    public void autoSave(){};
    public void save(){};
    public void load(){};

    public static PersistenceHandler getInstance() {
        return PersistenceHandlerHolder.INSTANCE;
    }

    private static class PersistenceHandlerHolder {
        private static final PersistenceHandler INSTANCE = new PersistenceHandler();
    }
 }
