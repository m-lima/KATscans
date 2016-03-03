/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uib.inf252.katscan.io;

import org.junit.*;

import static org.junit.Assert.assertNotNull;

/**
 * @author Marcelo Lima
 */
public class LoadSaveHandlerTester {

    private DatLoadSaveHandler sut;

    public LoadSaveHandlerTester() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        sut = new DatLoadSaveHandler();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void constructionAndInitializationOfLoadSaveHandler() {
        assertNotNull(sut);
    }

    @Test
    public void loadingOfDatFile() {
        Object loadedData = sut.loadData(null);
        assertNotNull(loadedData);
    }
}
