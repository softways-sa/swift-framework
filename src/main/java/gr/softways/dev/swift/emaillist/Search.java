package gr.softways.dev.swift.emaillist;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;

public class Search extends SearchBean2 {

  public Search() {
    super("emailList");
    
    setSortedByCol("ELNameUp");
    setSortedByOrder("ASC");
  }

  // Τα κριτήρια αναζήτησης {
  private String _ELCode = "";

  public String getELCode() {
    return _ELCode;
  }

  private String _ELName = "";

  public String getELName() {
    return _ELName;
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
    
    select_clause = "SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " * ";
    
    from_clause = " FROM emaillist WHERE 1=1 ";
    
    if (_ELName.length() > 0) {
      from_clause += " AND ELNameUp LIKE '%" + SwissKnife.searchConvert(SwissKnife.sqlEncode(_ELName)) + "%'";
    }
    
    parseTable(select_clause, from_clause);
    
    return dbRet;
  }

  protected void storeState(HttpServletRequest request) {
    // αποθήκευσε τις τιμές των κριτηρίων αναζήτησης
    if ( (_ELCode = request.getParameter("ELCode")) == null ) _ELCode = "";
    if ( (_ELName = request.getParameter("ELName")) == null ) _ELName = "";
  }
  
  protected void resetSearch() {
    setSortedByCol("ELNameUp");
    setSortedByOrder("ASC");
  }
}