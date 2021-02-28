package com.mflima.katscans.view;

import com.mflima.katscans.project.KatNode;
import com.mflima.katscans.project.KatViewNode;
import com.mflima.katscans.project.ProjectHandler;
import com.mflima.katscans.project.displayable.Displayable;
import com.mflima.katscans.project.io.PersistenceHandler;
import com.mflima.katscans.view.project.ProjectBrowser;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowAdapter;
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

/** @author Marcelo Lima */
public class MainFrame extends JFrame implements TreeModelListener, ActionListener {

  private static final int DATASET_MENU_POSITION = 1;
  private static final int WINDOW_MENU_POSITION = 2;
  public static final Color THEME_COLOR = new Color(51, 51, 51);
  private static final String ICON_NAME = "/icons/iconSmall.png";
  private static final String IMAGE_NAME = "/img/simple.png";

  private final RootWindow rootWindow;
  private final RootWindowProperties properties;
  private final View datasetView;
  private final ProjectBrowser datasetBrowser;
  private final HelpDiag helpDiag;

  private boolean pojectBrowserMinized;

  private final ArrayList<KatViewNode> views;
  private final JMenuItem mitCloseAll;
  private boolean updatingMenu;

  private final Timer autoSaveTimer;

  public MainFrame(GraphicsConfiguration gc) {
    super(gc);
    BufferedImage image = null;
    try {
      setIconImage(ImageIO.read(getClass().getResource(ICON_NAME)));
      image = ImageIO.read(getClass().getResource(IMAGE_NAME));
    } catch (IOException ex) {
      Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
    }

    helpDiag = new HelpDiag(this);

    views = new ArrayList<>();
    mitCloseAll = new JMenuItem("Close all", 'C');
    mitCloseAll.setIcon(new ImageIcon(getClass().getResource("/icons/closeWindows.png")));
    mitCloseAll.addActionListener(this);

    initComponents();

    setTitle("KATscans");
    setSize(1000, 1000);
    setExtendedState(getExtendedState() | MAXIMIZED_BOTH);

    datasetBrowser = new ProjectBrowser();
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

    setupMenus();

    autoSaveTimer = new Timer(5 * 60 * 1000, e -> PersistenceHandler.getInstance().autoSave());
    autoSaveTimer.setRepeats(true);
    autoSaveTimer.setInitialDelay(0);
  }

  private void setupRootView(BufferedImage image) {
    rootWindow.setBackgroundColor(THEME_COLOR);
    if (image != null) {
      rootWindow.setBackgroundImage(image);
    }
    rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);
    rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);
    rootWindow.getWindowBar(Direction.LEFT).setContentPanelSize(300);
    rootWindow.addListener(
        new DockingWindowAdapter() {
          @Override
          public void windowClosed(DockingWindow window) {
            checkAndRemove(window);
          }
        });
  }

  private void checkAndRemove(DockingWindow view) {
    if (view instanceof View) {
      for (KatViewNode katView : views) {
        if (katView.getView().equals(view)) {
          ProjectHandler.getInstance().removeNodeFromParent(katView);

          Component component = ((View) view).getComponent();
          Displayable parent = katView.getParent();
          katView.stopListening(parent, component);
          return;
        }
      }
    } else {
      for (int i = 0; i < view.getChildWindowCount(); i++) {
        checkAndRemove(view.getChildWindow(i));
      }
    }
  }

  private void setupRootProperties() {
    DockingWindowsTheme theme = new ShapedGradientDockingTheme();
    properties.addSuperObject(theme.getRootWindowProperties());
    properties.getSplitWindowProperties().setDividerLocationDragEnabled(true);
    properties
        .getShapedPanelProperties()
        .setComponentPainter(new SolidColorComponentPainter(new FixedColorProvider(THEME_COLOR)));
    properties
        .getDragRectangleShapedPanelProperties()
        .setComponentPainter(
            new SolidColorComponentPainter(new FixedColorProvider(new Color(50, 50, 150, 100))));
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
    pojectBrowserMinized = true;
  }

  private void setupMenus() {
    if (updatingMenu) {
      return;
    }

    updatingMenu = true;
    while (mbrMain.getComponentCount() > 2) {
      mbrMain.remove(1);
    }

    mbrMain.add(ProjectHandler.getInstance().getRoot().getMenu(true), DATASET_MENU_POSITION);
    mbrMain.add(buildWindowMenu(), WINDOW_MENU_POSITION);
    mbrMain.validate();
    updatingMenu = false;
  }

  private void setupGlobalKeys() {
    getRootPane()
        .getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK), "showTree");
    getRootPane()
        .getActionMap()
        .put(
            "showTree",
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                if (pojectBrowserMinized) {
                  datasetView.makeVisible();
                  datasetBrowser.focusTree();
                  pojectBrowserMinized = false;
                } else {
                  datasetView.restore();
                  datasetView.minimize();
                  pojectBrowserMinized = true;
                }
              }
            });
    getRootPane()
        .getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, KeyEvent.SHIFT_DOWN_MASK), "showHelp");
    getRootPane()
        .getActionMap()
        .put(
            "showHelp",
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                helpDiag.setVisible(!helpDiag.isVisible());
              }
            });
  }

  private JMenu buildWindowMenu() {
    JMenu menu = new JMenu("Windows");
    menu.setMnemonic('W');

    views.clear();
    populateViewList(ProjectHandler.getInstance().getRoot());

    JMenuItem item;
    char mnemonic;
    String title;
    for (int i = 0; i < views.size(); i++) {
      title = views.get(i).getName();
      if (i < 10) {
        mnemonic = (char) (i + '1');
        item = new JMenuItem(String.format("[%c] %s", mnemonic, title));
        item.setMnemonic(mnemonic);
      } else {
        item = new JMenuItem(title);
      }
      item.addActionListener(this);
      menu.add(item);
    }

    if (!views.isEmpty()) {
      menu.addSeparator();
    }

    menu.add(mitCloseAll);

    return menu;
  }

  private void populateViewList(KatNode node) {
    if (node instanceof KatViewNode) {
      views.add((KatViewNode) node);
    } else {
      Enumeration<KatNode> children = node.children();
      while (children.hasMoreElements()) {
        populateViewList(children.nextElement());
      }
    }
  }

  @Override
  public void treeNodesChanged(TreeModelEvent e) {
    treeChanged((KatNode) e.getTreePath().getLastPathComponent());
  }

  @Override
  public void treeNodesInserted(TreeModelEvent e) {
    treeChanged((KatNode) e.getTreePath().getLastPathComponent());
  }

  @Override
  public void treeNodesRemoved(TreeModelEvent e) {
    treeChanged((KatNode) e.getTreePath().getLastPathComponent());
  }

  @Override
  public void treeStructureChanged(TreeModelEvent e) {
    treeChanged((KatNode) e.getTreePath().getLastPathComponent());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    JMenuItem item = (JMenuItem) e.getSource();
    if (item == mitCloseAll) {
      updatingMenu = true;
      for (KatViewNode view : views) {
        view.removeWindow();
      }
      updatingMenu = false;
      setupMenus();
    } else {
      Container parent = item.getParent();
      for (int i = 0; i < parent.getComponentCount(); i++) {
        if (parent.getComponent(i) == item) {
          views.get(i).getView().makeVisible();
        }
      }
    }
  }

  private void treeChanged(KatNode node) {
    setupMenus();
    traverseAndFindViews(node);
    autoSaveTimer.restart();
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

  private void initComponents() {

    mbrMain = new JMenuBar();

    JMenu mnuFile = new JMenu();
    JMenuItem mitNew = new JMenuItem();
    JMenuItem mitLoad = new JMenuItem();
    JMenuItem mitSave = new JMenuItem();
    JMenuItem mitSaveAs = new JMenuItem();
    JMenuItem mitExit = new JMenuItem();

    JMenu mnuHelp = new JMenu();
    JMenuItem mitHelp = new JMenuItem();

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    mnuFile.setMnemonic('F');
    mnuFile.setText("File");

    mitNew.setIcon(new ImageIcon(getClass().getResource("/icons/new.png")));
    mitNew.setMnemonic('N');
    mitNew.setText("New");
    mnuFile.add(mitNew);
    mnuFile.add(new JPopupMenu.Separator());

    mitLoad.setIcon(new ImageIcon(getClass().getResource("/icons/open.png")));
    mitLoad.setMnemonic('L');
    mitLoad.setText("Load");
    mitLoad.addActionListener(this::mitLoadActionPerformed);
    mnuFile.add(mitLoad);

    mitSave.setIcon(new ImageIcon(getClass().getResource("/icons/save.png")));
    mitSave.setMnemonic('S');
    mitSave.setText("Save");
    mitSave.addActionListener(this::mitSaveActionPerformed);
    mnuFile.add(mitSave);

    mitSaveAs.setIcon(new ImageIcon(getClass().getResource("/icons/save.png")));
    mitSaveAs.setMnemonic('A');
    mitSaveAs.setText("Save As");
    mitSaveAs.addActionListener(this::mitSaveAsActionPerformed);
    mnuFile.add(mitSaveAs);
    mnuFile.add(new JPopupMenu.Separator());

    mitExit.setIcon(new ImageIcon(getClass().getResource("/icons/exit.png")));
    mitExit.setMnemonic('X');
    mitExit.setText("Exit");
    mitExit.addActionListener(this::mitExitActionPerformed);
    mnuFile.add(mitExit);

    mbrMain.add(mnuFile);

    mnuHelp.setMnemonic('H');
    mnuHelp.setText("Help");

    mitHelp.setIcon(new ImageIcon(getClass().getResource("/icons/help.png")));
    mitHelp.setMnemonic('C');
    mitHelp.setText("Commands");
    mitHelp.addActionListener(this::mitHelpActionPerformed);
    mnuHelp.add(mitHelp);

    mbrMain.add(mnuHelp);

    setJMenuBar(mbrMain);

    pack();
  }

  private void mitLoadActionPerformed(ActionEvent evt) {
    KatViewNode[] oldViews = new KatViewNode[views.size()];
    if (oldViews.length > 0) {
      oldViews = views.toArray(oldViews);
    }

    if (PersistenceHandler.getInstance().load()) {
      updatingMenu = true;
      for (KatViewNode view : oldViews) {
        view.getView().close();
      }
      updatingMenu = false;
    }
  }

  private void mitExitActionPerformed(ActionEvent evt) {
    PersistenceHandler.getInstance().autoSave();
    System.exit(0);
  }

  private void mitSaveActionPerformed(ActionEvent evt) {
    PersistenceHandler.getInstance().save();
  }

  private void mitSaveAsActionPerformed(ActionEvent evt) {
    PersistenceHandler.getInstance().saveAs();
  }

  private void mitHelpActionPerformed(ActionEvent evt) {
    helpDiag.setVisible(true);
  }

  private JMenuBar mbrMain;
}
