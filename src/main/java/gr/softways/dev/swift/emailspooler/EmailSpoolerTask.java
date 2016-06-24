package gr.softways.dev.swift.emailspooler;

import java.sql.Timestamp;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class EmailSpoolerTask extends TimerTask {
  
  private String _databaseId = "";
  
  public EmailSpoolerTask() {
    _databaseId = SwissKnife.jndiLookup("swconf/databaseId");
  }
  
  public void run() {
    //System.out.println(SwissKnife.currentDate() + " - Starting Email Spooler");
    
    doTask();
    
    //System.out.println(SwissKnife.currentDate() + " - Email Spooler Finished");
  }
  
  private DbRet doTask() {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    Database database = null;
    
    int prevTransIsolation = 0;
    
    QueryDataSet queryDataSet = null;
    
    String select = "SELECT * FROM EmailSpooler";
    
    try {
      database = director.getDBConnection(_databaseId);
      
      queryDataSet = new QueryDataSet();
      
      queryDataSet.setQuery(new QueryDescriptor(database,select,null,true,Load.UNCACHED));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();
      
      EMail email = null;
      
      String EMSCode = null, to = null, from = null, subject = null, body = null, smtpserver = null, content = null, charset = null;
      
      Timestamp EMSDateSpooled = null;
      
      int EMSRetries = 0;
      
      while (queryDataSet.inBounds() == true) {
        
        try {
          EMSCode = SwissKnife.sqlDecode(queryDataSet.getString("EMSCode"));
          
          EMSDateSpooled = queryDataSet.getTimestamp("EMSDateSpooled");
          EMSRetries = queryDataSet.getInt("EMSRetries");
          
          to = SwissKnife.sqlDecode(queryDataSet.getString("EMSTo"));
          from = SwissKnife.sqlDecode(queryDataSet.getString("EMSFrom"));
          subject = SwissKnife.sqlDecode(queryDataSet.getString("EMSSubject"));
          body = SwissKnife.sqlDecode(queryDataSet.getString("EMSBody"));
          smtpserver = SwissKnife.sqlDecode(queryDataSet.getString("EMSSMTPServer"));
          content = SwissKnife.sqlDecode(queryDataSet.getString("EMSContent"));
          charset = SwissKnife.sqlDecode(queryDataSet.getString("EMSCharset"));
          
          email = new EMail(to,from,subject,body,smtpserver,content,charset,null);
          
          if (SendMail.sendMessage(email) == true) {
            doSent(database,email,EMSCode,EMSRetries);
          }
          else {
            doFailed(database,email,EMSCode,EMSDateSpooled,EMSRetries);
          }
        }
        catch (Exception e) {
          dbRet.setNoError(0);
          
          e.printStackTrace();
        }
        
        queryDataSet.next();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      try { queryDataSet.close(); } catch (Exception e) { }
      director.freeDBConnection(_databaseId,database);
    }
    
    return dbRet;
  }
  
  private DbRet doSent(Database database,EMail email,String EMSCode,int EMSRetries) {
    DbRet dbRet = null;
    
    EMSRetries++;
    
    String query = "INSERT INTO EmailSpoolerSent (ESSCode,ESSDateSent,ESSTo,ESSFrom,ESSSubject,ESSBody,ESSSMTPserver,ESSContent,ESSCharset,ESSRetries"
                 + ") VALUES ("
                 + " '" + SwissKnife.buildPK() + "'"
                 + ",'" + SwissKnife.currentDate() + "'"
                 + ",'" + SwissKnife.sqlEncode(email.getTo()) + "'"
                 + ",'" + SwissKnife.sqlEncode(email.getFrom()) + "'"
                 + ",'" + SwissKnife.sqlEncode(email.getSubject()) + "'"
                 + ",'" + SwissKnife.sqlEncode(email.getBody()) + "'"
                 + ",'" + SwissKnife.sqlEncode(email.getSMTPServer()) + "'"
                 + ",'" + SwissKnife.sqlEncode(email.getContent()) + "'"
                 + ",'" + SwissKnife.sqlEncode(email.getCharset()) + "'"
                 + ",'" + EMSRetries + "'"
                 + ")";
    
    database.execQuery(query);
    
    query = "DELETE FROM EmailSpooler WHERE EMSCode = '" + EMSCode + "'";
    
    database.execQuery(query);
    
    return dbRet;
  }
  
  private DbRet doFailed(Database database,EMail email,String EMSCode,Timestamp EMSDateSpooled,int EMSRetries) {
    DbRet dbRet = null;
    
    String query = "";
    
    EMSRetries++;
    
    Timestamp now = SwissKnife.currentDate();
    
    DateAssistant da = new DateAssistant();
    int days = da.calcDateDiff(SwissKnife.getTDateInt(EMSDateSpooled,"DAY"),SwissKnife.getTDateInt(EMSDateSpooled,"MONTH"),SwissKnife.getTDateInt(EMSDateSpooled,"YEAR"),
                               SwissKnife.getTDateInt(now,"DAY"),SwissKnife.getTDateInt(now,"MONTH"),SwissKnife.getTDateInt(now,"YEAR"));
                               
    if (days >= 5 && EMSRetries>12) {
      dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);
      int prevTransIsolation = dbRet.getRetInt();
      
      query = "INSERT INTO EmailSpoolerFailed (ESFCode,ESFDateSpooled,ESFTo,ESFFrom,ESFSubject,ESFBody,ESFSMTPserver,ESFContent,ESFCharset,ESFRetries"
           + ") VALUES ("
           + " '" + SwissKnife.buildPK() + "'"
           + ",'" + EMSDateSpooled + "'"
           + ",'" + SwissKnife.sqlEncode(email.getTo()) + "'"
           + ",'" + SwissKnife.sqlEncode(email.getFrom()) + "'"
           + ",'" + SwissKnife.sqlEncode(email.getSubject()) + "'"
           + ",'" + SwissKnife.sqlEncode(email.getBody()) + "'"
           + ",'" + SwissKnife.sqlEncode(email.getSMTPServer()) + "'"
           + ",'" + SwissKnife.sqlEncode(email.getContent()) + "'"
           + ",'" + SwissKnife.sqlEncode(email.getCharset()) + "'"
           + ",'" + EMSRetries + "'"
           + ")";
    
      database.execQuery(query);
    
      query = "DELETE FROM EmailSpooler WHERE EMSCode = '" + EMSCode + "'";
    
      database.execQuery(query);
      
      database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    }
    else {
      query = "UPDATE EmailSpooler SET EMSRetries = '" + EMSRetries + "' WHERE EMSCode = '" + EMSCode + "'";
      
      database.execQuery(query);
    }
    
    return dbRet;
  }
}
