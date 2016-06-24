package gr.softways.dev.eshop.emaillists.newsletter;

import java.io.*;
import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

import gr.softways.dev.eshop.emaillists.lists.Present;

public class NewsletterServlet2 extends HttpServlet {

  private Director _director;
  
  private String _charset = null;
  
  private String _databaseId = null;
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    _databaseId = SwissKnife.jndiLookup("swconf/databaseId");
    
    _director = Director.getInstance();
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    PrintWriter out = null;
    
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    DbRet dbRet = new DbRet();
    
    String action = request.getParameter("cmd") == null ? "" : request.getParameter("cmd");

    if (action.equals("subscribe")) {
      dbRet = doSubscribe(request, _databaseId);
    }
    else if (action.equals("unsubscribe")) {
      dbRet = doUnsubscribe(request, _databaseId);
    }
    else {
      dbRet.setNoError(0);
    }
    
    try {
      out = response.getWriter();

      out.println(dbRet.getNoError());

      out.close();
    }
    catch (Exception e) {
    }
  }
  
  private DbRet doSubscribe(HttpServletRequest request, String databaseId) {
    DbRet dbRet = null;
    
    String EMLMEmail = request.getParameter("EMLMEmail"),
           EMLMAltEmail = request.getParameter("EMLMAltEmail"),
           EMLMLastName = request.getParameter("EMLMLastName"),
           EMLMLastNameUp = SwissKnife.searchConvert(EMLMLastName),
           EMLMFirstName = request.getParameter("EMLMFirstName"),
           EMLMActive = Present.STATUS_ACTIVE,
           EMLMAddress = request.getParameter("EMLMAddress"),
           EMLMZipCode = request.getParameter("EMLMZipCode"),
           EMLMCity = request.getParameter("EMLMCity"),
           EMLMCompanyName = request.getParameter("EMLMCompanyName"),
           EMLMCompanyNameUp = SwissKnife.searchConvert(EMLMCompanyName),
           EMLMCountry = request.getParameter("EMLMCountry"),
           EMLMPhone = request.getParameter("EMLMPhone"),
           EMLMField1 = request.getParameter("EMLMField1"),
           EMLMField2 = request.getParameter("EMLMField2"),
           EMLMField3 = request.getParameter("EMLMField3");
           
    Timestamp EMLMBirthDate = null, EMLMRegDate = SwissKnife.currentDate();

    EMLMBirthDate = SwissKnife.buildTimestamp(request.getParameter("EMLMBirthDateDay"),
                                              request.getParameter("EMLMBirthDateMonth"),
                                              request.getParameter("EMLMBirthDateYear"));
                                              
    String receiveEmail = Present.STATUS_ACTIVE, EMLRListCode = "";
    
    String newsletterListCode = request.getParameter("id");
    
    // get database connection
    Database database = _director.getDBConnection(databaseId);

    // begin transaction
    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);
    
    int prevTransIsolation = dbRet.getRetInt();
    
    dbRet = Newsletter.getDefEMLRListCode(database,newsletterListCode);
    if (dbRet.getNoError() == 1) {
      EMLRListCode = dbRet.getRetStr();
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = Newsletter.insert(database,EMLMEmail,EMLMAltEmail,EMLMLastName,EMLMLastNameUp,EMLMFirstName,EMLMBirthDate,
                                EMLMRegDate,EMLMCompanyName,EMLMCompanyNameUp,EMLMAddress,EMLMZipCode,EMLMCity,EMLMCountry,
                                EMLMPhone,EMLMActive,EMLMField1,EMLMField2,EMLMField3,EMLRListCode,receiveEmail);
    }
    
    // End transaction (commit or rollback)
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    _director.freeDBConnection(databaseId,database);

    return dbRet;
  }
  
  private DbRet doUnsubscribe(HttpServletRequest request, String databaseId) {
    DbRet dbRet = null;
    
    String EMLMEmail = request.getParameter("EMLMEmail");
    
    String receiveEmail = Present.STATUS_UNREGISTERED, EMLRListCode = "", EMLMCode = "";
    
    String newsletterListCode = request.getParameter("id");
    
    // get database connection
    Database database = _director.getDBConnection(databaseId);

    // begin transaction
    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);
    
    int prevTransIsolation = dbRet.getRetInt();
    
    dbRet = Newsletter.getDefEMLRListCode(database,newsletterListCode);
    if (dbRet.getNoError() == 1) {
      EMLRListCode = dbRet.getRetStr();
    }
    
    if (dbRet.getNoError() == 1) {
      String query = "SELECT EMLMCode FROM emailListMember WHERE EMLMEmail = '" + SwissKnife.sqlEncode(EMLMEmail) + "'";
    
      QueryDataSet queryDataSet = null;
      
      try {
        queryDataSet = new QueryDataSet();
        
        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
        
        queryDataSet.refresh();
        
        if (queryDataSet.isEmpty() == true) throw new Exception();
  
        EMLMCode = queryDataSet.getString("EMLMCode");
      }
      catch (Exception e) {
        dbRet.setNoError(0);
      }
      finally {
        if (queryDataSet != null) queryDataSet.close();
      }
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = Newsletter.updateSubscription(database,EMLRListCode,EMLMCode,receiveEmail);;
    }
    
    // End transaction (commit or rollback)
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    _director.freeDBConnection(databaseId,database);

    return dbRet;
  }
}