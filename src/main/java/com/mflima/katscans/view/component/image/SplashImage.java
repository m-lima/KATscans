package com.mflima.katscans.view.component.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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

    private static final String IMAGE_NAME = "/img/splashWhite.png";
    private static final String IMAGE_NAME_FADE = "/img/splash.png";
    private final BufferedImage IMAGE;
    private final BufferedImage IMAGE_FADE;
    private float mix;

    public SplashImage() {
        setOpaque(true);
        setBackground(Color.WHITE);
        
        BufferedImage image = null;
        BufferedImage imageFade = null;
        try {
            image = ImageIO.read(getClass().getResource(IMAGE_NAME));
            imageFade = ImageIO.read(getClass().getResource(IMAGE_NAME_FADE));
        } catch (IOException ex) {
            Logger.getLogger(SplashImage.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (image != null) {
            setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        }

        IMAGE = image;
        IMAGE_FADE = imageFade;
    }
    
    public void setMix(float amount) {
        if (amount < 0f) {
            amount = 0f;
        } else if (amount > 1f) {
            amount = 1f;
        }
        
        mix = amount;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (IMAGE != null) {
            g.setColor(Color.WHITE);
            Rectangle bounds = getBounds();
            Insets insets = getInsets();
            g.fillRect(0, 0, bounds.width, bounds.height);
            g.drawImage(IMAGE, bounds.width - (insets.right + IMAGE.getWidth()), bounds.height - (insets.bottom + IMAGE.getHeight()), this);
            if (IMAGE_FADE != null && mix > 0f) {
                ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, mix));
                g.drawImage(IMAGE_FADE, bounds.width - (insets.right + IMAGE.getWidth()), bounds.height - (insets.bottom + IMAGE.getHeight()), this);
            }
        } else {
            super.paintComponent(g);
        }
    }
}
