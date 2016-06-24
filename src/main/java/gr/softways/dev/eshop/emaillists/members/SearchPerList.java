/*
 * searchperlist.java
 *
 * Created on 12 ��������� 2001, 10:04 ��
 */

package gr.softways.dev.eshop.emaillists.members;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;

/**
 *
 * @author  Administrator
 * @version 
 */
public class SearchPerList extends SearchBean {
  
  public SearchPerList() {
    super("emailListMember");
  }
  
  // �� �������� ���������� {
  private String _EMLMEmail = "";

  public String getEMLMEmail() {
    return _EMLMEmail;
  }
  
  private String _EMLTCode = "";

  public String getEMLTCode() {
    return _EMLTCode;
  }
  // } �� �������� ����������

  /**
   * ������ �� queryDataSet �� �� rows
   * ��� ������� �� �������� ����������
   */
  protected synchronized DbRet parseTable(HttpServletRequest request) {
    String query = SwissKnife.buildQueryString(request,
                       "SELECT * FROM emailListMember,emailListTab,emailListReg" 
                     + " WHERE EMLRListCode = EMLTCode"
                     + " AND EMLRMemberCode = EMLMCode",
                     " ORDER BY EMLMEmail",
                     new String[] {"EMLMEmail","EMLTCode"},
                     new String[] {"", ""},
                     new String[] {"", ""},
                     new String[] {" EMLMEmail LIKE ", " EMLTCode = "},
                     new String[] {"C","C"},
                     new String[] {"'%","'"},
                     new String[] {"%'","'"},
                     " AND", " AND", 2);

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
    if ( (_EMLMEmail = request.getParameter("EMLMEmail")) == null) _EMLMEmail =  "";
    if ( (_EMLTCode = request.getParameter("EMLTCode")) == null) _EMLTCode = "";
  }  
}