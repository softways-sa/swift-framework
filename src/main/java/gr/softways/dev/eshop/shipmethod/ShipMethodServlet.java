package gr.softways.dev.eshop.shipmethod;

import gr.softways.dev.eshop.shiprange.*;
import java.io.*;
import java.util.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class ShipMethodServlet extends HttpServlet {

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
      dbRet = doInsert(request, databaseId);
    }
    else if (action.equals("UPDATE")) {
      dbRet = doUpdate(request, databaseId);
    }
    else if (action.equals("DELETE")) {
      dbRet = doDelete(request, databaseId);
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

    int auth = _director.auth(databaseId,authUsername,authPassword,"ShipCostMethod",Director.AUTH_INSERT);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String SHCMCode = null,
           SHCMTitle = SwissKnife.sqlEncode(request.getParameter("SHCMTitle")),
           SHCMTitleLG = SwissKnife.sqlEncode(request.getParameter("SHCMTitleLG")),
           SHCMText = SwissKnife.sqlEncode(request.getParameter("SHCMText")),
           SHCMTextLG = SwissKnife.sqlEncode(request.getParameter("SHCMTextLG")),
           SHCMFlag = SwissKnife.sqlEncode(request.getParameter("SHCMFlag"));

    if (SHCMTitle.equals("")) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    SHCMCode = SwissKnife.buildPK();
    
    String query = "INSERT INTO ShipCostMethod ("
               + "SHCMCode,SHCMTitle,SHCMTitleLG,SHCMText,SHCMTextLG,SHCMFlag"
               + ") VALUES ("
               + "'" + SHCMCode + "'"
               + ",'" + SHCMTitle + "'"
               + ",'" + SHCMTitleLG + "'"
               + ",'" + SHCMText + "'"
               + ",'" + SHCMTextLG + "'"
               + ",'" + SHCMFlag + "'"
               + ")";
    
    dbRet = database.execQuery(query);
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  private DbRet doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"ShipCostMethod",Director.AUTH_DELETE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String SHCMCode = SwissKnife.sqlEncode(request.getParameter("SHCMCode"));
    
    if (SHCMCode.equals("")) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    String query = null;
    
    query = "DELETE FROM ShipCostMethod WHERE SHCMCode = '" + SHCMCode + "'";
    
    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(query);
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  private DbRet doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"ShipCostMethod",Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String SHCMCode = SwissKnife.sqlEncode(request.getParameter("SHCMCode")),
           SHCMTitle = SwissKnife.sqlEncode(request.getParameter("SHCMTitle")),
           SHCMTitleLG = SwissKnife.sqlEncode(request.getParameter("SHCMTitleLG")),
           SHCMText = SwissKnife.sqlEncode(request.getParameter("SHCMText")),
           SHCMTextLG = SwissKnife.sqlEncode(request.getParameter("SHCMTextLG")),
           SHCMFlag = SwissKnife.sqlEncode(request.getParameter("SHCMFlag"));
    
    if (SHCMCode.equals("") || SHCMTitle.equals("")) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    if (dbRet.getNoError() == 1) {
      String query = "UPDATE ShipCostMethod SET"
                  + " SHCMTitle = '" + SHCMTitle + "'"
                  + ",SHCMTitleLG = '" + SHCMTitleLG + "'"
                  + ",SHCMText = '" + SHCMText + "'"
                  + ",SHCMTextLG = '" + SHCMTextLG + "'"
                  + ",SHCMFlag = '" + SHCMFlag + "'"
                  + " WHERE SHCMCode = '" + SHCMCode + "'";
                  
      dbRet = database.execQuery(query);
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
}