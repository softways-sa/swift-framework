package gr.softways.dev.eshop.emaillists.members;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class Present extends JSPBean  {

  public Present() {
  }

  public int locateEmailMember(String EMLMCode) {
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId, authUsername, authPassword,
                             "emailListMember", AUTH_READ);

    if (auth < 0) return auth;

    String query = "SELECT * FROM emailListMember"
                 + " WHERE EMLMCode = '" + EMLMCode + "'";

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

  public int locateMemberLists(String EMLMCode) {
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId, authUsername, authPassword,
                             "emailListReg", AUTH_READ);

    if (auth < 0)
      return auth;


    String query = "SELECT * FROM emailListReg"
                 + " WHERE EMLRMemberCode = '" + EMLMCode + "'";

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
  
  public DbRet updateMemberStatus(String EMLMCode, String EMLMActive) {
    Director director = Director.getInstance();
    
    DbRet dbRet = new DbRet();
    
    int auth = director.auth(databaseId, authUsername, authPassword,
                             "emailListMember", AUTH_UPDATE);

    if (auth < 0) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(1);
      
      return dbRet;
    }

    String query = "UPDATE emailListMember"
                 + " SET EMLMActive = '" + EMLMActive + "'"
                 + " WHERE EMLMCode = '" + EMLMCode + "'";

    database = director.getDBConnection(databaseId);
    
    dbRet = database.execQuery(query);
    
    director.freeDBConnection(databaseId,database);
    
    return dbRet;
  }
}