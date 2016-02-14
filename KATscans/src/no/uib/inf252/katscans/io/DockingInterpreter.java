package no.uib.inf252.katscans.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import net.infonode.docking.View;
import net.infonode.docking.util.AbstractViewMap;
import net.infonode.docking.util.ViewFactory;

/**
 *
 * @author Marcelo Lima
 */
public class DockingInterpreter extends AbstractViewMap implements Serializable {

    private final HashMap<Double, View> viewMap = new HashMap<Double, View>();
    private final ArrayList<View> views = new ArrayList<View>(20);

    public void addView(Double identifier, View view) {
        addView((Object) identifier, view);
    }

    public void removeView(Double identifier) {
        removeView((Object) identifier);
    }

    public View getView(Double identifier) {
        return getView((Object) identifier);
    }

    public void clear() {
        views.clear();
        viewMap.clear();
    }

    @Override
    public int getViewCount() {
        return viewMap.size();
    }

    @Override
    public View getViewAtIndex(int index) {
        return views.get(index);
    }

    @Override
    public ViewFactory[] getViewFactories() {
        ArrayList f = new ArrayList();

        for (final View view : views) {

            if (view.getRootWindow() == null) {
                f.add(new ViewFactory() {

                    @Override
                    public Icon getIcon() {
                        return view.getIcon();
                    }

                    @Override
                    public String getTitle() {
                        return view.getTitle();
                    }

                    @Override
                    public View createView() {
                        return view;
                    }
                });
            }
        }

        return (ViewFactory[]) f.toArray(new ViewFactory[f.size()]);
    }

    @Override
    public boolean contains(View view) {
        return views.contains(view);
    }

    @Override
    protected void writeViewId(Object o, ObjectOutputStream stream) throws IOException {
        stream.writeObject(o);
    }
    
    @Override
    public void writeView(View view, ObjectOutputStream out) throws IOException {
        if (viewMap.isEmpty()) {
            return;
        }
        for (Iterator it = viewMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();

            if (entry.getValue() == view) {
                Double id = (Double) entry.getKey();
//                if (PluginManager.getInstance().getPlugin(id).getStatus() == RTPlugin.ACTIVE) {
//                    writeViewId(id, out);
//                }
                return;
            }
        }

        throw new IOException("Serializacao de um view desconhecido!");
    }


    @Override
    protected Object readViewId(ObjectInputStream stream) throws IOException {
        try {
            return stream.readObject();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DockingInterpreter.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }        
    }

    @Override
    public View readView(ObjectInputStream stream) throws IOException {
        return null;
    }

    @Override
    protected void addView(Object id, View view) {
        View oldView = viewMap.put((Double) id, view);
        
        if (oldView != null) {
            views.remove(oldView);
        }
        
        views.add(view);
    }

    @Override
    protected void removeView(Object id) {
        View view = viewMap.remove((Double) id);

        if (view != null) {
            views.remove(view);
        }
    }

    @Override
    protected View getView(Object id) {
        return viewMap.get((Double) id);
    }

}
