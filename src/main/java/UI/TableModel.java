package UI;

import java.util.List;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

public class TableModel extends AbstractTableModel {

    private List<String> data = new ArrayList<>();
    @Override
    public int getColumnCount(){
        return 16;
    }

    @Override
    public int getRowCount(){
        return data.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data[rowIndex][columnIndex];
    }

    @Override
    public boolean isCellEditable(int row, int col){
        return true;
    }
}
