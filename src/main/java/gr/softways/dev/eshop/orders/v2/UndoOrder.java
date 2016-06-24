package gr.softways.dev.eshop.orders.v2;

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
    
    String [] hotdealFlagA = null;
    Timestamp [] orderDateA = null;
    BigDecimal [] quantityA = null;
    BigDecimal [] valueA = null;
    BigDecimal [] valueEUA = null;

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
          undoTransAttribute(queryDataSet.getInt("transId"), queryDataSet.getString("orderId"), quantityA[i], database);

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

        updateStm = "UPDATE product SET" +
                      " stockQua = " + stockQua + "," +
                      " outQua = " + outQua + "," +
                      " outVal = " + outVal + "," +
                      " outValEU = " + outValEU + "," +
                      " prdLock = " + "'" + "0" + "'" + " " +
                      "WHERE prdId = " + "'" + prdId + "'";
        
        if (dbRet.getNoError() == 1) dbRet = database.execQuery(updateStm);
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
  
  private DbRet undoTransAttribute(int transId, String orderId, BigDecimal quantity, Database database) {
    DbRet dbRet = new DbRet();
    QueryDataSet qta = new QueryDataSet();
    String query = null;
    int rc = 0;
    
    if (dbRet.getNoError() == 1) {
      try {
        query = "SELECT * FROM transAttribute"
                      + " WHERE TAV_transId = " + transId + " AND TAV_orderId = '" + orderId + "'";

        qta.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        qta.setMetaDataUpdate(MetaDataUpdate.NONE);
        qta.refresh();

        rc = qta.getRowCount();

        for (int i=0; i<rc; i++, qta.next()) {
          if (qta.getInt("TAVKeepStock")==1) {
            if (qta.getInt("TAVSlaveFlag")==1) {
              dbRet = updatePMASVStock(qta.getString("TAV_PMAVCode"), quantity, database);
            }
            else {
              dbRet = updatePMAVStock(qta.getString("TAV_PMAVCode"), quantity, database);
            }
          }
        }
        if (qta.isOpen()) qta.close();
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
    }
    return dbRet;
  }
  
  
  private DbRet updatePMAVStock(String PMAVCode, BigDecimal quantity, Database database) {
    DbRet dbRet1, dbRet3 = null;
    DbRet dbRet2 = new DbRet();
    QueryDataSet qps = null;
    BigDecimal stockQua = null;
    
    String query = "UPDATE productMasterAttributeValue SET PMAVLock = '1'"
                 + " WHERE PMAVCode = '" + PMAVCode + "'";
    dbRet1 = database.execQuery(query);
    
    if (dbRet1.getNoError() == 1) {
      query = "SELECT PMAVCode, PMAVStock FROM productMasterAttributeValue"
            + " WHERE PMAVCode= '" + PMAVCode + "'";
      
      try {
        qps = new QueryDataSet();
        qps.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        qps.setMetaDataUpdate(MetaDataUpdate.NONE);
        qps.refresh();
        
        stockQua = qps.getBigDecimal("PMAVStock");
        if (stockQua != null)
          stockQua = stockQua.add(quantity);
        else
          stockQua = new BigDecimal("0");
      }
      catch (DataSetException e) {
        dbRet2.setNoError(0);
        e.printStackTrace();
      }
      finally {
        try { qps.close(); } catch (Exception e) { }
      }
    }  
      
    if (dbRet1.getNoError() == 1 && dbRet2.getNoError() == 1) {
      query = "UPDATE productMasterAttributeValue SET" 
            + " PMAVStock = " + stockQua + "" 
            + " WHERE PMAVCode = '" + PMAVCode + "'";
      
      dbRet1 = database.execQuery(query);
    }
    
    return dbRet3;
  }
  
  
  private DbRet updatePMASVStock(String PMAVCode, BigDecimal quantity, Database database) {
    DbRet dbRet1 = null;
    DbRet dbRet2 = new DbRet();
    DbRet dbRet3 = new DbRet();
    QueryDataSet qps = null;
    BigDecimal stockQua = null;
    String query = "UPDATE PMASV SET PMASVLock = '1'"
                 + " WHERE PMASVCode = '" + PMAVCode + "'";
    dbRet1 = database.execQuery(query);
    
    if (dbRet1.getNoError() == 1) {
      query = "SELECT PMASVCode, PMASVStock FROM PMASV"
            + " WHERE PMASVCode= '" + PMAVCode + "'";
      
      try {
        qps = new QueryDataSet();
        qps.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        qps.setMetaDataUpdate(MetaDataUpdate.NONE);
        qps.refresh();
        
        stockQua = qps.getBigDecimal("PMASVStock");
        if (stockQua != null)
          stockQua = stockQua.add(quantity);
        else
          stockQua = new BigDecimal("0");
      }
      catch (DataSetException e) {
        dbRet2.setNoError(0);
        e.printStackTrace();
      }
      finally {
        try { qps.close(); } catch (Exception e) { }
      }
    }  
      
    if (dbRet1.getNoError() == 1 && dbRet2.getNoError() == 1) {
      query = "UPDATE PMASV SET" 
            + " PMASVStock = " + stockQua + "" 
            + " WHERE PMASVCode = '" + PMAVCode + "'";
      
      dbRet3 = database.execQuery(query);
    }
    
    return dbRet3;
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
}