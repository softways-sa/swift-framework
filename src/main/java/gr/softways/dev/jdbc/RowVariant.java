/*
 * RowVariant.java
 *
 * Created on 6 Φεβρουάριος 2003, 11:24 πμ
 */

package gr.softways.dev.jdbc;

import java.math.BigDecimal;

import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;
import java.sql.Types;

/**
 * The RowVariant class is a type of storage class whose value can be 
 * one of many data types.
 *
 * @author  minotauros
 */
public class RowVariant {

  public RowVariant() {
  }
  
  public String format() {
    String s = null;
    
    switch (_dataType) {
      case Variant.STRING : s = getString();
                            break;
                            
      case Variant.INT : s = String.valueOf(getInt());
                         break;
                         
      case Variant.TIMESTAMP : if (isNull() == false) s = getTimestamp().toString();
                               else s = null;
                               break;
                     
      case Variant.TIME : if (isNull() == false) s = getTime().toString();
                          else s = null;
                          break;
                          
      case Variant.DATE : if (isNull() == false) s = getDate().toString();
                          else s = null;
                          break;
      
      case Variant.BOOLEAN : if (isNull() == false) {
                               if (getBoolean() == true) s = "true";
                               else s = "false";
                             }
                             else s = null;
                             break;
                             
      case Variant.BIGDECIMAL : if (isNull() == false) s = getBigDecimal().toString();
                                else s = null;
                                break;
                                
      case Variant.DOUBLE : s = String.valueOf(getDouble());
                            break;
                            
      case Variant.FLOAT : s = String.valueOf(getFloat());
                           break;
                          
      case Variant.SHORT : s = String.valueOf(getShort());
                           break;
                           
      case Variant.BYTE : s = String.valueOf(getByte());
                          break;

      default : DataSetException.unrecognizedDataType(_dataType);
    }
    
    return s;
  }
  
  public String getString() {
    if (_dataType == Variant.STRING) {
      return _aString;
    }
    else {
      DataSetException.invalidColumnType("getString()", dataTypeName());
      return null;
    }
  }
  public void setString(String aString) {
    _aString = aString;
    setDataType(Variant.STRING);
  }
  
  public int getInt() {
    if (_dataType == Variant.INT) {
      return _intValue;
    }
    else {
      DataSetException.invalidColumnType("getInt()", dataTypeName());
      return 0;
    }
  }
  public void setInt(int intValue) {
    _intValue = intValue;
    setDataType(Variant.INT);
  }
  
  public BigDecimal getBigDecimal() {
    if (_dataType == Variant.BIGDECIMAL) {
      return _aBigDecimal;
    }
    else {
      DataSetException.invalidColumnType("getBigDecimal()", dataTypeName());
      return null;
    }
  }
  public void setBigDecimal(BigDecimal aBigDecimal) {
    _aBigDecimal = aBigDecimal;
    setDataType(Variant.BIGDECIMAL);
  }
  
  public Timestamp getTimestamp() {
    if (_dataType == Variant.TIMESTAMP) {
      return _aTimestamp;
    }
    else {
      DataSetException.invalidColumnType("getTimestamp()", dataTypeName());
      return null;
    }
  }
  public void setTimestamp(Timestamp aTimestamp) {
    _aTimestamp = aTimestamp;
    setDataType(Variant.TIMESTAMP);
  }
  
  public double getDouble() {
    if (_dataType == Variant.DOUBLE) {
      return _doubleValue;
    }
    else {
      DataSetException.invalidColumnType("getDouble()", dataTypeName());
      return 0;
    }
  }
  public void setDouble(double doubleValue) {
    _doubleValue = doubleValue;
    setDataType(Variant.DOUBLE);
  }
  
  public float getFloat() {
    if (_dataType == Variant.FLOAT) {
      return _floatValue;
    }
    else {
      DataSetException.invalidColumnType("getFloat()", dataTypeName());
      return 0;
    }
  }
  public void setFloat(float floatValue) {
    _floatValue = floatValue;
    setDataType(Variant.FLOAT);
  }
  
  public short getShort() {
    if (_dataType == Variant.SHORT) {
      return _shortValue;
    }
    else {
      DataSetException.invalidColumnType("getShort()", dataTypeName());
      return 0;
    }
  }
  public void setShort(short shortValue) {
    _shortValue = shortValue;
    setDataType(Variant.SHORT);
  }
  
  public Date getDate() {
    if (_dataType == Variant.DATE) {
      return _aDate;
    }
    else {
      DataSetException.invalidColumnType("getDate()", dataTypeName());
      return null;
    }
  }
  public void setDate(Date aDate) {
    _aDate = aDate;
    setDataType(Variant.DATE);
  }
  
  public Time getTime() {
    if (_dataType == Variant.TIME) {
      return _aTime;
    }
    else {
      DataSetException.invalidColumnType("getTime()", dataTypeName());
      return null;
    }
  }
  public void setTime(Time aTime) {
    _aTime = aTime;
    setDataType(Variant.TIME);
  }
  
  public byte getByte() {
    if (_dataType == Variant.BYTE) {
      return _byteValue;
    }
    else {
      DataSetException.invalidColumnType("getByte()", dataTypeName());
      return 0;
    }
  }
  public void setByte(byte byteValue) {
    _byteValue = byteValue;
    setDataType(Variant.BYTE);
  }
  
  public boolean getBoolean() {
    if (_dataType == Variant.BOOLEAN) {
      return _booleanValue;
    }
    else {
      DataSetException.invalidColumnType("getBoolean()", dataTypeName());
      return false;
    }
  }
  public void setBoolean(boolean booleanValue) {
    _booleanValue = booleanValue;
    setDataType(Variant.BOOLEAN);
  }
  
  public void setIsNull(boolean isNull) {
    _isNull = isNull;
  }
  public boolean isNull() {
    return _isNull;
  }
  
  /**
   * Set data type. 
   */
  public void setDataType(int dataType) {
    _dataType = dataType;
  }
  
  public int getDataType() {
    return _dataType;
  }  

  private String dataTypeName() {
    String dataTypeName = "NOT_DEFINED";
    
         if (_dataType == Variant.STRING) dataTypeName = "String";
    else if (_dataType == Variant.BIGDECIMAL) dataTypeName = "BigDecimal";
    else if (_dataType == Variant.INT) dataTypeName = "int";
    else if (_dataType == Variant.TIMESTAMP) dataTypeName = "Timestamp";
    else if (_dataType == Variant.DOUBLE) dataTypeName = "double";
    else if (_dataType == Variant.FLOAT) dataTypeName = "float";
    else if (_dataType == Variant.DATE) dataTypeName = "Date";
    else if (_dataType == Variant.TIME) dataTypeName = "Time";
    else if (_dataType == Variant.BOOLEAN) dataTypeName = "boolean";
    else if (_dataType == Variant.SHORT) dataTypeName = "short";
    else if (_dataType == Variant.BYTE) dataTypeName = "byte";
            
    return dataTypeName;
  }
  
  public static int mapSQLtoDataType(int sqlType) {
    int dataType = -1;
    
    switch (sqlType) {
      case Types.CHAR : dataType = Variant.STRING;
                        break;
      case Types.VARCHAR : dataType =  Variant.STRING;
                           break;
      case Types.LONGVARCHAR : dataType = Variant.STRING;
                               break;
      case Types.CLOB : dataType = Variant.STRING;
                               break;
      case Types.NUMERIC : dataType = Variant.BIGDECIMAL;
                           break;
      case Types.DECIMAL : dataType = Variant.BIGDECIMAL;
                           break;
      case Types.INTEGER : dataType = Variant.INT;
                           break;
      case Types.BIGINT : dataType = Variant.INT;
                          break;
      case Types.TIMESTAMP : dataType = Variant.TIMESTAMP;
                             break;
      case Types.DOUBLE : dataType = Variant.DOUBLE;
                          break;
      case Types.FLOAT : dataType = Variant.DOUBLE;
                         break;
      case Types.REAL : dataType = Variant.FLOAT;
                        break;
      case Types.SMALLINT : dataType = Variant.SHORT;
                            break;
      case Types.DATE : dataType = Variant.DATE;
                        break;
      case Types.TIME : dataType = Variant.TIME;
                        break;
      case Types.TINYINT : dataType = Variant.BYTE;
                           break;
      case Types.BOOLEAN : dataType = Variant.BOOLEAN;
                           break;
      default : DataSetException.unrecognizedSQLColumnType(sqlType);
    }
    
    return dataType;
  }
  
  private int _intValue;
  private short _shortValue;
  private byte _byteValue;
  private double _doubleValue;
  private float _floatValue;
  private boolean _booleanValue;
  
  private String _aString;
  private BigDecimal _aBigDecimal;
  private Timestamp _aTimestamp;
  private Date _aDate;
  private Time _aTime;
 
  private boolean _isNull = false;
  
  private int _dataType;
}