package gr.softways.dev.eshop.product;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

/**
 *
 * @author  minotauros
 */
public class Present extends JSPBean {
  
  public Present() {
  }
  
  public DbRet getPrdCategories(String prdId, String orderBy) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId, authUsername, authPassword,
                             "prdInCatTab", AUTH_READ);
    if (auth < 0) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      
      return dbRet;
    }
    
    String query = "SELECT prdCategory.catId, prdCategory.catName,"
                 + "       prdCategory.catNameLG, prdCategory.catParentFlag,"
                 + "       prdInCatTab.*"
                 + " FROM prdInCatTab,prdCategory" 
                 + " WHERE PINCCatId = catId " 
                 + " AND PINCPrdId = '" + prdId + "'";
    
    if (orderBy != null && orderBy.length()>0) {
      query += " ORDER BY " + orderBy;
    }
    
    //System.out.println(query);
    
    database = director.getDBConnection(databaseId);

    dbRet = database.beginTransaction(Database.TRANS_ISO_1);

    int prevTransIsolation = dbRet.getRetInt();

    try {
      if (queryDataSet.isOpen()) queryDataSet.close();

      queryDataSet.setQuery(new QueryDescriptor(database, query, null, 
                                                true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      queryDataSet.refresh();

      dbRet.setRetInt( queryDataSet.getRowCount() );
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      
      e.printStackTrace();
    }

    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);

    director.freeDBConnection(databaseId,database);
    
    return dbRet;
  }
  
  public DbRet getPrdAttr2(String prdId, String orderBy) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId, authUsername, authPassword,
                             "attributeTab2", AUTH_READ);
    if (auth < 0) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      
      return dbRet;
    }
    
    String query = "SELECT * FROM attributeTab2"
                 + " WHERE att2PrdId = '" + prdId + "'";
    
    if (orderBy != null && orderBy.length()>0) {
      query += " ORDER BY " + orderBy;
    }
    
    database = director.getDBConnection(databaseId);

    dbRet = database.beginTransaction(Database.TRANS_ISO_1);

    int prevTransIsolation = dbRet.getRetInt();

    try {
      if (queryDataSet.isOpen()) queryDataSet.close();

      queryDataSet.setQuery(new QueryDescriptor(database, query, null, 
                                                true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      queryDataSet.refresh();

      dbRet.setRetInt( queryDataSet.getRowCount() );
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      
      e.printStackTrace();
    }

    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);

    director.freeDBConnection(databaseId,database);
    
    return dbRet;
  }
  
  public DbRet getPrdAttributes(String prdAPrdId, String prdAColorCode, 
                                String prdASizeCode, String orderBy) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId, authUsername, authPassword,
                             "prdAttributes", AUTH_READ);
    if (auth < 0) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      
      return dbRet;
    }
    
    String query = "SELECT * FROM prdAttributes"
                 + " WHERE prdAPrdId = '" + prdAPrdId + "'";
    
    if (prdAColorCode != null && prdAColorCode.length()>0) {
      query += " AND prdAColorCode = '" + prdAColorCode + "'";
    }
    
    if (prdASizeCode != null && prdASizeCode.length()>0) {
      query += " AND prdASizeCode = '" + prdASizeCode + "'";
    }
    
    if (orderBy != null && orderBy.length()>0) {
      query += " ORDER BY " + orderBy;
    }
    
    database = director.getDBConnection(databaseId);

    dbRet = database.beginTransaction(Database.TRANS_ISO_1);

    int prevTransIsolation = dbRet.getRetInt();

    try {
      if (queryDataSet.isOpen()) queryDataSet.close();

      queryDataSet.setQuery(new QueryDescriptor(database, query, null, 
                                                true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      queryDataSet.refresh();

      dbRet.setRetInt( queryDataSet.getRowCount() );
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      
      e.printStackTrace();
    }

    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);

    director.freeDBConnection(databaseId,database);
    
    return dbRet;
  }
  
  public DbRet getPrdMonthly(String prdId, int year) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId, authUsername, authPassword,
                             "prdMonthly", AUTH_READ);
    if (auth < 0) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      
      return dbRet;
    }
    
    String query = "SELECT product.*,prdMonthly.*" 
                 + " FROM product,prdMonthly"
                 + " WHERE product.prdId = prdMonthly.prdMId"
                 + " AND product.prdId = '" + prdId + "'" 
                 + " AND prdMonthly.prdMYear = " + year;
    
    //System.out.println(query);
    
    database = director.getDBConnection(databaseId);

    dbRet = database.beginTransaction(Database.TRANS_ISO_1);

    int prevTransIsolation = dbRet.getRetInt();

    try {
      if (queryDataSet.isOpen()) queryDataSet.close();

      queryDataSet.setQuery(new QueryDescriptor(database, query, null, 
                                                true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      queryDataSet.refresh();

      dbRet.setRetInt( queryDataSet.getRowCount() );
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      
      e.printStackTrace();
    }

    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);

    director.freeDBConnection(databaseId,database);
    
    return dbRet;
  }
  
  /**
   * This method assumes that catId length equals to 25 and that
   * catDepth is 2.
   */
  public DbRet getParents(String catId, String orderBy) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"prdCategory",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    int prdIdsLength = catId.length()/2;
    String[] prdIds = new String[prdIdsLength];
    
    for (int i=0; i<prdIdsLength; i++) {
      prdIds[i] = catId.substring(0, (i+1)*2);
    }

    String clause = "";
    
    String query = "SELECT * FROM prdCategory WHERE";
    for (int i=0; i<prdIdsLength; i++) {
      query += clause + " catId = '" + prdIds[i] + "'";
      clause = "OR";
    }

    if (orderBy != null && orderBy.length()>0) {
      query += " ORDER BY " + orderBy;
    }
    
    //System.out.println(query);
    
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