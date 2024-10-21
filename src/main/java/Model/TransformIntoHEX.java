package Model;

import java.io.*;
import java.nio.file.Files;

public class TransformIntoHEX {
    private String UNKNOWN_CHARACTER = ".";
    private StringBuilder hex = new StringBuilder();
    private StringBuilder result = new StringBuilder();
    private StringBuilder input = new StringBuilder();
    private int count = 0;
    private int value;

    public void getFileInHEX(File file){
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

                    /*Метод выше позволяет открыть файл в консоли,
                     но НЕОБХОДИМО найти иной, дабы он открывался в окне приложения!!!
                     */

    }
}


