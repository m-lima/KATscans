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

    private TransferFunction transferFunction;
    private TransferFunctionViewer pnlViewer;
    private JPanel pnlMarker;

    public TransferFunctionEditor() {
        this(new TransferFunction());
    }

    public TransferFunctionEditor(TransferFunction transferFunction) {
        this.transferFunction = new TransferFunction();
        this.transferFunction.addTransferFunctionListener(this);

        setOpaque(true);
        setLayout(new BorderLayout());
        Dimension dimension = new Dimension(32, 32);
        setMinimumSize(dimension);
        setPreferredSize(dimension);

        pnlMarker = new JPanel();
        pnlMarker.setOpaque(false);
        pnlMarker.setPreferredSize(new Dimension(10, 10));
        pnlMarker.setLayout(null);

        pnlViewer = new TransferFunctionViewer(this);

        add(pnlViewer, BorderLayout.CENTER);
        add(pnlMarker, BorderLayout.NORTH);

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
            marker = new TransferFunctionMarker(point);
            pnlMarker.add(marker);
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
