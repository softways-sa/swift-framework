package gr.softways.dev.eshop.category.servlets;

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
           urlSuccess = request.getParameter("urlSuccess") == null ? "" : request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");

    int status = Director.STATUS_ERROR;

    if (databaseId.equals(""))
      status = Director.STATUS_ERROR;
    else if (action.equals("INSERT"))
      status = doInsert(request, databaseId);
    else if (action.equals("UPDATE"))
      status = doUpdate(request, databaseId);
    else if (action.equals("DELETE"))
      status = doDelete(request, databaseId);
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
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = director.auth(databaseId, authUsername, authPassword,
                             "prdCategory", Director.AUTH_INSERT);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String catId = SwissKnife.sqlEncode(request.getParameter("catId")),
           catShowFlag = SwissKnife.sqlEncode(request.getParameter("catShowFlag")),
           catParentFlag = SwissKnife.sqlEncode(request.getParameter("catParentFlag")),
           name = SwissKnife.sqlEncode(request.getParameter("catName")),
           nameLG = SwissKnife.sqlEncode(request.getParameter("catNameLG")),
           keywords = SwissKnife.sqlEncode(request.getParameter("keywords")),
           keywordsLG = SwissKnife.sqlEncode(request.getParameter("keywordsLG"));

    String nameUp = SwissKnife.searchConvert(name);
    String keywordsUp = SwissKnife.searchConvert(keywords);

    String nameUpLG = SwissKnife.searchConvert(nameLG);
    String keywordsUpLG = SwissKnife.searchConvert(keywordsLG);
    
    if (catId.equals("")) return Director.STATUS_ERROR;

    String query = "INSERT INTO prdCategory " +
                   " (catId,catName,catNameUp,catNameLG," +
                   "  catNameUpLG,keywords,keywordsUp,keywordsLG," +
                   "  keywordsUpLG,catShowFlag,catParentFlag)" +
                   " VALUES (" +
                   "'" + catId         + "'," +
                   "'" + name          + "'," +
                   "'" + nameUp        + "'," +
                   "'" + nameLG        + "'," +
                   "'" + nameUpLG      + "'," +
                   "'" + keywords      + "'," +
                   "'" + keywordsUp    + "'," +
                   "'" + keywordsLG    + "'," +
                   "'" + keywordsUpLG  + "'," +
                   "'" + catShowFlag   + "'," +
                   "'" + catParentFlag + "')";

    return executeQuery(databaseId, query);
  }

  /**
   * Διώρθωση κατηγορίας
   */
  private int doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = director.auth(databaseId, authUsername, authPassword,
                             "prdCategory", Director.AUTH_UPDATE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String catId = SwissKnife.sqlEncode(request.getParameter("catId")),
           catShowFlag = SwissKnife.sqlEncode(request.getParameter("catShowFlag")),
           catParentFlag = SwissKnife.sqlEncode(request.getParameter("catParentFlag")),
           name = SwissKnife.sqlEncode(request.getParameter("catName")),
           nameLG = SwissKnife.sqlEncode(request.getParameter("catNameLG")),
           keywords = SwissKnife.sqlEncode(request.getParameter("keywords")),
           keywordsLG = SwissKnife.sqlEncode(request.getParameter("keywordsLG"));

    String nameUp = SwissKnife.searchConvert(name);
    String keywordsUp = SwissKnife.searchConvert(keywords);

    String nameUpLG = SwissKnife.searchConvert(nameLG);
    String keywordsUpLG = SwissKnife.searchConvert(keywordsLG);
    
    if (catId.equals("")) return Director.STATUS_ERROR;
    
    String query = "UPDATE prdCategory SET catName = '" + name
                   + "', catNameUp = '"     + nameUp
                   + "', catNameLG = '"     + nameLG
                   + "', catNameUpLG = '"   + nameUpLG
                   + "', keywords = '"      + keywords
                   + "', keywordsUp = '"    + keywordsUp
                   + "', keywordsLG = '"    + keywordsLG
                   + "', keywordsUpLG = '"  + keywordsUpLG
                   + "', catShowFlag = '"   + catShowFlag
                   + "', catParentFlag = '" + catParentFlag
                   + "' WHERE catId = '"    + catId + "'";

    return executeQuery(databaseId, query);
  }

  /**
   * Διαγραφή κατηγορίας
   */
  private int doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = 0;

    auth = director.auth(databaseId,authUsername,authPassword,
                         "prdCategory",Director.AUTH_DELETE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String catId = SwissKnife.sqlEncode(request.getParameter("catId"));

    if (catId.equals("")) return Director.STATUS_ERROR;

    DbRet dbRet = null;
    
    // get database connection
    Database database = director.getDBConnection(databaseId);

    // begin transaction
    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();

    String query = "";

    if (dbRet.getNoError() == 1) {
      query = "DELETE FROM prdCategoryInter"
            + " WHERE PCICatId = '" + catId + "'";
      
      dbRet = database.execQuery(query);
    }

    if (dbRet.noError == 1) {
      query = "DELETE FROM prdCategory"
            + " WHERE catId = '" + catId + "'";

      dbRet = database.execQuery(query);
    }

    // End transaction (commit or rollback)
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    director.freeDBConnection(databaseId,database);

    if (dbRet.getNoError() == 1)
      return Director.STATUS_OK;
    else
      return Director.STATUS_ERROR;
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