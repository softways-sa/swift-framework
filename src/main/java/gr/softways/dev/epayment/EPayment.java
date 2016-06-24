package gr.softways.dev.epayment;

import java.sql.*;
import java.math.BigDecimal;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

/**
 *
 * @author Panos
 */
public class EPayment {

  public EPayment() {
  }
  
  public DbRet insert() {
    Database database = null;
    
    DbRet dbRet = new DbRet();
    
    PreparedStatement statement = null;
    
    String query = "INSERT INTO EPayment ("
        + "PAYNT_Code,PAYNT_PayDate,PAYNT_CompanyName,PAYNT_IRSNum"
        + ",PAYNT_RefCode,PAYNT_Fullname,PAYNT_Reason,PAYNT_Email,PAYNT_Amount"
        + ",PAYNT_Instalments,PAYNT_Lang"
        + ") VALUES ("
        + "?,?,?,?,?,?,?,?,?,?,?"
        + ")";
    
    int index = 0;
    
    try {
      database = director.getDBConnection(databaseId);
      
      statement = database.createPreparedStatement(query);
      
      statement.setString(++index, getPAYNT_Code());
      
      setPAYNT_PayDate(SwissKnife.currentDate());
      statement.setTimestamp(++index, getPAYNT_PayDate());
      
      statement.setString(++index, getPAYNT_CompanyName());
      
      if (getPAYNT_IRSNum() == null) statement.setNull(++index, Types.VARCHAR);
      else statement.setString(++index, getPAYNT_IRSNum());
      
      if (getPAYNT_RefCode() == null) statement.setNull(++index, Types.VARCHAR);
      else statement.setString(++index, getPAYNT_RefCode());
      
      if (getPAYNT_Fullname() == null) statement.setNull(++index, Types.VARCHAR);
      else statement.setString(++index, getPAYNT_Fullname());
      
      if (getPAYNT_Reason() == null) statement.setNull(++index, Types.VARCHAR);
      else statement.setString(++index, getPAYNT_Reason());
      
      if (getPAYNT_Email() == null) statement.setNull(++index, Types.VARCHAR);
      else statement.setString(++index, getPAYNT_Email());
      
      statement.setBigDecimal(++index, getPAYNT_Amount());
      
      statement.setInt(++index, getPAYNT_Instalments());
      
      if (getPAYNT_Lang() == null) statement.setNull(++index, Types.VARCHAR);
      else statement.setString(++index, getPAYNT_Lang());
      
      statement.executeUpdate();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    finally {
      if (statement != null) try { statement.close(); } catch (Exception e) {}
      director.freeDBConnection(databaseId, database);
    }
    
    return dbRet;
  }

  public BigDecimal getPAYNT_Amount() {
    return PAYNT_Amount;
  }

  public void setPAYNT_Amount(BigDecimal PAYNT_Amount) {
    this.PAYNT_Amount = PAYNT_Amount;
  }

  public String getPAYNT_Code() {
    return PAYNT_Code;
  }

  public void setPAYNT_Code(String PAYNT_Code) {
    this.PAYNT_Code = PAYNT_Code;
  }

  public String getPAYNT_CompanyName() {
    return PAYNT_CompanyName;
  }

  public void setPAYNT_CompanyName(String PAYNT_CompanyName) {
    this.PAYNT_CompanyName = PAYNT_CompanyName;
  }

  public String getPAYNT_Email() {
    return PAYNT_Email;
  }

  public void setPAYNT_Email(String PAYNT_Email) {
    this.PAYNT_Email = PAYNT_Email;
  }

  public String getPAYNT_Fullname() {
    return PAYNT_Fullname;
  }

  public void setPAYNT_Fullname(String PAYNT_Fullname) {
    this.PAYNT_Fullname = PAYNT_Fullname;
  }

  public String getPAYNT_IRSNum() {
    return PAYNT_IRSNum;
  }

  public void setPAYNT_IRSNum(String PAYNT_IRSNum) {
    this.PAYNT_IRSNum = PAYNT_IRSNum;
  }

  public int getPAYNT_Instalments() {
    return PAYNT_Instalments;
  }

  public void setPAYNT_Instalments(int PAYNT_Instalments) {
    this.PAYNT_Instalments = PAYNT_Instalments;
  }

  public String getPAYNT_Lang() {
    return PAYNT_Lang;
  }

  public void setPAYNT_Lang(String PAYNT_Lang) {
    this.PAYNT_Lang = PAYNT_Lang;
  }

  public Timestamp getPAYNT_PayDate() {
    return PAYNT_PayDate;
  }

  public void setPAYNT_PayDate(Timestamp PAYNT_PayDate) {
    this.PAYNT_PayDate = PAYNT_PayDate;
  }

  public String getPAYNT_Reason() {
    return PAYNT_Reason;
  }

  public void setPAYNT_Reason(String PAYNT_Reason) {
    this.PAYNT_Reason = PAYNT_Reason;
  }

  public String getPAYNT_RefCode() {
    return PAYNT_RefCode;
  }

  public void setPAYNT_RefCode(String PAYNT_RefCode) {
    this.PAYNT_RefCode = PAYNT_RefCode;
  }
  
  public static DbRet updateBank(String PAYNT_Code, String PAYNT_BankPayStatus, String PAYNT_BankTransID) {
    Database database = null;
    
    DbRet dbRet = new DbRet();
    
    PreparedStatement statement = null;
    
    String query = "UPDATE EPayment SET"
        + " PAYNT_BankPayStatus = ?"
        + ",PAYNT_BankTransID = ?"
        + " WHERE PAYNT_Code = ?";
    
    int index = 0;
    
    try {
      database = director.getDBConnection(databaseId);
      
      statement = database.createPreparedStatement(query);
      
      statement.setString(++index, SwissKnife.sqlEncode(PAYNT_BankPayStatus));
      statement.setString(++index, SwissKnife.sqlEncode(PAYNT_BankTransID));
      statement.setString(++index, SwissKnife.sqlEncode(PAYNT_Code));
      
      statement.executeUpdate();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    finally {
      if (statement != null) try { statement.close(); } catch (Exception e) {}
      director.freeDBConnection(databaseId, database);
    }
    
    return dbRet;
  }
  
  String PAYNT_Code,PAYNT_CompanyName,PAYNT_IRSNum,PAYNT_RefCode,PAYNT_Fullname,
      PAYNT_Reason,PAYNT_Email,PAYNT_Lang;
    
  private BigDecimal PAYNT_Amount = null;
  
  private Timestamp PAYNT_PayDate  = null;
  
  private int PAYNT_Instalments = 0;
  
  private static String databaseId = SwissKnife.jndiLookup("swconf/databaseId");
  
  private static Director director = Director.getInstance();
}