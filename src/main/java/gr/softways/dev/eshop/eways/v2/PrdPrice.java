package gr.softways.dev.eshop.eways.v2;

import java.math.BigDecimal;

import gr.softways.dev.util.SwissKnife;

/**
 *
 * @author  minotauros
 * 
 */
public class PrdPrice {
  
  /** Creates a new instance of PrdPrice */
  public PrdPrice() {
  }
  
  public void setQuantity(BigDecimal quantity) {
    _quantity = quantity;
  }
  
  public BigDecimal getQuantity() {
    return _quantity;
  }
  
  public void setCurr1Scale(int curr1Scale) {
    _curr1Scale = curr1Scale;
  }
  
  public void setCurr2Scale(int curr2Scale) {
    _curr2Scale = curr2Scale;
  }
  
  public void setVATPct(BigDecimal vatPct) {
    _vatPct = vatPct;
  }
  
  public BigDecimal getVATPct() {
    return _vatPct;
  }
  
  public void setUnitNetCurr1(BigDecimal unitNetCurr1) {
    _unitNetCurr1 = unitNetCurr1;
  }
  
  public BigDecimal getUnitNetCurr1() {
    return _unitNetCurr1.subtract(_unitNetCurr1.multiply(_discountPct)).setScale(_curr1Scale, BigDecimal.ROUND_HALF_UP);
  }
  
  public BigDecimal getUnitVATCurr1() {
    return getUnitNetCurr1().multiply(_vatPct).setScale(_curr1Scale, BigDecimal.ROUND_HALF_UP);
  }
  
  public BigDecimal getUnitGrossCurr1() {
    return getUnitNetCurr1().add(getUnitVATCurr1()).setScale(_curr1Scale, BigDecimal.ROUND_HALF_UP);
  }
  
  
  public BigDecimal getTotalNetCurr1() {
    return getUnitNetCurr1().multiply(_quantity).setScale(_curr1Scale, BigDecimal.ROUND_HALF_UP);
  }

  public BigDecimal getTotalVATCurr1() {
   return getUnitVATCurr1().multiply(_quantity).setScale(_curr1Scale, BigDecimal.ROUND_HALF_UP);
  }
  
  public BigDecimal getTotalGrossCurr1() {
    return getUnitGrossCurr1().multiply(_quantity).setScale(_curr1Scale, BigDecimal.ROUND_HALF_UP);
  }
  
  public void setDiscountPct(BigDecimal discountPct) {
    _discountPct = discountPct;
  }
  
  private BigDecimal _zero = new BigDecimal("0");
  
  private BigDecimal _unitNetCurr1 = _zero;
  
  private BigDecimal _vatPct = _zero;
  
  private BigDecimal _quantity = _zero;
  
  private BigDecimal _discountPct = _zero;
  
  private int _curr1Scale = Integer.parseInt(SwissKnife.jndiLookup("swconf/curr1Scale"));
  private int _curr2Scale = 0;
}
