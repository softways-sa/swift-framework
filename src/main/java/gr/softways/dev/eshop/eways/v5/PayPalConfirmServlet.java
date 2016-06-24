package gr.softways.dev.eshop.eways.v5;

import java.io.*;
import java.util.*;
import java.math.BigDecimal;
import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class PayPalConfirmServlet extends HttpServlet {

  private ServletContext _servletContext;
  
  private Director _director;
  
  private String _charset = null;
  
  private String _databaseId = null;
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    
    _servletContext = config.getServletContext();

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    _databaseId = SwissKnife.jndiLookup("swconf/databaseId");
    
    _director = Director.getInstance();
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
        helperBean = new SQLHelper2();
        helperBean.initBean(_databaseId, request, response, this, null);
        
        rows = helperBean.getSQL("SELECT * FROM orders WHERE orderId = '" + SwissKnife.sqlEncode(merchantRef) + "'").getRetInt();

        if (rows == 1) {
          BigDecimal vatValEU = helperBean.getBig("vatValEU").setScale(2, BigDecimal.ROUND_HALF_UP),
              valueEU = helperBean.getBig("valueEU").setScale(2, BigDecimal.ROUND_HALF_UP),
              shippingValueEU = helperBean.getBig("shippingValueEU").setScale(2, BigDecimal.ROUND_HALF_UP),
              shippingVatValueEU = helperBean.getBig("shippingVatValEU").setScale(2, BigDecimal.ROUND_HALF_UP),
              totalOrderValue = null;
          
          valueEU = valueEU.add(vatValEU);
          shippingValueEU = shippingValueEU.add(shippingVatValueEU);
          
          totalOrderValue = valueEU.add(shippingValueEU);
          //System.out.println("totalOrderValue = " + totalOrderValue);
          
          if (valueEU.subtract(new BigDecimal("1.0")).compareTo(new BigDecimal(mc_gross)) <= 0) {
            dbRet = Transaction.updateBank(merchantRef, txn_id, gr.softways.dev.eshop.eways.v2.Order.STATUS_PENDING);
          }
          else {
            dbRet.setNoError(0);
          }

          if (dbRet.getNoError() == 1) {
            EmailReport emailReport = new EmailReport();

            emailReport.sendClientReport(merchantRef);

            emailReport.sendAdminReport(merchantRef);
          }
        }
        else dbRet.setNoError(0);
        
        helperBean.closeResources();
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