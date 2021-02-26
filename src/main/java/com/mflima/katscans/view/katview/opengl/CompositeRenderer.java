package com.mflima.katscans.view.katview.opengl;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLException;
import com.mflima.katscans.event.LightListener;
import com.mflima.katscans.event.TransferFunctionListener;
import com.mflima.katscans.model.Light;
import com.mflima.katscans.model.TransferFunction;
import com.mflima.katscans.project.displayable.Displayable;
import com.mflima.katscans.util.Normal;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

/** @author Marcelo Lima */
public class CompositeRenderer extends VolumeRenderer
    implements TransferFunctionListener, LightListener {

  private static final int LIGHT_DIRTY = 1;
  private static final int NORMAL_DIRTY = 1 << 1;
  private static final int TRANSFER_FUNCTION_DIRTY = 1 << 2;

  private static final int TEXTURE_TRANSFER_LOCAL = 0;
  private static final int TEXTURE_TRANSFER = TEXTURE_COUNT_PARENT + TEXTURE_TRANSFER_LOCAL;

  private final int[] textureLocation = new int[1];

  private int dirtyValues;
  private final Normal normal;
  private final Light light;

  public CompositeRenderer(Displayable displayable) throws GLException {
    super(displayable, "compoCaster", 0.5f);
    light = displayable.getLight();
    normal = new Normal();
  }

  @Override
  public boolean isUnlit() {
    return false;
  }

  @Override
  protected void preDraw(GLAutoDrawable drawable) {
    GL4 gl = drawable.getGL().getGL4();

    if ((dirtyValues & (LIGHT_DIRTY | NORMAL_DIRTY | TRANSFER_FUNCTION_DIRTY)) != 0) {
      int uniformLocation;
      if ((dirtyValues & TRANSFER_FUNCTION_DIRTY) > 0) {
        updateTransferFunction(gl);
      }

      if ((dirtyValues & NORMAL_DIRTY) > 0) {
        uniformLocation = gl.glGetUniformLocation(mainProgram, "normalMatrix");
        gl.glUniformMatrix3fv(uniformLocation, 1, false, normal.getNormalMatrix(), 0);
      }

      if ((dirtyValues & LIGHT_DIRTY) > 0) {
        uniformLocation = gl.glGetUniformLocation(mainProgram, "lightPos");
        float[] lightPos = light.getLightPosition();
        gl.glUniform3fv(uniformLocation, 1, lightPos, 0);
        uniformLocation = gl.glGetUniformLocation(mainProgram, "lightPosFront");
        gl.glUniform3f(uniformLocation, -lightPos[0], -lightPos[1], -lightPos[2]);
      }

      dirtyValues = 0;
    }
  }

  @Override
  public void init(GLAutoDrawable drawable) {
    super.init(drawable);

    GL4 gl = drawable.getGL().getGL4();
    dirtyValues = LIGHT_DIRTY | NORMAL_DIRTY | TRANSFER_FUNCTION_DIRTY;
    normal.updateMatrices(camera, rotation, tempMatrix);

    gl.glGenTextures(1, textureLocation, TEXTURE_TRANSFER_LOCAL);
    gl.glActiveTexture(GL4.GL_TEXTURE0 + TEXTURE_TRANSFER);
    gl.glBindTexture(GL4.GL_TEXTURE_1D, textureLocation[TEXTURE_TRANSFER_LOCAL]);
    gl.glTexParameteri(GL4.GL_TEXTURE_1D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
    gl.glTexParameteri(GL4.GL_TEXTURE_1D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
    gl.glTexParameteri(GL4.GL_TEXTURE_1D, GL4.GL_TEXTURE_WRAP_R, GL4.GL_CLAMP_TO_BORDER);

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
    dirtyValues |= TRANSFER_FUNCTION_DIRTY;
    repaint();
  }

  @Override
  public void pointValueChanged() {
    dirtyValues |= TRANSFER_FUNCTION_DIRTY;
    repaint();
  }

  @Override
  public void lightValueChanged() {
    dirtyValues |= LIGHT_DIRTY;
    repaint();
  }

  @Override
  public void rotationValueChanged() {
    super.rotationValueChanged();
    dirtyValues |= NORMAL_DIRTY;
    normal.updateMatrices(camera, rotation, tempMatrix);
  }

  @Override
  public void viewValueChanged() {
    super.viewValueChanged();
    dirtyValues |= NORMAL_DIRTY;
    normal.updateMatrices(camera, rotation, tempMatrix);
  }
}
