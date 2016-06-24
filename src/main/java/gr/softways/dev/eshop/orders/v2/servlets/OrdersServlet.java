package gr.softways.dev.eshop.orders.v2.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.sql.PreparedStatement;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

import gr.softways.dev.eshop.orders.v2.UndoOrder;

public class OrdersServlet extends HttpServlet {

  private Director _director;
    
  private String _charset = null;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    _director = Director.getInstance();
  }

  //Process the HTTP Post request
  public void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    DbRet dbRet = new DbRet();
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1"),
           databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId"),
           urlSuccess = request.getParameter("urlSuccess") == null ? "" : request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");

    if (databaseId.equals("")) {
      dbRet.setNoError(0);
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
  
  private DbRet doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"orders",Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String orderId = SwissKnife.sqlEncode(request.getParameter("orderId")),
           orderStatus = SwissKnife.sqlEncode(request.getParameter("orderStatus")),
           ordHistDetails  = SwissKnife.sqlEncode(request.getParameter("ordHistDetails"));

    if (orderId.equals("") || orderStatus.equals("")) {
      dbRet.setNoError(0);
      
      return dbRet;
    }

    // get database connection
    Database database = _director.getDBConnection(databaseId);

    // begin transaction
    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();
    
    PreparedStatement stm = null;

    String query = "UPDATE orders SET"
                 + " status = ?"
                 + ",ordHistDetails = ?"
                 + " WHERE orderId = ?";
    
   try {
      stm = database.createPreparedStatement(query);
      
      stm.setString(1, orderStatus);
      stm.setString(2, ordHistDetails);
      
      stm.setString(3, orderId);

      stm.executeUpdate();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    finally {
      try { if (stm != null) stm.close(); } catch (Exception e) { }
      
      stm = null;
    }
    
    if (dbRet.getNoError() == 1) {
      query = "UPDATE transactions SET" +
               " status = '" + orderStatus + "'" +
               " WHERE orderId = '" + orderStatus + "'";
      
      dbRet = database.execQuery(query);
    }
    
    // End transaction (commit or rollback)
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    _director.freeDBConnection(databaseId,database);
    
    return dbRet;
  }
  
  private DbRet doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"orders",Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String orderId = SwissKnife.sqlEncode(request.getParameter("orderId"));

    if (orderId.equals("")) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    UndoOrder undoOrder = new UndoOrder();
    undoOrder.initBean(databaseId, request, null, this, request.getSession());
    
    dbRet = undoOrder.doAction(orderId);
    
    return dbRet;
  }
}
