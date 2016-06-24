package gr.softways.dev.eshop.category;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;

public class Search extends SearchBean {

  public Search() {
    super("prdCategory");
  }

  // �� �������� ���������� {
  private String _catId = "";

  public String getCatId() {
    return _catId;
  }

  private String _catName = "";

  public String getCatName() {
    return _catName;
  }
  // } �� �������� ����������

  /**
    *  ������ �� queryDataSet �� �� rows
    * ��� ������� �� �������� ����������
   */
  protected synchronized DbRet parseTable(HttpServletRequest request) {
    String query = SwissKnife.buildQueryString(request,
                     "SELECT * FROM prdCategory ",
                     " ORDER BY catId",
                     new String[] {"catId","catName"},
                     new String[] {"", ""},
                     new String[] {"", ""},
                     new String[] {" catId LIKE "," catNameUp LIKE "},
                     new String[] {"C","UP"},
                     new String[] {"'","'"},
                     new String[] {"%'","%'"},
                     " WHERE", " AND", 2);

    //System.out.println(query);

    storeState(request, query);

    setMaxRows(Integer.parseInt(Director.getInstance().getPoolAttr(databaseId + ".maxRows")));
    
    return fillQueryDataSet(query);
  }

  /**
   * ���������� ��� state.
   *
   */
  protected void storeState(HttpServletRequest request, String query) {
    _query = query;

    // ���������� ��� ����� ��� ��������� ����������
    if ( (_catId = request.getParameter("catId")) == null ) _catId = "";
    if ( (_catName = request.getParameter("catName")) == null ) _catName = "";
  }
}