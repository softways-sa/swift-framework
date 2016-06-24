package gr.softways.dev.eshop.emaillists.sendemail.v2;

import java.util.HashMap;
    
import gr.softways.dev.util.*;

public class SendNewsletterThreadMgr extends Thread {

  private String databaseId = null;
  private HashMap<java.lang.String,java.lang.String[]> params = null;
  
  private String _defaultContent = "text/plain";
  private String _defaultCharset = "UTF-8";
  
  private String _ls = "\r\n";
  
  public SendNewsletterThreadMgr(HashMap<java.lang.String,java.lang.String[]> params, String databaseId) {
    super("SendNewsletterThreadMgr");
    
    this.databaseId = databaseId;
    this.params = params;
  }
  
  public void run() {
    String from = SwissKnife.grEncode(getParam("from"));
    String body = SwissKnife.grEncode(getParam("body"));
    String mailhost = SwissKnife.grEncode(getParam("mailhost"));
    
    int rows = Integer.parseInt(getParam("rowCount"));
    
    String mailContent = SwissKnife.grEncode(getParam("mailContent"));
    String mailCharset = SwissKnife.grEncode(getParam("mailCharset"));
    
    String appendTop = SwissKnife.grEncode(getParam("appendTop"));
    String appendBottom = SwissKnife.grEncode(getParam("appendBottom"));
    
    String subject = SwissKnife.grEncode(getParam("subject"));
    
    if (appendTop.length() > 0) body = appendTop + _ls + body;
    
    if (appendBottom.length() > 0) body = body + _ls + appendBottom;
    
    if (mailContent.equals("")) mailContent = _defaultContent;
    if (mailCharset.equals("")) mailCharset = _defaultCharset;
    
    String EMLTCode = null, send = null;

    for (int i=0; i<rows; i++) {
      send = SwissKnife.grEncode( getParam("send_" + i) ).equals("") ? "0" : getParam("send_" + i);
      
      EMLTCode = SwissKnife.grEncode( getParam("EMLTCode_" + i) );

      if (send.equals("1")) {
        SendNewsletterThread sendNewsletterThread = new SendNewsletterThread(from,subject,body,mailhost,EMLTCode,databaseId,mailContent,mailCharset);
    
        sendNewsletterThread.run();
      }
    }
  }
  
  private String getParam(String paramName) {
    String s = null;
    
    if ( params.containsKey(paramName) ) s = params.get(paramName)[0];
    else s = null;
      
    return s;
  }
}