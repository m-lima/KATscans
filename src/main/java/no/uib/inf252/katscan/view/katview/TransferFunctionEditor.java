package no.uib.inf252.katscan.view.katview;

import java.awt.BorderLayout;
import java.awt.geom.Rectangle2D;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import no.uib.inf252.katscan.project.displayable.Displayable;
import no.uib.inf252.katscan.util.TransferFunction;
import no.uib.inf252.katscan.view.component.FullLayout;
import no.uib.inf252.katscan.view.transferfunction.TransferFunctionBarEditor;
import no.uib.inf252.katscan.view.transferfunction.TransferFunctionChartEditor;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;

/**
 *
 * @author Marcelo Lima
 */
public class TransferFunctionEditor extends Histogram implements ChartProgressListener {

    private final JPanel pnlMain;
    
    private final JLayeredPane pnlChartHolder;
    private final JPanel pnlChartEditorHolder;
    private final TransferFunctionChartEditor chartEditor;
    
    private final JPanel pnlBarEditorHolder;
    private final TransferFunctionBarEditor barEditor;

    public TransferFunctionEditor(Displayable displayable, TransferFunction transferFunction) {
        super(displayable);
        
        if (transferFunction == null) {
            throw new NullPointerException();
        }
        
        chart.addProgressListener(this);
        remove(chartPanel);
        
        barEditor = new TransferFunctionBarEditor(transferFunction);
        chartEditor = new TransferFunctionChartEditor(transferFunction);
        
        pnlBarEditorHolder = new JPanel(null);
        pnlBarEditorHolder.setPreferredSize(barEditor.getPreferredSize());
        pnlBarEditorHolder.add(barEditor);
        
        pnlChartEditorHolder = new JPanel(null);
        pnlChartEditorHolder.setOpaque(false);
        pnlChartEditorHolder.add(chartEditor);
        
        pnlChartHolder = new JLayeredPane();
        pnlChartHolder.setLayout(new FullLayout());
        pnlChartHolder.add(chartPanel, JLayeredPane.DEFAULT_LAYER);
        pnlChartHolder.add(pnlChartEditorHolder, new Integer(JLayeredPane.DEFAULT_LAYER + 1));
        
        pnlMain = new JPanel(new BorderLayout());
        pnlMain.add(pnlChartHolder, BorderLayout.CENTER);
        pnlMain.add(pnlBarEditorHolder, BorderLayout.SOUTH);
        
        add(pnlMain, BorderLayout.CENTER);
        validate();
    }

    private void resizeTransferFunctionEditors() {
        Rectangle2D dataArea = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();

        int x = (int) domainAxis.valueToJava2D(
                domainAxis.getRange().getLowerBound(),
                dataArea,
                plot.getDomainAxisEdge());

        int y = (int) valueAxis.valueToJava2D(
                valueAxis.getRange().getUpperBound(),
                dataArea,
                plot.getRangeAxisEdge());

        int width = (int) domainAxis.valueToJava2D(
                domainAxis.getRange().getUpperBound(),
                dataArea,
                plot.getDomainAxisEdge());

        int height = (int) valueAxis.valueToJava2D(
                valueAxis.getRange().getLowerBound(),
                dataArea,
                plot.getRangeAxisEdge());

        x *= chartPanel.getScaleX();
        y *= chartPanel.getScaleY();
        
        width *= chartPanel.getScaleX();
        width -= x;
        height *= chartPanel.getScaleY();
        height -= y;

        barEditor.setBounds(x - TransferFunctionBarEditor.MARKER_SIZE_HALF, 0, width + TransferFunctionBarEditor.MARKER_SIZE, pnlBarEditorHolder.getHeight());
        chartEditor.setBounds(x, y, width, height);

        barEditor.setRange(domainAxis.getRange().getLowerBound(), domainAxis.getRange().getUpperBound());
        chartEditor.setRange(domainAxis.getRange().getLowerBound(), domainAxis.getRange().getUpperBound());
    }

    @Override
    public void chartProgress(ChartProgressEvent event) {
        if (event.getPercent() == 100) {
            resizeTransferFunctionEditors();
        }
    }

}
