package gr.softways.dev.poolmanager;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;

import gr.softways.dev.util.LogWriter;

public class AppACL implements ACLInterface {

  private String name;

  // Αντικείμενο για την αποθήκευση των user
  // που κάνουν login
  private Hashtable users = new Hashtable();

  // Αντικείμενο για την αποθήκευση των userGroups
  // και των permission για το κάθε database object
  private Hashtable userGroups = new Hashtable();

  private PrintWriter pw;
  private LogWriter logWriter;

  private AppPoolManager appPoolManager;

  public AppACL(String name, AppPoolManager appPoolManager, int logLevel) {
    this.name = name;
    this.appPoolManager = appPoolManager;

    //appPoolManager = AppPoolManager.getInstance();
    try {      
      pw = new PrintWriter(new FileWriter("swlogs/" + name + "_logs/" + name + "ACL.log", false), true);
    }
    catch (Exception ioe) {
      ioe.printStackTrace();
    }

    logWriter = new LogWriter(name, logLevel, pw);
    logWriter.log("ACL starting for databaseId " + name, LogWriter.INFO);

    init();
  }

  private void init() {
    Database database = null;

    QueryDataSet queryDataSet1 = new QueryDataSet(),
                 queryDataSet2 = new QueryDataSet();

    database = appPoolManager.getDBConnection(name);


    String groupsQuery = "SELECT * FROM userGroups";
    
    try {
      queryDataSet1.setQuery(new QueryDescriptor(database, groupsQuery, null, 
                                                 true, Load.ALL));
      queryDataSet1.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      queryDataSet1.refresh();

      String SPQuery = "";

      int rc1 = queryDataSet1.getRowCount();
      
      for (int i=0; i<rc1; i++) {

        SPQuery = "SELECT * FROM securityPolicy WHERE SPId = " +
                  queryDataSet1.getInt("userGroupId");
        
        // get securityPolicy for specified userGroup
        queryDataSet2.setQuery(new QueryDescriptor(database, SPQuery, null, true, Load.ALL));
        queryDataSet2.setMetaDataUpdate(MetaDataUpdate.NONE);

        queryDataSet2.refresh();

        int rc2 = queryDataSet2.getRowCount();

        for (int x=0; x<rc2; x++) {
          GroupPerm groupPerm = new GroupPerm(queryDataSet1.format("userGroupId"),
                                              queryDataSet2.getString("SPObject"),
                                              queryDataSet2.getInt("SPPermissions"));

          userGroups.put(queryDataSet2.format("SPId")+"."+queryDataSet2.getString("SPObject").trim(), groupPerm);
          
          queryDataSet2.next();
        }

        queryDataSet2.close();

        queryDataSet1.next();
      }

      queryDataSet1.close();
    }
    catch (Exception dse) {
      logWriter.log(dse,"Problem creating ACL", LogWriter.ERROR);
    }
    
    appPoolManager.freeDBConnection(name,database);
  }

  /**
   * Προσθήκη authenticated user στο hashtable των users
   */
  synchronized public boolean addAuthUser(String keyName, AuthEmp authEmp) {
    AuthEmp oldAuthEmp = null;

    boolean userExists = users.containsKey(authEmp.getUsername());

    if (userExists) {
      oldAuthEmp = (AuthEmp)users.get(authEmp.getUsername());
      oldAuthEmp.increaseUserCounter();
      authEmp.setUserCounter(oldAuthEmp.getUserCounter());
    }

    users.put(authEmp.getUsername(), authEmp);

    logWriter.log("User " + authEmp.getUsername() + " logged in(" +
                  authEmp.getUserCounter() + ")", LogWriter.INFO);

    return userExists;
  }

  public AuthEmp getAuthUser(String userName) {
    AuthEmp authEmp = (AuthEmp)users.get(userName);
    return authEmp;
  }

  /**
   * Αφαίρεση user από το hashtable των users
   */
  synchronized public void removeAuthUser(String keyName, AuthEmp authEmp) {
    boolean userExists = users.containsKey(authEmp.getUsername());

    if (userExists) {
      authEmp = (AuthEmp)users.get(authEmp.getUsername());
      authEmp.decreaseUserCounter();
    }

    if (authEmp.getUserCounter() > 0)
      users.put(authEmp.getUsername(), authEmp);
    else
      users.remove(authEmp.getUsername());

    logWriter.log("User " + authEmp.getUsername() + " logged out("
                + authEmp.getUserCounter() + ")", LogWriter.INFO);
  }

  /**
    *  Authentication για την εκτέλεση μιας μεθόδου.
   */
  public int auth(String username, String password,
                  String tableName, int permission,
                  String databaseId) {

    int authStatus = AUTH_NOACCESS;

    AuthEmp authEmp;
    GroupPerm groupPerm;

    if (!users.containsKey(username)) {
      tryToAuthUser(username, password, databaseId);
    }

    if (users.containsKey(username)) {
      authEmp = (AuthEmp) users.get(username);
      if ( ( !password.equals( authEmp.getPassword() ) ) )
      {
        authStatus = AUTH_PASSWORD_MISMATCH;
        logWriter.log("User " + username + " failed to authenticate for table " +
               tableName + " due to bad password", LogWriter.DEBUG);
      }
      else
      {
        // username & password valid
        // check permissions for the object
        if ( userGroups.containsKey(authEmp.getAccessLevel()+"."+tableName) )
        {
           groupPerm = (GroupPerm) userGroups.get(authEmp.getAccessLevel()+"."+tableName);
           if ( (groupPerm != null) && (groupPerm.getSPPerm() & permission) > 0 )
           {
             authStatus = AUTH_OK;
             logWriter.log("User " + username + " authenticated for table " +
               tableName, LogWriter.DEBUG);
           }
           else
           {
             authStatus = AUTH_NOACCESS;
             logWriter.log("User " + username + " failed to authenticate for table " +
               tableName + " due to lower permissions", LogWriter.DEBUG);
           }
        }
        else
        {
          logWriter.log("Table " + tableName + " does not exist in hashtable.",
            LogWriter.DEBUG);
        }
      }
    }
    else
    {
      authStatus = AUTH_NOUSER;
      logWriter.log("User " + username + " failed to authenticate for table " +
               tableName +
               " cause I couldn't find him in authenticated users", LogWriter.DEBUG);
    }

    return authStatus;
  }

 /* override auth- passing session */
  public int auth(String username, String password,
                  String tableName, int permission,
                  String databaseId, HttpSession session) {

    int authStatus = AUTH_NOACCESS;

    AuthEmp authEmp;
    GroupPerm groupPerm;

    if (!users.containsKey(username)) {
      tryToAuthUser(username, password, databaseId, session);
    }

    if (users.containsKey(username)) {
      authEmp = (AuthEmp) users.get(username);
      if ( ( !password.equals( authEmp.getPassword() ) ) ) {
        authStatus = AUTH_PASSWORD_MISMATCH;
        logWriter.log("User " + username + " failed to authenticate for table " +
               tableName + " due to bad password", LogWriter.DEBUG);
      }
      else {
        // username & password valid
        // check permissions for the object
        if ( userGroups.containsKey(authEmp.getAccessLevel()+"."+tableName) ) {
           groupPerm = (GroupPerm) userGroups.get(authEmp.getAccessLevel()+"."+tableName);
           if ( (groupPerm != null) && (groupPerm.getSPPerm() & permission) > 0 ) {
             authStatus = AUTH_OK;
             logWriter.log("User " + username + " authenticated for table " +
               tableName, LogWriter.DEBUG);
           }
           else {
             authStatus = AUTH_NOACCESS;
             logWriter.log("User " + username + " failed to authenticate for table " +
               tableName + " due to lower permissions", LogWriter.DEBUG);
           }
        }
        else {
          logWriter.log("Table " + tableName + " does not exist in hashtable.",
            LogWriter.DEBUG);
        }
      }
    }
    else
    {
      authStatus = AUTH_NOUSER;
      logWriter.log("User " + username + " failed to authenticate for table " +
               tableName +
               " cause I couldn't find him in authenticated users", LogWriter.DEBUG);
    }

    return authStatus;
  }




  /**
    * ΠΡΟΣΟΧΗ :  Προσπάθεια να κάνει validation του username χωρίς έλεγχο για το
    *           αν το userGroup έχει permission to login.
   */
  private int tryToAuthUser(String username, String password, String databaseId) {
    Database database = null;
    QueryDataSet queryDataSet = new QueryDataSet();

    String query = "SELECT * FROM users WHERE " +
      "usrName = '" + username + "' AND " +
      "usrPasswd = '" + password + "'";

    int status = AUTH_NOUSER;

    logWriter.log("Could not find user " + username + " in users authenticated "
                + "hashtable. I'll try to validate him against RDBMS...", LogWriter.ERROR);

    database = appPoolManager.getDBConnection(databaseId);

    try {
      queryDataSet.setQuery(new QueryDescriptor(database, query, null, true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();

      if (queryDataSet.getRowCount() >= 1) {
        // username & password pair valid
        AuthEmp authEmp = new AuthEmp(username,
                                      password,
                                      queryDataSet.getInt("usrAccessLevel"),
                                      databaseId);

        // πρόσθεσε στο hashtable τον χρήστη
        addAuthUser(username,authEmp);

        status = AUTH_OK;
      }

      queryDataSet.close();
    }
    catch (DataSetException dse) {
      logWriter.log(dse,"Problem trying to validate user in RDBMS", LogWriter.ERROR);
      try {
        queryDataSet.close();
      }
      catch (DataSetException dsex) { }
    }

    appPoolManager.freeDBConnection(databaseId, database);
    return status;
  }

  /**
    * override tryToAuthUser - passing session
    * ΠΡΟΣΟΧΗ :  Προσπάθεια να κάνει validation του username χωρίς έλεγχο για το
    *           αν το userGroup έχει permission to login.
   */
  private int tryToAuthUser(String username, String password, String databaseId,
                            HttpSession session) {
    Database database = null;
    QueryDataSet queryDataSet = new QueryDataSet();

    String query = "SELECT * FROM users WHERE " +
      "usrName = '" + username + "' AND " +
      "usrPasswd = '" + password + "'";

    int status = AUTH_NOUSER;

    logWriter.log("Could not find user " + username + " in users authenticated " +
      "hashtable. I'll try to validate him against RDBMS...", LogWriter.ERROR);

    database = appPoolManager.getDBConnection(databaseId);

    try {
      queryDataSet.setQuery(new QueryDescriptor(database, query, null, true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();

      if (queryDataSet.getRowCount() >= 1) {
        // username & password pair valid
        AuthEmp authEmp = new AuthEmp(username,
                                      password,
                                      queryDataSet.getInt("usrAccessLevel"),
                                      databaseId);

        // πρόσθεσε στο hashtable τον χρήστη
        addAuthUser(username,authEmp);
        session.setAttribute(databaseId + ".unbindObject", authEmp);

        status = AUTH_OK;
      }

      queryDataSet.close();
    }
    catch (DataSetException dse) {
      logWriter.log(dse,"Problem trying to validate user in RDBMS", LogWriter.ERROR);
      try {
        queryDataSet.close();
      }
      catch (DataSetException dsex) { }
    }

    appPoolManager.freeDBConnection(databaseId, database);
    return status;
  }
}