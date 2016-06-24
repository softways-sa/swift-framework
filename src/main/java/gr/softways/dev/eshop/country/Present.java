package gr.softways.dev.eshop.country;

import java.io.*;
import java.util.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

import javax.servlet.http.*;

public class Present extends JSPBean {

  public Present() {
  }

  public int getCountries() {
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId, authUsername, authPassword,
                             "country", Director.AUTH_READ);

    if (auth != AUTH_OK) {
      return auth;
    }

    String query = "SELECT * FROM country,countryZones" 
                 + " WHERE countryZone = CZCode";

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