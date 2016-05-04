package no.uib.inf252.katscan.view.component.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Marcelo Lima
 */
public class LoadingPanel extends JPanel {

    private final boolean transparent;
    
    public LoadingPanel(boolean transparent) {
        super(new GridBagLayout());
        setBackground(Color.BLACK);
        setOpaque(!transparent);
        this.transparent = transparent;
        
        JLabel loadingIcon = new JLabel(new ImageIcon(getClass().getResource("/img/loading.gif")));
        add(loadingIcon);
    }

    @Override
    public void paint(Graphics g) {
        if (transparent) {
            ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f));
        }
        super.paint(g); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

}
