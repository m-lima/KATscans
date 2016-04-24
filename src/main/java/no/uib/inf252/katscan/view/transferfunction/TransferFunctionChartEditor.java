package no.uib.inf252.katscan.view.transferfunction;

import java.awt.Rectangle;
import javax.swing.JPanel;
import no.uib.inf252.katscan.util.TransferFunction;

/**
 *
 * @author Marcelo Lima
 */
public class TransferFunctionChartEditor extends JPanel {

    private final TransferFunction transferFunction;
    private Rectangle chartArea;

    public TransferFunctionChartEditor(TransferFunction transferFunction) {
        this.transferFunction = transferFunction;
        chartArea = new Rectangle();
        
        setOpaque(false);
    }
    
    public void resizeChartArea() {
        
    }

}
