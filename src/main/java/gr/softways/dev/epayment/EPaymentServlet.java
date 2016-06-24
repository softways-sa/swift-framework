package gr.softways.dev.epayment;

import java.io.*;
import java.util.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class EPaymentServlet extends HttpServlet {

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
    else if (action.equals("UPDATE_BANK_STATUS")) {
      dbRet = doUpdate(request,databaseId);
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

    int auth = _director.auth(databaseId,authUsername,authPassword,"EPayment",Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String PAYNT_Code = request.getParameter("PAYNT_Code"),
           PAYNT_BankPayStatus = request.getParameter("PAYNT_BankPayStatus");
           
    if (PAYNT_Code == null || PAYNT_Code.length() == 0) {
      dbRet.setNoError(0);
      return dbRet;
    }
    
    PreparedStatement ps = null;
    
    Database database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    String query = "UPDATE EPayment SET"
        + " PAYNT_BankPayStatus = ?"
        + " WHERE PAYNT_Code = ?";
                 
    if (dbRet.getNoError() == 1) {
      try {
        ps = database.createPreparedStatement(query);
        
        ps.setString(1, SwissKnife.sqlEncode(PAYNT_BankPayStatus));
        ps.setString(2, SwissKnife.sqlEncode(PAYNT_Code));
        
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