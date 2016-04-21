package no.uib.inf252.katscan.view;

import com.jogamp.opengl.GLException;
import no.uib.inf252.katscan.view.component.Histogram;
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
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.properties.DockingWindowProperties;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.theme.ShapedGradientDockingTheme;
import net.infonode.docking.util.AbstractViewMap;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.ViewMap;
import net.infonode.gui.colorprovider.FixedColorProvider;
import net.infonode.gui.componentpainter.SolidColorComponentPainter;
import net.infonode.util.Direction;
import no.uib.inf252.katscan.data.LoadedDataHolder;
import no.uib.inf252.katscan.event.DataHolderListener;
import no.uib.inf252.katscan.model.PersistenceHandler;
import no.uib.inf252.katscan.view.component.dataset.DatasetBrowser;
import no.uib.inf252.katscan.view.component.dataset.DatasetBrowserPopups;
import no.uib.inf252.katscan.view.opengl.CompositeRenderer;
import no.uib.inf252.katscan.view.opengl.MaximumRenderer;
import no.uib.inf252.katscan.view.opengl.SliceNavigator;

/**
 *
 * @author Marcelo Lima
 */
public class MainFrame extends javax.swing.JFrame implements DataHolderListener {

    public static final Color THEME_COLOR = new Color(51, 51, 51);
    public static final Color THEME_COLOR_BRIGHTER = new Color(60, 61, 63);
    public static final Color SOFT_GRAY = new Color(187, 187, 187);
    private static final String ICON_NAME = "/icons/iconSmall.png";
    private static final String IMAGE_NAME = "/img/simple.png";
    
    private final AbstractViewMap viewMap;
    private final RootWindow rootWindow;
    private final RootWindowProperties properties;

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        BufferedImage image = null;
        try {
            setIconImage(ImageIO.read(getClass().getResource(ICON_NAME)));
            image = ImageIO.read(getClass().getResource(IMAGE_NAME));
        } catch (IOException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        initComponents();

        setTitle("KATscans");
        setSize(1000, 1000);
        setExtendedState(getExtendedState() | MAXIMIZED_BOTH);

        viewMap = new ViewMap();
        rootWindow = DockingUtil.createRootWindow(viewMap, true);
        properties = rootWindow.getRootWindowProperties();
        
        setupRootView(image);
        setupRootProperties();        
        setupDatasetBrowser();
        setupMenu();
        
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(rootWindow, BorderLayout.CENTER);
        
        LoadedDataHolder.getInstance().addDataHolderListener(this);
        
//        loadAutomaticView();
    }

    private void loadAutomaticView() throws GLException {
        LoadedDataHolder.getInstance().load("Sinus", new File("C:\\Users\\mflim_000\\Documents\\Code\\Java\\Maven\\KATscan\\misc\\sinusveins-256x256x166.dat"));
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

        View view = new View("Sinus", null, new CompositeRenderer("Sinus"));
        view.setPreferredMinimizeDirection(Direction.RIGHT);
        tabWindow.addTab(view);        
    }

    private void setupRootView(BufferedImage image) {
        rootWindow.setBackgroundColor(THEME_COLOR);
        if (image != null) {
            rootWindow.setBackgroundImage(image);
        }
        rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);
        rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);
    }

    private void setupRootProperties() {
        DockingWindowsTheme theme = new ShapedGradientDockingTheme();
        properties.addSuperObject(theme.getRootWindowProperties());
        properties.getSplitWindowProperties().setDividerLocationDragEnabled(true);
        properties.getShapedPanelProperties().setComponentPainter(new SolidColorComponentPainter(new FixedColorProvider(THEME_COLOR)));
        properties.getDragRectangleShapedPanelProperties().setComponentPainter(new SolidColorComponentPainter(new FixedColorProvider(new Color(50, 50, 150, 100))));
        properties.setEdgeSplitDistance(50);
    }

    private void setupDatasetBrowser() {
        TabWindow tabWindow = (TabWindow) rootWindow.getWindow();
        View datasetView = new View("Datasets", null, new DatasetBrowser());
        DockingWindowProperties datasetsProperties = datasetView.getWindowProperties();
        datasetsProperties.setCloseEnabled(false);
        datasetsProperties.setDragEnabled(false);
        datasetsProperties.setUndockEnabled(false);
        datasetsProperties.setUndockOnDropEnabled(false);
        datasetsProperties.setRestoreEnabled(false);
        datasetView.setPreferredMinimizeDirection(Direction.LEFT);
        tabWindow.addTab(datasetView);
        datasetView.minimize();
    }
    
    private void setupMenu() {
        DatasetBrowserPopups popups = DatasetBrowserPopups.getInstance();
        mnuDatasets.addSeparator();
        mnuDatasets.add(popups.getLoadDataset());
        mnuDatasets.add(popups.getClearDatasets());
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
        mitSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mitSaveActionPerformed(evt);
            }
        });
        mnuFile.add(mitSave);
        mnuFile.add(sepSaveLoad);

        mitExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/exit.png"))); // NOI18N
        mitExit.setMnemonic('X');
        mitExit.setText("Exit");
        mitExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mitExitActionPerformed(evt);
            }
        });
        mnuFile.add(mitExit);

        mbrMain.add(mnuFile);

        mnuDatasets.setMnemonic('D');
        mnuDatasets.setText("Datasets");
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
        PersistenceHandler.getInstance().load();
    }//GEN-LAST:event_mitLoadActionPerformed

    private void mitExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mitExitActionPerformed
        PersistenceHandler.getInstance().save();
        System.exit(0);
    }//GEN-LAST:event_mitExitActionPerformed

    private void mitSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mitSaveActionPerformed
        PersistenceHandler.getInstance().save();
    }//GEN-LAST:event_mitSaveActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar mbrMain;
    private javax.swing.JMenuItem mitCloseWindow;
    private javax.swing.JMenuItem mitExit;
    private javax.swing.JMenuItem mitLoad;
    private javax.swing.JMenuItem mitNew;
    private javax.swing.JMenuItem mitSave;
    private javax.swing.JMenu mnuDatasets;
    private javax.swing.JMenu mnuFile;
    private javax.swing.JMenu mnuWindow;
    private javax.swing.JPopupMenu.Separator sepCloseWindow;
    private javax.swing.JPopupMenu.Separator sepNew;
    private javax.swing.JPopupMenu.Separator sepSaveLoad;
    // End of variables declaration//GEN-END:variables

    @Override
    public void dataAdded(final String name, final String file) {
        JMenu dataItem = new JMenu(name);
        
        JMenuItem rendererMenu = new JMenuItem("Volume Renderer", 'V');
        JMenuItem maximumMenu = new JMenuItem("Maximum Renderer", 'M');
        JMenuItem sliceMenu = new JMenuItem("Slice Navigator", 'S');
        JMenuItem histogramMenu = new JMenuItem("Histogram", 'H');
        JMenuItem removeMenu = new JMenuItem("Remove", 'R');
        
        rendererMenu.addActionListener(new ActionListener() {
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
                
                View view = new View("Volume Renderer - " + name, null, new CompositeRenderer(name));
                view.setPreferredMinimizeDirection(Direction.RIGHT);
                tabWindow.addTab(view);
            }
        });
        
        maximumMenu.addActionListener(new ActionListener() {
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
                
                View view = new View("Volume Renderer - " + name, null, new MaximumRenderer(name));
                view.setPreferredMinimizeDirection(Direction.RIGHT);
                tabWindow.addTab(view);
            }
        });
        
        sliceMenu.addActionListener(new ActionListener() {
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

                View view = new View("Slice Navigator - " + name, null, new SliceNavigator(name));
                view.setPreferredMinimizeDirection(Direction.RIGHT);
                tabWindow.addTab(view);
            }
        });
        
        histogramMenu.addActionListener(new ActionListener() {
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
        
        removeMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LoadedDataHolder.getInstance().unload(name);
            }
        });
        
        dataItem.add(rendererMenu);
        dataItem.add(maximumMenu);
        dataItem.add(sliceMenu);
        dataItem.add(histogramMenu);
        dataItem.addSeparator();
        dataItem.add(removeMenu);
        
        mnuDatasets.add(dataItem, 0);
    }

    @Override
    public void dataRemoved(String name) {
        for (int i = 0; i < mnuDatasets.getMenuComponentCount(); i++) {
            JMenu subMenu = (JMenu) mnuDatasets.getMenuComponent(i);
            if (name.equals(subMenu.getText())) {
                mnuDatasets.remove(subMenu);
                return;
            }
        }
    }
 
}
