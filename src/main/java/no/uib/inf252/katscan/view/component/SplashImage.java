package no.uib.inf252.katscan.view.component;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
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
public class SplashImage extends JPanel {

    private static final String IMAGE_NAME = "/img/splash.png";
    private final Image IMAGE;

    public SplashImage() {
        BufferedImage image = null;
        try {
            image = ImageIO.read(getClass().getResource(IMAGE_NAME));
        } catch (IOException ex) {
            Logger.getLogger(SplashImage.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (image != null) {
            setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        }

        IMAGE = image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (IMAGE != null) {
            g.drawImage(IMAGE, 0, 0, this);
        } else {
            super.paintComponent(g);
        }
    }
}
