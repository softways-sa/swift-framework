/*
 * doPrdCat.java
 *
 * Created on 20 Ιούνιος 2002, 5:04 μμ
 */

package gr.softways.dev.eshop.product.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class doRelateCat extends HttpServlet {

  private Director _director;
    
  private String _charset = null;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    _director = Director.getInstance();
  }

  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
         throws ServletException, IOException {
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
    else if (action.equals("UPDATE_PRIMARY")) {
      dbRet = doUpdatePrimary(request,databaseId);
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

    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "prdInCatTab", Director.AUTH_INSERT);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
     
    String PINCPrdId = SwissKnife.sqlEncode(request.getParameter("prdId")),
           PINCCatId = SwissKnife.sqlEncode(request.getParameter("catId")),
           PINCPrimary = "";
    
    if (PINCPrdId.length() == 0 || PINCCatId.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    Database database = _director.getDBConnection(databaseId);
       
    //elegxe an to proion exei kuria kathgoria
    String select = "SELECT * FROM prdInCatTab"
                  + " WHERE PINCPrdId = '" + PINCPrdId + "'" 
                  + " AND PINCPrimary = '1'";
    
    int rows = 0;
    
    try {
      // κλείσε το query ώστε να το ανοίξουμε ξανά
      if (queryDataSet.isOpen()) queryDataSet.close();
    
      queryDataSet.setQuery(new QueryDescriptor(database, select, null, true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();
      
      rows = queryDataSet.getRowCount();
     
      queryDataSet.close();
    }    
    catch (Exception e) {
      e.printStackTrace();
    }
    
    if (rows > 0) {
      PINCPrimary = "0";
    }  
    else {
      PINCPrimary = "1";
    }
    
    String query = "INSERT INTO prdInCatTab (" 
                 + "PINCCode,PINCPrdId,PINCCatId,PINCPrimary" 
                 + ") VALUES (" 
                 + "'" + SwissKnife.buildPK() + "'"
                 + ",'" + PINCPrdId + "'"
                 + ",'" + PINCCatId + "'" 
                 + ",'" + PINCPrimary + "'"
                 + ")";
                   
    dbRet = database.execQuery(query);
    
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  private DbRet doUpdatePrimary(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "prdInCatTab", Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();
    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String prdId = SwissKnife.sqlEncode(request.getParameter("prdId")),
           PINCCatId = SwissKnife.sqlEncode(request.getParameter("catId"));
    
    if (prdId.length() == 0 || PINCCatId.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);

    String query = "UPDATE prdInCatTab"
                 + " SET PINCPrimary = '0'" 
                 + " WHERE PINCPrdId = '" + prdId + "'" 
                 + " AND PINCPrimary = '1'";
    
    dbRet = database.execQuery(query);
    
    if (dbRet.getNoError() == 1) {
      query = "UPDATE prdInCatTab" 
            + " SET PINCPrimary = '1'"
            + " WHERE PINCPrdId = '" + prdId + "'" 
            + " AND PINCCatId = '" + PINCCatId + "'";
    }
    
    dbRet = database.execQuery(query);
    
    _director.freeDBConnection(databaseId,database);

    return dbRet;
  }
  
  private DbRet doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "prdInCatTab", Director.AUTH_DELETE);

    DbRet dbRet = new DbRet();
    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String prdId = SwissKnife.sqlEncode(request.getParameter("prdId")),
           PINCCatId = SwissKnife.sqlEncode(request.getParameter("catId"));
    
    if (prdId.length() == 0 || PINCCatId.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
        
    Database database = _director.getDBConnection(databaseId);    

    String query = "DELETE FROM prdInCatTab" 
                 + " WHERE PINCPrdId = '" + prdId + "'" 
                 + " AND PINCCatId  = '"  + PINCCatId + "'";
                   
    //System.out.println("query = " + query );
    
    dbRet = database.execQuery(query);

    _director.freeDBConnection(databaseId,database);

    return dbRet;
  }
}