package no.uib.inf252.katscan.io;

import no.uib.inf252.katscan.data.VoxelMatrix;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.InputStream;

import static junit.framework.TestCase.assertNotNull;

/**
 * @author Marcelo Lima
 */
public class LoadSaveHandlerTest {

    private DatLoadSaveHandler sut;

    public LoadSaveHandlerTest() {
    }

//    @BeforeClass
//    public static void setUpClass() {
//    }

//    @AfterClass
//    public static void tearDownClass() {
//    }

    @Before
    public void setUp() {
        sut = new DatLoadSaveHandler();
    }

//    @After
//    public void tearDown() {
//    }

    @Test
    public void constructionAndInitializationOfLoadSaveHandler() {
        assertNotNull(sut);
    }

    @Test
    public void loadingOfDatFile() {
        InputStream stream = sut.getClass().getResourceAsStream("/sinusveins-256x256x166.dat");

        VoxelMatrix loadedData = sut.loadData(stream);
        assertNotNull(loadedData);

        assertEquals(256, loadedData.getLength(VoxelMatrix.Axis.X));
        assertEquals(256, loadedData.getLength(VoxelMatrix.Axis.Y));
        assertEquals(166, loadedData.getLength(VoxelMatrix.Axis.Z));

        assertEquals(1, loadedData.getValue(0, 0, 0));
        assertEquals(1039, loadedData.getValue(100, 100, 100));
    }
}
