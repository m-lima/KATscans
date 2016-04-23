package no.uib.inf252.katscan.view.opengl;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLException;
import no.uib.inf252.katscan.model.displayable.Displayable;

/**
 *
 * @author Marcelo Lima
 */
public class MaximumRenderer extends VolumeRenderer {
    
    public MaximumRenderer(Displayable displayable) throws GLException {
        super(displayable, "maxRaycaster");
    }

    @Override
    protected void preDraw(GLAutoDrawable drawable) {}
    
}