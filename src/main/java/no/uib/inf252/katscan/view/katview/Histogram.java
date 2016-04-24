package no.uib.inf252.katscan.view.katview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.UIManager;
import no.uib.inf252.katscan.data.VoxelMatrix;
import no.uib.inf252.katscan.project.displayable.Displayable;
import no.uib.inf252.katscan.util.TransferFunction;
import no.uib.inf252.katscan.view.MainFrame;
import no.uib.inf252.katscan.view.transferfunction.TransferFunctionBarEditor;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
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

    private final XYPlot plot;
    private final NumberAxis valueAxis;
    private final NumberAxis domainAxis;
    private final Displayable displayable;
    
    private JPanel pnlHolder;
    private TransferFunctionBarEditor barEditor;
    private final ChartPanel chartPanel;

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
        
        JFreeChart chart = new JFreeChart(plot);
        chartPanel = new ChartPanel(chart);
        chart.setSubtitles(new ArrayList());
        chartPanel.setOpaque(false);

        add(chartPanel, BorderLayout.CENTER);
        initPlot();
        
        if (transferFunction != null) {
            barEditor = new TransferFunctionBarEditor(transferFunction);
            pnlHolder = new JPanel();
            pnlHolder.setPreferredSize(barEditor.getPreferredSize());
            pnlHolder.setLayout(null);
            pnlHolder.add(barEditor);
            add(pnlHolder, BorderLayout.SOUTH);
            
            chart.addProgressListener(new ChartProgressListener() {
                @Override
                public void chartProgress(ChartProgressEvent event) {
                    if (event.getPercent() == 100) {
                        resizeTransferFunctionBar();
                    }
                }
            });
        }

        validate();
    }
    
    private void resizeTransferFunctionBar() {
        Rectangle2D dataArea = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();

        int x = (int) domainAxis.valueToJava2D(
                domainAxis.getRange().getLowerBound(),
                dataArea,
                plot.getDomainAxisEdge());

        int width = (int) domainAxis.valueToJava2D(
                domainAxis.getRange().getUpperBound(),
                dataArea,
                plot.getDomainAxisEdge());

        x *= chartPanel.getScaleX();
        width *= chartPanel.getScaleX();
        width -= x;

        barEditor.setRange(domainAxis.getRange().getLowerBound(), domainAxis.getRange().getUpperBound());
        //TODO Dont like this
        barEditor.setBounds(x - TransferFunctionBarEditor.COLOR_SIZE_HALF, 0, width + TransferFunctionBarEditor.COLOR_SIZE, pnlHolder.getHeight());

        pnlHolder.validate();
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
        
        domainAxis.setFixedAutoRange(dataset.getMaxValue());
    }
    
}
