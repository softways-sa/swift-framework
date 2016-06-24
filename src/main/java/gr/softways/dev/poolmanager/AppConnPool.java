package gr.softways.dev.poolmanager;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.LogWriter;
import java.io.*;
import java.util.*;

public class AppConnPool {

   private String name;
   private String URL;
   private String driver;
   private String charset;
   private String user;
   private String password;

   private String SQLVendor;
   
   private int checkDBConnection;

   private String AppServer;

   private int maxRows;

   private int maxConns;
   private int timeOut;
   public LogWriter logWriter;

   private PrintWriter pw;

   private int checkedOut;
   
   private String housekeeping = "";
   private int housekeepingIntervalTime = 0;
   
   private int maxIdleTime = 0;
   private int maxSpareDBConn = 0;
   
   //private Vector freeConnections = new Vector();
   
   private Object[][] dbConnections = null;

   private int totalDatabaseConnections = 0;
   private int freeDatabaseConnections = 0;
   
   private Timer timer = null;
   private TimerTask houseKeepingThread = null;
   
   private static final long SLOW_QUERY_THRESHOLD = 499;
   
   public AppConnPool(String name, String URL, String driver,
                      String charset,String user,
                      String password, int maxConns, int initConns, int timeOut,
                      PrintWriter pw, int logLevel, String SQLVendor,
                      String AppServer,int checkDBConnection,
                      String housekeeping, int housekeepingIntervalTime,
                      int maxIdleTime, int maxSpareDBConn) {
      this.name = name;
      this.URL = URL;
      this.driver = driver;
      this.charset = charset;
      this.user = user;
      this.password = password;

      this.SQLVendor = SQLVendor;

      this.AppServer = AppServer;

      this.maxRows = maxRows;

      this.maxConns = maxConns;
      this.timeOut = timeOut > 0 ? timeOut : 5;
      
      this.checkDBConnection = checkDBConnection;
      
      this.housekeeping = housekeeping;
      this.housekeepingIntervalTime = housekeepingIntervalTime;
      this.maxIdleTime = maxIdleTime;
      this.maxSpareDBConn = maxSpareDBConn;
      
      try {
        this.pw = new PrintWriter(new FileWriter("swlogs/" + name 
                                               + "_logs/" + name 
                                               + ".log", false), true);
      }
      catch (IOException ioe) {
        ioe.printStackTrace();
      }

      // print to main AppLog
      logWriter = new LogWriter(name, logLevel, pw);
      logWriter.log("Request for a new pool starting for " + name, LogWriter.INFO);

      // change to your own log
      logWriter.setPrintWriter(this.pw);

      dbConnections = new Object[maxConns][3];
      
      initPool(initConns);

      logWriter.log("New pool created", LogWriter.INFO);
      String lf = System.getProperty("line.separator");
      logWriter.log(lf +
          " url=" + URL + lf +
          " driver=" + driver + lf +
          " charset=" + charset + lf +
          " user=" + user + lf +
          " password=" + password + lf +
          " initconns=" + initConns + lf +
          " maxconns=" + maxConns + lf +
          " logintimeout=" + this.timeOut + lf +
          " checkdbconnection=" + this.checkDBConnection + lf +
          " housekeeping=" + this.housekeeping + lf +
          " housekeepingIntervalTime=" + this.housekeepingIntervalTime + lf +
          " maxIdleTime=" + this.maxIdleTime + lf +
          " maxSpareDBConn=" + this.maxSpareDBConn,
          LogWriter.DEBUG);
      logWriter.log(getStats(), LogWriter.DEBUG);
      
      if (this.housekeeping == "true") {
        timer = new Timer(true);
        houseKeepingThread = new HouseKeepingThread(this);
        
        timer.schedule(houseKeepingThread, 5 * 60 * 1000, this.housekeepingIntervalTime * 60 * 1000);
      }
   }

  private synchronized void initPool(int initConns) {
    int free = 0;
    
    for (int i = 0; i < initConns; i++) {
      // mark new connections as available
      if (newConnection("1") != null) free++;
    }
    
    freeDatabaseConnections = free;
  }
   
  private Database newConnection(String availability) {
    Database database = null;
    
    try {
      for (int i=0; i<maxConns; i++) {
        if (dbConnections[i][0] == null) {
          database = openConnection();

          dbConnections[i][0] = database;
          dbConnections[i][1] = availability;
          dbConnections[i][2] = System.currentTimeMillis();

          totalDatabaseConnections++;

          logWriter.log("Opened a new connection", LogWriter.DEBUG);
          break;
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return database;
  }
  
  private Database openConnection() throws Exception {
    Database database = null;
    
    try {
      database = new Database();
  
      Properties props = new Properties();
      
      props.setProperty("password", password);
      props.setProperty("user", user);
  
      if (!SQLVendor.equals("INTERBASE")) {
        props.setProperty("charSet", charset);
      }
      
      props.setProperty("lc_ctype", charset); // charset for jaybird
      
      props.setProperty("sqlvendor", SQLVendor);
      
      database.setConnection(URL, props);
      
      database.openConnection();
    }
    catch (Exception e) {
      database = null;
      
      System.out.println("[" + new java.util.Date() + "] " + name + ": Could not open new connection.");
      throw e;
    }
    
    return database;
  }
   
  public Database getDBConnection() throws Exception {
    Database d = null;
    
    logWriter.log("Request for connection received - " + getStats(), LogWriter.DEBUG);
    
    try {
      d = getDBConnection(timeOut * 1000);
      
      return d;
    }
    catch (Exception e) {
      System.out.println("[" + new java.util.Date() + "] " + name + ": getDBConnection() timed-out");
      logWriter.log(e, "Exception getting connection", LogWriter.ERROR);
      
      throw e;
    }
  }

  private synchronized Database getDBConnection(long timeout) throws Exception {
    // Get a pooled Connection from the cache or a new one.
    // Wait if all are checked out and the max limit has
    // been reached.
    long startTime = System.currentTimeMillis();
    long remaining = timeout;
    
    Database database = null;
    
    while ((database = getPooledConnection()) == null) {
      try {
        logWriter.log("Waiting for connection. Timeout=" + remaining,LogWriter.DEBUG);
        
        wait(remaining);
      }
      catch (InterruptedException e) {
      }

      remaining = timeout - (System.currentTimeMillis() - startTime);
      
      if (remaining <= 0) {
        // Timeout has expired
        logWriter.log("Time-out while waiting for connection",LogWriter.DEBUG);
        throw new DataSetException("getConnection() timed-out");
      }
    }

    if (database == null) throw new DataSetException("getConnection() timed-out");
    
    checkedOut++;
    
    logWriter.log("Delivered connection from pool - " + getStats(), LogWriter.DEBUG);
    
    return database;
  }

  /**
   * Έλεγχος αν το connection είναι ακόμα valid.
   *
   */
  public boolean isConnectionOK(Database database) {
    boolean connOK = true;

    //Statement statement = null;
    
    String query = null;
    
    QueryDataSet queryDataSet = null;
    
    try {
      //statement = database.createStatement();
      queryDataSet = new QueryDataSet();
      
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      if (SQLVendor.equals("INTERBASE")) {
        query = "SELECT 1 FROM rdb$database";
        
        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        queryDataSet.refresh();
      }
      else if (SQLVendor.equals("SYBASE")) {
        //statement.execute("SELECT suid FROM sysusers WHERE suid = 999");
        query = "SELECT suid FROM sysusers WHERE suid = 999";
        
        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        queryDataSet.refresh();
      }
      else if (SQLVendor.equals("MSSQL")) {
        query = "SELECT uid FROM sysusers WHERE uid = 999";
        
        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        queryDataSet.refresh();
      }
      else {
        // validation not supported
      }
    }
    catch (Throwable e) {
      connOK = false;
      e.printStackTrace();
    }
    finally {
      //if (statement != null) try { statement.close(); } catch (Throwable t) { }
      //statement = null;
      if (queryDataSet != null) queryDataSet.close();
      queryDataSet = null;
      query = null;
    }
        
    if (connOK == false) {
      logWriter.log("Pooled Connection was not okay", LogWriter.ERROR);
    }

    return connOK;
   }

  /**
   * Get a pooled connection and marked it unavailable.
   */
  private Database fetchDatabaseConnection() {
    Database database = null;
    
    for (int i=0; i<maxConns; i++) {
      
      if ( dbConnections[i][0] != null && ((String)dbConnections[i][1]).equals("1") ) {
        database = (Database)dbConnections[i][0];
        
        // Check if the Connection is still OK
        if (checkDBConnection == 1 && !isConnectionOK(database)) {
          // It was bad, try to replace database connection
          try {
            database = openConnection();
            dbConnections[i][0] = database;
            
            logWriter.log("Replaced bad connection from pool",LogWriter.ERROR);
          }
          catch (Exception e) {
            e.printStackTrace();
          }
        }
        
        dbConnections[i][1] = "0";
        dbConnections[i][2] = System.currentTimeMillis();
        
        freeDatabaseConnections--;
        
        break;
      }
      
    }
    
    return database;
  }
  
  private Database getPooledConnection() {
    Database database = null;

    if (freeDatabaseConnections > 0) {
      database = fetchDatabaseConnection();
    }
    else if (totalDatabaseConnections < maxConns) {
      database = newConnection("0"); // mark new connections as unavailable
    }
    
    return database;
  }

  public synchronized void freeConnection(Database database) {
    long timeReturned = System.currentTimeMillis();
    
    for (int i=0; i<maxConns; i++) {
      if ( dbConnections[i][0] != null && database == (Database)dbConnections[i][0] ) {
        long timeElapsedInUse = 0;
        long timeTaken = 0;
        
        if (dbConnections[i][2] != null) timeTaken = (Long)dbConnections[i][2];
        
        dbConnections[i][1] = "1";
        dbConnections[i][2] = timeReturned;
        
        timeElapsedInUse = timeReturned - timeTaken;
        freeDatabaseConnections++;
        checkedOut--;
        
        if (timeElapsedInUse > SLOW_QUERY_THRESHOLD) {
          System.out.println("[" + new java.util.Date() + "] " + name + " - DB connection elapsed time: (" + trace(Thread.currentThread().getStackTrace()) + ") " + timeElapsedInUse + "ms");
        }
        
        logWriter.log("Returned connection to pool - " + getStats(), LogWriter.DEBUG);
        
        notifyAll();
        
        break;
      }
    }
  }
  
  public synchronized void release() {
    for (int i=0; i<maxConns; i++) {
      
      try {
        if (dbConnections[i][0] != null) {
          ((Database)dbConnections[i][0]).closeConnection();
          dbConnections[i][1] = "1";
          dbConnections[i][2] = System.currentTimeMillis();
          
          logWriter.log("Closed connection", LogWriter.DEBUG);
        }
      }
      catch (DataSetException e) {
        logWriter.log(e, "Couldn't close connection", LogWriter.ERROR);
      }
      
    }
    if (timer != null) timer.cancel();
  }

  public synchronized void releaseDBConnection(Database database) {
    for (int i=0; i<maxConns; i++) {
      
      if ( dbConnections[i][0] != null && database == (Database)dbConnections[i][0] ) {
        try {
          ((Database)dbConnections[i][0]).closeConnection();
          
          break;
        }
        catch (Exception e) {
          System.out.println("[" + new java.util.Date() + "] " + name + ": Could not close connection.");
          e.printStackTrace();
          logWriter.log(e, "Couldn't close connection", LogWriter.ERROR);
          
          break;
        }
        finally {
          dbConnections[i][0] = null;
          dbConnections[i][1] = null;
          dbConnections[i][2] = null;

          freeDatabaseConnections--;
          totalDatabaseConnections--;

          logWriter.log("Closed connection from pool - " + getStats(), LogWriter.INFO);
        }
      }
      
    }
  }
  
  private String getStats() {
    return "Total connections: " +
       totalDatabaseConnections +
       " Available: " + freeDatabaseConnections +
       " Checked-out: " + checkedOut;
  }

  public String getSQLVendor() {
    return SQLVendor;
  }

  public String getAppServer() {
    return AppServer;
  }

  /**
   * maxRows for a QueryDataSet
   */
  public int getMaxRows() {
    return maxRows;
  }
  
  public String getName() {
    return name;
  }
  
  public int getTotalDatabaseConnections() {
    return totalDatabaseConnections;
  }
  
  public synchronized Object getDBConnections(int row, int column) {
    return dbConnections[row][column];
  }
  
  public int getMaxIdleTime() {
    return maxIdleTime;
  }
  public int getMaxSpareDBConn() {
    return maxSpareDBConn;
  }
  public int getMaxConns() {
    return maxConns;
  }
  
  public static String trace(StackTraceElement e[]) {
    String methodName = "";
    
    try {
      for (StackTraceElement s : e) {
        if (s.getMethodName().equals("getStackTrace") 
            || "getDBConnection".equals(s.getMethodName()) 
            || "freeConnection".equals(s.getMethodName()) 
            || "freeDBConnection".equals(s.getMethodName())
            || ("parseTable".equals(s.getMethodName()) && "gr.softways.dev.util.SearchBean2".equals(s.getClassName()))
        ) {
          continue;
        }
        else {
          methodName = s.getClassName() + " - " + s.getMethodName();
          break;
        }
      }
    }
    catch (Exception ex) {
    }
    
    return methodName;
  }
}