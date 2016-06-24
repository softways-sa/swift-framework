package gr.softways.dev.swift.cmrow;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class SearchArticle2 extends JSPBean {

  // Τα κριτήρια αναζήτησης {
  private String _CMRKeyWords = "";

  public String getCMRKeyWords() {
    return _CMRKeyWords;
  }
  
  private String _lang = "";
  
  public String geLang() {
    return _lang;
  }
  
  private String _CMCCode = "";

  public String getCMCCode() {
    return _CMCCode;
  }
  
  private String _CMRCode = "";

  public String getCMRCode() {
    return _CMRCode;
  }
  // } Τα κριτήρια αναζήτησης
  
  public SearchArticle2() {
    _tableName = "CMRow";

    setSortedByCol("CMRDateCreated");
    setSortedByOrder("DESC");
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
    
    try {
        _dispRows = Integer.parseInt(request.getParameter("dispRows"));
    }
    catch (Exception e) {
    }
    if (_dispRows <= 0 || _dispRows > getMaxDispRows()) _dispRows = getDefDispRows();
    
    if (request.getParameter("sorted_by_col") != null) _sorted_by_col = request.getParameter("sorted_by_col");
    if (request.getParameter("sorted_by_order") != null) _sorted_by_order = request.getParameter("sorted_by_order");
    
    dbRet = search(request);
    
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
    
    query = "SELECT COUNT(CMRCode) AS totalRowCount " + query;
    
    //System.out.println(query);
    
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
  
  protected synchronized DbRet search(HttpServletRequest request) {
    DbRet dbRet = new DbRet();
    
    String s = null, select_clause = null, from_clause = null;
    
    
    if ( (s = request.getParameter("start")) == null ) s = "0";
    try {
      setStart(Integer.parseInt(s));
    }
    catch (Exception e) {
      setStart(0);
    }
    
    // hack to work for protected content and take care so that same methods work for admin also
    boolean hasProtectedSection = false, isAdministrator = false, isAuthenticatedUser = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedSection") != null && SwissKnife.jndiLookup("swconf/hasProtectedSection").equals("true")) hasProtectedSection = true;
    if (hasProtectedSection == true) {
      if (getSession().getAttribute(databaseId + ".authGrantLogin") != null) isAdministrator = true;
      if (getSession().getAttribute(databaseId + ".isAuthenticatedUser") != null && getSession().getAttribute(databaseId + ".isAuthenticatedUser").toString().equals("true")) isAuthenticatedUser = true;
    }
    
    select_clause = "SELECT * FROM CMRow JOIN CMCRelCMR ON CMRow.CMRCode = CMCRelCMR.CCCR_CMRCode"
        + " JOIN " 
        + " (SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " CMRCode AS bCMRCode";
    
    from_clause = " FROM CMRow JOIN CMCRelCMR ON CMRow.CMRCode = CMCRelCMR.CCCR_CMRCode"
        + " WHERE CMCRelCMR.CCCRIsHidden <> '1'";
    
    if (hasProtectedSection == true && isAdministrator == false && isAuthenticatedUser == false) {
      from_clause += " AND CMRow.CMRIsProtected <> '1'";
    }
    
    if ( (_lang = request.getParameter("extLang")) == null ) _lang = "";
    
    if ( (_CMCCode = request.getParameter("CMCCode")) == null ) _CMCCode = "";
    if ( (_CMRCode = request.getParameter("CMRCode")) == null ) _CMRCode = "";
    if ( (_CMRKeyWords = request.getParameter("q")) == null ) _CMRKeyWords = "";
    
    if (_CMCCode.equals("") && _CMRCode.equals("")) {
      if (request.getAttribute("CMCCode") != null) _CMCCode = (String) request.getAttribute("CMCCode");
    }
    
    if (_CMCCode.length() > 0) {
      from_clause += " AND CCCR_CMCCode = '" + SwissKnife.sqlEncode(_CMCCode) + "'";
    }
    else from_clause += " AND CCCRPrimary = '1'";
    
    if (_CMRCode.length() > 0) {
      from_clause += " AND CMRow.CMRCode = '" + SwissKnife.sqlEncode(_CMRCode) + "'";
    }
    
    if (_CMRKeyWords.length() > 0) {
      StringTokenizer words = new StringTokenizer(_CMRKeyWords, " ");
      
      while (words.hasMoreTokens()) {
        String word = SwissKnife.searchConvert(SwissKnife.sqlEncode(words.nextToken()));
        
        from_clause += " AND (CMRTitle1Up" + _lang + " LIKE '%" + SwissKnife.searchConvert(SwissKnife.sqlEncode(word)) + "%'" +
                       " OR CMRTitle2Up" + _lang + " LIKE '%" + SwissKnife.searchConvert(SwissKnife.sqlEncode(word)) + "%'" +
                       " OR CMRKeyWords1Up" + _lang + " LIKE '%" + SwissKnife.searchConvert(SwissKnife.sqlEncode(word)) + "%'" +
                       " OR CMRKeyWords2Up" + _lang + " LIKE '%" + SwissKnife.searchConvert(SwissKnife.sqlEncode(word)) + "%')";
      }
    }
    
    setEndClause(") AS b ON CMRow.CMRCode = b.bCMRCode");
    if (_CMCCode.length() > 0) {
      setEndClause(getEndClause() + " AND CCCR_CMCCode = '" + SwissKnife.sqlEncode(_CMCCode) + "'");
    }
    else {
      setEndClause(getEndClause() + " AND CCCRPrimary = '1'");
    }
    if (getSortedByCol() != null && getSortedByCol().length()>0) setEndClause(getEndClause() + " ORDER BY " + getSortedByCol() + " " + getSortedByOrder());
    
    //System.out.println(select_clause + " " + from_clause + " ORDER BY " + getSortedByCol() + " " + getSortedByOrder() + " " + getEndClause());

    parseTable(select_clause, from_clause);
    
    return dbRet;
  }
  
  protected void resetSearch() {
    setSortedByCol("CMRDateCreated");
    setSortedByOrder("DESC");
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