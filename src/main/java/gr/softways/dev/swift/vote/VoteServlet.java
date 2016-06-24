package gr.softways.dev.swift.vote;

import java.io.*;
import java.util.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class VoteServlet extends HttpServlet {

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
    
    if (action.equals("INSERT")) {
      dbRet = doInsert(request,databaseId);
    }
    else if (action.equals("UPDATE")) {
      dbRet = doUpdate(request,databaseId);
    }
    else if (action.equals("DELETE")) {
      dbRet = doDelete(request,databaseId);
    }
    else if (action.equals("INSERT_VOTE_FOR")) {
      dbRet = doInsertVoteFor(request,databaseId);
    }
    else if (action.equals("UPDATE_VOTE_FOR")) {
      dbRet = doUpdateVoteFor(request,databaseId);
    }
    else if (action.equals("DELETE_VOTE_FOR")) {
      dbRet = doDeleteVoteFor(request,databaseId);
    }
           
    if (dbRet.getNoError() == 1) {
      if (action.equals("INSERT")) {
        urlSuccess += "&VTCode=" + dbRet.getRetStr();
      }
      
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

    int auth = _director.auth(databaseId,authUsername,authPassword,"voteTab",Director.AUTH_INSERT);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String VTCode = null;
    
    String VTSubject = SwissKnife.sqlEncode(request.getParameter("VTSubject")),
           VTSubjectLG = SwissKnife.sqlEncode(request.getParameter("VTSubjectLG")),
           VTSubjectLG1 = SwissKnife.sqlEncode(request.getParameter("VTSubjectLG1")),
           VTSubjectLG2 = SwissKnife.sqlEncode(request.getParameter("VTSubjectLG2")),
           VTSubjectLG3 = SwissKnife.sqlEncode(request.getParameter("VTSubjectLG3")),
           VTSubjectLG4 = SwissKnife.sqlEncode(request.getParameter("VTSubjectLG4")),
           VTSubjectLG5 = SwissKnife.sqlEncode(request.getParameter("VTSubjectLG5")),
           VTDescr = SwissKnife.sqlEncode(request.getParameter("VTDescr")),
           VTDescrLG = SwissKnife.sqlEncode(request.getParameter("VTDescrLG")),
           VTDescrLG1 = SwissKnife.sqlEncode(request.getParameter("VTDescrLG1")),
           VTDescrLG2 = SwissKnife.sqlEncode(request.getParameter("VTDescrLG2")),
           VTDescrLG3 = SwissKnife.sqlEncode(request.getParameter("VTDescrLG3")),
           VTDescrLG4 = SwissKnife.sqlEncode(request.getParameter("VTDescrLG4")),
           VTDescrLG5 = SwissKnife.sqlEncode(request.getParameter("VTDescrLG5"));
           
    Timestamp VTTo = null, VTFrom = null;
    
    VTFrom = SwissKnife.buildTimestamp(request.getParameter("VTFromDay"),
                                     request.getParameter("VTFromMonth"),
                                     request.getParameter("VTFromYear"),
                                     request.getParameter("VTFromHour"),
                                     request.getParameter("VTFromMinutes"),
                                     "0","0");
    
    VTTo = SwissKnife.buildTimestamp(request.getParameter("VTToDay"),
                                     request.getParameter("VTToMonth"),
                                     request.getParameter("VTToYear"),
                                     request.getParameter("VTToHour"),
                                     request.getParameter("VTToMinutes"),
                                     "0","0");

    if (VTSubject.length() == 0 || VTFrom == null || VTTo == null) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    
    String query = "INSERT INTO voteTab (" 
        + "VTCode,VTSubject,VTSubjectLG,VTSubjectLG1,VTSubjectLG2,VTSubjectLG3,VTSubjectLG4,VTSubjectLG5"
        + ",VTDescr,VTDescrLG,VTDescrLG1,VTDescrLG2,VTDescrLG3,VTDescrLG4,VTDescrLG5,VTFrom,VTTo"
        + ") VALUES ("
        + "?,?,?,?,?,?,?"
        + ",?,?,?,?,?,?,?,?,?,?"
        + ")";
    
    PreparedStatement ps = null;
    
    try {
      ps = database.createPreparedStatement(query);
      
      VTCode = SwissKnife.buildPK();
      
      ps.setString(1, VTCode);
      ps.setString(2, VTSubject);
      ps.setString(3, VTSubjectLG);
      ps.setString(4, VTSubjectLG1);
      ps.setString(5, VTSubjectLG2);
      ps.setString(6, VTSubjectLG3);
      ps.setString(7, VTSubjectLG4);
      ps.setString(8, VTSubjectLG5);
      ps.setString(9, VTDescr);
      ps.setString(10, VTDescrLG);
      ps.setString(11, VTDescrLG1);
      ps.setString(12, VTDescrLG2);
      ps.setString(13, VTDescrLG3);
      ps.setString(14, VTDescrLG4);
      ps.setString(15, VTDescrLG5);
      ps.setTimestamp(16, VTFrom);
      ps.setTimestamp(17, VTTo);
      
      ps.executeUpdate();
      
      dbRet.setRetStr(VTCode);
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      
      e.printStackTrace();
    }
    finally {
      try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
    }

    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  private DbRet doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);
    
    int auth = _director.auth(databaseId,authUsername,authPassword,"voteTab",Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String VTCode = SwissKnife.sqlEncode(request.getParameter("VTCode")),
           VTSubject = SwissKnife.sqlEncode(request.getParameter("VTSubject")),
           VTSubjectLG = SwissKnife.sqlEncode(request.getParameter("VTSubjectLG")),
           VTSubjectLG1 = SwissKnife.sqlEncode(request.getParameter("VTSubjectLG1")),
           VTSubjectLG2 = SwissKnife.sqlEncode(request.getParameter("VTSubjectLG2")),
           VTSubjectLG3 = SwissKnife.sqlEncode(request.getParameter("VTSubjectLG3")),
           VTSubjectLG4 = SwissKnife.sqlEncode(request.getParameter("VTSubjectLG4")),
           VTSubjectLG5 = SwissKnife.sqlEncode(request.getParameter("VTSubjectLG5")),
           VTDescr = SwissKnife.sqlEncode(request.getParameter("VTDescr")),
           VTDescrLG = SwissKnife.sqlEncode(request.getParameter("VTDescrLG")),
           VTDescrLG1 = SwissKnife.sqlEncode(request.getParameter("VTDescrLG1")),
           VTDescrLG2 = SwissKnife.sqlEncode(request.getParameter("VTDescrLG2")),
           VTDescrLG3 = SwissKnife.sqlEncode(request.getParameter("VTDescrLG3")),
           VTDescrLG4 = SwissKnife.sqlEncode(request.getParameter("VTDescrLG4")),
           VTDescrLG5 = SwissKnife.sqlEncode(request.getParameter("VTDescrLG5"));
            
    Timestamp VTTo = null, VTFrom = null;
    
    VTFrom = SwissKnife.buildTimestamp(request.getParameter("VTFromDay"),
                                       request.getParameter("VTFromMonth"),
                                       request.getParameter("VTFromYear"),
                                       request.getParameter("VTFromHour"),
                                       request.getParameter("VTFromMinutes"),
                                       "0","0");
    
    VTTo = SwissKnife.buildTimestamp(request.getParameter("VTToDay"),
                                     request.getParameter("VTToMonth"),
                                     request.getParameter("VTToYear"),
                                     request.getParameter("VTToHour"),
                                     request.getParameter("VTToMinutes"),
                                     "0","0");

    if (VTCode.length() == 0 || VTSubject.length() == 0 || VTFrom == null || VTTo == null) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    
    String query = "UPDATE voteTab SET"
                 + " VTSubject = ?"
                 + ",VTSubjectLG = ?"
                 + ",VTSubjectLG1 = ?"
                 + ",VTSubjectLG2 = ?"
                 + ",VTSubjectLG3 = ?"
                 + ",VTSubjectLG4 = ?"
                 + ",VTSubjectLG5 = ?"
                 + ",VTDescr = ?"
                 + ",VTDescrLG = ?"
                 + ",VTDescrLG1 = ?"
                 + ",VTDescrLG2 = ?"
                 + ",VTDescrLG3 = ?"
                 + ",VTDescrLG4 = ?"
                 + ",VTDescrLG5 = ?"
                 + ",VTFrom = ?"
                 + ",VTTo = ?"
                 + " WHERE VTCode = ?";
    
    PreparedStatement ps = null;
    
    try {
      ps = database.createPreparedStatement(query);
      
      ps.setString(1, VTSubject);
      ps.setString(2, VTSubjectLG);
      ps.setString(3, VTSubjectLG1);
      ps.setString(4, VTSubjectLG2);
      ps.setString(5, VTSubjectLG3);
      ps.setString(6, VTSubjectLG4);
      ps.setString(7, VTSubjectLG5);
      ps.setString(8, VTDescr);
      ps.setString(9, VTDescrLG);
      ps.setString(10, VTDescrLG1);
      ps.setString(11, VTDescrLG2);
      ps.setString(12, VTDescrLG3);
      ps.setString(13, VTDescrLG4);
      ps.setString(14, VTDescrLG5);
      ps.setTimestamp(15, VTFrom);
      ps.setTimestamp(16, VTTo);
      ps.setString(17, VTCode);
      
      ps.executeUpdate();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      
      e.printStackTrace();
    }
    finally {
      try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
    }
    
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  private DbRet doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"voteTab",Director.AUTH_DELETE);

    DbRet dbRet = new DbRet();
    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String VTCode = SwissKnife.sqlEncode(request.getParameter("VTCode"));
    
    if (VTCode.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
        
    Database database = _director.getDBConnection(databaseId);

    String query = "DELETE FROM voteTab WHERE VTCode = '" + VTCode + "'";
                   
    //System.out.println("query = " + query );
    
    dbRet = database.execQuery(query);

    _director.freeDBConnection(databaseId,database);

    return dbRet;
  }
  
  private DbRet doInsertVoteFor(HttpServletRequest request, String databaseId) {  
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"voteForTab",Director.AUTH_INSERT);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String VFTVTCode = SwissKnife.sqlEncode(request.getParameter("VFTVTCode")),
           VFTVoteFor = SwissKnife.sqlEncode(request.getParameter("VFTVoteFor")),
           VFTVoteForLG = SwissKnife.sqlEncode(request.getParameter("VFTVoteForLG")),
           VFTVoteForLG1 = SwissKnife.sqlEncode(request.getParameter("VFTVoteForLG1")),
           VFTVoteForLG2 = SwissKnife.sqlEncode(request.getParameter("VFTVoteForLG2")),
           VFTVoteForLG3 = SwissKnife.sqlEncode(request.getParameter("VFTVoteForLG3")),
           VFTVoteForLG4 = SwissKnife.sqlEncode(request.getParameter("VFTVoteForLG4")),
           VFTVoteForLG5 = SwissKnife.sqlEncode(request.getParameter("VFTVoteForLG5")),
           VFTVotes = "0",
           VFTAA = SwissKnife.sqlEncode(request.getParameter("VFTAA"));

    if (VFTVTCode.length() == 0 || VFTVoteFor.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    
    String query = "INSERT INTO voteForTab (" 
                 + "VFTCode,VFTVTCode,VFTVoteFor,VFTVoteForLG,VFTVoteForLG1,VFTVoteForLG2,VFTVoteForLG3,VFTVoteForLG4,VFTVoteForLG5,VFTVotes,VFTAA"
                 + ") VALUES (" 
                 + "'"  + SwissKnife.buildPK() + "'"
                 + ",'" + VFTVTCode      + "'"
                 + ",'" + VFTVoteFor     + "'"
                 + ",'" + VFTVoteForLG   + "'"
                 + ",'" + VFTVoteForLG1  + "'"
                 + ",'" + VFTVoteForLG2  + "'"
                 + ",'" + VFTVoteForLG3  + "'"
                 + ",'" + VFTVoteForLG4  + "'"
                 + ",'" + VFTVoteForLG5  + "'"
                 + ",'" + VFTVotes       + "'"
                 + ",'" + VFTAA          + "'"
                 + ")";
    
    dbRet = database.execQuery(query);
    
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  private DbRet doUpdateVoteFor(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"voteForTab",Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String VFTCode = SwissKnife.sqlEncode(request.getParameter("VFTCode")),
           VFTVTCode = SwissKnife.sqlEncode(request.getParameter("VFTVTCode")),
           VFTVoteFor = SwissKnife.sqlEncode(request.getParameter("VFTVoteFor")),
           VFTVoteForLG = SwissKnife.sqlEncode(request.getParameter("VFTVoteForLG")),
           VFTVoteForLG1 = SwissKnife.sqlEncode(request.getParameter("VFTVoteForLG1")),
           VFTVoteForLG2 = SwissKnife.sqlEncode(request.getParameter("VFTVoteForLG2")),
           VFTVoteForLG3 = SwissKnife.sqlEncode(request.getParameter("VFTVoteForLG3")),
           VFTVoteForLG4 = SwissKnife.sqlEncode(request.getParameter("VFTVoteForLG4")),
           VFTVoteForLG5 = SwissKnife.sqlEncode(request.getParameter("VFTVoteForLG5")),
           //VFTVotes = SwissKnife.sqlEncode(request.getParameter("VFTVotes")),
           VFTAA = SwissKnife.sqlEncode(request.getParameter("VFTAA"));

    if (VFTCode.length() == 0 || VFTVTCode.length() == 0 || VFTVoteFor.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    
    String query = "UPDATE voteForTab SET"  
                 + " VFTVTCode = '"     + VFTVTCode    + "'"
                 + ",VFTVoteFor = '"    + VFTVoteFor   + "'" 
                 + ",VFTVoteForLG = '"  + VFTVoteForLG + "'" 
                 + ",VFTVoteForLG1 = '"  + VFTVoteForLG1 + "'" 
                 + ",VFTVoteForLG2 = '"  + VFTVoteForLG2 + "'" 
                 + ",VFTVoteForLG3 = '"  + VFTVoteForLG3 + "'" 
                 + ",VFTVoteForLG4 = '"  + VFTVoteForLG4 + "'" 
                 + ",VFTVoteForLG5 = '"  + VFTVoteForLG5 + "'" 
                 + ",VFTAA = "          + VFTAA        + "" 
                 //+ ",VFTVotes = "       + VFTVotes     + ""
                 + " WHERE VFTCode = '" + VFTCode      + "'";
    
    dbRet = database.execQuery(query);
    
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  private DbRet doDeleteVoteFor(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"voteForTab",Director.AUTH_DELETE);

    DbRet dbRet = new DbRet();
    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String VFTCode = SwissKnife.sqlEncode(request.getParameter("VFTCode"));
    
    if (VFTCode.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
        
    Database database = _director.getDBConnection(databaseId);

    String query = "DELETE FROM voteForTab WHERE VFTCode = '" + VFTCode + "'";
    //System.out.println("query = " + query );
    
    dbRet = database.execQuery(query);

    _director.freeDBConnection(databaseId,database);

    return dbRet;
  }
}