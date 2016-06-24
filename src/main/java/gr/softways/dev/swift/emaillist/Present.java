package gr.softways.dev.swift.emaillist;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class Present  extends JSPBean {
  
  /** Creates a new instance of Present */
  public Present() {
  }
  
  public DbRet locateEmailList(String ELCode) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    ELCode = SwissKnife.sqlEncode(ELCode);
    
    int auth = director.auth(databaseId,authUsername,authPassword,"emailList",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String query = "SELECT * FROM emailList"
                 + " WHERE emailList.ELCode = '" + ELCode + "'";
    
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
