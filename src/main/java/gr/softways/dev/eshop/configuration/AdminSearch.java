package gr.softways.dev.eshop.configuration;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class AdminSearch extends SearchBean2 {

  public AdminSearch() {
    super("Configuration");
    
    setSortedByCol("CO_SortOrder");
    setSortedByOrder("ASC");
  }
  
  // Τα κριτήρια αναζήτησης {
  private String _CO_Key = "";

  public String getCO_Key() {
    return _CO_Key;
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
    
    if ( (_CO_Key = request.getParameter("CO_Key")) == null ) _CO_Key = "";
    
    select_clause = "SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " *";
    
    from_clause = " FROM Configuration WHERE CO_Visible = 1";
    
    if (_CO_Key.length()>0) {
      from_clause += " AND CO_Key LIKE '%" + SwissKnife.sqlEncode(_CO_Key) + "%'";
    }
    
    //System.out.println(from_clause);
    
    parseTable(select_clause, from_clause);
    
    return dbRet;
  }
  
  protected void resetSearch() {
    setSortedByCol("CO_SortOrder");
    setSortedByOrder("ASC");
  }
}