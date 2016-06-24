/*
 * ProxyPay.java
 *
 * Created on 10 Ã‹˙ÔÚ 2004, 11:33 ÏÏ
 */

package gr.softways.dev.eshop.viva;

import gr.softways.dev.util.*;
import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import org.apache.commons.codec.binary.Base64;
import org.json.*;

/**
 *
 * @author  panos
 */
public class VIVA {
  
  public VIVA() {
  }
  
  public void setAmount(BigDecimal amount) {
    this.amount = String.valueOf(amount.setScale(2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_HALF_UP));
  }
  
  public String getAmount() {
    return amount;
  }
  
  public void setDebug(boolean debug) {
    this.debug = debug;
  }
  
  public boolean isDebug() {
    return debug;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getMerchantID() {
    return merchantID;
  }

  public void setMerchantID(String merchantID) {
    this.merchantID = merchantID;
  }

  public String getAPIKey() {
    return APIKey;
  }

  public void setAPIKey(String APIKey) {
    this.APIKey = APIKey;
  }

  public String getOrderCode() {
    return orderCode;
  }

  public void setOrderCode(String orderCode) {
    this.orderCode = orderCode;
  }

  public String getRequestLang() {
    return requestLang;
  }

  public void setRequestLang(String requestLang) {
    this.requestLang = requestLang;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getMerchantTrns() {
    return merchantTrns;
  }

  public void setMerchantTrns(String merchantTrns) {
    this.merchantTrns = merchantTrns;
  }

  public String getSourceCode() {
    return sourceCode;
  }

  public void setSourceCode(String sourceCode) {
    this.sourceCode = sourceCode;
  }

  public Integer getMaxInstallments() {
    return maxInstallments;
  }

  public void setMaxInstallments(Integer maxInstallments) {
    this.maxInstallments = maxInstallments;
  }

  public Integer getPaymentTimeOut() {
    return paymentTimeOut;
  }

  public void setPaymentTimeOut(Integer paymentTimeOut) {
    this.paymentTimeOut = paymentTimeOut;
  }

  public Boolean getDisableCash() {
    return disableCash;
  }

  public void setDisableCash(Boolean disableCash) {
    this.disableCash = disableCash;
  }

  public Boolean getDisableCard() {
    return disableCard;
  }

  public void setDisableCard(Boolean disableCard) {
    this.disableCard = disableCard;
  }
  
  public DbRet createOrder() {
    DbRet dbRet = new DbRet();
    
    HttpURLConnection con = null;
    
    StringBuilder post = new StringBuilder();

    try {
      post.append("Amount=");
      post.append(URLEncoder.encode(getAmount(),enc));
      
      if (getRequestLang() != null) {
        post.append("&");
        post.append("RequestLang=" + URLEncoder.encode(getRequestLang(),enc));
      }
      
      if (getFullName() != null) {
        post.append("&");
        post.append("FullName=" + URLEncoder.encode(getFullName(),enc));
      }
        
      if (getEmail() != null) {
        post.append("&");
        post.append("Email=" + URLEncoder.encode(getEmail(),enc));
      }
      
      if (getPhone() != null) {
        post.append("&");
        post.append("Phone=" + URLEncoder.encode(getPhone(),enc));
      }
      
      if (getMaxInstallments() != null) {
        post.append("&");
        post.append("MaxInstallments=");
        post.append(getMaxInstallments());
      }
      
      if (getMerchantTrns() != null) {
        post.append("&");
        post.append("MerchantTrns=" + URLEncoder.encode(getMerchantTrns(),enc));
      }
      
      if (getSourceCode() != null) {
        post.append("&");
        post.append("SourceCode=" + URLEncoder.encode(getSourceCode(),enc));
      }
      
      if (getPaymentTimeOut() != null) {
        post.append("&");
        post.append("PaymentTimeOut=");
        post.append(getPaymentTimeOut());
      }
      
      if (getDisableCash() != null) {
        post.append("&");
        post.append("DisableCash=" + getDisableCash());
      }
      
      if (getDisableCard() != null) {
        post.append("&");
        post.append("DisableCard=" + getDisableCard());
      }
      
      if (isDebug() == true) System.out.println("[" + SwissKnife.currentDate() + "] " + " : POST=" + post);
      
      String authString = getMerchantID() + ":" + getAPIKey();
      if (isDebug() == true) System.out.println("[" + SwissKnife.currentDate() + "] " + " : Authorization string=" + authString);
      
      String authStringEnc = new String( Base64.encodeBase64( (getMerchantID() + ":" + getAPIKey()).getBytes() ));
      
      con = (HttpURLConnection)new URL(getUrl()).openConnection();
      con.setRequestProperty("Authorization", "Basic " + authStringEnc);
      con.setRequestMethod("POST");
      con.setUseCaches(false);
      con.setDoOutput(true);
      con.setDoInput(true);
      con.setRequestProperty("Content-type","application/x-www-form-urlencoded; charset=UTF-8");
      
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
      
      if (isDebug() == true) System.out.println("[" + SwissKnife.currentDate() + "] " + " : RESPONSE=" + buffer);
      
      con.disconnect();
      
      JSONObject json = new JSONObject(buffer.toString());
      
      if ( "0".equals( json.get("ErrorCode").toString() )) setOrderCode( json.get("OrderCode").toString() );
      else throw new Exception();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    
    return dbRet;
  }
  
  private String url, amount, email, merchantID, APIKey, orderCode, requestLang,
      fullName, phone, merchantTrns, sourceCode;
  
  private Integer maxInstallments, paymentTimeOut;
  
  Boolean disableCash, disableCard;
  
  private boolean debug = false;
  
  private final String enc = "UTF8";
}