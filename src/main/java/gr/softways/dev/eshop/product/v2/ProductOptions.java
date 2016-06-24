/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gr.softways.dev.eshop.product.v2;

import java.util.Vector;
import java.util.Enumeration;
import java.math.BigDecimal;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

/**
 *
 * @author Panos
 */
public class ProductOptions {
  
  public static ProductOptions getProductOptions(String prdId) {
    ProductOptions productOptions = new ProductOptions();
    
    String databaseId = SwissKnife.jndiLookup("swconf/databaseId");
    
    String query = "SELECT * FROM ProductOptions WHERE PO_prdId = '" + SwissKnife.sqlEncode(prdId) + "' AND PO_Enabled = '1' ORDER BY PO_Order";
    
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
      
      while (queryDataSet.inBounds() == true) {
        ProductOptionsValue productOptionsValue = new ProductOptionsValue();
        
        productOptionsValue.setPO_Code(SwissKnife.sqlDecode(queryDataSet.getString("PO_Code")));
        
        productOptionsValue.setPO_RetailPrcEU(queryDataSet.getBigDecimal("PO_RetailPrcEU"));
        productOptionsValue.setPO_WholesalePrcEU(queryDataSet.getBigDecimal("PO_WholesalePrcEU"));
        productOptionsValue.setPO_RetailOfferPrcEU(queryDataSet.getBigDecimal("PO_RetailOfferPrcEU"));
        productOptionsValue.setPO_WholesaleOfferPrcEU(queryDataSet.getBigDecimal("PO_WholesaleOfferPrcEU"));
        
        productOptionsValue.setValue("PO_Name",SwissKnife.sqlDecode(queryDataSet.getString("PO_Name")));
        productOptionsValue.setValue("PO_NameLG",SwissKnife.sqlDecode(queryDataSet.getString("PO_NameLG")));
        productOptionsValue.setValue("PO_NameLG1",SwissKnife.sqlDecode(queryDataSet.getString("PO_NameLG1")));
        productOptionsValue.setValue("PO_NameLG2",SwissKnife.sqlDecode(queryDataSet.getString("PO_NameLG2")));
        
        productOptions.addProductOptionsValue(productOptionsValue);
        
        queryDataSet.next();
      }
      productOptions.setInBounds(true);
    }
    catch (Exception e) {
      productOptions = null;
    }
    finally {
      if (queryDataSet != null) queryDataSet.close();
    }

    database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    director.freeDBConnection(databaseId, database);
    
    return productOptions;
  }
  
  private void setInBounds(boolean inBounds) {
    _inBounds = inBounds;
  }
  
  public boolean inBounds() {
    return _inBounds;
  }
  
  public void addProductOptionsValue(ProductOptionsValue productOptionsValue) {
    _productOptionsValues.add(productOptionsValue);
  }
  
  public ProductOptionsValue getProductOptionsValue() {
    return (ProductOptionsValue) _productOptionsValues.get(_currentIndex);
  }
  
  public boolean next() {
    _currentIndex++;
    
    if (_currentIndex >= _productOptionsValues.size()) {
      _currentIndex--;
      setInBounds(false);
    }
    else setInBounds(true);
    
   return inBounds();
  }
  
  private ProductOptions() {
  }
  
  private Vector _productOptionsValues = new Vector();
  
  private int _currentIndex = 0;
  
  private boolean _inBounds = false;
}