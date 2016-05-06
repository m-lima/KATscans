package no.uib.inf252.katscan.view.katview.opengl;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLException;
import no.uib.inf252.katscan.project.displayable.Displayable;

/**
 *
 * @author Marcelo Lima
 */
public class MaximumRenderer extends VolumeRenderer {
    
    public MaximumRenderer(Displayable displayable) throws GLException {
        super(displayable, "maxCaster", 0.5f);
    }

    @Override
    public boolean isIlluminated() {
        return false;
    }

    @Override
    protected void preDraw(GLAutoDrawable drawable) {}
    
}