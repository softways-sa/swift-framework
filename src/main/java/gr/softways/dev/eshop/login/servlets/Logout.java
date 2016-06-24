package gr.softways.dev.eshop.login.servlets;

import java.io.*;
import java.util.*;

import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.poolmanager.AuthEmp;
import gr.softways.dev.jdbc.*;

public class Logout extends HttpServlet {

  private String _charset = null;
  private String _databaseId = null;
  
  private Director bean;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    _databaseId = SwissKnife.jndiLookup("swconf/databaseId");
    
    bean = Director.getInstance();
  }

  public void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
    doPost(request,response);
  }
  public void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String urlSuccess = request.getParameter("urlSuccess") != null ? request.getParameter("urlSuccess") : "/";
    
    HttpSession session = request.getSession();
    session.invalidate();

    response.sendRedirect(urlSuccess);
  }
}
