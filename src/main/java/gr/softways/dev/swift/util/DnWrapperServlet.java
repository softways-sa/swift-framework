package gr.softways.dev.swift.util;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;

import gr.softways.dev.util.*;

/**
 *
 * @author Panos
 */
public class DnWrapperServlet extends HttpServlet {
  private String _databaseId = null;

  private String _loginPage = "index.jsp";
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _databaseId = SwissKnife.jndiLookup("swconf/databaseId");
    
    if (getInitParameter("loginPage") != null) {
      _loginPage = getInitParameter("loginPage");
    }
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    int auth = -999;
    
    HttpSession session = null;
    
    //System.out.println("getPathTranslated() = " + request.getPathTranslated());
    
    if (request.getPathTranslated() == null) {
      response.sendRedirect(request.getRequestURI() + "/");
      //System.out.println("path = null");
      return;
    }
    
    String serverName = request.getServerName();
    
    session = request.getSession(false);
    
    if (session != null) {
      if (session.getAttribute(_databaseId + ".authGrantLogin") != null) auth = Director.AUTH_OK;
      else if (session.getAttribute(_databaseId + ".isAuthenticatedUser") != null && session.getAttribute(_databaseId + ".isAuthenticatedUser").toString().equals("true")) auth = Director.AUTH_OK;
    }
    
    if (auth == Director.AUTH_OK) {
      String filename = getFullFilename(request);

      //if (filename.indexOf(".doc") != -1 || filename.indexOf(".DOC") != -1) response.setContentType("application/msword");
      //else if (filename.indexOf(".pdf") != -1 || filename.indexOf(".PDF") != -1) response.setContentType("application/pdf");
      
      BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
      javax.servlet.ServletOutputStream out = response.getOutputStream();

      byte abyte0[] = new byte[2048];
      int l;
      try {
        while((l = in.read(abyte0, 0, abyte0.length)) != -1)
          out.write(abyte0, 0, l);
      }
      finally {
        in.close();
        out.flush();
        out.close();
      }
    }
    else {
      response.sendRedirect("http://" + serverName + "/" + _loginPage);
    }
  }

  /**
   * Return full filename (path + filename)
   * Default page is index.html
   * (eg. 'd:/inetpub/wwwroot/wwwstats/index.html')
   *
   */
  private String getFullFilename(HttpServletRequest request) {
    StringBuffer fullFilename = new StringBuffer();

    String pathTranslated = request.getPathTranslated();
    if (!pathTranslated.endsWith(File.separator))
      pathTranslated = pathTranslated + File.separator;

    String servletName = request.getServletPath();
    servletName = servletName.substring(1);

    String pathInfo = request.getPathInfo();
    if (pathInfo == null) pathInfo = "/";
    pathInfo = replace(pathInfo, "/", File.separator);

    int i = pathTranslated.indexOf(pathInfo);
    if (i == -1 || pathInfo.equals(File.separator))
      fullFilename.append(pathTranslated);
    else
      fullFilename.append(pathTranslated.substring(0, i+1));

    fullFilename.append(servletName);
    //if (pathInfo.equals(File.separator)) {
      //pathInfo = File.separator + _welcomePage;
    //}
    fullFilename.append(pathInfo);

    return fullFilename.toString();
  }

  /**
   * Replace in s, all s1 to s2
   *
   */
  private String replace(String s, String s1, String s2) {
    int i = 0;
    StringBuffer stringbuffer = new StringBuffer();
    String s3 = s.toUpperCase();
    String s4 = s1.toUpperCase();
    int k;
    while((k = s3.indexOf(s4, i)) != -1) {
      stringbuffer.append(s.substring(i, k));
      stringbuffer.append(s2);
      i = k + s4.length();
    }
    stringbuffer.append(s.substring(i));
    return stringbuffer.toString();
  }
  
  //Get Servlet information
  public String getServletInfo() {
    return "";
  }
}