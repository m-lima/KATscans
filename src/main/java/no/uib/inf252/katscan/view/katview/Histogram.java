package no.uib.inf252.katscan.view.katview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.UIManager;
import no.uib.inf252.katscan.data.VoxelMatrix;
import no.uib.inf252.katscan.project.displayable.Displayable;
import no.uib.inf252.katscan.view.MainFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.RangeType;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Marcelo Lima
 */
public class Histogram extends JPanel implements KatView {

    protected final XYPlot plot;
    protected final NumberAxis valueAxis;
    protected final NumberAxis domainAxis;
    protected final Displayable displayable;
    protected final JFreeChart chart;
    protected final ChartPanel chartPanel;
    protected final XYBarRenderer barRenderer;

    public Histogram(Displayable displayable) {
        super(new BorderLayout());
        
        if (displayable == null) {
            throw new NullPointerException();
        }
        this.displayable = displayable;
        
        setBackground(MainFrame.THEME_COLOR);

        valueAxis = new NumberAxis();       
        domainAxis = new NumberAxis();
        barRenderer = new XYBarRenderer();
        plot = new XYPlot();        
        chart = new JFreeChart(plot);
        chartPanel = new ChartPanel(chart);

        configureChart();
        
        add(chartPanel, BorderLayout.CENTER);
        
        initPlot();
    }
    
    protected void configureChart() {
        valueAxis.setTickLabelPaint(UIManager.getDefaults().getColor("Label.foreground"));
        
        domainAxis.setTickLabelPaint(UIManager.getDefaults().getColor("Label.foreground"));
        domainAxis.setRangeType(RangeType.POSITIVE);
        domainAxis.setFixedAutoRange(displayable.getMatrix().getMaxValue());
        
        barRenderer.setShadowVisible(false);
        barRenderer.setMargin(0);
        barRenderer.setBarPainter(new StandardXYBarPainter());
        barRenderer.setSeriesPaint(0, new Color(200, 80, 80));
        
        plot.setDomainAxis(domainAxis);
        plot.setBackgroundPaint(null);
        plot.setRangeAxis(0, valueAxis);
        plot.setRenderer(0, barRenderer);
        plot.mapDatasetToRangeAxis(0, 0);
        
        chart.setSubtitles(new ArrayList());
        chartPanel.setOpaque(false);
    }
    
    private void initPlot() {
        VoxelMatrix dataset = displayable.getMatrix();
        int[] histogram = dataset.getHistogram();
        
        XYSeries series = new XYSeries(displayable.getName());
        XYSeriesCollection collection = new XYSeriesCollection(series);
        
        plot.setDataset(0, collection);

        for (int i = 0; i < dataset.getMaxValue(); i++) {
            series.add(i, histogram[i]);
        }
    }
    
}
