package gr.softways.dev.swift.emailspooler;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class EmailSpooler {
  
  protected EmailSpooler() {
  }
  
  public static DbRet spoolEmail(Database database,String EMSTo,
                                 String EMSFrom,String EMSSubject,String EMSBody,
                                 String EMSSMTPServer,String EMSContent,String EMSCharset) {
    DbRet dbRet = null;
    
    String query = "INSERT INTO EmailSpooler (EMSCode,EMSDateSpooled,EMSTo,EMSFrom,EMSSubject,EMSBody,EMSSMTPServer,EMSContent,EMSCharset,EMSRetries"
                 + ") VALUES ("
                 + " '" + SwissKnife.buildPK() + "'"
                 + ",'" + SwissKnife.currentDate() + "'"
                 + ",'" + SwissKnife.sqlEncode(EMSTo) + "'"
                 + ",'" + SwissKnife.sqlEncode(EMSFrom) + "'"
                 + ",'" + SwissKnife.sqlEncode(EMSSubject) + "'"
                 + ",'" + SwissKnife.sqlEncode(EMSBody) + "'"
                 + ",'" + SwissKnife.sqlEncode(EMSSMTPServer) + "'"
                 + ",'" + SwissKnife.sqlEncode(EMSContent) + "'"
                 + ",'" + SwissKnife.sqlEncode(EMSCharset) + "'"
                 + ",'" + "0" + "'"
                 + ")";
    
    dbRet = database.execQuery(query);
    
    return dbRet;
  }
}
