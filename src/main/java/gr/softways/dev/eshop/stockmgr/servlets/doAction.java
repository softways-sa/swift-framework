package gr.softways.dev.eshop.stockmgr.servlets;

import java.io.*;
import java.util.*;
import java.math.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

import gr.softways.dev.eshop.stockmgr.StockMgr;

public class doAction extends HttpServlet {

  private Director _director;

  private String _charset = null;

  private BigDecimal _zero = new BigDecimal("0");
  
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
   * Εισάγει στον πίνακα prdImports και στον πίνακα PILines αν το προϊόν έχει
   * attributes και τέλος ενημερώνει τους πίνακες prdMonthly, product &
   * prdAttributes.
   */
  private DbRet doInsert(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    DbRet dbRet = new DbRet();
    
    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "prdImports", Director.AUTH_INSERT);

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    int prevTransIsolation = 0, counter = 0;
    
    String status = "0", query = null, updateStm = null, transId = null,
           PILPrdAId = "";
    
    String localeLanguage = request.getParameter("localeLanguage"),
           localeCountry = request.getParameter("localeCountry");
    
    String prdId = SwissKnife.sqlEncode(request.getParameter("prdId")),
           inOutFlag = SwissKnife.sqlEncode(request.getParameter("inOutFlag")),
           rows = SwissKnife.sqlEncode(request.getParameter("rows"));
           
    BigDecimal PILPrdAQua = _zero,
               quantity = SwissKnife.parseBigDecimal(request.getParameter("quantity"), localeLanguage, localeCountry),
               unitPrc = SwissKnife.parseBigDecimal(request.getParameter("unitPrc"), localeLanguage, localeCountry),
               unitPrcEU = SwissKnife.parseBigDecimal(request.getParameter("unitPrcEU"), localeLanguage, localeCountry),
               value = SwissKnife.parseBigDecimal(request.getParameter("value"), localeLanguage, localeCountry),
               valueEU = SwissKnife.parseBigDecimal(request.getParameter("valueEU"), localeLanguage, localeCountry),
               vatVal = SwissKnife.parseBigDecimal(request.getParameter("vatVal"), localeLanguage, localeCountry),
               vatValEU = SwissKnife.parseBigDecimal(request.getParameter("vatValEU"), localeLanguage, localeCountry);

    if (unitPrc == null) unitPrc = _zero;
    if (unitPrcEU == null) unitPrcEU = _zero;
    if (value == null) value = _zero;
    if (valueEU == null) valueEU = _zero;
    if (vatVal == null) vatVal = _zero;
    if (vatValEU == null) vatValEU = _zero;
    
    Timestamp importDate = SwissKnife.buildTimestamp(request.getParameter("importDateDay"),
                                                     request.getParameter("importDateMonth"),
                                                     request.getParameter("importDateYear"));

    if (quantity == null || importDate == null || prdId == null) {
      dbRet.setNoError(0);
      
      return dbRet;
    }

    Database database = null;
    
    database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    prevTransIsolation = dbRet.getRetInt();

    counter = Integer.parseInt(rows);
    
    updateStm = "UPDATE lTab SET lTabFld = '1' WHERE lTabCode = '1'";

    if (dbRet.getNoError() == 1){
      dbRet = database.execQuery(updateStm);
      
      if (dbRet.getNoError() == 1) {         
        // Εισαγωγή στον πίνακα prdImports
        dbRet = StockMgr.insertToImport(prdId,quantity,unitPrc,unitPrcEU,value,
                                        valueEU,vatVal,vatValEU,importDate,status,
                                        inOutFlag,database);
         if (dbRet.getNoError() == 1) {
          transId = dbRet.getRetStr();
          
          if (counter > 0) {
            for (int i=0; i<counter; i++) {
              PILPrdAId = SwissKnife.sqlEncode(request.getParameter("PILPrdAId" + i));
              PILPrdAQua = SwissKnife.parseBigDecimal(request.getParameter("PILPrdAQua" + i), localeLanguage, localeCountry);
              
              if (PILPrdAQua != null && PILPrdAQua.compareTo(_zero) == 1) {
                // Εισαγωγή στον πίνακα PILines
                dbRet = StockMgr.insertToPILines(transId,prdId,PILPrdAId,
                                                 PILPrdAQua,database);
                if (dbRet.getNoError() == 1) {
                  // Ενημέρωση του πίνακα prdattributes
                  dbRet = StockMgr.updatePrdAttributes(PILPrdAId,PILPrdAQua,
                                                       inOutFlag,database,databaseId);
                }
                else break;
              }
            }
          }
          
          if (dbRet.getNoError() == 1) {
            // Ενημέρωση του πίνακα product & prdMonthly
            dbRet = StockMgr.updateProduct(transId, prdId, quantity, value, valueEU,
                                           vatVal, vatValEU, importDate, inOutFlag,
                                           database, databaseId);
          }
        }
      }
    }

    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    _director.freeDBConnection(databaseId, database);

    return dbRet;
  }


  /**
   * Ενημερώνει τον πίνακα prdImports και τον πίνακα PILines αν το προϊόν έχει
   * attributes και τέλος ενημερώνει τους πίνακες prdMonthly, product &
   * prdAttributes.
   */
  private DbRet doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    DbRet dbRet = new DbRet();
    
    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "prdImports", Director.AUTH_UPDATE);

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    int prevTransIsolation = 0, retries = 0, counter = 0;

    String localeLanguage = request.getParameter("localeLanguage"),
           localeCountry = request.getParameter("localeCountry");
    
    String orderId = SwissKnife.sqlEncode(request.getParameter("orderId")),
           transId =SwissKnife.sqlEncode(request.getParameter("transId")),
           prdId = SwissKnife.sqlEncode(request.getParameter("prdId")),
           inOutFlag = SwissKnife.sqlEncode(request.getParameter("inOutFlag")),
           status = SwissKnife.sqlEncode(request.getParameter("status")),
           rows = SwissKnife.sqlEncode(request.getParameter("rows"));
           
    BigDecimal PILPrdAQua = _zero,
               quantity = SwissKnife.parseBigDecimal(request.getParameter("quantity"), localeLanguage, localeCountry),
               unitPrc = SwissKnife.parseBigDecimal(request.getParameter("unitPrc"), localeLanguage, localeCountry),
               unitPrcEU = SwissKnife.parseBigDecimal(request.getParameter("unitPrcEU"), localeLanguage, localeCountry),
               value = SwissKnife.parseBigDecimal(request.getParameter("value"), localeLanguage, localeCountry),
               valueEU = SwissKnife.parseBigDecimal(request.getParameter("valueEU"), localeLanguage, localeCountry),
               vatVal = SwissKnife.parseBigDecimal(request.getParameter("vatVal"), localeLanguage, localeCountry),
               vatValEU = SwissKnife.parseBigDecimal(request.getParameter("vatValEU"), localeLanguage, localeCountry);
           
    if (unitPrc == null) unitPrc = _zero;
    if (unitPrcEU == null) unitPrcEU = _zero;
    if (value == null) value = _zero;
    if (valueEU == null) valueEU = _zero;
    if (vatVal == null) vatVal = _zero;
    if (vatValEU == null) vatValEU = _zero;
    
    Timestamp importDate = SwissKnife.buildTimestamp(request.getParameter("importDateDay"),
                                                     request.getParameter("importDateMonth"),
                                                     request.getParameter("importDateYear"));
    
    if (quantity == null || importDate == null || prdId == null) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    String updateStm = null, PILCode = "", query1 = "", PILPrdAId = "";
    
    counter = Integer.parseInt(rows);
    
    Database database = null;
    
    database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    prevTransIsolation = dbRet.getRetInt();

    updateStm = "UPDATE lTab SET lTabFld = '1' WHERE lTabCode = '1'";
    
    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(updateStm);
    }
    
    if (dbRet.getNoError() == 1) {
      if (counter > 0) {
        for (int i=0; i<counter; i++) {
          PILCode = SwissKnife.sqlEncode(request.getParameter("PILCode" + i));
          PILPrdAId = SwissKnife.sqlEncode(request.getParameter("PILPrdAId" + i));
          PILPrdAQua = SwissKnife.parseBigDecimal(request.getParameter("PILPrdAQua" + i), localeLanguage, localeCountry);
          
          if (PILPrdAQua != null && PILPrdAQua.compareTo(_zero) == 1) {
            if (!PILCode.equals("")) {
              dbRet = StockMgr.rollBackPrdAttributes(PILCode, transId, database);
              
              if (dbRet.getNoError() == 1) {
                query1 = "UPDATE PILines SET " +
                         "PILPrdAQua = "  + PILPrdAQua  + ","  +
                         "PILLock = '0' " +
                         "WHERE PILCode = '" + PILCode + "'";
                
                 dbRet = database.execQuery(query1);
              }
              else break;
              
              if (dbRet.getNoError() == 1) {
                // Ενημέρωση του πίνακα prdattributes
                dbRet = StockMgr.updatePrdAttributes(PILPrdAId,PILPrdAQua,
                                                        inOutFlag,database,databaseId);
              }
              else break;
            }
            else {
                if (dbRet.getNoError() == 1) {
                  // Εισαγωγή στον πίνακα PILines
                  dbRet = StockMgr.insertToPILines(transId,prdId,PILPrdAId,
                                                      PILPrdAQua,database);
                }
                if (dbRet.getNoError() == 1) {
                  // Ενημέρωση του πίνακα prdattributes
                  dbRet = StockMgr.updatePrdAttributes(PILPrdAId,PILPrdAQua,
                                                          inOutFlag,database,databaseId);
                }
                else break;
            }
          }
        }
      }
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = StockMgr.rollBackProduct(transId, database, databaseId);
    }
    
    if (dbRet.getNoError() == 1) {
      String query = "UPDATE prdImports SET " +
                     "transId = '"    + transId   + "'," +
                     "orderId = '"    + orderId   + "'," +
                     "prdId = '"      + prdId     + "'," +
                     "quantity = "    + quantity  + ","  +
                     "unitPrc = "     + unitPrc   + ","  +
                     "unitPrcEU = "   + unitPrcEU + ","  +
                     "valueDR = "     + value     + ","  +
                     "valueEU = "     + valueEU   + ","  +
                     "vatVal = "      + vatVal    + ","  +
                     "vatValEU = "    + vatValEU  + ","  +
                     "importDate = '" + importDate + "'," +
                     "status = '"     + status    + "'," +
                     "inOutFlag = '"  + inOutFlag + "'," +
                     "importLock = '0' " +
                     "WHERE transId = '" + transId + "'";

      dbRet = database.execQuery(query);
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = StockMgr.updateProduct(transId, prdId, quantity, value, valueEU, vatVal,
                                        vatValEU, importDate, inOutFlag,
                                        database, databaseId);
    }

    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    
    _director.freeDBConnection(databaseId, database);

    return dbRet;
  }


  private DbRet doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    DbRet dbRet = new DbRet();
    
    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "prdImports", Director.AUTH_DELETE);

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    Database database = null;
    
    int prevTransIsolation = 0, retries = 0, counter = 0, flag = 0, loopFlag = 0;

    String localeLanguage = request.getParameter("localeLanguage"),
           localeCountry = request.getParameter("localeCountry");
    
    boolean updateOK = false;

    String transId = SwissKnife.sqlEncode(request.getParameter("transId")),
           orderId = SwissKnife.sqlEncode(request.getParameter("orderId")),
           prdId = SwissKnife.sqlEncode(request.getParameter("prdId")),
           rows = SwissKnife.sqlEncode(request.getParameter("rows"));

    String updateStm = null, PILCode = "", query1 = "", PILPrdAId = "";
    
    BigDecimal PILPrdAQua = _zero;

    counter = Integer.parseInt(rows);
    
    database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    prevTransIsolation = dbRet.getRetInt();

    updateStm = "UPDATE lTab SET lTabFld = '1' WHERE lTabCode = '1'";
    
    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(updateStm);
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = StockMgr.rollBackProduct(transId, database, databaseId);
    }

    if (dbRet.getNoError() == 1) {
      if (counter > 0) {
        query1 = "DELETE FROM PILines WHERE (PILCode = '";
        
        for (int i=0; i<counter; i++) {
          PILCode = SwissKnife.sqlEncode(request.getParameter("PILCode" + i));
          PILPrdAId = SwissKnife.sqlEncode(request.getParameter("PILPrdAId" + i));
          PILPrdAQua = SwissKnife.parseBigDecimal(request.getParameter("PILPrdAQua" + i), localeLanguage, localeCountry);
          
          if (PILPrdAQua != null && PILPrdAQua.compareTo(_zero) == 1) {
            if (!PILCode.equals("")) {
              dbRet = StockMgr.rollBackPrdAttributes(PILCode, transId, database);
              
              if (dbRet.getNoError() == 1) {
                if (flag == 0) {
                  loopFlag = 1;
                  query1 = query1 + PILCode + "' ";
                  flag = 1;
                }
                else query1 = query1 + "OR PILCode = '" + PILCode + "'";
              }
              else break;
            }
          }
        }
        
        query1 = query1 + ")";
      }
    }
    
    if (dbRet.getNoError() == 1 && loopFlag == 1) {
      dbRet = database.execQuery(query1);
    }

    if (dbRet.getNoError() == 1) {
      String query = "DELETE FROM prdImports WHERE transId = '" + transId + "'";
      dbRet = database.execQuery(query);
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
}