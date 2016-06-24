package gr.softways.dev.eshop.attr1;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;

public class Search extends SearchBean {

  public Search() {
    super("attributeTab");
  }
  
  // �� �������� ���������� {
  private String _attAttCode = "";

  public String getAttAttCode() {
    return _attAttCode;
  }
  
  private String _attName = "";

  public String getAttName() {
    return _attName;
  }
  // } �� �������� ����������
  
  private String _attPrdId = "";

  public void setAttPrdId(String attPrdId) {
    _attPrdId = attPrdId;
  }
  public String getAttPrdId() {
    return _attPrdId;
  }
  
  /**
   * ������ �� queryDataSet �� �� rows
   * ��� ������� �� �������� ����������
   */
  protected synchronized DbRet parseTable(HttpServletRequest request) {
    String select = "SELECT * FROM attributeTab WHERE"
                  + " attPrdId = '" + getAttPrdId() + "'";
    
    String query = SwissKnife.buildQueryString(request,
                     select,
                     " ORDER BY attAttCode",
                     new String[] {"attAttCode","attName"},
                     new String[] {"",""},
                     new String[] {"",""},
                     new String[] {" attAttCode = ", " attName LIKE "},
                     new String[] {"C","C"},
                     new String[] {"'","'"},
                     new String[] {"'","%'"},
                     " AND", " AND", 2);
                   
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
    if ( (_attAttCode = request.getParameter("attAttCode")) == null ) _attAttCode = "";
    if ( (_attName = request.getParameter("attName")) == null ) _attName = "";
  }
  
  protected void resetSearchCriteria() {
    _query = "";
    
    _attAttCode = "";
    _attName = "";
  }
}