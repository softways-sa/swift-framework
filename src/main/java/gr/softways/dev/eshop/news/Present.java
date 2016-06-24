/*
 * Present.java
 *
 * Created on 16 Ιούλιος 2003, 1:14 μμ
 */

package gr.softways.dev.eshop.news;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

/**
 *
 * @author  minotauros
 */
public class Present extends JSPBean {
  
  public static String TYPE_HIDDEN = "---";
  
  public Present() {
  }
  
  public DbRet getList(String lang, String newsType, String orderBy, int maxRows) {
    return getList(lang, new String[] {newsType}, orderBy, maxRows);
  }  
  public DbRet getList(String lang, String[] newsType, String orderBy, int maxRows) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId, authUsername, authPassword, "newsTab", AUTH_READ);
    if (auth < 0) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      
      return dbRet;
    }

    String query = "SELECT newsCode, newsDay, newsImg, newsImg2, newsUrl"
                 + "      ,newsTitle" + lang
                 + "      ,newsSummary" + lang
                 + "      ,newsType" + lang
                 + " FROM newsTab WHERE ";

    for (int i=0; i<newsType.length; i++) {
      if (i>0) query += " OR ";
      query += " newsType" + lang + " = '" + newsType[i] + "'";
    }

    if (orderBy != null && orderBy.length()>0) 
      query += " ORDER BY " + orderBy;

    database = director.getDBConnection(databaseId);

    dbRet = database.beginTransaction(Database.TRANS_ISO_1);

    int prevTransIsolation = dbRet.getRetInt();

    try {
      if (queryDataSet.isOpen()) queryDataSet.close();

      queryDataSet.setQuery(new QueryDescriptor(database, query, null, 
                                                true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      if (maxRows>0) queryDataSet.setMaxRows(maxRows);
      
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
