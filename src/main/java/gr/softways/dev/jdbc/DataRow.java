/*
 * DataRow.java
 *
 * Created on 17 Φεβρουάριος 2003, 3:13 μμ
 */

package gr.softways.dev.jdbc;

/**
 * The DataRow contains one row's worth of storage for Column components 
 * of the DataSet it is constructed from.
 *
 * @author  minotauros
 */
public class DataRow extends ReadWriteRow {

  /**
   * Constructs a "scoped" DataRow containing the data structure (but no data) 
   * from specified columns of the DataSet.
   *
   */
  public DataRow(DataSet aDataSet, String columnName) {
    this(aDataSet, new String[] {columnName});
  }
  
  /**
   * Constructs a "scoped" DataRow containing the data structure (but no data) 
   * from specified columns of the DataSet.
   *
   */
  public DataRow(DataSet aDataSet, String[] columnNames) {
    super(columnNames.length);
    
    _aColumnList = new ColumnList(columnNames.length);
    
    ColumnList aDataSetColumnList = aDataSet.getColumnList();
    
    for (int i=0; i<columnNames.length; i++) {
      _aColumnList.addColumn(columnNames[i],
                             aDataSetColumnList.getColumn(columnNames[i]).getColumnDataType(),
                             i);
    }
    
    super.setColumnList(_aColumnList);
  }
  
  public final ColumnList getColumnList() {
    return _aColumnList;
  }
  
  private ColumnList _aColumnList;
}
