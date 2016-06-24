/*
 * QueryDescriptor.java
 *
 * Created on 31 Ιανουάριος 2003, 12:32 μμ
 */

package gr.softways.dev.jdbc;

import gr.softways.dev.jdbc.Database;
import gr.softways.dev.jdbc.Load;

/**
 * The QueryDescriptor stores properties that set a query statement 
 * to run against a SQL database.
 *
 * @author  minotauros
 */
public class QueryDescriptor {
  
  /**
   * Constructs a QueryDescriptor object with the following properties: 
   * 
   * @param database The Database object the QueryDataSet is to run against.
   * @param query The query string to execute against the specified database.
   * @param parameters not implemented!!!
   * @param executeOnOpen not implemented!!!
   * @param loadOption How the data should be loaded into the QueryDataSet. 
   *        Constants for this parameter are 
   *        defined as {@link gr.softways.dev.jdbc.Load Load} variables.
   */
  public QueryDescriptor(Database database, String query, 
                         Object parameters, boolean executeOnOpen, int loadOption) {
    _database = database;
    _query = query;
    _loadOption = loadOption;
  }
  
  public Database getDatabase() {
    return _database;
  }
  
  public String getQuery() {
    return _query;
  }
  
  public int getLoadOption() {
    return _loadOption;
  }
    
  private Database _database;
  private String _query;
  private int _loadOption;
}
