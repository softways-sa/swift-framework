/*
 * Present.java
 *
 * Created on 30 Μάρτιος 2006, 5:17 μμ
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package gr.softways.dev.swift.cmcategory;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

/**
 *
 * @author haris
 */
public class Present  extends JSPBean {
  
  /** Creates a new instance of Present */
  public Present() {
  }
  
  /**
   * This method assumes that catId length equals to 25 and that
   * catDepth is 2.
   */
  public DbRet getParents(String CMCCode, String orderBy) {
    DbRet dbRet = new DbRet();
    
    CMCCode = SwissKnife.sqlEncode(CMCCode);
    orderBy = SwissKnife.sqlEncode(orderBy);
    
    if (CMCCode.length() > 255) {
      dbRet.setNoError(0);

      return dbRet;
    }
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"CMCategory",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    int depth = CMCCode.length()/2;
    String[] parents = new String[depth];
    
    for (int i=0; i<depth; i++) {
      parents[i] = CMCCode.substring(0, (i+1)*2);
    }

    String clause = "";
    
    String query = "SELECT * FROM CMCategory WHERE";
    for (int i=0; i<depth; i++) {
      query += clause + " CMCCode = '" + parents[i] + "'";
      clause = "OR";
    }

    if (orderBy != null && orderBy.length()>0) {
      query += " ORDER BY " + orderBy;
    }
    
    //System.out.println(query);
    
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
  
  public DbRet getCMCategoryTree(String CMCCode, String orderBy) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    CMCCode = SwissKnife.sqlEncode(CMCCode);
    orderBy = SwissKnife.sqlEncode(orderBy);
    
    int auth = director.auth(databaseId,authUsername,authPassword,"CMCategory",Director.AUTH_READ);

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

    String query = "SELECT *"
                 + " FROM CMCategory WHERE CMCShowFlag = '1' AND"
                 + " CMCategory.CMCCode LIKE '" + CMCCode + "%'";
                 
    if (hasProtectedSection == true && isAdministrator == false && isAuthenticatedUser == false) {
      query += " AND CMCategory.CMCIsProtected <> '1'";
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
  
  public DbRet getCMCategoryTree(String orderBy) {
    DbRet dbRet = new DbRet();
    
    orderBy = SwissKnife.sqlEncode(orderBy);
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"CMCategory",Director.AUTH_READ);

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
    
    String query = "SELECT *"
                 + " FROM CMCategory WHERE CMCShowFlag = '1'";
                 
    if (hasProtectedSection == true && isAdministrator == false && isAuthenticatedUser == false) {
      query += " AND CMCategory.CMCIsProtected <> '1'";
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
  
  public DbRet getCMCategoryChildren(String CMCCode, String orderBy) {
    DbRet dbRet = new DbRet();
    
    CMCCode = SwissKnife.sqlEncode(CMCCode);
    orderBy = SwissKnife.sqlEncode(orderBy);
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"CMCategory",Director.AUTH_READ);

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
    
    int childLength = CMCCode.length() + 2;
    String query = "SELECT *"
                 + " FROM CMCategory WHERE CMCShowFlag = '1' AND"
                 + " CMCategory.CMCCode LIKE '" + CMCCode + "%'"
                 + " AND SUBSTRING(CMCategory.CMCCode FROM 1 FOR " + childLength + ") = CMCCode"
                 + " AND CMCategory.CMCCode <> '" + CMCCode + "'";

    if (hasProtectedSection == true && isAdministrator == false && isAuthenticatedUser == false) {
      query += " AND CMCategory.CMCIsProtected <> '1'";
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

  public DbRet getCMCategoryChildren(String CMCCode, int depth, String orderBy) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"CMCategory",Director.AUTH_READ);

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
    
    CMCCode = SwissKnife.sqlEncode(CMCCode);
    orderBy = SwissKnife.sqlEncode(orderBy);
    
    int childLength = CMCCode.length() + 2*depth;
    String query = "SELECT *"
                 + " FROM CMCategory WHERE CMCShowFlag = '1' AND"
                 + " CMCategory.CMCCode LIKE '" + CMCCode + "%'"
                 + " AND SUBSTRING(CMCategory.CMCCode FROM 1 FOR " + childLength + ") = CMCCode"
                 + " AND CMCategory.CMCCode <> '" + CMCCode + "'";
                 
    if (hasProtectedSection == true && isAdministrator == false && isAuthenticatedUser == false) {
      query += " AND CMCategory.CMCIsProtected <> '1'";
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
  
  public String[][] getCMCategoryTreePath(String CMCCode, String lang) {
    DbRet dbRet = new DbRet();
    
    CMCCode = SwissKnife.sqlEncode(CMCCode);
    lang = SwissKnife.sqlEncode(lang);
    
    if (CMCCode.length() > 255) {
      dbRet.setNoError(0);

      return new String[0][0];
    }
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"CMCategory",Director.AUTH_READ);

    int depth = CMCCode.length()/2;
    String[] parents = new String[depth];
    String[][] parentsNames = new String[depth][3];
    for (int i=0; i<depth; i++) {
      parents[i] = CMCCode.substring(0, (i+1)*2);
      parentsNames[i][0]= "";
      parentsNames[i][1]= "";
      parentsNames[i][2]= "";
    }
    
    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return parentsNames;
    }

    // hack to work for protected content and take care so that same methods work for admin also
    boolean hasProtectedSection = false, isAdministrator = false, isAuthenticatedUser = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedSection") != null && SwissKnife.jndiLookup("swconf/hasProtectedSection").equals("true")) hasProtectedSection = true;
    if (hasProtectedSection == true) {
      if (getSession().getAttribute(databaseId + ".authGrantLogin") != null) isAdministrator = true;
      if (getSession().getAttribute(databaseId + ".isAuthenticatedUser") != null && getSession().getAttribute(databaseId + ".isAuthenticatedUser").toString().equals("true")) isAuthenticatedUser = true;
    }
    
    String clause = "";
    
    String query = "SELECT CMCCode, CMCName" + lang + ", CMCURL" + lang + " FROM CMCategory WHERE";
    for (int i=0; i<depth; i++) {
      if (hasProtectedSection == true && isAdministrator == false && isAuthenticatedUser == false) {
          query += clause + " (CMCCode = '" + parents[i] + "' AND CMCategory.CMCIsProtected <> '1')";
      }
      else {
        query += clause + " CMCCode = '" + parents[i] + "'";
      }
      clause = "OR";
    }

    query += " ORDER BY CMCCode";
    
    //System.out.println(query);
    
    database = director.getDBConnection(databaseId);

    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();
    
    try {
      if (queryDataSet.isOpen()) queryDataSet.close();
      
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();

      dbRet.setRetInt( queryDataSet.getRowCount() );
      
      for (int ptr=0; queryDataSet.inBounds(); queryDataSet.next()){
        parentsNames[ptr][0] = SwissKnife.sqlDecode( queryDataSet.getString("CMCName" + lang) );
        parentsNames[ptr][1] = SwissKnife.sqlDecode( queryDataSet.getString("CMCCode") );
        parentsNames[ptr][2] = SwissKnife.sqlDecode( queryDataSet.getString("CMCURL" + lang) );
        ptr++;
      }
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }

    database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    director.freeDBConnection(databaseId, database);
    
    return parentsNames;
  }
}