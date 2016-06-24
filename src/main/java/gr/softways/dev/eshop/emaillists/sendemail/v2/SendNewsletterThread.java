package gr.softways.dev.eshop.emaillists.sendemail.v2;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

import gr.softways.dev.eshop.emaillists.lists.Present;

public class SendNewsletterThread {

  private String _databaseId = null;
  
  private String _from = null;
  private String _subject = null;
  private String _body = null;
  private String _mailhost = null;
  private String _EMLTCode = null;
  private String _content = null;
  private String _charset = null;
  
  public SendNewsletterThread(String from,String subject,String body,String mailhost,String EMLTCode,String databaseId,String content,String charset) {
    _databaseId = databaseId;
  
    _from = from;
    _subject = subject;
    _body = body;
    _mailhost = mailhost;
    _EMLTCode = EMLTCode;
    _content = content;
    _charset = charset;
  }
  
  public void run() {
    DbRet dbRet = new DbRet();
    
    StringBuffer buf = new StringBuffer();
    
    Director director = Director.getInstance();
    
    QueryDataSet queryDataSet = null;

    String ls = "\r\n";
    
    String query = "SELECT * FROM emailListReg,emailListMember,emailListTab"
                 + " WHERE emailListMember.EMLMCode = emailListReg.EMLRMemberCode"
                 + " AND emailListTab.EMLTCode = '" + _EMLTCode + "'"
                 + " AND emailListReg.EMLRListCode = '" + _EMLTCode + "'"
                 + " AND emailListMember.EMLMActive = '" + Present.STATUS_ACTIVE + "'";

    EMail email = null;
    
    String to = null, EMLTName = "";
    
    Database database = null;
    
    try {
      database = director.getDBConnection(_databaseId);
      
      queryDataSet = new QueryDataSet();
      
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.UNCACHED));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      // εκτέλεσε το query
      queryDataSet.refresh();
      
      EMLTName = SwissKnife.sqlDecode(queryDataSet.getString("EMLTName"));

      while (queryDataSet.inBounds() == true) {
        to = SwissKnife.sqlDecode(queryDataSet.getString("EMLMEmail"));
        
        String text = _body.replaceAll("\\^EMAIL\\^", to).replaceAll("%5EEMAIL%5E", to).replaceAll("\\^FIRSTNAME\\^", SwissKnife.sqlDecode(queryDataSet.getString("EMLMFirstName"))).replaceAll("\\^LASTNAME\\^", SwissKnife.sqlDecode(queryDataSet.getString("EMLMLastName"))).replaceAll("\\^COMPANYNAME\\^", SwissKnife.sqlDecode(queryDataSet.getString("EMLMCompanyName")));
        
        email = new EMail(to,_from,_subject,text,_mailhost,_content,_charset,null);
        
        if (SendMail.sendMessage(email) == true) {
          buf.append("Sent to " + to);
        }
        else {
          buf.append("ERORR to " + to);
        }
        buf.append(ls + ls);
        
        queryDataSet.next();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      dbRet.setNoError(0);
    }
    finally {
      try { if (queryDataSet != null && queryDataSet.isOpen() == true) queryDataSet.close(); } catch (Exception e) { e.printStackTrace(); }
      
      director.freeDBConnection(_databaseId,database);
    }
    
    email = new EMail(_from,_from,"Report for email list " + EMLTName + " on " + SwissKnife.currentDate(),buf.toString(),_mailhost,"text/plain","UTF-8",null);
    SendMail.sendMessage(email);
  }
}