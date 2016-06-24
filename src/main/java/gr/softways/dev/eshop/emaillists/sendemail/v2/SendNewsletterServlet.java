package gr.softways.dev.eshop.emaillists.sendemail.v2;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.util.*;

public class SendNewsletterServlet extends HttpServlet {

  private Director bean;
  
  private String _charset = null;
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    bean = Director.getInstance();
  }

  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1"),
           databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId").trim(),
           urlSuccess = request.getParameter("urlSuccess") == null ? "" : request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");

    DbRet dbRet = new DbRet();

    if (databaseId.equals("")) dbRet.setNoError(0);
    else if (action.equals("SEND")) dbRet = doSend(request, databaseId);

    if (dbRet.getAuthError() == 1) {
      response.sendRedirect(urlNoAccess);
    }
    else if (dbRet.getNoError() == 1) response.sendRedirect(urlSuccess);
    else response.sendRedirect( response.encodeURL(urlFailure + "?errors=" + dbRet.getRetStr()) );
  }

  /**
   *
   */
  public DbRet doSend(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

    int authStatus = 0;

    DbRet dbRet = new DbRet();
    
    authStatus = bean.auth(databaseId,authUsername,authPassword,"emailListSendEmail", Director.AUTH_INSERT);

    if (authStatus < 0) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      
      return dbRet;
    }

    String from = SwissKnife.grEncode(request.getParameter("from"));
    String body = SwissKnife.grEncode(request.getParameter("body"));
    String mailhost = SwissKnife.grEncode(request.getParameter("mailhost"));
    
    if (from.equals("") || body.equals("") || mailhost.equals("")) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    HashMap<java.lang.String,java.lang.String[]> params = new HashMap( request.getParameterMap() );
    
    SendNewsletterThreadMgr sendNewsletterThreadMgr = new SendNewsletterThreadMgr(params,databaseId);
    sendNewsletterThreadMgr.start();

    return dbRet;
  }
}