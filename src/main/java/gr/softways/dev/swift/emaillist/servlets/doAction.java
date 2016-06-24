package gr.softways.dev.swift.emaillist.servlets;

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
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1"),
           databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId"),
           urlSuccess = request.getParameter("urlSuccess") == null ? "" : request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");

    int status = Director.STATUS_ERROR;

    if (databaseId.equals("")) status = Director.STATUS_ERROR;
    else if (action.equals("INSERT")) status = doInsert(request, databaseId);
    else if (action.equals("UPDATE")) status = doUpdate(request, databaseId);
    else if (action.equals("DELETE")) status = doDelete(request, databaseId);
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

  private int doInsert(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

    int auth = director.auth(databaseId,authUsername,authPassword,"emailList",Director.AUTH_INSERT);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String ELCode = SwissKnife.buildPK(),
           ELIsDefault = SwissKnife.sqlEncode(request.getParameter("ELIsDefault")),
           ELActive = SwissKnife.sqlEncode(request.getParameter("ELActive")),
           ELKey = SwissKnife.sqlEncode(request.getParameter("ELKey")),            
           ELName = SwissKnife.sqlEncode(request.getParameter("ELName")),
           ELNameLG = SwissKnife.sqlEncode(request.getParameter("ELNameLG")),
           ELDescr = SwissKnife.sqlEncode(request.getParameter("ELDescr")),
           ELDescrLG = SwissKnife.sqlEncode(request.getParameter("ELDescrLG")),
           ELToEmail = SwissKnife.sqlEncode(request.getParameter("ELToEmail")),
           ELSmtpServer = SwissKnife.sqlEncode(request.getParameter("ELSmtpServer")),
           ELConfRegFrom = SwissKnife.sqlEncode(request.getParameter("ELConfRegFrom")),
           ELConfRegSubject = SwissKnife.sqlEncode(request.getParameter("ELConfRegSubject")),
           ELConfRegSubjectLG = SwissKnife.sqlEncode(request.getParameter("ELConfRegSubjectLG")),
           ELConfRegMessage = SwissKnife.sqlEncode(request.getParameter("ELConfRegMessage")),            
           ELConfRegMessageLG = SwissKnife.sqlEncode(request.getParameter("ELConfRegMessageLG")),            
           ELThankRegSubject = SwissKnife.sqlEncode(request.getParameter("ELThankRegSubject")),
           ELThankRegSubjectLG = SwissKnife.sqlEncode(request.getParameter("ELThankRegSubjectLG")),
           ELThankRegMessage = SwissKnife.sqlEncode(request.getParameter("ELThankRegMessage")),            
           ELThankRegMessageLG = SwissKnife.sqlEncode(request.getParameter("ELThankRegMessageLG")),                        
           ELTopHTML = SwissKnife.sqlEncode(request.getParameter("ELTopHTML")),
           ELTopHTMLLG = SwissKnife.sqlEncode(request.getParameter("ELTopHTMLLG")),            
           ELBottomHTML = SwissKnife.sqlEncode(request.getParameter("ELBottomHTML")),
           ELBottomHTMLLG = SwissKnife.sqlEncode(request.getParameter("ELBottomHTMLLG"));                
            
    String ELNameUp = SwissKnife.searchConvert(ELName);
    String ELNameUpLG = SwissKnife.searchConvert(ELNameLG);
    
   
    String query = "INSERT INTO emailList " +
                   " (ELCode,ELName,ELNameUp,ELNameLG," +
                   "  ELNameUpLG, ELDescr, ELDescrLG, ELToEmail," +
                   "  ELIsDefault, ELActive, ELKey, ELSmtpServer," + 
                   " ELConfRegFrom, ELConfRegSubject, ELConfRegSubjectLG," + 
                   " ELConfRegMessage, ELConfRegMessageLG, ELThankRegSubject, ELThankRegSubjectLG," + 
                   " ELThankRegMessage, ELThankRegMessageLG, ELTopHTML, ELTopHTMLLG, ELBottomHTML, ELBottomHTMLLG)" +
                   " VALUES (" +
                   "'" + ELCode         + "'," +
                   "'" + ELName          + "'," +
                   "'" + ELNameUp        + "'," +
                   "'" + ELNameLG        + "'," +
                   "'" + ELNameUpLG      + "'," +
                   "'" + ELDescr      + "'," +
                   "'" + ELDescrLG      + "'," +            
                   "'" + ELToEmail      + "'," +                        
                   "'" + ELIsDefault   + "'," +
                   "'" + ELActive   + "'," +            
                   "'" + ELKey   + "'," +                        
                   "'" + ELSmtpServer   + "'," +                        
                   "'" + ELConfRegFrom   + "'," +                        
                   "'" + ELConfRegSubject   + "'," +                        
                   "'" + ELConfRegSubjectLG   + "'," +                        
                   "'" + ELConfRegMessage   + "'," +                        
                   "'" + ELConfRegMessageLG   + "'," +                        
                   "'" + ELThankRegSubject   + "'," +                        
                   "'" + ELThankRegSubjectLG   + "'," +                        
                   "'" + ELThankRegMessage   + "'," +                        
                   "'" + ELThankRegMessageLG   + "'," +                        
                   "'" + ELTopHTML   + "'," +                        
                   "'" + ELTopHTMLLG   + "'," +                                
                   "'" + ELBottomHTML   + "'," +                                
                   "'" + ELBottomHTMLLG   + "'"
                   + ")";

    return executeQuery(databaseId, query);
  }

  private int doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

    int auth = director.auth(databaseId,authUsername,authPassword,"emailList",Director.AUTH_UPDATE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String ELCode = SwissKnife.sqlEncode(request.getParameter("ELCode")),
           ELIsDefault = SwissKnife.sqlEncode(request.getParameter("ELIsDefault")),
           ELActive = SwissKnife.sqlEncode(request.getParameter("ELActive")),
           ELKey = SwissKnife.sqlEncode(request.getParameter("ELKey")),            
           ELName = SwissKnife.sqlEncode(request.getParameter("ELName")),
           ELNameLG = SwissKnife.sqlEncode(request.getParameter("ELNameLG")),
           ELDescr = SwissKnife.sqlEncode(request.getParameter("ELDescr")),
           ELDescrLG = SwissKnife.sqlEncode(request.getParameter("ELDescrLG")),
           ELToEmail = SwissKnife.sqlEncode(request.getParameter("ELToEmail")),
           ELSmtpServer = SwissKnife.sqlEncode(request.getParameter("ELSmtpServer")),
           ELConfRegFrom = SwissKnife.sqlEncode(request.getParameter("ELConfRegFrom")),
           ELConfRegSubject = SwissKnife.sqlEncode(request.getParameter("ELConfRegSubject")),
           ELConfRegSubjectLG = SwissKnife.sqlEncode(request.getParameter("ELConfRegSubjectLG")),
           ELConfRegMessage = SwissKnife.sqlEncode(request.getParameter("ELConfRegMessage")),            
           ELConfRegMessageLG = SwissKnife.sqlEncode(request.getParameter("ELConfRegMessageLG")),            
           ELThankRegSubject = SwissKnife.sqlEncode(request.getParameter("ELThankRegSubject")),
           ELThankRegSubjectLG = SwissKnife.sqlEncode(request.getParameter("ELThankRegSubjectLG")),
           ELThankRegMessage = SwissKnife.sqlEncode(request.getParameter("ELThankRegMessage")),            
           ELThankRegMessageLG = SwissKnife.sqlEncode(request.getParameter("ELThankRegMessageLG")),                        
           ELTopHTML = SwissKnife.sqlEncode(request.getParameter("ELTopHTML")),
           ELTopHTMLLG = SwissKnife.sqlEncode(request.getParameter("ELTopHTMLLG")),            
           ELBottomHTML = SwissKnife.sqlEncode(request.getParameter("ELBottomHTML")),
           ELBottomHTMLLG = SwissKnife.sqlEncode(request.getParameter("ELBottomHTMLLG"));                
            
    String ELNameUp = SwissKnife.searchConvert(ELName);
    String ELNameUpLG = SwissKnife.searchConvert(ELNameLG);
    
    if (ELCode.equals("")) return Director.STATUS_ERROR;
    
   
    String query = "UPDATE emailList SET ELName = '" + ELName
                   + "', ELNameUp = '"     + ELNameUp
                   + "', ELNameLG = '"     + ELNameLG
                   + "', ELNameUpLG = '"   + ELNameUpLG
                   + "', ELDescr = '"   + ELDescr
                   + "', ELDescrLG = '"   + ELDescrLG          
                   + "', ELToEmail = '"   + ELToEmail          
                   + "', ELIsDefault = '"   + ELIsDefault                        
                   + "', ELActive = '"   + ELActive            
                   + "', ELKey = '"   + ELKey                    
                   + "', ELSmtpServer = '"   + ELSmtpServer                    
                   + "', ELConfRegFrom = '"   + ELConfRegFrom                    
                   + "', ELConfRegSubject = '"   + ELConfRegSubject                    
                   + "', ELConfRegSubjectLG = '"   + ELConfRegSubjectLG                    
                   + "', ELConfRegMessage = '"   + ELConfRegMessage                                
                   + "', ELConfRegMessageLG = '"   + ELConfRegMessageLG                                            
                   + "', ELThankRegSubject = '"   + ELThankRegSubject                    
                   + "', ELThankRegSubjectLG = '"   + ELThankRegSubjectLG                    
                   + "', ELThankRegMessage = '"   + ELThankRegMessage                                
                   + "', ELThankRegMessageLG = '"   + ELThankRegMessageLG                                                        
                   + "', ELTopHTML = '"   + ELTopHTML            
                   + "', ELTopHTMLLG = '"   + ELTopHTMLLG            
                   + "', ELBottomHTML = '"   + ELBottomHTML            
                   + "', ELBottomHTMLLG = '"   + ELBottomHTMLLG                        
                   + "'"
                   + " WHERE ELCode = '"    + ELCode + "'";

    return executeQuery(databaseId, query);
  }

  private int doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

    int auth = 0;

    auth = director.auth(databaseId,authUsername,authPassword,"emailList",Director.AUTH_DELETE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String ELCode = SwissKnife.sqlEncode(request.getParameter("ELCode"));

    if (ELCode.equals("")) return Director.STATUS_ERROR;

    DbRet dbRet = null;
    
    // get database connection
    Database database = director.getDBConnection(databaseId);

    // begin transaction
    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();

    String query = "";


    if (dbRet.noError == 1) {
      query = "DELETE FROM emailList"
            + " WHERE ELCode = '" + ELCode + "'";

      dbRet = database.execQuery(query);
    }

    // End transaction (commit or rollback)
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    director.freeDBConnection(databaseId,database);

    if (dbRet.getNoError() == 1)
      return Director.STATUS_OK;
    else
      return Director.STATUS_ERROR;
  }

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
