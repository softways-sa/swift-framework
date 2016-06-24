/*
 * Present.java
 *
 * Created on 30 March 2006, 1:14 μμ
 */

package gr.softways.dev.swift.cmrow;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

/**
 *
 * @author  haris
 */

public class Present extends JSPBean {
  
  public Present() {
  }
  
  public DbRet getRelatedCMCategories(String CMRCode, String orderBy) {
    DbRet dbRet = new DbRet();
    
    CMRCode = SwissKnife.sqlEncode(CMRCode);
    orderBy = SwissKnife.sqlEncode(orderBy);
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"CMCategory",AUTH_READ);
    if (auth < 0) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      
      return dbRet;
    }
    
    // hack to work for protected content and take care so that same methods work for admin also
    boolean hasProtectedSection = false, isAdministrator = false, isAuthenticatedUser = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedSection") != null && SwissKnife.jndiLookup("swconf/hasProtectedSection").equals("true")) hasProtectedSection = true;
    if (hasProtectedSection == true) {
      if (getSession().getAttribute(databaseId + ".authGrantLogin") != null) isAdministrator = true;
      if (getSession().getAttribute(databaseId + ".isAuthenticatedUser") != null && getSession().getAttribute(databaseId + ".isAuthenticatedUser").toString().equals("true")) isAuthenticatedUser = true;
    }
    
    String query = "SELECT CMCategory.*, CMCRelCMR.*"
                 + " FROM CMCRelCMR,CMCategory" 
                 + " WHERE CCCR_CMCCode = CMCCode " 
                 + " AND CCCR_CMRCode = '" + CMRCode + "'";
    
    if (hasProtectedSection == true && isAdministrator == false && isAuthenticatedUser == false) {
      query += " AND CMCategory.CMCIsProtected <> '1'";
    }
    
    if (orderBy != null && orderBy.length()>0) {
      query += " ORDER BY " + orderBy;
    }
    
    //System.out.println(query);
    
    database = director.getDBConnection(databaseId);

    database.setReadOnly(true);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
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

    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    database.setReadOnly(false);

    director.freeDBConnection(databaseId,database);
    
    return dbRet;
  }
  
  public DbRet getAdminRelatedCMRows(String CMRCode, String orderBy) {
    DbRet dbRet = new DbRet();
    
    CMRCode = SwissKnife.sqlEncode(CMRCode);
    orderBy = SwissKnife.sqlEncode(orderBy);
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"CMRRelCMR",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    // hack to work for protected content and take care so that same methods work for admin also
    boolean hasProtectedSection = false, isAdministrator = false, isAuthenticatedUser = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedSection") != null && SwissKnife.jndiLookup("swconf/hasProtectedSection").equals("true")) hasProtectedSection = true;
    if (hasProtectedSection == true) {
      if (getSession().getAttribute(databaseId + ".authGrantLogin") != null) isAdministrator = true;
      if (getSession().getAttribute(databaseId + ".isAuthenticatedUser") != null && getSession().getAttribute(databaseId + ".isAuthenticatedUser").toString().equals("true")) isAuthenticatedUser = true;
    }
    
    String query = "SELECT CMRow.CMRCode,CMRow.CMRDateCreated,CMRow.CMRDateUpdated,"+
            "CMRow.CMRTitle,CMRRelCMR.CMCMCode,CMRRelCMR.CMCM_CMRCode1,CMRRelCMR.CMCM_CMRCode2"
                 + ",CMRRelCMR.CMCMIsHidden,CMRRelCMR.CMCMRank FROM CMRow,CMRRelCMR WHERE"
                 + " CMRow.CMRCode = CMRRelCMR.CMCM_CMRCode2"
                 + " AND CMRRelCMR.CMCM_CMRCode1 = '" + CMRCode + "'";
                 
    if (hasProtectedSection == true && isAdministrator == false && isAuthenticatedUser == false) {
      query += " AND CMRow.CMRIsProtected <> '1'";
    }
    
    if (orderBy != null && orderBy.length()>0) {
      query += " ORDER BY " + orderBy;
    }
    
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
  
  public DbRet getRelatedCMRows(String CMRCode, String orderBy) {
    DbRet dbRet = new DbRet();

    CMRCode = SwissKnife.sqlEncode(CMRCode);
    orderBy = SwissKnife.sqlEncode(orderBy);
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"CMRRelCMR",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    // hack to work for protected content and take care so that same methods work for admin also
    boolean hasProtectedSection = false, isAdministrator = false, isAuthenticatedUser = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedSection") != null && SwissKnife.jndiLookup("swconf/hasProtectedSection").equals("true")) hasProtectedSection = true;
    if (hasProtectedSection == true) {
      if (getSession().getAttribute(databaseId + ".authGrantLogin") != null) isAdministrator = true;
      if (getSession().getAttribute(databaseId + ".isAuthenticatedUser") != null && getSession().getAttribute(databaseId + ".isAuthenticatedUser").toString().equals("true")) isAuthenticatedUser = true;
    }
    
    String query = "SELECT CMRow.*,CMRRelCMR.CMCMCode,CMRRelCMR.CMCM_CMRCode1,CMRRelCMR.CMCM_CMRCode2,CMCRelCMR.*"
                 + " FROM CMRow,CMRRelCMR,CMCRelCMR WHERE"
                 + " CMRow.CMRCode = CMRRelCMR.CMCM_CMRCode2"
                 + " AND CMRCode = CCCR_CMRCode"
                 + " AND CCCRPrimary = '1'"
                 + " AND CMRRelCMR.CMCMIsHidden <> '1' AND CMRRelCMR.CMCM_CMRCode1 = '" + CMRCode + "'";
                 
    if (hasProtectedSection == true && isAdministrator == false && isAuthenticatedUser == false) {
      query += " AND CMRow.CMRIsProtected <> '1'";
    }
    
    if (orderBy != null && orderBy.length()>0) {
      query += " ORDER BY " + orderBy;
    }
    
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

  public DbRet getCMRowsCount(String CMCCode, String orderBy) {
    DbRet dbRet = new DbRet();
    
    CMCCode = SwissKnife.sqlEncode(CMCCode);
    orderBy = SwissKnife.sqlEncode(orderBy);
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"CMRow",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    // hack to work for protected content and take care so that same methods work for admin also
    boolean hasProtectedSection = false, isAdministrator = false, isAuthenticatedUser = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedSection") != null && SwissKnife.jndiLookup("swconf/hasProtectedSection").equals("true")) hasProtectedSection = true;
    if (hasProtectedSection == true) {
      if (getSession().getAttribute(databaseId + ".authGrantLogin") != null) isAdministrator = true;
      if (getSession().getAttribute(databaseId + ".isAuthenticatedUser") != null && getSession().getAttribute(databaseId + ".isAuthenticatedUser").toString().equals("true")) isAuthenticatedUser = true;
    }
    
    String query = "SELECT COUNT(*)"
                 + " FROM CMRow, CMCRelCMR, CMCategory WHERE"
                 + " CMCRelCMR.CCCR_CMCCode = CMCategory.CMCCode"
                 + " AND CMCRelCMR.CCCR_CMRCode = CMRow.CMRCode AND CMCRelCMR.CCCRIsHidden <> '1'"
                 + " AND CMCRelCMR.CCCR_CMCCode = '" + CMCCode + "'";
    
    if (hasProtectedSection == true && isAdministrator == false && isAuthenticatedUser == false) {
      query += " AND CMRow.CMRIsProtected <> '1'";
    }
    
    if (orderBy != null && orderBy.length()>0) {
      query += " ORDER BY " + orderBy;
    }
    
    database = director.getDBConnection(databaseId);

    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();
    
    try {
      if (queryDataSet.isOpen()) queryDataSet.close();
      
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();

      dbRet.setRetInt( queryDataSet.getInt(0) );
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }

    database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }


  public DbRet getCMRowsTitles(String CMCCode, String orderBy) {
    DbRet dbRet = new DbRet();

    CMCCode = SwissKnife.sqlEncode(CMCCode);
    orderBy = SwissKnife.sqlEncode(orderBy);

    Director director = Director.getInstance();

    int auth = director.auth(databaseId,authUsername,authPassword,"CMRow",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);

      return dbRet;
    }

    // hack to work for protected content and take care so that same methods work for admin also
    boolean hasProtectedSection = false, isAdministrator = false, isAuthenticatedUser = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedSection") != null && SwissKnife.jndiLookup("swconf/hasProtectedSection").equals("true")) hasProtectedSection = true;
    if (hasProtectedSection == true) {
      if (getSession().getAttribute(databaseId + ".authGrantLogin") != null) isAdministrator = true;
      if (getSession().getAttribute(databaseId + ".isAuthenticatedUser") != null && getSession().getAttribute(databaseId + ".isAuthenticatedUser").toString().equals("true")) isAuthenticatedUser = true;
    }
    
    String query = "SELECT CMRow.CMRCode, CMRow.CMRTitle, CMRow.CMRTitleLG, CMCRelCMR.*, CMCategory.*"
                 + " FROM CMRow, CMCRelCMR, CMCategory WHERE"
                 + " CMCRelCMR.CCCR_CMCCode = CMCategory.CMCCode"
                 + " AND CMCRelCMR.CCCR_CMRCode = CMRow.CMRCode AND CMCRelCMR.CCCRIsHidden <> '1'"
                 + " AND CMCRelCMR.CCCR_CMCCode = '" + CMCCode + "'";

    if (hasProtectedSection == true && isAdministrator == false && isAuthenticatedUser == false) {
      query += " AND CMRow.CMRIsProtected <> '1'";
    }

    if (orderBy != null && orderBy.length()>0) {
      query += " ORDER BY " + orderBy;
    }

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

  public DbRet getCMRowsList(String CMCCode, String orderBy) {
    DbRet dbRet = new DbRet();
    
    CMCCode = SwissKnife.sqlEncode(CMCCode);
    orderBy = SwissKnife.sqlEncode(orderBy);
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"CMRow",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    // hack to work for protected content and take care so that same methods work for admin also
    boolean hasProtectedSection = false, isAdministrator = false, isAuthenticatedUser = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedSection") != null && SwissKnife.jndiLookup("swconf/hasProtectedSection").equals("true")) hasProtectedSection = true;
    if (hasProtectedSection == true) {
      if (getSession().getAttribute(databaseId + ".authGrantLogin") != null) isAdministrator = true;
      if (getSession().getAttribute(databaseId + ".isAuthenticatedUser") != null && getSession().getAttribute(databaseId + ".isAuthenticatedUser").toString().equals("true")) isAuthenticatedUser = true;
    }
    
    String query = "SELECT CMRow.*, CMCRelCMR.*, CMCategory.*"
                 + " FROM CMRow, CMCRelCMR, CMCategory WHERE"
                 + " CMCRelCMR.CCCR_CMCCode = CMCategory.CMCCode"
                 + " AND CMCRelCMR.CCCR_CMRCode = CMRow.CMRCode AND CMCRelCMR.CCCRIsHidden <> '1'"
                 + " AND CMCRelCMR.CCCR_CMCCode = '" + CMCCode + "'";
                 
    if (hasProtectedSection == true && isAdministrator == false && isAuthenticatedUser == false) {
      query += " AND CMRow.CMRIsProtected <> '1'";
    }
    
    if (orderBy != null && orderBy.length()>0) {
      query += " ORDER BY " + orderBy;
    }
    
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
  
  public DbRet getCMRowsList(String CMCCode, String orderBy, int rowsLimit) {
    DbRet dbRet = new DbRet();
    
    CMCCode = SwissKnife.sqlEncode(CMCCode);
    orderBy = SwissKnife.sqlEncode(orderBy);
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"CMRow",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    // hack to work for protected content and take care so that same methods work for admin also
    boolean hasProtectedSection = false, isAdministrator = false, isAuthenticatedUser = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedSection") != null && SwissKnife.jndiLookup("swconf/hasProtectedSection").equals("true")) hasProtectedSection = true;
    if (hasProtectedSection == true) {
      if (getSession().getAttribute(databaseId + ".authGrantLogin") != null) isAdministrator = true;
      if (getSession().getAttribute(databaseId + ".isAuthenticatedUser") != null && getSession().getAttribute(databaseId + ".isAuthenticatedUser").toString().equals("true")) isAuthenticatedUser = true;
    }
    
    String query = "SELECT FIRST " + rowsLimit + " CMRow.*, CMCRelCMR.*, CMCategory.*"
                 + " FROM CMRow, CMCRelCMR, CMCategory WHERE"
                 + " CMCRelCMR.CCCR_CMCCode = CMCategory.CMCCode"
                 + " AND CMCRelCMR.CCCR_CMRCode = CMRow.CMRCode AND CMCRelCMR.CCCRIsHidden <> '1'"
                 + " AND CMCRelCMR.CCCR_CMCCode = '" + CMCCode + "'";
    
    if (hasProtectedSection == true && isAdministrator == false && isAuthenticatedUser == false) {
      query += " AND CMRow.CMRIsProtected <> '1'";
    }
    
    if (orderBy != null && orderBy.length()>0) {
      query += " ORDER BY " + orderBy;
    }
    
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

  public DbRet getChildrenCMRows(String CMCCode, String orderBy, int depth, boolean strict, int rowsLimit) {
    DbRet dbRet = new DbRet();
    
    CMCCode = SwissKnife.sqlEncode(CMCCode);
    orderBy = SwissKnife.sqlEncode(orderBy);
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"CMRow",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    int childLength = CMCCode.length() + 2*depth;
    String strictOption = "";
    if (strict)
      strictOption = " AND SUBSTRING(CMCRelCMR.CCCR_CMCCode FROM " + (childLength-1) + " FOR 2) <> '  '";

    String query = "";
    
    // hack to work for protected content and take care so that same methods work for admin also
    boolean hasProtectedSection = false, isAdministrator = false, isAuthenticatedUser = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedSection") != null && SwissKnife.jndiLookup("swconf/hasProtectedSection").equals("true")) hasProtectedSection = true;
    if (hasProtectedSection == true) {
      if (getSession().getAttribute(databaseId + ".authGrantLogin") != null) isAdministrator = true;
      if (getSession().getAttribute(databaseId + ".isAuthenticatedUser") != null && getSession().getAttribute(databaseId + ".isAuthenticatedUser").toString().equals("true")) isAuthenticatedUser = true;
    }
    
    if (rowsLimit == 0) {
      query = "SELECT CMRow.*, CMCRelCMR.*, CMCategory.*"
                 + " FROM CMRow, CMCRelCMR, CMCategory WHERE"
                 + " CMCRelCMR.CCCR_CMCCode = CMCategory.CMCCode"
            
                 + " AND SUBSTRING(CMCategory.CMCCode FROM 1 FOR " + childLength + ") = CMCCode"
            
                 + " AND CMCRelCMR.CCCR_CMRCode = CMRow.CMRCode AND CMCRelCMR.CCCRIsHidden <> '1'"
                 + " AND CMCRelCMR.CCCR_CMCCode LIKE '" + CMCCode + "%'" + strictOption;
    }
    else {
      query = "SELECT FIRST " + rowsLimit + " CMRow.CMRCode,CMRow.CMRDateCreated,CMRow.CMRDateUpdated,"+
            "CMRow.CMRTitle,CMRow.CMRTitleLG,CMRow.CMRSummary,CMRow.CMRSummaryLG,CMCRelCMR.*,CMCategory.*"
                 + " FROM CMRow, CMCRelCMR, CMCategory WHERE"
                 + " CMCRelCMR.CCCR_CMCCode = CMCategory.CMCCode"
            
                 + " AND SUBSTRING(CMCategory.CMCCode FROM 1 FOR " + childLength + ") = CMCCode"
            
                 + " AND CMCRelCMR.CCCR_CMRCode = CMRow.CMRCode AND CMCRelCMR.CCCRIsHidden <> '1'"
                 + " AND CMCRelCMR.CCCR_CMCCode LIKE '" + CMCCode + "%'" + strictOption;
    }
    
    if (hasProtectedSection == true && isAdministrator == false && isAuthenticatedUser == false) {
      query += " AND CMRow.CMRIsProtected <> '1'";
    }
    
    if (orderBy != null && orderBy.length()>0) {
      query += " ORDER BY " + orderBy;
    }
    
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
  
  public DbRet getCMRow(String CMCCode, String orderBy) {
    DbRet dbRet = new DbRet();
    
    CMCCode = SwissKnife.sqlEncode(CMCCode);
    orderBy = SwissKnife.sqlEncode(orderBy);
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"CMRow",Director.AUTH_READ);
    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    // hack to work for protected content and take care so that same methods work for admin also
    boolean hasProtectedSection = false, isAdministrator = false, isAuthenticatedUser = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedSection") != null && SwissKnife.jndiLookup("swconf/hasProtectedSection").equals("true")) hasProtectedSection = true;
    if (hasProtectedSection == true) {
      if (getSession().getAttribute(databaseId + ".authGrantLogin") != null) isAdministrator = true;
      if (getSession().getAttribute(databaseId + ".isAuthenticatedUser") != null && getSession().getAttribute(databaseId + ".isAuthenticatedUser").toString().equals("true")) isAuthenticatedUser = true;
    }
    
    String query = "SELECT CMRow.*, CMCRelCMR.*, CMCategory.*"
                 + " FROM CMRow, CMCRelCMR, CMCategory WHERE"
                 + " CMCRelCMR.CCCR_CMCCode = CMCategory.CMCCode"
                 + " AND CMCRelCMR.CCCR_CMRCode = CMRow.CMRCode AND CMCRelCMR.CCCRIsHidden <> '1'"
                 + " AND CMCRelCMR.CCCR_CMCCode = '" + CMCCode + "'";
    
    if (hasProtectedSection == true && isAdministrator == false && isAuthenticatedUser == false) {
      query += " AND CMRow.CMRIsProtected <> '1'";
    }
    
    if (orderBy != null && orderBy.length()>0) {
      query += " ORDER BY " + orderBy;
    }
    
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

  public DbRet getCMRow(String CMRCode) {
    DbRet dbRet = new DbRet();
    
    CMRCode = SwissKnife.sqlEncode(CMRCode);
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"CMRow",Director.AUTH_READ);
    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    // hack to work for protected content and take care so that same methods work for admin also
    boolean hasProtectedSection = false, isAdministrator = false, isAuthenticatedUser = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedSection") != null && SwissKnife.jndiLookup("swconf/hasProtectedSection").equals("true")) hasProtectedSection = true;
    if (hasProtectedSection == true) {
      if (getSession().getAttribute(databaseId + ".authGrantLogin") != null) isAdministrator = true;
      if (getSession().getAttribute(databaseId + ".isAuthenticatedUser") != null && getSession().getAttribute(databaseId + ".isAuthenticatedUser").toString().equals("true")) isAuthenticatedUser = true;
    }
    
    String query = "SELECT CMRow.*, CMCRelCMR.*, CMCategory.*"
                 + " FROM CMRow LEFT JOIN CMCRelCMR ON CMCRelCMR.CCCR_CMRCode = CMRow.CMRCode,"
                 + " CMCategory WHERE"
                 + " CMCRelCMR.CCCR_CMCCode = CMCategory.CMCCode AND CMCRelCMR.CCCRIsHidden <> '1'"
                 + " AND CMCRelCMR.CCCRPrimary = '1'"
                 + " AND CMRow.CMRCode = '" + CMRCode + "'";
                 
    if (hasProtectedSection == true && isAdministrator == false && isAuthenticatedUser == false) {
      query += " AND CMRow.CMRIsProtected <> '1'";
    }
    
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
