package com.mflima.katscans.util;

import com.jogamp.opengl.math.FloatUtil;

/** @author Marcelo Lima */
public final class MatrixUtil {

  private static final float[] tempMatrix = new float[16];

  private MatrixUtil() {};

  public static String toString(float[] matrix) {
    if (matrix.length == 9) {
      return String.format(
          "|%09.4f|%09.4f|%09.4f|\n|%09.4f|%09.4f|%09.4f|\n|%09.4f|%09.4f|%09.4f|\n",
          matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5], matrix[6], matrix[7],
          matrix[8]);
    } else if (matrix.length == 16) {
      return String.format(
          "|%09.4f|%09.4f|%09.4f|%09.4f|\n"
              + "|%09.4f|%09.4f|%09.4f|%09.4f|\n"
              + "|%09.4f|%09.4f|%09.4f|%09.4f|\n"
              + "|%09.4f|%09.4f|%09.4f|%09.4f|\n",
          matrix[0],
          matrix[4],
          matrix[8],
          matrix[12],
          matrix[1],
          matrix[5],
          matrix[9],
          matrix[13],
          matrix[2],
          matrix[6],
          matrix[10],
          matrix[14],
          matrix[3],
          matrix[7],
          matrix[11],
          matrix[15]);
      //                    matrix[0], matrix[1], matrix[2], matrix[3],
      //                    matrix[4], matrix[5], matrix[6], matrix[7],
      //                    matrix[8], matrix[9], matrix[10], matrix[11],
      //                    matrix[12], matrix[13], matrix[14], matrix[15]));
    } else {
      return "";
    }
  }

  public static float[] getMatrix3(float[] matrix4) {
    synchronized (tempMatrix) {
      int k = 0;
      for (int i = 0; i < 11; i++) {
        if ((i + 1) % 4 == 0) {
          continue;
        }
        tempMatrix[k++] = matrix4[i];
      }
      return tempMatrix;
    }
  }

  public static float[] getMatrix3(float[] matrix4, float[] matrix3) {
    int k = 0;
    for (int i = 0; i < 11; i++) {
      if ((i + 1) % 4 == 0) {
        continue;
      }
      matrix3[k++] = matrix4[i];
    }
    return matrix3;
  }

  public static float[] getInverse(float[] matrix) {
    synchronized (tempMatrix) {
      return FloatUtil.invertMatrix(matrix, tempMatrix);
    }
  }

  public static float[] multiply(float[] matrix1, float[] matrix2) {
    synchronized (tempMatrix) {
      return FloatUtil.multMatrix(matrix1, matrix2, tempMatrix);
    }
  }
}
