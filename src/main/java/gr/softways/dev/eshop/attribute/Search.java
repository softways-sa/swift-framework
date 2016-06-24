package gr.softways.dev.eshop.attribute;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;

public class Search extends SearchBean2 {

  public Search() {
    super("attribute");
    setSortedByCol("atrName");
    setSortedByOrder("ASC");
  }

  // Τα κριτήρια αναζήτησης {
  private String _atrName = "";

  public String getAtrName() {
    return _atrName;
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
    
    from_clause = " FROM attribute"
                + " WHERE 1=1 ";
    
   
    if (_atrName.length() > 0) {
      from_clause += " AND atrNameUp LIKE '%" + SwissKnife.searchConvert(SwissKnife.sqlEncode(_atrName)) + "%'";
    }
    
    parseTable(select_clause, from_clause);
    
    return dbRet;
  }  


  protected void storeState(HttpServletRequest request) {

    // αποθήκευσε τις τιμές των κριτηρίων αναζήτησης
    if ( (_atrName = request.getParameter("atrName")) == null ) _atrName = "";
  }
  
  protected void resetSearch() {
    setSortedByCol("atrName");
    setSortedByOrder("ASC");
  }
}