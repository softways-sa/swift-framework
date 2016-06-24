/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.softways.dev.swift.contactus;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

/**
 *
 * @author panos
 */
public class EmailUtility {
  
  /**
   * Sends an e-mail message from a SMTP host with a list of attached files.
   *
   */
  public static void sendEmailWithAttachment(String host, String fromAddress, String toAddress,
            String subject, String message, List<File> attachedFiles)
                    throws AddressException, MessagingException, UnsupportedEncodingException {
    // sets SMTP server properties
    Properties properties = new Properties();
    properties.put("mail.smtp.host", host);
    
    Session session = Session.getInstance(properties, null);

    // creates a new e-mail message
    Message msg = new MimeMessage(session);
    
    msg.setFrom(new InternetAddress(fromAddress));
    InternetAddress[] toAddresses = { new InternetAddress(toAddress) };
    msg.setRecipients(Message.RecipientType.TO, toAddresses);
    msg.setSubject(MimeUtility.encodeText(subject,"utf-8","B"));
    msg.setSentDate(new Date());

    // creates message part
    MimeBodyPart messageBodyPart = new MimeBodyPart();
    messageBodyPart.setContent(message, "text/html; charset=\"utf-8\"");

    // creates multi-part
    Multipart multipart = new MimeMultipart();
    multipart.addBodyPart(messageBodyPart);

    // adds attachments
    if (attachedFiles != null && attachedFiles.size() > 0) {
      for (File aFile : attachedFiles) {
        MimeBodyPart attachPart = new MimeBodyPart();

        try {
            attachPart.attachFile(aFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        multipart.addBodyPart(attachPart);
      }
    }

    // sets the multi-part as e-mail's content
    msg.setContent(multipart);

    // sends the e-mail
    Transport.send(msg);
  }
}