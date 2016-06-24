package gr.softways.dev.swift.pendingmember;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

/**
 *
 * @author haris
 */
public class Present  extends JSPBean{
  
  /** Creates a new instance of Present */
  public Present() {
  }
  
  public DbRet getPMRow(String PMCode) {
    DbRet dbRet = new DbRet();
    
    PMCode = SwissKnife.sqlEncode(PMCode);
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"pendingMember",Director.AUTH_READ);
    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String query = "SELECT pendingMember.*, ELRelPM.*, emailList.*"
                 + " FROM pendingMember LEFT JOIN ELRelPM ON"
                + " pendingMember.PMCode = ELRelPM.ELPM_PMCode LEFT JOIN emailList ON ELPM_ELCode=ELCode"             
                 + " WHERE pendingMember.PMCode = '" + PMCode + "'";
                 
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
  
  public DbRet getPMCRows(String PMCode) {
    DbRet dbRet = new DbRet();
    
    PMCode = SwissKnife.sqlEncode(PMCode);
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"pendingMember",Director.AUTH_READ);
    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String query = "SELECT pendingMember.PMCode, PMChild.*"
                 + " FROM pendingMember LEFT JOIN PMChild ON"
                + " PMCode = PMC_PMCode"             
                 + " WHERE pendingMember.PMCode = '" + PMCode + "'";
                 
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
