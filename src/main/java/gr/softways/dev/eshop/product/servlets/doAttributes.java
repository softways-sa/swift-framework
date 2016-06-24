package gr.softways.dev.eshop.product.servlets;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.math.BigDecimal;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class doAttributes extends HttpServlet {

  private Director _director;
    
  private String _charset = null;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    _director = Director.getInstance();
  }

 //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
              throws ServletException, IOException {
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

  /**
   * Καταχώρηση στον πίνακα prdAttributes.
   *
   * @param prdACode ... prdANotes2 τα ονόματα των στηλών του πίνακα που θα
   *                                κάνουμε insert
   * @param database                για να μην χαθεί το ανοικτό connection
   *                                με τη βάση
   * @return                        το object dbRet
   */
  private DbRet insertToAttribute(String prdACode,String prdAPrdId,String prdAColorCode,
                                  String prdASizeCode,String prdAAtt1,String prdAAtt1LG,
                                  String prdAAtt2, String prdAAtt2LG,
                                  BigDecimal prdAWholesalePrc,BigDecimal prdAWholesalePrcEU,
                                  BigDecimal prdARetailPrc,BigDecimal prdARetailPrcEU,
                                  BigDecimal prdASlWholesalePrc,BigDecimal prdASlWholesalePrcEU,
                                  BigDecimal prdASlRetailPrc,BigDecimal prdASlRetailPrcEU,
                                  BigDecimal prdAHdWholesalePrc,BigDecimal prdAHdWholesalePrcEU,
                                  BigDecimal prdAHdRetailPrc,BigDecimal prdAHdRetailPrcEU,
                                  String prdAImg,Database database) {

    DbRet dbRet = new DbRet();
    dbRet.setRetry(1);
    int retries = 0;
    String query = "";

    for (;dbRet.getRetry() == 1 && retries < 10 ; retries++) {

      query = "INSERT INTO prdAttributes " +
              "(prdAId,prdACode,prdAPrdId,prdAColorCode,prdASizeCode," +
              "prdAAtt1,prdAAtt1LG,prdAAtt2,prdAAtt2LG," +
              "prdAWholesalePrc,prdAWholesalePrcEU,"    +
              "prdARetailPrc,prdARetailPrcEU,prdASlWholesalePrc,"         +
              "prdASlWholesalePrcEU,prdASlRetailPrc,prdASlRetailPrcEU,"   +
              "prdAHdWholesalePrc,prdAHdWholesalePrcEU,prdAHdRetailPrc,"  +
              "prdAHdRetailPrcEU,prdAImg,prdAStock,prdANotes,prdANotes2," +
              "prdALock) VALUES (" +
              "'" + SwissKnife.buildPK() + "'," +
              "'" + prdACode       + "'," +
              "'" + prdAPrdId      + "'," +
              "'" + prdAColorCode  + "'," +
              "'" + prdASizeCode   + "'," +
              "'" + prdAAtt1       + "'," +
              "'" + prdAAtt1LG     + "'," +
              "'" + prdAAtt2       + "'," +
              "'" + prdAAtt2LG     + "'," +
                    prdAWholesalePrc      + ","  +
                    prdAWholesalePrcEU    + ","  +
                    prdARetailPrc         + ","  +
                    prdARetailPrcEU       + ","  +
                    prdASlWholesalePrc    + ","  +
                    prdASlWholesalePrcEU  + ","  +
                    prdASlRetailPrc       + ","  +
                    prdASlRetailPrcEU     + ","  +
                    prdAHdWholesalePrc    + ","  +
                    prdAHdWholesalePrcEU  + ","  +
                    prdAHdRetailPrc       + ","  +
                    prdAHdRetailPrcEU     + ","  +
              "'" + prdAImg               + "'," +
                    "0,'','', '0')";

      dbRet = database.execQuery(query);
    }
    
    return dbRet;
  }

  /*
   * Πολλαπλές καταχωρήσεις στον πίνακα prdattributes
   *
   * @param  request    το HttpServletRequest από την σελίδα
   * @param  databaseId το αναγνωριστικό της βάσης που θα χρησιμοποιηθεί
   * @return            κωδικό κατάστασης
  */
  private DbRet doInsert(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    DbRet dbRet = new DbRet();
    
    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "prdAttributes", Director.AUTH_INSERT);

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String prdAPrdId = SwissKnife.sqlEncode(request.getParameter("prdId")),
           prdAColorCode = SwissKnife.sqlEncode(request.getParameter("prdAColorCode")),
           prdAAtt1 = SwissKnife.sqlEncode(request.getParameter("prdAAtt1")),
           prdAAtt1LG = SwissKnife.sqlEncode(request.getParameter("prdAAtt1LG")),
           prdAImg = SwissKnife.sqlEncode(request.getParameter("prdAImg"));

    String  prdASizeCode = "", prdAAtt2 = "", prdAAtt2LG = "", prdACode = "";
    
    BigDecimal prdAWholesalePrc = null, prdAWholesalePrcEU = null, prdARetailPrc = null,
               prdARetailPrcEU = null, prdASlWholesalePrc = null, prdASlWholesalePrcEU = null,
               prdASlRetailPrc = null, prdASlRetailPrcEU = null, prdAHdWholesalePrc = null,
               prdAHdWholesalePrcEU = null, prdAHdRetailPrc = null, prdAHdRetailPrcEU = null;

    String rows1 = SwissKnife.sqlEncode(request.getParameter("rows1"));
    if (rows1.length() == 0) rows1 = "0";
    
    int counter = Integer.parseInt(rows1);

    String localeLanguage = request.getParameter("localeLanguage"),
           localeCountry = request.getParameter("localeCountry");
    
    Database database = null;

    int prevTransIsolation = 0;

    database = _director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    
    prevTransIsolation = dbRet.getRetInt();

    if (prdAPrdId.equals("") || prdAColorCode.equals("")) {
      dbRet.setNoError(0);
    }
    else {
      for (int i=1; i<counter + 1; i++) {

        if (SwissKnife.sqlEncode(request.getParameter("attrComp" + i)).equals("0") && !SwissKnife.sqlEncode(request.getParameter("insertFlag" + i)).equals("1")) {
            prdASizeCode = SwissKnife.sqlEncode(request.getParameter("prdASizeCode" + i));
            prdAAtt2 = SwissKnife.grEncode(request.getParameter("prdAAtt2" + i));
            prdAAtt2LG = SwissKnife.grEncode(request.getParameter("prdAAtt2LG" + i));
            
            prdAWholesalePrc = SwissKnife.parseBigDecimal(request.getParameter("prdAWholesalePrc" + i), localeLanguage, localeCountry);
            prdAWholesalePrcEU = SwissKnife.parseBigDecimal(request.getParameter("prdAWholesalePrcEU" + i), localeLanguage, localeCountry);
            prdARetailPrc = SwissKnife.parseBigDecimal(request.getParameter("prdARetailPrc" + i), localeLanguage, localeCountry);
            prdARetailPrcEU = SwissKnife.parseBigDecimal(request.getParameter("prdARetailPrcEU" + i), localeLanguage, localeCountry);
            prdASlWholesalePrc = SwissKnife.parseBigDecimal(request.getParameter("prdASlWholesalePrc" + i), localeLanguage, localeCountry);
            prdASlWholesalePrcEU = SwissKnife.parseBigDecimal(request.getParameter("prdASlWholesalePrcEU" + i), localeLanguage, localeCountry);
            prdASlRetailPrc = SwissKnife.parseBigDecimal(request.getParameter("prdASlRetailPrc" + i), localeLanguage, localeCountry);
            prdASlRetailPrcEU = SwissKnife.parseBigDecimal(request.getParameter("prdASlRetailPrcEU" + i), localeLanguage, localeCountry);
            prdAHdWholesalePrc = SwissKnife.parseBigDecimal(request.getParameter("prdAHdWholesalePrc" + i), localeLanguage, localeCountry);
            prdAHdWholesalePrcEU = SwissKnife.parseBigDecimal(request.getParameter("prdAHdWholesalePrcEU" + i), localeLanguage, localeCountry);
            prdAHdRetailPrc = SwissKnife.parseBigDecimal(request.getParameter("prdAHdRetailPrc" + i), localeLanguage, localeCountry);
            prdAHdRetailPrcEU = SwissKnife.parseBigDecimal(request.getParameter("prdAHdRetailPrcEU" + i), localeLanguage, localeCountry);

            if (prdAWholesalePrc == null) prdAWholesalePrc = _zero;
            if (prdAWholesalePrcEU == null) prdAWholesalePrcEU = _zero;
            if (prdARetailPrc == null) prdARetailPrc = _zero;
            if (prdARetailPrcEU == null) prdARetailPrcEU = _zero;
            if (prdASlWholesalePrc == null) prdASlWholesalePrc = _zero;
            if (prdASlWholesalePrcEU == null) prdASlWholesalePrcEU = _zero;
            if (prdASlRetailPrc == null) prdASlRetailPrc = _zero;
            if (prdASlRetailPrcEU == null) prdASlRetailPrcEU = _zero;
            if (prdAHdWholesalePrc == null) prdAHdWholesalePrc = _zero;
            if (prdAHdWholesalePrcEU == null) prdAHdWholesalePrcEU = _zero;
            if (prdAHdRetailPrc == null) prdAHdRetailPrc = _zero;
            if (prdAHdRetailPrcEU == null) prdAHdRetailPrcEU = _zero;
            
            prdACode = prdAColorCode + "~" + prdASizeCode;

            //Εισαγωγή στον πίνακα prdAttributes
            dbRet = insertToAttribute(prdACode,prdAPrdId,prdAColorCode,prdASizeCode,
                                      prdAAtt1,prdAAtt1LG,prdAAtt2,prdAAtt2LG,
                                      prdAWholesalePrc,prdAWholesalePrcEU,prdARetailPrc,
                                      prdARetailPrcEU,prdASlWholesalePrc,prdASlWholesalePrcEU,
                                      prdASlRetailPrc,prdASlRetailPrcEU,prdAHdWholesalePrc,
                                      prdAHdWholesalePrcEU,prdAHdRetailPrc,prdAHdRetailPrcEU,
                                      prdAImg,database);
        }

        if (dbRet.getNoError() == 0) break;
      }
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    
    _director.freeDBConnection(databaseId,database);
    
    return dbRet;
  }


  /*
   * Eνημέρωση (UPDATE) του στον πίνακα prdAttributes
   *
   * @param  request    το HttpServletRequest από την σελίδα
   * @param  databaseId το αναγνωριστικό της βάσης που θα χρησιμοποιηθεί
   * @return            κωδικό κατάστασης
  */
  private DbRet doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    DbRet dbRet = new DbRet();
    
    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "prdAttributes", Director.AUTH_UPDATE);

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String prdAImg = SwissKnife.sqlEncode(request.getParameter("prdAImg"));

    String prdAId = "";
    
    BigDecimal prdAWholesalePrc = null, prdAWholesalePrcEU = null, prdARetailPrc = null,
               prdARetailPrcEU = null, prdASlWholesalePrc = null, prdASlWholesalePrcEU = null,
               prdASlRetailPrc = null, prdASlRetailPrcEU = null, prdAHdWholesalePrc = null,
               prdAHdWholesalePrcEU = null, prdAHdRetailPrc = null, prdAHdRetailPrcEU = null;

    String rows1 = SwissKnife.sqlEncode(request.getParameter("rows1"));
    if (rows1.length() == 0) rows1 = "0";
    
    int counter = Integer.parseInt(rows1) + 1;
    
    String localeLanguage = request.getParameter("localeLanguage"),
           localeCountry = request.getParameter("localeCountry");
    
    Database database = null;

    int prevTransIsolation = 0;

    database = _director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    
    prevTransIsolation = dbRet.getRetInt();

    for (int i=1; i<counter; i++){
      prdAId = SwissKnife.sqlEncode( request.getParameter("prdAId" + i));

      if (prdAId.equals("")) {
        dbRet.setNoError(0);
        break;
      }

      prdAWholesalePrc = SwissKnife.parseBigDecimal(request.getParameter("prdAWholesalePrc" + i), localeLanguage, localeCountry);
      prdAWholesalePrcEU = SwissKnife.parseBigDecimal(request.getParameter("prdAWholesalePrcEU" + i), localeLanguage, localeCountry);
      prdARetailPrc = SwissKnife.parseBigDecimal(request.getParameter("prdARetailPrc" + i), localeLanguage, localeCountry);
      prdARetailPrcEU = SwissKnife.parseBigDecimal(request.getParameter("prdARetailPrcEU" + i), localeLanguage, localeCountry);
      prdASlWholesalePrc = SwissKnife.parseBigDecimal(request.getParameter("prdASlWholesalePrc" + i), localeLanguage, localeCountry);
      prdASlWholesalePrcEU = SwissKnife.parseBigDecimal(request.getParameter("prdASlWholesalePrcEU" + i), localeLanguage, localeCountry);
      prdASlRetailPrc = SwissKnife.parseBigDecimal(request.getParameter("prdASlRetailPrc" + i), localeLanguage, localeCountry);
      prdASlRetailPrcEU = SwissKnife.parseBigDecimal(request.getParameter("prdASlRetailPrcEU" + i), localeLanguage, localeCountry);
      prdAHdWholesalePrc = SwissKnife.parseBigDecimal(request.getParameter("prdAHdWholesalePrc" + i), localeLanguage, localeCountry);
      prdAHdWholesalePrcEU = SwissKnife.parseBigDecimal(request.getParameter("prdAHdWholesalePrcEU" + i), localeLanguage, localeCountry);
      prdAHdRetailPrc = SwissKnife.parseBigDecimal(request.getParameter("prdAHdRetailPrc" + i), localeLanguage, localeCountry);
      prdAHdRetailPrcEU = SwissKnife.parseBigDecimal(request.getParameter("prdAHdRetailPrcEU" + i), localeLanguage, localeCountry);

      if (prdAWholesalePrc == null) prdAWholesalePrc = _zero;
      if (prdAWholesalePrcEU == null) prdAWholesalePrcEU = _zero;
      if (prdARetailPrc == null) prdARetailPrc = _zero;
      if (prdARetailPrcEU == null) prdARetailPrcEU = _zero;
      if (prdASlWholesalePrc == null) prdASlWholesalePrc = _zero;
      if (prdASlWholesalePrcEU == null) prdASlWholesalePrcEU = _zero;
      if (prdASlRetailPrc == null) prdASlRetailPrc = _zero;
      if (prdASlRetailPrcEU == null) prdASlRetailPrcEU = _zero;
      if (prdAHdWholesalePrc == null) prdAHdWholesalePrc = _zero;
      if (prdAHdWholesalePrcEU == null) prdAHdWholesalePrcEU = _zero;
      if (prdAHdRetailPrc == null) prdAHdRetailPrc = _zero;
      if (prdAHdRetailPrcEU == null) prdAHdRetailPrcEU = _zero;

      String query = "UPDATE prdAttributes SET " +
                     "prdAWholesalePrc = "       + prdAWholesalePrc     + ","  +
                     "prdAWholesalePrcEU = "     + prdAWholesalePrcEU   + ","  +
                     "prdARetailPrc = "          + prdARetailPrc        + ","  +
                     "prdARetailPrcEU = "        + prdARetailPrcEU      + ","  +
                     "prdASlWholesalePrc = "     + prdASlWholesalePrc   + ","  +
                     "prdASlWholesalePrcEU = "   + prdASlWholesalePrcEU + ","  +
                     "prdASlRetailPrc = "        + prdASlRetailPrc      + ","  +
                     "prdASlRetailPrcEU = "      + prdASlRetailPrcEU    + ","  +
                     "prdAHdWholesalePrc = "     + prdAHdWholesalePrc   + ","  +
                     "prdAHdWholesalePrcEU = "   + prdAHdWholesalePrcEU + ","  +
                     "prdAHdRetailPrc = "        + prdAHdRetailPrc      + ","  +
                     "prdAHdRetailPrcEU = "      + prdAHdRetailPrcEU    + ", "  +
                     "prdAImg = '"               + prdAImg              + "' "  +
                     "WHERE prdAId = '" + prdAId + "'";

      dbRet = database.execQuery(query);
      
      if (dbRet.getNoError() == 0) break;
    }

    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    
    _director.freeDBConnection(databaseId,database);
    
    return dbRet;
  }


  private DbRet doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    DbRet dbRet = new DbRet();
    
    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "prdAttributes", Director.AUTH_DELETE);

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String prdAId = "", attDelete = "",
           prdAPrdId = SwissKnife.sqlEncode(request.getParameter("prdId"));

    int flag = 0, loopFlag = 0, rows1 = 0;

    String rows = SwissKnife.sqlEncode(request.getParameter("rows1"));
    if (rows.length() == 0) rows = "0";
    
    int counter = Integer.parseInt(rows) + 1;

    if (prdAPrdId.equals("")) {
      dbRet.setNoError(0);
      
      return dbRet;
    }

    String query = "DELETE FROM prdAttributes " +
                   "WHERE prdAPrdId = '" + prdAPrdId + "'" +
                   " AND (prdAId = '";

    for (int i=1; i<counter; i++) {
      attDelete = SwissKnife.sqlEncode(request.getParameter("attrComp" + i));

      if (attDelete.equals("0")) {
        prdAId = SwissKnife.sqlEncode(request.getParameter("prdAId" + i));

        // Ελέγχει αν υπάρχει έστω και ένα τέτοιο χρώμα στον πίνακα PILines
        dbRet = rowsInPILines(databaseId, prdAId, prdAPrdId);
        if (dbRet.getNoError() == 0 || dbRet.getRetInt() > 0) {
          dbRet.setNoError(0);
          
          return dbRet;
        }
        
        // Ελέγχει αν υπάρχει έστω και ένα τέτοιο χρώμα στον πίνακα transaction
        dbRet = rowsInTransactions(databaseId, prdAId, prdAPrdId);
        if (dbRet.getNoError() == 0 || dbRet.getRetInt() > 0) {
          dbRet.setNoError(0);
          
          return dbRet;
        }
        
        if (flag == 0) {
          loopFlag = 1;
          query = query + prdAId + "' ";
          flag = 1;
        }
        else query = query + "OR prdAId = '" + prdAId + "'";
      }
    }

    query = query + ")";

    if (loopFlag == 1) return executeQuery(databaseId,query);
    else {
      dbRet.setNoError(0);
      return dbRet;
    }
  }


  /**
   *  Εκτέλεση query απ' ευθείας στην βάση.
   *
   * @param  databaseId το αναγνωριστικό της βάσης που
   *                    θα χρησιμοποιηθεί
   * @param  query      το query προς εκτέλεση
   * @return            τον κωδικό κατάστασης
   */
  private DbRet executeQuery(String databaseId, String query) {
    DbRet dbRet = new DbRet();
    
    Database database = _director.getDBConnection(databaseId);

    dbRet = database.execQuery(query);

    _director.freeDBConnection(databaseId, database);

    return dbRet;
  }
 
  private DbRet rowsInPILines(String databaseId, String prdAId, 
                              String prdAPrdId) {
    DbRet dbRet = new DbRet();
 
    Database database = _director.getDBConnection(databaseId);
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    int rows = 0;
    
    String query = "SELECT count(*) FROM PILines"
                 + " WHERE PILPrdAId = '" + prdAId + "'"
                 + " AND PILPrdId = '" + prdAPrdId + "'";

    try {
      queryDataSet.setQuery(new QueryDescriptor(database, query, null, true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();
      
      rows = queryDataSet.getInt(0);
     
      queryDataSet.close();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  private DbRet rowsInTransactions(String databaseId, String prdAId, 
                                   String prdAPrdId) {
    DbRet dbRet = new DbRet();
 
    Database database = _director.getDBConnection(databaseId);
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    int rows = 0;
    
    String query = "SELECT count(*) FROM transactions"
                 + " WHERE transPrdAttCode = '" + prdAId + "'"
                 + " AND prdId = '" + prdAPrdId + "'";

    try {
      queryDataSet.setQuery(new QueryDescriptor(database, query, null, true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();
      
      rows = queryDataSet.getInt(0);
     
      queryDataSet.close();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  private BigDecimal _zero = new BigDecimal(0);
}