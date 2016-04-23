package no.uib.inf252.katscan.view.transferfunction;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import no.uib.inf252.katscan.util.TransferFunction;

/**
 *
 * @author Marcelo Lima
 */
public class TransferFunctionViewer extends JComponent {
    
    private final TransferFunctionEditor parent;

    public TransferFunctionViewer(TransferFunctionEditor parent) {
        setBorder(new EtchedBorder());
        setOpaque(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    float point = e.getX();
                    point /= getWidth();

                    BufferedImage image = new BufferedImage(getWidth(), 1, BufferedImage.TYPE_4BYTE_ABGR);
                    Graphics g = image.getGraphics();
                    paintComponent(g);
                    TransferFunctionViewer.this.parent.getTransferFunction().addPoint(new Color(image.getRGB(e.getX(), 0)), point);
                }
            }
        });
        
        this.parent = parent;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        final int width = getWidth();
        final int height = getHeight();
        
        g2d.setPaint(getBackground());
        g2d.fillRect(0, 0, width, height);
        
        g2d.setPaint(Color.GRAY);
        for (int x = 0; x < width; x += 10) {
            for (int y = 0; y < height; y += 10) {
                g2d.fillRect(x, y, 5, 5);
            }
        }
        for (int x = 5; x < width; x += 10) {
            for (int y = 5; y < height; y += 10) {
                g2d.fillRect(x, y, 5, 5);
            }
        }
        
        TransferFunction transferFunction = parent.getTransferFunction();
        g2d.setPaint(transferFunction.getPaint(width));
        g2d.fillRect(0, 0, width, height);
        
        g2d.setColor(Color.BLACK);
        
        for (int i = 0; i < transferFunction.getPointCount(); i++) {
            int x = (int) (transferFunction.getPoint(i).getPoint() * width);
            g2d.drawLine(x, 0, x, height);
        }
    }
    
}
