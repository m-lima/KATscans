package no.uib.inf252.katscan.model.displayable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import no.uib.inf252.katscan.data.LoadedData;
import no.uib.inf252.katscan.data.VoxelMatrix;
import no.uib.inf252.katscan.model.DataFile;
import no.uib.inf252.katscan.model.KatNode;
import no.uib.inf252.katscan.model.KatView;
import no.uib.inf252.katscan.view.KatViewHandler;

/**
 *
 * @author Marcelo Lima
 */
public abstract class Displayable extends KatNode {

    public abstract VoxelMatrix getMatrix();
    public abstract int[] getHistogram();

    public Displayable(String name) {
        super(name);
    }

    @Override
    protected JMenu getMainMenu() {
        JMenu menu = new JMenu(getName());
        
        KatView.Type[] types = KatView.Type.values();
        for (final KatView.Type type : types) {
            JMenuItem item = new JMenuItem(type.getName(), type.getMnemonic());
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    KatView view = KatView.buildKatView(type, Displayable.this);
                    KatViewHandler.getInstance().requestAddView(view);
                }
            });
            menu.add(item);
        }

        JMenuItem removeMenu = new JMenuItem("Remove", 'R');

        removeMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Displayable.this instanceof DataFile) {
                    LoadedData.getInstance().unload(getName());
                }
            }
        });

        if (types.length > 0) {
            menu.addSeparator();
        }
        menu.add(removeMenu);
        
        return menu;
    }

    public JMenu getMenu() {
        return null;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 27 * hash + Objects.hashCode(getMatrix());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return getMatrix().equals(((Displayable)obj).getMatrix());
        }
        return false;
    }

}
