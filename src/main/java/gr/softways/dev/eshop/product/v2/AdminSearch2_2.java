package gr.softways.dev.eshop.product.v2;

import java.io.*;
import java.util.*;
import java.sql.Timestamp;
import java.math.BigDecimal;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

import gr.softways.dev.eshop.eways.Product;

public class AdminSearch2_2 extends SearchBean2 {

  public AdminSearch2_2() {
    super("product");
    
    setSortedByCol("prdId");
    setSortedByOrder("ASC");
  }
  
  // Τα κριτήρια αναζήτησης {
  private String _prdId = "";

  public String getPrdId() {
    return _prdId;
  }
  
  private String _name = "";

  public String getName() {
    return _name;
  }

  private String _catId = "";

  public String getCatId() {
    return _catId;
  }
  
  private String _prdHideFlag = "";

  public String getPrdHideFlag() {
    return _prdHideFlag;
  }
  
  private String _prdHideFlagW = "";

  public String getPrdHideFlagW() {
    return _prdHideFlagW;
  }

  private String _prdNewColl = "";

  public String getPrdNewColl() {
    return _prdNewColl;
  }
  
  private String _hotdeal = "";

  public String getHotdeal() {
    return _hotdeal;
  }
  
  private BigDecimal _stockQua = null;

  public BigDecimal getStockQua() {
    return _stockQua;
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
    
    if ( (_prdId = request.getParameter("prdId")) == null ) _prdId = "";
    if ( (_name = request.getParameter("name")) == null ) _name = "";
    if ( (_catId = request.getParameter("catId")) == null ) _catId = "";
    if ( (_prdHideFlag = request.getParameter("prdHideFlag")) == null ) _prdHideFlag = "";
    if ( (_prdHideFlagW = request.getParameter("prdHideFlagW")) == null ) _prdHideFlagW = "";
    if ( (_prdNewColl = request.getParameter("prdNewColl")) == null ) _prdNewColl = "";
    if ( (_hotdeal = request.getParameter("hotdeal")) == null ) _hotdeal = "";
    
    try {
      _stockQua = new BigDecimal( request.getParameter("stockQua") );
    }
    catch (Exception e) {
      _stockQua = null;
    }
    
    select_clause = "SELECT * FROM product";
    if (_catId.length()>0) {
      select_clause += " JOIN prdInCatTab ON prdInCatTab.PINCPrdId = product.prdId JOIN prdCategory ON prdCategory.catId = prdInCatTab.PINCCatId JOIN VAT ON product.PRD_VAT_ID = VAT.VAT_ID"
          + " JOIN" 
          + " (SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " product.prdId as bprdid";
    }
    else {
      select_clause += " JOIN VAT ON product.PRD_VAT_ID = VAT.VAT_ID JOIN" 
          + " (SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " product.prdId as bprdid";
    }
    
    if (_catId.length()>0) {
      from_clause = " FROM product JOIN prdInCatTab ON prdInCatTab.PINCPrdId = product.prdId JOIN prdCategory ON prdCategory.catId = prdInCatTab.PINCCatId";
    }
    else {
      from_clause = " FROM product JOIN VAT ON product.PRD_VAT_ID = VAT.VAT_ID WHERE 1 = 1";
    }
    
    if (_prdId.length()>0) {
      from_clause += " AND product.prdId = '" + SwissKnife.sqlEncode(_prdId) + "'";
    }
    
    if (_name.length()>0) {
      from_clause += " AND product.nameUp LIKE '%" + SwissKnife.searchConvert(SwissKnife.sqlEncode(_name)) + "%'";
    }
    
    if (_catId.length()>0) {
      from_clause += " AND prdInCatTab.PINCCatId LIKE '" + SwissKnife.sqlEncode(_catId) + "%'";
    }
    
    if (_prdHideFlag.length()>0) {
      from_clause += " AND product.prdHideFlag = '" + SwissKnife.sqlEncode(_prdHideFlag) + "'";
    }
    
    if (_prdHideFlagW.length()>0) {
      from_clause += " AND product.prdHideFlagW = '" + SwissKnife.sqlEncode(_prdHideFlagW) + "'";
    }
    
    if (_prdNewColl.length()>0) {
      from_clause += " AND product.prdNewColl = '" + SwissKnife.sqlEncode(_prdNewColl) + "'";
    }
    
    if (_hotdeal.equals("1")) {
      Timestamp today = SwissKnife.currentDate();
      
      from_clause += " AND (";
      from_clause += "( (hotdealFlag = '" + Product.HOTDEAL_FLAG_DATE + "' AND '" + today + "' >= hdBeginDate "
                   + " AND '" + today + "' <= hdEndDate) "
                   + " OR (hotdealFlag = '" + Product.HOTDEAL_FLAG_DATE_STOCK + "' AND stockQua > 0 AND '" + today + "' >= hdBeginDate "
                   + " AND '" + today + "' <= hdEndDate) "
                   + " OR (hotdealFlag = '" + Product.HOTDEAL_FLAG_STOCK + "' AND stockQua > 0) "
                   + " OR (hotdealFlag = '" + Product.HOTDEAL_FLAG_ALWAYS + "')"
                   + ")";
      from_clause += " OR ";
      from_clause += "( (hotdealFlagW = '" + Product.HOTDEAL_FLAG_DATE + "' AND '" + today + "' >= hdBeginDateW "
                   + " AND '" + today + "' <= hdEndDateW) "
                   + " OR (hotdealFlagW = '" + Product.HOTDEAL_FLAG_DATE_STOCK + "' AND stockQua > 0 AND '" + today + "' >= hdBeginDateW "
                   + " AND '" + today + "' <= hdEndDateW) "
                   + " OR (hotdealFlagW = '" + Product.HOTDEAL_FLAG_STOCK + "' AND stockQua > 0) "
                   + " OR (hotdealFlagW = '" + Product.HOTDEAL_FLAG_ALWAYS + "')"
                   + ")";
      from_clause += ")";
    }
    
    if (_stockQua != null) {
      from_clause += " AND product.stockQua < '" + _stockQua + "'";
    }
    
    setEndClause(") as b on product.prdid = b.bprdid");
    if (_catId.length()>0) {
      setEndClause(getEndClause() + " AND prdInCatTab.PINCCatId LIKE '" + SwissKnife.sqlEncode(_catId) + "%'");
    }
    
    if (getSortedByCol() != null && getSortedByCol().length()>0) setEndClause(getEndClause() + " ORDER BY " + getSortedByCol() + " " + getSortedByOrder());
    
    parseTable(select_clause, from_clause);
    
    return dbRet;
  }
  
  protected void resetSearch() {
    setSortedByCol("prdId");
    setSortedByOrder("ASC");
  }
}