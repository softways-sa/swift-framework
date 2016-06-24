package gr.softways.dev.eshop.attribute;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class Present extends JSPBean {
  
  public Present() {
  }
  
  public DbRet getAdminRelatedAttributes(String atrCode, String orderBy) {
    DbRet dbRet = new DbRet();
    
    orderBy = SwissKnife.sqlEncode(orderBy);
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"slaveAttribute",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      return dbRet;
    }
    
    String query = "SELECT atrCode,atrName,atrNameLG,SLAT_slave_atrCode,SLATCode FROM attribute,slaveAttribute" + 
            " WHERE atrCode=SLAT_slave_atrCode AND SLAT_master_atrCode = '"+ SwissKnife.sqlEncode(atrCode) + "'";
                     
    if (orderBy.length()>0) {
      query += " ORDER BY " + orderBy;
    }
    
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
  
  public DbRet getValues(String atrCode, String orderBy) {
    DbRet dbRet = new DbRet();
    
    orderBy = SwissKnife.sqlEncode(orderBy);
    
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
                     
    if (orderBy.length()>0) {
      query += " ORDER BY " + orderBy;
    }
    
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



