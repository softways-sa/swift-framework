package gr.softways.dev.eshop.uploadfiles.servlets;

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
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String uploadPath = null, urlSuccess = null, urlNoAccess = null,
           urlFailure = null, filename = null, databaseId = null,
           tmpDeny = null;
    
    StrTokenizer denyFilesExtTokenizer = null;

    boolean problem = false;
    
    try {
      parameters.load( new FileInputStream( _uploadPropertiesFilename ) );
      
      uploadPath = parameters.getProperty("uploadTransientPath");
      if (uploadPath == null || uploadPath.length() == 0) {
        throw new Exception("Temporary upload path not found.");
      }
      
      tmpDeny = parameters.getProperty("uploadDenyFilesExt");
      if (tmpDeny != null && tmpDeny.length()>0) {
        denyFilesExtTokenizer = new StrTokenizer(tmpDeny, '|');
      }
      
      // 100 MB limit!!!!
      MultiRequest multi = new MultiRequest(request,uploadPath,100 * 1024 * 1024,_charset);

      databaseId = multi.getParameter("databaseId");
      urlNoAccess = multi.getParameter("urlNoAccess");
      urlSuccess = multi.getParameter("urlSuccess");
      urlFailure = multi.getParameter("urlFailure");

      String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
             authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

      int auth = bean.auth(databaseId,authUsername,authPassword,"uploadFiles",Director.AUTH_UPDATE);

      if (auth != Director.AUTH_OK) {
        response.sendRedirect(urlNoAccess);
        return;
      }

      int filesCount = Integer.parseInt(multi.getParameter("filesCount"));
      
      boolean overwrite = true;
      if (multi.getParameter("overwrite") != null && multi.getParameter("overwrite").equals("false")) overwrite = false;
      
      boolean lowerCase = true;
      if (multi.getParameter("lowerCase") != null && multi.getParameter("lowerCase").equals("false")) lowerCase = false;

      for (int i=1; i<=filesCount; i++) {
        uploadPath = multi.getParameter("uploadPath" + i);

        if (uploadPath != null && uploadPath.length()>0) {
          MultiRequest.uploadFile(multi,"file" + i,denyFilesExtTokenizer,uploadPath,overwrite,lowerCase);
        }
      }
    }
    catch (Exception e) {
      problem = true;
      e.printStackTrace();
    }

    if (problem==true) response.sendRedirect(urlFailure);
    else response.sendRedirect(urlSuccess);
  }
}