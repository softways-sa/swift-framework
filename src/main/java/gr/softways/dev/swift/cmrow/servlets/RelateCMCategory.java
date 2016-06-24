/*
 * RelateCMCategory.java
 *
 * Created on 6 Απρίλιος 2006, 9:55 πμ
 */

package gr.softways.dev.swift.cmrow.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;
/**
 *
 * @author haris
 * @version
 */
public class RelateCMCategory extends HttpServlet {
  
  /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param request servlet request
   * @param response servlet response
   */
  
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
    
    if (action.equals("INSERT")) {
      dbRet = doInsert(request,databaseId);
    }
    else if (action.equals("UPDATE")) {
      dbRet = doUpdate(request,databaseId);
    }
    else if (action.equals("UPDATE_PRIMARY")) {
      dbRet = doUpdatePrimary(request,databaseId);
    }
    else if (action.equals("DELETE")) {
      dbRet = doDelete(request,databaseId);
    }
            
    if (dbRet.getNoError() == 1) {   
      response.sendRedirect(urlSuccess);
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

  private DbRet doInsert(HttpServletRequest request, String databaseId) {  
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "CMCRelCMR", Director.AUTH_INSERT);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
     
    String CCCR_CMRCode = SwissKnife.sqlEncode(request.getParameter("CMRCode")),
           CCCR_CMCCode = SwissKnife.sqlEncode(request.getParameter("CMCCode")),
           CCCRPrimary = "",
           CCCRRank = "0";
    
    if (CCCR_CMRCode.length() == 0 || CCCR_CMCCode.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
   
    QueryDataSet queryDataSet = new QueryDataSet();
    
       
    //elegxe an to arthro exei kuria kathgoria
    String select = "SELECT * FROM CMCRelCMR"
                  + " WHERE CCCR_CMRCode = '" + CCCR_CMRCode + "'" 
                  + " AND CCCRPrimary = '1'";
    
    int rows = 0;
    
    try {
      // κλείσε το query ώστε να το ανοίξουμε ξανά
      if (queryDataSet.isOpen()) queryDataSet.close();
    
      queryDataSet.setQuery(new QueryDescriptor(database, select, null, true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();
      
      rows = queryDataSet.getRowCount();
     
      queryDataSet.close();
    }    
    catch (Exception e) {
      e.printStackTrace();
    }
    
    if (rows > 0) {
      CCCRPrimary = "0";
    }  
    else {
      CCCRPrimary = "1";
    }
    
    String query = "INSERT INTO CMCRelCMR (" 
                 + "CCCRCode,CCCR_CMCCode,CCCR_CMRCode,CCCRPrimary,CCCRRank,CCCRIsHidden" 
                 + ") VALUES (" 
                 + "'" + SwissKnife.buildPK() + "'"
                 + ",'" + CCCR_CMCCode + "'"
                 + ",'" + CCCR_CMRCode + "'" 
                 + ",'" + CCCRPrimary + "'"
                 + "," + CCCRRank + ",'0'"            
                 + ")";
                   
    dbRet = database.execQuery(query);
    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    _director.freeDBConnection(databaseId,database);        
    
    return dbRet;
  }
  
  private DbRet doUpdatePrimary(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "CMCRelCMR", Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();
    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String CCCR_CMRCode = SwissKnife.sqlEncode(request.getParameter("CMRCode")),
           CCCR_CMCCode = SwissKnife.sqlEncode(request.getParameter("CMCCode"));
    
    if (CCCR_CMRCode.length() == 0 || CCCR_CMCCode.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }

    Database database = _director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    
    String query = "UPDATE CMCRelCMR"
                 + " SET CCCRPrimary = '0'" 
                 + " WHERE CCCR_CMRCode = '" + CCCR_CMRCode + "'" 
                 + " AND CCCRPrimary = '1'";
    
    dbRet = database.execQuery(query);
    
    if (dbRet.getNoError() == 1) {
      query = "UPDATE CMCRelCMR" 
            + " SET CCCRPrimary = '1'"
            + " WHERE CCCR_CMRCode = '" + CCCR_CMRCode + "'" 
            + " AND CCCR_CMRCode = '" + CCCR_CMRCode + "'";
    }
    
    dbRet = database.execQuery(query);
    
    
    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    _director.freeDBConnection(databaseId,database);            

    return dbRet;
  }
  
  private DbRet doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "CMCRelCMR", Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();
    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String CCCR_CMRCode = SwissKnife.sqlEncode(request.getParameter("CMRCode")),
           CCCR_CMCCode = SwissKnife.sqlEncode(request.getParameter("CMCCode")),
           CCCRPrimary = SwissKnife.sqlEncode(request.getParameter("CCCRPrimary")),
           CCCRIsHidden = SwissKnife.sqlEncode(request.getParameter("CCCRIsHidden")),            
           CCCRRank = SwissKnife.sqlEncode(request.getParameter("CCCRRank"));
    
    if (CCCR_CMRCode.length() == 0 || CCCR_CMCCode.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }

    int tmpRank = 0;
    try {
      tmpRank = Integer.parseInt(CCCRRank);
    }
    catch(Exception e){
      e.printStackTrace();
      tmpRank = 0;
    }
    
    
    
    Database database = _director.getDBConnection(databaseId);

    String query = "UPDATE CMCRelCMR SET"
                 + " CCCRPrimary = '" + CCCRPrimary + "'"
                 + ",CCCRRank = " + tmpRank
                 + ",CCCRIsHidden = '" + CCCRIsHidden + "'"            
                 + " WHERE CCCR_CMRCode = '" + CCCR_CMRCode + "'"
                 + " AND CCCR_CMCCode = '" + CCCR_CMCCode + "'";
    
    dbRet = database.execQuery(query);
    
    _director.freeDBConnection(databaseId,database);

    return dbRet;
  }
  
  
  private DbRet doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId, authUsername, authPassword,
                              "CMCRelCMR", Director.AUTH_DELETE);

    DbRet dbRet = new DbRet();
    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String CCCR_CMRCode = SwissKnife.sqlEncode(request.getParameter("CMRCode")),
           CCCR_CMCCode = SwissKnife.sqlEncode(request.getParameter("CMCCode"));
    
    if (CCCR_CMRCode.length() == 0 || CCCR_CMCCode.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
        
    Database database = _director.getDBConnection(databaseId);    

    String query = "DELETE FROM CMCRelCMR" 
                 + " WHERE CCCR_CMRCode = '" + CCCR_CMRCode + "'" 
                 + " AND CCCR_CMCCode  = '"  + CCCR_CMCCode + "'";
                   
    //System.out.println("query = " + query );
    
    dbRet = database.execQuery(query);

    _director.freeDBConnection(databaseId,database);

    return dbRet;
  }  

}
