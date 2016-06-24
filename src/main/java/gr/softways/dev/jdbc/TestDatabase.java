/*
 * TestDatabase.java
 *
 * Created on 30 ÉáíïõÜñéïò 2003, 1:49 ìì
 */

package gr.softways.dev.jdbc;

import gr.softways.dev.jdbc.Database;
import gr.softways.dev.jdbc.DataSetException;
import gr.softways.dev.jdbc.Load;
import gr.softways.dev.jdbc.QueryDataSet;
import gr.softways.dev.jdbc.QueryDescriptor;

import java.util.Properties;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Timestamp;

import java.math.BigDecimal;

/**
 *
 * @author  minotauros
 */
public class TestDatabase {
      
  public TestDatabase() {
    boolean isOK = false;
    
    _propertiesInfo = new Properties();
    _propertiesInfo.setProperty("lc_ctype", "WIN1253");
    _propertiesInfo.setProperty("charSet", "iso88597");
    //_propertiesInfo.setProperty("user", "SYSDBA");
    _propertiesInfo.setProperty("user", "sa");
    _propertiesInfo.setProperty("password", "tmrbumb$");
    
    isOK = testDatabaseOpenConnection();
    
    if (isOK) {
      isOK = testDatabaseExecuteStatement();
    }
    
    if (isOK) {
      isOK = testDatabaseTransaction();
    }
    
    if (isOK) {
      isOK = testQueryDataSetOneRow();
    }
    
    if (isOK) {
      isOK = testQueryDataSetNullValues();
    }
    
    if (isOK) {
      isOK = testQueryDataSetManyRows();
    }

    if (isOK) {
     isOK = testQueryDataSetJoin();
    }
    
    if (isOK) {
     isOK = testQueryDataSetUNCACHED();
    }

    if (isOK) {
     isOK = testDataRow();
    }

    if (isOK) {
     isOK = testQueryDataSetLocate();
    }    
    
    if (isOK) {
     isOK = testQueryDataSetLookup();
    }    
    
    isOK = testDatabaseCloseConnection();
  }
  
  public boolean testDatabaseOpenConnection() {
    boolean isOK = false;
    
    try {
      //Driver driver = (Driver) Class.forName(_jdbcInterbaseDriver).newInstance();
      Driver driver = (Driver) Class.forName(_jdbcSybaseDriver).newInstance();

      DriverManager.registerDriver(driver);
    }
    catch (Throwable e) {
      System.err.println("There seems to be a problem with jdbc driver. Exiting test.");
      System.exit(-1);
    }
    
    database = new Database();
            
    try {
      //database.setConnection(_jdbcInterbaseURL, _propertiesInfo);
      database.setConnection(_jdbcSybaseURL, _propertiesInfo);
      
      database.openConnection();
      
      if (database.getJdbcConnection() != null) {
        isOK = true;
        System.out.println("DatabaseOpenConnection : passed");
      }
      else {
        isOK = false;
        System.err.println("DatabaseOpenConnection : failed!!!");
      }
    }
    catch (DataSetException dse) {
      isOK = false;
      dse.printStackTrace();
    }
    
    return isOK;
  }
  
  public boolean testDatabaseCloseConnection() {
    boolean isOK = false;
    
    try {
      database.closeConnection();
      
      if (database.isClosed() == true) {
        isOK = true;
        System.out.println("DatabaseCloseConnection : passed");
      }
      else {
        isOK = false;
        System.err.println("DatabaseCloseConnection : failed");
      }
    }
    catch (Exception dse) {
      isOK = false;
      dse.printStackTrace();
    }
    
    return isOK;
  }
  
  public boolean testDatabaseExecuteStatement() {
    boolean isOK = false;
    
    try {
      database.executeStatement(_sqlDelete);
      
      int rowsAffected = database.executeStatement(_sqlInsert);
      
      if (rowsAffected == 1) {      
        isOK = true;
        System.out.println("DatabaseExecuteStatement : passed");
      }
      else {
        isOK = false;
        System.err.println("DatabaseExecuteStatement: failed!!!");
        System.err.println("DatabaseExecuteStatement: rowsAffected should be 1 and is " + rowsAffected);
      }
    }
    catch (DataSetException dse) {
      isOK = false;
      dse.printStackTrace();
      System.err.println("DatabaseExecuteStatement: failed!!! query = " + _sqlInsert);
    }
    
    return isOK;
  }
  
  public boolean testDatabaseTransaction() {
    boolean isOK = false;
    
    try {
      database.executeStatement(_sqlDelete);
      
      database.setAutoCommit(false);
      
      int oldTransactionIsolationLevel = database.getTransactionIsolation();
      
      database.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

      int rowsAffected = database.executeStatement(_sqlInsert);
      if (rowsAffected != 1) {
        System.err.println("DatabaseTransactionLevel : failed");
        System.err.println("DatabaseTransactionLevel : rowsAffected should be 1 and is " + rowsAffected);
        throw new Exception("DatabaseTransactionLevel : rowsAffected should be 1 and is " + rowsAffected);
      }
      
      database.rollback();
      
      database.setTransactionIsolation(oldTransactionIsolationLevel);
      
      database.setAutoCommit(true);
      
      rowsAffected = database.executeStatement(_sqlInsert);
      if (rowsAffected != 1) {
        System.err.println("DatabaseTransactionLevel : failed");
        System.err.println("DatabaseTransactionLevel : rowsAffected should be 1 and is " + rowsAffected);
        throw new Exception("DatabaseTransactionLevel : rowsAffected should be 1 and is " + rowsAffected);
      }
      
      database.executeStatement(_sqlDelete);
      
      isOK = true;
      
      System.out.println("DatabaseTransactionLevel : passed");
    }
    catch (Exception dse) {
      isOK = false;
      dse.printStackTrace();
    }
    
    return isOK;
  }
  
  public boolean testQueryDataSetOneRow() {
    boolean isOK = false;
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    String sqlDelete = "DELETE FROM TESTTAB1";
    
    String sqlInsert = "INSERT INTO TESTTAB1 ("
                     + "CHARCOL, INTCOL, VARCHARCOL, TIMESTAMPCOL"
                     + ",BIGDECIMALCOL"
                     + ") VALUES ("
                     + "'ÁÂÃÄÅæçèé'"
                     + ",99"
                     + ",'æ÷øùâíìÆ×ØÙÂÍÌ'"
                     + ",'1975-11-07 10:00:00.00'"
                     + ",500.89"
                     + ")";    
    // new Timestamp(184575600000l) == 1975-11-07 10:00:00.00
    
    try {
      database.executeStatement(sqlDelete);
      
      database.executeStatement(sqlInsert);
      
      queryDataSet.setQuery(new QueryDescriptor(database, "SELECT * FROM TESTTAB1"
                                               ,null, true, Load.ALL)
                                               );
      if (queryDataSet.isOpen() == true) {
        isOK = false;
        System.err.println("QueryDataSetOneRow: failed");
        throw new Exception("QueryDataSetOneRow: dataset is open, should be closed.");
      }
      
      queryDataSet.refresh();

      if (  (queryDataSet.getInt(1) == queryDataSet.getInt("INTCOL"))
         && (queryDataSet.getInt(1) == 99)
         && (queryDataSet.getString(2).equals(queryDataSet.getString("VARCHARCOL")))
         && (queryDataSet.getString(2).equals("æ÷øùâíìÆ×ØÙÂÍÌ"))
         && (queryDataSet.getString("CHARCOL").equals(queryDataSet.getString(0)))
         && (queryDataSet.getString("CHARCOL").trim().equals("ÁÂÃÄÅæçèé"))
         && (queryDataSet.getTimestamp("TIMESTAMPCOL").equals(queryDataSet.getTimestamp(3)))
         && (queryDataSet.getTimestamp("TIMESTAMPCOL").equals(new Timestamp(184575600000l)))
         && (queryDataSet.getBigDecimal("BIGDECIMALCOL").compareTo(queryDataSet.getBigDecimal(4)) == 0)
         && (queryDataSet.getBigDecimal("BIGDECIMALCOL").compareTo(new BigDecimal("500.89")) == 0)
         ) {
        isOK = true;
      }
      else {
        System.err.println("QueryDataSetOneRow: failed to match values from rows");
        
        throw new Exception();
      }
      
      queryDataSet.close();
                  
      /**
      System.out.println("queryDataSet.getInt(\"INTCOL\") = " + queryDataSet.getInt("INTCOL"));
      System.out.println("queryDataSet.getString(\"VARCHARCOL\") = " + queryDataSet.getString("VARCHARCOL"));
      System.out.println("queryDataSet.getString(\"CHARCOL\") = " + queryDataSet.getString("CHARCOL"));
      System.out.println("queryDataSet.getTimestamp(\"TIMESTAMPCOL\") = " + queryDataSet.getTimestamp("TIMESTAMPCOL"));
      System.out.println("queryDataSet.getBigDecimal(\"BIGDECIMALCOL\") = " + queryDataSet.getBigDecimal("BIGDECIMALCOL"));
      **/
            
      System.out.println("QueryDataSetOneRow: passed");
    }
    catch (Exception e) {
      isOK = false;
      e.printStackTrace();
    }
    
    return isOK;
   }
  
  public boolean testQueryDataSetManyRows() {
    boolean isOK = false;
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    String sqlDelete = "DELETE FROM TESTTAB1";
    
    // new Timestamp(184575600000l) == 1975-11-07 10:00:00.00
    
    try {
      database.executeStatement(sqlDelete);
      
      database.executeStatement("INSERT INTO TESTTAB1 ("
                     + "CHARCOL, INTCOL, VARCHARCOL, TIMESTAMPCOL"
                     + ",BIGDECIMALCOL"
                     + ") VALUES ("
                     + "'QÁÂÃÄÅæçèé'"
                     + ",0"
                     + ",'æ÷øùâíìÆ×ØÙÂÍÌ'"
                     + ",'1980-11-07 10:00:00.00'"
                     + ",900.222"
                     + ")");
      database.executeStatement("INSERT INTO TESTTAB1 ("
                     + "CHARCOL, INTCOL, VARCHARCOL, TIMESTAMPCOL"
                     + ",BIGDECIMALCOL"
                     + ") VALUES ("
                     + "'VÁÂÃÄÅæçèé'"
                     + ",1"
                     + ",'æ÷øùâíìÆ×ØÙÂÍÌ'"
                     + ",'1980-11-07 10:00:00.00'"
                     + ",900.222"
                     + ")");
      database.executeStatement("INSERT INTO TESTTAB1 ("
                     + "CHARCOL, INTCOL, VARCHARCOL, TIMESTAMPCOL"
                     + ",BIGDECIMALCOL"
                     + ") VALUES ("
                     + "'HÁÂÃÄÅæçèé'"
                     + ",2"
                     + ",'æ÷øùâíìÆ×ØÙÂÍÌ'"
                     + ",'1980-11-07 10:00:00.00'"
                     + ",900.222"
                     + ")");
      
      database.executeStatement("INSERT INTO TESTTAB1 ("
                     + "CHARCOL, INTCOL, VARCHARCOL, TIMESTAMPCOL"
                     + ",BIGDECIMALCOL"
                     + ") VALUES ("
                     + "'XÁÂÃÄÅæçèé'"
                     + ",3"
                     + ",'æ÷øùâíìÆ×ØÙÂÍÌ'"
                     + ",'1975-11-07 10:00:00.00'"
                     + ",500.89"
                     + ")");
      
      queryDataSet.setQuery(new QueryDescriptor(database, "SELECT * FROM TESTTAB1 ORDER BY INTCOL"
                                               ,null, true, Load.ALL)
                                               );

      queryDataSet.refresh();
      
      if (queryDataSet.isOpen() == false) throw new Exception("QueryDataSetManyRows: failed, isOpen() should be true.");
      
      int rows = queryDataSet.getRowCount();
      
      if (rows != 4) {
        System.err.println("QueryDataSetManyRows: failed, getRowCount() is " + rows + ". Should be 4.");
        
        throw new Exception();
      }

      for (int i=0; i<rows; i++) {
        if (  (queryDataSet.getInt(1) == queryDataSet.getInt("INTCOL"))
           && (queryDataSet.getInt(1) == i) 
           ) {
          queryDataSet.next();
        }
        else {
          System.err.println("QueryDataSetManyRows: failed to match values from rows");
        
          throw new Exception();
        }
      }
      
      // check that we have reached the end of dataset
      if ( queryDataSet.next() == true) {
        System.err.println("QueryDataSetManyRows: next() should be false");
        
        throw new Exception();
      }
      
      // let's iterate dataset once more      
      queryDataSet.first();
      for (int i=0; i<rows; i++) {
        if (  (queryDataSet.getInt(1) == queryDataSet.getInt("INTCOL"))
           && (queryDataSet.getInt(1) == i) 
           ) {
          queryDataSet.next();
        }
        else {
          System.err.println("QueryDataSetManyRows: failed to match values from rows in 2nd iteration.");
        
          throw new Exception();
        }
      }
      
      // try closing and open it again
      queryDataSet.close();
      if (queryDataSet.isOpen() == true) throw new Exception("QueryDataSetManyRows: failed, isOpen() should be false.");
      if (queryDataSet.getRowCount() != 0) throw new Exception("QueryDataSetManyRows: failed, getRowCount() should be 0.");
      
      // open it again
      queryDataSet.setQuery(new QueryDescriptor(database, "SELECT * FROM TESTTAB1 ORDER BY INTCOL"
                                               ,null, true, Load.ALL)
                                               );
      queryDataSet.refresh();
      
      // let's iterate dataset once more
      for (int i=0; i<rows; i++) {
        if (  (queryDataSet.getInt(1) == queryDataSet.getInt("INTCOL"))
           && (queryDataSet.getInt(1) == i) 
           ) {
          queryDataSet.next();
        }
        else {
          System.err.println("QueryDataSetManyRows: failed to match values from rows in 2nd iteration.");
        
          throw new Exception();
        }
      }
      
      //try goToRow() directly
      queryDataSet.goToRow(2);
      if (  (queryDataSet.getInt(1) != queryDataSet.getInt("INTCOL"))
           || (queryDataSet.getInt(1) != 2) 
         ) {
        System.err.println("QueryDataSetManyRows: failed to match values from rows in goToRow(2).");
        
        throw new Exception();
      }
      
      // test to throw exception when getString() from an INT
      try {
        queryDataSet.getString("INTCOL");
        
        System.err.println("QueryDataSetManyRows: failed to throw exception when getString() from an INT.");
        
        throw new Exception();
      }
      catch (DataSetException dse) {
      }
      
      // test to throw exception when getInt() from a String
      try {
        queryDataSet.getInt("VARCHARCOL");
        
        System.err.println("QueryDataSetManyRows: failed to throw exception when getInt() from a String.");
        
        throw new Exception();
      }
      catch (DataSetException dse) {
      }
      
      //try goToRow() directly
      if (queryDataSet.goToRow(4) == true) {
        System.err.println("QueryDataSetManyRows: failed, goToRow(4) should return false.");
        throw new Exception();
      }
      
      //try goToRow() directly
      if (queryDataSet.goToRow(3) == false) {
        System.err.println("QueryDataSetManyRows: failed, goToRow(3) should return true.");
        throw new Exception();
      }
      
      // set max rows and refetch rows from database
      queryDataSet.setMaxRows(2);
      queryDataSet.refresh();
      if (queryDataSet.goToRow(3) == true) {
        System.err.println("QueryDataSetManyRows: failed, goToRow(3) should return false.");
        throw new Exception();
      }
      // let's iterate dataset once more (3rd time)
      rows = queryDataSet.getRowCount();
      if (rows != 2) {
        System.err.println("QueryDataSetManyRows: failed, getRowCount() is " + rows + ". Should be 2.");
        
        throw new Exception();
      }
      for (int i=0; i<rows; i++) {
        if (  (queryDataSet.getInt(1) == queryDataSet.getInt("INTCOL"))
           && (queryDataSet.getInt(1) == i) 
           ) {
          queryDataSet.next();
        }
        else {
          System.err.println("QueryDataSetManyRows: failed to match values from rows in 3rd iteration.");
        
          throw new Exception();
        }
      }
      // check that we have reached the end of dataset
      if ( queryDataSet.next() == true) {
        System.err.println("QueryDataSetManyRows: next() should be false");
        
        throw new Exception();
      }
      
      isOK = true;
      System.out.println("QueryDataSetManyRows: passed");
    }
    catch (Exception e) {
      isOK = false;
      e.printStackTrace();
    }
    
    return isOK;
   }
  
  public boolean testQueryDataSetJoin() {
    boolean isOK = false;
    
    QueryDataSet queryDataSet = new QueryDataSet();

    try {
      database.executeStatement("DELETE FROM TESTTAB1");
      database.executeStatement("DELETE FROM TESTTAB2");
      
      database.executeStatement("INSERT INTO TESTTAB1 ("
                     + "CHARCOL, INTCOL, VARCHARCOL, TIMESTAMPCOL"
                     + ",BIGDECIMALCOL"
                     + ") VALUES ("
                     + "'ÁÂÃÄÅæçèé'"
                     + ",1"
                     + ",'æ÷øùâíìÆ×ØÙÂÍÌ'"
                     + ",'1975-11-07 10:00:00.00'"
                     + ",500.89"
                     + ")");
      database.executeStatement("INSERT INTO TESTTAB2 ("
                     + "PKCOL, VARCHARCOL, BLOBCOL"
                     + ") VALUES ("
                     + "1"
                     + ",'QWERTY'"
                     + ",'qwertyuiopasdfghjklzxcvbnm'"
                     + ")");
      
      queryDataSet.setQuery(new QueryDescriptor(database, 
                                                "SELECT * FROM TESTTAB1,TESTTAB2"
                                              + " WHERE INTCOL = PKCOL"
                                               ,null, true, Load.ALL));

      queryDataSet.refresh();

      if (!queryDataSet.getString("VARCHARCOL").equals("æ÷øùâíìÆ×ØÙÂÍÌ")
            || !queryDataSet.getString("VARCHARCOL1").equals("QWERTY")
         ) {
        System.err.println("QueryDataSetJoin: failed to match columns with same name");
        
        throw new Exception();
      }
      
      queryDataSet.close();
      
      isOK = true;
      System.out.println("QueryDataSetJoin: passed");
    }
    catch (Exception e) {
      isOK = false;
      e.printStackTrace();
    }
    
    return isOK;
   }
    
  public boolean testQueryDataSetUNCACHED() {
    boolean isOK = false;
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    String sqlDelete = "DELETE FROM TESTTAB1";

    try {
      database.executeStatement(sqlDelete);
      
      database.executeStatement("INSERT INTO TESTTAB1 ("
                     + "CHARCOL, INTCOL, VARCHARCOL, TIMESTAMPCOL"
                     + ",BIGDECIMALCOL"
                     + ") VALUES ("
                     + "'QÁÂÃÄÅæçèé'"
                     + ",0"
                     + ",'æ÷øùâíìÆ×ØÙÂÍÌ'"
                     + ",'1980-11-07 10:00:00.00'"
                     + ",900.222"
                     + ")");
      database.executeStatement("INSERT INTO TESTTAB1 ("
                     + "CHARCOL, INTCOL, VARCHARCOL, TIMESTAMPCOL"
                     + ",BIGDECIMALCOL"
                     + ") VALUES ("
                     + "'VÁÂÃÄÅæçèé'"
                     + ",1"
                     + ",'æ÷øùâíìÆ×ØÙÂÍÌ'"
                     + ",'1980-11-07 10:00:00.00'"
                     + ",900.222"
                     + ")");
      database.executeStatement("INSERT INTO TESTTAB1 ("
                     + "CHARCOL, INTCOL, VARCHARCOL, TIMESTAMPCOL"
                     + ",BIGDECIMALCOL"
                     + ") VALUES ("
                     + "'HÁÂÃÄÅæçèé'"
                     + ",2"
                     + ",'æ÷øùâíìÆ×ØÙÂÍÌ'"
                     + ",'1980-11-07 10:00:00.00'"
                     + ",900.222"
                     + ")");
      
      database.executeStatement("INSERT INTO TESTTAB1 ("
                     + "CHARCOL, INTCOL, VARCHARCOL, TIMESTAMPCOL"
                     + ",BIGDECIMALCOL"
                     + ") VALUES ("
                     + "'XÁÂÃÄÅæçèé'"
                     + ",3"
                     + ",'æ÷øùâíìÆ×ØÙÂÍÌ'"
                     + ",'1975-11-07 10:00:00.00'"
                     + ",500.89"
                     + ")");
      
      queryDataSet.setQuery(new QueryDescriptor(database, "SELECT * FROM TESTTAB1 ORDER BY INTCOL"
                                               ,null, true, Load.UNCACHED)
                                               );

      queryDataSet.refresh();
      
      if (queryDataSet.isOpen() == false) throw new Exception("QueryDataSetUNCACHED: failed, isOpen() should be true.");
      
      int rows = queryDataSet.getRowCount();
      
      if (rows != 1) {
        System.err.println("QueryDataSetUNCACHED: failed, getRowCount() is " + rows + ". Should be 1.");
        
        throw new Exception();
      }

      for (int i=0; i<4; i++) {
        if (  (queryDataSet.getInt(1) == queryDataSet.getInt("INTCOL"))
           && (queryDataSet.getInt(1) == i) 
           ) {
          queryDataSet.next();
        }
        else {
          System.err.println("QueryDataSetUNCACHED: failed to match values from rows");
        
          throw new Exception();
        }
      }
      
      // check that we have reached the end of dataset
      if ( queryDataSet.next() == true) {
        System.err.println("QueryDataSetUNCACHED: next() should be false");
        
        throw new Exception();
      }
      
      queryDataSet.close();
      
      isOK = true;
      System.out.println("QueryDataSetUNCACHED: passed");
    }
    catch (Exception e) {
      isOK = false;
      e.printStackTrace();
    }
    
    return isOK;
  }
  
  public boolean testDataRow() {
    boolean isOK = false;
    
    QueryDataSet queryDataSet = new QueryDataSet();

    try {
      database.executeStatement("DELETE FROM TESTTAB1");
      database.executeStatement("DELETE FROM TESTTAB2");
      
      database.executeStatement("INSERT INTO TESTTAB1 ("
                     + "CHARCOL, INTCOL, VARCHARCOL, TIMESTAMPCOL"
                     + ",BIGDECIMALCOL"
                     + ") VALUES ("
                     + "'ÁÂÃÄÅæçèé'"
                     + ",1"
                     + ",'æ÷øùâíìÆ×ØÙÂÍÌ'"
                     + ",'1975-11-07 10:00:00.00'"
                     + ",500.89"
                     + ")");
      database.executeStatement("INSERT INTO TESTTAB2 ("
                     + "PKCOL, VARCHARCOL, BLOBCOL"
                     + ") VALUES ("
                     + "1"
                     + ",'QWERTY'"
                     + ",'qwertyuiopasdfghjklzxcvbnm'"
                     + ")");
      
      queryDataSet.setQuery(new QueryDescriptor(database, 
                                                "SELECT * FROM TESTTAB1,TESTTAB2"
                                              + " WHERE INTCOL = PKCOL"
                                               ,null, true, Load.ALL));

      queryDataSet.refresh();

      DataRow aDataRow = new DataRow(queryDataSet, "BIGDECIMALCOL");
      aDataRow.setBigDecimal("BIGDECIMALCOL", new BigDecimal("500.89"));
      
      if (queryDataSet.getBigDecimal("BIGDECIMALCOL").compareTo(aDataRow.getBigDecimal("BIGDECIMALCOL")) != 0) {
        System.err.println("DataRow: failed to match DataRow BigDecimal value.");
        
        throw new Exception();
      }
      
      aDataRow = new DataRow(queryDataSet, new String[] {"VARCHARCOL", "INTCOL"});
      aDataRow.setInt("INTCOL", 555);
      aDataRow.setString("VARCHARCOL", "jingle!!!");
      
      if (aDataRow.getInt("INTCOL") != 555) {
        System.err.println("DataRow: failed to match DataRow int value.");
        
        throw new Exception();
      }
      
      if (!aDataRow.getString("VARCHARCOL").equals("jingle!!!")) {
        System.err.println("DataRow: failed to match DataRow String value.");
        
        throw new Exception();
      }
      
      queryDataSet.close();
      
      isOK = true;
      System.out.println("DataRow: passed");
    }
    catch (Exception e) {
      isOK = false;
      e.printStackTrace();
    }
    
    return isOK;
   }
 
  public boolean testQueryDataSetNullValues() {
    boolean isOK = false;
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    String sqlDelete = "DELETE FROM TESTTAB1";
    
    String sqlInsert = "INSERT INTO TESTTAB1 ("
                     + "CHARCOL, INTCOL, VARCHARCOL, TIMESTAMPCOL"
                     + ",BIGDECIMALCOL"
                     + ") VALUES ("
                     + "'ÁÂÃÄÅæçèé'"
                     + ",null"
                     + ",null"
                     + ",'1975-11-07 10:00:00.00'"
                     + ",500.89"
                     + ")";
        
    try {
      database.executeStatement(sqlDelete);
      
      database.executeStatement(sqlInsert);
      
      queryDataSet.setQuery(new QueryDescriptor(database, "SELECT * FROM TESTTAB1"
                                               ,null, true, Load.ALL)
                                               );
      if (queryDataSet.isOpen() == true) {
        isOK = false;
        System.err.println("QueryDataSetNullValues: failed");
        throw new Exception("QueryDataSetNullValues: dataset is open, should be closed.");
      }
      
      queryDataSet.refresh();
            
      if (  (queryDataSet.getInt(1) == queryDataSet.getInt("INTCOL"))
         && (queryDataSet.isNull(1) == true)
         && (queryDataSet.isNull("INTCOL") == true)
         && (queryDataSet.getString(2) == queryDataSet.format("VARCHARCOL"))
         && (queryDataSet.getString(2) == null)
         && (queryDataSet.isNull(2) == true)
         && (queryDataSet.format("CHARCOL").equals(queryDataSet.getString(0)))
         && (queryDataSet.format("CHARCOL").trim().equals("ÁÂÃÄÅæçèé"))
         && (queryDataSet.isNull("CHARCOL") == false)
         && (queryDataSet.getTimestamp("TIMESTAMPCOL").equals(queryDataSet.getTimestamp(3)))
         && (queryDataSet.getTimestamp("TIMESTAMPCOL").equals(new Timestamp(184575600000l)))
         && (queryDataSet.isNull("TIMESTAMPCOL") == false)
         && (queryDataSet.getBigDecimal("BIGDECIMALCOL").compareTo(queryDataSet.getBigDecimal(4)) == 0)
         && (queryDataSet.getBigDecimal("BIGDECIMALCOL").compareTo(new BigDecimal("500.89")) == 0)
         ) {
        isOK = true;
      }
      else {
        System.err.println("QueryDataSetNullValues: failed to match values from rows");
        
        throw new Exception();
      }
      
      /**
      System.out.println("queryDataSet.getInt(\"INTCOL\") = " + queryDataSet.getInt("INTCOL"));
      System.out.println("queryDataSet.format(\"INTCOL\") = " + queryDataSet.format("INTCOL"));
      
      System.out.println("queryDataSet.getString(\"VARCHARCOL\") = " + queryDataSet.getString("VARCHARCOL"));      
      System.out.println("queryDataSet.format(\"VARCHARCOL\") = " + queryDataSet.format("VARCHARCOL"));
      
      System.out.println("queryDataSet.format(\"TIMESTAMPCOL\") = " + queryDataSet.format("TIMESTAMPCOL"));
      
      System.out.println("queryDataSet.getInt(\"INTCOL\") = " + queryDataSet.getInt("INTCOL"));
      System.out.println("queryDataSet.getString(\"VARCHARCOL\") = " + queryDataSet.getString("VARCHARCOL"));
      System.out.println("queryDataSet.getString(\"CHARCOL\") = " + queryDataSet.getString("CHARCOL"));
      System.out.println("queryDataSet.getTimestamp(\"TIMESTAMPCOL\") = " + queryDataSet.getTimestamp("TIMESTAMPCOL"));
      System.out.println("queryDataSet.getBigDecimal(\"BIGDECIMALCOL\") = " + queryDataSet.getBigDecimal("BIGDECIMALCOL"));
      **/

      queryDataSet.close();
      
      System.out.println("QueryDataSetNullValues: passed");
    }
    catch (Exception e) {
      isOK = false;
      e.printStackTrace();
    }
    
    return isOK;
   }
 
  public boolean testQueryDataSetLocate() {
    boolean isOK = false;
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    String sqlDelete = "DELETE FROM TESTTAB1";

    try {
      database.executeStatement(sqlDelete);
      
      database.executeStatement("INSERT INTO TESTTAB1 ("
                     + "CHARCOL, INTCOL, VARCHARCOL, TIMESTAMPCOL"
                     + ",BIGDECIMALCOL"
                     + ") VALUES ("
                     + "'QÁÂÃÄÅæçèé'"
                     + ",0"
                     + ",'cvvvcvderâíìÆ×ØÙÂÍÌfgfgfg'"
                     + ",'1980-11-07 10:00:00.00'"
                     + ",900.222"
                     + ")");
      database.executeStatement("INSERT INTO TESTTAB1 ("
                     + "CHARCOL, INTCOL, VARCHARCOL, TIMESTAMPCOL"
                     + ",BIGDECIMALCOL"
                     + ") VALUES ("
                     + "'VÁÂÃÄÅæçèé'"
                     + ",1"
                     + ",'æ÷øùâíìÆ×ØÙÂÍÌ'"
                     + ",'1980-11-07 10:00:00.00'"
                     + ",900.222"
                     + ")");
      database.executeStatement("INSERT INTO TESTTAB1 ("
                     + "CHARCOL, INTCOL, VARCHARCOL, TIMESTAMPCOL"
                     + ",BIGDECIMALCOL"
                     + ") VALUES ("
                     + "'HÁÂÃÄÅæçèé'"
                     + ",2"
                     + ",'æ÷øÙâíìÆ×ØÙÂÍÌ'"
                     + ",'1980-11-07 10:00:00.00'"
                     + ",900.222"
                     + ")");
      
      database.executeStatement("INSERT INTO TESTTAB1 ("
                     + "CHARCOL, INTCOL, VARCHARCOL, TIMESTAMPCOL"
                     + ",BIGDECIMALCOL"
                     + ") VALUES ("
                     + "'XÁÂÃÄÅæçèé'"
                     + ",3"
                     + ",'æ÷øùâíìÆ×ØÙÂÍÌ'"
                     + ",'1975-11-07 10:00:00.00'"
                     + ",500.89"
                     + ")");
      
      queryDataSet.setQuery(new QueryDescriptor(database, "SELECT * FROM TESTTAB1 ORDER BY INTCOL"
                                               ,null, true, Load.ALL)
                                               );

      queryDataSet.refresh();
      
      if (queryDataSet.isOpen() == false) throw new Exception("QueryDataSetLocate: failed, isOpen() should be true.");
      
      int rows = queryDataSet.getRowCount();
      
      if (rows != 4) {
        System.err.println("QueryDataSetLocate: failed, getRowCount() is " + rows + ". Should be 1.");
        
        throw new Exception();
      }

      DataRow aDataRow = new DataRow(queryDataSet, "VARCHARCOL");
      aDataRow.setString("VARCHARCOL", "æ÷øùâíìÆ×ØÙÂÍÌ");
      
      if ( (queryDataSet.locate(aDataRow, Locate.FIRST) == false)
         ||(queryDataSet.getRow() != 1)
         ) {
        System.err.println("QueryDataSetLocate: failed, Locate.FIRST for String column failed.");
        
        throw new Exception();
      }
      // find the same row second time
      if ( (queryDataSet.locate(aDataRow, Locate.FIRST) == false)
         ||(queryDataSet.getRow() != 1)
         ) {
        System.err.println("QueryDataSetLocate: failed, Locate.FIRST for String column failed.");
        
        throw new Exception();
      }
      
      // use Locate.NEXT this time
      if ( (queryDataSet.locate(aDataRow, Locate.NEXT) == false)
         ||(queryDataSet.getRow() != 3)
         ) {
        System.err.println("QueryDataSetLocate: failed, Locate.NEXT for String column failed.");
        
        throw new Exception();
      }
      // use Locate.NEXT second time, should return false
      if ( (queryDataSet.locate(aDataRow, Locate.NEXT) == true)
         ||(queryDataSet.getRow() != 3)
         ) {
        System.err.println("QueryDataSetLocate: failed, Locate.NEXT for String column failed.");
        
        throw new Exception();
      }
      
      // use Locate.FIRST + Locate.CASE_INSENSITIVE
      if ( (queryDataSet.locate(aDataRow, Locate.FIRST | Locate.CASE_INSENSITIVE) == false)
         ||(queryDataSet.getRow() != 1)
         ) {
        System.err.println("QueryDataSetLocate: failed, 'Locate.FIRST | Locate.CASE_INSENSITIVE' for String column failed.");
        
        throw new Exception();
      }
      // use Locate.NEXT + Locate.CASE_INSENSITIVE, second time
      if ( (queryDataSet.locate(aDataRow, Locate.NEXT | Locate.CASE_INSENSITIVE) == false)
         ||(queryDataSet.getRow() != 2)
         ) {
        System.err.println("QueryDataSetLocate: failed, 'Locate.NEXT | Locate.CASE_INSENSITIVE' for String column failed.");
        
        throw new Exception();
      }
      // use Locate.NEXT + Locate.CASE_INSENSITIVE, third time
      if ( (queryDataSet.locate(aDataRow, Locate.NEXT | Locate.CASE_INSENSITIVE) == false)
         ||(queryDataSet.getRow() != 3)
         ) {
        System.err.println("QueryDataSetLocate: failed, 'Locate.NEXT | Locate.CASE_INSENSITIVE' for String column failed.");
        
        throw new Exception();
      }
      
      aDataRow = new DataRow(queryDataSet, new String[] {"VARCHARCOL", "INTCOL"});
      aDataRow.setString("VARCHARCOL", "æ÷øùâíìÆ×ØÙÂÍÌ");
      aDataRow.setInt("INTCOL", 1);
      
      if ( (queryDataSet.locate(aDataRow, Locate.FIRST) == false)
         ||(queryDataSet.getRow() != 1)
         ) {
        System.err.println("QueryDataSetLocate: failed, Locate.FIRST for String & int column failed.");
        
        throw new Exception();
      }
      if ( (queryDataSet.locate(aDataRow, Locate.NEXT) == true)
         ||(queryDataSet.getRow() != 1)
         ) {
        System.err.println("QueryDataSetLocate: failed, Locate.NEXT for String & int column failed.");
        
        throw new Exception();
      }
      
      queryDataSet.close();
      
      isOK = true;
      System.out.println("QueryDataSetLocate: passed");
    }
    catch (Exception e) {
      isOK = false;
      e.printStackTrace();
    }
    
    return isOK;
  }
    
  public boolean testQueryDataSetLookup() {
    boolean isOK = false;
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    String sqlDelete = "DELETE FROM TESTTAB1";

    try {
      database.executeStatement(sqlDelete);
      
      database.executeStatement("INSERT INTO TESTTAB1 ("
                     + "CHARCOL, INTCOL, VARCHARCOL, TIMESTAMPCOL"
                     + ",BIGDECIMALCOL"
                     + ") VALUES ("
                     + "'QÁÂÃÄÅæçèé'"
                     + ",0"
                     + ",'cvvvcvderâíìÆ×ØÙÂÍÌfgfgfg'"
                     + ",'1980-11-07 10:00:00.00'"
                     + ",900.222"
                     + ")");
      database.executeStatement("INSERT INTO TESTTAB1 ("
                     + "CHARCOL, INTCOL, VARCHARCOL, TIMESTAMPCOL"
                     + ",BIGDECIMALCOL"
                     + ") VALUES ("
                     + "'VÁÂÃÄÅæçèé'"
                     + ",1"
                     + ",'ÁÂÃäåæçè'"
                     + ",'1975-11-07 10:00:00.00'"
                     + ",900.222"
                     + ")");
      database.executeStatement("INSERT INTO TESTTAB1 ("
                     + "CHARCOL, INTCOL, VARCHARCOL, TIMESTAMPCOL"
                     + ",BIGDECIMALCOL"
                     + ") VALUES ("
                     + "'HÁÂÃÄÅæçèé'"
                     + ",2"
                     + ",'æ÷øÙâíìÆ×ØÙÂÍÌ'"
                     + ",'1980-11-07 10:00:00.00'"
                     + ",900.222"
                     + ")");
      
      database.executeStatement("INSERT INTO TESTTAB1 ("
                     + "CHARCOL, INTCOL, VARCHARCOL, TIMESTAMPCOL"
                     + ",BIGDECIMALCOL"
                     + ") VALUES ("
                     + "'XÁÂÃÄÅæçèé'"
                     + ",3"
                     + ",'æ÷øùâíìÆ×ØÙÂÍÌ'"
                     + ",'1975-11-07 10:00:00.00'"
                     + ",500.89"
                     + ")");
      
      queryDataSet.setQuery(new QueryDescriptor(database, "SELECT * FROM TESTTAB1 ORDER BY INTCOL"
                                               ,null, true, Load.ALL)
                                               );

      queryDataSet.refresh();
      
      if (queryDataSet.isOpen() == false) throw new Exception("QueryDataSetLookup: failed, isOpen() should be true.");
      
      int rows = queryDataSet.getRowCount();
      
      if (rows != 4) {
        System.err.println("QueryDataSetLookup: failed, getRowCount() is " + rows + ". Should be 1.");
        
        throw new Exception();
      }

      DataRow locateRow = new DataRow(queryDataSet, "VARCHARCOL");
      locateRow.setString("VARCHARCOL", "ÁÂÃäåæçè");

      DataRow resultRow = new DataRow(queryDataSet, new String[] {"VARCHARCOL", "CHARCOL", "INTCOL", "TIMESTAMPCOL", "BIGDECIMALCOL"});

      if ( (queryDataSet.lookup(locateRow, resultRow, Locate.FIRST) == false) 
         ||(!resultRow.getString("VARCHARCOL").equals("ÁÂÃäåæçè"))
         ||(!resultRow.getString("VARCHARCOL").equals(resultRow.getString(0)))
         ||(!resultRow.getString("CHARCOL").trim().equals("VÁÂÃÄÅæçèé"))
         ||(!resultRow.getString("CHARCOL").equals(resultRow.getString(1)))
         ||(resultRow.getInt("INTCOL") != 1)
         ||(resultRow.getInt("INTCOL") != resultRow.getInt(2))
         ||(!resultRow.getTimestamp("TIMESTAMPCOL").equals(new Timestamp(184575600000l)))
         ||(queryDataSet.getBigDecimal("BIGDECIMALCOL").compareTo(new BigDecimal("900.222")) != 0)
         ) {
           
        System.err.println("QueryDataSetLookup: failed, Locate.FIRST for String column failed.");

        throw new Exception();
      }
      
      queryDataSet.close();
      
      isOK = true;
      System.out.println("QueryDataSetLookup: passed");
    }
    catch (Exception e) {
      isOK = false;
      e.printStackTrace();
    }
    
    return isOK;
  }
    
  public static void main(String[] args) {
    new TestDatabase();
  }
  
  private String _jdbcInterbaseDriver = "org.firebirdsql.jdbc.FBDriver";
  private String _jdbcInterbaseURL = "jdbc:firebirdsql:amorgos.softways.lan/3050:/opt/interbase.data/TEST.GDB";

  private String _jdbcSybaseDriver = "com.sybase.jdbc2.jdbc.SybDriver";
  private String _jdbcSybaseURL = "jdbc:sybase:Tds:naxos.softways.lan:5000/testbase";
  
  private Properties _propertiesInfo = null;
  
  private Database database = null;
  
  private String _sqlDelete = "DELETE FROM TESTTAB1";
  private String _sqlInsert = "INSERT INTO TESTTAB1 ("
                            + "CHARCOL, INTCOL, VARCHARCOL, TIMESTAMPCOL"
                            + ",BIGDECIMALCOL"
                            + ") VALUES ("
                            + "'ÁÂÃÄÅæçèé'"
                            + ",99"
                            + ",'æ÷øùâíìÆ×ØÙÂÍÌ'"
                            + ",'1975-11-07 10:00:00.00'"
                            + ",500.89"
                            + ")";
}