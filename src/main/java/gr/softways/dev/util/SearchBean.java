package gr.softways.dev.util;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public abstract class SearchBean extends JSPBean {

  public SearchBean(String tableName) {
    this.tableName = tableName;
  }

  /** Το όνομα του table στο οποίο θα γινει authentication */
  protected String tableName = "";

  /** Το query που εκτελέσθηκε την τελευταία φορά */
  protected String _query = "";

  public String getQuery() {
    return _query;
  }
  
  protected String[] _sortBy = new String[3];
  
  public String getSortBy(int ordinal) {
    return _sortBy[ordinal];
  }
  
  protected String _sortOrder = null;
  
  public String getSortOrder() {
    return _sortOrder;
  }
  
  /**
   * Max rows that a queryDataSet keeps.
   * If 0 then unlimited.
   */
  private int _maxRows = 0;
  
  public int getMaxRows() {
    return _maxRows;
  }
  
  public void setMaxRows(int maxRows) {
    _maxRows = maxRows;
  }
  
  // Μεταβλητές για την σελίδα που έβλεπε την τελευταία φορά
  // ο χρήστης. {
  /** το πλήθος των rows */

  private int _rowCount = 0;

  public int getRowCount() {
    return _rowCount;
  }

  /** το πρώτο row της κάθε σελίδας */
  private int _startRow = 0;

  public int getStartRow() {
    return _startRow;
  }

  /** ο αριθμός της τρέχον σελίδας */
  private int _pageNum = 0;

  public int getPageNum() {
    return _pageNum;
  }

  /** πόσα rows σε κάθε σελίδα */
  private int _dispRows = 10;

  public void setDispRows(int dispRows) {
    _dispRows = dispRows;
  }

  public int getDispRows() {
    return _dispRows;
  }

  /** group page */
  private int _groupPages = 0;

  public int getGroupPages() {
    return _groupPages;
  }
  // } Μεταβλητές για την σελίδα που έβλεπε την τελευταία φορά
  // ο χρήστης.


  public DbRet doAction(HttpServletRequest request) {
    DbRet dbRet = new DbRet();
    dbRet.setNoError(1);

    int auth = Director.getInstance().auth(databaseId,authUsername,
                                           authPassword,tableName,AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);

      return dbRet;
    }

    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1");
    
    if (request.getParameter("sr") != null && !request.getParameter("sr").equals("")) {
      _startRow = Integer.parseInt(request.getParameter("sr"));
    }
    if (request.getParameter("p") != null && !request.getParameter("p").equals("")) {
      _pageNum = Integer.parseInt(request.getParameter("p"));
    }
    if (request.getParameter("gp") != null && !request.getParameter("gp").equals("")) {
      _groupPages = Integer.parseInt(request.getParameter("gp"));
    }
    if (request.getParameter("dr") != null && !request.getParameter("dr").equals("")) {
      _dispRows = Integer.parseInt(request.getParameter("dr"));
    }
    
    if ("SEARCH".equals(action)) {
      dbRet = parseTable(request);

      if (dbRet.getNoError() == 1 && _sortBy[0] != null) {
        dbRet = sortTable(_sortBy, _sortOrder);
      }
      
      _startRow = 0;
      _pageNum = 1;
      _groupPages = 0;
    }
    else if ("UPDATE_SEARCH".equals(action) && _query.length() > 0) {
      dbRet = updateSearch(request, _query);
      
      if (dbRet.getNoError() == 1 && _sortBy[0] != null) {
        dbRet = sortTable(_sortBy, _sortOrder);
      }
      
      //_startRow = 0;
      //_pageNum = 1;
      //_groupPages = 0;
    }
    else if ("CLOSE_SEARCH".equals(action)) {
      resetSearchCriteria();
      
      _rowCount = 0;
      _startRow = 0;
      _pageNum = 1;
      _groupPages = 0;
      
      if (queryDataSet != null && queryDataSet.isOpen()) {
        try { queryDataSet.close(); } catch (Exception e) { }
      }
    }
    else if ("SORT_SEARCH_RESULTS".equals(action) && _query.length() > 0) {
      _sortBy[0] = request.getParameter("SEARCH_SORT_BY_1");
      _sortBy[1] = request.getParameter("SEARCH_SORT_BY_2");
      _sortBy[2] = request.getParameter("SEARCH_SORT_BY_3");
      
      _sortOrder = request.getParameter("SEARCH_SORT_ORDER");
      
      if (_sortOrder == null || _sortOrder.length() == 0) {
        _sortOrder = "ASC";
      }
      
      if (_sortBy[0] != null) {
        dbRet = sortTable(_sortBy, _sortOrder);
      }
      else dbRet.setNoError(0);
      
      if (dbRet.getNoError() == 1) {
        _startRow = 0;
        _pageNum = 1;
        _groupPages = 0;
      }
    }

    // μετακίνηση στο row
    if (queryDataSet.isOpen()) {
      goToRow(_startRow);
    }

    return dbRet;
  }

  /**
   * Χτίσιμο του query αναζήτησης.
   */
  protected abstract DbRet parseTable(HttpServletRequest request);

  protected abstract void storeState(HttpServletRequest request, String query);

  protected synchronized DbRet updateSearch(HttpServletRequest request,
                                            String query) {
    return fillQueryDataSet(query);
  }

  protected void resetSearchCriteria() {
    _query = "";
  }
  
  /**
   * Εκτέλεση query και <<γέμισμα>> QueryDataSet.
   *  
   */
  protected DbRet fillQueryDataSet(String query) {
    Director director = Director.getInstance();
    
    DbRet dbRet = null;
    
    database = director.getDBConnection(databaseId);

    database.setReadOnly(true);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();

    try {
      // κλείσε το query ώστε να το τροποποιήσουμε
      if (queryDataSet.isOpen()) queryDataSet.close();
      
      queryDataSet.setSort(null);
      
      queryDataSet.setQuery(new QueryDescriptor(database, query, null, 
                                                true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      if (getMaxRows() > 0) {
        queryDataSet.setMaxRows( getMaxRows() );
      }
      
      queryDataSet.refresh();

      _rowCount = queryDataSet.getRowCount();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }

    database.commitTransaction(1,prevTransIsolation);
    database.setReadOnly(false);

    director.freeDBConnection(databaseId,database);

    return dbRet;
  }
  
   /**
    *  Sorting στο ήδη υπάρχον queryDataSet.
   */
  protected DbRet sortTable(String[] sortBy, String sortOrder) {
    DbRet dbRet = new DbRet();
    
    try {
      queryDataSet.setSort(sortBy, sortOrder);
    }
    catch (Exception dse) {
      dbRet.setNoError(0);
      dse.printStackTrace();
    }
    
    return dbRet;
  }
  
  public boolean isSortedBy(String sortBy, String sortOrder) {
    int sortByLength = _sortBy.length;
    
    boolean isSortedBy = false;
    
    for (int i=0; i<sortByLength; i++) {
      if ( (_sortBy[i] != null && _sortBy[i].equals(sortBy))
             && (_sortOrder != null && _sortOrder.equals(sortOrder)) ) {
        isSortedBy = true;
        break;
      }
    }
    
    return isSortedBy;
  }
}