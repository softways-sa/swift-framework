package gr.softways.dev.eshop.manufacturer;

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
  
  public DbRet getManufactForPrdCat(String catId, String orderBy) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"manufact",Director.AUTH_READ);

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
    
    String query = "SELECT DISTINCT manufact.* FROM manufact,product,prdInCatTab,prdCategory"
                 + " WHERE manufactId = prdManufactId"
                 + " AND PINCPrdId = prdId"
                 + " AND PINCCatId = prdCategory.catId"
                 + " AND prdCategory.catId LIKE '" + SwissKnife.sqlEncode(catId) + "%'";
    
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
