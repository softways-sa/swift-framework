package gr.softways.dev.eshop.attr1.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class doAction extends HttpServlet {

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


  private DbRet doInsert(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "attributeTab", Director.AUTH_INSERT);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String attPrdId = SwissKnife.sqlEncode(request.getParameter("attPrdId")),
           attAttCode = SwissKnife.sqlEncode(request.getParameter("attAttCode")),
           attFlag = SwissKnife.sqlEncode(request.getParameter("attFlag")),
           attDisabled = SwissKnife.sqlEncode(request.getParameter("attDisabled")),
           attName = SwissKnife.sqlEncode(request.getParameter("attName")),
           attNameLG = SwissKnife.sqlEncode(request.getParameter("attNameLG")),
           attNameLG1 = SwissKnife.sqlEncode(request.getParameter("attNameLG1")),
           attNameLG2 = SwissKnife.sqlEncode(request.getParameter("attNameLG2")),
           attNameLG3 = SwissKnife.sqlEncode(request.getParameter("attNameLG3")),
           attNameLG4 = SwissKnife.sqlEncode(request.getParameter("attNameLG4")),
           attNameLG5 = SwissKnife.sqlEncode(request.getParameter("attNameLG5"));
                     
    if (attFlag.length() == 0) attFlag = "0";
    if (attDisabled.length() == 0) attDisabled = "0";
    
    if (attAttCode.equals("") || attPrdId.equals("")) {
      dbRet.setNoError(0);
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);

    String query = "INSERT INTO attributeTab ("
                 + "attCode,attPrdId,attAttCode,attFlag,attDisabled"
                 + ",attName,attNameLG,attNameLG1,attNameLG2"
                 + ",attNameLG3,attNameLG4,attNameLG5" 
                 + ") VALUES (" 
                 + "'" + SwissKnife.buildPK() + "'"
                 + ",'" + attPrdId + "'" 
                 + ",'" + attAttCode + "'"
                 + "," + attFlag + ""
                 + ",'" + attDisabled + "'"
                 + ",'" + attName + "'" 
                 + ",'" + attNameLG + "'"
                 + ",'" + attNameLG1 + "'"
                 + ",'" + attNameLG2 + "'"
                 + ",'" + attNameLG3 + "'"
                 + ",'" + attNameLG4 + "'"
                 + ",'" + attNameLG5 + "'"
                 + ")";

     dbRet = database.execQuery(query);
    
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }

  private DbRet doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "attributeTab", Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String attCode = SwissKnife.sqlEncode(request.getParameter("attCode")),
           attPrdId = SwissKnife.sqlEncode(request.getParameter("attPrdId")),
           attAttCode = SwissKnife.sqlEncode(request.getParameter("attAttCode")),
           attFlag = SwissKnife.sqlEncode(request.getParameter("attFlag")),
           attDisabled = SwissKnife.sqlEncode(request.getParameter("attDisabled")),
           attName = SwissKnife.sqlEncode(request.getParameter("attName")),
           attNameLG = SwissKnife.sqlEncode(request.getParameter("attNameLG")),
           attNameLG1 = SwissKnife.sqlEncode(request.getParameter("attNameLG1")),
           attNameLG2 = SwissKnife.sqlEncode(request.getParameter("attNameLG2")),
           attNameLG3 = SwissKnife.sqlEncode(request.getParameter("attNameLG3")),
           attNameLG4 = SwissKnife.sqlEncode(request.getParameter("attNameLG4")),
           attNameLG5 = SwissKnife.sqlEncode(request.getParameter("attNameLG5"));
                     
    if (attFlag.length() == 0) attFlag = "0";
    if (attDisabled.length() == 0) attDisabled = "0";
    
    if (attCode.equals("") || attAttCode.equals("") || attPrdId.equals("")) {
      dbRet.setNoError(0);
      return dbRet;
    }
    
    int prevTransIsolation = 0, rows = 0;

    Database database = _director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    
    prevTransIsolation = dbRet.getRetInt();

    /**
    // Ελέγχει αν υπάρχει έστω και ένα τέτοιο χρώμα στον πίνακα prdImports
    rows = bean.checkTableRows(request,databaseId,"PILines", "PILPrdAId", attPrdId);
    if (rows > 0){
      dbRet.setNoError(0);
    }
    else if (rows == 0 ){
      // Ελέγχει αν υπάρχει έστω και ένα τέτοιο χρώμα στον πίνακα transaction
      rows = bean.checkTableRows(request,databaseId,"transactions", "transPrdAttCode", attPrdId,"transPrdAttAtt1",attName);
      if (rows > 0) {
        dbRet.setNoError(0);
      }
    }**/
    
    String query = "UPDATE attributeTab SET" 
                 + " attFlag = " + attFlag
                 + ",attDisabled = '" + attDisabled + "'"
                 + ",attName = '" + attName + "'"
                 + ",attNameLG = '" + attNameLG + "'"
                 + ",attNameLG1 = '" + attNameLG1 + "'"
                 + ",attNameLG2 = '" + attNameLG2 + "'"
                 + ",attNameLG3 = '" + attNameLG3 + "'"
                 + ",attNameLG4 = '" + attNameLG4 + "'"
                 + ",attNameLG5 = '" + attNameLG5 + "'"
                 + " WHERE attCode = '" + attCode + "'";

    dbRet = database.execQuery(query);

    if (dbRet.getNoError() == 1) {
      query = "UPDATE prdAttributes SET" 
            + " prdAAtt1 = '" + attName + "'"
            + ",prdAAtt1LG = '" + attNameLG + "'"
            + ",prdAAtt1LG1 = '" + attNameLG1 + "'"
            + ",prdAAtt1LG2 = '" + attNameLG2 + "'"
            + ",prdAAtt1LG3 = '" + attNameLG3 + "'"
            + ",prdAAtt1LG4 = '" + attNameLG4 + "'"
            + ",prdAAtt1LG5 = '" + attNameLG5 + "'"
            + " WHERE prdAColorCode = '" + attAttCode + "'"
            + " AND prdAPrdId = '" + attPrdId + "'";
      
      dbRet = database.execQuery(query);
    }

    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    
    _director.freeDBConnection(databaseId,database);

    return dbRet;
  }


  private DbRet doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "attributeTab", Director.AUTH_DELETE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String attCode = SwissKnife.sqlEncode(request.getParameter("attCode")),
           attAttCode = SwissKnife.sqlEncode(request.getParameter("attAttCode")),
           attPrdId = SwissKnife.sqlEncode(request.getParameter("attPrdId"));

    if (attCode.equals("") || attAttCode.equals("") || attPrdId.equals("")) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    int rows = 0;
    
    String query = "SELECT count(*) FROM prdAttributes"
                 + " WHERE prdAColorCode = '" + attAttCode + "'"
                 + " AND prdAPrdId = '" + attPrdId + "'";

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
    
    if (rows == 0 && dbRet.getNoError() == 1) {
      query = "DELETE FROM attributeTab"
            + " WHERE attCode = '" + attCode + "'";

      dbRet = database.execQuery(query);
    }
    else dbRet.setNoError(0);

    _director.freeDBConnection(databaseId, database);

    return dbRet;
  }
}