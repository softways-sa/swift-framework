/*
 * PrdPrice.java
 *
 * Created on 19 Νοέμβριος 2003, 11:21 πμ
 */

package gr.softways.dev.eshop.eways.v2;

import java.math.BigDecimal;

import gr.softways.dev.util.SwissKnife;

/**
 *
 * @author  minotauros
 * 
 */
public class TotalPrice {
  
  public TotalPrice() {
  }
  
  public void setCurr1Scale(int curr1Scale) {
    _curr1Scale = curr1Scale;
  }
  
  public void setNetCurr1(BigDecimal netCurr1) {
    _netCurr1 = netCurr1;
  }
  public BigDecimal getNetCurr1() {
    return _netCurr1.setScale(_curr1Scale, BigDecimal.ROUND_HALF_UP);
  }
  
  public void setVATCurr1(BigDecimal vatCurr1) {
    _vatCurr1 = vatCurr1;
  }
  public BigDecimal getVATCurr1() {
    return _vatCurr1.setScale(_curr1Scale, BigDecimal.ROUND_HALF_UP);
  }
  
  public BigDecimal getGrossCurr1() {
    return getNetCurr1().add(getVATCurr1()).setScale(_curr1Scale, BigDecimal.ROUND_HALF_UP);
  }
  
  public void add(PrdPrice addPrdPrice) {
    setNetCurr1( getNetCurr1().add(addPrdPrice.getTotalNetCurr1()) );
    setVATCurr1( getVATCurr1().add(addPrdPrice.getTotalVATCurr1()) );
  }
  public void add(TotalPrice addPrdPrice) {
    setNetCurr1( getNetCurr1().add(addPrdPrice.getNetCurr1()) );
    setVATCurr1( getVATCurr1().add(addPrdPrice.getVATCurr1()) );
  }
  
  private BigDecimal _zero = new BigDecimal("0");
  
  private BigDecimal _netCurr1 = _zero;
  private BigDecimal _vatCurr1 = _zero;
  
  private int _curr1Scale = Integer.parseInt(SwissKnife.jndiLookup("swconf/curr1Scale"));
}
