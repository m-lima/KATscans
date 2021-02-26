package com.mflima.katscans;

import com.bulenkov.darcula.DarculaLaf;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;
import com.mflima.katscans.view.MainFrame;
import com.mflima.katscans.view.SplashScreen;

// TODO [ ] Contour (Normal · View) [|Δƒ| · (1 - N · V)^e]
// TODO [ ] 2D TF
// TODO [ ] Precalculate jitter in texture
// TODO [ ] Precalculate TF in texture
// TODO [ ] Precalculate Δƒ in texture
// TODO [ ] Test NO INTERPOLATION
// TODO [ ] Exceptions
// TODO [ ] Alpha compositing
// TODO [ ] Save project
// TODO [ ] Window management
// TODO [ ] Icons for all items
// TODO [ ] Remove reference to model from KatNodes
// TODO [ ] Make trackball a node
// TODO [ ] Split trackball functions
// TODO [ ] Subgroup should be "Overrider"
// TODO [ ] The verb should be override instead of add (i.e. Override Cut)
// TODO [ ] Make Human/Basic for histogram in every Displayable
// DONE [X] Loading glass pane when building view
// DONE [X] FIND AWESOME DEFAULT TRANSFER FUNCTION!!!!!!!!!!!!
// DONE [X] Load Hand from the top and see the oversampling.. Fix with frame buffer
// DONE [X] Auto-open transfer function or its children
// DONE [X] Slice as zooms
// DONE [X] All displayables have TransFunc
// DONE [X] Raycasting steps
// DONE [X] Precalculate matrices
// DONE [X] Max raycasting distance
// DONE [X] Always clip
// DONE [X] Raw ratio calculation
// DONE [X] Loading size (spinners)
// DONE [X] Raw reverse Z
// DONE [X] Camera initial zoom
// DONE [X] Saturation of transfer function

/** @author Marcelo Lima */
public class Init {

  private static MainFrame frameReference;
  private static final int MONITOR = 0;

  private static void setLookAndFeel() {
    try {
      ArrayList<AbstractMap.SimpleEntry<Object, Object>> icons = new ArrayList<>();
      UIManager.setLookAndFeel(new MetalLookAndFeel());

      UIDefaults lookAndFeelDefaults = UIManager.getLookAndFeelDefaults();
      Set<Map.Entry<Object, Object>> entrySet = lookAndFeelDefaults.entrySet();
      for (Map.Entry<Object, Object> entry : entrySet) {
        String name = entry.getKey().toString();
        if (!name.startsWith("Tree") && name.toLowerCase().contains("icon")) {
          icons.add(new AbstractMap.SimpleEntry<>(entry));
        }
      }

      UIManager.setLookAndFeel(new DarculaLaf());
      lookAndFeelDefaults = UIManager.getLookAndFeelDefaults();
      for (AbstractMap.SimpleEntry<Object, Object> entry : icons) {
        lookAndFeelDefaults.put(entry.getKey(), entry.getValue());
      }
    } catch (UnsupportedLookAndFeelException e) {
      java.util.logging.Logger.getLogger(SplashScreen.class.getName())
          .log(java.util.logging.Level.SEVERE, null, e);
      try {
        for (javax.swing.UIManager.LookAndFeelInfo info :
            javax.swing.UIManager.getInstalledLookAndFeels()) {
          if ("Nimbus".equals(info.getName())) {
            javax.swing.UIManager.setLookAndFeel(info.getClassName());
            break;
          }
        }
      } catch (ClassNotFoundException
          | InstantiationException
          | IllegalAccessException
          | UnsupportedLookAndFeelException ex) {
        java.util.logging.Logger.getLogger(SplashScreen.class.getName())
            .log(java.util.logging.Level.SEVERE, null, ex);
      }
    }
  }

  /** @param args the command line arguments */
  public static void main(String[] args) {
    setLookAndFeel();

    /* Create and display the dialog */
    java.awt.EventQueue.invokeLater(
        () -> {
          GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
          GraphicsDevice[] gs = ge.getScreenDevices();
          GraphicsConfiguration gc = gs[MONITOR].getDefaultConfiguration();

          SplashScreen dialog = new SplashScreen(null, gc);
          dialog.setVisible(true);

          frameReference = new MainFrame(gc);
          frameReference.setVisible(true);
        });
  }

  public static MainFrame getFrameReference() {
    return frameReference;
  }
}
