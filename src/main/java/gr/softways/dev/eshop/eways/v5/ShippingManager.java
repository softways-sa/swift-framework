package gr.softways.dev.eshop.eways.v5;

import java.sql.Timestamp;
import java.math.BigDecimal;
import java.util.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

import gr.softways.dev.eshop.eways.v2.Product;
import gr.softways.dev.eshop.eways.v2.PrdPrice;
import gr.softways.dev.eshop.eways.v2.TotalPrice;

/**
 *
 * @author  minotauros
 */
public class ShippingManager {

  protected ShippingManager() {
  }
  
  /**public static TotalPrice getShippingPrice(Customer customer) {
    TotalPrice shippingPrice = new TotalPrice(),
               orderPrice = customer.getOrder().getOrderPrice();
    
    String SHCE_countryCode = customer.getShippingCountryCode();
    
    int curr1Scale = customer.getCurr1DisplayScale();
    
    String query = "SELECT SHCEPrice,SHCEVATPct FROM ShipCostRange,ShipCostEntry "
                 + " WHERE SHCE_SHCRCode = SHCRCode"
                 + "   AND SHCRStart <= '" + orderPrice.getGrossCurr1().setScale(curr1Scale, BigDecimal.ROUND_HALF_UP) + "'"
                 + "   AND SHCREnd   >= '" + orderPrice.getGrossCurr1().setScale(curr1Scale, BigDecimal.ROUND_HALF_UP) + "'"
                 + "   AND SHCE_countryCode = '" + SwissKnife.sqlEncode(SHCE_countryCode) + "'"
                 + " ORDER BY SHCEPrice DESC";
                 
    //System.out.println(query);
    
    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(_databaseId);
    
    QueryDataSet queryDataSet = null;
    
    try {
      queryDataSet = new QueryDataSet();
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      queryDataSet.refresh();
      
      if (queryDataSet.getRowCount() == 0) {
        // there is no entry for this country, get default entry for range
        query = "SELECT SHCEPrice,SHCEVATPct FROM ShipCostRange,ShipCostEntry "
              + " WHERE SHCE_SHCRCode = SHCRCode"
              + "   AND SHCRStart <= '" + orderPrice.getGrossCurr1().setScale(curr1Scale, BigDecimal.ROUND_HALF_UP) + "'"
              + "   AND SHCREnd   >= '" + orderPrice.getGrossCurr1().setScale(curr1Scale, BigDecimal.ROUND_HALF_UP) + "'"
              + "   AND SHCE_countryCode IS NULL"
              + " ORDER BY SHCEPrice DESC";
              
        queryDataSet.close();
        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
        queryDataSet.refresh();
      }
      
      if (queryDataSet.getRowCount() > 0) {
        shippingPrice.setNetCurr1(queryDataSet.getBigDecimal("SHCEPrice").setScale(curr1Scale, BigDecimal.ROUND_HALF_UP));
        shippingPrice.setVATCurr1(queryDataSet.getBigDecimal("SHCEPrice").setScale(curr1Scale, BigDecimal.ROUND_HALF_UP).multiply(queryDataSet.getBigDecimal("SHCEVATPct")).setScale(curr1Scale, BigDecimal.ROUND_HALF_UP));
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (queryDataSet != null) queryDataSet.close();
    }
    
    director.freeDBConnection(_databaseId, database);
    
    return shippingPrice;
  }**/
  
  public static TotalPrice getShippingPrice(Customer customer) {
    TotalPrice shippingPrice = new TotalPrice(),
               orderPrice = customer.getOrder().getOrderPrice();
    
    String SHCE_countryCode = customer.getShippingCountryCode();
    
    int curr1Scale = customer.getCurr1DisplayScale();
    
    String query = "SELECT SHCEPrice,VAT_Pct FROM ShipCostEntry,VAT "
                 + " WHERE SHCE_VAT_ID = VAT_ID AND SHCECode = '" + SwissKnife.sqlEncode(customer.getOrder().getShippingWay()) + "'";
                 
    //System.out.println(query);
    
    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(_databaseId);
    
    QueryDataSet queryDataSet = null;
    
    try {
      queryDataSet = new QueryDataSet();
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      queryDataSet.refresh();
      
      if (queryDataSet.getRowCount() > 0) {
        shippingPrice.setNetCurr1(queryDataSet.getBigDecimal("SHCEPrice").setScale(curr1Scale, BigDecimal.ROUND_HALF_UP));
        shippingPrice.setVATCurr1(queryDataSet.getBigDecimal("SHCEPrice").setScale(curr1Scale, BigDecimal.ROUND_HALF_UP).multiply(queryDataSet.getBigDecimal("VAT_Pct")).setScale(curr1Scale, BigDecimal.ROUND_HALF_UP));
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (queryDataSet != null) queryDataSet.close();
    }
    
    director.freeDBConnection(_databaseId, database);
    
    return shippingPrice;
  }
  
  public static String[][] getAvailableShipping(Customer customer) {
    String[][] availShipping = null;
    
    Hashtable aShip = new Hashtable();
    
    TotalPrice shippingPrice = new TotalPrice(),
               orderPrice = customer.getOrder().getOrderPrice();
    
    String SHCE_countryCode = customer.getShippingCountryCode(),
        lang = customer.getCustLang();
    
    int curr1Scale = customer.getCurr1DisplayScale();
    
    String query = "";
                 
    //System.out.println(query);
    
    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(_databaseId);
    
    QueryDataSet queryDataSet = null;
    
    int index = 0;
    
    try {
      query = "SELECT SHCEPrice,VAT_Pct,SHCECode,SHCMCode,SHCMTitle,SHCMTitleLG,SHCMText,SHCMTextLG FROM ShipCostRange,ShipCostEntry,ShipCostMethod,VAT "
            + " WHERE SHCE_SHCRCode = SHCRCode"
            + "   AND SHCE_SHCMCode = SHCMCode"
            + "   AND SHCE_VAT_ID = VAT_ID"
            + "   AND SHCRStart <= '" + orderPrice.getGrossCurr1().setScale(curr1Scale, BigDecimal.ROUND_HALF_UP) + "'"
            + "   AND SHCREnd   >= '" + orderPrice.getGrossCurr1().setScale(curr1Scale, BigDecimal.ROUND_HALF_UP) + "'"
            + "   AND SHCE_countryCode IS NULL"
            + " ORDER BY SHCEPrice DESC";
      
      
      queryDataSet = new QueryDataSet();
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();
      
      while (queryDataSet.inBounds()) {
        String[][] availShippingTemp = new String[1][5];
        
        availShippingTemp[0][0] = SwissKnife.sqlDecode( queryDataSet.getString("SHCECode") );
        availShippingTemp[0][1] = SwissKnife.sqlDecode( queryDataSet.getString("SHCMCode") );
        availShippingTemp[0][2] = SwissKnife.sqlDecode( queryDataSet.getString("SHCMTitle" + lang) );
        availShippingTemp[0][3] = String.valueOf( queryDataSet.getBigDecimal("SHCEPrice").setScale(curr1Scale, BigDecimal.ROUND_HALF_UP).add( queryDataSet.getBigDecimal("SHCEPrice").setScale(curr1Scale, BigDecimal.ROUND_HALF_UP).multiply(queryDataSet.getBigDecimal("VAT_Pct")).setScale(curr1Scale, BigDecimal.ROUND_HALF_UP) ) );
        availShippingTemp[0][4] = SwissKnife.sqlDecode( queryDataSet.getString("SHCMText" + lang) );
        
        aShip.put(SwissKnife.sqlDecode(queryDataSet.getString("SHCMCode")), availShippingTemp);
        
        queryDataSet.next();
      }
      
      query = "SELECT SHCEPrice,VAT_Pct,SHCECode,SHCMCode,SHCMTitle,SHCMTitleLG,SHCMText,SHCMTextLG FROM ShipCostRange,ShipCostEntry,ShipCostMethod,VAT "
          + " WHERE SHCE_SHCRCode = SHCRCode"
          + "   AND SHCE_SHCMCode = SHCMCode"
          + "   AND SHCE_VAT_ID = VAT_ID"
          + "   AND SHCRStart <= '" + orderPrice.getGrossCurr1().setScale(curr1Scale, BigDecimal.ROUND_HALF_UP) + "'"
          + "   AND SHCREnd   >= '" + orderPrice.getGrossCurr1().setScale(curr1Scale, BigDecimal.ROUND_HALF_UP) + "'"
          + "   AND SHCE_countryCode = '" + SwissKnife.sqlEncode(SHCE_countryCode) + "'"
          + " ORDER BY SHCEPrice DESC";

      queryDataSet.close();
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();
      
      while (queryDataSet.inBounds()) {
        String[][] availShippingTemp = new String[1][5];
        
        availShippingTemp[0][0] = SwissKnife.sqlDecode( queryDataSet.getString("SHCECode") );
        availShippingTemp[0][1] = SwissKnife.sqlDecode( queryDataSet.getString("SHCMCode") );
        availShippingTemp[0][2] = SwissKnife.sqlDecode( queryDataSet.getString("SHCMTitle" + lang) );
        availShippingTemp[0][3] = String.valueOf( queryDataSet.getBigDecimal("SHCEPrice").setScale(curr1Scale, BigDecimal.ROUND_HALF_UP).add( queryDataSet.getBigDecimal("SHCEPrice").setScale(curr1Scale, BigDecimal.ROUND_HALF_UP).multiply(queryDataSet.getBigDecimal("VAT_Pct")).setScale(curr1Scale, BigDecimal.ROUND_HALF_UP) ) );
        availShippingTemp[0][4] = SwissKnife.sqlDecode( queryDataSet.getString("SHCMText" + lang) );
        
        aShip.put(SwissKnife.sqlDecode(queryDataSet.getString("SHCMCode")), availShippingTemp);
        
        queryDataSet.next();
      }
      
      availShipping = new String[aShip.size()][5];
      for (Enumeration e = aShip.elements() ; e.hasMoreElements() ;) {
        String[][] availShippingTemp = (String[][])e.nextElement();
        
        availShipping[index][0] = availShippingTemp[0][0];
        availShipping[index][1] = availShippingTemp[0][1];
        availShipping[index][2] = availShippingTemp[0][2];
        availShipping[index][3] = availShippingTemp[0][3];
        availShipping[index][4] = availShippingTemp[0][4];
       
        index++;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (queryDataSet != null) queryDataSet.close();
    }
    
    director.freeDBConnection(_databaseId, database);
    
    return availShipping;
  }
  
  private static BigDecimal _zero = new BigDecimal("0");
  
  private static String _databaseId = SwissKnife.jndiLookup("swconf/databaseId");
}