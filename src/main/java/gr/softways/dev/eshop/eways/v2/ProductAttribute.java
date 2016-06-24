/*
 * ProductAttribute.java
 *
 * Created on 13 Ιούλιος 2007, 4:56 μμ
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gr.softways.dev.eshop.eways.v2;

import java.math.BigDecimal;

/**
 *
 * @author haris
 */
public class ProductAttribute {
  
  /** Creates a new instance of ProductAttribute */
  public ProductAttribute() {
  }
  
  private int _hasPrice = 0;
  public void setHasPrice(int hasPrice) { _hasPrice = hasPrice; }
  public int getHasPrice() { return _hasPrice; }
  private int _keepStock = 0;
  public void setKeepStock(int keepStock) { _keepStock = keepStock; }
  public int getKeepStock() { return _keepStock; }  
  private String _atrName = "";
  public void setAtrName(String atrName) { _atrName = atrName; }
  public String getAtrName() { return _atrName; }
  private String _PMAVCode = null;
  public void setPMAVCode(String PMAVCode) { _PMAVCode = PMAVCode; }
  public String getPMAVCode() { return _PMAVCode; }
  private String _ATVAValue = null;
  public void setATVAValue(String ATVAValue) { _ATVAValue = ATVAValue; }
  public String getATVAValue() { return _ATVAValue; }
  private int _slaveFlag = 0;
  public void setSlaveFlag(int slaveFlag) { _slaveFlag = slaveFlag; }
  public int getSlaveFlag() { return _slaveFlag; }
  
  private String _PMAVID = null;
  public void setPMAVID(String PMAVID) { _PMAVID = PMAVID; }
  public String getPMAVID() { return _PMAVID; }
  
  private BigDecimal _PMAVStock = null;
  public void setPMAVStock(BigDecimal PMAVStock) { _PMAVStock = PMAVStock; }
  public BigDecimal getPMAVStock() { return _PMAVStock; }
  private BigDecimal _PMAVPrice = null;
  public void setPMAVPrice(BigDecimal PMAVPrice) { _PMAVPrice = PMAVPrice; }
  public BigDecimal getPMAVPrice() { return _PMAVPrice; }
}
