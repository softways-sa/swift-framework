package gr.softways.dev.eshop.attr2.servlets;

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
                              "attributeTab2", Director.AUTH_INSERT);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String att2PrdId = SwissKnife.sqlEncode(request.getParameter("att2PrdId")),
           att2AttCode = SwissKnife.sqlEncode(request.getParameter("att2AttCode")),
           att2Flag = SwissKnife.sqlEncode(request.getParameter("att2Flag")),
           att2Disabled = SwissKnife.sqlEncode(request.getParameter("att2Disabled")),
           att2Name = SwissKnife.sqlEncode(request.getParameter("att2Name")),
           att2NameLG = SwissKnife.sqlEncode(request.getParameter("att2NameLG")),
           att2NameLG1 = SwissKnife.sqlEncode(request.getParameter("att2NameLG1")),
           att2NameLG2 = SwissKnife.sqlEncode(request.getParameter("att2NameLG2")),
           att2NameLG3 = SwissKnife.sqlEncode(request.getParameter("att2NameLG3")),
           att2NameLG4 = SwissKnife.sqlEncode(request.getParameter("att2NameLG4")),
           att2NameLG5 = SwissKnife.sqlEncode(request.getParameter("att2NameLG5"));
                     
    if (att2Flag.length() == 0) att2Flag = "0";
    if (att2Disabled.length() == 0) att2Disabled = "0";
    
    if (att2AttCode.equals("") || att2PrdId.equals("")) {
      dbRet.setNoError(0);
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);

    String query = "INSERT INTO attributeTab2 ("
                 + "att2Code,att2PrdId,att2AttCode,att2Flag,att2Disabled"
                 + ",att2Name,att2NameLG,att2NameLG1,att2NameLG2"
                 + ",att2NameLG3,att2NameLG4,att2NameLG5" 
                 + ") VALUES (" 
                 + "'" + SwissKnife.buildPK() + "'"
                 + ",'" + att2PrdId + "'" 
                 + ",'" + att2AttCode + "'"
                 + "," + att2Flag + ""
                 + ",'" + att2Disabled + "'"
                 + ",'" + att2Name + "'" 
                 + ",'" + att2NameLG + "'"
                 + ",'" + att2NameLG1 + "'"
                 + ",'" + att2NameLG2 + "'"
                 + ",'" + att2NameLG3 + "'"
                 + ",'" + att2NameLG4 + "'"
                 + ",'" + att2NameLG5 + "'"
                 + ")";

     dbRet = database.execQuery(query);
    
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }

  private DbRet doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "attributeTab2", Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String att2Code = SwissKnife.sqlEncode(request.getParameter("att2Code")),
           att2PrdId = SwissKnife.sqlEncode(request.getParameter("att2PrdId")),
           att2AttCode = SwissKnife.sqlEncode(request.getParameter("att2AttCode")),
           att2Flag = SwissKnife.sqlEncode(request.getParameter("att2Flag")),
           att2Disabled = SwissKnife.sqlEncode(request.getParameter("att2Disabled")),
           att2Name = SwissKnife.sqlEncode(request.getParameter("att2Name")),
           att2NameLG = SwissKnife.sqlEncode(request.getParameter("att2NameLG")),
           att2NameLG1 = SwissKnife.sqlEncode(request.getParameter("att2NameLG1")),
           att2NameLG2 = SwissKnife.sqlEncode(request.getParameter("att2NameLG2")),
           att2NameLG3 = SwissKnife.sqlEncode(request.getParameter("att2NameLG3")),
           att2NameLG4 = SwissKnife.sqlEncode(request.getParameter("att2NameLG4")),
           att2NameLG5 = SwissKnife.sqlEncode(request.getParameter("att2NameLG5"));
                     
    if (att2Flag.length() == 0) att2Flag = "0";
    if (att2Disabled.length() == 0) att2Disabled = "0";
    
    if (att2AttCode.equals("") || att2PrdId.equals("")) {
      dbRet.setNoError(0);
      return dbRet;
    }
    
    int prevTransIsolation = 0, rows = 0;

    Database database = _director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    
    prevTransIsolation = dbRet.getRetInt();

    String query = "UPDATE attributeTab2 SET" 
                 + " att2Flag = " + att2Flag
                 + ",att2Disabled = '" + att2Disabled + "'"
                 + ",att2Name = '" + att2Name + "'"
                 + ",att2NameLG = '" + att2NameLG + "'"
                 + ",att2NameLG1 = '" + att2NameLG1 + "'"
                 + ",att2NameLG2 = '" + att2NameLG2 + "'"
                 + ",att2NameLG3 = '" + att2NameLG3 + "'"
                 + ",att2NameLG4 = '" + att2NameLG4 + "'"
                 + ",att2NameLG5 = '" + att2NameLG5 + "'"
                 + " WHERE att2Code = '" + att2Code + "'";

    dbRet = database.execQuery(query);

    if (dbRet.getNoError() == 1) {
      query = "UPDATE prdAttributes SET" 
            + " prdAAtt2 = '" + att2Name + "'"
            + ",prdAAtt2LG = '" + att2NameLG + "'"
            + ",prdAAtt2LG1 = '" + att2NameLG1 + "'"
            + ",prdAAtt2LG2 = '" + att2NameLG2 + "'"
            + ",prdAAtt2LG3 = '" + att2NameLG3 + "'"
            + ",prdAAtt2LG4 = '" + att2NameLG4 + "'"
            + ",prdAAtt2LG5 = '" + att2NameLG5 + "'"
            + " WHERE prdASizeCode = '" + att2AttCode + "'"
            + " AND prdAPrdId = '" + att2PrdId + "'";
      
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
                              "attributeTab2", Director.AUTH_DELETE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String att2Code = SwissKnife.sqlEncode(request.getParameter("att2Code")),
           att2AttCode = SwissKnife.sqlEncode(request.getParameter("att2AttCode")),
           att2PrdId = SwissKnife.sqlEncode(request.getParameter("att2PrdId"));

    if (att2Code.equals("") || att2AttCode.equals("") || att2PrdId.equals("")) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    int rows = 0;
    
    String query = "SELECT count(*) FROM prdAttributes"
                 + " WHERE prdASizeCode = '" + att2AttCode + "'"
                 + " AND prdAPrdId = '" + att2PrdId + "'";

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
      query = "DELETE FROM attributeTab2"
            + " WHERE att2Code = '" + att2Code + "'";

      dbRet = database.execQuery(query);
    }
    else dbRet.setNoError(0);

    _director.freeDBConnection(databaseId, database);

    return dbRet;
  }
}