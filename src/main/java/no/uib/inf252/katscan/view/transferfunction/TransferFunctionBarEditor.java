package no.uib.inf252.katscan.view.transferfunction;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import no.uib.inf252.katscan.Init;
import no.uib.inf252.katscan.event.TransferFunctionListener;
import no.uib.inf252.katscan.util.TransferFunction;
import no.uib.inf252.katscan.util.TransferFunction.TransferFunctionPoint;

/**
 *
 * @author Marcelo Lima
 */
public class TransferFunctionBarEditor extends JPanel implements TransferFunctionListener {

    public static final int COLOR_SIZE = 10;
    public static final int COLOR_SIZE_HALF = 5;

    private final TransferFunction transferFunction;
    private final TransferFunctionViewer pnlViewer;
    private final JPanel pnlMarker;
    private final double maxValue;
    
    private double minRange;
    private double maxRange;
    private double ratio;

    public TransferFunctionBarEditor(TransferFunction transferFunction) {
        this.transferFunction = transferFunction;
        this.transferFunction.addTransferFunctionListener(this);
        
        this.maxValue = transferFunction.getMaxValue();
        minRange = 0d;
        maxRange = this.maxValue;
        ratio = 1d;
        
        Dimension dimension = new Dimension(32, 32);

        setOpaque(true);
        setLayout(new BorderLayout());
        setMinimumSize(dimension);
        setPreferredSize(dimension);

        pnlMarker = new JPanel();
        pnlMarker.setOpaque(false);
        pnlMarker.setPreferredSize(new Dimension(COLOR_SIZE, COLOR_SIZE));
        pnlMarker.setLayout(null);
        add(pnlMarker, BorderLayout.NORTH);

        JPanel pnlViewerHolder = new JPanel();
        pnlViewerHolder.setOpaque(false);
        pnlViewerHolder.setLayout(new BorderLayout());
        add(pnlViewerHolder, BorderLayout.CENTER);

        pnlViewer = new TransferFunctionViewer();
        pnlViewerHolder.add(pnlViewer, BorderLayout.CENTER);

        JPanel pnlGap = new JPanel();
        pnlGap.setOpaque(false);
        pnlGap.setPreferredSize(new Dimension(COLOR_SIZE_HALF, COLOR_SIZE_HALF));
        pnlViewerHolder.add(pnlGap, BorderLayout.EAST);
        pnlGap = new JPanel();
        pnlGap.setOpaque(false);
        pnlGap.setPreferredSize(new Dimension(COLOR_SIZE_HALF, COLOR_SIZE_HALF));
        pnlViewerHolder.add(pnlGap, BorderLayout.WEST);

        buildMarkers();

        pnlMarker.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateMarkersPositions();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                updateMarkersPositions();
            }

        });
    }

    private void buildMarkers() {
        pnlMarker.removeAll();
        TransferFunctionMarker marker;
        for (int i = 0; i < transferFunction.getPointCount(); i++) {
            TransferFunctionPoint point = transferFunction.getPoint(i);
            marker = new TransferFunctionMarker(point);
            pnlMarker.add(marker);
            marker.setSize(pnlMarker.getHeight(), pnlMarker.getHeight());
        }
        updateMarkersPositions();
    }

    private void updateMarkersPositions() {
        Component[] markers = pnlMarker.getComponents();
        for (Component marker : markers) {
            if (marker instanceof TransferFunctionMarker) {
                ((TransferFunctionMarker) marker).updatePosition();
            }
        }
        validate();
        repaint();
    }
    
    public void setRange(double lower, double upper) {
        minRange = lower / maxValue;
        maxRange = upper / maxValue;
        ratio = 1d / (maxRange - minRange);
        updateMarkersPositions();
    }

    @Override
    public void pointCountChanged() {
        buildMarkers();
    }

    @Override
    public void pointValueChanged() {
        repaint();
    }

    private class TransferFunctionViewer extends JComponent {

        private TransferFunctionViewer() {
            setBorder(new EtchedBorder());
            setOpaque(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        double point = e.getX() / (double) getWidth();
                        point /= ratio;
                        point += minRange;

                        BufferedImage image = new BufferedImage(getWidth(), 1, BufferedImage.TYPE_4BYTE_ABGR);
                        Graphics g = image.getGraphics();
                        paintComponent(g, true);
                        transferFunction.addPoint(new Color(image.getRGB(e.getX(), 0), true), (float) point);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            paintComponent(g, false);
        }
        
        private void paintComponent(Graphics g, boolean onlyGradient) {
            Graphics2D g2d = (Graphics2D) g;
            final int width = getWidth();
            final int height = getHeight();

            if (!onlyGradient) {
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
            }

            g2d.setPaint(transferFunction.getPaint((float) (-minRange * ratio * width), (float) ((1d - minRange) * ratio * width)));
            g2d.fillRect(0, 0, width, height);

            if (!onlyGradient) {
                g2d.setColor(Color.BLACK);

                for (int i = 0; i < transferFunction.getPointCount(); i++) {
                    double x = (transferFunction.getPoint(i).getPoint() - minRange) * ratio;
                    x *= width;
                    g2d.drawLine((int) x, 0, (int) x, height);
                }
            }
        }

    }

    private class TransferFunctionMarker extends JComponent {

        private TransferFunctionPoint point;

        private TransferFunctionMarker(TransferFunctionPoint newPoint) {
            this.point = newPoint;

            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        Color newColor = JColorChooser.showDialog(Init.getFrameReference(), null, point.getColor().getWrappedColor());
                        if (newColor != null) {
                            point.setColor(newColor);
                        }
                    } else {
                        if (SwingUtilities.isMiddleMouseButton(e)) {
                            if (point.isMovable()) {
                                transferFunction.removePoint(point);
                            }
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

                        double newValue = mousePoint.x / (double) parent.getWidth();
                        newValue /= ratio;
                        newValue += minRange;

                        if (newValue < 0f + TransferFunction.MIN_STEP) {
                            newValue = 0f + TransferFunction.MIN_STEP;
                        } else {
                            if (newValue > 1f - TransferFunction.MIN_STEP) {
                                newValue = 1f - TransferFunction.MIN_STEP;
                            }
                        }

                        point.setPoint((float) newValue);
                        updatePosition();
                    }
                });
            }
        }

        public void updatePosition() {
            int parentWidth = getParent().getWidth();
            double x = (point.getPoint() - minRange) * ratio;
            x *= (parentWidth - COLOR_SIZE);
            
            setVisible(x >= 0d && x < parentWidth);
            setBounds((int) x, 0, COLOR_SIZE, COLOR_SIZE);
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

}
