/*
 * AbstractSorter.java
 *
 * Created on 22 Ιούλιος 2003, 11:07 πμ
 */

package gr.softways.dev.util.sort;

/**
 *
 * @author  haris
 */
public abstract class AbstractSorter implements Sorter{
  protected Comparable [] array;
  protected int n;
    
  protected abstract void sort();
  public final void sort(Comparable[] array) {
    
    n = array.length;
    this.array = array;
    if (n > 0)
      sort();
    this.array = null;
  }
  
  protected final void swap(int i, int j) {
    Comparable tmp = array[i];
    array[i] = array[j];
    array[j] = tmp;
  }
    
}
