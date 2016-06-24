package gr.softways.dev.eshop.orders.v2;

import java.io.*;
import java.util.*;
import java.sql.Timestamp;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class AdminSearch extends SearchBean2 {

  public AdminSearch() {
    super("orders");
    
    setSortedByCol("orderDate");
    setSortedByOrder("DESC");
  }
  
  // Τα κριτήρια αναζήτησης {
  private String[] _orderStatus;

  public String[] getOrderStatus() {
    return _orderStatus;
  }
  
  public String getOrderStatus(int index) {
    return _orderStatus[index];
  }
  
  public boolean hasOrderStatus(String orderStatus) {
    boolean hasOrderStatus = false;
    
    int i=0;
    while (_orderStatus != null && i<_orderStatus.length) {
      if (_orderStatus[i].equals(orderStatus)) {
        hasOrderStatus = true;
        break;
      }
      
      i++;
    }
    
    return hasOrderStatus;
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

  private Timestamp _orderDateApo = null;
  
  private String _orderDateMonthEos = "";

  public String getOrderDateMonthEos() {
    return _orderDateMonthEos;
  }

  private String _orderDateYearEos = "";

  public String getOrderDateYearEos() {
    return _orderDateYearEos;
  }
  
  private Timestamp _orderDateEos = null;
  
  private String _email = "";

  public String getEmail() {
    return _email;
  }
  
  private String _lastname = "";

  public String getLastname() {
    return _lastname;
  }
  // } Τα κριτήρια αναζήτησης

  protected synchronized DbRet search(HttpServletRequest request) {
    DbRet dbRet = new DbRet();
    
    String s = null, select_clause = null, from_clause = null;
    
    if ( (s = request.getParameter("start")) == null ) s = "0";
    try {
      setStart(Integer.parseInt(s));
    }
    catch (Exception e) {
      setStart(0);
    }
    
    _orderStatus = request.getParameterValues("orderStatus");
    
    if ( (_orderId = request.getParameter("orderId")) == null ) _orderId = "";
    
    if ( (_orderDateDayApo = request.getParameter("orderDateDayApo")) == null ) _orderDateDayApo = "";
    if ( (_orderDateMonthApo = request.getParameter("orderDateMonthApo")) == null ) _orderDateMonthApo = "";
    if ( (_orderDateYearApo = request.getParameter("orderDateYearApo")) == null ) _orderDateYearApo = "";
    _orderDateApo = SwissKnife.buildTimestamp(_orderDateDayApo,_orderDateMonthApo,_orderDateYearApo, "0", "0", "0", "0");
    
    if ( (_orderDateDayEos = request.getParameter("orderDateDayEos")) == null ) _orderDateDayEos = "";
    if ( (_orderDateMonthEos = request.getParameter("orderDateMonthEos")) == null ) _orderDateMonthEos = "";
    if ( (_orderDateYearEos = request.getParameter("orderDateYearEos")) == null ) _orderDateYearEos = "";
    _orderDateEos = SwissKnife.buildTimestamp(_orderDateDayEos,_orderDateMonthEos,_orderDateYearEos, "0", "0", "0", "0");
    
    if ( (_email = request.getParameter("email")) == null ) _email = "";
    
    select_clause = "SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " *";
    
    from_clause = " FROM orders WHERE 1 = 1";
    
    if (_orderStatus != null && _orderStatus.length > 0) {
      from_clause += " AND (";
      
      for (int i=0; i<_orderStatus.length; i++) {
        from_clause += "status = '" + SwissKnife.sqlEncode(_orderStatus[i]) + "'";
        
        if ((i+1) < _orderStatus.length) from_clause += " OR ";
      }
      
      from_clause += ")";
    }
    
    if (_orderId.length()>0) {
      from_clause += " AND orderId = '" + SwissKnife.sqlEncode(_orderId) + "'";
    }
    
    if (_orderDateApo != null) {
      from_clause += " AND orderDate >= '" + _orderDateApo.toString() + "'";
    }
    
    if (_orderDateEos != null) {
      from_clause += " AND orderDate < '" + _orderDateEos.toString() + "'";
    }
    
    if (_email.length()>0) {
      from_clause += " AND email LIKE '%" + SwissKnife.sqlEncode(_email) + "%'";
    }
    
    //System.out.println(from_clause);
    
    parseTable(select_clause, from_clause);
    
    return dbRet;
  }
  
  protected void resetSearch() {
    setSortedByCol("orderDate");
    setSortedByOrder("DESC");
  }
}
