/*
 * Customer.java
 *
 * Created on 30 Δεκέμβριος 2003, 1:42 μμ
 */

package gr.softways.dev.eshop.customer;

import java.math.BigDecimal;
import java.sql.Timestamp;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

/**
 *
 * @author  minotauros
 */
public class Customer {
  
  public static String SHIPPING_INFO = "1";
  public static String BILLING_INFO = "2";
  
  protected Customer() {
  }
  
  public static DbRet newUser(Database database, String usrName, 
                              String usrPasswd, String usrAccessLevel,
                              String lastIpUsed, Timestamp dateLastUsed) {
    DbRet dbRet = new DbRet();
    dbRet.setRetry(1);
    
    int retries = 0;
    for (; dbRet.getRetry() == 1 && retries < 30; retries++) {
      String logCode = SwissKnife.buildPK();
      
      String query = "INSERT INTO users ("
                   + "logCode,usrName,usrPasswd,usrAccessLevel"
                   + ",lastIpUsed,dateLastUsed"
                   + ") VALUES ("
                   + "'" + logCode + "'"
                   + ",'" + usrName + "'"
                   + ",'" + usrPasswd + "'"
                   + "," + usrAccessLevel
                   + ",'" + lastIpUsed + "'";
      
      if (dateLastUsed == null) {
        query += ",null";
      }
      else {
        query += ",'" + dateLastUsed + "'";
      }
      
      query += ")";
      
      dbRet = database.execQuery(query);
      
      dbRet.setRetStr(logCode);
    }
    
    return dbRet;
  }
  
  public static DbRet newCustomer(Database database,String custLogCode,
                                  Timestamp dateCreated,Timestamp dateLastUsed,
                                  String firstname,String firstnameUp,
                                  String lastname,String lastnameUp,
                                  String occupation,String email,
                                  String phone,String fax,String title,
                                  String sex,BigDecimal discountPct,
                                  String customerType,String contactUsId,
                                  String contactUsDescr,Timestamp birthDate,
                                  BigDecimal purchaseVal,BigDecimal purchaseValEU,
                                  BigDecimal purchaseValCUR1,BigDecimal purchaseValCUR2,
                                  String hotdealBuysCnt,String buysCnt,String custLang) {
    DbRet dbRet = new DbRet();
    dbRet.setRetry(1);
    
    int retries = 0;
    for (; dbRet.getRetry() == 1 && retries < 30; retries++) {
      String customerId = SwissKnife.buildPK();
      
      String query = "INSERT INTO customer ("
                   + "customerId,custLogCode,dateCreated,dateLastUsed"
                   + ",firstname,firstnameUp,lastname,lastnameUp"
                   + ",occupation,email,phone,fax,title"
                   + ",sex,discountPct,customerType,contactUsId"
                   + ",contactUsDescr,birthDate,purchaseVal,purchaseValEU"
                   + ",purchaseValCUR1,purchaseValCUR2,hotdealBuysCnt"
                   + ",buysCnt,custLang"
                   + ") VALUES ("
                   + "'" + customerId + "'"
                   + ",'" + custLogCode + "'";
      
      if (dateCreated == null) {
        query += ",null";
      }
      else {
        query += ",'" + dateCreated + "'";
      }
      
      if (dateLastUsed == null) {
        query += ",null";
      }
      else {
        query += ",'" + dateLastUsed + "'";
      }
      
      query += ",'" + firstname + "'"
             + ",'" + firstnameUp + "'"
             + ",'" + lastname + "'"
             + ",'" + lastnameUp + "'"
             + ",'" + occupation + "'"
             + ",'" + email + "'"
             + ",'" + phone + "'"
             + ",'" + fax + "'"
             + ",'" + title + "'"
             + ",'" + sex + "'"
             + "," + discountPct
             + "," + customerType
             + "," + contactUsId
             + ",'" + contactUsDescr + "'";
      
      if (birthDate == null) {
        query += ",null";
      }
      else {
        query += ",'" + birthDate + "'";
      }
      
      query += "," + purchaseVal
             + "," + purchaseValEU
             + "," + purchaseValCUR1
             + "," + purchaseValCUR2
             + "," + hotdealBuysCnt
             + "," + buysCnt
             + ",'" + custLang + "'"
             + ")";
      
      dbRet = database.execQuery(query);
      
      dbRet.setRetStr(customerId);
    }
    
    return dbRet;
  }
  
  public static DbRet newShipBillInfo(Database database,String SBCustomerId,
                                      String SBSorB,String SBName,
                                      String SBProfession,String SBAddress,
                                      String SBAreaCode,String SBCity,
                                      String SBRegion,String SBCountryCode,
                                      String SBZipCode,String SBPhone,
                                      String SBFax,String SBEmail,
                                      String SBAfm,String SBDoy,
                                      String SBCreditType, 
                                      Timestamp SBCreditExpDay,
                                      String SBCreditNum,
                                      String SBCreditOwner,String SBLang) {
    DbRet dbRet = new DbRet();
    dbRet.setRetry(1);
    
    int retries = 0;
    for (; dbRet.getRetry() == 1 && retries < 30; retries++) {      
      String query = "INSERT INTO shipBillInfo ("
                   + "SBCode,SBCustomerId,SBSorB,SBName"
                   + ",SBProfession,SBAddress,SBAreaCode"
                   + ",SBCity,SBRegion,SBCountryCode,SBZipCode"
                   + ",SBPhone,SBFax,SBEmail,SBAfm,SBDoy"
                   + ",SBCreditType,SBCreditExpDay,SBCreditNum"
                   + ",SBCreditOwner,SBLang"
                   + ") VALUES ("
                   + "'" + SwissKnife.buildPK() + "'"
                   + ",'" + SBCustomerId + "'"
                   + ",'" + SBSorB + "'"
                   + ",'" + SBName + "'"
                   + ",'" + SBProfession + "'"
                   + ",'" + SBAddress + "'"
                   + ",'" + SBAreaCode + "'"
                   + ",'" + SBCity + "'"
                   + ",'" + SBRegion + "'"
                   + ",'" + SBCountryCode + "'"
                   + ",'" + SBZipCode + "'"
                   + ",'" + SBPhone + "'"
                   + ",'" + SBFax + "'"
                   + ",'" + SBEmail + "'"
                   + ",'" + SBAfm + "'"
                   + ",'" + SBDoy + "'"
                   + ",'" + SBCreditType + "'";
      
      if (SBCreditExpDay == null) {
        query += ",null";
      }
      else {
        query += ",'" + SBCreditExpDay + "'";
      }
      
      query += ",'" + SBCreditNum + "'"
             + ",'" + SBCreditOwner + "'"
             + ",'" + SBLang + "'"
             + ")";
      
      dbRet = database.execQuery(query);
    }
    
    return dbRet;                                        
  }
    
  public static DbRet newCustMonthly(Database database, String customerId) {
        
    String procParams[] = new String[2], procParamDlm[] = new String[2];

    DbRet dbRet = new DbRet();
    dbRet.setRetry(1);
    
    int retries = 0, year = 0;
    for (; dbRet.getRetry() == 1 && retries < 30; retries++) {

      year = SwissKnife.getTDateInt(SwissKnife.currentDate(),"YEAR");

      procParams[0] = customerId;
      procParamDlm[0] = "'";
      procParams[1] = String.valueOf(year);
      procParamDlm[1] = "";

      dbRet = database.execProcedure("newCustMonthly",procParams, procParamDlm);

      if (dbRet.getNoError() == 1) {
        year++;
        procParams[0] = customerId;
        procParamDlm[0] = "'";
        procParams[1] = String.valueOf(year);
        procParamDlm[1] = "";

        dbRet = database.execProcedure("newCustMonthly",procParams,procParamDlm);
      }
    }
    
    return dbRet;
  }
  
  public static DbRet newCustZones(Database database, String customerId) {
    String procParams[] = new String[1], procParamDlm[] = new String[1];

    procParams[0] = customerId;
    procParamDlm[0] = "'";

    return database.execProcedure("newCustZones",procParams,procParamDlm);
  }
  
  public static DbRet updateUser(Database database, 
                                 String logCode, String usrName, 
                                 String usrPasswd, String usrAccessLevel) {
    DbRet dbRet = new DbRet();
    
    String query = "UPDATE users SET"
                 + " usrName = '" + usrName + "'";
    
    if (usrPasswd != null && usrPasswd.length()>0) {
      query += ",usrPasswd = '" + usrPasswd + "'";
    }
    
    query += ",usrAccessLevel = '" + usrAccessLevel + "'"
           + " WHERE logCode = '" + logCode + "'";
    
    dbRet = database.execQuery(query);
    
    return dbRet;
  }
  
  public static DbRet updateCustomer(Database database,String customerId,
                                     String firstname,String firstnameUp,
                                     String lastname,String lastnameUp,
                                     String occupation,String email,
                                     String phone,String fax,String title,
                                     String sex,BigDecimal discountPct,
                                     String customerType,String contactUsId,
                                     String contactUsDescr,Timestamp birthDate,
                                     String custLang) {
    DbRet dbRet = new DbRet();
    
    String query = "UPDATE customer SET"
                 + " firstname = '" + firstname + "'"
                 + ",firstnameUp = '" + firstnameUp + "'"
                 + ",lastname = '" + lastname + "'"
                 + ",lastnameUp = '" + lastnameUp + "'"
                 + ",occupation = '" + occupation + "'"
                 + ",email = '" + email + "'"
                 + ",phone = '" + phone + "'"
                 + ",fax = '" + fax + "'"
                 + ",title = '" + title + "'"
                 + ",sex = '" + sex + "'"
                 + ",discountPct = " + discountPct
                 + ",customerType = " + customerType
                 + ",contactUsId = " + contactUsId
                 + ",contactUsDescr = '" + contactUsDescr + "'";
                 
    if (birthDate != null) {
      query += ",birthDate = '" + birthDate + "'";
    }
    else {
      query += ",birthDate = null";
    }
    
    query += ",custLang = '" + custLang + "'"
           + " WHERE customerId = '" + customerId + "'";
    
    dbRet = database.execQuery(query);
    
    return dbRet;
  }
  
  public static DbRet updateShipBillInfo(Database database,String SBCode,
                                         String SBSorB,String SBName,
                                         String SBProfession,
                                         String SBAddress,String SBAreaCode,
                                         String SBCity,String SBRegion,
                                         String SBCountryCode,String SBZipCode,
                                         String SBPhone,String SBFax,String SBEmail,
                                         String SBAfm,String SBDoy,String SBCreditType,
                                         Timestamp SBCreditExpDay,String SBCreditNum,
                                         String SBCreditOwner,String SBLang) {
    DbRet dbRet = new DbRet();
    
    String query = "UPDATE shipBillInfo SET"
                + " SBSorB = '" + SBSorB + "'"
                + ",SBName = '" + SBName + "'"
                + ",SBProfession = '" + SBProfession + "'"
                + ",SBAddress = '" + SBAddress + "'"
                + ",SBAreaCode = '" + SBAreaCode + "'"
                + ",SBCity = '" + SBCity + "'"
                + ",SBRegion = '" + SBRegion + "'"
                + ",SBCountryCode = '" + SBCountryCode + "'"
                + ",SBZipCode = '" + SBZipCode + "'"
                + ",SBPhone = '" + SBPhone + "'"
                + ",SBFax = '" + SBFax + "'"
                + ",SBEmail = '" + SBEmail + "'"
                + ",SBAfm = '" + SBAfm + "'"
                + ",SBDoy = '" + SBDoy + "'"
                + ",SBCreditType = '" + SBCreditType + "'";
                
    if (SBCreditExpDay != null) {
      query += ",SBCreditExpDay = '" + SBCreditExpDay + "'";
    }
    else {
      query += ",SBCreditExpDay = null";
    }
    
    query += ",SBCreditNum = '" + SBCreditNum + "'"
           + ",SBCreditOwner = '" + SBCreditOwner + "'"
           + ",SBLang = '" + SBLang + "'"
           + " WHERE SBCode = '" + SBCode + "'";
    
    dbRet = database.execQuery(query);
    
    return dbRet;
  }
}