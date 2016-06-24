package gr.softways.dev.swift.vote;

import java.io.*;
import java.util.*;
import java.sql.Timestamp;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class Search extends SearchBean2 {

  public Search() {
    super("voteTab");
    
    setSortedByCol("VTFrom");
    setSortedByOrder("DESC");
  }
  
  // Τα κριτήρια αναζήτησης {
  private String _VTFromDay = "";

  public String getVTFromDay() {
    return _VTFromDay;
  }
  
  private String _VTFromMonth = "";

  public String getVTFromMonth() {
    return _VTFromMonth;
  }
  
  private String _VTFromYear = "";

  public String getVTFromYear() {
    return _VTFromYear;
  }
  
  private String _VTToDay = "";

  public String getVTToDay() {
    return _VTToDay;
  }
  
  private String _VTToMonth = "";

  public String getVTToMonth() {
    return _VTToMonth;
  }
  
  private String _VTToYear = "";

  public String getVTToYear() {
    return _VTToYear;
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
    
    Timestamp VTFrom = null, VTTo = null;
    
    if ( (_VTFromDay = request.getParameter("VTFromDay")) == null ) _VTFromDay = "";
    if ( (_VTFromMonth = request.getParameter("VTFromMonth")) == null ) _VTFromMonth = "";
    if ( (_VTFromYear = request.getParameter("VTFromYear")) == null ) _VTFromYear = "";
    VTFrom = SwissKnife.buildTimestamp(_VTFromDay,_VTFromMonth,_VTFromYear, "0", "0", "0", "0");
    
    if ( (_VTToDay = request.getParameter("VTToDay")) == null ) _VTToDay = "";
    if ( (_VTToMonth = request.getParameter("VTToMonth")) == null ) _VTToMonth = "";
    if ( (_VTToYear = request.getParameter("VTToYear")) == null ) _VTToYear = "";
    VTTo = SwissKnife.buildTimestamp(_VTToDay,_VTToMonth,_VTToYear, "0", "0", "0", "0");
    
    select_clause = "SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " *";
    
    from_clause = " FROM voteTab WHERE 1 = 1";
    
    if (VTFrom != null) {
      from_clause += " AND VTFrom >= '" + VTFrom + "'";
    }
    
    if (VTTo != null) {
      from_clause += " AND VTTo < '" + VTTo + "'";
    }
    
    parseTable(select_clause, from_clause);
    
    return dbRet;
  }
  
  protected void resetSearch() {
    setSortedByCol("VTFrom");
    setSortedByOrder("DESC");
  }
}
