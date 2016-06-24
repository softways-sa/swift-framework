package gr.softways.dev.eshop.eways.v5;

import java.sql.*;
import java.math.BigDecimal;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import javax.servlet.*;
import java.util.Hashtable;
import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;
import gr.softways.dev.poolmanager.AuthEmp;

import gr.softways.dev.eshop.emaillists.lists.Present;
import gr.softways.dev.eshop.emaillists.newsletter.Newsletter;

public class Customer {
  
  public Customer(String databaseId) {
    _databaseId = databaseId;
    
    _order = new Order(this);
  }
  
  public boolean isSignedIn() {
    if (getCustomerId().length() > 0) return true; else return false;
  }
  
  public DbRet doSignIn(HttpServletRequest request) {
    DbRet dbRet = new DbRet();
    
    String username = request.getParameter("username") == null ? "" : request.getParameter("username").toLowerCase(),
           password = request.getParameter("password") == null ? "" : request.getParameter("password");
           
    return doSignIn(request, username, password);
  }
  
  public DbRet doSignIn(HttpServletRequest request, String username, String password) {
    DbRet dbRet = new DbRet();
    
    password = Crypto.encrypt(password);
    
    PreparedStatement ps = null;
    ResultSet resultSet = null;
    
    int rowsAffected = 0;
    
    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(getDatabaseId());

    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();

    /* lock customer row in database */
    String query = "UPDATE customer set custLock=? WHERE custLogCode IN " +
                    "(SELECT logCode FROM users WHERE usrName=?)";
    
    if (dbRet.getNoError() == 1) {
      try {
        ps = database.createPreparedStatement(query);
        
        ps.setString(1, "1");
        ps.setString(2, username);
      
        rowsAffected = ps.executeUpdate();
      }
      catch (Exception eee) {
        dbRet.setNoError(0);
        eee.printStackTrace();
      }
      finally {
        try { if (ps != null) ps.close(); } catch (Exception e) { }
      }
    }
    
    if (dbRet.getNoError() == 1) {
      query = "SELECT *"
                 + " FROM customer,shipBillInfo,users,userGroups"
                 + " WHERE custLogCode = logCode"
                 + " AND usrAccessLevel = userGroupId"
                 + " AND SBCustomerId = customerId"
                 + " AND users.usrName = ?"
                 + " AND users.usrPasswd = ?"
                 + " AND customer.lockedAccnt != '1'";

      try {
        ps = database.createPreparedStatement(query);
        ps.setString(1, username);
        ps.setString(2, password);
        
        resultSet = ps.executeQuery();
        
        if (resultSet.next() == true) {
          // username & password pair valid
          AuthEmp authEmp = new AuthEmp(username,password,resultSet.getInt("usrAccessLevel"),getDatabaseId());
          
          setCustomerId(resultSet.getString("customerId").trim());
          
          setCustLogCode(resultSet.getString("custLogCode").trim());
          
          setLastname(SwissKnife.sqlDecode(resultSet.getString("lastname")).trim());
          setFirstname(SwissKnife.sqlDecode(resultSet.getString("firstname")).trim());
          
          setSBCodeBilling(resultSet.getString("SBCode").trim());
          setBillingAddress(SwissKnife.sqlDecode(resultSet.getString("SBAddress")).trim());
          setBillingCity(SwissKnife.sqlDecode(resultSet.getString("SBCity")).trim());
          
          setBillingCountryCode(SwissKnife.sqlDecode(resultSet.getString("SBCountryCode")).trim(), database);
          
          setBillingZipCode(SwissKnife.sqlDecode(resultSet.getString("SBZipCode")).trim());
          setBillingPhone(SwissKnife.sqlDecode(resultSet.getString("SBPhone")).trim());
          setBillingRegion(SwissKnife.sqlDecode(resultSet.getString("SBRegion")).trim());
          setBillingName(SwissKnife.sqlDecode(resultSet.getString("SBName")).trim());
          setBillingAfm(SwissKnife.sqlDecode(resultSet.getString("SBAfm")).trim());
          setBillingDoy(SwissKnife.sqlDecode(resultSet.getString("SBDoy")).trim());
          setBillingProfession(SwissKnife.sqlDecode(resultSet.getString("SBProfession")).trim());
          
          setCustomerType(resultSet.getInt("customerType"));
          
          setDiscountPct(resultSet.getBigDecimal("discountPct"));
          
          setBillingCellPhone(SwissKnife.sqlDecode(resultSet.getString("SBCellPhone")).trim());
          
          setOccupation(SwissKnife.sqlDecode(resultSet.getString("occupation")));
          
          setEmail(SwissKnife.sqlDecode(resultSet.getString("email")).trim());
          
          setAuthUsername(username);
          setAuthPassword(password);
          
          setHotdealBuysCnt(resultSet.getInt("hotdealBuysCnt") + 1);
          setDateLastUsed(SwissKnife.currentDate());
          setLastIPUsed(request.getRemoteAddr());
          
          director.addAuthUser(getDatabaseId(), authEmp);

          HttpSession session = null;
        
          session = request.getSession();

          session.setAttribute(getDatabaseId() + ".unbindObject", authEmp);
          session.setAttribute(getDatabaseId() + ".authUsername", username);
          session.setAttribute(getDatabaseId() + ".authPassword", password);
          
          session.setAttribute(getDatabaseId() + ".front_end.customerType", String.valueOf(getCustomerType()));
        }
        else {
          dbRet.setNoError(0);
          dbRet.setRetInt(99);
          System.out.println("[" + SwissKnife.currentDate() + " " + getDatabaseId() + "] " + username + " failed to login.");
        }
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
      finally {
        try { if (resultSet != null) resultSet.close(); } catch (Exception e) { }
        try { if (ps != null) ps.close(); } catch (Exception e) { }
      }
    }
    
    /* update customer login data in database */
    query = "UPDATE customer set dateLastUsed=?, hotdealBuysCnt=? WHERE customerId=?";
    if (dbRet.getNoError() == 1) {
      try {
        ps = database.createPreparedStatement(query);
        
        ps.setTimestamp(1, _dateLastUsed);
        ps.setInt(2, getHotdealBuysCnt());
        ps.setString(3, _customerId);
        
        rowsAffected = ps.executeUpdate();
      }
      catch (Exception ee) {
        dbRet.setNoError(0);
        ee.printStackTrace();
      }
      finally {
        try { if (resultSet != null) resultSet.close(); } catch (Exception e) { }
        try { if (ps != null) ps.close(); } catch (Exception e) { }
      }
    }
    
    /* update user login data in database */
    query = "UPDATE users set dateLastUsed=?, lastIPUsed=? WHERE usrName=?";
    if (dbRet.getNoError() == 1) {
      try {
        ps = database.createPreparedStatement(query);
        
        ps.setTimestamp(1, _dateLastUsed);
        ps.setString(2, getLastIPUsed());
        ps.setString(3, username);
      
        rowsAffected = ps.executeUpdate();
      }
      catch (Exception eeee) {
        dbRet.setNoError(0);
        eeee.printStackTrace();
      }    
      finally {
        try { if (resultSet != null) resultSet.close(); } catch (Exception e) { }
        try { if (ps != null) ps.close(); } catch (Exception e) { }
      }
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    director.freeDBConnection(getDatabaseId(), database);
    
    return dbRet;
  }
  
  public DbRet doBillingAddress(HttpServletRequest request) {
    DbRet dbRet = new DbRet();
    
    String lastname = request.getParameter("lastname") == null ? "" : request.getParameter("lastname"),
           firstname = request.getParameter("firstname") == null ? "" : request.getParameter("firstname"),
           email = request.getParameter("email") == null ? "" : request.getParameter("email"),
           billingPhone = request.getParameter("billingPhone") == null ? "" : request.getParameter("billingPhone"),
           billingAddress = request.getParameter("billingAddress") == null ? "" : request.getParameter("billingAddress"),
           billingCity = request.getParameter("billingCity") == null ? "" : request.getParameter("billingCity"),
           billingZipCode = request.getParameter("billingZipCode") == null ? "" : request.getParameter("billingZipCode"),
           billingArea = request.getParameter("billingArea") == null ? "" : request.getParameter("billingArea"),
           billingRegion = request.getParameter("billingRegion") == null ? "" : request.getParameter("billingRegion"),
           billingCellPhone = request.getParameter("billingPhone") == null ? "" : request.getParameter("billingCellPhone"),
           billingCountryCode = request.getParameter("billingCountryCode") == null ? "" : request.getParameter("billingCountryCode");

    // invoice
    String billingName = request.getParameter("billingName") == null ? "" : request.getParameter("billingName"),
           billingAfm = request.getParameter("billingAfm") == null ? "" : request.getParameter("billingAfm"),
           billingDoy = request.getParameter("billingDoy") == null ? "" : request.getParameter("billingDoy"),
           billingProfession = request.getParameter("billingProfession") == null ? "" : request.getParameter("billingProfession");
   
    String ordPrefNotes = request.getParameter("ordPrefNotes") == null ? "" : request.getParameter("ordPrefNotes");
    
    String useBilling = request.getParameter("useBilling") == null ? "" : request.getParameter("useBilling");
    
    if (firstname.trim().length()>0) {
      setFirstname(firstname);
    }
    else dbRet.setNoError(0);

    if (lastname.trim().length()>0) {
      setLastname(lastname);
    }
    else dbRet.setNoError(0);
    
    if (email.trim().length()>0) {
      setEmail(email);
    }
    else dbRet.setNoError(0);

    setBillingPhone(billingPhone);
    
    setBillingCellPhone(billingCellPhone);
    
    if (billingAddress.trim().length()>0) {
      setBillingAddress(billingAddress);
    }
    else dbRet.setNoError(0);

    if (billingCountryCode.trim().length()>0) {
      setBillingCountryCode(billingCountryCode);
    }
    else dbRet.setNoError(0);

    if (billingCity.trim().length()>0) {
      setBillingCity(billingCity);
    }
    else dbRet.setNoError(0);
    
    setBillingZipCode(billingZipCode);
    
    setBillingArea(billingArea);
    setBillingRegion(billingRegion);
    
    setBillingName(billingName);
    setBillingAfm(billingAfm);
    setBillingDoy(billingDoy);
    setBillingProfession(billingProfession);
    
    if (getBillingName() != null && getBillingName().length()>0
          && getBillingAfm() != null && getBillingAfm().length()>0) {
      _order.setDocumentType(gr.softways.dev.eshop.eways.v2.Order.DOCUMENT_TYPE_INVOICE);
    }
    else {
      _order.setDocumentType(gr.softways.dev.eshop.eways.v2.Order.DOCUMENT_TYPE_RECEIPT);
    }

    setOrdPrefNotes(ordPrefNotes);
    
    if (useBilling.equals("1")) {
      setShippingName(getFirstname() + " " + getLastname());
      setShippingAddress(getBillingAddress());
      setShippingPhone(getBillingPhone());
      setShippingZipCode(getBillingZipCode());
      setShippingCity(getBillingCity());
      setShippingCountryCode(getBillingCountryCode());
    }
    
    // store in-session customer info to database
    if (dbRet.getNoError() == 1) doUpdatePInfo();
    
    return dbRet;
  }
  
  public DbRet doShippingAddress(HttpServletRequest request) {
    DbRet dbRet = new DbRet();
    
    String shippingName = request.getParameter("shippingName") == null ? "" : request.getParameter("shippingName"),
           shippingAddress = request.getParameter("shippingAddress") == null ? "" : request.getParameter("shippingAddress"), 
           shippingZipCode = request.getParameter("shippingZipCode") == null ? "" : request.getParameter("shippingZipCode"),
           shippingCity = request.getParameter("shippingCity") == null ? "" : request.getParameter("shippingCity"),
           shippingCountryCode = request.getParameter("shippingCountryCode") == null ? "" : request.getParameter("shippingCountryCode"),
           shippingPhone = request.getParameter("shippingPhone") == null ? "" : request.getParameter("shippingPhone");

    if (shippingName.trim().length()>0) {
      setShippingName(shippingName);
    }
    else dbRet.setNoError(0);
    
    if (shippingAddress.trim().length()>0) {
      setShippingAddress(shippingAddress);
    }
    else dbRet.setNoError(0);
    
    if (shippingZipCode.trim().length()>0) {
      setShippingZipCode(shippingZipCode);
    }
    else dbRet.setNoError(0);
    
    if (shippingCity.trim().length()>0) {
      setShippingCity(shippingCity);
    }
    else dbRet.setNoError(0);
    
    if (shippingPhone.trim().length()>0) {
      setShippingPhone(shippingPhone);
    }
    else dbRet.setNoError(0);
    
    if (shippingCountryCode.trim().length()>0) {
      setShippingCountryCode(shippingCountryCode);
    }
    else dbRet.setNoError(0);
    
    return dbRet;
  }
  
  private DbRet getDefCountryCode() {
    DbRet dbRet = null;
    
    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(getDatabaseId());
    
    dbRet = getDefCountryCode(database);
    
    director.freeDBConnection(getDatabaseId(), database);
    
    return dbRet;
  }
    
  private DbRet getDefCountryCode(Database database) {
    DbRet dbRet = new DbRet();
    
    String query = "SELECT countryCode FROM country ORDER BY countryCode DESC";
    
    QueryDataSet queryDataSet = null;
    
    try {
      queryDataSet = new QueryDataSet();
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.setMaxRows(1);
      
      queryDataSet.refresh();

      dbRet.setRetStr( queryDataSet.getString("countryCode").trim() );
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      
      e.printStackTrace();
    }
    finally {
      if (queryDataSet != null) queryDataSet.close();
    }
    
    return dbRet;
  }
  
  public DbRet doRegister(HttpServletRequest request) {
    DbRet dbRet = new DbRet();
    
    String lastname = SwissKnife.sqlEncode(request.getParameter("lastname")),
           firstname = SwissKnife.sqlEncode(request.getParameter("firstname")),
           email = SwissKnife.sqlEncode(request.getParameter("email")).toLowerCase(),
           password = SwissKnife.sqlEncode(request.getParameter("password")),
           custLang = SwissKnife.sqlEncode(request.getParameter("custLang")),
           receiveEmail = SwissKnife.sqlEncode(request.getParameter("receiveEmail"));
           
    if (!"1".equals(receiveEmail)) receiveEmail = "0";
    
    String[] values = Configuration.getValues(new String[] {"smtpServer","shopEmailFrom","shopName"});
    String smtpServer = values[0], emailFrom = values[1], shopName = values[2];
    
    if (lastname.length() == 0 || firstname.length() == 0 || email.length() == 0 || password.length() == 0 || emailFrom.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    String EMLMEmail = email,
           EMLMAltEmail = "",
           EMLMLastName = lastname,
           EMLMLastNameUp = SwissKnife.searchConvert(EMLMLastName),
           EMLMFirstName = firstname,
           EMLMCompanyName = "",
           EMLMCompanyNameUp = SwissKnife.searchConvert(EMLMCompanyName),
           EMLMAddress = "",
           EMLMZipCode = "",
           EMLMCity = "",
           EMLMCountry = "",
           EMLMPhone = "",
           EMLMActive = Present.STATUS_ACTIVE,
           EMLMField1 = "",
           EMLMField2 = "",
           EMLMField3 = "";
    
    Timestamp EMLMBirthDate = null, EMLMRegDate = SwissKnife.currentDate();
    
    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(getDatabaseId());
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    String customerId = null, custLogCode = null, usrAccessLevel = "-1",
           SBCustomerId = null, SBCountryCodeBilling = null, SBCodeBilling = null;
    
    String lockedAccnt = "";
    
    int customerType = gr.softways.dev.eshop.eways.Customer.CUSTOMER_TYPE_RETAIL;
    
    Timestamp dateCreated = SwissKnife.currentDate(),
              dateLastUsed = SwissKnife.currentDate(),
              birthDate = null;

    BigDecimal purchaseVal = _zero, purchaseValEU = _zero,
               purchaseValCUR1 = _zero, purchaseValCUR2 = _zero;
    
    String firstnameUp = SwissKnife.searchConvert(firstname),
           lastnameUp = SwissKnife.searchConvert(lastname);
    
    dbRet = getDefUserGroup(database);
    if (dbRet.getNoError() == 1) {
      usrAccessLevel = String.valueOf(dbRet.getRetInt());
    }
    
    dbRet = getDefCountryCode(database);
    if (dbRet.getNoError() == 1) {
      SBCountryCodeBilling = dbRet.getRetStr();
    }
    
    String encryptedPassword = Crypto.encrypt(password);
    if (encryptedPassword == null || encryptedPassword.trim().length() == 0) {
      dbRet.setNoError(0);
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = gr.softways.dev.eshop.customer.v2.Customer.newUser(database,email,encryptedPassword,usrAccessLevel,request.getRemoteAddr(),SwissKnife.currentDate());
      custLogCode = dbRet.getRetStr();
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = gr.softways.dev.eshop.customer.v2.Customer.newCustomer(database,custLogCode,dateCreated,dateLastUsed,
                                  firstname,firstnameUp,lastname,lastnameUp,"",
                                  email,"","","","",_zero,
                                  String.valueOf(customerType),"0","",null,
                                  _zero,_zero,_zero,
                                  _zero,"0","0",custLang,receiveEmail,lockedAccnt);
      customerId = SBCustomerId = dbRet.getRetStr();
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = gr.softways.dev.eshop.customer.v2.Customer.newShipBillInfo(database,SBCustomerId,gr.softways.dev.eshop.eways.Customer.BILLING_INFO,
                                       "","","","","","",
                                       SBCountryCodeBilling,
                                       "","","","","","","",
                                       null,"","","");
      SBCodeBilling = dbRet.getRetStr();
    }
    
    /**if (dbRet.getNoError() == 1 && receiveEmail.equals("1")) {
      String EMLRListCode = null;
      
      EMLRListCode = gr.softways.dev.eshop.customer.v2.Customer.getDefEMLRListCode(database).getRetStr();
      
      if (EMLRListCode != null) {
        gr.softways.dev.eshop.customer.v2.Customer.insertNewsletterMember(database,EMLMEmail,EMLMAltEmail,EMLMLastName,
                                                                       EMLMLastNameUp,EMLMFirstName,EMLMBirthDate,
                                                                       EMLMRegDate,EMLMCompanyName,EMLMCompanyNameUp,
                                                                       EMLMAddress,EMLMZipCode,EMLMCity,EMLMCountry,
                                                                       EMLMPhone,EMLMActive,EMLMField1,EMLMField2,EMLMField3,
                                                                       EMLRListCode,receiveEmail);
      }
    }**/
    if (dbRet.getNoError() == 1) {
      String EMLRListCode = "";
      
      EMLRListCode = Newsletter.getDefEMLRListCode(database, "NEWSLETTER").getRetStr();
      
      if (EMLRListCode != null && EMLRListCode.length() > 0) {
        dbRet = Newsletter.update(database,EMLMEmail.toLowerCase(),"",EMLMLastName,
            EMLMLastNameUp,EMLMFirstName,null,
            null,"","","","","","","",EMLMActive,"","","",EMLRListCode,receiveEmail);
      }
    }
    
    if (dbRet.getNoError() == 1) {
      sendRegistrationEmail(firstname,lastname,email,custLang,smtpServer,emailFrom,shopName);
    }
    
    if (dbRet.getNoError() == 1) {
      setCustLogCode(custLogCode);
      setSBCodeBilling(SBCodeBilling);
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    director.freeDBConnection(getDatabaseId(), database);
    
    return dbRet;
  }
  
  public DbRet doUpdatePInfo(HttpServletRequest request) {
    DbRet dbRet = new DbRet();
    
    String encryptedPassword = null, usrAccessLevel = null;
    
    String occupation = SwissKnife.sqlEncode(getOccupation());
    
    String firstname = SwissKnife.sqlEncode(request.getParameter("firstname")),
           firstnameUp = SwissKnife.searchConvert(firstname),
           lastname = SwissKnife.sqlEncode(request.getParameter("lastname")),
           lastnameUp = SwissKnife.searchConvert(lastname),
           email = SwissKnife.sqlEncode(request.getParameter("email")).toLowerCase(),
           title = SwissKnife.sqlEncode(request.getParameter("title")),
           sex = SwissKnife.sqlEncode(request.getParameter("sex")),
           phone = SwissKnife.sqlEncode(request.getParameter("phone")),
           fax = SwissKnife.sqlEncode(request.getParameter("fax")),
           contactUsDescr = SwissKnife.sqlEncode(request.getParameter("contactUsDescr")),
           custLang = SwissKnife.sqlEncode(request.getParameter("custLang")),
           receiveEmail = SwissKnife.sqlEncode(request.getParameter("receiveEmail")),
           lockedAccnt = null;
    
    if (!"1".equals(receiveEmail)) receiveEmail = "0";
    
    BigDecimal discountPct = _zero;
    
    Timestamp birthDate = SwissKnife.buildTimestamp(request.getParameter("birthDateDay"),
                                                    request.getParameter("birthDateMonth"),
                                                    request.getParameter("birthDateYear"));
                                                    
    int customerType = getCustomerType();
    
    String contactUsId = null;
    if ((contactUsId = request.getParameter("contactUsId")) == null) contactUsId = "0";

    String SBLangBilling = SwissKnife.sqlEncode(request.getParameter("SBLangBilling")),
           SBNameBilling = SwissKnife.sqlEncode(request.getParameter("SBNameBilling")),
           SBProfessionBilling = SwissKnife.sqlEncode(request.getParameter("SBProfessionBilling")),
           SBAddressBilling = SwissKnife.sqlEncode(request.getParameter("SBAddressBilling")),
           SBAreaCodeBilling = SwissKnife.sqlEncode(request.getParameter("SBAreaCodeBilling")),
           SBCityBilling = SwissKnife.sqlEncode(request.getParameter("SBCityBilling")),
           SBRegionBilling = SwissKnife.sqlEncode(request.getParameter("SBRegionBilling")),
           SBZipCodeBilling = SwissKnife.sqlEncode(request.getParameter("SBZipCodeBilling")),
           SBPhoneBilling = SwissKnife.sqlEncode(request.getParameter("SBPhoneBilling")),
           SBFaxBilling = SwissKnife.sqlEncode(request.getParameter("SBFaxBilling")),
           SBEmailBilling = SwissKnife.sqlEncode(request.getParameter("SBEmailBilling")),
           SBAfmBilling = SwissKnife.sqlEncode(request.getParameter("SBAfmBilling")),
           SBDoyBilling = SwissKnife.sqlEncode(request.getParameter("SBDoyBilling")),
           SBCreditTypeBilling = SwissKnife.sqlEncode(request.getParameter("SBCreditTypeBilling")),
           SBCreditNumBilling = SwissKnife.sqlEncode(request.getParameter("SBCreditNumBilling")),
           SBCreditOwnerBilling = SwissKnife.sqlEncode(request.getParameter("SBCreditOwnerBilling")),
           SBCountryCodeBilling = SwissKnife.sqlEncode(request.getParameter("SBCountryCodeBilling"));
    
    //String SBCountryCodeBilling = getDefCountryCode().getRetStr();
    
    Timestamp SBCreditExpDayBilling = SwissKnife.buildTimestamp(request.getParameter("SBCreditExpDayBillingDay"),
                                                                request.getParameter("SBCreditExpDayBillingMonth"),
                                                                request.getParameter("SBCreditExpDayBillingYear"));
                                                                
    if (lastname.length() == 0 || firstname.length() == 0 || email.length() == 0 || SBCountryCodeBilling.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    String EMLMEmail = email,
           EMLMAltEmail = "",
           EMLMLastName = lastname,
           EMLMLastNameUp = SwissKnife.searchConvert(EMLMLastName),
           EMLMFirstName = firstname,
           EMLMCompanyName = "",
           EMLMCompanyNameUp = SwissKnife.searchConvert(EMLMCompanyName),
           EMLMAddress = SBAddressBilling,
           EMLMZipCode = SBZipCodeBilling,
           EMLMCity = SBCityBilling,
           EMLMCountry = "",
           EMLMPhone = "",
           EMLMActive = "1",
           EMLMField1 = "",
           EMLMField2 = "",
           EMLMField3 = "";
    
    Timestamp EMLMBirthDate = null, EMLMRegDate = SwissKnife.currentDate();
    
    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(getDatabaseId());
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    if (dbRet.getNoError() == 1) {
      dbRet = gr.softways.dev.eshop.customer.v2.Customer.updateUser(database,getCustLogCode(),email,encryptedPassword,usrAccessLevel);
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = gr.softways.dev.eshop.customer.v2.Customer.updateCustomer(database,getCustomerId(),firstname,firstnameUp,
                                      lastname,lastnameUp,occupation,
                                      email,phone,fax,title,sex,discountPct,
                                      String.valueOf(customerType),contactUsId,contactUsDescr,
                                      birthDate,custLang,receiveEmail,lockedAccnt);
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = gr.softways.dev.eshop.customer.v2.Customer.updateShipBillInfo(database,getSBCodeBilling(),gr.softways.dev.eshop.eways.Customer.BILLING_INFO,
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
      String EMLRListCode = null;
      
      EMLRListCode = gr.softways.dev.eshop.customer.v2.Customer.getDefEMLRListCode(database).getRetStr();
      
      if (EMLRListCode != null) {
        gr.softways.dev.eshop.customer.v2.Customer.insertNewsletterMember(database,EMLMEmail,EMLMAltEmail,EMLMLastName,
                                                                       EMLMLastNameUp,EMLMFirstName,EMLMBirthDate,
                                                                       EMLMRegDate,EMLMCompanyName,EMLMCompanyNameUp,
                                                                       EMLMAddress,EMLMZipCode,EMLMCity,EMLMCountry,
                                                                       EMLMPhone,EMLMActive,EMLMField1,EMLMField2,EMLMField3,
                                                                       EMLRListCode,receiveEmail);
      }
    }**/
    if (dbRet.getNoError() == 1) {
      String EMLRListCode = "";
      
      EMLRListCode = Newsletter.getDefEMLRListCode(database, "NEWSLETTER").getRetStr();
      
      if (EMLRListCode != null && EMLRListCode.length() > 0) {
        dbRet = Newsletter.update(database,EMLMEmail.toLowerCase(),"",EMLMLastName,
            EMLMLastNameUp,EMLMFirstName,null,
            null,"","","","","","","",EMLMActive,"","","",EMLRListCode,receiveEmail);
      }
    }
    
    if (dbRet.getNoError() == 1) {
      setEmail(email);
      
      setFirstname(SwissKnife.sqlDecode(firstname));
      setLastname(SwissKnife.sqlDecode(lastname));
      
      setOccupation(SwissKnife.sqlDecode(occupation));
      
      setBillingAddress(SwissKnife.sqlDecode(SBAddressBilling));
      setBillingCity(SwissKnife.sqlDecode(SBCityBilling));
      
      setBillingCountryCode(SBCountryCodeBilling, database);
      
      setBillingZipCode(SwissKnife.sqlDecode(SBZipCodeBilling));
      setBillingPhone(SwissKnife.sqlDecode(SBPhoneBilling));
      setBillingRegion(SwissKnife.sqlDecode(SBRegionBilling));
      
      setBillingName(SwissKnife.sqlDecode(SBNameBilling));
      setBillingAfm(SwissKnife.sqlDecode(SBAfmBilling));
      setBillingDoy(SwissKnife.sqlDecode(SBDoyBilling));
      setBillingProfession(SwissKnife.sqlDecode(SBProfessionBilling));
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    director.freeDBConnection(getDatabaseId(), database);
    
    return dbRet;
  }
  
  /**
  /* Store in-session customer info to database
  **/
  public DbRet doUpdatePInfo() {
    DbRet dbRet = new DbRet();
    
    String logCode = getCustLogCode(), encryptedPassword = null, usrAccessLevel = null;
           
    String firstname = SwissKnife.sqlEncode(getFirstname()),
           firstnameUp = SwissKnife.searchConvert(firstname),
           lastname = SwissKnife.sqlEncode(getLastname()),
           lastnameUp = SwissKnife.searchConvert(lastname),
           email = getEmail(),
           occupation = SwissKnife.sqlEncode(getOccupation()),
           title = "",
           sex = "",
           phone = "",
           fax = "",
           contactUsDescr = "",
           custLang = "",
           receiveEmail = null,
           lockedAccnt = null,
           receiveSMS = null;
    
    int customerType = getCustomerType();
    
    BigDecimal discountPct = getDiscountPct();
    
    Timestamp birthDate = getBirthDate();
    
    String contactUsId = "0";
    
    String SBCodeBilling = getSBCodeBilling(),
           SBLangBilling = "",
           SBNameBilling = SwissKnife.sqlEncode(getBillingName()),
           SBProfessionBilling = SwissKnife.sqlEncode(getBillingProfession()),
           SBAddressBilling = SwissKnife.sqlEncode(getBillingAddress()),
           SBAreaCodeBilling = "",
           SBCityBilling = SwissKnife.sqlEncode(getBillingCity()),
           SBRegionBilling = SwissKnife.sqlEncode(getBillingRegion()),
           SBCountryCodeBilling = getBillingCountryCode(),
           SBZipCodeBilling = SwissKnife.sqlEncode(getBillingZipCode()),
           SBPhoneBilling = SwissKnife.sqlEncode(getBillingPhone()),
           SBFaxBilling = SwissKnife.sqlEncode(getBillingFax()),
           SBEmailBilling = "",
           SBAfmBilling = SwissKnife.sqlEncode(getBillingAfm()),
           SBDoyBilling = SwissKnife.sqlEncode(getBillingDoy()),
           SBCreditTypeBilling = "",
           SBCreditNumBilling = "",
           SBCreditOwnerBilling = "",
           SBCellPhoneBilling = SwissKnife.sqlEncode(getBillingCellPhone());
    
    Timestamp SBCreditExpDayBilling = getBillingCreditExpDay();
    
    if (lastname.length() == 0 || firstname.length() == 0
          || email.length() == 0
          || logCode.length() == 0 || SBCodeBilling.length() == 0
          || SBCountryCodeBilling.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(getDatabaseId());
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    if (dbRet.getNoError() == 1) {
      dbRet = gr.softways.dev.eshop.customer.v2.Customer.updateUser(database,logCode,email,encryptedPassword,usrAccessLevel);
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = gr.softways.dev.eshop.customer.v2.Customer.updateCustomer(database,getCustomerId(),firstname,firstnameUp,
                                      lastname,lastnameUp,occupation,
                                      email,phone,fax,title,sex,discountPct,
                                      String.valueOf(customerType),contactUsId,contactUsDescr,
                                      birthDate,custLang,receiveEmail,lockedAccnt);
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = gr.softways.dev.eshop.customer.v2.Customer.updateShipBillInfo(database,SBCodeBilling,gr.softways.dev.eshop.eways.Customer.BILLING_INFO,
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
    director.freeDBConnection(getDatabaseId(), database);
    
    return dbRet;
  }
  
  private DbRet sendRegistrationEmail(String firstname,String lastname,String email,String custLang,String smtpServer,String emailFrom,String shopName) {
    DbRet dbRet = new DbRet();
    
    EMail msg = null;
    
    String ls = "\r\n";
    
    String subject = null;
    
    StringBuffer body = new StringBuffer();
    
    if (custLang.length() == 0) {
      subject = "Καλώς ήρθατε στο " + shopName;
      
      body.append("Αγαπητέ/ή " + firstname + " " + lastname + "," + ls + ls);
      body.append("Σας ευχαριστούμε για την εγγραφή σας! Αυτό είναι το email που θα χρησιμοποιείτε για την είσοδο σας:" + ls);
      body.append(email);
      body.append(ls + ls);
      body.append("Με εκτίμηση," + ls);
    }
    else {
      subject = "Welcome to " + shopName;
      
      body.append("Dear " + firstname + " " + lastname + "," + ls + ls);
      body.append("Thank you for joining! Here is the email that you will use to sign in:" + ls);
      body.append(email);
      body.append(ls + ls);
      body.append("Best regards," + ls);
    }
    
    body.append(shopName + ls);
    
    msg = new EMail(email,emailFrom,subject,body.toString(),smtpServer,"text/plain","UTF-8",null);
    
    SendMail.sendMessage(msg);
    
    return dbRet;
  }
  
  public DbRet doSendLostPassword(HttpServletRequest request) {
    DbRet dbRet = new DbRet();
    
    String email = SwissKnife.sqlEncode(request.getParameter("email")).toLowerCase();
           
    String[] values = Configuration.getValues(new String[] {"smtpServer","shopEmailFrom"});
    String smtpServer = values[0], from = values[1];
    
    QueryDataSet queryDataSet = null;
    
    String query  = "SELECT usrPasswd FROM users WHERE usrName = '" + email + "'";
    
    StringBuffer body = new StringBuffer();
    
    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(getDatabaseId());

    try {
      queryDataSet = new QueryDataSet();
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      queryDataSet.refresh();

      if (queryDataSet.getRowCount() == 1) {
        String subject = "";
        
        if (getCustLang().equals("")) {
          subject = "Ανάκτηση κωδικού πρόσβασης";
          body.append("Ο κωδικός σας είναι " + Crypto.decrypt(queryDataSet.getString("usrPasswd")) + "\n\n");
        }
        else {
          subject = "Retrieve password";
          body.append("Your password is " + Crypto.decrypt(queryDataSet.getString("usrPasswd")) + "\n\n");
        }
        
        EMail msg = new EMail(email,from,subject,body.toString(),smtpServer,"text/plain","UTF-8",null);
        
        boolean sent = SendMail.sendMessage(msg);
        
        if (sent == true) {
          dbRet.setNoError(1);
        }
        else {
          dbRet.setNoError(0);
        }
      }
      else dbRet.setNoError(0);
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      
      e.printStackTrace();
    }
    finally {
      if (queryDataSet != null) queryDataSet.close();
    }
    
    director.freeDBConnection(getDatabaseId(), database);
    
    return dbRet;
  }
  
  public DbRet doSignOut(HttpServletRequest request) {
    DbRet dbRet = new DbRet();

    setCustomerId("");
    setCustLogCode("");
    
    setFirstname("");
    setLastname("");
    setOccupation("");
    setEmail("");
    setPhone("");
    setFax("");
    setSex("");
    setContactUsDescr("");
    setContactUsId(0);
    setDateCreated(null);
    setDateLastUsed(null);
    setBirthDate(null);
    
    setCustomerType(-1);
    
    HttpSession session = null;
    
    session = request.getSession();
    session.removeAttribute(getDatabaseId() + ".front_end.customerType");
    
    setDiscountPct(_zero);

    setBillingName("");
    setBillingProfession("");
    setBillingAddress("");
    setBillingArea("");
    setBillingCity("");
    setBillingRegion("");
    setBillingCountryCode("");
    setBillingZipCode("");
    
    setBillingPhone("");
    setBillingCellPhone("");
    
    setBillingAfm("");
    setBillingDoy("");
    setBillingCreditType("");
    setBillingCreditExpMonth("");
    setBillingCreditExpYear("");
    setBillingCreditNum("");
    setBillingCreditOwner("");
    setBillingCreditExpDay(null);

    doResetShipping();
    
    return dbRet;
  }
  
  public DbRet doResetShipping() {
    DbRet dbRet = new DbRet();
    
    setTitle("");
    
    setShippingName("");
    setShippingAddress("");
    setShippingArea("");
    setShippingAreaZone("");
    setShippingCity("");
    setShippingRegion("");
    setShippingCountryCode("");
    setShippingCountryZone("");
    setShippingZipCode("");
    setShippingPhone("");
    
    setShippingDeliveryDate(null);
    
    setOrdPrefNotes("");
    
    setShippingLocationCode("");
    
    return dbRet;
  }
  
  public DbRet isValidForCheckout() {
    DbRet dbRet = new DbRet();
    
    dbRet.setRetInt(0);
    
    if (getFirstname() == null || getFirstname().length()==0) {
      dbRet.setRetInt(1);
    }
    else if (getLastname() == null || getLastname().length()==0) {
      dbRet.setRetInt(1);
    }
    else if (getEmail() == null || getEmail().length()==0) {
      dbRet.setRetInt(1);
    }
    else if (getBillingAddress() == null || getBillingAddress().length()==0) {
      dbRet.setRetInt(1);
    }
    else if (getBillingCountryCode() == null || getBillingCountryCode().length()==0) {
      dbRet.setRetInt(1);
    }
    else if (getBillingCity() == null || getBillingCity().length()==0) {
      dbRet.setRetInt(1);
    }
    else if (getShippingName().length()==0) {
      dbRet.setRetInt(1);
    }
    //else if (getShippingLocationCode().length()==0) {
      //dbRet.setRetInt(1);
    //}
    else if (getShippingAddress().length()==0) {
      dbRet.setRetInt(1);
    }
    
    return dbRet;
  }
  
  private DbRet getDefUserGroup(Database database) {
    DbRet dbRet = new DbRet();
    
    String query = "SELECT userGroupId FROM userGroups"
                 + " WHERE userGroupDefFlag = '1'"
                 + " AND userGroupGrantLogin != '1'";
    
    QueryDataSet queryDataSet = null;
    
    try {
      queryDataSet = new QueryDataSet();
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      queryDataSet.refresh();

      dbRet.setRetInt( queryDataSet.getInt("userGroupId") );
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      
      e.printStackTrace();
    }
    finally {
      if (queryDataSet != null) queryDataSet.close();
    }
    
    return dbRet;
  }
  
  private DbRet getColumnInfo(String query, String returnColumnName) {
    DbRet dbRet = new DbRet();

    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(getDatabaseId());
    
    QueryDataSet queryDataSet = null;
    
    try {
      queryDataSet = new QueryDataSet();
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      queryDataSet.refresh();

      if (queryDataSet.isEmpty()) dbRet.setNoError(0);
      else dbRet.setRetStr( queryDataSet.format(returnColumnName) );
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      
      e.printStackTrace();
    }
    finally {
      if (queryDataSet != null) queryDataSet.close();
    }
    
    director.freeDBConnection(getDatabaseId(), database);
    
    return dbRet;
  }
  
  protected Order _order = null;
  
  public Order getOrder() {
    return _order;
  }

  public void setCustomerId(String customerId) {
    _customerId = customerId;
  }

  public String getCustomerId() {
    return _customerId;
  }
  
  public void setCustLogCode(String custLogCode) {
    _custLogCode = custLogCode;
  }

  public String getCustLogCode() {
    return _custLogCode;
  }

  public String getDatabaseId() {
    return _databaseId;
  }
  
  public void setAuthUsername(String authUsername) {
    _authUsername = authUsername;
  }

  public String getAuthUsername() {
    return _authUsername;
  }
  
  public void setAuthPassword(String authPassword) {
    _authPassword = authPassword;
  }

  public String getAuthPassword() {
    return _authPassword;
  }

  public void setAccessLevel(int accessLevel) {
    _accessLevel = accessLevel;
  }

  public int getAccessLevel() {
    return _accessLevel;
  }

  public void setDateCreated(Timestamp dateCreated) {
    _dateCreated = dateCreated;
  }

  public Timestamp getDateCreated() {
    return _dateCreated;
  }

  public void setDateLastUsed(Timestamp dateLastUsed) {
    _dateLastUsed = dateLastUsed;
  }

  public Timestamp getDateLastUsed() {
    return _dateLastUsed;
  }

  public void setFirstname(String firstname) {
    _firstname = firstname;
  }

  public String getFirstname() {
    return _firstname;
  }

  public void setLastname(String lastname) {
    _lastname = lastname;
  }

  public String getLastname() {
    return _lastname;
  }  

  public void setOccupation(String occupation) {
    _occupation = occupation;
  }

  public String getOccupation() {
    return _occupation;
  }
  
  public void setEmail(String email) {
    _email = email;
  }

  public String getEmail() {
    return _email;
  }
  
  public void setPhone(String phone) {
    _phone = phone;
  }

  public String getPhone() {
    return _phone;
  }
  
  public void setFax(String fax) {
    _fax = fax;
  }

  public String getFax() {
    return _fax;
  }
  
  public void setTitle(String title) {
    _title = title;
  }

  public String getTitle() {
    return _title;
  }

  public void setSex(String sex) {
    _sex = sex;
  }

  public String getSex() {
    return _sex;
  }

  public void setDiscountPct(BigDecimal discountPct) {
    _discountPct = discountPct;
    
    _order.setDiscountPct(discountPct);
  }

  public BigDecimal getDiscountPct() {
    return _discountPct;
  }

  public void setCustomerType(int customerType) {
    _customerType = customerType;
  }

  public int getCustomerType() {
    return _customerType;
  }
  
  public void setContactUsId(int contactUsId) {
    _contactUsId = contactUsId;
  }

  public int getContactUsId() {
    return _contactUsId;
  }

  public void setContactUsDescr(String contactUsDescr) {
    _contactUsDescr = contactUsDescr;
  }

  public String getContactUsDescr() {
    return _contactUsDescr;
  }
  
  public void setBirthDate(Timestamp birthDate) {
    _birthDate = birthDate;
  }

  public Timestamp getBirthDate() {
    return _birthDate;
  }

  public void setCustLang(String custLang) {
    _custLang = custLang;
  }

  public String getCustLang() {
    return _custLang;
  }

  public void setLocaleLanguage(String localeLanguage) {
    _localeLanguage = localeLanguage;
  }

  public String getLocaleLanguage() {
    return _localeLanguage;
  }
  
  public void setLocaleCountry(String localeCountry) {
    _localeCountry = localeCountry;
  }

  public String getLocaleCountry() {
    return _localeCountry;
  }
  
  public void setCurr1DisplayScale(int curr1DisplayScale) {
    _curr1DisplayScale = curr1DisplayScale;
  }

  public int getCurr1DisplayScale() {
    return _curr1DisplayScale;
  }
  
  public void setMinCurr1DispFractionDigits(int minCurr1DispFractionDigits) {
    _minCurr1DispFractionDigits = minCurr1DispFractionDigits;
  }

  public int getMinCurr1DispFractionDigits() {
    return _minCurr1DispFractionDigits;
  }
  
  public String getRSCode() {
    return _RSCode;
  }
  
  public String getRSCode2() {
    return _RSCode2;
  }
  
  public String getRSUsrName() {
    return _RSUsrName;
  }
  
  public void setSBCodeBilling(String SBCodeBilling) {
    _SBCodeBilling = SBCodeBilling;
  }
  public String getSBCodeBilling() {
    return _SBCodeBilling;
  }
  
  public void setBillingName(String billingName) {
    _billingName = billingName;
  }

  public String getBillingName() {
    return _billingName;
  }
  
  public void setBillingProfession(String billingProfession) {
    _billingProfession = billingProfession;
  }

  public String getBillingProfession() {
    return _billingProfession;
  }

  public void setBillingAddress(String billingAddress) {
    _billingAddress = billingAddress;
  }

  public String getBillingAddress() {
    return _billingAddress;
  }

  public void setBillingArea(String billingArea) {
    _billingArea = billingArea;
  }

  public String getBillingArea() {
    return _billingArea;
  }

  public void setBillingCity(String billingCity) {
    _billingCity = billingCity;
  }

  public String getBillingCity() {
    return _billingCity;
  }

  public void setBillingRegion(String billingRegion) {
    _billingRegion = billingRegion;
  }

  public String getBillingRegion() {
    return _billingRegion;
  }

  public void setBillingCountryCode(String billingCountryCode) {
    setBillingCountryCode(billingCountryCode,null);
  }
  public void setBillingCountryCode(String billingCountryCode, Database database) {
    _billingCountryCode = billingCountryCode;
    
    String countryName = "", query = null;
    
    if (_billingCountryCode != null && _billingCountryCode.length()>0) {
      query = "SELECT countryName" + getCustLang() + " FROM country WHERE countryCode = '" + _billingCountryCode + "'";
      
      SQLHelper2 sql = new SQLHelper2();
      
      if (database != null) sql.getSQL(database, query);
      else sql.getSQL(query);
      
      countryName = sql.getColumn("countryName" + getCustLang());
    }
    
    setBillingCountry(countryName);
  }

  public String getBillingCountryCode() {
    return _billingCountryCode;
  }
  
  public void setBillingCountry(String billingCountry) {
    _billingCountry = billingCountry;
  }

  public String getBillingCountry() {
    return _billingCountry;
  }

  public void setBillingZipCode(String billingZipCode) {
    _billingZipCode = billingZipCode;
  }

  public String getBillingZipCode() {
    return _billingZipCode;
  }

  public void setBillingPhone(String billingPhone) {
    _billingPhone = billingPhone;
  }
  public String getBillingPhone() {
    return _billingPhone;
  }
  
  public void setBillingCellPhone(String billingCellPhone) {
    _billingCellPhone = billingCellPhone;
  }
  public String getBillingCellPhone() {
    return _billingCellPhone;
  }
  
  public void setBillingFax(String billingFax) {
    _billingFax = billingFax;
  }
  public String getBillingFax() {
    return _billingFax;
  }

  public void setBillingAfm(String billingAfm) {
    _billingAfm = billingAfm;
  }

  public String getBillingAfm() {
    return _billingAfm;
  }

  public void setBillingDoy(String billingDoy) {
    _billingDoy = billingDoy;
  }

  public String getBillingDoy() {
    return _billingDoy;
  }

  public void setBillingCreditType(String billingCreditType) {
    _billingCreditType = billingCreditType;
  }

  public String getBillingCreditType() {
    return _billingCreditType;
  }

  public void setBillingCreditExpDay(Timestamp billingCreditExpDay) {
    _billingCreditExpDay = billingCreditExpDay;
  }

  public Timestamp getBillingCreditExpDay() {
    return _billingCreditExpDay;
  }

  public void setBillingCreditNum(String billingCreditNum) {
    _billingCreditNum = billingCreditNum;
  }

  public String getBillingCreditNum() {
    return _billingCreditNum;
  }

  public void setBillingCreditExpMonth(String billingCreditExpMonth) {
    _billingCreditExpMonth = billingCreditExpMonth;
  }

  public String getBillingCreditExpMonth() {
    return _billingCreditExpMonth;
  }

  public void setBillingCreditExpYear(String billingCreditExpYear) {
    _billingCreditExpYear = billingCreditExpYear;
  }

  public String getBillingCreditExpYear() {
    return _billingCreditExpYear;
  }

  public void setBillingCreditOwner(String billingCreditOwner) {
    _billingCreditOwner = billingCreditOwner;
  }

  public String getBillingCreditOwner() {
    return _billingCreditOwner;
  }

  

  public void setShippingName(String shippingName) {
    _shippingName = shippingName;
  }

  public String getShippingName() {
    return _shippingName;
  }

  public void setShippingAddress(String shippingAddress) {
    _shippingAddress = shippingAddress;
  }

  public String getShippingAddress() {
    return _shippingAddress;
  }

  public void setShippingArea(String shippingArea) {
    _shippingArea = shippingArea;
  }

  public String getShippingArea() {
    return _shippingArea;
  }

  public void setShippingAreaZone(String shippingAreaZone) {
    _shippingAreaZone = shippingAreaZone;
  }

  public String getShippingAreaZone() {
    return _shippingAreaZone;
  }

  public void setShippingCity(String shippingCity) {
    _shippingCity = shippingCity;
  }

  public String getShippingCity() {
    return _shippingCity;
  }

  public void setShippingRegion(String shippingRegion) {
    _shippingRegion = shippingRegion;
  }

  public String getShippingRegion() {
    return _shippingRegion;
  }

  public void setShippingCountryCode(String shippingCountryCode) {
    _shippingCountryCode = shippingCountryCode;
    
    String countryName = "", query = null;
    
    if (_shippingCountryCode != null && _shippingCountryCode.length()>0) {
      query = "SELECT countryName" + getCustLang() + " FROM country"
            + " WHERE countryCode = '" + _shippingCountryCode + "'";
      
      countryName = getColumnInfo(query, "countryName" + getCustLang()).getRetStr();
    }
    
    setShippingCountry(countryName);
  }

  public String getShippingCountryCode() {
    return _shippingCountryCode;
  }
  
  public void setShippingCountry(String shippingCountry) {
    _shippingCountry = shippingCountry;
  }

  public String getShippingCountry() {
    return _shippingCountry;
  }

  public void setShippingCountryZone(String shippingCountryZone) {
    _shippingCountryZone = shippingCountryZone;
  }

  public String getShippingCountryZone() {
    return _shippingCountryZone;
  }

  public void setShippingZipCode(String shippingZipCode) {
    _shippingZipCode = shippingZipCode;
  }

  public String getShippingZipCode() {
    return _shippingZipCode;
  }

  public void setShippingPhone(String shippingPhone) {
    _shippingPhone = shippingPhone;
  }

  public String getShippingPhone() {
    return _shippingPhone;
  }
  
  public void setShippingLocationCode(String shippingLocationCode) {
    _shippingLocationCode = shippingLocationCode;
    
    if (_shippingLocationCode.length() > 4) setShippingDistrictCode(_shippingLocationCode.substring(0,4));
    else setShippingDistrictCode("");
    
    String SLName = "", query = null;
    
    if (_shippingLocationCode != null && _shippingLocationCode.length()>0) {
      query = "SELECT SLName" + getCustLang() + " FROM shippingLocation"
            + " WHERE SLCode = '" + _shippingLocationCode + "'";
      
      DbRet dbRet = getColumnInfo(query, "SLName" + getCustLang());
      
      SLName = dbRet.getRetStr();
    }
    
    setShippingLocation(SLName);
  }
  public String getShippingLocationCode() {
    return _shippingLocationCode;
  }
  public void setShippingLocation(String shippingLocation) {
    _shippingLocation = shippingLocation;
  }
  public String getShippingLocation() {
    return _shippingLocation;
  }
  
  public void setShippingDistrictCode(String shippingDistrictCode) {
    _shippingDistrictCode = shippingDistrictCode;
    
    String SLName = "", query = null;
    
    if (_shippingDistrictCode != null && _shippingDistrictCode.length()>0) {
      query = "SELECT SLName" + getCustLang() + " FROM shippingLocation"
            + " WHERE SLCode = '" + _shippingDistrictCode + "'";
      
      DbRet dbRet = getColumnInfo(query, "SLName" + getCustLang());
      
      SLName = dbRet.getRetStr();
    }
    
    setShippingDistrict(SLName);
  }
  public String getShippingDistrictCode() {
    return _shippingDistrictCode;
  }
  public void setShippingDistrict(String shippingDistrict) {
    _shippingDistrict = shippingDistrict;
  }
  public String getShippingDistrict() {
    return _shippingDistrict;
  }
  
  public void setCurrencyCode(String currencyCode) {
    _currencyCode = currencyCode;
  }

  public String getCurrencyCode() {
    return _currencyCode;
  }
  
  public void setOrdPrefNotes(String ordPrefNotes) {
    _ordPrefNotes = ordPrefNotes;
  }

  public String getOrdPrefNotes() {
    return _ordPrefNotes;
  }
  
  public int getHotdealBuysCnt() {
    return _hotdealBuysCnt;
  }
  public void setHotdealBuysCnt(int hotdealBuysCnt) {
    _hotdealBuysCnt = hotdealBuysCnt;
  }

  public String getLastIPUsed() {
    return _lastIPUsed;
  }
  public void setLastIPUsed(String lastIPUsed) {
    _lastIPUsed = lastIPUsed;
  }
  
  public void setShippingDeliveryDate(Timestamp deliveryDate) {
    _deliveryDate = deliveryDate;
  }
  
  public Timestamp getShippingDeliveryDate() {
    if (_deliveryDate == null) _deliveryDate = SwissKnife.currentDate();
    
    return _deliveryDate;
  }
  
  private BigDecimal _zero = new BigDecimal("0");
  
  protected String _customerId = "";
  
  protected String _custLogCode;
  protected String _authUsername;
  protected String _authPassword;
  protected String _databaseId;
  protected String _firstname;
  protected String _lastname;
  protected String _occupation;
  protected String _email;
  protected String _phone;
  protected String _fax;
  protected String _title;
  protected String _sex;
  protected String _contactUsDescr;
  protected String _custLang;
  protected int _accessLevel = 0;
  protected int _customerType = -1;
  protected int _contactUsId;
  protected Timestamp _dateCreated;
  protected Timestamp _dateLastUsed;
  protected Timestamp _birthDate;
  
  protected BigDecimal _discountPct = _zero;
  
  protected String _localeLanguage;
  protected String _localeCountry;
  protected int _curr1DisplayScale = 2;
  protected int _minCurr1DispFractionDigits = 2;
  
  // retail shop
  protected String _RSCode = "";
  protected String _RSCode2 = "";
  protected String _RSUsrName = "";
  
  protected String _SBCodeBilling = "";
  protected String _billingName = "";
  protected String _billingProfession = "";
  protected String _billingAddress = "";
  protected String _billingArea = "";
  protected String _billingCity = "";
  protected String _billingRegion = "";
  protected String _billingCountry = "";
  protected String _billingCountryCode = "";
  protected String _billingZipCode = "";
  
  protected String _billingPhone = "";
  protected String _billingCellPhone = "";
  
  protected String _billingFax = "";
  protected String _billingAfm = "";
  protected String _billingDoy = "";
  protected String _billingCreditType = "";
  protected String _billingCreditExpMonth = "";
  protected String _billingCreditExpYear = "";
  protected String _billingCreditNum = "";
  protected String _billingCreditOwner = "";
  protected Timestamp _billingCreditExpDay = null;
 
  protected String _shippingName = "";
  protected String _shippingAddress = "";
  protected String _shippingArea = "";
  protected String _shippingAreaZone = "";
  protected String _shippingCity = "";
  protected String _shippingRegion = "";
  protected String _shippingCountry = "";
  protected String _shippingCountryCode = "";
  protected String _shippingCountryZone = "";
  protected String _shippingZipCode = "";
  protected String _shippingPhone = "";
  
  protected String _shippingDistrict = "";
  protected String _shippingDistrictCode = "";
  protected String _shippingLocation = "";
  protected String _shippingLocationCode = "";
  
  protected int _hotdealBuysCnt = 0;
  protected String _lastIPUsed = "";
  
  protected String _currencyCode = "";
  
  protected Timestamp _deliveryDate = null;
  
  protected String _ordPrefNotes = "";
}