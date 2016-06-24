/*
 * JSPBean.java
 *
 * Created on 11 Ιούλιος 2003, 1:11 μμ
 */

package gr.softways.dev.util;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.poolmanager.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author  minotauros
 */
public class JSPBean implements ACLInterface {

  // Αναγνωριστικό όνομα του database pool που χρησιμοποιεί
  // το συγκεκριμένο bean. Η τιμή του ορίζεται από το καθε ενα
  // bean που κάνει extend αυτό το class.
  public String databaseId = "DEF_SERVLET_ID";
  
  // username & password for method authentication
  public String authUsername = "", authPassword = "";
  
  protected Database database = null;
  protected QueryDataSet queryDataSet = new QueryDataSet();
  
  /* standard objects that need to be setup for each JSP Bean */
  private HttpServletRequest _request;
  private HttpServletResponse _response;
  private Servlet _servlet;
  private HttpSession _session;
  
  public JSPBean() {
  }
  
  public void initBean(String databaseId, HttpServletRequest request,
                       HttpServletResponse response, Servlet servlet,
                       HttpSession session) {
    setSession(session);
    setDatabaseId(databaseId);
    setRequest(request);
    setResponse(response);
    setServlet(servlet);
    
    HttpSession s = getSession();
    String id = getDatabaseId();
    
    if (s != null) {
      if (s.getAttribute(id + ".authUsername") != null) {
        authUsername = s.getAttribute(id + ".authUsername").toString();
      }
      if (s.getAttribute(id + ".authPassword") != null) {
        authPassword = s.getAttribute(id + ".authPassword").toString();
      }
    }
    
    s = null;
  }
  
  /**
   * Απόλυτη μετακίνηση του δείκτη.
   */
  public boolean goToRow(int row) {
    try {
      return queryDataSet.goToRow(row);
    }
    catch (DataSetException e) {
      e.printStackTrace();
     }
     return false;
  }
  
  /**
   * set servlet
   */
  public void setServlet(Servlet servlet) {
    _servlet = servlet;
  }

  /**
   * set request, session, authUsername & authPassword
   */
  public void setRequest(HttpServletRequest request) {
    _request = request;
  }

  /**
   * set response
   */
  public void setResponse(HttpServletResponse response) {
    _response = response;
  }

  /**
   * set databaseId
   */
  public void setDatabaseId(String databaseStr) {
    databaseId = databaseStr;
  }

  /**
   * set session
   */
  public void setSession(HttpSession session) {
    _session = session;
  }

  /**
   * set authUsername
   */
  public void setAuthUsername(String authUsernameStr) {
    authUsername = authUsernameStr;
  }

  /**
   * set authUsername
   */
  public void setAuthPassword(String authPasswordStr) {
    authPassword = authPasswordStr;
  }

  /**
   * get servlet
   */
  public Servlet getServlet() {
    return _servlet;
  }

  /**
   * get request
   */
  public HttpServletRequest getRequest() {
    return _request;
  }

  /**
   * get response
   */
  public HttpServletResponse getResponse() {
    return _response;
  }

  public String getDatabaseId() {
    return databaseId;
  }
   
  /**
   * get session
   */
  public HttpSession getSession() {
    return _session;
  }
  
  /**
    *  Κλείσιμο των resources.
   */
  public void closeResources() {
    try {
      queryDataSet.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   *
   */
  public String getHexColumn(String columnName) {
    return SwissKnife.hexEscape(getColumn(columnName));
  }
  
  /**
   *
   */
  public String getColumn(String columnName) {
    try {
      return SwissKnife.sqlDecode( queryDataSet.format(columnName) );
    }
    catch (Exception e) {
      System.out.println("Exception getting column " + columnName + " for " + databaseId);
    }
    
    return "";
  }
  
  public String getString(String columnName) {
    try {
      return SwissKnife.sqlDecode( queryDataSet.getString(columnName) );
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return "";
  }
  
  public int getInt(String columnName) {
    try {
      return queryDataSet.getInt(columnName);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }
  
  public Timestamp getTimestamp(String columnName) {
    Timestamp date = null; // Timestamp.valueOf("1900-01-01 00:00:00.00");

    try {
      if (queryDataSet.isNull(columnName) == false) {
        date = queryDataSet.getTimestamp(columnName);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return date;
  }
  
  public BigDecimal getBig(String columnName) {
    BigDecimal big = null;
    
    try {
      if (queryDataSet.isNull(columnName) == false) {
        big = queryDataSet.getBigDecimal(columnName);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return big;
  }
  
  public boolean nextRow() {
    try {
      return queryDataSet.next();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }
  
  public boolean inBounds() {
    try {
      return queryDataSet.inBounds();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }
  
  public int getTable(String tableName, String orderBy) {
    int authStatus = 0;
    
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();

    authStatus = director.auth(databaseId,authUsername,authPassword,
                               tableName,AUTH_READ);

    if (authStatus <= 0) return authStatus;

    int rows = 0;

    String query = "SELECT * FROM " + tableName;
    
    if (orderBy != null && orderBy.length()>0) {
      query += " ORDER BY " + orderBy;
    }

    database = director.getDBConnection(databaseId);

    database.setReadOnly(true);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    try {
      fillQueryDataSet(database, query, Load.ALL);
      
      rows = queryDataSet.getRowCount();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }

    database.commitTransaction(1, prevTransIsolation);
    database.setReadOnly(false);

    director.freeDBConnection(databaseId, database);
    
    return rows;
  }
  
  /**
   * Retrieve rows from table tableName where column columnName equals to
   * columnValue (presumes column is not numeric).
   */
  public int getTablePK(String tableName, String columnName, 
                        String columnValue) {
    return getTablePK(tableName, columnName, columnValue, "");
  }
  
  public int getTablePK(String tableName, String columnName, String columnValue, 
                        String orderBy) {
    int authStatus = 0;
    
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();

    authStatus = director.auth(databaseId,authUsername,authPassword,
                               tableName,AUTH_READ);

    if (authStatus <= 0) return authStatus;

    int rows = 0;

    String query = "SELECT * FROM " + tableName 
                 + " WHERE " + columnName + " = '" + columnValue + "'";
    
    if (orderBy != null && orderBy.length()>0) {
      query += " ORDER BY " + orderBy;
    }

    database = director.getDBConnection(databaseId);

    database.setReadOnly(true);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    try {
      fillQueryDataSet(database, query, Load.ALL);
      
      rows = queryDataSet.getRowCount();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }

    database.commitTransaction(1, prevTransIsolation);
    database.setReadOnly(false);

    director.freeDBConnection(databaseId, database);
    
    return rows;
  }
  
  public boolean locateOneRow(String columnName, String columnValue) {
    return SwissKnife.locateOneRow(columnName, columnValue, queryDataSet);
  }
  
  private DbRet fillQueryDataSet(Database database, String query, int load) 
                throws DataSetException {
    DbRet dbRet = new DbRet();
    
    if (queryDataSet.isOpen()) queryDataSet.close();

    queryDataSet.setQuery(new QueryDescriptor(database, query, 
                                              null, true, load));
    queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

    queryDataSet.refresh();
    
    dbRet.setNoError(1);
      
    return dbRet;
  }
  
  public QueryDataSet getQueryDataSet() {
    return queryDataSet;
  }
}