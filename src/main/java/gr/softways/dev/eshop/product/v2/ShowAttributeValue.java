package gr.softways.dev.eshop.product.v2;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;
import java.math.BigDecimal;

public class ShowAttributeValue extends JSPBean {
  
  public ShowAttributeValue() {
  }
  
  public DbRet getSlaveAttribute(String atrCode) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"slaveAttribute",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      return dbRet;
    }
    
    String query = "SELECT * FROM slaveAttribute" + 
            " WHERE SLAT_master_atrCode = '"+ SwissKnife.sqlEncode(atrCode) + "'";
    
    database = director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);
    int prevTransIsolation = dbRet.getRetInt();
   
    try {
      if (queryDataSet.isOpen()) queryDataSet.close();
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();
      dbRet.setRetInt( queryDataSet.getRowCount() );
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }

    database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  public DbRet getAttributeValues(String atrCode) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"attributeValue",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      return dbRet;
    }
    
    String query = "SELECT * FROM attribute,attributeValue" + 
            " WHERE atrCode=ATVA_atrCode AND ATVA_atrCode = '"+ SwissKnife.sqlEncode(atrCode) + "'";
    
    database = director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);
    int prevTransIsolation = dbRet.getRetInt();
   
    try {
      if (queryDataSet.isOpen()) queryDataSet.close();
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();
      dbRet.setRetInt( queryDataSet.getRowCount() );
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }

    database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  public String [][] getPMAV(String PMACode, String localeLanguage, String localeCountry) {
    DbRet dbRet = new DbRet();
    String [][] PMAV = null;
   
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"productMasterAttributeValue",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      return (String [][])null;
    }
    BigDecimal bd = null;
    String query = "SELECT * FROM productMasterAttributeValue LEFT JOIN PMASV ON PMAVCode=PMASV_PMAVCode"
                + " WHERE PMAV_PMACode = '"+ SwissKnife.sqlEncode(PMACode) + "'";
    
    database = director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);
    int prevTransIsolation = dbRet.getRetInt();
    
    try {
      if (queryDataSet.isOpen()) queryDataSet.close();
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();
      dbRet.setRetInt( queryDataSet.getRowCount() );
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    if (dbRet.getRetInt() > 0) {
      PMAV = new String[dbRet.getRetInt()][13];
      
      for (int i=0; i<dbRet.getRetInt(); i++) {
        PMAV[i][0] = getColumn("PMAVCode");
        PMAV[i][1] = getColumn("PMASVCode");
        PMAV[i][2] = getColumn("PMAV_ATVACode");
        PMAV[i][3] = getColumn("PMASV_ATVACode");
        
        bd = getBig("PMAVStock");
        if (bd != null) PMAV[i][4] = SwissKnife.formatNumber(bd,localeLanguage,localeCountry,0,2);
        else PMAV[i][4] = "";
        bd = getBig("PMAVPrice");
        if (bd != null) PMAV[i][5] = SwissKnife.formatNumber(bd,localeLanguage,localeCountry,0,2);
        else PMAV[i][5] = "";
        PMAV[i][6] = getColumn("PMAVImageName_s");
        PMAV[i][7] = getColumn("PMAVImageName_b");
        
        bd = getBig("PMASVStock");
        if (bd != null) PMAV[i][8] = SwissKnife.formatNumber(bd,localeLanguage,localeCountry,0,2);
        else PMAV[i][8] = "";
        bd = getBig("PMASVPrice");
        if (bd != null) PMAV[i][9] = SwissKnife.formatNumber(bd,localeLanguage,localeCountry,0,2);
        else PMAV[i][9] = "";
        
        PMAV[i][10] = String.valueOf(getInt("PMAVRank"));
        PMAV[i][11] = String.valueOf(getInt("PMASVRank"));
        
        try {
          PMAV[i][12] = getColumn("PMAVID");
        }
        catch (Exception e) { PMAV[i][12] = ""; }
        
        nextRow();
      }
    }

    database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    director.freeDBConnection(databaseId, database);
    
    return PMAV;
  }

  public int getPMAVIndex(String [][] PMAV, int c, String ATVACode) {
    if (PMAV == null) return -1;
    int ATVAIndex = -1;
    for (int i=0; i<PMAV.length && ATVAIndex == -1; i++) {
      if (PMAV[i][c].equals(ATVACode))
        ATVAIndex = i;
    }
    return ATVAIndex;
  }
  
  public int getPMASVIndex(String [][] PMAV, int c1, String ATVACode1, int c2, String ATVACode2) {
    if (PMAV == null) return -1;
    int ATVAIndex = -1;
    for (int i=0; i<PMAV.length && ATVAIndex == -1; i++) {
      if (PMAV[i][c1].equals(ATVACode1) && PMAV[i][c2].equals(ATVACode2))
        ATVAIndex = i;
    }
    return ATVAIndex;
  }  
  
  
  public DbRet getProductMasterAttribute(String prdId, String positionKey) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"productMasterAttributeValue",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      return dbRet;
    }
    
    String query = "SELECT * FROM attribute,attributeValue,productMasterAttribute,productMasterAttributeValue" + 
            " WHERE PMA_atrCode=atrCode AND PMAV_ATVACode=ATVACode AND PMAV_PMACode=PMACode AND PMA_prdId = '"+ SwissKnife.sqlEncode(prdId) + "'"
            + " AND PMAPositionKey='" + positionKey + "'";
    
    database = director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);
    int prevTransIsolation = dbRet.getRetInt();
   
    try {
      if (queryDataSet.isOpen()) queryDataSet.close();
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();
      dbRet.setRetInt( queryDataSet.getRowCount() );
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }

    database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
}
