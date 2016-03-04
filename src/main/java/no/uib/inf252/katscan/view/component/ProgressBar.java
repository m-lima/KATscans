package no.uib.inf252.katscan.view.component;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JLabel;

/**
 *
 * @author Marcelo Lima
 */
public class ProgressBar extends JLabel {

    private double value;
    private Color progressColor;
    private Color progressBorderColor;
    
    private int gapThickness;
    private int borderThickness;

    public ProgressBar() {
        progressColor = Color.getHSBColor(1f/3f, 1f/3f, 1f/3f);
        progressBorderColor = Color.getHSBColor(1f/3f, 1f/3f, 1f/3f);
        setGapThickness(1);
        setBorderThickness(1);
        setOpaque(false);
    }

    public int getGapThickness() {
        return gapThickness;
    }

    public void setGapThickness(int gapThickness) {
        if (gapThickness < 0) {
            this.gapThickness = 0;
        } else {
            this.gapThickness = gapThickness;
        }
    }

    public int getBorderThickness() {
        return borderThickness;
    }

    public void setBorderThickness(int borderThickness) {
        if (borderThickness < 0) {
            this.borderThickness = 0;
        } else {
            this.borderThickness = borderThickness;
        }
    }

    public Color getProgressColor() {
        return progressColor;
    }

    public void setProgressColor(Color progressColor) {
        this.progressColor = progressColor;
    }

    public Color getProgressBorderColor() {
        return progressBorderColor;
    }

    public void setProgressBorderColor(Color progressBorderColor) {
        this.progressBorderColor = progressBorderColor;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        if (value < 0) {
            this.value = 0d;
        } else if (value > 1) {
            this.value = 1d;
        } else {
            this.value = value;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (progressColor != null) {
            final Rectangle bounds = getBounds();
            final Insets insets = getInsets();
            
            if (progressBorderColor != null) {
                g.setColor(progressBorderColor);
                if (borderThickness == 1) {
                    g.drawRect(insets.left, insets.top, (bounds.width - 1)  - (insets.left + insets.right), (bounds.height - 1)  - (insets.top + insets.bottom));
                } else if (borderThickness > 1) {
                    g.fillRect(insets.left, insets.top, bounds.width - (insets.left + insets.right), borderThickness);
                    g.fillRect(insets.left, bounds.height - (insets.bottom + borderThickness), (bounds.width)  - (insets.left + insets.right), borderThickness);
                    g.fillRect(insets.left, insets.top + borderThickness, borderThickness, (bounds.height - (borderThickness * 2)) - (insets.top + insets.bottom));
                    g.fillRect(bounds.width - (insets.right + borderThickness), insets.top + borderThickness, borderThickness, (bounds.height - (borderThickness * 2)) - (insets.top + insets.bottom));
                }
            }
            
            g.setColor(progressColor);
            final int insetOffset = gapThickness + borderThickness;
            
            insets.left += insetOffset;
            insets.right += insetOffset;
            insets.top += insetOffset;
            insets.bottom += insetOffset;
            
            g.fillRect(insets.left, insets.top, (int) (value * bounds.width - (insets.left + insets.right)), bounds.height - (insets.top + insets.bottom));
        }
        
        super.paintComponent(g);
    }

}
