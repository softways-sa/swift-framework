package gr.softways.dev.eshop.eways.v4;

import java.io.*;
import java.util.*;

import java.math.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

import gr.softways.dev.eshop.eways.v2.Product;
import gr.softways.dev.eshop.eways.v2.PrdPrice;
import gr.softways.dev.eshop.eways.v2.TotalPrice;
import gr.softways.dev.eshop.eways.v2.ProductAttribute;
import gr.softways.dev.eshop.product.v2.ProductOptionsValue;

/**
 *
 * @author  Administrator
 * @version 
 */
public class Transaction extends JSPBean {
  
  private Customer _customer = null;
  private Order _order = null;
  
  private BigDecimal _zero = new BigDecimal("0");
  
  public Transaction() {
  }
  
  public DbRet doOrder(Customer customer, String status) {
    DbRet dbRet = new DbRet();

    Director director = Director.getInstance();
    
    dbRet.setAuthErrorCode(director.auth(databaseId,authUsername,authPassword,"transactions",Director.AUTH_INSERT));

    if (dbRet.getAuthErrorCode() != Director.AUTH_OK) {
      dbRet.setAuthError(1);
      dbRet.setNoError(0);

      return dbRet;
    }

    _customer = customer;
    
    Product product = null;

    Database database = null;
    
    database = director.getDBConnection(databaseId);

    String query = "";

    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();

    String orderId = null;
    Timestamp orderDate = null;
      
    try {
      if (_customer == null) {
        dbRet.setNoError(0);
      }

      if (dbRet.getNoError() == 1) {
        _order = _customer.getOrder();

        if (_order == null) {
          dbRet.setNoError(0);
        }
      }

      query = "UPDATE lTab SET lTabFld = '1' WHERE lTabCode = '1'";

      /** { loop for interbase transaction conflict **/
      if (dbRet.getNoError() == 1) {
        dbRet.setNoError(0);
        for (int i=0; i<10 && dbRet.getNoError() == 0; i++) {
          dbRet = database.execQuery(query);
        }
      }
      /** } loop for interbase transaction conflict **/

      if (dbRet.getNoError() == 1) {
        dbRet = doOrderTable(database, status);

        orderId = dbRet.getRetStr();
        orderDate = dbRet.getRetTs();
      }

      int productCount = _order.getOrderLines();

      if (dbRet.getNoError() == 1) {
        for (int i=0; i<productCount && dbRet.getNoError() == 1; i++) {
          product = _order.getProductAt(i);

          dbRet = doTransactionTable(orderId, i+1, orderDate, status, product, database);
        }
      }

      if (dbRet.getNoError() == 1) {
        for (int i=0; i<productCount && dbRet.getNoError() == 1; i++) {
          product = _order.getProductAt(i);

          dbRet = doProduct(product, database);
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      dbRet.setNoError(0);
    }
    finally {
      dbRet = database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
      
      director.freeDBConnection(databaseId, database);
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet.setRetStr(orderId);
    }
      
    return dbRet;
  }
  
  private DbRet doOrderTable(Database database, String status) {
    DbRet dbRet = new DbRet();

    String query = null, orderId = null;

    int ordYear = 0;
    int ordAA = 0;
    
    Timestamp orderDate = null;
    
    String customerId = SwissKnife.sqlEncode(_customer.getCustomerId()),
           documentType = SwissKnife.sqlEncode(_order.getDocumentType()),
           
           shippingWay = SwissKnife.sqlEncode(_order.getShippingWay()),
           
           afm = SwissKnife.sqlEncode(_customer.getBillingAfm()),
           doy = SwissKnife.sqlEncode(_customer.getBillingDoy()),
           firstname = SwissKnife.sqlEncode(_customer.getFirstname()),
           lastname = SwissKnife.sqlEncode(_customer.getLastname()),
           occupation = SwissKnife.sqlEncode(_customer.getOccupation()),
           companyName = SwissKnife.sqlEncode(_customer.getBillingName()),
           shippingName = SwissKnife.sqlEncode(_customer.getShippingName()),
           shippingAddress = SwissKnife.sqlEncode(_customer.getShippingAddress()),
           billingAddress = SwissKnife.sqlEncode(_customer.getBillingAddress()),
           shippingArea = SwissKnife.sqlEncode(_customer.getShippingArea()),
           billingArea = SwissKnife.sqlEncode(_customer.getBillingArea()),
           
           shippingCity = SwissKnife.sqlEncode(_customer.getShippingCity()),
           billingCity = SwissKnife.sqlEncode(_customer.getBillingCity()),
           
           shippingDistrict = SwissKnife.sqlEncode(_customer.getShippingDistrict()),
           shippingLocation = SwissKnife.sqlEncode(_customer.getShippingLocation()),
           
           shippingRegion = SwissKnife.sqlEncode(_customer.getShippingRegion()),
           billingRegion = SwissKnife.sqlEncode(_customer.getBillingRegion()),
           shippingCountry = SwissKnife.sqlEncode(_customer.getShippingCountry()),
           billingCountry = SwissKnife.sqlEncode(_customer.getBillingCountry()),
           shippingZipCode = SwissKnife.sqlEncode(_customer.getShippingZipCode()),
           billingZipCode = SwissKnife.sqlEncode(_customer.getBillingZipCode()),
           shippingPhone = SwissKnife.sqlEncode(_customer.getShippingPhone()),
           phone = SwissKnife.sqlEncode(_customer.getBillingPhone()),
           email = SwissKnife.sqlEncode(_customer.getEmail()),
           billingProfession = SwissKnife.sqlEncode(_customer.getBillingProfession()),
           title = SwissKnife.sqlEncode(_customer.getTitle()),
           sex = SwissKnife.sqlEncode(_customer.getSex()),
           ordPayWay = SwissKnife.sqlEncode(_order.getOrdPayWay()),
           ordGiftCardMsg = "",
           ordPrefNotes = SwissKnife.sqlEncode(_customer.getOrdPrefNotes());
    
    Timestamp deliveryDate = _customer.getShippingDeliveryDate();
    
    BigDecimal ordPTTotalRate = _customer.getDiscountPct();
    if (ordPTTotalRate == null) ordPTTotalRate = new BigDecimal("0");

    String giftFlag = "", memberFlag = "";
    
    if (customerId != null && customerId.trim().length()>0) {
      memberFlag = "1";
    }
    else {
      memberFlag = "0";
    }
    
    TotalPrice orderPrice = _order.getOrderPrice();
    
    BigDecimal valueDR = _zero,
               valueEU = orderPrice.getNetCurr1(),
               vatVal = _zero,
               vatValEU = orderPrice.getVATCurr1();

    TotalPrice shippingPrice = _order.getShippingPrice();

    BigDecimal shippingValue = _zero,
               shippingValueEU = shippingPrice.getNetCurr1(),
               shippingVatVal = _zero,
               shippingVatValEU = shippingPrice.getVATCurr1();
    
    PreparedStatement ps = null;

    QueryDataSet queryDataSet = null;
    
    try {
      orderDate = SwissKnife.currentDate();
      
      ordYear = SwissKnife.getTDateInt(orderDate, "YEAR");
      
      query = "SELECT MAX(ordAA) FROM orders WHERE ordYear = " + ordYear;
      queryDataSet = new QueryDataSet();
      
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();
      
      if (queryDataSet.isNull(0) == true) ordAA = 1;
      else ordAA = queryDataSet.getInt(0) + 1;
      
      query = "INSERT INTO orders ("
            + " orderId, orderDate, customerId, documentType"
            + ",quantity, valueDR, valueEU, vatVal, vatValEU"
            + ",shippingValue, shippingValueEU, shippingVatVal"
            + ",shippingVatValEU"
            + ",shippingWayZone, status, memberFlag, giftFlag"
            + ",afm, doy, firstname, lastname, occupation, companyName"
            + ",shippingName, shippingAddress, billingAddress"
            + ",shippingArea, billingArea, shippingCity"
            + ",billingCity, shippingRegion, billingRegion"
            + ",shippingCountry, billingCountry, shippingZipCode"
            + ",billingZipCode, shippingPhone, billingPhone"
            + ",email, profession, title, sex, ordPayWay"
            + ",ordGiftCardMsg,ordPTTotalRate,ordPrefNotes,deliveryDate"
            + ",ordYear,ordAA,shippingDistrict,shippingLocation"
            + ") VALUES ("
            + " ?,?,?,?,?,?,?,?,?,?"
            + ",?,?,?,?,?,?,?,?,?,?"
            + ",?,?,?,?,?,?,?,?,?,?"
            + ",?,?,?,?,?,?,?,?,?,?"
            + ",?,?,?,?,?,?,?,?,?,?"
            + ",?"
            + ")";
            
      ps = database.createPreparedStatement(query);
      
      ps.setTimestamp(2, orderDate);
      ps.setString(3,customerId);
      ps.setInt(4,Integer.parseInt(documentType));
      ps.setBigDecimal(5,new BigDecimal(String.valueOf(_order.getProductCount())));
      ps.setBigDecimal(6,valueDR);
      ps.setBigDecimal(7,valueEU);
      ps.setBigDecimal(8,vatVal);
      ps.setBigDecimal(9,vatValEU);
      ps.setBigDecimal(10,shippingValue);
      ps.setBigDecimal(11,shippingValueEU);
      ps.setBigDecimal(12,shippingVatVal);
      ps.setBigDecimal(13,shippingVatValEU);
      ps.setString(14,shippingWay);
      ps.setString(15,status);
      ps.setString(16,memberFlag);
      ps.setString(17,giftFlag);
      ps.setString(18,afm);
      ps.setString(19,doy);
      ps.setString(20,firstname);
      ps.setString(21,lastname);
      ps.setString(22,occupation);
      ps.setString(23,companyName);
      ps.setString(24,shippingName);
      ps.setString(25,shippingAddress);
      ps.setString(26,billingAddress);
      ps.setString(27,shippingArea);
      ps.setString(28,billingArea);
      ps.setString(29,shippingCity);
      ps.setString(30,billingCity);
      ps.setString(31,shippingRegion);
      ps.setString(32,billingRegion);
      ps.setString(33,shippingCountry);
      ps.setString(34,billingCountry);
      ps.setString(35,shippingZipCode);
      ps.setString(36,billingZipCode);
      ps.setString(37,shippingPhone);
      ps.setString(38,phone);
      ps.setString(39,email);
      ps.setString(40,billingProfession);
      ps.setString(41,title);
      ps.setString(42,sex);
      ps.setString(43,ordPayWay);
      
      ps.setString(44,ordGiftCardMsg);
      ps.setBigDecimal(45,ordPTTotalRate);
      ps.setString(46,ordPrefNotes);
      
      if (deliveryDate == null) ps.setNull(47, Types.TIMESTAMP);
      else ps.setTimestamp(47, deliveryDate);
      
      ps.setInt(48,ordYear);
      ps.setInt(49,ordAA);
      
      ps.setString(50,shippingDistrict);
      ps.setString(51,shippingLocation);
      
      orderId = SwissKnife.buildPK();
      
      ps.setString(1,orderId);
      
      ps.executeUpdate();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    finally {
      try { if (ps != null) ps.close(); } catch (Exception e) { }
      
      try { if (queryDataSet != null) queryDataSet.close(); } catch (Exception e) { }
    }

    dbRet.setRetStr(orderId);
    dbRet.setRetTs(orderDate);
    
    return dbRet;
  }
  
  /**
   * Simple products with or without 2 attributes.
   *
   */
  private DbRet doTransactionTable(String orderId, int transId, Timestamp orderDate, String status, Product product, Database database) {
    DbRet dbRet = null;
        
    String query = null, hotDealFlag = null;
    
    String inOutFlag = "1";

    if (product.isOffer() == true) {
      hotDealFlag = "1";
    }
    else {
      hotDealFlag = "0";
    }
    
    PrdPrice prdPrice = product.getPrdPrice();
    ProductOptionsValue productOptionsValue = product.getProductOptionsValue();
    
    BigDecimal unitPrc = null, uniPrcEU = null,
               valueDR = null, valueEU = null,
               vatVal = null, vatValEU = null;
    
    unitPrc = _zero;
    uniPrcEU = prdPrice.getUnitNetCurr1();
    valueDR = _zero;
    valueEU = prdPrice.getTotalNetCurr1();
    vatVal = _zero;
    vatValEU = prdPrice.getTotalVATCurr1();
    
    String transPO_Name = "", transPO_NameLG = "", transPO_NameLG1 = "", transPO_NameLG2 = "";
    
    if (productOptionsValue != null) {
      transPO_Name = SwissKnife.sqlEncode(productOptionsValue.getValue("PO_Name"));
      transPO_NameLG = SwissKnife.sqlEncode(productOptionsValue.getValue("PO_NameLG"));
      transPO_NameLG1 = SwissKnife.sqlEncode(productOptionsValue.getValue("PO_NameLG1"));
      transPO_NameLG2 = SwissKnife.sqlEncode(productOptionsValue.getValue("PO_NameLG2"));
    }
        
    String transPrdAttCode = "",
           attAttCode = product.getAttAttCode(),
           att2AttCode = product.getAtt2AttCode(),
           transPrdAttAtt1 = product.getAttName(),
           transPrdAttAtt2 = product.getAtt2Name(),
           transPrdAttAtt3 = product.getStamp();
           
    if (attAttCode.length()>0 && att2AttCode.length()>0) {
      transPrdAttCode = attAttCode + "~" + att2AttCode;
    }
    
    query = "INSERT INTO transactions ("
          + " transId, orderId, prdId, quantity"
          + ",unitPrc, unitPrcEU, valueDR, valueEU"
          + ",vatVal, vatValEU, orderDate, status"
          + ",inOutFlag, hotdealFlag, transPrdAttCode, transPrdAttAtt1"
          + ",transPrdAttAtt2, transPrdAttAtt3"
          + ",transPO_Name, transPO_NameLG, transPO_NameLG1, transPO_NameLG2"
          + ") VALUES ("
          + " "  + transId + ""
          + ",'" + orderId + "'"
          + ",'" + product.getPrdId() + "'"
          + ","  + product.getQuantity() + ""
          + ","  + unitPrc + ""
          + ","  + uniPrcEU + ""
          + ","  + valueDR + ""
          + ","  + valueEU + ""
          + ","  + vatVal + ""
          + ","  + vatValEU + ""
          + ",'" + orderDate + "'"
          + ",'" + status + "'"
          + ",'" + inOutFlag + "'"
          + ",'" + hotDealFlag + "'"
          + ",'" + transPrdAttCode + "'"
          + ",'" + transPrdAttAtt1 + "'"
          + ",'" + transPrdAttAtt2 + "'"
          + ",'" + transPrdAttAtt3 + "'"
          + ",'" + transPO_Name + "'"
          + ",'" + transPO_NameLG + "'"
          + ",'" + transPO_NameLG1 + "'"
          + ",'" + transPO_NameLG2 + "'"
          + ")";
        
    dbRet = database.execQuery(query);

    if (dbRet.getNoError()==1) {
      dbRet = doTransAttribute(orderId, transId, product, database);
    }
    
    return dbRet;
  }
  
  private DbRet doProduct(Product product, Database database) {
    QueryDataSet queryDataSet = null;
    
    DbRet dbRet = null;
    
    PrdPrice prdPrice = null;
    
    BigDecimal quantity = null, value = null, valueEU = null, vatVal = null, vatValEU = null;
    BigDecimal stockQua = null, inQua = null, outQua = null, inVal = null,
               inValEU = null, outVal = null, outValEU = null;
    
    String query = "UPDATE product SET prdLock = '1' WHERE prdId = '" + product.getPrdId() + "'";
    
    dbRet = database.execQuery(query);
    
    if (dbRet.getNoError() == 1) {
      query = "SELECT * FROM product WHERE prdId= '" + product.getPrdId() + "'";
      
      try {
        queryDataSet = new QueryDataSet();
        
        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        
        queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
        
        // εκτέλεσε το query
        queryDataSet.refresh();
        
        stockQua = queryDataSet.getBigDecimal("stockQua");
        inQua = queryDataSet.getBigDecimal("inQua");
        outQua = queryDataSet.getBigDecimal("outQua");
        inVal = queryDataSet.getBigDecimal("inVal");
        inValEU = queryDataSet.getBigDecimal("inValEU");
        outVal = queryDataSet.getBigDecimal("outVal");
        outValEU = queryDataSet.getBigDecimal("outValEU");

        /* Ετοίμασε τα δεδομένα του UPDATE */
        prdPrice = product.getPrdPrice();
              
        value = _zero;
        valueEU = prdPrice.getTotalNetCurr1();
        
        quantity = product.getQuantity();
        
        stockQua = stockQua.subtract(quantity);
        outQua = outQua.add(quantity);
        outVal = outVal.add(value);
        outValEU = outValEU.add(valueEU);
      }
      catch (DataSetException e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
      finally {
        try { queryDataSet.close(); } catch (Exception e) { }
      }
    }  
      
    if (dbRet.getNoError() == 1) {
      query = "UPDATE product SET" 
            + " stockQua = " + stockQua + "," 
            + " outQua = " + outQua + "," 
            + " outVal = " + outVal + "," 
            + " outValEU = " + outValEU + "," 
            + " prdLock = '0'"
            + " WHERE prdId = '" + product.getPrdId() + "'";
      
      dbRet = database.execQuery(query);
    }
    
    return dbRet;
  }
  
  private DbRet doTransAttribute(String orderId,int transId,Product product,Database database) {
    DbRet dbRet = new DbRet();
    String query = null;
    Vector PAVector = product.getPAVector();
    ProductAttribute pa = null;
    for (int i=0; i<PAVector.size() && dbRet.getNoError()==1; i++) {
      pa = (ProductAttribute)PAVector.elementAt(i);
      query = "INSERT INTO transAttribute ("
          + " TAVCode, TAV_transId, TAV_orderId, TAVHasPrice"
          + ",TAVKeepStock, TAVAtrName, TAV_PMAVCode, TAVValue"
          + ",TAVSlaveFlag, TAVStock, TAVPrice";
          
      if (pa.getPMAVID() != null) query += ",TAVID";
      
      query += ") VALUES ("
          + " '"  + SwissKnife.buildPK() + "'"
          + "," + transId + ""
          + ",'" + orderId + "'"
          + ","  + pa.getHasPrice() + ""
          + ","  + pa.getKeepStock() + ""
          + ",'"  + pa.getAtrName() + "'"
          + ",'"  + pa.getPMAVCode() + "'"
          + ",'"  + pa.getATVAValue() + "'"
          + ","  + pa.getSlaveFlag() + ""
          + ","  + pa.getPMAVStock() + ""
          + "," + pa.getPMAVPrice() + "";
          
      if (pa.getPMAVID() != null) query += ",'" + SwissKnife.sqlEncode(pa.getPMAVID()) + "'";
      
      query += ")";
      
      dbRet = database.execQuery(query);
      if (dbRet.getNoError() == 1 && pa.getKeepStock() == 1) {
        if (pa.getSlaveFlag()==1)
          dbRet = updatePMASVStock(pa, product, database);
        else
          dbRet = updatePMAVStock(pa, product, database);
      }
    }
    return dbRet;
  }
  
  private DbRet updatePMAVStock(ProductAttribute pa, Product product, Database database) {
    DbRet dbRet1, dbRet3 = null;
    DbRet dbRet2 = new DbRet();
    QueryDataSet queryDataSet = null;
    BigDecimal stockQua = null;
    
    String query = "UPDATE productMasterAttributeValue SET PMAVLock = '1'"
                 + " WHERE PMAVCode = '" + pa.getPMAVCode() + "'";
    dbRet1 = database.execQuery(query);
    
    if (dbRet1.getNoError() == 1) {
      query = "SELECT PMAVCode, PMAVStock FROM productMasterAttributeValue"
            + " WHERE PMAVCode= '" + pa.getPMAVCode() + "'";
      
      try {
        queryDataSet = new QueryDataSet();
        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
        queryDataSet.refresh();
        
        stockQua = queryDataSet.getBigDecimal("PMAVStock");
        if (stockQua != null)
          stockQua = stockQua.subtract(product.getQuantity());
        else
          stockQua = new BigDecimal("0");
      }
      catch (DataSetException e) {
        dbRet2.setNoError(0);
        e.printStackTrace();
      }
      finally {
        try { queryDataSet.close(); } catch (Exception e) { }
      }
    }  
      
    if (dbRet1.getNoError() == 1 && dbRet2.getNoError() == 1) {
      query = "UPDATE productMasterAttributeValue SET" 
            + " PMAVStock = " + stockQua + "" 
            + " WHERE PMAVCode = '" + pa.getPMAVCode() + "'";
      
      dbRet1 = database.execQuery(query);
    }
    
    if (dbRet3.getNoError() == 0)
      return dbRet3;
    else
      return dbRet2;
  }
  
  private DbRet updatePMASVStock(ProductAttribute pa, Product product, Database database) {
    DbRet dbRet1 = null;
    DbRet dbRet2 = new DbRet();
    DbRet dbRet3 = new DbRet();
    QueryDataSet queryDataSet = null;
    BigDecimal stockQua = null;
    String query = "UPDATE PMASV SET PMASVLock = '1'"
                 + " WHERE PMASVCode = '" + pa.getPMAVCode() + "'";
    dbRet1 = database.execQuery(query);
    
    if (dbRet1.getNoError() == 1) {
      query = "SELECT PMASVCode, PMASVStock FROM PMASV"
            + " WHERE PMASVCode= '" + pa.getPMAVCode() + "'";
      
      try {
        queryDataSet = new QueryDataSet();
        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
        queryDataSet.refresh();
        
        stockQua = queryDataSet.getBigDecimal("PMASVStock");
        if (stockQua != null)
          stockQua = stockQua.subtract(product.getQuantity());
        else
          stockQua = new BigDecimal("0");
      }
      catch (DataSetException e) {
        dbRet2.setNoError(0);
        e.printStackTrace();
      }
      finally {
        try { queryDataSet.close(); } catch (Exception e) { }
      }
    }  
      
    if (dbRet1.getNoError() == 1 && dbRet2.getNoError() == 1) {
      query = "UPDATE PMASV SET" 
            + " PMASVStock = " + stockQua + "" 
            + " WHERE PMASVCode = '" + pa.getPMAVCode() + "'";
      
      dbRet3 = database.execQuery(query);
    }
    
    if (dbRet3.getNoError() == 0)
      return dbRet3;
    else
      return dbRet2;
  }
  
  public DbRet updateBankRef(String orderId, String bankRef) {
    DbRet dbRet = new DbRet();

    Director director = Director.getInstance();
    
    Database database = null;
    
    database = director.getDBConnection(databaseId);

    String query = "UPDATE orders"
                 + " SET ordBankTran = '" + SwissKnife.sqlEncode(bankRef) + "'"
                 + " WHERE orderId = '" + orderId + "'";
    
    dbRet = database.execQuery(query);
 
    director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
}