package no.uib.inf252.katscan;

import com.bulenkov.darcula.DarculaLaf;
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

/**
 *
 * @author Marcelo Lima
 */
public class Init {
    
    private static MainFrame frameReference;

    private static void setLookAndFeel() {
        try {
            ArrayList<AbstractMap.SimpleEntry<Object, Object>> icons = new ArrayList<>();
            UIManager.setLookAndFeel(new MetalLookAndFeel());
                
            UIDefaults lookAndFeelDefaults = UIManager.getLookAndFeelDefaults();
            Set<Map.Entry<Object, Object>> entrySet = lookAndFeelDefaults.entrySet();
            for (Map.Entry<Object, Object> entry : entrySet) {
//                if (entry.getValue() instanceof Color) {
//                    Color color = (Color) entry.getValue();
//                    lookAndFeelDefaults.put(entry.getKey(), new Color(color.getRed(), color.getBlue(), color.getGreen()));
//                }
//                if (entry.getKey().toString().toLowerCase().contains("border") && entry.getKey().toString().toLowerCase().contains("button")) {
                if (entry.getKey().toString().toLowerCase().contains("icon")) {
                    icons.add(new AbstractMap.SimpleEntry<>(entry));
//                    System.out.println(entry.getKey() + " :: " + entry.getValue());
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
                SplashScreen dialog = new SplashScreen();
                dialog.setVisible(true);
                
                frameReference = new MainFrame();
                frameReference.setVisible(true);
            }
        });
    }

    public static MainFrame getFrameReference() {
        return frameReference;
    }
}
