package gr.softways.dev.eshop.eways.v5;

import gr.softways.dev.util.*;
import java.io.*;
import java.math.BigDecimal;
import javax.servlet.*;
import javax.servlet.http.*;

public class VIVAConfirmServlet extends HttpServlet {

  private ServletContext _servletContext;
  
  private Director _director;
  
  private String _charset = null;
  
  private String _databaseId = null;
  
  private static final int curr1Scale = 2;
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    
    _servletContext = config.getServletContext();

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    _databaseId = SwissKnife.jndiLookup("swconf/databaseId");
    
    _director = Director.getInstance();
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request,response);
  }
  
  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    
    DbRet dbRet = new DbRet();
    
    BigDecimal totalAmount = BigDecimal.ZERO, totalShippingAmount = BigDecimal.ZERO,
        totalTax = BigDecimal.ZERO;
    
    String orderCode = request.getParameter("s"),
        tid = request.getParameter("t"),
        orderId = null;
    
    RequestDispatcher requestDispatcher = null;
    
    HttpSession session = request.getSession(true);
    
    int rows = 0;
    
    SQLHelper2 helperBean = new SQLHelper2();
    helperBean.initBean(_databaseId, request, response, this, null);
    
    rows = helperBean.getSQL("SELECT orderId,valueEU,vatValEU,shippingValueEU,shippingVatValEU FROM orders WHERE ordBankTran = '" + SwissKnife.sqlEncode(orderCode) + "' AND status = '" + gr.softways.dev.eshop.eways.v2.Order.STATUS_PENDING_PAYMENT + "'").getRetInt();
    
    if (rows == 1) {
      orderId = helperBean.getColumn("orderId");
      
      BigDecimal valueEU  = helperBean.getBig("valueEU").setScale(curr1Scale, BigDecimal.ROUND_HALF_UP);
      BigDecimal vatValEU = helperBean.getBig("vatValEU").setScale(curr1Scale, BigDecimal.ROUND_HALF_UP);
      BigDecimal shippingValueEU = helperBean.getBig("shippingValueEU").setScale(curr1Scale, BigDecimal.ROUND_HALF_UP);
      BigDecimal shippingVatValueEU = helperBean.getBig("shippingVatValEU").setScale(curr1Scale, BigDecimal.ROUND_HALF_UP);
      
      totalShippingAmount = shippingValueEU.add(shippingVatValueEU);
      totalAmount = valueEU.add(vatValEU).add(totalShippingAmount);
      totalTax = vatValEU.add(shippingVatValueEU);
    }
    
    helperBean.closeResources();
    
    if (rows == 1 && tid != null && tid.length() > 0) {
      
      dbRet = Transaction.updateBank(orderId, orderCode, gr.softways.dev.eshop.eways.v2.Order.STATUS_PENDING);
      
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
      Transaction.updateBank(orderId, orderCode, gr.softways.dev.eshop.eways.v2.Order.STATUS_CANCELED);
    }
    else dbRet.setNoError(0);
    
    if (dbRet.getNoError() == 1) {
      EmailReport emailReport = new EmailReport();

      emailReport.sendClientReport(orderId);
      
      emailReport.sendAdminReport(orderId);
    }
    
    if (dbRet.getNoError() == 1) {
      request.setAttribute(_databaseId + ".checkout.orderID", orderId);
      request.setAttribute(_databaseId + ".checkout.totalAmount", totalAmount);
      request.setAttribute(_databaseId + ".checkout.totalShippingAmount", totalShippingAmount);
      request.setAttribute(_databaseId + ".checkout.totalTax", totalTax);
      
      requestDispatcher = request.getRequestDispatcher("/proxypay_ok.jsp");
      requestDispatcher.forward(request, response);
    }
    else {
      requestDispatcher = request.getRequestDispatcher("/proxypay_nok.jsp");
      requestDispatcher.forward(request, response);
    }
  }
}