package gr.softways.dev.eshop.emaillists.lists;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class Present extends JSPBean  {

  public static String STATUS_INACTIVE = "0";
  public static String STATUS_ACTIVE = "1";
  public static String STATUS_UNREGISTERED = "2";
  public static String STATUS_UNVERIFIED = "6";
  
  public Present() {
  }

  public int locateEmailList(String emailListCode) {
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId, authUsername, authPassword,
                                "emailListTab", AUTH_READ);

    if (auth < 0) return auth;

    String query = "SELECT * FROM emailListTab"
                 + " WHERE EMLTCode = '" + emailListCode + "'";

    int found = 0;

    // System.out.println(query);

    database = director.getDBConnection(databaseId);

    DbRet dbRet = null;

    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();

    try {
      // κλείσε το query ώστε να το ανοίξουμε ξανά
      if (queryDataSet.isOpen()) queryDataSet.close();

      queryDataSet.setQuery(new QueryDescriptor(database, query, null, true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      // εκτέλεσε το query
      queryDataSet.refresh();

      found = queryDataSet.getRowCount();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    database.commitTransaction(1,prevTransIsolation);

    director.freeDBConnection(databaseId,database);
    return found;
  }
}