package gr.softways.dev.eshop.attribute;

import java.io.*;
import java.util.*;

import java.sql.Timestamp;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;


public class AdminRelAttSearch extends SearchBean2 {

  public AdminRelAttSearch() {
    super("attribute");
    
    setSortedByCol("atrName");
    setSortedByOrder("ASC");    
  }

  // parameters received {
  private String _SLAT_master_atrCode = "";

  public String getSLAT_master_atrCode() {
    return _SLAT_master_atrCode;
  }
  
  public void setSLAT_master_atrCode(String SLAT_master_atrCode) {
    _SLAT_master_atrCode = SLAT_master_atrCode;
  }
  

  private String _atrName = "";

  public String getatrName() {
    return _atrName;
  }
  
  public void setatrName(String atrName) {
    _atrName = atrName;
  }
  
  private String _searchatrName = "";

  public String getSearchatrName() {
    return _searchatrName;
  }
  
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
    
    storeState(request);
    
    select_clause = "SELECT FIRST " + getDispRows() + " SKIP " + getStart() + " * ";
    
    from_clause = " FROM attribute"
                + " WHERE atrCode <> '" + _SLAT_master_atrCode + "'" 
                + " AND atrCode NOT IN (SELECT SLAT_slave_atrCode FROM slaveAttribute"
                + " WHERE SLAT_master_atrCode='" + _SLAT_master_atrCode + "')";
    
    if (_searchatrName.length() > 0) {
      from_clause += " AND atrNameUp LIKE '%" + SwissKnife.searchConvert(SwissKnife.sqlEncode(_searchatrName)) + "%'";
    }
    
    parseTable(select_clause, from_clause);

    return dbRet;
  }  


  protected void storeState(HttpServletRequest request) {

    // αποθήκευσε τις τιμές των κριτηρίων αναζήτησης
    if ( (_searchatrName = request.getParameter("searchatrName")) == null ) _searchatrName = "";
  }
  
  protected void resetSearch() {
    setSortedByCol("atrName");
    setSortedByOrder("ASC");    
  }    
}