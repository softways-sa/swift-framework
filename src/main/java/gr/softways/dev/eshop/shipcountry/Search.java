package gr.softways.dev.eshop.shipcountry;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;

public class Search extends SearchBean {

  public Search() {
    super("shipCountry");
  }
  
  // Τα κριτήρια αναζήτησης {
  private String _SCName = "";

  public String getSCName() {
    return _SCName;
  }

  // } Τα κριτήρια αναζήτησης

  /**
    *  Γέμισε το queryDataSet με τα rows
    * που πληρούν τα κριτήρια αναζήτησης
   */
  protected synchronized DbRet parseTable(HttpServletRequest request) {
    String query = SwissKnife.buildQueryString(request,
                     "SELECT * FROM shipCountry,shipCountryZones"
                   + " WHERE SC_SCZCode = SCZCode",
                     " ORDER BY SCName",
                     new String[] {"SCName"},
                     new String[] {""},
                     new String[] {""},
                     new String[] {" SCName LIKE "},
                     new String[] {"C"},
                     new String[] {"'"},
                     new String[] {"%'"},
                     " AND", " AND", 1);

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

    // αποθήκευσε τις τιμές των κρητιρίων αναζήτησης
    if ( (_SCName = request.getParameter("SCName")) == null ) _SCName = "";
  }
}