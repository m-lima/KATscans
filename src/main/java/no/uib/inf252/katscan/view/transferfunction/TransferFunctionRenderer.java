package no.uib.inf252.katscan.view.transferfunction;

import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import no.uib.inf252.katscan.util.TransferFunction.TransferFunctionColor;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.util.LineUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

/**
 *
 * @author Marcelo Lima
 */
public class TransferFunctionRenderer extends AbstractXYItemRenderer implements XYItemRenderer {
    
    private final BasicStroke lineStroke;

    public TransferFunctionRenderer() {
        lineStroke = new BasicStroke();
    }
    
    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item, CrosshairState crosshairState, int pass) {
        drawLine(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, series, item);
        drawNode(g2, dataArea, plot, domainAxis, rangeAxis, dataset, series, item);
    }

    private void drawNode(Graphics2D g2, Rectangle2D dataArea, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item) {
        double x = dataset.getXValue(series, item);
        double y = dataset.getYValue(series, item);
        if (Double.isNaN(y) || Double.isNaN(x)) {
            return;
        }

        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

        double transX = domainAxis.valueToJava2D(x, dataArea, xAxisLocation);
        double transY = rangeAxis.valueToJava2D(y, dataArea, yAxisLocation);
        
        if (Double.isNaN(transX) || Double.isNaN(transY)) {
            return;
        }
        
        TransferFunctionColor color = (TransferFunctionColor) dataset.getY(series, item);
        g2.setPaint(color.getOpaqueWrappedColor());
        
        PlotOrientation orientation = plot.getOrientation();
        if (orientation == PlotOrientation.HORIZONTAL) {
            g2.fillOval((int)(transY - 3d), (int)(transX - 3d), 7, 7);
        } else if (orientation == PlotOrientation.VERTICAL) {
            g2.fillOval((int)(transX - 3d), (int)(transY - 3d), 7, 7);
        }
    }
    
    private void drawLine(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item) {
        if (item == 0) {
            return;
        }

        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        if (Double.isNaN(y1) || Double.isNaN(x1)) {
            return;
        }

        double x0 = dataset.getXValue(series, item - 1);
        double y0 = dataset.getYValue(series, item - 1);
        if (Double.isNaN(y0) || Double.isNaN(x0)) {
            return;
        }

        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

        double transX0 = domainAxis.valueToJava2D(x0, dataArea, xAxisLocation);
        double transY0 = rangeAxis.valueToJava2D(y0, dataArea, yAxisLocation);

        double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);
        
        if (Double.isNaN(transX0) || Double.isNaN(transY0)
            || Double.isNaN(transX1) || Double.isNaN(transY1)) {
            return;
        }

        PlotOrientation orientation = plot.getOrientation();
        boolean visible;
        
        if (orientation == PlotOrientation.HORIZONTAL) {
            state.workingLine.setLine(transY0, transX0, transY1, transX1);
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            state.workingLine.setLine(transX0, transY0, transX1, transY1);
        }
        visible = LineUtilities.clipLine(state.workingLine, dataArea);
        
        TransferFunctionColor color0 = (TransferFunctionColor) dataset.getY(series, item - 1);
        TransferFunctionColor color1 = (TransferFunctionColor) dataset.getY(series, item);
        
        if (visible) {
            g2.setStroke(lineStroke);
            g2.setPaint(new GradientPaint(state.workingLine.getP1(), color0.getOpaqueWrappedColor(), state.workingLine.getP2(), color1.getOpaqueWrappedColor()));
            g2.draw(state.workingLine);
        }
    }
}
