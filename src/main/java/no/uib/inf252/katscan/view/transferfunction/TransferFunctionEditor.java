package no.uib.inf252.katscan.view.transferfunction;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JPanel;
import no.uib.inf252.katscan.event.TransferFunctionListener;
import no.uib.inf252.katscan.util.TransferFunction;
import no.uib.inf252.katscan.util.TransferFunction.TransferFunctionPoint;

/**
 *
 * @author Marcelo Lima
 */
public class TransferFunctionEditor extends JPanel implements TransferFunctionListener {

    public static final int COLOR_SIZE = 10;
    public static final int COLOR_SIZE_HALF = 5;
    
    private TransferFunction transferFunction;
    private TransferFunctionViewer pnlViewer;
    private JPanel pnlMarker;

    public TransferFunctionEditor() {
        this(new TransferFunction());
    }

    public TransferFunctionEditor(TransferFunction transferFunction) {
        this.transferFunction = new TransferFunction();
        this.transferFunction.addTransferFunctionListener(this);
        
        Dimension dimension = new Dimension(32, 32);

        setOpaque(true);
        setLayout(new BorderLayout());
        setMinimumSize(dimension);
        setPreferredSize(dimension);

        pnlMarker = new JPanel();
        pnlMarker.setOpaque(false);
        pnlMarker.setPreferredSize(new Dimension(COLOR_SIZE, COLOR_SIZE));
        pnlMarker.setLayout(null);
        add(pnlMarker, BorderLayout.NORTH);

        JPanel pnlViewerHolder = new JPanel();
        pnlViewerHolder.setOpaque(false);
        pnlViewerHolder.setLayout(new BorderLayout());
        add(pnlViewerHolder, BorderLayout.CENTER);
        
        pnlViewer = new TransferFunctionViewer(this);
        pnlViewerHolder.add(pnlViewer, BorderLayout.CENTER);
        
        JPanel pnlGap = new JPanel();
        pnlGap.setOpaque(false);
        pnlGap.setPreferredSize(new Dimension(COLOR_SIZE_HALF, COLOR_SIZE_HALF));
        pnlViewerHolder.add(pnlGap, BorderLayout.EAST);
        pnlGap = new JPanel();
        pnlGap.setOpaque(false);
        pnlGap.setPreferredSize(new Dimension(COLOR_SIZE_HALF, COLOR_SIZE_HALF));
        pnlViewerHolder.add(pnlGap, BorderLayout.WEST);

        buildMarkers();
        
        pnlMarker.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateMarkersPositions();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                updateMarkersPositions();
            }

        });
    }

    private void buildMarkers() {
        pnlMarker.removeAll();
        TransferFunctionMarker marker;
        for (int i = 0; i < transferFunction.getPointCount(); i++) {
            TransferFunctionPoint point = transferFunction.getPoint(i);
            marker = new TransferFunctionMarker(this, point);
            pnlMarker.add(marker);
            marker.setSize(pnlMarker.getHeight(), pnlMarker.getHeight());
        }
        updateMarkersPositions();
    }

    private void updateMarkersPositions() {
        Component[] markers = pnlMarker.getComponents();
        for (Component marker : markers) {
            if (marker instanceof TransferFunctionMarker) {
                ((TransferFunctionMarker) marker).updatePosition();
            }
        }
        validate();
        repaint();
    }

    public TransferFunction getTransferFunction() {
        return transferFunction;
    }

    public void setTransferFunction(TransferFunction transferFunction) {
        if (transferFunction != this.transferFunction) {
            this.transferFunction.removeTransferFunctionListener(this);
            this.transferFunction = transferFunction;
            this.transferFunction.addTransferFunctionListener(this);
        }
    }

    @Override
    public void pointCountChanged() {
        buildMarkers();
    }

    @Override
    public void pointValueChanged() {
        repaint();
    }

}
