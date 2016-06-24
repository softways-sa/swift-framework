package gr.softways.dev.eshop.product.v2.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class ProductMasterAttribute extends HttpServlet {
  
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
    
    if (action.equals("INSERT")) {
      dbRet = doInsert(request,databaseId);
    }
    else if (action.equals("UPDATE")) {
      dbRet = doUpdate(request,databaseId);
    }
    else if (action.equals("DELETE")) {
      dbRet = doDelete(request,databaseId);
    }
    
    if (dbRet.getNoError() == 1) {
      response.sendRedirect(urlSuccess);
    }
    else {
      if (dbRet.getAuthError() == 1)
        response.sendRedirect(urlNoAccess + "?authError=" + dbRet.getAuthErrorCode());
      else if (dbRet.get_validError() == 1)
        response.sendRedirect(urlFailure + "?validField=" + dbRet.getRetStr() + "&validError=" + dbRet.get_validErrorCode());
      else if (dbRet.getDbErrorCode() == 1)
        response.sendRedirect(urlFailure + "?dbMethod=" + dbRet.getRetStr() + "&dbError=" + dbRet.getDbErrorCode());
      else response.sendRedirect(urlFailure);
    }
  }

  private DbRet doInsert(HttpServletRequest request, String databaseId) {  
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"productMasterAttribute",Director.AUTH_INSERT);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
     
    String PMA_atrCode = SwissKnife.sqlEncode(request.getParameter("atrCode")),
           PMA_prdId = SwissKnife.sqlEncode(request.getParameter("prdId")),
           PMARank = SwissKnife.sqlEncode(request.getParameter("PMARank")),
           PMAPositionKey = SwissKnife.sqlEncode(request.getParameter("PMAPositionKey"));
    
    if (PMA_atrCode.length() == 0 || PMA_prdId.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
 
    Database database = _director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
   
    QueryDataSet queryDataSet = new QueryDataSet();
   
    String query = "INSERT INTO productMasterAttribute (" 
                 + "PMACode,PMA_atrCode,PMA_prdId,PMAPositionKey,PMARank" 
                 + ") VALUES (" 
                 + "'" + SwissKnife.buildPK() + "'"
                 + ",'" + PMA_atrCode + "'" 
                 + ",'" + PMA_prdId + "'"
                 + ",'" + PMAPositionKey + "'"            
                 + "," + getIntFromString(PMARank) + ""            
                 + ")";
                   
    dbRet = database.execQuery(query);
    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    _director.freeDBConnection(databaseId,database);        
    
    return dbRet;
  }
  
  
  private DbRet doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "productMasterAttribute", Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();
    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String PMACode = SwissKnife.sqlEncode(request.getParameter("PMACode")),
           PMARank = SwissKnife.sqlEncode(request.getParameter("PMARank")),
           PMAPositionKey = SwissKnife.sqlEncode(request.getParameter("PMAPositionKey"));
    
    if (PMACode.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }

    Database database = _director.getDBConnection(databaseId);

    String query = "UPDATE productMasterAttribute SET"
                 + " PMAPositionKey = '" + PMAPositionKey + "'"
                 + ", PMARank = " + getIntFromString(PMARank) + ""
                 + " WHERE PMACode = '" + PMACode + "'";
    dbRet = database.execQuery(query);
    
    _director.freeDBConnection(databaseId,database);

    return dbRet;
  }
  
  private DbRet doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "productMasterAttribute", Director.AUTH_DELETE);

    DbRet dbRet = new DbRet();
    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String PMACode = SwissKnife.sqlEncode(request.getParameter("PMACode"));
    
    if (PMACode.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
        
    Database database = _director.getDBConnection(databaseId);    

    String query = "DELETE FROM productMasterAttribute" 
                 + " WHERE PMACode = '" + PMACode + "'";
                   
    dbRet = database.execQuery(query);

    _director.freeDBConnection(databaseId,database);

    return dbRet;
  }  
  
  public int getIntFromString(String s) {
    int n = 0;
    try {
      n = Integer.parseInt(s);
    }
    catch(Exception e){
      n = 0;
    }
    return n;
  }

}
