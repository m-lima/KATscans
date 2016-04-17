package no.uib.inf252.katscan.data;

import no.uib.inf252.katscan.data.io.DatLoadSaveHandler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    public void testLoadData() throws FileNotFoundException {
        InputStream stream = new FileInputStream(new File("misc/sinusveins-256x256x166.dat"));

        VoxelMatrix loadedData = sut.loadData(stream);
        assertNotNull(loadedData);

        assertEquals(256, loadedData.getSizeX());
        assertEquals(256, loadedData.getSizeY());
        assertEquals(166, loadedData.getSizeZ());

        assertEquals(1, loadedData.getValue(0, 0, 0));
        assertEquals(1039, loadedData.getValue(100, 100, 100));
    }

    @Test
    public void testSaveData() {
        ByteArrayOutputStream out = new ByteArrayOutputStream(20 * 20 * 20 + 6);
        VoxelMatrix grid = new VoxelMatrix(10, 10, 10);
        grid.setValue(0, 0, 0, (short)2);
        
        sut.saveData(out, grid);
        assertTrue(out.size() > 0);
        byte[] byteArray = out.toByteArray();
        assertNotNull(byteArray);
        assertTrue((byteArray[0] | (byteArray[1] << 1)) == 10);
    }
    
    @Test
    public void testLoadSaveLoad() throws IOException {
        InputStream stream = new FileInputStream(new File("misc/sinusveins-256x256x166.dat"));

        System.gc();
        VoxelMatrix loadedData = sut.loadData(stream);
        
        int sizeX = loadedData.getSizeX();
        int sizeY = loadedData.getSizeY();
        int sizeZ = loadedData.getSizeZ();
        
        ByteArrayOutputStream out = new ByteArrayOutputStream(sizeX * sizeY * sizeZ + 6); //Not doubling values on purpose
        
        sut.saveData(out, loadedData);
        InputStream in = new ByteArrayInputStream(out.toByteArray());
        VoxelMatrix otherLoad = sut.loadData(in);
        in.close();
        out.close();
        in = null;
        out = null;
        System.gc();
        
        assertEquals(loadedData, otherLoad);
    }
}
