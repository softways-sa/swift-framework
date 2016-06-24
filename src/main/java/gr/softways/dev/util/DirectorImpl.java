/*
 * DirectorImpl.java
 *
 * Created on 14 Ιούλιος 2003, 10:07 πμ
 */

package gr.softways.dev.util;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.poolmanager.*;

import java.sql.Connection;

/**
 *
 * @author  minotauros
 */
public class DirectorImpl extends Director {
  
  private AppPoolManager _poolManager = null;
  
  DirectorImpl() {
    _poolManager = AppPoolManager.getInstance();
  }
  
  public int auth(String databaseId, String username, String password,
                  String tableName, int permissions) {
    return _poolManager.auth(databaseId, username, password, 
                             tableName, permissions);
  }
  
  public boolean addAuthUser(String databaseId, AuthEmp authEmp) {
    return _poolManager.addAuthUser(databaseId, authEmp);
  }
  public void removeAuthUser(String databaseId, AuthEmp authEmp) {
    _poolManager.removeAuthUser(databaseId, authEmp);
  }
  
  public Database getDBConnection(String databaseId) {
    return _poolManager.getDBConnection(databaseId);
  }
  
  public void freeDBConnection(String databaseId, Database database) {
    _poolManager.freeDBConnection(databaseId, database);
  }
  
  public String getPoolAttr(String attrName) {
    return _poolManager.getAttr(attrName);
  }
}