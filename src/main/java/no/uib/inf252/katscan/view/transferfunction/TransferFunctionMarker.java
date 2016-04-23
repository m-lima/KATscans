package no.uib.inf252.katscan.view.transferfunction;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import no.uib.inf252.katscan.Init;
import no.uib.inf252.katscan.util.TransferFunction.TransferFunctionPoint;

/**
 *
 * @author Marcelo Lima
 */
public class TransferFunctionMarker extends JComponent {

    private final TransferFunctionEditor parent;
    private TransferFunctionPoint point;

    public TransferFunctionMarker(TransferFunctionEditor parent, TransferFunctionPoint point) {
        this.parent = parent;
        this.point = point;

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    Color newColor = JColorChooser.showDialog(Init.getFrameReference(), null, TransferFunctionMarker.this.point.getColor().getWrappedColor());
                    TransferFunctionMarker.this.point.setColor(newColor);
                } else if (SwingUtilities.isMiddleMouseButton(e)) {
                    if (TransferFunctionMarker.this.point.isMovable()) {
                        TransferFunctionMarker.this.parent.getTransferFunction().removePoint(TransferFunctionMarker.this.point);
                    }
                }
            }
        });

        if (point.isMovable()) {
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    final Container parent = getParent();

                    Point mousePoint = SwingUtilities.convertPoint(TransferFunctionMarker.this, e.getPoint(), parent);
                    if (mousePoint.x > parent.getWidth() || mousePoint.x < 0) {
                        return;
                    }

                    float newValue = mousePoint.x / (float) parent.getWidth();
                    if (newValue < 0f + Float.MIN_NORMAL) {
                        newValue = 0f + Float.MIN_NORMAL;
                    } else {
                        if (newValue > 1f - Float.MIN_NORMAL) {
                            newValue = 1f - Float.MIN_NORMAL;
                        }
                    }

                    TransferFunctionMarker.this.point.setPoint(newValue);
                    updatePosition();
                }
            });
        }
    }

    public void updatePosition() {
        final Container parent = getParent();
        setBounds((int) (point.getPoint() * (parent.getWidth()
                - parent.getHeight())), 0, parent.getHeight(), parent.getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        int radius = Math.min(getWidth(), getHeight());
        int radius2 = radius >> 1;
        int iniX = (getWidth() >> 1) - radius2;
        int iniY = (getHeight() >> 1) - radius2;

        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(point.getColor().getOpaqueWrappedColor());
        g.fillOval(iniX, iniY, radius, radius);
    }

}
