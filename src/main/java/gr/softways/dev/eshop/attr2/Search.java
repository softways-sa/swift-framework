package gr.softways.dev.eshop.attr2;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;

public class Search extends SearchBean {

  public Search() {
    super("attributeTab2");
  }
  
  // Τα κριτήρια αναζήτησης {
  private String _att2AttCode = "";

  public String getAtt2AttCode() {
    return _att2AttCode;
  }
  
  private String _att2Name = "";

  public String getAtt2Name() {
    return _att2Name;
  }
  // } Τα κριτήρια αναζήτησης
  
  private String _att2PrdId = "";

  public void setAtt2PrdId(String att2PrdId) {
    _att2PrdId = att2PrdId;
  }
  public String getAtt2PrdId() {
    return _att2PrdId;
  }
  
  /**
   * Γέμισε το queryDataSet με τα rows
   * που πληρούν τα κριτήρια αναζήτησης
   */
  protected synchronized DbRet parseTable(HttpServletRequest request) {
    String select = "SELECT * FROM attributeTab2 WHERE"
                  + " att2PrdId = '" + getAtt2PrdId() + "'";
    
    String query = SwissKnife.buildQueryString(request,
                     select,
                     " ORDER BY att2AttCode",
                     new String[] {"att2AttCode","att2Name"},
                     new String[] {"",""},
                     new String[] {"",""},
                     new String[] {" att2AttCode = ", " att2Name LIKE "},
                     new String[] {"C","C"},
                     new String[] {"'","'"},
                     new String[] {"'","%'"},
                     " AND", " AND", 2);
                   
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

    // αποθήκευσε τις τιμές των κρητιρίων αναζήτησης
    if ( (_att2AttCode = request.getParameter("att2AttCode")) == null ) _att2AttCode = "";
    if ( (_att2Name = request.getParameter("att2Name")) == null ) _att2Name = "";
  }
  
  protected void resetSearchCriteria() {
    _query = "";
    
    _att2AttCode = "";
    _att2Name = "";
  }
}