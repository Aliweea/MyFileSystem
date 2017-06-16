package FileSystem;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyFile {
    private File myFile;
	private String blockName;		//文件所在盘区
    private FCB fcb = new FCB();

    public MyFile(File myFile, String blokName, double capacity){
        this.myFile = myFile;
        fcb.fileName = myFile.getName();
        fcb.isDir = myFile.isDirectory();
        fcb.filePath = myFile.getPath();
        fcb.capacity = capacity;
        this.blockName = blokName;
        
        String ctime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
        fcb.createTime = ctime;
        fcb.lastUpdateTime = ctime;
        
    }

    public String getFileName(){
        return fcb.fileName;
    }

    public String getFilePath(){
        return fcb.filePath;
    }

    public boolean renameFile(String name){
        String parentPath = myFile.getParent();
        File mm = new File(parentPath + File.separator + name);
        if (myFile.renameTo(mm)){
            myFile = mm;
            fcb.fileName = name;
            return true;
        }else{
            return false;
        }
    }

    public File getMyFile(){
        return myFile;
    }

    public String getBlockName() {
        return blockName;
    }

    public double getCapacity() {
        return fcb.capacity;
    }
    
    
    public String getCreateTime(){
    	return fcb.createTime;
    }
    
    public String getLastUpdateTime(){
    	return fcb.lastUpdateTime;
    }
    
    public void setLastUpdateTime(String time){
    	fcb.lastUpdateTime = time;
    }

    @Override
    public String toString(){
        return fcb.fileName;
    }
    
    
    class FCB{
    	public String fileName;			//文件名
    	public boolean isDir;			//文件类型
    	public String filePath;			//文件地址
    	public double capacity;			//文件大小
    	public String createTime;		//创建时间
    	public String lastUpdateTime;	//最近修改时间
    }
}



