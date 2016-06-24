package gr.softways.dev.eshop.emaillists.newsletter;

import java.sql.Timestamp;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

import gr.softways.dev.eshop.emaillists.lists.Present;

/**
 *
 * @author  minotauros
 */
public class Newsletter {
  
  protected Newsletter() {
  }
  
  public static DbRet insert(Database database,String EMLMEmail,String EMLMAltEmail,String EMLMLastName,
                             String EMLMLastNameUp,String EMLMFirstName,Timestamp EMLMBirthDate,
                             Timestamp EMLMRegDate,String EMLMCompanyName,String EMLMCompanyNameUp,
                             String EMLMAddress,String EMLMZipCode,String EMLMCity,String EMLMCountry,
                             String EMLMPhone,String EMLMActive,String EMLMField1,String EMLMField2,
                             String EMLMField3,String EMLRListCode,String receiveEmail) {
    DbRet dbRet = new DbRet();
    
    String EMLMCode = null;
    
    QueryDataSet queryDataSet = null;
    
    String query = "SELECT EMLMCode FROM emailListMember WHERE EMLMEmail = '" + EMLMEmail + "'";
    
    try {
      queryDataSet = new QueryDataSet();
      
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      queryDataSet.refresh();
      
      // new member
      if (queryDataSet.getRowCount() == 0) {
        EMLMCode = SwissKnife.buildPK();
    
        query = "INSERT INTO emailListMember (EMLMCode,EMLMEmail"
              + ",EMLMAltEmail,EMLMLastName,EMLMLastNameUp"
              + ",EMLMFirstName,EMLMBirthDate,EMLMRegDate"
              + ",EMLMCompanyName,EMLMCompanyNameUp,EMLMAddress"
              + ",EMLMZipCode,EMLMCity,EMLMCountry,EMLMPhone,EMLMActive"
              + ",EMLMField1,EMLMField2,EMLMField3"
              + ")"
              + " VALUES ("
              + "'"  + EMLMCode   + "'"
              + ",'" + SwissKnife.sqlEncode(EMLMEmail)  + "'"
              + ",'" + SwissKnife.sqlEncode(EMLMAltEmail)  + "'"
              + ",'" + SwissKnife.sqlEncode(EMLMLastName)  + "'"
              + ",'"  + SwissKnife.sqlEncode(EMLMLastNameUp) + "'"
              + ",'"  + SwissKnife.sqlEncode(EMLMFirstName) + "'";
              
        if (EMLMBirthDate != null) {
          query += ",'" + EMLMBirthDate + "'";
        }
        else {
          query += ",null";
        }
        
        if (EMLMRegDate != null) {
          query += ",'" + EMLMRegDate + "'";
        }
        else {
          query += ",null";
        }
        
        query +=  ",'"  + SwissKnife.sqlEncode(EMLMCompanyName) + "'"
                + ",'"  + SwissKnife.sqlEncode(EMLMCompanyNameUp) + "'"
                + ",'"  + SwissKnife.sqlEncode(EMLMAddress) + "'"
                + ",'"  + SwissKnife.sqlEncode(EMLMZipCode) + "'"
                + ",'"  + SwissKnife.sqlEncode(EMLMCity) + "'"
                + ",'"  + SwissKnife.sqlEncode(EMLMCountry) + "'"
                + ",'"  + SwissKnife.sqlEncode(EMLMPhone) + "'"
                + ",'"  + SwissKnife.sqlEncode(EMLMActive) + "'"
                + ",'"  + SwissKnife.sqlEncode(EMLMField1) + "'"
                + ",'"  + SwissKnife.sqlEncode(EMLMField2) + "'"
                + ",'"  + SwissKnife.sqlEncode(EMLMField3) + "'"
                + ")";
                
        dbRet = database.execQuery(query);
      }
      else {
        EMLMCode = queryDataSet.getString("EMLMCode");
      }
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    finally {
      if (queryDataSet != null) queryDataSet.close();
      
      queryDataSet = null;
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = updateSubscription(database,EMLRListCode,EMLMCode,receiveEmail);
    }
    
    return dbRet;
  }
  
  public static DbRet updateSubscription(Database database,String EMLRListCode,String EMLRMemberCode,String receiveEmail) {
    DbRet dbRet = new DbRet();
    
    String query = "DELETE FROM emailListReg WHERE EMLRListCode = '" + SwissKnife.sqlEncode(EMLRListCode) + "' AND EMLRMemberCode = '" + SwissKnife.sqlEncode(EMLRMemberCode) + "'";
    
    dbRet = database.execQuery(query);
    
    if (receiveEmail.equals(Present.STATUS_ACTIVE)) {
      query = "UPDATE emailListMember SET EMLMActive = '" + SwissKnife.sqlEncode(receiveEmail) + "'"
            + " WHERE EMLMCode = '" + SwissKnife.sqlEncode(EMLRMemberCode) + "'";
          
      dbRet = database.execQuery(query);
    }
    
    if (receiveEmail.equals(Present.STATUS_ACTIVE)) {
      query = "INSERT INTO emailListReg (EMLRCode,EMLRListCode,EMLRMemberCode"
            + ") VALUES ("
            + "'"  + SwissKnife.buildPK() + "'"
            + ",'" + SwissKnife.sqlEncode(EMLRListCode)  + "'"
            + ",'" + SwissKnife.sqlEncode(EMLRMemberCode)  + "'"
            + ")";
            
      dbRet = database.execQuery(query);
    }
    
    return dbRet;
  }
  
  public static DbRet update(Database database,String EMLMEmail,String EMLMAltEmail,String EMLMLastName,
                             String EMLMLastNameUp,String EMLMFirstName,Timestamp EMLMBirthDate,
                             Timestamp EMLMRegDate,String EMLMCompanyName,String EMLMCompanyNameUp,
                             String EMLMAddress,String EMLMZipCode,String EMLMCity,String EMLMCountry,
                             String EMLMPhone,String EMLMActive,String EMLMField1,String EMLMField2,
                             String EMLMField3,String EMLRListCode,String receiveEmail) {
    DbRet dbRet = new DbRet();
    
    String EMLMCode = null;
    
    QueryDataSet queryDataSet = null;
    
    String query = "SELECT EMLMCode FROM emailListMember WHERE EMLMEmail = '" + EMLMEmail + "'";
    
    try {
      queryDataSet = new QueryDataSet();
      
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      queryDataSet.refresh();
      
      // new member
      if (queryDataSet.getRowCount() == 0) {
        EMLMCode = SwissKnife.buildPK();
    
        query = "INSERT INTO emailListMember (EMLMCode,EMLMEmail"
              + ",EMLMAltEmail,EMLMLastName,EMLMLastNameUp"
              + ",EMLMFirstName,EMLMBirthDate,EMLMRegDate"
              + ",EMLMCompanyName,EMLMCompanyNameUp,EMLMAddress"
              + ",EMLMZipCode,EMLMCity,EMLMCountry,EMLMPhone,EMLMActive"
              + ",EMLMField1,EMLMField2,EMLMField3"
              + ")"
              + " VALUES ("
              + "'"  + EMLMCode   + "'"
              + ",'" + SwissKnife.sqlEncode(EMLMEmail)  + "'"
              + ",'" + SwissKnife.sqlEncode(EMLMAltEmail)  + "'"
              + ",'" + SwissKnife.sqlEncode(EMLMLastName)  + "'"
              + ",'"  + SwissKnife.sqlEncode(EMLMLastNameUp) + "'"
              + ",'"  + SwissKnife.sqlEncode(EMLMFirstName) + "'";
              
        if (EMLMBirthDate != null) {
          query += ",'" + EMLMBirthDate + "'";
        }
        else {
          query += ",null";
        }
        
        if (EMLMRegDate != null) {
          query += ",'" + EMLMRegDate + "'";
        }
        else {
          query += ",null";
        }
        
        query +=  ",'"  + SwissKnife.sqlEncode(EMLMCompanyName) + "'"
                + ",'"  + SwissKnife.sqlEncode(EMLMCompanyNameUp) + "'"
                + ",'"  + SwissKnife.sqlEncode(EMLMAddress) + "'"
                + ",'"  + SwissKnife.sqlEncode(EMLMZipCode) + "'"
                + ",'"  + SwissKnife.sqlEncode(EMLMCity) + "'"
                + ",'"  + SwissKnife.sqlEncode(EMLMCountry) + "'"
                + ",'"  + SwissKnife.sqlEncode(EMLMPhone) + "'"
                + ",'"  + SwissKnife.sqlEncode(EMLMActive) + "'"
                + ",'"  + SwissKnife.sqlEncode(EMLMField1) + "'"
                + ",'"  + SwissKnife.sqlEncode(EMLMField2) + "'"
                + ",'"  + SwissKnife.sqlEncode(EMLMField3) + "'"
                + ")";
                
        dbRet = database.execQuery(query);
      }
      else {
        EMLMCode = queryDataSet.getString("EMLMCode");
        
        query = "UPDATE emailListMember SET"
            + " EMLMEmail = '" + SwissKnife.sqlEncode(EMLMEmail) + "'"
            + ",EMLMAltEmail = '" + SwissKnife.sqlEncode(EMLMAltEmail) + "'"
            + ",EMLMLastName = '" + SwissKnife.sqlEncode(EMLMLastName) + "'"
            + ",EMLMLastNameUp = '" + SwissKnife.sqlEncode(EMLMLastNameUp) + "'"
            + ",EMLMFirstName = '" + SwissKnife.sqlEncode(EMLMFirstName) + "'";
        
        if (EMLMBirthDate != null) {
          query += ",'" + EMLMBirthDate + "'";
        }
        else {
          query += ",EMLMBirthDate = null";
        }
        
        if (EMLMRegDate != null) {
          query += ",'" + EMLMRegDate + "'";
        }
        else {
          query += ",EMLMRegDate = null";
        }
        
        query += ",EMLMCompanyName = '" + SwissKnife.sqlEncode(EMLMCompanyName) + "'"
            + ",EMLMCompanyNameUp = '" + SwissKnife.sqlEncode(EMLMCompanyNameUp) + "'"
            + ",EMLMAddress = '" + SwissKnife.sqlEncode(EMLMAddress) + "'"
            + ",EMLMZipCode = '" + SwissKnife.sqlEncode(EMLMZipCode) + "'"
            + ",EMLMCity = '" + SwissKnife.sqlEncode(EMLMCity) + "'"
            + ",EMLMCountry = '" + SwissKnife.sqlEncode(EMLMCountry) + "'"
            + ",EMLMPhone = '" + SwissKnife.sqlEncode(EMLMPhone) + "'"
            + ",EMLMField1 = '" + SwissKnife.sqlEncode(EMLMField1) + "'"
            + ",EMLMField2 = '" + SwissKnife.sqlEncode(EMLMField2) + "'"
            + ",EMLMField3 = '" + SwissKnife.sqlEncode(EMLMField3) + "'"
            + " WHERE EMLMCode = '" + EMLMCode + "'";
        
        dbRet = database.execQuery(query);
      }
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    finally {
      if (queryDataSet != null) queryDataSet.close();
      
      queryDataSet = null;
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = updateListSubscription(database,EMLRListCode,EMLMCode,receiveEmail);
    }
    
    return dbRet;
  }
  
  public static DbRet updateListSubscription(Database database,String EMLRListCode,String EMLRMemberCode,String receiveEmail) {
    DbRet dbRet = new DbRet();
    
    String query = null;
    
    if ("0".equals(receiveEmail)) {
      query = "DELETE FROM emailListReg WHERE EMLRListCode = '" + SwissKnife.sqlEncode(EMLRListCode) + "' AND EMLRMemberCode = '" + SwissKnife.sqlEncode(EMLRMemberCode) + "'";
      
      dbRet = database.execQuery(query);
    }
    else if ("1".equals(receiveEmail)) {
      String EMLRCode = "";
      
      QueryDataSet queryDataSet = null;
    
      query = "SELECT EMLRCode FROM emailListReg WHERE EMLRListCode = '" + SwissKnife.sqlEncode(EMLRListCode) + "' AND EMLRMemberCode = '" + SwissKnife.sqlEncode(EMLRMemberCode) + "'";
      
      try {
        queryDataSet = new QueryDataSet();

        queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
        queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

        queryDataSet.refresh();
      
        if (queryDataSet.getRowCount() > 0) EMLRCode = queryDataSet.getString("EMLRCode");
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
      finally {
        if (queryDataSet != null) queryDataSet.close();

        queryDataSet = null;
      }
      
      if (dbRet.getNoError() == 1 && EMLRCode.length() == 0) {
        query = "INSERT INTO emailListReg (EMLRCode,EMLRListCode,EMLRMemberCode"
              + ") VALUES ("
              + "'"  + SwissKnife.buildPK() + "'"
              + ",'" + SwissKnife.sqlEncode(EMLRListCode)  + "'"
              + ",'" + SwissKnife.sqlEncode(EMLRMemberCode)  + "'"
              + ")";

        dbRet = database.execQuery(query);
      }
    }
    else dbRet.setNoError(0);
    
    return dbRet;
  }
  
  public static DbRet checkListStatus(String EMLMEmail,String EMLTField1) {
    DbRet dbRet = new DbRet();
    
    String query = null, EMLRListCode = "",  EMLRMemberCode = "";
    
    Director director = Director.getInstance();

    Database database = director.getDBConnection(_databaseId);
    
    QueryDataSet queryDataSet = null;
    
    EMLRMemberCode = getEMLMCode(database, EMLMEmail).getRetStr();
    EMLRListCode = getDefEMLRListCode(database, EMLTField1).getRetStr();
    
    query = "SELECT EMLRCode FROM emailListReg WHERE EMLRListCode = '" + SwissKnife.sqlEncode(EMLRListCode) + "' AND EMLRMemberCode = '" + SwissKnife.sqlEncode(EMLRMemberCode) + "'";

    try {
      queryDataSet = new QueryDataSet();

      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();

      if (queryDataSet.getRowCount() >= 1) {
        dbRet.setRetInt(1);
        dbRet.setRetStr(queryDataSet.getString("EMLRCode"));
      }
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    finally {
      if (queryDataSet != null) queryDataSet.close();

      queryDataSet = null;
    }
    
    director.freeDBConnection(_databaseId, database);
   
    return dbRet;
  }
  
  public static DbRet getDefEMLRListCode(Database database, String EMLTField1) {
    DbRet dbRet = new DbRet();
    
    String query = "SELECT EMLTCode FROM emailListTab WHERE EMLTField1 = '" + SwissKnife.sqlEncode(EMLTField1) + "'";
    
    QueryDataSet queryDataSet = null;
    
    try {
      queryDataSet = new QueryDataSet();
      
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      queryDataSet.refresh();
      
      if (queryDataSet.isEmpty() == true) throw new Exception();

      dbRet.setRetStr( queryDataSet.getString("EMLTCode") );
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      dbRet.setRetStr(null);
      
      //e.printStackTrace();
    }
    finally {
      if (queryDataSet != null) queryDataSet.close();
    }
    
    return dbRet;
  }
  
  public static DbRet getEMLMCode(Database database, String EMLMEmail) {
    DbRet dbRet = new DbRet();
    
    String query = "SELECT EMLMCode FROM emailListMember WHERE EMLMEmail = '" + EMLMEmail.toLowerCase() + "'";
    
    QueryDataSet queryDataSet = null;
    
    try {
      queryDataSet = new QueryDataSet();
      
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      
      queryDataSet.refresh();
      
      if (queryDataSet.getRowCount() > 0) dbRet.setRetStr( queryDataSet.getString("EMLMCode") );
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
  
  public static DbRet getEmailListCount(String EMLTCode) {
    DbRet dbRet = new DbRet();
    
    String query = null;
    
    Director director = Director.getInstance();

    Database database = director.getDBConnection(_databaseId);
    
    QueryDataSet queryDataSet = null;
    
    query = "SELECT COUNT(*) FROM emailListMember,emailListReg WHERE EMLRMemberCode = EMLMCode AND EMLMActive = '" + Present.STATUS_ACTIVE + "' AND EMLRListCode = '" + SwissKnife.sqlEncode(EMLTCode) + "'";

    try {
      queryDataSet = new QueryDataSet();

      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();

      dbRet.setRetInt(queryDataSet.getInt(0));
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    finally {
      if (queryDataSet != null) queryDataSet.close();

      queryDataSet = null;
    }
    
    director.freeDBConnection(_databaseId, database);
   
    return dbRet;
  }
  
  private static String _databaseId = SwissKnife.jndiLookup("swconf/databaseId");
}