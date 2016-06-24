package gr.softways.dev.eshop.eways.v3;

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
  
  public static TotalPrice getShippingPrice(Customer customer) {
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
        // get maximum entry for this country
        query = "SELECT FIRST 1 SHCEPrice,SHCEVATPct FROM ShipCostRange,ShipCostEntry "
                 + " WHERE SHCE_SHCRCode = SHCRCode"
                 + "   AND SHCE_countryCode = '" + SwissKnife.sqlEncode(SHCE_countryCode) + "'"
                 + " ORDER BY SHCREnd DESC";
        
        queryDataSet.close();
        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
        queryDataSet.refresh();
      }
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
      if (queryDataSet.getRowCount() == 0) {
        // there is no entry for no country and for this range, get entry for no country and no range
        query = "SELECT FIRST 1 SHCEPrice,SHCEVATPct FROM ShipCostRange,ShipCostEntry "
                 + " WHERE SHCE_SHCRCode = SHCRCode"
                 + "   AND SHCE_countryCode IS NULL"
                 + " ORDER BY SHCREnd DESC";
                 
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
  }
  
  private static BigDecimal _zero = new BigDecimal("0");
  
  private static String _databaseId = SwissKnife.jndiLookup("swconf/databaseId");
}