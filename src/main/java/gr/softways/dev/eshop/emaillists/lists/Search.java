package gr.softways.dev.eshop.emaillists.lists;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class Search extends JSPBean {

  /** Το query που εκτελέσθηκε την τελευταία φορά */
  private String _query = "";

  // Τα κριτήρια αναζήτησης {
  private String _emailListName = "";

  public String getEmailListName() {
    return _emailListName;
  }
  // } Τα κριτήρια αναζήτησης

  // Μεταβλητές για την σελίδα που έβλεπε την τελευταία φορά
  // ο χρήστης. {
  /** το πλήθος των rows */
  private int _rowCount = 0;

  public int getRowCount() {
    return _rowCount;
  }

  /** το πρώτο row της κάθε σελίδας */
  private int _startRow = 0;

  public int getStartRow() {
    return _startRow;
  }

  /** ο αριθμός της τρέχον σελίδας */
  private int _pageNum = 0;

  public int getPageNum() {
    return _pageNum;
  }

  /** πόσα rows σε κάθε σελίδα */
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
  // } Μεταβλητές για την σελίδα που έβλεπε την τελευταία φορά
  // ο χρήστης.

  public Search() {
  }

  public DbRet doAction(HttpServletRequest request) {
    DbRet dbRet = new DbRet();
    dbRet.setNoError(1);

    int auth = Director.getInstance().auth(databaseId, authUsername, authPassword,
                                           "emailListTab", AUTH_READ);

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
    else if ("UPDATE_SEARCH".equals(action)) {
      dbRet = updateParseTable(request, _query);
    }

     // μετακίνηση στο row
    if (queryDataSet.isOpen()) {
      goToRow(_startRow);
    }
    
    return dbRet;
  }

  /**
   * Γέμισε το queryDataSet με τα rows
   * που πληρούν τα κριτήρια αναζήτησης
   */
  private synchronized DbRet parseTable(HttpServletRequest request) {
    String query = SwissKnife.buildQueryString(request,
                   "SELECT * FROM emailListTab ",
                   " ORDER BY EMLTName",
                   new String[] {"emailListName"},
                   new String[] {""},
                   new String[] {""},
                   new String[] {" EMLTNameUp LIKE "},
                   new String[] {"UP"},
                   new String[] {"'"},
                   new String[] {"%'"},
                   " WHERE", " AND", 1);
                   
    // System.out.println(query);

    // save bean state
    storeState(request, query);

    return fillQueryDataSet(query);
  }

  /**
   * Αποθήκευση του state.
   *
   */
  private void storeState(HttpServletRequest request, String query) {
    _query = query;

    // αποθήκευσε τις τιμές των κρητιρίων αναζήτησης
    if( (_emailListName = request.getParameter("emailListName")) == null) _emailListName = "";

  }

  /**
   * Εκτέλεσε το αποθηκευμένο query.
   */
  private synchronized DbRet updateParseTable(HttpServletRequest request,
                                                        String query) {
    return fillQueryDataSet(query);
  }

  /**
   * Εκτέλεση query και <<γέμισμα>> QueryDataSet.
   *
   */
  private DbRet fillQueryDataSet(String query) {
    Director director = Director.getInstance();
    
    database = director.getDBConnection(databaseId);

    DbRet dbRet = null;

    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.retInt;

    try {
      // κλείσε το query ώστε να το τροποποιήσουμε
      if (queryDataSet.isOpen())
        queryDataSet.close();

      queryDataSet.setQuery(new QueryDescriptor(database, query, null, true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();

      _rowCount = queryDataSet.getRowCount();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }

    database.commitTransaction(1,prevTransIsolation);

    director.freeDBConnection(databaseId,database);

    return dbRet;
  }
}
