/*
 * Comparable.java
 *
 * Created on 22 Ιούλιος 2003, 2:25 μμ
 */

package gr.softways.dev.util.sort;

/**
 *
 * @author  haris
 */
public interface Comparable {
  boolean isLT (Comparable object);
  boolean isLE (Comparable object);
  boolean isGT (Comparable object);
  boolean isGE (Comparable object);
  boolean isEQ (Comparable object);
  boolean isNE (Comparable object);
  int compare (Comparable object); 
  boolean isLT (Comparable object, String[] sortColumnNames);
  boolean isLE (Comparable object, String[] sortColumnNames);
  boolean isGT (Comparable object, String[] sortColumnNames);
  boolean isGE (Comparable object, String[] sortColumnNames);
  boolean isEQ (Comparable object, String[] sortColumnNames);
  boolean isNE (Comparable object, String[] sortColumnNames);
  int compare (Comparable object, String[] sortColumnNames);   
}
