package no.uib.inf252.katscan.view.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
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
    private final BufferedImage IMAGE;

    public SplashImage() {
        setOpaque(true);
        setBackground(Color.WHITE);
        
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
            g.setColor(Color.WHITE);
            Rectangle bounds = getBounds();
            Insets insets = getInsets();
            g.fillRect(0, 0, bounds.width, bounds.height);
            g.drawImage(IMAGE, bounds.width - (insets.right + IMAGE.getWidth()), bounds.height - (insets.bottom + IMAGE.getHeight()), this);
        } else {
            super.paintComponent(g);
        }
    }
}
