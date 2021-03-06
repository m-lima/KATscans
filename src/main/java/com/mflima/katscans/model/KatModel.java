package com.mflima.katscans.model;

import com.mflima.katscans.event.KatModelListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import javax.swing.event.EventListenerList;

/** @author Marcelo Lima */
abstract class KatModel<T> implements Serializable {

  protected transient EventListenerList listenerList = new EventListenerList();

  protected abstract T newInstance();

  public abstract void assimilate(T katModel);

  public void reset() {
    assimilate(newInstance());
  }

  public synchronized void addKatModelListener(KatModelListener listener) {
    if (listener == null) {
      return;
    }

    listenerList.add(KatModelListener.class, listener);
  }

  public synchronized void removeKatModelListener(KatModelListener listener) {
    if (listener == null) {
      return;
    }

    listenerList.remove(KatModelListener.class, listener);
  }

  private void readObject(ObjectInputStream ignored) throws IOException, ClassNotFoundException {
    listenerList = new EventListenerList();
  }
}
