package UI;

import Model.TransformIntoHEX;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;


public class Frame extends JFrame {
    private JMenuBar jMenuBar;
    private JMenu jMenu;
    private JMenuItem fileOpen;
    private JMenuItem fileSave;
    private JPanel contentPane;
    private JPanel panelForTable;
    private JPanel panelForButtons;
    private JFileChooser fileChooser = new JFileChooser();
    private TransformIntoHEX transformIntoHEX = new TransformIntoHEX();
    private File file;
    private JTable hexTable;
    private int currentPage = 0;
    private int pageSize = 256;
    private JTextField pageTextField;


    public void createFrame() {
        // Создание меню - без изменений
        jMenuBar = new JMenuBar();
        jMenu = new JMenu("File");
        fileOpen = new JMenuItem("Open");
        fileSave = new JMenuItem("Save");
        jMenuBar.add(jMenu);
        jMenu.add(fileOpen);
        jMenu.add(fileSave);
        setJMenuBar(jMenuBar);

        panelForTable = new JPanel(new BorderLayout()); // Используем BorderLayout
        panelForTable.setBackground(Color.GRAY);

        panelForButtons = new JPanel(new FlowLayout()); // Используем FlowLayout
        panelForButtons.setBackground(Color.GREEN);

        JButton prevButton = new JButton("Previous");
        JButton nextButton = new JButton("Next");
        pageTextField = new JTextField(5); // Поле для ввода номера страницы
        panelForButtons.add(prevButton);
        panelForButtons.add(nextButton);
        panelForButtons.add(pageTextField);

        contentPane = new JPanel(new BorderLayout()); // Используем BorderLayout
        contentPane.add(panelForTable, BorderLayout.CENTER);
        contentPane.add(panelForButtons, BorderLayout.SOUTH); // Кнопки снизу
        add(contentPane);


        fileOpen.addActionListener(e -> openFile());
        //fileSave.addActionListener(e -> saveFile()); // Сохранение пока не реализовано

        prevButton.addActionListener(e -> showPage(currentPage - 1));
        nextButton.addActionListener(e -> showPage(currentPage + 1));
        pageTextField.addActionListener(e -> {
            try {
                int newPage = Integer.parseInt(pageTextField.getText()) - 1;
                showPage(newPage);
            } catch (NumberFormatException ex) {JOptionPane.showMessageDialog(this, "Неверный номер страницы!");
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
