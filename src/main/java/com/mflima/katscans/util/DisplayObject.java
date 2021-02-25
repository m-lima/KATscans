package com.mflima.katscans.util;

/**
 * @author Marcelo Lima
 */
public class DisplayObject {

  public enum Type {
    TRIANGLE, SQUARE, CUBE;
  }

  private static DisplayObject TRIANGLE_INSTANCE;
  private static DisplayObject SQUARE_INSTANCE;
  private static DisplayObject CUBE_INSTANCE;

  public static DisplayObject getObject(Type type) {
    if (type == null) {
      throw new NullPointerException("Null type for DisplayObject");
    }

    switch (type) {
      default:
      case TRIANGLE:
        if (TRIANGLE_INSTANCE == null) {
          TRIANGLE_INSTANCE = new DisplayObject(Type.TRIANGLE);
        }
        return TRIANGLE_INSTANCE;
      case SQUARE:
        if (SQUARE_INSTANCE == null) {
          SQUARE_INSTANCE = new DisplayObject(Type.SQUARE);
        }
        return SQUARE_INSTANCE;
      case CUBE:
        if (CUBE_INSTANCE == null) {
          CUBE_INSTANCE = new DisplayObject(Type.CUBE);
        }
        return CUBE_INSTANCE;
    }
  }

  private final float[] vertices;
  private final short[] indices;
  private final short[] indicesRev;

  private DisplayObject(Type type) {
    switch (type) {
      default:
      case TRIANGLE:
        vertices = new float[]{
            0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 0.0f
        };
        indices = new short[]{
            0, 1, 2
        };
        indicesRev = new short[]{
            0, 2, 1
        };
        break;
      case SQUARE:
        vertices = new float[]{
            0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
            1.0f, 0.0f, 0.0f
        };
        indices = new short[]{
            0, 1, 2,
            0, 3, 2
        };
        indicesRev = new short[]{
            0, 2, 1,
            0, 2, 3
        };
        break;
      case CUBE:
        vertices = new float[]{
//                    -0.5f, -0.5f, -0.5f,
//                    -0.5f, -0.5f, 0.5f,
//                    -0.5f, 0.5f, -0.5f,
//                    -0.5f, 0.5f, 0.5f,
//                    0.5f, -0.5f, -0.5f,
//                    0.5f, -0.5f, 0.5f,
//                    0.5f, 0.5f, -0.5f,
//                    0.5f, 0.5f, 0.5f
            0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 1.0f
        };
        indices = new short[]{
            1, 5, 7,
            7, 3, 1,
            0, 2, 6,
            6, 4, 0,
            0, 1, 3,
            3, 2, 0,
            7, 5, 4,
            4, 6, 7,
            2, 3, 7,
            7, 6, 2,
            1, 0, 4,
            4, 5, 1
        };
        indicesRev = new short[]{
            1, 7, 5,
            7, 1, 3,
            0, 6, 2,
            6, 0, 4,
            0, 3, 1,
            3, 0, 2,
            7, 4, 5,
            4, 7, 6,
            2, 7, 3,
            7, 2, 6,
            1, 4, 0,
            4, 1, 5
        };
        break;
    }
  }

  public float[] getVertices() {
    return vertices;
  }

  public short[] getIndicesCW() {
    return indices;
  }

  public short[] getIndicesCCW() {
    return indicesRev;
  }
}
