package com.mflima.katscans.view.katview.opengl;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLException;
import com.mflima.katscans.project.displayable.Displayable;

/** @author Marcelo Lima */
public class MaximumRenderer extends VolumeRenderer {

  public MaximumRenderer(Displayable displayable) throws GLException {
    super(displayable, "maxCaster", 0.5f);
  }

  @Override
  public boolean isUnlit() {
    return true;
  }

  @Override
  protected void preDraw(GLAutoDrawable drawable) {}
}
