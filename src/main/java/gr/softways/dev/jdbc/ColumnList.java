/*
 * ColumnList.java
 *
 * Created on 6 Φεβρουάριος 2003, 12:03 μμ
 */

package gr.softways.dev.jdbc;

/**
 * The ColumnList component contains database column name lookup mechanism.
 *
 * @author  minotauros
 */
public class ColumnList {

  public ColumnList(int size) {
    _columns = new Column[size];
  }

  /**
   * Add a Column. Column names must be unique and are case insensitive. 
   * If a column name being added already exists append "1","2",... 
   * to the end of the column name.
   *
   * @param columnName column name
   * @param columnType column {@link java.sql.Types SQL type}
   * @param ordinal column index in the DataSet
   */
  public void addColumn(String columnName, int columnType, int ordinal) {
    String tmpColumnName = columnName;
    
    try {
      findOrdinal(tmpColumnName);
      
      // If no exception is thrown, then columnName already exists.
      // Try to find one that does not.
      for (int i=1; i<1000; i++) {
        tmpColumnName = columnName + i;
        findOrdinal(tmpColumnName);
      }
    }
    catch (DataSetException e) {
      _columns[_index] = new Column(tmpColumnName, columnType, ordinal);
      _index++;
      
      return;
    }
    DataSetException.throwException(new Exception());
  }
  
  public Column getColumn(int ordinal) {
    return _columns[ordinal];
  }
  
  public Column getColumn(String columnName) {
    return getColumn(findOrdinal(columnName));
  }
  
  /**
   * Return column index in the DataSet (the first column is 0, the second is 1, ... ).
   * Column names are case insensitive. Throws a DataSetException if a column name
   * does not exist.
   *
   * @return the first column is 0, the second is 1, ...
   */
  public int findOrdinal(String columnName) {
    for (int i=0; i<_columns.length; i++) {
      if (_columns[i] != null 
            && _columns[i].getColumnName().equalsIgnoreCase(columnName))
        return _columns[i].getOrdinal();
    }
    DataSetException.unknownColumnName(columnName);
    
    return -1;
  }
  
  public int getColumnCount() {
    return _columns.length;
  }
  
  private Column[] _columns;
  
  private int _index = 0;
}