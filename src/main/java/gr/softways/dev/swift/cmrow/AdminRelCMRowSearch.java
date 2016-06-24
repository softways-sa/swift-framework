/*
 * AdminRelCMRowSearch.java
 *
 * Created on 11 Απρίλιος 2006, 3:12 μμ
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */


package gr.softways.dev.swift.cmrow;

/**
 *
 * @author haris
 */
import java.io.*;
import java.util.*;

import java.sql.Timestamp;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;


public class AdminRelCMRowSearch extends SearchBean2 {

  public AdminRelCMRowSearch() {
    super("CMRow");
    
    setSortedByCol("CMRDateCreated");
    setSortedByOrder("DESC");    
  }

  // parameters received {
  private String _CMCM_CMRCode1 = "";

  public String getCMCM_CMRCode1() {
    return _CMCM_CMRCode1;
  }
  
  public void setCMCM_CMRCode1(String CMCM_CMRCode1) {
    _CMCM_CMRCode1 = CMCM_CMRCode1;
  }
  
  private String _CMRTitle = "";

  public String getCMRTitle() {
    return _CMRTitle;
  }
  
  public void setCMRTitle(String CMRTitle) {
    _CMRTitle = CMRTitle;
  }

  // Τα κριτήρια αναζήτησης {
  private String _searchCMCCode = "";

  public String getSearchCMCCode() {
    return _searchCMCCode;
  }

  private String _searchCMRDateCreatedDay = "";

  public String getSearchCMRDateCreatedDay() {
    return _searchCMRDateCreatedDay;
  }
  
  private String _searchCMRDateCreatedMonth = "";

  public String getSearchCMRDateCreatedMonth() {
    return _searchCMRDateCreatedMonth;
  }
  
  private String _searchCMRDateCreatedYear = "";

  public String getSearchCMRDateCreatedYear() {
    return _searchCMRDateCreatedYear;
  }

  private Timestamp _searchCMRDateCreatedFrom = null;

  public String getSearchCMRDateCreatedFrom() {
    return _searchCMRDateCreatedFrom.toString();
  }
  
  private String _searchCMRDateCreatedToDay = "";

  public String getSearchCMRDateCreatedToDay() {
    return _searchCMRDateCreatedToDay;
  }
  
  private String _searchCMRDateCreatedToMonth = "";

  public String getSearchCMRDateCreatedToMonth() {
    return _searchCMRDateCreatedToMonth;
  }
  
  private String _searchCMRDateCreatedToYear = "";

  public String getSearchCMRDateCreatedToYear() {
    return _searchCMRDateCreatedToYear;
  }
  
  private Timestamp _searchCMRDateCreatedTo = null;

  public String getSearchCMRDateCreatedTo() {
    return _searchCMRDateCreatedTo.toString();
  }
  
  
 
  private String _searchCMRDateUpdatedDay = "";

  public String getSearchCMRDateUpdatedDay() {
    return _searchCMRDateUpdatedDay;
  }
  
  private String _searchCMRDateUpdatedMonth = "";

  public String getSearchCMRDateUpdatedMonth() {
    return _searchCMRDateUpdatedMonth;
  }
  
  private String _searchCMRDateUpdatedYear = "";

  public String getSearchCMRDateUpdatedYear() {
    return _searchCMRDateUpdatedYear;
  }
  
  private Timestamp _searchCMRDateUpdatedFrom = null;
  
  public String getSearchCMRDateUpdatedFrom() {
    return _searchCMRDateUpdatedFrom.toString();
  }
  

  private String _searchCMRDateUpdatedToDay = "";

  public String getSearchCMRDateUpdatedToDay() {
    return _searchCMRDateUpdatedToDay;
  }
  
  private String _searchCMRDateUpdatedToMonth = "";

  public String getSearchCMRDateUpdatedToMonth() {
    return _searchCMRDateUpdatedToMonth;
  }
  
  private String _searchCMRDateUpdatedToYear = "";

  public String getSearchCMRDateUpdatedToYear() {
    return _searchCMRDateUpdatedToYear;
  }
  
  private Timestamp _searchCMRDateUpdatedTo = null;

  public String getSearchCMRDateUpdatedTo() {
    return _searchCMRDateUpdatedTo.toString();
  }
  
  private String _searchCMRTitle = "";

  public String getSearchCMRTitle() {
    return _searchCMRTitle;
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
    
    select_clause = "SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " CMRow.CMRCode,CMRow.CMRDateCreated,CMRow.CMRDateUpdated,CMRow.CMRTitle,CMCRelCMR.* ";
    
    from_clause = " FROM CMRow, CMCRelCMR"
                + " WHERE CMRCode = CCCR_CMRCode AND CMRCode <> '" + _CMCM_CMRCode1 + "'"
                + " AND CMRCode NOT IN (SELECT CMCM_CMRCode2 FROM CMRRelCMR"
                + " WHERE CMCM_CMRCode1='" + _CMCM_CMRCode1 + "')";
    
    if (_searchCMCCode.length() > 0) {
      from_clause += " AND CCCR_CMCCode LIKE '" + SwissKnife.sqlEncode(_searchCMCCode) + "%'";
    }
    if (_searchCMRDateCreatedFrom != null) {
      from_clause += " AND '" + _searchCMRDateCreatedFrom.toString() + "' <= CMRDateCreated";
    }
    if (_searchCMRDateCreatedTo != null) {
      from_clause += " AND CMRDateCreated <= '" + _searchCMRDateCreatedTo.toString() + "'";
    }    
    if (_searchCMRDateUpdatedFrom != null) {
      from_clause += " AND '" + _searchCMRDateUpdatedFrom.toString() + "' <= CMRDateUpdated";
    }
    if (_searchCMRDateUpdatedTo != null) {
      from_clause += " AND '" + _searchCMRDateUpdatedTo.toString() + "' <= CMRDateUpdated";
    }    
    
    
    if (_searchCMRTitle.length() > 0) {
      from_clause += " AND (CMRTitle1Up LIKE '%" + SwissKnife.searchConvert(SwissKnife.sqlEncode(_searchCMRTitle)) + "%'" +
                     " OR CMRTitle2Up LIKE '%" + SwissKnife.searchConvert(SwissKnife.sqlEncode(_searchCMRTitle)) + "%')";
    }
    
    parseTable(select_clause, from_clause);

    return dbRet;
  }  


  protected void storeState(HttpServletRequest request) {

    // αποθήκευσε τις τιμές των κριτηρίων αναζήτησης
    if ( (_searchCMRDateCreatedDay = request.getParameter("searchCMRDateCreatedDay")) == null ) _searchCMRDateCreatedDay = "";
    if ( (_searchCMRDateCreatedMonth = request.getParameter("searchCMRDateCreatedMonth")) == null ) _searchCMRDateCreatedMonth = "";
    if ( (_searchCMRDateCreatedYear = request.getParameter("searchCMRDateCreatedYear")) == null ) _searchCMRDateCreatedYear = "";
    _searchCMRDateCreatedFrom = SwissKnife.buildTimestamp(_searchCMRDateCreatedDay,_searchCMRDateCreatedMonth,_searchCMRDateCreatedYear, "0", "0", "0", "0");
    
    
    if ( (_searchCMRDateCreatedToDay = request.getParameter("searchCMRDateCreatedToDay")) == null ) _searchCMRDateCreatedToDay = "";
    if ( (_searchCMRDateCreatedToMonth = request.getParameter("searchCMRDateCreatedToMonth")) == null ) _searchCMRDateCreatedToMonth = "";
    if ( (_searchCMRDateCreatedToYear = request.getParameter("searchCMRDateCreatedToYear")) == null ) _searchCMRDateCreatedToYear = "";
    _searchCMRDateCreatedTo = SwissKnife.buildTimestamp(_searchCMRDateCreatedToDay,_searchCMRDateCreatedToMonth,_searchCMRDateCreatedToYear, "23", "59", "59", "999");    
   
    
    if ( (_searchCMRDateUpdatedDay = request.getParameter("searchCMRDateUpdatedDay")) == null ) _searchCMRDateUpdatedDay = "";
    if ( (_searchCMRDateUpdatedMonth = request.getParameter("searchCMRDateUpdatedMonth")) == null ) _searchCMRDateUpdatedMonth = "";
    if ( (_searchCMRDateUpdatedYear = request.getParameter("searchCMRDateUpdatedYear")) == null ) _searchCMRDateUpdatedYear = "";
    _searchCMRDateUpdatedFrom = SwissKnife.buildTimestamp(_searchCMRDateUpdatedDay,_searchCMRDateUpdatedMonth,_searchCMRDateUpdatedYear, "0", "0", "0", "0");    
    
    
    if ( (_searchCMRDateUpdatedToDay = request.getParameter("searchCMRDateUpdatedToDay")) == null ) _searchCMRDateUpdatedToDay = "";
    if ( (_searchCMRDateUpdatedToMonth = request.getParameter("searchCMRDateUpdatedToMonth")) == null ) _searchCMRDateUpdatedToMonth = "";
    if ( (_searchCMRDateUpdatedToYear = request.getParameter("searchCMRDateUpdatedToYear")) == null ) _searchCMRDateUpdatedToYear = "";    
    _searchCMRDateUpdatedTo = SwissKnife.buildTimestamp(_searchCMRDateUpdatedToDay,_searchCMRDateUpdatedToMonth,_searchCMRDateUpdatedToYear, "23", "59", "59", "999");        
    
    if ( (_searchCMCCode = request.getParameter("searchCMCCode")) == null ) _searchCMCCode = "";    
    if ( (_searchCMRTitle = request.getParameter("searchCMRTitle")) == null ) _searchCMRTitle = "";
  }
  
  protected void resetSearch() {
    setSortedByCol("CMRDateCreated");
    setSortedByOrder("DESC");    
  }
}