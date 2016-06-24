package gr.softways.dev.eshop.customer;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;

public class Search extends SearchBean {

  public Search() {
    super("customer");
  }
  
  // Τα κριτήρια αναζήτησης {
  private String _lastname = "";

  public String getLastname() {
    return _lastname;
  }

  private String _email = "";

  public String getEmail() {
    return _email;
  }
  
  private String _customerType = "";

  public String getCustomerType() {
    return _customerType;
  }
  // } Τα κριτήρια αναζήτησης

  /**
    *  Γέμισε το queryDataSet με τα rows
    * που πληρούν τα κριτήρια αναζήτησης
   */
  protected synchronized DbRet parseTable(HttpServletRequest request) {
    String query = SwissKnife.buildQueryString(request,
                     "SELECT * FROM customer,users,userGroups"
                   + " WHERE custLogCode = logCode"
                   + " AND usrAccessLevel = userGroupId",
                     " ORDER BY lastnameUp",
                     new String[] {"lastname","email","customerType"},
                     new String[] {"","",""},
                     new String[] {"","",""},
                     new String[] {" lastnameUp LIKE ", " email = "," customerType = "},
                     new String[] {"UP","C","N"},
                     new String[] {"'","'","'"},
                     new String[] {"%'","'","'"},
                     " AND"," AND",3);

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
    if ( (_lastname = request.getParameter("lastname")) == null ) _lastname = "";
    if ( (_email = request.getParameter("email")) == null ) _email = "";
    if ( (_customerType = request.getParameter("customerType")) == null ) _customerType = "";
  }
}