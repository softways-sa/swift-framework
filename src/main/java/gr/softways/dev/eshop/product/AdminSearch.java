/*
 * AdminSearch.java
 *
 * Created on 19 Νοέμβριος 2003, 1:49 μμ
 */

package gr.softways.dev.eshop.product;

import java.io.*;
import java.util.*;
import java.sql.Timestamp;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.eshop.eways.Product;

public class AdminSearch extends SearchBean {

  public AdminSearch() {
    super("product");
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
  // } Τα κριτήρια αναζήτησης

  /**
   * Γέμισε το queryDataSet με τα rows
   * που πληρούν τα κριτήρια αναζήτησης
   */
  protected synchronized DbRet parseTable(HttpServletRequest request) {
    String select = null, catId = null, clause = null;
    
    String selectFromProduct = "product.prdId, product.name, product.vatPct"
                      + ",product.wholesalePrcEU, product.retailPrcEU"
                      + ",product.wholesalePrc, product.retailPrc"
                      + ",product.slWholesalePrc, product.slRetailPrcEU"
                      + ",product.slWholesalePrcEU, product.slRetailPrc"
                      + ",product.hdWholesalePrcEU, product.hdRetailPrcEU"
                      + ",product.hdWholesalePrc, product.hdRetailPrc"
                      + ",product.giftPrcEU, product.giftPrc, product.stockQua"
                      + ",product.hdStockFlag, product.hdStockFlagW"
                      + ",product.hotdealFlag, product.hotdealFlagW"
                      + ",product.salesFlag, product.salesFlagW"
                      + ",product.hdBeginDate, product.hdEndDate"
                      + ",product.hdBeginDateW, product.hdEndDateW"
                      + ",product.prdHideFlag, product.prdHideFlagW";
    
    if ( (catId = request.getParameter("catId")) == null ) catId = "";
    
    if (catId.length()>0) {
      select = "SELECT prdInCatTab.*" 
             + "," + selectFromProduct
             + " FROM product,prdInCatTab,prdCategory"
             + " WHERE prdInCatTab.PINCPrdId = product.prdId" 
             + " AND prdCategory.catId = prdInCatTab.PINCCatId";
      
      clause = " AND ";
    }
    else {
      select = "SELECT " 
             + selectFromProduct
             + " FROM product";
      
      clause = " WHERE ";
    }
            
    String query = SwissKnife.buildQueryString(request,
                     select,
                     "",
                     new String[] {"prdId","name","catId","prdHideFlag","prdHideFlagW","prdNewColl"},
                     new String[] {"","","","","",""},
                     new String[] {"","","","","",""},
                     new String[] {" product.prdId = "," product.nameUp LIKE "," prdInCatTab.PINCCatId = "," prdHideFlag = "," prdHideFlagW = "," prdNewColl = "},
                     new String[] {"C","UP","C","C","C","C"},
                     new String[] {"'","'%","'","'","'","'"},
                     new String[] {"'","%'","'","'","'","'"},
                     clause, " AND", 6);

    if ( (_hotdeal = request.getParameter("hotdeal")) == null ) _hotdeal = "";
    if (_hotdeal.equals("1")) {
      Timestamp today = SwissKnife.currentDate();
      
      if (clause.indexOf("AND") != -1 || query.indexOf("FROM product WHERE") != -1) {
        clause = "AND";
      }
      else {
        clause = "WHERE";
      }
      
      query += " " + clause + " (";
      query += "( (hotdealFlag = '" + Product.HOTDEAL_FLAG_DATE + "' AND '" + today + "' >= hdBeginDate "
             + " AND '" + today + "' <= hdEndDate) "
             + " OR (hotdealFlag = '" + Product.HOTDEAL_FLAG_DATE_STOCK + "' AND stockQua > 0 AND '" + today + "' >= hdBeginDate "
             + " AND '" + today + "' <= hdEndDate) "
             + " OR (hotdealFlag = '" + Product.HOTDEAL_FLAG_STOCK + "' AND stockQua > 0) "
             + " OR (hotdealFlag = '" + Product.HOTDEAL_FLAG_ALWAYS + "')"
             + ")";
      query += " OR ";
      query += "( (hotdealFlagW = '" + Product.HOTDEAL_FLAG_DATE + "' AND '" + today + "' >= hdBeginDateW "
             + " AND '" + today + "' <= hdEndDateW) "
             + " OR (hotdealFlagW = '" + Product.HOTDEAL_FLAG_DATE_STOCK + "' AND stockQua > 0 AND '" + today + "' >= hdBeginDateW "
             + " AND '" + today + "' <= hdEndDateW) "
             + " OR (hotdealFlagW = '" + Product.HOTDEAL_FLAG_STOCK + "' AND stockQua > 0) "
             + " OR (hotdealFlagW = '" + Product.HOTDEAL_FLAG_ALWAYS + "')"
             + ")";
        query += ")";
    }
    
    query += " ORDER BY prdId";
    
    //System.out.println(query);

    storeState(request, query);

    setMaxRows(Integer.parseInt(Director.getInstance().getPoolAttr(databaseId + ".maxRows")));
    
    return fillQueryDataSet(query);
  }

  /**
   * Αποθήκευση του state.
   *
   */
  protected void storeState(HttpServletRequest request, String query) {
    _query = query;

    // αποθήκευσε τις τιμές των κριτηρίων αναζήτησης
    if ( (_prdId = request.getParameter("prdId")) == null ) _prdId = "";
    if ( (_name = request.getParameter("name")) == null ) _name = "";
    if ( (_catId = request.getParameter("catId")) == null ) _catId = "";
    if ( (_prdHideFlag = request.getParameter("prdHideFlag")) == null ) _prdHideFlag = "";
    if ( (_prdHideFlagW = request.getParameter("prdHideFlagW")) == null ) _prdHideFlagW = "";
    if ( (_prdNewColl = request.getParameter("prdNewColl")) == null ) _prdNewColl = "";
    if ( (_hotdeal = request.getParameter("hotdeal")) == null ) _hotdeal = "";
  }
}