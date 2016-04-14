package no.uib.inf252.katscan.util;

/**
 *
 * @author Marcelo Lima
 */
public class DisplayObject {

    public enum Type {
        TRIANGLE, SQUARE, CUBE;
    }

    private final float[] vertices;
    private final short[] indices;

    public DisplayObject() {
        this(Type.SQUARE);
    }

    public DisplayObject(Type type) {
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
                break;
            case CUBE:
                vertices = new float[]{
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
                    //Back
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
                    
                    //Orig
//                    1, 5, 7,
//                    7, 3, 1,
//                    0, 2, 6,
//                    6, 4, 0,
//                    0, 1, 3,
//                    3, 2, 0,
//                    7, 5, 4,
//                    4, 6, 7,
//                    2, 3, 7,
//                    7, 6, 2,
//                    1, 0, 4,
//                    4, 5, 1
                };
                break;
        }
    }

    public float[] getVertices() {
        return vertices;
    }

    public short[] getIndices() {
        return indices;
    }
}
