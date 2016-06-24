package gr.softways.dev.eshop.securityobjects;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class SearchGrpSO extends JSPBean{

  private String _query = "";

  // �� �������� ���������� {
  private String _userGroupId = "";

  public String getUserGroupId() {
    return _userGroupId;
  }
  // } �� �������� ����������

  private int _rowCount = 0;

  public int getRowCount() {
    return _rowCount;
  }

  /** �� ����� row ��� ���� ������� */
  private int _startRow = 0;

  public int getStartRow() {
    return _startRow;
  }

  /** � ������� ��� ������ ������� */
  private int _pageNum = 0;

  public int getPageNum() {
    return _pageNum;
  }

  /** ���� rows �� ���� ������ */
  private int _dispRows = 10;

  public void setDispRows(int dispRows) {
    _dispRows = dispRows;
  }

  public int getDispRows() {
    return _dispRows;
  }

  /** group page */
  private int _groupPages = 0;

  public int getGroupPages() {
    return _groupPages;
  }
  // } ���������� ��� ��� ������ ��� ������ ��� ��������� ����
  // � �������.

  public SearchGrpSO() {
  }

  public DbRet doAction(HttpServletRequest request) {
    DbRet dbRet = new DbRet();
    dbRet.setNoError(1);

    int auth = Director.getInstance().auth(databaseId, authUsername, authPassword,
                                           "securityObjects", AUTH_READ);

    if (auth < 0) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);

      return dbRet;
    }

    String action = request.getParameter("action1") == null ? "" :  request.getParameter("action1");

    if (request.getParameter("sr") != null && !request.getParameter("sr").equals("")) {
      _startRow = Integer.parseInt(request.getParameter("sr"));
    }
    if (request.getParameter("p") != null && !request.getParameter("p").equals("")) {
      _pageNum = Integer.parseInt(request.getParameter("p"));
    }
    if (request.getParameter("gp") != null && !request.getParameter("gp").equals("")) {
      _groupPages = Integer.parseInt(request.getParameter("gp"));
    }
    if (request.getParameter("dr") != null && !request.getParameter("dr").equals("")) {
      _dispRows = Integer.parseInt(request.getParameter("dr"));
    }

    if ("SEARCH".equals(action)) {
      dbRet = parseTable(request);

      _startRow = 0;
      _pageNum = 1;
      _groupPages = 0;
    }
    else if ("UPDATE_SEARCH".equals(action) && _query.length() > 0) {
      dbRet = updateSearch(request, _query);
    }

     // ���������� ��� row
    if (queryDataSet.isOpen()) {
      goToRow(_startRow);
    }

    return dbRet;
  }

  private synchronized DbRet parseTable(HttpServletRequest request) {

   String query = SwissKnife.buildQueryString(request,
                    "SELECT * FROM securityObjects"
                  + " WHERE SOObjectName ",
                    " ORDER BY SOObjectName",
                    new String[] {"userGroupId"},
                    new String[] {""},
                    new String[] {""},
                    new String[] {" NOT IN (SELECT SPObject FROM securityPolicy WHERE SPId = "},
                    new String[] {"N"},
                    new String[] {""},
                    new String[] {")"},
                    " ", " AND", 1);

    //System.out.println(query);

    storeState(request, query);

    return fillQueryDataSet(query);
  }

  private void storeState(HttpServletRequest request, String query) {
    _query = query;

    if ( (_userGroupId = request.getParameter("userGroupId")) == null) _userGroupId = "";

  }

  private synchronized DbRet updateSearch(HttpServletRequest request,
                                          String query) {
    return fillQueryDataSet(query);
  }

  /**
   * �������� query ��� <<�������>> QueryDataSet.
   *
   */
  private DbRet fillQueryDataSet(String query) {
    
    Director director = Director.getInstance();
    
    database = director.getDBConnection(databaseId);
    
    DbRet dbRet = null;

    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE); 

    int prevTransIsolation = dbRet.retInt;

    try {
      // ������ �� query ���� �� �� ��������������
      if (queryDataSet.isOpen())
        queryDataSet.close();

      queryDataSet.setQuery(new QueryDescriptor(database, query, null, 
                                                true, Load.ALL));
      
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();

      _rowCount = queryDataSet.getRowCount();
    }
    catch (Exception e) {
      dbRet.noError = 0;
      e.printStackTrace();
    }

    database.commitTransaction(1,prevTransIsolation);

    director.freeDBConnection(databaseId,database);

    return dbRet;
  }

}