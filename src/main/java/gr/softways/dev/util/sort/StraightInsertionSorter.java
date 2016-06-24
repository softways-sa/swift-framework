/*
 * StraightInsertionSorter.java
 *
 * Created on 22 Ιούλιος 2003, 1:05 μμ
 */

package gr.softways.dev.util.sort;

/**
 *
 * @author  haris
 */
public class StraightInsertionSorter extends AbstractSorter {
  
  protected void sort() {
    for (int i=1; i < n; ++i)
      for (int j=i; j > 0 && array[j-1].isGT(array[j]); --j) {
        swap(j, j-1);
      }
  }
  
}
