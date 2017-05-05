package gr.softways.dev.eshop.facet;

import javax.servlet.http.*;
import gr.softways.dev.util.*;

public class FacetValueAdminSearch extends SearchBean2 {

  public FacetValueAdminSearch() {
    super("facet_values");

    setSortedByCol("facet_id");
    setSortedByOrder("ASC");
  }

  // Τα κριτήρια αναζήτησης {
  private String name = "";

  public String getName() {
    return name;
  }
  // } Τα κριτήρια αναζήτησης

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

    if ((name = request.getParameter("name")) == null) {
      name = "";
    }

    select_clause = "SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " facet_values.*, facet.name AS facet_name";

    from_clause = " FROM facet_values JOIN facet ON facet_values.facet_id = facet.id";

    if (name.length() > 0) {
      from_clause += " AND facet_values.name CONTAINING '" + SwissKnife.sqlEncode(name) + "'";
    }

    parseTable(select_clause, from_clause);

    return dbRet;
  }

  protected void resetSearch() {
    setSortedByCol("facet_id");
    setSortedByOrder("ASC");
  }
}
