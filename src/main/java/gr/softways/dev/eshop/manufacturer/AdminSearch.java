package gr.softways.dev.eshop.manufacturer;

import java.io.*;
import java.util.*;
import java.sql.Timestamp;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class AdminSearch extends SearchBean2 {

  public AdminSearch() {
    super("manufact");
    
    setSortedByCol("manufactName");
    setSortedByOrder("ASC");
  }
  
  // Τα κριτήρια αναζήτησης {
  private String _manufactId = "";

  public String getManufactId() {
    return _manufactId;
  }

  private String _manufactName = "";

  public String getManufactName() {
    return _manufactName;
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
    
    if ( (_manufactId = request.getParameter("manufactId")) == null ) _manufactId = "";
    
    if ( (_manufactName = request.getParameter("manufactName")) == null ) _manufactName = "";
    
    select_clause = "SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " *";
    
    from_clause = " FROM manufact WHERE 1 = 1";
    
    if (_manufactId.length()>0) {
      from_clause += " AND manufactId = '" + SwissKnife.sqlEncode(_manufactId) + "'";
    }
    
    if (_manufactName.length()>0) {
      from_clause += " AND manufactNameUp LIKE '%" + SwissKnife.searchConvert(SwissKnife.sqlEncode(_manufactName)) + "%'";
    }
    
    //System.out.println(from_clause);
    
    parseTable(select_clause, from_clause);
    
    return dbRet;
  }
  
  protected void resetSearch() {
    setSortedByCol("manufactName");
    setSortedByOrder("ASC");
  }
}
