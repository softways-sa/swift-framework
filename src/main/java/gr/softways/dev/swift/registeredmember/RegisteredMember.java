package gr.softways.dev.swift.registeredmember;

import java.sql.*;
import java.math.BigDecimal;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import javax.servlet.*;
import java.util.Hashtable;
import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;
import gr.softways.dev.poolmanager.AuthEmp;

public class RegisteredMember {
  
  public static String VISITOR_TYPE = "VIS";
  public static String IN_LISTS_NOT_REG = "LST";
  public static String REGISTERED = "REG";
  
  //public static int USERGROUPID = 3;
  
  public static String ELKEY_FOR_LIST_MESSAGES = "WWW";
  public static String ELKEY_FOR_MEMBER_MESSAGES = "ZZZ";

  public RegisteredMember(String databaseId) {
    _databaseId = databaseId;
  }

  public String getDatabaseId() { return _databaseId; }

  public String getRMCode() { return _RMCode; }
  public String getRM_LogCode() { return _RM_LogCode; }
  public String getRMEmail() { return _RMEmail; }
  public String getRMAltEmail() { return _RMAltEmail; }
  public String getRMTitle() { return _RMTitle; }
  public String getRMLastName() { return _RMLastName; }
  public String getRMFirstName() { return _RMFirstName; }
  public Timestamp getRMRegDate() { return _RMRegDate; }
  public Timestamp getRMBirthDate() { return _RMBirthDate; }
  public String getRMCompanyName() { return _RMCompanyName; }
  public String getRMSex() { return _RMSex; }
  public String getRMAddress() { return _RMAddress; }
  public String getRMZipCode() { return _RMZipCode; }
  public String getRMCity() { return _RMCity; }
  public String getRM_CNTRCode() { return _RM_CNTRCode; }
  public String getRMPhone() { return _RMPhone; }
  public String getRMMobile() { return _RMMobile; }
  public String getRMLoginTypeKey() { return _RMLoginTypeKey; }
  public int getRMLoginsCnt() { return _RMLoginsCnt; }
  public String getRMLang() { return _RMLang; }
  public String getAuthUsername() { return _authUsername; }  
  public String getAuthPassword() { return _authPassword; }  
  public String getLastIPUsed() { return _lastIPUsed; }
  public Timestamp getDateLastUsed() { return _dateLastUsed; }
  
  public void setRMCode(String RMCode) { _RMCode = RMCode; }
  public void setRM_LogCode(String RM_LogCode) { _RM_LogCode = RM_LogCode; }
  public void setRMEmail(String RMEmail) { _RMEmail = RMEmail; }
  public void setRMAltEmail(String RMAltEmail) { _RMAltEmail = RMAltEmail; }
  public void setRMTitle(String RMTitle) { _RMTitle = RMTitle; }
  public void setRMLastName(String RMLastName) { _RMLastName = RMLastName; }
  public void setRMFirstName(String RMFirstName) { _RMFirstName = RMFirstName; }
  public void setRMRegDate(Timestamp RMRegDate) { _RMRegDate = RMRegDate; }
  public void setRMBirthDate(Timestamp RMBirthDate) { _RMBirthDate = RMBirthDate; }
  public void setRMCompanyName(String RMCompanyName) { _RMCompanyName = RMCompanyName; }
  public void setRMSex(String RMSex) { _RMSex = RMSex; }
  public void setRMAddress(String RMAddress) { _RMAddress = RMAddress; }
  public void setRMZipCode(String RMZipCode) { _RMZipCode = RMZipCode; }
  public void setRMCity(String RMCity) { _RMCity = RMCity; }
  public void setRM_CNTRCode(String RM_CNTRCode) { _RM_CNTRCode = RM_CNTRCode; }
  public void setRMPhone(String RMPhone) { _RMPhone = RMPhone; }
  public void setRMMobile(String RMMobile) { _RMMobile = RMMobile; }
  public void setRMLoginTypeKey(String RMLoginTypeKey) { _RMLoginTypeKey = RMLoginTypeKey; }
  public void setRMLoginsCnt(int RMLoginsCnt) { _RMLoginsCnt = RMLoginsCnt; }
  public void setRMLang(String RMLang) { _RMLang = RMLang; }
  public void setAuthUsername(String authUsername) { _authUsername = authUsername; }
  public void setAuthPassword(String authPassword) { _authPassword = authPassword; }
  public void setLastIPUsed(String lastIPUsed) { _lastIPUsed = lastIPUsed; }
  public void setDateLastUsed(Timestamp dateLastUsed) { _dateLastUsed = dateLastUsed; }
  
  public DbRet doSignIn(HttpServletRequest request) {
    DbRet dbRet = new DbRet();
    
    String username = request.getParameter("username") == null ? "" : request.getParameter("username").toLowerCase(),
           password = request.getParameter("password") == null ? "" : request.getParameter("password");
           
    return doSignIn(request, username, password);
  }
  
  public DbRet doSignIn(HttpServletRequest request, String username, String password) {
    DbRet dbRet = new DbRet();
    
    password = Crypto.encrypt(password);
    
    PreparedStatement ps = null;
    ResultSet resultSet = null;
    
    int rowsAffected = 0;
    
    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(getDatabaseId());

    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();

    /* lock customer row in database */
    String query = "UPDATE registeredMember set RMLock=? WHERE RM_LogCode IN (SELECT logCode FROM users WHERE usrName=?)";
    
    if (dbRet.getNoError() == 1) {
      try {
        ps = database.createPreparedStatement(query);
        
        ps.setString(1, "1");
        ps.setString(2, username);
      
        rowsAffected = ps.executeUpdate();
      }
      catch (Exception eee) {
        dbRet.setNoError(0);
        eee.printStackTrace();
      }
      finally {
        try { if (ps != null) ps.close(); } catch (Exception e) { }
      }
    }
    
    if (dbRet.getNoError() == 1) {
      query = "SELECT *"
            + " FROM registeredMember,users,userGroups"
            + " WHERE RM_LogCode = logCode"
            + " AND usrAccessLevel = userGroupId"
            + " AND users.usrName = ?"
            + " AND users.usrPasswd = ?"
            + " AND registeredMember.RMLoginTypeKey = '" + RegisteredMember.REGISTERED + "'"
            + " AND registeredMember.RMIsActive = '1'";

      try {
        ps = database.createPreparedStatement(query);
        ps.setString(1, username);
        ps.setString(2, password);
        
        resultSet = ps.executeQuery();
        
        if (resultSet.next() == true) {
          // username & password pair valid
          AuthEmp authEmp = new AuthEmp(username,password,resultSet.getInt("usrAccessLevel"),getDatabaseId());
          
          setRMCode(resultSet.getString("RMCode").trim());
          setRM_LogCode(SwissKnife.sqlDecode(resultSet.getString("RM_LogCode")).trim());
          setRMEmail(SwissKnife.sqlDecode(resultSet.getString("RMEmail")).trim());
          setRMAltEmail(SwissKnife.sqlDecode(resultSet.getString("RMAltEmail")).trim());
          setRMTitle(SwissKnife.sqlDecode(resultSet.getString("RMTitle")).trim());
          setRMLastName(SwissKnife.sqlDecode(resultSet.getString("RMLastName")).trim());
          setRMFirstName(SwissKnife.sqlDecode(resultSet.getString("RMFirstName")).trim());
          setRMRegDate(resultSet.getTimestamp("RMRegDate"));          
          setRMBirthDate(resultSet.getTimestamp("RMBirthDate"));
          setRMCompanyName(SwissKnife.sqlDecode(resultSet.getString("RMCompanyName")).trim());
          setRMSex(SwissKnife.sqlDecode(resultSet.getString("RMSex")).trim());
          setRMAddress(SwissKnife.sqlDecode(resultSet.getString("RMAddress")).trim());
          setRMZipCode(SwissKnife.sqlDecode(resultSet.getString("RMZipCode")).trim());
          setRMCity(SwissKnife.sqlDecode(resultSet.getString("RMCity")).trim());
          setRM_CNTRCode(SwissKnife.sqlDecode(resultSet.getString("RM_CNTRCode")).trim());
          setRMPhone(SwissKnife.sqlDecode(resultSet.getString("RMPhone")).trim());
          setRMMobile(SwissKnife.sqlDecode(resultSet.getString("RMMobile")).trim());
          setRMLoginTypeKey(SwissKnife.sqlDecode(resultSet.getString("RMLoginTypeKey")).trim());
          setRMLoginsCnt(resultSet.getInt("RMLoginsCnt") + 1);
          setRMLang(SwissKnife.sqlDecode(resultSet.getString("RMLang")).trim());

          setAuthUsername(username);
          setAuthPassword(password);
          setDateLastUsed(SwissKnife.currentDate());
          setLastIPUsed(request.getRemoteAddr());
          
          director.addAuthUser(getDatabaseId(), authEmp);

          HttpSession session = null;
        
          session = request.getSession();

          session.setAttribute(getDatabaseId() + ".unbindObject", authEmp);
          session.setAttribute(getDatabaseId() + ".authUsername", username);
          session.setAttribute(getDatabaseId() + ".authPassword", password);
        }
        else {
          dbRet.setNoError(0);
          dbRet.setRetInt(99);
          
          System.out.println("[" + SwissKnife.currentDate() + " " + getDatabaseId() + "] " + username + " failed to login.");
        }
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
      finally {
        try { if (resultSet != null) resultSet.close(); } catch (Exception e) { }
        try { if (ps != null) ps.close(); } catch (Exception e) { }
      }
    }
    
    /* update customer login data in database */
    if (dbRet.getNoError() == 1) {
      query = "UPDATE registeredMember set RMLoginsCnt=? WHERE RMCode=?";
      
      try {
        ps = database.createPreparedStatement(query);
        
        ps.setInt(1, getRMLoginsCnt());
        ps.setString(2, _RMCode);
        
        rowsAffected = ps.executeUpdate();
      }
      catch (Exception ee) {
        dbRet.setNoError(0);
        ee.printStackTrace();
      }
      finally {
        try { if (resultSet != null) resultSet.close(); } catch (Exception e) { }
        try { if (ps != null) ps.close(); } catch (Exception e) { }
      }
    }
    
    /* update user login data in database */
    if (dbRet.getNoError() == 1) {
      query = "UPDATE users set dateLastUsed=?, lastIPUsed=? WHERE usrName=?";
      
      try {
        ps = database.createPreparedStatement(query);
        
        ps.setTimestamp(1, _dateLastUsed);
        ps.setString(2, getLastIPUsed());
        ps.setString(3, username);
      
        rowsAffected = ps.executeUpdate();
      }
      catch (Exception eeee) {
        dbRet.setNoError(0);
        eeee.printStackTrace();
      }
      finally {
        try { if (resultSet != null) resultSet.close(); } catch (Exception e) { }
        try { if (ps != null) ps.close(); } catch (Exception e) { }
      }
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    director.freeDBConnection(getDatabaseId(), database);
    
    return dbRet;
  }
  
  public DbRet doSignOut(HttpServletRequest request) {
    DbRet dbRet = new DbRet();

    setRMCode("");
    setRM_LogCode("");
    setRMEmail("");
    setRMAltEmail("");
    setRMTitle("");
    setRMLastName("");
    setRMFirstName("");
    setRMRegDate(null);
    setRMBirthDate(null);
    setRMCompanyName("");
    setRMSex("");
    setRMAddress("");
    setRMZipCode("");
    setRMCity("");
    setRM_CNTRCode("");
    setRMPhone("");
    setRMMobile("");
    setRMLoginTypeKey(VISITOR_TYPE);
    setRMLoginsCnt(0);
    setRMLang("");
    
    setAuthUsername(null);
    setAuthPassword(null);
    setDateLastUsed(null);
    setLastIPUsed("");
    
    HttpSession session = request.getSession();
    session.removeAttribute(getDatabaseId() + ".unbindObject");
    
    return dbRet;
  }
  
  public boolean isSignedIn() {
    if (getRMCode() != null && getRMCode().length() > 0) return true;
    else return false;
  }
  
  public DbRet doRetrievePassword(HttpServletRequest request) {
    DbRet dbRet = new DbRet();
    
    String smtpServer = SwissKnife.sqlEncode(request.getParameter("smtpServer")),
           defaultMailSender = SwissKnife.sqlEncode(request.getParameter("defaultMailSender")),            
           RMEmail = SwissKnife.sqlEncode(request.getParameter("RMEmail")).toLowerCase();
    
    QueryDataSet queryDataSet = null;
    
    String query  = "SELECT usrPasswd,usrName,RMEmail FROM users,registeredMember WHERE RM_logCode = logCode AND RMEmail = '" + RMEmail + "'";
    
    StringBuffer body = new StringBuffer();
    
    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(getDatabaseId());

    try {
      queryDataSet = new QueryDataSet();
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      queryDataSet.refresh();

      if (queryDataSet.getRowCount() == 1) {
        String subject = "";
        
        if (getRMLang().equals("")) {
          subject = "Αίτηση ανάκτησης κωδικού πρόσβασης";
          body.append("Ο κωδικός πρόσβασης ανακτήθηκε επιτυχώς: " + Crypto.decrypt(queryDataSet.getString("usrPasswd")) + "\n\n");
        }
        else {
          subject = "User password retrieval";
          body.append("Your password was retrieved successfully: " + Crypto.decrypt(queryDataSet.getString("usrPasswd")) + "\n\n");
        }
        
        EMail msg = new EMail(RMEmail,defaultMailSender,subject,body.toString(),smtpServer,"text/plain","UTF-8",null);
        
        boolean sent = SendMail.sendMessage(msg);
        
        if (sent == true) {
          dbRet.setNoError(1);
        }
        else {
          dbRet.setNoError(0);
        }
      }
      else dbRet.setNoError(0);
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      
      e.printStackTrace();
    }
    finally {
      if (queryDataSet != null) queryDataSet.close();
    }
    
    director.freeDBConnection(getDatabaseId(), database);
    
    return dbRet;
  }
  
  public DbRet doUpdatePInfo(HttpServletRequest request) {
    String authUsername = getAuthUsername(),
           authPassword = getAuthPassword();
           
    Director director = Director.getInstance();
    
    int auth = director.auth(getDatabaseId(),authUsername,authPassword,"registeredMember",Director.AUTH_READ);
    
    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      return dbRet;
    }

    String RMCode = getRMCode(),
           //RMEmail = SwissKnife.sqlEncode(request.getParameter("RMEmail")),
           RMTitle = SwissKnife.sqlEncode(request.getParameter("RMTitle")),
           RMLastName = SwissKnife.sqlEncode(request.getParameter("RMLastName")), 
           RMFirstName = SwissKnife.sqlEncode(request.getParameter("RMFirstName")),
           RMCompanyName = SwissKnife.sqlEncode(request.getParameter("RMCompanyName")),
           RMSex = SwissKnife.sqlEncode(request.getParameter("RMSex")),
           RMAddress = SwissKnife.sqlEncode(request.getParameter("RMAddress")),
           RMZipCode = SwissKnife.sqlEncode(request.getParameter("RMZipCode")),
           RMCity = SwissKnife.sqlEncode(request.getParameter("RMCity")),
           RM_CNTRCode = SwissKnife.sqlEncode(request.getParameter("RM_CNTRCode")),
           RMPhone = SwissKnife.sqlEncode(request.getParameter("RMPhone")),
           RMMobile = SwissKnife.sqlEncode(request.getParameter("RMMobile")),
           RMLang = SwissKnife.sqlEncode(request.getParameter("RMLang")),
           RMBirthDateDay=null, RMBirthDateMonth=null, RMBirthDateYear=null,
           RMPeriodDateDay=null, RMPeriodDateMonth=null, RMPeriodDateYear=null;
    
    Timestamp RMBirthDate=null, RMPeriodDate=null;

    String RM_CNTRCode_ap = "'";
    if (RM_CNTRCode.length() == 0) {
      RM_CNTRCode = null;
      RM_CNTRCode_ap = "";
    }
    
    if ( (RMBirthDateDay = request.getParameter("RMBirthDateDay")) == null ) RMBirthDateDay = "";
    if ( (RMBirthDateMonth = request.getParameter("RMBirthDateMonth")) == null ) RMBirthDateMonth = "";
    if ( (RMBirthDateYear = request.getParameter("RMBirthDateYear")) == null ) RMBirthDateYear = "";
    RMBirthDate = SwissKnife.buildTimestamp(RMBirthDateDay,RMBirthDateMonth,RMBirthDateYear, "0", "0", "0", "0");
    
    String RMBirthDate_ap = "'", RMBirthDateStr = null;
    if (RMBirthDate == null) RMBirthDate_ap = "";
    else RMBirthDateStr = RMBirthDate.toString();

    String RMLastNameUp = SwissKnife.searchConvert(RMLastName);
    String RMCompanyNameUp = SwissKnife.searchConvert(RMCompanyName);
        
    String updRegisteredMember = "UPDATE registeredMember SET " +
                   //" RMEmail = '" + RMEmail + "'," +
                   " RMTitle = '" + RMTitle + "'," +
                   " RMLastName = '" + RMLastName + "'," +
                   " RMLastNameUp = '" + RMLastNameUp + "'," +
                   " RMFirstName = '" + RMFirstName + "'," +
                   " RMBirthDate = " + RMBirthDate_ap + RMBirthDateStr + RMBirthDate_ap + "," +
                   " RMCompanyName = '" + RMCompanyName + "'," +
                   " RMCompanyNameUp = '" + RMCompanyNameUp + "'," +
                   " RMSex = '" + RMSex + "'," +
                   " RMAddress = '" + RMAddress + "'," +
                   " RMZipCode = '" + RMZipCode + "'," +
                   " RMCity = '" + RMCity + "'," +
                   " RM_CNTRCode = " + RM_CNTRCode_ap + RM_CNTRCode + RM_CNTRCode_ap + "," +
                   " RMPhone = '" + RMPhone + "'," +
                   " RMMobile = '" + RMMobile + "'," +
                   " RMLang = '" + RMLang + "'" +
                   " WHERE RMCode = '" + RMCode + "'";

    Database database = director.getDBConnection(getDatabaseId());
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();

    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(updRegisteredMember);
    }

    if (dbRet.getNoError() == 1) {
      //setRMEmail(RMEmail);
      setRMTitle(RMTitle);
      setRMLastName(RMLastName);
      setRMFirstName(RMFirstName);
      setRMBirthDate(RMBirthDate);
      setRMCompanyName(RMCompanyName);
      setRMSex(RMSex);
      setRMAddress(RMAddress);
      setRMZipCode(RMZipCode);
      setRMCity(RMCity);
      setRM_CNTRCode(RM_CNTRCode);
      setRMPhone(RMPhone);
      setRMMobile(RMMobile);
      setRMLang(RMLang);
    }
    
    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    director.freeDBConnection(getDatabaseId(),database);
    
    return dbRet;
  }
  
  public DbRet doUpdateCredentials(HttpServletRequest request) {
    String authUsername = getAuthUsername(),
           authPassword = getAuthPassword();
           
    Director director = Director.getInstance();
    
    int auth = director.auth(getDatabaseId(),authUsername,authPassword,"registeredMember",Director.AUTH_READ);
    
    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      return dbRet;
    }

    String usrPasswd = SwissKnife.sqlEncode(request.getParameter("usrPasswd"));

    if (usrPasswd.trim().length()>0) {
      usrPasswd = Crypto.encrypt(usrPasswd);
    }    
    else {
      dbRet.setNoError(0);
      return dbRet;      
    }
    
    String updUsers = "UPDATE users SET " +
                   " usrPasswd = '" + usrPasswd + "'" +
                   " WHERE usrName = '" + getAuthUsername() + "'";    

    Database database = director.getDBConnection(getDatabaseId());
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();

    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(updUsers);
    }

    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    director.freeDBConnection(getDatabaseId(),database);
    
    return dbRet;
  }
  
  public DbRet doRemoveList(HttpServletRequest request) {
    String authUsername = getAuthUsername(),
           authPassword = getAuthPassword();
           
    Director director = Director.getInstance();
    
    int auth = director.auth(getDatabaseId(),authUsername,authPassword,"ELRelRM",Director.AUTH_DELETE);
    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      return dbRet;
    }

    String ELCode = SwissKnife.sqlEncode(request.getParameter("code"));
    
    String RMCode = this.getRMCode();
    
    if (RMCode == null || RMCode.equals("") || ELCode.equals("")) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    dbRet = null;
    
    // get database connection
    Database database = director.getDBConnection(getDatabaseId());

    // begin transaction
    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();

    String delELRelRM = "DELETE FROM ELRelRM"
                     + " WHERE ELRM_ELCode = '" + ELCode + "'"
                     + "   AND ELRM_RMCode = '" + RMCode + "'";
    
    
    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(delELRelRM);
    }
    
    // End transaction (commit or rollback)
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    director.freeDBConnection(getDatabaseId(),database);

    return dbRet;
  }
  
  public DbRet doAddList(HttpServletRequest request) {
    String authUsername = getAuthUsername(),
           authPassword = getAuthPassword();
           
    Director director = Director.getInstance();
    
    int auth = director.auth(getDatabaseId(),authUsername,authPassword,"ELRelRM",Director.AUTH_INSERT);
    
    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String ELCode = SwissKnife.sqlEncode(request.getParameter("code"));
    
    String RMCode = this.getRMCode();
    
    if (RMCode == null || RMCode.equals("") || ELCode.equals("")){
      dbRet.setNoError(0);
      
      return dbRet;
    }
    dbRet = null;
    
    // get database connection
    Database database = director.getDBConnection(getDatabaseId());

    // begin transaction
    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();

    String insELRelRM = "INSERT INTO ELRelRM (ELRMCode,ELRM_ELCode,ELRM_RMCode) VALUES" + 
            " ('" + SwissKnife.buildPK() + "','" + ELCode + "','" +  RMCode + "')";
    
    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(insELRelRM);
    }
    
    // End transaction (commit or rollback)
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    director.freeDBConnection(getDatabaseId(),database);

    return dbRet;
  }

  protected String _authUsername;
  protected String _authPassword;
  protected String _databaseId;
  
  protected String _RMCode;
  protected String _RMEmail;
  protected String _RMAltEmail;
  protected String _RMTitle;
  protected String _RMLastName;
  protected String _RMFirstName;
  protected Timestamp _RMBirthDate;
  protected Timestamp _RMRegDate;
  protected String _RMCompanyName;
  protected String _RMSex;
  protected String _RMAddress;
  protected String _RMZipCode;
  protected String _RMCity;
  protected String _RM_CNTRCode;
  protected String _RMPhone;
  protected String _RMMobile;
  protected String _RMLoginTypeKey;
  protected int _RMLoginsCnt;
  protected String _RMLang;
  protected String _RM_LogCode;

  protected Timestamp _dateLastUsed;
  protected String _lastIPUsed = "";
}
