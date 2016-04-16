/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package no.uib.inf252.katscan.view.component.dataset;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import no.uib.inf252.katscan.Init;
import no.uib.inf252.katscan.data.LoadedDataHolder;

/**
 *
 * @author Marcelo Lima
 */
public class DatasetBrowserPopups implements ActionListener {
    
    private final JPopupMenu projectPopup;

    private DatasetBrowserPopups() {
        projectPopup = new JPopupMenu();
        setupProjectPopup();
    }
    
    private void setupProjectPopup() {
        projectPopup.add(getLoadDataset());
        projectPopup.add(getClearDatasets());
        projectPopup.addSeparator();
        JMenuItem item = new JMenuItem("Rename", 'R');
        item.setIcon(new ImageIcon(getClass().getResource("/icons/edit.png")));
        projectPopup.add(item);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JMenuItem menuItem = (JMenuItem)e.getSource();
        if ("Load".equals(menuItem.getText())) {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(Init.getFrameReference());

            if (option == JFileChooser.APPROVE_OPTION) {
                File data = fileChooser.getSelectedFile();

                if (!LoadedDataHolder.getInstance().load(data.getName(), data)) {
                }
            }
        }
    }

    public JPopupMenu getProjectPopup() {
        return projectPopup;
    }

    public JMenuItem getLoadDataset() {
        JMenuItem loadDataset = new JMenuItem("Load", 'L');
        loadDataset.setIcon(new ImageIcon(getClass().getResource("/icons/open.png")));
        loadDataset.addActionListener(this);
        return loadDataset;
    }

    public JMenuItem getClearDatasets() {
        JMenuItem clearDatasets = new JMenuItem("Clear all", 'C');
        clearDatasets.setIcon(new ImageIcon(getClass().getResource("/icons/closeData.png")));
        clearDatasets.addActionListener(this);
        return clearDatasets;
    }

    public static DatasetBrowserPopups getInstance() {
        return DatasetBrowserPopupsHolder.INSTANCE;
    }

    private static class DatasetBrowserPopupsHolder {
        private static final DatasetBrowserPopups INSTANCE = new DatasetBrowserPopups();
    }
 }
