package gr.softways.dev.swift.cmrow;

import java.io.*;
import java.util.*;
import java.sql.Timestamp;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class SearchArticle extends SearchBean2 {

  public SearchArticle() {
    super("CMRow");

    setSortedByCol("CMRDateCreated");
    setSortedByOrder("DESC");    
  }
  
  // Τα κριτήρια αναζήτησης {
  private String _CMRDateCreatedDay = "";

  public String getCMRDateCreatedDay() {
    return _CMRDateCreatedDay;
  }
  
  private String _CMRDateCreatedMonth = "";

  public String getCMRDateCreatedMonth() {
    return _CMRDateCreatedMonth;
  }
  
  private String _CMRDateCreatedYear = "";

  public String getCMRDateCreatedYear() {
    return _CMRDateCreatedYear;
  }

  private Timestamp _CMRDateCreatedFrom = null;

  public String getCMRDateCreatedFrom() {
    return _CMRDateCreatedFrom.toString();
  }
  
  
  
  private String _CMRDateCreatedToDay = "";

  public String getCMRDateCreatedToDay() {
    return _CMRDateCreatedToDay;
  }
  
  private String _CMRDateCreatedToMonth = "";

  public String getCMRDateCreatedToMonth() {
    return _CMRDateCreatedToMonth;
  }
  
  private String _CMRDateCreatedToYear = "";

  public String getCMRDateCreatedToYear() {
    return _CMRDateCreatedToYear;
  }
  
  private Timestamp _CMRDateCreatedTo = null;

  public String getCMRDateCreatedTo() {
    return _CMRDateCreatedTo.toString();
  }
  
  
 
  private String _CMRDateUpdatedDay = "";

  public String getCMRDateUpdatedDay() {
    return _CMRDateUpdatedDay;
  }
  
  private String _CMRDateUpdatedMonth = "";

  public String getCMRDateUpdatedMonth() {
    return _CMRDateUpdatedMonth;
  }
  
  private String _CMRDateUpdatedYear = "";

  public String getCMRDateUpdatedYear() {
    return _CMRDateUpdatedYear;
  }
  
  private Timestamp _CMRDateUpdatedFrom = null;
  
  public String getCMRDateUpdatedFrom() {
    return _CMRDateUpdatedFrom.toString();
  }
  

  
  
  
  private String _CMRDateUpdatedToDay = "";

  public String getCMRDateUpdatedToDay() {
    return _CMRDateUpdatedToDay;
  }
  
  private String _CMRDateUpdatedToMonth = "";

  public String getCMRDateUpdatedToMonth() {
    return _CMRDateUpdatedToMonth;
  }
  
  private String _CMRDateUpdatedToYear = "";

  public String getCMRDateUpdatedToYear() {
    return _CMRDateUpdatedToYear;
  }
  
  private Timestamp _CMRDateUpdatedTo = null;

  public String getCMRDateUpdatedTo() {
    return _CMRDateUpdatedTo.toString();
  }  
  
  
  
  
  private String _CMRTitle = "";

  public String getCMRTitle() {
    return _CMRTitle;
  }
  
  private String _searchLang = "";  
  
  public String getSearchLang() {
    return _searchLang;
  }
  
  private String _CMCCode = "";

  public String getCMCCode() {
    return _CMCCode;
  }  

  private String _searchPattern = "";

  public String getSearchPattern() {
    return _searchPattern;
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
    
    storeState(request);
    
    select_clause = "SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " DISTINCT CMRow.* ";
    
    from_clause = " FROM CMRow, CMCRelCMR"
                + " WHERE CMRCode = CCCR_CMRCode  AND CMCRelCMR.CCCRIsHidden <> '1'";
    
    if (_CMCCode.length() > 0) {
      from_clause += " AND CCCR_CMCCode LIKE '" + SwissKnife.sqlEncode(_CMCCode) + "%'";
    }
    if (_CMRDateCreatedFrom != null) {
      from_clause += " AND '" + _CMRDateCreatedFrom.toString() + "' <= CMRDateCreated";
    }
    if (_CMRDateCreatedTo != null) {
      from_clause += " AND CMRDateCreated <= '" + _CMRDateCreatedTo.toString() + "'";
    }    
    if (_CMRDateUpdatedFrom != null) {
      from_clause += " AND '" + _CMRDateUpdatedFrom.toString() + "' <= CMRDateUpdated";
    }
    if (_CMRDateUpdatedTo != null) {
      from_clause += " AND '" + _CMRDateUpdatedTo.toString() + "' <= CMRDateUpdated";
    }    
    
    
    if (_CMRTitle.length() > 0) {
      from_clause += " AND (CMRTitle1Up LIKE '%" + SwissKnife.searchConvert(SwissKnife.sqlEncode(_CMRTitle)) + "%'" +
                     " OR CMRTitle2Up LIKE '%" + SwissKnife.searchConvert(SwissKnife.sqlEncode(_CMRTitle)) + "%')";
    }
    
    if (_searchPattern.length() > 0) {
      from_clause += " AND (CMRTitle1Up" + _searchLang + " LIKE '%" + SwissKnife.searchConvert(SwissKnife.sqlEncode(_searchPattern)) + "%'" +
                     " OR CMRTitle2Up" + _searchLang + " LIKE '%" + SwissKnife.searchConvert(SwissKnife.sqlEncode(_searchPattern)) + "%'" +
                     " OR CMRKeyWords1Up" + _searchLang + " LIKE '%" + SwissKnife.searchConvert(SwissKnife.sqlEncode(_searchPattern)) + "%'" +
                     " OR CMRKeyWords2Up" + _searchLang + " LIKE '%" + SwissKnife.searchConvert(SwissKnife.sqlEncode(_searchPattern)) + "%')" + 
                     " AND CCCR_CMCCode <> '0402'";
    }    

    parseTable(select_clause, from_clause);
    
    return dbRet;
  }

  protected DbRet getQueryTotalRowCount(String query, Database database) {
    DbRet dbRet = new DbRet();
    
    query = "SELECT COUNT(DISTINCT CMRCode) AS totalRowCount " + query;
//    System.out.println("countQuery=" + query);
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
  
  
  protected void storeState(HttpServletRequest request) {

    // αποθήκευσε τις τιμές των κριτηρίων αναζήτησης
    if ( (_CMRDateCreatedDay = request.getParameter("CMRDateCreatedDay")) == null ) _CMRDateCreatedDay = "";
    if ( (_CMRDateCreatedMonth = request.getParameter("CMRDateCreatedMonth")) == null ) _CMRDateCreatedMonth = "";
    if ( (_CMRDateCreatedYear = request.getParameter("CMRDateCreatedYear")) == null ) _CMRDateCreatedYear = "";
    _CMRDateCreatedFrom = SwissKnife.buildTimestamp(_CMRDateCreatedDay,_CMRDateCreatedMonth,_CMRDateCreatedYear, "0", "0", "0", "0");
    
    
    if ( (_CMRDateCreatedToDay = request.getParameter("CMRDateCreatedToDay")) == null ) _CMRDateCreatedToDay = "";
    if ( (_CMRDateCreatedToMonth = request.getParameter("CMRDateCreatedToMonth")) == null ) _CMRDateCreatedToMonth = "";
    if ( (_CMRDateCreatedToYear = request.getParameter("CMRDateCreatedToYear")) == null ) _CMRDateCreatedToYear = "";
    _CMRDateCreatedTo = SwissKnife.buildTimestamp(_CMRDateCreatedToDay,_CMRDateCreatedToMonth,_CMRDateCreatedToYear, "23", "59", "59", "999");    
    
   
    
    if ( (_CMRDateUpdatedDay = request.getParameter("CMRDateUpdatedDay")) == null ) _CMRDateUpdatedDay = "";
    if ( (_CMRDateUpdatedMonth = request.getParameter("CMRDateUpdatedMonth")) == null ) _CMRDateUpdatedMonth = "";
    if ( (_CMRDateUpdatedYear = request.getParameter("CMRDateUpdatedYear")) == null ) _CMRDateUpdatedYear = "";
    _CMRDateUpdatedFrom = SwissKnife.buildTimestamp(_CMRDateUpdatedDay,_CMRDateUpdatedMonth,_CMRDateUpdatedYear, "0", "0", "0", "0");    
    
    
    if ( (_CMRDateUpdatedToDay = request.getParameter("CMRDateUpdatedToDay")) == null ) _CMRDateUpdatedToDay = "";
    if ( (_CMRDateUpdatedToMonth = request.getParameter("CMRDateUpdatedToMonth")) == null ) _CMRDateUpdatedToMonth = "";
    if ( (_CMRDateUpdatedToYear = request.getParameter("CMRDateUpdatedToYear")) == null ) _CMRDateUpdatedToYear = "";    
    _CMRDateUpdatedTo = SwissKnife.buildTimestamp(_CMRDateUpdatedToDay,_CMRDateUpdatedToMonth,_CMRDateUpdatedToYear, "23", "59", "59", "999");        
    
    
    if ( (_CMRTitle = request.getParameter("CMRTitle")) == null ) _CMRTitle = "";
    if ( (_CMCCode = request.getParameter("CMCCode")) == null ) _CMCCode = "";
    if ( (_searchLang = request.getParameter("searchLang")) == null ) _searchLang = "";
    
    if ( (_searchPattern = request.getParameter("searchPattern")) == null ) _searchPattern = "";    
  }
  
  protected void resetSearch() {
    setSortedByCol("CMRDateCreated");
    setSortedByOrder("DESC");    
  }  
}