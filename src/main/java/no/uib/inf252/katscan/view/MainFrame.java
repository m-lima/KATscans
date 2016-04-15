package no.uib.inf252.katscan.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.theme.ShapedGradientDockingTheme;
import net.infonode.docking.util.AbstractViewMap;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.StringViewMap;
import net.infonode.gui.colorprovider.FixedColorProvider;
import net.infonode.gui.componentpainter.SolidColorComponentPainter;
import net.infonode.util.Direction;
import no.uib.inf252.katscan.data.LoadedDataHolder;
import no.uib.inf252.katscan.event.DataHolderListener;
import no.uib.inf252.katscan.view.component.BackgroundPanel;
import no.uib.inf252.katscan.view.opengl.SliceNavigator;
import no.uib.inf252.katscan.view.opengl.VolumeRenderer;

/**
 *
 * @author Marcelo Lima
 */
public class MainFrame extends javax.swing.JFrame implements DataHolderListener {

    private static final Color GRAY = new Color(51, 51, 51);
    private static final String ICON_NAME = "/icons/iconSmall.png";
    private static final String IMAGE_NAME = "/img/simple.png";

    private final AbstractViewMap rootMap;
    private final RootWindow rootWindow;
    private final RootWindowProperties rootProperties;

    private final AbstractViewMap viewMap;
    private final RootWindow viewWindow;
    private final RootWindowProperties viewProperties;

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        BufferedImage image = null;
        try {
            setIconImage(ImageIO.read(getClass().getResource(ICON_NAME)));
            image = ImageIO.read(getClass().getResource(IMAGE_NAME));
        } catch (IOException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null,
                    ex);
        }

        setTitle("KATscans");

        initComponents();
        setSize(1000, 1000);
        setExtendedState(getExtendedState() | MAXIMIZED_BOTH);

        DockingWindowsTheme theme = new ShapedGradientDockingTheme();

        rootMap = new StringViewMap();
        viewMap = new StringViewMap();

        rootWindow = DockingUtil.createRootWindow(rootMap, true);
        rootWindow.setBackgroundColor(GRAY);
        if (image != null) {
            rootWindow.setBackgroundImage(image);
        }
        rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);
        rootProperties = rootWindow.getRootWindowProperties();
        rootProperties.addSuperObject(theme.getRootWindowProperties());
        rootProperties.getDockingWindowProperties().setCloseEnabled(false);
        rootProperties.getDockingWindowProperties().setDragEnabled(false);
        rootProperties.getDockingWindowProperties().setUndockEnabled(false);
        rootProperties.getDockingWindowProperties().
                setUndockOnDropEnabled(false);
        rootProperties.getDockingWindowProperties().setDockEnabled(false);
        rootProperties.getSplitWindowProperties().setDividerLocationDragEnabled(
                true);
        rootProperties.getShapedPanelProperties().setComponentPainter(
                new SolidColorComponentPainter(new FixedColorProvider(GRAY)));

        viewWindow = DockingUtil.createRootWindow(viewMap, true);
        viewWindow.setBackgroundColor(GRAY);
        viewWindow.getWindowBar(Direction.RIGHT).setEnabled(true);
        viewProperties = viewWindow.getRootWindowProperties();
        viewProperties.addSuperObject(theme.getRootWindowProperties());
        viewProperties.getSplitWindowProperties().setDividerLocationDragEnabled(
                true);
        viewProperties.getShapedPanelProperties().setComponentPainter(
                new SolidColorComponentPainter(new FixedColorProvider(GRAY)));
        viewProperties.setEdgeSplitDistance(50);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(rootWindow, BorderLayout.CENTER);

        LoadedDataHolder.getInstance().addDataHolderListener(this);
        final SplitWindow splitWindow = new SplitWindow(true, 0.15f, new View(
                "Datasets", null, new BackgroundPanel()), new View("bla",
                null, new BackgroundPanel()));
        splitWindow.setOpaque(false);
        rootWindow.setWindow(splitWindow);

//        rootWindow.split(new View("Datasets", null, new BackgroundPanel()), Direction.LEFT, 0.15f);

//        DockingWindow oldViews = rootWindow.getWindow();
//
//        TabWindow tabWindow;
//        if (oldViews instanceof TabWindow) {
//            tabWindow = (TabWindow) oldViews;
//        } else {
//            tabWindow = new TabWindow();
//            tabWindow.setBackground(GRAY);
//            if (oldViews != null) {
//                tabWindow.addTab(oldViews);
//            }
//        }
//        
//        tabWindow.addTab(splitWindow);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mbrMain = new javax.swing.JMenuBar();
        mnuFile = new javax.swing.JMenu();
        mitNew = new javax.swing.JMenuItem();
        sepNew = new javax.swing.JPopupMenu.Separator();
        mitLoad = new javax.swing.JMenuItem();
        mitSave = new javax.swing.JMenuItem();
        sepSaveLoad = new javax.swing.JPopupMenu.Separator();
        mitExit = new javax.swing.JMenuItem();
        mnuDatasets = new javax.swing.JMenu();
        sepCloseData = new javax.swing.JPopupMenu.Separator();
        mitCloseData = new javax.swing.JMenuItem();
        mnuWindow = new javax.swing.JMenu();
        sepCloseWindow = new javax.swing.JPopupMenu.Separator();
        mitCloseWindow = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        mnuFile.setMnemonic('F');
        mnuFile.setText("File");

        mitNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/new.png"))); // NOI18N
        mitNew.setMnemonic('N');
        mitNew.setText("New");
        mnuFile.add(mitNew);
        mnuFile.add(sepNew);

        mitLoad.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/open.png"))); // NOI18N
        mitLoad.setMnemonic('L');
        mitLoad.setText("Load");
        mitLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mitLoadActionPerformed(evt);
            }
        });
        mnuFile.add(mitLoad);

        mitSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/save.png"))); // NOI18N
        mitSave.setMnemonic('S');
        mitSave.setText("Save");
        mnuFile.add(mitSave);
        mnuFile.add(sepSaveLoad);

        mitExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/exit.png"))); // NOI18N
        mitExit.setMnemonic('X');
        mitExit.setText("Exit");
        mnuFile.add(mitExit);

        mbrMain.add(mnuFile);

        mnuDatasets.setMnemonic('D');
        mnuDatasets.setText("Datasets");
        mnuDatasets.add(sepCloseData);

        mitCloseData.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/closeData.png"))); // NOI18N
        mitCloseData.setMnemonic('X');
        mitCloseData.setText("Close All");
        mnuDatasets.add(mitCloseData);

        mbrMain.add(mnuDatasets);

        mnuWindow.setMnemonic('W');
        mnuWindow.setText("Window");
        mnuWindow.add(sepCloseWindow);

        mitCloseWindow.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/closeWindows.png"))); // NOI18N
        mitCloseWindow.setMnemonic('X');
        mitCloseWindow.setText("Close All");
        mnuWindow.add(mitCloseWindow);

        mbrMain.add(mnuWindow);

        setJMenuBar(mbrMain);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mitLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mitLoadActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {
            File data = fileChooser.getSelectedFile();

            LoadedDataHolder.getInstance().load(data.getName(), data);
        }
    }//GEN-LAST:event_mitLoadActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar mbrMain;
    private javax.swing.JMenuItem mitCloseData;
    private javax.swing.JMenuItem mitCloseWindow;
    private javax.swing.JMenuItem mitExit;
    private javax.swing.JMenuItem mitLoad;
    private javax.swing.JMenuItem mitNew;
    private javax.swing.JMenuItem mitSave;
    private javax.swing.JMenu mnuDatasets;
    private javax.swing.JMenu mnuFile;
    private javax.swing.JMenu mnuWindow;
    private javax.swing.JPopupMenu.Separator sepCloseData;
    private javax.swing.JPopupMenu.Separator sepCloseWindow;
    private javax.swing.JPopupMenu.Separator sepNew;
    private javax.swing.JPopupMenu.Separator sepSaveLoad;
    // End of variables declaration//GEN-END:variables

    @Override
    public void dataAdded(final String name) {
        JMenu dataItem = new JMenu(name);

        JMenuItem rendererMenu = new JMenuItem("Volume Renderer");
        JMenuItem sliceMenu = new JMenuItem("Slice Navigator");
        JMenuItem histogramMenu = new JMenuItem("Histogram");

        rendererMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DockingWindow oldViews = viewWindow.getWindow();

                TabWindow tabWindow;
                if (oldViews instanceof TabWindow) {
                    tabWindow = (TabWindow) oldViews;
                } else {
                    tabWindow = new TabWindow();
                    tabWindow.setBackground(GRAY);
                    if (oldViews != null) {
                        tabWindow.addTab(oldViews);
                    }
                }

                tabWindow.addTab(new View("Volume", null, new VolumeRenderer(
                        name)));
            }
        });

        sliceMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DockingWindow oldViews = viewWindow.getWindow();

                TabWindow tabWindow;
                if (oldViews instanceof TabWindow) {
                    tabWindow = (TabWindow) oldViews;
                } else {
                    tabWindow = new TabWindow();
                    tabWindow.setBackground(GRAY);
                    if (oldViews != null) {
                        tabWindow.addTab(oldViews);
                    }
                }

                tabWindow.addTab(new View("Volume", null, new SliceNavigator(
                        name)));
            }
        });

        dataItem.add(rendererMenu);
        dataItem.add(sliceMenu);
        dataItem.add(histogramMenu);

        mnuDatasets.add(dataItem, 0);
    }

    @Override
    public void dataRemoved(String name) {
    }

}
