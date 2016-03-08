package no.uib.inf252.katscan.view.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 *
 * @author Marcelo Lima
 */
public class BackgroundPanel extends JPanel {

    private static final String IMAGE_NAME = "/img/background.png";
    private static final BufferedImage IMAGE;
    private final AffineTransform transform;
    
    static {
        BufferedImage image = null;
        try {
            image = ImageIO.read(BackgroundPanel.class.getResource(IMAGE_NAME));
        } catch (IOException ex) {
            Logger.getLogger(BackgroundPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        IMAGE = image;
    }

    public BackgroundPanel() {
        setOpaque(true);
        setBackground(Color.WHITE);

        if (IMAGE != null) {
            setPreferredSize(new Dimension(IMAGE.getWidth(), IMAGE.getHeight()));
        }
        
        transform = new AffineTransform();
        transform.setToIdentity();
    }

//    @Override
//    public void setBounds(int x, int y, int width, int height) {
//        super.setBounds(x, y, width, height); //To change body of generated methods, choose Tools | Templates.
//        Insets insets = getInsets();
//        width -= (insets.left + insets.right);
//        height -= (insets.bottom + insets.bottom);
//        double scaleX = width / 1920d;
//        double scaleY = height / 1080d;
//        double scale = Math.min(scaleX, scaleY);
////        transform.setToScale(scale, scale);
//        transform.setToTranslation(width - (IMAGE.getWidth()), height - (IMAGE.getHeight()));
////        System.out.println("Scale: " + scale);
//    }

    @Override
    protected void paintComponent(Graphics g) {
        if (IMAGE != null) {
            g.setColor(Color.WHITE);
            Insets insets = getInsets();
            Rectangle bounds = getBounds();
            g.fillRect(0, 0, bounds.width, bounds.height);
            g.drawImage(IMAGE, bounds.width - (insets.right + IMAGE.getWidth()), bounds.height - (insets.bottom + IMAGE.getHeight()), this);
//            Graphics2D g2d = (Graphics2D) g;
//            g2d.drawImage(IMAGE, transform, this);
        } else {
            super.paintComponent(g);
        }
    }
}
