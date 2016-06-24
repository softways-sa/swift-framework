package gr.softways.dev.eshop.configuration;

import java.io.*;
import java.util.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class ConfigurationServlet extends HttpServlet {

  private Director _director;

  private String _charset = null;
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    _director = Director.getInstance();
  }

  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    DbRet dbRet = new DbRet();
    
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1"),
           databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId"),
           urlSuccess = request.getParameter("urlSuccess") == null ? "" :  request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");

    if (databaseId.equals("")) {
      dbRet.setNoError(0);
    }
    else if (action.equals("INSERT")) {
      dbRet = doInsert(request,databaseId);
    }
    else if (action.equals("UPDATE")) {
      dbRet = doUpdate(request,databaseId);
    }
    else if (action.equals("DELETE")) {
      dbRet = doDelete(request,databaseId);
    }
    else {
      dbRet.setNoError(0);
    }
    
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

  private DbRet doInsert(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"Configuration",Director.AUTH_INSERT);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String CO_Code = null,
           CO_Title = request.getParameter("CO_Title"),
           CO_Key = request.getParameter("CO_Key"),
           CO_Value = request.getParameter("CO_Value"),
           CO_ValueLG = request.getParameter("CO_ValueLG"),
           CO_ValueLG1 = request.getParameter("CO_ValueLG1"),
           CO_ValueLG2 = request.getParameter("CO_ValueLG2"),
           CO_Params = request.getParameter("CO_Params"),
           CO_ParamsLG = request.getParameter("CO_ParamsLG"),
           CO_ParamsLG1 = request.getParameter("CO_ParamsLG1"),
           CO_ParamsLG2 = request.getParameter("CO_ParamsLG2"),
           CO_Description = request.getParameter("CO_Description");
           
    int CO_GroupID = 0, CO_SortOrder = 0, CO_Visible = 0, CO_Editable = 0;
    
    try {
      CO_GroupID = Integer.parseInt(request.getParameter("CO_GroupID"));
    }
    catch (Exception e) {
      CO_GroupID = 0;
    }
    
    try {
      CO_SortOrder = Integer.parseInt(request.getParameter("CO_SortOrder"));
    }
    catch (Exception e) {
      CO_SortOrder = 0;
    }
    
    try {
      CO_Visible = Integer.parseInt(request.getParameter("CO_Visible"));
    }
    catch (Exception e) {
      CO_Visible = 1;
    }
    
    try {
      CO_Editable = Integer.parseInt(request.getParameter("CO_Editable"));
    }
    catch (Exception e) {
      CO_Editable = 1;
    }
    
    if (CO_Key == null || CO_Key.length() == 0) {
      dbRet.setNoError(0);
      return dbRet;
    }
    
    PreparedStatement ps = null;
    
    Database database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    String query = "INSERT INTO Configuration ("
                 + " CO_Code,CO_GroupID,CO_Title,CO_Key,CO_Value,CO_ValueLG,CO_ValueLG1,CO_ValueLG2,CO_Params,CO_ParamsLG"
                 + ",CO_ParamsLG1,CO_ParamsLG2,CO_Description,CO_SortOrder,CO_Visible,CO_Editable"
                 + ") VALUES ("
                 + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?"
                 + ")";
                 
    if (dbRet.getNoError() == 1) {
      try {
        ps = database.createPreparedStatement(query);
        
        CO_Code = SwissKnife.buildPK();
        
        ps.setString(1, CO_Code);
        
        ps.setInt(2, CO_GroupID);
        
        ps.setString(3, SwissKnife.sqlEncode(CO_Title));
        ps.setString(4, SwissKnife.sqlEncode(CO_Key));
        ps.setString(5, SwissKnife.sqlEncode(CO_Value));
        ps.setString(6, SwissKnife.sqlEncode(CO_ValueLG));
        
        ps.setString(7, SwissKnife.sqlEncode(CO_ValueLG1));
        ps.setString(8, SwissKnife.sqlEncode(CO_ValueLG2));
        ps.setString(9, SwissKnife.sqlEncode(CO_Params));
        ps.setString(10, SwissKnife.sqlEncode(CO_ParamsLG));
        
        ps.setString(11, SwissKnife.sqlEncode(CO_ParamsLG1));
        ps.setString(12, SwissKnife.sqlEncode(CO_ParamsLG2));
        ps.setString(13, SwissKnife.sqlEncode(CO_Description));
        
        ps.setInt(14, CO_SortOrder);
        ps.setInt(15, CO_Visible);
        ps.setInt(16, CO_Editable);
        
        ps.executeUpdate();
      }
      catch (Exception e) {
        e.printStackTrace();
        dbRet.setNoError(0);
      }
      finally {
        try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
      }
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    _director.freeDBConnection(databaseId,database);
    
    return dbRet;
  }
  
  private DbRet doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"Configuration",Director.AUTH_DELETE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String CO_Code = request.getParameter("CO_Code");
    
    if (CO_Code == null || CO_Code.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    String query = null;
    
    if (dbRet.getNoError() == 1) {
      query = "DELETE FROM Configuration WHERE CO_Code = '" + SwissKnife.sqlEncode(CO_Code) + "'";
      
      dbRet = database.execQuery(query);
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  private DbRet doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"Configuration",Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String CO_Code = request.getParameter("CO_Code"),
           CO_Title = request.getParameter("CO_Title"),
           CO_Value = request.getParameter("CO_Value"),
           CO_ValueLG = request.getParameter("CO_ValueLG"),
           CO_ValueLG1 = request.getParameter("CO_ValueLG1"),
           CO_ValueLG2 = request.getParameter("CO_ValueLG2"),
           CO_Params = request.getParameter("CO_Params"),
           CO_ParamsLG = request.getParameter("CO_ParamsLG"),
           CO_ParamsLG1 = request.getParameter("CO_ParamsLG1"),
           CO_ParamsLG2 = request.getParameter("CO_ParamsLG2"),
           CO_Description = request.getParameter("CO_Description");
           
    if (CO_Code == null || CO_Code.length() == 0) {
      dbRet.setNoError(0);
      return dbRet;
    }
    
    PreparedStatement ps = null;
    
    Database database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    String query = "UPDATE Configuration SET"
        + " CO_Title = ?"
        + ",CO_Value = ?"
        + ",CO_ValueLG = ?"
        + ",CO_ValueLG1 = ?"
        + ",CO_ValueLG2 = ?"
        + ",CO_Params = ?"
        + ",CO_ParamsLG = ?"
        + ",CO_ParamsLG1 = ?"
        + ",CO_ParamsLG2 = ?"
        + ",CO_Description = ?"
        + " WHERE CO_Code = ?";
                 
    if (dbRet.getNoError() == 1) {
      try {
        ps = database.createPreparedStatement(query);
        
        ps.setString(1, SwissKnife.sqlEncode(CO_Title));
        ps.setString(2, SwissKnife.sqlEncode(CO_Value));
        ps.setString(3, SwissKnife.sqlEncode(CO_ValueLG));
        ps.setString(4, SwissKnife.sqlEncode(CO_ValueLG1));
        ps.setString(5, SwissKnife.sqlEncode(CO_ValueLG2));
        ps.setString(6, SwissKnife.sqlEncode(CO_Params));
        ps.setString(7, SwissKnife.sqlEncode(CO_ParamsLG));
        ps.setString(8, SwissKnife.sqlEncode(CO_ParamsLG1));
        ps.setString(9, SwissKnife.sqlEncode(CO_ParamsLG2));
        ps.setString(10, SwissKnife.sqlEncode(CO_Description));
        ps.setString(11, SwissKnife.sqlEncode(CO_Code));
        
        ps.executeUpdate();
      }
      catch (Exception e) {
        e.printStackTrace();
        dbRet.setNoError(0);
      }
      finally {
        try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
      }
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    _director.freeDBConnection(databaseId,database);
    
    return dbRet;
  }
}