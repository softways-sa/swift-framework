package gr.softways.dev.swift.pendingmember;

import java.io.*;
import java.util.*;
import java.sql.Timestamp;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class Search extends SearchBean2 {

  public Search() {
    super("pendingMember");
    
    setSortedByCol("PMRegDate");
    setSortedByOrder("DESC");
  }
  
  // Τα κριτήρια αναζήτησης {
  private String _PMRegDateDay = "";

  public String getPMRegDateDay() {
    return _PMRegDateDay;
  }
  
  private String _PMRegDateMonth = "";

  public String getPMRegDateMonth() {
    return _PMRegDateMonth;
  }
  
  private String _PMRegDateYear = "";

  public String getPMRegDateYear() {
    return _PMRegDateYear;
  }

  private Timestamp _PMRegDateFrom = null;

  public String getPMRegDateFrom() {
    return _PMRegDateFrom.toString();
  }
  
  private String _PMLastName = "";

  public String getPMLastName() {
    return _PMLastName;
  }

  private String _PMLoginTypeKey = "";

  public String getPMLoginTypeKey() {
    return _PMLoginTypeKey;
  }
  
  private String _PMEmail = "";

  public String getPMEmail() {
    return _PMEmail;
  }  
  
  private String _PMIsActive = "";

  public String getPMIsActive() {
    return _PMIsActive;
  }

  private String _ELCode = "";

  public String getELCode() {
    return _ELCode;
  }
  // } Τα κριτήρια αναζήτησης

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

    select_clause = "SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " DISTINCT PMCode,PMRegDate,PMEmail,PMLastName,PMIsActive";
    
    from_clause = " FROM pendingMember LEFT JOIN ELRelPM ON"
                + " pendingMember.PMCode = ELRelPM.ELPM_PMCode LEFT JOIN emailList ON ELPM_ELCode=ELCode"
                + " WHERE 1 = 1";
    
    if (_PMRegDateFrom != null) {
      from_clause += " AND '" + _PMRegDateFrom.toString() + "' <= PMRegDate";
    }

    if (_PMLoginTypeKey.length() > 0) {
      from_clause += " AND PMLoginTypeKey = '" + SwissKnife.sqlEncode(_PMLoginTypeKey) + "'";
    }
    
    if (_PMLastName.length() > 0) {
      from_clause += " AND PMLastNameUp LIKE '%" + SwissKnife.searchConvert(SwissKnife.sqlEncode(_PMLastName)) + "%'";
    }
    
    if (_ELCode.length() > 0) {
      from_clause += " AND ELPM_ELCode = '" + SwissKnife.sqlEncode(_ELCode) + "'";
    }
    
    if (_PMEmail.length() > 0) {
      from_clause += " AND PMEmail = '" + SwissKnife.sqlEncode(_PMEmail) + "'";
    }
    
    if (_PMIsActive.length() > 0) {
      from_clause += " AND PMIsActive = '" + SwissKnife.sqlEncode(_PMIsActive) + "'";
    }
    
    parseTable(select_clause, from_clause);
    
    return dbRet;
  }

  protected DbRet getQueryTotalRowCount(String query, Database database) {
    DbRet dbRet = new DbRet();
    
    query = "SELECT COUNT(DISTINCT PMCode) AS totalRowCount " + query;
    
    // System.out.println("countQuery=" + query);
    
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
    if ( (_PMRegDateDay = request.getParameter("src_PMRegDateDay")) == null ) _PMRegDateDay = "";
    if ( (_PMRegDateMonth = request.getParameter("src_PMRegDateMonth")) == null ) _PMRegDateMonth = "";
    if ( (_PMRegDateYear = request.getParameter("src_PMRegDateYear")) == null ) _PMRegDateYear = "";
    _PMRegDateFrom = SwissKnife.buildTimestamp(_PMRegDateDay,_PMRegDateMonth,_PMRegDateYear, "0", "0", "0", "0");
    
    if ( (_PMLastName = request.getParameter("src_PMLastName")) == null ) _PMLastName = "";
    if ( (_PMEmail = request.getParameter("src_PMEmail")) == null ) _PMEmail = "";
    if ( (_PMIsActive = request.getParameter("src_PMIsActive")) == null ) _PMIsActive = "";
    if ( (_PMLoginTypeKey = request.getParameter("src_PMLoginTypeKey")) == null ) _PMLoginTypeKey = "";
    if ( (_ELCode = request.getParameter("src_ELCode")) == null ) _ELCode = "";
  }
  
  protected void resetSearch() {
    setSortedByCol("PMRegDate");
    setSortedByOrder("DESC");
  }
}