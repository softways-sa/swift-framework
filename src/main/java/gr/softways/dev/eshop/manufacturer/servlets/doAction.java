package gr.softways.dev.eshop.manufacturer.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class doAction extends HttpServlet {

  private Director director;
    
  private String _charset = null;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    director = Director.getInstance();
  }

  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
         throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1"),
           databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId"),
           urlSuccess = request.getParameter("urlSuccess") == null ? "" :  request.getParameter("urlSuccess"),
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
    else status = Director.STATUS_ERROR;

    if (status < 0) {
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
   *  Καταχώρηση νέου manufacturer στο RDBMS.
   *
   * @param  request    το HttpServletRequest από την σελίδα
   * @param  databaseId το αναγνωριστικό της βάσης που
   *                    θα χρησιμοποιηθεί
   * @return            κωδικό κατάστασης
   */
  private int doInsert(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = director.auth(databaseId, authUsername, authPassword,
                             "manufact", Director.AUTH_INSERT);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String manufactId = SwissKnife.sqlEncode(request.getParameter("manufactId")),
           manufactAfm = SwissKnife.sqlEncode(request.getParameter("manufactAfm")),
           manufactName = SwissKnife.sqlEncode(request.getParameter("manufactName")),           
           manufactNameLG = SwissKnife.sqlEncode(request.getParameter("manufactNameLG")),           
           manufactAddress = SwissKnife.sqlEncode(request.getParameter("manufactAddress")),
           manufactArea = SwissKnife.sqlEncode(request.getParameter("manufactArea")),
           manufactCity = SwissKnife.sqlEncode(request.getParameter("manufactCity")),
           manufactRegion = SwissKnife.sqlEncode(request.getParameter("manufactRegion")),
           manufactCountry = SwissKnife.sqlEncode(request.getParameter("manufactCountry")),
           manufactZipCode = SwissKnife.sqlEncode(request.getParameter("manufactZipCode")),
           manufactPhone = SwissKnife.sqlEncode(request.getParameter("manufactPhone")),
           manufactEmail = SwissKnife.sqlEncode(request.getParameter("manufactEmail"));

    String manufactNameUp = SwissKnife.searchConvert(manufactName);
    String manufactNameUpLG = SwissKnife.searchConvert(manufactNameLG);
    
    if (manufactId.equals("")) return Director.STATUS_ERROR;

    String query = "INSERT INTO manufact (manufactId,manufactAfm," +
                   "manufactName,manufactNameUp,manufactNameLG," +
                   "manufactNameUpLG,manufactAddress,manufactArea," +
                   "manufactCity,manufactRegion,manufactCountry," +
                   "manufactZipCode,manufactPhone,manufactEmail) " +
                   "VALUES (" +
                   "'" + manufactId         + "'," +
                   "'" + manufactAfm        + "'," +
                   "'" + manufactName       + "'," +
                   "'" + manufactNameUp     + "'," +
                   "'" + manufactNameLG     + "'," +
                   "'" + manufactNameUpLG   + "'," +
                   "'" + manufactAddress    + "'," +
                   "'" + manufactArea       + "'," +
                   "'" + manufactCity       + "'," +
                   "'" + manufactRegion     + "'," +
                   "'" + manufactCountry    + "'," +
                   "'" + manufactZipCode    + "'," +
                   "'" + manufactPhone      + "'," +
                   "'" + manufactEmail      + "')";

    return executeQuery(databaseId, query);
  }

  /**
   *  Μεταβολή manufacturer στο RDBMS.
   *
   * @param  request    το HttpServletRequest από την σελίδα
   * @param  databaseId το αναγνωριστικό της βάσης που
   *                    θα χρησιμοποιηθεί
   * @return            κωδικό κατάστασης
   */
  private int doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = director.auth(databaseId, authUsername, authPassword,
                             "manufact", Director.AUTH_UPDATE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String manufactId = SwissKnife.sqlEncode(request.getParameter("manufactId")),
           manufactAfm = SwissKnife.sqlEncode(request.getParameter("manufactAfm")),
           manufactName = SwissKnife.sqlEncode(request.getParameter("manufactName")),           
           manufactNameLG = SwissKnife.sqlEncode(request.getParameter("manufactNameLG")),           
           manufactAddress = SwissKnife.sqlEncode(request.getParameter("manufactAddress")),
           manufactArea = SwissKnife.sqlEncode(request.getParameter("manufactArea")),
           manufactCity = SwissKnife.sqlEncode(request.getParameter("manufactCity")),
           manufactRegion = SwissKnife.sqlEncode(request.getParameter("manufactRegion")),
           manufactCountry = SwissKnife.sqlEncode(request.getParameter("manufactCountry")),
           manufactZipCode = SwissKnife.sqlEncode(request.getParameter("manufactZipCode")),
           manufactPhone = SwissKnife.sqlEncode(request.getParameter("manufactPhone")),
           manufactEmail = SwissKnife.sqlEncode(request.getParameter("manufactEmail"));

    String manufactNameUp = SwissKnife.searchConvert(manufactName);
    String manufactNameUpLG = SwissKnife.searchConvert(manufactNameLG);
    
    if (manufactId.equals("")) return Director.STATUS_ERROR;

    String query =  "UPDATE manufact SET " +
                    "manufactAfm = '"      + manufactAfm      + "'," +
                    "manufactName = '"     + manufactName     + "'," +
                    "manufactNameUp = '"   + manufactNameUp   + "'," +
                    "manufactNameLG = '"   + manufactNameLG   + "'," +
                    "manufactNameUpLG = '" + manufactNameUpLG + "'," +
                    "manufactAddress = '"  + manufactAddress  + "'," +
                    "manufactArea = '"     + manufactArea     + "'," +
                    "manufactCity = '"     + manufactCity     + "'," +
                    "manufactRegion = '"   + manufactRegion   + "'," +
                    "manufactCountry = '"  + manufactCountry  + "'," +
                    "manufactZipCode = '"  + manufactZipCode  + "'," +
                    "manufactPhone = '"    + manufactPhone    + "'," +
                    "manufactEmail = '"    + manufactEmail    + "'" +
                    " WHERE manufactId = '" + manufactId + "'";

    return executeQuery(databaseId, query);
  }

  /**
   * Διαγραφή κατασκευαστή
   */
  private int doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);
    
    int auth = director.auth(databaseId, authUsername, authPassword,
                             "manufact", Director.AUTH_DELETE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String manufactId = SwissKnife.sqlEncode(request.getParameter("manufactId"));

    if (manufactId.equals("")) return Director.STATUS_ERROR;

    String query = "DELETE FROM manufact"
                 + " WHERE manufactId = '" + manufactId + "'";

    return executeQuery(databaseId, query);
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
    Database database = director.getDBConnection(databaseId);

    int status = Director.STATUS_OK;
    
    DbRet dbRet = null;

    dbRet = database.execQuery(query);

    if (dbRet.getNoError() == 0) {
      status = Director.STATUS_ERROR;
    }

    director.freeDBConnection(databaseId,database);

    return status;
  }
}