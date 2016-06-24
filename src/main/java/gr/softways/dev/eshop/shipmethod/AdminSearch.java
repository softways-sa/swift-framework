package gr.softways.dev.eshop.shipmethod;

import java.io.*;
import java.util.*;
import java.sql.Timestamp;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class AdminSearch extends SearchBean2 {

  public AdminSearch() {
    super("ShipCostMethod");
    
    setSortedByCol("SHCMTitle");
    setSortedByOrder("DESC");
  }
  
  // Τα κριτήρια αναζήτησης {
  
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
    
    select_clause = "SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " *";
    
    from_clause = " FROM ShipCostMethod WHERE 1 = 1";
    
    //System.out.println(from_clause);
    
    parseTable(select_clause, from_clause);
    
    return dbRet;
  }
  
  protected void resetSearch() {
    setSortedByCol("SHCMTitle");
    setSortedByOrder("DESC");
  }
}