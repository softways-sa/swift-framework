package gr.softways.dev.eshop.orders;

import java.sql.Timestamp;
import java.math.BigDecimal;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class UndoOrder extends JSPBean {
  
  private Director director = Director.getInstance();
  
  private QueryDataSet queryDataSetO = new QueryDataSet();
  
  public UndoOrder() {
  }
  
  public DbRet doAction(String orderId) {
    DbRet dbRet = new DbRet();
    
    int prevTransIsolation = 0;
    
    String getInQua = null, getOutQua = null, getInVal = null, getOutVal = null,
           getInValEU = null, getOutValEU = null;
    
    String queryGetTrans = null;
    String queryGetProduct = null;
    String updateStm = null;
    String queryGetOrder = null;

    int rc = 0;
    
    String prdId = null;
    
    BigDecimal stockQua = null, inQua = null, outQua = null, inVal = null,
               inValEU = null, outVal = null, outValEU = null;
    
    String customerId = null;
    
    String [] prdIdA = null;
    String[] prdAttrCodeA = null;
    String [] hotdealFlagA = null;
    Timestamp [] orderDateA = null;
    BigDecimal [] quantityA = null;
    BigDecimal [] valueA = null;
    BigDecimal [] valueEUA = null;

    // ��� �������� �� ATTRIBUTES {
    QueryDataSet attrDataSet = new QueryDataSet();
    String prdAttrCode = "", queryGetAttr = "", updateStmAttr = "";
    BigDecimal stockQuaAttr = null;
    boolean hasAttributes = false;
    // } ��� �������� �� ATTRIBUTES

    database = director.getDBConnection(databaseId);

    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    
    prevTransIsolation = dbRet.getRetInt();

    updateStm = "UPDATE lTab SET"
              + " lTabFld = '1'"
              + " WHERE lTabCode = '1'";
    
    /* loop for interbase transaction conflict  { */
    if (dbRet.getNoError() == 1) {
      dbRet.setNoError(0);
      for ( int i=0; i < 10 && dbRet.getNoError() == 0; i++) {
        dbRet = database.execQuery(updateStm);
      }
    }
    /* } loop for interbase transaction conflict */

    updateStm = "UPDATE transactions SET"
              + " transLock = " + "'" + "1" + "'" + " "
              + " WHERE orderId = " + "'" + orderId + "'";
    if (dbRet.getNoError() == 1)
      dbRet = database.execQuery(updateStm);
    if (dbRet.getNoError() == 1) {
      try {
        if (queryDataSet.isOpen()) queryDataSet.close();
        queryGetTrans = "SELECT * FROM transactions"
                      + " WHERE orderId = '" + orderId + "'";

        queryDataSet.setQuery(new QueryDescriptor(database,queryGetTrans,null,true,Load.ALL));
        queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
        queryDataSet.refresh();

        rc = queryDataSet.getRowCount();

        prdIdA = new String[rc];
        prdAttrCodeA = new String[rc];
        hotdealFlagA = new String[rc];
        quantityA = new BigDecimal[rc];
        valueA = new BigDecimal[rc];
        valueEUA = new BigDecimal[rc];
        orderDateA = new Timestamp[rc];
        for (int i=0; i<rc; i++, queryDataSet.next()) {
          prdIdA[i] = queryDataSet.getString("prdId");
          hotdealFlagA[i] = queryDataSet.getString("hotdealFlag");
          quantityA[i] = queryDataSet.getBigDecimal("quantity");
          valueA[i] = queryDataSet.getBigDecimal("valueDR");
          valueEUA[i] = queryDataSet.getBigDecimal("valueEU");
          orderDateA[i] = queryDataSet.getTimestamp("orderDate");

          // if attributes {
          if (queryDataSet.getString("transPrdAttCode") != null
                && queryDataSet.getString("transPrdAttCode").trim().length() > 0) {
            prdAttrCodeA[i] = queryDataSet.getString("transPrdAttCode");
          } // } if attributes
          else prdAttrCodeA[i] = "";

        }
        if (queryDataSet.isOpen()) queryDataSet.close();
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
    }
    if (dbRet.getNoError() == 1) dbRet = undoTransactions(orderId);

    if (dbRet.getNoError() == 1) {
      updateStm = "UPDATE orders SET" +
         " orderLock = " + "'" + "1" + "'" + " " +
         "WHERE orderId = " + "'" + orderId + "'";
      dbRet = database.execQuery(updateStm);
      
      if (dbRet.getNoError() == 1) {
        try {
          if (queryDataSetO.isOpen()) queryDataSetO.close();

          queryGetOrder = "SELECT * FROM orders"
                        + " WHERE orderId = '" + orderId + "'";
          
          queryDataSetO.setQuery(new QueryDescriptor(database,queryGetOrder,null,true,Load.ALL));
          queryDataSetO.setMetaDataUpdate(MetaDataUpdate.NONE);
          queryDataSetO.refresh();
          
          customerId = queryDataSetO.getString("customerId");
        }
        catch (Exception e) {
          dbRet.setNoError(0);
          e.printStackTrace();
        }
      }
      if (dbRet.getNoError() == 1) dbRet = undoOrders(orderId);
    }

    if (dbRet.getNoError() == 1) {
      dbRet = undoPrdMonthly(prdIdA, orderDateA, quantityA, valueA, valueEUA, rc);
      
      for (int i=0; i<rc && dbRet.getNoError() == 1; i++) {
        prdId = prdIdA[i];
        prdAttrCode = prdAttrCodeA[i];
        hasAttributes = false;
        
        updateStm = "UPDATE product SET" +
                 " prdLock = " + "'" + "1" + "'" + " " +
                 "WHERE prdId = " + "'" + prdId + "'";
        dbRet = database.execQuery(updateStm);
        
        if (dbRet.getNoError() == 1) {
          try {
            queryGetProduct = "SELECT * FROM product"
                          +  " WHERE prdId = '" + prdId + "'";

            queryDataSet.setQuery(new QueryDescriptor(database,queryGetProduct,null,true,Load.ALL));
            queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
            queryDataSet.refresh();

            // if has attributes {
            if (!prdAttrCode.equals("")) {
              // lock attributes row
              updateStm = "UPDATE prdAttributes SET"
                        + " prdALock = '1'"
                        + " WHERE prdACode = '" + prdAttrCode + "'"
                        + " AND prdAPrdId = '" + prdId + "'";
              dbRet = database.execQuery(updateStm);

              hasAttributes = true;
              queryGetAttr = "SELECT * FROM prdAttributes"
                           + " WHERE prdACode = '" + prdAttrCode + "'"
                           + " AND prdAPrdId = '" + prdId + "'";

              if (attrDataSet.isOpen()) attrDataSet.close();

              attrDataSet.setQuery(new QueryDescriptor(database,queryGetAttr,null,true,Load.ALL));
              attrDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
              attrDataSet.refresh();

              stockQuaAttr = attrDataSet.getBigDecimal("prdAStock");

              stockQuaAttr = stockQuaAttr.add(quantityA[i]);
            } // } if has attributes
            else stockQuaAttr = new BigDecimal(0);

            stockQua = queryDataSet.getBigDecimal("stockQua");
            inQua = queryDataSet.getBigDecimal("inQua");
            outQua = queryDataSet.getBigDecimal("outQua");
            inVal = queryDataSet.getBigDecimal("inVal");
            inValEU = queryDataSet.getBigDecimal("inValEU");
            outVal = queryDataSet.getBigDecimal("outVal");
            outValEU = queryDataSet.getBigDecimal("outValEU");

            if (queryDataSet.isOpen()) queryDataSet.close();

            stockQua = stockQua.add(quantityA[i]);
            outQua = outQua.subtract(quantityA[i]);
            outVal = outVal.subtract(valueA[i]);
            outValEU = outValEU.subtract(valueEUA[i]);
          }
          catch (Exception e) {
            dbRet.setNoError(0);
            e.printStackTrace();
          }
        }
        updateStmAttr = "UPDATE prdAttributes SET" +
                        " prdAStock = " + stockQuaAttr + "" +
                        ",prdALock = '0'" +
                        " WHERE prdACode = '" + prdAttrCode + "'" +
                        " AND prdAPrdId = '" + prdId + "'";

        if (dbRet.getNoError() == 1 && hasAttributes) {
          dbRet = database.execQuery(updateStmAttr);
        }

        updateStm = "UPDATE product SET" +
                      " stockQua = " + stockQua + "," +
                      " outQua = " + outQua + "," +
                      " outVal = " + outVal + "," +
                      " outValEU = " + outValEU + "," +
                      " prdLock = " + "'" + "0" + "'" + " " +
                      "WHERE prdId = " + "'" + prdId + "'";
        
        if (dbRet.getNoError() == 1) dbRet = database.execQuery(updateStm);
      }
      
      if (dbRet.getNoError() == 1 && customerId != null 
              && customerId.trim().length()>0) {
        dbRet = undoCustomer(orderId, hotdealFlagA);
      }
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(),prevTransIsolation);

    director.freeDBConnection(databaseId,database);
    
    try {
      if (queryDataSetO != null && queryDataSetO.isOpen()) queryDataSetO.close();
    }
    catch (Exception e) {
    }
    
    return dbRet;
  }

  private DbRet undoOrders(String orderId) {
    DbRet dbRet = null;
    
    String query = null;
    
    query = "DELETE FROM orders"
          + " WHERE orderId = '" + orderId + "'";
    
    dbRet = database.execQuery(query);
    
    return dbRet;
  }

  private DbRet undoTransactions(String orderId) {
    DbRet dbRet = null;
    
    String query = null;
    
    query = "DELETE FROM transactions"
          + " WHERE orderId = '" + orderId + "'";

    dbRet = database.execQuery(query);
    
    return dbRet;
  }

  private DbRet undoPrdMonthly(String [] prdIdA, Timestamp [] orderDateA,
                               BigDecimal [] outQuaA, BigDecimal [] outValA,
                               BigDecimal [] outValEUA, int rc) {
    DbRet dbRet = new DbRet();
    int m=0, y=0;
    BigDecimal monthInQua = null, monthOutQua = null, monthInVal = null,
           monthOutVal = null, monthInValEU = null, monthOutValEU = null;
    String queryGetPrdMonthly = null;
    String updateStm = null;
    String prdId = null;

    try {
       if (queryDataSet.isOpen()) queryDataSet.close();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    for (int i=0; i<rc && dbRet.noError == 1; i++) {
      m = SwissKnife.getTDateInt(orderDateA[i], "MONTH");
      y = SwissKnife.getTDateInt(orderDateA[i], "YEAR");
      prdId = prdIdA[i];
      updateStm = "UPDATE prdMonthly SET" +
            " prdMLock = " + "'" + "1" + "'" + " " +
            "WHERE prdMId = " + " '" + prdId + "'" +
            " AND prdMYear = " + y + " AND prdMMonth = " + m;
      dbRet = database.execQuery(updateStm);
      
      if (dbRet.getNoError() == 1) {
        try {
          queryGetPrdMonthly = "SELECT * FROM prdMonthly WHERE prdMId = " +
                              "'" + prdId + "'" + " AND prdMYear = " + y +
                               " AND prdMMonth = " + m;

          queryDataSet.setQuery(new QueryDescriptor(database,queryGetPrdMonthly,null,true,Load.ALL));
          queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

          queryDataSet.refresh();
          
          if (queryDataSet.isEmpty() == false) {
            monthOutQua = queryDataSet.getBigDecimal("prdMOutQua");
            monthOutVal = queryDataSet.getBigDecimal("prdMOutVal");
            monthOutValEU = queryDataSet.getBigDecimal("prdMOutValEU");
            
            monthOutQua = monthOutQua.subtract(outQuaA[i]);
            monthOutVal = monthOutVal.subtract(outValA[i]);
            monthOutValEU = monthOutValEU.subtract(outValEUA[i]);
             
            updateStm = "UPDATE prdMonthly SET" +
                      " prdMOutQua = " + monthOutQua + "," +
                      " prdMOutVal = " + monthOutVal + "," +
                      " prdMOutValEU = " + monthOutValEU + "," +
                      " prdMLock = " + "'" + "0" + "'" + " " +
                      "WHERE prdMId = " + " '" + prdId + "'" +
                      " AND prdMYear = " + y + " AND prdMMonth = " + m;
                      
            dbRet = database.execQuery(updateStm);
          }
          
          if (queryDataSet.isOpen()) queryDataSet.close();
        }
        catch (Exception e) {
          dbRet.setNoError(0);
          e.printStackTrace();
        }
      }
    }
    
    return dbRet;
  }

  private DbRet undoCustomer(String orderId, String [] hotdealFlagA) {
    DbRet dbRet = new DbRet();
    
    int hotdealCnt = 0;
    int retries = 0;
    int m = 0, y = 0;
    
    BigDecimal value = null, valueEU = null, vatVal = null, vatValEU = null;
    BigDecimal shippingValue = null, shippingValueEU = null, shippingVatVal = null,
               shippingVatValEU = null;
    
    Timestamp orderDate = null;
    
    String getDateLastUsed = null, getPurchaseVal = null, getPurchaseValEU = null,
           getHotdealBuysCnt = null, getBuysCnt = null;

    BigDecimal purchaseVal = null, purchaseValEU = null;

    int buysCnt = 0, hotdealBuysCnt = 0;
    String queryGetCustomer = null;
    String queryGetOrder = null;
    String updateStm = null;
    String customerId = null;
    int prcZoneId = 0;
    
    for (int i=0; i<hotdealFlagA.length; i++) {
       if (hotdealFlagA[i].equals("1")) hotdealCnt++;
    }
    
    try {
       value = queryDataSetO.getBigDecimal("valueDR");
       valueEU = queryDataSetO.getBigDecimal("valueEU");
       vatVal = queryDataSetO.getBigDecimal("vatVal");
       vatValEU = queryDataSetO.getBigDecimal("vatValEU");
       shippingValue = queryDataSetO.getBigDecimal("shippingValue");
       shippingValueEU = queryDataSetO.getBigDecimal("shippingValueEU");
       shippingVatVal = queryDataSetO.getBigDecimal("shippingVatVal");
       shippingVatValEU = queryDataSetO.getBigDecimal("shippingVatValEU");
       customerId = queryDataSetO.getString("customerId");
       orderDate = queryDataSetO.getTimestamp("orderDate");
       m = SwissKnife.getTDateInt(orderDate, "MONTH");
       y = SwissKnife.getTDateInt(orderDate, "YEAR");

       if (queryDataSetO.isOpen()) queryDataSetO.close();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }

    value = value.add(vatVal);
    value = value.add(shippingValue);
    value = value.add(shippingVatVal);
    valueEU = valueEU.add(vatValEU);
    valueEU = valueEU.add(shippingValueEU);
    valueEU = valueEU.add(shippingVatValEU);

    try {
      if (queryDataSetO.isOpen()) queryDataSetO.close();

      if (queryDataSet.isOpen()) queryDataSet.close();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    
    if (dbRet.getNoError() == 1) {
      updateStm = "UPDATE customer SET" +
        " custLock = " + "'" + "1" + "'" + " " +
        "WHERE customerId = " + "'" + customerId + "'";
      
      dbRet = database.execQuery(updateStm);
    }
    
    if (dbRet.getNoError() == 1) {
      try {
        queryGetCustomer = "SELECT * FROM customer"
                         + " WHERE customerId = '" + customerId + "'";

        queryDataSet.setQuery(new QueryDescriptor(database,queryGetCustomer,null,true,Load.ALL));
        queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

        queryDataSet.refresh();

        purchaseVal = queryDataSet.getBigDecimal("purchaseVal");
        purchaseValEU = queryDataSet.getBigDecimal("purchaseValEU");
        buysCnt = queryDataSet.getInt("buysCnt");
        hotdealBuysCnt = queryDataSet.getInt("hotdealBuysCnt");
        
        if (queryDataSet.isOpen()) queryDataSet.close();
        
        purchaseVal = purchaseVal.subtract(value);
        purchaseValEU = purchaseValEU.subtract(valueEU);
        hotdealBuysCnt -= hotdealCnt;
        buysCnt--;
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
    }
    updateStm = "UPDATE customer SET" +
                    " purchaseVal = " + purchaseVal + "," +
                    " purchaseValEU = " + purchaseValEU + "," +
                    " hotdealBuysCnt = " + hotdealBuysCnt + "," +
                    " buysCnt = " + buysCnt + "," +
                    " custLock = " + "'" + "0" + "'" + " " +
                    "WHERE customerId = " + "'" + customerId + "'";
    
    if (dbRet.getNoError() == 1) dbRet = database.execQuery(updateStm);
    
    if (dbRet.getNoError() == 1) dbRet = undoCustMonthly(customerId, y, m, value, valueEU);
    
    if (dbRet.getNoError() == 1) dbRet = undoCustZones(customerId, value, valueEU);
    
    return dbRet;
  }


  private DbRet undoCustMonthly(String customerId, int y, int m,
                                BigDecimal purchaseVal, BigDecimal purchaseValEU) {
    DbRet dbRet = new DbRet();
    
    BigDecimal monthPurchaseVal = null, monthPurchaseValEU = null;
    
    String queryGetCustMonthly = null;
    String updateStm = null;
    
    updateStm = "UPDATE custMonthly SET" +
          " custMLock = " + "'" + "1" + "'" + " " +
          "WHERE custMCustomerId =" + " '" + customerId + "'" +
          " AND custMYear = " + y + " AND custMMonth = " + m;
    
    dbRet = database.execQuery(updateStm);
    
    if (dbRet.getNoError() == 1) {
      try {
        if (queryDataSet.isOpen()) queryDataSet.close();
        
        queryGetCustMonthly =
           "SELECT * FROM custMonthly WHERE custMCustomerId = " +
            "'" + customerId + "'" +
           " AND custMYear = " + y + " AND custMMonth = " + m;

        queryDataSet.setQuery(new QueryDescriptor(database,queryGetCustMonthly,null,true,Load.ALL));
        queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

        queryDataSet.refresh();

        if (queryDataSet.isEmpty() == false) {
          monthPurchaseVal = queryDataSet.getBigDecimal("custMPurchaseVal");
          monthPurchaseValEU = queryDataSet.getBigDecimal("custMPurchaseValEU");
          
          monthPurchaseVal = monthPurchaseVal.subtract(purchaseVal);
          monthPurchaseValEU = monthPurchaseValEU.subtract(purchaseValEU);
          
          updateStm = "UPDATE custMonthly SET" +
                        " custMPurchaseVal =" + monthPurchaseVal + "," +
                        " custMPurchaseValEU =" + monthPurchaseValEU + "," +
                        " custMLock = " + "'" + "0" + "'" + " " +
                        "WHERE custMCustomerId = " + " '" + customerId + "'" +
                        " AND custMYear = " + y + " AND custMMonth = " + m;
      
          dbRet = database.execQuery(updateStm);
        }
        
        if (queryDataSet.isOpen()) queryDataSet.close();
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
    }
    
    return dbRet;
  }

  private DbRet undoCustZones(String customerId, BigDecimal purchaseVal,
                              BigDecimal purchaseValEU) {
    DbRet dbRet = new DbRet();
    
    String getPurchaseZoneVal = null, getPurchaseZoneValEU = null,
           getPurchaseZoneCnt = null;
    
    BigDecimal purchaseZoneVal = null, purchaseZoneValEU = null;
    
    int purchaseZoneCnt = 0;
    
    String queryGetCustZones = null;
    String updateStm = null;
    int prcZoneId = 0;
    
    try {
       if (queryDataSet.isOpen()) queryDataSet.close();

       queryDataSet.setQuery(new QueryDescriptor(database,
           "select * from prcZone where (" +
           purchaseVal +
           " >= downLimit and " +
           purchaseVal + " <= upLimit) or (" +
           purchaseValEU + " >= downLimitEU and " +
           purchaseValEU + " <= upLimitEU)", null, true, Load.ALL));
       queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

       queryDataSet.refresh();
       
       if (queryDataSet.isEmpty()) {
         prcZoneId = 1;
       }
       else {
          queryDataSet.first();
          prcZoneId = queryDataSet.getInt("prcZoneId");
       }
       
       if (queryDataSet.isOpen()) queryDataSet.close();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    
    updateStm = "UPDATE custZones SET" +
            " custZLock = " + "'" + "1" + "'" + " " +
            "WHERE custZCustomerId =" + " '" + customerId + "'" +
            " AND custZZoneId = " + prcZoneId;
    
    if (dbRet.getNoError() == 1) dbRet = database.execQuery(updateStm);
    
    if (dbRet.getNoError() == 1) {
      try {
        queryGetCustZones = "SELECT * FROM custZones WHERE custZCustomerId = " +
                              "'" + customerId + "'" +
                              " AND custZZoneId = " + prcZoneId;

        queryDataSet.setQuery(new QueryDescriptor(database,queryGetCustZones,null,true,Load.ALL));
        queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
        
        queryDataSet.refresh();
        
        if (queryDataSet.isEmpty() == false) {
          purchaseZoneVal = queryDataSet.getBigDecimal("custZPurchaseZoneVal");
          purchaseZoneValEU = queryDataSet.getBigDecimal("custZPurchaseZoneValEU");
          purchaseZoneCnt = queryDataSet.getInt("custZPurchaseZoneCnt");
          
          purchaseZoneVal = purchaseZoneVal.subtract(purchaseVal);
          purchaseZoneValEU = purchaseZoneValEU.subtract(purchaseValEU);
          purchaseZoneCnt--;
          
          updateStm = "UPDATE custZones SET" +
                    " custZPurchaseZoneVal = " + purchaseZoneVal + "," +
                    " custZPurchaseZoneValEU = " + purchaseZoneValEU + "," +
                    " custZPurchaseZoneCnt = " + purchaseZoneCnt + "," +
                    " custZLock = " + "'" + "0" + "'" + " " +
                    "WHERE custZCustomerId =" + " '" + customerId + "'" +
                    " AND custZZoneId = " + prcZoneId;
    
          dbRet = database.execQuery(updateStm);
        }
        
        if (queryDataSet.isOpen()) queryDataSet.close();
      }
      catch (DataSetException e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
    }
    
    return dbRet;
  }
}