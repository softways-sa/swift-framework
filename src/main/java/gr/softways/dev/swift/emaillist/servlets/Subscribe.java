/*
 * Subscribe.java
 *
 * Created on 26 Ιούνιος 2006, 3:35 μμ
 */

package gr.softways.dev.swift.emaillist.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;
import java.sql.Timestamp;

import gr.softways.dev.swift.registeredmember.RegisteredMember;

public class Subscribe extends HttpServlet {

  private Director _director;
  
  private String _charset = null;
  private QueryDataSet _ELDataSet = null;
  private QueryDataSet _PMDataSet = null;
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    _director = Director.getInstance();
  }

  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1"),
           databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId"),
           urlSuccess = request.getParameter("urlSuccess") == null ? "" : request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure");
           
    DbRet dbRet = new DbRet();
    
    if (databaseId.equals("")) {
      dbRet.setNoError(0);
    }
    else if (action.equals("SUBSCRIBE")) {
      dbRet = NLSubscribe(request, databaseId);
    }
    else if (action.equals("UNSUBSCRIBE")) {
      dbRet = UnSubscribe(request, databaseId);
    }    
    else {
      dbRet.setNoError(0);
    }
    
    if (action.equals("SUBSCRIBE")) {
       System.out.println("NO_ERROR=" + dbRet.getNoError() + "&RETURN_STRING=" + dbRet.getRetStr() + "&AUTH_ERROR=" + dbRet.getAuthError() + "&AUTH_ERROR_CODE=" + dbRet.getAuthErrorCode() + "&DB_ERROR_CODE=" + dbRet.getDbErrorCode() + "&VALID_ERROR=" + dbRet.get_validError() + "&VALID_ERROR_CODE=" + dbRet.get_validErrorCode() + "&UNKNOWN_ERROR=" + dbRet.getUnknownError());      
       response.sendRedirect(urlSuccess + "&subscribed=NL");
    }
    else if (action.equals("UNSUBSCRIBE")) {
       System.out.println("NO_ERROR=" + dbRet.getNoError() + "&RETURN_STRING=" + dbRet.getRetStr() + "&AUTH_ERROR=" + dbRet.getAuthError() + "&AUTH_ERROR_CODE=" + dbRet.getAuthErrorCode() + "&DB_ERROR_CODE=" + dbRet.getDbErrorCode() + "&VALID_ERROR=" + dbRet.get_validError() + "&VALID_ERROR_CODE=" + dbRet.get_validErrorCode() + "&UNKNOWN_ERROR=" + dbRet.getUnknownError());      
       response.sendRedirect(urlSuccess + "&subscribed=DEL");
    }
  }
  
  //Process the HTTP Post request
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1"),
           databaseId = request.getParameter("regId") == null ? "" : request.getParameter("regId");
           
    DbRet dbRet = new DbRet();
    
    if (databaseId.equals("")) {
      dbRet.setNoError(0);
    }
    else if (action.equals("REGISTER")) {
      dbRet = NLRegister(request, databaseId);
      if (dbRet.getNoError() == 0) {
        System.out.println("NewsLetter registration failed." + " ErrorCode = " + dbRet.getDbErrorCode() + ". Error Message = " + dbRet.getRetStr());
      }
    }    
    else if (action.equals("UNREGISTER")) {
      dbRet = UnRegister(request, databaseId);
      if (dbRet.getNoError() == 0) {
        System.out.println("NewsLetter unregistration failed." + " ErrorCode = " + dbRet.getDbErrorCode() + ". Error Message = " + dbRet.getRetStr());
      }
    }
    else {
      dbRet.setNoError(0);
    }
  }
  
  /**
   *  Καταχώρηση νέου pendingMember στο RDBMS.
   *
   * @param  request    το HttpServletRequest από την σελίδα
   * @param  databaseId το αναγνωριστικό της βάσης που
   *                    θα χρησιμοποιηθεί
   * @return            κωδικό κατάστασης
   */
  private DbRet NLSubscribe(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"emailList",Director.AUTH_READ);

    DbRet dbRet = new DbRet();    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      return dbRet;
    }

    String PMCode = null, RMCode = null,
           PMEmail = SwissKnife.sqlEncode(request.getParameter("PMEmail")),
           PMLang = SwissKnife.sqlEncode(request.getParameter("PMLang")),
           ELKey = SwissKnife.sqlEncode(request.getParameter("ELKey"));

    boolean confirmationSent = false;
    dbRet = new DbRet();
    
    String ELCode = "";
    
    if (PMEmail == null || PMEmail.equals("")) {
      dbRet.setNoError(0);
      return dbRet;
    }
    Timestamp PMRegDate=null;
    
    PMRegDate = SwissKnife.currentDate();

    Database database = _director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();

    if (dbRet.getNoError() == 1) {
      dbRet = getELRow(database, ELKey);
      if (dbRet.getRetInt() < 1) {
        dbRet.setNoError(0);
      }      
      if (dbRet.getNoError() == 1) {
        ELCode = dbRet.getRetStr();
      }
    }
    boolean pendingRowExists = false, registeredRowExists = false;
    if (dbRet.getNoError() == 1) {
      dbRet = checkIfPendingExists(database, PMEmail, ELCode);
    }    
    if (dbRet.getNoError() == 1) {
      if (dbRet.getRetInt() > 0) {
        PMCode = dbRet.getRetStr();
        pendingRowExists = true;  // pending row exists but not this list
      }
      dbRet = checkIfRegisteredExists(database, PMEmail, ELCode);
    }        
    if (dbRet.getNoError() == 1) {
      if (dbRet.getRetInt() > 0) {
        RMCode = dbRet.getRetStr();
        registeredRowExists = true;  // registered row exists but not this list
      }
    }

    String writePendingMember = null;
    if (dbRet.getNoError() == 1 && !pendingRowExists) {
      PMCode = SwissKnife.buildPK();      
      writePendingMember = "INSERT INTO pendingMember " +
                   " (PMCode,PMEmail,PMAltEmail,PMTitle,PMLastName," +
                   "  PMLastNameUp,PMFirstName,PMBirthDate,PMRegDate,PMCompanyName," +
                   "  PMCompanyNameUp,PMSex,PMAddress,PMZipCode," +
                   "  PMCity,PM_CNTRCode,PMPhone,PMMobile,PMIsActive," + 
                   " PMLoginTypeKey,PMLang,PMUsrName,PMPasswd)" +
                   " VALUES (" +
                   "'" + PMCode         + "'," +
                   "'" + PMEmail         + "'," +
                   "''," +
                   "''," +
                   "''," +            
                   "''," +            
                   "''," +                        
                   "null," +
                   "'" + PMRegDate.toString()        + "'," +
                   "''," +
                   "''," +
                   "''," +
                   "''," +
                   "''," +
                   "''," +
                   "null," +
                   "''," +
                   "''," +            
                   "''," +            
                   "'" + RegisteredMember.IN_LISTS_NOT_REG + "'," +            
                   "'" + PMLang + "'," +                        
                   "''," +                                      
                   "''" +                                    
                ")";
    }
    else if (dbRet.getNoError() == 1 && pendingRowExists) {
      writePendingMember = "UPDATE pendingMember SET " +
                   " PMLang = '" + PMLang + "'" +
                   " WHERE PMCode = '" + PMCode + "'";    
    }
    
    String insELRelPM = "INSERT INTO ELRelPM " +
                   " (ELPMCode,ELPM_ELCode,ELPM_PMCode" +
                   ")" +
                   " VALUES (" +
                   "'" + PMCode         + "'," +
                   "'" + ELCode          + "'," +
                   "'" + PMCode        + "'" +
                   ")";    
    
    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(writePendingMember);
    }

    if (dbRet.getNoError() == 1 && ELCode.length() > 0) {
      dbRet = database.execQuery(insELRelPM);
    }
 
    if (dbRet.getNoError() == 1 && ELCode.length() > 0) {    
      try {
        confirmationSent = sendConfirmEmail(request, PMCode, PMEmail, PMLang, databaseId, ELKey);
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        dbRet.setDbErrorCode(13);
        dbRet.setRetStr("EMAIL COULD NOT BE SENT");
        e.printStackTrace();
      }
      if (!confirmationSent) {
        dbRet.setNoError(0);
        dbRet.setDbErrorCode(13);
        dbRet.setRetStr("EMAIL COULD NOT BE SENT");
      }      
    }
    
    closeELDataSet();
    
    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    _director.freeDBConnection(databaseId,database);    
    return dbRet;
  }

  
  /**
   *  Καταχώρηση νέου pendingMember στο RDBMS.
   *
   * @param  request    το HttpServletRequest από την σελίδα
   * @param  databaseId το αναγνωριστικό της βάσης που
   *                    θα χρησιμοποιηθεί
   * @return            κωδικό κατάστασης
   */
  private DbRet UnSubscribe(HttpServletRequest request, String databaseId) {

    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

    
    int auth = _director.auth(databaseId,authUsername,authPassword,"emailList",Director.AUTH_READ);

    DbRet dbRet = new DbRet();    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      return dbRet;
    }
    String PMCode = null, RMCode = null,
           PMEmail = SwissKnife.sqlEncode(request.getParameter("PMEmail")),
           PMLang = SwissKnife.sqlEncode(request.getParameter("PMLang")),                        
           ELCode = SwissKnife.sqlEncode(request.getParameter("ELCodeUnRel")),
           ELKey = SwissKnife.sqlEncode(request.getParameter("ELKey"));

    boolean confirmationSent = false;
    dbRet = new DbRet();
    
    if (PMEmail == null || PMEmail.equals("")) {
      dbRet.setNoError(0);
      return dbRet;      
    }
    Timestamp PMRegDate=null;
    
    PMRegDate = SwissKnife.currentDate();

    Database database = _director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    String ELName = "";
    if (dbRet.getNoError() == 1) {
      dbRet = getELRow(database, ELKey);
      if (dbRet.getRetInt() < 1) {
        dbRet.setNoError(0);
      }
      if (dbRet.getNoError() == 1) {
        ELCode = dbRet.getRetStr();
        ELName = SwissKnife.sqlDecode(_ELDataSet.getString("ELName" + PMLang));
      }          
    }    
    closeELDataSet();
    boolean pendingRowExists = false, registeredRowExists = false;
    if (dbRet.getNoError() == 1) {
      dbRet = checkIfRegisteredExists_UnReg(database, PMEmail, ELCode);
    }        
    if (dbRet.getNoError() == 1) {
      if (dbRet.getRetInt() > 0) {
        RMCode = dbRet.getRetStr();
        registeredRowExists = true;  // registered row exists proceed to delete this list
      }
    }

    String writePendingMember = null;
    if (dbRet.getNoError() == 1) {
      PMCode = SwissKnife.buildPK();      
      writePendingMember = "INSERT INTO pendingMember " +
                   " (PMCode,PMEmail,PMAltEmail,PMTitle,PMLastName," +
                   "  PMLastNameUp,PMFirstName,PMBirthDate,PMRegDate,PMCompanyName," +
                   "  PMCompanyNameUp,PMSex,PMAddress,PMZipCode," +
                   "  PMCity,PM_CNTRCode,PMPhone,PMMobile,PMIsActive," + 
                   " PMLoginTypeKey,PMLang,PMUsrName,PMPasswd)" +
                   " VALUES (" +
                   "'" + PMCode         + "'," +
                   "'" + PMEmail         + "'," +
                   "''," +
                   "''," +
                   "''," +            
                   "''," +            
                   "''," +                        
                   "null," +
                   "'" + PMRegDate.toString()        + "'," +
                   "''," +
                   "''," +
                   "''," +
                   "''," +
                   "''," +
                   "''," +
                   "null," +
                   "''," +
                   "''," +            
                   "''," +            
                   "'" + RegisteredMember.IN_LISTS_NOT_REG + "'," +            
                   "'" + PMLang + "'," +                        
                   "''," +                                      
                   "''" +                                    
                ")";
    }
    
    String insELRelPM = "INSERT INTO ELRelPM " +
                   " (ELPMCode,ELPM_ELCode,ELPM_PMCode" +
                   ")" +
                   " VALUES (" +
                   "'" + PMCode         + "'," +
                   "'" + ELCode          + "'," +
                   "'" + PMCode        + "'" +
                   ")";    
    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(writePendingMember);
    }

    if (dbRet.getNoError() == 1 && ELCode.length() > 0) {
      dbRet = database.execQuery(insELRelPM);
    }
 
    if (dbRet.getNoError() == 1 && ELCode.length() > 0) {    
      try {
        confirmationSent = sendUnSubscribeEmail(request, PMCode, PMEmail, PMLang, databaseId, database, ELKey, ELName);
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        dbRet.setDbErrorCode(13);
        dbRet.setRetStr("EMAIL COULD NOT BE SENT");
        e.printStackTrace();
      }
      if (!confirmationSent) {
        dbRet.setNoError(0);
        dbRet.setDbErrorCode(13);
        dbRet.setRetStr("EMAIL COULD NOT BE SENT");
      }
    }
    
    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    
    _director.freeDBConnection(databaseId,database);
    
    return dbRet;
  }
  
  private void closeELDataSet() {
    try {
      _ELDataSet.close();
    }
    catch (Exception eclose) {
      eclose.printStackTrace();
    }
    _ELDataSet = null;
  }
  
  private DbRet getELRow(Database database, String ELKey) {  
    DbRet dbRet = new DbRet();
    _ELDataSet = new QueryDataSet();
    String retELRowQuery = "SELECT *"
                 + " FROM emaillist WHERE"
                 + " emaillist.ELKey = '" + ELKey + "'";

    try {
      if (_ELDataSet.isOpen()) _ELDataSet.close();
      _ELDataSet.setQuery(new QueryDescriptor(database,retELRowQuery,null,true,Load.ALL));
      _ELDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      _ELDataSet.refresh();
      dbRet.setRetInt( _ELDataSet.getRowCount() );
      if (dbRet.getRetInt() == 1) {
        dbRet.setRetStr(_ELDataSet.getString("ELCode"));
      }
      else 
        dbRet.setRetStr("");          
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }

    return dbRet;
  }


  private void closePMDataSet() {
    try {
      _PMDataSet.close();
    }
    catch (Exception eclose) {
      eclose.printStackTrace();
    }
    _PMDataSet = null;
  }
  
  private DbRet getPMRow(Database database, String PMCode, String ELKey) {  
    boolean foundPMRow = false;
    DbRet dbRet = new DbRet();
    _PMDataSet = new QueryDataSet();
    String retPMRowQuery = "SELECT pendingMember.*, ELRelPM.*, emailList.*"
                 + " FROM pendingMember LEFT JOIN ELRelPM ON PMCode=ELPM_PMCode LEFT JOIN emailList ON ELPM_ELCode=ELCode WHERE"
                 + " PMCode = '" + PMCode + "'";

    try {
      if (_PMDataSet.isOpen()) _PMDataSet.close();
      _PMDataSet.setQuery(new QueryDescriptor(database,retPMRowQuery,null,true,Load.ALL));
      _PMDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      _PMDataSet.refresh();
      dbRet.setRetInt( _PMDataSet.getRowCount() );
      dbRet.setRetStr("");         
      String PMLoginTypeKey = null;      
      for (int i=0; i<dbRet.getRetInt(); i++) {
        if (i==0) {
          PMLoginTypeKey = _PMDataSet.getString("PMLoginTypeKey");          
          dbRet.setRetStr(_PMDataSet.getString("PMCode"));          
        }
        if (_PMDataSet.getString("ELKey").equals(ELKey)) {
          foundPMRow = true;
          break;
        }
        _PMDataSet.next();
      }
      if (!foundPMRow) {
        dbRet.setRetStr("NO PENDING ROW TO REGISTER");
        dbRet.setDbErrorCode(20);        
        dbRet.setNoError(0);        
      }
      else if (dbRet.getRetInt() > 0 && PMLoginTypeKey.equals(RegisteredMember.REGISTERED)) {
        dbRet.setRetStr("PENDING MEMBER ROW EXISTS. NOT ALLOWED TO REGISTER LIST");
        dbRet.setDbErrorCode(10);        
        dbRet.setNoError(0);                
      }      
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }

    return dbRet;
  }
  
  private DbRet checkIfPendingExists(Database database, String PMEmail, String ELCode) {  
    DbRet dbRet = new DbRet();
    QueryDataSet queryDataSet = new QueryDataSet();
    String checkPMEmailQuery = "SELECT PMCode,PMEmail,ELPM_ELCode,ELName,PMLoginTypeKey"
                 + " FROM pendingMember LEFT JOIN ELRelPM ON PMCode=ELPM_PMCode LEFT JOIN emailList ON ELPM_ELCode=ELCode WHERE"
                 + " PMEmail = '" + PMEmail + "'";

    try {
      if (queryDataSet.isOpen()) queryDataSet.close();
      queryDataSet.setQuery(new QueryDescriptor(database,checkPMEmailQuery,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();
      dbRet.setRetInt( queryDataSet.getRowCount() );
      boolean foundPending = false;
      String PMLoginTypeKey = null;
      for (int i=0; i<dbRet.getRetInt(); i++) {
        if (i==0) {
          PMLoginTypeKey = queryDataSet.getString("PMLoginTypeKey");
          dbRet.setRetStr(queryDataSet.getString("PMCode"));
        }
        String ELPM_ELCode = queryDataSet.getString("ELPM_ELCode");
        if (ELPM_ELCode != null && ELPM_ELCode.equals(ELCode)) {
          foundPending = true;
          break;
        }
        queryDataSet.next();
      }
      if (foundPending) {
        dbRet.setRetStr("EMAIL LIST EXISTS - PENDING");
        dbRet.setDbErrorCode(11);        
        dbRet.setNoError(0);        
      }
      else if (dbRet.getRetInt() > 0 && PMLoginTypeKey.equals(RegisteredMember.REGISTERED)) {
        dbRet.setRetStr("PENDING MEMBER ROW EXISTS. NOT ALLOWED TO ADD LIST");
        dbRet.setDbErrorCode(10);        
        dbRet.setNoError(0);                
      }
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }

    try {
      queryDataSet.close();
    }
    catch (Exception eclose) {
      dbRet.setNoError(0);
      eclose.printStackTrace();
    } 
    return dbRet;
  }
  
  private DbRet checkIfRegisteredExists(Database database, String RMEmail, String ELCode) {  
    DbRet dbRet = new DbRet();
    QueryDataSet queryDataSet = new QueryDataSet();
    String checkRMEmailQuery = "SELECT RMCode,RMEmail,RMLoginTypeKey,ELRM_ELCode,ELName"
                 + " FROM registeredMember LEFT JOIN ELRelRM ON RMCode=ELRM_RMCode LEFT JOIN emailList ON ELRM_ELCode=ELCode WHERE"
                 + " RMEmail = '" + RMEmail + "'";

    try {
      if (queryDataSet.isOpen()) queryDataSet.close();
      queryDataSet.setQuery(new QueryDescriptor(database,checkRMEmailQuery,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();
      dbRet.setRetInt( queryDataSet.getRowCount() );
      boolean foundRegistered = false;
      String RMLoginTypeKey = null;      
      for (int i=0; i<dbRet.getRetInt(); i++) {
        if (i==0) {
          RMLoginTypeKey = queryDataSet.getString("RMLoginTypeKey");          
          dbRet.setRetStr(queryDataSet.getString("RMCode"));        
        }
        String ELRM_ELCode = queryDataSet.getString("ELRM_ELCode");
        if (ELRM_ELCode != null && ELRM_ELCode.equals(ELCode)) {
          foundRegistered = true;
          break;
        }
        queryDataSet.next();
      }
      if (foundRegistered) {
        dbRet.setRetStr("EMAIL LIST EXISTS - REGISTERED");
        dbRet.setDbErrorCode(12);
        dbRet.setNoError(0);
      }
      else if (dbRet.getRetInt() > 0 && RMLoginTypeKey.equals(RegisteredMember.REGISTERED)) {
        dbRet.setRetStr("REGISTERED MEMBER ROW EXISTS. NOT ALLOWED TO ADD LIST");
        dbRet.setDbErrorCode(13);
        dbRet.setNoError(0);
      }      
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
      
    try {
      queryDataSet.close();
    }
    catch (Exception eclose) {
      dbRet.setNoError(0);
      eclose.printStackTrace();
    } 
    return dbRet;
  }

  private DbRet checkIfRegisteredExists_UnReg(Database database, String RMEmail, String ELCode) {  
    DbRet dbRet = new DbRet();
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    String checkRMEmailQuery = "SELECT RMCode,RMEmail,RMLoginTypeKey,ELRM_ELCode,ELName"
                 + " FROM registeredMember LEFT JOIN ELRelRM ON RMCode=ELRM_RMCode LEFT JOIN emailList ON ELRM_ELCode=ELCode WHERE"
                 + " RMEmail = '" + RMEmail + "'";

    try {
      if (queryDataSet.isOpen()) queryDataSet.close();
      queryDataSet.setQuery(new QueryDescriptor(database,checkRMEmailQuery,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();
      dbRet.setRetInt( queryDataSet.getRowCount() );
      boolean foundRegistered = false;
      String RMLoginTypeKey = null;
      for (int i=0; i<dbRet.getRetInt(); i++) {
        if (i==0) {
          RMLoginTypeKey = queryDataSet.getString("RMLoginTypeKey");
          dbRet.setRetStr(queryDataSet.getString("RMCode"));
        }
        String ELRM_ELCode = queryDataSet.getString("ELRM_ELCode");
        if (ELRM_ELCode != null && ELRM_ELCode.equals(ELCode)) {
          foundRegistered = true;
          break;
        }
        queryDataSet.next();
      }
      if (!foundRegistered) {
        dbRet.setRetStr("EMAIL LIST DOES NOT EXIST");
        dbRet.setDbErrorCode(22);
        dbRet.setNoError(0);
      }
      else if (dbRet.getRetInt() > 0 && RMLoginTypeKey.equals(RegisteredMember.REGISTERED)) {
        dbRet.setRetStr("REGISTERED MEMBER ROW EXISTS. NOT ALLOWED TO DELETE LIST");
        dbRet.setDbErrorCode(23);
        dbRet.setNoError(0);
      }
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
      
    try {
      queryDataSet.close();
    }
    catch (Exception eclose) {
      dbRet.setNoError(0);
      eclose.printStackTrace();
    } 
    return dbRet;
  }
  
  private DbRet checkIfCanRegister(Database database, String RMEmail, String ELCode) {  
    DbRet dbRet = new DbRet();
    QueryDataSet queryDataSet = new QueryDataSet();
    String checkRMEmailQuery = "SELECT RMCode,RMEmail,RMLoginTypeKey,ELRM_ELCode,ELName"
                 + " FROM registeredMember LEFT JOIN ELRelRM ON RMCode=ELRM_RMCode LEFT JOIN emailList ON ELRM_ELCode=ELCode WHERE"
                 + " RMEmail = '" + RMEmail + "'";

    try {
      if (queryDataSet.isOpen()) queryDataSet.close();
      queryDataSet.setQuery(new QueryDescriptor(database,checkRMEmailQuery,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();
      dbRet.setRetInt( queryDataSet.getRowCount() );
      boolean foundRegistered = false;
      String RMLoginTypeKey = null;      
      for (int i=0; i<dbRet.getRetInt(); i++) {
        if (i==0) {
          RMLoginTypeKey = queryDataSet.getString("RMLoginTypeKey");
          dbRet.setRetStr(queryDataSet.getString("RMCode"));
        }
        String ELRM_ELCode = queryDataSet.getString("ELRM_ELCode");
        if (ELRM_ELCode != null && ELRM_ELCode.equals(ELCode)) {
          foundRegistered = true;
          break;
        }
        queryDataSet.next();
      }
      if (foundRegistered) {
        dbRet.setRetStr("EMAIL LIST EXISTS - REGISTERED");
        dbRet.setDbErrorCode(12);
        dbRet.setNoError(0);
      }
      else if (dbRet.getRetInt() > 0 && RMLoginTypeKey.equals(RegisteredMember.REGISTERED)) {
        dbRet.setRetStr("REGISTERED MEMBER ROW EXISTS. NOT ALLOWED TO ADD LIST");
        dbRet.setDbErrorCode(13);
        dbRet.setNoError(0);
      }            
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
      
    try {
      queryDataSet.close();
    }
    catch (Exception eclose) {
      dbRet.setNoError(0);
      eclose.printStackTrace();
    } 
    return dbRet;
  }
  
  private DbRet checkIfCanUnRegister(Database database, String RMEmail, String ELCode) {  
    DbRet dbRet = new DbRet();
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    String checkRMEmailQuery = "SELECT RMCode,RMEmail,RMLoginTypeKey,ELRM_ELCode,ELName"
                 + " FROM registeredMember LEFT JOIN ELRelRM ON RMCode=ELRM_RMCode LEFT JOIN emailList ON ELRM_ELCode=ELCode WHERE"
                 + " RMEmail = '" + RMEmail + "'";

    try {
      if (queryDataSet.isOpen()) queryDataSet.close();
      queryDataSet.setQuery(new QueryDescriptor(database,checkRMEmailQuery,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();
      dbRet.setRetInt( queryDataSet.getRowCount() );
      boolean foundRegistered = false;
      String RMLoginTypeKey = null;      
      for (int i=0; i<dbRet.getRetInt(); i++) {
        if (i==0) {
          RMLoginTypeKey = queryDataSet.getString("RMLoginTypeKey");          
          dbRet.setRetStr(queryDataSet.getString("RMCode"));        
        }
        String ELRM_ELCode = queryDataSet.getString("ELRM_ELCode");
        if (ELRM_ELCode != null && ELRM_ELCode.equals(ELCode)) {        
          foundRegistered = true;
          break;
        }
        queryDataSet.next();
      }
      if (!foundRegistered) {
        dbRet.setRetStr("EMAIL LIST DOES NOT EXIST");
        dbRet.setDbErrorCode(12);
        dbRet.setNoError(0);
      }
      else if (dbRet.getRetInt() > 0 && RMLoginTypeKey.equals(RegisteredMember.REGISTERED)) {
        dbRet.setRetStr("REGISTERED MEMBER ROW EXISTS. NOT ALLOWED TO REMOVE LIST");
        dbRet.setDbErrorCode(13);
        dbRet.setNoError(0);
      }
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
      
    try {
      queryDataSet.close();
    }
    catch (Exception eclose) {
      dbRet.setNoError(0);
      eclose.printStackTrace();
    } 
    return dbRet;
  }
  
 /* Εγγραφή pendingMember σε registeredMember
  */ 
  private DbRet NLRegister(HttpServletRequest request, String databaseId) {
    DbRet dbRet = new DbRet();
    String RMRegDate_ap = "'", RMRegDateStr = null;
    Timestamp RMRegDate = null;

    boolean confRegSent = false, registeredRowExists = false;
    String RMCode = null;
    String PMCode = SwissKnife.sqlEncode(request.getParameter("regCode"));
    String ELKey = SwissKnife.sqlEncode(request.getParameter("regKey"));    
  
    if (PMCode.length() == 0 || ELKey.length() == 0) {
      dbRet.setNoError(0);
      dbRet.setDbErrorCode(30);
      dbRet.setRetStr("NULL PARAMETERS PASSED");
    }
    
    int PMRows = 0;
    
    Database database = _director.getDBConnection(databaseId);
    QueryDataSet queryDataSet = new QueryDataSet();
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
  
    if (dbRet.getNoError() == 1) {
      dbRet = getPMRow(database, PMCode, ELKey);
      if (dbRet.getNoError() == 1) {
        PMCode = dbRet.getRetStr();
        PMRows = dbRet.getRetInt();
      }          
    }    
    
    if (dbRet.getNoError() == 1) {    
      dbRet = checkIfCanRegister(database, _PMDataSet.getString("PMEmail"), _PMDataSet.getString("ELPM_ELCode"));
    }
    if (dbRet.getNoError() == 1) {
      if (dbRet.getRetInt() > 0) {
        RMCode = dbRet.getRetStr();
        registeredRowExists = true;  // registered row exists but not this list
      }
      RMRegDate = _PMDataSet.getTimestamp("PMRegDate");
      if (RMRegDate == null) 
        RMRegDate_ap = "";
      else
        RMRegDateStr = RMRegDate.toString();            
    }
    
    String writeRegisteredMember = null;
    String insELRelRM = null;
    String delPendingMember = null;
    if (dbRet.getNoError() == 1 && !registeredRowExists) {
      writeRegisteredMember = "INSERT INTO registeredMember " +
                   " (RMCode,RMEmail,RMAltEmail,RMTitle,RMLastName," +
                   "  RMLastNameUp,RMFirstName,RMBirthDate,RMRegDate,RMCompanyName," +
                   "  RMCompanyNameUp,RMSex,RMAddress,RMZipCode," +
                   "  RMCity,RM_CNTRCode,RMPhone,RMMobile,RMIsActive," + 
                   " RMLoginTypeKey,RMLoginsCnt,RMLang,RM_LogCode)" +
                   " VALUES (" +
                   "'" + PMCode         + "'," +
                   "'" + _PMDataSet.getString("PMEmail")         + "'," +
                   "''," +
                   "''," +
                   "''," +            
                   "''," +            
                   "''," +                        
                   "null," +
                   RMRegDate_ap + RMRegDateStr + RMRegDate_ap + "," +              
                   "''," +
                   "''," +
                   "''," +
                   "''," +
                   "''," +
                   "''," +
                   "null," +
                   "''," +
                   "''," +            
                   "''," +            
                   "'" + RegisteredMember.IN_LISTS_NOT_REG + "'," +            
                   "0," +                        
                   "'" + _PMDataSet.getString("PMLang") + "'," +                          
                   "null" +                        
                ")";
    

      insELRelRM = "INSERT INTO ELRelRM " +
                   " (ELRMCode,ELRM_ELCode,ELRM_RMCode) " +
                   " VALUES (" +
                   "'" + _PMDataSet.getString("ELPMCode")         + "'," +
                   "'" + _PMDataSet.getString("ELPM_ELCode")         + "'," +
                   "'" + PMCode         + "')";              
    }
    else if (dbRet.getNoError() == 1 && registeredRowExists) {
      writeRegisteredMember = "UPDATE registeredMember SET " +
                   " RMLang = '" + _PMDataSet.getString("PMLang") + "'" +
                   " WHERE RMCode = '" + RMCode + "'";    
      
      insELRelRM = "INSERT INTO ELRelRM " +
                   " (ELRMCode,ELRM_ELCode,ELRM_RMCode) " +
                   " VALUES (" +
                   "'" + _PMDataSet.getString("ELPMCode")         + "'," +
                   "'" + _PMDataSet.getString("ELPM_ELCode")         + "'," +
                   "'" + RMCode         + "')";                    
    }
    if (dbRet.getNoError() == 1 && PMRows == 1) {
      delPendingMember = "DELETE FROM pendingMember"
            + " WHERE PMCode = '" + PMCode + "'";
    }
    else if (dbRet.getNoError() == 1 && PMRows > 1) {
      delPendingMember = "DELETE FROM ELRelPM"
            + " WHERE ELPM_PMCode = '" + PMCode + "'" +
              " AND ELPM_ELCode = '" + _PMDataSet.getString("ELPM_ELCode") + "'";
    }
      
    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(writeRegisteredMember);
    }
    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(insELRelRM);
    }
    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(delPendingMember);
    }
    if (dbRet.getNoError() == 1) {    
      try {
        confRegSent = sendIsRegisteredEmail();
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        dbRet.setDbErrorCode(13);
        dbRet.setRetStr("EMAIL COULD NOT BE SENT");
        e.printStackTrace();
      }
      if (!confRegSent) {
        dbRet.setNoError(0);
        dbRet.setDbErrorCode(13);
        dbRet.setRetStr("EMAIL COULD NOT BE SENT");
      }
    }
    closePMDataSet();
     
    
    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    _director.freeDBConnection(databaseId,database);    
    
    return dbRet;    
  }

  
 /* Εγγραφή pendingMember σε registeredMember
  */ 
  private DbRet UnRegister(HttpServletRequest request, String databaseId) {

    DbRet dbRet = new DbRet();
    String RMRegDate_ap = "'", RMRegDateStr = null;
    Timestamp RMRegDate = null;    

    boolean confRegSent = false, registeredRowExists = false;
    String RMCode = null, ELCode = "";
    String PMCode = SwissKnife.sqlEncode(request.getParameter("regCode"));
    String ELKey = SwissKnife.sqlEncode(request.getParameter("regKey"));    
  
    if (PMCode.length() == 0 || ELKey.length() == 0) {
      dbRet.setNoError(0);
      dbRet.setDbErrorCode(30);
      dbRet.setRetStr("NULL PARAMETERS PASSED");
    }
    
    int PMRows = 0;
    
    Database database = _director.getDBConnection(databaseId);
    QueryDataSet queryDataSet = new QueryDataSet();
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
  
    if (dbRet.getNoError() == 1) {
      dbRet = getPMRow(database, PMCode, ELKey);
      if (dbRet.getNoError() == 1) {
        PMCode = dbRet.getRetStr();
        PMRows = dbRet.getRetInt();
        ELCode = _PMDataSet.getString("ELPM_ELCode");
      }          
    }    
    
    if (dbRet.getNoError() == 1) {    
      dbRet = checkIfCanUnRegister(database, _PMDataSet.getString("PMEmail"), _PMDataSet.getString("ELPM_ELCode"));
    }
    if (dbRet.getNoError() == 1) {
      if (dbRet.getRetInt() > 0) {
        RMCode = dbRet.getRetStr();
        registeredRowExists = true;  // registered row exists
      }
    }
    
    String deleteRegisteredList = null;
    String delPendingMember = null;
    if (dbRet.getNoError() == 1 && registeredRowExists) {
      deleteRegisteredList = "DELETE FROM ELRelRM " +
                   " WHERE ELRM_ELCode = '" + ELCode + "'" +
                    " AND ELRM_RMCode = '" + RMCode + "'";
    }
    else
      dbRet.setNoError(0);
    if (dbRet.getNoError() == 1) {
      delPendingMember = "DELETE FROM pendingMember"
            + " WHERE PMCode = '" + PMCode + "'";
    }
      
    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(deleteRegisteredList);
    }
    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(delPendingMember);
    }
    if (dbRet.getNoError() == 1) {    
      try {
        confRegSent = sendIsUnRegisteredEmail(database);
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        dbRet.setDbErrorCode(13);
        dbRet.setRetStr("EMAIL COULD NOT BE SENT");
        e.printStackTrace();
      }
      if (!confRegSent) {
        dbRet.setNoError(0);
        dbRet.setDbErrorCode(13);
        dbRet.setRetStr("EMAIL COULD NOT BE SENT");
      }
    }
    closePMDataSet();
     
    
    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    _director.freeDBConnection(databaseId,database);    
    
    return dbRet;    
  }
  
  
  public boolean sendConfirmEmail(HttpServletRequest request, String PMCode, String PMEmail, String PMLang, String databaseId, String ELKey) throws ServletException, IOException {
    StringBuffer body = new StringBuffer();

    boolean send = false, errorFlag = false;
    
    String smtpServer = SwissKnife.sqlDecode( _ELDataSet.getString("ELSmtpServer") );
    String to = PMEmail;
    String from = SwissKnife.sqlDecode( _ELDataSet.getString("ELConfRegFrom") );
    String subject = SwissKnife.sqlDecode( _ELDataSet.getString("ELConfRegSubject" + PMLang) );

    String serverName = request.getServerName();    
    String message = SwissKnife.sqlDecode( _ELDataSet.getString("ELConfRegMessage" + PMLang) );

    
    if ( request.getHeader("Referer") != null) {
  	  if ( !request.getHeader("Referer").startsWith (request.getScheme() + "://" + serverName + "/")) {
        System.out.println( "Violation attempt of sendmail recorded from host "
          + request.getRemoteHost() + " (IP address " + request.getRemoteAddr()
          + ")." );
        errorFlag = true;
      }
    }
    else {
      System.out.println( "Violation attempt of sendmail recorded from host "
          + request.getRemoteHost() + " (IP address " + request.getRemoteAddr()
          + ")." );
      errorFlag = true;
    }
    
    // check required fields - all are required
    if (errorFlag || to.equals("") || from.equals("") || smtpServer.equals("")) {
      return send;
    }
    
    // build the body of the mail
    body.append(message + "\n\n");
    body.append("http://" + serverName + "/" + "servlet/NLSubscribe?action1=REGISTER&regCode=" + PMCode + "&regId=" + databaseId+ "&regKey=" + ELKey);
    
    send =  gr.softways.dev.util.SendMail.sendMessage(from,to,subject,body.toString(),smtpServer);

    return send;
  }

  public boolean sendIsRegisteredEmail() throws ServletException, IOException {
    StringBuffer body = new StringBuffer();

    boolean send = false;
    
    String smtpServer = SwissKnife.sqlDecode( _PMDataSet.getString("ELSmtpServer") );
    String to = SwissKnife.sqlDecode( _PMDataSet.getString("PMEmail") );
    String from = SwissKnife.sqlDecode( _PMDataSet.getString("ELConfRegFrom") );
    String lang = SwissKnife.sqlDecode( _PMDataSet.getString("PMLang") );    
    String subject = SwissKnife.sqlDecode( _PMDataSet.getString("ELThankRegSubject" + lang) );

    String message = SwissKnife.sqlDecode( _PMDataSet.getString("ELThankRegMessage" + lang) );

    // check required fields - all are required
    if (to.equals("") || from.equals("") || smtpServer.equals("")) {
      return send;
    }
    
    // build the body of the mail
    body.append(message);
    
    send =  gr.softways.dev.util.SendMail.sendMessage(from,to,subject,body.toString(),smtpServer);

    return send;
  }


  
  public boolean sendUnSubscribeEmail(HttpServletRequest request, String PMCode, String PMEmail, String PMLang, String databaseId, Database database, String ELKey, String ELName) throws ServletException, IOException {
    
    DbRet dbRet = null;
    
    dbRet = getELRow(database, RegisteredMember.ELKEY_FOR_LIST_MESSAGES);
    if (dbRet.getNoError() == 0) {
      dbRet.setNoError(0);
      dbRet.setDbErrorCode(13);
      dbRet.setRetStr("EMAIL COULD NOT BE SENT: DATABASE ERROR");      
      closeELDataSet();
      return false;
    }          
    
    
    StringBuffer body = new StringBuffer();

    boolean send = false, errorFlag = false;
    
    String smtpServer = SwissKnife.sqlDecode( _ELDataSet.getString("ELSmtpServer") );
    String to = PMEmail;
    String from = SwissKnife.sqlDecode( _ELDataSet.getString("ELConfRegFrom") );
    String subject = SwissKnife.sqlDecode( _ELDataSet.getString("ELConfRegSubject" + PMLang)) + ":" + ELName;

    String serverName = request.getServerName();    
    String message = SwissKnife.sqlDecode( _ELDataSet.getString("ELConfRegMessage" + PMLang) );

    
    if ( request.getHeader("Referer") != null) {
  	  if ( !request.getHeader("Referer").startsWith (request.getScheme() + "://" + serverName + "/")) {
        System.out.println( "Violation attempt of sendmail recorded from host "
          + request.getRemoteHost() + " (IP address " + request.getRemoteAddr()
          + ")." );
        errorFlag = true;
      }
    }
    else {
      System.out.println( "Violation attempt of sendmail recorded from host "
          + request.getRemoteHost() + " (IP address " + request.getRemoteAddr()
          + ")." );
      errorFlag = true;
    }
    closeELDataSet();    
    // check required fields - all are required
    if (errorFlag || to.equals("") || from.equals("") || smtpServer.equals("")) {
      return send;
    }
    
    // build the body of the mail
    body.append(message + "\n\n");
    body.append("http://" + serverName + "/" + "servlet/NLSubscribe?action1=UNREGISTER&regCode=" + PMCode + "&regId=" + databaseId+ "&regKey=" + ELKey);
    
    send =  gr.softways.dev.util.SendMail.sendMessage(from,to,subject,body.toString(),smtpServer);
    

    return send;
  }

  public boolean sendIsUnRegisteredEmail(Database database) throws ServletException, IOException {
    DbRet dbRet = null;
    
    dbRet = getELRow(database, RegisteredMember.ELKEY_FOR_LIST_MESSAGES);
    if (dbRet.getNoError() == 0) {
      dbRet.setNoError(0);
      dbRet.setDbErrorCode(13);
      dbRet.setRetStr("EMAIL COULD NOT BE SENT: DATABASE ERROR");      
      closeELDataSet();
      return false;
    }              
    
    StringBuffer body = new StringBuffer();

    boolean send = false;
    
    String smtpServer = SwissKnife.sqlDecode( _PMDataSet.getString("ELSmtpServer") );
    String to = SwissKnife.sqlDecode( _PMDataSet.getString("PMEmail") );
    String from = SwissKnife.sqlDecode( _ELDataSet.getString("ELConfRegFrom") );
    String lang = SwissKnife.sqlDecode( _PMDataSet.getString("PMLang") );    
    String subject = SwissKnife.sqlDecode( _ELDataSet.getString("ELThankRegSubject" + lang) ) + ":" + _PMDataSet.getString("ELName" + lang);

    String message = SwissKnife.sqlDecode( _ELDataSet.getString("ELThankRegMessage" + lang) );
    
    closeELDataSet();
    // check required fields - all are required
    if (to.equals("") || from.equals("") || smtpServer.equals("")) {
      return send;
    }
    
    // build the body of the mail
    body.append(message);
    
    send =  gr.softways.dev.util.SendMail.sendMessage(from,to,subject,body.toString(),smtpServer);

    return send;
  }
}
