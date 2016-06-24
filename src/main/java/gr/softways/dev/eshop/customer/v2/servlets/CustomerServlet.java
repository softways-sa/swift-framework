package gr.softways.dev.eshop.customer.v2.servlets;

import java.io.*;
import java.util.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

import gr.softways.dev.eshop.emaillists.lists.Present;
import gr.softways.dev.eshop.emaillists.newsletter.Newsletter;

import gr.softways.dev.eshop.customer.v2.Customer;

public class CustomerServlet extends HttpServlet {

  private Director _director;
  
  private String _charset = null;

  private BigDecimal _zero = new BigDecimal("0");
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    _director = Director.getInstance();
  }

  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
         throws ServletException, IOException {
    DbRet dbRet = new DbRet();
    
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1"),
           databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId"),
           urlSuccess = request.getParameter("urlSuccess") == null ? "" :  request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");

    if (databaseId.equals("")) {
      dbRet.setNoError(0);
    }
    else if (action.equals("INSERT")) {
      dbRet = doAdminInsert(request, databaseId);
    }
    else if (action.equals("UPDATE")) {
      dbRet = doAdminUpdate(request, databaseId);
    }
    else if (action.equals("DELETE")) {
      dbRet = doAdminDelete(request, databaseId);
    }
    else {
      dbRet.setNoError(0);
    }
    
    if (dbRet.getNoError() == 1) {
       response.sendRedirect(urlSuccess);
    }
    else {
      if (dbRet.getAuthError() == 1) {
        response.sendRedirect(urlNoAccess + "?authError=" + dbRet.getAuthErrorCode());
      }
      else if (dbRet.get_validError() == 1) {
        response.sendRedirect(urlFailure + "?validField=" + dbRet.getRetStr() + "&validError=" + dbRet.get_validErrorCode());
      }
      else if (dbRet.getDbErrorCode() == 1) {
        response.sendRedirect(urlFailure + "?dbMethod=" + dbRet.getRetStr() + "&dbError=" + dbRet.getDbErrorCode());
      }  
      else {
         response.sendRedirect(urlFailure);
      }
    }
  }

  private DbRet doAdminInsert(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"customer",Director.AUTH_INSERT);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String localeLanguage = SwissKnife.sqlEncode(request.getParameter("localeLanguage")),
           localeCountry = SwissKnife.sqlEncode(request.getParameter("localeCountry"));
    
    String usrPasswd = SwissKnife.sqlEncode(request.getParameter("usrPasswd")),
           usrAccessLevel = SwissKnife.sqlEncode(request.getParameter("usrAccessLevel")),
           lastIpUsed = "";
           
    if (usrPasswd.trim().length()>0) {
      usrPasswd = Crypto.encrypt(usrPasswd);
    }
    
    Timestamp dateLastUsed = null;
    
    String customerId = null, custLogCode = null,
           firstname = SwissKnife.sqlEncode(request.getParameter("firstname")),
           firstnameUp = SwissKnife.searchConvert(firstname),
           lastname = SwissKnife.sqlEncode(request.getParameter("lastname")),
           lastnameUp = SwissKnife.searchConvert(lastname),
           email = SwissKnife.sqlEncode(request.getParameter("email")).toLowerCase(),
           occupation = SwissKnife.sqlEncode(request.getParameter("occupation")),
           title = SwissKnife.sqlEncode(request.getParameter("title")),
           sex = SwissKnife.sqlEncode(request.getParameter("sex")),
           phone = SwissKnife.sqlEncode(request.getParameter("phone")),
           fax = SwissKnife.sqlEncode(request.getParameter("fax")),
           contactUsDescr = SwissKnife.sqlEncode(request.getParameter("contactUsDescr")),
           custLang = SwissKnife.sqlEncode(request.getParameter("custLang")),
           receiveEmail = SwissKnife.sqlEncode(request.getParameter("receiveEmail")),
           lockedAccnt = SwissKnife.sqlEncode(request.getParameter("lockedAccnt"));
    
    String custText = request.getParameter("custText"),
        custTextLG = request.getParameter("custTextLG");
    
    BigDecimal purchaseVal = _zero,
               purchaseValEU = _zero,
               purchaseValCUR1 = _zero,
               purchaseValCUR2 = _zero,
               discountPct = SwissKnife.parseBigDecimal(request.getParameter("discountPct"),localeLanguage,localeCountry);
               
    if (discountPct == null) discountPct = _zero;
    
    Timestamp dateCreated = SwissKnife.currentDate(),
              birthDate = SwissKnife.buildTimestamp(request.getParameter("birthDateDay"),
                                                    request.getParameter("birthDateMonth"),
                                                    request.getParameter("birthDateYear"));
    String hotdealBuysCnt = "0", buysCnt = "0",
           customerType = null, contactUsId = null;
           
    if ((customerType = request.getParameter("customerType")) == null) customerType = "0";
    if ((contactUsId = request.getParameter("contactUsId")) == null) contactUsId = "0";
    
    String SBCustomerId = null, 
           SBLangBilling = SwissKnife.sqlEncode(request.getParameter("SBLangBilling")),
           SBNameBilling = SwissKnife.sqlEncode(request.getParameter("SBNameBilling")),
           SBProfessionBilling = SwissKnife.sqlEncode(request.getParameter("SBProfessionBilling")),
           SBAddressBilling = SwissKnife.sqlEncode(request.getParameter("SBAddressBilling")),
           SBAreaCodeBilling = SwissKnife.sqlEncode(request.getParameter("SBAreaCodeBilling")),
           SBCityBilling = SwissKnife.sqlEncode(request.getParameter("SBCityBilling")),
           SBRegionBilling = SwissKnife.sqlEncode(request.getParameter("SBRegionBilling")),
           SBCountryCodeBilling = SwissKnife.sqlEncode(request.getParameter("SBCountryCodeBilling")),
           SBZipCodeBilling = SwissKnife.sqlEncode(request.getParameter("SBZipCodeBilling")),
           SBPhoneBilling = SwissKnife.sqlEncode(request.getParameter("SBPhoneBilling")),
           SBFaxBilling = SwissKnife.sqlEncode(request.getParameter("SBFaxBilling")),
           SBEmailBilling = SwissKnife.sqlEncode(request.getParameter("SBEmailBilling")),
           SBAfmBilling = SwissKnife.sqlEncode(request.getParameter("SBAfmBilling")),
           SBDoyBilling = SwissKnife.sqlEncode(request.getParameter("SBDoyBilling")),
           SBCreditTypeBilling = SwissKnife.sqlEncode(request.getParameter("SBCreditTypeBilling")),
           SBCreditNumBilling = SwissKnife.sqlEncode(request.getParameter("SBCreditNumBilling")),
           SBCreditOwnerBilling = SwissKnife.sqlEncode(request.getParameter("SBCreditOwnerBilling"));
    
    Timestamp SBCreditExpDayBilling = SwissKnife.buildTimestamp(request.getParameter("SBCreditExpDayBillingDay"),
                                                                request.getParameter("SBCreditExpDayBillingMonth"),
                                                                request.getParameter("SBCreditExpDayBillingYear"));
                                                                
    if (localeLanguage.equals("") || localeCountry.equals("")
          || usrPasswd == null || usrPasswd.equals("")
          || usrAccessLevel.equals("") || email.equals("")
          || SBCountryCodeBilling.equals("")) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    if (dbRet.getNoError() == 1) {
      dbRet = Customer.newUser(database,email,usrPasswd,usrAccessLevel,lastIpUsed,dateLastUsed);
      custLogCode = dbRet.getRetStr();
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = Customer.newCustomer(database,custLogCode,dateCreated,dateLastUsed,
                                   firstname,firstnameUp,lastname,lastnameUp,occupation,
                                   email,phone,fax,title,sex,discountPct,
                                   customerType,contactUsId,contactUsDescr,
                                   birthDate,purchaseVal,purchaseValEU,purchaseValCUR1,
                                   purchaseValCUR2,hotdealBuysCnt,buysCnt,custLang,
                                   receiveEmail,lockedAccnt,custText,custTextLG);
      customerId = SBCustomerId = dbRet.getRetStr();
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = Customer.newShipBillInfo(database,SBCustomerId,gr.softways.dev.eshop.eways.Customer.BILLING_INFO,
                                       SBNameBilling,SBProfessionBilling,
                                       SBAddressBilling,SBAreaCodeBilling,
                                       SBCityBilling,SBRegionBilling,
                                       SBCountryCodeBilling,SBZipCodeBilling,
                                       SBPhoneBilling,SBFaxBilling,SBEmailBilling,
                                       SBAfmBilling,SBDoyBilling,SBCreditTypeBilling,
                                       SBCreditExpDayBilling,SBCreditNumBilling,
                                       SBCreditOwnerBilling,SBLangBilling);
    }
    
    /**if (dbRet.getNoError() == 1) {
      dbRet = Customer.newCustMonthly(database,customerId);
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = Customer.newCustZones(database,customerId);
    }**/
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  private DbRet doAdminDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"customer",Director.AUTH_DELETE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String logCode = SwissKnife.sqlEncode(request.getParameter("logCode")),
           customerId = SwissKnife.sqlEncode(request.getParameter("customerId"));
    
    if (logCode.equals("") || customerId.equals("")) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    String query = null, receiveEmail = "0", EMLRListCode = "", EMLRMemberCode = "";
    
    EMLRListCode = Newsletter.getDefEMLRListCode(database, "NEWSLETTER").getRetStr();
    
    if (EMLRListCode != null && EMLRListCode.length() > 0) {
      QueryDataSet queryDataSet = null;

      query = "SELECT EMLRMemberCode FROM emailListReg,emailListMember,Customer WHERE email = EMLMEmail and EMLRMemberCode = EMLMCode"
          + " AND customerId = '" + customerId + "'"
          + " AND EMLRListCode = '" + EMLRListCode + "'";

      try {
        queryDataSet = new QueryDataSet();
        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

        queryDataSet.refresh();

        if (queryDataSet.getRowCount() > 0) EMLRMemberCode = queryDataSet.getString("EMLRMemberCode");
      }
      catch (Exception e) {
        dbRet.setNoError(0);

        e.printStackTrace();
      }
      finally {
        if (queryDataSet != null) queryDataSet.close();
      }
    }

    if (dbRet.getNoError() == 1 && EMLRMemberCode.length() > 0) {
      dbRet = Newsletter.updateListSubscription(database,EMLRListCode,EMLRMemberCode,receiveEmail);
    }
    
    /**query = "DELETE FROM custMonthly"  
          + " WHERE custMCustomerId = '" + customerId + "'";

    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(query);
    }

    query = "DELETE FROM custZones"
          + " WHERE custZCustomerId = '" + customerId + "'";

    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(query);
    }**/

    query = "DELETE FROM shipBillInfo" 
          + " WHERE SBCustomerId = '" + customerId + "'";

    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(query);
    }
    
    query = "DELETE FROM customer" 
          + " WHERE customerId = '" + customerId + "'";
    
    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(query);
    }
    
    query = "DELETE FROM users"
          + " WHERE logCode = '" + logCode + "'";
    
    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(query);
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  private DbRet doAdminUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"customer",Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String localeLanguage = SwissKnife.sqlEncode(request.getParameter("localeLanguage")),
           localeCountry = SwissKnife.sqlEncode(request.getParameter("localeCountry"));
    
    String logCode = SwissKnife.sqlEncode(request.getParameter("logCode")),
           usrPasswd = SwissKnife.sqlEncode(request.getParameter("usrPasswd")),
           usrAccessLevel = SwissKnife.sqlEncode(request.getParameter("usrAccessLevel"));
           
    if (usrPasswd.trim().length()>0) {
      usrPasswd = Crypto.encrypt(usrPasswd);
    }
    
    String customerId = SwissKnife.sqlEncode(request.getParameter("customerId")),
           firstname = SwissKnife.sqlEncode(request.getParameter("firstname")),
           firstnameUp = SwissKnife.searchConvert(firstname),
           lastname = SwissKnife.sqlEncode(request.getParameter("lastname")),
           lastnameUp = SwissKnife.searchConvert(lastname),
           email = SwissKnife.sqlEncode(request.getParameter("email")).toLowerCase(),
           occupation = SwissKnife.sqlEncode(request.getParameter("occupation")),
           title = SwissKnife.sqlEncode(request.getParameter("title")),
           sex = SwissKnife.sqlEncode(request.getParameter("sex")),
           phone = SwissKnife.sqlEncode(request.getParameter("phone")),
           fax = SwissKnife.sqlEncode(request.getParameter("fax")),
           contactUsDescr = SwissKnife.sqlEncode(request.getParameter("contactUsDescr")),
           custLang = SwissKnife.sqlEncode(request.getParameter("custLang")),
           receiveEmail = SwissKnife.sqlEncode(request.getParameter("receiveEmail")),
           lockedAccnt = SwissKnife.sqlEncode(request.getParameter("lockedAccnt"));
    
    String custText = request.getParameter("custText"),
        custTextLG = request.getParameter("custTextLG");
    
    BigDecimal discountPct = SwissKnife.parseBigDecimal(request.getParameter("discountPct"),localeLanguage,localeCountry);
    if (discountPct == null) discountPct = _zero;
    
    Timestamp birthDate = SwissKnife.buildTimestamp(request.getParameter("birthDateDay"),
                                                    request.getParameter("birthDateMonth"),
                                                    request.getParameter("birthDateYear"));
    String customerType = null, contactUsId = null;
    if ((customerType = request.getParameter("customerType")) == null) customerType = "0";
    if ((contactUsId = request.getParameter("contactUsId")) == null) contactUsId = "0";

    String SBCodeBilling = SwissKnife.sqlEncode(request.getParameter("SBCodeBilling")),
           SBLangBilling = SwissKnife.sqlEncode(request.getParameter("SBLangBilling")),
           SBNameBilling = SwissKnife.sqlEncode(request.getParameter("SBNameBilling")),
           SBProfessionBilling = SwissKnife.sqlEncode(request.getParameter("SBProfessionBilling")),
           SBAddressBilling = SwissKnife.sqlEncode(request.getParameter("SBAddressBilling")),
           SBAreaCodeBilling = SwissKnife.sqlEncode(request.getParameter("SBAreaCodeBilling")),
           SBCityBilling = SwissKnife.sqlEncode(request.getParameter("SBCityBilling")),
           SBRegionBilling = SwissKnife.sqlEncode(request.getParameter("SBRegionBilling")),
           SBCountryCodeBilling = SwissKnife.sqlEncode(request.getParameter("SBCountryCodeBilling")),
           SBZipCodeBilling = SwissKnife.sqlEncode(request.getParameter("SBZipCodeBilling")),
           SBPhoneBilling = SwissKnife.sqlEncode(request.getParameter("SBPhoneBilling")),
           SBFaxBilling = SwissKnife.sqlEncode(request.getParameter("SBFaxBilling")),
           SBEmailBilling = SwissKnife.sqlEncode(request.getParameter("SBEmailBilling")),
           SBAfmBilling = SwissKnife.sqlEncode(request.getParameter("SBAfmBilling")),
           SBDoyBilling = SwissKnife.sqlEncode(request.getParameter("SBDoyBilling")),
           SBCreditTypeBilling = SwissKnife.sqlEncode(request.getParameter("SBCreditTypeBilling")),
           SBCreditNumBilling = SwissKnife.sqlEncode(request.getParameter("SBCreditNumBilling")),
           SBCreditOwnerBilling = SwissKnife.sqlEncode(request.getParameter("SBCreditOwnerBilling"));
    
    Timestamp SBCreditExpDayBilling = SwissKnife.buildTimestamp(request.getParameter("SBCreditExpDayBillingDay"),
                                                                request.getParameter("SBCreditExpDayBillingMonth"),
                                                                request.getParameter("SBCreditExpDayBillingYear"));
    
    if (logCode.equals("") || customerId.equals("")
          || SBCodeBilling.equals("")
          || localeLanguage.equals("") || localeCountry.equals("") 
          || usrAccessLevel.equals("") || email.equals("")
          || SBCountryCodeBilling.equals("")) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    if (dbRet.getNoError() == 1) {
      dbRet = Customer.updateUser(database,logCode,email,usrPasswd,usrAccessLevel);
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = Customer.updateCustomer(database,customerId,firstname,firstnameUp,
                                      lastname,lastnameUp,occupation,
                                      email,phone,fax,title,sex,discountPct,
                                      customerType,contactUsId,contactUsDescr,
                                      birthDate,custLang,receiveEmail,lockedAccnt,custText,custTextLG);
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = Customer.updateShipBillInfo(database,SBCodeBilling,gr.softways.dev.eshop.eways.Customer.BILLING_INFO,
                                          SBNameBilling,SBProfessionBilling,
                                          SBAddressBilling,SBAreaCodeBilling,
                                          SBCityBilling,SBRegionBilling,
                                          SBCountryCodeBilling,SBZipCodeBilling,
                                          SBPhoneBilling,SBFaxBilling,SBEmailBilling,
                                          SBAfmBilling,SBDoyBilling,SBCreditTypeBilling,
                                          SBCreditExpDayBilling,SBCreditNumBilling,
                                          SBCreditOwnerBilling,SBLangBilling);
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
}
