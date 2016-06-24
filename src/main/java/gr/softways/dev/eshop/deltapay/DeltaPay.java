/*
 * ProxyPay.java
 *
 * Created on 10 Ã‹˙ÔÚ 2004, 11:33 ÏÏ
 */

package gr.softways.dev.eshop.deltapay;

import java.io.*;
import java.net.*;
import java.math.BigDecimal;
import java.util.StringTokenizer;

import javax.servlet.http.*;

import gr.softways.dev.jdbc.Database;
import gr.softways.dev.util.*;

/**
 *
 * @author  panos
 */
public class DeltaPay {
  
  /** Creates a new instance of ProxyPay */
  public DeltaPay() {
  }
  
  public void setMerchantRef(String merchantRef) {
    _merchantRef = merchantRef;
  }
  
  public String getMerchantRef() {
    return _merchantRef;
  }
  
  public void setCardHolderEmail(String CardHolderEmail) {
    _CardHolderEmail = CardHolderEmail;
  }
  
  public String getCardHolderEmail() {
    return _CardHolderEmail;
  }
  
  public void setURL(String url) {
    _url = url; 
  }
  
  public String getURL() {
    return _url;
  }
  
  public void setMerchantCode(String merchantCode) {
    _merchantCode = merchantCode;
  }
  
  public String getMerchantCode() {
    return _merchantCode;
  }
  
  public void setReferer(String referer) {
    _referer = referer;
  }
  
  public String getReferer() {
    return _referer;
  }
  
  public void setAmount(BigDecimal amount) {
    _amount = String.valueOf(amount.setScale(2,BigDecimal.ROUND_HALF_UP)).replace('.',',');
  }
  
  public String getAmount() {
    return _amount;
  }
  
  public void setCurrency(String currency) {
    _currency = currency;
  }
  
  public String getCurrency() {
    return _currency;
  }
  
  public void setInstallments(String installments) {
    _installments = installments;
  }
  
  public String getInstallments() {
    return _installments;
  }
  
  public void setEncoding(String encoding) {
    _encoding = encoding;
  }
  
  public String getEncoding() {
    return _encoding;
  }
  
  public void setVar1(String var1) {
    _var1 = var1;
  }
  
  public String getVar1() {
    return _var1;
  }
  
  public void setVar2(String var2) {
    _var2 = var2;
  }
  
  public String getVar2() {
    return _var2;
  }
  
  public void setCardHolderName(String CardHolderName) {
    _CardHolderName = CardHolderName;
  }
  
  public String getCardHolderName() {
    return _CardHolderName;
  }
  
  public void setDatabaseID(String databaseID) {
    _databaseID = databaseID;
  }
  
  public String getDatabaseID() {
    return _databaseID;
  }
  
  public void setGuid1(String Guid1) {
    _Guid1 = Guid1;
  }
  
  public String getGuid1() {
    return _Guid1;
  }
  
  public void setGuid2(String Guid2) {
    _Guid2 = Guid2;
  }
  
  public String getGuid2() {
    return _Guid2;
  }
  
  public void setDebug(boolean debug) {
    _debug = debug;
  }
  
  public boolean isDebug() {
    return _debug;
  }
  
  public void setTransactionType(String TransactionType) {
    _TransactionType = TransactionType;
  }
  
  public String getTransactionType() {
    return _TransactionType;
  }
  
  public void setRequest(HttpServletRequest request) {
    _request = request;
  }
  
  public HttpServletRequest getRequest() {
    return _request;
  }
     
  public DbRet getGuid() {
    DbRet dbRet = new DbRet();
    
    HttpURLConnection con = null;
    
    String enc = getEncoding();
    
    StringBuilder post = new StringBuilder();

    try {
      post.append("MerchantCode=" + getMerchantCode());
      post.append("&");
      post.append("Charge=" + getAmount());
      post.append("&");
      post.append("CurrencyCode=" + getCurrency());
      
      if (getInstallments() != null) {
        post.append("&");
        post.append("Installments=" + getInstallments());
      }
      
      if (getCardHolderName() != null) {
        post.append("&");
        post.append("CardHolderName=" + URLEncoder.encode(getCardHolderName(),enc));
      }
      
      if (getCardHolderEmail() != null) {
        post.append("&");
        post.append("CardHolderEmail=" + URLEncoder.encode(getCardHolderEmail(),enc));
      }
      
      post.append("&");
      post.append("Param1=" + URLEncoder.encode(getVar1(),enc));
      post.append("&");
      post.append("Param2=" + URLEncoder.encode(getVar2(),enc));
      
      if (getTransactionType() != null) {
        post.append("&");
        post.append("TransactionType=" + getTransactionType());
      }
      
      if (isDebug() == true) System.out.println("[" + SwissKnife.currentDate() + "] " + getDatabaseID() + " : POST=" + post);
      
      con = (HttpURLConnection)new URL(getURL()).openConnection();
      con.setRequestMethod("POST");
      con.setUseCaches(false);
      con.setDoOutput(true);
      con.setDoInput(true);
      con.setRequestProperty("Content-type","application/x-www-form-urlencoded; charset=UTF-8");
      if (getReferer() != null) con.setRequestProperty("Referer",getReferer());
      
      DataOutputStream output = new DataOutputStream(con.getOutputStream());
      output.writeBytes(post.toString());
      output.close();
      
      // read the result of the post
      InputStreamReader in = new InputStreamReader(con.getInputStream());
      StringBuffer buffer = new StringBuffer();
      int chr = in.read();
      while (chr != -1) {
        buffer.append((char)chr);
        chr = in.read();
      }
      in.close();
      
      if (isDebug() == true) System.out.println("[" + SwissKnife.currentDate() + "] " + getDatabaseID() + " : DELTAPAY RESPONSE=" + buffer);
      
      try {
        String[] res = buffer.toString().split("<br>");
        
        if (res.length != 2) throw new Exception();
        
        setGuid1(res[0]);
        setGuid2(res[1]);
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
      
      if (dbRet.getNoError() == 1) {
        dbRet = updateOrderGuid2();
      }
      
      con.disconnect();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    
    return dbRet;
  }
  
  private DbRet updateOrderGuid2() {
    DbRet dbRet = new DbRet();

    Director director = Director.getInstance();
    
    Database database = null;
    
    String query = "UPDATE orders SET ORDGuid2 = '" + SwissKnife.sqlEncode(getGuid2()) + "' WHERE orderId = '" + SwissKnife.sqlEncode(getVar1()) + "'";
    
    database = director.getDBConnection(getDatabaseID());
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
      
    int prevTransIsolation = dbRet.getRetInt();
    
    int rowCount = database.executeStatement(query);
    
    if (rowCount != 1) dbRet.setNoError(0);
    
    dbRet = database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
      
    director.freeDBConnection(getDatabaseID(), database);
    
    return dbRet;
  }
  
  private String _databaseID = "";
  
  private HttpServletRequest _request = null;
  
  private String _merchantRef = "";
  
  private String _url = "";
  private String _merchantCode = "";
  private String _referer = "";
  private String _amount = "";
  
  private String _CardHolderName = null;
  private String _CardHolderEmail = null;
  
  private String _currency = "978"; // default EURO
  
  private String _installments = null;
  
  private String _TransactionType = null;
  
  private String _encoding = "";
  
  private String _var1 = "",
    _var2 = "";
  
  private String _Guid1 = null, _Guid2 = null;
  
  private boolean _debug = false;
}