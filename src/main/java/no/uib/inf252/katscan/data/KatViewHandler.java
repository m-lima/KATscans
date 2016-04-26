package no.uib.inf252.katscan.data;

import java.awt.EventQueue;
import java.util.HashMap;
import javax.swing.event.EventListenerList;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.View;
import no.uib.inf252.katscan.event.KatViewListener;
import no.uib.inf252.katscan.project.KatViewNode;

/**
 *
 * @author Marcelo Lima
 */
public class KatViewHandler implements DockingWindowListener {
    
    private final EventListenerList listenerList;
    private final HashMap<DockingWindow, KatViewNode> pendingViews;
    private final HashMap<DockingWindow, KatViewNode> currentViews;

    private KatViewHandler() {
        listenerList = new EventListenerList();
        pendingViews = new HashMap<>();
        currentViews = new HashMap<>();
    }

    public synchronized void requestAddView(KatViewNode view) {
        if (!pendingViews.containsKey(view)) {
            pendingViews.put(view.getView(), view);
            fireViewAddRequested(view);
        }
    }
    
    public synchronized void addKatViewListener(KatViewListener listener) {
        if (listener == null) {
            return;
        }
        
        listenerList.add(KatViewListener.class, listener);
    }
    
    public synchronized void removeKatViewListener(KatViewListener listener) {
        if (listener == null) {
            return;
        }
        
        listenerList.remove(KatViewListener.class, listener);
    }

    private void fireViewAddRequested(final KatViewNode view) {
        KatViewListener[] listeners = listenerList.getListeners(KatViewListener.class);

        for (final KatViewListener listener : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.viewAddRequested(view);
                }
            });
        }
    }
    
    private void fireViewAdded(final KatViewNode view) {
        KatViewListener[] listeners = listenerList.getListeners(KatViewListener.class);

        for (final KatViewListener listener : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.viewAdded(view);
                }
            });
        }
    }

    private void fireViewRemoved(final KatViewNode view) {
        KatViewListener[] listeners = listenerList.getListeners(KatViewListener.class);

        for (final KatViewListener listener : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.viewRemoved(view);
                }
            });
        }
    }
    
    @Override
    public void windowAdded(DockingWindow addedToWindow, DockingWindow addedWindow) {
        KatViewNode view = pendingViews.remove(addedWindow);
        if (view != null) {
            currentViews.put(addedWindow, view);
            fireViewAdded(view);
        }
    }

    @Override
    public void windowClosed(DockingWindow window) {
        KatViewNode view = currentViews.remove(window);
        if (view == null) {
            throw new NullPointerException("Could not find view");
        }
        fireViewRemoved(view);
    }

    @Override
    public void windowRemoved(DockingWindow removedFromWindow, DockingWindow removedWindow) {}

    @Override
    public void windowShown(DockingWindow window) {}
    
    @Override
    public void windowHidden(DockingWindow window) {}

    @Override
    public void viewFocusChanged(View previouslyFocusedView, View focusedView) {}

    @Override
    public void windowClosing(DockingWindow window) throws OperationAbortedException {}

    @Override
    public void windowUndocking(DockingWindow window) throws OperationAbortedException {}

    @Override
    public void windowUndocked(DockingWindow window) {}

    @Override
    public void windowDocking(DockingWindow window) throws OperationAbortedException {}

    @Override
    public void windowDocked(DockingWindow window) {}

    @Override
    public void windowMinimizing(DockingWindow window) throws OperationAbortedException {}

    @Override
    public void windowMinimized(DockingWindow window) {}

    @Override
    public void windowMaximizing(DockingWindow window) throws OperationAbortedException {}

    @Override
    public void windowMaximized(DockingWindow window) {}

    @Override
    public void windowRestoring(DockingWindow window) throws OperationAbortedException {}

    @Override
    public void windowRestored(DockingWindow window) {}

    public static KatViewHandler getInstance() {
        return KatViewHandlerHolder.INSTANCE;
    }

    private static class KatViewHandlerHolder {
        private static final KatViewHandler INSTANCE = new KatViewHandler();
    }
 }
