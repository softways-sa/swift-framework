package gr.softways.dev.eshop.emaillists.newsletter;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class Search extends SearchBean2 {

  public Search() {
    super("Newsletter");
    
    setSortedByCol("NWLR_Date");
    setSortedByOrder("DESC");
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
    
    select_clause = "SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " *";
    
    from_clause = " FROM Newsletter WHERE 1 = 1";
    
    //System.out.println(from_clause);
    
    parseTable(select_clause, from_clause);
    
    return dbRet;
  }
  
  protected void resetSearch() {
    setSortedByCol("NWLR_Date");
    setSortedByOrder("DESC");
  }
}