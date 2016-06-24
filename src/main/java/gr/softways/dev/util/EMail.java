package gr.softways.dev.util;

import java.sql.Timestamp;

public class EMail {

  private String _to = null;
  private String _from = null;
  private String _subject = null;
  private String _body = null;

  private String _charset = null;
  private String _content = null;

  private Timestamp _dateCreated = null;

  private String _smtpServer = null;

  public EMail() {
  }

  public EMail(String to, String from, String subject, String body,
               String smtpServer, String content, String charset,
               Timestamp dateCreated) {
    _to = to;
    _from = from;
    _subject = subject;
    _body = body;

    _content = content;
    _charset = charset;

    _smtpServer = smtpServer;

    _dateCreated = dateCreated;
  }

  public void setTo(String to) {
    _to = to;
  }

  public String getTo() {
    return _to;
  }

  public void setFrom(String from) {
    _from = from;
  }

  public String getFrom() {
    return _from;
  }

  public void setSubject(String subject) {
    _subject = subject;
  }

  public String getSubject() {
    return _subject;
  }

  public void setBody(String body) {
    _body = body;
  }

  public String getBody() {
    return _body;
  }

  public void setSMTPServer(String smtpServer) {
    _smtpServer = smtpServer;
  }

  public String getSMTPServer() {
    return _smtpServer;
  }

  public void setContent(String content) {
    _content = content;
  }

  public String getContent() {
    return _content;
  }

  public void setCharset(String charset) {
    _charset = charset;
  }

  public String getCharset() {
    return _charset;
  }

  public void setDateCreated(Timestamp dateCreated) {
    _dateCreated = dateCreated;
  }

  public Timestamp getDateCreated() {
    return _dateCreated;
  }
}