package gr.softways.dev.util;

import java.io.*;

public class ListDir {
  
  /** Creates a new instance of ListDir */
  public ListDir() {
  }
  
  public String[] getFileNames(String dirName) {
    String [] dirFileList = null;
    
    File dir = new File(dirName);
    
    if (!dir.exists()) {
      return dirFileList;
    }
    
    if (!dir.isDirectory()) {
      return dirFileList;
    }
    
    dirFileList = dir.list();
    if (dirFileList != null && dirFileList.length > 0) sort(dirFileList);

    return dirFileList;
  }

  public boolean deleteFile(String fileName) {
    File f = new File(fileName);
    
    boolean deleted = false;
    
    if (f.isDirectory() == false) deleted = f.delete();
    
    return deleted;
  }
  
  public void sort (String[] tableName) {
    int min, length = tableName.length;
    
    String tmp;

    for (int i = 0; i < length - 1; i ++){
      min = i;
      for (int pos = i + 1; pos < length; pos ++)
        if (tableName[pos].compareTo(tableName[min]) < 0) min = pos;
      tmp = tableName[min];
      tableName[min] = tableName[i];
      tableName[i] = tmp;
    }
  }
}
