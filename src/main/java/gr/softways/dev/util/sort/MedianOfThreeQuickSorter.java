/*
 * MedianOfThreeQuickSorter.java
 *
 * Created on 22 Ιούλιος 2003, 1:19 μμ
 */

package gr.softways.dev.util.sort;

/**
 *
 * @author  haris
 */
public class MedianOfThreeQuickSorter extends AbstractQuickSorter {
  

  protected int selectPivot(int left, int right) {
    int middle = (left + right) / 2;
    if (array[left].isGT(array[middle]))
      swap(left, middle);
    if (array[left].isGT(array[right]))
      swap(left, right);
    if (array[middle].isGT(array[right]))
      swap(middle, right);
    return middle;
  }
  
  
}
