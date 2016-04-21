package no.uib.inf252.katscan.view.opengl;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLException;

/**
 *
 * @author Marcelo Lima
 */
public class MaximumRenderer extends VolumeRenderer {
    
    public MaximumRenderer(String name) throws GLException {
        super(name, "maxRaycaster");
    }

    @Override
    protected void preDraw(GLAutoDrawable drawable) {}
    
}