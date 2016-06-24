package gr.softways.dev.eshop.securityobjects.servlets;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.sql.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class doAction extends HttpServlet {

  private Director bean;

  private String _charset = null;
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
      
    bean = Director.getInstance();
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException, IOException {
        
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1"),
           databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId").trim(),
           urlSuccess = request.getParameter("urlSuccess") == null ? "" : request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");

    int status = 0;

    if (action.equals("INSERT"))
      status = doInsert(request, databaseId);
    else if (action.equals("UPDATE"))
      status = doUpdate(request,databaseId);
    else if (action.equals("DELETE"))
      status = doDelete(request,databaseId);
    else if (action.equals("UPDATE_BATCH_PERM"))
      status = doUpdateBatch(request,databaseId);

    if (status < 0) {
      response.sendRedirect(urlNoAccess);
    }
    else if (status == Director.AUTH_OK) {
      response.sendRedirect(urlSuccess);
    }
    else {
      response.sendRedirect(urlFailure);
    }
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
                         "securityObjects", Director.AUTH_INSERT);

    if (auth != Director.AUTH_OK) {
      return auth;
    }
    
    String SOObjectName = SwissKnife.sqlEncode(  request.getParameter("SOObjectName") ).trim(),
           SODescr = SwissKnife.sqlEncode( request.getParameter("SODescr") ).trim();
    
    boolean addToUserGroups = SwissKnife.sqlEncode( request.getParameter("addToUserGroups") ).equals("") ? false : true;

    if (addToUserGroups == true) {
      auth = bean.auth(databaseId, authUsername, authPassword,
                       "securityPolicy", Director.AUTH_INSERT);

      if (auth != Director.AUTH_OK) {
        return auth;
      }
    }

    int authRead = request.getParameter("authRead") == null ? 0 : Integer.parseInt( request.getParameter("authRead") ),
        authInsert = request.getParameter("authInsert") == null ? 0 : Integer.parseInt( request.getParameter("authInsert") ),
        authUpdate = request.getParameter("authUpdate") == null ? 0 : Integer.parseInt( request.getParameter("authUpdate") ),
        authDelete = request.getParameter("authDelete") == null ? 0 : Integer.parseInt( request.getParameter("authDelete") );

    int SODefPerm = 0;
    SODefPerm = ( authRead | authInsert | authUpdate | authDelete);

    int SOId = 0;
    SOId = getNewSOId(databaseId);

    int status = Director.STATUS_ERROR;

    if (SOObjectName.equals("")) return Director.STATUS_ERROR;
    
    DbRet dbRet = new DbRet();

    Database database = bean.getDBConnection(databaseId);

    dbRet = database.beginTransaction(Database.TRANS_ISO_1);

    int prevTransIsolation = dbRet.getRetInt();

    if (dbRet.getNoError() == 1) {
      String insertQuery
                 = "INSERT INTO securityObjects (" +
                   "SOId,SOObjectName,SODescr,SODefPerm) " +
                   " VALUES (" +
                   "" + SOId    + "," +
                   "'" + SOObjectName + "'," +
                   "'" + SODescr + "'," +
                   "" + SODefPerm    + ")";

      dbRet = database.execQuery(insertQuery);
    }

    if (dbRet.getNoError() == 1 && addToUserGroups == true) {
      dbRet = addToUserGroups(database, SOObjectName, SODefPerm);
    }

    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    if (dbRet.getNoError() == 1)
      status = Director.STATUS_OK;

    bean.freeDBConnection(databaseId,database);
    
    return status;
  }


  /**
   * Προσθήκη νέου security object στα υπάρχουσα userGroup
   */
  private DbRet addToUserGroups(Database database, String SOObjectName,
                                int SODefPerm) {
    QueryDataSet userGroupDataSet = new QueryDataSet();

    String query = "SELECT * FROM userGroups";

    DbRet dbRet = new DbRet();

    String SPQuery = "";

    try {
      userGroupDataSet.setQuery(new QueryDescriptor(database, query, 
                                                    null, true, Load.ALL));
      userGroupDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      userGroupDataSet.refresh();

      int userGroupRowCount = userGroupDataSet.getRowCount();

      for (int i=0; i<userGroupRowCount; i++) {
        SPQuery = "INSERT INTO securityPolicy (" +
                  "SPId,SPObject,SPPermissions) " +
                  " VALUES (" +
                  "" + userGroupDataSet.getInt("userGroupId") + "," +
                  "'" + SOObjectName + "'," +
                  "" + SODefPerm + ")";

        dbRet = database.execQuery(SPQuery);

        if (dbRet.getNoError() == 0) {
          break;
        }

        userGroupDataSet.next();
      }

      userGroupDataSet.close();
    }
    catch (Exception dse) {
      dbRet.setNoError(0);
      dse.printStackTrace();
    }

    return dbRet;
  }

  /**
   * Μεταβολή στα security object στα υπάρχουσα userGroup
   *   !!! ΧΩΡΙΣ τα DEFAULT PERMISSIONS !!!
   */
  private DbRet deleteFromUserGroups(Database database, String SOObjectName) {
    String query = "DELETE FROM securityPolicy WHERE " 
                 + "SPObject = '" + SOObjectName + "'";

    DbRet dbRet = new DbRet();

    dbRet = database.execQuery(query);

    return dbRet;
  }

  /**
    *  Ανεύρεση του μέγιστου modId και αύξηση του κατα ένα (1).
    *
    * @return τον καινούργιο κωδικό για την εγγραφή
   */
  private int getNewSOId(String databaseId) {
    Database database = null;
    QueryDataSet queryDataSet1 = new QueryDataSet();

    String query = "select max(SOId) from securityObjects";

    int maxSOId = 0;

    database = bean.getDBConnection(databaseId);

    DbRet dbRet = null;

    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();

    try {
      queryDataSet1.setQuery(new QueryDescriptor(database, query, 
                                                 null, true, Load.ALL));
      queryDataSet1.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      queryDataSet1.refresh();

      maxSOId = Integer.parseInt( queryDataSet1.format(0) );

      maxSOId++;

      queryDataSet1.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    database.commitTransaction(1,prevTransIsolation);
    
    bean.freeDBConnection(databaseId,database);
    
    return maxSOId;
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
           authPassword = SwissKnife.getSessionAttr(databaseId +  ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "securityObjects", Director.AUTH_UPDATE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    int status = Director.STATUS_OK;

    String SOObjectName = SwissKnife.sqlEncode( request.getParameter("SOObjectName") ).trim(),
           SODescr = SwissKnife.sqlEncode( request.getParameter("SODescr") ).trim(),
           SOId =  SwissKnife.sqlEncode( request.getParameter("SOId") ).trim();

    int authRead = request.getParameter("authRead") == null ? 0 : Integer.parseInt( request.getParameter("authRead") ),
        authInsert = request.getParameter("authInsert") == null ? 0 : Integer.parseInt( request.getParameter("authInsert") ),
        authUpdate = request.getParameter("authUpdate") == null ? 0 : Integer.parseInt( request.getParameter("authUpdate") ),
        authDelete = request.getParameter("authDelete") == null ? 0 : Integer.parseInt( request.getParameter("authDelete") );

    int SODefPerm = 0;
    SODefPerm = ( authRead | authInsert | authUpdate | authDelete);

    if (SOObjectName.equals("")) return Director.STATUS_ERROR;
    
    Database database = bean.getDBConnection(databaseId);
    
    String updateQuery
                 = "UPDATE securityObjects SET " +
                   "SOObjectName = '"  + SOObjectName      +  "'," +
                   "SODescr = '"  + SODescr      +  "'," +
                   "SODefPerm = "  + SODefPerm + "" +
                   " WHERE SOId = " + SOId;
     
    try {
      database.executeStatement(updateQuery);
    }
    catch (Exception dse) {
      status = Director.STATUS_ERROR;
      dse.printStackTrace();
    }

    bean.freeDBConnection(databaseId,database);
    
    return status;
  }

  /**
    *  Διαγραφή στο RDBMS.
    *
    * @param  request    το HttpServletRequest από την σελίδα
    * @param  databaseId το αναγνωριστικό της βάσης που
    *                    θα χρησιμοποιηθεί
    * @return            κωδικό κατάστασης
   */
  public int doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId +  ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "securityObjects", Director.AUTH_DELETE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    boolean addToUserGroups = SwissKnife.sqlEncode( request.getParameter("addToUserGroups") ).equals("") ? false : true;

    if (addToUserGroups == true) {
      auth = bean.auth(databaseId, authUsername, authPassword,
                       "securityPolicy", Director.AUTH_DELETE);

      if (auth != Director.AUTH_OK) {
        return auth;
      }
    }

    int status = Director.STATUS_OK;

    DbRet dbRet = new DbRet();

    Database database = bean.getDBConnection(databaseId);

    dbRet = database.beginTransaction(Database.TRANS_ISO_1);

    int prevTransIsolation = dbRet.getRetInt();

    String SOId = request.getParameter("SOId"),
           SOObjectName = SwissKnife.sqlEncode( request.getParameter("SOObjectName") ).trim();

     if (dbRet.getNoError() == 1 && addToUserGroups == true) {
      dbRet = deleteFromUserGroups(database, SOObjectName);
    }

    if (dbRet.getNoError() == 1) {
      String deleteQuery
                 = "DELETE FROM " +
                   " securityObjects WHERE " +
                   " SOId = " + SOId;

      dbRet = database.execQuery(deleteQuery);
    }

    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    if (dbRet.getNoError() == 1) status = Director.STATUS_OK;

    bean.freeDBConnection(databaseId,database);
    
    return status;
  }

  /**
   *  Μεταβολή PERMISSIONS στο RDBMS.
   * Προσοχή! Επιστρέφει πάντα STATUS_OK !!!
   *
   * @param  request    το HttpServletRequest από την σελίδα
   * @param  databaseId το αναγνωριστικό της βάσης που
   *                    θα χρησιμοποιηθεί
   * @return            κωδικό κατάστασης
   */
  public int doUpdateBatch(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "securityObjects", Director.AUTH_UPDATE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String SOId = "", SOObjectName = "", updated = "";

    int rowCounter = Integer.parseInt( SwissKnife.sqlEncode( request.getParameter("rowCounter") ).trim() );

    int authRead = 0,
        authInsert = 0,
        authUpdate = 0,
        authDelete = 0;

    int SODefPerm = 0;

    String updateQuery = "";

    Database database = null;
    database = bean.getDBConnection(databaseId);
    
    for (int i=0; i<=rowCounter; i++) {
      updated = request.getParameter("updated"+i);

      if (updated.equals("1")) {
        SOId = SwissKnife.sqlEncode( request.getParameter("SOId"+i) ).trim();
        SOObjectName = SwissKnife.sqlEncode( request.getParameter("SOObjectName"+i) ).trim();

        authRead = request.getParameter("authRead"+i) == null ? 0 : Integer.parseInt( request.getParameter("authRead"+i) );
        authInsert = request.getParameter("authInsert"+i) == null ? 0 : Integer.parseInt( request.getParameter("authInsert"+i) );
        authUpdate = request.getParameter("authUpdate"+i) == null ? 0 : Integer.parseInt( request.getParameter("authUpdate"+i) );
        authDelete = request.getParameter("authDelete"+i) == null ? 0 : Integer.parseInt( request.getParameter("authDelete"+i) );

        SODefPerm = ( authRead | authInsert | authUpdate | authDelete );

        updateQuery = "UPDATE securityObjects SET " +
                      "SODefPerm = "  + SODefPerm +  "" +
                      " WHERE SOId = " + SOId;

        //System.out.println(updateQuery);
        database.execQuery(updateQuery);
      }
    }
    
    bean.freeDBConnection(databaseId,database);

    return bean.STATUS_OK;
  }
}