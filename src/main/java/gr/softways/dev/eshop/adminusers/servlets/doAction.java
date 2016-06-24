package gr.softways.dev.eshop.adminusers.servlets;

import java.io.*;
import java.util.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;
import java.text.SimpleDateFormat;

public class doAction extends HttpServlet {

  private Director bean;
    
  private String _charset = null;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    
    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    bean = Director.getInstance();
  }

  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
                     throws ServletException, IOException {
                       
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String action = SwissKnife.grEncode(request.getParameter("action1")),
           databaseId = SwissKnife.grEncode(request.getParameter("databaseId")),
           urlSuccess = SwissKnife.grEncode(request.getParameter("urlSuccess")),
           urlFailure = SwissKnife.grEncode(request.getParameter("urlFailure")),
           urlNoAccess = SwissKnife.grEncode(request.getParameter("urlNoAccess"));

    int status = Director.STATUS_ERROR;

    if (databaseId.equals(""))
      status = Director.STATUS_ERROR;
    else if (action.equals("INSERT"))
        status = doInsert(request, databaseId);
    else if (action.equals("UPDATE"))
        status = doUpdate(request,databaseId);
    else if (action.equals("DELETE"))
        status = doDelete(request,databaseId);

    if (status < 0)    {
      response.sendRedirect(urlNoAccess);
    }
    else if (status == Director.STATUS_OK) {
      response.sendRedirect(urlSuccess);
    }
    else {
      response.sendRedirect(urlFailure);
    }
  }

  /**
    *  Καταχώρηση νέου στο RDBMS.
    *
    * @param  request    το HttpServletRequest από την σελίδα
    * @param  databaseId το αναγνωριστικό της βάσης που
    *                    θα χρησιμοποιηθεί
    * @return            κωδικό κατάστασης
   */
  public int doInsert(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "adminUsers", Director.AUTH_INSERT);

    if (auth != Director.AUTH_OK)
    {
      return auth;
    }

    String ausrCode = SwissKnife.grEncode( request.getParameter("ausrCode") ).trim(),
        ausrFirstname = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("ausrFirstname") ).trim() ),
        ausrLastname = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("ausrLastname") ).trim() ),
        ausrUsername = SwissKnife.grEncode( request.getParameter("ausrUsername") ).trim(),
        ausrPassword = SwissKnife.grEncode( request.getParameter("ausrPassword") ).trim(),
        ausrUserGroupId = SwissKnife.grEncode( request.getParameter("ausrUserGroupId") ).trim(),
        lastIpUsed = request.getRemoteAddr(),
        dLastUsed = SwissKnife.grEncode( request.getParameter("dateLastUsed") ).trim();
    
    SimpleDateFormat fixedDateFormat = new SimpleDateFormat("dd/MM/yyyy H:m:s");
    
    Timestamp dateLastUsed = null;
    
    if (dLastUsed.length() > 0) {
      try {
        dateLastUsed = new Timestamp(fixedDateFormat.parse(dLastUsed + " 00:00:00").getTime());
      }
      catch (Exception e) {
        dateLastUsed = null;
      }
    }
    
    String logCode = "";

    if (ausrCode.equals("") || ausrUsername.equals("")) return Director.STATUS_ERROR;

    String ausrFirstnameUp = SwissKnife.searchConvert(ausrFirstname),
           ausrLastnameUp = SwissKnife.searchConvert(ausrLastname);

    int status = Director.STATUS_OK;

    DbRet dbRet = null;
    
    Database database = bean.getDBConnection(databaseId);

    dbRet = database.beginTransaction(Database.TRANS_ISO_1);

    int prevTransIsolation = dbRet.retInt;

    if (dbRet.getNoError() == 1) {
      logCode = SwissKnife.buildPK();

      String insertLoginQuery  = "INSERT INTO users (" +
        "logCode,usrName, usrPasswd, usrAccessLevel, " +
        "lastIpUsed, dateLastUsed) " +
        "VALUES (" +
        "'" + logCode + "'," +
        "'" + ausrUsername    + "'," +
        "'" + ausrPassword + "'," +
        "" + ausrUserGroupId + "," +
        "'" + lastIpUsed    + "',";
      
      if (dateLastUsed == null) insertLoginQuery += "null";
      else insertLoginQuery += "'" + dateLastUsed + "'";
          
      insertLoginQuery += ")";

      //System.out.println(insertLoginQuery);
      dbRet = database.execQuery(insertLoginQuery);
    }
    else status = bean.STATUS_ERROR;

    if (dbRet.getNoError() == 1 ) {
      String insertUserQuery  
             = "INSERT INTO adminUsers (" +
               "ausrCode, ausrFirstname, ausrFirstnameUp, " +
               "ausrLastname, ausrLastnameUp, ausrLogCode) " +
               "VALUES (" +
               "'" + ausrCode    + "'," +
               "'" + ausrFirstname + "'," +
               "'" + ausrFirstnameUp + "'," +
               "'" + ausrLastname + "'," +
               "'" + ausrLastnameUp + "'," +
               "'" + logCode + "')";

      //System.out.println(insertUserQuery);
      dbRet = database.execQuery(insertUserQuery);
    }
    else
      status = Director.STATUS_ERROR;

    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    if (dbRet.getNoError() == 0) {
      status = Director.STATUS_ERROR;
    }

    bean.freeDBConnection(databaseId,database);
    
    return status;
  }


  /**
    *  Μεταβολή στο RDBMS.
    *
    * @param  request    το HttpServletRequest από την σελίδα
    * @param  databaseId το αναγνωριστικό της βάσης που
    *                    θα χρησιμοποιηθεί
    * @return            κωδικό κατάστασης
   */
  public int doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId +  ".authPassword", request);

    int auth = bean.auth(databaseId, authUsername, authPassword, "adminUsers", Director.AUTH_UPDATE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String ausrCode = SwissKnife.grEncode( request.getParameter("ausrCode") ).trim(),
        ausrFirstname = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("ausrFirstname") ).trim() ),
        ausrLastname = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("ausrLastname") ).trim() ),
        ausrUsername = SwissKnife.grEncode( request.getParameter("ausrUsername") ).trim(),
        ausrLogCode = SwissKnife.grEncode( request.getParameter("ausrLogCode") ).trim(),
        ausrPassword = SwissKnife.grEncode( request.getParameter("ausrPassword") ).trim(),
        ausrUserGroupId = SwissKnife.grEncode( request.getParameter("ausrUserGroupId") ).trim(),
        lastIpUsed = request.getRemoteAddr(),
        dLastUsed = SwissKnife.grEncode( request.getParameter("dateLastUsed") ).trim();
    
    SimpleDateFormat fixedDateFormat = new SimpleDateFormat("dd/MM/yyyy H:m:s");
    
    Timestamp dateLastUsed = null;
    
    if (dLastUsed.length() > 0) {
      try {
        dateLastUsed = new Timestamp(fixedDateFormat.parse(dLastUsed + " 00:00:00").getTime());
      }
      catch (Exception e) {
        dateLastUsed = null;
      }
    }

    if (ausrCode.equals("")) return Director.STATUS_ERROR;

    String ausrFirstnameUp = SwissKnife.searchConvert(ausrFirstname),
           ausrLastnameUp = SwissKnife.searchConvert(ausrLastname);

    Database database = bean.getDBConnection(databaseId);

    int status = Director.STATUS_OK;

    DbRet dbRet = null;

    dbRet = database.beginTransaction(Database.TRANS_ISO_1);

    int prevTransIsolation = dbRet.retInt;

    if (dbRet.getNoError() == 1) {
      String updateLoginQuery = "UPDATE users SET "
          + "usrName = '" + ausrUsername + "',"
          + "usrPasswd = '" + ausrPassword + "',"
          + "usrAccessLevel = " + ausrUserGroupId;
          
      if (dateLastUsed != null) updateLoginQuery += ",dateLastUsed = '" + dateLastUsed + "'";
      else updateLoginQuery += ",dateLastUsed = null";
          
      updateLoginQuery += " WHERE logCode = '" + ausrLogCode + "'";

      //System.out.println(updateLoginQuery);
      dbRet = database.execQuery(updateLoginQuery);
    }
    else status = Director.STATUS_ERROR;

    if (dbRet.getNoError() == 1) {
      String updateUserQuery = "UPDATE adminUsers SET "
          + "ausrFirstname = '" + ausrFirstname + "',"
          + "ausrFirstnameUp = '" + ausrFirstnameUp + "', "
          + "ausrLastname = '" + ausrLastname + "', "
          + "ausrLastnameUp = '" + ausrLastnameUp + "' "
          + "WHERE ausrCode = '" + ausrCode + "'";

      //System.out.println(updateUserQuery);
      dbRet = database.execQuery(updateUserQuery);
    }
    else status = Director.STATUS_ERROR;

    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    if (dbRet.getNoError() == 0) {
      status = Director.STATUS_ERROR;
    }

    bean.freeDBConnection(databaseId,database);
    return status;
  }

  /**
    *  Διαγραφή login + adminUser από RDBMS.
    *
    * @param  request    το HttpServletRequest από την σελίδα
    * @param  databaseId το αναγνωριστικό της βάσης που
    *                    θα χρησιμοποιηθεί
    * @return            κωδικό κατάστασης
   */
  public int doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId +  ".authPassword", request);

    int auth = bean.auth(databaseId, authUsername, authPassword, "adminUsers", Director.AUTH_DELETE);

    if (auth != Director.AUTH_OK)
    {
      return auth;
    }

    String ausrCode = SwissKnife.grEncode( request.getParameter("ausrCode") ).trim(),
           ausrLogCode = SwissKnife.grEncode( request.getParameter("ausrLogCode") ).trim();

    if (ausrCode.equals(""))
      return Director.STATUS_ERROR;

    Database database = bean.getDBConnection(databaseId);

    int status = Director.STATUS_OK;

    DbRet dbRet = null;

    dbRet = database.beginTransaction(Database.TRANS_ISO_1);

    int prevTransIsolation = dbRet.retInt;

    if (dbRet.getNoError() == 1)
    {
      String deleteUserQuery
             = "DELETE FROM adminUsers " +
               "WHERE ausrCode = '" + ausrCode + "'";

      //System.out.println(updateLoginQuery);
      dbRet = database.execQuery(deleteUserQuery);
    }
    else
      status = Director.STATUS_ERROR;

    if (dbRet.getNoError() == 1)
    {
      String deleteLoginQuery  
             = "DELETE FROM users " +
               "WHERE logCode = '" + ausrLogCode + "'";

      //System.out.println(updateLoginQuery);
      dbRet = database.execQuery(deleteLoginQuery);
    }
    else
      status = Director.STATUS_ERROR;

    dbRet = database.commitTransaction(dbRet.noError, prevTransIsolation);

    if (dbRet.getNoError() == 0) {
      status = Director.STATUS_ERROR;
    }

    bean.freeDBConnection(databaseId,database);
    
    return status;
  }
}