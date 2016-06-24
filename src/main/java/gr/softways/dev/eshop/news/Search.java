package gr.softways.dev.eshop.news;

import java.io.*;
import java.util.*;
import java.sql.Timestamp;

import javax.servlet.http.*;

import gr.softways.dev.util.*;

public class Search extends SearchBean {

  public Search() {
    super("newsTab");
  }
  
  // Τα κριτήρια αναζήτησης {
  private String _newsDayDay = "";

  public String getNewsDayDay() {
    return _newsDayDay;
  }
  
  private String _newsDayMonth = "";

  public String getNewsDayMonth() {
    return _newsDayMonth;
  }
  
  private String _newsDayYear = "";

  public String getNewsDayYear() {
    return _newsDayYear;
  }
  
  private String _newsDayToDay = "";

  public String getNewsDayToDay() {
    return _newsDayToDay;
  }
  
  private String _newsDayToMonth = "";

  public String getNewsDayToMonth() {
    return _newsDayToMonth;
  }
  
  private String _newsDayToYear = "";

  public String getNewsDayToYear() {
    return _newsDayToYear;
  }
  
  private String _newsTitle = "";

  public String getNewsTitle() {
    return _newsTitle;
  }
  
  private String _newsType = "";

  public String getNewsType() {
    return _newsType;
  }
  // } Τα κριτήρια Αναζήτησης
  
  /**
   * Γέμισε το queryDataSet με τα rows
   * που πληρούν τα κριτήρια αναζήτησης
   */
  protected synchronized DbRet parseTable(HttpServletRequest request) {
    String query = SwissKnife.buildQueryString(request,
                     "SELECT newsCode,newsDay,newsTitle,newsType FROM newsTab ",
                     " ORDER BY newsDay DESC",
                     new String[] {"newsDayYear","newsDayToYear","newsTitle","newsType"},
                     new String[] {"newsDayMonth","newsDayToMonth","",""},
                     new String[] {"newsDayDay","newsDayToDay","",""},
                     new String[] {" newsDay >= "," newsDay <= "," newsTitleUp LIKE ", " newsType = "},
                     new String[] {"D","D","UP","C"},
                     new String[] {"'","'","'","'"},
                     new String[] {"'","'","%'","'"},
                     " WHERE ", " AND ", 4);
                     
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
    if ( (_newsDayDay = request.getParameter("newsDayDay")) == null ) _newsDayDay = "";
    if ( (_newsDayMonth = request.getParameter("newsDayMonth")) == null ) _newsDayMonth = "";
    if ( (_newsDayYear = request.getParameter("newsDayYear")) == null ) _newsDayYear = "";
    
    if ( (_newsDayToDay = request.getParameter("newsDayToDay")) == null ) _newsDayToDay = "";
    if ( (_newsDayToMonth = request.getParameter("newsDayToMonth")) == null ) _newsDayToMonth = "";
    if ( (_newsDayToYear = request.getParameter("newsDayToYear")) == null ) _newsDayToYear = "";
    
    if ( (_newsTitle = request.getParameter("newsTitle")) == null ) _newsTitle = "";
    if ( (_newsType = request.getParameter("newsType")) == null ) _newsType = "";
  }
}
