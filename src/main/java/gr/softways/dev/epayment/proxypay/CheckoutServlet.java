package gr.softways.dev.epayment.proxypay;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

import gr.softways.dev.epayment.EPayment;

public class CheckoutServlet extends HttpServlet {

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
    DbRet dbRet = new DbRet();
    
    request.setCharacterEncoding(_charset);
    
    RequestDispatcher requestDispatcher = null;
    
    EPayment epayment = null;
    
    HttpSession session = request.getSession(false);
    
    if (session == null) {
      request.setAttribute("session", "SessionExpired");
      dbRet.setNoError(0);
    }
    else {
      epayment = (EPayment) session.getAttribute(_databaseId + ".epayment");
      
      if (epayment == null) {
        request.setAttribute("session", "SessionExpired");
        dbRet.setNoError(0);
      }
    }
    
    if (dbRet.getNoError() == 0) {
      requestDispatcher = request.getRequestDispatcher("/problem.jsp");
      requestDispatcher.forward(request, response);
      
      return;
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = epayment.insert();
    }
    
    if (dbRet.getNoError() == 0) {
      requestDispatcher = request.getRequestDispatcher("/problem.jsp");
      requestDispatcher.forward(request, response);
      
      return;
    }
    
    requestDispatcher = request.getRequestDispatcher("/epay_proxypay_gate.jsp");
    requestDispatcher.forward(request, response);
  }
}