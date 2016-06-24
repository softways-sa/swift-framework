package gr.softways.dev.eshop.product.v2.servlets;

import java.io.*;
import java.util.*;
import java.math.BigDecimal;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;
import gr.softways.dev.eshop.eways.Product;

public class OnSalesServlet extends HttpServlet {

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
    else if (action.equals("DISCOUNT")) {
      dbRet = doDiscount(request, databaseId);
    }
    else if (action.equals("REVERT")) {
      dbRet = doRevert(request, databaseId);
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

  private DbRet doDiscount(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"product",Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String query = null;
    
    String catId = SwissKnife.sqlEncode(request.getParameter("catId"));
    
    BigDecimal discount = null;
    try {
      discount = new BigDecimal( Integer.parseInt( request.getParameter("discount") ) );
    }
    catch (Exception e) {
      discount = null;
    }
    
    if (discount == null) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    PreparedStatement statement = null;
    
    QueryDataSet queryDataSet = null;
    
    BigDecimal hdRetailPrcEU = null, retailPrcEU = null;
    
    String prdId = null;
    
    if (dbRet.getNoError() == 1) {
      try {
        query = "UPDATE product SET"
              + " hdRetailPrcEU = ?"
              + ",hotdealFlag = ?"
              + " WHERE prdId = ?";
        
        statement = database.createPreparedStatement(query);
        
        query = "SELECT prdId,retailPrcEU FROM product,prdInCatTab WHERE PINCPrdId = prdId AND PINCCatId LIKE '" + catId + "%'";
        
        queryDataSet = new QueryDataSet();
        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
        queryDataSet.refresh();
        
        while (queryDataSet.inBounds() == true) {
          prdId = queryDataSet.getString("prdId");
          retailPrcEU = queryDataSet.getBigDecimal("retailPrcEU");
          
          hdRetailPrcEU = retailPrcEU.subtract( retailPrcEU.multiply( discount.movePointLeft(2) ) );
              
          statement.setBigDecimal(1, hdRetailPrcEU);
          statement.setString(2, Product.HOTDEAL_FLAG_ALWAYS);
          statement.setString(3, prdId);

          statement.executeUpdate();
          
          queryDataSet.next();
        }
      }
      catch (Exception e) {
        dbRet.setNoError(0);
      }
      finally {
        try { queryDataSet.close(); } catch (Exception e) { }
        
        try { if (statement != null) statement.close(); } catch (Exception e) { }
        statement = null;
      }
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  private DbRet doRevert(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"product",Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String query = null;
    
    String catId = SwissKnife.sqlEncode(request.getParameter("catId"));
    
    Database database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    PreparedStatement statement = null;
    
    QueryDataSet queryDataSet = null;
    
    String prdId = null;
    
    if (dbRet.getNoError() == 1) {
      try {
        query = "UPDATE product SET"
              + " hotdealFlag = ?"
              + " WHERE prdId = ?";
        
        statement = database.createPreparedStatement(query);
        
        query = "SELECT prdId,retailPrcEU FROM product,prdInCatTab WHERE PINCPrdId = prdId AND PINCCatId LIKE '" + catId + "%'";
        
        queryDataSet = new QueryDataSet();
        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
        queryDataSet.refresh();
        
        while (queryDataSet.inBounds() == true) {
          prdId = queryDataSet.getString("prdId");
          
          statement.setString(1, "0");
          statement.setString(2, prdId);

          statement.executeUpdate();
          
          queryDataSet.next();
        }
      }
      catch (Exception e) {
        dbRet.setNoError(0);
      }
      finally {
        try { queryDataSet.close(); } catch (Exception e) { }
        
        try { if (statement != null) statement.close(); } catch (Exception e) { }
        statement = null;
      }
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
}