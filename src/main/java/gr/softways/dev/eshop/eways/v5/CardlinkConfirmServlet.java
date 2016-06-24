package gr.softways.dev.eshop.eways.v5;

import gr.softways.dev.util.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.codec.binary.Base64;

public class CardlinkConfirmServlet extends HttpServlet {

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
    
    String mid = request.getParameter("mid") != null ? request.getParameter("mid") : "",
        orderid = request.getParameter("orderid") != null ? request.getParameter("orderid") : "",
        status = request.getParameter("status") != null ? request.getParameter("status") : "",
        orderAmount = request.getParameter("orderAmount") != null ? request.getParameter("orderAmount") : "",
        currency = request.getParameter("currency") != null ? request.getParameter("currency") : "",
        paymentTotal = request.getParameter("paymentTotal") != null ? request.getParameter("paymentTotal") : "",
        message = request.getParameter("message") != null ? request.getParameter("message") : "",
        riskScore = request.getParameter("riskScore") != null ? request.getParameter("riskScore") : "",
        payMethod = request.getParameter("payMethod") != null ? request.getParameter("payMethod") : "",
        txId = request.getParameter("txId") != null ? request.getParameter("txId") : "",
        paymentRef = request.getParameter("paymentRef") != null ? request.getParameter("paymentRef") : "";
    
    String[] configurationValues = Configuration.getValues(new String[] {"ProxyPayConfirmPass"});
    
    String sharedSecret = configurationValues[0];
    
    String data = mid + orderid + status + orderAmount + currency + paymentTotal
        + message + riskScore + payMethod + txId + paymentRef + sharedSecret;
    
    //System.out.println(data);
        
    java.security.MessageDigest mdigest = null;
    try {
      mdigest = java.security.MessageDigest.getInstance("SHA-1");
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    byte [] digestResult = mdigest.digest(data.getBytes("UTF-8"));
    
    String digest = new String(Base64.encodeBase64(digestResult));
		
		String requestDigest = request.getParameter("digest");
    
    RequestDispatcher requestDispatcher = null;
    
    HttpSession session = request.getSession(true);
    
    if (!digest.equals(requestDigest)) {
      requestDispatcher = request.getRequestDispatcher("/problem.jsp");
      requestDispatcher.forward(request, response);
      
      return;
    }
    
    SQLHelper2 helperBean = new SQLHelper2();
    helperBean.initBean(_databaseId, request, response, this, null);
    
    int rows = helperBean.getSQL("SELECT orderId FROM orders WHERE orderId = '" + SwissKnife.sqlEncode(orderid) + "' AND status = '" + gr.softways.dev.eshop.eways.v2.Order.STATUS_PENDING_PAYMENT + "'").getRetInt();
    helperBean.closeResources();
    
    if (rows == 1 && ("AUTHORIZED".equalsIgnoreCase(status) || "CAPTURED".equalsIgnoreCase(status))) {
      
      dbRet = Transaction.updateBank(orderid, txId, gr.softways.dev.eshop.eways.v2.Order.STATUS_PENDING);
      
      if (dbRet.getNoError() == 1) {
        Customer customer = (Customer) session.getAttribute(_databaseId + ".customer");

        if (customer != null) {
          Order order = customer.getOrder();
          order.resetOrder();

          customer.doResetShipping();
        }
      }
      
    }
    else if (rows == 1) {
      dbRet.setNoError(0);
      Transaction.updateBank(orderid, txId, gr.softways.dev.eshop.eways.v2.Order.STATUS_CANCELED);
    }
    else {
      dbRet.setNoError(0);
    }
    
    if (dbRet.getNoError() == 1) {
      EmailReport emailReport = new EmailReport();

      emailReport.sendClientReport(orderid);
      
      emailReport.sendAdminReport(orderid);
    }
    
    if (dbRet.getNoError() == 1) {
      requestDispatcher = request.getRequestDispatcher("/proxypay_ok.jsp");
      requestDispatcher.forward(request, response);
    }
    else {
      requestDispatcher = request.getRequestDispatcher("/proxypay_nok.jsp");
      requestDispatcher.forward(request, response);
    }
  }
}