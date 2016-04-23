package no.uib.inf252.katscan.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.UIManager;
import no.uib.inf252.katscan.data.VoxelMatrix;
import no.uib.inf252.katscan.model.displayable.Displayable;
import no.uib.inf252.katscan.util.TransferFunction;
import no.uib.inf252.katscan.util.TransferFunction.TransferFunctionPoint;
import no.uib.inf252.katscan.view.transferfunction.TransferFunctionEditor;
import no.uib.inf252.katscan.view.transferfunction.TransferFunctionRenderer;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.Range;
import org.jfree.data.RangeType;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Marcelo Lima
 */
public class Histogram extends JPanel {

    private final XYPlot plot;
    private final NumberAxis valueAxis;
    private final NumberAxis domainAxis;
//    private final NumberAxis alphaAxis;
    private final Displayable displayable;
//    private final TransferFunction transferFunction;

    public Histogram(Displayable displayable) {
        this(displayable, null);
    }

    public Histogram(Displayable displayable, TransferFunction transferFunction) {
        
        setOpaque(true);
        setBackground(MainFrame.THEME_COLOR);

        this.displayable = displayable;

        setLayout(new BorderLayout());

        valueAxis = new NumberAxis();
        valueAxis.setTickLabelPaint(UIManager.getDefaults().getColor("Label.foreground"));
        
        domainAxis = new NumberAxis();
        domainAxis.setTickLabelPaint(UIManager.getDefaults().getColor("Label.foreground"));
        domainAxis.setRangeType(RangeType.POSITIVE);

        XYBarRenderer barRenderer = new XYBarRenderer();
        barRenderer.setShadowVisible(false);
        barRenderer.setMargin(0);
        barRenderer.setBarPainter(new StandardXYBarPainter());
        barRenderer.setSeriesPaint(0, new Color(200, 80, 80));

        plot = new XYPlot();
        plot.setDomainAxis(domainAxis);
        plot.setBackgroundPaint(null);
        plot.setRangeAxis(0, valueAxis);
        plot.setRenderer(0, barRenderer);
        plot.mapDatasetToRangeAxis(0, 0);
        
//        if (transferFunction != null) {
//            alphaAxis = new NumberAxis();
//            alphaAxis.setTickLabelPaint(UIManager.getDefaults().getColor("Label.foreground"));
////            alphaAxis.setAutoRange(false);
////            alphaAxis.setRange(0d, 1d);
//            alphaAxis.setDefaultAutoRange(new Range(0d, 1d));
//            alphaAxis.setRangeType(RangeType.POSITIVE);
//            
//            TransferFunctionRenderer tranferFunctionRenderer = new TransferFunctionRenderer();
//            
//            plot.setRangeAxis(1, alphaAxis);
//            plot.setRenderer(1, tranferFunctionRenderer);
//            plot.mapDatasetToRangeAxis(1, 1);
//            plot.setRangeAxisLocation(1, AxisLocation.TOP_OR_RIGHT);
//        } else {
//            alphaAxis = null;
//        }

        JFreeChart chart = new JFreeChart(plot);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chart.setSubtitles(new ArrayList());
        chartPanel.setOpaque(false);
//        chartPanel.setRangeZoomable(false);

        add(chartPanel, BorderLayout.CENTER);

        validate();
        initPlot();
        
//        this.transferFunction = null;
        
        if (transferFunction != null) {
            transferFunction = new TransferFunction();
            final TransferFunctionEditor editor = new TransferFunctionEditor(transferFunction);
            add(editor, BorderLayout.SOUTH);
            
//            plot.addChangeListener(new PlotChangeListener() {
//                @Override
//                public void plotChanged(PlotChangeEvent event) {
//                    Rectangle2D dataArea = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
//                    Rectangle2D plotArea = chartPanel.getChartRenderingInfo().getPlotInfo().getPlotArea();
//                    Rectangle2D chartArea = chartPanel.getChartRenderingInfo().getChartArea();
//                    
//                    editor.setBounds((int) domainAxis.valueToJava2D(domainAxis.getRange().getLowerBound(), dataArea, plot.getDomainAxisEdge()), 0, (int) domainAxis.valueToJava2D(domainAxis.getRange().getUpperBound(), dataArea, plot.getDomainAxisEdge()), editor.getHeight());//(int)(dataArea.getX() * transferFunctionHolder.getWidth() / chartArea.getWidth()), 0, (int)(dataArea.getWidth()), transferFunctionHolder.getHeight());
//                    editor.validate();
//                }
//            });
        }
    }

    private void initPlot() {
        VoxelMatrix dataset = displayable.getMatrix();
        int[] histogram = dataset.getHistogram();
        
        XYSeries series = new XYSeries(displayable.getName());
        XYSeriesCollection collection = new XYSeriesCollection(series);
        
        plot.setDataset(0, collection);

        for (int i = 2; i < dataset.getMaxValue(); i++) {
            series.add(i, histogram[i]);
        }
        
//        if (transferFunction != null) {
//            series = new XYSeries("transferFunction");
//            collection = new XYSeriesCollection(series);
//            
//            plot.setDataset(1, collection);
//            
//            for (int i = 0; i < transferFunction.getPointCount(); i++) {
//                TransferFunctionPoint point = transferFunction.getPoint(i);
//                series.add(point.getPoint() * dataset.getMaxValue(), point.getColor());
//            }
//        }
        
        domainAxis.setDefaultAutoRange(new Range(0d, dataset.getMaxValue()));
    }

}
