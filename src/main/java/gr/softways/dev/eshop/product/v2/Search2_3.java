package gr.softways.dev.eshop.product.v2;

import java.io.*;
import java.util.*;
import java.sql.Timestamp;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

import gr.softways.dev.eshop.eways.Product;
import gr.softways.dev.eshop.eways.Customer;
import gr.softways.dev.eshop.eways.v2.Order;

public class Search2_3 extends SearchBean2 {

  public Search2_3() {
    super("product");
  }
  
  // Τα κριτήρια αναζήτησης {
  
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
  
  private String _prdCompFlag = "";

  public String getPrdCompFlag() {
    return _prdCompFlag;
  }
  
  public void setPrdCompFlag(String prdCompFlag) {
    _prdCompFlag = prdCompFlag;
  }
  // } Τα κριτήρια αναζήτησης

  protected synchronized DbRet search(HttpServletRequest request) {
    DbRet dbRet = new DbRet();
    
    String s = null;
    
    StringBuilder select_clause = new StringBuilder(), from_clause = new StringBuilder();
    
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
    
    if ( (_qid = request.getParameter("qid")) == null ) _qid = "";
    
    if ( (_catId = request.getParameter("catId")) == null ) _catId = "";
    
    if ( (_manufactId = request.getParameter("manufactId")) == null ) _manufactId = "";
    
    if ( (_hotdealFlag = request.getParameter("spof")) == null ) _hotdealFlag = "";
    
    if ( (_prdNewColl = request.getParameter("fprd")) == null ) _prdNewColl = "";
    
    if ( (_prdCompFlag = request.getParameter("newarr")) == null ) _prdCompFlag = "";
    
    String inventoryType = gr.softways.dev.util.SwissKnife.jndiLookup("swconf/inventoryType");
    
    select_clause.append("SELECT FIRST ").append(getDispRows()).append(" SKIP ").append(getStart());
    select_clause.append(" product.prdId");
    select_clause.append(",product.name").append(lang);
    select_clause.append(",product.hotdealFlag,product.hdBeginDate,product.hdEndDate,product.hdRetailPrcEU");
    select_clause.append(",product.retailPrcEU,product.giftPrcEU,product.hotdealFlagW,product.hdBeginDateW");
    select_clause.append(",product.hdEndDateW,product.hdWholesalePrcEU,product.wholesalePrcEU");
    select_clause.append(",product.stockQua,product.prdCompFlag");
    select_clause.append(",prdCategory.catName").append(lang);
    select_clause.append(",VAT.VAT_Pct");
        
    from_clause.append(" FROM product LEFT JOIN prdInCatTab ON product.prdId = prdInCatTab.PINCPrdId LEFT JOIN prdCategory ON prdInCatTab.PINCCatId = prdCategory.catId LEFT JOIN VAT ON product.PRD_VAT_ID = VAT.VAT_ID");
    from_clause.append(" WHERE prdCategory.catShowFlag = '1'");
    
    if (customerType == Customer.CUSTOMER_TYPE_WHOLESALE) {
      from_clause.append(" AND product.prdHideFlagW = '0'");
    }
    else {
      from_clause.append(" AND product.prdHideFlag = '0'");
    }
    
    if (_qid.length() > 0) {
      StringTokenizer words = new StringTokenizer(_qid, " ");
      
      while (words.hasMoreTokens()) {
        String nextToken = words.nextToken();
        
        from_clause.append(" AND (");
        from_clause.append(" product.nameUp").append(lang).append(" LIKE '%").append(SwissKnife.sqlEncode(SwissKnife.searchConvert(nextToken))).append("%'");
        from_clause.append(" OR");
        from_clause.append(" product.prdId LIKE '%").append(SwissKnife.sqlEncode(SwissKnife.searchConvert(nextToken))).append("%'");
        from_clause.append(")");
      }
    }
    
    if (_hotdealFlag.equals("1")) {
      Timestamp today = SwissKnife.currentDate();
      
      if (customerType == Customer.CUSTOMER_TYPE_WHOLESALE) {
        from_clause.append(" AND ( (hotdealFlagW = '").append(Product.HOTDEAL_FLAG_DATE).append("' AND '").append(today).append("' >= hdBeginDateW");
        from_clause.append(" AND '").append(today).append("' <= hdEndDateW)");
        from_clause.append(" OR (hotdealFlagW = '").append(Product.HOTDEAL_FLAG_DATE_STOCK).append("' AND stockQua > 0 AND '").append(today).append("' >= hdBeginDateW");
        from_clause.append(" AND '").append(today).append("' <= hdEndDateW)");
        from_clause.append(" OR (hotdealFlagW = '").append(Product.HOTDEAL_FLAG_STOCK).append("' AND stockQua > 0)");
        from_clause.append(" OR (hotdealFlagW = '").append(Product.HOTDEAL_FLAG_ALWAYS).append("')");
        from_clause.append(")");
      }
      else {
        from_clause.append(" AND ( (hotdealFlag = '").append(Product.HOTDEAL_FLAG_DATE).append("' AND '").append(today).append("' >= hdBeginDate ");
        from_clause.append(" AND '").append(today).append("' <= hdEndDate) ");
        from_clause.append(" OR (hotdealFlag = '").append(Product.HOTDEAL_FLAG_DATE_STOCK).append("' AND stockQua > 0 AND '").append(today).append("' >= hdBeginDate ");
        from_clause.append(" AND '").append(today).append("' <= hdEndDate) ");
        from_clause.append(" OR (hotdealFlag = '").append(Product.HOTDEAL_FLAG_STOCK).append("' AND stockQua > 0) ");
        from_clause.append(" OR (hotdealFlag = '").append(Product.HOTDEAL_FLAG_ALWAYS).append("')");
        from_clause.append(")");
      }
    }
    
    if (_catId.length()>0) {
      from_clause.append(" AND prdInCatTab.PINCCatId LIKE '").append(SwissKnife.sqlEncode(_catId)).append("%'");
    }
    else {
      from_clause.append(" AND prdInCatTab.PINCPrimary = '1'");
    }
    
    if (_manufactId.length()>0) {
      from_clause.append(" AND product.prdManufactId = '").append(SwissKnife.sqlEncode(_manufactId)).append("'");
    }
    
    if (_prdNewColl.length()>0) {
      from_clause.append(" AND product.prdNewColl = '").append(SwissKnife.sqlEncode(_prdNewColl)).append("'");
    }
    
    if (_prdCompFlag.length()>0) {
      from_clause.append(" AND product.prdCompFlag = '").append(SwissKnife.sqlEncode(_prdCompFlag)).append("'");
    }
    
    if (hasProtectedPrdCat == true) {
      from_clause.append(" AND (prdCategory.catCustomerType IS NULL OR prdCategory.catCustomerType = ").append(customerType).append(")");
    }
    
    if (inventoryType != null && inventoryType.equals(Order.STOCK_INVENTORY)) {
      from_clause.append(" AND product.stockQua > 0");
    }
    
    if (getSortedByCol() == null || getSortedByCol().length() == 0) {
      setSortedByCol("");
      setSortedByOrder("");
    }
    
    //System.out.println(select_clause.toString() + " " + from_clause.toString() + " ORDER BY " + getSortedByCol() + " " + getSortedByOrder());
    
    parseTable(select_clause.toString(), from_clause.toString());
    
    return dbRet;
  }
  
  protected void resetSearch() {
    setSortedByCol("");
    setSortedByOrder("");
  }
}