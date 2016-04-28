package no.uib.inf252.katscan;

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
import no.uib.inf252.katscan.view.MainFrame;
import no.uib.inf252.katscan.view.SplashScreen;

//TODO [ ] Exceptions
//TODO [ ] Always clip
//TODO [ ] Raw ratio calculation
//TODO [ ] Loading size (spinners)
//TODO [ ] Alpha compositing
//TODO [ ] Slice as zooms
//TODO [ ] Icons for all items
//TODO [ ] Remove reference to model from KatNodes
//TODO [ ] Remove CUT and STRUCTURE from Transfer Function
//TODO [ ] Auto-open transfer function or its children
//TODO [ ] All displayables have TransFunc
//TODO [ ] Precalculate matrices
//TODO [ ] Max raycasting distance
//TODO [ ] Raycasting steps
//DONE [X] Raw reverse Z
//DONE [X] Camera initial zoom
//DONE [X] Saturation of transfer function

/**
 *
 * @author Marcelo Lima
 */
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
                if (entry.getKey().toString().toLowerCase().contains("icon")) {
                    icons.add(new AbstractMap.SimpleEntry<>(entry));
                }
            }

            UIManager.setLookAndFeel(new DarculaLaf());
            lookAndFeelDefaults = UIManager.getLookAndFeelDefaults();
            for (AbstractMap.SimpleEntry<Object, Object> entry : icons) {
                lookAndFeelDefaults.put(entry.getKey(), entry.getValue());
            }
        } catch (UnsupportedLookAndFeelException e) {
            java.util.logging.Logger.getLogger(SplashScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, e);
            try {
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (ClassNotFoundException ex) {
                java.util.logging.Logger.getLogger(SplashScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                java.util.logging.Logger.getLogger(SplashScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                java.util.logging.Logger.getLogger(SplashScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (javax.swing.UnsupportedLookAndFeelException ex) {
                java.util.logging.Logger.getLogger(SplashScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        setLookAndFeel();

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice[] gs = ge.getScreenDevices();
                GraphicsConfiguration gc;

                if (gs.length > MONITOR) {
                    gc = gs[MONITOR].getDefaultConfiguration();
                } else {
                    gc = gs[0].getDefaultConfiguration();
                }
                
//                SplashScreen dialog = new SplashScreen(null, gc);
//                dialog.setVisible(true);

                frameReference = new MainFrame(gc);
                frameReference.setVisible(true);
            }
        });
    }

    public static MainFrame getFrameReference() {
        return frameReference;
    }
}
