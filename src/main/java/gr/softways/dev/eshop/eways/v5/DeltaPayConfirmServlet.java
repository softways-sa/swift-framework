package gr.softways.dev.eshop.eways.v5;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class DeltaPayConfirmServlet extends HttpServlet {

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
    
    String Param1 = request.getParameter("Param1"), DeltaPayId = request.getParameter("DeltaPayId"),
        Guid2 = request.getParameter("Guid2"), Result = request.getParameter("Result"),
        status = null;
    
    RequestDispatcher requestDispatcher = null;
    
    HttpSession session = request.getSession(true);
    
    if (Result != null && "1".equals(Result)) dbRet.setNoError(1);
    else dbRet.setNoError(0);
    
    int rows = 0;
    
    SQLHelper2 helperBean = new SQLHelper2();
    helperBean.initBean(_databaseId, request, response, this, null);
    
    if (dbRet.getNoError() == 1) {
      rows = helperBean.getSQL("SELECT * FROM orders WHERE orderId = '" + SwissKnife.sqlEncode(Param1) + "' AND ORDGuid2 = '" + SwissKnife.sqlEncode(Guid2) + "'").getRetInt();
      
      if (rows == 1) {
        dbRet = Transaction.updateBank(Param1, DeltaPayId, gr.softways.dev.eshop.eways.v2.Order.STATUS_PENDING);
      }
      else dbRet.setNoError(0);
    }
    
    if (dbRet.getNoError() == 1) {
      EmailReport emailReport = new EmailReport();

      emailReport.sendClientReport(Param1);
      
      emailReport.sendAdminReport(Param1);
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