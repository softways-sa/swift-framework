package gr.softways.dev.eshop.filetemplate.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class doUpload extends HttpServlet {

  private Director bean;

  private String _charset = null;
  
  private String _uploadPropertiesFilename = null;
  
  Properties parameters = new Properties();
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    _uploadPropertiesFilename = SwissKnife.jndiLookup("upload/properties");
    
    bean = Director.getInstance();
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response)
              throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String uploadPath = null, urlSuccess = null, urlNoAccess = null,
           urlFailure = null, filename = null, databaseId = null;

    boolean problem = false;
    
    boolean okren = true;
          
    try {
      parameters.load( new FileInputStream( _uploadPropertiesFilename ) );
      
      uploadPath = parameters.getProperty("uploadTransientPath", "/");
      
      MultiRequest multi = new MultiRequest(request, uploadPath, 
                                            14 * 1024 * 1024,_charset);

      databaseId = multi.getParameter("databaseId");
      urlNoAccess = multi.getParameter("urlNoAccess");
      uploadPath = multi.getParameter("uploadPath");
      urlSuccess = multi.getParameter("urlSuccess");
      urlFailure = multi.getParameter("urlFailure");
      filename = SwissKnife.grEncode(multi.getParameter("FTemFilename"));

      String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                      request),
             authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                      request);

      int auth = bean.auth(databaseId, authUsername, authPassword,
                           "fileTemplateUpload", Director.AUTH_UPDATE);

      if (auth != Director.AUTH_OK) {
        response.sendRedirect(urlNoAccess);
        return;
      }

      Enumeration myFiles = multi.getFileNames();

      String fname = (String)myFiles.nextElement();

      String oldFilename = multi.getFilesystemName(fname);

      if (oldFilename == null) {
        response.sendRedirect(urlFailure);
        return;
      }

      File oldF = multi.getFile(fname);
      File nf = new File(uploadPath,filename);
      nf.delete();

      while ( !(okren = oldF.renameTo(nf)));
    }
    catch (Exception e) {
      problem = true;
      e.printStackTrace();
    }

    if (problem==true) response.sendRedirect(urlFailure);
    else response.sendRedirect(urlSuccess);
  }
}