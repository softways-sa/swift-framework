package gr.softways.dev.eshop.orders.v2;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

/**
 *
 * @author  minotauros
 */
public class WishList extends JSPBean {
  
  public WishList() {
  }
  
  public DbRet getTable(String WLST_customerId) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"WishList",AUTH_READ);
    if (auth < 0) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      
      return dbRet;
    }
    
    String query = "SELECT * FROM product,WishList,VAT WHERE product.prdId = WishList.WLST_prdId AND product.PRD_VAT_ID = VAT.VAT_ID"
        + " AND WishList.WLST_customerId = '" + SwissKnife.sqlEncode(WLST_customerId) + "'"
        + " AND product.prdHideFlagW != '1'";
    
    //System.out.println(query);
    
    database = director.getDBConnection(databaseId);

    dbRet = database.beginTransaction(Database.TRANS_ISO_1);

    int prevTransIsolation = dbRet.getRetInt();

    try {
      if (queryDataSet.isOpen()) queryDataSet.close();

      queryDataSet.setQuery(new QueryDescriptor(database, query, null, true, Load.ALL));
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