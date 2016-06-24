/*
 * PriceChecker.java
 *
 * Created on 19 Νοέμβριος 2003, 11:42 πμ
 */

package gr.softways.dev.eshop.eways;

import java.sql.Timestamp;
import java.math.BigDecimal;

import gr.softways.dev.util.SwissKnife;
import gr.softways.dev.jdbc.QueryDataSet;

/**
 *
 * @author  minotauros
 */
public class PriceChecker {

  protected PriceChecker() {
  }
  
  public static PrdPrice calcPrd(BigDecimal quantity, QueryDataSet dataSet, 
                                 int customerType, boolean isOffer, 
                                 BigDecimal exchangeRate) {
    BigDecimal unitNetCurr1 = null, 
               unitNetCurr2 = null;
    
    boolean curr2Static = false;
    if (SwissKnife.jndiLookup("swconf/curr2Static").equals("true")) {
      curr2Static = true;
    }
    
    boolean vatIncluded = false;
    if (SwissKnife.jndiLookup("swconf/vatIncluded").equals("true")) {
      vatIncluded = true;
    }
    
    if (Customer.CUSTOMER_TYPE_RETAIL == customerType
          && isOffer == true) {
      unitNetCurr1 = dataSet.getBigDecimal(hdRetailPrcEUColumn);
      unitNetCurr2 = dataSet.getBigDecimal(hdRetailPrcColumn);
    }
    else if (Customer.CUSTOMER_TYPE_RETAIL == customerType
                && isOffer == false) {
      unitNetCurr1 = dataSet.getBigDecimal(retailPrcEUColumn);
      unitNetCurr2 = dataSet.getBigDecimal(retailPrcColumn);
    }
    else if (Customer.CUSTOMER_TYPE_WHOLESALE == customerType
          && isOffer == true) {
      unitNetCurr1 = dataSet.getBigDecimal(hdWholesalePrcEUColumn);
      unitNetCurr2 = dataSet.getBigDecimal(hdWholesalePrcColumn);
    }
    else if (Customer.CUSTOMER_TYPE_WHOLESALE == customerType
                && isOffer == false) {
      unitNetCurr1 = dataSet.getBigDecimal(wholesalePrcEUColumn);
      unitNetCurr2 = dataSet.getBigDecimal(wholesalePrcColumn);
    }
    
    return calcPrd(quantity, unitNetCurr1, unitNetCurr2,
                   dataSet.getBigDecimal("vatPct"), 
                   exchangeRate,curr2Static,vatIncluded,
                   Integer.parseInt(SwissKnife.jndiLookup("swconf/curr1Scale")),
                   Integer.parseInt(SwissKnife.jndiLookup("swconf/curr2Scale")));
  }
  
  private static PrdPrice calcPrd(BigDecimal quantity, 
                                  BigDecimal unitNetCurr1, BigDecimal unitNetCurr2,
                                  BigDecimal vatPct, BigDecimal exchangeRate, 
                                  boolean curr2Static, boolean vatIncluded,
                                  int curr1Scale, int curr2Scale) {
                                    
    BigDecimal unitVATCurr1 = _zero, unitVATCurr2 = _zero,
               unitGrossCurr1 = _zero, unitGrossCurr2 = _zero;
    
    BigDecimal totalNetCurr1 = _zero, totalNetCurr2 = _zero,
               totalVATCurr1 = _zero, totalVATCurr2 = _zero,
               totalGrossCurr1 = _zero, totalGrossCurr2 = _zero;
                       
    PrdPrice prdPrice = new PrdPrice();

    if (curr2Static == false) {
      unitNetCurr2 = unitNetCurr1.divide(exchangeRate, curr2Scale, BigDecimal.ROUND_HALF_UP);
    }

    // if VAT is included extract and calculate net prices
    if (vatIncluded == true) {
      unitVATCurr1 = vatPct.multiply(_oneHundred);
      unitVATCurr1 = unitVATCurr1.multiply(unitNetCurr1).divide(vatPct.multiply(_oneHundred).add(_oneHundred), 
                                                                BigDecimal.ROUND_HALF_UP);

      unitVATCurr2 = vatPct.multiply(_oneHundred);
      unitVATCurr2 = unitVATCurr2.multiply(unitNetCurr2).divide(vatPct.multiply(_oneHundred).add(_oneHundred), 
                                                                BigDecimal.ROUND_HALF_UP);

      unitNetCurr1 = unitNetCurr1.subtract(unitVATCurr1).setScale(curr1Scale, 
                                                                  BigDecimal.ROUND_HALF_UP);
      unitNetCurr2 = unitNetCurr2.subtract(unitVATCurr2).setScale(curr2Scale, 
                                                                  BigDecimal.ROUND_HALF_UP);
    }
    else {
      unitVATCurr1 = unitNetCurr1.multiply(vatPct);
      unitVATCurr2 = unitNetCurr2.multiply(vatPct);
    }

    // net unit price
    unitNetCurr1 = unitNetCurr1.setScale(curr1Scale, BigDecimal.ROUND_HALF_UP);
    unitNetCurr2 = unitNetCurr2.setScale(curr2Scale, BigDecimal.ROUND_HALF_UP);
    prdPrice.setUnitNetCurr1( unitNetCurr1 );
    prdPrice.setUnitNetCurr2( unitNetCurr2 );

    // unit VAT
    unitVATCurr1 = unitVATCurr1.setScale(curr1Scale, BigDecimal.ROUND_HALF_UP);
    unitVATCurr2 = unitVATCurr2.setScale(curr2Scale, BigDecimal.ROUND_HALF_UP);
    prdPrice.setUnitVATCurr1( unitVATCurr1 );
    prdPrice.setUnitVATCurr2( unitVATCurr2 );

    // gross unit price
    unitGrossCurr1 = unitNetCurr1.add(unitVATCurr1);
    unitGrossCurr2 = unitNetCurr2.add(unitVATCurr2);
    prdPrice.setUnitGrossCurr1( unitGrossCurr1 );
    prdPrice.setUnitGrossCurr2( unitGrossCurr2 );
    
    // total net price
    totalNetCurr1 = unitNetCurr1.multiply(quantity);
    totalNetCurr2 = unitNetCurr2.multiply(quantity);
    totalNetCurr1 = totalNetCurr1.setScale(curr1Scale, BigDecimal.ROUND_HALF_UP);
    totalNetCurr2 = totalNetCurr2.setScale(curr2Scale, BigDecimal.ROUND_HALF_UP);
    prdPrice.setTotalNetCurr1( totalNetCurr1 );
    prdPrice.setTotalNetCurr2( totalNetCurr2 );
    
    // total VAT
    totalVATCurr1 = unitVATCurr1.multiply(quantity);
    totalVATCurr2 = unitVATCurr2.multiply(quantity);
    totalVATCurr1 = totalVATCurr1.setScale(curr1Scale, BigDecimal.ROUND_HALF_UP);
    totalVATCurr2 = totalVATCurr2.setScale(curr2Scale, BigDecimal.ROUND_HALF_UP);
    prdPrice.setTotalVATCurr1(totalVATCurr1);
    prdPrice.setTotalVATCurr2(totalVATCurr2);

    // total gross price
    totalGrossCurr1 = totalNetCurr1.add(totalVATCurr1);
    totalGrossCurr2 = totalNetCurr2.add(totalVATCurr2);
    totalGrossCurr1 = totalGrossCurr1.setScale(curr1Scale, BigDecimal.ROUND_HALF_UP);
    totalGrossCurr2 = totalGrossCurr2.setScale(curr2Scale, BigDecimal.ROUND_HALF_UP);
    prdPrice.setTotalGrossCurr1( totalGrossCurr1 );
    prdPrice.setTotalGrossCurr2( totalGrossCurr2 );
    
    return prdPrice;
  }
  
  static public boolean isOffer(QueryDataSet dataSet, int customerType) {
    if (Customer.CUSTOMER_TYPE_RETAIL == customerType) {
      return isOffer(dataSet.getString(hotdealFlagRetailColumn),
                     dataSet.getTimestamp(hdBeginDateRetailColumn),
                     dataSet.getTimestamp(hdEndDateRetailColumn),
                     dataSet.getBigDecimal("stockQua"));
    }
    else if (Customer.CUSTOMER_TYPE_WHOLESALE == customerType) {
      return isOffer(dataSet.getString("hotdealFlagW"),
                     dataSet.getTimestamp("hdBeginDateW"),
                     dataSet.getTimestamp("hdEndDateW"),
                     dataSet.getBigDecimal("stockQua"));
    }
    else return false;
  }
  
  static private boolean isOffer(String hotdealFlag,
                                 Timestamp beginDate, Timestamp endDate,
                                 BigDecimal stockQua) {
    Timestamp today = SwissKnife.currentDate();

    boolean haveInStock = stockQua.compareTo( new BigDecimal("0") ) == 1 ? true : false;
    
    boolean validDate = false;

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
  static private String hdRetailPrcColumn = "hdRetailPrc";
  static private String retailPrcEUColumn = "retailPrcEU";
  static private String retailPrcColumn = "retailPrc";
  
  static private String hotdealFlagWholesaleColumn = "hotdealFlagW";
  static private String hdBeginDateWholesaleColumn = "hdBeginDateW";
  static private String hdEndDateWholesaleColumn = "hdEndDateW";
  static private String hdWholesalePrcEUColumn = "hdWholesalePrcEU";
  static private String hdWholesalePrcColumn = "hdWholesalePrc";
  static private String wholesalePrcEUColumn = "wholesalePrcEU";
  static private String wholesalePrcColumn = "wholesalePrc";
  
  private static BigDecimal _zero = new BigDecimal(0);
  private static BigDecimal _oneHundred = new BigDecimal(100);
}