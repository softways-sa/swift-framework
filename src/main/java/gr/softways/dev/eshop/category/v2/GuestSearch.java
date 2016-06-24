package gr.softways.dev.eshop.category.v2;

import gr.softways.dev.eshop.eways.Customer;
import javax.servlet.http.*;

import gr.softways.dev.util.*;

public class GuestSearch extends SearchBean2 {

  public GuestSearch() {
    super("prdCategory");
  }
  
  // Τα κριτήρια αναζήτησης {
  private String _catId = "";

  public String getCatId() {
    return _catId;
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
    
    int customerType = 0;
    try {
      if (getSession().getAttribute(databaseId + ".front_end.customerType") != null) customerType = Integer.parseInt(getSession().getAttribute(databaseId + ".front_end.customerType").toString());
      else customerType = Customer.CUSTOMER_TYPE_RETAIL;
    }
    catch (Exception e) {
      customerType = Customer.CUSTOMER_TYPE_RETAIL;
    }
    
    boolean hasProtectedPrdCat = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedPrdCat") != null && SwissKnife.jndiLookup("swconf/hasProtectedPrdCat").equals("true")) hasProtectedPrdCat = true;
    
    if ( (_catId = request.getParameter("catId")) == null ) _catId = "";
    
    String catIdBlanks = "__";
    int fillBlanks = 25-catIdBlanks.length()-_catId.length();
    for (int i=0; i<fillBlanks; i++) catIdBlanks = catIdBlanks + " ";
    
    select_clause = "SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " *";
    
    from_clause = " FROM prdCategory WHERE catShowFlag = '1'";
    
    from_clause += " AND catId LIKE '" + SwissKnife.sqlEncode(_catId) + catIdBlanks + "' AND catId != '" + SwissKnife.sqlEncode(_catId) + "'";
    
    if (hasProtectedPrdCat == true) from_clause += " AND (prdCategory.catCustomerType IS NULL OR prdCategory.catCustomerType = " + customerType + ")";
    
    if (getSortedByCol() == null || getSortedByCol().length() == 0) {
      setSortedByCol("catRank");
      setSortedByOrder("DESC");
    }
    
    parseTable(select_clause, from_clause);
    
    return dbRet;
  }
  
  protected void resetSearch() {
  }
}