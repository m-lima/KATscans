package com.mflima.katscans.project.displayable;

import java.io.Serializable;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import com.mflima.katscans.model.Rotation;

/**
 *
 * @author Marcelo Lima
 */
public class RotationNode extends SubGroup implements Serializable {
    
    private final Rotation rotation;

    public RotationNode() {
        super("Rotation");
        rotation = new Rotation();
    }
    
    @Override
    protected RotationNode internalCopy() {
        RotationNode newNode = new RotationNode();
        newNode.rotation.assimilate(rotation);
        return newNode;
    }

    @Override
    public Rotation getRotation() {
        return rotation;
    }
    
    @Override
    public ImageIcon getIcon() {
        return new ImageIcon(getClass().getResource("/icons/tree/rotation.png"));
    }

    @Override
    protected JMenuItem[] getExtraMenus() {
        return null;
    }

}
