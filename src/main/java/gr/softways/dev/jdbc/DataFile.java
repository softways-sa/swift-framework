/*
 * DataFile.java
 *
 * Created on 20 Φεβρουάριος 2003, 3:36 μμ
 */

package gr.softways.dev.jdbc;

/**
 * This class collects the basic behavior of all file-based data sources: 
 * loading data from a file. 
 * <p>
 * Extend this base class when creating new classes to define a custom file 
 * format that you want to import data from (a ExcelDataFile class for an 
 * EXCEL file for instance).
 * <p>
 * The TextDataFile component extends this class. It provides the ability 
 * to read data from a text file into the TableDataSet component.
 *
 * @author  minotauros
 */
public abstract class DataFile {
  
  public abstract void load(DataSet aDataSet);  
}