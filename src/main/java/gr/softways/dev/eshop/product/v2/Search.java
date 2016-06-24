package gr.softways.dev.eshop.product.v2;

import java.io.*;
import java.util.*;
import java.sql.Timestamp;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

import gr.softways.dev.eshop.eways.Product;
import gr.softways.dev.eshop.eways.Customer;

public class Search extends SearchBean2 {

  public Search() {
    super("product");
  }
  
  // Τα κριτήρια αναζήτησης {
  private String _q = "";

  public String getQ() {
    return _q;
  }
  
  // search in title and prdId
  private String _qid = "";

  public String getQID() {
    return _qid;
  }
  
  private String _catId = "";

  public String getCatId() {
    return _catId;
  }
  
  private String _manufactId = "";

  public String getManufactId() {
    return _manufactId;
  }
  
  private String _hotdealFlag = "";

  public String getHotdealFlag() {
    return _hotdealFlag;
  }
  
  private String _prdNewColl = "";

  public String getPrdNewColl() {
    return _prdNewColl;
  }
  public void setPrdNewColl(String prdNewColl) {
    _prdNewColl = prdNewColl;
  }
  // } Τα κριτήρια αναζήτησης

  protected synchronized DbRet search(HttpServletRequest request) {
    DbRet dbRet = new DbRet();
    
    String s = null, select_clause = null, from_clause = null;
    
    String lang = SwissKnife.getSessionAttr(databaseId + ".lang", request);
    
    if ( (s = request.getParameter("start")) == null ) s = "0";
    try {
      setStart(Integer.parseInt(s));
    }
    catch (Exception e) {
      setStart(0);
    }
    
    boolean hasProtectedPrdCat = false;
    if (SwissKnife.jndiLookup("swconf/hasProtectedPrdCat") != null && SwissKnife.jndiLookup("swconf/hasProtectedPrdCat").equals("true")) hasProtectedPrdCat = true;
    
    int customerType = 0;
    try {
      if (getSession().getAttribute(databaseId + ".front_end.customerType") != null) customerType = Integer.parseInt(getSession().getAttribute(databaseId + ".front_end.customerType").toString());
      else customerType = Customer.CUSTOMER_TYPE_RETAIL;
    }
    catch (Exception e) {
      customerType = Customer.CUSTOMER_TYPE_RETAIL;
    }
    
    if ( (_q = request.getParameter("q")) == null ) _q = "";
    
    if ( (_qid = request.getParameter("qid")) == null ) _qid = "";
    
    if ( (_catId = request.getParameter("catId")) == null ) _catId = "";
    
    if ( (_manufactId = request.getParameter("manufactId")) == null ) _manufactId = "";
    
    if ( (_hotdealFlag = request.getParameter("spof")) == null ) _hotdealFlag = "";
    
    if ( (_prdNewColl = request.getParameter("fprd")) == null ) _prdNewColl = "";
    
    select_clause = "SELECT * FROM product "
        + "JOIN prdInCatTab ON prdInCatTab.PINCPrdId = product.prdId "
        + "JOIN prdCategory ON prdCategory.catId = prdInCatTab.PINCCatId "
        + "JOIN "
        + "(SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " product.prdId as bprdid";
    
    from_clause = " FROM product JOIN prdInCatTab ON prdInCatTab.PINCPrdId = product.prdId JOIN prdCategory ON prdCategory.catId = prdInCatTab.PINCCatId"
                + " WHERE catShowFlag = '1'";
                
    if (customerType == Customer.CUSTOMER_TYPE_WHOLESALE) {
      from_clause += "       AND product.prdHideFlagW != '1'";
    }
    else {
      from_clause += "       AND product.prdHideFlag != '1'";
    }
    
    if (_q.length()>0) {
      StringTokenizer words = new StringTokenizer(_q, " ");
      
      while (words.hasMoreTokens()) {
        from_clause += " AND product.nameUp" + lang + " LIKE '%" + SwissKnife.sqlEncode(SwissKnife.searchConvert(words.nextToken())) + "%'";
      }
    }
    
    if (_qid.length()>0) {
      StringTokenizer words = new StringTokenizer(_qid, " ");
      
      while (words.hasMoreTokens()) {
        String nextToken = words.nextToken();
        
        from_clause += " AND (";
        from_clause += " product.nameUp" + lang + " LIKE '%" + SwissKnife.sqlEncode(SwissKnife.searchConvert(nextToken)) + "%'";
        from_clause += " OR";
        from_clause += " product.prdId LIKE '%" + SwissKnife.sqlEncode(SwissKnife.searchConvert(nextToken)) + "%'";
        from_clause += ")";
      }
    }
    
    if (_hotdealFlag.equals("1")) {
      Timestamp today = SwissKnife.currentDate();
      
      if (customerType == Customer.CUSTOMER_TYPE_WHOLESALE) {
        from_clause += " AND ( (hotdealFlagW = '" + Product.HOTDEAL_FLAG_DATE + "' AND '" + today + "' >= hdBeginDateW "
                     + " AND '" + today + "' <= hdEndDateW) "
                     + " OR (hotdealFlagW = '" + Product.HOTDEAL_FLAG_DATE_STOCK + "' AND stockQua > 0 AND '" + today + "' >= hdBeginDateW "
                     + " AND '" + today + "' <= hdEndDateW) "
                     + " OR (hotdealFlagW = '" + Product.HOTDEAL_FLAG_STOCK + "' AND stockQua > 0) "
                     + " OR (hotdealFlagW = '" + Product.HOTDEAL_FLAG_ALWAYS + "')"
                     + ")";
      }
      else {
        from_clause += " AND ( (hotdealFlag = '" + Product.HOTDEAL_FLAG_DATE + "' AND '" + today + "' >= hdBeginDate "
                     + " AND '" + today + "' <= hdEndDate) "
                     + " OR (hotdealFlag = '" + Product.HOTDEAL_FLAG_DATE_STOCK + "' AND stockQua > 0 AND '" + today + "' >= hdBeginDate "
                     + " AND '" + today + "' <= hdEndDate) "
                     + " OR (hotdealFlag = '" + Product.HOTDEAL_FLAG_STOCK + "' AND stockQua > 0) "
                     + " OR (hotdealFlag = '" + Product.HOTDEAL_FLAG_ALWAYS + "')"
                     + ")";
      }
    }
    
    if (_catId.length()>0) {
      from_clause += " AND prdInCatTab.PINCCatId LIKE '" + SwissKnife.sqlEncode(_catId) + "%'";
    }
    else from_clause += " AND prdInCatTab.PINCPrimary = '1'";
    
    if (_manufactId.length()>0) {
      from_clause += " AND product.prdManufactId = '" + SwissKnife.sqlEncode(_manufactId) + "'";
    }
    
    if (_prdNewColl.length()>0) {
      from_clause += " AND product.prdNewColl = '" + SwissKnife.sqlEncode(_prdNewColl) + "'";
    }
    
    if (hasProtectedPrdCat == true) from_clause += " AND (prdCategory.catCustomerType IS NULL OR prdCategory.catCustomerType = " + customerType + ")";
    
    if (getSortedByCol() == null || getSortedByCol().length() == 0) {
      setSortedByCol("PINCRank DESC, product.nameUp" + lang);
      setSortedByOrder("ASC");
    }
    
    setEndClause(") AS b ON product.prdid = b.bprdid");
    if (_catId.length()>0) {
      setEndClause(getEndClause() + " AND prdInCatTab.PINCCatId LIKE '" + SwissKnife.sqlEncode(_catId) + "%'");
    }
    else setEndClause(getEndClause() + " AND prdInCatTab.PINCPrimary = '1'");
    
    if (getSortedByCol() != null && getSortedByCol().length()>0) setEndClause(getEndClause() + " ORDER BY " + getSortedByCol() + " " + getSortedByOrder());
    
    parseTable(select_clause, from_clause);
    
    return dbRet;
  }
  
  protected void resetSearch() {
  }
}
