package no.uib.inf252.katscan.data.io;

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
        int sizeZ, sizeY, sizeX;

        try {
            VoxelMatrix grid;
            if (stream.read(byteBuffer.array()) > 0) {
                sizeX = byteBuffer.getShort();
                sizeY = byteBuffer.getShort();
                sizeZ = byteBuffer.getShort();

                grid = new VoxelMatrix(sizeZ, sizeY, sizeX);
            } else {
                throw new StreamCorruptedException("Could not read dat header from the stream");
            }

            ShortBuffer shortBuffer;
            byteBuffer = ByteBuffer.allocate(sizeX * 2);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            short[] gridData = grid.getData();
            
            for (int i = 0; i < sizeZ; i++) {
                for (int j = 0; j < sizeY; j++) {
                    if (stream.read(byteBuffer.array()) < sizeX * 2)
                        throw new IOException("Expected data, but could not be read");
                    shortBuffer = byteBuffer.asShortBuffer();
                    shortBuffer.get(gridData, i * sizeY * sizeX + j * sizeX, sizeX);
                }
            }

            grid.updateHistogram();
            return grid;
        } catch (IOException ex) {
            Logger.getLogger(DatLoadSaveHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public void saveData(OutputStream stream, VoxelMatrix grid) {
        int sizeX = grid.getSizeX();
        int sizeY = grid.getSizeY();
        int sizeZ = grid.getSizeZ();
        
        ByteBuffer byteBuffer = ByteBuffer.allocate(6);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putShort((short) sizeX);
        byteBuffer.putShort((short) sizeY);
        byteBuffer.putShort((short) sizeZ);
        
        short[] gridData = grid.getData();
        
        try {
            stream.write(byteBuffer.array());         
            stream.flush();   
            byteBuffer = ByteBuffer.allocate(sizeX * 2);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            
            for (int i = 0; i < sizeZ; i++) {
                for (int j = 0; j < sizeY; j++) {
                    ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
                    shortBuffer.put(gridData, i * sizeY * sizeX + j * sizeX, sizeX);
                    stream.write(byteBuffer.array());
                    stream.flush();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(DatLoadSaveHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
