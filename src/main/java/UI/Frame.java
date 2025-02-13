package UI;

import Model.TransformIntoHEX;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;


public class Frame extends JFrame {

    private JMenuBar jMenuBar;

    private JMenu jMenu;
    private JMenu jMenuEdit;

    private JMenuItem fileOpen;
    private JMenuItem fileSave;
    private JMenuItem fileSaveAs;
    private JMenuItem deleteWithZeroingMenuItem;
    private JMenuItem putWithChange;
    private JMenuItem deleteWithShift;
    private JMenuItem insertWithoutReplace;
    private JMenuItem copySelectedBytes;
    private JMenuItem cutBytes;
    private JMenuItem pasteBytes;

    private JPanel contentPane;
    private JPanel panelForTable;
    private JPanel panelForButtons;

    private JLabel dataValueLabel;
    private JLabel resultLabel;
    private JTextField pageTextField;
    private JTextField searchTextField;
    private JComboBox<String> dataTypeComboBox;
    private JButton searchButton;
    private JRadioButton exactMatchRadioButton;
    private JRadioButton maskMatchRadioButton;
    private ButtonGroup searchTypeGroup;

    private JTable hexTable;
    private JFileChooser fileChooser = new JFileChooser();
    private TransformIntoHEX transformIntoHEX = new TransformIntoHEX();
    private File file;
    private int currentPage = 0;
    private static final int BYTES_PER_ROW = 16;
    private static final int PAGE_SIZE = 256;



    public void createFrame() {
        jMenuBar = new JMenuBar();
        jMenu = new JMenu("File");
        jMenuEdit = new JMenu("Edit");
        fileOpen = new JMenuItem("Open");
        fileSave = new JMenuItem("Save");
        fileSaveAs = new JMenuItem("Save As");
        deleteWithZeroingMenuItem = new JMenuItem("Удалить (обнулением)");
        putWithChange = new JMenuItem("Вставка с заменой");
        deleteWithShift = new JMenuItem("Удаление со сдвигом");
        insertWithoutReplace = new JMenuItem("Вставка(без замены)");
        copySelectedBytes = new JMenuItem("Копировать");
        cutBytes = new JMenuItem("Вырезать");
        pasteBytes = new JMenuItem("Копировать из буфера");


        jMenuBar.add(jMenu);
        jMenuBar.add(jMenuEdit);
        jMenu.add(fileOpen);
        jMenu.add(fileSave);
        jMenu.add(fileSaveAs);
        jMenuEdit.add(deleteWithZeroingMenuItem);
        jMenuEdit.add(putWithChange);
        jMenuEdit.add(deleteWithShift);
        jMenuEdit.add(insertWithoutReplace);
        jMenuEdit.add(copySelectedBytes);
        jMenuEdit.add(cutBytes);
        jMenuEdit.add(pasteBytes);
        setJMenuBar(jMenuBar);

        panelForTable = new JPanel(new BorderLayout());
        panelForTable.setBackground(Color.GRAY);
        panelForButtons = new JPanel(new FlowLayout());
        panelForButtons.setBackground(Color.GREEN);

        JButton prevButton = new JButton("Previous");
        JButton nextButton = new JButton("Next");
        pageTextField = new JTextField(5);
        panelForButtons.add(prevButton);
        panelForButtons.add(nextButton);
        panelForButtons.add(pageTextField);

        String[] dataTypes = {
                "Int16 (signed 2 bytes)", "UInt16 (unsigned 2 bytes)",
                "Int32 (signed 4 bytes)", "UInt32 (unsigned 4 bytes)",
                "Float (4 bytes)",
                "Int64 (signed 8 bytes)", "UInt64 (unsigned 8 bytes)",
                "Double (8 bytes)"
        };
        dataTypeComboBox = new JComboBox<>(dataTypes);
        panelForButtons.add(dataTypeComboBox);
        dataValueLabel = new JLabel("Value: ");
        panelForButtons.add(dataValueLabel);
        searchTextField = new JTextField(20);
        searchButton = new JButton("Search");
        resultLabel = new JLabel("Results: ");
        exactMatchRadioButton = new JRadioButton("Exact match");
        maskMatchRadioButton = new JRadioButton("Mask match");
        searchTypeGroup = new ButtonGroup();
        searchTypeGroup.add(exactMatchRadioButton);
        searchTypeGroup.add(maskMatchRadioButton);
        exactMatchRadioButton.setSelected(true);
        panelForButtons.add(dataValueLabel);


        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchTextField);
        searchPanel.add(exactMatchRadioButton);
        searchPanel.add(maskMatchRadioButton);
        searchPanel.add(searchButton);
        searchPanel.add(resultLabel);

        add(searchPanel, BorderLayout.SOUTH);
        contentPane = new JPanel(new BorderLayout());
        contentPane.add(panelForTable, BorderLayout.CENTER);
        contentPane.add(panelForButtons, BorderLayout.SOUTH);
        add(contentPane);

        searchButton.addActionListener(e -> {
            String searchText = searchTextField.getText();
            boolean useMask = maskMatchRadioButton.isSelected();
            try {
                byte[] searchBytes = transformIntoHEX.stringToBytes(searchText, useMask);
                if (searchBytes.length == 0) {
                    resultLabel.setText("Incorrect input");
                    return;
                }
                List<Long> results = transformIntoHEX.findBytes(file, searchBytes, useMask);
                if (results.isEmpty()) {
                    resultLabel.setText("No matches found.");
                } else {
                    resultLabel.setText("Match found at the following offsets: " + results);
                    highlightSearchResults(results);
                }
            } catch (IOException ex) {
                resultLabel.setText("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        hexTable = new JTable();

        panelForTable.add(new JScrollPane(hexTable), BorderLayout.CENTER);
        addTableModelListener(hexTable);
        hexTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                displaySelectedData();
            }
        });

        fileOpen.addActionListener(e -> openFile());
        fileSaveAs.addActionListener(e -> saveFileAs());
        deleteWithZeroingMenuItem.addActionListener(e->deleteSelectedBytesWithZeroing());
        putWithChange.addActionListener(e->insertBytesWithReplace());
        deleteWithShift.addActionListener(e->deleteSelectedBytesWithShift());
        insertWithoutReplace.addActionListener(e->insertBytesWithoutReplace());
        copySelectedBytes.addActionListener(e->copySelectedBytes());
        cutBytes.addActionListener(e->cutSelectedBytes());
        pasteBytes.addActionListener(e->pasteBytes());

        prevButton.addActionListener(e -> showPage(currentPage - 1));
        nextButton.addActionListener(e -> showPage(currentPage + 1));
        pageTextField.addActionListener(e -> {
            try {
                int newPage = Integer.parseInt(pageTextField.getText()) - 1;
                showPage(newPage);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Неверный номер страницы!");
            }
        });


        setSize(1200, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }
    // Метод для сохранения файла через диалоговое окно
    private void saveFileAs() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save As");
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (RandomAccessFile raf = new RandomAccessFile(fileToSave, "rw")){
                file = fileToSave; // Обновляем текущий файл

                long fileLength = file.length();
                int totalPages = (int) Math.ceil((double) fileLength / PAGE_SIZE);

                for (int page = 0; page < totalPages; page++) {
                    DefaultTableModel model = transformIntoHEX.getHexPage(file, page);
                    if(model == null) continue;
                    int rowCount = model.getRowCount();
                    int colCount = model.getColumnCount();

                    for (int row = 0; row < rowCount; row++) {
                        for (int col = 1; col < colCount; col++) {
                            if (col < model.getColumnCount()) {
                                String hexValue = (String) model.getValueAt(row, col);
                                if(hexValue == null) continue;
                                int address = Integer.parseInt(model.getValueAt(row,0).toString(), 16) + col - 1;
                                byte newByte = (byte) Integer.parseInt(hexValue, 16);
                                raf.seek(address);
                                raf.writeByte(newByte);
                            }
                        }
                    }

                }
                JOptionPane.showMessageDialog(this, "File saved successfully to: " + fileToSave.getAbsolutePath());
                System.out.println("saveFileAs: файл сохранен по адресу: " + fileToSave.getAbsolutePath());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                System.out.println("saveFileAs: Ошибка сохранения: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void openFile() {
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            showPage(0);
        }
    }

    private void displaySelectedData() {
        int selectedRow = hexTable.getSelectedRow();
        int selectedCol = hexTable.getSelectedColumn();
        if (selectedRow == -1 || selectedCol <= 0) {
            return;
        }
        int address = Integer.parseInt(hexTable.getModel().getValueAt(selectedRow, 0).toString(), 16);
        int offset = address + selectedCol - 1;
        try {
            byte[] data = getBytesFromOffset(offset);
            if (data == null) {
                dataValueLabel.setText("Value: -");
                return;
            }
            String selectedType = (String) dataTypeComboBox.getSelectedItem();
            if (selectedType != null) {
                Object value = interpretData(data, selectedType);
                dataValueLabel.setText("Value: " + value);
            }
        } catch (Exception e) {
            dataValueLabel.setText("Value: Error");
            e.printStackTrace();
        }
    }
    private byte[] getBytesFromOffset(int offset) throws IOException {
        if (file == null) {
            return null;
        }
        long fileLength = file.length();
        if (offset < 0 || offset >= fileLength) {
            return null;
        }
        int bytesToRead = 0;
        String selectedType = (String) dataTypeComboBox.getSelectedItem();
        if (selectedType != null) {
            if (selectedType.contains("2 bytes")) {
                bytesToRead = 2;
            } else if (selectedType.contains("4 bytes")) {
                bytesToRead = 4;
            } else if (selectedType.contains("8 bytes")) {
                bytesToRead = 8;
            }
        }

        long remainingBytes = fileLength - offset;
        bytesToRead = (int) Math.min(bytesToRead, remainingBytes);

        if (bytesToRead <= 0) {
            return null;
        }
        byte[] buffer = new byte[bytesToRead];
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(offset);
            raf.readFully(buffer);
        }
        return buffer;
    }
    private Object interpretData(byte[] data, String type) {
        if (data == null || data.length == 0) {
            return "-";
        }
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        try {
            switch (type) {
                case "Int16 (signed 2 bytes)":
                    if (data.length < 2) return "Not enough bytes";
                    return buffer.getShort();
                case "UInt16 (unsigned 2 bytes)":
                    if (data.length < 2) return "Not enough bytes";
                    return buffer.getShort() & 0xFFFF;
                case "Int32 (signed 4 bytes)":
                    if (data.length < 4) return "Not enough bytes";
                    return buffer.getInt();
                case "UInt32 (unsigned 4 bytes)":
                    if (data.length < 4) return "Not enough bytes";
                    return buffer.getInt() & 0xFFFFFFFFL;
                case "Float (4 bytes)":
                    if (data.length < 4) return "Not enough bytes";
                    return buffer.getFloat();
                case "Int64 (signed 8 bytes)":
                    if (data.length < 8) return "Not enough bytes";
                    return buffer.getLong();
                case "UInt64 (unsigned 8 bytes)":
                    if (data.length < 8) return "Not enough bytes";
                    return buffer.getLong() & 0xFFFFFFFFFFFFFFFFL;
                case "Double (8 bytes)":
                    if (data.length < 8) return "Not enough bytes";
                    return buffer.getDouble();
            }
        } catch (BufferUnderflowException e) {
            return "Not enough bytes";
        }
        catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
        return "Unknown type";
    }
    private void showPage(int pageNumber) {
        try {
            DefaultTableModel model = transformIntoHEX.getHexPage(file, pageNumber);
            if (model == null) {
                JOptionPane.showMessageDialog(this, "Страница не найдена!");
                return;
            }
            System.out.println("showPage: Модель таблицы создана");
            hexTable.setModel(model);
            System.out.println("showPage: Модель установлена в JTable");
            addTableModelListener(hexTable);
            System.out.println("showPage: Слушатель добавлен к таблице");
            currentPage = pageNumber;
            pageTextField.setText(String.valueOf(currentPage + 1));
            hexTable.repaint();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки страницы: " + ex.getMessage());
        }
    }

    private void addTableModelListener(JTable table) {
        table.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                System.out.println("tableChanged: Событие произошло"); // Добавлен лог
                if (e.getType() == TableModelEvent.UPDATE) {
                    System.out.println("tableChanged: Обновление ячейки"); // Добавлен лог
                    int row = e.getFirstRow();
                    int col = e.getColumn();
                    DefaultTableModel model = (DefaultTableModel) e.getSource();
                    String newValue = (String) model.getValueAt(row, col);
                    try {
                        updatePageBuffer(row, col, newValue);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(Frame.this, "Ошибка обновления буфера: " + ex.getMessage());
                    }
                }
            }
        });
    }
    private void updatePageBuffer(int row, int col, String newValue) {
        if (col <= 0 || col >= hexTable.getColumnCount()) {
            return;  // Ничего не делаем, если столбец некорректен
        }
        try {
            int address = Integer.parseInt(hexTable.getModel().getValueAt(row, 0).toString(), 16) + col-1;
            System.out.println("updatePageBuffer: Адрес (десятичный): " + address);
            System.out.println("updatePageBuffer: Адрес (шестнадцатеричный): " + String.format("%08X", address));

            // Преобразуем новое значение в байт
            byte newByte = (byte) Integer.parseInt(newValue, 16);

            // Записываем байт в файл в отдельном потоке
            new Thread(() -> {
                try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                    raf.seek(address);
                    raf.writeByte(newByte);
                    System.out.println("updatePageBuffer: Записан байт " + String.format("%02X", newByte) + " по адресу " + address);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(Frame.this, "Ошибка записи в файл: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }).start();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(Frame.this, "Неверный формат числа: " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(Frame.this, "Ошибка обновления буфера: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    private void highlightSearchResults(List<Long> results) {
        if (results == null || results.isEmpty() || hexTable.getModel() == null) {
            return;
        }
        hexTable.clearSelection();
        for (long result : results) {
            int pageNumber = (int) (result / PAGE_SIZE);
            if (pageNumber == currentPage) {
                int offsetInPage = (int) (result % PAGE_SIZE);
                int row = offsetInPage / BYTES_PER_ROW;
                int col = offsetInPage % BYTES_PER_ROW + 1;
                hexTable.addRowSelectionInterval(row, row);
                hexTable.addColumnSelectionInterval(col, col);
            }
        }
    }

    private void deleteSelectedBytesWithZeroing() {
        int[] selectedRows = hexTable.getSelectedRows();
        int[] selectedColumns = hexTable.getSelectedColumns();

        if (selectedRows.length == 0 || selectedColumns.length == 0) {
            JOptionPane.showMessageDialog(this, "Выделите ячейки для удаления.");
            return;
        }

        // 1. Вычисляем адреса байтов, которые нужно обнулить
        List<Integer> addressesToDelete = new ArrayList<>();
        for (int row : selectedRows) {
            for (int col : selectedColumns) {
                if (col > 0) { // Пропускаем столбец с адресами
                    try {
                        int address = Integer.parseInt(hexTable.getModel().getValueAt(row, 0).toString(), 16) + col - 1;
                        addressesToDelete.add(address);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, "Ошибка при вычислении адреса: " + e.getMessage());
                        return;
                    }
                }
            }
        }

        // 2. Записываем 0x00 в соответствующие позиции в файле
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            for (int address : addressesToDelete) {
                raf.seek(address);
                raf.writeByte(0x00);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при записи в файл: " + e.getMessage());
            return;
        }

        // 3. Обновляем таблицу
        updateTableData();
    }

    private void insertBytesWithReplace() {
        int selectedRow = hexTable.getSelectedRow();
        int selectedColumn = hexTable.getSelectedColumn();

        if (selectedRow == -1 || selectedColumn <= 0) {
            JOptionPane.showMessageDialog(this, "Выделите ячейку для вставки.");
            return;
        }

        // 1. Получаем адрес для вставки
        int insertAddress;
        try {
            insertAddress = Integer.parseInt(hexTable.getModel().getValueAt(selectedRow, 0).toString(), 16) + selectedColumn - 1;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при вычислении адреса: " + e.getMessage());
            return;
        }

        // 2. Получаем данные для вставки из диалогового окна
        String bytesToInsert = JOptionPane.showInputDialog(this, "Введите байты для вставки (в HEX формате, разделенные пробелами):");
        if (bytesToInsert == null || bytesToInsert.isEmpty()) {
            return; // Пользователь отменил ввод
        }

        // Разбиваем строку на отдельные байты
        String[] byteStrings = bytesToInsert.split("\\s+");
        byte[] dataToInsert = new byte[byteStrings.length];
        try {
            for (int i = 0; i < byteStrings.length; i++) {
                dataToInsert[i] = (byte) Integer.parseInt(byteStrings[i], 16);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Неверный формат байта: " + e.getMessage());
            return;
        }

        // 3. Записываем данные в файл
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(insertAddress);
            raf.write(dataToInsert);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при записи в файл: " + e.getMessage());
            return;
        }

        // 4. Обновляем таблицу
        updateTableData();
    }

    private void deleteSelectedBytesWithShift() {
        int[] selectedRows = hexTable.getSelectedRows();
        int[] selectedColumns = hexTable.getSelectedColumns();

        if (selectedRows.length == 0 || selectedColumns.length == 0) {
            JOptionPane.showMessageDialog(this, "Выделите ячейки для удаления.");
            return;
        }

        // 1. Вычисляем адреса байтов, которые нужно удалить
        List<Integer> addressesToDelete = new ArrayList<>();
        for (int row : selectedRows) {
            for (int col : selectedColumns) {
                if (col > 0) { // Пропускаем столбец с адресами
                    try {
                        int address = Integer.parseInt(hexTable.getModel().getValueAt(row, 0).toString(), 16) + col - 1;
                        addressesToDelete.add(address);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, "Ошибка при вычислении адреса: " + e.getMessage());
                        return;
                    }
                }
            }
        }

        // Сортируем адреса в порядке убывания, чтобы удалять с конца
        Collections.sort(addressesToDelete, Collections.reverseOrder());

        // 2. Удаляем байты из файла и сдвигаем данные
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            long fileLength = raf.length();
            for (int address : addressesToDelete) {
                if (address >= 0 && address < fileLength) {
                    // Сдвигаем все байты после address на 1 байт влево
                    for (long i = address + 1; i < fileLength; i++) {
                        raf.seek(i);
                        byte b = raf.readByte();
                        raf.seek(i - 1);
                        raf.writeByte(b);
                    }
                    // Уменьшаем длину файла на 1 байт
                    raf.setLength(fileLength - 1);
                    fileLength--;
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при удалении байтов: " + e.getMessage());
            return;
        }

        // 3. Обновляем таблицу
        updateTableData();
    }

    private void insertBytesWithoutReplace() {
        int selectedRow = hexTable.getSelectedRow();
        int selectedColumn = hexTable.getSelectedColumn();

        if (selectedRow == -1 || selectedColumn <= 0) {
            JOptionPane.showMessageDialog(this, "Выделите ячейку для вставки.");
            return;
        }

        // 1. Получаем адрес для вставки
        int insertAddress;
        try {
            insertAddress = Integer.parseInt(hexTable.getModel().getValueAt(selectedRow, 0).toString(), 16) + selectedColumn - 1;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при вычислении адреса: " + e.getMessage());
            return;
        }

        // 2. Получаем данные для вставки из диалогового окна
        String bytesToInsert = JOptionPane.showInputDialog(this, "Введите байты для вставки (в HEX формате, разделенные пробелами):");
        if (bytesToInsert == null || bytesToInsert.isEmpty()) {
            return; // Пользователь отменил ввод
        }

        // Разбиваем строку на отдельные байты
        String[] byteStrings = bytesToInsert.split("\\s+");
        byte[] dataToInsert = new byte[byteStrings.length];
        try {
            for (int i = 0; i < byteStrings.length; i++) {
                dataToInsert[i] = (byte) Integer.parseInt(byteStrings[i], 16);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Неверный формат байта: " + e.getMessage());
            return;
        }

        // 3. Сдвигаем все байты, начиная с insertAddress, в сторону конца файла
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            long fileLength = raf.length();
            long insertLength = dataToInsert.length;

            // Сдвигаем байты с конца файла, чтобы не затереть данные
            for (long i = fileLength - 1; i >= insertAddress; i--) {
                raf.seek(i);
                byte b = raf.readByte();
                raf.seek(i + insertLength);
                if (i + insertLength < fileLength + insertLength) {
                    raf.writeByte(b);
                } else {
                    // Если выходим за пределы исходного файла, просто пропускаем запись
                }
            }

            // 4. Записываем данные в файл, начиная с insertAddress
            raf.seek(insertAddress);
            raf.write(dataToInsert);

            // 5. Увеличиваем длину файла на размер вставленных данных
            raf.setLength(fileLength + insertLength);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при вставке байтов: " + e.getMessage());
            return;
        }

        // 6. Обновляем таблицу
        updateTableData();
    }

    private void copySelectedBytes() {
        int[] selectedRows = hexTable.getSelectedRows();
        int[] selectedColumns = hexTable.getSelectedColumns();

        if (selectedRows.length == 0 || selectedColumns.length == 0) {
            JOptionPane.showMessageDialog(this, "Выделите ячейки для копирования.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int row : selectedRows) {
            for (int col : selectedColumns) {
                if (col > 0) { // Пропускаем столбец с адресами
                    String hexValue = (String) hexTable.getValueAt(row, col);
                    if (hexValue != null) {
                        sb.append(hexValue).append(" ");
                    }
                }
            }
        }
        String data = sb.toString().trim(); // trim() удаляет последний пробел
        if (!data.isEmpty()) {
            StringSelection selection = new StringSelection(data);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
        }
    }

    private void cutSelectedBytes() {
        int[] selectedRows = hexTable.getSelectedRows();
        int[] selectedColumns = hexTable.getSelectedColumns();

        if (selectedRows.length == 0 || selectedColumns.length == 0) {
            JOptionPane.showMessageDialog(this, "Выделите ячейки для вырезки.");
            return;
        }

        // 1. Копируем выделенные байты в буфер обмена
        copySelectedBytes();

        // 2. Спрашиваем пользователя, как удалить байты: с обнулением или со сдвигом
        Object[] options = {"Обнулить", "Сдвинуть", "Отмена"};
        int choice = JOptionPane.showOptionDialog(this,
                "Как удалить вырезанные байты?",
                "Выбор способа удаления",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        // 3. Удаляем байты в соответствии с выбором пользователя
        switch (choice) {
            case 0: // Обнулить
                deleteSelectedBytesWithZeroing();
                break;
            case 1: // Сдвинуть
                deleteSelectedBytesWithShift();
                break;
            default: // Отмена
                return;
        }

        // 4. Обновляем таблицу
        updateTableData();
    }

    private void pasteBytes() {
        int selectedRow = hexTable.getSelectedRow();
        int selectedColumn = hexTable.getSelectedColumn();

        if (selectedRow == -1 || selectedColumn <= 0) {
            JOptionPane.showMessageDialog(this, "Выделите ячейку для вставки.");
            return;
        }

        // 1. Получаем адрес для вставки
        int insertAddress;
        try {
            insertAddress = Integer.parseInt(hexTable.getModel().getValueAt(selectedRow, 0).toString(), 16) + selectedColumn - 1;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при вычислении адреса: " + e.getMessage());
            return;
        }

        // 2. Получаем данные из буфера обмена
        String data = "";
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            data = (String) clipboard.getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            JOptionPane.showMessageDialog(this, "Не удалось получить данные из буфера обмена: " + e.getMessage());
            return;
        }

        // 3. Преобразуем данные из буфера обмена в массив байтов
        String[] byteStrings = data.split("\\s+");
        byte[] dataToInsert = new byte[byteStrings.length];
        try {
            for (int i = 0; i < byteStrings.length; i++) {
                dataToInsert[i] = (byte) Integer.parseInt(byteStrings[i], 16);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Неверный формат байта в буфере обмена: " + e.getMessage());
            return;
        }

        // 4. Спрашиваем пользователя, как вставить байты: с заменой или без
        Object[] options = {"Заменить", "Вставить", "Отмена"};
        int choice = JOptionPane.showOptionDialog(this,
                "Как вставить байты из буфера обмена?",
                "Выбор способа вставки",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        // 5. Вставляем байты в соответствии с выбором пользователя
        switch (choice) {
            case 0: // Заменить
                insertBytesWithReplace(insertAddress, dataToInsert); // Используем перегруженный метод
                break;
            case 1: // Вставить
                insertBytesWithoutReplace(insertAddress, dataToInsert); // Используем перегруженный метод
                break;
            default: // Отмена
                return;
        }

        // 6. Обновляем таблицу
        updateTableData();
    }

    // Перегруженный метод insertBytesWithReplace для использования массива байтов
    private void insertBytesWithReplace(int insertAddress, byte[] dataToInsert) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(insertAddress);
            raf.write(dataToInsert);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при записи в файл: " + e.getMessage());
            return;
        }
    }

    // Перегруженный метод insertBytesWithoutReplace для использования массива байтов
    private void insertBytesWithoutReplace(int insertAddress, byte[] dataToInsert) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            long fileLength = raf.length();
            long insertLength = dataToInsert.length;

            // Сдвигаем байты с конца файла, чтобы не затереть данные
            for (long i = fileLength - 1; i >= insertAddress; i--) {
                raf.seek(i);
                byte b = raf.readByte();
                raf.seek(i + insertLength);
                if (i + insertLength < fileLength + insertLength) {
                    raf.writeByte(b);
                } else {
                    // Если выходим за пределы исходного файла, просто пропускаем запись
                }
            }

            // 4. Записываем данные в файл, начиная с insertAddress
            raf.seek(insertAddress);
            raf.write(dataToInsert);

            // 5. Увеличиваем длину файла на размер вставленных данных
            raf.setLength(fileLength + insertLength);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при вставке байтов: " + e.getMessage());
            return;
        }
    }

    private void updateTableData() {
        // 1. Очистить текущую модель таблицы
        DefaultTableModel model = (DefaultTableModel) hexTable.getModel();
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }

        // 2. Заново прочитать данные из файла
        int currentPage = getCurrentPage(); // Получаем номер текущей страницы
        DefaultTableModel newModel;
        try {
            newModel = transformIntoHEX.getHexPage(file, currentPage); // Читаем данные для текущей страницы
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при чтении файла: " + e.getMessage());
            return;
        }

        // 3. Заполнить таблицу новыми данными
        hexTable.setModel(newModel);
    }

    private int getCurrentPage() {
        try {
            int firstAddress = Integer.parseInt(hexTable.getModel().getValueAt(0, 0).toString(), 16);
            return firstAddress / PAGE_SIZE;
        } catch (Exception e) {
            return 0; // Если произошла ошибка, возвращаем 0 (первая страница)
        }
    }
}
