package gr.softways.dev.eshop.eways.v5;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import gr.softways.dev.util.*;
import gr.softways.dev.eshop.eways.v2.TotalPrice;

public class PlaceOrderServlet extends HttpServlet {

  private ServletContext _servletContext;
  
  private Director _director;
  
  private String _charset = null;
  
  private String _databaseId = null;
  
  private static String _urlToCheckout = "/checkout_billing.jsp";
  private static String _urlToProblem = "/problem.jsp";
  
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
    DbRet dbRet = new DbRet();
    
    request.setCharacterEncoding(_charset);
    
    RequestDispatcher requestDispatcher = null;
    
    String ordPayWay = request.getParameter("ordPayWay") == null ? "" : request.getParameter("ordPayWay"),
        bnk = request.getParameter("bnk") == null ? "" : request.getParameter("bnk"); // '1' for ALPHA BANK, '6' for cardlink
    
    String installments = request.getParameter("installments") == null ? "0" : request.getParameter("installments");
    
    String orderId = null, status = "";

    Customer customer = null;
    
    Order order = null;
    
    HttpSession session = request.getSession(false);
    
    if (session == null) {
      request.setAttribute("session", "SessionExpired");
      dbRet.setNoError(0);
    }
    else {
      customer = (Customer) session.getAttribute(_databaseId + ".customer");
      
      if (customer == null) {
        request.setAttribute("session", "SessionExpired");
        dbRet.setNoError(0);
      }
    }
    
    if (dbRet.getNoError() == 0) {
      requestDispatcher = request.getRequestDispatcher("/problem.jsp");
      requestDispatcher.forward(request, response);
      return;
    }
    
    order = customer.getOrder();
    
    int orderLines = order.getOrderLines();
    if (orderLines <= 0) {
      requestDispatcher = request.getRequestDispatcher("/problem.jsp?errorStatus=26");
      requestDispatcher.forward(request, response);
      return;
    }

    dbRet = customer.isValidForCheckout();
    if (dbRet.getRetInt() == 1) {
        requestDispatcher = request.getRequestDispatcher("/problem.jsp?errorStatus=12");
        requestDispatcher.forward(request, response);
        return;
    }

    Transaction transaction = null;
    
    TotalPrice orderPrice = order.getOrderPrice(), shippingPrice = order.getShippingPrice();
    
    if (ordPayWay.length() > 0) {
      order.setOrdPayWay(ordPayWay);

      if (gr.softways.dev.eshop.eways.v2.Order.PAY_TYPE_ON_DELIVERY.equals(ordPayWay)) {
        status = gr.softways.dev.eshop.eways.v2.Order.STATUS_PENDING;
      }
      else if (gr.softways.dev.eshop.eways.v2.Order.PAY_TYPE_DEPOSIT.equals(ordPayWay)) {
        status = gr.softways.dev.eshop.eways.v2.Order.STATUS_PENDING_PAYMENT;
      }
      else if (gr.softways.dev.eshop.eways.v2.Order.PAY_TYPE_CREDIT_CARD.equals(ordPayWay)) {
        status = gr.softways.dev.eshop.eways.v2.Order.STATUS_PENDING_PAYMENT;
      }
      else if (gr.softways.dev.eshop.eways.v2.Order.PAY_TYPE_PAYPAL.equals(ordPayWay)) {
        status = gr.softways.dev.eshop.eways.v2.Order.STATUS_PENDING_PAYMENT;
      }
      else if (gr.softways.dev.eshop.eways.v2.Order.PAY_TYPE_VIVA.equals(ordPayWay)) {
        status = gr.softways.dev.eshop.eways.v2.Order.STATUS_PENDING_PAYMENT;
      }
      else if (gr.softways.dev.eshop.eways.v2.Order.PAY_TYPE_CASH.equals(ordPayWay)) {
        status = gr.softways.dev.eshop.eways.v2.Order.STATUS_PENDING_PAYMENT;
      }
      else {
        status = gr.softways.dev.eshop.eways.v2.Order.STATUS_PENDING;
      }
    }
    else {
      requestDispatcher = request.getRequestDispatcher("/problem.jsp?errorStatus=12");
      requestDispatcher.forward(request, response);
      return;
    }

    order.buildOrderId();
    
    transaction = new Transaction();
    transaction.initBean(_databaseId, request, response, null, session);

    dbRet = transaction.doOrder(customer, status);
    transaction.closeResources();
    
    if (dbRet.getNoError() == 0) {
      requestDispatcher = request.getRequestDispatcher("/problem.jsp?errorStatus=12");
      requestDispatcher.forward(request, response);
      return;
    }
    
    request.setAttribute(_databaseId + ".checkout.orderID", order.getOrderId());
    
    request.setAttribute(_databaseId + ".checkout.totalAmount", orderPrice.getGrossCurr1().add(shippingPrice.getGrossCurr1()));
    request.setAttribute(_databaseId + ".checkout.totalOrderAmount", orderPrice.getGrossCurr1());
    request.setAttribute(_databaseId + ".checkout.totalShippingAmount", shippingPrice.getGrossCurr1());
    request.setAttribute(_databaseId + ".checkout.totalTax", orderPrice.getVATCurr1().add(shippingPrice.getVATCurr1()));
    
    request.setAttribute(_databaseId + ".checkout.customerEmail", customer.getEmail());
    request.setAttribute(_databaseId + ".checkout.ordPayWay", order.getOrdPayWay());
    request.setAttribute(_databaseId + ".checkout.lang", customer.getCustLang());
    
    request.setAttribute(_databaseId + ".checkout.installments", installments);
    
    if (gr.softways.dev.eshop.eways.v2.Order.PAY_TYPE_PAYPAL.equals(ordPayWay)) {
      requestDispatcher = request.getRequestDispatcher("/checkout_paygate_paypal.jsp");
    }
    else if (gr.softways.dev.eshop.eways.v2.Order.PAY_TYPE_VIVA.equals(ordPayWay)) {
      requestDispatcher = request.getRequestDispatcher("/checkout_paygate_viva.jsp");
    }
    else if (gr.softways.dev.eshop.eways.v2.Order.PAY_TYPE_CREDIT_CARD.equals(ordPayWay) && "6".equals(bnk)) {
      requestDispatcher = request.getRequestDispatcher("/checkout_paygate_cardlink.jsp");
    }
    else if (gr.softways.dev.eshop.eways.v2.Order.PAY_TYPE_CREDIT_CARD.equals(ordPayWay)) {
      if ("1".equals(bnk)) requestDispatcher = request.getRequestDispatcher("/checkout-paygate-deltapay.jsp");
      else requestDispatcher = request.getRequestDispatcher("/checkout_paygate.jsp");
      
      order.resetOrder();
      customer.doResetShipping();
    }
    else {
      EmailReport emailReport = new EmailReport();

      emailReport.sendClientReport(order.getOrderId());
      
      emailReport.sendAdminReport(order.getOrderId());
      
      requestDispatcher = request.getRequestDispatcher("/checkout_ok.jsp");
      
      order.resetOrder();
      customer.doResetShipping();
    }
    
    requestDispatcher.forward(request, response);
  }
}