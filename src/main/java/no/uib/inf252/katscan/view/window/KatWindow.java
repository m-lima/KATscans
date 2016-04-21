package no.uib.inf252.katscan.view.window;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.util.Direction;
import static no.uib.inf252.katscan.view.MainFrame.THEME_COLOR;
import no.uib.inf252.katscan.view.component.Histogram;

/**
 *
 * @author Marcelo Lima
 */
public abstract class KatWindow extends View {

    protected String type;
    protected String name;
    
    protected KatWindow(String type, String name, Icon icon, Component component) {
        super(type + " - " + name, icon, component);
        this.type = type;
        this.name = name;
        setPreferredMinimizeDirection(Direction.RIGHT);
    }
    
    public JMenuItem getMenuItem(final RootWindow rootWindow) {
        JMenuItem menu = new JMenuItem(type, type.charAt(0));
        
        menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DockingWindow oldViews = rootWindow.getWindow();

                TabWindow tabWindow;
                if (oldViews instanceof TabWindow) {
                    tabWindow = (TabWindow) oldViews;
                } else {
                    tabWindow = new TabWindow();
                    tabWindow.setBackground(THEME_COLOR);
                    if (oldViews != null) {
                        tabWindow.addTab(oldViews);
                    }
                    rootWindow.setWindow(tabWindow);
                }

                View view = new View("Histogram - " + name, null, new Histogram(name));
                view.setPreferredMinimizeDirection(Direction.RIGHT);
                tabWindow.addTab(view);
            }
        });
        
        return menu;
    }

}
