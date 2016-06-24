package gr.softways.dev.eshop.category.v2;

import javax.servlet.http.*;

import gr.softways.dev.util.*;

public class Search extends SearchBean2 {

  public Search() {
    super("prdCategory");
    
    setSortedByCol("catId");
    setSortedByOrder("ASC");
  }
  
  // Τα κριτήρια αναζήτησης {
  private String _catId = "";

  public String getCatId() {
    return _catId;
  }

  private String _catName = "";

  public String getCatName() {
    return _catName;
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
    
    if ( (_catId = request.getParameter("catId")) == null ) _catId = "";
    if ( (_catName = request.getParameter("catName")) == null ) _catName = "";
    
    select_clause = "SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " *";
    
    from_clause = " FROM prdCategory WHERE 1 = 1";
    
    if (_catId.length()>0) {
      from_clause += " AND catId LIKE '" + SwissKnife.sqlEncode(_catId) + "%'";
    }
    
    if (_catName.length()>0) {
      from_clause += " AND catNameUp LIKE '%" + SwissKnife.searchConvert(SwissKnife.sqlEncode(_catName)) + "%'";
    }
    
    parseTable(select_clause, from_clause);
    
    return dbRet;
  }
  
  protected void resetSearch() {
    setSortedByCol("catId");
    setSortedByOrder("ASC");
  }
}