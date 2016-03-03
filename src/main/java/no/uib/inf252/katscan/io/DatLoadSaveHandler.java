package no.uib.inf252.katscan.io;

import no.uib.inf252.katscan.data.VoxelMatrix;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Marcelo Lima
 */
public class DatLoadSaveHandler implements LoadSaveHandler {

    @Override
    public VoxelMatrix loadData(InputStream stream) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(6);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        BufferedInputStream bufferedInput = new BufferedInputStream(stream);
        int sizeZ, sizeY, sizeX;

        try {
            VoxelMatrix grid;
            if (bufferedInput.read(byteBuffer.array()) > 0) {
                sizeX = byteBuffer.getShort();
                sizeY = byteBuffer.getShort();
                sizeZ = byteBuffer.getShort();

                grid = new VoxelMatrix(sizeZ, sizeY, sizeX);
            } else {
                throw new StreamCorruptedException("Could not read dat header from the stream");
            }

            short[] column;
            ShortBuffer shortBuffer;
            byteBuffer = ByteBuffer.allocate(sizeX * 2);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < sizeZ; i++) {
                for (int j = 0; j < sizeY; j++) {
                    if (bufferedInput.read(byteBuffer.array()) <= sizeX)
                        throw new IOException("Expected data, but could not be read");
                    column = grid.getColumn(i, j);
                    shortBuffer = byteBuffer.asShortBuffer();
                    shortBuffer.get(column);
                }
            }

            return grid;
        } catch (IOException ex) {
            Logger.getLogger(DatLoadSaveHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public void saveData(OutputStream stream, Object object) {

    }
}
