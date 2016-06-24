package gr.softways.dev.eshop.orders.v2;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

/**
 *
 * @author  minotauros
 */
public class Present extends JSPBean {
  
  public Present() {
  }
  
  public DbRet getOrder(String orderId, String orderBy) {
    return getOrder(null,orderId,orderBy);
  }
  public DbRet getOrder(String customerId, String orderId, String orderBy) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"orders",AUTH_READ);
    if (auth < 0) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      
      return dbRet;
    }
    
    String query = "SELECT orders.*, transactions.*, product.name, product.nameLG, product.prdId, product.catId, transAttribute.*"
      + " FROM orders"
      + " LEFT JOIN transactions ON orders.orderId=transactions.orderId"
      + " LEFT JOIN product ON transactions.prdId=product.prdId"
      + " LEFT JOIN transAttribute ON (transactions.transId=transAttribute.TAV_transId AND transactions.orderId=transAttribute.TAV_orderId)"
      + " WHERE orders.orderId = '" + orderId + "'";
      
    if (customerId != null && customerId.length()>0) {
      query += " AND orders.customerId = '" + customerId + "'";
    }
    
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