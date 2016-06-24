package gr.softways.dev.eshop.customer.v2;

import java.io.*;
import java.util.*;
import java.sql.Timestamp;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class AdminSearch extends SearchBean2 {

  public AdminSearch() {
    super("customer");
    
    setSortedByCol("lastnameUp ASC, firstnameUp");
    setSortedByOrder("ASC");
  }
  
  // Τα κριτήρια αναζήτησης {
  private String _lastname = "";

  public String getLastname() {
    return _lastname;
  }

  private String _email = "";

  public String getEmail() {
    return _email;
  }
  
  private String _customerType = "";

  public String getCustomerType() {
    return _customerType;
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
    
    if ( (_lastname = request.getParameter("lastname")) == null ) _lastname = "";
    if ( (_email = request.getParameter("email")) == null ) _email = "";
    if ( (_customerType = request.getParameter("customerType")) == null ) _customerType = "";
    
    select_clause = "SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " *";
    
    from_clause = " FROM customer LEFT JOIN shipBillInfo ON customerId = SBCustomerId LEFT JOIN users ON custLogCode = logCode LEFT JOIN userGroups ON usrAccessLevel = userGroupId WHERE 1 = 1";
    
    if (_lastname.length()>0) {
      from_clause += " AND customer.lastnameUp LIKE '" + SwissKnife.searchConvert(SwissKnife.sqlEncode(_lastname)) + "%'";
    }
    
    if (_email.length()>0) {
      from_clause += " AND customer.email LIKE '%" + SwissKnife.sqlEncode(_email) + "%'";
    }
    
    if (_customerType.length()>0) {
      from_clause += " AND customer.customerType = '" + SwissKnife.sqlEncode(_customerType) + "'";
    }
    
    //System.out.println(select_clause + from_clause);
    parseTable(select_clause, from_clause);
    
    return dbRet;
  }
  
  protected void resetSearch() {
    setSortedByCol("lastnameUp ASC, firstnameUp");
    setSortedByOrder("ASC");
  }
}