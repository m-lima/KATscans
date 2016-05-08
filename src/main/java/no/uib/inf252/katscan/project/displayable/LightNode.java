package no.uib.inf252.katscan.project.displayable;

import java.io.Serializable;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import no.uib.inf252.katscan.model.Light;

/**
 *
 * @author Marcelo Lima
 */
public class LightNode extends SubGroup implements Serializable {
    
    private final Light light;

    public LightNode() {
        super("Camera");
        light = new Light();
    }
    
    @Override
    protected LightNode internalCopy() {
        LightNode newNode = new LightNode();
        newNode.light.assimilate(light);
        return newNode;
    }

    @Override
    public Light getLight() {
        return light;
    }
    
    @Override
    public ImageIcon getIcon() {
        return new ImageIcon(getClass().getResource("/icons/tree/light.png"));
    }

    @Override
    protected JMenuItem[] getExtraMenus() {
        return null;
    }

}
