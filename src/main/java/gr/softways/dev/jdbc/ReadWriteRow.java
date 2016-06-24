/*
 * ReadWriteRow.java
 *
 * Created on 6 Φεβρουάριος 2003, 11:18 πμ
 */
package gr.softways.dev.jdbc;

import gr.softways.dev.util.sort.Comparable;
import gr.softways.dev.util.sort.AbstractObject;
import java.math.BigDecimal;

import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;

/**
 * The ReadRow class provides read access to a row of data.
 *
 * @author  minotauros
 */
public class ReadWriteRow extends AbstractObject {
  
  public ReadWriteRow(int columnCount) {
    _oneRowStorage = new RowVariant[columnCount];
  }
  
  public String format(int ordinal) {
    return getVariantStorage(ordinal).format();
  }
  public String format(String columnName) {
    return format(findOrdinal(columnName));
  }
  
  public String getString(int ordinal) {
    return getVariantStorage(ordinal).getString();
  }
  public String getString(String columnName) {
    return getString(findOrdinal(columnName));
  }
  public void setString(int ordinal, String aString) {
    RowVariant aRowVariant = new RowVariant();
    aRowVariant.setString(aString);
    
    setVariantStorage(ordinal, aRowVariant);
  }
  public void setString(String columnName, String aString) {
    setString(findOrdinal(columnName), aString);
  }
  
  public int getInt(int ordinal) {
    return getVariantStorage(ordinal).getInt();
  }
  public int getInt(String columnName) {
    return getInt(findOrdinal(columnName));
  }
  public void setInt(int ordinal, int intValue) {
    RowVariant aRowVariant = new RowVariant();
    aRowVariant.setInt(intValue);
    
    setVariantStorage(ordinal, aRowVariant);
  }
  public void setInt(String columnName, int intValue) {
    setInt(findOrdinal(columnName), intValue);
  }
  
  public BigDecimal getBigDecimal(int ordinal) {
    return getVariantStorage(ordinal).getBigDecimal();
  }
  public BigDecimal getBigDecimal(String columnName) {
    return getBigDecimal(findOrdinal(columnName));
  }
  public void setBigDecimal(int ordinal, BigDecimal aBigDecimal) {
    RowVariant aRowVariant = new RowVariant();
    aRowVariant.setBigDecimal(aBigDecimal);
    
    setVariantStorage(ordinal, aRowVariant);
  }
  public void setBigDecimal(String columnName, BigDecimal aBigDecimal) {
    setBigDecimal(findOrdinal(columnName), aBigDecimal);
  }
  
  public Timestamp getTimestamp(int ordinal) {
    return getVariantStorage(ordinal).getTimestamp();
  }
  public Timestamp getTimestamp(String columnName) {
    return getTimestamp(findOrdinal(columnName));
  }
  public void setTimestamp(int ordinal, Timestamp aTimestamp) {
    RowVariant aRowVariant = new RowVariant();
    aRowVariant.setTimestamp(aTimestamp);
    
    setVariantStorage(ordinal, aRowVariant);
  }
  public void setTimestamp(String columnName, Timestamp aTimestamp) {
    setTimestamp(findOrdinal(columnName), aTimestamp);
  }
  
  public double getDouble(int ordinal) {
    return getVariantStorage(ordinal).getDouble();
  }
  public double getDouble(String columnName) {
    return getDouble(findOrdinal(columnName));
  }
  public void setDouble(int ordinal, double doubleValue) {
    RowVariant aRowVariant = new RowVariant();
    aRowVariant.setDouble(doubleValue);
    
    setVariantStorage(ordinal, aRowVariant);
  }
  public void setDouble(String columnName, double doubleValue) {
    setDouble(findOrdinal(columnName), doubleValue);
  }
  
  public float getFloat(int ordinal) {
    return getVariantStorage(ordinal).getFloat();
  }
  public float getFloat(String columnName) {
    return getFloat(findOrdinal(columnName));
  }
  public void setFloat(int ordinal, float floatValue) {
    RowVariant aRowVariant = new RowVariant();
    aRowVariant.setFloat(floatValue);
    
    setVariantStorage(ordinal, aRowVariant);
  }
  public void setFloat(String columnName, float floatValue) {
    setFloat(findOrdinal(columnName), floatValue);
  }
  
  public short getShort(int ordinal) {
    return getVariantStorage(ordinal).getShort();
  }
  public short getShort(String columnName) {
    return getShort(findOrdinal(columnName));
  }
  public void setShort(int ordinal, short shortValue) {
    RowVariant aRowVariant = new RowVariant();
    aRowVariant.setShort(shortValue);
    
    setVariantStorage(ordinal, aRowVariant);
  }
  public void setShort(String columnName, short shortValue) {
    setShort(findOrdinal(columnName), shortValue);
  }
  
  public Date getDate(int ordinal) {
    return getVariantStorage(ordinal).getDate();
  }
  public Date getDate(String columnName) {
    return getDate(findOrdinal(columnName));
  }
  public void setDate(int ordinal, Date aDate) {
    RowVariant aRowVariant = new RowVariant();
    aRowVariant.setDate(aDate);
    
    setVariantStorage(ordinal, aRowVariant);
  }
  public void setDate(String columnName, Date aDate) {
    setDate(findOrdinal(columnName), aDate);
  }
  
  public Time getTime(int ordinal) {
    return getVariantStorage(ordinal).getTime();
  }
  public Time getTime(String columnName) {
    return getTime(findOrdinal(columnName));
  }
  public void setTime(int ordinal, Time aTime) {
    RowVariant aRowVariant = new RowVariant();
    aRowVariant.setTime(aTime);
    
    setVariantStorage(ordinal, aRowVariant);
  }
  public void setTime(String columnName, Time aTime) {
    setTime(findOrdinal(columnName), aTime);
  }
  
  public byte getByte(int ordinal) {
    return getVariantStorage(ordinal).getByte();
  }
  public byte getByte(String columnName) {
    return getByte(findOrdinal(columnName));
  }
  public void setByte(int ordinal, byte byteValue) {
    RowVariant aRowVariant = new RowVariant();
    aRowVariant.setByte(byteValue);
    
    setVariantStorage(ordinal, aRowVariant);
  }
  public void setByte(String columnName, byte byteValue) {
    setByte(findOrdinal(columnName), byteValue);
  }
  
  public boolean getBoolean(int ordinal) {
    return getVariantStorage(ordinal).getBoolean();
  }
  public boolean getBoolean(String columnName) {
    return getBoolean(findOrdinal(columnName));
  }  
  public void setBoolean(int ordinal, boolean booleanValue) {
    RowVariant aRowVariant = new RowVariant();
    aRowVariant.setBoolean(booleanValue);
    
    setVariantStorage(ordinal, aRowVariant);
  }
  public void setBoolean(String columnName, boolean booleanValue) {
    setBoolean(findOrdinal(columnName), booleanValue);
  }

  public boolean isNull(int ordinal) {
    return getVariantStorage(ordinal).isNull();
  }
  public boolean isNull(String columnName) {
    return isNull(findOrdinal(columnName));
  }
  public void setIsNull(int ordinal, boolean isNull) {
    getVariantStorage(ordinal).setIsNull(isNull);
  }
  public void setIsNull(String columnName, boolean isNull) {
    setIsNull(findOrdinal(columnName), isNull);
  }
  
  /**
   * Set data type for a column after the value
   * of the column has been inserted.
   */
  public void setDataType(int ordinal, int dataType) {
    getVariantStorage(ordinal).setDataType(dataType);
  }
  
  void setColumnList(ColumnList aColumnList) {
    _aColumnList = aColumnList;
  }
  
  public int compareTo(Comparable arg) {
    return 0;
  }  
  
  public int compareTo(Comparable arg, String[] sortColumnNames) {
    int j;
    for (j=0; j < sortColumnNames.length; j++) {
      if (sortColumnNames[j] == null || sortColumnNames[j].equals(""))
        break;
    }
    int keysCount = j;
    int ordinal, dataType, result = 0;
    for (int i=0; result == 0 && i < keysCount; i++) {
      ordinal = findOrdinal(sortColumnNames[i]);
      dataType = getVariantStorage(ordinal).getDataType();    
      result = 0;
      switch(dataType) {
        case Variant.STRING:
          String s1 = getString(ordinal);
          String s2 = ((ReadWriteRow)arg).getString(ordinal);        
          if (s1 == null && s2 == null)
            result = 0;
          else if (s1 == null)
            result = -1;
          else if (s2 == null)
            result = 1;
          else
            result = s1.compareToIgnoreCase(s2);
          break;
        
        case Variant.INT :
          int n1 = getInt(ordinal);
          int n2 = ((ReadWriteRow)arg).getInt(ordinal);
          if (n1 < n2)
            result = -1;
          else if (n1 == n2)
            result = 0;
          else
            result = 1;
          break;
                      
        case Variant.TIMESTAMP :
          Timestamp ts1 = getTimestamp(ordinal);
          Timestamp ts2 = ((ReadWriteRow)arg).getTimestamp(ordinal);        
          if (ts1 == null && ts2 == null)
            result = 0;
          else if (ts1 == null)
            result = -1;
          else if (ts2 == null)
            result = 1;
          else {       
            if (ts1.before(ts2))
              result = -1;
            else if (ts1.after(ts2))
              result = 1;
            else
              result = 0;
          }
          break;       
          
        case Variant.TIME :
          Time t1 = getTime(ordinal);
          Time t2 = ((ReadWriteRow)arg).getTime(ordinal);                
          if (t1 == null && t2 == null)
            result = 0;
          else if (t1 == null)
            result = -1;
          else if (t2 == null)
            result = 1;
          else {               
            if (t1.before(t2))
              result = -1;
            else if (t1.after(t2))
              result = 1;
            else
              result = 0;        
          }
          break;       
        
        case Variant.DATE :
          Date d1 = getDate(ordinal);
          Date d2 = ((ReadWriteRow)arg).getDate(ordinal);                        
          if (d1 == null && d2 == null)
            result = 0;
          else if (d1 == null)
            result = -1;
          else if (d2 == null)
            result = 1;
          else {                       
            if (d1.before(d2))
              result = -1;
            else if (d1.after(d2))
              result = 1;
            else
              result = 0;                
          }
          break;     
          
        case Variant.BOOLEAN :
          boolean boo1 = getBoolean(ordinal);
          boolean boo2 = ((ReadWriteRow)arg).getBoolean(ordinal);        
          if (!boo1 && boo2)
            result = -1;
          else if (boo1 == boo2)
            result = 0;
          else
            result = 1;
          break;            
        
        case Variant.BIGDECIMAL :
          BigDecimal bd1 = getBigDecimal(ordinal);
          BigDecimal bd2 = ((ReadWriteRow)arg).getBigDecimal(ordinal);        
          if (bd1 == null && bd2 == null)
            result = 0;
          else if (bd1 == null)
            result = -1;
          else if (bd2 == null)
            result = 1;
          else
            result = bd1.compareTo(bd2);
          break;                                
        
        case Variant.DOUBLE :
          double db1 = getDouble(ordinal);
          double db2 = ((ReadWriteRow)arg).getDouble(ordinal);                
          if (db1 < db2)
            result = -1;
          else if (db1 == db2)
            result = 0;
          else
            result = 1;        
          break;     
        
        case Variant.FLOAT :
          float f1 = getFloat(ordinal);
          float f2 = ((ReadWriteRow)arg).getFloat(ordinal);                
          if (f1 < f2)
            result = -1;
          else if (f1 == f2)
            result = 0;
          else
            result = 1;                
          break;   
        
        case Variant.SHORT :
          short sh1 = getShort(ordinal);
          short sh2 = ((ReadWriteRow)arg).getShort(ordinal);                
          if (sh1 < sh2)
            result = -1;
          else if (sh1 == sh2)
            result = 0;
          else
            result = 1;                        
          break;                           
        
        case Variant.BYTE : 
          byte byte1 = getByte(ordinal);
          byte byte2 = ((ReadWriteRow)arg).getByte(ordinal);                
          if (byte1 < byte2)
            result = -1;
          else if (byte1 == byte2)
            result = 0;
          else
            result = 1;                                
          break;        
      }
    }
   
    return result;
  }
  
  private RowVariant getVariantStorage(int ordinal) {
    return _oneRowStorage[ordinal];
  }
  private void setVariantStorage(int ordinal, RowVariant aRowVariant) {
    _oneRowStorage[ordinal] = aRowVariant;
  }
  
  private int findOrdinal(String columnName) {
    if (_aColumnList == null) DataSetException.nullColumnList();
    
    return _aColumnList.findOrdinal(columnName);
  }
  
  private RowVariant[] _oneRowStorage;
  
  private ColumnList _aColumnList;
  
  
}