package gr.softways.dev.epayment;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class Search extends SearchBean2 {

  public Search() {
    super("EPayment");
    
    setSortedByCol("PAYNT_PayDate");
    setSortedByOrder("DESC");
  }
  
  // Τα κριτήρια αναζήτησης {
  private String _PAYNT_Code = "";

  public String getPAYNT_Code() {
    return _PAYNT_Code;
  }
  
  private String _PAYNT_BankPayStatus = "";

  public String getPAYNT_BankPayStatus() {
    return _PAYNT_BankPayStatus;
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
    
    if ( (_PAYNT_Code = request.getParameter("PAYNT_Code")) == null ) _PAYNT_Code = "";
    if ( (_PAYNT_BankPayStatus = request.getParameter("PAYNT_BankPayStatus")) == null ) _PAYNT_BankPayStatus = "";
    
    select_clause = "SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " *";
    
    from_clause = " FROM EPayment WHERE 1 = 1";
    
    if (_PAYNT_Code.length()>0) {
      from_clause += " AND PAYNT_Code = '" + SwissKnife.sqlEncode(_PAYNT_Code) + "'";
    }
    
    if (_PAYNT_BankPayStatus.length()>0) {
      from_clause += " AND PAYNT_BankPayStatus = '" + SwissKnife.sqlEncode(_PAYNT_BankPayStatus) + "'";
    }
    
    //System.out.println(from_clause);
    
    parseTable(select_clause, from_clause);
    
    return dbRet;
  }
  
  protected void resetSearch() {
    setSortedByCol("PAYNT_PayDate");
    setSortedByOrder("DESC");
  }
}