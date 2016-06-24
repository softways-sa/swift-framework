package gr.softways.dev.poolmanager;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;

public class AppPoolServlet extends HttpServlet implements ACLInterface {

  //Initialize global variables
  AppPoolManager poolManager;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    // properties filename
    String propsName = getInitParameter("propsName");

    //System.out.println("[" + new java.util.Date() + "] AppPoolServlet : propsName = " + propsName);
    
    if (propsName != null && propsName.length()>0) {
      poolManager = AppPoolManager.getInstance(propsName);
    }
    else {
      poolManager = AppPoolManager.getInstance();
    }
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    doGet(request, response);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    String username = "", password = "",
           urlSuccess = request.getParameter("urlSuccess") == null ? "" : request.getParameter("urlSuccess").trim(),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure").trim(),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess").trim(),
           databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId").trim();

    int status = 0;

    String restartConn = request.getParameter("restartConn") == null ? "" : request.getParameter("restartConn"),
           restartACL = request.getParameter("restartACL") == null ? "" : request.getParameter("restartACL");

    if (!restartACL.equals("")) {
      if (databaseId.length()==0) {
        databaseId = restartACL;
      }
      username = getAuth(databaseId,"authUsername",request);
      password = getAuth(databaseId,"authPassword",request);

      status = poolManager.auth(databaseId,username,password,
                                "poolManagerACL",AUTH_UPDATE);

      if (status == AUTH_OK) {
        poolManager.restartACL(restartACL);
        response.sendRedirect(urlSuccess);
      }
      else {
        response.sendRedirect(urlNoAccess);
      }
    }

    if (!restartConn.equals("")) {
      if (databaseId.length()==0) {
        databaseId = restartConn;
      }
      username = getAuth(databaseId,"authUsername",request);
      password = getAuth(databaseId,"authPassword",request);

      status = poolManager.auth(databaseId,username,password,
                                "poolManagerConn",AUTH_UPDATE);

      if (status == AUTH_OK) {
        poolManager.restartConn(restartConn);
        response.sendRedirect(urlSuccess);
      }
      else {
        response.sendRedirect(urlNoAccess);
      }
    }
  }

  private String getAuth(String databaseId, String name,
                         HttpServletRequest request) {
    String value = "";

    HttpSession session = request.getSession();

    if (session != null) {
      value = session.getAttribute(databaseId + "." + name).toString();
    }

    return value;
  }
  
  public void destroy() {
    Enumeration connectionsPool = poolManager.getConnPool().elements();
    
    AppConnPool pool = null;
    
    while (connectionsPool.hasMoreElements()) {
      pool = (AppConnPool) connectionsPool.nextElement();
      
      try {
        System.out.println(new java.util.Date().toString() + ": Closing connections from " + pool.getName());
      
        pool.release();      
      }
      catch (Exception e) {
        System.out.println("A pool did not release connections.....");
        
        e.printStackTrace();
      }
    }
    
    connectionsPool = null;
  }
  
}