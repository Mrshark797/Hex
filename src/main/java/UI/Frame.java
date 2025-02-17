package UI;

import Model.TransformIntoHEX;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;
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
        jMenu = new JMenu("Файл");
        jMenuEdit = new JMenu("Редактирование");
        fileOpen = new JMenuItem("Открыть");
        fileSaveAs = new JMenuItem("Сохранить как");
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
        jMenu.add(fileSaveAs);
        jMenuEdit.add(deleteWithZeroingMenuItem);
        jMenuEdit.add(putWithChange);
        jMenuEdit.add(deleteWithShift);
        jMenuEdit.add(insertWithoutReplace);
        jMenuEdit.add(copySelectedBytes);
        jMenuEdit.add(cutBytes);
        jMenuEdit.add(pasteBytes);
        setJMenuBar(jMenuBar);
        //
        panelForTable = new JPanel(new BorderLayout());
        panelForTable.setBackground(Color.GRAY);
        panelForButtons = new JPanel(new FlowLayout());
        panelForButtons.setBackground(Color.GREEN);

        JButton prevButton = new JButton("Назад");
        JButton nextButton = new JButton("Далее");
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
        dataValueLabel = new JLabel("Значение: ");
        panelForButtons.add(dataValueLabel);
        searchTextField = new JTextField(20);
        searchButton = new JButton("Поиск");
        resultLabel = new JLabel("Результат: ");
        exactMatchRadioButton = new JRadioButton("Полн. совп.");
        maskMatchRadioButton = new JRadioButton("По маске");
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
        hexTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    displaySelectedData();
                }
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

    private void openFile() {
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            showPage(0);
        }
    }

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

    private void displaySelectedData() {
        int selectedRow = hexTable.getSelectedRow();
        int selectedCol = hexTable.getSelectedColumn();

        if (selectedRow == -1 || selectedCol <= 0) {
            dataValueLabel.setText("Value: -");
            return;
        }

        TableModel model = hexTable.getModel();
        try {
            int address = Integer.parseInt(model.getValueAt(selectedRow, 0).toString(), 16);
            int offset = address + selectedCol - 1;

            // Получаем выбранный тип данных
            String selectedType = (String) dataTypeComboBox.getSelectedItem();

            String valueString = ""; // Строка для отображения значений

            // Сначала добавляем значение для выбранного типа данных (2, 4, или 8 байт)
            if (selectedType != null && !selectedType.isEmpty()) {
                byte[] data = getBytesFromOffset(offset);
                if (data == null) {
                    valueString = "Not enough data for " + selectedType + ", ";
                } else {
                    Object interpretedValue = interpretData(data, selectedType);
                    if (interpretedValue != null) {
                        valueString = selectedType + ": " + interpretedValue + ", ";
                    } else {
                        valueString = "Error interpreting " + selectedType + ", ";
                    }
                }
            }

            // Теперь добавляем десятичные значения со знаком и без знака (для 1 байта)
            byte selectedByte = getByteFromTable(selectedRow, selectedCol);
            int signedDecimal = (int) selectedByte;
            int unsignedDecimal = selectedByte & 0xFF;

            valueString += "Value (signed): " + signedDecimal + ", Value (unsigned): " + unsignedDecimal;

            dataValueLabel.setText(valueString);

        } catch (NumberFormatException e) {
            dataValueLabel.setText("Value: Error");
            e.printStackTrace();
        } catch (Exception e) {
            dataValueLabel.setText("Value: General Error");
            e.printStackTrace();
        }
    }

    private byte getByteFromTable(int row, int col) {
        TableModel model = hexTable.getModel();
        String hexValue = (String) model.getValueAt(row, col);
        return (byte) Integer.parseInt(hexValue, 16);
    }

    private void showPage(int pageNumber) {
        try {
            DefaultTableModel model = transformIntoHEX.getHexPage(file, pageNumber);
            if (model == null) {
                JOptionPane.showMessageDialog(this, "Страница не найдена!");
                return;
            }
            currentPage = pageNumber;
            pageTextField.setText(String.valueOf(currentPage + 1)); // Номер страницы с 1
            if (hexTable == null) {
                hexTable = new JTable(model);
                addTableModelListener(hexTable);
                panelForTable.add(new JScrollPane(hexTable), BorderLayout.CENTER);
                panelForTable.revalidate();
                panelForTable.repaint();
            } else {
                hexTable.setModel(model);
                hexTable.repaint();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки страницы: " + ex.getMessage());
        }
    }

    private void addTableModelListener(JTable table) {
        table.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int col = e.getColumn();
                    String newValue = (String) hexTable.getModel().getValueAt(row, col);
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
        try {
            if (col > 0) {
                int address = Integer.parseInt(hexTable.getModel().getValueAt(row, 0).toString(), 16) + col -1;
                byte[] bytes = transformIntoHEX.hexToBytes(newValue);
                if (bytes.length > 0) {
                    System.arraycopy(bytes, 0, transformIntoHEX.pageBuffer, address - (currentPage * pageSize), bytes.length);
                }
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка обновления буфера: " + ex.getMessage());
        }
    }
}
