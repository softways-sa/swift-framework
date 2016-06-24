package gr.softways.dev.eshop.filetemplateformat.servlets;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

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
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1"),
           databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId"),
           urlSuccess = request.getParameter("urlSuccess") == null ? "" : request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");

    int status = Director.STATUS_OK;

    if (databaseId.equals(""))
      status = bean.STATUS_ERROR;
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
                         "fileTemplateFormat", Director.AUTH_INSERT);

    if (auth != bean.AUTH_OK) return auth;

    String FTeFCode = SwissKnife.buildPK(),
           FTeFFileTemCode = SwissKnife.sqlEncode( request.getParameter("FTeFFileTemCode") ).trim(),
           FTeFColName = SwissKnife.sqlEncode( request.getParameter("FTeFColName") ).trim(),
           FTeFRemColName = SwissKnife.sqlEncode( request.getParameter("FTeFRemColName") ).trim(),
           FTeFColOrder = SwissKnife.sqlEncode( request.getParameter("FTeFColOrder") ).trim(),
           FTeFColType = SwissKnife.sqlEncode( request.getParameter("FTeFColType") ).trim(),
           FTeFRemColType = SwissKnife.sqlEncode( request.getParameter("FTeFRemColType") ).trim(),
           FTeFColLength = SwissKnife.sqlEncode( request.getParameter("FTeFColLength") ).trim(),
           FTeFColPKFlag = SwissKnife.sqlEncode( request.getParameter("FTeFColPKFlag") ).trim(),
           FTeFRemColPKFlag = SwissKnife.sqlEncode( request.getParameter("FTeFRemColPKFlag") ).trim(),
           FTeFColFormat = SwissKnife.sqlEncode( request.getParameter("FTeFColFormat") ).trim(),
           FTeFRemColFormat = SwissKnife.sqlEncode( request.getParameter("FTeFRemColFormat") ).trim();

    if (FTeFColLength.equals("")) {
      FTeFColLength = "0";
    }

    String query = "INSERT INTO fileTemplateFormat (FTeFCode,"    +
                   "FTeFFileTemCode, FTeFColName, FTeFColOrder, " +
                   "FTeFColType, FTeFColLength,FTeFColPKFlag, "   +
                   "FTeFRemColName, FTeFRemColType, " +
                   "FTeFRemColPKFlag,FTeFColFormat,FTeFRemColFormat) " +
                   "VALUES (" +
                   "'" + FTeFCode        + "'," +
                   "'" + FTeFFileTemCode + "'," +
                   "'" + FTeFColName     + "'," +
                         FTeFColOrder    +  "," +
                   "'" + FTeFColType     + "'," +
                         FTeFColLength   +  "," +
                   "'" + FTeFColPKFlag   +  "'," +
                   "'" + FTeFRemColName  +  "'," +
                   "'" + FTeFRemColType  +  "'," +
                   "'" + FTeFRemColPKFlag + "'," +
                   "'" + FTeFColFormat    + "'," +
                   "'" + FTeFRemColFormat + "')";

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
                         "fileTemplateFormat", Director.AUTH_UPDATE);
    
    if (auth != Director.AUTH_OK) return auth;

    String FTeFCode = SwissKnife.sqlEncode( request.getParameter("FTeFCode") ).trim(),
           FTeFFileTemCode = SwissKnife.sqlEncode( request.getParameter("FTeFFileTemCode") ).trim(),
           FTeFColName = SwissKnife.sqlEncode( request.getParameter("FTeFColName") ).trim(),
           FTeFRemColName = SwissKnife.sqlEncode( request.getParameter("FTeFRemColName") ).trim(),
           FTeFColOrder = SwissKnife.sqlEncode( request.getParameter("FTeFColOrder") ).trim(),
           FTeFColType = SwissKnife.sqlEncode( request.getParameter("FTeFColType") ).trim(),
           FTeFRemColType = SwissKnife.sqlEncode( request.getParameter("FTeFRemColType") ).trim(),
           FTeFColLength = SwissKnife.sqlEncode( request.getParameter("FTeFColLength") ).trim(),
           FTeFRemColLength = SwissKnife.sqlEncode( request.getParameter("FTeFRemColLength") ).trim(),
           FTeFColPKFlag = SwissKnife.sqlEncode( request.getParameter("FTeFColPKFlag") ).trim(),
           FTeFRemColPKFlag = SwissKnife.sqlEncode(  request.getParameter("FTeFRemColPKFlag") ).trim(),
           FTeFColFormat = SwissKnife.sqlEncode( request.getParameter("FTeFColFormat") ).trim(),
           FTeFRemColFormat = SwissKnife.sqlEncode( request.getParameter("FTeFRemColFormat") ).trim();

    if (FTeFCode.equals("")) return Director.STATUS_ERROR;

    if (FTeFColLength.equals("")) {
      FTeFColLength = "0";
    }

    String query = "UPDATE fileTemplateFormat SET " +
              "FTeFColName = '"     + FTeFColName     + "'," +
              "FTeFRemColName = '"     + FTeFRemColName     + "'," +
              "FTeFColOrder = "     + FTeFColOrder    + ","  +
              "FTeFColType = '"     + FTeFColType     + "'," +
              "FTeFRemColType = '"     + FTeFRemColType     + "'," +
              "FTeFColLength = "    + FTeFColLength   + ","  +
              "FTeFColPKFlag = '"   + FTeFColPKFlag   + "',"  +
              "FTeFRemColPKFlag = '"   + FTeFRemColPKFlag   + "'," +
              "FTeFColFormat = '" + FTeFColFormat + "'," +
              "FTeFRemColFormat = '" + FTeFRemColFormat + "'" +
              " WHERE FTeFCode = '" + FTeFCode        + "'";

    return executeQuery(databaseId,query);
  }


  /**
   * Διαγραφή γραμμογράφησης
   */
  public int doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);
    
    int authStatus = 0;

    authStatus = bean.auth(databaseId,authUsername,authPassword,
                           "fileTemplateFormat", Director.AUTH_DELETE);

    if (authStatus <= 0) return authStatus;

    String FTeFCode = SwissKnife.sqlEncode( request.getParameter("FTeFCode") ).trim();

    if (FTeFCode.equals("")) return bean.STATUS_ERROR;

    String delete = "DELETE FROM fileTemplateFormat " +
                    "WHERE FTeFCode = '" + FTeFCode + "'";

    return executeQuery(databaseId,delete);
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