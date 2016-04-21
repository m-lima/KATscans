package no.uib.inf252.katscan.view.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.UIManager;
import no.uib.inf252.katscan.data.LoadedDataHolder;
import no.uib.inf252.katscan.data.VoxelMatrix;
import no.uib.inf252.katscan.view.MainFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
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
    private final NumberAxis alphaAxis;
    private final String dataName;

    public Histogram(String dataName) {
        setOpaque(true);
        setBackground(MainFrame.THEME_COLOR);
        
        this.dataName = dataName;
        
        setLayout(new BorderLayout());
        
        valueAxis = new NumberAxis();
        valueAxis.setTickLabelPaint(UIManager.getDefaults().getColor("Label.foreground"));
        domainAxis = new NumberAxis();
        domainAxis.setTickLabelPaint(UIManager.getDefaults().getColor("Label.foreground"));
        alphaAxis = new NumberAxis();
        alphaAxis.setTickLabelPaint(UIManager.getDefaults().getColor("Label.foreground"));
        alphaAxis.setRange(0d, 1d);
        alphaAxis.setAutoRange(false);
        
        plot = new XYPlot();
        plot.setRangeAxis(valueAxis);
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(1, alphaAxis);
        plot.setBackgroundPaint(null);
        plot.setRangeAxisLocation(1, AxisLocation.TOP_OR_RIGHT);

        XYBarRenderer barRenderer = new XYBarRenderer();
        barRenderer.setShadowVisible(false);
        barRenderer.setMargin(0);
        barRenderer.setBarPainter(new StandardXYBarPainter());
        plot.setRenderer(0, barRenderer);
        
        JFreeChart chart = new JFreeChart(plot);
        ChartPanel chartPanel = new ChartPanel(chart);
        chart.setSubtitles(new ArrayList());
        chartPanel.setOpaque(false);
        
        add(chartPanel, BorderLayout.CENTER);
        
        validate();
        
        initPlot();
    }

    private void initPlot() {
        VoxelMatrix dataset = LoadedDataHolder.getInstance().getDataset(dataName);
        int[] histogram = dataset.getHistogram();
        
        XYSeries series = new XYSeries(dataName);
        XYSeriesCollection collection = new XYSeriesCollection(series);
        
        plot.setDataset(0, collection);
        
        for (int i = 2; i < dataset.getMaxValue(); i++) {
            series.add(i, histogram[i]);
        }
    }
    
}
