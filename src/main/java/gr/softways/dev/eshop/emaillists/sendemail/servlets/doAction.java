package gr.softways.dev.eshop.emaillists.sendemail.servlets;

import java.io.*;
import java.util.*;

import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;
import gr.softways.dev.eshop.emaillists.lists.Present;

public class doAction extends HttpServlet {

  private Director bean;
  
  private String _charset = null;

  // max of BCC in each email
  private final int BCC_MAX_RECIPIENTS = 50;
  
  private String _defaultContent = "text/plain";
  private String _defaultCharset = "ISO-8859-7";
  
  private String _ls = "\r\n";
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    bean = Director.getInstance();
  }

  //Process the HTTP Post request
  public void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
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
  
  public DbRet doSend(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

    int authStatus = 0;

    DbRet dbRet = new DbRet();
    
    DbRet dbRet2 = null;
    
    authStatus = bean.auth(databaseId,authUsername,authPassword,"emailListSendEmail",Director.AUTH_INSERT);

    if (authStatus < 0) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      
      return dbRet;
    }

    int rows = Integer.parseInt(request.getParameter("rowCount"));

    int status = Director.STATUS_OK;
    boolean sent = false;

    String from = SwissKnife.grEncode(request.getParameter("from")),
           subject = SwissKnife.grEncode(request.getParameter("subject")),
           body = SwissKnife.grEncode(request.getParameter("body")),
           mailhost = SwissKnife.grEncode(request.getParameter("mailhost"));
    
    String mailContent = SwissKnife.grEncode(request.getParameter("mailContent")),
           mailCharset = SwissKnife.grEncode(request.getParameter("mailCharset")),
           appendHTMLTop = SwissKnife.grEncode(request.getParameter("appendHTMLTop"));

    if (mailContent.equals("text/html") && appendHTMLTop.length() > 0) body = appendHTMLTop + _ls + body;
    
    if (from.equals("") || body.equals("") || mailhost.equals("")) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    if (mailContent.equals("")) mailContent = _defaultContent;
    if (mailCharset.equals("")) mailCharset = _defaultCharset;
    
    String EMLTCode = null, send = null;

    Database database = bean.getDBConnection(databaseId);

    for (int i=0; i<rows; i++) {
      send = SwissKnife.grEncode(request.getParameter("send_" + i)).equals("") ? "0" : request.getParameter("send_" + i);
      EMLTCode = SwissKnife.grEncode(request.getParameter("EMLTCode_" + i));

      if (send.equals("1")) {
        dbRet2 = doSendToList(from,subject,body,mailhost,EMLTCode,database,mailContent,mailCharset);
        if (dbRet2.getNoError() == 0) {
          dbRet.setRetStr( dbRet.getRetStr() + "|" + dbRet2.getRetStr() );
          dbRet.setNoError(0);
        }
      }
    }

    bean.freeDBConnection(databaseId,database);

    return dbRet;
  }

  private DbRet doSendToList(String from,String subject,String body,String mailhost,
                             String EMLTCode,Database database,String content, String charset) {

    QueryDataSet queryDataSet = new QueryDataSet();

    int rows = 0;

    String query = "SELECT * FROM emailListReg, emailListMember,emailListTab"
                 + " WHERE emailListMember.EMLMCode = emailListReg.EMLRMemberCode"
                 + " AND emailListTab.EMLTCode = '" + EMLTCode + "'"
                 + " AND emailListReg.EMLRListCode = '" + EMLTCode + "'"
                 + " AND emailListMember.EMLMActive = '" 
                 + Present.STATUS_ACTIVE
                 + "'";

    InternetAddress[] addressBcc = null;

    EMail email = null;
    
    String to = ""; 
    String listName = "";

    DbRet dbRet = new DbRet(), errorRet = new DbRet();
    
    try {
      queryDataSet.setQuery(new QueryDescriptor(database, query, null, 
                                                true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      // εκτέλεσε το query
      queryDataSet.refresh();

      rows = queryDataSet.getRowCount();
      to = queryDataSet.getString("EMLTTo");    
      listName = SwissKnife.sqlDecode(queryDataSet.getString("EMLTName"));
      
      if (rows>0) {
        email = new EMail(to, from, subject, body, mailhost, content, charset, null);
        
        int actualSent = 0, maxBCCRecipients = 0;
        
        while (actualSent < rows) {
          if ( (rows - actualSent) < BCC_MAX_RECIPIENTS) {
            maxBCCRecipients = rows - actualSent;
          }
          else {
            maxBCCRecipients = BCC_MAX_RECIPIENTS;
          }
          
          addressBcc = new InternetAddress[maxBCCRecipients];
          for (int i=0; i<maxBCCRecipients; i++) {
            try {
              addressBcc[i] = new InternetAddress( SwissKnife.sqlDecode(queryDataSet.getString("EMLMEmail")).trim() );
            }
            catch (Exception e) {
              addressBcc[i] = new InternetAddress();
            }
            actualSent++;
            
            queryDataSet.next();
          }
          errorRet = SendMail.sendMessage(email, addressBcc);
          if (errorRet.getNoError() == 0) dbRet.setNoError(0);
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      dbRet.setNoError(0);
    }
    finally {
      try { if (queryDataSet.isOpen() == true) queryDataSet.close(); } catch (Exception e) { e.printStackTrace(); }
    }
    
    dbRet.setRetStr(listName);
    
    return dbRet;
  }
}