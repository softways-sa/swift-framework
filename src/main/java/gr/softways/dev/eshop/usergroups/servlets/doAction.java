package gr.softways.dev.eshop.usergroups.servlets;

import java.io.*;
import java.util.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

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
    else if (action.equals("UPDATE"))
      status = doUpdate(request,databaseId);
    else if (action.equals("UPDATE_PERM"))
      status = doUpdatePerm(request,databaseId);
    else if (action.equals("ADD_PERM"))
      status = doAddPerm(request,databaseId);
    else if (action.equals("DEL_PERM"))
      status = doDelPerm(request,databaseId);
    else if (action.equals("DELETE"))
      status = doDelete(request,databaseId);

    if (status < 0)
    {
      response.sendRedirect(urlNoAccess);
      
      //RequestDispatcher dispatcher =
        //servletContext.getRequestDispatcher(urlNoAccess);

      //request.setAttribute("authCode", String.valueOf(status) );
      //dispatcher.forward(request, response);
    }
    else if (status == Director.STATUS_OK)
      response.sendRedirect(urlSuccess);
    else
      response.sendRedirect(urlFailure);
  }

  /**
    *  Καταχώρηση νέου userGroup στο RDBMS.
    *
    * @param  request    το HttpServletRequest από την σελίδα
    * @param  databaseId το αναγνωριστικό της βάσης που
    *                    θα χρησιμοποιηθεί
    * @return            κωδικό κατάστασης
   */
  public int doInsert(HttpServletRequest request, String databaseId) {
    int prevTransIsolation = 0;

    DbRet dbRet = new DbRet();

    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "userGroups", Director.AUTH_INSERT);

    if (auth != Director.AUTH_OK)
    {
      return auth;
    }

    String userGroupId = SwissKnife.grEncode( request.getParameter("userGroupId") ).trim(),
           userGroupName = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("userGroupName") ).trim() ),
           userGroupDescr = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("userGroupDescr") ).trim() ),
           userGroupDefFlag = SwissKnife.grEncode( request.getParameter("userGroupDefFlag") ).trim(),
           userGroupGrantLogin = SwissKnife.grEncode( request.getParameter("userGroupGrantLogin") ).trim();

    if (userGroupName.equals("") || userGroupId.equals(""))
      return Director.STATUS_ERROR;

    // get database connection
    Database database = bean.getDBConnection(databaseId);

    // begin transaction
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    
    prevTransIsolation = dbRet.retInt;
    
    QueryDataSet queryDataSet1 = new QueryDataSet();

    String insertQuery = null;
    dbRet.retry = 1;
    
    if (dbRet.getNoError() == 1) {
       for (int retries = 0;dbRet.retry == 1 && retries < 10; retries++) {
          insertQuery = "INSERT INTO userGroups (" +
                        "userGroupId,userGroupName,userGroupDescr," +
                        "userGroupDefFlag,userGroupGrantLogin) " +
                        " VALUES (" +
                        "" + userGroupId    + "," +
                        "'" + userGroupName + "'," +
                        "'" + userGroupDescr + "'," +
                        "'" + userGroupDefFlag + "'," +
                        "'" + userGroupGrantLogin + "')";
          
          if (dbRet.getNoError() == 1)
             dbRet = database.execQuery(insertQuery);
       }
    }
    
    String query = "SELECT * FROM securityObjects";
    
    if (dbRet.getNoError() == 1) {
      try {
        queryDataSet1.setQuery(new QueryDescriptor(database, query, null, true, Load.ALL));
        queryDataSet1.setMetaDataUpdate(MetaDataUpdate.NONE);

        queryDataSet1.refresh();

        int rc = queryDataSet1.getRowCount();
        for (int i=0; dbRet.getNoError() == 1 && i < rc; i++) {
          
          insertQuery = "INSERT INTO securityPolicy (" +
                        "SPId,SPObject,SPPermissions) " +
                        " VALUES (" +
                        "" + userGroupId    + "," +
                        "'" + queryDataSet1.getString("SOObjectName") + "'," +
                        "" + queryDataSet1.getInt("SODefPerm") + ")";
          
          if (dbRet.getNoError() == 1)
            dbRet = database.execQuery(insertQuery);
          
          queryDataSet1.next();
        }
      }
      catch (DataSetException dse) {
        dse.printStackTrace();
        dbRet.setNoError(0);
      }
      try {
        queryDataSet1.close();
      }
      catch (DataSetException dse) {
        dse.printStackTrace();
        dbRet.setNoError(0);
      }
    }
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    if (dbRet.getNoError() == 1)
      return Director.STATUS_OK;
    else
      return Director.STATUS_ERROR;
  }


  /**
    *  Ανεύρεση του μέγιστου Id και αύξηση του κατα ένα (1).
    *
    * @return τον καινούργιο κωδικό για την εγγραφή
   */
  /*private int getNewUserGroupId(String databaseId) {
    Database database = null;
    QueryDataSet queryDataSet1 = new QueryDataSet();

    String query = "select max(userGroupId) from userGroups";

    int maxUserGroupId = 0;

    database = bean.poolManager.getDBConnection(databaseId);

    try {
      queryDataSet1.setQuery(new com.borland.dx.sql.dataset.QueryDescriptor(database, query, null, true, Load.ALL));
      queryDataSet1.refresh();

      maxUserGroupId = queryDataSet1.getInt(0);

      maxUserGroupId++;

      queryDataSet1.close();
    }
    catch (DataSetException e) {
      e.printStackTrace();
    }

    bean.poolManager.freeDBConnection(databaseId,database);
    return maxUserGroupId;
  }*/

  /**
    *  Μεταβολή στο RDBMS.
    *
    * @param  request    το HttpServletRequest από την σελίδα
    * @param  databaseId το αναγνωριστικό της βάσης που
    *                    θα χρησιμοποιηθεί
    * @return            κωδικό κατάστασης
   */
  public int doUpdate(HttpServletRequest request, String databaseId) {
    DbRet dbRet = new DbRet();
    
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "userGroups", Director.AUTH_UPDATE);

    if (auth != Director.AUTH_OK)
    {
      return auth;
    }

    String userGroupId = SwissKnife.grEncode( request.getParameter("userGroupId") ).trim(),
           userGroupName = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("userGroupName") ).trim() ),
           userGroupDescr = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("userGroupDescr") ).trim() ),
           userGroupDefFlag = SwissKnife.grEncode( request.getParameter("userGroupDefFlag") ).trim(),
           userGroupGrantLogin = SwissKnife.grEncode( request.getParameter("userGroupGrantLogin") ).trim();

    if (userGroupName.equals(""))
      return Director.STATUS_ERROR;
    
    // get database connection
    Database database = bean.getDBConnection(databaseId);
    
    String updateQuery
             = "UPDATE userGroups SET " +
               "userGroupName = '"  + userGroupName   +  "'," +
               "userGroupDescr = '"  + userGroupDescr +  "'," +
               "userGroupDefFlag = '"  + userGroupDefFlag +  "'," +
               "userGroupGrantLogin = '"  + userGroupGrantLogin +  "'" +
               " WHERE userGroupId = " + userGroupId;

    //System.out.println(updateQuery);

    dbRet = database.execQuery(updateQuery);
    
    bean.freeDBConnection(databaseId,database);
    
    if (dbRet.getNoError() == 1)
      return Director.STATUS_OK;
    else
      return Director.STATUS_ERROR;
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
  public int doUpdatePerm(HttpServletRequest request, String databaseId) {
    DbRet dbRet = new DbRet();
    
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                 "securityPolicy", Director.AUTH_UPDATE);

    if (auth != Director.AUTH_OK)
    {
      return auth;
    }

    String userGroupId = SwissKnife.grEncode( request.getParameter("userGroupId") ).trim(),
           SPObject = "";

    String updated = "";

    int rowCounter = Integer.parseInt( SwissKnife.grEncode( request.getParameter("rowCounter") ).trim() );

    int authRead = 0,
        authInsert = 0,
        authUpdate = 0,
        authDelete = 0;

    int SPPermissions = 0;

    String updateQuery = "";

    for (int i=0; i<=rowCounter; i++) {
      updated = request.getParameter("updated"+i);

      if (updated.equals("1")) {
        SPObject = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("SPObject"+i) ).trim() );

        authRead = request.getParameter("authRead"+i) == null ? 0 : Integer.parseInt( request.getParameter("authRead"+i) );
        authInsert = request.getParameter("authInsert"+i) == null ? 0 : Integer.parseInt( request.getParameter("authInsert"+i) );
        authUpdate = request.getParameter("authUpdate"+i) == null ? 0 : Integer.parseInt( request.getParameter("authUpdate"+i) );
        authDelete = request.getParameter("authDelete"+i) == null ? 0 : Integer.parseInt( request.getParameter("authDelete"+i) );

        SPPermissions = ( authRead | authInsert | authUpdate | authDelete );
        
        // get database connection
        Database database = bean.getDBConnection(databaseId);

        updateQuery = "UPDATE securityPolicy SET " +
                      "SPPermissions = "  + SPPermissions +  "" +
                      " WHERE SPId = " + userGroupId +
                      " AND SPObject = '" + SPObject + "'";


        dbRet = database.execQuery(updateQuery);
        
        bean.freeDBConnection(databaseId,database);
      }
    }
    
    if (dbRet.getNoError() == 1)
      return Director.STATUS_OK;
    else
      return Director.STATUS_ERROR;
  }

  public int doAddPerm(HttpServletRequest request, String databaseId)
  {
    int status = Director.STATUS_OK;

    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "securityPolicy", Director.AUTH_INSERT);

    if (auth != Director.AUTH_OK)
    {
      return auth;
    }

    int rc = 0;

    String query = null;

    // get database connection
    Database database = bean.getDBConnection(databaseId);

    String userGroupId = SwissKnife.grEncode( request.getParameter("userGroupId") ).trim(),
           SPObject = "";

    String updated = "";

    int rowCounter = Integer.parseInt( SwissKnife.grEncode( request.getParameter("rowCounter2") ).trim() );

    int authRead = 0,
        authInsert = 0,
        authUpdate = 0,
        authDelete = 0;

    int SPPermissions = 0;

    String updateQuery = "";

    for (int i=0; i<=rowCounter; i++) {
      updated = request.getParameter("SOUpdated"+i);

      if (updated.equals("1"))
      {
        SPObject = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("SOObjectName"+i) ).trim() );
        authRead = request.getParameter("SOAuthRead"+i) == null ? 0 : Integer.parseInt( request.getParameter("SOAuthRead"+i) );
        authInsert = request.getParameter("SOAuthInsert"+i) == null ? 0 : Integer.parseInt( request.getParameter("SOAuthInsert"+i) );
        authUpdate = request.getParameter("SOAuthUpdate"+i) == null ? 0 : Integer.parseInt( request.getParameter("SOAuthUpdate"+i) );
        authDelete = request.getParameter("SOAuthDelete"+i) == null ? 0 : Integer.parseInt( request.getParameter("SOAuthDelete"+i) );
        SPPermissions = ( authRead | authInsert | authUpdate | authDelete );
        
        try {
           updateQuery = "INSERT INTO securityPolicy ("   +
                         "SPId,SPObject,SPPermissions) "  +
                         " VALUES (" + userGroupId   + ","  +
                         "'"         + SPObject      + "'," +
                                     + SPPermissions + ")";
        }
        catch(Exception e) {
           status = Director.STATUS_ERROR;
           e.printStackTrace();
        }
        try {
           database.executeStatement(updateQuery);
        }
        catch (Exception e) {
           status = Director.STATUS_ERROR;
           e.printStackTrace();
        }

        bean.freeDBConnection(databaseId,database);
      }
    }

    return Director.STATUS_OK;
  }


  public int doDelPerm(HttpServletRequest request, String databaseId) {
    int status = Director.STATUS_OK;
    
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "securityPolicy", Director.AUTH_DELETE);

    if (auth != Director.AUTH_OK)
    {
      return auth;
    }

    int rc = 0;
    
    // get database connection
    Database database = bean.getDBConnection(databaseId);
    
    String userGroupId = SwissKnife.grEncode( request.getParameter("userGroupId") ).trim(),
           SPObject = "";

    int delRow = Integer.parseInt( SwissKnife.grEncode( request.getParameter("delRow") ).trim() );
    String updateQuery = "";
    SPObject = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("SPObject"+delRow) ).trim() );
    
    updateQuery = "DELETE FROM securityPolicy WHERE " +
                  "SPId = " + userGroupId + " AND SPObject = '" +
                   SPObject + "'";
    try {
       database.executeStatement(updateQuery);
    }
    catch (Exception e) {
       status = Director.STATUS_ERROR;
       e.printStackTrace();
    }
    bean.freeDBConnection(databaseId,database);
    return Director.STATUS_OK;
  }


  /**
    *  Διαγραφή userGroups + securityPolicy στο RDBMS.
    *
    * @param  request    το HttpServletRequest από την σελίδα
    * @param  databaseId το αναγνωριστικό της βάσης που
    *                    θα χρησιμοποιηθεί
    * @return            κωδικό κατάστασης
   */
  public int doDelete(HttpServletRequest request, String databaseId) {

    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                 "userGroups", Director.AUTH_DELETE);

    if (auth != Director.AUTH_OK)
    {
      return auth;
    }

    DbRet dbRet = null;
    int prevTransIsolation = 0;
    
    String userGroupId = SwissKnife.grEncode( request.getParameter("userGroupId") ).trim();

// get database connection
    Database database = bean.getDBConnection(databaseId);
// begin transaction
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    prevTransIsolation = dbRet.retInt;
    
    String deleteQuery = "DELETE FROM " +
                         " securityPolicy WHERE " +
                         " SPId = " + userGroupId;
    
    if (dbRet.getNoError() == 1)
       dbRet = database.execQuery(deleteQuery);

    deleteQuery = "DELETE FROM " +
                  " userGroups WHERE " +
                  " userGroupId = " + userGroupId;
    
    if (dbRet.getNoError() == 1)
       dbRet = database.execQuery(deleteQuery);
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    
    bean.freeDBConnection(databaseId,database);
    
    if (dbRet.getNoError() == 1)
      return Director.STATUS_OK;
    else
      return Director.STATUS_ERROR;
  }

}
