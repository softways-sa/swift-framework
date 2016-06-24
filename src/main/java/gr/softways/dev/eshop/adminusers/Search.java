package gr.softways.dev.eshop.adminusers;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;

public class Search extends SearchBean {

  public Search() {
    super("adminUsers");
  }

  // Τα κριτήρια αναζήτησης {
  private String _ausrCode = "";

  public String getAusrCode(){
    return _ausrCode;
  }

  private String _ausrLastname = "";

  public String getAusrLastname(){
    return _ausrLastname;
  }

  // } Τα κριτήρια αναζήτησης

  protected synchronized DbRet parseTable(HttpServletRequest request) {

    String query = SwissKnife.buildQueryString(request,
                     "SELECT * FROM adminUsers, users "
                   + " WHERE ausrLogCode = users.logCode ",
                     " ORDER BY ausrLastname",
                     new String[] {"ausrCode", "ausrLastname"},
                     new String[] {"", ""},
                     new String[] {"", ""},
                     new String[] {" ausrCode = ", " ausrLastnameUp LIKE "},
                     new String[] {"C", "UP"},
                     new String[] {"'", "'"},
                     new String[] {"'", "%'"},
                     " AND", " AND", 2);

    storeState(request, query);

    return fillQueryDataSet(query);

  }

  protected void storeState(HttpServletRequest request, String query) {
    _query = query;

    if( (_ausrCode = request.getParameter("ausrCode")) == null ) _ausrCode = "";
    if( (_ausrLastname = request.getParameter("ausrLastname")) == null ) _ausrLastname = "";
  }
}