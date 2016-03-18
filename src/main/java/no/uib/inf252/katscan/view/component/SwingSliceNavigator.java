package no.uib.inf252.katscan.view.component;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JPanel;
import no.uib.inf252.katscan.data.VoxelMatrix;

/**
 *
 * @author Marcelo Lima
 */
public class SwingSliceNavigator extends JPanel implements MouseWheelListener {

    private static final double FACTOR = 256d / 4095d;

    private VoxelMatrix matrix;
    private int slice = 0;

    public SwingSliceNavigator() {
        addMouseWheelListener(this);
    }

    public VoxelMatrix getMatrix() {
        return matrix;
    }

    public void setMatrix(VoxelMatrix matrix) {
        this.matrix = matrix;
        slice = matrix.getLength(VoxelMatrix.Axis.Z) / 2;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        slice += e.getWheelRotation();
        if (slice < 0) {
            slice = 0;
        } else {
            if (slice >= matrix.getLength(VoxelMatrix.Axis.Z)) {
                slice = matrix.getLength(VoxelMatrix.Axis.Z) - 1;
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Rectangle bounds = getBounds();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, bounds.width, bounds.height);
        
        int width = bounds.width / matrix.getLength(VoxelMatrix.Axis.X);
        if (width < 1) {
            width = 1;
        }
        int height = bounds.height / matrix.getLength(VoxelMatrix.Axis.Y);
        if (height < 1) {
            height = 1;
        }
        width = 1;
        height = 1;

        short[] column;
        short s;
        int value;
        for (int row = 0; row < matrix.getLength(VoxelMatrix.Axis.Y); row++) {
            column = matrix.getRow(slice, row);

            for (int col = 0; col < column.length; col++) {
                s = column[col];
                value = (int) (s * FACTOR);
                if (value > 255) {
                    value = 255;
                } else if (value < 0) {
                    value = 0;
                }
                g.setColor(new Color(value, value, value));
                g.fillRect(col * width, row * height, width, height);
            }
        }
    }
}
