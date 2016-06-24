/*
 * AbstractObject.java
 *
 * Created on 22 Ιούλιος 2003, 2:31 μμ
 */

package gr.softways.dev.util.sort;

/**
 *
 * @author  haris
 */
public abstract class AbstractObject implements Comparable {
  
  public final boolean isLT(Comparable object) {
    return compare(object) < 0;
  }
  
  public final boolean isLE(Comparable object) {
    return compare(object) <= 0;
  }
  
  public final boolean isGT(Comparable object) {
    return compare(object) > 0;
  }
  
  public final boolean isGE(Comparable object) {
    return compare(object) >= 0;
  }
  
  public final boolean isEQ(Comparable object) {
    return compare(object) == 0;
  }
  
  public final boolean isNE(Comparable object) {
    return compare(object) != 0;
  }
  
  public final boolean equals(Object object) {
    if (object instanceof Comparable)
      return isEQ((Comparable) object);
    else
      return false;
  }  
  
  protected abstract int compareTo(Comparable arg);
  
  public final int compare(Comparable arg) {
    if (getClass() == arg.getClass())
      return compareTo(arg);
    else
      return getClass().getName().compareTo(arg.getClass().getName());
  }
  
  public final boolean isLT(Comparable object, String[] sortColumnNames) {
    return compare(object, sortColumnNames) < 0;
  }
  
  public final boolean isLE(Comparable object, String[] sortColumnNames) {
    return compare(object, sortColumnNames) <= 0;
  }
  
  public final boolean isGT(Comparable object, String[] sortColumnNames) {
    return compare(object, sortColumnNames) > 0;
  }
  
  public final boolean isGE(Comparable object, String[] sortColumnNames) {
    return compare(object, sortColumnNames) >= 0;
  }
  
  public final boolean isEQ(Comparable object, String[] sortColumnNames) {
    return compare(object, sortColumnNames) == 0;
  }
  
  public final boolean isNE(Comparable object, String[] sortColumnNames) {
    return compare(object, sortColumnNames) != 0;
  }
  
  public final boolean equals(Object object, String[] sortColumnNames) {
    if (object instanceof Comparable)
      return isEQ((Comparable) object, sortColumnNames);
    else
      return false;
  }  
  
  protected abstract int compareTo(Comparable arg, String[] sortColumnNames);
  
  public final int compare(Comparable arg, String[] sortColumnNames) {
    if (getClass() == arg.getClass())
      return compareTo(arg, sortColumnNames);
    else
      return getClass().getName().compareTo(arg.getClass().getName());
  }  
  
}
