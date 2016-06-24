package gr.softways.dev.util;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public abstract class SearchBean2 extends JSPBean {

  public SearchBean2(String tableName) {
    _tableName = tableName;
  }
  
  public DbRet doAction(HttpServletRequest request) {
    DbRet dbRet = new DbRet();
    
    int auth = Director.getInstance().auth(databaseId,authUsername,authPassword,_tableName,AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);

      return dbRet;
    }
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1");
    
    try {
        _dispRows = Integer.parseInt(request.getParameter("dispRows"));
    }
    catch (Exception e) {
    }
    if (_dispRows <= 0 || _dispRows > getMaxDispRows()) _dispRows = getDefDispRows();
    
    if ("SEARCH".equals(action)) {
      if (request.getParameter("sorted_by_col") != null) _sorted_by_col = request.getParameter("sorted_by_col");
      if (request.getParameter("sorted_by_order") != null) _sorted_by_order = request.getParameter("sorted_by_order");
      
      dbRet = search(request);
    }
    else if ("UPDATE_SEARCH".equals(action) && _select_clause.length() > 0) {
      dbRet = parseTable(_select_clause, _from_clause);
    }
    else if ("SORT".equals(action) && _select_clause.length() > 0) {
      _sorted_by_col = request.getParameter("sorted_by_col") == null ? "" : request.getParameter("sorted_by_col");
      _sorted_by_order = request.getParameter("sorted_by_order") == null ? "" : request.getParameter("sorted_by_order");
      
      setStart(0);
      
      dbRet = parseTable(_select_clause, _from_clause);
    }
    else if ("CLOSE_SEARCH".equals(action)) {
      _select_clause = "";
      _from_clause = "";
      
      _sorted_by_col = "";
      _sorted_by_order = "";
  
      _totalRowCount = 0;
      _currentRowCount = 0;
      
      //_dispRows = getDefDispRows();
  
      _start = 0;
      
      resetSearch();
    }
    else if (queryDataSet.isOpen()) goToRow(0);
    
    return dbRet;
  }
  
  public void setTotalRowCount(int totalRowCount) {
    _totalRowCount = totalRowCount;
  }
  public int getTotalRowCount() {
    return _totalRowCount;
  }
  
  public int getCurrentRowCount() {
    return _currentRowCount;
  }
  
  public int getDispRows() {
    return _dispRows;
  }
  public void setDispRows(int dispRows) {
    _dispRows = dispRows;
  }
  
  public int getStart() {
    return _start;
  }
  public void setStart(int start) {
    _start = start;
  }
  
  public int getMaxDispRows() {
    return _maxDispRows;
  }
  public void setMaxDispRows(int maxDispRows) {
    _maxDispRows = maxDispRows;
  }
  
  public int getDefDispRows() {
    return _defDispRows;
  }
  public void setDefDispRows(int defDispRows) {
    _defDispRows = defDispRows;
  }
  
  public String getSelectClause() {
    return _select_clause;
  }
  
  public String getFromClause() {
    return _from_clause;
  }
  
  public String getEndClause() {
    return _end_clause;
  }
  
  public String setEndClause(String end_clause) {
    return _end_clause = end_clause;
  }
  
  public int getTotalPages() {
    int totalPages = 0;
    
    if (getTotalRowCount() > 0 && getTotalRowCount() > getDispRows()) {
      totalPages = (getTotalRowCount() / getDispRows());
      if ( (getTotalRowCount() % getDispRows()) > 0 ) totalPages++;
    }
    else totalPages = 1;
    
    return totalPages;
  }
  
  public void setSortedByCol(String sorted_by_col) {
    _sorted_by_col = sorted_by_col;
  }
  public String getSortedByCol() {
    return _sorted_by_col;
  }
  
  public void setSortedByOrder(String sorted_by_order) {
    _sorted_by_order = sorted_by_order;
  }
  public String getSortedByOrder() {
    return _sorted_by_order;
  }
  
  public boolean isSortedBy(String sorted_by_col, String sorted_by_order) {
    boolean isSortedBy = false;
    
    if (sorted_by_col.equalsIgnoreCase(_sorted_by_col) && sorted_by_order.equalsIgnoreCase(_sorted_by_order)) isSortedBy = true;
    
    return isSortedBy;
  }
  
  public int getCurrentPage() {
    int currentPage = 0;
    
    if (getTotalPages()>1 && _start > 0) {
      currentPage = (_start / getDispRows()) + 1;
      
      if (currentPage <= 0) currentPage = 1;
    }
    else currentPage = 1;
    
    return currentPage;
  }
  
  /**
  * Deprecated
  **/
  protected DbRet parseTable(String select_clause, String from_clause, String order_by_clause) {
    return parseTable(select_clause, from_clause);
  }
  protected DbRet parseTable(String select_clause, String from_clause) {
    DbRet dbRet = new DbRet();
    
    _select_clause = select_clause;
    _from_clause = from_clause;
    
    String order_by_clause = "";
    if (_sorted_by_col.length()>0) {
      order_by_clause = SwissKnife.sqlEncode(_sorted_by_col + " " + _sorted_by_order);
    }
    
    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(databaseId);
    
    database.setReadOnly(true);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
        
    dbRet = getQueryTotalRowCount(from_clause, database);
    _totalRowCount = dbRet.getRetInt();
    
    if (order_by_clause.length()>0) from_clause += " ORDER BY " + order_by_clause;
    
    if (_end_clause.length()>0) from_clause += _end_clause;
    
    dbRet = fillQueryDataSet(select_clause + from_clause, database);
    
    database.commitTransaction(1, prevTransIsolation);
    database.setReadOnly(false);

    director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  /**
   * Εκτέλεση query και <<γέμισμα>> QueryDataSet.
   *
   */
  protected DbRet fillQueryDataSet(String query, Database database) {
    DbRet dbRet = new DbRet();

    try {
      // κλείσε το query ώστε να το τροποποιήσουμε
      if (queryDataSet.isOpen()) queryDataSet.close();
      
      queryDataSet.setSort(null);
      
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();

      _currentRowCount = queryDataSet.getRowCount();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }

    return dbRet;
  }
  
  protected DbRet getQueryTotalRowCount(String query, Database database) {
    DbRet dbRet = new DbRet();
    
    query = "SELECT COUNT(*) AS totalRowCount " + query;
    
    QueryDataSet queryDataSet = null;
    
    try {
      queryDataSet = new QueryDataSet();
      
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();

      dbRet.setRetInt(Integer.parseInt(queryDataSet.format("totalRowCount")));
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    finally {
      try { queryDataSet.close(); } catch (Exception e) { }
      
      queryDataSet = null;
    }

    return dbRet;
  }
  
  protected abstract DbRet search(HttpServletRequest request);
  
  protected void resetSearch() {
  }
  
  private String _select_clause = "";
  private String _from_clause = "";
  private String _end_clause = "";
  
  private String _sorted_by_col = "";
  private String _sorted_by_order = "";
  
  private int _totalRowCount = 0;
  private int _currentRowCount = 0;
  private int _dispRows = 0;
  
  private int _maxDispRows = 300;
  private int _defDispRows = 10;
  
  private int _start = 0;
  
  /** Το όνομα του table στο οποίο θα γινει authentication */
  private String _tableName = "";
}