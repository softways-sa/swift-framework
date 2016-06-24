package gr.softways.dev.eshop.batchimport;

import gr.softways.dev.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author  minotauros
 */
public class ProductManagerServlet extends HttpServlet {

  private int _status = 0;
  private String _textMsg = "";
  
  private int _maxUploadSize = 14 * 1024 * 1024;
  
  private String _uploadPropertiesFilename = null;
  
  private String _uploadTransientPath = null;
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    
    _uploadPropertiesFilename = SwissKnife.jndiLookup("upload/properties");
    
    try {
      Properties parameters = new Properties();
      
      parameters.load( new FileInputStream( _uploadPropertiesFilename ) );
             
      _uploadTransientPath = parameters.getProperty("uploadTransientPath", "");
      if (_uploadTransientPath.equals("")) throw new Exception("uploadTransientPath not set!!!");
      
      parameters = null;
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new ServletException();
    }
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    DbRet dbRet = new DbRet();
    
    MultiRequest multi = null;
    
    multi = new MultiRequest(request,_uploadTransientPath,_maxUploadSize,"UTF-8");
    
    String action = multi.getParameter("action1"),
           databaseId = multi.getParameter("databaseId"),
           urlReturn = multi.getParameter("urlReturn");
    
    if (action.equals("BATCH_UPDATE_PRODUCT") || action.equals("BATCH_UPDATE_PRICELIST")) {
      dbRet = doProcessExcel(request, databaseId, multi, action);
    }
    else if (action.equals("QUERY_STATUS")) {
    }
    else {
      dbRet.setNoError(0);
    }
    
    response.sendRedirect(urlReturn + "?status=" + _status + "&textMsg=" + _textMsg);
  }
  
  public void setStatus(int status) {
    _status = status;
  }
  
  public void setTextMsg(String textMsg) {
    _textMsg = textMsg;
  }
  
  private synchronized DbRet doProcessExcel(HttpServletRequest request,String databaseId,MultiRequest multi,String action) {
    DbRet dbRet = new DbRet();
    
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);
           
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"product",Director.AUTH_INSERT);
    
    File inputFile = null;
    
    try {
      inputFile = multi.getFile( (String)(multi.getFileNames().nextElement()) );
    }
    catch (Exception e) {
      dbRet.setNoError(0);
    }
    
    if (dbRet.getNoError() == 0) {
      setTextMsg(SwissKnife.currentDate() + ": Command canceled because there was some problem uploading file.");
    }
    else if (_status != 0) {
      dbRet.setNoError(0);
      
      setTextMsg(SwissKnife.currentDate() + ": Command canceled because another operation already in progress.");
      
      inputFile.delete();
      inputFile = null;
    }
    else if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
    
      setTextMsg(SwissKnife.currentDate() + ": Command canceled because of lower privileges.");
      
      inputFile.delete();
      inputFile = null;
    }
    else {
      if (action.equals("BATCH_UPDATE_PRODUCT")) {
        setStatus(1);
      }
      else if (action.equals("BATCH_UPDATE_PRICELIST")) {
        setStatus(2);
      }
      setTextMsg("");
      
      ProductWorkerThread productWorkerThread = new ProductWorkerThread(this,action,inputFile,databaseId);
      
      productWorkerThread.start();
    }
    
    return dbRet;
  }
}