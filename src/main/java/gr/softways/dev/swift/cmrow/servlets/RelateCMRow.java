/*
 * RelateCMRow.java
 *
 * Created on 11 Απρίλιος 2006, 10:07 πμ
 */

package gr.softways.dev.swift.cmrow.servlets;

import java.io.*;
import java.net.*;
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
public class RelateCMRow extends HttpServlet {
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
    
    if (action.equals("RELATE")) {
      dbRet = doRelate(request,databaseId);
    }
    else if (action.equals("UNRELATE")) {
      dbRet = doUnrelate(request,databaseId);
    }
    else if (action.equals("UPDATE")) {
      dbRet = doUpdate(request,databaseId);
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

  private DbRet doRelate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"CMRRelCMR",Director.AUTH_INSERT);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
     
    String CMCM_CMRCode1 = SwissKnife.sqlEncode(request.getParameter("CMCM_CMRCode1")),
           CMCM_CMRCode2 = SwissKnife.sqlEncode(request.getParameter("CMCM_CMRCode2"));
    
    if (CMCM_CMRCode1.length() == 0 || CMCM_CMRCode2.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    
    String query = "INSERT INTO CMRRelCMR ("
                 + " CMCMCode,CMCM_CMRCode1,CMCM_CMRCode2,CMCMIsHidden"
                 + ") VALUES (" 
                 + "'" + SwissKnife.buildPK() + "'"
                 + ",'" + CMCM_CMRCode1 + "'"
                 + ",'" + CMCM_CMRCode2 + "'"
                 + ",'0'"            
                 + ")";
                 
    dbRet = database.execQuery(query);
    
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  private DbRet doUnrelate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"CMRRelCMR",Director.AUTH_DELETE);

    DbRet dbRet = new DbRet();
    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String CMCMCode = SwissKnife.sqlEncode(request.getParameter("CMCMCode"));
    
    if (CMCMCode.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
        
    Database database = _director.getDBConnection(databaseId);

    String query = "DELETE FROM CMRRelCMR" 
                 + " WHERE CMCMCode = '" + CMCMCode + "'";
                   
    //System.out.println("query = " + query );
    
    dbRet = database.execQuery(query);

    _director.freeDBConnection(databaseId,database);

    return dbRet;
  }

  
  private DbRet doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"CMRRelCMR",Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();
    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String CMCMCode = SwissKnife.sqlEncode(request.getParameter("CMCMCode")), 
        CMCMIsHidden = SwissKnife.sqlEncode(request.getParameter("CMCMIsHidden"));
    
    int CMCMRank = 0;
    try {
      CMCMRank = Integer.parseInt(request.getParameter("CMCMRank"));
    }
    catch (Exception e) {
      CMCMRank = 0;
    }
    
    if (CMCMCode.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
        
    Database database = _director.getDBConnection(databaseId);

    String query = "UPDATE CMRRelCMR SET CMCMIsHidden='" + CMCMIsHidden + "'" 
        + ",CMCMRank = '" + CMCMRank + "'"
        + " WHERE CMCMCode = '" + CMCMCode + "'";
                   
    //System.out.println("query = " + query );
    
    dbRet = database.execQuery(query);

    _director.freeDBConnection(databaseId,database);

    return dbRet;
  }
}