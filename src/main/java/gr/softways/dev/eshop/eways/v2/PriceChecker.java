/*
 * PriceChecker.java
 *
 * Created on 19 Νοέμβριος 2003, 11:42 πμ
 */

package gr.softways.dev.eshop.eways.v2;

import java.sql.Timestamp;
import java.math.BigDecimal;

import gr.softways.dev.util.SwissKnife;
import gr.softways.dev.jdbc.QueryDataSet;

import gr.softways.dev.eshop.eways.Product;
import gr.softways.dev.eshop.eways.Customer;

/**
 *
 * @author  minotauros
 */
public class PriceChecker {

  protected PriceChecker() {
  }
  
  public static PrdPrice calcPrd(BigDecimal quantity,QueryDataSet dataSet,int customerType,boolean isOffer) {
    return calcPrd(quantity,dataSet,customerType,isOffer,_zero);
  }
  
  public static PrdPrice calcPrd(BigDecimal quantity,QueryDataSet dataSet,int customerType,boolean isOffer,BigDecimal discountPct) {
    BigDecimal unitNetCurr1 = null;

    if (Customer.CUSTOMER_TYPE_RETAIL == customerType && isOffer == true) {
      unitNetCurr1 = dataSet.getBigDecimal(hdRetailPrcEUColumn);
    }
    else if (Customer.CUSTOMER_TYPE_RETAIL == customerType && isOffer == false) {
      unitNetCurr1 = dataSet.getBigDecimal(retailPrcEUColumn);
    }
    else if (Customer.CUSTOMER_TYPE_WHOLESALE == customerType && isOffer == true) {
      unitNetCurr1 = dataSet.getBigDecimal(hdWholesalePrcEUColumn);
    }
    else if (Customer.CUSTOMER_TYPE_WHOLESALE == customerType && isOffer == false) {
      unitNetCurr1 = dataSet.getBigDecimal(wholesalePrcEUColumn);
    }
    
    return calcPrd(quantity,unitNetCurr1,dataSet.getBigDecimal("vatPct"),Integer.parseInt(SwissKnife.jndiLookup("swconf/curr1Scale")),discountPct);
  }
  
  private static PrdPrice calcPrd(BigDecimal quantity,BigDecimal unitNetCurr1,BigDecimal vatPct,int curr1Scale,BigDecimal discountPct) {
    PrdPrice prdPrice = new PrdPrice();
    
    boolean vatIncluded = false;
    if (SwissKnife.jndiLookup("swconf/vatIncluded").equals("true")) {
      vatIncluded = true;
    }
    if (vatIncluded == true) {
      BigDecimal unitVATCurr1 = _zero;
      unitVATCurr1 = vatPct.multiply(_oneHundred);
      unitVATCurr1 = unitVATCurr1.multiply(unitNetCurr1);
      unitVATCurr1 = unitVATCurr1.divide(vatPct.multiply(_oneHundred).add(_oneHundred),BigDecimal.ROUND_HALF_UP).setScale(curr1Scale,BigDecimal.ROUND_HALF_UP);

      unitNetCurr1 = unitNetCurr1.subtract(unitVATCurr1).setScale(curr1Scale,BigDecimal.ROUND_HALF_UP);
    }
    
    prdPrice.setVATPct(vatPct);
    
    prdPrice.setCurr1Scale(curr1Scale);
    
    prdPrice.setQuantity(quantity);
    
    prdPrice.setDiscountPct(discountPct);

    // net unit price
    unitNetCurr1 = unitNetCurr1.setScale(curr1Scale, BigDecimal.ROUND_HALF_UP);
    prdPrice.setUnitNetCurr1( unitNetCurr1 );
    
    return prdPrice;
  }
  
  static public boolean isOffer(QueryDataSet dataSet, int customerType) {
    if (Customer.CUSTOMER_TYPE_RETAIL == customerType) {
      return isOffer(dataSet.getString(hotdealFlagRetailColumn),dataSet.getTimestamp(hdBeginDateRetailColumn),dataSet.getTimestamp(hdEndDateRetailColumn),dataSet.getBigDecimal("stockQua"));
    }
    else if (Customer.CUSTOMER_TYPE_WHOLESALE == customerType) {
      return isOffer(dataSet.getString(hotdealFlagWholesaleColumn),dataSet.getTimestamp(hdBeginDateWholesaleColumn),dataSet.getTimestamp(hdEndDateWholesaleColumn),dataSet.getBigDecimal("stockQua"));
    }
    else return false;
  }
  
  static private boolean isOffer(String hotdealFlag,Timestamp beginDate, Timestamp endDate,BigDecimal stockQua) {
    Timestamp today = SwissKnife.currentDate();

    boolean haveInStock = stockQua.compareTo( new BigDecimal("0") ) == 1 ? true : false;
    
    boolean validDate = false;

    if (hotdealFlag == null) hotdealFlag = "";
    
    if ( beginDate != null && endDate != null
           && beginDate.before(today) && endDate.after(today) )
      validDate = true;
    else validDate = false;

    if (hotdealFlag.equals(Product.HOTDEAL_FLAG_ALWAYS)) {
      return true;
    }
    else if (hotdealFlag.equals(Product.HOTDEAL_FLAG_DATE) && validDate) {
      return true;
    }
    else if (hotdealFlag.equals(Product.HOTDEAL_FLAG_DATE_STOCK) 
                  && haveInStock && validDate) {
      return true;
    }
    else if (hotdealFlag.equals(Product.HOTDEAL_FLAG_STOCK) && haveInStock) {
      return true;
    }
    else return false;
  }
 
  static private String hotdealFlagRetailColumn = "hotdealFlag";
  static private String hdBeginDateRetailColumn = "hdBeginDate";
  static private String hdEndDateRetailColumn = "hdEndDate";
  static private String hdRetailPrcEUColumn = "hdRetailPrcEU";
  static private String retailPrcEUColumn = "retailPrcEU";
  
  static private String extraCostPrcEUColumn = "giftPrcEU";
  
  static private String hotdealFlagWholesaleColumn = "hotdealFlagW";
  static private String hdBeginDateWholesaleColumn = "hdBeginDateW";
  static private String hdEndDateWholesaleColumn = "hdEndDateW";
  static private String hdWholesalePrcEUColumn = "hdWholesalePrcEU";
  static private String wholesalePrcEUColumn = "wholesalePrcEU";
    
  private static BigDecimal _zero = new BigDecimal("0");
  private static BigDecimal _oneHundred = new BigDecimal("100");
}
