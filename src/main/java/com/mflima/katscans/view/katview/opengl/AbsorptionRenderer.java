package com.mflima.katscans.view.katview.opengl;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import com.mflima.katscans.event.TransferFunctionListener;
import com.mflima.katscans.project.displayable.Displayable;
import com.mflima.katscans.model.TransferFunction;

/**
 * @author Marcelo Lima
 */
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
      updateTransferFunction(drawable.getGL().getGL2());
      transferFunctionDirty = false;
    }
  }

  @Override
  public void init(GLAutoDrawable drawable) {
    super.init(drawable);

    GL2 gl2 = drawable.getGL().getGL2();

    gl2.glGenTextures(1, textureLocation, TEXTURE_TRANSFER_LOCAL);
    gl2.glActiveTexture(GL2.GL_TEXTURE0 + TEXTURE_TRANSFER);
    gl2.glBindTexture(GL2.GL_TEXTURE_1D, textureLocation[TEXTURE_TRANSFER_LOCAL]);
    gl2.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
    gl2.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
    gl2.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_BORDER);
    transferFunctionDirty = true;

    int location = gl2.glGetUniformLocation(mainProgram, "transferFunction");
    gl2.glUniform1i(location, TEXTURE_TRANSFER);

    checkError(gl2, "Create transfer function");
  }

  @Override
  public void dispose(GLAutoDrawable drawable) {
    super.dispose(drawable);
    GL2 gl2 = drawable.getGL().getGL2();

    gl2.glDeleteTextures(textureLocation.length, textureLocation, 0);
    checkError(gl2, "Dispose Composite Renderer");
  }

  private void updateTransferFunction(GL2 gl2) {
    BufferedImage transferImage = new BufferedImage(TransferFunction.TEXTURE_SIZE, 1,
        BufferedImage.TYPE_4BYTE_ABGR);
    Graphics2D g2d = (Graphics2D) transferImage.getGraphics();
    g2d.setPaint(displayable.getTransferFunction().getPaint());
    g2d.drawLine(0, 0, TransferFunction.TEXTURE_SIZE, 0);
    g2d.dispose();

    byte[] dataElements = (byte[]) transferImage.getRaster()
        .getDataElements(0, 0, TransferFunction.TEXTURE_SIZE, 1, null);
    gl2.glActiveTexture(GL2.GL_TEXTURE0 + TEXTURE_TRANSFER);
    gl2.glBindTexture(GL2.GL_TEXTURE_1D, textureLocation[0]);
    gl2.glTexImage1D(GL2.GL_TEXTURE_1D, 0, GL2.GL_RGBA, TransferFunction.TEXTURE_SIZE, 0,
        GL2.GL_RGBA, GL2.GL_UNSIGNED_INT_8_8_8_8_REV, ByteBuffer.wrap(dataElements));
    checkError(gl2, "Update transfer function");
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