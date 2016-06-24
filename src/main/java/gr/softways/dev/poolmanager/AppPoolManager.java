package gr.softways.dev.poolmanager;

import java.sql.*;
import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;

import gr.softways.dev.util.SwissKnife;
import gr.softways.dev.util.LogWriter;

public class AppPoolManager implements ACLInterface {

  static private AppPoolManager instance;
  static private int clients;

  // το όνομα του configuration αρχείο
  //static private String PROPS_NAME = "poolmanager.cfg";
  static private String PROPS_NAME = "";
  
  private LogWriter logWriter;
  private PrintWriter pw;

  //private Vector drivers = new Vector();
  
  // Database connections pool
  private Hashtable connPool = new Hashtable();

  // Access Control List pool
  private Hashtable ACLPool = new Hashtable();

  // διάφορες πληροφορίες που αφορούν κάποιο pool
  private Hashtable etc = new Hashtable();

  private AppPoolManager() {
    init();
  }

  // get init filename from parameter
  static synchronized public AppPoolManager getInstance(String propsName) {
    PROPS_NAME = propsName;

    System.out.println("[" + new java.util.Date() + "] AppPoolManager getInstance() : PROPS_NAME = " + PROPS_NAME); 

    return getInstance();
  }

  static synchronized public AppPoolManager getInstance() {
    if (instance == null && PROPS_NAME.equals("")) {
      System.out.println("[" + new java.util.Date() + "] AppPoolManager : PROPS_NAME is empty, trying jndi lookup for poolmanager/propsName."); 

      PROPS_NAME = SwissKnife.jndiLookup("poolmanager/propsName");
      
      if (PROPS_NAME == null || PROPS_NAME.equals("")) {
        System.err.println("[" + new java.util.Date() + "] AppPoolManager : jndi lookup for poolmanager/propsName failed."); 
      }
    }
    
    if (instance == null) {
      instance = new AppPoolManager();
    }
    clients++;
    return instance;
  }

  static synchronized public void main(String[] args) {
    if (instance == null) {
      instance = new AppPoolManager();
    }
  }

  private void init() {
      // Log to System.err until we have read the logfile property
      pw = new PrintWriter(System.err, true);
      logWriter = new LogWriter("AppPoolManager", LogWriter.INFO, pw);

      Properties dbProps = new Properties();
      try {
        InputStream is = new FileInputStream(PROPS_NAME);
        dbProps.load(is);
      }
      catch (Exception e) {
        logWriter.log("Can't read the properties file. (" + PROPS_NAME + ")",
                      LogWriter.ERROR);
        return;
      }
      
      String logFile = dbProps.getProperty("logfile");
      if (logFile != null) {
        try {
            pw = new PrintWriter(new FileWriter(logFile, true), true);
            logWriter.setPrintWriter(pw);
        }
        catch (IOException e) {
            logWriter.log("Can't open the log file: " + logFile + ". Using System.err instead",
                          LogWriter.ERROR);
        }
      }
      
      loadDrivers(dbProps);
      createPools(dbProps);
      dbProps.clear();
   }

  private void loadDrivers(Properties props) {
    StringTokenizer driverNames = new StringTokenizer(props.getProperty("drivers"));

    String driverClassName = null;
    
    Enumeration drivers = null;
    
    while (driverNames.hasMoreElements()) {
      driverClassName = driverNames.nextToken().trim();
      
      try {
        boolean match = false;
        drivers = DriverManager.getDrivers();
        
        while (drivers.hasMoreElements() == true && match == false) {
          if (drivers.nextElement().getClass().getName().equals(driverClassName)) match = true;
        }
        
        if (match == false) {
          DriverManager.registerDriver((Driver)Class.forName(driverClassName).newInstance());
          logWriter.log("Registered JDBC driver " + driverClassName, LogWriter.INFO);
        }
      }
      catch (Exception e) {
        logWriter.log(e, "Can't register JDBC driver: " + driverClassName, LogWriter.ERROR);
      }
    }
    
    if (props.getProperty("jdbcLogging") != null && "true".equals(props.getProperty("jdbcLogging")) && DriverManager.getLogWriter() == null) {
      
      try {
        DriverManager.setLogWriter(new PrintWriter(new FileWriter("swlogs/jdbc.log",false), true));
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      
    }
  }

   private void createPools(Properties props) {
      Enumeration propNames = props.propertyNames();
      
      while (propNames.hasMoreElements()) {
         String name = (String) propNames.nextElement();
         
         if (name.endsWith(".url")) {
            String poolName = name.substring(0, name.lastIndexOf("."));
            String url = props.getProperty(poolName + ".url");
            if (url == null) {
               logWriter.log("No URL specified for " + poolName,LogWriter.ERROR);
               continue;
            }

            String driver = props.getProperty(poolName + ".driver");
            String charset = props.getProperty(poolName + ".charset");
            String user = props.getProperty(poolName + ".user");
            String password = props.getProperty(poolName + ".password");

            String SQLVendor = props.getProperty(poolName + ".sqlvendor", "");
            etc.put(poolName+".SQLVendor", SQLVendor);

            String catDepth = props.getProperty(poolName + ".catdepth", "");
            etc.put(poolName+".catDepth", catDepth);

            String AppServer = props.getProperty(poolName + ".appserver", "");
            etc.put(poolName+".AppServer", AppServer);
            etc.put("DEF_SERVLET_ID"+".AppServer", AppServer);

            String acl = props.getProperty(poolName + ".acl", "false");

            String maxRows = props.getProperty(poolName + ".maxrows", "0");
            etc.put(poolName+".maxRows", maxRows);

            String maxConns = props.getProperty(poolName + ".maxconns", "0");
            int max;
            try {
               max = Integer.valueOf(maxConns).intValue();
            }
            catch (NumberFormatException e) {
               logWriter.log("Invalid maxconns value " + maxConns + " for " + poolName, 
                             LogWriter.ERROR);
               max = 0;
            }

            String initConns = props.getProperty(poolName + ".initconns", "0");
            int init;
            try {
              init = Integer.valueOf(initConns).intValue();
            }
            catch (NumberFormatException e) {
               logWriter.log("Invalid initconns value " + initConns + " for " + poolName,
                             LogWriter.ERROR);
               init = 0;
            }

            String loginTimeOut = props.getProperty(poolName + ".logintimeout", "5");
            int timeOut;
            try {
               timeOut = Integer.valueOf(loginTimeOut).intValue();
            }
            catch (NumberFormatException e) {
               logWriter.log("Invalid logintimeout value " + loginTimeOut + " for " + poolName,
                             LogWriter.ERROR);
               timeOut = 5;
            }

            String logLevelProp = props.getProperty(poolName + ".loglevel", 
                                                    String.valueOf(LogWriter.ERROR));
            int logLevel = LogWriter.INFO;
            if (logLevelProp.equalsIgnoreCase("none")) {
               logLevel = LogWriter.NONE;
            }
            else if (logLevelProp.equalsIgnoreCase("error")) {
               logLevel = LogWriter.ERROR;
            }
            else if (logLevelProp.equalsIgnoreCase("debug")) {
               logLevel = LogWriter.DEBUG;
            }
            
            String s_checkDBConnection = props.getProperty(poolName + ".checkdbconnection", "0");
            int checkDBConnection;
            try {
               checkDBConnection = Integer.valueOf(s_checkDBConnection).intValue();
            }
            catch (NumberFormatException e) {
               logWriter.log("Invalid checkDBConnection value " + s_checkDBConnection + " for " + poolName,LogWriter.ERROR);
               checkDBConnection = 0;
            }
            
            // thread for closing spare connections
            String housekeeping = props.getProperty(poolName + ".housekeeping", "true");
            
            // housekeeping thread for closing spare connections interval time in minutes
            String s_housekeepingIntervalTime = props.getProperty(poolName + ".housekeepingIntervalTime", "10");
            int housekeepingIntervalTime = 0;
            try {
               housekeepingIntervalTime = Integer.valueOf(s_housekeepingIntervalTime).intValue();
            }
            catch (NumberFormatException e) {
               housekeepingIntervalTime = 10;
            }
            
            // time in minutes
            String s_maxIdleTime = props.getProperty(poolName + ".maxIdleTime", "1");
            int maxIdleTime = 0;
            try {
               maxIdleTime = Integer.valueOf(s_maxIdleTime).intValue();
            }
            catch (NumberFormatException e) {
               maxIdleTime = 1;
            }
            
            String s_maxSpareDBConn = props.getProperty(poolName + ".maxSpareDBConn", "0");
            int maxSpareDBConn = 0;
            try {
               maxSpareDBConn = Integer.valueOf(s_maxSpareDBConn).intValue();
            }
            catch (NumberFormatException e) {
               maxSpareDBConn = 0;
            }

            AppConnPool pool = new AppConnPool(poolName, url, driver,
                charset,user, password,max, init, timeOut, pw, logLevel,
                SQLVendor, AppServer, checkDBConnection,
                housekeeping,housekeepingIntervalTime,maxIdleTime,maxSpareDBConn);
            connPool.put(poolName, pool);

            if (acl.equalsIgnoreCase("true")) {
              // δημιουργία ACL
              logWriter.log("Request ACL for " + poolName, LogWriter.INFO);
              AppACL appACL = new AppACL(poolName, this, logLevel);
              ACLPool.put(poolName, appACL);
            }
         }
      }
   }

  public Database getDBConnection(String name) {
    Database database = null;

    AppConnPool pool = (AppConnPool) connPool.get(name);

    if (pool != null) {
      try {
        database = pool.getDBConnection();
        if (database == null)
          pool.logWriter.log("connection is null", LogWriter.ERROR);
       }
       catch (Exception e) {
        logWriter.log(e, "Exception getting connection from " + name, LogWriter.ERROR);
       }
    }

    return database;
  }

  public void freeDBConnection(String name, Database database) {
    AppConnPool pool = (AppConnPool) connPool.get(name);
    if (pool != null && database != null) {
       pool.freeConnection(database);
    }
    else
      logWriter.log("Request to free connection " + name + " failed",LogWriter.ERROR);
   }

   public synchronized void release() {
      --clients;
      // Wait until called by the last client
      /*if (--clients != 0)
      {
         return;
      }

      Enumeration allPools = pools.elements();
      while (allPools.hasMoreElements())
      {
         ConnectionPool pool = (ConnectionPool) allPools.nextElement();
         pool.release();
      }

      Enumeration allDrivers = drivers.elements();
      while (allDrivers.hasMoreElements())
      {
         Driver driver = (Driver) allDrivers.nextElement();
         try
         {
            DriverManager.deregisterDriver(driver);
            logWriter.log("Deregistered JDBC driver " +
                          driver.getClass().getName(), LogWriter.INFO);
         }
         catch (SQLException e)
         {
            logWriter.log(e, "Couldn't deregister JDBC driver: " +
                             driver.getClass().getName(), LogWriter.ERROR);
         }
      }*/
   }

   public boolean addAuthUser(String keyName, AuthEmp authEmp) {
     AppACL appACL = (AppACL) ACLPool.get(keyName);
     boolean userExists = false;

     if (appACL != null)
     {
        userExists = appACL.addAuthUser(keyName, authEmp);

     }
     else {
        logWriter.log("Attempt to authenticate " + authEmp.getUsername() +
            " for " + keyName + " failed.", LogWriter.DEBUG);

     }
     return userExists;
   }

   public AuthEmp getAuthUser(String keyName, String userName) {
     AppACL appACL = (AppACL) ACLPool.get(keyName);
     AuthEmp authEmp = null;

     if (appACL != null)
     {
        authEmp = appACL.getAuthUser(userName);
     }
     return authEmp;
   }


   public void removeAuthUser(String keyName, AuthEmp authEmp) {
     AppACL appACL = (AppACL) ACLPool.get(keyName);

     if (appACL != null) {
        appACL.removeAuthUser(keyName, authEmp);
     }
     else {
        logWriter.log("Attempt to log out " + authEmp.getUsername() +
            " for " +   keyName  + " failed.", LogWriter.DEBUG);
     }
   }

  /**
    * Validate user's rights for tables
   */
  public int auth(String keyName, String username, String password,
                  String tableName, int permissions) {
    AppACL appACL = (AppACL) ACLPool.get(keyName);

     if (appACL != null) {
       return appACL.auth(username, password, tableName, permissions, keyName);
     }
     else {
       logWriter.log("Attempt to authenticate permission for " + username
                   + " for " + keyName + " failed.", LogWriter.DEBUG);
       return AUTH_NOPOOL;
     }
  }

  /**
    * override auth - passing session
    * Validate user's rights for tables
   */
  public int auth(String keyName, String username, String password,
    String tableName, int permissions, HttpSession session)
  {
    AppACL appACL = (AppACL) ACLPool.get(keyName);
     if (appACL != null)
     {
       return appACL.auth(username, password, tableName, permissions, keyName, session);
     }
     else
     {
       logWriter.log("Attempt to authenticate permission for " + username +
            " for " + keyName + " failed.", LogWriter.DEBUG);
       return AUTH_NOPOOL;
     }
  }


  synchronized public void restartACL(String ACLName)
  {
    ACLPool.remove(ACLName);

    // επανεκίνηση ACL
    logWriter.log("Request restart ACL for " + ACLName, LogWriter.INFO);

    AppACL appACL = new AppACL(ACLName, this, LogWriter.DEBUG);
    ACLPool.put(ACLName, appACL);
  }

  // deprecated
  synchronized public void restartConn(String connName) {
    /**AppConnPool appConnPool = (AppConnPool) connPool.get(connName);

    if (appConnPool != null) {
      logWriter.log("Request restart connection pooling for " + connName, LogWriter.INFO);

      // free connections !!! be carefull it doesn't release conn given allready !!!
      appConnPool.release();

      connPool.remove(connName);

      String[] url = new String[1], driver = new String[1],
               charset = new String[1], user = new String[1],
               password = new String[1], SQLVendor = new String[1],
               AppServer = new String[1];

      int[] max = new int[1], init = new int[1],
            timeOut = new int[1], logLevel = new int[1], checkDBConnection = new int[1];

      // get properties for conn pooling
      getConnPoolProps(connName, url, driver, charset, user,
        password, max, init, timeOut, logLevel, SQLVendor, AppServer);

      AppConnPool pool = new AppConnPool(connName, url[0], driver[0],
                                         charset[0],user[0], password[0],
                                         max[0], init[0], timeOut[0], pw, logLevel[0],
                                         SQLVendor[0], AppServer[0], checkDBConnection[0]);
      connPool.put(connName, pool);
    }**/
  }

  private void getConnPoolProps(String connName, String[] url, String[] driver,
    String[] charset, String[] user, String[] password,
    int[] max, int[] init, int[] timeOut, int[] logLevel, String[] SQLVendor,
    String[] AppServer)
  {
      Properties props = new Properties();

      try
      {
         InputStream is = new FileInputStream(PROPS_NAME);
         props.load(is);
      }
      catch (Exception e)
      {
         logWriter.log("Can't read the properties file. ", LogWriter.ERROR);
         return;
      }

      url[0] = props.getProperty(connName + ".url");

      driver[0] = props.getProperty(connName + ".driver");

      charset[0] = props.getProperty(connName + ".charset");

      user[0] = props.getProperty(connName + ".user");

      password[0] = props.getProperty(connName + ".password");

      SQLVendor[0] = props.getProperty(connName + ".sqlvendor","");
      etc.put(connName+".SQLVendor", SQLVendor[0]);

      AppServer[0] = props.getProperty(connName + ".appserver","");
      etc.put(connName+".AppServer", AppServer[0]);
      etc.put("DEF_SERVLET_ID"+".AppServer", AppServer[0]);

      String maxRows = props.getProperty(connName +
               ".maxrows", "0");
      etc.put(connName+".maxRows", maxRows);

      String maxConns = props.getProperty(connName +
               ".maxconns", "0");
            try
            {
               max[0] = Integer.valueOf(maxConns).intValue();
            }
            catch (NumberFormatException e)
            {
               logWriter.log("Invalid maxconns value " + maxConns +
                               " for " + connName, LogWriter.ERROR);
               max[0] = 0;
            }

            String initConns = props.getProperty(connName +
                              ".initconns", "0");
            try
            {
               init[0] = Integer.valueOf(initConns).intValue();
            }
            catch (NumberFormatException e)
            {
               logWriter.log("Invalid initconns value " + initConns +
                             " for " + connName, LogWriter.ERROR);
               init[0] = 0;
            }

            String loginTimeOut = props.getProperty(connName +
               ".logintimeout", "5");
            try
            {
               timeOut[0] = Integer.valueOf(loginTimeOut).intValue();
            }
            catch (NumberFormatException e)
            {
               logWriter.log("Invalid logintimeout value " + loginTimeOut +
                            " for " + connName, LogWriter.ERROR);
               timeOut[0] = 5;
            }

            String logLevelProp = props.getProperty(connName +
                           ".loglevel", String.valueOf(LogWriter.ERROR));
            logLevel[0] = LogWriter.INFO;
            if (logLevelProp.equalsIgnoreCase("none"))
            {
               logLevel[0] = LogWriter.NONE;
            }
            else if (logLevelProp.equalsIgnoreCase("error"))
            {
               logLevel[0] = LogWriter.ERROR;
            }
            else if (logLevelProp.equalsIgnoreCase("debug"))
            {
               logLevel[0] = LogWriter.DEBUG;
            }
      props.clear();
  }

  public Hashtable getConnPool() {
    return connPool;
  }

  public Hashtable getACLPool() {
    return ACLPool;
  }

  public String getAttr(String keyName)
  {
    if ( etc.get(keyName) != null)
      return etc.get(keyName).toString();
    else
      return "";
  }
}