package gr.softways.dev.swift.registeredmember;

import java.io.*;
import java.util.*;
import java.sql.Timestamp;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class Search extends SearchBean2 {

  public Search() {
    super("registeredMember");
    
    setSortedByCol("RMRegDate");
    setSortedByOrder("DESC");
  }
  
  // Τα κριτήρια αναζήτησης {
  private String _RMRegDateDay = "";

  public String getRMRegDateDay() {
    return _RMRegDateDay;
  }
  
  private String _RMRegDateMonth = "";

  public String getRMRegDateMonth() {
    return _RMRegDateMonth;
  }
  
  private String _RMRegDateYear = "";

  public String getRMRegDateYear() {
    return _RMRegDateYear;
  }

  private Timestamp _RMRegDateFrom = null;

  public String getRMRegDateFrom() {
    return _RMRegDateFrom.toString();
  }
  
  private String _RMLastName = "";

  public String getRMLastName() {
    return _RMLastName;
  }

  private String _RMLoginTypeKey = "";

  public String getRMLoginTypeKey() {
    return _RMLoginTypeKey;
  }
  
  private String _RMEmail = "";

  public String getRMEmail() {
    return _RMEmail;
  }  
  
  private String _RMIsActive = "";

  public String getRMIsActive() {
    return _RMIsActive;
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
    
    select_clause = "SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " DISTINCT RMCode,RMRegDate,RMEmail,RMLastName,RMIsActive";
    
    from_clause = " FROM registeredMember LEFT JOIN ELRelRM ON"
                + " RMCode = ELRM_RMCode LEFT JOIN emailList ON ELRM_ELCode = ELCode"
                + " WHERE 1 = 1";
    
    if (_RMRegDateFrom != null) {
      from_clause += " AND '" + _RMRegDateFrom.toString() + "' <= RMRegDate";
    }
    
    if (_RMLastName.length() > 0) {
      from_clause += " AND RMLastNameUp LIKE '%" + SwissKnife.searchConvert(SwissKnife.sqlEncode(_RMLastName)) + "%'";
    }
    
    if (_ELCode.length() > 0) {
      from_clause += " AND ELRM_ELCode = '" + SwissKnife.sqlEncode(_ELCode) + "'";
    }
    
    if (_RMEmail.length() > 0) {
      from_clause += " AND RMEmail = '" + SwissKnife.sqlEncode(_RMEmail) + "'";
    }
    
    if (_RMIsActive.length() > 0) {
      from_clause += " AND RMIsActive = '" + SwissKnife.sqlEncode(_RMIsActive) + "'";
    }
    
    if (_RMLoginTypeKey.length() > 0) {
      from_clause += " AND RMLoginTypeKey = '" + SwissKnife.sqlEncode(_RMLoginTypeKey) + "'";
    }

    parseTable(select_clause, from_clause);
    
    return dbRet;
  }
  
  protected DbRet getQueryTotalRowCount(String query, Database database) {
    DbRet dbRet = new DbRet();
    
    query = "SELECT COUNT(DISTINCT RMCode) AS totalRowCount " + query;

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
    if ( (_RMRegDateDay = request.getParameter("src_RMRegDateDay")) == null ) _RMRegDateDay = "";
    if ( (_RMRegDateMonth = request.getParameter("src_RMRegDateMonth")) == null ) _RMRegDateMonth = "";
    if ( (_RMRegDateYear = request.getParameter("src_RMRegDateYear")) == null ) _RMRegDateYear = "";
    _RMRegDateFrom = SwissKnife.buildTimestamp(_RMRegDateDay,_RMRegDateMonth,_RMRegDateYear, "0", "0", "0", "0");
    
    if ( (_RMLastName = request.getParameter("src_RMLastName")) == null ) _RMLastName = "";
    if ( (_RMEmail = request.getParameter("src_RMEmail")) == null ) _RMEmail = "";
    if ( (_RMIsActive = request.getParameter("src_RMIsActive")) == null ) _RMIsActive = "";
    if ( (_ELCode = request.getParameter("src_ELCode")) == null ) _ELCode = "";
    if ( (_RMLoginTypeKey = request.getParameter("src_RMLoginTypeKey")) == null ) _RMLoginTypeKey = "";
  }
  
  protected void resetSearch() {
    setSortedByCol("RMRegDate");
    setSortedByOrder("DESC");
  }  
}