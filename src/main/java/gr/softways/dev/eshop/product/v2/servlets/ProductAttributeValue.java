package gr.softways.dev.eshop.product.v2.servlets;

import java.io.*;
import java.util.*;

import java.math.BigDecimal;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class ProductAttributeValue extends HttpServlet {
  
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
           goLabel = request.getParameter("goLabel") == null ? "" : request.getParameter("goLabel"),
           databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId"),
           urlSuccess = request.getParameter("urlSuccess") == null ? "" :  request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");
    
    if (action.equals("INSERTMASTER")) {
      dbRet = doInsertMaster(request,databaseId);
    }
    else if (action.equals("INSERTSLAVE")) {
      dbRet = doInsertSlave(request,databaseId);
    }
    else if (action.equals("UPDATEMASTER")) {
      dbRet = doUpdateMaster(request,databaseId);
    }
    else if (action.equals("UPDATESLAVE")) {
      dbRet = doUpdateSlave(request,databaseId);
    }
    else if (action.equals("DELETEMASTER")) {
      dbRet = doDeleteMaster(request,databaseId);
    }
    else if (action.equals("DELETESLAVE")) {
      dbRet = doDeleteSlave(request,databaseId);
    }
            
    if (dbRet.getNoError() == 1) {   
      response.sendRedirect(urlSuccess + "&goLabel=" + goLabel);
    }
    else {
      if (dbRet.getAuthError() == 1)
        response.sendRedirect(urlNoAccess + "?authError=" + dbRet.getAuthErrorCode());
      else if (dbRet.get_validError() == 1)
        response.sendRedirect(urlFailure + "?validField=" + dbRet.getRetStr() + "&validError=" + dbRet.get_validErrorCode());
      else if (dbRet.getDbErrorCode() == 1)
        response.sendRedirect(urlFailure + "?dbMethod=" + dbRet.getRetStr() + "&dbError=" + dbRet.getDbErrorCode());
      else response.sendRedirect(urlFailure);
    }
  }

  private DbRet doInsertMaster(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"productMasterAttributeValue",Director.AUTH_INSERT);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String localeLanguage = request.getParameter("localeLanguage"),
           localeCountry = request.getParameter("localeCountry");
    
    String PMAV_PMACode = SwissKnife.sqlEncode(request.getParameter("PMACode")),
           PMAV_ATVACode = SwissKnife.sqlEncode(request.getParameter("PMAV_ATVACode")),
           PMAVImageName_s = SwissKnife.sqlEncode(request.getParameter("PMAVImageName_s")),
           PMAVImageName_b = SwissKnife.sqlEncode(request.getParameter("PMAVImageName_b")),
           PMAVRank = SwissKnife.sqlEncode(request.getParameter("PMAVRank"));

    BigDecimal PMAVStock = SwissKnife.parseBigDecimal(request.getParameter("PMAVStock"), localeLanguage, localeCountry),
               PMAVPrice = SwissKnife.parseBigDecimal(request.getParameter("PMAVPrice"), localeLanguage, localeCountry);
    
    String PMAVStockStr = PMAVStock == null ? null : "'" + PMAVStock.toString() + "'";
    String PMAVPriceStr = PMAVPrice == null ? null : "'" + PMAVPrice.toString() + "'";
    
    String PMAVID = request.getParameter("PMAVID");
    
    if (PMAV_PMACode.length() == 0 || PMAV_ATVACode.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
   
    QueryDataSet queryDataSet = new QueryDataSet();
   
    String query = "INSERT INTO productMasterAttributeValue (" 
                 + "PMAVCode,PMAV_PMACode,PMAV_ATVACode,PMAVStock,PMAVPrice,PMAVImageName_s,PMAVImageName_b,PMAVRank";
                 
    if (PMAVID != null) {
      query += ",PMAVID";
    }
    
    query += ") VALUES (" 
          + "'" + SwissKnife.buildPK() + "'"
          + ",'" + PMAV_PMACode + "'" 
          + ",'" + PMAV_ATVACode + "'"
          + "," + PMAVStockStr + ""
          + "," + PMAVPriceStr + ""
          + ",'" + PMAVImageName_s + "'"
          + ",'" + PMAVImageName_b + "'"
          + ",'" + getIntFromString(PMAVRank) + "'";
          
    if (PMAVID != null) {
      query += ",'" + SwissKnife.sqlEncode(PMAVID) + "'";
    }
    
    query += ")";
    
    dbRet = database.execQuery(query);
    
    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    _director.freeDBConnection(databaseId,database);
    
    return dbRet;
  }
  
  private DbRet doInsertSlave(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"PMASV",Director.AUTH_INSERT);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
     
    String localeLanguage = request.getParameter("localeLanguage"),
           localeCountry = request.getParameter("localeCountry");        
    
    String PMASV_PMAVCode = SwissKnife.sqlEncode(request.getParameter("PMAVCode")),
           PMASV_ATVACode = SwissKnife.sqlEncode(request.getParameter("PMASV_ATVACode")),
           PMASVRank = SwissKnife.sqlEncode(request.getParameter("PMASVRank"));

    BigDecimal PMASVStock = SwissKnife.parseBigDecimal(request.getParameter("PMASVStock"), localeLanguage, localeCountry),
               PMASVPrice = SwissKnife.parseBigDecimal(request.getParameter("PMASVPrice"), localeLanguage, localeCountry);
    
    String PMASVStockStr = PMASVStock == null ? null : "'" + PMASVStock.toString() + "'";
    String PMASVPriceStr = PMASVPrice == null ? null : "'" + PMASVPrice.toString() + "'";    
            
    if (PMASV_PMAVCode.length() == 0 || PMASV_ATVACode.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
   
    QueryDataSet queryDataSet = new QueryDataSet();
   
    String query = "INSERT INTO PMASV (" 
                 + "PMASVCode,PMASV_PMAVCode,PMASV_ATVACode,PMASVStock,PMASVPrice,PMASVRank" 
                 + ") VALUES (" 
                 + "'" + SwissKnife.buildPK() + "'"
                 + ",'" + PMASV_PMAVCode + "'" 
                 + ",'" + PMASV_ATVACode + "'"
                 + "," + PMASVStockStr + ""            
                 + "," + PMASVPriceStr + ""            
                 + "," + getIntFromString(PMASVRank) + ""
                 + ")";
                   
    dbRet = database.execQuery(query);
    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    _director.freeDBConnection(databaseId,database);        
    
    return dbRet;
  }
  
  private DbRet doUpdateMaster(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"productMasterAttributeValue",Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();
    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String localeLanguage = request.getParameter("localeLanguage"),
           localeCountry = request.getParameter("localeCountry");    
    
    String PMAVCode = SwissKnife.sqlEncode(request.getParameter("PMAVCode")),
           PMAV_PMACode = SwissKnife.sqlEncode(request.getParameter("PMACode")),
           PMAV_ATVACode = SwissKnife.sqlEncode(request.getParameter("PMAV_ATVACode")),
           PMAVImageName_s = SwissKnife.sqlEncode(request.getParameter("PMAVImageName_s")),
           PMAVImageName_b = SwissKnife.sqlEncode(request.getParameter("PMAVImageName_b")),
           PMAVRank = SwissKnife.sqlEncode(request.getParameter("PMAVRank"));
           
    String PMAVID = request.getParameter("PMAVID");

    BigDecimal PMAVStock = SwissKnife.parseBigDecimal(request.getParameter("PMAVStock"), localeLanguage, localeCountry),
               PMAVPrice = SwissKnife.parseBigDecimal(request.getParameter("PMAVPrice"), localeLanguage, localeCountry);
    
    String PMAVStockStr = PMAVStock == null ? null : "'" + PMAVStock.toString() + "'";
    String PMAVPriceStr = PMAVPrice == null ? null : "'" + PMAVPrice.toString() + "'";
   
    if (PMAVCode.length() == 0 || PMAV_PMACode.length() == 0 || PMAV_ATVACode.length() == 0) {
      dbRet.setNoError(0);
      return dbRet;
    }

    Database database = _director.getDBConnection(databaseId);

    String query = "UPDATE productMasterAttributeValue SET"
                 + " PMAVStock = " + PMAVStockStr + ","
                 + " PMAVPrice = " + PMAVPriceStr + ","
                 + " PMAVImageName_s = '" + PMAVImageName_s + "',"
                 + " PMAVImageName_b = '" + PMAVImageName_b + "',"
                 + " PMAVRank = '" + getIntFromString(PMAVRank) + "'";
                 
    if (PMAVID != null) {
      query += ",PMAVID = '" + SwissKnife.sqlEncode(PMAVID) + "'";
    }
    
    query += " WHERE PMAVCode = '" + PMAVCode + "'";
    
    dbRet = database.execQuery(query);
    
    _director.freeDBConnection(databaseId,database);

    return dbRet;
  }
  
  
  private DbRet doUpdateSlave(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "PMASV", Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();
    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String localeLanguage = request.getParameter("localeLanguage"),
           localeCountry = request.getParameter("localeCountry");    
    
    String PMAVCode = SwissKnife.sqlEncode(request.getParameter("PMAVCode")),
           PMASVCode = SwissKnife.sqlEncode(request.getParameter("PMASVCode")),
           PMASV_ATVACode = SwissKnife.sqlEncode(request.getParameter("PMASV_ATVACode")),
           PMASVRank = SwissKnife.sqlEncode(request.getParameter("PMASVRank"));
           

    BigDecimal PMASVStock = SwissKnife.parseBigDecimal(request.getParameter("PMASVStock"), localeLanguage, localeCountry),
               PMASVPrice = SwissKnife.parseBigDecimal(request.getParameter("PMASVPrice"), localeLanguage, localeCountry);
    
    String PMASVStockStr = PMASVStock == null ? null : "'" + PMASVStock.toString() + "'";
    String PMASVPriceStr = PMASVPrice == null ? null : "'" + PMASVPrice.toString() + "'";
   
    if (PMAVCode.length() == 0 || PMASVCode.length() == 0 || PMASV_ATVACode.length() == 0) {
      dbRet.setNoError(0);
      return dbRet;
    }

    Database database = _director.getDBConnection(databaseId);

    String query = "UPDATE PMASV SET"
                 + " PMASVStock = " + PMASVStockStr + ","
                 + " PMASVPrice = " + PMASVPriceStr + ","
                 + " PMASVRank = " + getIntFromString(PMASVRank) + ""
                 + " WHERE PMASVCode = '" + PMASVCode + "'";
    dbRet = database.execQuery(query);
    
    _director.freeDBConnection(databaseId,database);

    return dbRet;
  }  
  
  private DbRet doDeleteMaster(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "productMasterAttributeValue", Director.AUTH_DELETE);

    DbRet dbRet = new DbRet();
    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String PMAVCode = SwissKnife.sqlEncode(request.getParameter("PMAVCode"));
    
    if (PMAVCode.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
        
    Database database = _director.getDBConnection(databaseId);    

    String query = "DELETE FROM productMasterAttributeValue" 
                 + " WHERE PMAVCode = '" + PMAVCode + "'";
                   
    dbRet = database.execQuery(query);

    _director.freeDBConnection(databaseId,database);

    return dbRet;
  }  

  
  private DbRet doDeleteSlave(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "PMASV", Director.AUTH_DELETE);

    DbRet dbRet = new DbRet();
    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String PMASVCode = SwissKnife.sqlEncode(request.getParameter("PMASVCode"));
    
    if (PMASVCode.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
        
    Database database = _director.getDBConnection(databaseId);    

    String query = "DELETE FROM PMASV" 
                 + " WHERE PMASVCode = '" + PMASVCode + "'";
                   
    dbRet = database.execQuery(query);

    _director.freeDBConnection(databaseId,database);

    return dbRet;
  }  
  
  
  public int getIntFromString(String s) {
    int n = 0;
    try {
      n = Integer.parseInt(s);
    }
    catch(Exception e){
      n = 0;
    }
    return n;
  }
}
