package gr.softways.dev.eshop.filetemplate;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class Present extends JSPBean {

  public Present() {
  }

  public int locateFileTemplate(String FTemCode) {
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId, authUsername, authPassword,
                             "fileTemplate", Director.AUTH_READ);

    if (auth != Director.AUTH_OK) return auth;

    String query = "SELECT * FROM fileTemplate " +
                   " WHERE FTemCode = '" + FTemCode + "'";

    int found = 0;

    // System.out.println(query);

    database = director.getDBConnection(databaseId);

    DbRet dbRet = null;

    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();

    try {
      // κλείσε το query ώστε να το ανοίξουμε ξανά
      if (queryDataSet.isOpen()) queryDataSet.close();

      queryDataSet.setQuery(new QueryDescriptor(database, query, null, 
                                                true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

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