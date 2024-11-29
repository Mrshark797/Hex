package UI;

import Model.TransformIntoHEX;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class Frame extends JFrame{
    private JMenuBar jMenuBar;
    private JMenu jMenu;
    private JMenuItem fileOpen;
    private JMenuItem fileSave;
    private JPanel contentPane;
    private JPanel panelForTable;
    private JPanel panelForButtons;
    private JFileChooser fileChooser;

    public void createFrame(){

        jMenuBar = new JMenuBar();
        jMenu = new JMenu("File");
        fileOpen = new JMenuItem("Open");
        fileSave = new JMenuItem("Save");
        setJMenuBar(jMenuBar);
        jMenuBar.add(jMenu);
        jMenu.add(fileOpen);
        jMenu.add(fileSave);

        panelForTable = new JPanel();
        panelForTable.setBackground(Color.GRAY);

        JTable jTable = new JTable();
        DefaultTableModel md = new DefaultTableModel();
        jTable.setModel(md);


        panelForButtons = new JPanel();
        panelForButtons.setBackground(Color.GREEN);

        contentPane = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.5;
        constraints.weighty = 1;

        constraints.gridx = 0;
        constraints.gridy = 0;
        contentPane.add(panelForTable, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        contentPane.add(panelForButtons, constraints);

        add(contentPane);


        JSplitPane jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelForTable, panelForButtons);
        jSplitPane.setDividerLocation(850);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(jSplitPane, BorderLayout.CENTER);

        fileOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser = new JFileChooser();
                int ret = fileChooser.showOpenDialog(null);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    TransformIntoHEX transformIntoHEX = new TransformIntoHEX();
                    JTable table = transformIntoHEX.getFileInHEX(file); //  Получаем  JTable  из  getFileInHEX
                    JScrollPane jsc = new JScrollPane(table);
                    jsc.setPreferredSize(new Dimension(800, 600));

                    //  Добавляем  JTable  в  panelForTable
                    panelForTable.removeAll(); //  Очищаем  panelForTable
                    panelForTable.add(jsc);
                    panelForTable.revalidate();
                    panelForTable.repaint();
                }
            }
        });




        setSize(1200, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }



}
