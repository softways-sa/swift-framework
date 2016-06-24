package gr.softways.dev.eshop.shippricelist;

import java.io.*;
import java.util.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class ShipPricelistServlet extends HttpServlet {

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
    else if (action.equals("INSERT")) {
      dbRet = doInsert(request, databaseId);
    }
    else if (action.equals("UPDATE")) {
      dbRet = doUpdate(request, databaseId);
    }
    else if (action.equals("DELETE")) {
      dbRet = doDelete(request, databaseId);
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

  private DbRet doInsert(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"ShipCostEntry",Director.AUTH_INSERT);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String localeLanguage = request.getParameter("localeLanguage"),
           localeCountry = request.getParameter("localeCountry");
           
    String SHCECode = null,
           SHCE_SHCRCode = SwissKnife.sqlEncode(request.getParameter("SHCE_SHCRCode")),
           SHCE_countryCode = SwissKnife.sqlEncode(request.getParameter("SHCE_countryCode")),
           SHCE_SHCMCode = SwissKnife.sqlEncode(request.getParameter("SHCE_SHCMCode")),
           SHCETitle = SwissKnife.sqlEncode(request.getParameter("SHCETitle")),
           SHCEText = SwissKnife.sqlEncode(request.getParameter("SHCEText")),
           SHCETextLG = SwissKnife.sqlEncode(request.getParameter("SHCETextLG")),
           SHCETextLG1 = SwissKnife.sqlEncode(request.getParameter("SHCETextLG1")),
           SHCETextLG2 = SwissKnife.sqlEncode(request.getParameter("SHCETextLG2")),
           SHCE_VAT_ID = request.getParameter("SHCE_VAT_ID");
           
    BigDecimal SHCEPrice = SwissKnife.parseBigDecimal(request.getParameter("SHCEPrice"), localeLanguage, localeCountry),
               SHCEVATPct = SwissKnife.parseBigDecimal(request.getParameter("SHCEVATPct"), localeLanguage, localeCountry);

    if (SHCEVATPct == null) SHCEVATPct = new BigDecimal("0");
    
    if (localeLanguage.equals("") || localeCountry.equals("") || SHCEPrice == null || SHCEVATPct == null) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    SHCECode = SwissKnife.buildPK();
    
    String query = "INSERT INTO ShipCostEntry ("
               + " SHCECode,SHCE_SHCRCode,SHCE_countryCode,SHCE_SHCMCode,SHCETitle,SHCEPrice,SHCEVATPct"
               + ",SHCEText,SHCETextLG,SHCETextLG1,SHCETextLG2";
    
    if (SHCE_VAT_ID != null) {
      query += ",SHCE_VAT_ID";
    }
               
    query += ") VALUES ("
        + "'" + SHCECode + "'";
               
    if (SHCE_SHCRCode.length() == 0) query += ",null";
    else query += ",'" + SHCE_SHCRCode + "'";
    
    if (SHCE_countryCode.length() == 0) query += ",null";
    else query += ",'" + SHCE_countryCode + "'";
    
    if (SHCE_SHCMCode.length() == 0) query += ",null";
    else query += ",'" + SHCE_SHCMCode + "'";
    
    query += ",'" + SHCETitle + "'"
           + ",'" + SHCEPrice + "'"
           + ",'" + SHCEVATPct + "'"
           + ",'" + SHCEText + "'"
           + ",'" + SHCETextLG + "'"
           + ",'" + SHCETextLG1 + "'"
           + ",'" + SHCETextLG2 + "'";
    
    if (SHCE_VAT_ID != null) {
      query += ",'" + SHCE_VAT_ID + "'";
    }
    
    query += ")";
    
    dbRet = database.execQuery(query);
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  private DbRet doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"ShipCostEntry",Director.AUTH_DELETE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String SHCECode = SwissKnife.sqlEncode(request.getParameter("SHCECode"));
    
    if (SHCECode.equals("")) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    String query = null;
    
    query = "DELETE FROM ShipCostEntry WHERE SHCECode = '" + SHCECode + "'";
    
    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(query);
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  private DbRet doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"ShipCostEntry",Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String localeLanguage = request.getParameter("localeLanguage"),
           localeCountry = request.getParameter("localeCountry");
           
    String SHCECode = SwissKnife.sqlEncode(request.getParameter("SHCECode")),
           SHCE_SHCRCode = SwissKnife.sqlEncode(request.getParameter("SHCE_SHCRCode")),
           SHCE_countryCode = SwissKnife.sqlEncode(request.getParameter("SHCE_countryCode")),
           SHCE_SHCMCode = SwissKnife.sqlEncode(request.getParameter("SHCE_SHCMCode")),
           SHCETitle = SwissKnife.sqlEncode(request.getParameter("SHCETitle")),
           SHCEText = SwissKnife.sqlEncode(request.getParameter("SHCEText")),
           SHCETextLG = SwissKnife.sqlEncode(request.getParameter("SHCETextLG")),
           SHCETextLG1 = SwissKnife.sqlEncode(request.getParameter("SHCETextLG1")),
           SHCETextLG2 = SwissKnife.sqlEncode(request.getParameter("SHCETextLG2")),
           SHCE_VAT_ID = request.getParameter("SHCE_VAT_ID");
           
           
    BigDecimal SHCEPrice = SwissKnife.parseBigDecimal(request.getParameter("SHCEPrice"), localeLanguage, localeCountry),
               SHCEVATPct = SwissKnife.parseBigDecimal(request.getParameter("SHCEVATPct"), localeLanguage, localeCountry);
    
    if (SHCEVATPct == null) SHCEVATPct = new BigDecimal("0");
    
    if (SHCECode.equals("") || localeLanguage.equals("") || localeCountry.equals("") || SHCEPrice == null || SHCEVATPct == null) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    if (dbRet.getNoError() == 1) {
      String query = "UPDATE ShipCostEntry SET";
      
      if (SHCE_SHCRCode.length() == 0) query += " SHCE_SHCRCode = null";
      else query += " SHCE_SHCRCode = '" + SHCE_SHCRCode + "'";
      
      if (SHCE_countryCode.length() == 0) query += ",SHCE_countryCode = null";
      else query += ",SHCE_countryCode = '" + SHCE_countryCode + "'";
      
      if (SHCE_SHCMCode.length() == 0) query += ",SHCE_SHCMCode = null";
      else query += ",SHCE_SHCMCode = '" + SHCE_SHCMCode + "'";
      
      query += ",SHCETitle = '" + SHCETitle + "'"
             + ",SHCEPrice = '" + SHCEPrice + "'"
             + ",SHCEVATPct = '" + SHCEVATPct + "'"
             + ",SHCEText = '" + SHCEText + "'"
             + ",SHCETextLG = '" + SHCETextLG + "'"
             + ",SHCETextLG1 = '" + SHCETextLG1 + "'"
             + ",SHCETextLG2 = '" + SHCETextLG2 + "'";
      
      if (SHCE_VAT_ID != null) {
        query += ",SHCE_VAT_ID = '" + SHCE_VAT_ID + "'";
      }
             
      query += " WHERE SHCECode = '" + SHCECode + "'";

      dbRet = database.execQuery(query);
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
}