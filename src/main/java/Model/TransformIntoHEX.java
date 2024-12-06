package Model;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


public class TransformIntoHEX {
    /* private String UNKNOWN_CHARACTER = ".";
    private StringBuilder hex = new StringBuilder();
    private StringBuilder result = new StringBuilder();
    private StringBuilder input = new StringBuilder();
    private int count = 0;
    private int value;
    */

    /*
    Метод ниже создаёт таблицу и размещает в ней 16-ые данные в каждой ячейке
     */
    public JTable getFileInHEX(File file) {
        List<String[]> hexLines = new ArrayList<>();
        try (InputStream fis = Files.newInputStream(file.toPath())) {
            byte[] buffer = new byte[16];
            int bytesRead;
            int lineNumber = 0;
            /*
            Метод read(buffer) считывает в массив buffer из потока символов fis (которые берётся из выбранного файла),
            количество которых равно длине массива buffer.
            Возвращает количество успешно считанных символов.
            При достижении конца файла возвращает -1.
            */
            while ((bytesRead = fis.read(buffer)) != -1) {
                String[] hexRow = new String[bytesRead + 1]; //  Создаем  массив  для  строки  таблицы
                hexRow[0] = String.format("%07X", lineNumber++)+"0";
                for (int i = 0; i < bytesRead; i++) {
                    hexRow[i+1] = String.format("%02X ", buffer[i]); //  Добавляем  16-ричное  значение  в  массив

                }
                hexLines.add(hexRow);
                //  Добавляем  массив  в  список
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        //  Создаем  DefaultTableModel  и  JTable  с  помощью  hexLines
        DefaultTableModel model = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 0;
            }
        };

        //  Добавляем  заголовки  столбцов  (по  количеству  байтов  в  строке)

        for (int i = 0; i < hexLines.get(0).length; i++) {
                if(i == 0){
                    model.addColumn("");
                }
                else{
                    model.addColumn("0" + Integer.toHexString(i-1));
                }
            }

        for(String[] hexRow : hexLines){
            model.addRow(hexRow); //  Добавляем  массив  в  строку  таблицы
        }

        JTable table = new JTable(model);
        return table;
    }


    /* public void getFileInHEX(File file){
        try (InputStream fis = Files.newInputStream(file.toPath())) {
            while ((value = fis.read()) != -1) {
                hex.append(String.format("%02X ", value));

                if (!Character.isISOControl(value)) {
                    input.append((char) value);
                } else {
                    input.append(UNKNOWN_CHARACTER);
                }

                if (count == 15) {
                    result.append(String.format("%-60s | %s%n", hex, input));
                    hex.setLength(0);
                    input.setLength(0);
                    count = 0;
                } else {
                    count++;
                }
            }

            if (count > 0) {
                result.append(String.format("%-60s | %s%n", hex, input));
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        System.out.println(result);
        }
        */
                    /*Метод выше позволяет открыть файл в консоли,
                     но НЕОБХОДИМО найти иной, дабы он открывался в окне приложения!!!
                     */
}