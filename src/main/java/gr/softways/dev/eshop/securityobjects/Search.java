/*
 * Search.java
 *
 * Created on 15 Ιούλιος 2003, 10:16 πμ
 */

package gr.softways.dev.eshop.securityobjects;

import javax.servlet.http.*;

import gr.softways.dev.util.*;

/**
 *
 * @author  minotauros
 */
public class Search extends SearchBean {
  
  public Search() {
    super("securityObjects");
  }
  
  private String _SOObjectName = "";

  public String getSOObjectName() {
    return _SOObjectName;
  }
  
  protected synchronized DbRet parseTable(HttpServletRequest request) {
    String query = SwissKnife.buildQueryString(request,
                                               "SELECT * FROM securityObjects ",
                                               " ORDER BY SOObjectName",
                                               new String[] {"SOObjectName"},
                                               new String[] {""},
                                               new String[] {""},
                                               new String[] {" SOObjectName LIKE "},
                                               new String[] {"C"},
                                               new String[] {"'"},
                                               new String[] {"%'"},
                                               " WHERE", " AND", 1);
                                               
    storeState(request, query);

    setMaxRows(Integer.parseInt(Director.getInstance().getPoolAttr(databaseId + ".maxRows")));
    
    return fillQueryDataSet(query);
  }
  
  protected void storeState(HttpServletRequest request, String query) {
    _query = query;

    if ( (_SOObjectName = request.getParameter("SOObjectName")) == null) _SOObjectName = "";
  }
}