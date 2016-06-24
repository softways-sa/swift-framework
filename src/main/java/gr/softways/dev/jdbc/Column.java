/*
 * Column.java
 *
 * Created on 4 Φεβρουάριος 2003, 1:36 μμ
 */

package gr.softways.dev.jdbc;

/**
 * The Column component stores important column-level metadata 
 * type properties (such as column name, data type).
 *
 * @author  minotauros
 */
public class Column {
  
  public Column(String columnName, int columnDataType, int ordinal) {
    _columnName = columnName;
    _columnDataType = columnDataType;
    _ordinal = ordinal;
  }
  
  public String getColumnName() {
    return _columnName;
  }
  
  public int getColumnDataType() {
    return _columnDataType;
  }
  
  public int getOrdinal() {
    return _ordinal;
  }
  
  private String _columnName;
  private int _columnDataType;
  private int _ordinal;
}
