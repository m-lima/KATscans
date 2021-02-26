package com.mflima.katscans.view.transferfunction;

import com.mflima.katscans.Init;
import com.mflima.katscans.event.TransferFunctionListener;
import com.mflima.katscans.model.TransferFunction;
import com.mflima.katscans.model.TransferFunction.TransferFunctionPoint;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** @author Marcelo Lima */
public class TransferFunctionChartEditor extends JPanel implements TransferFunctionListener {

  public static final int MARKER_SIZE = 10;
  public static final int MARKER_SIZE_HALF = 5;

  private final TransferFunction transferFunction;

  private double minRange;
  private double maxRange;
  // private double gapRange;
  private double ratio;

  private static final class PointPair {
    final int x;
    final int y;

    private PointPair(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }

  public TransferFunctionChartEditor(TransferFunction transferFunction) {
    super(null);
    this.transferFunction = transferFunction;

    setOpaque(false);

    minRange = 0d;
    maxRange = 1d;
    // gapRange = 0d;
    ratio = 1d;

    buildMarkers();
  }

  private void buildMarkers() {
    removeAll();
    transferFunction
        .getPoints()
        .forEach(
            point -> {
              Marker marker = new Marker(point);
              add(marker);
              marker.setSize(MARKER_SIZE, MARKER_SIZE);
            });
    updateMarkersPositions();
  }

  private void updateMarkersPositions() {
    Component[] markers = getComponents();
    for (Component marker : markers) {
      if (marker instanceof Marker) {
        ((Marker) marker).updatePosition();
      }
    }
    repaint();
  }

  public void setRange(double lower, double upper) {
    minRange = lower;
    maxRange = upper;
    ratio = 1d / (maxRange - minRange);
    updateMarkersPositions();
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    final int width = getWidth();

    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setPaint(
        transferFunction.getPaint(
            (float) (-minRange * ratio * width), (float) ((1d - minRange) * ratio * width)));

    // Rust-style stream
    //noinspection ResultOfMethodCallIgnored
    transferFunction
        .getPoints()
        .map(
            point -> {
              double x = (point.getPoint() - minRange) * ratio;
              x *= getWidth();

              double y = 1d - point.getAlpha();
              y *= getHeight();
              return new Point((int) x, (int) y);
            })
        .sequential()
        .reduce(
            null,
            (last, point) -> {
              if (last != null) {
                g2d.drawLine(last.x, last.y, point.x, point.y);
              }
              return new Point(point.x, point.y);
            });
  }

  @Override
  public void pointCountChanged() {
    buildMarkers();
  }

  @Override
  public void pointValueChanged() {
    updateMarkersPositions();
  }

  private class Marker extends JComponent {

    private final TransferFunctionPoint point;

    private Marker(TransferFunctionPoint newPoint) {
      this.point = newPoint;

      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

      addMouseListener(
          new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              if (SwingUtilities.isRightMouseButton(e)) {
                Color newColor =
                    JColorChooser.showDialog(Init.getFrameReference(), null, point.getColor());
                if (newColor != null) {
                  point.setColor(
                      new Color(
                          newColor.getRed(),
                          newColor.getGreen(),
                          newColor.getBlue(),
                          point.getColor().getAlpha()));
                }
              } else if (SwingUtilities.isMiddleMouseButton(e)) {
                if (point.isMovable()) {
                  transferFunction.removePoint(point);
                }
              }
            }
          });

      addMouseMotionListener(
          new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
              final Container parent = getParent();

              Point mousePoint = SwingUtilities.convertPoint(Marker.this, e.getPoint(), parent);

              double alpha = 1d - mousePoint.y / (double) parent.getHeight();
              point.setAlpha(alpha);

              if (point.isMovable()) {
                double newPoint = mousePoint.x / (double) parent.getWidth();
                newPoint /= ratio;
                newPoint += minRange;

                if (newPoint < 0f + TransferFunction.MIN_STEP) {
                  newPoint = 0f + TransferFunction.MIN_STEP;
                } else if (newPoint > 1f - TransferFunction.MIN_STEP) {
                  newPoint = 1f - TransferFunction.MIN_STEP;
                }

                point.setPoint((float) newPoint);
                updatePosition();
              }
            }
          });
    }

    public void updatePosition() {
      int parentWidth = getParent().getWidth();
      int parentHeight = getParent().getHeight();

      double x = (point.getPoint() - minRange) * ratio;
      x *= parentWidth;
      x -= MARKER_SIZE_HALF;

      double y = 1d - point.getAlpha();
      y *= parentHeight;
      y -= MARKER_SIZE_HALF;

      setVisible(x >= -MARKER_SIZE && x < parentWidth);
      setBounds((int) x, (int) y, MARKER_SIZE, MARKER_SIZE);
    }

    @Override
    protected void paintComponent(Graphics g) {
      int radius = Math.min(getWidth(), getHeight());
      int radius2 = radius >> 1;
      int iniX = (getWidth() >> 1) - radius2;
      int iniY = (getHeight() >> 1) - radius2;

      ((Graphics2D) g)
          .setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setColor(Color.BLACK);
      g.fillOval(iniX, iniY, radius, radius);
      g.setColor(point.getColor());
      g.fillOval(iniX + 1, iniY + 1, radius - 2, radius - 2);
    }
  }
}
