package no.uib.inf252.katscan.view.dataset;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import no.uib.inf252.katscan.data.io.LoadSaveHandler;

/**
 *
 * @author Marcelo Lima
 */
public class DatasetMenuBuilder {

    public static class ProjectMenuBuilder {
        private static final ProjectMenuListener LISTENER = new ProjectMenuListener();

        public static JMenu buildProjectMenu() {
            JMenu projectMenu = new JMenu("Datasets");
            projectMenu.setMnemonic('D');
            projectMenu.add(getLoadDataset());
            projectMenu.add(getClearDatasets());
            JMenuItem item = new JMenuItem("Rename", 'R');
            item.setIcon(new ImageIcon(ProjectMenuBuilder.class.getResource("/icons/edit.png")));
            projectMenu.add(item);
            return projectMenu;
        }

        private static JMenu getLoadDataset() {
            JMenu loadMenu = new JMenu("Load");
            loadMenu.setMnemonic('L');
            loadMenu.setIcon(new ImageIcon(ProjectMenuBuilder.class.getResource("/icons/open.png")));

            LoadSaveHandler.Format[] formats = LoadSaveHandler.Format.values();
            for (LoadSaveHandler.Format format : formats) {
                JMenuItem menuItem = new JMenuItem(format.getFormat().getName(), format.getFormat().getMnemonic());
                menuItem.addActionListener(LISTENER);
                loadMenu.add(menuItem);
            }

            return loadMenu;
        }

        private static JMenuItem getClearDatasets() {
            JMenuItem clearDatasets = new JMenuItem("Clear all", 'C');
            clearDatasets.setIcon(new ImageIcon(ProjectMenuBuilder.class.getResource("/icons/closeData.png")));
            clearDatasets.addActionListener(LISTENER);
            return clearDatasets;
        }

        private static class ProjectMenuListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                JMenuItem menuItem = (JMenuItem) e.getSource();

                String formatName = menuItem.getText();
                LoadSaveHandler.Format[] formats = LoadSaveHandler.Format.values();
                for (LoadSaveHandler.Format format : formats) {
                    if (format.getFormat().getName() == formatName) {
                        LoadSaveHandler.getInstance().load(format);
                        break;
                    }
                }
            }

        }

    }
    
    public static class DisplayableMenuBuilder {
        private static final DisplayableMenuListener LISTENER = new DisplayableMenuListener();

        public static JPopupMenu buildProjectMenu() {
            JPopupMenu projectMenu = new JPopupMenu();
            projectMenu.add(getLoadDataset());
            projectMenu.add(getClearDatasets());
            projectMenu.addSeparator();
            JMenuItem item = new JMenuItem("Rename", 'R');
            item.setIcon(new ImageIcon(DisplayableMenuBuilder.class.getResource("/icons/edit.png")));
            projectMenu.add(item);
            return projectMenu;
        }

        public static JMenu getLoadDataset() {
            JMenu loadMenu = new JMenu("Load");
            loadMenu.setMnemonic('L');
            loadMenu.setIcon(new ImageIcon(DisplayableMenuBuilder.class.getResource("/icons/open.png")));

            LoadSaveHandler.Format[] formats = LoadSaveHandler.Format.values();
            for (LoadSaveHandler.Format format : formats) {
                JMenuItem menuItem = new JMenuItem(format.getFormat().getName(), format.getFormat().getMnemonic());
                menuItem.addActionListener(LISTENER);
                loadMenu.add(menuItem);
            }

            return loadMenu;
        }

        public static JMenuItem getClearDatasets() {
            JMenuItem clearDatasets = new JMenuItem("Clear all", 'C');
            clearDatasets.setIcon(new ImageIcon(DisplayableMenuBuilder.class.getResource("/icons/closeData.png")));
            clearDatasets.addActionListener(LISTENER);
            return clearDatasets;
        }

        private static class DisplayableMenuListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                JMenuItem menuItem = (JMenuItem) e.getSource();

                String formatName = menuItem.getText();
                LoadSaveHandler.Format[] formats = LoadSaveHandler.Format.values();
                for (LoadSaveHandler.Format format : formats) {
                    if (format.getFormat().getName() == formatName) {
                        LoadSaveHandler.getInstance().load(format);
                        break;
                    }
                }
            }

        }

    }

}
