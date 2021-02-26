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
import com.mflima.katscans.data.VoxelMatrix;
import com.mflima.katscans.event.CameraListener;
import com.mflima.katscans.event.CutListener;
import com.mflima.katscans.event.RotationListener;
import com.mflima.katscans.event.ScreenListener;
import com.mflima.katscans.model.Camera;
import com.mflima.katscans.model.Cut;
import com.mflima.katscans.model.Rotation;
import com.mflima.katscans.model.Screen;
import com.mflima.katscans.project.displayable.Displayable;
import com.mflima.katscans.util.DisplayObject;
import com.mflima.katscans.util.KatViewHandler;
import com.mflima.katscans.util.MatrixUtil;
import com.mflima.katscans.view.katview.KatView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;

/** @author Marcelo Lima */
public abstract class VolumeRenderer extends GLJPanel
    implements KatView,
        GLEventListener,
        RotationListener,
        CameraListener,
        CutListener,
        ScreenListener {

  private static final int MODEL_DIRTY = 0;
  private static final int VIEW_DIRTY = 1 << 1;
  private static final int PROJECTION_DIRTY = 1 << 2;
  private static final int ORTHO_DIRTY = 1 << 3;
  private static final int SLICE_DIRTY = 1 << 4;
  private static final int MIN_DIRTY = 1 << 5;
  private static final int MAX_DIRTY = 1 << 6;
  private static final int STEP_DIRTY = 1 << 7;
  private static final int TRACKED_FLAGS =
      PROJECTION_DIRTY
          | VIEW_DIRTY
          | MODEL_DIRTY
          | ORTHO_DIRTY
          | SLICE_DIRTY
          | MIN_DIRTY
          | MAX_DIRTY
          | STEP_DIRTY;

  private static final String PROPERTY_SCREEN = "Screen";

  private static final String SHADERS_ROOT = "/shaders/";
  private static final String SHADER_RAYCASTER_NAME = "raycaster";
  private static final String SHADER_MAIN_NAME = "maincaster";
  private final String shaderName;

  private static final int SHADER_RAYCASTER_VERTEX = 0;
  private static final int SHADER_RAYCASTER_FRAG = 1;
  private static final int SHADER_MAIN_VERTEX = 2;
  private static final int SHADER_MAIN_FRAG = 3;

  private static final int BUFFER_VERTICES = 0;
  private static final int BUFFER_INDICES = 1;
  private static final int BUFFER_INDICES_REV = 2;

  private static final int TEXTURE_VOLUME = 0;
  private static final int TEXTURE_FRAME_BUFFER = 1;
  private static final int TEXTURE_RESO_BUFFER = 2;
  protected static final int TEXTURE_COUNT_PARENT = 3;

  private static final int FRAME_BUFFER_FRONT = 0;
  private static final int FRAME_BUFFER_RESO = 1;

  private final int[] shaderLocation;
  private final int[] bufferLocation;
  private final int[] textureLocation;
  private final int[] frameBuffer;

  protected final transient float[] tempMatrix;

  private int indicesCount;

  protected final Displayable displayable;
  protected final Cut cut;
  protected final Camera camera;
  protected final Rotation rotation;
  protected final Screen screen;

  private int raycastingProgram;
  protected int mainProgram;

  private int numSample;

  private final Timer threadLOD;
  private boolean highLOD;
  private final float lodFactor;
  private int lodWidth;
  private int lodHeight;

  private int dirtyValues;

  VolumeRenderer(Displayable displayable, String shaderName, float lodFactor) throws GLException {
    super(new GLCapabilities(GLProfile.get(GLProfile.GL4)));
    addGLEventListener(this);

    shaderLocation = new int[4];
    bufferLocation = new int[3];
    textureLocation = new int[3];
    frameBuffer = new int[2];

    this.lodFactor = lodFactor;
    this.shaderName = shaderName;

    this.displayable = displayable;
    this.cut = displayable.getCut();
    this.camera = displayable.getCamera();
    this.rotation = displayable.getRotation();

    screen = new Screen();
    screen.addKatModelListener(this);

    new KatViewHandler(this, displayable, screen);

    tempMatrix = new float[16];
    numSample = 256;

    threadLOD =
        new Timer(
            750,
            e -> {
              highLOD = true;
              repaint();
            });
    threadLOD.setRepeats(false);
  }

  protected abstract void preDraw(GLAutoDrawable drawable);

  public abstract boolean isUnlit();

  public void createStructure(int x, int y, float threshold) {}

  @Override
  public void init(GLAutoDrawable drawable) {
    VoxelMatrix voxelMatrix = displayable.getMatrix();
    if (voxelMatrix == null) {
      throw new GLException("Could not load the volume data");
    }

    dirtyValues = TRACKED_FLAGS;
    highLOD = true;

    GL4 gl = drawable.getGL().getGL4();
    loadVertices(gl);
    loadTexture(gl, voxelMatrix);
    loadFrameBuffers(gl);
    loadPrograms(gl);
    loadRaycastingInitialUniforms(gl, voxelMatrix);
    loadMainInitialUniforms(gl, voxelMatrix);
  }

  private void loadVertices(GL4 gl) {
    gl.glGenBuffers(bufferLocation.length, bufferLocation, 0);
    DisplayObject displayObject = DisplayObject.getObject(DisplayObject.Type.CUBE);
    indicesCount = displayObject.getIndicesCW().length;

    float[] vertices = displayObject.getVertices();
    gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, bufferLocation[BUFFER_VERTICES]);
    gl.glBufferData(
        GL4.GL_ARRAY_BUFFER,
        (long) vertices.length * SIZEOF_FLOAT,
        FloatBuffer.wrap(vertices),
        GL4.GL_STATIC_DRAW);

    short[] indices = displayObject.getIndicesCW();
    gl.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, bufferLocation[BUFFER_INDICES]);
    gl.glBufferData(
        GL4.GL_ELEMENT_ARRAY_BUFFER,
        (long) indices.length * SIZEOF_SHORT,
        ShortBuffer.wrap(indices),
        GL4.GL_STATIC_DRAW);

    indices = displayObject.getIndicesCCW();
    gl.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, bufferLocation[BUFFER_INDICES_REV]);
    gl.glBufferData(
        GL4.GL_ELEMENT_ARRAY_BUFFER,
        (long) indices.length * SIZEOF_SHORT,
        ShortBuffer.wrap(indices),
        GL4.GL_STATIC_DRAW);

    checkError(gl, "Load vertices");
  }

  private void loadTexture(GL4 gl, VoxelMatrix voxelMatrix) throws RuntimeException {
    short[] texture = voxelMatrix.getData();

    numSample =
        (int)
            Math.sqrt(
                voxelMatrix.getSizeX() * voxelMatrix.getSizeX()
                    + voxelMatrix.getSizeY() * voxelMatrix.getSizeY()
                    + voxelMatrix.getSizeZ() * voxelMatrix.getSizeZ());

    gl.glGenTextures(1, textureLocation, TEXTURE_VOLUME);
    gl.glActiveTexture(GL4.GL_TEXTURE0 + TEXTURE_VOLUME);
    gl.glBindTexture(GL4.GL_TEXTURE_3D, textureLocation[TEXTURE_VOLUME]);
    gl.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
    gl.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
    gl.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_WRAP_R, GL4.GL_CLAMP_TO_BORDER);
    gl.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_WRAP_S, GL4.GL_CLAMP_TO_BORDER);
    gl.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_WRAP_T, GL4.GL_CLAMP_TO_BORDER);
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
  }

  private void loadFrameBuffers(GL4 gl) {
    lodWidth = (int) (getWidth() * lodFactor);
    lodHeight = (int) (getHeight() * lodFactor);

    gl.glGenTextures(1, textureLocation, TEXTURE_FRAME_BUFFER);
    gl.glActiveTexture(GL4.GL_TEXTURE0 + TEXTURE_FRAME_BUFFER);
    gl.glBindTexture(GL4.GL_TEXTURE_2D, textureLocation[TEXTURE_FRAME_BUFFER]);

    gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
    gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
    gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_S, GL4.GL_CLAMP_TO_BORDER);
    gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_T, GL4.GL_CLAMP_TO_BORDER);
    gl.glTexImage2D(
        GL4.GL_TEXTURE_2D,
        0,
        GL4.GL_RGB,
        lodWidth,
        lodHeight,
        0,
        GL4.GL_RGB,
        GL4.GL_UNSIGNED_BYTE,
        null);

    gl.glGenFramebuffers(1, frameBuffer, FRAME_BUFFER_FRONT);
    gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, frameBuffer[FRAME_BUFFER_FRONT]);
    gl.glFramebufferTexture2D(
        GL4.GL_FRAMEBUFFER,
        GL4.GL_COLOR_ATTACHMENT0,
        GL4.GL_TEXTURE_2D,
        textureLocation[TEXTURE_FRAME_BUFFER],
        0);

    if (gl.glCheckFramebufferStatus(GL4.GL_FRAMEBUFFER) != GL4.GL_FRAMEBUFFER_COMPLETE) {
      throw new GLException("Failed to load front frame buffer");
    }

    checkError(gl, "Load front frame buffer");

    gl.glGenTextures(1, textureLocation, TEXTURE_RESO_BUFFER);
    gl.glActiveTexture(GL4.GL_TEXTURE0 + TEXTURE_RESO_BUFFER);
    gl.glBindTexture(GL4.GL_TEXTURE_2D, textureLocation[TEXTURE_RESO_BUFFER]);

    gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
    gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
    gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_S, GL4.GL_CLAMP_TO_BORDER);
    gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_T, GL4.GL_CLAMP_TO_BORDER);

    gl.glTexImage2D(
        GL4.GL_TEXTURE_2D,
        0,
        GL4.GL_RGB,
        lodWidth,
        lodHeight,
        0,
        GL4.GL_RGB,
        GL4.GL_UNSIGNED_BYTE,
        null);

    gl.glGenFramebuffers(1, frameBuffer, FRAME_BUFFER_RESO);
    gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, frameBuffer[FRAME_BUFFER_RESO]);
    gl.glFramebufferTexture2D(
        GL4.GL_FRAMEBUFFER,
        GL4.GL_COLOR_ATTACHMENT0,
        GL4.GL_TEXTURE_2D,
        textureLocation[TEXTURE_RESO_BUFFER],
        0);

    if (gl.glCheckFramebufferStatus(GL4.GL_FRAMEBUFFER) != GL4.GL_FRAMEBUFFER_COMPLETE) {
      throw new GLException("Failed to load resolution frame buffer");
    }

    checkError(gl, "Load resolution frame buffer");
  }

  private void loadPrograms(GL4 gl) throws GLException {
    loadAndCompileShader(SHADER_RAYCASTER_NAME, SHADER_RAYCASTER_VERTEX, GL4.GL_VERTEX_SHADER, gl);
    loadAndCompileShader(SHADER_RAYCASTER_NAME, SHADER_RAYCASTER_FRAG, GL4.GL_FRAGMENT_SHADER, gl);
    loadAndCompileShader(SHADER_MAIN_NAME, SHADER_MAIN_VERTEX, GL4.GL_VERTEX_SHADER, gl);
    loadAndCompileShader(shaderName, SHADER_MAIN_FRAG, GL4.GL_FRAGMENT_SHADER, gl);

    raycastingProgram = gl.glCreateProgram();
    if (raycastingProgram == 0) {
      throw new GLException("Could not create raycasting program");
    }

    mainProgram = gl.glCreateProgram();
    if (mainProgram == 0) {
      throw new GLException("Could not create main program");
    }

    gl.glAttachShader(raycastingProgram, shaderLocation[SHADER_RAYCASTER_VERTEX]);
    gl.glAttachShader(raycastingProgram, shaderLocation[SHADER_RAYCASTER_FRAG]);
    gl.glLinkProgram(raycastingProgram);
    gl.glDetachShader(raycastingProgram, shaderLocation[SHADER_RAYCASTER_VERTEX]);
    gl.glDetachShader(raycastingProgram, shaderLocation[SHADER_RAYCASTER_FRAG]);
    checkLink(gl, raycastingProgram);
    checkError(gl, "Link raycasting program");

    gl.glAttachShader(mainProgram, shaderLocation[SHADER_MAIN_VERTEX]);
    gl.glAttachShader(mainProgram, shaderLocation[SHADER_MAIN_FRAG]);
    gl.glLinkProgram(mainProgram);
    gl.glDetachShader(mainProgram, shaderLocation[SHADER_MAIN_VERTEX]);
    gl.glDetachShader(mainProgram, shaderLocation[SHADER_MAIN_FRAG]);
    checkLink(gl, mainProgram);
    checkError(gl, "Link main program");

    for (int shader : shaderLocation) {
      gl.glDeleteShader(shader);
    }
  }

  private void loadAndCompileShader(String codeFile, int location, int type, GL4 gl)
      throws GLException {
    String[] shaderCode = new String[1];
    StringBuilder codeBuilder = new StringBuilder();
    codeFile += type == GL4.GL_VERTEX_SHADER ? ".vp" : ".fp";
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(getClass().getResourceAsStream(SHADERS_ROOT + codeFile)))) {
      String line;
      while ((line = reader.readLine()) != null) {
        codeBuilder.append(line);
        codeBuilder.append('\n');
      }
    } catch (IOException ex) {
      Logger.getLogger(VolumeRenderer.class.getName()).log(Level.SEVERE, null, ex);
    }

    shaderCode[0] = codeBuilder.toString();
    if (shaderCode[0].isEmpty()) {
      throw new GLException("Could not read shader");
    }

    int shader = gl.glCreateShader(type);
    gl.glShaderSource(shader, 1, shaderCode, new int[] {shaderCode[0].length()}, 0);
    gl.glCompileShader(shader);
    shaderLocation[location] = shader;
    checkCompile(gl, shader, codeFile);
    checkError(gl, "Load and compile shader");
  }

  private void loadRaycastingInitialUniforms(GL4 gl, VoxelMatrix voxelMatrix) {
    gl.glUseProgram(raycastingProgram);

    gl.glBindFragDataLocation(raycastingProgram, 0, "fragColor");
    gl.glBindAttribLocation(raycastingProgram, 0, "position");

    int location = gl.glGetUniformLocation(raycastingProgram, "ratio");
    gl.glUniform3fv(location, 1, voxelMatrix.getRatio(), 0);

    checkError(gl, "Load initial raycasting uniforms");
  }

  private void loadMainInitialUniforms(GL4 gl, VoxelMatrix voxelMatrix) {
    gl.glUseProgram(mainProgram);

    gl.glBindFragDataLocation(mainProgram, 0, "fragColor");
    gl.glBindAttribLocation(mainProgram, 0, "position");

    int location = gl.glGetUniformLocation(mainProgram, "numSamples");
    gl.glUniform1i(location, numSample);

    location = gl.glGetUniformLocation(mainProgram, "ratio");
    gl.glUniform3fv(location, 1, voxelMatrix.getRatio(), 0);

    location = gl.glGetUniformLocation(mainProgram, "volumeTexture");
    gl.glUniform1i(location, TEXTURE_VOLUME);

    location = gl.glGetUniformLocation(mainProgram, "raycastTexture");
    gl.glUniform1i(location, TEXTURE_FRAME_BUFFER);

    checkError(gl, "Load initial main uniforms");
  }

  @Override
  public void display(GLAutoDrawable drawable) {
    GL4 gl = drawable.getGL().getGL4();

    if (highLOD) {
      updateFrameBuffersSize(gl, getWidth(), getHeight());
    }

    gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, frameBuffer[FRAME_BUFFER_FRONT]);
    gl.glViewport(0, 0, lodWidth, lodHeight);
    initializeRender(gl);
    gl.glUseProgram(raycastingProgram);

    checkAndLoadRaycastingUpdates(gl);
    draw(gl, false);
    gl.glFinish();

    gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, frameBuffer[FRAME_BUFFER_RESO]);
    gl.glViewport(0, 0, lodWidth, lodHeight);
    initializeRender(gl);
    gl.glUseProgram(mainProgram);

    checkAndLoadMainUpdates(gl);
    preDraw(drawable);
    checkError(gl, "Pre draw");
    draw(gl, true);
    gl.glFinish();

    gl.glBindFramebuffer(GL4.GL_DRAW_FRAMEBUFFER, 0);
    gl.glViewport(0, 0, getWidth(), getHeight());
    initializeRender(gl);
    gl.glBlitFramebuffer(
        0,
        0,
        lodWidth,
        lodHeight,
        0,
        0,
        getWidth(),
        getHeight(),
        GL4.GL_COLOR_BUFFER_BIT,
        GL4.GL_NEAREST);

    if (highLOD) {
      updateFrameBuffersSize(gl, (int) (getWidth() * lodFactor), (int) (getHeight() * lodFactor));
      highLOD = false;
    } else {
      threadLOD.restart();
    }
  }

  private void initializeRender(GL4 gl) {
    //        gl4.glClearColor(0.234375f, 0.24609375f, 0.25390625f,1.0f);
    //        gl4.glClearColor(0.2f,0.2f,0.2f,1.0f);
    gl.glClearColor(0f, 0f, 0f, 1.0f);
    gl.glClear(GL4.GL_COLOR_BUFFER_BIT);

    //        gl4.glEnable(GL4.GL_DEPTH_TEST);
    gl.glEnable(GL4.GL_CULL_FACE);
    gl.glCullFace(GL4.GL_BACK);
    gl.glEnable(GL4.GL_BLEND);
    gl.glBlendFunc(GL4.GL_SRC_ALPHA, GL4.GL_ONE_MINUS_SRC_ALPHA);
    //        gl.glBlendFunc(GL4.GL_ONE_MINUS_DST_ALPHA, GL4.GL_ONE);

    checkError(gl, "Initialize render");
  }

  private void checkAndLoadRaycastingUpdates(GL4 gl) {
    int uniformLocation;
    if ((dirtyValues & (PROJECTION_DIRTY | VIEW_DIRTY | MODEL_DIRTY)) != 0) {

      if ((dirtyValues & PROJECTION_DIRTY) > 0) {
        uniformLocation = gl.glGetUniformLocation(raycastingProgram, "projection");
        gl.glUniformMatrix4fv(uniformLocation, 1, false, screen.getProjectionMatrix(), 0);
      }

      if ((dirtyValues & VIEW_DIRTY) > 0) {
        uniformLocation = gl.glGetUniformLocation(raycastingProgram, "view");
        gl.glUniformMatrix4fv(uniformLocation, 1, false, camera.getViewMatrix(), 0);
      }

      if ((dirtyValues & MODEL_DIRTY) > 0) {
        uniformLocation = gl.glGetUniformLocation(raycastingProgram, "model");
        gl.glUniformMatrix4fv(uniformLocation, 1, false, rotation.getModelMatrix(), 0);
      }

      checkError(gl, "Update raycasting dirty values");
    }
  }

  protected void checkAndLoadMainUpdates(GL4 gl) {
    int uniformLocation;
    if ((dirtyValues & (TRACKED_FLAGS)) != 0) {

      if ((dirtyValues & PROJECTION_DIRTY) > 0) {
        uniformLocation = gl.glGetUniformLocation(mainProgram, "projection");
        gl.glUniformMatrix4fv(uniformLocation, 1, false, screen.getProjectionMatrix(), 0);
      }

      if ((dirtyValues & VIEW_DIRTY) > 0) {
        uniformLocation = gl.glGetUniformLocation(mainProgram, "view");
        gl.glUniformMatrix4fv(uniformLocation, 1, false, camera.getViewMatrix(), 0);
        uniformLocation = gl.glGetUniformLocation(mainProgram, "eyePos");
        gl.glUniform3fv(uniformLocation, 1, camera.getEyePosition(), 0);
      }

      if ((dirtyValues & MODEL_DIRTY) > 0) {
        uniformLocation = gl.glGetUniformLocation(mainProgram, "model");
        gl.glUniformMatrix4fv(uniformLocation, 1, false, rotation.getModelMatrix(), 0);
        uniformLocation = gl.glGetUniformLocation(mainProgram, "invModel");
        gl.glUniformMatrix3fv(
            uniformLocation,
            1,
            false,
            MatrixUtil.getMatrix3(MatrixUtil.getInverse(rotation.getModelMatrix())),
            0);
      }

      if ((dirtyValues & ORTHO_DIRTY) > 0) {
        uniformLocation = gl.glGetUniformLocation(mainProgram, "orthographic");
        gl.glUniform1i(uniformLocation, screen.isOrthographic() ? 1 : 0);
      }

      if ((dirtyValues & SLICE_DIRTY) > 0) {
        uniformLocation = gl.glGetUniformLocation(mainProgram, "slice");
        float slice = cut.getSlice() + camera.getZoom() - camera.getInitialZoom();
        gl.glUniform1f(uniformLocation, Math.max(slice, 0f));
      }

      if ((dirtyValues & MIN_DIRTY) > 0) {
        uniformLocation = gl.glGetUniformLocation(mainProgram, "minValues");
        gl.glUniform3fv(uniformLocation, 1, cut.getMinValues(), 0);
      }

      if ((dirtyValues & MAX_DIRTY) > 0) {
        uniformLocation = gl.glGetUniformLocation(mainProgram, "maxValues");
        gl.glUniform3fv(uniformLocation, 1, cut.getMaxValues(), 0);
      }

      if ((dirtyValues & STEP_DIRTY) > 0) {
        uniformLocation = gl.glGetUniformLocation(mainProgram, "stepFactor");
        gl.glUniform1f(uniformLocation, screen.getStepFactor());
      }

      dirtyValues = 0;
      checkError(gl, "Update main dirty values");
    }
  }

  private void draw(GL4 gl, boolean back) {
    gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, bufferLocation[BUFFER_VERTICES]);
    gl.glBindBuffer(
        GL4.GL_ELEMENT_ARRAY_BUFFER, bufferLocation[back ? BUFFER_INDICES_REV : BUFFER_INDICES]);
    gl.glEnableVertexAttribArray(0);
    gl.glVertexAttribPointer(0, 3, GL4.GL_FLOAT, false, 0, 0);

    gl.glDrawElements(GL4.GL_TRIANGLES, indicesCount, GL4.GL_UNSIGNED_SHORT, 0);

    checkError(gl, "Draw");
  }

  @Override
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    screen.updateProjection(camera, width, height);

    GL4 gl = drawable.getGL().getGL4();
    updateFrameBuffersSize(gl, (int) (width * lodFactor), (int) (height * lodFactor));
  }

  @Override
  public void dispose(GLAutoDrawable drawable) {
    GL4 gl = drawable.getGL().getGL4();

    gl.glUseProgram(0);
    gl.glDeleteProgram(raycastingProgram);
    gl.glDeleteProgram(mainProgram);

    gl.glDeleteBuffers(bufferLocation.length, bufferLocation, 0);
    gl.glDeleteFramebuffers(frameBuffer.length, frameBuffer, 0);

    gl.glDeleteTextures(textureLocation.length, textureLocation, 0);
    checkError(gl, "Dispose");
  }

  protected void checkError(GL4 gl, String location) {

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
      String message =
          String.format(
              "%s :: OpenGL Error(%s): %s", getClass().getSimpleName(), errorString, location);
      System.err.println(message);
      throw new Error(message);
    }
  }

  private void updateFrameBuffersSize(GL4 gl, int width, int height) {
    lodWidth = width;
    lodHeight = height;

    gl.glActiveTexture(GL4.GL_TEXTURE0 + TEXTURE_FRAME_BUFFER);
    gl.glBindTexture(GL4.GL_TEXTURE_2D, textureLocation[TEXTURE_FRAME_BUFFER]);
    gl.glTexImage2D(
        GL4.GL_TEXTURE_2D,
        0,
        GL4.GL_RGB,
        lodWidth,
        lodHeight,
        0,
        GL4.GL_RGB,
        GL4.GL_UNSIGNED_BYTE,
        null);

    gl.glActiveTexture(GL4.GL_TEXTURE0 + TEXTURE_RESO_BUFFER);
    gl.glBindTexture(GL4.GL_TEXTURE_2D, textureLocation[TEXTURE_RESO_BUFFER]);
    gl.glTexImage2D(
        GL4.GL_TEXTURE_2D,
        0,
        GL4.GL_RGB,
        lodWidth,
        lodHeight,
        0,
        GL4.GL_RGB,
        GL4.GL_UNSIGNED_BYTE,
        null);

    gl.glUseProgram(mainProgram);
    int location = gl.glGetUniformLocation(mainProgram, "screenSize");
    gl.glUniform2i(location, width, height);

    checkError(gl, "Update frame buffer textures");
  }

  private void checkCompile(GL4 gl, int shader, String shaderName) {
    int[] returnValue = new int[1];
    gl.glGetShaderiv(shader, GL4.GL_COMPILE_STATUS, returnValue, 0);

    if (returnValue[0] == GL4.GL_FALSE) {
      gl.glGetShaderiv(shader, GL4.GL_INFO_LOG_LENGTH, returnValue, 0);

      StringBuilder message =
          new StringBuilder(String.format("Compilation error on %s: ", shaderName));

      IntBuffer written = IntBuffer.allocate(1);
      ByteBuffer log = ByteBuffer.allocate(returnValue[0]);
      gl.glGetShaderInfoLog(shader, returnValue[0], written, log);
      byte[] logArray = log.array();

      for (byte letter : logArray) {
        message.append((char) letter);
      }

      System.err.println(message.toString());
      throw new Error(message.toString());
    }
  }

  private void checkLink(GL4 gl, int program) {
    int[] returnValue = new int[1];
    gl.glGetProgramiv(program, GL4.GL_LINK_STATUS, returnValue, 0);

    if (returnValue[0] == GL4.GL_FALSE) {
      gl.glGetShaderiv(program, GL4.GL_INFO_LOG_LENGTH, returnValue, 0);

      StringBuilder message =
          new StringBuilder(
              String.format(
                  "Link error on %s program: ",
                  program == raycastingProgram ? "raycasting" : "main"));

      IntBuffer written = IntBuffer.allocate(1);
      ByteBuffer log = ByteBuffer.allocate(returnValue[0]);
      gl.glGetProgramInfoLog(program, returnValue[0], written, log);
      byte[] logArray = log.array();

      for (byte letter : logArray) {
        message.append((char) letter);
      }

      System.err.println(message.toString());
      throw new Error(message.toString());
    }
  }

  public float[] getTempMatrix() {
    return tempMatrix;
  }

  @Override
  public void rotationValueChanged() {
    dirtyValues |= MODEL_DIRTY;
    repaint();
  }

  @Override
  public void viewValueChanged() {
    dirtyValues |= VIEW_DIRTY;
    repaint();
  }

  @Override
  public void zoomValueChanged() {
    dirtyValues |= SLICE_DIRTY;
    if (screen.isOrthographic()) {
      screen.updateProjection(camera, getWidth(), getHeight());
    }
    repaint();
  }

  @Override
  public void orthographicValueChanged() {
    dirtyValues |= ORTHO_DIRTY;
    screen.updateProjection(camera, getWidth(), getHeight());
    repaint();
  }

  @Override
  public void minValueChanged() {
    dirtyValues |= MIN_DIRTY;
    repaint();
  }

  @Override
  public void maxValueChanged() {
    dirtyValues |= MAX_DIRTY;
    repaint();
  }

  @Override
  public void sliceValueChanged() {
    dirtyValues |= SLICE_DIRTY;
    repaint();
  }

  @Override
  public void projectionValueChanged() {
    dirtyValues |= PROJECTION_DIRTY;
    repaint();
  }

  @Override
  public void stepValueChanged() {
    dirtyValues |= STEP_DIRTY;
    repaint();
  }

  @Override
  public Map<String, Object> packProperties() {
    HashMap<String, Object> properties = new HashMap<>();
    properties.put(PROPERTY_SCREEN, screen);
    return properties;
  }

  @Override
  public void loadProperties(Map<String, Object> properties) {
    if (properties == null || properties.isEmpty()) {
      return;
    }

    Screen newScreen = (Screen) properties.get(PROPERTY_SCREEN);
    if (newScreen == null) {
      return;
    }

    this.screen.assimilate(newScreen);
  }
}
