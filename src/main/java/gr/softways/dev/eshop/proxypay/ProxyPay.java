/*
 * ProxyPay.java
 *
 * Created on 10 Ã‹˙ÔÚ 2004, 11:33 ÏÏ
 */

package gr.softways.dev.eshop.proxypay;

import java.io.*;
import java.net.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;

/**
 *
 * @author  panos
 */
public class ProxyPay {
  
  /** Creates a new instance of ProxyPay */
  public ProxyPay() {
  }
  
  public void setMerchantRef(String merchantRef) {
    _merchantRef = merchantRef;
  }
  
  public String  getMerchantRef() {
    return _merchantRef;
  }
  
  public void setCustomerEmail(String customerEmail) {
    _customerEmail = customerEmail;
  }
  
  public String getCustomerEmail() {
    return _customerEmail;
  }
  
  public void setURL(String url) {
    _url = url;
  }
  
  public String getURL() {
    return _url;
  }
  
  public void setMerchantID(String merchantID) {
    _merchantID = merchantID;
  }
  
  public String getMerchantID() {
    return _merchantID;
  }
  
  public void setPassword(String password) {
    _password = password;
  }
  
  public String getPassword() {
    return _password;
  }
  
  public void setReferer(String referer) {
    _referer = referer;
  }
  
  public String getReferer() {
    return _referer;
  }
  
  public void setAmount(String amount) {
    _amount = amount;
  }
  
  public String getAmount() {
    return _amount;
  }
  
  public void setCCN(String ccn) {
    _ccn = ccn;
  }
  
  public String getCCN() {
    return _ccn;
  }
  
  public void setExpDate(String expDate) {
    _expDate = expDate;
  }
  
  public String getExpDate() {
    return _expDate;
  }
  
  public void setCVCCVV(String CVCCVV) {
    _CVCCVV = CVCCVV;
  }
  
  public String getCVCCVV() {
    return _CVCCVV;
  }
  
  public void setCurrency(String currency) {
    _currency = currency;
  }
  
  public String getCurrency() {
    return _currency;
  }
  
  public void setInstallmentOffset(String installmentOffset) {
    _installmentOffset = installmentOffset;
  }
  
  public String getInstallmentOffset() {
    return _installmentOffset;
  }
  
  public void setInstallmentPeriod(String installmentPeriod) {
    _installmentPeriod = installmentPeriod;
  }
  
  public String getInstallmentPeriod() {
    return _installmentPeriod;
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
  
  public void setVar3(String var3) {
    _var3 = var3;
  }
  
  public String getVar3() {
    return _var3;
  }
  
  public void setVar4(String var4) {
    _var4 = var4;
  }
  
  public String getVar4() {
    return _var4;
  }
  
  public void setVar5(String var5) {
    _var5 = var5;
  }
  
  public String getVar5() {
    return _var5;
  }
  
  public void setVar6(String var6) {
    _var6 = var6;
  }
  
  public String getVar6() {
    return _var6;
  }
  
  public void setVar7(String var7) {
    _var7 = var7;
  }
  
  public String getVar7() {
    return _var7;
  }
  
  public void setVar8(String var8) {
    _var8 = var8;
  }
  
  public String getVar8() {
    return _var8;
  }
  
  public void setVar9(String var9) {
    _var9 = var9;
  }
  
  public String getVar9() {
    return _var9;
  }
  
  public void setDatabaseID(String databaseID) {
    _databaseID = databaseID;
  }
  
  public String getDatabaseID() {
    return _databaseID;
  }
  
  public void setRequest(HttpServletRequest request) {
    _request = request;
  }
  
  public HttpServletRequest getRequest() {
    return _request;
  }
     
  public DbRet sendPreAuthRequest() {
    DbRet dbRet = new DbRet();
    dbRet.setNoError(0);
    
    HttpURLConnection con = null;
    
    String enc = getEncoding();
    
    StringBuffer post = new StringBuffer();

    try {
      if (getMerchantRef() == null || getMerchantRef().length() == 0 || getAmount() == null || getAmount().length() == 0
          || getURL() == null || getURL().length() == 0) {
        throw new Exception();
      }
      
      post.append(URLEncoder.encode("APACScommand",enc));
      post.append("=");
      post.append(URLEncoder.encode("NewRequest",enc));
      post.append("&");
      post.append(URLEncoder.encode("Data",enc));
      post.append("=");
      post.append(URLEncoder.encode("<?xml version=\"1.0\" encoding=\"UTF-8\"?>",enc));
      post.append(URLEncoder.encode("<JProxyPayLink>",enc));
      post.append(URLEncoder.encode("<Message>",enc));
      post.append(URLEncoder.encode("<Type>PreAuth</Type>",enc));
      post.append(URLEncoder.encode("<Authentication>",enc));
      post.append(URLEncoder.encode("<MerchantID>" + getMerchantID() + "</MerchantID>",enc));
      post.append(URLEncoder.encode("<Password>" + getPassword() + "</Password>",enc));
      post.append(URLEncoder.encode("</Authentication>",enc));
      post.append(URLEncoder.encode("<OrderInfo>",enc));
      post.append(URLEncoder.encode("<Amount>",enc));
      post.append(URLEncoder.encode(getAmount(),enc));
      post.append(URLEncoder.encode("</Amount>",enc));
      post.append(URLEncoder.encode("<MerchantRef>",enc));
      post.append(URLEncoder.encode(getMerchantRef(),enc));
      post.append(URLEncoder.encode("</MerchantRef>",enc));
      post.append(URLEncoder.encode("<MerchantDesc></MerchantDesc>",enc));
      post.append(URLEncoder.encode("<Currency>" + getCurrency() + "</Currency>",enc));
      post.append(URLEncoder.encode("<CustomerEmail>" + getCustomerEmail() + "</CustomerEmail>",enc));
      post.append(URLEncoder.encode("<Var1>" + getVar1() + "</Var1>",enc));
      post.append(URLEncoder.encode("<Var2>" + getVar2() + "</Var2>",enc));
      post.append(URLEncoder.encode("<Var3>" + getVar3() + "</Var3>",enc));
      post.append(URLEncoder.encode("<Var4>" + getVar4() + "</Var4>",enc));
      post.append(URLEncoder.encode("<Var5>" + getVar5() + "</Var5>",enc));
      post.append(URLEncoder.encode("<Var6>" + getVar6() + "</Var6>",enc));
      post.append(URLEncoder.encode("<Var7>" + getVar7() + "</Var7>",enc));
      post.append(URLEncoder.encode("<Var8>" + getVar8() + "</Var8>",enc));
      post.append(URLEncoder.encode("<Var9>" + getVar9() + "</Var9>",enc));
      post.append(URLEncoder.encode("</OrderInfo>",enc));
      post.append(URLEncoder.encode("<PaymentInfo>",enc));
      post.append(URLEncoder.encode("<CCN>",enc));
      post.append(URLEncoder.encode(getCCN(),enc));
      post.append(URLEncoder.encode("</CCN>",enc));
      post.append(URLEncoder.encode("<Expdate>",enc));
      post.append(URLEncoder.encode(getExpDate(),enc));
      post.append(URLEncoder.encode("</Expdate>",enc));
      post.append(URLEncoder.encode("<CVCCVV>",enc));
      post.append(URLEncoder.encode(getCVCCVV(),enc));
      post.append(URLEncoder.encode("</CVCCVV>",enc));
      post.append(URLEncoder.encode("<InstallmentOffset>" + getInstallmentOffset() + "</InstallmentOffset>",enc));
      post.append(URLEncoder.encode("<InstallmentPeriod>" + getInstallmentPeriod() + "</InstallmentPeriod>",enc));
      post.append(URLEncoder.encode("</PaymentInfo>",enc));
      post.append(URLEncoder.encode("</Message>",enc));
      post.append(URLEncoder.encode("</JProxyPayLink>",enc));

      con = (HttpURLConnection)new URL(getURL()).openConnection();
      con.setRequestMethod("POST");
      con.setUseCaches(false);
      con.setDoOutput(true);
      con.setDoInput(true);
      con.setRequestProperty("Content-type","application/x-www-form-urlencoded; charset=UTF-8");
      con.setRequestProperty("Referer",getReferer());
      
      DataOutputStream output = new DataOutputStream(con.getOutputStream());
      output.writeBytes(post.toString());
      output.close();
      
      String errorcode = null, errormessage = null, proxypayref = null;
      
      DocumentBuilder docBuilder = null;
      docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      
      Document doc = docBuilder.parse(con.getInputStream());

      Element response = (Element)doc.getElementsByTagName("RESPONSE").item(0);

      errorcode = response.getElementsByTagName("ERRORCODE").item(0).getChildNodes().item(0).getNodeValue();
      
      if (errorcode != null && errorcode.equals("0")) {
        dbRet.setNoError(1);
        
        proxypayref = response.getElementsByTagName("PROXYPAYREF").item(0).getChildNodes().item(0).getNodeValue();
        
        dbRet.setRetStr(proxypayref);
      }
      else {
        dbRet.setNoError(0);
        dbRet.setRetStr(errorcode);
        
        errormessage = response.getElementsByTagName("ERRORMESSAGE").item(0).getChildNodes().item(0).getNodeValue();
        
        System.out.println("[" + SwissKnife.currentDate() + "] " + getDatabaseID() + " : PROXYPAY ERRORCODE=" + errorcode + ", ERRORMESSAGE=" + errormessage);
      }
      
      con.disconnect();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    
    return dbRet;
  }
  
  private String _databaseID = "";
  
  private HttpServletRequest _request = null;
  
  private String _merchantRef = "";
  private String _customerEmail = "";
  private String _url = "";
  private String _merchantID = "";
  private String _password = "";
  private String _referer = "";
  private String _amount = "";
  private String _ccn = "";
  private String _expDate = "";
  private String _CVCCVV = "";
  
  private String _currency = "978"; // default EURO
  
  private String _installmentOffset = "0";
  private String _installmentPeriod = "0";
  
  private String _encoding = "";
  
  private String _var1 = "",
    _var2 = "",
    _var3 = "",
    _var4 = "",
    _var5 = "",
    _var6 = "",
    _var7 = "",
    _var8 = "",
    _var9 = "";
}