package gr.softways.dev.eshop.filetemplate.servlets;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class doActionGen extends HttpServlet {
  
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
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1"),
           databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId"),
           urlSuccess = request.getParameter("urlSuccess") == null ? "" : request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");

    int status = Director.STATUS_OK;

    if (databaseId.equals(""))
      status = Director.STATUS_ERROR;
    else if (action.equals("INSERT"))
      status = doInsert(request, databaseId);
    else if (action.equals("UPDATE"))
      status = doUpdate(request,databaseId);
    else if (action.equals("DELETE"))
      status = doDelete(request,databaseId);

    if (status < 0) {
      response.sendRedirect(urlNoAccess);
    }
    else if (status == Director.STATUS_OK)
      response.sendRedirect(urlSuccess);
    else
      response.sendRedirect(urlFailure);
  }


  /**
   *  Καταχώρηση νέας γραμμογράφησης στο RDBMS.
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
                         "fileTemplate", Director.AUTH_INSERT);

    if (auth != Director.AUTH_OK) return auth;

    String FTemCode = SwissKnife.buildPK(),
           FTemName = SwissKnife.sqlEncode( request.getParameter("FTemName") ).trim(),
           FTemNameUp = SwissKnife.searchConvert(FTemName),
           FTemDelimiter = SwissKnife.sqlEncode( request.getParameter("FTemDelimiter") ).trim(),
           FTemFilename = SwissKnife.sqlEncode( request.getParameter("FTemFilename") ).trim(),
           FTemTablename = SwissKnife.sqlEncode( request.getParameter("FTemTablename") ).trim(),
           FTemRemTablename = SwissKnife.sqlEncode( request.getParameter("FTemRemTablename") ).trim();

    String query = "INSERT INTO fileTemplate (FTemCode, FTemName, FTemNameUp," +
                   "FTemDelimiter, FTemFilename, FTemTablename, FTemRemTablename) " +
                   "VALUES (" +
                   "'" + FTemCode      + "'," +
                   "'" + FTemName      + "'," +
                   "'" + FTemNameUp    + "'," +
                   "'" + FTemDelimiter + "'," +
                   "'" + FTemFilename  + "'," +
                   "'" + FTemTablename +  "'," +
                   "'" + FTemRemTablename + "'" +
                   ")";

    return executeQuery(databaseId,query);
  }


  /**
   *  Μεταβολή γραμμογράφησης στο RDBMS.
   *
   * @param  request    το HttpServletRequest από την σελίδα
   * @param  databaseId το αναγνωριστικό της βάσης που
   *                    θα χρησιμοποιηθεί
   * @return            κωδικό κατάστασης
   */
  public int doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "fileTemplate", Director.AUTH_UPDATE);

    if (auth != Director.AUTH_OK) return auth;

    String FTemCode = SwissKnife.sqlEncode( request.getParameter("FTemCode") ).trim(),
           FTemName = SwissKnife.sqlEncode( request.getParameter("FTemName") ).trim(),
           FTemNameUp = SwissKnife.searchConvert(FTemName),
           FTemDelimiter = SwissKnife.sqlEncode( request.getParameter("FTemDelimiter") ).trim(),
           FTemFilename = SwissKnife.sqlEncode( request.getParameter("FTemFilename") ).trim(),
           FTemTablename = SwissKnife.sqlEncode( request.getParameter("FTemTablename") ).trim(),
           FTemRemTablename = SwissKnife.sqlEncode( request.getParameter("FTemRemTablename") ).trim();

    if (FTemCode.equals("")) return Director.STATUS_ERROR;

    String query = "UPDATE fileTemplate SET " +
              "FTemName = '"        + FTemName      + "'," +
              "FTemNameUp = '"      + FTemNameUp    + "'," +
              "FTemDelimiter = '"   + FTemDelimiter + "'," +
              "FTemFilename = '"    + FTemFilename  + "'," +
              "FTemTablename = '"   + FTemTablename + "',"  +
              "FTemRemTablename = '" + FTemRemTablename + "'" +
              " WHERE FTemCode = '" + FTemCode      + "'";

    return executeQuery(databaseId,query);
  }


  /**
   * Διαγραφή γραμμογράφησης
   */
  private int doDelete(HttpServletRequest request, String databaseId) {
    DbRet dbRet = null;

    int prevTransIsolation = 0;

    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int authStatus = 0;

    authStatus = bean.auth(databaseId,authUsername,authPassword,
                           "fileTemplate", Director.AUTH_DELETE);

    if (authStatus <= 0) return authStatus;

    String FTemCode = SwissKnife.sqlEncode( request.getParameter("FTemCode") ).trim();

    if (FTemCode.equals("")) return Director.STATUS_ERROR;

    // get database connection
    Database database = bean.getDBConnection(databaseId);

    // begin transaction
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    
    prevTransIsolation = dbRet.getRetInt();

    String query1 = "DELETE FROM fileTemplateFormat " +
                    "WHERE FTeFFileTemCode = '" + FTemCode + "'";

    String query2 = "DELETE FROM fileTemplate " +
                    "WHERE FTemCode = '" + FTemCode + "'";

    dbRet = database.execQuery(query1);

    if (dbRet.getNoError() == 1) dbRet = database.execQuery(query2);

    // End transaction (commit or rollback)
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    bean.freeDBConnection(databaseId, database);
    
    if (dbRet.getNoError() == 1) return Director.STATUS_OK;
    else return Director.STATUS_ERROR;
  }


  /**
   *  Εκτέλεση query απ' ευθείας στην βάση.
   *
   * @param  databaseId το αναγνωριστικό της βάσης που
   *                    θα χρησιμοποιηθεί
   * @param  query      το query προς εκτέλεση
   * @return            τον κωδικό κατάστασης
   */
  private int executeQuery(String databaseId, String query) {
    Database database = bean.getDBConnection(databaseId);

    int status = Director.STATUS_OK;

    DbRet dbRet = null;

    dbRet = database.execQuery(query);

    if (dbRet.getNoError() == 0) {
      status = Director.STATUS_ERROR;
    }

    bean.freeDBConnection(databaseId, database);

    return status;
  }
}