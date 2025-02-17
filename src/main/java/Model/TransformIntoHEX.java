package Model;

import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.*;

public class TransformIntoHEX {
    public byte[] pageBuffer;
    private static final int BYTES_PER_ROW = 16;
    private static final int PAGE_SIZE = 256;


    public static DefaultTableModel getHexPage(File file, int pageNumber) throws IOException {

        if(!file.exists()){
            throw new IOException("Файл не найден" + file.getAbsolutePath());
        }
        long offset = (long) pageNumber * PAGE_SIZE;
        long limit = Math.min(offset + PAGE_SIZE, file.length());
        byte[] buffer;

        if (offset >= file.length()) {
            return null;
        }
        buffer = new byte[(int) (limit - offset)];
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(offset);
            raf.readFully(buffer);
            System.out.println("getHexPage: Прочитано " + buffer.length + " байтов из файла, offset: " + offset );

        }

        DefaultTableModel model = new DefaultTableModel();

        model.addColumn("Address");
        for (int i = 0; i < BYTES_PER_ROW; i++) {
            model.addColumn(String.format("%02X", i));
        }
        int lines = (buffer.length + 15) / BYTES_PER_ROW;
        int address = (int) offset;
        for (int i = 0; i < lines; i++) {
            String[] row = new String[BYTES_PER_ROW + 1];
            row[0] = String.format("%08X", address);

            for (int j = 0; j < BYTES_PER_ROW ; j++) {
                int bufferIndex = i * BYTES_PER_ROW + j;
                if (bufferIndex >= 0 && bufferIndex < buffer.length) {
                    int currentAddress = (int)offset + bufferIndex;
                    row[j + 1] = String.format("%02X", buffer[bufferIndex]);
                    System.out.println("getHexPage: buffer[" + bufferIndex +"] = " + String.format("%02X",buffer[bufferIndex])  + " address = "+ currentAddress);
                } else {
                    break;
                }
            }
            model.addRow(row);
            address += BYTES_PER_ROW; // Увеличиваем address на 16 для следующей строки
        }
        return model;
    }

    public byte [] stringToBytes(String hexString, boolean useMask){
        if(hexString == null || hexString.isEmpty()){
            return new byte[0];
        }
        String[] parts = hexString.trim().split("\\s+");
        ArrayList<Byte> bytes = new ArrayList<>();

        for(String part : parts){
            if(part.equals("??") || part.equals("**")){
                if(useMask){
                    bytes.add(null);
                }
                else{
                    return new byte[0];
                }
            }
            else if(part.matches("[0-9A-Fa-f]{2}")){
                bytes.add((byte) Integer.parseInt(part, 16));
            }
            else{
                return new byte[0];
            }
        }
        byte[] byteArray = new byte[bytes.size()];
        for(int i = 0; i<bytes.size(); i++){
            Byte b = bytes.get(i);
            byteArray[i] = (b == null) ? (byte) 0 : b;
        }
        return byteArray;
    }

    public List<Long> findBytes(File file, byte[] searchBytes, boolean useMask) throws IOException {
        List<Long> results = new ArrayList<>();
        if (searchBytes == null || searchBytes.length == 0 || file == null) {
            return results;
        }
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] buffer = new byte[1024];
            long position = 0;
            int bytesRead;

            while ((bytesRead = raf.read(buffer)) != -1) {
                for (int i = 0; i <= bytesRead - searchBytes.length; i++) {
                    boolean match = true;
                    for (int j = 0; j < searchBytes.length; j++) {
                        if(useMask){
                            if(searchBytes[j] != (byte) 0 && searchBytes[j] != buffer[i+j]){
                                match = false;
                                break;
                            }
                        }
                        else{
                            if(searchBytes[j] != buffer[i+j]){
                                match = false;
                                break;
                            }
                        }
                    }
                    if (match) {
                        results.add(position + i);
                    }
                }
                position += bytesRead;
            }
        }
        return results;
    }
}