package gr.softways.dev.eshop.stockmgr;

import java.math.BigDecimal;
import java.sql.Timestamp;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class StockMgr {

  public StockMgr() {
  }
  
  /*
   * ���������� ���� ������ prdImports
   *
   * @param orderId ... inOutFlag �� ������� ��� ������ ��� ������ ��� ��
   *                              ������� insert
   * @param database              ��� �� ��� ����� �� ������� connection
   *                              �� �� ����
   * @return                      �� object dbRet
  */
  public static DbRet insertToImport(String prdId,BigDecimal quantity,
                                     BigDecimal unitPrc,BigDecimal unitPrcEU,
                                     BigDecimal value,BigDecimal valueEU,
                                     BigDecimal vatVal,BigDecimal vatValEU,
                                     Timestamp importDate,String status,
                                     String inOutFlag,Database database) {
    DbRet dbRet = new DbRet();
    
    String query = "", transId = null;
    
    int retries = 0;
    
    dbRet.setRetry(1);

    for (; dbRet.getRetry() == 1 && retries<10; retries++) {
      transId = SwissKnife.buildPK();
      
      query = "INSERT INTO prdImports (transId,prdId,quantity,unitPrc," +
              "unitPrcEU,valueDR,valueEU,vatVal,vatValEU,importDate,"   +
              "status,inOutFlag) VALUES (" +
              "'" + transId     + "'," +
              "'" + prdId       + "'," +
                    quantity    + ","  +
                    unitPrc     + ","  +
                    unitPrcEU   + ","  +
                    value       + ","  +
                    valueEU     + ","  +
                    vatVal      + ","  +
                    vatValEU    + ","  +
              "'" + importDate  + "'," +
              "'" + status      + "'," +
              "'" + inOutFlag   + "')";

      dbRet = database.execQuery(query);
    }
    
    dbRet.setRetStr(transId);
    
    return dbRet;
  }

  /*
   * ���������� ���� ������ PILines
   *
   * @param PILTransId ... PILLock �� ������� ��� ������ ��� ������ ��� ��
   *                               ������� insert
   * @param database               ��� �� ��� ����� �� ������� connection
   *                               �� �� ����
   * @return                       �� object dbRet
  */
  public static DbRet insertToPILines(String PILTransId,String PILPrdId,
                                      String PILPrdAId,BigDecimal PILPrdAQua,
                                      Database database){
    DbRet dbRet = new DbRet();
    
    String query = "";
    
    int retries = 0;
    
    dbRet.setRetry(1);

    for (;dbRet.getRetry() == 1 && retries<10; retries++) {
      query = "INSERT INTO PILines (PILCode,PILTransId,PILPrdId,PILPrdAId," +
              "PILPrdAQua,PILLock) VALUES (" +
              "'" + SwissKnife.buildPK() + "'," +
              "'" + PILTransId     + "'," +
              "'" + PILPrdId       + "'," +
              "'" + PILPrdAId      + "'," +
                    PILPrdAQua     + ",'0')";
      
      dbRet = database.execQuery(query);
    }
    
    return dbRet;
  }


  /*
   * ���������� ��� ������ prdAttributes. ������� �� ��� select
   * ��� ������� ��� ��������� ��� ��� ������ prdAttributes ��� ����������
   * ��� �������� ��� ��������.
   * @return                      �� object dbRet
  */
  public static DbRet updatePrdAttributes(String PILPrdAId,BigDecimal quantity,
                                          String inOutFlag,Database database,
                                          String databaseId) {
    DbRet dbRet = null;
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    boolean foundPrd = false;

    String updateStm = null, query = null;

    BigDecimal stockQua = null;

    updateStm = "UPDATE prdAttributes "    +
                "SET prdALock = '1' " +
                "WHERE prdAId = '" + PILPrdAId + "'";

    dbRet = database.execQuery(updateStm);

    if (dbRet.getNoError() == 1) {
      query = "SELECT prdAStock FROM prdAttributes WHERE prdAId = '" + PILPrdAId + "'";

      try {
        if (queryDataSet.isOpen()) queryDataSet.close();
        
        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        queryDataSet.refresh();

        stockQua = queryDataSet.getBigDecimal("prdAStock");

        if (queryDataSet.isOpen()) queryDataSet.close();
      }
      catch (DataSetException e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
    }
    
    if (dbRet.getNoError() == 1) {
      if (inOutFlag.equals("0")) stockQua = stockQua.add(quantity);
      else if (inOutFlag.equals("1")) stockQua = stockQua.subtract(quantity);

      updateStm = "UPDATE prdAttributes SET "    +
                  "prdAStock = " + stockQua + "," +
                  "prdALock = '0' " +
                  "WHERE prdAId = '" + PILPrdAId + "'";
      
      dbRet = database.execQuery(updateStm);
    }
    
    return dbRet;
  }

  /*
   * ���������� ���� ������� prdMonthly & product. ������� �� ��� select
   * �� ������� ��� ��������� ��� ��� ������ product, ��� ���� ������� ��
   * ������ �������� � ������� ��������� ��������� ������������ ���� ����������
   * ���������.
   * @return                      �� object dbRet
  */
  public static DbRet updateProduct(String transId,String prdId,BigDecimal quantity,
                                    BigDecimal value,BigDecimal valueEU,
                                    BigDecimal vatVal, BigDecimal vatValEU,
                                    Timestamp importDate,String inOutFlag, 
                                    Database database, String databaseId) {

    DbRet dbRet = null;
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    boolean foundPrd = false;

    String updateStm = null, query = null;

    BigDecimal stockQua = null, inQua = null, outQua = null, inVal = null,
               inValEU = null, outVal = null, outValEU = null;

    //���������� ��� ������ prdMonthly
    dbRet = updatePrdMonthly(prdId,quantity,value,valueEU,importDate,
                             inOutFlag,database,databaseId);

    if (dbRet.getNoError() == 1) {
      updateStm = "UPDATE product "    +
                  "SET prdLock = '1' " +
                  "WHERE prdId = '" + prdId + "'";
      
      dbRet = database.execQuery(updateStm);
    }
    
    if (dbRet.getNoError() == 1) {
      query = "SELECT * FROM product WHERE prdId = '" + prdId + "'";
      
      try {
        if (queryDataSet.isOpen()) queryDataSet.close();

        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        queryDataSet.refresh();

        stockQua = queryDataSet.getBigDecimal("stockQua");
        inQua = queryDataSet.getBigDecimal("inQua");
        outQua = queryDataSet.getBigDecimal("outQua");
        inVal = queryDataSet.getBigDecimal("inVal");
        inValEU = queryDataSet.getBigDecimal("inValEU");
        outVal = queryDataSet.getBigDecimal("outVal");
        outValEU = queryDataSet.getBigDecimal("outValEU");

        queryDataSet.close();
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
    }
    
    if (dbRet.getNoError() == 1) {
      if (inOutFlag.equals("0")) {
        stockQua = stockQua.add(quantity);
        inQua = inQua.add(quantity);
        inVal = inVal.add(value);
        inValEU = inValEU.add(valueEU);
      }
      else if (inOutFlag.equals("1")) {
        stockQua = stockQua.subtract(quantity);
        inQua = inQua.subtract(quantity);
        inVal = inVal.subtract(value);
        inValEU = inValEU.subtract(valueEU);
      }
      
      updateStm = "UPDATE product SET "    +
                  "stockQua = " + stockQua + "," +
                  "inQua = "    + inQua    + "," +
                  "inVal = "    + inVal    + "," +
                  "inValEU = "  + inValEU  + "," +
                  "prdLock = '0' " +
                  "WHERE prdId = '" + prdId + "'";
      
      dbRet = database.execQuery(updateStm);
    }
    
    return dbRet;
  }


  /**
   * ���������� ��� ������ prdMonthly. ������� �� ��� select
   * ��� �������� ��� ��������� ��� �� ������ prdMonthly ��� �� ���� ��� ��
   * ���� ��������� ��� ������� ��� ���� ������� �� ������ �������� �
   * ������� ��������� ��������� ������������ ���� ���������� ���������.
   * @return                      �� object dbRet
   */
  public static DbRet updatePrdMonthly(String prdId,BigDecimal quantity,
                                       BigDecimal value,BigDecimal valueEU,
                                       Timestamp importDate,String inOutFlag,
                                       Database database, String databaseId) {
    DbRet dbRet = null;
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    boolean foundPrd = false;

    BigDecimal monthInQua = null, monthOutQua = null, monthInVal = null,
               monthOutVal = null, monthInValEU = null, monthOutValEU = null,
               inQua = null, outQua = null, inVal = null, inValEU = null,
               outVal = null, outValEU = null;

    int m = SwissKnife.getTDateInt(importDate, "MONTH"),
        y = SwissKnife.getTDateInt(importDate, "YEAR");

    String queryGetPrdMonthly = null, updateStm = null;

    updateStm = "UPDATE prdMonthly "  +
                "SET prdMLock = '1' " +
                "WHERE prdMId = '" + prdId + "' " +
                "AND prdMYear = "  + y     + " "  +
                "AND prdMMonth = " + m;
    
    dbRet = database.execQuery(updateStm);

    if (dbRet.getNoError() == 1) {
      try {
        if (queryDataSet.isOpen()) queryDataSet.close();
        
        queryGetPrdMonthly = "SELECT * FROM prdMonthly " +
                             "WHERE prdMId = '"  + prdId + "' " +
                             "AND prdMYear = "   + y     + " "  +
                             "AND prdMMonth = " + m;

        queryDataSet.setQuery(new QueryDescriptor(database,queryGetPrdMonthly,null,true,Load.ALL));
        queryDataSet.refresh();

        monthInQua = queryDataSet.getBigDecimal("prdMInQua");
        monthInVal = queryDataSet.getBigDecimal("prdMInVal");
        monthInValEU = queryDataSet.getBigDecimal("prdMInValEU");
        monthOutQua = queryDataSet.getBigDecimal("prdMOutQua");
        monthOutVal = queryDataSet.getBigDecimal("prdMOutVal");
        monthOutValEU = queryDataSet.getBigDecimal("prdMOutValEU");

        queryDataSet.close();
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
    }
    
    if (dbRet.getNoError() == 1) {
      if (inOutFlag.equals("0")) {
        monthInQua = monthInQua.add(quantity);
        monthInVal = monthInVal.add(value);
        monthInValEU = monthInValEU.add(valueEU);
      }
      else if (inOutFlag.equals("1")) {
        monthInQua = monthInQua.subtract(quantity);
        monthInVal = monthInVal.subtract(value);
        monthInValEU = monthInValEU.subtract(valueEU);
      }
      
      updateStm = "UPDATE prdMonthly SET " +
                  "prdMInQua = "     + monthInQua   + "," +
                  "prdMInVal = "     + monthInVal   + "," +
                  "prdMInValEU = "   + monthInValEU + "," +
                  "prdMLock = '0' "  +
                  "WHERE prdMId = '" + prdId + "' " +
                  "AND prdMYear = "  + y     + " "  +
                  "AND prdMMonth = " + m;
      
      dbRet = database.execQuery(updateStm);
    }
    
    return dbRet;
  }

  /*
   * ������� ��� ������� ��� ��������� ��� ��� ������ prdImports,prdAttributes,
   * & PILines ��� ����� update ���� ������ prattributes ����������
   * ��� ������. ������ �� ������ �������� ������� �� quantity ��� �� ������
   * ������� ��� ���������.
   * @return                      �� object dbRet
  */
  public static DbRet rollBackPrdAttributes(String PILCode,String transId,
                                            Database database) {
    DbRet dbRet = null;
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    boolean foundPrd = false;
    
    Timestamp oldImportDate = null;
    
    int oldM = 0;

    String oldPrdId = null, oldInOutFlag = null, updateStm = null, query = "",
           updateStm1 = null, oldPrdAId = null;

    BigDecimal oldQuantity = null, stockQua = null;

    query = "SELECT * FROM PILines,prdImports " +
            "WHERE PILines.PILTransId = prdImports.transId " +
            "AND transId = '" + transId + "' " +
            "AND PILCode = '" + PILCode + "'";

    updateStm = "UPDATE PILines SET PILLock = '1' " +
                "WHERE PILTransId = '" + transId + "'";
    
    dbRet = database.execQuery(updateStm);

    if (dbRet.getNoError() == 1) {
      updateStm1 = "UPDATE prdImports SET importLock = '1' " +
                   "WHERE transId = '" + transId + "'";
      
      dbRet = database.execQuery(updateStm1);
    }

    if (dbRet.getNoError() == 1) {
      try {
        if (queryDataSet.isOpen()) queryDataSet.close();

        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        queryDataSet.refresh();
        
        if (queryDataSet.getRowCount() > 0) foundPrd = true;

        oldQuantity = queryDataSet.getBigDecimal("PILPrdAQua");
        oldPrdId = queryDataSet.getString("prdId");
        oldPrdAId = queryDataSet.getString("PILPrdAId");
        oldInOutFlag = queryDataSet.getString("inOutFlag");
      }
      catch (DataSetException e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
    }
    
    if (dbRet.getNoError() == 1) {
      updateStm = "UPDATE prdAttributes SET " +
                  "prdALock = '1' "      +
                  "WHERE prdAPrdId = '" + oldPrdId + "'";
      
      dbRet = database.execQuery(updateStm);
    }
    
    if (dbRet.getNoError() == 1) {
      try {
        if (queryDataSet.isOpen()) queryDataSet.close();
        
        queryDataSet.setQuery(new QueryDescriptor(database,
          "SELECT prdAStock FROM prdAttributes WHERE prdAId= '" + oldPrdAId + "'",
          null, true, Load.ALL));
        queryDataSet.refresh();

        stockQua = queryDataSet.getBigDecimal("prdAStock");
        
        queryDataSet.close();
      }
      catch (DataSetException e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
    }
    
    if (dbRet.getNoError() == 1) {
      if (oldInOutFlag.equals("0")) stockQua = stockQua.subtract(oldQuantity);
      else if (oldInOutFlag.equals("1")) stockQua = stockQua.add(oldQuantity);

      updateStm = "UPDATE prdAttributes SET "    +
                  "prdAStock = "     + stockQua  + "," +
                  "prdALock = '0' "  +
                  "WHERE prdAId = '" + oldPrdAId + "'";

      dbRet = database.execQuery(updateStm);
    }
    
    return dbRet;
  }


  /*
   * ������� ��� ������� ��� ��������� ��� ��� ������ prdImports & product
   * ��� ����� update ���� ������ product & prdImports ���������� ��� ������.
   * ������ �� ������ �������� ������� �� quantity ��� �� ������ ������� ���
   * ���������.
   * @return                      �� object dbRet
  */
  public static DbRet rollBackProduct(String transId, Database database, 
                                      String databaseId) {
    DbRet dbRet = null;
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    boolean foundPrd = false;
    
    Timestamp oldImportDate = null;
    
    int oldM = 0;
    
    String oldPrdId = null, oldInOutFlag = null, updateStm = null, query = "";

    BigDecimal oldQuantity = null, oldValue = null, oldValueEU = null, oldVatVal = null,
               oldVatValEU = null, stockQua = null, inQua = null, outQua = null,
               inVal = null, inValEU = null, outVal = null, outValEU = null;

    query = "SELECT * FROM prdImports WHERE transId = '" + transId + "'";

    updateStm = "UPDATE prdImports SET importLock = '1' " +
                "WHERE transId = '" + transId + "'";

    dbRet = database.execQuery(updateStm);

    if (dbRet.getNoError() == 1) {
      try {
        if (queryDataSet.isOpen()) queryDataSet.close();
        
        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        queryDataSet.refresh();
        
        if (queryDataSet.getRowCount() > 0) {
          foundPrd = true;
        }
        else {
          dbRet.setNoError(0);
        }

        oldQuantity = queryDataSet.getBigDecimal("quantity");
        oldValue = queryDataSet.getBigDecimal("valueDR");
        oldValueEU = queryDataSet.getBigDecimal("valueEU");
        oldVatVal = queryDataSet.getBigDecimal("vatVal");
        oldVatValEU = queryDataSet.getBigDecimal("vatValEU");
        oldImportDate = queryDataSet.getTimestamp("importDate");
        oldPrdId = queryDataSet.getString("prdId");
        oldInOutFlag = queryDataSet.getString("inOutFlag");
      }
      catch (DataSetException e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = rollBackPrdMonthly(oldPrdId, oldQuantity, oldValue,oldValueEU,
                                 oldImportDate,oldInOutFlag, database, databaseId);
    }
    
    if (dbRet.getNoError() == 1) {
      updateStm = "UPDATE product SET " +
                  "prdLock = '1' "      +
                  "WHERE prdId = '" + oldPrdId + "'";
      
      dbRet = database.execQuery(updateStm);
    }
    
    if (dbRet.getNoError() == 1) {
      try {
        if (queryDataSet.isOpen()) queryDataSet.close();
        
        query = "SELECT stockQua,inQua,outQua,inVal,inValEU,outVal,outValEU FROM product"
              + " WHERE prdId= '" + oldPrdId + "'";
        
        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        queryDataSet.refresh();

        stockQua = queryDataSet.getBigDecimal("stockQua");
        inQua = queryDataSet.getBigDecimal("inQua");
        outQua = queryDataSet.getBigDecimal("outQua");
        inVal = queryDataSet.getBigDecimal("inVal");
        inValEU = queryDataSet.getBigDecimal("inValEU");
        outVal = queryDataSet.getBigDecimal("outVal");
        outValEU = queryDataSet.getBigDecimal("outValEU");
        
        queryDataSet.close();
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
    }
    
    if (dbRet.getNoError() == 1) {
      if (oldInOutFlag.equals("0")) {
        stockQua = stockQua.subtract(oldQuantity);
        inQua = inQua.subtract(oldQuantity);
        inVal = inVal.subtract(oldValue);
        inValEU = inValEU.subtract(oldValueEU);
      }
      else if (oldInOutFlag.equals("1")) {
        stockQua = stockQua.add(oldQuantity);
        inQua = inQua.add(oldQuantity);
        inVal = inVal.add(oldValue);
        inValEU = inValEU.add(oldValueEU);
      }
      
      updateStm = "UPDATE product SET " +
                  "stockQua = " + stockQua + "," +
                  "inQua = "    + inQua    + "," +
                  "inVal = "    + inVal    + "," +
                  "inValEU = "  + inValEU  + "," +
                  "prdLock = '0' " +
                  "WHERE prdId = '" + oldPrdId + "'";
      
      dbRet = database.execQuery(updateStm);
    }
    
    return dbRet;
  }


  /*
   * K���� update ���� ������ prdImports ���������� ��� ������. ������ ��
   * ������ �������� ������� �� quantity ��� �� ������ ������� ��� ���������.
   * @return                      �� object dbRet
  */
  public static DbRet rollBackPrdMonthly(String oldPrdId,BigDecimal oldQuantity,
                                         BigDecimal oldValue,BigDecimal oldValueEU,
                                         Timestamp oldImportDate,String oldInOutFlag,
                                         Database database, String databaseId) {

    DbRet dbRet = null;
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    boolean foundPrd = false;

    BigDecimal monthInQua = null, monthOutQua = null, monthInVal = null,
               monthOutVal = null, monthInValEU = null, monthOutValEU = null;

    String queryGetPrdMonthly = null, updateStm = null;

    int oldM = SwissKnife.getTDateInt(oldImportDate, "MONTH"),
        oldY = SwissKnife.getTDateInt(oldImportDate, "YEAR");

    updateStm = "UPDATE prdMonthly SET prdMLock = '1' " +
                "WHERE prdMId = '" + oldPrdId + "' " +
                "AND prdMYear = "  + oldY     + " "  +
                "AND prdMMonth = " + oldM;

    dbRet = database.execQuery(updateStm);
    
    if (dbRet.getNoError() == 1) {
      try {
        queryGetPrdMonthly = "SELECT * FROM prdMonthly " +
                             "WHERE prdMId = '" + oldPrdId + "' " +
                             "AND prdMYear = "  + oldY     +  " " +
                             "AND prdMMonth = " + oldM;
        
        if (queryDataSet.isOpen()) queryDataSet.close();
        
        queryDataSet.setQuery(new QueryDescriptor(database,queryGetPrdMonthly,null,true,Load.ALL));
        queryDataSet.refresh();

        monthInQua = queryDataSet.getBigDecimal("prdMInQua");
        monthInVal = queryDataSet.getBigDecimal("prdMInVal");
        monthInValEU = queryDataSet.getBigDecimal("prdMInValEU");
        monthOutQua = queryDataSet.getBigDecimal("prdMOutQua");
        monthOutVal = queryDataSet.getBigDecimal("prdMOutVal");
        monthOutValEU = queryDataSet.getBigDecimal("prdMOutValEU");

        queryDataSet.close();
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
    }

    if (dbRet.getNoError() == 1) {
      if (oldInOutFlag.equals("0")) {
        monthInQua = monthInQua.subtract(oldQuantity);
        monthInVal = monthInVal.subtract(oldValue);
        monthInValEU = monthInValEU.subtract(oldValueEU);
      }
      else if (oldInOutFlag.equals("1")) {
        monthInQua = monthInQua.add(oldQuantity);
        monthInVal = monthInVal.add(oldValue);
        monthInValEU = monthInValEU.add(oldValueEU);
      }
      
      updateStm = "UPDATE prdMonthly SET " +
                  "prdMInQua = "   + monthInQua   + "," +
                  "prdMInVal = "   + monthInVal   + "," +
                  "prdMInValEU = " + monthInValEU + "," +
                  "prdMLock = '0' "  +
                  "WHERE prdMId = '" + oldPrdId   + "' " +
                  "AND prdMYear = "  + oldY       + " "  +
                  "AND prdMMonth = " + oldM;
      
      dbRet = database.execQuery(updateStm);
    }
    
    return dbRet;
  }
}