package gr.softways.dev.eshop.customer;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

/**
 *
 * @author  minotauros
 */
public class Present extends JSPBean {
  
  public Present() {
  }
  
  public DbRet getCustomer(String customerId, String orderBy) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId, authUsername, authPassword,
                             "customer", AUTH_READ);
    if (auth < 0) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      
      return dbRet;
    }
    
    String query = "SELECT * FROM customer,shipBillInfo,users,userGroups"
                 + " WHERE custLogCode = logCode"
                 + " AND usrAccessLevel = userGroupId"
                 + " AND SBCustomerId = customerId"
                 + " AND customerId = '" + customerId + "'";
    
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
}