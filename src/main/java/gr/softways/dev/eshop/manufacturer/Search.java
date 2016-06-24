package gr.softways.dev.eshop.manufacturer;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;

public class Search extends SearchBean {

  public Search() {
    super("manufact");
  }
  
  // �� �������� ���������� {
  private String _manufactId = "";

  public String getManufactId() {
    return _manufactId;
  }

  private String _manufactName = "";

  public String getManufactName() {
    return _manufactName;
  }
  // } �� �������� ����������
  
  /**
   * ������ �� queryDataSet �� �� rows
   * ��� ������� �� �������� ����������
   */
  protected synchronized DbRet parseTable(HttpServletRequest request) {
    String query = SwissKnife.buildQueryString(request,
                     "SELECT * FROM manufact ",
                     " ORDER BY manufactId",
                     new String[] {"manufactId","manufactName"},
                     new String[] {"", ""},
                     new String[] {"", ""},
                     new String[] {" manufactId = "," manufactNameUp LIKE "},
                     new String[] {"C","UP"},
                     new String[] {"'","'"},
                     new String[] {"'","%'"},
                     " WHERE", " AND", 2);

    // System.out.println(query);

    // save bean state
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
    if ( (_manufactId = request.getParameter("manufactId")) == null ) _manufactId = "";
    if ( (_manufactName = request.getParameter("manufactName")) == null ) _manufactName = "";
  }
}