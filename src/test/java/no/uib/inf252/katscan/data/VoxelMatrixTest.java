package no.uib.inf252.katscan.data;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by mflim_000 on 03-Mar-16.
 */
public class VoxelMatrixTest {

    private VoxelMatrix sut;

    @Before
    public void setUp() {
    }

    @Test
    public void testConstructor() {
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                for (int k = -1; k < 2; k++) {

                    boolean expected = !(i == 1 && j == 1 && k == 1);
                    boolean threw = false;
                    try {
                        sut = new VoxelMatrix(k, j, i, 4096);
                    } catch (IllegalArgumentException ex) {
                        assertTrue(expected);
                        threw = true;
                    }

                    assertFalse(threw && !expected);
                }
            }
        }
    }

    @Test
    public void testGetLength() throws Exception {
        int z = (int) (Math.random() * 20 + 1);
        int y = (int) (Math.random() * 20 + 1);
        int x = (int) (Math.random() * 20 + 1);

        sut = new VoxelMatrix(z, y, x, 4096);
        assertEquals(z, sut.getSizeZ());
        assertEquals(y, sut.getSizeY());
        assertEquals(x, sut.getSizeX());
    }

    @Test
    public void testGetSetValue() throws Exception {
        sut = new VoxelMatrix(10, 10, 10, 4096);

        int z = (int) (Math.random() * 10);
        int y = (int) (Math.random() * 10);
        int x = (int) (Math.random() * 10);

        short value = (short) (Math.random() * 100);
        sut.setValue(z, y, x, value);

        assertEquals(value, sut.getValue(z, y, x));
    }

}