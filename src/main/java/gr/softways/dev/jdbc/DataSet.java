/*
 * DataSet.java
 *
 * Created on 6 Φεβρουάριος 2003, 11:26 πμ
 */

package gr.softways.dev.jdbc;

import java.math.BigDecimal;
import gr.softways.dev.util.sort.Comparable;
import gr.softways.dev.util.sort.DataSetSorter;
import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;

/**
 * The DataSet class is an abstract class that provides basic view, 
 * and cursor functionality for access to rows of data. 
 * It supports the concept of a current row position which allows 
 * for navigation of the data in the DataSet.
 *
 * @author  minotauros
 */
public abstract class DataSet {
  
  public DataSet() {
  }
  
  /**
   * Moves the cursor to the given row number. The first row is 0, 
   * the second is 1 and so on.
   *
   * @param row the number of the row to which the cursor should move.
   */
  public boolean goToRow(int row) {
    if (isOpen() == false) DataSetException.openDataSetFirst();
    
    if (row < 0) {
      _inBounds = false;
      
      return false;
    }
    
    if ( (row >= getRowCount())
            && (_aProvider != null && _aProvider.hasMoreData())
       ) {
      // replace existing row with another from ResultSet
      _rowCount = 0;
      _currentRowIndex = 0;
      
      _inBounds = _aProvider.provideMoreData(this);
      
      return true;
    }
    else if (row >= getRowCount()) {
      _inBounds = false;
      
      return false;
    }
    else {
      _currentRowIndex = row;
      _inBounds = true;
      
      return true;
    }
  }
  
  public boolean first() {
    return goToRow(0);
  }
  
  public boolean last() {
    return goToRow( getRowCount()-1 );
  }
  
  public boolean next() {
    return goToRow(_currentRowIndex + 1);
  }
  
  public boolean prior() {
    return goToRow(_currentRowIndex - 1);
  }
  
  public boolean isEmpty() {
    return (getRowCount() <= 0);
  }
  
  /**
   * The value of inBounds is determined by the result of the 
   * last execution of goToRow(). If it was successfull then returns
   * true else returns false.
   */
  public boolean inBounds() {
    return _inBounds;
  }
  
  public String format(int ordinal) {
    return _storage[_currentRowIndex].format(ordinal);
  }
  public String format(String columnName) {
    return _storage[_currentRowIndex].format(columnName);
  }
  
  public String getString(int ordinal) {
    return _storage[_currentRowIndex].getString(ordinal);
  }
  public String getString(String columnName) {
    return _storage[_currentRowIndex].getString(columnName);
  }
  
  public int getInt(int ordinal) {
    return _storage[_currentRowIndex].getInt(ordinal);
  }
  public int getInt(String columnName) {
    return _storage[_currentRowIndex].getInt(columnName);
  }
  
  public BigDecimal getBigDecimal(int ordinal) {
    return _storage[_currentRowIndex].getBigDecimal(ordinal);
  }
  public BigDecimal getBigDecimal(String columnName) {
    return _storage[_currentRowIndex].getBigDecimal(columnName);
  }
  
  public Timestamp getTimestamp(int ordinal) {
    return _storage[_currentRowIndex].getTimestamp(ordinal);
  }
  public Timestamp getTimestamp(String columnName) {
    return _storage[_currentRowIndex].getTimestamp(columnName);
  }
  
  public double getDouble(int ordinal) {
    return _storage[_currentRowIndex].getDouble(ordinal);
  }
  public double getDouble(String columnName) {
    return _storage[_currentRowIndex].getDouble(columnName);
  }
  
  public float getFloat(int ordinal) {
    return _storage[_currentRowIndex].getFloat(ordinal);
  }
  public float getFloat(String columnName) {
    return _storage[_currentRowIndex].getFloat(columnName);
  }
  
  public short getShort(int ordinal) {
    return _storage[_currentRowIndex].getShort(ordinal);
  }
  public short getShort(String columnName) {
    return _storage[_currentRowIndex].getShort(columnName);
  }
  
  public Date getDate(int ordinal) {
    return _storage[_currentRowIndex].getDate(ordinal);
  }
  public Date getDate(String columnName) {
    return _storage[_currentRowIndex].getDate(columnName);
  }
  
  public Time getTime(int ordinal) {
    return _storage[_currentRowIndex].getTime(ordinal);
  }
  public Time getTime(String columnName) {
    return _storage[_currentRowIndex].getTime(columnName);
  }
  
  public byte getByte(int ordinal) {
    return _storage[_currentRowIndex].getByte(ordinal);
  }
  public byte getByte(String columnName) {
    return _storage[_currentRowIndex].getByte(columnName);
  }
  
  public boolean getBoolean(int ordinal) {
    return _storage[_currentRowIndex].getBoolean(ordinal);
  }
  public boolean getBoolean(String columnName) {
    return _storage[_currentRowIndex].getBoolean(columnName);
  }
  
  public boolean isNull(int ordinal) {
    return _storage[_currentRowIndex].isNull(ordinal);
  }
  public boolean isNull(String columnName) {
    return _storage[_currentRowIndex].isNull(columnName);
  }
  
 
  /**
   * Populate the DataSet. If the DataSet is open, 
   * it will close the DataSet, execute the query, then re-open the DataSet.
   * Initially the cursor is positioned at the first row.
   *
   */
  public void refresh() {
    if (_aProvider == null) DataSetException.needProvider();
    
    if (isOpen() == true) close();
    
    if (getMaxRows() > 0) {
      _storageCapacity = getMaxRows();
    }

    _storage = new ReadWriteRow[_storageCapacity];
        
    _aProvider.provideData(this);
    
    _isOpen = true;
    
    first();
  }
  
  public void addOneRow(ReadWriteRow aReadWriteRow) {
    ReadWriteRow[] _tmpStorage;
    
    if (_rowCount >= _storageCapacity) {
      _storageCapacity = _rowCount + _storageCapacityIncrement;
      
     _tmpStorage = new ReadWriteRow[_storageCapacity];
     
     System.arraycopy(_storage,0,_tmpStorage,0,_rowCount);
     
     _storage = _tmpStorage;
     _tmpStorage = null;
    }
        
    _storage[_rowCount] = aReadWriteRow;
    _rowCount++;
  }

  public final ColumnList getColumnList() {
    return _aColumnList;
  }
  public final void setColumnList(ColumnList aColumnList) {
    _aColumnList = aColumnList;
  }
  
  public void setProvider(Provider aProvider) {
    _aProvider = aProvider;
  }
  
  public void setSort(String[] columnNames) {
    setSort(columnNames, "ASC");
  }
  
  public void setSort(String[] columnNames, String sortOrder) {
    if (columnNames == null) 
      return;
    DataSetSorter sorter = new DataSetSorter();
    sorter.sort(_storage, getRowCount(), columnNames, sortOrder);
    first();
  }
  
  
  /** 
   * Set the maximum of rows that can be initially loaded to the DataSet. 
   * This property defaults to 0 which indicates that there is no limit 
   * to the number of rows that can be initially loaded into a DataSet.
   */
  public void setMaxRows(int maxRows) {
    _maxRows = maxRows;
  }
  
  /** 
   * Returns current row position. First row is 0, second 1 etc.
   */
  public final int getRow() {
    return _currentRowIndex;
  }
  
  /** 
   * Return the maximum of rows that can be loaded to the DataSet.
   */
  public int getMaxRows() {
    return _maxRows;
  }
  
  /**
   * Return how many rows are loaded in the DataSet.
   */
  public int getRowCount() {
    return _rowCount;
  }
  
  public final boolean isOpen() {
    return _isOpen;
  }
 
  /**
   * This method returns true if the DataSet is closed by executing this 
   * method and false if the DataSet does not need closing 
   * (for example, it is already closed). 
   * <p>
   * DataSet's resources are released (Any rows loaded in the DataSet, 
   * JDBC resources from a QueryProvider etc.).
   *
   */
  public final boolean close() {
    if (isOpen() == false) return false;

    _storage = null;
    _storageCapacity = 512;
    
    _currentRowIndex = 0;
    
    _rowCount = 0;

    setMaxRows(0);
    
    if (_aProvider != null) {
      _aProvider.close();
    }
    
    _isOpen = false;
    
    return true;
  }
  
  /**
   * Locates the row of data with the specified row of values and moves the 
   * current row position to that row. The locate operation includes all 
   * columns of the ReadRow, to limit the locate to specific columns 
   * of interest, use a scoped DataRow. If the row is scoped 
   * to a specific set of columns, only those columns are used for the locate. 
   *
   */
  public final boolean locate(DataRow aLocateDataRow, int locateOptions) {
    int rowIndex = -1;
    int fromRowIndex = -1;
    
    boolean found = false;
    
    boolean caseInsensitive = false;
    
    if ( (locateOptions & Locate.FIRST) == Locate.FIRST) {
      fromRowIndex = 0;
    }
    if ( (locateOptions & Locate.NEXT) == Locate.NEXT) {
      fromRowIndex = _currentRowIndex+1;
    }
    if ( (locateOptions & Locate.CASE_INSENSITIVE) == Locate.CASE_INSENSITIVE) {
      caseInsensitive = true;
    }   
    
    rowIndex = findRowIndex(aLocateDataRow, fromRowIndex, caseInsensitive);
    
    if (rowIndex >= 0) {
      goToRow(rowIndex);
      found = true;
    }
    else {
      found = false;
    }

    return found;
  }
  
  /**
   * Performs a lookup for the row with the specified values. If a match is 
   * found, this method returns the data from the matching row as resultRow, 
   * and returns true but does not navigate to the matching row. 
   * If no match is found, this method returns false. 
   * This method includes all columns of the ReadRow, to limit the lookup 
   * to specific columns, use a "scoped" DataRow that includes only 
   * the columns of interest.
   *
   */
  public final boolean lookup(DataRow aLocateDataRow, DataRow aResultRow, 
                              int locateOptions) {
    int rowIndex = -1;
    int fromRowIndex = -1;
    
    boolean found = false;
            
    boolean caseInsensitive = false;
    
    if ( (locateOptions & Locate.FIRST) == Locate.FIRST) {
      fromRowIndex = 0;
    }
    if ( (locateOptions & Locate.NEXT) == Locate.NEXT) {
      fromRowIndex = _currentRowIndex+1;
    }
    if ( (locateOptions & Locate.CASE_INSENSITIVE) == Locate.CASE_INSENSITIVE) {
      caseInsensitive = true;
    }
    
    rowIndex = findRowIndex(aLocateDataRow, fromRowIndex, caseInsensitive);
    
    if (rowIndex >= 0) {
      copyToDataRow(rowIndex, aResultRow);
      found = true;
    }
    else {
      found = false;
    }
    
    return found;
  }
  
  /**
   * Return row index of the row with the specified values if found, else
   * return -1.
   */
  private int findRowIndex(DataRow aLocateDataRow, int fromRowIndex, 
                           boolean caseInsensitive) {
    int rowIndex = fromRowIndex;
    boolean found = false;
    
    ColumnList aLocateColumnList = aLocateDataRow.getColumnList();
    int columnCount = aLocateColumnList.getColumnCount();
    
    Column aLocateColumn = null;
    String columnName = null;
    int dataType = 0;
    
    for (; rowIndex<_rowCount && found == false; rowIndex++) {
      // columns iteration {
      for (int i=0; i<columnCount; i++) {
        aLocateColumn = aLocateColumnList.getColumn(i);
        columnName = aLocateColumn.getColumnName();
        dataType = aLocateColumn.getColumnDataType();
        
        if (_storage[rowIndex].isNull(columnName) == true) {
          found = false;
          break;
        }
        else if (dataType == Variant.STRING) {
          /*System.out.println(_storage[rowIndex].isNull(columnName));
          System.out.println(_storage[rowIndex].getString(columnName));
          System.out.println(aLocateDataRow.getString(columnName));*/
          if ( (caseInsensitive == true) &&
                    (_storage[rowIndex].getString(columnName).equalsIgnoreCase(aLocateDataRow.getString(columnName)))
             ) {
            found = true;
          }
          else if ( (caseInsensitive == false) &&
                        (_storage[rowIndex].getString(columnName).equals(aLocateDataRow.getString(columnName)))
                  ) {
            found = true;
          }
          else {
            found = false;
            break;
          }
        }
        else if (dataType == Variant.INT) {
          if (_storage[rowIndex].getInt(columnName) 
                == aLocateDataRow.getInt(columnName)) {
            found = true;
          }
          else {
            found = false;
            break;
          }
        }
        else DataSetException.unrecognizedColumnType(columnName, dataType);
      } // } columns iteration
    }
    
    if (found == false) rowIndex = -1;
    
    return --rowIndex;
  }
  
  private void copyToDataRow(int targetRowIndex, DataRow aResultDataRow) {
    ColumnList aColumnList = aResultDataRow.getColumnList();
    int columnCount = aColumnList.getColumnCount();
    
    Column aColumn = null;
    String columnName = null;
    int dataType = 0;
    int ordinal = 0;
    
    // columns iteration {
    for (int i=0; i<columnCount; i++) {
      aColumn = aColumnList.getColumn(i);
      columnName = aColumn.getColumnName();
      dataType = aColumn.getColumnDataType();
      ordinal = aColumn.getOrdinal();
      
      switch (dataType) {
        case Variant.STRING : aResultDataRow.setString(ordinal, _storage[targetRowIndex].getString(columnName));
                              break;
        case Variant.BIGDECIMAL : aResultDataRow.setBigDecimal(ordinal, _storage[targetRowIndex].getBigDecimal(columnName));
                                  break;
        case Variant.INT : aResultDataRow.setInt(ordinal, _storage[targetRowIndex].getInt(columnName));
                           break;
        case Variant.TIMESTAMP : aResultDataRow.setTimestamp(ordinal, _storage[targetRowIndex].getTimestamp(columnName));
                                 break;
        case Variant.DOUBLE : aResultDataRow.setDouble(ordinal, _storage[targetRowIndex].getDouble(columnName));
                              break;
        case Variant.FLOAT : aResultDataRow.setFloat(ordinal, _storage[targetRowIndex].getFloat(columnName));
                             break;
        case Variant.DATE : aResultDataRow.setDate(ordinal, _storage[targetRowIndex].getDate(columnName));
                            break;
        case Variant.TIME : aResultDataRow.setTime(ordinal, _storage[targetRowIndex].getTime(columnName));
                            break;
        case Variant.BOOLEAN : aResultDataRow.setBoolean(ordinal, _storage[targetRowIndex].getBoolean(columnName));
                               break;
        case Variant.SHORT : aResultDataRow.setShort(ordinal, _storage[targetRowIndex].getShort(columnName));
                             break;
        case Variant.BYTE : aResultDataRow.setByte(ordinal, _storage[targetRowIndex].getByte(columnName));
                            break;
        default : DataSetException.unrecognizedDataType(dataType);
      }
    }
  }
  
  /**
   * There is no implementation!!!
   */
  public final void setMetaDataUpdate(int metaDataUpdate) {
  }
  
  private ReadWriteRow[] _storage;
  
  private int _currentRowIndex = 0;
  
  private boolean _inBounds = false;
  
  private ColumnList _aColumnList;
  
  private int _maxRows = 0;
  
  /** The number of rows in the DataSet. **/
  private int _rowCount = 0;
  
  private Provider _aProvider;
  
  private boolean _isOpen = false;
  
  private int _storageCapacity = 512;
  
  /** 
   * The amount by which the capacity of the storage
   * is automatically incremented when its size becomes 
   * greater than its capacity.
   */
  private int _storageCapacityIncrement = 256;
  
  private String[] _sortColumnNames = null;
}