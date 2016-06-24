/*
 * Director.java
 *
 * Created on 11 Ιούλιος 2003, 6:22 μμ
 */

package gr.softways.dev.util;

import java.sql.Connection;

import gr.softways.dev.jdbc.Database;

import gr.softways.dev.util.SwissKnife;
import gr.softways.dev.util.DbRet;

import gr.softways.dev.poolmanager.AuthEmp;
import gr.softways.dev.poolmanager.ACLInterface;

/**
 *
 * @author  minotauros
 */
public abstract class Director implements ACLInterface {

  private static Director _director;
  
  protected Director() {
  }
  
  public static synchronized Director getInstance() {
    if (_director == null) {
      try {
        _director = (Director) 
          Class.forName(SwissKnife.jndiLookup("swconf/Director")).newInstance();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return _director;
  }
  
  public abstract int auth(String databaseId, String username, String password,
                           String tableName, int permissions);
  
  public abstract boolean addAuthUser(String databaseId, AuthEmp authEmp);
  public abstract void removeAuthUser(String databaseId, AuthEmp authEmp);
  
  public abstract Database getDBConnection(String databaseId);
  
  public abstract void freeDBConnection(String databaseId, Database database);
  
  public abstract String getPoolAttr(String attrName);
}