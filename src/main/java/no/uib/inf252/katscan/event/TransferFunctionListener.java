package no.uib.inf252.katscan.event;

/**
 *
 * @author Marcelo Lima
 */
public interface TransferFunctionListener extends KatModelListener {
    
    public void pointCountChanged();
    public void pointValueChanged();

}
