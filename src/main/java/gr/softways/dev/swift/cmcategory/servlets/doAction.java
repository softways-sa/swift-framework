package gr.softways.dev.swift.cmcategory.servlets;

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
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1"),
           databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId"),
           urlSuccess = request.getParameter("urlSuccess") == null ? "" : request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");

    int status = Director.STATUS_ERROR;

    if (databaseId.equals("")) status = Director.STATUS_ERROR;
    else if (action.equals("INSERT")) status = doInsert(request, databaseId);
    else if (action.equals("UPDATE")) status = doUpdate(request, databaseId);
    else if (action.equals("DELETE")) status = doDelete(request, databaseId);
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
   *  Καταχώρηση νέας κατηγορίας στο RDBMS.
   *
   * @param  request    το HttpServletRequest από την σελίδα
   * @param  databaseId το αναγνωριστικό της βάσης που
   *                    θα χρησιμοποιηθεί
   * @return            κωδικό κατάστασης
   */
  private int doInsert(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

    int auth = director.auth(databaseId,authUsername,authPassword,"CMCategory",Director.AUTH_INSERT);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String CMCCode = SwissKnife.sqlEncode(request.getParameter("CMCCode")),
           CMCShowFlag = SwissKnife.sqlEncode(request.getParameter("CMCShowFlag")),
           CMCParentFlag = SwissKnife.sqlEncode(request.getParameter("CMCParentFlag")),
           CMCRank = SwissKnife.sqlEncode(request.getParameter("CMCRank")),
           CMCName = SwissKnife.sqlEncode(request.getParameter("CMCName")),
           CMCNameLG = SwissKnife.sqlEncode(request.getParameter("CMCNameLG")),
           CMCText = SwissKnife.sqlEncode(request.getParameter("CMCText")),
           CMCTextLG = SwissKnife.sqlEncode(request.getParameter("CMCTextLG")),
           CMCURL = SwissKnife.sqlEncode(request.getParameter("CMCURL")),
           CMCURLLG = SwissKnife.sqlEncode(request.getParameter("CMCURLLG"));

    String CMCIsProtected = request.getParameter("CMCIsProtected");
    
    String CMCNameUp = SwissKnife.searchConvert(CMCName),
           CMCNameUpLG = SwissKnife.searchConvert(CMCNameLG);
    
    String CMCNameLG1 = request.getParameter("CMCNameLG1"),
           CMCNameLG2 = request.getParameter("CMCNameLG2"),
           CMCNameLG3 = request.getParameter("CMCNameLG3"),
           CMCNameLG4 = request.getParameter("CMCNameLG4"),
           CMCNameLG5 = request.getParameter("CMCNameLG5"),
           CMCNameLG6 = request.getParameter("CMCNameLG6"),
           CMCNameLG7 = request.getParameter("CMCNameLG7"),
           CMCTextLG1 = request.getParameter("CMCTextLG1"),
           CMCTextLG2 = request.getParameter("CMCTextLG2"),
           CMCTextLG3 = request.getParameter("CMCTextLG3"),
           CMCTextLG4 = request.getParameter("CMCTextLG4"),
           CMCTextLG5 = request.getParameter("CMCTextLG5"),
           CMCTextLG6 = request.getParameter("CMCTextLG6"),
           CMCTextLG7 = request.getParameter("CMCTextLG7"),
           CMCURLLG1 = request.getParameter("CMCURLLG1"),
           CMCURLLG2 = request.getParameter("CMCURLLG2"),
           CMCURLLG3 = request.getParameter("CMCURLLG3"),
           CMCURLLG4 = request.getParameter("CMCURLLG4"),
           CMCURLLG5 = request.getParameter("CMCURLLG5"),
           CMCURLLG6 = request.getParameter("CMCURLLG6"),
           CMCURLLG7 = request.getParameter("CMCURLLG7");
    
    String CMCNameUpLG1 = SwissKnife.searchConvert(CMCNameLG1),
           CMCNameUpLG2 = SwissKnife.searchConvert(CMCNameLG2),
           CMCNameUpLG3 = SwissKnife.searchConvert(CMCNameLG3),
           CMCNameUpLG4 = SwissKnife.searchConvert(CMCNameLG4),
           CMCNameUpLG5 = SwissKnife.searchConvert(CMCNameLG5),
           CMCNameUpLG6 = SwissKnife.searchConvert(CMCNameLG6),
           CMCNameUpLG7 = SwissKnife.searchConvert(CMCNameLG7);
    
    if (CMCCode.equals("")) return Director.STATUS_ERROR;

    int tmpRank = 0;
    try {
      tmpRank = Integer.parseInt(CMCRank);
    }
    catch(Exception e){
      e.printStackTrace();
      tmpRank = 0;
    }
    
    String query = "INSERT INTO CMCategory " +
                   " (CMCCode,CMCName,CMCNameUp,CMCNameLG," +
                   "  CMCNameUpLG,CMCText,CMCTextLG," +
                   "  CMCShowFlag,CMCRank,CMCParentFlag,CMCURL,CMCURLLG";
                   
    if (CMCIsProtected != null) {
      query += ",CMCIsProtected";
    }
    
    if (CMCNameLG1 != null) {
      query += ",CMCNameLG1,CMCNameUpLG1";
    }
    if (CMCNameLG2 != null) {
      query += ",CMCNameLG2,CMCNameUpLG2";
    }
    if (CMCNameLG3 != null) {
      query += ",CMCNameLG3,CMCNameUpLG3";
    }
    if (CMCNameLG4 != null) {
      query += ",CMCNameLG4,CMCNameUpLG4";
    }
    if (CMCNameLG5 != null) {
      query += ",CMCNameLG5,CMCNameUpLG5";
    }
    if (CMCNameLG6 != null) {
      query += ",CMCNameLG6,CMCNameUpLG6";
    }
    if (CMCNameLG7 != null) {
      query += ",CMCNameLG7,CMCNameUpLG7";
    }
    
    if (CMCTextLG1 != null) {
      query += ",CMCTextLG1";
    }
    if (CMCTextLG2 != null) {
      query += ",CMCTextLG2";
    }
    if (CMCTextLG3 != null) {
      query += ",CMCTextLG3";
    }
    if (CMCTextLG4 != null) {
      query += ",CMCTextLG4";
    }
    if (CMCTextLG5 != null) {
      query += ",CMCTextLG5";
    }
    if (CMCTextLG6 != null) {
      query += ",CMCTextLG6";
    }
    if (CMCTextLG7 != null) {
      query += ",CMCTextLG7";
    }
    
    if (CMCURLLG1 != null) {
      query += ",CMCURLLG1";
    }
    if (CMCURLLG2 != null) {
      query += ",CMCURLLG2";
    }
    if (CMCURLLG3 != null) {
      query += ",CMCURLLG3";
    }
    if (CMCURLLG4 != null) {
      query += ",CMCURLLG4";
    }
    if (CMCURLLG5 != null) {
      query += ",CMCURLLG5";
    }
    if (CMCURLLG6 != null) {
      query += ",CMCURLLG6";
    }
    if (CMCURLLG7 != null) {
      query += ",CMCURLLG7";
    }
    
    query += ") VALUES (" +
             "'" + CMCCode + "'," +
             "'" + CMCName + "'," +
             "'" + CMCNameUp + "'," +
             "'" + CMCNameLG + "'," +
             "'" + CMCNameUpLG + "'," +
             "'" + CMCText + "'," +
             "'" + CMCTextLG + "'," +
             "'" + CMCShowFlag + "'," +
             "'" + tmpRank + "'," +
             "'" + CMCParentFlag + "'," +
             "'" + CMCURL + "'," +
             "'" + CMCURLLG + "'";
             
    if (CMCIsProtected != null) {
      query += ",'" + CMCIsProtected + "'";
    }
    
    if (CMCNameLG1 != null) {
      query += ",'" + SwissKnife.sqlEncode(CMCNameLG1) + "'"
             + ",'" + SwissKnife.sqlEncode(CMCNameUpLG1) + "'";
    }
    if (CMCNameLG2 != null) {
      query += ",'" + SwissKnife.sqlEncode(CMCNameLG2) + "'"
             + ",'" + SwissKnife.sqlEncode(CMCNameUpLG2) + "'";
    }
    if (CMCNameLG3 != null) {
      query += ",'" + SwissKnife.sqlEncode(CMCNameLG3) + "'"
             + ",'" + SwissKnife.sqlEncode(CMCNameUpLG3) + "'";
    }
    if (CMCNameLG4 != null) {
      query += ",'" + SwissKnife.sqlEncode(CMCNameLG4) + "'"
             + ",'" + SwissKnife.sqlEncode(CMCNameUpLG4) + "'";
    }
    if (CMCNameLG5 != null) {
      query += ",'" + SwissKnife.sqlEncode(CMCNameLG5) + "'"
             + ",'" + SwissKnife.sqlEncode(CMCNameUpLG5) + "'";
    }
    if (CMCNameLG6 != null) {
      query += ",'" + SwissKnife.sqlEncode(CMCNameLG6) + "'"
             + ",'" + SwissKnife.sqlEncode(CMCNameUpLG6) + "'";
    }
    if (CMCNameLG7 != null) {
      query += ",'" + SwissKnife.sqlEncode(CMCNameLG7) + "'"
             + ",'" + SwissKnife.sqlEncode(CMCNameUpLG7) + "'";
    }
    
    if (CMCTextLG1 != null) {
      query += ",'" + SwissKnife.sqlEncode(CMCTextLG1) + "'";
    }
    if (CMCTextLG2 != null) {
      query += ",'" + SwissKnife.sqlEncode(CMCTextLG2) + "'";
    }
    if (CMCTextLG3 != null) {
      query += ",'" + SwissKnife.sqlEncode(CMCTextLG3) + "'";
    }
    if (CMCTextLG4 != null) {
      query += ",'" + SwissKnife.sqlEncode(CMCTextLG4) + "'";
    }
    if (CMCTextLG5 != null) {
      query += ",'" + SwissKnife.sqlEncode(CMCTextLG5) + "'";
    }
    if (CMCTextLG6 != null) {
      query += ",'" + SwissKnife.sqlEncode(CMCTextLG6) + "'";
    }
    if (CMCTextLG7 != null) {
      query += ",'" + SwissKnife.sqlEncode(CMCTextLG7) + "'";
    }
    
    if (CMCURLLG1 != null) {
      query += ",'" + SwissKnife.sqlEncode(CMCURLLG1) + "'";
    }
    if (CMCURLLG2 != null) {
      query += ",'" + SwissKnife.sqlEncode(CMCURLLG2) + "'";
    }
    if (CMCURLLG3 != null) {
      query += ",'" + SwissKnife.sqlEncode(CMCURLLG3) + "'";
    }
    if (CMCURLLG4 != null) {
      query += ",'" + SwissKnife.sqlEncode(CMCURLLG4) + "'";
    }
    if (CMCURLLG5 != null) {
      query += ",'" + SwissKnife.sqlEncode(CMCURLLG5) + "'";
    }
    if (CMCURLLG6 != null) {
      query += ",'" + SwissKnife.sqlEncode(CMCURLLG6) + "'";
    }
    if (CMCURLLG7 != null) {
      query += ",'" + SwissKnife.sqlEncode(CMCURLLG7) + "'";
    }
    
    query += ")";

    return executeQuery(databaseId, query);
  }

  /**
   * Διώρθωση κατηγορίας
   */
  private int doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

    int auth = director.auth(databaseId,authUsername,authPassword,"CMCategory",Director.AUTH_UPDATE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String CMCCode = SwissKnife.sqlEncode(request.getParameter("CMCCode")),
           CMCShowFlag = SwissKnife.sqlEncode(request.getParameter("CMCShowFlag")),
           CMCParentFlag = SwissKnife.sqlEncode(request.getParameter("CMCParentFlag")),
           CMCRank = SwissKnife.sqlEncode(request.getParameter("CMCRank")),
           CMCName = SwissKnife.sqlEncode(request.getParameter("CMCName")),
           CMCNameLG = SwissKnife.sqlEncode(request.getParameter("CMCNameLG")),
           CMCText = SwissKnife.sqlEncode(request.getParameter("CMCText")),
           CMCTextLG = SwissKnife.sqlEncode(request.getParameter("CMCTextLG")),
           CMCURL = SwissKnife.sqlEncode(request.getParameter("CMCURL")),
           CMCURLLG = SwissKnife.sqlEncode(request.getParameter("CMCURLLG"));
           
    String CMCIsProtected = request.getParameter("CMCIsProtected");

    String CMCNameUp = SwissKnife.searchConvert(CMCName),
           CMCNameUpLG = SwissKnife.searchConvert(CMCNameLG);
    
    String CMCNameLG1 = request.getParameter("CMCNameLG1"),
           CMCNameLG2 = request.getParameter("CMCNameLG2"),
           CMCNameLG3 = request.getParameter("CMCNameLG3"),
           CMCNameLG4 = request.getParameter("CMCNameLG4"),
           CMCNameLG5 = request.getParameter("CMCNameLG5"),
           CMCNameLG6 = request.getParameter("CMCNameLG6"),
           CMCNameLG7 = request.getParameter("CMCNameLG7"),
           CMCTextLG1 = request.getParameter("CMCTextLG1"),
           CMCTextLG2 = request.getParameter("CMCTextLG2"),
           CMCTextLG3 = request.getParameter("CMCTextLG3"),
           CMCTextLG4 = request.getParameter("CMCTextLG4"),
           CMCTextLG5 = request.getParameter("CMCTextLG5"),
           CMCTextLG6 = request.getParameter("CMCTextLG6"),
           CMCTextLG7 = request.getParameter("CMCTextLG7"),
           CMCURLLG1 = request.getParameter("CMCURLLG1"),
           CMCURLLG2 = request.getParameter("CMCURLLG2"),
           CMCURLLG3 = request.getParameter("CMCURLLG3"),
           CMCURLLG4 = request.getParameter("CMCURLLG4"),
           CMCURLLG5 = request.getParameter("CMCURLLG5"),
           CMCURLLG6 = request.getParameter("CMCURLLG6"),
           CMCURLLG7 = request.getParameter("CMCURLLG7");
    
    String CMCNameUpLG1 = SwissKnife.searchConvert(CMCNameLG1),
           CMCNameUpLG2 = SwissKnife.searchConvert(CMCNameLG2),
           CMCNameUpLG3 = SwissKnife.searchConvert(CMCNameLG3),
           CMCNameUpLG4 = SwissKnife.searchConvert(CMCNameLG4),
           CMCNameUpLG5 = SwissKnife.searchConvert(CMCNameLG5),
           CMCNameUpLG6 = SwissKnife.searchConvert(CMCNameLG6),
           CMCNameUpLG7 = SwissKnife.searchConvert(CMCNameLG7);
           
    if (CMCCode.equals("")) return Director.STATUS_ERROR;
    
    int tmpRank = 0;
    try {
      tmpRank = Integer.parseInt(CMCRank);
    }
    catch(Exception e){
      e.printStackTrace();
      tmpRank = 0;
    }
    
    String query = "UPDATE CMCategory SET CMCName = '" + CMCName
                   + "', CMCNameUp = '" + CMCNameUp
                   + "', CMCNameLG = '" + CMCNameLG
                   + "', CMCNameUpLG = '" + CMCNameUpLG
                   + "', CMCText = '" + CMCText
                   + "', CMCTextLG = '" + CMCTextLG
                   + "', CMCRank = '" + tmpRank
                   + "', CMCShowFlag = '" + CMCShowFlag
                   + "', CMCParentFlag = '" + CMCParentFlag
                   + "', CMCURL = '" + CMCURL
                   + "', CMCURLLG = '" + CMCURLLG + "'";
                   
    if (CMCIsProtected != null) {
      query += ", CMCIsProtected = '" + CMCIsProtected + "'";
    }
    
    if (CMCNameLG1 != null) {
      query += ", CMCNameLG1 = '" + SwissKnife.sqlEncode(CMCNameLG1) + "'"
             + ", CMCNameUpLG1 = '" + SwissKnife.sqlEncode(CMCNameUpLG1) + "'";
    }
    if (CMCNameLG2 != null) {
      query += ", CMCNameLG2 = '" + SwissKnife.sqlEncode(CMCNameLG2) + "'"
             + ", CMCNameUpLG2 = '" + SwissKnife.sqlEncode(CMCNameUpLG2) + "'";
    }
    if (CMCNameLG3 != null) {
      query += ", CMCNameLG3 = '" + SwissKnife.sqlEncode(CMCNameLG3) + "'"
             + ", CMCNameUpLG3 = '" + SwissKnife.sqlEncode(CMCNameUpLG3) + "'";
    }
    if (CMCNameLG4 != null) {
      query += ", CMCNameLG4 = '" + SwissKnife.sqlEncode(CMCNameLG4) + "'"
             + ", CMCNameUpLG4 = '" + SwissKnife.sqlEncode(CMCNameUpLG4) + "'";
    }
    if (CMCNameLG5 != null) {
      query += ", CMCNameLG5 = '" + SwissKnife.sqlEncode(CMCNameLG5) + "'"
             + ", CMCNameUpLG5 = '" + SwissKnife.sqlEncode(CMCNameUpLG5) + "'";
    }
    if (CMCNameLG6 != null) {
      query += ", CMCNameLG6 = '" + SwissKnife.sqlEncode(CMCNameLG6) + "'"
             + ", CMCNameUpLG6 = '" + SwissKnife.sqlEncode(CMCNameUpLG6) + "'";
    }
    if (CMCNameLG7 != null) {
      query += ", CMCNameLG7 = '" + SwissKnife.sqlEncode(CMCNameLG7) + "'"
             + ", CMCNameUpLG7 = '" + SwissKnife.sqlEncode(CMCNameUpLG7) + "'";
    }
    
    if (CMCTextLG1 != null) {
      query += ", CMCTextLG1 = '" + SwissKnife.sqlEncode(CMCTextLG1) + "'";
    }
    if (CMCTextLG2 != null) {
      query += ", CMCTextLG2 = '" + SwissKnife.sqlEncode(CMCTextLG2) + "'";
    }
    if (CMCTextLG3 != null) {
      query += ", CMCTextLG3 = '" + SwissKnife.sqlEncode(CMCTextLG3) + "'";
    }
    if (CMCTextLG4 != null) {
      query += ", CMCTextLG4 = '" + SwissKnife.sqlEncode(CMCTextLG4) + "'";
    }
    if (CMCTextLG5 != null) {
      query += ", CMCTextLG5 = '" + SwissKnife.sqlEncode(CMCTextLG5) + "'";
    }
    if (CMCTextLG6 != null) {
      query += ", CMCTextLG6 = '" + SwissKnife.sqlEncode(CMCTextLG6) + "'";
    }
    if (CMCTextLG7 != null) {
      query += ", CMCTextLG7 = '" + SwissKnife.sqlEncode(CMCTextLG7) + "'";
    }
    
    if (CMCURLLG1 != null) {
      query += ", CMCURLLG1 = '" + SwissKnife.sqlEncode(CMCURLLG1) + "'";
    }
    if (CMCURLLG2 != null) {
      query += ", CMCURLLG2 = '" + SwissKnife.sqlEncode(CMCURLLG2) + "'";
    }
    if (CMCURLLG3 != null) {
      query += ", CMCURLLG3 = '" + SwissKnife.sqlEncode(CMCURLLG3) + "'";
    }
    if (CMCURLLG4 != null) {
      query += ", CMCURLLG4 = '" + SwissKnife.sqlEncode(CMCURLLG4) + "'";
    }
    if (CMCURLLG5 != null) {
      query += ", CMCURLLG5 = '" + SwissKnife.sqlEncode(CMCURLLG5) + "'";
    }
    if (CMCURLLG6 != null) {
      query += ", CMCURLLG6 = '" + SwissKnife.sqlEncode(CMCURLLG6) + "'";
    }
    if (CMCURLLG7 != null) {
      query += ", CMCURLLG7 = '" + SwissKnife.sqlEncode(CMCURLLG7) + "'";
    }
    
    query += " WHERE CMCCode = '" + CMCCode + "'";

    return executeQuery(databaseId, query);
  }

  private int doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

    int auth = 0;

    auth = director.auth(databaseId,authUsername,authPassword,"CMCategory",Director.AUTH_DELETE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String CMCCode = SwissKnife.sqlEncode(request.getParameter("CMCCode"));

    if (CMCCode.equals("")) return Director.STATUS_ERROR;

    DbRet dbRet = null;
    
    // get database connection
    Database database = director.getDBConnection(databaseId);

    // begin transaction
    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();

    String query = "";

    if (dbRet.noError == 1) {
      query = "DELETE FROM CMCategory"
            + " WHERE CMCCode = '" + CMCCode + "'";

      dbRet = database.execQuery(query);
    }

    // End transaction (commit or rollback)
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    director.freeDBConnection(databaseId,database);

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