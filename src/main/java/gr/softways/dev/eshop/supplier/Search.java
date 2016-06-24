package gr.softways.dev.eshop.supplier;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;

public class Search extends SearchBean {

  public Search() {
    super("supplier");
  }
  
  // Τα κριτήρια αναζήτησης {
  private String _supplierId = "";

  public String getSupplierId() {
    return _supplierId;
  }

  private String _companyName = "";

  public String getCompanyName() {
    return _companyName;
  }
  // } Τα κριτήρια αναζήτησης

  /**
    *  Γέμισε το queryDataSet με τα rows
    * που πληρούν τα κριτήρια αναζήτησης
   */
  protected synchronized DbRet parseTable(HttpServletRequest request) {
    String query = SwissKnife.buildQueryString(request,
                     "SELECT * FROM supplier ",
                     " ORDER BY companyName",
                     new String[] {"supplierId", "companyName"},
                     new String[] {"", ""},
                     new String[] {"", ""},
                     new String[] {" supplierId = ", " companyNameUp LIKE "},
                     new String[] {"C", "UP"},
                     new String[] {"'", "'"},
                     new String[] {"'", "%'"},
                     " WHERE", " AND", 2);

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
    if ( (_supplierId = request.getParameter("supplierId")) == null ) _supplierId = "";
    if ( (_companyName = request.getParameter("companyName")) == null ) _companyName = "";
  }
}