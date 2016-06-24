package gr.softways.dev.eshop.orders;

import java.io.*;
import java.util.*;
import java.sql.Timestamp;

import javax.servlet.http.*;

import gr.softways.dev.util.*;

public class AdminSearch extends SearchBean {

  public AdminSearch() {
    super("orders");
  }
  
  // Τα κριτήρια αναζήτησης {
  private String _orderStatus = "";

  public String getOrderStatus() {
    return _orderStatus;
  }
  
  private String _ordBankTran = "";
  
  public String getOrdBankTran(){
    return _ordBankTran;
  }
  
  private String _orderId = "";

  public String getOrderId() {
    return _orderId;
  }

  private String _orderDateDayApo = "";

  public String getOrderDateDayApo() {
    return _orderDateDayApo;
  }

  private String _orderDateMonthApo = "";

  public String getOrderDateMonthApo() {
    return _orderDateMonthApo;
  }

  private String _orderDateYearApo = "";

  public String getOrderDateYearApo() {
    return _orderDateYearApo;
  }

  private String _orderDateDayEos = "";

  public String getOrderDateDayEos() {
    return _orderDateDayEos;
  }

  private String _orderDateMonthEos = "";

  public String getOrderDateMonthEos() {
    return _orderDateMonthEos;
  }

  private String _orderDateYearEos = "";

  public String getOrderDateYearEos() {
    return _orderDateYearEos;
  }
  
  private String _email = "";

  public String getEmail() {
    return _email;
  }
  
  private String _lastname = "";

  public String getLastname() {
    return _lastname;
  }
  // } Τα κριτήρια Αναζήτησης
  
  /**
   * Γέμισε το queryDataSet με τα rows
   * που πληρούν τα κριτήρια αναζήτησης
   */
  protected synchronized DbRet parseTable(HttpServletRequest request) {
    String query = SwissKnife.buildQueryString(request,
                       "SELECT * FROM orders",
                       " ORDER BY orderDate DESC, orderId DESC",
                       new String[] {"ordBankTran","orderStatus","orderDateYearApo","orderDateYearEos","orderId","email","lastname"},
                       new String[] {"","","orderDateMonthApo","orderDateMonthEos","","",""},
                       new String[] {"","","orderDateDayApo","orderDateDayEos","","",""},
                       new String[] {" ordBankTran = "," status = "," orders.orderDate >= "," orders.orderDate < ", " orders.orderId = "," orders.email LIKE ", " orders.lastname LIKE "},
                       new String[] {"C","C","D","D","C","C","C"},
                       new String[] {"'","'","'","'","'","'%","'%"},
                       new String[] {"'","'","'","'","'","%'","%'"},
                       " WHERE ", " AND ", 7);
    
    // save bean state
    storeState(request, query);
    
    setMaxRows(Integer.parseInt(Director.getInstance().getPoolAttr(databaseId + ".maxRows")));
    
    return fillQueryDataSet(query);
  }

  /**
   * Αποθήκευση του state.
   *
   */
  protected void storeState(HttpServletRequest request, String query) {
    _query = query;

    // αποθήκευσε τις τιμές των κριτηρίων αναζήτησης
    if ( (_orderStatus = request.getParameter("orderStatus")) == null ) _orderStatus = "";
    if ( (_ordBankTran = request.getParameter("ordBankTran")) == null ) _ordBankTran = "";
    if ( (_orderId = request.getParameter("orderId")) == null ) _orderId = "";
    
    if ( (_orderDateDayApo = request.getParameter("orderDateDayApo")) == null ) _orderDateDayApo = "";
    if ( (_orderDateMonthApo = request.getParameter("orderDateMonthApo")) == null ) _orderDateMonthApo = "";
    if ( (_orderDateYearApo = request.getParameter("orderDateYearApo")) == null ) _orderDateYearApo = "";
    
    if ( (_orderDateDayEos = request.getParameter("orderDateDayEos")) == null ) _orderDateDayEos = "";
    if ( (_orderDateMonthEos = request.getParameter("orderDateMonthEos")) == null ) _orderDateMonthEos = "";
    if ( (_orderDateYearEos = request.getParameter("orderDateYearEos")) == null ) _orderDateYearEos = "";
    
    if ( (_email = request.getParameter("email")) == null ) _email = "";
    if ( (_lastname = request.getParameter("lastname")) == null ) _lastname = "";
  }
}