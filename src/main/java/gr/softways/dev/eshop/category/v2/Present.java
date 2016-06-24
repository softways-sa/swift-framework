package gr.softways.dev.eshop.category.v2;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

import gr.softways.dev.eshop.eways.Customer;

/**
 *
 * @author  minotauros
 */
public class Present extends JSPBean {
  
  /** Creates a new instance of Present */
  public Present() {
  }
  
  /**
   * This method assumes that catId length equals to 25 and that
   * catDepth is 2.
   */
  public DbRet getCategLevel(int level, String orderBy) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"prdCategory",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String lang = "";
    try {
      lang = (String) getSession().getAttribute(databaseId + ".lang");
    }
    catch (Exception e) {
    }
    
    boolean hasProtectedPrdCat = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedPrdCat") != null && SwissKnife.jndiLookup("swconf/hasProtectedPrdCat").equals("true")) hasProtectedPrdCat = true;
    
    int customerType = 0;
    
    if (hasProtectedPrdCat == true) {
      try {
        if (getSession().getAttribute(databaseId + ".front_end.customerType") != null) customerType = Integer.parseInt(getSession().getAttribute(databaseId + ".front_end.customerType").toString());
        else customerType = Customer.CUSTOMER_TYPE_RETAIL;
      }
      catch (Exception e) {
        customerType = Customer.CUSTOMER_TYPE_RETAIL;
      }
    }
    
    StringBuilder s = new StringBuilder();
    
    for (int i=0; i<level; i++) s.append("__");
    
    for (int i=0; i<25-s.length(); i++) s.append(" ");
    
    StringBuilder query = new StringBuilder();
    
    query.append("SELECT catId,catParentFlag,catImgName1,catImgName2,catCustomerType");
    query.append(",catName").append(lang);
    query.append(" FROM prdCategory WHERE catId LIKE '").append(s.toString()).append("%'");
    query.append(" AND catShowFlag = '1'");

    if (hasProtectedPrdCat == true) {
      query.append(" AND (catCustomerType IS NULL OR catCustomerType = ").append(customerType).append(")");
    }
    
    if (orderBy != null && orderBy.length()>0) {
      query.append(" ORDER BY ").append(orderBy);
    }
    
    //System.out.println(query);
    
    database = director.getDBConnection(databaseId);

    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();
    
    try {
      if (queryDataSet.isOpen()) queryDataSet.close();
      
      queryDataSet.setQuery(new QueryDescriptor(database,query.toString(),null,true,Load.ALL));
      
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
  
  /**
   * This method assumes that catId length equals to 25 and that
   * catDepth is 2.
   */
  public DbRet getSubCateg(String catId, String orderBy) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"prdCategory",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    boolean hasProtectedPrdCat = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedPrdCat") != null && SwissKnife.jndiLookup("swconf/hasProtectedPrdCat").equals("true")) hasProtectedPrdCat = true;
    
    int customerType = 0;
    
    if (hasProtectedPrdCat == true) {
      try {
        if (getSession().getAttribute(databaseId + ".front_end.customerType") != null) customerType = Integer.parseInt(getSession().getAttribute(databaseId + ".front_end.customerType").toString());
        else customerType = Customer.CUSTOMER_TYPE_RETAIL;
      }
      catch (Exception e) {
        customerType = Customer.CUSTOMER_TYPE_RETAIL;
      }
    }
    
    String query = "SELECT * FROM prdCategory"
                 + " WHERE catId LIKE '" + SwissKnife.sqlEncode(catId) + "%'"
                 + " AND catShowFlag = '1'"
                 + " AND catId != '" + SwissKnife.sqlEncode(catId) + "'";
                 
    if (hasProtectedPrdCat == true) query += " AND (catCustomerType IS NULL OR catCustomerType = " + customerType + ")";
    
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
  
  /**
   * This method assumes that catId length equals to 25 and that
   * catDepth is 2.
   */
  public DbRet getSubCateg(String catId, int level, String orderBy) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"prdCategory",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    boolean hasProtectedPrdCat = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedPrdCat") != null && SwissKnife.jndiLookup("swconf/hasProtectedPrdCat").equals("true")) hasProtectedPrdCat = true;
    
    int customerType = 0;
    
    if (hasProtectedPrdCat == true) {
      try {
        if (getSession().getAttribute(databaseId + ".front_end.customerType") != null) customerType = Integer.parseInt(getSession().getAttribute(databaseId + ".front_end.customerType").toString());
        else customerType = Customer.CUSTOMER_TYPE_RETAIL;
      }
      catch (Exception e) {
        customerType = Customer.CUSTOMER_TYPE_RETAIL;
      }
    }
    
    String s = "";
    
    for (int i=0; i<level; i++) s = s + "__";
    
    for (int i=0; i<25-s.length(); i++) s = s + " ";
    
    String query = "SELECT * FROM prdCategory"
                 + " WHERE catId LIKE '" + SwissKnife.sqlEncode(catId) + "%'"
                 + " AND catId LIKE '" + s + "%'"
                 + " AND catShowFlag = '1'"
                 + " AND catId != '" + SwissKnife.sqlEncode(catId) + "'";
                 
    if (hasProtectedPrdCat == true) query += " AND (catCustomerType IS NULL OR catCustomerType = " + customerType + ")";
    
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
  
  /**
   * This method assumes that catId length equals to 25 and that
   * catDepth is 2.
   */
  public DbRet getSubCateg(String catId, String manufactId, int level, String orderBy) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"prdCategory",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    boolean hasProtectedPrdCat = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedPrdCat") != null && SwissKnife.jndiLookup("swconf/hasProtectedPrdCat").equals("true")) hasProtectedPrdCat = true;
    
    int customerType = 0;
    
    if (hasProtectedPrdCat == true) {
      try {
        if (getSession().getAttribute(databaseId + ".front_end.customerType") != null) customerType = Integer.parseInt(getSession().getAttribute(databaseId + ".front_end.customerType").toString());
        else customerType = Customer.CUSTOMER_TYPE_RETAIL;
      }
      catch (Exception e) {
        customerType = Customer.CUSTOMER_TYPE_RETAIL;
      }
    }
    
    String s = "";
    
    for (int i=0; i<level; i++) s = s + "__";
    
    for (int i=0; i<25-s.length(); i++) s = s + " ";
    
    String query = "SELECT * FROM prdCategory"
                 + " WHERE catId LIKE '" + SwissKnife.sqlEncode(catId) + "%'"
                 + " AND catId LIKE '" + s + "%'"
                 + " AND catShowFlag = '1'"
                 + " AND catId != '" + SwissKnife.sqlEncode(catId) + "'"
                 + " AND catId IN ("
                 + "                SELECT SUBSTRING(PINCCatId FROM 1 FOR " + (level*2) + ")"
                 + "                FROM product,prdInCatTab"
                 + "                WHERE PINCPrdId = prdId"
                 + "                AND prdManufactId = '" + SwissKnife.sqlEncode(manufactId) + "')";
                 
    if (hasProtectedPrdCat == true) query += " AND (catCustomerType IS NULL OR catCustomerType = " + customerType + ")";
    
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
  
  
  /**
   * This method assumes that catId length equals to 25 and that
   * catDepth is 2.
   */
  public DbRet getCatPath(String catId, String orderBy) {
    DbRet dbRet = new DbRet();
    
    if (catId.length() > 255) {
      dbRet.setNoError(0);

      return dbRet;
    }
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"prdCategory",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    boolean hasProtectedPrdCat = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedPrdCat") != null && SwissKnife.jndiLookup("swconf/hasProtectedPrdCat").equals("true")) hasProtectedPrdCat = true;
    
    int customerType = 0;
    
    if (hasProtectedPrdCat == true) {
      try {
        if (getSession().getAttribute(databaseId + ".front_end.customerType") != null) customerType = Integer.parseInt(getSession().getAttribute(databaseId + ".front_end.customerType").toString());
        else customerType = Customer.CUSTOMER_TYPE_RETAIL;
      }
      catch (Exception e) {
        customerType = Customer.CUSTOMER_TYPE_RETAIL;
      }
    }
    
    int prdIdsLength = catId.length()/2;
    String[] prdIds = new String[prdIdsLength];
    
    for (int i=0; i<prdIdsLength; i++) {
      prdIds[i] = catId.substring(0, (i+1)*2);
    }

    String clause = "";
    
    String query = "SELECT * FROM prdCategory WHERE (";
    for (int i=0; i<prdIdsLength; i++) {
      query += clause + " catId = '" + prdIds[i] + "'";
      clause = " OR ";
    }
    query += ")";
    
    if (hasProtectedPrdCat == true) query += " AND (catCustomerType IS NULL OR catCustomerType = " + customerType + ")";

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
