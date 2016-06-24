package gr.softways.dev.swift.registeredmember;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

/**
 *
 * @author haris
 */
public class Present extends JSPBean{
  
  /** Creates a new instance of Present */
  public Present() {
  }
  
  public DbRet getRMRow(String RMCode) {
    DbRet dbRet = new DbRet();
    
    RMCode = SwissKnife.sqlEncode(RMCode);
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"registeredMember",Director.AUTH_READ);
    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String query = "SELECT registeredMember.*, ELRelRM.*, emailList.*, users.*"
                 + " FROM registeredMember LEFT JOIN ELRelRM ON"
                + " RMCode = ELRM_RMCode LEFT JOIN emailList ON ELRM_ELCode = ELCode"
                +  " LEFT JOIN users ON RM_logCode=logCode"
                 + " WHERE registeredMember.RMCode = '" + RMCode + "'";
                 
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
  
  public DbRet getELRows(String RMCode) {
    DbRet dbRet = new DbRet();
    
    RMCode = SwissKnife.sqlEncode(RMCode);
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"registeredMember",Director.AUTH_READ);
    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String query = "SELECT registeredMember.RMCode, ELRelRM.*, emailList.*"
                 + " FROM registeredMember,ELRelRM,emailList"
                 + " WHERE registeredMember.RMCode = ELRelRM.ELRM_RMCode"
                 + " AND ELRelRM.ELRM_ELCode = emailList.ELCode" 
                 + " AND emailList.ELActive = '1'"
                 + " AND registeredMember.RMCode = '" + RMCode + "'";

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
  
  public DbRet getMListsUnsubscribed(String RMCode) {
    DbRet dbRet = new DbRet();
    
    RMCode = SwissKnife.sqlEncode(RMCode);
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"registeredMember",Director.AUTH_READ);
    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String query = "SELECT * FROM emailList"
                 + " WHERE ELActive = '1'"
                 + "   AND ELKey = 'NL1'"
                 + "   AND ELCode NOT IN (SELECT ELRM_ELCode FROM ELRelRM WHERE ELRM_RMCode = '" + RMCode + "')";
                 
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
  
  public DbRet getRMCRows(String RMCode) {
    DbRet dbRet = new DbRet();
    
    RMCode = SwissKnife.sqlEncode(RMCode);
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"registeredMember",Director.AUTH_READ);
    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String query = "SELECT registeredMember.RMCode, RMChild.*"
                 + " FROM registeredMember LEFT JOIN RMChild ON"
                + " RMCode = RMC_RMCode"             
                 + " WHERE registeredMember.RMCode = '" + RMCode + "'";
                 
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
