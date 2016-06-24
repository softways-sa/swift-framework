package gr.softways.dev.eshop.usergroups;

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
    super("userGroups");
  }

  // �� �������� ���������� {
  private String _userGroupName = "";

  public String getUserGroupName() {
    return _userGroupName;
  }
  // } �� �������� ����������

  /**
   * ������ �� queryDataSet �� �� rows
   * ��� ������� �� �������� ����������
   */
  public synchronized DbRet parseTable(HttpServletRequest request) {
    String query = SwissKnife.buildQueryString(request,
                     "SELECT * FROM userGroups ",
                     " ORDER BY userGroupName",
                     new String[] {"userGroupName"},
                     new String[] {""},
                     new String[] {""},
                     new String[] {" userGroupName LIKE "},
                     new String[] {"C"},
                     new String[] {"'"},
                     new String[] {"%'"},
                     " WHERE", " AND ", 1);
    // System.out.println(query);

    // save bean state
    storeState(request, query);

    return fillQueryDataSet(query);
  }

  /**
   * ���������� ��� state.
   *
   */
  protected void storeState(HttpServletRequest request, String query) {
    _query = query;

    // ���������� ��� ����� ��� ��������� ����������
    if ( (_userGroupName = request.getParameter("userGroupName")) == null) _userGroupName = "";
  }
}