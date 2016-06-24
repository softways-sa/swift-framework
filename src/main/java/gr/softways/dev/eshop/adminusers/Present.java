package gr.softways.dev.eshop.adminusers;

import java.io.*;
import java.util.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

import javax.servlet.http.*;

public class Present extends JSPBean {

  public Present() {
  }

  public int locateAdminUser(String ausrCode) {
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId, authUsername, authPassword,
                             "adminUsers", AUTH_READ);

    if (auth != AUTH_OK)
    {
      return auth;
    }

    int rows = 0;

    String query = "SELECT * FROM adminUsers,users WHERE " +
                   " ausrLogCode = users.logCode AND " +
                   " ausrCode = '" + ausrCode + "'";

    // System.out.println(query);

    database = director.getDBConnection(databaseId);

    DbRet dbRet = null;

    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.retInt;

    try {
      // κλείσε το query ώστε να το ανοίξουμε ξανά
      if (queryDataSet.isOpen())
        queryDataSet.close();

      queryDataSet.setQuery(new QueryDescriptor(database, query, null, true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      // εκτέλεσε το query
      queryDataSet.refresh();

      rows = queryDataSet.getRowCount();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    database.commitTransaction(1,prevTransIsolation);

    director.freeDBConnection(databaseId,database);
    return rows;
  }
}
