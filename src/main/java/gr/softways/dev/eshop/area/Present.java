package gr.softways.dev.eshop.area;

import java.io.*;
import java.util.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

import javax.servlet.http.*;

public class Present extends JSPBean {

  public Present() {
  }

  public int getAreas(String orderBy) {
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId, authUsername, authPassword, 
                             "area", Director.AUTH_READ);

    if (auth != Director.AUTH_OK) {
      return auth;
    }
    
    String query = "SELECT * FROM area,areaZones" 
                 + " WHERE areaZone = AZCode";

    if (orderBy != null && orderBy.length()>0) {
      query += " ORDER BY " + orderBy;
    }

    int rows = 0;

    DbRet dbRet = null;
    
    database = director.getDBConnection(databaseId);

    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();

    try {
      // κλείσε το query ώστε να το τροποποιήσουμε
      if (queryDataSet.isOpen()) queryDataSet.close();

      queryDataSet.setQuery(new QueryDescriptor(database, query, null, true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      
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