package no.uib.inf252.katscan.util;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.event.EventListenerList;
import no.uib.inf252.katscan.event.TransferFunctionListener;

/**
 *
 * @author Marcelo Lima
 */
public class TransferFunction {

    public static final int TEXTURE_SIZE = 2048;
    public static final float MIN_STEP = 1f / TEXTURE_SIZE;

    private final ArrayList<TransferFunctionPoint> points;
    private final EventListenerList listenerList;

    private boolean dirtyPaint;
    private Color[] colors;
    private float[] colorPoints;

    public TransferFunction() {
        points = new ArrayList<>();
        points.add(new TransferFunctionPoint(new Color(0, 0, 0, 0), 0f, this, false));
        points.add(new TransferFunctionPoint(new Color(255, 255, 255, 255), 0.5f, this));
        points.add(new TransferFunctionPoint(new Color(0, 0, 0, 0), 1f, this, false));

        listenerList = new EventListenerList();
        dirtyPaint = true;
    }

    public int getPointCount() {
        return points.size();
    }

    public boolean addPoint(Color color, float point) {
        TransferFunctionPoint newPoint = new TransferFunctionPoint(color, point, this);
        final boolean returnValue = points.add(newPoint);
        dirtyPaint = true;

        firePointCountChanged();
        return returnValue;
    }

    public TransferFunctionPoint getPoint(int index) {
        return points.get(index);
    }

    public boolean removePoint(TransferFunctionPoint point) {
        boolean removed = points.remove(point);
        if (removed) {
            dirtyPaint = true;
            firePointCountChanged();
        }
        return removed;
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

    //TODO Proper gradient; see https://en.wikipedia.org/wiki/Lab_color_space
    public LinearGradientPaint getPaint(float startX, float endX) {
        if (dirtyPaint) {
            rebuildPaint();
        }

        return new LinearGradientPaint(startX, 0f, endX, 0f, colorPoints, colors, MultipleGradientPaint.CycleMethod.NO_CYCLE);
    }

    private void rebuildPaint() {
        Collections.sort(points);

        colors = new Color[points.size()];
        colorPoints = new float[points.size()];
        int index = 0;
        int removedCount = 0;
        for (TransferFunctionPoint point : points) {
            float pointValue = point.getPoint();
            
            if (index > 0 && index < colorPoints.length - 1) {
                if (pointValue <= colorPoints[index - 1]) {
                    pointValue = colorPoints[index - 1] + MIN_STEP;
                    if (pointValue >= 1f) {
                        removedCount++;
                        continue;
                    }
                }
            }
            colorPoints[index] = pointValue;
            colors[index] = point.getColor();
            index++;
        }

        if (removedCount > 0) {
            colors = Arrays.copyOf(colors, colors.length - removedCount);
            colorPoints = Arrays.copyOf(colorPoints, colorPoints.length - removedCount);
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

        private Color color;
        private float point;
        private TransferFunction owner;
        private boolean movable;

        private TransferFunctionPoint(Color color, float point, TransferFunction owner) {
            this(color, point, owner, true);
        }

        private TransferFunctionPoint(Color color, float point, TransferFunction owner, boolean movable) {
            if (owner == null) {
                throw new NullPointerException("Cannot have a " + getClass().getName() + " without owner.");
            }
            
            if (point < 0f || point > 1.0f) {
                throw new IndexOutOfBoundsException("Cannot create a transfer function point at "
                        + point);
            }
            this.color = color;
            this.point = point;
            this.owner = owner;
            this.movable = movable;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
            owner.valueChanged();
        }
        
        public double getAlpha() {
            return color.getAlpha() / 255d;
        }
        
        public void setAlpha(double alpha) {
            if (alpha < 0d) {
                alpha = 0d;
            } else if (alpha > 1d) {
                alpha = 1d;
            }
            
            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255));
            owner.valueChanged();
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
            owner.valueChanged();
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
}
