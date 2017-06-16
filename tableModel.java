package FileSystem;
import javax.swing.table.AbstractTableModel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;

/**
 * Created by hongjiayong on 16/6/7.
 */
public class tableModel extends AbstractTableModel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Object> content = null;
    private String[] title_name = { "文件名", "文件地址", "文件类型", "文件大小/KB", "最近修改时间"};

    public tableModel(){
        content = new ArrayList<Object>();
    }

    public void addRow(MyFile myFile){
        ArrayList<Object> v = new ArrayList<Object>();
        DecimalFormat format=new DecimalFormat("#0.00");
        v.add(0, myFile.getFileName());
        v.add(1,myFile.getFilePath());
        if (myFile.getMyFile().isFile()){
            v.add(2, "File");
            v.add(3, format.format(myFile.getCapacity()));
        }else {
            v.add(2, "Directory");
            v.add(3, "-");
        }
        long time = myFile.getMyFile().lastModified();
        String ctime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(time));
        v.add(4, ctime);
        content.add(v);
    }

    @SuppressWarnings("unchecked")
	public void removeRow(String name) {
        for (int i = 0; i < content.size(); i++){
            if (((ArrayList<Object>) content.get(i)).get(1).equals(name)){
                content.remove(i);
                break;
            }
        }
    }

 
    public void removeRows(int row, int count){
        for (int i = row+count-1; i >= row; i--){
                content.remove(i);
        }
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int colIndex){
        ((ArrayList<Object>) content.get(rowIndex)).remove(colIndex);
        ((ArrayList<Object>) content.get(rowIndex)).add(colIndex, value);
        this.fireTableCellUpdated(rowIndex, colIndex);
    }

    public String getColumnName(int col) {
        return title_name[col];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex){
        return false;
    }

    @Override
    public int getRowCount() {
        return content.size();
    }

    @Override
    public int getColumnCount() {
        return title_name.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return ((ArrayList<Object>) content.get(rowIndex)).get(columnIndex);
    }
}