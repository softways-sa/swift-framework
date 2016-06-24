package gr.softways.dev.eshop.emaillists.members;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;

public class Search extends SearchBean {

  public Search() {
    super("emailListMember");
  }
  
  // Τα κριτήρια αναζήτησης {
  private String _EMLMEmail = "";

  public String getEMLMEmail() {
    return _EMLMEmail;
  }

  private String _EMLMLastName = "";

  public String getEMLMLastName() {
    return _EMLMLastName;
  }
  // } Τα κριτήρια αναζήτησης

  /**
   * Γέμισε το queryDataSet με τα rows
   * που πληρούν τα κριτήρια αναζήτησης
   */
  protected synchronized DbRet parseTable(HttpServletRequest request) {
    String query = SwissKnife.buildQueryString(request,
                       "SELECT * FROM emailListMember ",
                       " ORDER BY EMLMEmail",
                       new String[] {"EMLMEmail","EMLMLastName"},
                       new String[] {"", ""},
                       new String[] {"", ""},
                       new String[] {" EMLMEmail LIKE ", " EMLMLastNameUp LIKE "},
                       new String[] {"C","UP"},
                       new String[] {"'%","'"},
                       new String[] {"%'","%'"},
                       " WHERE", " AND", 2);

    // System.out.println(query);

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
    if( (_EMLMEmail = request.getParameter("EMLMEmail")) == null) _EMLMEmail = "";
    if( (_EMLMLastName = request.getParameter("EMLMLastName")) == null) _EMLMLastName = "";
  }  
}
