package gr.softways.dev.eshop.eways.v5;

import java.io.*;
import java.util.*;
import java.sql.Timestamp;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class CustomerActionServlet extends HttpServlet {

  private ServletContext _servletContext;
  
  private Director _director;
  
  private String _charset = null;
  
  private String _databaseId = null;
  
  private static String _urlToCheckout = "/checkout_billing.jsp";
  private static String _urlToShipping = "/checkout_shipping.jsp";
  private static String _urlToBilling = "/checkout_billing.jsp";
  private static String _urlToConfirm = "/checkout_confirm.jsp";
  private static String _urlToMyAccount = "/customer_myaccount.jsp";
  private static String _urlToSignin = "/customer_signin.jsp";
  private static String _urlToForgotPassword = "/customer_forgot_password.jsp";
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
    
    String action = request.getParameter("cmd") == null ? "" : request.getParameter("cmd"),
           target = request.getParameter("target") == null ? "" : request.getParameter("target"),
           lt = request.getParameter("lt") == null ? "" : request.getParameter("lt");
    
    Customer customer = null;
    
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
      requestDispatcher = request.getRequestDispatcher(_urlToSignin + "?target=" + target);
    }
    else if (action.equals("create_account")) {
      dbRet = doCustomerRegister(request, response, customer);
      
      if (dbRet.getNoError() == 1) {
        if (target.equals("checkout")) requestDispatcher = request.getRequestDispatcher(_urlToCheckout);
        else requestDispatcher = request.getRequestDispatcher(_urlToMyAccount);
      }
      else {
        requestDispatcher = request.getRequestDispatcher(_urlToProblem);
      }
    }
    else if (action.equals("signin")) {
      dbRet = doCustomerSignIn(request, response, customer);
      
      if (dbRet.getNoError() == 1) {
        if (target.equals("checkout")) requestDispatcher = request.getRequestDispatcher(_urlToCheckout);
        else requestDispatcher = request.getRequestDispatcher(_urlToMyAccount);
      }
      else {
        requestDispatcher = request.getRequestDispatcher(_urlToSignin + "?target=" + target + "&lt=" + lt);
      }
    }
    else if (action.equals("retrieve_password")) {
      dbRet = customer.doSendLostPassword(request);
      
      if (dbRet.getNoError() == 0) request.setAttribute("retrieve_password", "no_such_email");
      else request.setAttribute("retrieve_password", "retrieve_password_ok");
      
      requestDispatcher = request.getRequestDispatcher(_urlToForgotPassword);
    }
    else if (action.equals("edit_info")) {
      dbRet = doCustomerUpdatePInfo(request, response, customer);
      
      if (dbRet.getNoError() == 1) requestDispatcher = request.getRequestDispatcher(_urlToMyAccount);
      else requestDispatcher = request.getRequestDispatcher(_urlToProblem);
    }
    else if (action.equals("signout")) {
      dbRet = doCustomerSignOut(request, response, customer);
      
      if (dbRet.getNoError() == 1) requestDispatcher = request.getRequestDispatcher(_urlToMyAccount);
      else requestDispatcher = request.getRequestDispatcher(_urlToProblem);
    }
    else if (action.equals("set_billing_address")) {
      dbRet = customer.doBillingAddress(request);
      
      String useBilling = request.getParameter("useBilling") == null ? "" : request.getParameter("useBilling");
      
      if (dbRet.getNoError() == 0) requestDispatcher = request.getRequestDispatcher(_urlToBilling);
      else {
        if (useBilling.equals("1")) requestDispatcher = request.getRequestDispatcher(_urlToConfirm);
        else requestDispatcher = request.getRequestDispatcher(_urlToShipping);
      }
    }
    else if (action.equals("set_shipping_address")) {
      dbRet = customer.doShippingAddress(request);
      
      if (dbRet.getNoError() == 1) requestDispatcher = request.getRequestDispatcher(_urlToConfirm);
      else requestDispatcher = request.getRequestDispatcher(_urlToBilling);
    }
    else {
      requestDispatcher = request.getRequestDispatcher(_urlToSignin);
    }
    
    requestDispatcher.forward(request, response);
  }

  private DbRet doCustomerSignIn(HttpServletRequest request, HttpServletResponse response, Customer customer) {
    DbRet dbRet = new DbRet();
    
    dbRet = customer.doSignIn(request);
    if (dbRet.getNoError() == 0) request.setAttribute("signin", "invalid_login");
     
    return dbRet;
  }
  
  private DbRet doCustomerSignOut(HttpServletRequest request, HttpServletResponse response, Customer customer) {
    DbRet dbRet = new DbRet();
    
    dbRet = customer.doSignOut(request);
    
    return dbRet;
  }
  
  private DbRet doCustomerRegister(HttpServletRequest request, HttpServletResponse response, Customer customer) {
    DbRet dbRet = new DbRet();
    
    dbRet = customer.doRegister(request);
    
    if (dbRet.getNoError() == 0) request.setAttribute("error_code", "create_account_problem");
    else {
      String username = request.getParameter("email") == null ? "" : request.getParameter("email").toLowerCase(),
      password = request.getParameter("password") == null ? "" : request.getParameter("password");
    
      dbRet = customer.doSignIn(request, username, password);
    }
    
    return dbRet;
  }
  
  private DbRet doCustomerUpdatePInfo(HttpServletRequest request, HttpServletResponse response, Customer customer) {
    DbRet dbRet = new DbRet();
    
    dbRet = customer.doUpdatePInfo(request);
    
    if (dbRet.getNoError() == 0) request.setAttribute("edit_info", "failed_update");
    
    return dbRet;
  }
}