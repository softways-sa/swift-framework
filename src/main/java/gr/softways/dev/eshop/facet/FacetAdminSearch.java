package gr.softways.dev.eshop.facet;

import javax.servlet.http.*;
import gr.softways.dev.util.*;

public class FacetAdminSearch extends SearchBean2 {

  public FacetAdminSearch() {
    super("facet");

    setSortedByCol("display_order");
    setSortedByOrder("ASC");
  }

  protected synchronized DbRet search(HttpServletRequest request) {
    DbRet dbRet = new DbRet();

    String s = null, select_clause = null, from_clause = null;

    if ((s = request.getParameter("start")) == null) {
      s = "0";
    }

    try {
      setStart(Integer.parseInt(s));
    }
    catch (Exception e) {
      setStart(0);
    }

    select_clause = "SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " *";

    from_clause = " FROM facet";

    //System.out.println(from_clause);
    parseTable(select_clause, from_clause);

    return dbRet;
  }

  protected void resetSearch() {
    setSortedByCol("display_order");
    setSortedByOrder("ASC");
  }
}
