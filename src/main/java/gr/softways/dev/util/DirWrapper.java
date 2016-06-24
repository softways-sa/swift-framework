package gr.softways.dev.util;

import javax.servlet.http.*;
import javax.servlet.*;

import java.io.*;
import java.util.*;
import java.sql.*;

import gr.softways.dev.poolmanager.*;
import gr.softways.dev.jdbc.*;

public class DirWrapper extends HttpServlet {
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    director = Director.getInstance();
    
    _databaseId = getInitParameter("databaseId");
    
    if (getInitParameter("welcomePage") != null) {
      _welcomePage = getInitParameter("welcomePage");
    }
    
    if (getInitParameter("loginPage") != null) {
      _loginPage = getInitParameter("loginPage");
    }
    
    if (getInitParameter("username") != null) {
      _username = getInitParameter("username");
    }
    if (getInitParameter("password") != null) {
      _password = getInitParameter("password");
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
    
    session = request.getSession();

    String username = request.getParameter("username") != null ? request.getParameter("username") : "",
           password = request.getParameter("password") != null ? request.getParameter("password") : "";

    if (username.length()>0 || password.length()>0) {
      if (_username != null && _password != null) {
        validateUser(username, password, session);
      }
      else {
        validateDBUser(username, password, session);
      }
    }
    
    username = (String)session.getAttribute(_databaseId + ".authUsername");
    password = (String)session.getAttribute(_databaseId + ".authPassword");

    if (username == null) username = "";
    if (password == null) password = "";
    
    if (_username != null && _password != null) {
      auth = auth(username,password);
    }
    else {
      auth = director.auth(_databaseId,username,password,serverName,Director.AUTH_READ);
    }
    
    if (auth == Director.AUTH_OK) {
      response.setContentType("text/html");
      
      String filename = getFullFilename(request);

      //System.out.println("Request document = " + filename);
      
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
    if (pathInfo.equals(File.separator)) {
      pathInfo = File.separator + _welcomePage;
    }
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

  private void validateDBUser(String username, String password, HttpSession session) {
    Database database;
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    String query = "SELECT * FROM users WHERE" 
                 + " usrName = '" + SwissKnife.sqlEncode(username) + "'"
                 + " AND usrPasswd = '" + SwissKnife.sqlEncode(password) + "'";
    
    database = director.getDBConnection(_databaseId);

    //DbRet dbRet = null;

    //dbRet = database.beginTransaction(Database.TRANS_ISO_1);

    //int prevTransIsolation = dbRet.getRetInt();
    
    try {
      queryDataSet.setQuery(new QueryDescriptor(database, query, null, true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();

      if (queryDataSet.getRowCount() >= 1) {
        // username & password pair valid
        AuthEmp authEmp = new AuthEmp(username,password,queryDataSet.getInt("usrAccessLevel"),_databaseId);

        // πρόσθεσε στο hashtable τον χρήστη
        director.addAuthUser(_databaseId, authEmp);
        
        session.setAttribute(_databaseId + ".unbindObject", authEmp);
        
        // το username & password που θα συνοδεύει τον χρήστη
        // κατα την διάρκεια του session του
        session.setAttribute(_databaseId + ".authUsername",username);
        session.setAttribute(_databaseId + ".authPassword",password);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    //database.commitTransaction(1, prevTransIsolation);

    director.freeDBConnection(_databaseId, database);
  }
  
  private void validateUser(String username, String password, HttpSession session) {
    if (_username.equals(username) && _password.equals(password)) {
      // username & password pair valid
      // το username & password που θα συνοδεύει τον χρήστη
      // κατα την διάρκεια του session του
      session.setAttribute(_databaseId + ".authUsername",username);
      session.setAttribute(_databaseId + ".authPassword",password);
    }
  }
  
  private int auth(String username, String password) {
    if (_username.equals(username) && _password.equals(password)) return Director.AUTH_OK;
    else return Director.AUTH_NOACCESS;
  }
  
  private Director director;
  
  private String _databaseId = null;

  private String _welcomePage = "index.html";
  
  private String _loginPage = "statslogin.html";
  
  private String _username = null;
  private String _password = null;
}