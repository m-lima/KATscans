package no.uib.inf252.katscan.view.katview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;
import no.uib.inf252.katscan.data.VoxelMatrix;
import no.uib.inf252.katscan.project.displayable.Displayable;
import no.uib.inf252.katscan.view.MainFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
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
public class Histogram extends JPanel implements KatView {

    protected final XYPlot plot;
    protected final LogAxis logAxis;
    protected final NumberAxis linAxis;
    protected final NumberAxis domainAxis;
    protected final Displayable displayable;
    protected final JFreeChart chart;
    protected final ChartPanel chartPanel;
    protected final XYBarRenderer barRenderer;
    protected final JMenuItem menuLog;
    
    private boolean log;

    public Histogram(Displayable displayable) {
        super(new BorderLayout());
        
        if (displayable == null) {
            throw new NullPointerException();
        }
        this.displayable = displayable;
        
        setBackground(MainFrame.THEME_COLOR);

        logAxis = new LogAxis();
        linAxis = new NumberAxis();
        domainAxis = new NumberAxis();
        barRenderer = new XYBarRenderer();
        plot = new XYPlot();        
        chart = new JFreeChart(plot);
        chartPanel = new ChartPanel(chart) {
            @Override
            public void restoreAutoBounds() {
                super.restoreAutoBounds();
                domainAxis.setRange(new Range(Histogram.this.displayable.getMatrix().getMinValue(), Histogram.this.displayable.getMatrix().getMaxValue()));
            }
        };
        
        menuLog = new JMenuItem("Linear");
        log = true;

        configureChart();
        
        add(chartPanel, BorderLayout.CENTER);
        
        initPlot();
    }
    
    protected void configureChart() {
        logAxis.setTickLabelPaint(UIManager.getDefaults().getColor("Label.foreground"));
        linAxis.setTickLabelPaint(UIManager.getDefaults().getColor("Label.foreground"));
        
        domainAxis.setTickLabelPaint(UIManager.getDefaults().getColor("Label.foreground"));
        domainAxis.setRange(new Range(displayable.getMatrix().getMinValue(), displayable.getMatrix().getMaxValue()));
        domainAxis.setRangeType(RangeType.POSITIVE);
        
        barRenderer.setShadowVisible(false);
        barRenderer.setMargin(0);
        barRenderer.setBarPainter(new StandardXYBarPainter());
        barRenderer.setSeriesPaint(0, new Color(200, 80, 80));
        
        plot.setDomainAxis(domainAxis);
        plot.setBackgroundPaint(null);
                
        chart.setSubtitles(new ArrayList());
        chartPanel.setOpaque(false);
        
        chartPanel.getPopupMenu().addSeparator();
        chartPanel.getPopupMenu().add(menuLog);
        
        menuLog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (log) {
                    menuLog.setText("Logarithmic");
                    plot.setRangeAxis(0, linAxis);                    
                } else {
                    menuLog.setText("Linear");
                    plot.setRangeAxis(0, logAxis);                    
                }
                log = !log;
            }
        });
    }
    
    private void initPlot() {
        VoxelMatrix dataset = displayable.getMatrix();
        int[] histogram = dataset.getHistogram();
        
        XYSeries series = new XYSeries(displayable.getName());
        XYSeriesCollection collection = new XYSeriesCollection(series);
        
        plot.setDataset(0, collection);

        int value;
        for (int i = 0; i < dataset.getMaxValue(); i++) {
            value = histogram[i];
            if (value > 0) {
                series.add(i, value);
            }
        }
        
        plot.setRangeAxis(0, logAxis);
        plot.setRenderer(0, barRenderer);
        plot.mapDatasetToRangeAxis(0, 0);
    }
    
}
