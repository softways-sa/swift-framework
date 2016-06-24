package gr.softways.dev.eshop.eways.v5;

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

    String query = null;

    int ordYear = 0;
    int ordAA = 0;
    
    Timestamp orderDate = null;
    
    String orderId = _order.getOrderId(),
           customerId = SwissKnife.sqlEncode(_customer.getCustomerId()),
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
           ordPrefNotes = SwissKnife.sqlEncode(_customer.getOrdPrefNotes()),
           ordLang = SwissKnife.sqlEncode(_customer.getCustLang());
    
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
      queryDataSet.close();
      
      query = "SELECT SHCMTitle,SHCMTitleLG FROM ShipCostMethod,ShipCostEntry WHERE SHCE_SHCMCode = SHCMCode AND SHCECode = '" + shippingWay + "'";
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();
      
      String ord_ShipMethodTitle = queryDataSet.getString("SHCMTitle");
      String ord_ShipMethodTitleLG = queryDataSet.getString("SHCMTitleLG");
      
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
            + ",ordYear,ordAA,shippingDistrict,shippingLocation,ordLang,ord_ShipMethodTitle,ord_ShipMethodTitleLG"
            + ") VALUES ("
            + " ?,?,?,?,?,?,?,?,?,?"
            + ",?,?,?,?,?,?,?,?,?,?"
            + ",?,?,?,?,?,?,?,?,?,?"
            + ",?,?,?,?,?,?,?,?,?,?"
            + ",?,?,?,?,?,?,?,?,?,?"
            + ",?,?,?,?"
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
      ps.setString(52,ordLang);
      
      ps.setString(53,ord_ShipMethodTitle);
      ps.setString(54,ord_ShipMethodTitleLG);
      
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
    
    String transPRD_GiftWrap = "0";
    
    if (product.isGiftWrap() == true) transPRD_GiftWrap = "1";
        
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
    
    String transPO_Name = "", transPO_NameLG = "", 
        transPO_NameLG1 = "", transPO_NameLG2 = "", transPrdAId = "";
    
    if (productOptionsValue != null) {
      transPO_Name = SwissKnife.sqlEncode(productOptionsValue.getValue("PO_Name"));
      transPO_NameLG = SwissKnife.sqlEncode(productOptionsValue.getValue("PO_NameLG"));
      transPO_NameLG1 = SwissKnife.sqlEncode(productOptionsValue.getValue("PO_NameLG1"));
      transPO_NameLG2 = SwissKnife.sqlEncode(productOptionsValue.getValue("PO_NameLG2"));
      
      transPrdAId = productOptionsValue.getPO_Code();
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
        + ",transPO_Name, transPO_NameLG, transPO_NameLG1, transPO_NameLG2,transPRD_GiftWrap"
        + ",transPrdAId"
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
        + ",'" + transPRD_GiftWrap + "'"
        + ",'" + transPrdAId + "'"
        + ")";
        
    dbRet = database.execQuery(query);

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
      
      if (_inventoryType != null && _inventoryType.equals(gr.softways.dev.eshop.eways.v2.Order.STOCK_INVENTORY)) {
        query += " AND stockQua > 0";
      }
      
      try {
        queryDataSet = new QueryDataSet();
        
        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        
        queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
        
        // εκτέλεσε το query
        queryDataSet.refresh();
        
        if (queryDataSet.isEmpty() == true) throw new Exception("Product not found.");
        
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
      catch (Exception e) {
        dbRet.setNoError(0);
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
  
  public static DbRet updateBank(String orderId, String ordBankTran, String status) {
    DbRet dbRet = new DbRet();

    Director director = Director.getInstance();
    
    Database database = null;
    
    PreparedStatement statement = null;
    
    int index = 0, rowsAffected = 0 , prevTransIsolation = 0;
    
    String query = null;
    
    try {
      database = director.getDBConnection(_databaseId);
      dbRet = database.beginTransaction(Database.TRANS_ISO_1);
      
      prevTransIsolation = dbRet.getRetInt();
    
      query = "UPDATE orders SET"
          + " ordBankTran = ?"
          + ",status = ?"
          + " WHERE orderId = ?";
      
      statement = database.createPreparedStatement(query);
      
      statement.setString(++index, SwissKnife.sqlEncode(ordBankTran));
      statement.setString(++index, SwissKnife.sqlEncode(status));
      statement.setString(++index, SwissKnife.sqlEncode(orderId));
     
      rowsAffected = statement.executeUpdate();
      
      if (rowsAffected == 0) throw new Exception();
      
      index = 0;
      
      query = "UPDATE transactions SET"
          + " status = ?"
          + " WHERE orderId = ?";
      
      statement = database.createPreparedStatement(query);
      
      statement.setString(++index, SwissKnife.sqlEncode(status));
      statement.setString(++index, SwissKnife.sqlEncode(orderId));
      statement.executeUpdate();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    finally {
      dbRet = database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
      
      if (statement != null) try { statement.close(); } catch (Exception e) {}
      
      director.freeDBConnection(_databaseId, database);
    }
    
    return dbRet;
  }
  
  private Customer _customer = null;
  private Order _order = null;
  
  private BigDecimal _zero = new BigDecimal("0");
  
  private static String _databaseId = SwissKnife.jndiLookup("swconf/databaseId");
  
  private static String _inventoryType = SwissKnife.jndiLookup("swconf/inventoryType");
}