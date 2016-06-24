/*
 * DatasetInsertionSorter.java
 *
 * Created on 23 Ιούλιος 2003, 1:51 μμ
 */

package gr.softways.dev.util.sort;

/**
 *
 * @author  haris
 */
public class DataSetInsertionSorter extends AbstractSorter {
  
  protected void sort() {  
  }
  
  public final void sort(Comparable[] array, int rowCount, String[] sortColumnNames, String sortOrder) {
    n = rowCount;
    this.array = array;
    if (n > 0) {
      if (sortOrder.equals("DESC"))
        sortDesc(sortColumnNames);
      else
        sort(sortColumnNames);
    }
    this.array = null;
  }
  
  protected void sort(String[] sortColumnNames) {
    for (int i=1; i < n; ++i)
      for (int j=i; j > 0 && array[j-1].isGT(array[j], sortColumnNames); --j) {
        swap(j, j-1);
      }
  }  
  
  protected void sortDesc(String[] sortColumnNames) {
    for (int i=1; i < n; ++i)
      for (int j=i; j > 0 && array[j-1].isLT(array[j], sortColumnNames); --j) {
        swap(j, j-1);
      }
  }    
}
