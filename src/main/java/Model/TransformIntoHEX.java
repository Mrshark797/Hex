package Model;

import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class TransformIntoHEX {
    public byte[] pageBuffer;
    private static final int BYTES_PER_ROW = 16;
    private static final int PAGE_SIZE = 256;


    public DefaultTableModel getHexPage(File file, int pageNumber) throws IOException {
        long offset = (long) pageNumber * PAGE_SIZE;
        long limit = Math.min(offset + PAGE_SIZE, file.length());

        if (offset >= file.length()) {
            return null;
        }

        byte[] buffer = new byte[(int) (limit - offset)];
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(offset);
            raf.readFully(buffer);
        }
        pageBuffer = buffer;

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 0;
            }
        };

        model.addColumn("00000000");

        for (int i = 0; i < BYTES_PER_ROW; i++) {
            model.addColumn(String.format("%02X", i));
        }


        int lines = (buffer.length + 15) / BYTES_PER_ROW;
        int address = (int) offset;
        for (int i = 0; i < lines; i++) {
            String[] row = new String[BYTES_PER_ROW + 1];
            row[0] = String.format("%08X", address);
            for (int j = 0; j < BYTES_PER_ROW && address < limit; j++) {
                int bufferIndex = address - (int)offset + j;
                if(bufferIndex >= 0 && bufferIndex < buffer.length){
                    row[j + 1] = String.format("%02X", buffer[bufferIndex]);
                    address++;
                } else {
                    break;
                }
            }
            model.addRow(row);
        }
        return model;
    }
    public byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}