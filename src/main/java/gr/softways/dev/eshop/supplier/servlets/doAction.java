package gr.softways.dev.eshop.supplier.servlets;

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
   *  Καταχώρηση νέου στο RDBMS.
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
                             "supplier", Director.AUTH_INSERT);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String supplierId = SwissKnife.sqlEncode(request.getParameter("supplierId")),
           companyName = SwissKnife.sqlEncode(request.getParameter("companyName")),
           companyNameLG = SwissKnife.sqlEncode(request.getParameter("companyNameLG")),
           address = SwissKnife.sqlEncode(request.getParameter("address")),
           area = SwissKnife.sqlEncode(request.getParameter("area")),
           city = SwissKnife.sqlEncode(request.getParameter("city")),
           region = SwissKnife.sqlEncode(request.getParameter("region")),
           country = SwissKnife.sqlEncode(request.getParameter("country")),
           zipCode = SwissKnife.sqlEncode(request.getParameter("zipCode")),
           phone = SwissKnife.sqlEncode(request.getParameter("phone")),
           email = SwissKnife.sqlEncode(request.getParameter("email")),
           profession = SwissKnife.sqlEncode(request.getParameter("profession")),
           logCode = SwissKnife.sqlEncode(request.getParameter("logCode")),
           afm = SwissKnife.sqlEncode(request.getParameter("afm"));

    String companyNameUp = SwissKnife.searchConvert(companyName),
           companyNameUpLG = SwissKnife.searchConvert(companyNameLG);
    
    if (supplierId.equals("") || companyName.equals("")) {
      return Director.STATUS_ERROR;
    }

    String query  = "INSERT INTO supplier (supplierId," +
                    "companyName, companyNameUp, companyNameLG," +
                    "companyNameUpLG,address,area,city,region,"  +
                    "country,zipCode,phone,email,profession,"    +
                    "supLogCode,afm) VALUES (" +
                    "'" + supplierId      + "'," +
                    "'" + companyName     + "'," +
                    "'" + companyNameUp   + "'," +
                    "'" + companyNameLG   + "'," +
                    "'" + companyNameUpLG + "'," +
                    "'" + address         + "'," +
                    "'" + area            + "'," +
                    "'" + city            + "'," +
                    "'" + region          + "'," +
                    "'" + country         + "'," +
                    "'" + zipCode         + "'," +
                    "'" + phone           + "'," +
                    "'" + email           + "'," +
                    "'" + profession      + "'," +
                    "'" + logCode         + "'," +
                    "'" + afm             + "')";

    //System.out.println(insertUserQuery);

    return executeQuery(databaseId, query);
  }


  /**
    *  Μεταβολή στο RDBMS.
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
                             "supplier", Director.AUTH_UPDATE);

    String supplierId = SwissKnife.sqlEncode(request.getParameter("supplierId")),
           companyName = SwissKnife.sqlEncode(request.getParameter("companyName")),
           companyNameLG = SwissKnife.sqlEncode(request.getParameter("companyNameLG")),
           address = SwissKnife.sqlEncode(request.getParameter("address")),
           area = SwissKnife.sqlEncode(request.getParameter("area")),
           city = SwissKnife.sqlEncode(request.getParameter("city")),
           region = SwissKnife.sqlEncode(request.getParameter("region")),
           country = SwissKnife.sqlEncode(request.getParameter("country")),
           zipCode = SwissKnife.sqlEncode(request.getParameter("zipCode")),
           phone = SwissKnife.sqlEncode(request.getParameter("phone")),
           email = SwissKnife.sqlEncode(request.getParameter("email")),
           profession = SwissKnife.sqlEncode(request.getParameter("profession")),
           logCode = SwissKnife.sqlEncode(request.getParameter("logCode")),
           afm = SwissKnife.sqlEncode(request.getParameter("afm"));

    String companyNameUp = SwissKnife.searchConvert(companyName),
           companyNameUpLG = SwissKnife.searchConvert(companyNameLG);
    
    if (supplierId.equals("") || companyName.equals("")) {
      return Director.STATUS_ERROR;
    }
    
    String query =  "UPDATE supplier SET " +
                    "companyName = '"     + companyName     + "'," +
                    "companyNameUp = '"   + companyNameUp   + "', " +
                    "companyNameLG = '"   + companyNameLG   + "', " +
                    "companyNameUpLG = '" + companyNameUpLG + "', " +
                    "address = '"         + address         + "', " +
                    "area = '"            + area            + "', " +
                    "city = '"            + city            + "', " +
                    "region = '"          + region          + "', " +
                    "country = '"         + country         + "', " +
                    "zipCode = '"         + zipCode         + "', " +
                    "phone = '"           + phone           + "', " +
                    "email = '"           + email           + "', " +
                    "profession = '"      + profession      + "', " +
                    "afm = '"             + afm             + "', " +
                    "supLogCode = '"      + logCode          + "' " +
                    "WHERE supplierId = '" + supplierId + "'";

    //System.out.println(updateUserQuery);

    return executeQuery(databaseId, query);
  }

  /**
    *  Διαγραφή suplier από RDBMS.
    *
    * @param  request    το HttpServletRequest από την σελίδα
    * @param  databaseId το αναγνωριστικό της βάσης που
    *                    θα χρησιμοποιηθεί
    * @return            κωδικό κατάστασης
   */
  private int doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = director.auth(databaseId, authUsername, authPassword,
                             "supplier", Director.AUTH_DELETE);

    if (auth != Director.AUTH_OK) return auth;

    String supplierId = SwissKnife.sqlEncode(request.getParameter("supplierId"));

    if (supplierId.equals("")) return Director.STATUS_ERROR;

    String query = "DELETE FROM supplier" 
                 + " WHERE supplierId = '" + supplierId + "'";

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