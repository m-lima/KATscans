package no.uib.inf252.katscan.model;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import javax.swing.event.EventListenerList;
import no.uib.inf252.katscan.event.KatModelListener;

/**
 *
 * @author Marcelo Lima
 * @param <T>
 */
public abstract class KatModel<T extends KatModel> implements Serializable {
    
    protected transient EventListenerList listenerList;
    
    public KatModel() {
        listenerList = new EventListenerList();
    }
    
    protected abstract T newInstance();
    public abstract void assimilate(T katModel);
    
    public void reset() {
        assimilate(newInstance());
    }
    
    public final T copy() {
        T katModel = newInstance();
        katModel.assimilate(this);
        return katModel;
    }

    protected void fireRepaint() {
        KatModelListener[] listeners = listenerList.getListeners(KatModelListener.class);

        for (final KatModelListener listener : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.repaint();
                }
            });
        }
    }

    public synchronized void addTransferFunctionListener(KatModelListener listener) {
        if (listener == null) {
            return;
        }

        listenerList.add(KatModelListener.class, listener);
    }

    public synchronized void removeTransferFunctionListener(KatModelListener listener) {
        if (listener == null) {
            return;
        }

        listenerList.remove(KatModelListener.class, listener);
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        listenerList = new EventListenerList();
    }

}
