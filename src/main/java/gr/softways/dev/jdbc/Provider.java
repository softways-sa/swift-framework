/*
 * Provider.java
 *
 * Created on 14 Φεβρουάριος 2003, 1:39 μμ
 */

package gr.softways.dev.jdbc;

/**
 * The Provider class is an abstract base class that "provides" 
 * (or populates) a DataSet with data.
 *
 * @author  minotauros
 */
public abstract class Provider {

  public Provider() {
  }
  
  public boolean hasMoreData() {
    return false;
  }

  public void close() {
  }
  
  public boolean provideMoreData(DataSet aDataSet) {
    return false;
  }
  
  public abstract void provideData(DataSet aDataSet);
}