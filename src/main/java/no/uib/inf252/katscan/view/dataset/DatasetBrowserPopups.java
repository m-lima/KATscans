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
public class DatasetBrowserPopups implements ActionListener {
    
//    private final JMenu projectPopup;
//
//    private DatasetBrowserPopups() {
//        projectPopup = new JMenu();
//        setupProjectPopup();
//    }
//    
//    private void setupProjectPopup() {
//        projectPopup.add(getLoadDataset());
//        projectPopup.add(getClearDatasets());
//        projectPopup.addSeparator();
//        JMenuItem item = new JMenuItem("Rename", 'R');
//        item.setIcon(new ImageIcon(getClass().getResource("/icons/edit.png")));
//        projectPopup.add(item);
//    }

    @Override
    public void actionPerformed(ActionEvent e) {
//        JMenuItem menuItem = (JMenuItem) e.getSource();
//        
//        String formatName = menuItem.getText();
//        LoadSaveHandler.Format[] formats = LoadSaveHandler.Format.values();
//        for (LoadSaveHandler.Format format : formats) {
//            if (format.getFormat().getName() == formatName) {
//                LoadSaveHandler.getInstance().load(format);
//                break;
//            }
//        }
    }

//    public JPopupMenu getProjectPopup() {
//        return projectPopup.getPopupMenu();
//    }
//
//    public JMenu getLoadDataset() {
//        JMenu loadMenu = new JMenu("Load");
//        loadMenu.setMnemonic('L');
//        loadMenu.setIcon(new ImageIcon(getClass().getResource("/icons/open.png")));
// 
//        LoadSaveHandler.Format[] formats = LoadSaveHandler.Format.values();
//        for (LoadSaveHandler.Format format : formats) {
//            JMenuItem menuItem = new JMenuItem(format.getFormat().getName(), format.getFormat().getMnemonic());
//            menuItem.addActionListener(this);
//            loadMenu.add(menuItem);
//        }
//        
//        return loadMenu;
//    }
//
//    public JMenuItem getClearDatasets() {
//        JMenuItem clearDatasets = new JMenuItem("Clear all", 'C');
//        clearDatasets.setIcon(new ImageIcon(getClass().getResource("/icons/closeData.png")));
//        clearDatasets.addActionListener(this);
//        return clearDatasets;
//    }
//
//    public static DatasetBrowserPopups getInstance() {
//        return DatasetBrowserPopupsHolder.INSTANCE;
//    }
//
//    private static class DatasetBrowserPopupsHolder {
//        private static final DatasetBrowserPopups INSTANCE = new DatasetBrowserPopups();
//    }
 }
