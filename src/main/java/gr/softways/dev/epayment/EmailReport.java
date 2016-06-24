package gr.softways.dev.epayment;

import java.util.Hashtable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import gr.softways.dev.util.*;
    
/**
 *
 * @author  Administrator
 * @version 
 */
public class EmailReport extends JSPBean {
  private BigDecimal _zero = new BigDecimal("0");
  
  static Hashtable lb = new Hashtable();
  
  private String[] values = null;
  
  static {
    lb.put("subject", "Απόδειξη online συναλλαγής - ");
    lb.put("subjectLG", "Online receipt - ");
    
    lb.put("text", "Αποδεικτικό e-mail συναλλαγής μέσω του συστήματος online πληρωμών - ");
    lb.put("textLG", "Receipt e-mail from online payments - ");
    
    lb.put("idref", "Κωδ. Αναφοράς:");
    lb.put("idrefLG", "Code Ref:");
    lb.put("bankref", "Κωδ. Συναλλαγής:");
    lb.put("bankrefLG", "Transaction Ref:");
    lb.put("amount","Ποσό:");
    lb.put("amountLG","Amount:");
    lb.put("compName","Επωνυμία επιχείρησης:");
    lb.put("compNameLG","Company name:");
    lb.put("instalments","Δόσεις:");
    lb.put("instalmentsLG","Instalments:");
    
    lb.put("thankYou", "Σας ευχαριστούμε.");
    lb.put("thankYouLG", "Thank you.");
  }
  
  public EmailReport() {
    values = Configuration.getValues(new String[] {"smtpServer","epayEmailFrom","epayEmailTo","epayWebsite"});
  }

  public DbRet sendClientReport(String PAYNT_Code) {
    DbRet dbRet = new DbRet();
    
    StringBuffer body = new StringBuffer();
    
    String ls = "\r\n";

    String smtpServer = values[0],
        eshopSalesEmail = values[1],
        website = values[3];
    
    SQLHelper2 helperBean = new SQLHelper2();
    helperBean.initBean(_databaseId, null, null, null, null);
    
    helperBean.getSQL("SELECT * FROM EPayment WHERE PAYNT_Code = '" + SwissKnife.sqlEncode(PAYNT_Code) + "'");
    
    String PAYNT_Lang = helperBean.getColumn("PAYNT_Lang"),
        PAYNT_Email = helperBean.getColumn("PAYNT_Email"),
        PAYNT_CompanyName = helperBean.getColumn("PAYNT_CompanyName"),
        PAYNT_BankTransID = helperBean.getColumn("PAYNT_BankTransID");
    
    BigDecimal PAYNT_Amount = helperBean.getBig("PAYNT_Amount");
    
    int PAYNT_Instalments = helperBean.getInt("PAYNT_Instalments");
    
    helperBean.closeResources();
    
    String subject = lb.get("subject" + PAYNT_Lang).toString() + website;
    
    EMail email = new EMail(PAYNT_Email,eshopSalesEmail,subject,"",smtpServer,"text/plain","UTF-8",null);

    body.append(lb.get("text" + PAYNT_Lang).toString() + website + ls + ls);
    
    body.append(lb.get("compName" + PAYNT_Lang).toString() + " " + PAYNT_CompanyName + ls);
    body.append(lb.get("amount" + PAYNT_Lang).toString() + " " + SwissKnife.formatNumber(PAYNT_Amount,"el","GR",2,2) + " EURO" + ls);
    if (PAYNT_Instalments > 0) {
      body.append(lb.get("instalments" + PAYNT_Lang).toString() + " " + PAYNT_Instalments + ls);
    }
    body.append(lb.get("bankref" + PAYNT_Lang).toString() + " " + PAYNT_BankTransID + ls);
    body.append(lb.get("idref" + PAYNT_Lang).toString() + " " + PAYNT_Code + ls);
    
    body.append(ls + ls);
    body.append(lb.get("thankYou" + PAYNT_Lang).toString());
    
    email.setBody( body.toString() );

    boolean sent = SendMail.sendMessage(email);

    if (sent == false) dbRet.setNoError(0);
    else dbRet.setNoError(1);
        
    return dbRet;
  }
  
  public DbRet sendAdminReport(String PAYNT_Code) {
    DbRet dbRet = new DbRet();
    
    EMail email = null;
 
    String smtpServer = values[0],
        shopEmailFrom = values[1],
        shopEmailTo = values[2];
    
    email = new EMail(shopEmailTo,shopEmailFrom,
        "Πληρωμή online",
        "Πραγματοποιήθηκε πληρωμή online με κωδικό " + PAYNT_Code + ".",
        smtpServer,"text/plain","UTF-8",null);
    
    boolean sent = SendMail.sendMessage(email);

    if (sent == false) dbRet.setNoError(0);
    else dbRet.setNoError(1);
    
    return dbRet;
  }
  
  private String _databaseId = SwissKnife.jndiLookup("swconf/databaseId");
}