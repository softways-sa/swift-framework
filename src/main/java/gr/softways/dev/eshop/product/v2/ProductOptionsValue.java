/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gr.softways.dev.eshop.product.v2;

import java.util.*;
import java.math.BigDecimal;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

/**
 *
 * @author Panos
 */
public class ProductOptionsValue {
  
  public static ProductOptionsValue getProductOptionsValue(String PO_Code) {
    return getProductOptionsValue(PO_Code, null);
  }
  
  public static ProductOptionsValue getProductOptionsValue(String PO_Code, String PO_prdId) {
    ProductOptionsValue productOptionsValue = new ProductOptionsValue();
    
    String databaseId = SwissKnife.jndiLookup("swconf/databaseId");
    
    String query = "SELECT * FROM product,ProductOptions"
        + " WHERE PO_prdId = prdId"
        + " AND PO_Code = '" + SwissKnife.sqlEncode(PO_Code) + "' AND PO_Enabled = '1'";
    
    if (PO_prdId != null && PO_prdId.length()>0) query += " AND PO_prdId = '" + SwissKnife.sqlEncode(PO_prdId) + "'";
    
    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(databaseId);
    QueryDataSet queryDataSet = null;
    
    DbRet dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();
    
    try {
      queryDataSet = new QueryDataSet();
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();
      
      if (queryDataSet.getRowCount() == 0) throw new Exception();

      productOptionsValue.setPO_Code(SwissKnife.sqlDecode(queryDataSet.getString("PO_Code")));

      if (_zero.compareTo(queryDataSet.getBigDecimal("PO_RetailPrcEU")) == 0) productOptionsValue.setPO_RetailPrcEU(queryDataSet.getBigDecimal("retailPrcEU"));
      else productOptionsValue.setPO_RetailPrcEU(queryDataSet.getBigDecimal("PO_RetailPrcEU"));
      
      if (_zero.compareTo(queryDataSet.getBigDecimal("PO_WholesalePrcEU")) == 0) productOptionsValue.setPO_WholesalePrcEU(queryDataSet.getBigDecimal("wholesalePrcEU"));
      else productOptionsValue.setPO_WholesalePrcEU(queryDataSet.getBigDecimal("PO_WholesalePrcEU"));
      
      if (_zero.compareTo(queryDataSet.getBigDecimal("PO_RetailOfferPrcEU")) == 0) productOptionsValue.setPO_RetailOfferPrcEU(queryDataSet.getBigDecimal("hdRetailPrcEU"));
      else productOptionsValue.setPO_RetailOfferPrcEU(queryDataSet.getBigDecimal("PO_RetailOfferPrcEU"));
      
      if (_zero.compareTo(queryDataSet.getBigDecimal("PO_WholesaleOfferPrcEU")) == 0) productOptionsValue.setPO_WholesaleOfferPrcEU(queryDataSet.getBigDecimal("hdWholesalePrcEU"));
      else productOptionsValue.setPO_WholesaleOfferPrcEU(queryDataSet.getBigDecimal("PO_WholesaleOfferPrcEU"));

      productOptionsValue.setValue("PO_Name",SwissKnife.sqlDecode(queryDataSet.getString("PO_Name")));
      productOptionsValue.setValue("PO_NameLG",SwissKnife.sqlDecode(queryDataSet.getString("PO_NameLG")));
      productOptionsValue.setValue("PO_NameLG1",SwissKnife.sqlDecode(queryDataSet.getString("PO_NameLG1")));
      productOptionsValue.setValue("PO_NameLG2",SwissKnife.sqlDecode(queryDataSet.getString("PO_NameLG2")));
    }
    catch (Exception e) {
      productOptionsValue = null;
    }
    finally {
      if (queryDataSet != null) queryDataSet.close();
    }

    database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    director.freeDBConnection(databaseId, database);
    
    return productOptionsValue;
  }
  
  public void setPO_RetailPrcEU(BigDecimal PO_RetailPrcEU) {
    _PO_RetailPrcEU = PO_RetailPrcEU;
  }
  public BigDecimal getPO_RetailPrcEU() {
    return _PO_RetailPrcEU;
  }
  
  public void setPO_WholesalePrcEU(BigDecimal PO_WholesalePrcEU) {
    _PO_WholesalePrcEU = PO_WholesalePrcEU;
  }
  public BigDecimal getPO_WholesalePrcEU() {
    return _PO_WholesalePrcEU;
  }
  
  public void setPO_RetailOfferPrcEU(BigDecimal PO_RetailOfferPrcEU) {
    _PO_RetailOfferPrcEU = PO_RetailOfferPrcEU;
  }
  public BigDecimal getPO_RetailOfferPrcEU() {
    return _PO_RetailOfferPrcEU;
  }
  
  public void setPO_WholesaleOfferPrcEU(BigDecimal PO_WholesaleOfferPrcEU) {
    _PO_WholesaleOfferPrcEU = PO_WholesaleOfferPrcEU;
  }
  public BigDecimal getPO_WholesaleOfferPrcEU() {
    return _PO_WholesaleOfferPrcEU;
  }
  
  public void setValue(String key, String value) {
    _values.put(key, value);
  }
  public String getValue(String key) {
    return (String) _values.get(key);
  }
  
  public void setPO_Code(String PO_Code) {
    _PO_Code = PO_Code;
  }
  public String getPO_Code() {
    return _PO_Code;
  }
  
  public ProductOptionsValue() {
  }
  
  private static BigDecimal _zero = new BigDecimal("0");
      
  private BigDecimal _PO_RetailPrcEU = null,
      _PO_WholesalePrcEU = null,
      _PO_RetailOfferPrcEU = null,
      _PO_WholesaleOfferPrcEU = null;
  
  private Hashtable _values = new Hashtable();
  
  private String _PO_Code = null;
}