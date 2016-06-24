package gr.softways.dev.util;

import gr.softways.dev.jdbc.*;

/**
 *
 * @author  haris
 */
public class SQLHelper2 extends JSPBean {
  
  public SQLHelper2() {
    databaseId = SwissKnife.jndiLookup("swconf/databaseId");
  }
  
  public DbRet getSQL(String SQLQuery) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    database = director.getDBConnection(databaseId);

    database.setReadOnly(true);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();

    try {
      if (queryDataSet.isOpen()) queryDataSet.close();

      queryDataSet.setQuery(new QueryDescriptor(database, SQLQuery, null, true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      queryDataSet.refresh();

      dbRet.setRetInt( queryDataSet.getRowCount() );
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      
      e.printStackTrace();
    }

    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    database.setReadOnly(false);

    director.freeDBConnection(databaseId,database);
    
    return dbRet;
  }
  public DbRet getSQL(Database database, String SQLQuery) {
    DbRet dbRet = new DbRet();
    
    try {
      if (queryDataSet.isOpen()) queryDataSet.close();

      queryDataSet.setQuery(new QueryDescriptor(database, SQLQuery, null, true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      queryDataSet.refresh();

      dbRet.setRetInt( queryDataSet.getRowCount() );
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      
      e.printStackTrace();
    }
    
    return dbRet;
  }
}