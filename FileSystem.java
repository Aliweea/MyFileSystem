package FileSystem;


import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class FileSystem{
	
    
    // Delete a file or a dir
    public static void deleteDirectory(String filePath){
        File file = new File(filePath);
        
        if(!file.exists()){
            return;
        }
        
        if(file.isFile()){
            file.delete();
        }else if(file.isDirectory()){
            File[] files = file.listFiles();
            for (File myfile : files) {
                deleteDirectory(filePath + File.separator + myfile.getName());
            }
            file.delete();
        }
    }
    

}