/*
 * MasterAttribute.java
 *
 * Created on 10 Ιούλιος 2007, 5:46 μμ
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gr.softways.dev.eshop.product.v2;

import java.math.BigDecimal;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

/**
 *
 * @author haris
 */
public class MasterAttribute {
  
  /** Creates a new instance of MasterAttribute */
  public MasterAttribute() {
  }
  
  public MasterAttribute(String prdId, String positionKey, String localeLanguage, String localeCountry, String lang) {
    _prdId = prdId;
    _positionKey = positionKey;
    _localeLanguage = localeLanguage;
    _localeCountry = localeCountry;
    _lang = lang;
  }
  
  public void createPMAV(int size) {
    _masterRows = size;
    _PMAVCode = new String[size];
    _PMAV_ATVACode = new String[size];
    _PMAVName = new String[size];
    _PMAVStock = new String[size];
    _PMAVPrice = new String[size];
    _PMAVImageName_s = new String[size];
    _PMAVImageName_b = new String[size];
    
    _PMAVID = new String[size];
  }
  
  public void setPMAVRow(int i,String PMAVCode,String PMAV_ATVACode,String PMAVName,BigDecimal PMAVStock,
                         BigDecimal PMAVPrice,String PMAVImageName_s,String PMAVImageName_b,String PMAVID) {
    _PMAVCode[i] = PMAVCode;
    _PMAV_ATVACode[i] = PMAV_ATVACode;
    _PMAVName[i] = PMAVName;
    if (PMAVStock != null) _PMAVStock[i] = SwissKnife.formatNumber(PMAVStock,_localeLanguage,_localeCountry,0,2);
    else _PMAVStock[i] = "";
    if (PMAVPrice != null) _PMAVPrice[i] = SwissKnife.formatNumber(PMAVPrice,_localeLanguage,_localeCountry,0,2);
    else _PMAVPrice[i] = "";
    _PMAVImageName_s[i] = PMAVImageName_s;
    _PMAVImageName_b[i] = PMAVImageName_b;
    
    _PMAVID[i] = PMAVID;
  }
  
  public void createPMASV(int size) {
    _slaveRows = size;
    _PMASVCode = new String[size];
    _PMASV_PMAVCode = new String[size];
    _PMASV_ATVACode = new String[size];
    _PMASVName = new String[size];
    _PMASVStock = new String[size];
    _PMASVPrice = new String[size];  
  }  
  
  public void setPMASVRow(int i, String PMASV_PMAVCode, String PMASVCode, String PMASV_ATVACode, String PMASVName,
          BigDecimal PMASVStock, BigDecimal PMASVPrice) {
    _PMASVCode[i] = PMASVCode;
    _PMASV_PMAVCode[i] = PMASV_PMAVCode;
    _PMASV_ATVACode[i] = PMASV_ATVACode;
    _PMASVName[i] = PMASVName;
    if (PMASVStock != null) _PMASVStock[i] = SwissKnife.formatNumber(PMASVStock,_localeLanguage,_localeCountry,0,2);
    else _PMASVStock[i] = "";
    if (PMASVPrice != null) _PMASVPrice[i] = SwissKnife.formatNumber(PMASVPrice,_localeLanguage,_localeCountry,0,2);
    else _PMASVPrice[i] = "";
  }
  
  private String _lang = ""; 
  public void setLang(String lang) { _lang = lang; }
  public String getLang() { return _lang; }
  private String _localeLanguage = ""; 
  public void setLocaleLanguage(String localeLanguage) { _localeLanguage = localeLanguage; }
  public String getLocaleLanguage() { return _localeLanguage; }
  private String _localeCountry = "";
  public void setLocaleCountry(String localeCountry) { _localeCountry = localeCountry; }
  public String getLocaleCountry() { return _localeCountry; }
  private String _prdId = "";
  public void setPrdId(String prdId) { _prdId = prdId; }
  public String getPrdId() { return _prdId; }
  private String _positionKey = "";
  public void setPositionKey(String positionKey) { _positionKey = positionKey; }
  public String getPositionKey() { return _positionKey; }
  private int _masterRows = 0;
  public void setMasterRows(int masterRows) { _masterRows = masterRows; }
  public int getMasterRows() { return _masterRows; }
  private int _slaveRows = 0;
  public void setSlaveRows(int slaveRows) { _slaveRows = slaveRows; }
  public int getSlaveRows() { return _slaveRows; }
  private int _hasSlave = 0;
  public void setHasSlave(int hasSlave) { _hasSlave = hasSlave; }
  public int getHasSlave() { return _hasSlave; }
  private int _hasPrice = 0;
  public void setHasPrice(int hasPrice) { _hasPrice = hasPrice; }
  public int getHasPrice() { return _hasPrice; }
  private int _keepStock = 0;
  public void setKeepStock(int keepStock) { _keepStock = keepStock; }
  public int getKeepStock() { return _keepStock; }
  private String _PMACode = "";
  public void setPMACode(String PMACode) { _PMACode = PMACode; }
  public String getPMACode() { return _PMACode; }
  private String _PMA_atrCode = "";
  public void setPMA_atrCode(String PMA_atrCode) { _PMA_atrCode = PMA_atrCode; }
  public String getPMA_atrCode() { return _PMA_atrCode; }
  private String _SLAT_slave_atrCode = "";
  public void setSLAT_slave_atrCode(String SLAT_slave_atrCode) { _SLAT_slave_atrCode = SLAT_slave_atrCode; }
  public String getSLAT_slave_atrCode() { return _SLAT_slave_atrCode; }
  private String _atrName = "";
  public void setAtrName(String atrName) { _atrName = atrName; }
  public String getAtrName() { return _atrName; }
  private String _slaveAtrName = "";
  public void setSlaveAtrName(String slaveAtrName) { _slaveAtrName = slaveAtrName; }
  public String getSlaveAtrName() { return _slaveAtrName; }
  private int _PMARank = 0;
  public void setPMARank(int PMARank) { _PMARank = PMARank; }
  public int getPMARank() { return _PMARank; }
  private String [] _PMAVCode = null;
  public void setPMAVCode(String [] PMAVCode) { _PMAVCode = PMAVCode; }
  public String [] getPMAVCode() { return _PMAVCode; }
  
  private String [] _PMAVID = null;
  public void setPMAVID(String [] PMAVID) { _PMAVID = PMAVID; }
  public String [] getPMAVID() { return _PMAVID; }
  
  private String [] _PMAV_ATVACode = null;
  public void setPMAV_ATVACode(String [] PMAV_ATVACode) { _PMAV_ATVACode = PMAV_ATVACode; }
  public String [] getPMAV_ATVACode() { return _PMAV_ATVACode; }
  private String [] _PMAVName = null;
  public void setPMAVName(String [] PMAVName) { _PMAVName = PMAVName; }
  public String [] getPMAVName() { return _PMAVName; }
  private String [] _PMAVStock = null;
  public void setPMAVStock(String [] PMAVStock) { _PMAVStock = PMAVStock; }
  public String [] getPMAVStock() { return _PMAVStock; }
  private String [] _PMAVPrice = null;
  public void setPMAVPrice(String [] PMAVPrice) { _PMAVPrice = PMAVPrice; }
  public String [] getPMAVPrice() { return _PMAVPrice; }
  private String [] _PMAVImageName_s = null;
  public void setPMAVImageName_s(String [] PMAVImageName_s) { _PMAVImageName_s = PMAVImageName_s; }
  public String [] getPMAVImageName_s() { return _PMAVImageName_s; }
  private String [] _PMAVImageName_b = null;
  public void setPMAVImageName_b(String [] PMAVImageName_b) { _PMAVImageName_b = PMAVImageName_b; }
  public String [] getPMAVImageName_b() { return _PMAVImageName_b; }
    
  private int _hasSlavePrice = 0;
  public void setHasSlavePrice(int hasSlavePrice) { _hasSlavePrice = hasSlavePrice; }
  public int getHasSlavePrice() { return _hasSlavePrice; }
  private int _keepSlaveStock = 0;
  public void setKeepSlaveStock(int keepSlaveStock) { _keepSlaveStock = keepSlaveStock; }
  public int getKeepSlaveStock() { return _keepSlaveStock; }
  
  private String [] _PMASVCode = null;
  public void setPMASVCode(String [] PMASVCode) { _PMASVCode = PMASVCode; }
  public String [] getPMASVCode() { return _PMASVCode; }
  private String [] _PMASV_PMAVCode = null;
  public void setPMASV_PMAVCode(String [] PMASV_PMAVCode) { _PMASV_PMAVCode = PMASV_PMAVCode; }
  public String [] getPMASV_PMAVCode() { return _PMASV_PMAVCode; }
  private String [] _PMASV_ATVACode = null;
  public void setPMASV_ATVACode(String [] PMASV_ATVACode) { _PMASV_ATVACode = PMASV_ATVACode; }
  public String [] getPMASV_ATVACode() { return _PMASV_ATVACode; }
  private String [] _PMASVName = null;
  public void setPMASVName(String [] PMASVName) { _PMASVName = PMASVName; }
  public String [] getPMASVName() { return _PMASVName; }
  private String [] _PMASVStock = null;
  public void setPMASVStock(String [] PMASVStock) { _PMASVStock = PMASVStock; }
  public String [] getPMASVStock() { return _PMASVStock; }
  private String [] _PMASVPrice = null;
  public void setPMASVPrice(String [] PMASVPrice) { _PMASVPrice = PMASVPrice; }
  public String [] getPMASVPrice() { return _PMASVPrice; }
  
  public static MasterAttribute newMasterAttribute(HttpServletRequest request, String prdId, String positionKey, String localeLanguage, String localeCountry, String lang) {
    MasterAttribute ma = new MasterAttribute(prdId, positionKey, localeLanguage, localeCountry, lang);
    
    fillMasterAttribute(ma,request);
    
    return ma;
  }
  
  private static DbRet fillMasterAttribute(MasterAttribute ma,HttpServletRequest request) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();

    String databaseId = SwissKnife.jndiLookup("swconf/databaseId");
    
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);
           
    int auth = director.auth(databaseId,authUsername,authPassword,"productMasterAttribute",Director.AUTH_READ);

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String query = "SELECT * FROM attribute,attributeValue,productMasterAttribute,productMasterAttributeValue" + 
            " WHERE PMA_atrCode=atrCode AND PMAV_ATVACode=ATVACode AND PMAV_PMACode=PMACode AND PMA_prdId = '"+ SwissKnife.sqlEncode(ma.getPrdId()) + "'"
            + " AND PMAPositionKey='" + ma.getPositionKey() + "'"
            + " ORDER BY PMAVRank DESC";
    
    Database database = director.getDBConnection(databaseId);
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);
    int prevTransIsolation = dbRet.getRetInt();
   
    try {
      if (queryDataSet.isOpen()) queryDataSet.close();
      
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();
      
      dbRet.setRetInt( queryDataSet.getRowCount() );
      
      if (dbRet.getRetInt() > 0) ma.createPMAV(dbRet.getRetInt());
      
      String PMAVID = null;
      
      for (int i=0; i < dbRet.getRetInt(); i++) {
        try {
          PMAVID = SwissKnife.sqlDecode(queryDataSet.getString("PMAVID"));
        }
        catch (Exception e) {
          PMAVID = null;
        }
        
        ma.setPMAVRow(i,SwissKnife.sqlDecode(queryDataSet.getString("PMAVCode")),SwissKnife.sqlDecode(queryDataSet.getString("PMAV_ATVACode")),
                      SwissKnife.sqlDecode(queryDataSet.getString("ATVAValue" + ma.getLang())),queryDataSet.getBigDecimal("PMAVStock"),queryDataSet.getBigDecimal("PMAVPrice"),
                      SwissKnife.sqlDecode(queryDataSet.getString("PMAVImageName_s")),SwissKnife.sqlDecode(queryDataSet.getString("PMAVImageName_b")),
                      PMAVID
                      );
        
        if (SwissKnife.sqlDecode(queryDataSet.getString("atrKeepStock")).equals("1")) ma.setKeepStock(1);
        if (SwissKnife.sqlDecode(queryDataSet.getString("atrHasPrice")).equals("1")) ma.setHasPrice(1);
        ma.setPMACode(SwissKnife.sqlDecode(queryDataSet.getString("PMACode")));
        ma.setPMA_atrCode(SwissKnife.sqlDecode(queryDataSet.getString("PMA_atrCode")));
        ma.setPMARank(queryDataSet.getInt("PMARank"));
        ma.setAtrName(SwissKnife.sqlDecode(queryDataSet.getString("atrName" + ma.getLang())));
        
        queryDataSet.next();
      }
      
      queryDataSet.close();
      
      query = "SELECT * FROM attribute,slaveAttribute"
            + " WHERE atrCode=SLAT_slave_atrCode AND SLAT_master_atrCode = '"+ ma.getPMA_atrCode() + "'";
                     
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();
      
      dbRet.setRetInt( queryDataSet.getRowCount() );
      
      if (dbRet.getRetInt() > 0) {
        ma.setHasSlave(1);
        ma.setSLAT_slave_atrCode(SwissKnife.sqlDecode(queryDataSet.getString("SLAT_slave_atrCode")));
        ma.setSlaveAtrName(SwissKnife.sqlDecode(queryDataSet.getString("atrName" + ma.getLang())));
        if (SwissKnife.sqlDecode(queryDataSet.getString("atrKeepStock")).equals("1")) ma.setKeepSlaveStock(1);
        if (SwissKnife.sqlDecode(queryDataSet.getString("atrHasPrice")).equals("1")) ma.setHasSlavePrice(1);
        
        queryDataSet.close();
        query = "SELECT * FROM  attributeValue, productMasterAttributeValue, PMASV"
             +  " WHERE ATVACode = PMASV_ATVACode AND PMAVCode=PMASV_PMAVCode AND PMAV_PMACode = '"+ ma.getPMACode() + "'"
             + " ORDER BY PMASV_PMAVCode, PMASVRank DESC";
             
        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
        queryDataSet.refresh();
        dbRet.setRetInt( queryDataSet.getRowCount() );
        
        if (dbRet.getRetInt() > 0)
          ma.createPMASV(dbRet.getRetInt());
      
        for (int i=0; i < dbRet.getRetInt(); i++) {
          ma.setPMASVRow(i,SwissKnife.sqlDecode(queryDataSet.getString("PMAVCode")),SwissKnife.sqlDecode(queryDataSet.getString("PMASVCode")),
                         SwissKnife.sqlDecode(queryDataSet.getString("PMASV_ATVACode")),SwissKnife.sqlDecode(queryDataSet.getString("ATVAValue" + ma.getLang())),
                         queryDataSet.getBigDecimal("PMASVStock"),queryDataSet.getBigDecimal("PMASVPrice"));
        
          queryDataSet.next();
        }
      
        queryDataSet.close();
      }
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
