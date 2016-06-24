package gr.softways.dev.eshop.shippricelist;

import java.io.*;
import java.util.*;
import java.sql.Timestamp;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class AdminSearch extends SearchBean2 {

  public AdminSearch() {
    super("ShipCostEntry");
    
    setSortedByCol("countryName");
    setSortedByOrder("ASC");
  }
  
  // Τα κριτήρια αναζήτησης {
  private String _SHCE_countryCode = "";

  public String getSHCE_countryCode() {
    return _SHCE_countryCode;
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
    
    if ( (_SHCE_countryCode = request.getParameter("SHCE_countryCode")) == null ) _SHCE_countryCode = "";
    
    select_clause = "SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " *";
    
    from_clause = " FROM ShipCostEntry LEFT JOIN ShipCostRange ON SHCE_SHCRCode = SHCRCode LEFT JOIN country ON SHCE_countryCode = countryCode LEFT JOIN ShipCostMethod ON SHCE_SHCMCode = SHCMCode"
                + " WHERE 1 = 1";
    
    if (_SHCE_countryCode.length()>0) {
      from_clause += " AND SHCE_countryCode = '" + SwissKnife.sqlEncode(_SHCE_countryCode) + "'";
    }
    
    parseTable(select_clause, from_clause);
    
    return dbRet;
  }
  
  protected void resetSearch() {
    setSortedByCol("countryName");
    setSortedByOrder("ASC");
  }
}