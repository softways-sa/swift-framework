package gr.softways.dev.eshop.eways.v5;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class ProxyPayConfirmServlet extends HttpServlet {

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
    
    String[] configurationValues = Configuration.getValues(new String[] {"ProxyPayConfirmPass"});
    
    String proxypayConfirmPass = configurationValues[0];
        
    SQLHelper2 helperBean = new SQLHelper2();
    helperBean.initBean(_databaseId, request, response, this, null);
    
    int rows = 0;
        
    String merchantRef = request.getParameter("Ref"), password = request.getParameter("Password"), 
        transid = request.getParameter("Transid"), status = "";
    
    if (!proxypayConfirmPass.equals(password)) {
      System.out.println(_databaseId + ": " + SwissKnife.currentDate() + ": Attempt to POST to ProxyPayConfirmServlet with wrong password.");
      
      dbRet.setNoError(0);
    }
    
    if (dbRet.getNoError() == 1) {
      rows = helperBean.getSQL("SELECT * FROM orders WHERE orderId = '" + SwissKnife.sqlEncode(merchantRef) + "'").getRetInt();
      
      if (rows == 1) {
        dbRet = Transaction.updateBank(merchantRef, transid, gr.softways.dev.eshop.eways.v2.Order.STATUS_PENDING);
      }
      else dbRet.setNoError(0);
    }
    
    if (dbRet.getNoError() == 1) {
      EmailReport emailReport = new EmailReport();

      emailReport.sendClientReport(merchantRef);
      
      emailReport.sendAdminReport(merchantRef);
    }
    
    helperBean.closeResources();
    
    if (dbRet.getNoError() == 1) {
      status = "[OK]";
    }
    else {
      status = "[ERROR]";
    }
    
    PrintWriter out = null;
    
    try {
      out = response.getWriter();
      
      out.println(status);
      out.close();
    }
    catch (Exception e) {
    }
  }
}