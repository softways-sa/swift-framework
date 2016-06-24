package gr.softways.dev.eshop.attribute.servlets;

import java.io.*;
import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class doAction extends HttpServlet {

  private Director _director;
    
  private String _charset = null;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    _director = Director.getInstance();
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1"),
           databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId"),
           urlSuccess = request.getParameter("urlSuccess") == null ? "" : request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");

    DbRet dbRet = new DbRet();

    if (databaseId.equals("")) dbRet.setNoError(0);
    else if (action.equals("INSERT")) dbRet = doInsert(request, databaseId);
    else if (action.equals("UPDATE")) dbRet = doUpdate(request, databaseId);
    else if (action.equals("DELETE")) dbRet = doDelete(request, databaseId);
    else dbRet.setNoError(0);

    if (dbRet.getNoError() == 1) {
      response.sendRedirect(urlSuccess);
    }
    else if (dbRet.getAuthError() == 1) {
      response.sendRedirect(urlNoAccess);
    }
    else {
      response.sendRedirect(urlFailure);
    }
  }

  private DbRet doInsert(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);
    DbRet dbRet = new DbRet();
    
    int auth = _director.auth(databaseId,authUsername,authPassword,"attribute",Director.AUTH_INSERT);
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      return dbRet;
    }

    PreparedStatement ps = null;    
    
    String insRow = "INSERT INTO attribute " +
    " (atrCode,atrName,atrNameLG,atrNameLG1,atrNameLG2,atrNameLG3,atrNameLG4,atrNameLG5,atrNameUp,atrNameUpLG,atrKeepStock,atrHasPrice)" +
    " VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
                   
    Database database = _director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    int rowsAffected = 0;
    if (dbRet.getNoError() == 1) {
      try {
        ps = database.createPreparedStatement(insRow);
        
        ps.setString(1, SwissKnife.buildPK());
        ps.setString(2, SwissKnife.sqlEncode(request.getParameter("atrName")));
        ps.setString(3, SwissKnife.sqlEncode(request.getParameter("atrNameLG")));
        ps.setString(4, SwissKnife.sqlEncode(request.getParameter("atrNameLG1")));
        ps.setString(5, SwissKnife.sqlEncode(request.getParameter("atrNameLG2")));
        ps.setString(6, SwissKnife.sqlEncode(request.getParameter("atrNameLG3")));
        ps.setString(7, SwissKnife.sqlEncode(request.getParameter("atrNameLG4")));
        ps.setString(8, SwissKnife.sqlEncode(request.getParameter("atrNameLG5")));
        ps.setString(9, SwissKnife.searchConvert(SwissKnife.sqlEncode(request.getParameter("atrName"))));
        ps.setString(10, SwissKnife.searchConvert(SwissKnife.sqlEncode(request.getParameter("atrNameLG"))));
        ps.setString(11, SwissKnife.sqlEncode(request.getParameter("atrKeepStock")));
        ps.setString(12, SwissKnife.sqlEncode(request.getParameter("atrHasPrice")));
        rowsAffected = ps.executeUpdate();
      }
      catch (Exception eee) {
        dbRet.setNoError(0);
        eee.printStackTrace();
      }
      finally {
        try { if (ps != null) ps.close(); } catch (Exception e) { }
      }
    }
    
    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    _director.freeDBConnection(databaseId,database);    

    return dbRet;
  }

  private DbRet doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

    DbRet dbRet = new DbRet();
    
    int auth = _director.auth(databaseId,authUsername,authPassword,"attribute",Director.AUTH_UPDATE);
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      return dbRet;
    }
    
    String updateRow = "UPDATE attribute SET"
    + " atrName=?,atrNameLG=?,atrNameLG1=?,atrNameLG2=?,atrNameLG3=?,atrNameLG4=?,atrNameLG5=?,atrNameUp=?,atrNameUpLG=?,atrKeepStock=?,atrHasPrice=?"
    + " WHERE atrCode=?";
    
    Database database = _director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();    
    PreparedStatement ps = null;        
    int rowsAffected = 0;
    if (dbRet.getNoError() == 1) {
      try {
        ps = database.createPreparedStatement(updateRow);
        
        ps.setString(1, SwissKnife.sqlEncode(request.getParameter("atrName")));
        ps.setString(2, SwissKnife.sqlEncode(request.getParameter("atrNameLG")));
        ps.setString(3, SwissKnife.sqlEncode(request.getParameter("atrNameLG1")));
        ps.setString(4, SwissKnife.sqlEncode(request.getParameter("atrNameLG2")));
        ps.setString(5, SwissKnife.sqlEncode(request.getParameter("atrNameLG3")));
        ps.setString(6, SwissKnife.sqlEncode(request.getParameter("atrNameLG4")));
        ps.setString(7, SwissKnife.sqlEncode(request.getParameter("atrNameLG5")));
        ps.setString(8, SwissKnife.searchConvert(SwissKnife.sqlEncode(request.getParameter("atrName"))));
        ps.setString(9, SwissKnife.searchConvert(SwissKnife.sqlEncode(request.getParameter("atrNameLG"))));
        ps.setString(10, SwissKnife.sqlEncode(request.getParameter("atrKeepStock")));
        ps.setString(11, SwissKnife.sqlEncode(request.getParameter("atrHasPrice")));        
        ps.setString(12, SwissKnife.sqlEncode(request.getParameter("atrCode")));            
        
        rowsAffected = ps.executeUpdate();
      }
      catch (Exception eee) {
        dbRet.setNoError(0);
        eee.printStackTrace();
      }
      finally {
        try { if (ps != null) ps.close(); } catch (Exception e) { }
      }
    }
    
    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    _director.freeDBConnection(databaseId,database);    
    
    return dbRet;
    
  }

  private DbRet doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

    DbRet dbRet = new DbRet();
    
    int auth = _director.auth(databaseId,authUsername,authPassword,"attribute",Director.AUTH_DELETE);
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      return dbRet;
    }

    String deleteRow = "DELETE FROM attribute WHERE atrCode=?";
    
    Database database = _director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();    
    PreparedStatement ps = null;        
    int rowsAffected = 0;
    if (dbRet.getNoError() == 1) {
      try {
        ps = database.createPreparedStatement(deleteRow);
        ps.setString(1, SwissKnife.sqlEncode(request.getParameter("atrCode")));
        rowsAffected = ps.executeUpdate();
      }
      catch (Exception eee) {
        dbRet.setNoError(0);
        eee.printStackTrace();
      }
      finally {
        try { if (ps != null) ps.close(); } catch (Exception e) { }
      }
    }
    
    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    _director.freeDBConnection(databaseId,database);    
    
    return dbRet;
  }

}
