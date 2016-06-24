package gr.softways.dev.eshop.area.servlets;

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

  private int doInsert(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = director.auth(databaseId, authUsername, authPassword,
                             "area", Director.AUTH_INSERT);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String areaCode = "", areaName = "", areaNameLG = "",
           areaZone = "", areaNameLG1 = "",  areaNameLG2 = "",
           areaNameLG3 = "", areaNameLG4 = "", areaNameLG5 = "";

    areaCode = SwissKnife.sqlEncode(request.getParameter("areaCode"));
    areaName = SwissKnife.sqlEncode(request.getParameter("areaName"));
    areaNameLG = SwissKnife.sqlEncode(request.getParameter("areaNameLG"));
    areaZone = SwissKnife.sqlEncode(request.getParameter("areaZone"));
    areaNameLG1 = SwissKnife.sqlEncode(request.getParameter("areaNameLG1"));
    areaNameLG2 = SwissKnife.sqlEncode(request.getParameter("areaNameLG2"));
    areaNameLG3 = SwissKnife.sqlEncode(request.getParameter("areaNameLG3"));
    areaNameLG4 = SwissKnife.sqlEncode(request.getParameter("areaNameLG4"));
    areaNameLG5 = SwissKnife.sqlEncode(request.getParameter("areaNameLG5"));

    if (areaName.equals("") || areaCode.equals("")) return Director.STATUS_ERROR;

    String query = "INSERT INTO area ("
                 + "areaCode,areaName,areaNameLG,areaZone"
                 + ",areaNameLG1,areaNameLG2,areaNameLG3,areaNameLG4,areaNameLG5"
                 + ") VALUES ("
                 + "'"  + areaCode   + "'"
                 + ",'" + areaName   + "'"
                 + ",'" + areaNameLG + "'"
                 + ",'" + areaZone + "'"
                 + ",'" + areaNameLG1 + "'"
                 + ",'" + areaNameLG2 + "'"
                 + ",'" + areaNameLG3 + "'"
                 + ",'" + areaNameLG4 + "'"
                 + ",'" + areaNameLG5 + "'"
                 + ")";

    return executeQuery(databaseId, query);
  }

  private int doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = director.auth(databaseId, authUsername, authPassword,
                             "area", Director.AUTH_UPDATE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String areaCode = "", areaName = "", areaNameLG = "",
           areaZone = "", areaNameLG1 = "",  areaNameLG2 = "",
           areaNameLG3 = "", areaNameLG4 = "", areaNameLG5 = "";

    areaCode = SwissKnife.sqlEncode(request.getParameter("areaCode"));
    areaName = SwissKnife.sqlEncode(request.getParameter("areaName"));
    areaNameLG = SwissKnife.sqlEncode(request.getParameter("areaNameLG"));
    areaZone = SwissKnife.sqlEncode(request.getParameter("areaZone"));
    areaNameLG1 = SwissKnife.sqlEncode(request.getParameter("areaNameLG1"));
    areaNameLG2 = SwissKnife.sqlEncode(request.getParameter("areaNameLG2"));
    areaNameLG3 = SwissKnife.sqlEncode(request.getParameter("areaNameLG3"));
    areaNameLG4 = SwissKnife.sqlEncode(request.getParameter("areaNameLG4"));
    areaNameLG5 = SwissKnife.sqlEncode(request.getParameter("areaNameLG5"));
    
    if (areaName.equals("") || areaCode.equals("")) return director.STATUS_ERROR;

    String query = "UPDATE area SET"
                 + " areaName = '" + areaName + "'"
                 + ",areaNameLG = '" + areaNameLG + "'"
                 + ",areaZone = '" + areaZone + "'"
                 + ",areaNameLG1 = '" + areaNameLG1 + "'"
                 + ",areaNameLG2 = '" + areaNameLG2 + "'"
                 + ",areaNameLG3 = '" + areaNameLG3 + "'"
                 + ",areaNameLG4 = '" + areaNameLG4 + "'"
                 + ",areaNameLG5 = '" + areaNameLG5 + "'"
                 + " WHERE areaCode = '" + areaCode + "'";

    return executeQuery(databaseId, query);
  }

  private int doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = director.auth(databaseId, authUsername, authPassword,
                             "area", Director.AUTH_DELETE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String areaCode = SwissKnife.sqlEncode(request.getParameter("areaCode"));
    
    String query = "DELETE FROM area" 
                 + " WHERE areaCode = '" + areaCode + "'";

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