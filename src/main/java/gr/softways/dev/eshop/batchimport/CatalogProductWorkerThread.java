package gr.softways.dev.eshop.batchimport;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;
import java.io.File;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.HashMap;
import jxl.*;

public class CatalogProductWorkerThread extends Thread {
  
  private BigDecimal _zero = new BigDecimal("0");
  
  private int insert_product_prdId = 0;
  private int insert_product_catId = 1;
  private int insert_product_name = 2;
  private int insert_product_nameLG = 3;
  private int insert_product_retailPrcEU = 4;
  private int insert_product_wholesalePrcEU = 5;
  private int insert_product_PRD_VAT_ID = 6;
  private int insert_product_prdHideFlag = 7;
  private int insert_product_prdHideFlagW = 8;
  private int insert_product_descr = 9;
  private int insert_product_descrLG = 10;
  
  private CatalogProductManagerServlet _manager = null;
  
  private File _inputFile = null;
  
  private String _action = null;
  
  private String _databaseId = null;
  
  private int line = -1;
  
  public CatalogProductWorkerThread(CatalogProductManagerServlet manager,String action,File inputFile,String databaseId) {
    super("ProductWorkerThread");
    
    _manager = manager;

    _action = action;
    
    _inputFile = inputFile;
    
    _databaseId = databaseId;
  }
  
  public void run() {
    DbRet dbRet = new DbRet();
    
    Workbook workbook = null;
    
    Director director = Director.getInstance();
    
    try {
      workbook = Workbook.getWorkbook(_inputFile);
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    
    _inputFile.delete();
    _inputFile = null;
    
    if (dbRet.getNoError() == 0) {
    }
    else if (_action.equals("BATCH_UPDATE_PRODUCT")) {
      dbRet = doBatchUpdate(workbook.getSheet(0),_databaseId,director);
    }
    
    if (dbRet.getNoError() == 0) {
      _manager.setStatus(0);
      _manager.setTextMsg(SwissKnife.currentDate() + ": Batch process failed. (EXCEL line: " + (line+1) + ")");
    }
    else {
      _manager.setStatus(0);
      _manager.setTextMsg(SwissKnife.currentDate() + ": Batch process finished successfully.");
    }
    
    if (workbook != null) workbook.close();
  }
  
  private DbRet doBatchUpdate(Sheet sheet, String databaseId, Director director) {
    DbRet dbRet = new DbRet();
    
    int excelRows = sheet.getRows();
    
    if (excelRows == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    int prevTransIsolation = 0;
    
    Database database = director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);
    prevTransIsolation = dbRet.getRetInt();
    
    PreparedStatement insertStatement = null,
        insCatStatement = null, updStatement = null, updStatementWithDescr = null;

    HashMap prdidtable = new HashMap();
    
    String PINCPrimary = "";
    
    BigDecimal hdRetailPrcEU = _zero, hdWholesalePrcEU = _zero, stockQua = _zero,
        inQua = _zero, outQua = _zero, inVal = _zero, inValEU = _zero, 
        outVal = _zero, outValEU = _zero, giftPrcEU = _zero;
    
    String hotdealFlag = "0", hotdealFlagW = "0", prdHideFlag = "0", prdHideFlagW = "0",
        prdCompFlag = "0";
    
    String prdId = "", catId = "", name = "", nameUp = "", nameLG = "", nameUpLG = "",
        descr = "", descrLG = "";

    BigDecimal vatPct = _zero, retailPrcEU = _zero, wholesalePrcEU = _zero;
    
    String query = null;
    
    String PRD_VAT_ID = "";
    
    int colIndex = 1;
    
    // create insert prepared statement {
    if (dbRet.getNoError() == 1) {
      query = "INSERT INTO product ("
          + " prdId,name,nameUp,nameLG,nameUpLG,retailPrcEU,wholesalePrcEU,vatPct,hdRetailPrcEU,hdWholesalePrcEU"
          + ",prdHideFlag,prdHideFlagW,hotdealFlag,hotdealFlagW,stockQua,inQua,outQua,inVal,inValEU,outVal,outValEU"
          + ",PRD_VAT_ID,giftPrcEU,descr,descrLG,prdCompFlag"
          + ") VALUES ("
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?"
          + ")";
      
      try {
        insertStatement = database.createPreparedStatement(query);
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
    }
    // } create insert prepared statement
    
    // create insert for categories {
    if (dbRet.getNoError() == 1) {
      query = "INSERT INTO prdInCatTab ("
            + "PINCCode,PINCPrdId,PINCCatId,PINCPrimary"
            + ") VALUES ("
            + "?,?,?,?"
            + ")";
      
      try {
        insCatStatement = database.createPreparedStatement(query);
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
    }
    // } create insert for categories
    
    // create update product prepared statement {
    if (dbRet.getNoError() == 1) {
      query = "UPDATE product SET "
            + " name = ?"
            + ",nameUp = ?"
            + ",nameLG = ?"
            + ",nameUpLG = ?"
            + ",prdHideFlag = ?"
            + ",prdHideFlagW = ?"
            + ",retailPrcEU = ?"
            + ",wholesalePrcEU = ?"
            + ",PRD_VAT_ID = ?"
            + " WHERE prdId = ?";
            
      try {
        updStatement = database.createPreparedStatement(query);
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
    }
    
    if (dbRet.getNoError() == 1) {
      query = "UPDATE product SET "
            + " name = ?"
            + ",nameUp = ?"
            + ",nameLG = ?"
            + ",nameUpLG = ?"
            + ",prdHideFlag = ?"
            + ",prdHideFlagW = ?"
            + ",retailPrcEU = ?"
            + ",wholesalePrcEU = ?"
            + ",PRD_VAT_ID = ?"
            + ",descr = ?"
            + ",descrLG = ?"
            + " WHERE prdId = ?";
            
      try {
        updStatementWithDescr = database.createPreparedStatement(query);
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
    }
    // } create update product prepared statement
    
    if (dbRet.getNoError() == 1) {
      try {
        for (line=1; line<excelRows; line++) {
          _manager.setTextMsg("Processing EXCEL line " + (line+1));
          
          prdId = SwissKnife.sqlEncode(sheet.getCell(insert_product_prdId, line).getContents()).trim();
          if (prdId.length() == 0 || prdId.length()>25) throw new Exception();
          
          name = SwissKnife.sqlEncode( sheet.getCell(insert_product_name, line).getContents() ).trim();
          nameUp = SwissKnife.searchConvert(name);
          
          nameLG = SwissKnife.sqlEncode( sheet.getCell(insert_product_nameLG, line).getContents() ).trim();
          nameUpLG = SwissKnife.searchConvert(nameLG);
          
          catId = SwissKnife.sqlEncode(sheet.getCell(insert_product_catId, line).getContents()).trim();
          if (catId.length() == 0 || catId.length()>25) throw new Exception();
          
          prdHideFlag = SwissKnife.sqlEncode(sheet.getCell(insert_product_prdHideFlag, line).getContents()).trim();
          prdHideFlag = !prdHideFlag.equals("0") ? "0" : "1";
          
          prdHideFlagW = SwissKnife.sqlEncode(sheet.getCell(insert_product_prdHideFlagW, line).getContents()).trim();
          prdHideFlagW = !prdHideFlagW.equals("0") ? "0" : "1";
          
          retailPrcEU = new BigDecimal( String.valueOf(((NumberCell)sheet.getCell(insert_product_retailPrcEU, line)).getValue()) );
          wholesalePrcEU = new BigDecimal( String.valueOf(((NumberCell)sheet.getCell(insert_product_wholesalePrcEU, line)).getValue()) );
          
          PRD_VAT_ID = SwissKnife.sqlEncode(sheet.getCell(insert_product_PRD_VAT_ID, line).getContents()).trim();
          if (PRD_VAT_ID.length() == 0) PRD_VAT_ID = "001";
          
          descr = SwissKnife.sqlEncode( sheet.getCell(insert_product_descr, line).getContents() ).trim();
          descrLG = SwissKnife.sqlEncode( sheet.getCell(insert_product_descrLG, line).getContents() ).trim();
          
          // if product already exists update
          if (prdidtable.containsKey(prdId) == true || productExists(prdId, database) == true) {
            colIndex = 0;
            
            if (descr.length() == 0) {
              updStatement.setString(++colIndex, name);
              updStatement.setString(++colIndex, nameUp);
              updStatement.setString(++colIndex, nameLG);
              updStatement.setString(++colIndex, nameUpLG);
              updStatement.setString(++colIndex, prdHideFlag);
              updStatement.setString(++colIndex, prdHideFlagW);

              updStatement.setBigDecimal(++colIndex, retailPrcEU);
              updStatement.setBigDecimal(++colIndex, wholesalePrcEU);

              updStatement.setString(++colIndex, PRD_VAT_ID);

              updStatement.setString(++colIndex, prdId);

              updStatement.executeUpdate();
            }
            else {
              updStatementWithDescr.setString(++colIndex, name);
              updStatementWithDescr.setString(++colIndex, nameUp);
              updStatementWithDescr.setString(++colIndex, nameLG);
              updStatementWithDescr.setString(++colIndex, nameUpLG);
              updStatementWithDescr.setString(++colIndex, prdHideFlag);
              updStatementWithDescr.setString(++colIndex, prdHideFlagW);

              updStatementWithDescr.setBigDecimal(++colIndex, retailPrcEU);
              updStatementWithDescr.setBigDecimal(++colIndex, wholesalePrcEU);

              updStatementWithDescr.setString(++colIndex, PRD_VAT_ID);
              
              updStatementWithDescr.setString(++colIndex, descr);
              updStatementWithDescr.setString(++colIndex, descrLG);

              updStatementWithDescr.setString(++colIndex, prdId);

              updStatementWithDescr.executeUpdate();
            }
          }
          else {
            // insert into product
            colIndex = 0;
            
            insertStatement.setString(++colIndex, prdId);
            insertStatement.setString(++colIndex, name);
            insertStatement.setString(++colIndex, nameUp);
            insertStatement.setString(++colIndex, nameLG);
            insertStatement.setString(++colIndex, nameUpLG);
            
            insertStatement.setBigDecimal(++colIndex, retailPrcEU);
            insertStatement.setBigDecimal(++colIndex, wholesalePrcEU);
            insertStatement.setBigDecimal(++colIndex, vatPct);
            insertStatement.setBigDecimal(++colIndex, hdRetailPrcEU);
            insertStatement.setBigDecimal(++colIndex, hdWholesalePrcEU);
            
            insertStatement.setString(++colIndex, prdHideFlag);
            insertStatement.setString(++colIndex, prdHideFlagW);
            insertStatement.setString(++colIndex, hotdealFlag);
            insertStatement.setString(++colIndex, hotdealFlagW);
            
            insertStatement.setBigDecimal(++colIndex, stockQua);
            
            insertStatement.setBigDecimal(++colIndex, inQua);
            insertStatement.setBigDecimal(++colIndex, outQua);
            insertStatement.setBigDecimal(++colIndex, inVal);
            insertStatement.setBigDecimal(++colIndex, inValEU);
            insertStatement.setBigDecimal(++colIndex, outVal);
            insertStatement.setBigDecimal(++colIndex, outValEU);
            
            insertStatement.setString(++colIndex, PRD_VAT_ID);
            
            insertStatement.setBigDecimal(++colIndex, giftPrcEU);
            
            insertStatement.setString(++colIndex, descr);
            insertStatement.setString(++colIndex, descrLG);
            
            insertStatement.setString(++colIndex, prdCompFlag);
            
            insertStatement.executeUpdate();
          }
          
          if (prdidtable.containsKey(prdId) == true) {
            PINCPrimary = "0";
          }
          else {
            database.execQuery("DELETE FROM prdInCatTab WHERE PINCPrdId = '" + prdId + "'");
            PINCPrimary = "1";
          }
          
          insCatStatement.setString(1, SwissKnife.buildPK());
          insCatStatement.setString(2, prdId);
          insCatStatement.setString(3, catId);
          insCatStatement.setString(4, PINCPrimary);

          insCatStatement.executeUpdate();
          
          prdidtable.put(prdId, prdId);
        }
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        System.err.println("[" + databaseId + "] Problem occured in EXCEL line : " + (line+1));
        e.printStackTrace();
      }
    }
    
    try {
      if (insertStatement != null) insertStatement.close();
      
      if (insCatStatement != null) insCatStatement.close();
      
      if (updStatement != null) updStatement.close();
      if (updStatementWithDescr != null) updStatementWithDescr.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      insertStatement = null;
      insCatStatement = null;
      updStatement = null;
      updStatementWithDescr = null;
    }
    
    database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  private boolean productExists(String prdId, Database database) {
    boolean productExists = false;
    
    String query = "SELECT prdId FROM product WHERE prdId = '" + prdId + "'";
    
    QueryDataSet queryDataSet = null;
    
    try {
      queryDataSet = new QueryDataSet();
      
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();

      if (queryDataSet.getRowCount() > 0) productExists = true;
      else productExists = false;
    }
    catch (Exception e) {
      productExists = false;
      
      e.printStackTrace();
    }
    finally {
      try { queryDataSet.close(); } catch (Exception e) { }
      
      queryDataSet = null;
    }
    
    return productExists;
  }
}