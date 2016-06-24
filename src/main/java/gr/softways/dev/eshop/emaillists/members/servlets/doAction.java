package gr.softways.dev.eshop.emaillists.members.servlets;

import java.io.*;
import java.util.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

import gr.softways.dev.eshop.emaillists.lists.Present;

public class doAction extends HttpServlet {

  private Director bean;
    
  private String _charset = null;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    bean = Director.getInstance();
  }

  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
              throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1"),
           databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId").trim(),
           urlSuccess = request.getParameter("urlSuccess") == null ? "" : request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");

    int status = Director.STATUS_OK;

    if (databaseId.equals(""))
      status = Director.STATUS_ERROR;
    else if (action.equals("INSERT"))
      status = doInsert(request, databaseId);
    else if (action.equals("INSERT_BATCH"))
      status = doInsertBatch(request, databaseId);
    else if (action.equals("UPDATE"))
      status = doUpdate(request,databaseId);
    else if (action.equals("DELETE"))
      status = doDelete(request, databaseId);
    else if (action.equals("REGISTER"))
      status = doRegister(request, databaseId);
    else if (action.equals("UNREGISTER"))
      status = doUnregister(request, databaseId);
    
    if (status < 0) {
      response.sendRedirect(urlNoAccess);
    }
    else if (status == Director.STATUS_OK)
      response.sendRedirect(urlSuccess);
    else
      response.sendRedirect(urlFailure);
  }

  /**
   *  Καταχώρηση νέου στο RDBMS.
   *
   * @param  request    το HttpServletRequest από την σελίδα
   * @param  databaseId το αναγνωριστικό της βάσης που
   *                    θα χρησιμοποιηθεί
   * @return            κωδικό κατάστασης
   */
  public int doInsert(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);
    
    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "emailListMember", bean.AUTH_INSERT);

    if (auth < 0) {
      return auth;
    }

    DbRet dbRet = null;

    int status = Director.STATUS_OK;
    
    String EMLMEmail = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMEmail") ) ),
           EMLMAltEmail = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMAltEmail") ) ),
           EMLMLastName = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMLastName") ) ),
           EMLMFirstName = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMFirstName") ) ),
           EMLMCompanyName = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMCompanyName") ) ),
           EMLMAddress = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMAddress") ) ),
           EMLMZipCode = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMZipCode") ) ),
           EMLMCity = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMCity") ) ),
           EMLMCountry = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMCountry") ) ),
           EMLMPhone = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMPhone") ) ),
           EMLMActive = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMActive") ) ),
           EMLMField1 = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMField1") ) ),
           EMLMField2 = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMField2") ) ),
           EMLMField3 = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMField3") ) );

    String EMLMCode = SwissKnife.buildPK();

    if (EMLMEmail.trim().equals("")) return Director.STATUS_ERROR;
    
    EMLMEmail = EMLMEmail.toLowerCase();
    
    Timestamp EMLMBirthDate = SwissKnife.buildTimestamp(request.getParameter("EMLMBirthDateDay"),
                                                        request.getParameter("EMLMBirthDateMonth"),
                                                        request.getParameter("EMLMBirthDateYear"));
    
    Timestamp EMLMRegDate = SwissKnife.buildTimestamp(request.getParameter("EMLMRegDateDay"),
                                                      request.getParameter("EMLMRegDateMonth"),
                                                      request.getParameter("EMLMRegDateYear"));
    if (EMLMRegDate == null) {
      EMLMRegDate = SwissKnife.currentDate();
    }

    String EMLMLastNameUp = SwissKnife.searchConvert(EMLMLastName),
           EMLMCompanyNameUp = SwissKnife.searchConvert(EMLMCompanyName);

    Database database = bean.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    
    int prevTransIsolation = dbRet.getRetInt();
    
    String insert = "INSERT INTO emailListMember (EMLMCode,EMLMEmail"
                    + ",EMLMAltEmail,EMLMLastName,EMLMLastNameUp"
                    + ",EMLMFirstName,EMLMBirthDate,EMLMRegDate"
                    + ",EMLMCompanyName,EMLMCompanyNameUp,EMLMAddress"
                    + ",EMLMZipCode,EMLMCity,EMLMCountry,EMLMPhone,EMLMActive"
                    + ",EMLMField1,EMLMField2,EMLMField3"
                    + ")"
                    + " VALUES ("
                    + "'"  + EMLMCode   + "'"
                    + ",'" + EMLMEmail  + "'"
                    + ",'" + EMLMAltEmail  + "'"
                    + ",'" + EMLMLastName  + "'"
                    + ",'"  + EMLMLastNameUp + "'"
                    + ",'"  + EMLMFirstName + "'";

    if (EMLMBirthDate != null) {
      insert += ",'"  + EMLMBirthDate + "'";
    }
    else {
      insert += ",null";
    }
    
    insert += ",'"  + EMLMRegDate + "'"
            + ",'"  + EMLMCompanyName + "'"
            + ",'"  + EMLMCompanyNameUp + "'"
            + ",'"  + EMLMAddress + "'"
            + ",'"  + EMLMZipCode + "'"
            + ",'"  + EMLMCity + "'"
            + ",'"  + EMLMCountry + "'"
            + ",'"  + EMLMPhone + "'"
            + ",'"  + EMLMActive + "'"
            + ",'"  + EMLMField1 + "'"
            + ",'"  + EMLMField2 + "'"
            + ",'"  + EMLMField3 + "'"
            + ")";

    dbRet = database.execQuery(insert);

    if (dbRet.getNoError() == 1) {
      dbRet = doUpdateMemberLists(request, databaseId, database, EMLMCode);
    }    
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    
    bean.freeDBConnection(databaseId,database);
    
    if (dbRet.getNoError() == 0) {
      status = bean.STATUS_ERROR;
    }
    else {
      status = bean.STATUS_OK;
    }

    return status;
  }

  private DbRet doUpdateMemberLists(HttpServletRequest request, String databaseId,
                                   Database database, String EMLMCode) {
    
    DbRet dbRet = new DbRet();                               
    int listsRows = SwissKnife.grEncode( request.getParameter("listsRows") ).equals("") ? 0 : Integer.parseInt(request.getParameter("listsRows"));

    String choice = null, updated = null,
           EMLRCode = null, EMLTCode = null;

    for (int i=0; i<listsRows && dbRet.getNoError() == 1; i++) {
      choice = SwissKnife.grEncode( request.getParameter("listMemberActive_" + i) ).equals("") ? "0" : "1";
      updated = SwissKnife.grEncode( request.getParameter("updated_" + i) );
      
      if (updated.equals("1") && choice.equals("0")) {
        // delete from list
        EMLRCode = request.getParameter("regCode_" + i);

        dbRet = doRemoveFromList(EMLRCode, database);
      }
      else if (updated.equals("1")) {
        // add to list
        EMLTCode = SwissKnife.grEncode( request.getParameter("listCode_" + i) );

        dbRet = doAddToList(EMLTCode, EMLMCode, database);
      }
    }
    
    return dbRet;
  }

  private DbRet doRemoveFromList(String EMLRCode, Database database) {
    DbRet dbRet = new DbRet(); 
    
    String query = "DELETE FROM emailListReg"
                 + " WHERE EMLRCode = '" + EMLRCode + "'";

    dbRet = database.execQuery(query);
    
    return dbRet;
  }

  /**
   * Registers a member to a list (only if he is not already registered)
   */
  private DbRet doAddToList(String EMLTCode, String EMLMCode, Database database) {

    DbRet dbRet = new DbRet();

    QueryDataSet queryDataSet = null;
    
    String query = "SELECT EMLRCode FROM emailListReg"
                 + " WHERE EMLRListCode = '" + EMLTCode + "'"
                 + " AND EMLRMemberCode = '" + EMLMCode + "'";
    
    try {
      queryDataSet = new QueryDataSet();
      
      queryDataSet.setQuery(new QueryDescriptor(database, query, null, 
                                                true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      queryDataSet.refresh();
      
      if (queryDataSet.getRowCount() == 0) {
        // certainty loop
        dbRet.setRetry(1);
        int retries = 0;

        for (; dbRet.getRetry() == 1 && retries < 35; retries++) {

          query = "INSERT INTO emailListReg (EMLRCode,EMLRListCode"
                + ",EMLRMemberCode)"
                + " VALUES ("
                + "'"  + SwissKnife.buildPK()      + "'"
                + ",'" + EMLTCode  + "'"
                + ",'" + EMLMCode  + "'"
                + ")";

          dbRet = database.execQuery(query);
        }
      }
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    finally {
      if (queryDataSet != null) queryDataSet.close();
      
      queryDataSet = null;
    }
    
    return dbRet;
  }

  /**
    *  Μεταβολή στο RDBMS.
    *
    * @param  request    το HttpServletRequest από την σελίδα
    * @param  databaseId το αναγνωριστικό της βάσης που
    *                    θα χρησιμοποιηθεί
    * @return            κωδικό κατάστασης
   */
  public int doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "emailListMember", bean.AUTH_UPDATE);

    if (auth < 0) {
      return auth;
    }

    DbRet dbRet = null;

    int status = Director.STATUS_OK;   

    String EMLMCode = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMCode") ) ),
           EMLMEmail = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMEmail") ) ),
           EMLMAltEmail = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMAltEmail") ) ),
           EMLMLastName = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMLastName") ) ),
           EMLMFirstName = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMFirstName") ) ),
           EMLMCompanyName = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMCompanyName") ) ),
           EMLMAddress = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMAddress") ) ),
           EMLMZipCode = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMZipCode") ) ),
           EMLMCity = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMCity") ) ),
           EMLMCountry = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMCountry") ) ),
           EMLMPhone = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMPhone") ) ),
           EMLMActive = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMActive") ) ),
           EMLMField1 = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMField1") ) ),
           EMLMField2 = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMField2") ) ),
           EMLMField3 = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMField3") ) );

    if (EMLMEmail.trim().equals("")) return Director.STATUS_ERROR;
    
    EMLMEmail = EMLMEmail.toLowerCase();
    
    Timestamp EMLMBirthDate = SwissKnife.buildTimestamp(request.getParameter("EMLMBirthDateDay"),
                                                        request.getParameter("EMLMBirthDateMonth"),
                                                        request.getParameter("EMLMBirthDateYear"));
    
    Timestamp EMLMRegDate = SwissKnife.buildTimestamp(request.getParameter("EMLMRegDateDay"),
                                                      request.getParameter("EMLMRegDateMonth"),
                                                      request.getParameter("EMLMRegDateYear"));
    if (EMLMRegDate == null) {
      EMLMRegDate = SwissKnife.currentDate();
    }

    String EMLMLastNameUp = SwissKnife.searchConvert(EMLMLastName),
           EMLMCompanyNameUp = SwissKnife.searchConvert(EMLMCompanyName);

    Database database = bean.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    
    int prevTransIsolation = dbRet.getRetInt();
    
    String update = "UPDATE emailListMember SET "
                  + "EMLMEmail = '"  + EMLMEmail + "'"
                  + ",EMLMAltEmail = '" + EMLMAltEmail + "'"
                  + ",EMLMLastName = '" + EMLMLastName + "'"
                  + ",EMLMLastNameUp = '" + EMLMLastNameUp + "'"
                  + ",EMLMFirstName = '" + EMLMFirstName + "'";
    
    if (EMLMBirthDate != null) {
      update += ",EMLMBirthDate = '"  + EMLMBirthDate + "'";
    }
    else {
      update += ",EMLMBirthDate = null";
    }

    update += ",EMLMRegDate = '" + EMLMRegDate + "'"
            + ",EMLMCompanyName = '"  + EMLMCompanyName + "'"
            + ",EMLMCompanyNameUp = '"  + EMLMCompanyNameUp + "'"
            + ",EMLMAddress = '" + EMLMAddress + "'"
            + ",EMLMZipCode = '" + EMLMZipCode + "'"
            + ",EMLMCity = '" + EMLMCity + "'"
            + ",EMLMCountry = '" + EMLMCountry + "'"
            + ",EMLMPhone = '"  + EMLMPhone + "'"
            + ",EMLMActive = '"  + EMLMActive + "'"
            + ",EMLMField1 = '" + EMLMField1 + "'"
            + ",EMLMField2 = '" + EMLMField2 + "'"
            + ",EMLMField3 = '" + EMLMField3 + "'"
            + " WHERE EMLMCode = '" + EMLMCode + "'";
    
    dbRet = database.execQuery(update);

    if (dbRet.getNoError() == 1) {
      dbRet = doUpdateMemberLists(request, databaseId, database, EMLMCode);
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    
    bean.freeDBConnection(databaseId,database);
    
    if (dbRet.getNoError() == 0) {
      status = Director.STATUS_ERROR;
    }
    else {
      status = Director.STATUS_OK;
    }
    
    return status;
  }


  /**
   * Διαγραφή
   *
   */
  public int doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int authStatus = 0;

    authStatus = bean.auth(databaseId,authUsername,authPassword,
                           "emailListMember", Director.AUTH_DELETE);

    if (authStatus < 0) {
      return authStatus;
    }

    String EMLMCode = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMCode") ) );

    if (EMLMCode.trim().equals("")) return Director.STATUS_ERROR;

    Database database = bean.getDBConnection(databaseId);

    int status = Director.STATUS_OK;

    DbRet dbRet = null;

    String delete = "DELETE FROM emailListMember"
                  + " WHERE EMLMCode = '" + EMLMCode + "'";

    dbRet = database.execQuery(delete);

    bean.freeDBConnection(databaseId,database);
    
    if (dbRet.getNoError() == 0) {
      status = Director.STATUS_ERROR;
    }
    else {
      status = Director.STATUS_OK;
    }
    
    return status;
  }

  /**
   * Unregister user by setting his status to UNREGISTER using his email.
   *
   */
  public int doUnregister(HttpServletRequest request, String databaseId) {
    String EMLMEmail = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMEmail") ) ),
           EMLMActive = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMActive") ) );

    if (EMLMEmail.trim().equals("")) return Director.STATUS_ERROR;
    
    EMLMEmail = EMLMEmail.toLowerCase();
    
    Database database = bean.getDBConnection(databaseId);

    int status = Director.STATUS_OK;
    
    DbRet dbRet = new DbRet();
    
    QueryDataSet queryDataSet = null;

    String EMLMCode = "";
    
    try {
      queryDataSet = new QueryDataSet();

      String query = "SELECT EMLMCode FROM emailListMember"
                   + " WHERE EMLMEmail = '" + EMLMEmail + "'";
      
      queryDataSet.setQuery(new QueryDescriptor(database, query, null, 
                                                true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      queryDataSet.refresh();
      
      if (queryDataSet.isEmpty() == true) {
        dbRet.setNoError(0);
      }
      else {
        EMLMCode = queryDataSet.getString("EMLMCode");
            
        query = "UPDATE emailListMember SET "
              + " EMLMActive = '" + EMLMActive + "'"
              + " WHERE EMLMCode = '" + EMLMCode + "'";
      
        dbRet = database.execQuery(query);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      dbRet.setNoError(0);
    }
    finally {
      try { queryDataSet.close(); } catch (DataSetException e) { }
    }
    
    bean.freeDBConnection(databaseId,database);
    
    if (dbRet.getNoError() == 0) {
      status = Director.STATUS_ERROR;
    }
    else {
      status = Director.STATUS_OK;
    }
    
    return status;
  }
  
  /**
   * Μαζική ειγαγωγή μελών (μόνο email & λίστα)
   *
   */
  public int doInsertBatch(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);
    
    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "emailListMember", Director.AUTH_INSERT);

    if (auth < 0) {
      return auth;
    }

    DbRet dbRet = null;
        
    int status = Director.STATUS_OK;
    
    String emailMembers = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("emailMembers") ) ),
           EMLRListCode = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLRListCode") ) );   

    if (emailMembers.trim().equals("") || EMLRListCode.trim().equals(""))
      return Director.STATUS_ERROR;
    
    String EMLMCode = "", EMLMEmail = "";
    
    StringBuffer query = new StringBuffer();
    
    Database database = bean.getDBConnection(databaseId);

    int prevTransIsolation = 0;   
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    
    prevTransIsolation = dbRet.getRetInt();
               
    StringTokenizer emailMembersTokenizer = new StringTokenizer(emailMembers, "\n");
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    while (emailMembersTokenizer.hasMoreTokens() && dbRet.getNoError() == 1) {    
      EMLMEmail = emailMembersTokenizer.nextToken();
      EMLMEmail = EMLMEmail.toLowerCase();
      EMLMEmail = EMLMEmail.replace('\n',' '); // line feed - new line
      EMLMEmail = EMLMEmail.replace('\r',' '); // carriage return
      EMLMEmail = EMLMEmail.trim();
      EMLMEmail = SwissKnife.sqlEncode(EMLMEmail);
      
      try {
        query.setLength(0);
        query.append("SELECT EMLMCode FROM emailListMember");
        query.append(" WHERE EMLMEmail = '" + EMLMEmail + "'");
        
        queryDataSet.setQuery(new QueryDescriptor(database, query.toString(), 
                                                  null, true, Load.ALL));
        queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
        
        queryDataSet.refresh();
        
        if (queryDataSet.getRowCount() > 0) {
          // email already exists
          EMLMCode = queryDataSet.getString("EMLMCode");
        }
        else {
          // new email
          dbRet.setRetry(1);      
          for (int retries=0; dbRet.getRetry() == 1 && retries < 35; retries++) {
            EMLMCode = SwissKnife.buildPK();
    
            query.setLength(0);
            query.append("INSERT INTO emailListMember (");
            query.append("EMLMCode,EMLMEmail,EMLMActive");
            query.append(") VALUES (");
            query.append("'"  + EMLMCode + "'");
            query.append(",'" + EMLMEmail  + "'");
            query.append(",'1'");
            query.append(")");

            dbRet = database.execQuery(query.toString());
          }
        }
      }
      catch (Exception e) {
        dbRet.setNoError(0);
      }
      finally {
        try { queryDataSet.close(); } catch (Exception e) { }
      }

      if (dbRet.getNoError() == 1) {
        try {
          query.setLength(0);
          query.append("SELECT EMLRMemberCode FROM emailListReg");
          query.append(" WHERE EMLRMemberCode = '" + EMLMCode + "'");
          query.append(" AND EMLRListCode = '" + EMLRListCode + "'");
        
          queryDataSet.setQuery(new QueryDescriptor(database, query.toString(), null, true, Load.ALL));
          queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
        
          queryDataSet.refresh();
        
          if (queryDataSet.getRowCount() > 0) {
            // email already belongs to the list, do nothing
          }
          else {
            // join email to the list
            dbRet.setRetry(1);        
            for (int retries=0; dbRet.getRetry() == 1 && retries < 35; retries++) {
              query.setLength(0);          
              query.append("INSERT INTO emailListReg (");
              query.append("EMLRCode,EMLRListCode,EMLRMemberCode");
              query.append(") VALUES (");
              query.append("'"  + SwissKnife.buildPK() + "'");
              query.append(",'" + EMLRListCode  + "'");
              query.append(",'" + EMLMCode  + "'");
              query.append(")");
        
              dbRet = database.execQuery(query.toString());
            }
          }
        }
        catch (Exception e) {
          dbRet.setNoError(0);
        }
        finally {
          try { queryDataSet.close(); } catch (Exception e) { }
        }
      }
    }
    
    database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    
    bean.freeDBConnection(databaseId, database);
    
    if (dbRet.getNoError() == 0) {
      status = Director.STATUS_ERROR;
    }
    else {
      status = Director.STATUS_OK;
    }
    
    return status;
  }
  
  /**
   * if EMLMActive is included in the request then the mechanism
   * of confirmation email is overriden.
   */
  public int doRegister(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "emailListMember", Director.AUTH_INSERT);

    if (auth < 0) {
      return auth;
    }

    DbRet dbRet = null;

    int status = Director.STATUS_OK;
    
    String query = null;
    
    String EMLMEmail = null, EMLMAltEmail = null,
           EMLMLastName = null, EMLMFirstName = null,
           EMLMCompanyName = null, EMLMAddress = null,
           EMLMZipCode = null, EMLMCity = null,
           EMLMCountry = null, EMLMPhone = null,
           EMLMField1 = null, EMLMField2 = null,
           EMLMField3 = null, EMLTCode = null,
           EMLMActive = null, EMLMCode = null,
           EMLMLastNameUp = null, EMLMCompanyNameUp = null;
    
    Timestamp EMLMBirthDate = null, EMLMRegDate = null;
    
    // parameters for email verification
    String confirm_subject = null, confirm_smtpServer = null,
           confirm_urlConfirm = null, confirm_msg = null, 
           confirm_emailFrom = null,
           confirm_content = null, confirm_charset = null;
    
    QueryDataSet queryDataSet = null;
    
    EMLMEmail = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMEmail") ) );
    EMLTCode = SwissKnife.grEncode( request.getParameter("EMLTCode") );
    
    if (EMLMEmail.trim().equals("") 
          || EMLTCode.trim().equals("")) return Director.STATUS_ERROR;
    
    EMLMEmail = EMLMEmail.toLowerCase();
    
    EMLMActive = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMActive") ) );
           
    EMLMAltEmail = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMAltEmail") ) );
    EMLMLastName = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMLastName") ) );
    EMLMFirstName = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMFirstName") ) );
    EMLMCompanyName = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMCompanyName") ) );
    EMLMAddress = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMAddress") ) );
    EMLMZipCode = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMZipCode") ) );
    EMLMCity = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMCity") ) );
    EMLMCountry = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMCountry") ) );
    EMLMPhone = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMPhone") ) );
    EMLMField1 = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMField1") ) );
    EMLMField2 = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMField2") ) );
    EMLMField3 = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLMField3") ) );

    EMLMBirthDate = SwissKnife.buildTimestamp(request.getParameter("EMLMBirthDateDay"),
                                              request.getParameter("EMLMBirthDateMonth"),
                                              request.getParameter("EMLMBirthDateYear"));
    EMLMRegDate = SwissKnife.currentDate();
    
    EMLMLastNameUp = SwissKnife.searchConvert(EMLMLastName);
    EMLMCompanyNameUp = SwissKnife.searchConvert(EMLMCompanyName);

    Database database = bean.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    
    int prevTransIsolation = dbRet.getRetInt();
    
    try {
      // check if member exists
      queryDataSet = new QueryDataSet();
      
      query = "SELECT EMLMCode,EMLMActive FROM emailListMember"
            + " WHERE EMLMEmail = '" + EMLMEmail + "'";
      queryDataSet.setQuery(new QueryDescriptor(database, query, null, true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();
        
      if (queryDataSet.getRowCount() > 0) {
        // yep! email already exists, grab member code & status
        EMLMCode = queryDataSet.getString("EMLMCode");
        EMLMActive = queryDataSet.getString("EMLMActive");
      }
      else {
        // insert new member
        if (EMLMActive.length() == 0) {
          EMLMActive = Present.STATUS_UNVERIFIED;
        }
        
        dbRet = doInsertMember(database, EMLMEmail,
                               EMLMAltEmail, EMLMLastName,
                               EMLMLastNameUp, EMLMFirstName,
                               EMLMBirthDate, EMLMRegDate,
                               EMLMCompanyName, EMLMCompanyNameUp,
                               EMLMAddress, EMLMZipCode,
                               EMLMCity, EMLMCountry,
                               EMLMPhone, EMLMActive,
                               EMLMField1, EMLMField2, EMLMField3);
        if (dbRet.getNoError() == 1) {
          EMLMCode = dbRet.getRetStr();
        }
      }
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    finally {
      if (queryDataSet != null && queryDataSet.isOpen()) queryDataSet.close();
      queryDataSet = null;
    }
    
    if (dbRet.getNoError() == 1) {
      // register member to list
      dbRet = doAddToList(EMLTCode, EMLMCode, database);
    }
    
    if (dbRet.getNoError() == 1 
          && EMLMActive.equals(Present.STATUS_UNVERIFIED)) {
      // send confirm email
      confirm_subject = SwissKnife.grEncode( request.getParameter("confirm_subject") );
      confirm_smtpServer = SwissKnife.grEncode( request.getParameter("confirm_smtpServer") );
      confirm_urlConfirm = SwissKnife.grEncode( request.getParameter("confirm_urlConfirm") );
      confirm_msg = SwissKnife.grEncode( request.getParameter("confirm_msg") );
      confirm_emailFrom = SwissKnife.grEncode( request.getParameter("confirm_emailFrom") );
      confirm_content = SwissKnife.grEncode( request.getParameter("confirm_content") );
      confirm_charset = SwissKnife.grEncode( request.getParameter("confirm_charset") );
      
      if (confirm_urlConfirm.indexOf("?") != -1) {
        confirm_urlConfirm += "&";
      }
      else {
        confirm_urlConfirm += "?";
      }
      confirm_urlConfirm += "EMLMCode=" + EMLMCode;
      
      confirm_msg += "\n\n" + confirm_urlConfirm;
      
      EMail email = new EMail(EMLMEmail, confirm_emailFrom, confirm_subject, 
                              confirm_msg, confirm_smtpServer,
                              confirm_content, confirm_charset, null);
      
      boolean sent = SendMail.sendMessage(email);
      
      if (sent == true) dbRet.setNoError(1);
      else dbRet.setNoError(0);
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    
    bean.freeDBConnection(databaseId,database);
    
    if (dbRet.getNoError() == 0) {
      status = Director.STATUS_ERROR;
    }
    else {
      status = Director.STATUS_OK;
    }

    return status;
  }
  
  private DbRet doInsertMember(Database database, String EMLMEmail,
                               String EMLMAltEmail, String EMLMLastName,
                               String EMLMLastNameUp, String EMLMFirstName,
                               Timestamp EMLMBirthDate, Timestamp EMLMRegDate,
                               String EMLMCompanyName, String EMLMCompanyNameUp,
                               String EMLMAddress, String EMLMZipCode,
                               String EMLMCity, String EMLMCountry,
                               String EMLMPhone, String EMLMActive,
                               String EMLMField1, String EMLMField2,
                               String EMLMField3) {
    DbRet dbRet = new DbRet();
    
    String query = null;
    
    String EMLMCode = null;
    
    dbRet.setRetry(1);
    for (int retries=0; dbRet.getRetry() == 1 && retries < 35; retries++) {
      EMLMCode = SwissKnife.buildPK();
      
      query = "INSERT INTO emailListMember (EMLMCode,EMLMEmail"
            + ",EMLMAltEmail,EMLMLastName,EMLMLastNameUp"
            + ",EMLMFirstName,EMLMBirthDate,EMLMRegDate"
            + ",EMLMCompanyName,EMLMCompanyNameUp,EMLMAddress"
            + ",EMLMZipCode,EMLMCity,EMLMCountry,EMLMPhone,EMLMActive"
            + ",EMLMField1,EMLMField2,EMLMField3"
            + ")"
            + " VALUES ("
            + "'"   + EMLMCode          + "'"
            + ",'"  + EMLMEmail         + "'"
            + ",'"  + EMLMAltEmail      + "'"
            + ",'"  + EMLMLastName      + "'"
            + ",'"  + EMLMLastNameUp    + "'"
            + ",'"  + EMLMFirstName     + "'";
    
      if (EMLMBirthDate != null) {
        query += ",'"  + EMLMBirthDate + "'";
      }
      else {
        query += ",null";
      }
      
      if (EMLMRegDate != null) {
        query += ",'"  + EMLMRegDate + "'";
      }
      else {
        query += ",null";
      }
    
      query += ",'"  + EMLMCompanyName   + "'"
             + ",'"  + EMLMCompanyNameUp + "'"
             + ",'"  + EMLMAddress       + "'"
             + ",'"  + EMLMZipCode       + "'"
             + ",'"  + EMLMCity          + "'"
             + ",'"  + EMLMCountry       + "'"
             + ",'"  + EMLMPhone         + "'"
             + ",'"  + EMLMActive        + "'"
             + ",'"  + EMLMField1        + "'"
             + ",'"  + EMLMField2        + "'"
             + ",'"  + EMLMField3        + "'"
             + ")";

      dbRet = database.execQuery(query);
    }
    
    // store new member code
    dbRet.setRetStr(EMLMCode);
    
    return dbRet;
  }
}