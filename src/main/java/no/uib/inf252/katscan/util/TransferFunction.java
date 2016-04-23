package no.uib.inf252.katscan.util;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.event.EventListenerList;
import no.uib.inf252.katscan.event.TransferFunctionListener;

/**
 *
 * @author Marcelo Lima
 */
public class TransferFunction {

    private final ArrayList<TransferFunctionPoint> points;
    private final EventListenerList listenerList;
        
    private boolean dirtyPaint;
    private Color[] colors;
    private float[] colorPoints;

    public TransferFunction() {
        points = new ArrayList<>();
        points.add(new TransferFunctionPoint(new TransferFunctionColor(new Color(0, true)), 0f, this, false));
        points.add(new TransferFunctionPoint(new TransferFunctionColor(new Color(255, 255, 255, 255)), 1f, this, false));
        
        listenerList = new EventListenerList();
        dirtyPaint = true;
    }

    public int getPointCount() {
        return points.size();
    }

    public boolean addPoint(Color color, float point) {
        TransferFunctionPoint newPoint = new TransferFunctionPoint(new TransferFunctionColor(color), point, this);
        final boolean returnValue = points.add(newPoint);
        dirtyPaint = true;
        
        firePointCountChanged();
        return returnValue;
    }

    public TransferFunctionPoint getPoint(int index) {
        return points.get(index);
    }

    public TransferFunctionPoint removePoint(int index) {
        final TransferFunctionPoint point = points.remove(index);
        if (point != null) {
            dirtyPaint = true;
            firePointCountChanged();
        }        
        return point;
    }

    private void valueChanged() {
        dirtyPaint = true;
        firePointValueChanged();
    }

    public LinearGradientPaint getPaint(int width) {
        if (dirtyPaint) {
            rebuildPaint();
        }
        
        return new LinearGradientPaint(0f, 0f, width, 0f, colorPoints, colors, MultipleGradientPaint.CycleMethod.NO_CYCLE);
    }
    
    private void rebuildPaint() {
        Collections.sort(points);
        
        colors = new Color[points.size()];
        colorPoints = new float[points.size()];
        int index = 0;        
        for (TransferFunctionPoint point : points) {
            colors[index] = point.getColor().getWrappedColor();
            colorPoints[index] = point.getPoint();
            index++;
        }
    }

    private void firePointCountChanged() {
        TransferFunctionListener[] listeners = listenerList.getListeners(TransferFunctionListener.class);

        for (final TransferFunctionListener listener : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.pointCountChanged();
                }
            });
        }
    }

    private void firePointValueChanged() {
        TransferFunctionListener[] listeners = listenerList.getListeners(TransferFunctionListener.class);

        for (final TransferFunctionListener listener : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.pointValueChanged();
                }
            });
        }
    }
    
    public synchronized void addTransferFunctionListener(TransferFunctionListener listener) {
        if (listener == null) {
            return;
        }
        
        listenerList.add(TransferFunctionListener.class, listener);
    }
    
    public synchronized void removeTransferFunctionListener(TransferFunctionListener listener) {
        if (listener == null) {
            return;
        }
        
        listenerList.remove(TransferFunctionListener.class, listener);
    }

    public class TransferFunctionPoint implements Comparable<TransferFunctionPoint>, Serializable {

        private TransferFunctionColor color;
        private float point;
        private TransferFunction owner;
        private boolean movable;

        private TransferFunctionPoint(TransferFunctionColor color, float point, TransferFunction owner) {
            this(color, point, owner, true);
        }
        
        private TransferFunctionPoint(TransferFunctionColor color, float point, TransferFunction owner, boolean movable) {
            if (point < 0f || point > 1.0f) {
                throw new IndexOutOfBoundsException("Cannot create a transfer function point at "
                        + point);
            }
            this.color = color;
            this.point = point;
            this.owner = owner;
            this.movable = movable;
        }

        public TransferFunctionColor getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = new TransferFunctionColor(color);
            if (owner != null) {
                owner.valueChanged();
            }
        }

        public float getPoint() {
            return point;
        }

        public void setPoint(float point) {
            if (!movable) {
                return;
            }
            
            if (point < 0f || point > 1.0f) {
                throw new IndexOutOfBoundsException("Cannot create a transfer function point at "
                        + point);
            }
            this.point = point;
            if (owner != null) {
                owner.valueChanged();
            }
        }

        public boolean isMovable() {
            return movable;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 67 * hash + Float.floatToIntBits(this.point);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TransferFunctionPoint other = (TransferFunctionPoint) obj;
            if (Float.floatToIntBits(this.point)
                    != Float.floatToIntBits(other.point)) {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(TransferFunctionPoint o) {
            return Double.compare(point, o.point);
        }

    }

    public class TransferFunctionColor extends Number implements Paint, Serializable {

        private Color color;

        private TransferFunctionColor(Color color) {
            this.color = color;
        }

        public Color getWrappedColor() {
            return color;
        }

        public Color getOpaqueWrappedColor() {
            return new Color(color.getRGB() & 0xFFFFFF);
        }

        @Override
        public int intValue() {
            return 0;
        }

        @Override
        public long longValue() {
            return 0;
        }

        @Override
        public float floatValue() {
            return color.getAlpha() / 255f;
        }

        @Override
        public double doubleValue() {
            return color.getAlpha() / 255d;
        }

        @Override
        public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
            return color.createContext(cm, deviceBounds, userBounds, xform, hints);
        }

        @Override
        public int getTransparency() {
            return color.getTransparency();
        }

    }

}
