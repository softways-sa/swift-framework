package gr.softways.dev.eshop.filetemplate;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;

public class Search extends SearchBean {

  public Search() {
    super("fileTemplate");
  }

  // Τα κριτήρια αναζήτησης {
  private String _FTemName = "";

  public String getFTemName() {
    return _FTemName;
  }

  private String _FTemFilename = "";

  public String getFTemFilename() {
    return _FTemFilename;
  }

  private String _FTemTablename = "";

  public String getFTemTablename() {
    return _FTemTablename;
  }
  // } Τα κριτήρια αναζήτησης

  /**
   * Γέμισε το queryDataSet με τα rows
   * που πληρούν τα κριτήρια αναζήτησης
   */
  protected synchronized DbRet parseTable(HttpServletRequest request) {
    String query = SwissKnife.buildQueryString(request,
                     "SELECT * FROM fileTemplate ",
                     " ORDER BY FTemName",
                     new String[] {"FTemName","FTemFilename","FTemTablename"},
                     new String[] {"","",""},
                     new String[] {"","",""},
                     new String[] {" FTemNameUp LIKE "," FTemFilename LIKE "," FTemTablename LIKE "},
                     new String[] {"UP","C","C"},
                     new String[] {"'%","'%","'%"},
                     new String[] {"%'","%'","%'"},
                     " WHERE", " AND", 3);

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

    // αποθήκευσε τις τιμές των κριτηρίων αναζήτησης
    if ( (_FTemName = request.getParameter("FTemName")) == null) _FTemName = "";
    if ( (_FTemFilename = request.getParameter("FTemFilename")) == null) _FTemFilename = "";
    if ( (_FTemTablename = request.getParameter("FTemTablename")) == null) _FTemTablename = "";
  }
}