package gr.softways.dev.swift.contactus.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import gr.softways.dev.util.*;

public class ContactUs extends HttpServlet {
  
  private String _charset = null;
  
  //Initialize global variables
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    
    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
  }
  
  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String urlSuccess = request.getParameter("urlSuccess");
    String urlFailure = request.getParameter("urlFailure");
    
    String action = request.getParameter("action1") != null ? request.getParameter("action1") : "";
    
    String servername = request.getServerName();

    boolean send = false, errorFlag = false;

    /**
     * security protection so as servlet is not called from
     * another server than yours
    */
    if ( request.getHeader("Referer") != null) {
  	  if ( !request.getHeader("Referer").startsWith (request.getScheme() + "://" + servername + "/")) {
        System.out.println( "Violation attempt of sendmail recorded from host "
          + request.getRemoteHost() + " (IP address " + request.getRemoteAddr()
          + ")." );
        errorFlag = true;
      }
    }
    else {
      System.out.println( "Violation attempt of sendmail recorded from host "
          + request.getRemoteHost() + " (IP address " + request.getRemoteAddr()
          + ")." );
      errorFlag = true;
    }
    
    if (action.equals("") || errorFlag == true) {
      send = false;
    }
    else if (action.equals("CONTACT")) {
      send = doContactUs(request, response);
    }
    
    if (send == true) response.sendRedirect(urlSuccess);
    else response.sendRedirect(urlFailure);
  }
  
  public boolean doContactUs(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
    StringBuffer body = new StringBuffer();

    boolean send = false;

    String smtpServer = request.getParameter("smtpServer"),
           from = request.getParameter("from"),
           to = request.getParameter("to"),
           subject = request.getParameter("subject");
    
    String email = request.getParameter("email"),
           firstname = request.getParameter("firstname"),
           lastname = request.getParameter("lastname"),
           companyname = request.getParameter("companyname"),
           address = request.getParameter("address"),
           postalcode = request.getParameter("postalcode"),
           city = request.getParameter("city"),
           country = request.getParameter("country"),
           phone = request.getParameter("phone"),
           fax = request.getParameter("fax"),
           cellphone = request.getParameter("cellphone"),
           message = request.getParameter("message"),
           job = request.getParameter("job"),
           hearAboutUs = request.getParameter("hearAboutUs");
           
    if (from == null) from = email;
    
    // check required fields
    if (to.equals("") || from.equals("") || smtpServer.equals("")) {
      return send;
    }
    
    // build the body of the mail
    if (email != null) body.append("Email: " + email + "\n\n");
    
    if (firstname != null) body.append("Όνομα: " + firstname + "\n\n");
    
    if (lastname != null) body.append("Επώνυμο: " + lastname + "\n\n");
    
    if (job != null) body.append("Επάγγελμα: " + job + "\n\n");
    
    if (companyname != null) body.append("Επωνυμία εταιρίας: " + companyname + "\n\n");
    
    if (address != null) body.append("Διεύθυνση: " + address + "\n\n");
    
    if (postalcode != null) body.append("Τ.Κ.: " + postalcode + "\n\n");
    
    if (city != null) body.append("Πόλη: " + city + "\n\n");
    
    if (country != null) body.append("Χώρα: " + country + "\n\n");
    
    if (phone != null) body.append("Τηλέφωνο: " + phone + "\n\n");
    
    if (fax != null) body.append("Fax: " + fax + "\n\n");
    
    if (cellphone != null) body.append("Κινητό: " + cellphone + "\n\n");
    
    if (hearAboutUs != null && hearAboutUs.length() > 0) body.append("Πως μάθατε για μας; " + hearAboutUs + "\n\n");
    
    if (message != null) body.append("Μήνυμα: " + message);
    
    send = gr.softways.dev.util.SendMail.sendMessage(from,to,subject,body.toString(), smtpServer);
    
    return send;
  }
}