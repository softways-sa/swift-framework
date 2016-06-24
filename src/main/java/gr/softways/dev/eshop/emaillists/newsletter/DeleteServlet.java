package gr.softways.dev.eshop.emaillists.newsletter;

import java.io.*;
import java.util.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class DeleteServlet extends HttpServlet {

  private Director _director;
  
  private String _charset = null;
  
  private String _databaseId = null;
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    _databaseId = SwissKnife.jndiLookup("swconf/databaseId");
    
    _director = Director.getInstance();
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    DbRet dbRet = new DbRet();
    
    String urlSuccess = request.getParameter("urlSuccess") == null ? "" :  request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");
    
    String authUsername = SwissKnife.getSessionAttr(_databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(_databaseId + ".authPassword",request);

    int auth = _director.auth(_databaseId, authUsername, authPassword,
        "Newsletter", Director.AUTH_DELETE);
    
    if (auth != Director.AUTH_OK) dbRet.setNoError(0);
    else dbRet = doDelete(request);
    
    if (dbRet.getNoError() == 1) {
       response.sendRedirect(urlSuccess);
    }
    else {
      if (dbRet.getAuthError() == 1) {
        response.sendRedirect(urlNoAccess + "?authError=" + dbRet.getAuthErrorCode());
      }
      else if (dbRet.get_validError() == 1) {
        response.sendRedirect(urlFailure + "?validField=" + dbRet.getRetStr() + "&validError=" + dbRet.get_validErrorCode());
      }
      else if (dbRet.getDbErrorCode() == 1) {
        response.sendRedirect(urlFailure + "?dbMethod=" + dbRet.getRetStr() + "&dbError=" + dbRet.getDbErrorCode());
      }  
      else {
         response.sendRedirect(urlFailure);
      }
    }
  }
  
  private DbRet doDelete(HttpServletRequest request) {
    DbRet dbRet = new DbRet();
    
    String NWLR_Code = request.getParameter("NWLR_Code");
    
    if (NWLR_Code == null || NWLR_Code.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
            
    // get database connection
    Database database = _director.getDBConnection(_databaseId);

    String query = "DELETE FROM Newsletter WHERE NWLR_Code = '" + SwissKnife.sqlEncode(NWLR_Code) + "'";
    
    dbRet = database.execQuery(query);
    
    _director.freeDBConnection(_databaseId,database);

    return dbRet;
  }
}