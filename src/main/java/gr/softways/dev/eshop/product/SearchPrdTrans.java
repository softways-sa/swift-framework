package gr.softways.dev.eshop.product;

import java.io.*;
import java.util.*;
import java.sql.Timestamp;

import javax.servlet.http.*;

import gr.softways.dev.util.*;

public class SearchPrdTrans extends SearchBean {

  public SearchPrdTrans() {
    super("prdImports");
  }
  
  // Τα κριτήρια αναζήτησης {
  private String _prdId = "";

  public String getPrdId() {
    return _prdId;
  }
  public void setPrdId(String prdId) {
    _prdId = prdId;
  }
  
  private String _dateDayFrom = "";

  public String getDateDayFrom() {
    return _dateDayFrom;
  }
  
  private String _dateMonthFrom = "";

  public String getDateMonthFrom() {
    return _dateMonthFrom;
  }
  
  private String _dateYearFrom = "";

  public String getDateYearFrom() {
    return _dateYearFrom;
  }
  
  private String _dateDayTo = "";

  public String getDateDayTo() {
    return _dateDayTo;
  }
  
  private String _dateMonthTo = "";

  public String getDateMonthTo() {
    return _dateMonthTo;
  }
  
  private String _dateYearTo = "";

  public String getDateYearTo() {
    return _dateYearTo;
  }
  
  private String _searchFlag = "";

  public String getSearchFlag() {
    return _searchFlag;
  }
  // } Τα κριτήρια Αναζήτησης
  
  /**
   * Γέμισε το queryDataSet με τα rows
   * που πληρούν τα κριτήρια αναζήτησης
   */
  protected synchronized DbRet parseTable(HttpServletRequest request) {
    String query = null;
    
    String tmpSearchFlag = request.getParameter("searchFlag");
    
    if (tmpSearchFlag != null && tmpSearchFlag.equals("1")) {
      query = SwissKnife.buildQueryString(request,
                 "SELECT * FROM prdImports ",
                 " ORDER BY importDate DESC, transId DESC",
                 new String[] {"prdId","dateYearFrom","dateYearTo"},
                 new String[] {"","dateMonthFrom","dateMonthTo"},
                 new String[] {"","dateDayFrom","dateDayTo"},
                 new String[] {" prdId = "," importDate >= "," importDate < "},
                 new String[] {"C","D","D"},
                 new String[] {"'","'","'"},
                 new String[] {"'","'","'"},
                 " WHERE", " AND", 3);
    }
    else {
      query = SwissKnife.buildQueryString(request,
                 "SELECT * FROM transactions ",
                 " ORDER BY orderDate DESC",
                 new String[] {"prdId","dateYearFrom","dateYearTo"},
                 new String[] {"","dateMonthFrom","dateMonthTo"},
                 new String[] {"","dateDayFrom","dateDayTo"},
                 new String[] {" prdId = "," orderDate >= "," orderDate < "},
                 new String[] {"C","D","D"},
                 new String[] {"'","'","'"},
                 new String[] {"'","'","'"},
                 " WHERE", " AND", 3);
    }
    
    // save bean state
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
    if ( (_dateDayFrom = request.getParameter("dateDayFrom")) == null ) _dateDayFrom = "";    
    if ( (_dateMonthFrom = request.getParameter("dateMonthFrom")) == null ) _dateMonthFrom = "";
    if ( (_dateYearFrom = request.getParameter("dateYearFrom")) == null ) _dateYearFrom = "";
    
    if ( (_dateDayTo = request.getParameter("dateDayTo")) == null ) _dateDayTo = "";    
    if ( (_dateMonthTo = request.getParameter("dateMonthTo")) == null ) _dateMonthTo = "";
    if ( (_dateYearTo = request.getParameter("dateYearTo")) == null ) _dateYearTo = "";

    if ( (_searchFlag = request.getParameter("searchFlag")) == null ) _searchFlag = "";
  }
  
  protected void resetSearchCriteria() {
    _query = "";
    
    _dateDayFrom = "";
    _dateMonthFrom = "";
    _dateYearFrom = "";
    
    _dateDayTo = "";
    _dateMonthTo = "";
    _dateYearTo = "";
    
    _searchFlag = "";
  }
}