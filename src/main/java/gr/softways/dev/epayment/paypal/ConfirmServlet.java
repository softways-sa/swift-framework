package gr.softways.dev.epayment.paypal;

import gr.softways.dev.epayment.*;
import gr.softways.dev.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ConfirmServlet extends HttpServlet {
  
  private String _charset = null;
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    
    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
  }

  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    
    DbRet dbRet = new DbRet();
    
    SQLHelper2 helperBean = null;
    
    int rows = 0;
    
    /**Enumeration <String>parameterNames = request.getParameterNames();
    while (parameterNames.hasMoreElements()) {
      String s = parameterNames.nextElement();
      System.out.println(s + " = " + request.getParameter(s));
    }**/
    
    String[] configurationValues = Configuration.getValues(new String[] {"PayPalBusinessEmail","PayPalPostIPNVerifyURL"});
    
    String PayPalBusinessEmail = configurationValues[0],
        PayPalPostIPNVerifyURL = configurationValues[1];
    
    dbRet = verifyRequest(request, response, PayPalPostIPNVerifyURL);
    
    if (dbRet.getNoError() == 1) {
      String merchantRef = request.getParameter("invoice"),
          business = request.getParameter("business"),
          txn_id = request.getParameter("txn_id"),
          payment_status = request.getParameter("payment_status"),
          mc_gross = request.getParameter("mc_gross");
      
      if (!PayPalBusinessEmail.equals(business)) {
        dbRet.setNoError(0);
      }
      
      if (dbRet.getNoError() == 1 && "Completed".equals(payment_status)) {
        rows = helperBean.getSQL("SELECT * FROM EPayment WHERE PAYNT_Code = '" + SwissKnife.sqlEncode(merchantRef) + "'").getRetInt();
      
        if (rows == 1) {
          dbRet = EPayment.updateBank(merchantRef, "1", txn_id);

          if (dbRet.getNoError() == 1) {
            EmailReport emailReport = new EmailReport();

            if (helperBean.getColumn("PAYNT_Email").length() > 0) {
              emailReport.sendClientReport(merchantRef);
            }

            emailReport.sendAdminReport(merchantRef);
          }
        }
        else dbRet.setNoError(0);
      }
    }
  }
  
  private DbRet verifyRequest(HttpServletRequest request, HttpServletResponse response, String PayPalPostIPNVerifyURL) {
    DbRet dbRet = new DbRet();
    
    HttpURLConnection con = null;
    
    BufferedReader in = null;
    
    StringBuffer post = new StringBuffer();
    
    String enc = "UTF-8";
    
    try {
      post.append(URLEncoder.encode("cmd",enc));
      post.append("=");
      post.append(URLEncoder.encode("_notify-validate",enc));
      
      Enumeration <String>parameterNames = request.getParameterNames();
      
      while (parameterNames.hasMoreElements()) {
        String s = parameterNames.nextElement();
        
        post.append("&");
        post.append(URLEncoder.encode(s,enc));
        post.append("=");
        post.append(URLEncoder.encode(request.getParameter(s),enc));
      }
      
      //System.out.println("post to IPN:" + post);

      con = (HttpURLConnection)new URL(PayPalPostIPNVerifyURL).openConnection();
      con.setRequestMethod("POST");
      con.setUseCaches(false);
      con.setDoOutput(true);
      con.setDoInput(true);
      con.setRequestProperty("Content-type","application/x-www-form-urlencoded; charset=UTF-8");

      DataOutputStream output = new DataOutputStream(con.getOutputStream());
      output.writeBytes(post.toString());
      output.close();
      
      in = new BufferedReader(new InputStreamReader(con.getInputStream()));

      String result = "", line = null;
      while((line = in.readLine()) != null) {
        result += line;
      }
      
      if (result.startsWith("VERIFIED")) dbRet.setNoError(1);
      else dbRet.setNoError(1);
      
      con.disconnect();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    
    return dbRet;
  }
}