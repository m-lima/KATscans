package no.uib.inf252.katscan.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.properties.DockingWindowProperties;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.theme.ShapedGradientDockingTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.ViewMap;
import net.infonode.gui.colorprovider.FixedColorProvider;
import net.infonode.gui.componentpainter.SolidColorComponentPainter;
import net.infonode.util.Direction;
import no.uib.inf252.katscan.project.KatNode;
import no.uib.inf252.katscan.project.KatViewNode;
import no.uib.inf252.katscan.project.ProjectHandler;
import no.uib.inf252.katscan.project.io.PersistenceHandler;
import no.uib.inf252.katscan.view.project.ProjectBrowser;

/**
 *
 * @author Marcelo Lima
 */
public class MainFrame extends javax.swing.JFrame implements TreeModelListener {
    
    private static final int DATASET_MENU_POSITION = 1;
    public static final Color THEME_COLOR = new Color(51, 51, 51);
    public static final Color THEME_COLOR_BRIGHTER = new Color(60, 61, 63);
    public static final Color SOFT_GRAY = new Color(187, 187, 187);
    private static final String ICON_NAME = "/icons/iconSmall.png";
    private static final String IMAGE_NAME = "/img/simple.png";

    private final RootWindow rootWindow;
    private final RootWindowProperties properties;
    private final View datasetView;
    
    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        this(null);
    }
    
    public MainFrame(GraphicsConfiguration gc) {
        super(gc);
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

        ProjectBrowser datasetBrowser = new ProjectBrowser();
        ProjectHandler.getInstance().addTreeModelListener(this);
        
        rootWindow = DockingUtil.createRootWindow(new ViewMap(), true);
        properties = rootWindow.getRootWindowProperties();
        datasetView = new View("Datasets", null, datasetBrowser);

        setupRootView(image);
        setupRootProperties();
        setupDatasetBrowser();

        setupGlobalKeys();

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(rootWindow, BorderLayout.CENTER);
        
        setupMenu();

//        loadAutomaticView();
    }

//    private void loadAutomaticView() {
//        try {
//            File file = new File("C:\\Users\\mflim_000\\Documents\\Code\\Java\\Maven\\KATscan\\misc\\datasets\\sinusveins-256x256x166.dat");
//            VoxelMatrix voxelMatrix = new DatFormat().loadData(new FileInputStream(file));
//            LoadedData.getInstance().load("Sinus", file, voxelMatrix);
//            DockingWindow oldViews = rootWindow.getWindow();
//
//            TabWindow tabWindow;
//            if (oldViews instanceof TabWindow) {
//                tabWindow = (TabWindow) oldViews;
//            } else {
//                tabWindow = new TabWindow();
//                tabWindow.setBackground(THEME_COLOR);
//                if (oldViews != null) {
//                    tabWindow.addTab(oldViews);
//                }
//                rootWindow.setWindow(tabWindow);
//            }
//
//            View view = new View("Sinus", null, new CompositeRenderer(Displayable.getByName("Sinus"), new TransferFunction()));
//            view.setPreferredMinimizeDirection(Direction.RIGHT);
//            tabWindow.addTab(view);
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

    private void setupRootView(BufferedImage image) {
        rootWindow.setBackgroundColor(THEME_COLOR);
        if (image != null) {
            rootWindow.setBackgroundImage(image);
        }
        rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);
        rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);
        rootWindow.getWindowBar(Direction.LEFT).setContentPanelSize(300);
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
        JMenu menu = ProjectHandler.getInstance().getRoot().getMenu(true);
        if (mbrMain.getComponentCount() > 2) {
            mbrMain.remove(DATASET_MENU_POSITION);
        }
        mbrMain.add(menu, DATASET_MENU_POSITION);
        mbrMain.validate();
    }

    //TODO Set global keys
    private void setupGlobalKeys() {
        getRootPane().getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK), "showTree");
        getRootPane().getActionMap().put("showTree", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (datasetView.isVisible()) {
                    datasetView.restore();
//                    datasetView.show();
//                    ((DatasetBrowser) datasetView.getComponent()).focusTree();
                } else {
                    datasetView.minimize();
                }
            }
        });
    }
    
    
    @Override
    public void treeNodesChanged(TreeModelEvent e) {
        setupMenu();
    }

    @Override
    public void treeNodesInserted(TreeModelEvent e) {
        setupMenu();
        KatNode node = (KatNode) e.getTreePath().getLastPathComponent();
        traverseAndFindViews(node);
    }
    
    private void traverseAndFindViews(KatNode node) {
        Enumeration<KatNode> children = node.children();
        while (children.hasMoreElements()) {
            KatNode child = children.nextElement();
            if (child instanceof KatViewNode) {
                if (((KatViewNode) child).isNewView()) {
                    addView((KatViewNode) child);
                }
            } else {
                traverseAndFindViews(child);
            }
        }
    }

    @Override
    public void treeNodesRemoved(TreeModelEvent e) {
        setupMenu();
    }

    @Override
    public void treeStructureChanged(TreeModelEvent e) {
        setupMenu();
    }
    
    private void addView(KatViewNode view) {
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

        view.getView().setPreferredMinimizeDirection(Direction.RIGHT);
        view.setNewView(false);
        tabWindow.addTab(view.getView());
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
    private javax.swing.JMenu mnuFile;
    private javax.swing.JMenu mnuWindow;
    private javax.swing.JPopupMenu.Separator sepCloseWindow;
    private javax.swing.JPopupMenu.Separator sepNew;
    private javax.swing.JPopupMenu.Separator sepSaveLoad;
    // End of variables declaration//GEN-END:variables

}
