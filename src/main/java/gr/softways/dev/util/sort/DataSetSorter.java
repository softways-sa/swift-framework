/*
 * DataSetSorter.java
 *
 * Created on 23 Ιούλιος 2003, 11:34 πμ
 */

package gr.softways.dev.util.sort;

/**
 *
 * @author  haris
 */
public class DataSetSorter extends MedianOfThreeQuickSorter {
  
  public final void sort(Comparable[] array, int rowCount, String[] sortColumnNames, String sortOrder) {
    n = rowCount;
    this.array = array;
    if (n > 0)
      sort(sortColumnNames, sortOrder);
    this.array = null;
  }

  protected void sort(String[] sortColumnNames, String sortOrder) {
    if (sortOrder.equals("DESC"))
      sortDesc(0, n-1, sortColumnNames);
    else
      sort(0, n-1, sortColumnNames);      
    DataSetInsertionSorter sorter = new DataSetInsertionSorter();
    sorter.sort(array, n, sortColumnNames, sortOrder);
  }    
  
  protected void sort(int left, int right, String[] sortColumnNames) {
    if (right - left + 1 > cutOff) {
      int p = selectPivot(left, right, sortColumnNames);
      swap(p, right);
      Comparable pivot = array[right];
      int i = left;
      int j = right - 1;
      for (;;) {
        while (i < j && array[i].isLT(pivot, sortColumnNames)) 
          ++i;
        while (i < j && array[j].isGT(pivot, sortColumnNames))
          --j;
        if (i >= j)
          break;
        swap(i++, j--);
      }
      if (array[i].isGT(pivot, sortColumnNames))
        swap(i, right);
      if (left < i)
        sort(left, i-1, sortColumnNames);
      if (right > i)
        sort(i+1, right, sortColumnNames);
    }
  }
  
  protected void sortDesc(int left, int right, String[] sortColumnNames) {
    if (right - left + 1 > cutOff) {
      int p = selectPivotDesc(left, right, sortColumnNames);
      swap(p, right);
      Comparable pivot = array[right];
      int i = left;
      int j = right - 1;
      for (;;) {
        while (i < j && array[i].isGT(pivot, sortColumnNames)) 
          ++i;
        while (i < j && array[j].isLT(pivot, sortColumnNames))
          --j;
        if (i >= j)
          break;
        swap(i++, j--);
      }
      if (array[i].isLT(pivot, sortColumnNames))
        swap(i, right);
      if (left < i)
        sortDesc(left, i-1, sortColumnNames);
      if (right > i)
        sortDesc(i+1, right, sortColumnNames);
    }
  }  
  
  protected int selectPivot(int left, int right, String[] sortColumnNames) {
    int middle = (left + right) / 2;
    if (array[left].isGT(array[middle], sortColumnNames))
      swap(left, middle);
    if (array[left].isGT(array[right], sortColumnNames))
      swap(left, right);
    if (array[middle].isGT(array[right], sortColumnNames))
      swap(middle, right);
    return middle;
  }
  
  protected int selectPivotDesc(int left, int right, String[] sortColumnNames) {
    int middle = (left + right) / 2;
    if (array[left].isLT(array[middle], sortColumnNames))
      swap(left, middle);
    if (array[left].isLT(array[right], sortColumnNames))
      swap(left, right);
    if (array[middle].isLT(array[right], sortColumnNames))
      swap(middle, right);
    return middle;
  }  
  
}
