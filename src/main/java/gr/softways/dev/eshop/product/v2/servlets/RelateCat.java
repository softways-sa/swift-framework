package gr.softways.dev.eshop.product.v2.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class RelateCat extends HttpServlet {

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

    int auth = _director.auth(databaseId,authUsername,authPassword,"prdInCatTab",Director.AUTH_INSERT);

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
    
    QueryDataSet queryDataSet = null;
    
    Database database = _director.getDBConnection(databaseId);
    
    //elegxe an to proion exei kuria kathgoria
    String select = "SELECT * FROM prdInCatTab"
                  + " WHERE PINCPrdId = '" + PINCPrdId + "'" 
                  + " AND PINCPrimary = '1'";
    
    int rows = 0;
    
    try {
      queryDataSet = new QueryDataSet();
    
      queryDataSet.setQuery(new QueryDescriptor(database,select,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();
      
      rows = queryDataSet.getRowCount();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (queryDataSet != null) try { queryDataSet.close(); } catch (Exception e) { }
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
  
  private DbRet doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"prdInCatTab",Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();
    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String prdId = SwissKnife.sqlEncode(request.getParameter("prdId")),
           PINCCatId = SwissKnife.sqlEncode(request.getParameter("catId")),
           PINCPrimary = SwissKnife.sqlEncode(request.getParameter("PINCPrimary")),
           PINCRank = SwissKnife.sqlEncode(request.getParameter("PINCRank")),
           s_PINCCatId = null;
    
    if (prdId.length() == 0 || PINCCatId.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    QueryDataSet queryDataSet = null;
    
    Database database = _director.getDBConnection(databaseId);

    String query = null;
    
    // allow only one main category
    if (PINCPrimary.equals("1")) {
      query = "UPDATE prdInCatTab"
            + " SET PINCPrimary = '0'" 
            + " WHERE PINCPrdId = '" + prdId + "'" 
            + " AND PINCPrimary = '1'";
    
      dbRet = database.execQuery(query);
    }
    
    query = "UPDATE prdInCatTab SET"
          + " PINCPrimary = '" + PINCPrimary + "'"
          + ",PINCRank = " + PINCRank
          + " WHERE PINCPrdId = '" + prdId + "'"
          + " AND PINCCatId = '" + PINCCatId + "'";
    
    dbRet = database.execQuery(query);
    
    // ensure that at least one main category remains {
    if (dbRet.getNoError() == 1) {
      try {
        queryDataSet = new QueryDataSet();
        
        query = "SELECT * FROM prdInCatTab WHERE PINCPrdId = '" + prdId + "'";
        
        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
  
        queryDataSet.refresh();
        
        while (queryDataSet.inBounds() == true) {
          if (queryDataSet.getString("PINCPrimary").equals("0")) {
            s_PINCCatId = queryDataSet.getString("PINCCatId");
          }
          else {
            s_PINCCatId = null;
            break;
          }
          queryDataSet.next();
        }
        
        if (s_PINCCatId != null) {
          query = "UPDATE prdInCatTab SET"
                + " PINCPrimary = '1'"
                + " WHERE PINCPrdId = '" + prdId + "'"
                + " AND PINCCatId = '" + s_PINCCatId + "'";
                
          dbRet = database.execQuery(query);
        }
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      finally {
        if (queryDataSet != null) try { queryDataSet.close(); } catch (Exception e) { }
      }
    }
    // } ensure that at least one main category remains
    
    _director.freeDBConnection(databaseId,database);

    return dbRet;
  }
  
  private DbRet doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"prdInCatTab",Director.AUTH_DELETE);

    DbRet dbRet = new DbRet();
    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String prdId = SwissKnife.sqlEncode(request.getParameter("prdId")),
           PINCCatId = SwissKnife.sqlEncode(request.getParameter("catId")),
           s_PINCCatId = null;
    
    if (prdId.length() == 0 || PINCCatId.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    QueryDataSet queryDataSet = null;
    
    Database database = _director.getDBConnection(databaseId);

    String query = "DELETE FROM prdInCatTab"
                 + " WHERE PINCPrdId = '" + prdId + "'" 
                 + " AND PINCCatId  = '"  + PINCCatId + "'";
                 
    //System.out.println("query = " + query );
    
    dbRet = database.execQuery(query);

    // ensure that at least one main category remains {
    if (dbRet.getNoError() == 1) {
      try {
        queryDataSet = new QueryDataSet();
        
        query = "SELECT * FROM prdInCatTab WHERE PINCPrdId = '" + prdId + "'";
        
        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
  
        queryDataSet.refresh();
        
        while (queryDataSet.inBounds() == true) {
          if (queryDataSet.getString("PINCPrimary").equals("0")) {
            s_PINCCatId = queryDataSet.getString("PINCCatId");
          }
          else {
            s_PINCCatId = null;
            break;
          }
          queryDataSet.next();
        }
        
        if (s_PINCCatId != null) {
          query = "UPDATE prdInCatTab SET"
                + " PINCPrimary = '1'"
                + " WHERE PINCPrdId = '" + prdId + "'"
                + " AND PINCCatId = '" + s_PINCCatId + "'";
                
          dbRet = database.execQuery(query);
        }
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      finally {
        if (queryDataSet != null) try { queryDataSet.close(); } catch (Exception e) { }
      }
    }
    // } ensure that at least one main category remains

    _director.freeDBConnection(databaseId,database);

    return dbRet;
  }
}
