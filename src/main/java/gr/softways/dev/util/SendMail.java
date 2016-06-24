package gr.softways.dev.util;

import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import gr.softways.dev.util.*;

public class SendMail {

  // connection timeout
  private static String _cto = "60000";
  
  // I/O timeout
  private static String _ioto = "60000";
  
  protected SendMail() {
  }
  
  public static boolean sendMessage(String from,String to,String subj,String body,String mailhost) {
    return sendMessage(from,to,subj,body,mailhost,"ISO-8859-7");
  }
  /**
    * Αποστολή μυνήματος
    * @return true επιτυχής αποστολή
   */
  public static boolean sendMessage(String from,String to,String subj,String body,String mailhost,String charset) {
    Properties props = System.getProperties();
    
    props.put("mail.smtp.host", mailhost);
    props.put("mail.smtp.connectiontimeout", _cto);
    props.put("mail.smtp.timeout", _ioto);
    
    Session session = Session.getInstance(props, null);
    
    Message msg = new MimeMessage(session);

    try {
      // ΠΡΟΣ
      InternetAddress[] addressTo = { new InternetAddress(to) };
      msg.setRecipients(Message.RecipientType.TO, addressTo);

      // ΑΠΟ
      msg.setFrom(new InternetAddress(from));

      // πεδίο ΘΕΜΑ
      msg.setSubject(MimeUtility.encodeText(subj,charset,"B"));

      // πεδίο DATE
      msg.setSentDate(new Date());

      // θέτουμε το content με text/html
      msg.setContent(body,"text/plain; charset=\"" + charset + "\"");
      // εαν θέλουμε ορίζουμε και το encoding
      msg.setHeader("Content-Transfer-Encoding","base64");
      // στείλε το μύνημα
      Transport.send(msg);
    }
    catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    
    return true;
  }
  
  public static DbRet sendMessage(EMail email, InternetAddress[] bcc) {
    Properties props = System.getProperties();
    props.put("mail.smtp.host", email.getSMTPServer());
    props.put("mail.smtp.connectiontimeout", _cto);
    props.put("mail.smtp.timeout", _ioto);
    
    // send to valid addresses, ignore invalid
    props.put("mail.smtp.sendpartial", "true");
    
    DbRet dbRet = new DbRet();
    
    Session session = Session.getInstance(props, null);

    Message msg = new MimeMessage(session);

    try {
      // ΠΡΟΣ
      InternetAddress[] addressTo = { new InternetAddress(email.getTo()) };
      msg.setRecipients(Message.RecipientType.TO, addressTo);

      // ΑΠΟ
      msg.setFrom(new InternetAddress(email.getFrom()));

      // BCC
      if (bcc != null) msg.setRecipients(Message.RecipientType.BCC, bcc);
      
      // πεδίο ΘΕΜΑ
      msg.setSubject(MimeUtility.encodeText(email.getSubject(),
                                            email.getCharset(),"B"));

      // πεδίο DATE
      msg.setSentDate(new Date());

      // θέτουμε το content με text/html
      msg.setContent(email.getBody(),email.getContent() + "; charset=\""
                                   + email.getCharset() + "\"");

      // εαν θέλουμε ορίζουμε και το encoding
      msg.setHeader("Content-Transfer-Encoding", "base64");

      // στείλε το μύνημα
      Transport.send(msg);
    }
    catch (Exception e) {
      e.printStackTrace();

      dbRet.setNoError(0);      
    }

    return dbRet;
  }
  
  /**
    * Αποστολή μυνήματος
    * @return true επιτυχής αποστολή
   */
  public static boolean sendMessage(String from, String to, 
                                    InternetAddress[] bcc, String subj,
                                    String body, String mailhost) {

    Properties props = System.getProperties();
    props.put("mail.smtp.host", mailhost);
    props.put("mail.smtp.connectiontimeout", _cto);
    props.put("mail.smtp.timeout", _ioto);
    
    // send to valid addresses, ignore invalid
    props.put("mail.smtp.sendpartial", "true");
    
    Session session = Session.getInstance(props, null);
    
    Message msg = new MimeMessage(session);

    try {
      // ΠΡΟΣ
      InternetAddress[] addressTo = { new InternetAddress(to) };
      msg.setRecipients(Message.RecipientType.TO, addressTo);

      msg.setRecipients(Message.RecipientType.BCC, bcc);

      // ΑΠΟ
      msg.setFrom(new InternetAddress(from));

      // πεδίο ΘΕΜΑ
      msg.setSubject(MimeUtility.encodeText(subj,"ISO-8859-7","B"));

      // πεδίο DATE
	    msg.setSentDate(new Date());

      // θέτουμε το content με text/html
      msg.setContent(body,"text/plain; charset=\"ISO-8859-7\"");
      // εαν θέλουμε ορίζουμε και το encoding
      msg.setHeader("Content-Transfer-Encoding","base64");
      // στείλε το μύνημα
      Transport.send(msg);
    }
    catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public static boolean sendMessageAttach(String from, String to, String subj,
                                          String body, String mailhost,
                                          String attachFilename) {
    return sendMessageAttach(from,to,subj,body,mailhost,attachFilename,"ISO-8859-7");
  }
  /**
    * Αποστολή μυνήματος
    * @return true επιτυχής αποστολή
   */
  public static boolean sendMessageAttach(String from, String to, String subj,
                                          String body, String mailhost,
                                          String attachFilename, String charset) {

    Properties props = System.getProperties();
    props.put("mail.smtp.host", mailhost);
    props.put("mail.smtp.connectiontimeout", _cto);
    props.put("mail.smtp.timeout", _ioto);
    
    Session session = Session.getInstance(props, null);
    
    Message msg = new MimeMessage(session);

    try {
      // ΠΡΟΣ
      InternetAddress[] addressTo = { new InternetAddress(to) };
      msg.setRecipients(Message.RecipientType.TO, addressTo);

      // ΑΠΟ
      msg.setFrom(new InternetAddress(from));

      // πεδίο ΘΕΜΑ
      msg.setSubject(MimeUtility.encodeText(subj,charset,"B"));

      // πεδίο DATE
	    msg.setSentDate(new Date());

      MimeBodyPart mbp1 = new MimeBodyPart();
	    mbp1.setText(body);
      mbp1.setContent(body, "text/plain; charset=\"" + charset + "\"");

	    // create the second message part
	    MimeBodyPart mbp2 = new MimeBodyPart();

      // attach the file to the message
   	  FileDataSource fds = new FileDataSource(attachFilename);
	    mbp2.setDataHandler(new DataHandler(fds));
	    mbp2.setFileName(MimeUtility.encodeText(fds.getName(),charset,"B"));

	    // create the Multipart and its parts to it
	    Multipart mp = new MimeMultipart();
	    mp.addBodyPart(mbp1);
	    mp.addBodyPart(mbp2);

	    // add the Multipart to the message
	    msg.setContent(mp);

      // θέτουμε το content με text/html
      //msg.setContent(body,"text/plain; charset=\"ISO-8859-7\"");

      // εαν θέλουμε ορίζουμε και το encoding
      //msg.setHeader("Content-Transfer-Encoding","base64");

      // στείλε το μύνημα
      Transport.send(msg);
    }
    catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Αποστολή μυνήματος
   * @return true επιτυχής αποστολή
   */
  public static boolean sendMessage(EMail email) {
    Properties props = System.getProperties();
    
    props.put("mail.smtp.host", email.getSMTPServer());
    props.put("mail.smtp.connectiontimeout", _cto);
    props.put("mail.smtp.timeout", _ioto);

    Session session = Session.getInstance(props, null);

    Message msg = new MimeMessage(session);

    try {
      StringTokenizer s = new StringTokenizer(email.getTo(),"; ");
      
      InternetAddress[] addressTo = new InternetAddress[s.countTokens()];
      
      int i = 0;
      while (s.hasMoreTokens()) {
        addressTo[i++] = new InternetAddress(s.nextToken());
      }
      msg.setRecipients(Message.RecipientType.TO, addressTo);

      // ΑΠΟ
      msg.setFrom(new InternetAddress(email.getFrom()));

      // πεδίο ΘΕΜΑ
      msg.setSubject(MimeUtility.encodeText(email.getSubject(),
                                            email.getCharset(),"B"));

      // πεδίο DATE
      msg.setSentDate(new Date());

      // θέτουμε το content με text/html
      msg.setContent(email.getBody(),email.getContent() + "; charset=\""
                                   + email.getCharset() + "\"");

      // εαν θέλουμε ορίζουμε και το encoding
      msg.setHeader("Content-Transfer-Encoding", "base64");

      // στείλε το μύνημα
      Transport.send(msg);
    }
    catch (Exception e) {
      e.printStackTrace();

      return false;
    }

    return true;
  }
  
  public static DbRet validateEMail(String email) {
    DbRet dbRet = new DbRet();
            
    try {
      new InternetAddress( email );
      
      if (email.indexOf("@") == -1)
        throw new Exception("Didn't find @.");
    }
    catch (Exception e) {
      dbRet.setNoError(0);
    }
    
    return dbRet;
  }
}