package no.uib.inf252.katscan.project.io;

/**
 *
 * @author Marcelo Lima
 */
public class PersistenceHandler {
    
    private static final String LAST_LOAD = "lastProjLoad.kat";

    private PersistenceHandler() {}
    
    public void autoSave(){
    }
    
    public void save(){
    }
    
    public void load(){
    }

    public static PersistenceHandler getInstance() {
        return PersistenceHandlerHolder.INSTANCE;
    }

    private static class PersistenceHandlerHolder {
        private static final PersistenceHandler INSTANCE = new PersistenceHandler();
    }
 }
