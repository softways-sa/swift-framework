package gr.softways.dev.epayment.proxypay;

import java.io.*;
import java.util.*;
import java.sql.Timestamp;
import java.math.BigDecimal;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

import gr.softways.dev.epayment.EPayment;
import gr.softways.dev.epayment.EmailReport;

public class ConfirmServlet extends HttpServlet {

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
    
    String[] configurationValues = Configuration.getValues(new String[] {"proxypayConfirmPass"});
    
    String proxypayConfirmPass = configurationValues[0];
        
    SQLHelper2 helperBean = new SQLHelper2();
    helperBean.initBean(_databaseId, request, response, this, null);
    
    int rows = 0;
        
    String merchantRef = request.getParameter("Ref"), password = request.getParameter("Password"), 
        transid = request.getParameter("Transid"), status = "";
    
    if (!proxypayConfirmPass.equals(password)) {
      System.out.println(_databaseId + ": " + SwissKnife.currentDate() + ": Attempt to POST to ConfirmServlet with wrong password (Ref:" + merchantRef + ").");
      
      dbRet.setNoError(0);
    }
    
    if (dbRet.getNoError() == 1) {
      rows = helperBean.getSQL("SELECT * FROM EPayment WHERE PAYNT_Code = '" + SwissKnife.sqlEncode(merchantRef) + "'").getRetInt();
      
      if (rows == 1) {
        dbRet = EPayment.updateBank(merchantRef, "1", transid);
        
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