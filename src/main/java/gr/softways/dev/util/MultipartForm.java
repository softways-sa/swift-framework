package gr.softways.dev.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.ArrayUtils;

public class MultipartForm {
  
  public MultipartForm(HttpServletRequest request, String tempDirectory, String charset) throws IOException {
    new MultipartForm(request, tempDirectory, DEFAULT_SIZE_MAX, DEFAULT_FILE_SIZE_MAX, charset);
  }
  
  public String getParameter(String paramName) {
    String s = null;
    
    if (multipartParameters.containsKey(paramName)) s = multipartParameters.get(paramName)[0];
    
    return s;
  }
  
  public String[] getParameterValues(String paramName) {
    return multipartParameters.get(paramName);
  }
  
  public File writeFile(String key, String fullfilename) {
    File file = null;
    
    FileItem item = multipartFiles.get(key);
    
    if (item != null && item.getName().length() > 0) {
      try {
        file = new File(fullfilename);
        
        item.write(file);
      }
      catch (Exception e) {
        file = null;
        
        e.printStackTrace();
      }
    }
    
    return file;
  }
  
  public String getFileName(String key) {
    String filename = null;
    
    FileItem item = multipartFiles.get(key);
    
    if (item != null) {
      filename = item.getName();
    }
    
    return filename;
  }
  
  public MultipartForm(HttpServletRequest request, String tempDirectory,
      int sizeMax, int fileSizeMax, String charset) throws IOException {
    // Create a factory for disk-based file items
    FileItemFactory factory = new DiskFileItemFactory();

    // Create a new file upload handler
    ServletFileUpload upload = new ServletFileUpload(factory);
    upload.setSizeMax(sizeMax);
    upload.setFileSizeMax(fileSizeMax);
    
    List<FileItem> items = null;
    
    try {
      // Parse the request
      items = upload.parseRequest(request);
    }
    catch (Exception e) {
      throw new IOException();
    }

    multipartParameters = new HashMap<String, String[]>();
    multipartFiles = new HashMap<String, FileItem>();
  
    for (FileItem item : items) {
      if (item.isFormField()) {
        String value = null;
        
        try {
          value = item.getString(charset);
        }
        catch (Exception e) {
          continue;
        }
        
        String[] curParam = multipartParameters.get(item.getFieldName());
				if (curParam == null) {
					// simple form field
					multipartParameters.put(item.getFieldName(), new String[] {value});
				}
				else {
					// array of simple form fields
					String[] newParam = ArrayUtils.add(curParam, value);
					multipartParameters.put(item.getFieldName(), newParam);
				}
      }
      else {
        multipartFiles.put(item.getFieldName(), item);
      }
    }
  }
  
  private static final int DEFAULT_SIZE_MAX = 5 * 1024 * 1024;
  private static final int DEFAULT_FILE_SIZE_MAX = 5 * 1024 * 1024;
  
  private HashMap<String, String[]> multipartParameters = null;
  private HashMap<String, FileItem> multipartFiles = null;
}