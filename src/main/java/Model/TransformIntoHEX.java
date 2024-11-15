package Model;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HexFormat;
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
    Метод ниже создаёт массив из 16-чных строк
     */
    public JTable getFileInHEX(File file) {
        List<String> hexLines = new ArrayList<>();
        try (InputStream fis = Files.newInputStream(file.toPath())) {
            byte[] buffer = new byte[16];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                StringBuilder hexLine = new StringBuilder();
                for (int i = 0; i < bytesRead; i++) {
                    hexLine.append(String.format("%02X ", buffer[i]));
                }
                hexLines.add(hexLine.toString().trim()); //  Добавляем  строку  в  список
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        //  Создаем  DefaultTableModel  и  JTable  с  помощью  hexLines
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("HEX");
        for (String hexValue : hexLines) {
            model.addRow(new Object[]{hexValue});
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


