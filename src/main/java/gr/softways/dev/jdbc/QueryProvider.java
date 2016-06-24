/*
 * QueryProvider.java
 *
 * Created on 14 Φεβρουάριος 2003, 2:12 μμ
 */

package gr.softways.dev.jdbc;

import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.sql.SQLException;

/**
 * The QueryProvider component is used to provide data to a DataSet 
 * by running a query through JDBC.
 *
 * @author  minotauros
 */
public class QueryProvider extends Provider {
  
  public QueryProvider() {
  }
  
  public void setQueryDescriptor(QueryDescriptor aQueryDescriptor) {
    _aQueryDescriptor = aQueryDescriptor;
  }
  
  /**
   * 
   */
  public void provideData(DataSet aDataSet) {
    Database database = _aQueryDescriptor.getDatabase();

    try {
      _aStatement = database.createStatement();
      _isStatementOpen = true;

      if (aDataSet.getMaxRows() > 0) _aStatement.setMaxRows(aDataSet.getMaxRows());

      _aResultSet = _aStatement.executeQuery(_aQueryDescriptor.getQuery());
      _isResultSetOpen = true;
      
      _aResultSetMetaData = _aResultSet.getMetaData();
      int columnCount = _aResultSetMetaData.getColumnCount();
      ColumnList aColumnList = new ColumnList(columnCount);
      for (int i=1; i<=columnCount; i++) {
        aColumnList.addColumn(_aResultSetMetaData.getColumnName(i),
                              RowVariant.mapSQLtoDataType(_aResultSetMetaData.getColumnType(i)),
                              i-1);
      }
      aDataSet.setColumnList(aColumnList);

      if (_aQueryDescriptor.getLoadOption() == Load.UNCACHED) {
        if (_aResultSet.next() == true) {
          addOneRow(aDataSet, aColumnList);
          _hasMoreData = true;
        }
        else {
          close();
        }
      }
      else {
        while (_aResultSet.next() == true) {
          addOneRow(aDataSet, aColumnList);
        }
        close();
      }
    }
    catch (Exception exception) {
      close();
      DataSetException.throwException(exception);
    }
  }
  
  public boolean provideMoreData(DataSet aDataSet) {
    boolean providedMoreData = false;
    
    try {
      if (_aResultSet.next() == true) {
        addOneRow(aDataSet, aDataSet.getColumnList());
        providedMoreData = true;
      }
      else {
        close();
        providedMoreData = false;
      }
    }
    catch (Exception exception) {
      close();
      providedMoreData = false;
      
      DataSetException.throwException(exception);
    }
    
    return providedMoreData;
  }
  
  private void addOneRow(DataSet aDataSet, ColumnList aColumnList) {
                           
    int columnCount = aColumnList.getColumnCount();
    
    ReadWriteRow aReadWriteRow = new ReadWriteRow(columnCount);
    aReadWriteRow.setColumnList(aColumnList);
    
    int sqlType = 0;
    
    try {
      for (int i=1; i<=columnCount; i++) {
        sqlType = _aResultSetMetaData.getColumnType(i);
      
          switch (sqlType) {
            case Types.CHAR : String s = _aResultSet.getString(i);
                              if (s != null) s = trimLeadSpaces(s);
                              aReadWriteRow.setString(i-1, s);
                              break;
            case Types.VARCHAR : aReadWriteRow.setString(i-1, _aResultSet.getString(i));
                                 break;
            case Types.NUMERIC : aReadWriteRow.setBigDecimal(i-1, _aResultSet.getBigDecimal(i));
                                 break;
            case Types.INTEGER : aReadWriteRow.setInt(i-1, _aResultSet.getInt(i));
                                 break;
            case Types.BIGINT : aReadWriteRow.setInt(i-1, _aResultSet.getInt(i));
                                break;
            case Types.TIMESTAMP : aReadWriteRow.setTimestamp(i-1, _aResultSet.getTimestamp(i));
                                   break;
            case Types.LONGVARCHAR : aReadWriteRow.setString(i-1, _aResultSet.getString(i));
                                     break;
            case Types.CLOB : aReadWriteRow.setString(i-1, _aResultSet.getString(i));
                               break;
            case Types.DECIMAL : aReadWriteRow.setBigDecimal(i-1, _aResultSet.getBigDecimal(i));
                                 break;
            case Types.DOUBLE : aReadWriteRow.setDouble(i-1, _aResultSet.getDouble(i));
                                break;
            case Types.FLOAT : aReadWriteRow.setDouble(i-1, _aResultSet.getDouble(i));
                               break;
            case Types.REAL : aReadWriteRow.setFloat(i-1, _aResultSet.getFloat(i));
                              break;
            case Types.SMALLINT : aReadWriteRow.setShort(i-1, _aResultSet.getShort(i));
                                  break;
            case Types.DATE : aReadWriteRow.setDate(i-1, _aResultSet.getDate(i));
                              break;
            case Types.TIME : aReadWriteRow.setTime(i-1, _aResultSet.getTime(i));
                              break;
            case Types.TINYINT : aReadWriteRow.setByte(i-1, _aResultSet.getByte(i));
                                 break;
            case Types.BOOLEAN : aReadWriteRow.setBoolean(i-1, _aResultSet.getBoolean(i));
                                 break;
            default: DataSetException.unrecognizedColumnType(_aResultSetMetaData.getColumnName(i), 
                                                             _aResultSetMetaData.getColumnType(i));
          }
          aReadWriteRow.setIsNull(i-1, _aResultSet.wasNull());
        }
        aDataSet.addOneRow(aReadWriteRow);
    }
    catch (Exception exception) {
      DataSetException.throwException(exception);
    }
  }
  
  private static String trimLeadSpaces(String s) {
    if (s == null) return s;
    
    int i = 0;
    for (i=s.length(); i>0 && s.charAt(i - 1) == ' '; i--);
    
    return i >= s.length() ? s : s.substring(0, i);
  }
  
  public void close() {
    if (_isResultSetOpen == true) {
      try {
        _aResultSet.close();
      }
      catch (SQLException sqlException1) {
      }
      finally {
       _aResultSet = null;
       _isResultSetOpen = false;
      }
    }
    
    if (_isStatementOpen == true) {
      try {
        _aStatement.close();
      }
      catch (SQLException sqlException1) {
      }
      finally {
        _aStatement = null;
        _isStatementOpen = false;
      }
    }
    
    _hasMoreData = false;
  }
    
  public boolean hasMoreData() {
    return _hasMoreData;
  }
  
  private boolean _hasMoreData = false;

  private boolean _isStatementOpen = false;
  private boolean _isResultSetOpen = false;
  
  private Statement _aStatement;
  private ResultSet _aResultSet;
  private ResultSetMetaData _aResultSetMetaData;
  
  private QueryDescriptor _aQueryDescriptor;
}