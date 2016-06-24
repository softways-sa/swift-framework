package gr.softways.dev.swift.cmcategory;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;

public class Search extends SearchBean2 {

  public Search() {
    super("CMCategory");
    setSortedByCol("CMCCode");
    setSortedByOrder("ASC");
  }

  // Τα κριτήρια αναζήτησης {
  private String _CMCCode = "";

  public String getCMCCode() {
    return _CMCCode;
  }

  private String _CMCName = "";

  public String getCMCName() {
    return _CMCName;
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
    
    select_clause = "SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " * ";
    
    from_clause = " FROM CMCategory"
                + " WHERE 1=1 ";
    
   
    if (_CMCCode.length() > 0) {
      from_clause += " AND CMCCode = '" + SwissKnife.sqlEncode(_CMCCode) + "'";
    }
    if (_CMCName.length() > 0) {
      from_clause += " AND CMCNameUp LIKE '%" + SwissKnife.searchConvert(SwissKnife.sqlEncode(_CMCName)) + "%'";
    }
    
    parseTable(select_clause, from_clause);
    
    return dbRet;
  }  

  protected void storeState(HttpServletRequest request) {

    // αποθήκευσε τις τιμές των κριτηρίων αναζήτησης
    if ( (_CMCCode = request.getParameter("CMCCode")) == null ) _CMCCode = "";
    if ( (_CMCName = request.getParameter("CMCName")) == null ) _CMCName = "";
  }
  
  protected void resetSearch() {
    setSortedByCol("CMCCode");
    setSortedByOrder("ASC");
  }
}