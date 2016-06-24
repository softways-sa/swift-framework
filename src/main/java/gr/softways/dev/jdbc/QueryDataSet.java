/*
 * QueryDataSet.java
 *
 * Created on 3 Φεβρουάριος 2003, 10:47 πμ
 */

package gr.softways.dev.jdbc;

/**
 * The QueryDataSet class provides functionality to run a query statement
 * against a table in a SQL database. QueryDataSet is read-only and scrollable.
 * <p>
 * In any application that uses the QueryDataSet, 
 * the following components are also required: 
 * <p>
 * <li> an instantiated Database component to handle the JDBC connection to the SQL database 
 * <li> an instantiated QueryDescriptor object to store the query properties 
 * <p>
 * When a query joins two or more tables, it may return several columns 
 * that have the same name. Duplicate column names are handled by 
 * appending a number ("EMP_NO", "EMP_No1", etc.).
 *
 * @author  minotauros
 */
public class QueryDataSet extends DataSet {
  
  public QueryDataSet() {
    super();
  }
  
  public final void setQuery(QueryDescriptor aQueryDescriptor) {
    if (isOpen() == true) DataSetException.closeDataSetFirst();
    
    QueryProvider aQueryProvider = new QueryProvider();
    setProvider(aQueryProvider);
    aQueryProvider.setQueryDescriptor(aQueryDescriptor);
  }

  /**
   * You can call the executeQuery() method without an open DataSet. 
   * However, if the DataSet is open, executeQuery() will close the DataSet, 
   * execute the query, then re-open the DataSet. 
   * Calls {@link gr.softways.dev.jdbc.DataSet#refresh() refresh} method.
   */
  public void executeQuery() {
    refresh();
  }
}