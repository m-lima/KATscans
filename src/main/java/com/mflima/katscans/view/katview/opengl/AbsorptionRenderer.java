package com.mflima.katscans.view.katview.opengl;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import com.mflima.katscans.event.TransferFunctionListener;
import com.mflima.katscans.project.displayable.Displayable;
import com.mflima.katscans.model.TransferFunction;

/** @author Marcelo Lima */
public class AbsorptionRenderer extends VolumeRenderer implements TransferFunctionListener {

  private static final int TEXTURE_TRANSFER_LOCAL = 0;
  private static final int TEXTURE_TRANSFER = TEXTURE_COUNT_PARENT + TEXTURE_TRANSFER_LOCAL;

  private final int[] textureLocation = new int[1];
  private boolean transferFunctionDirty;

  public AbsorptionRenderer(Displayable displayable) throws GLException {
    super(displayable, "absoCaster", 0.5f);
  }

  @Override
  public boolean isIlluminated() {
    return false;
  }

  @Override
  protected void preDraw(GLAutoDrawable drawable) {
    if (transferFunctionDirty) {
      updateTransferFunction(drawable.getGL().getGL4());
      transferFunctionDirty = false;
    }
  }

  @Override
  public void init(GLAutoDrawable drawable) {
    super.init(drawable);

    GL4 gl = drawable.getGL().getGL4();

    gl.glGenTextures(1, textureLocation, TEXTURE_TRANSFER_LOCAL);
    gl.glActiveTexture(GL4.GL_TEXTURE0 + TEXTURE_TRANSFER);
    gl.glBindTexture(GL4.GL_TEXTURE_1D, textureLocation[TEXTURE_TRANSFER_LOCAL]);
    gl.glTexParameteri(GL4.GL_TEXTURE_1D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
    gl.glTexParameteri(GL4.GL_TEXTURE_1D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
    gl.glTexParameteri(GL4.GL_TEXTURE_1D, GL4.GL_TEXTURE_WRAP_R, GL4.GL_CLAMP_TO_BORDER);
    transferFunctionDirty = true;

    int location = gl.glGetUniformLocation(mainProgram, "transferFunction");
    gl.glUniform1i(location, TEXTURE_TRANSFER);

    checkError(gl, "Create transfer function");
  }

  @Override
  public void dispose(GLAutoDrawable drawable) {
    super.dispose(drawable);
    GL4 gl = drawable.getGL().getGL4();

    gl.glDeleteTextures(textureLocation.length, textureLocation, 0);
    checkError(gl, "Dispose Composite Renderer");
  }

  private void updateTransferFunction(GL4 gl) {
    BufferedImage transferImage =
        new BufferedImage(TransferFunction.TEXTURE_SIZE, 1, BufferedImage.TYPE_4BYTE_ABGR);
    Graphics2D g2d = (Graphics2D) transferImage.getGraphics();
    g2d.setPaint(displayable.getTransferFunction().getPaint());
    g2d.drawLine(0, 0, TransferFunction.TEXTURE_SIZE, 0);
    g2d.dispose();

    byte[] dataElements =
        (byte[])
            transferImage.getRaster().getDataElements(0, 0, TransferFunction.TEXTURE_SIZE, 1, null);
    gl.glActiveTexture(GL4.GL_TEXTURE0 + TEXTURE_TRANSFER);
    gl.glBindTexture(GL4.GL_TEXTURE_1D, textureLocation[0]);
    gl.glTexImage1D(
        GL4.GL_TEXTURE_1D,
        0,
        GL4.GL_RGBA,
        TransferFunction.TEXTURE_SIZE,
        0,
        GL4.GL_RGBA,
        GL4.GL_UNSIGNED_INT_8_8_8_8_REV,
        ByteBuffer.wrap(dataElements));
    checkError(gl, "Update transfer function");
  }

  @Override
  public void pointCountChanged() {
    transferFunctionDirty = true;
    repaint();
  }

  @Override
  public void pointValueChanged() {
    transferFunctionDirty = true;
    repaint();
  }
}
