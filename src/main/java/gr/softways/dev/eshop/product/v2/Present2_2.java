package gr.softways.dev.eshop.product.v2;

import java.util.Vector;

import gr.softways.dev.eshop.eways.Customer;
import gr.softways.dev.eshop.eways.Product;

import gr.softways.dev.eshop.eways.v2.Order;
import gr.softways.dev.eshop.eways.v2.ProductAttribute;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;
import java.sql.Timestamp;

public class Present2_2 extends JSPBean {
  
  /** Creates a new instance of present */
  public Present2_2() {
  }
  
  public DbRet getPrd(String prdId, String inventory) {
    boolean checkForPrdHideFlag = true, checkForPrdCategory = true;
    
    return getPrd(prdId,inventory,checkForPrdHideFlag,checkForPrdCategory);
  }
  
  public DbRet getPrd(String prdId,String inventory,boolean checkForPrdHideFlag,boolean checkForPrdCategory) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"product",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String query = null;
    
    boolean hasProtectedPrdCat = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedPrdCat") != null && SwissKnife.jndiLookup("swconf/hasProtectedPrdCat").equals("true")) hasProtectedPrdCat = true;
    
    int customerType = 0;
    
    try {
      if (getSession().getAttribute(databaseId + ".front_end.customerType") != null) customerType = Integer.parseInt(getSession().getAttribute(databaseId + ".front_end.customerType").toString());
      else customerType = Customer.CUSTOMER_TYPE_RETAIL;
    }
    catch (Exception e) {
      customerType = Customer.CUSTOMER_TYPE_RETAIL;
    }
    
    if (checkForPrdCategory == true) {
      query = "SELECT product.*,prdInCatTab.*,prdCategory.*,VAT.*"
           + " FROM product,prdInCatTab,prdCategory,VAT";
    }
    else {
      query = "SELECT product.*,VAT.*"
           + " FROM product,VAT";
    }
    
    query += " WHERE prdId = '" + SwissKnife.sqlEncode(prdId) + "' AND product.PRD_VAT_ID = VAT.VAT_ID";
    
    if (checkForPrdCategory == true) {
      query += " AND prdCategory.catId = prdInCatTab.PINCCatId"
             + " AND PINCPrimary = '1'"
             + " AND prdCategory.catShowFlag = '1'"
             + " AND product.prdId = prdInCatTab.PINCPrdId";
    }
    
    if (checkForPrdHideFlag == true) {
      if (customerType == Customer.CUSTOMER_TYPE_WHOLESALE) query += " AND product.prdHideFlagW != '1'";
      else query += " AND product.prdHideFlag != '1'";
    }

    if (inventory != null && inventory.equals(Order.STOCK_INVENTORY)) {
      query += " AND stockQua > 0";
    }
    
    if (hasProtectedPrdCat == true) query += " AND (catCustomerType IS NULL OR catCustomerType = " + customerType + ")";
    
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
  
  public DbRet getFeaturedPrds(String inventory,int maxRows,String orderBy) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"product",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    boolean hasProtectedPrdCat = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedPrdCat") != null && SwissKnife.jndiLookup("swconf/hasProtectedPrdCat").equals("true")) hasProtectedPrdCat = true;
    
    int customerType = 0;
    
    try {
      if (getSession().getAttribute(databaseId + ".front_end.customerType") != null) customerType = Integer.parseInt(getSession().getAttribute(databaseId + ".front_end.customerType").toString());
      else customerType = Customer.CUSTOMER_TYPE_RETAIL;
    }
    catch (Exception e) {
      customerType = Customer.CUSTOMER_TYPE_RETAIL;
    }
    
    String query = "SELECT *"
                 + " FROM product,prdInCatTab,prdCategory,VAT"
                 + " WHERE prdCategory.catId = prdInCatTab.PINCCatId"
                 + "   AND product.prdId = prdInCatTab.PINCPrdId"
                 + "   AND product.PRD_VAT_ID = VAT.VAT_ID"
                 + "   AND PINCPrimary = '1'"
                 + "   AND prdCategory.catShowFlag = '1'"
                 + "   AND product.prdNewColl = '1'";
                 
    if (inventory != null && inventory.equals(Order.STOCK_INVENTORY)) {
      query += " AND stockQua > 0";
    }
    
    if (customerType == Customer.CUSTOMER_TYPE_WHOLESALE) query += " AND product.prdHideFlagW != '1'";
    else query += " AND product.prdHideFlag != '1'";
    
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

      if (maxRows>0) {
        queryDataSet.setMaxRows(maxRows);
      }
      
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
  
  public DbRet getHomeFeaturedPrds(String inventory,int maxRows,String orderBy) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"product",Director.AUTH_READ);

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
    
    int customerType = 0;
    try {
      if (getSession().getAttribute(databaseId + ".front_end.customerType") != null) customerType = Integer.parseInt(getSession().getAttribute(databaseId + ".front_end.customerType").toString());
      else customerType = Customer.CUSTOMER_TYPE_RETAIL;
    }
    catch (Exception e) {
      customerType = Customer.CUSTOMER_TYPE_RETAIL;
    }
    
    StringBuilder query = new StringBuilder();
    
    query.append("SELECT");
    if (maxRows > 0) {
      query.append(" FIRST ");
      query.append(maxRows);
    }
    query.append(" product.prdId");
    query.append(",product.name").append(lang);
    query.append(",product.hotdealFlag,product.hdBeginDate,product.hdEndDate,product.hdRetailPrcEU");
    query.append(",product.retailPrcEU,product.giftPrcEU,product.hotdealFlagW,product.hdBeginDateW");
    query.append(",product.hdEndDateW,product.hdWholesalePrcEU,product.wholesalePrcEU,VAT.VAT_Pct");
    query.append(",product.stockQua,product.prdCompFlag");
    query.append(" FROM product");
    query.append(" LEFT JOIN prdInCatTab ON product.prdId = prdInCatTab.PINCPrdId");
    query.append(" LEFT JOIN prdCategory ON prdInCatTab.PINCCatId = prdCategory.catId");
    query.append(" LEFT JOIN VAT ON product.PRD_VAT_ID = VAT.VAT_ID");
    query.append(" WHERE prdInCatTab.PINCPrimary = '1'");
    query.append(" AND prdCategory.catShowFlag = '1'");
    query.append(" AND product.prdNewColl = '1'");
    
    if (inventory != null && inventory.equals(Order.STOCK_INVENTORY)) {
      query.append(" AND stockQua > 0");
    }
    
    if (customerType == Customer.CUSTOMER_TYPE_WHOLESALE) {
      query.append(" AND product.prdHideFlagW = '0'");
    }
    else {
      query.append(" AND product.prdHideFlag = '0'");
    }
    
    if (orderBy != null && orderBy.length()>0) {
      query.append(" ORDER BY ");
      query.append(orderBy);
    }
    
    //System.out.println(query.toString());
    
    database = director.getDBConnection(databaseId);

    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();
    
    try {
      if (queryDataSet.isOpen()) queryDataSet.close();
      
      queryDataSet.setQuery(new QueryDescriptor(database,query.toString(),null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      if (maxRows>0) {
        queryDataSet.setMaxRows(maxRows);
      }
      
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
  
  public DbRet getHomeFeaturedPrds(String inventory,int maxRows,String orderBy,boolean hotdealFlag) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"product",Director.AUTH_READ);

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
    
    int customerType = 0;
    try {
      if (getSession().getAttribute(databaseId + ".front_end.customerType") != null) customerType = Integer.parseInt(getSession().getAttribute(databaseId + ".front_end.customerType").toString());
      else customerType = Customer.CUSTOMER_TYPE_RETAIL;
    }
    catch (Exception e) {
      customerType = Customer.CUSTOMER_TYPE_RETAIL;
    }
    
    Timestamp today = SwissKnife.currentDate();
    
    StringBuilder query = new StringBuilder();
    
    query.append("SELECT");
    if (maxRows > 0) {
      query.append(" FIRST ");
      query.append(maxRows);
    }
    query.append(" product.prdId");
    query.append(",product.name").append(lang);
    query.append(",product.hotdealFlag,product.hdBeginDate,product.hdEndDate,product.hdRetailPrcEU");
    query.append(",product.retailPrcEU,product.giftPrcEU,product.hotdealFlagW,product.hdBeginDateW");
    query.append(",product.hdEndDateW,product.hdWholesalePrcEU,product.wholesalePrcEU,VAT.VAT_Pct");
    query.append(",product.stockQua,product.prdCompFlag");
    query.append(" FROM product");
    query.append(" LEFT JOIN prdInCatTab ON product.prdId = prdInCatTab.PINCPrdId");
    query.append(" LEFT JOIN prdCategory ON prdInCatTab.PINCCatId = prdCategory.catId");
    query.append(" LEFT JOIN VAT ON product.PRD_VAT_ID = VAT.VAT_ID");
    query.append(" WHERE prdInCatTab.PINCPrimary = '1'");
    query.append(" AND prdCategory.catShowFlag = '1'");
    query.append(" AND product.prdNewColl = '1'");
    
    if (inventory != null && inventory.equals(Order.STOCK_INVENTORY)) {
      query.append(" AND stockQua > 0");
    }
    
    if (customerType == Customer.CUSTOMER_TYPE_WHOLESALE) {
      query.append(" AND product.prdHideFlagW = '0'");
      
      if (hotdealFlag) {
        query.append(" AND ( (hotdealFlagW = '").append(Product.HOTDEAL_FLAG_DATE).append("' AND '").append(today).append("' >= hdBeginDateW");
        query.append(" AND '").append(today).append("' <= hdEndDateW)");
        query.append(" OR (hotdealFlagW = '").append(Product.HOTDEAL_FLAG_DATE_STOCK).append("' AND stockQua > 0 AND '").append(today).append("' >= hdBeginDateW");
        query.append(" AND '").append(today).append("' <= hdEndDateW)");
        query.append(" OR (hotdealFlagW = '").append(Product.HOTDEAL_FLAG_STOCK).append("' AND stockQua > 0)");
        query.append(" OR (hotdealFlagW = '").append(Product.HOTDEAL_FLAG_ALWAYS).append("')");
        query.append(")");
      }
      else {
        query.append(" AND ( (hotdealFlagW = '").append(Product.HOTDEAL_FLAG_DATE).append("' AND '").append(today).append("' < hdBeginDateW");
        query.append(" OR '").append(today).append("' > hdEndDateW)");
        query.append(" OR (hotdealFlagW = '").append(Product.HOTDEAL_FLAG_DATE_STOCK).append("' AND stockQua <= 0 AND '").append(today).append("' < hdBeginDateW");
        query.append(" OR '").append(today).append("' > hdEndDateW)");
        query.append(" OR (hotdealFlagW = '").append(Product.HOTDEAL_FLAG_STOCK).append("' AND stockQua <= 0)");
        query.append(" OR (hotdealFlagW != '").append(Product.HOTDEAL_FLAG_ALWAYS).append("')");
        query.append(" OR (hotdealFlagW = '0')");
        query.append(")");
      }
    }
    else {
      query.append(" AND product.prdHideFlag = '0'");
      
      if (hotdealFlag) {
        query.append(" AND ( (hotdealFlag = '").append(Product.HOTDEAL_FLAG_DATE).append("' AND '").append(today).append("' >= hdBeginDate ");
        query.append(" AND '").append(today).append("' <= hdEndDate) ");
        query.append(" OR (hotdealFlag = '").append(Product.HOTDEAL_FLAG_DATE_STOCK).append("' AND stockQua > 0 AND '").append(today).append("' >= hdBeginDate ");
        query.append(" AND '" + today + "' <= hdEndDate) ");
        query.append(" OR (hotdealFlag = '").append(Product.HOTDEAL_FLAG_STOCK).append("' AND stockQua > 0) ");
        query.append(" OR (hotdealFlag = '").append(Product.HOTDEAL_FLAG_ALWAYS).append("')");
        query.append(")");
      }
      else {
        query.append(" AND ( (hotdealFlag = '").append(Product.HOTDEAL_FLAG_DATE).append("' AND '").append(today).append("' < hdBeginDate ");
        query.append(" OR '").append(today).append("' > hdEndDate) ");
        query.append(" OR (hotdealFlag = '").append(Product.HOTDEAL_FLAG_DATE_STOCK).append("' AND stockQua < 0 AND '").append(today).append("' < hdBeginDate ");
        query.append(" OR '").append(today).append("' > hdEndDate) ");
        query.append(" OR (hotdealFlag = '").append(Product.HOTDEAL_FLAG_STOCK).append("' AND stockQua <= 0) ");
        query.append(" OR (hotdealFlag != '").append(Product.HOTDEAL_FLAG_ALWAYS).append("')");
        query.append(" OR (hotdealFlag = '0')");
        query.append(")");
      }
    }
    
    if (orderBy != null && orderBy.length()>0) {
      query.append(" ORDER BY ");
      query.append(orderBy);
    }
    
    //System.out.println(query.toString());
    
    database = director.getDBConnection(databaseId);

    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();
    
    try {
      if (queryDataSet.isOpen()) queryDataSet.close();
      
      queryDataSet.setQuery(new QueryDescriptor(database,query.toString(),null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      if (maxRows>0) {
        queryDataSet.setMaxRows(maxRows);
      }
      
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
  
  public DbRet fillPAVector(Vector PAVector) {
    ProductAttribute pa = null;
    
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"product",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
  
    String query = null;

    database = director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);
    int prevTransIsolation = dbRet.getRetInt();    
    for (int i=0; i<PAVector.size(); i++) {
      pa = (ProductAttribute)PAVector.elementAt(i);
      if (pa.getHasPrice()==1 || pa.getKeepStock()==1) {
        if (pa.getSlaveFlag()==1) 
          query = "SELECT * FROM PMASV WHERE PMASVCode = '" + pa.getPMAVCode() + "'";
        else
          query = "SELECT * FROM productMasterAttributeValue WHERE PMAVCode = '" + pa.getPMAVCode() + "'";
    
        try {
          if (queryDataSet.isOpen()) queryDataSet.close();
          queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
          queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

          queryDataSet.refresh();

          dbRet.setRetInt( queryDataSet.getRowCount() );
          if (dbRet.getRetInt()==1) {
            if (pa.getSlaveFlag()==1) {
              pa.setPMAVPrice(getBig("PMASVPrice"));
              pa.setPMAVStock(getBig("PMASVStock"));
            }
            else {
              pa.setPMAVPrice(getBig("PMAVPrice"));
              pa.setPMAVStock(getBig("PMAVStock"));
            }
          }
        }
        catch (Exception e) {
          dbRet.setNoError(0);
          e.printStackTrace();
        }
      }
    }

    database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  public DbRet getProductMasterAttribute(String prdId, String orderBy) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"productMasterAttribute",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String query = "SELECT *"
                 + " FROM productMasterAttribute,attribute WHERE"
                 + " PMA_atrCode = atrCode"
                 + " AND PMA_prdId = '" + prdId + "'";
                 
    if (orderBy != null && orderBy.length()>0) {
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
  
  public DbRet getRelatedPrds(String prdId, String inventory, int maxRows, String orderBy) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"product",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    boolean hasProtectedPrdCat = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedPrdCat") != null && SwissKnife.jndiLookup("swconf/hasProtectedPrdCat").equals("true")) hasProtectedPrdCat = true;
    
    int customerType = 0;
    
    try {
      if (getSession().getAttribute(databaseId + ".front_end.customerType") != null) customerType = Integer.parseInt(getSession().getAttribute(databaseId + ".front_end.customerType").toString());
      else customerType = Customer.CUSTOMER_TYPE_RETAIL;
    }
    catch (Exception e) {
      customerType = Customer.CUSTOMER_TYPE_RETAIL;
    }
    
    /**String query = "SELECT *"
                 + " FROM product,prdRelatedProducts,prdInCatTab,prdCategory,VAT"
                 + " WHERE prdCategory.catId = prdInCatTab.PINCCatId"
                 + "   AND product.prdId = prdRelatedProducts.PRDRP_relPrdId"
                 + "   AND product.prdId = prdInCatTab.PINCPrdId"
                 + "   AND product.PRD_VAT_ID = VAT.VAT_ID"
                 + "   AND PINCPrimary = '1'"
                 + "   AND prdCategory.catShowFlag = '1'"
                 + "   AND prdRelatedProducts.PRDRP_prdId = '" + SwissKnife.sqlEncode(prdId) + "'";**/
    
    String query = "SELECT * FROM prdRelatedProducts"
        + " LEFT JOIN product ON product.prdId = prdRelatedProducts.PRDRP_relPrdId"
        + " LEFT JOIN prdInCatTab ON product.prdId = prdInCatTab.PINCPrdId"
        + " LEFT JOIN prdCategory ON prdCategory.catId = prdInCatTab.PINCCatId"
        + " LEFT JOIN VAT ON product.PRD_VAT_ID = VAT.VAT_ID"
        + " WHERE prdRelatedProducts.PRDRP_prdId = '" + SwissKnife.sqlEncode(prdId) + "'"
        + " AND PINCPrimary = '1'"
        + " AND prdCategory.catShowFlag = '1'";
                 
    if (inventory != null && inventory.equals(Order.STOCK_INVENTORY)) {
      query += " AND stockQua > 0";
    }
    
    if (customerType == Customer.CUSTOMER_TYPE_WHOLESALE) query += " AND product.prdHideFlagW != '1'";
    else query += " AND product.prdHideFlag != '1'";
    
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

      if (maxRows>0) {
        queryDataSet.setMaxRows(maxRows);
      }
      
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