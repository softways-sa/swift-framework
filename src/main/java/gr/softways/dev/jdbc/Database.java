/*
 * Database.java
 *
 * Created on 30 Ιανουάριος 2003, 10:28 πμ
 */

package gr.softways.dev.jdbc;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;

import java.sql.SQLException;

import java.util.Properties;

import gr.softways.dev.util.DbRet;

/**
 * A connection (session) with a specific database. SQL statements are executed 
 * and results are returned within the context of a connection.
 *
 * @author  minotauros
 */
public class Database {
  
  // keep for legacy reasons
  public static int TRANS_ISO_1 = Connection.TRANSACTION_READ_COMMITTED;
  public static int TRANS_ISO_SIMPLE = Connection.TRANSACTION_READ_UNCOMMITTED;
  
  public Database() {
  }
  
  /**
   * Set database connection properties but do not attempt to 
   * establish a connection.
   *
   * @param url a database url of the form jdbc:subprotocol:subname
   * @param propertiesInfo a list of arbitrary string tag/value pairs 
   *        as connection arguments
   */
  public final synchronized void setConnection(String url, Properties propertiesInfo) {
    closeConnection();
    
    _url = url;
    _propertiesInfo = propertiesInfo;
  }
  
  /**
   * Establish database connection.
   */
  public final synchronized void openConnection() {
    try {
      if (_connection == null) _connection = DriverManager.getConnection(getURL(), getPropertiesInfo());
    }
    catch (SQLException sqlException1) {
     DataSetException.SQLException(sqlException1);
    }
  }
  
  /**
   * Executes the given SQL statement, which may be an INSERT, UPDATE, 
   * or DELETE statement or an SQL statement that returns nothing, 
   * such as an SQL DDL statement.
   *
   * @param sql an SQL INSERT, UPDATE or DELETE statement 
   *        or an SQL statement that returns nothing
   */
  public final int executeStatement(String sql) {
    Statement statement = null;
    
    int i = -1;
    
    try {
      statement = _connection.createStatement();
      i = statement.executeUpdate(sql);
    }
    catch (SQLException sqlException1) {
      DataSetException.SQLException(sqlException1);
    }
    finally {
      if (statement != null) {
        try {
          statement.close();
        }
        catch (SQLException sqlException2) {
          DataSetException.SQLException(sqlException2);
        }
      }
    }
    return i;
  }
  
  /**
   * Creates a Statement object that will generate ResultSet 
   * objects with the given type and concurrency.
   *
   * @param resultSetType a result set type; one of ResultSet.TYPE_FORWARD_ONLY, 
   *        ResultSet.TYPE_SCROLL_INSENSITIVE, 
   *        or ResultSet.TYPE_SCROLL_SENSITIVE
   * @param resultSetConcurrency a concurrency type; one of 
   *        ResultSet.CONCUR_READ_ONLY 
   *        or ResultSet.CONCUR_UPDATABLE
   */
  final synchronized Statement createStatement(int resultSetType,
                                               int resultSetConcurrency) {
    openConnection();
    
    try {
      return _connection.createStatement(resultSetType, resultSetConcurrency);
    }
    catch (SQLException sqlException1) {
      DataSetException.SQLException(sqlException1);
    }
    
    return null;
  }
  
  /**
   * Creates a Statement object that will generate ResultSet 
   * objects with TYPE_FORWARD_ONLY type and CONCUR_READ_ONLY concurrency.
   */
  final synchronized Statement createStatement() {
    openConnection();
    
    try {
      return _connection.createStatement();
    }
    catch (SQLException sqlException1) {
      DataSetException.SQLException(sqlException1);
    }
    
    return null;
  }
  
  /**
   * Creates a PreparedStatement object for sending parameterized 
   * SQL statements to the database.
   *
   * @param sql an SQL statement that 
   *        may contain one or more '?' IN parameter placeholders
   * @return a new default PreparedStatement object 
   *         containing the pre-compiled SQL statement 
   */
  public final synchronized PreparedStatement createPreparedStatement(String sql) {
    try {
      PreparedStatement preparedStatement = _connection.prepareStatement(sql);
      return preparedStatement;
    }
    catch(SQLException sqlException1) {
      DataSetException.SQLException(sqlException1);
    }
    return null;
  }
  
  /**
   * Releases this connection object's database and JDBC resources 
   * immediately instead of waiting for them to be automatically released.
   */
  public final synchronized void closeConnection() {
    if (_connection != null) {
      try {
        _connection.close();
      }
      catch(SQLException sqlException1) {
        DataSetException.SQLException(sqlException1);
      }
    }
  }
  
  /**
   * Makes all changes made since the previous commit/rollback permanent 
   * and releases any database locks currently held by this Connection object. 
   * This method should be used only when auto-commit mode has been disabled.
   */
  public void commit() {
    try {
      if (_connection != null) _connection.commit();
    }
    catch (SQLException sqlException1) {
      DataSetException.SQLException(sqlException1);
    }
  }

  /**
   * Undoes all changes made in the current transaction and releases any 
   * database locks currently held by this Connection object. 
   * This method should be used only when auto-commit mode has been disabled.
   */
  public void rollback() {
    try {
      if (_connection != null) _connection.rollback();
    }
    catch (SQLException sqlException1) {
      DataSetException.SQLException(sqlException1);
    }
  }

  /**
   * Retrieves this Connection object's current transaction isolation level.
   *
   * @return the current transaction isolation level
   */
  public final int getTransactionIsolation() {
    int i = -1;

    try {
      if (_connection != null) i = _connection.getTransactionIsolation();
    }
    catch (SQLException sqlException1) {
      DataSetException.SQLException(sqlException1);
    }
    
    return i;
  }
  
  /**
   * Attempts to change the transaction isolation level for this 
   * Connection object to the one given. The constants defined in the 
   * interface Connection are the possible transaction isolation levels.
   */
  public final synchronized void setTransactionIsolation(int level) {
    try {
      if (_connection != null) _connection.setTransactionIsolation(level);
    }
    catch (SQLException sqlException1) {
      switch (level) {
        case Connection.TRANSACTION_READ_UNCOMMITTED : 
          level = Connection.TRANSACTION_READ_COMMITTED;
          break;
          
        case Connection.TRANSACTION_READ_COMMITTED :
          level = Connection.TRANSACTION_REPEATABLE_READ;
          break;
          
        case Connection.TRANSACTION_REPEATABLE_READ :
          level = Connection.TRANSACTION_SERIALIZABLE;
          break;
        
        default : 
          DataSetException.SQLException(sqlException1);
      }
      setTransactionIsolation(level);
    }
  }

  /**
   * Sets this connection's auto-commit mode to the given state. If a connection 
   * is in auto-commit mode, then all its SQL statements will be executed 
   * and committed as individual transactions. Otherwise, its SQL statements 
   * are grouped into transactions that are terminated by a call 
   * to either the method commit or the method rollback. 
   * By default, new connections are in auto-commit mode. 
   * <p>
   * The commit occurs when the statement completes or the next execute occurs, 
   * whichever comes first. In the case of statements returning a ResultSet object, 
   * the statement completes when the last row of the ResultSet object has been 
   * retrieved or the ResultSet object has been closed. 
   * In advanced cases, a single statement may return multiple results 
   * as well as output parameter values. In these cases, the commit occurs 
   * when all results and output parameter values have been retrieved.
   * <p>
   * NOTE: If this method is called during a transaction, 
   * the transaction is committed. 
   *
   * @param autoCommit true to enable auto-commit mode; false to disable it
   */
  public final void setAutoCommit(boolean autoCommit) {
    try {
      if (_connection != null) _connection.setAutoCommit(autoCommit);
    }
    catch (SQLException sqlException1) {
      DataSetException.SQLException(sqlException1);
    }
  }
  
  /**
   * Retrieves whether this Connection object has been closed. 
   * A connection is closed if the method close has been called on it 
   * or if certain fatal errors have occurred. This method is guaranteed 
   * to return true only when it is called after the method Connection.close 
   * has been called. 
   * <p>
   * This method generally cannot be called to determine whether a connection 
   * to a database is valid or invalid. A typical client can determine 
   * that a connection is invalid by catching any exceptions that 
   * might be thrown when an operation is attempted.
   *
   * @return true if this Connection object is closed, false if it is still open 
   */
  public final synchronized boolean isClosed() {
    boolean closed = true;
    
    try {
      if (_connection != null) closed = _connection.isClosed();
    }
    catch (SQLException sqlException1) {
      DataSetException.SQLException(sqlException1);
    }
    
    return closed;
  }
  
  /**
   * Return underlying JDBC Connection object.
   */
  final Connection getJdbcConnection() {
    return _connection;
  }
  
  /**
   * Return jdbc url used to establish this connection.
   *
   * @return jdbc url used to establish this connection
   */
  public String getURL() {
    return _url;
  }
  
  /**
   * Return the list of arbitrary string tag/value pairs used 
   * as connection arguments for this connection.
   *
   * @return a list of arbitrary string tag/value pairs
   */
  public Properties getPropertiesInfo() {
    return _propertiesInfo;
  }
  
  /**
   *
   */
  public DbRet beginTransaction(int isoLevel) {
    DbRet dbRet = new DbRet();

    String SQLVendor = _propertiesInfo.getProperty("sqlvendor");
    
    try {
      dbRet.setRetInt(getTransactionIsolation());

      if (isoLevel == TRANS_ISO_SIMPLE && SQLVendor.equals("SYBASE")) {
         // in case of SYBASE signal back that we do not realy start a trans
         dbRet.setRetInt( 999999 );
      }
      
      if (SQLVendor.equals("MSSQL")) {
        // MICROSOFT SQL SERVER
        executeStatement("SET TRANSACTION ISOLATION LEVEL " 
                       + isoLevelString[isoLevel]);
      }
      else if (SQLVendor.equals("INTERBASE")) {
        // INTERBASE
        setAutoCommit(false);
        setTransactionIsolation(isoLevel);
      }
      else if (SQLVendor.equals("SYBASE")) {
        if (isoLevel != TRANS_ISO_SIMPLE) {
          setTransactionIsolation(isoLevel);
        }
      }
      else {
        // DEFAULT
        setTransactionIsolation(isoLevel);
      }

      if (SQLVendor.equals("SYBASE")) {
        if (isoLevel != TRANS_ISO_SIMPLE) {
          // SYBASE
          // executeStatement("SET CHAINED OFF");
          executeStatement("BEGIN TRANSACTION");
        }
      }
      else if (SQLVendor.equals("MSSQL")) {
        // MICROSOFT SQL SERVER
        executeStatement("BEGIN TRANSACTION");
      }
      else if (SQLVendor.equals("INTERBASE")) {
        // INTERBASE SERVER
        ;
      }
      else {
        // DEFAULT
        setAutoCommit(false);
      }

    }
    catch (Exception e) {
      e.printStackTrace();

      dbRet.setNoError(0);
    }

    return dbRet;
  }
  
  /**
   *
   */
  public DbRet commitTransaction(int commit, int prevTransIsolation) {
    DbRet dbRet = new DbRet();
    
    dbRet.setNoError(commit);
    
    String SQLVendor = _propertiesInfo.getProperty("sqlvendor");

    if (commit == 0) {
      // ROLLBACK
      try {
        if (SQLVendor.equals("SYBASE")) {
          if (prevTransIsolation != 999999) {
            // SYBASE
            executeStatement("ROLLBACK TRANSACTION");
            // rollback();
          }
        }
        else if (SQLVendor.equals("INTERBASE")) {
          // INTERBASE
          rollback();
          setAutoCommit(true);
        }
        else if (SQLVendor.equals("MSSQL")) {
          // MICROSOFT SQL SERVER
          executeStatement("ROLLBACK TRANSACTION");
        }
        else {
          // DEFAULT
          rollback();
          setAutoCommit(true);
        }
      }
      catch (Exception de) {
        dbRet.setNoError(0);
        de.printStackTrace();
      }
    }
    else {
      // COMMIT αποθήκευση αλλαγών στην βάση δεδομένων
      try {
        if (SQLVendor.equals("SYBASE")) {
          if (prevTransIsolation != 999999) {
            // SYBASE
            executeStatement("COMMIT TRANSACTION");
            // commit();
          }
        }
        else if (SQLVendor.equals("INTERBASE")) {
          // INTERBASE
          commit();
          setAutoCommit(true);
        }
        else if (SQLVendor.equals("MSSQL")) {
          // MICROSOFT SQL SERVER
          executeStatement("COMMIT TRANSACTION");
        }
        else {
          // DEFAULT
          commit();
          setAutoCommit(true);
        }

      }
      catch (Exception e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
    }

    try {
      if (SQLVendor.equals("MSSQL")) {
        executeStatement("SET TRANSACTION ISOLATION LEVEL " 
                       + isoLevelString[prevTransIsolation]);
      }
      else if (SQLVendor.equals("INTERBASE")) {
        setTransactionIsolation(prevTransIsolation);
      }
      else if (prevTransIsolation != 999999) {
        setTransactionIsolation(prevTransIsolation);
      }
    }
    catch (Exception de) {
      de.printStackTrace();
    }

    return dbRet;
  }
 
  public DbRet execQuery(String query) {
    DbRet dbRet = new DbRet();

    try {
      int affectedRows = executeStatement(query);
      dbRet.retry = 0;
      dbRet.unknownError = 0;
      dbRet.noError = 1;
      
      dbRet.retInt = affectedRows;
    }
    catch (DataSetException e) {
      if ((dbRet.dbErrorCode = e.getErrorCode()) != 66) {
         dbRet.retry = 0;
         dbRet.unknownError = 1;
         dbRet.noError = 0;
      }
      else {
         dbRet.retry = 1;
         dbRet.unknownError = 0;
         dbRet.noError = 0;
      }

      dbRet.dbErrorCode = e.getErrorCode();
      e.printStackTrace();
    }
    catch (Exception e) {
      dbRet.retry = 0;
      dbRet.unknownError = 1;
      dbRet.noError = 0;
      e.printStackTrace();
    }
    
    return dbRet;
  }
  
  public DbRet execProcedure(String procName, String[] procParams, 
                             String[] procParamDlm) {
    DbRet dbRet = new DbRet();
    
    String SQLVendor = _propertiesInfo.getProperty("sqlvendor");
    
    String query = null;
    
    int arrLen = procParams.length;
    
    int i = 0;
    
    if (SQLVendor.equals("INTERBASE")) {
      query = "execute procedure " + procName;
      for (; i < arrLen; i++) {
        if (i>0)
          query += ",";
        query += " " + procParamDlm[i] + procParams[i] + procParamDlm[i];
      }
    }
    else {
      query = "exec " + procName;
      for (; i < arrLen; i++) {
        if (i>0)
          query += ",";
        query += " " + procParamDlm[i] + procParams[i] + procParamDlm[i];
      }
    }
    try {
      executeStatement(query);
      dbRet.retry = 0;
      dbRet.unknownError = 0;
      dbRet.noError = 1;
    }
    catch (DataSetException e) {
      if ((dbRet.dbErrorCode = e.getErrorCode()) != 66) {
         dbRet.retry = 0;
         dbRet.unknownError = 1;
         dbRet.noError = 0;
      }
      else {
         dbRet.retry = 1;
         dbRet.unknownError = 0;
         dbRet.noError = 0;
      }
      
      e.printStackTrace();
    }
    catch (Exception e) {
      dbRet.retry = 0;
      dbRet.unknownError = 1;
      dbRet.noError = 0;
      e.printStackTrace();
    }
    
    return dbRet;
  }
  
  public DbRet setReadOnly(boolean readOnly) {
    DbRet dbRet = new DbRet();
    
    try {
      boolean pReadOnly = _connection.isReadOnly();
      
      if (pReadOnly != readOnly) _connection.setReadOnly(readOnly);
    }
    catch (Exception e) {
      dbRet.setNoError(0);
    }
    
    return dbRet;
  }
  
  private Connection _connection = null;
  
  private String _url = null;
  private Properties _propertiesInfo = null;
  
  private final String[] isoLevelString 
          = new String[] {"", "READ UNCOMMITTED",
                          "READ COMMITTED", "", "REPEATABLE READ", 
                          "", "", "", "SERIALIZABLE"};
}