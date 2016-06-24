package gr.softways.dev.eshop.shipcountry.servlets;

import java.io.*;
import java.util.*;
import java.math.BigDecimal;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class doAction extends HttpServlet {

  private Director bean;
    
  private String _charset = null;
  
  private BigDecimal _zero = new BigDecimal("0");

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

    int status = Director.STATUS_ERROR;

    if (databaseId.equals(""))
      status = Director.STATUS_ERROR;
    else if (action.equals("INSERT"))
      status = doInsert(request, databaseId);
    else if (action.equals("UPDATE"))
      status = doUpdate(request, databaseId);
    else if (action.equals("DELETE"))
      status = doDelete(request, databaseId);

    if (status < 0) {
      response.sendRedirect(urlNoAccess);
    }
    else if (status == Director.STATUS_OK) response.sendRedirect(urlSuccess);
    else response.sendRedirect(urlFailure);
  }

  private int doInsert(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "shipCountry", Director.AUTH_INSERT);

    if (auth != Director.AUTH_OK) {
      return auth;
    }
    
    String localeLanguage = SwissKnife.sqlEncode(request.getParameter("localeLanguage")),
           localeCountry = SwissKnife.sqlEncode(request.getParameter("localeCountry"));

    String SCCode = "", SCName = "", SCNameLG = "",
           SCNameLG1 = "", SCNameLG2 = "", SCNameLG3 = "",
           SCNameLG5 = "", SCNameLG4 = "",
           SC_SCZCode = "";

    int SCOrder = 0;
    
    SCName = SwissKnife.sqlEncode(request.getParameter("SCName"));
    SCNameLG = SwissKnife.sqlEncode(request.getParameter("SCNameLG"));
    SCNameLG1 = SwissKnife.sqlEncode(request.getParameter("SCNameLG1"));
    SCNameLG2 = SwissKnife.sqlEncode(request.getParameter("SCNameLG2"));
    SCNameLG3 = SwissKnife.sqlEncode(request.getParameter("SCNameLG3"));
    SCNameLG4 = SwissKnife.sqlEncode(request.getParameter("SCNameLG4"));
    SCNameLG5 = SwissKnife.sqlEncode(request.getParameter("SCNameLG5"));
    SC_SCZCode = SwissKnife.sqlEncode(request.getParameter("SC_SCZCode"));

    try {
      SCOrder = Integer.parseInt(request.getParameter("SCOrder"));
    }
    catch (Exception e) {
      SCOrder = 0;
    }
    
    BigDecimal SCVATPct = SwissKnife.parseBigDecimal(request.getParameter("SCVATPct"),localeLanguage,localeCountry);
    
    if (SCVATPct == null) SCVATPct = _zero;
    
    if (SCName.equals("")) return Director.STATUS_ERROR;
    
    String query = "INSERT INTO shipCountry ("
                 + "SCCode,SCName,SCNameLG"
                 + ",SCNameLG1,SCNameLG2,SCNameLG3,SCNameLG4"
                 + ",SCNameLG5,SCVATPct,SC_SCZCode,SCOrder"
                 + ") VALUES ("
                 + "'"  + SwissKnife.buildPK().trim() + "'"
                 + ",'" + SCName + "'"
                 + ",'" + SCNameLG + "'"
                 + ",'" + SCNameLG1 + "'"
                 + ",'" + SCNameLG2 + "'"
                 + ",'" + SCNameLG3 + "'"
                 + ",'" + SCNameLG4 + "'"
                 + ",'" + SCNameLG5 + "'"
                 + ","  + SCVATPct
                 + ",'" + SC_SCZCode + "'"
                 + "," + SCOrder
                 + ")";
                 
    return executeQuery(databaseId, query);
  }

  private int doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "shipCountry", Director.AUTH_UPDATE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }
    
    String localeLanguage = SwissKnife.sqlEncode(request.getParameter("localeLanguage")),
           localeCountry = SwissKnife.sqlEncode(request.getParameter("localeCountry"));

    String SCCode = "", SCName = "", SCNameLG = "",
           SCNameLG1 = "", SCNameLG2 = "", SCNameLG3 = "",
           SCNameLG5 = "", SCNameLG4 = "",
           SC_SCZCode = "";

    int SCOrder = 0;
           
    SCCode = SwissKnife.sqlEncode(request.getParameter("SCCode"));
    SCName = SwissKnife.sqlEncode(request.getParameter("SCName"));
    SCNameLG = SwissKnife.sqlEncode(request.getParameter("SCNameLG"));
    SCNameLG1 = SwissKnife.sqlEncode(request.getParameter("SCNameLG1"));
    SCNameLG2 = SwissKnife.sqlEncode(request.getParameter("SCNameLG2"));
    SCNameLG3 = SwissKnife.sqlEncode(request.getParameter("SCNameLG3"));
    SCNameLG4 = SwissKnife.sqlEncode(request.getParameter("SCNameLG4"));
    SCNameLG5 = SwissKnife.sqlEncode(request.getParameter("SCNameLG5"));
    SC_SCZCode = SwissKnife.sqlEncode(request.getParameter("SC_SCZCode"));

    try {
      SCOrder = Integer.parseInt(request.getParameter("SCOrder"));
    }
    catch (Exception e) {
      SCOrder = 0;
    }
    
    BigDecimal SCVATPct = SwissKnife.parseBigDecimal(request.getParameter("SCVATPct"),localeLanguage,localeCountry);
    
    if (SCVATPct == null) SCVATPct = _zero;

    if (SCCode.equals("") || SCName.equals("")) return Director.STATUS_ERROR;

    String query = "UPDATE shipCountry SET"
                 + " SCName = '" + SCName + "'"
                 + ",SCNameLG = '" + SCNameLG + "'"
                 + ",SCNameLG1 = '" + SCNameLG1 + "'"
                 + ",SCNameLG2 = '" + SCNameLG2 + "'"
                 + ",SCNameLG3 = '" + SCNameLG3 + "'"
                 + ",SCNameLG4 = '" + SCNameLG4 + "'"
                 + ",SCNameLG5 = '" + SCNameLG5 + "'"
                 + ",SCVATPct = " + SCVATPct
                 + ",SC_SCZCode = '" + SC_SCZCode + "'"
                 + ",SCOrder = " + SCOrder
                 + " WHERE SCCode = '" + SCCode + "'";

    return executeQuery(databaseId, query);
  }
  
  private int doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "shipCountry", Director.AUTH_DELETE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String SCCode = SwissKnife.sqlEncode(request.getParameter("SCCode"));

    String query = "DELETE FROM shipCountry"
                 + " WHERE SCCode = '" + SCCode + "'";

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
    Database database = bean.getDBConnection(databaseId);

    int status = Director.STATUS_OK;
    
    DbRet dbRet = null;

    dbRet = database.execQuery(query);

    if (dbRet.getNoError() == 0) {
      status = Director.STATUS_ERROR;
    }

    bean.freeDBConnection(databaseId,database);

    return status;
  }
}