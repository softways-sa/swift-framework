package gr.softways.dev.util;

import gr.softways.dev.jdbc.*;

/**
 *
 * @author  minotauros
 */
public class Configuration {

  protected Configuration() {
  }
  
  public static String getValue(String key) {
    return getValue(key, null);
  }
  public static String getValue(String key, String lang) {
    String value = "";
    
    if (lang == null) lang = "";
    
    String query = "SELECT * FROM Configuration WHERE CO_Key = '" + SwissKnife.sqlEncode(key)+ "'";
    
    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(_databaseId);

    QueryDataSet queryDataSet = new QueryDataSet();
    
    DbRet dbRet = null;

    dbRet = database.beginTransaction(Database.TRANS_ISO_1);

    int prevTransIsolation = dbRet.getRetInt();

    try {
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();
      
      if (queryDataSet.getRowCount() <= 0) {
        value = null;
      }
      else value = SwissKnife.sqlDecode(queryDataSet.getString("CO_Value" + lang));
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    finally {
      queryDataSet.close();
    }
    
    database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    director.freeDBConnection(_databaseId, database);
    
    return value;
  }
  
  public static String[] getValues(String[] key) {
    return getValues(key, null);
  }
  public static String[] getValues(String[] key, String lang) {
    String[] values = new String[key.length];
    
    if (lang == null) lang = "";
    
    String query = "SELECT * FROM Configuration WHERE CO_Key IN (";
    
    for (int i=0; i<key.length; i++) {
      query += "'" + SwissKnife.sqlEncode(key[i]) + "'";
      
      if (i+1<key.length) query += ",";
    }
    
    query += ")";
    
    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(_databaseId);

    QueryDataSet queryDataSet = new QueryDataSet();
    
    DbRet dbRet = null;

    dbRet = database.beginTransaction(Database.TRANS_ISO_1);

    int prevTransIsolation = dbRet.getRetInt();

    try {
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();
      
      for (int i=0; i<key.length; i++) {
        if (SwissKnife.locateOneRow("CO_Key", SwissKnife.sqlEncode(key[i]), queryDataSet) == true) values[i] = SwissKnife.sqlDecode(queryDataSet.getString("CO_Value" + lang));
      }
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    finally {
      queryDataSet.close();
    }
    
    database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    director.freeDBConnection(_databaseId, database);
    
    return values;
  }
  
  private static String _databaseId = SwissKnife.jndiLookup("swconf/databaseId");
}