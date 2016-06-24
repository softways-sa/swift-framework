package gr.softways.dev.epayment.proxypay;

import java.io.*;
import java.util.*;
import java.sql.Timestamp;
import java.math.BigDecimal;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class ValServlet extends HttpServlet {

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
    
    BigDecimal totalValueEU = null, amount = null;
    
    SQLHelper2 helperBean = new SQLHelper2();
    helperBean.initBean(_databaseId, request, response, this, null);
    
    String merchantRef = request.getParameter("Ref"), status = "";
    
    try {
      amount = new BigDecimal(request.getParameter("Amount"));
    }
    catch (Exception e) {
    }
    
    int rows = helperBean.getSQL("SELECT PAYNT_Amount FROM EPayment WHERE PAYNT_Code = '" + SwissKnife.sqlEncode(merchantRef) + "'").getRetInt();
    
    if (rows == 1) {
        totalValueEU = helperBean.getBig("PAYNT_Amount");
        
        if (totalValueEU.compareTo(amount) == 0) dbRet.setNoError(1);
        else dbRet.setNoError(0);
    }
    else dbRet.setNoError(0);

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