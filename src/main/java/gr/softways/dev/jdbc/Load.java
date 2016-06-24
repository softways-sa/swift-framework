/*
 * Load.java
 *
 * Created on 31 Ιανουάριος 2003, 12:47 μμ
 */

package gr.softways.dev.jdbc;

/**
 * The Load interface defines constants used to control how data is 
 * loaded into a QueryDataSet.
 *
 * @author  minotauros
 */
public interface Load {
  
  /**
   * Load all data in a single fetch. Note that if setMaxRows() is set,
   * the excess rows are silently dropped.
   */
  public static final int ALL = 0;
  
  /**
   * Initially, one row is loaded. Whenever a navigation beyond the loaded row 
   * is attempted, another row is loaded that replaces the previously loaded row. 
   * The JDBC ResultSet is kept open until the last record is read.
   *
   */
  public static final int UNCACHED = 4;
  
  /**
   * Not implemented!!!
   */
  //public static final int AS_NEEDED = 2;
  
  /**
   * Not implemented!!!
   */
  //public static final int ASYNCHRONOUS = 1;
}
