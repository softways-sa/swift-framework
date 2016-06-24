package gr.softways.dev.eshop.eways.v5;

import gr.softways.dev.util.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class VIVAConfirmServlet extends HttpServlet {

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

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request,response);
  }
  
  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    
    DbRet dbRet = new DbRet();
    
    String orderCode = request.getParameter("s"),
        tid = request.getParameter("t"),
        orderId = null;
    
    RequestDispatcher requestDispatcher = null;
    
    HttpSession session = request.getSession(true);
    
    int rows = 0;
    
    SQLHelper2 helperBean = new SQLHelper2();
    helperBean.initBean(_databaseId, request, response, this, null);
    
    rows = helperBean.getSQL("SELECT orderId FROM orders WHERE ordBankTran = '" + SwissKnife.sqlEncode(orderCode) + "' AND status = '" + gr.softways.dev.eshop.eways.v2.Order.STATUS_PENDING_PAYMENT + "'").getRetInt();
    
    orderId = helperBean.getColumn("orderId");
        
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
    
    helperBean.closeResources();
    
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