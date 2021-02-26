package com.mflima.katscans.view.katview.opengl;

import static com.jogamp.common.nio.Buffers.SIZEOF_FLOAT;
import static com.jogamp.common.nio.Buffers.SIZEOF_SHORT;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import java.awt.Graphics2D;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;
import com.mflima.katscans.data.VoxelMatrix;
import com.mflima.katscans.event.TransferFunctionListener;
import com.mflima.katscans.project.displayable.Displayable;
import com.mflima.katscans.model.TransferFunction;
import com.mflima.katscans.view.katview.KatView;

/** @author Marcelo Lima */
public class SliceNavigator extends GLJPanel
    implements KatView, GLEventListener, MouseWheelListener, TransferFunctionListener {

  private static final String PROPERTY_SLICE = "Slice";

  private static final int VERTICES = 0;
  private static final int INDICES = 1;

  private static final int VOLUME = 0;
  private static final int TRANSFER = 1;

  private final Displayable displayable;
  private boolean textureLoaded;

  private final int[] bufferLocation;
  private final int[] textureLocation;
  private boolean transferFunctionDirty;

  private final String SHADERS_ROOT = "/shaders";
  private final String SHADERS_NAME = "slicer";

  private float[] vertices = new float[] {0f, 0f, 0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f, 0f, 0f};
  private short[] indices = new short[] {0, 1, 2, 0, 2, 3};
  private int programName;
  private float sliceMax;
  private int slice;

  public SliceNavigator(Displayable displayable) throws GLException {
    super(new GLCapabilities(GLProfile.get(GLProfile.GL4)));
    addGLEventListener(this);

    bufferLocation = new int[2];
    textureLocation = new int[2];

    slice = (int) (sliceMax / 2f);

    this.displayable = displayable;

    addMouseWheelListener(this);
  }

  @Override
  public void init(GLAutoDrawable drawable) {
    GL4 gl = drawable.getGL().getGL4();

    gl.glGenBuffers(bufferLocation.length, bufferLocation, 0);

    gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, bufferLocation[VERTICES]);
    gl.glBufferData(
        GL4.GL_ARRAY_BUFFER,
        (long) vertices.length * SIZEOF_FLOAT,
        FloatBuffer.wrap(vertices),
        GL4.GL_STATIC_DRAW);

    gl.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, bufferLocation[INDICES]);
    gl.glBufferData(
        GL4.GL_ELEMENT_ARRAY_BUFFER,
        (long) indices.length * SIZEOF_SHORT,
        ShortBuffer.wrap(indices),
        GL4.GL_STATIC_DRAW);

    checkError(gl, "Create Buffers");

    VoxelMatrix voxelMatrix = displayable.getMatrix();
    textureLoaded = voxelMatrix != null;
    if (textureLoaded) {
      sliceMax = voxelMatrix.getSizeZ();
      if (slice >= sliceMax) {
        slice = ((int) sliceMax) - 1;
      }

      short[] texture = voxelMatrix.getData();

      gl.glGenTextures(2, textureLocation, 0);
      gl.glActiveTexture(GL4.GL_TEXTURE0 + VOLUME);
      gl.glBindTexture(GL4.GL_TEXTURE_3D, textureLocation[VOLUME]);
      gl.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
      gl.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
      gl.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_WRAP_R, GL4.GL_CLAMP_TO_EDGE);
      gl.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_WRAP_S, GL4.GL_CLAMP_TO_EDGE);
      gl.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_WRAP_T, GL4.GL_CLAMP_TO_EDGE);

      gl.glTexImage3D(
          GL4.GL_TEXTURE_3D,
          0,
          GL4.GL_RED,
          voxelMatrix.getSizeX(),
          voxelMatrix.getSizeY(),
          voxelMatrix.getSizeZ(),
          0,
          GL4.GL_RED,
          GL4.GL_UNSIGNED_SHORT,
          ShortBuffer.wrap(texture));

      checkError(gl, "Create Texture");

      gl.glActiveTexture(GL4.GL_TEXTURE0 + TRANSFER);
      gl.glBindTexture(GL4.GL_TEXTURE_1D, textureLocation[TRANSFER]);
      gl.glTexParameteri(GL4.GL_TEXTURE_1D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
      gl.glTexParameteri(GL4.GL_TEXTURE_1D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
      gl.glTexParameteri(GL4.GL_TEXTURE_1D, GL4.GL_TEXTURE_WRAP_R, GL4.GL_CLAMP_TO_BORDER);

      checkError(gl, "Create Transfer Function");
      transferFunctionDirty = true;
    }

    ShaderCode vertShader =
        ShaderCode.create(
            gl, GL4.GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT, null, SHADERS_NAME, true);
    ShaderCode fragShader =
        ShaderCode.create(
            gl, GL4.GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT, null, SHADERS_NAME, true);

    ShaderProgram shaderProgram = new ShaderProgram();
    shaderProgram.add(vertShader);
    shaderProgram.add(fragShader);
    shaderProgram.init(gl);

    programName = shaderProgram.program();
    shaderProgram.link(gl, System.out);

    gl.glUseProgram(programName);

    gl.glBindFragDataLocation(programName, 0, "fragColor");
    gl.glBindAttribLocation(programName, 0, "position");

    int location = gl.glGetUniformLocation(programName, "slice");
    gl.glUniform1f(location, slice / sliceMax);

    if (textureLoaded) {
      location = gl.glGetUniformLocation(programName, "volumeTexture");
      gl.glUniform1i(location, VOLUME);

      location = gl.glGetUniformLocation(programName, "transferFunction");
      gl.glUniform1i(location, TRANSFER);
    }

    checkError(gl, "Create Shaders");
  }

  @Override
  public void dispose(GLAutoDrawable drawable) {
    GL4 gl = drawable.getGL().getGL4();

    gl.glDeleteProgram(programName);
    gl.glDeleteBuffers(bufferLocation.length, bufferLocation, 0);

    if (textureLoaded) {
      gl.glDeleteTextures(textureLocation.length, textureLocation, 0);
    }
  }

  @Override
  public void display(GLAutoDrawable drawable) {
    GL4 gl = drawable.getGL().getGL4();
    gl.glUseProgram(programName);

    int location = gl.glGetUniformLocation(programName, "slice");
    gl.glUniform1f(location, slice / sliceMax);

    gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, bufferLocation[VERTICES]);
    gl.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, bufferLocation[INDICES]);
    gl.glEnableVertexAttribArray(0);
    gl.glVertexAttribPointer(0, 3, GL4.GL_FLOAT, false, 0, 0);

    if (transferFunctionDirty) {
      BufferedImage transferImage =
          new BufferedImage(TransferFunction.TEXTURE_SIZE, 1, BufferedImage.TYPE_4BYTE_ABGR);
      Graphics2D g2d = (Graphics2D) transferImage.getGraphics();
      g2d.setPaint(displayable.getTransferFunction().getPaint());
      g2d.drawLine(0, 0, TransferFunction.TEXTURE_SIZE, 0);
      g2d.dispose();

      byte[] dataElements =
          (byte[])
              transferImage
                  .getRaster()
                  .getDataElements(0, 0, TransferFunction.TEXTURE_SIZE, 1, null);
      gl.glActiveTexture(GL4.GL_TEXTURE0 + TRANSFER);
      gl.glBindTexture(GL4.GL_TEXTURE_1D, textureLocation[TRANSFER]);
      gl.glTexImage1D(
          GL4.GL_TEXTURE_1D,
          0,
          GL4.GL_RGBA,
          TransferFunction.TEXTURE_SIZE,
          0,
          GL4.GL_RGBA,
          GL4.GL_UNSIGNED_INT_8_8_8_8_REV,
          ByteBuffer.wrap(dataElements));
      transferFunctionDirty = false;
    }

    gl.glDrawElements(GL4.GL_TRIANGLES, indices.length, GL4.GL_UNSIGNED_SHORT, 0);
  }

  @Override
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    GL4 gl = drawable.getGL().getGL4();

    gl.glUseProgram(programName);
    int location = gl.glGetUniformLocation(programName, "screenSize");
    gl.glUniform2i(location, width, height);
  }

  private void checkError(GL4 gl, String location) {

    int error = gl.glGetError();
    if (error != GL4.GL_NO_ERROR) {
      String errorString;
      switch (error) {
        case GL4.GL_INVALID_ENUM:
          errorString = "GL_INVALID_ENUM";
          break;
        case GL4.GL_INVALID_VALUE:
          errorString = "GL_INVALID_VALUE";
          break;
        case GL4.GL_INVALID_OPERATION:
          errorString = "GL_INVALID_OPERATION";
          break;
        case GL4.GL_INVALID_FRAMEBUFFER_OPERATION:
          errorString = "GL_INVALID_FRAMEBUFFER_OPERATION";
          break;
        case GL4.GL_OUT_OF_MEMORY:
          errorString = "GL_OUT_OF_MEMORY";
          break;
        default:
          errorString = "UNKNOWN";
          break;
      }
      System.out.printf("OpenGL Error(%s): %s%n", errorString, location);
      throw new Error();
    }
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    slice += e.getWheelRotation();
    if (slice < 0) {
      slice = 0;
    } else {
      if (slice >= sliceMax) {
        slice = ((int) sliceMax) - 1;
      }
    }
    repaint();
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

  @Override
  public Map<String, Object> packProperties() {
    HashMap<String, Object> properties = new HashMap<>();
    properties.put(PROPERTY_SLICE, slice);
    return properties;
  }

  @Override
  public void loadProperties(Map<String, Object> properties) {
    if (properties == null || properties.isEmpty()) {
      return;
    }

    Integer newSlice = (Integer) properties.get(PROPERTY_SLICE);
    if (newSlice == null) {
      return;
    }

    this.slice = newSlice;
    repaint();
  }
}
