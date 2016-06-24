/*
 * PrdPrice.java
 *
 * Created on 19 Νοέμβριος 2003, 11:21 πμ
 */

package gr.softways.dev.eshop.eways;

import java.math.BigDecimal;

/**
 *
 * @author  minotauros
 * 
 */
public class PrdPrice {
  
  /** Creates a new instance of PrdPrice */
  public PrdPrice() {
  }
 
  // curr1
  
  public void setUnitNetCurr1(BigDecimal unitNetCurr1) {
    _unitNetCurr1 = unitNetCurr1;
  }
  public BigDecimal getUnitNetCurr1() {
    return _unitNetCurr1;
  }
  
  public void setUnitVATCurr1(BigDecimal unitVATCurr1) {
    _unitVATCurr1 = unitVATCurr1;
  }
  public BigDecimal getUnitVATCurr1() {
    return _unitVATCurr1;
  }
  
  public void setUnitGrossCurr1(BigDecimal unitGrossCurr1) {
    _unitGrossCurr1 = unitGrossCurr1;
  }
  public BigDecimal getUnitGrossCurr1() {
    return _unitGrossCurr1;
  }
  
  public void setTotalNetCurr1(BigDecimal totalNetCurr1) {
    _totalNetCurr1 = totalNetCurr1;
  }
  public BigDecimal getTotalNetCurr1() {
    return _totalNetCurr1;
  }

  public void setTotalVATCurr1(BigDecimal totalVATCurr1) {
    _totalVATCurr1 = totalVATCurr1;
  }
  public BigDecimal getTotalVATCurr1() {
    return _totalVATCurr1;
  }
  
  public void setTotalGrossCurr1(BigDecimal totalGrossCurr1) {
    _totalGrossCurr1 = totalGrossCurr1;
  }
  public BigDecimal getTotalGrossCurr1() {
    return _totalGrossCurr1;
  }

  
  // curr2
  
  public void setUnitNetCurr2(BigDecimal unitNetCurr2) {
    _unitNetCurr2 = unitNetCurr2;
  }
  public BigDecimal getUnitNetCurr2() {
    return _unitNetCurr2;
  }
  
  public void setUnitVATCurr2(BigDecimal unitVATCurr2) {
    _unitVATCurr2 = unitVATCurr2;
  }
  public BigDecimal getUnitVATCurr2() {
    return _unitVATCurr2;
  }
  
  public void setUnitGrossCurr2(BigDecimal unitGrossCurr2) {
    _unitGrossCurr2 = unitGrossCurr2;
  }
  public BigDecimal getUnitGrossCurr2() {
    return _unitGrossCurr2;
  }
  
  public void setTotalNetCurr2(BigDecimal totalNetCurr2) {
    _totalNetCurr2 = totalNetCurr2;
  }
  public BigDecimal getTotalNetCurr2() {
    return _totalNetCurr2;
  }
  
  public void setTotalVATCurr2(BigDecimal totalVATCurr2) {
    _totalVATCurr2 = totalVATCurr2;
  }
  public BigDecimal getTotalVATCurr2() {
    return _totalVATCurr2;
  }
  
  public void setTotalGrossCurr2(BigDecimal totalGrossCurr2) {
    _totalGrossCurr2 = totalGrossCurr2;
  }
  public BigDecimal getTotalGrossCurr2() {
    return _totalGrossCurr2;
  }
  
  public void add(PrdPrice addPrdPrice) {
    setUnitNetCurr1( getUnitNetCurr1().add(addPrdPrice.getUnitNetCurr1()) );
    setUnitVATCurr1( getUnitVATCurr1().add(addPrdPrice.getUnitVATCurr1()) );
    setUnitGrossCurr1( getUnitGrossCurr1().add(addPrdPrice.getUnitGrossCurr1()) );
    
    setTotalNetCurr1( getTotalNetCurr1().add(addPrdPrice.getTotalNetCurr1()) );
    setTotalVATCurr1( getTotalVATCurr1().add(addPrdPrice.getTotalVATCurr1()) );
    setTotalGrossCurr1( getTotalGrossCurr1().add(addPrdPrice.getTotalGrossCurr1()) );
    
    setUnitNetCurr2( getUnitNetCurr2().add(addPrdPrice.getUnitNetCurr2()) );
    setUnitVATCurr2( getUnitVATCurr2().add(addPrdPrice.getUnitVATCurr2()) );
    setUnitGrossCurr2( getUnitGrossCurr2().add(addPrdPrice.getUnitGrossCurr2()) );
    
    setTotalNetCurr2( getTotalNetCurr2().add(addPrdPrice.getTotalNetCurr2()) );
    setTotalVATCurr2( getTotalVATCurr2().add(addPrdPrice.getTotalVATCurr2()) );
    setTotalGrossCurr2( getTotalGrossCurr2().add(addPrdPrice.getTotalGrossCurr2()) );
  }
  
  /**
   * Πολλ/σμος βάση των τιμών μονάδος.
   */
  public void multiply(BigDecimal mul) {
    setTotalNetCurr1( getUnitNetCurr1().multiply(mul) );
    setTotalVATCurr1( getUnitVATCurr1().multiply(mul) );
    setTotalGrossCurr1( getUnitGrossCurr1().multiply(mul) );
    
    setTotalNetCurr2( getUnitNetCurr2().multiply(mul) );
    setTotalVATCurr2( getUnitVATCurr2().multiply(mul) );
    setTotalGrossCurr2( getUnitGrossCurr2().multiply(mul) );
  }
  
  private BigDecimal _zero = new BigDecimal("0");
  
  // curr1
  private BigDecimal _unitNetCurr1 = _zero;
  private BigDecimal _unitVATCurr1 = _zero;
  private BigDecimal _unitGrossCurr1 = _zero;
  private BigDecimal _totalNetCurr1 = _zero;
  private BigDecimal _totalVATCurr1 = _zero;
  private BigDecimal _totalGrossCurr1 = _zero;
  
  // curr2
  private BigDecimal _unitNetCurr2 = _zero;
  private BigDecimal _unitVATCurr2 = _zero;
  private BigDecimal _unitGrossCurr2 = _zero;
  private BigDecimal _totalNetCurr2 = _zero;
  private BigDecimal _totalVATCurr2 = _zero;
  private BigDecimal _totalGrossCurr2 = _zero;
}