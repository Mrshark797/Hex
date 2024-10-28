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

        fileOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser = new JFileChooser();
                int ret = fileChooser.showOpenDialog(null);
                if(ret == JFileChooser.APPROVE_OPTION){
                    File file = fileChooser.getSelectedFile();
                    TransformIntoHEX transformIntoHEX = new TransformIntoHEX();
                    transformIntoHEX.getFileInHEX(file);
                }
            }
        });

        jMenu.add(fileSave);

        panelForTable = new JPanel();
        panelForTable.setBackground(Color.GRAY);

        String[] columnsHead = new String[] {"00", "01", "02", "03"};
        String[][] data = new String[][] {
                {"00", "AA", "BB"},
                {"01", "AB", "C1"},
        };

        DefaultTableModel model = new DefaultTableModel(data, columnsHead);
        JTable jTable = new JTable(model);
        panelForTable.add(jTable);

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
        jSplitPane.setDividerLocation(650);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(jSplitPane, BorderLayout.CENTER);

        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }



}
