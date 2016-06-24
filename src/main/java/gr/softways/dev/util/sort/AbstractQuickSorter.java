/*
 * AbstractQuickSorter.java
 *
 * Created on 22 Ιούλιος 2003, 12:11 μμ
 */

package gr.softways.dev.util.sort;

/**
 *
 * @author  haris
 */
public abstract class AbstractQuickSorter extends AbstractSorter {
  
  protected static final int cutOff = 2; //minimum cutOff
  protected abstract int selectPivot(int left, int right);
  
  protected void sort(int left, int right) {
    if (right - left + 1 > cutOff) {
      int p = selectPivot(left, right);
      swap(p, right);
      Comparable pivot = array[right];
      int i = left;
      int j = right - 1;
      for (;;) {
        while (i < j && array[i].isLT(pivot)) 
          ++i;
        while (i < j && array[j].isGT(pivot))
          --j;
        if (i >= j)
          break;
        swap(i++, j--);
      }
      if (array[i].isGT(pivot))
        swap(i, right);
      if (left < i)
        sort(left, i-1);
      if (right > i)
        sort(i+1, right);
    }
  }
  
  protected void sort() {
    sort(0, n-1);
    Sorter sorter = new StraightInsertionSorter();
    sorter.sort(array);
  }
  
}
