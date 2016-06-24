/*
 * Locate.java
 *
 * Created on 18 Φεβρουάριος 2003, 12:40 μμ
 */

package gr.softways.dev.jdbc;

/**
 * The Locate interface encapsulates the most commonly-used options when performing 
 * a search operation. It allows you to specify how a particular row is found 
 * when using the DataSet.locate() method and the DataSet.lookup() method. 
 * For example, options include case sensitivity, search for the first, 
 * subsequent or last occurance, and so on.
 * <p>
 * The Locate variables may be combined where it makes sense to do so. 
 * For example, you can specify case insensitivity. Combine variables 
 * using the Java bitwise OR operator of a vertical pipe symbol (|) 
 * between each variable. 
 *
 * @author  minotauros
 */
public interface Locate {

  /** Locate the first occurance, that is search from the beginning. */
  public static final int FIRST = 32;
  
  /** 
   * Search ignoring upper or lower case differences. 
   * Valid only for String columns.
   */
  public static final int CASE_INSENSITIVE = 8;

  /** Search from the current row position and afterwards. */
  public static final int NEXT = 2;

  //public static final int PARTIAL = 1;  
  //public static final int START_MASK = 102;
  //public static final int PRIOR_FAST = 132;
  //public static final int DETAIL = 256;
  //public static final int FAST = 128;
  //public static final int LAST = 64;
  //public static final int PRIOR = 4;
  //public static final int NEXT_FAST = 130;
}
