/**
 * Χρησιμοποιείται για το login στο σύστημα διαχείρισης του
 * ηλεκτρονικού μαγαζιού.
 *
 */
 
package gr.softways.dev.eshop.login.servlets;

import java.io.*;
import java.util.*;

import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.poolmanager.AuthEmp;
import gr.softways.dev.jdbc.*;

public class Login extends HttpServlet {

  private String _charset = null;
  
  private Director bean;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    bean = Director.getInstance();
  }

  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
                     throws ServletException, IOException {
    boolean AUTHENTICATED = false;

    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String empUsername = request.getParameter("empUsername") != null ? request.getParameter("empUsername") : "",
           empPassword = request.getParameter("empPassword") != null ? request.getParameter("empPassword") : "";

    String urlSuccess = request.getParameter("urlSuccess") != null ? request.getParameter("urlSuccess") : "",
           urlFailure = request.getParameter("urlFailure") != null ? request.getParameter("urlFailure") : "",
           databaseId = request.getParameter("databaseId") != null ? request.getParameter("databaseId") : "";

    AUTHENTICATED = validateUser(empUsername,empPassword,databaseId,request);

    if (AUTHENTICATED) {
      response.sendRedirect(urlSuccess);
    }
    else {
      System.out.println("(" + databaseId + ") " + SwissKnife.currentDate() + " " + empUsername +  " failed to login.");
      response.sendRedirect(urlFailure);
    }
  }

  /**
    *  Validate user against database table.
    *
    * @return true if user is validated, false otherwise
   */
  private boolean validateUser(String empUsername, String empPassword,
                               String databaseId, HttpServletRequest request) {
    Database database;
    
    PreparedStatement ps = null;
    ResultSet resultSet = null;

    boolean AUTHENTICATED = false;
    
    Timestamp now = null;
    
    Calendar calendar = Calendar.getInstance();
    
    calendar.set(Calendar.HOUR_OF_DAY,0);
    calendar.set(Calendar.MINUTE,0);
    calendar.set(Calendar.SECOND,0);
    calendar.set(Calendar.MILLISECOND,0);
    now = new Timestamp(calendar.getTime().getTime());

    // we use dateLastUsed column as expiration date
    String query = "SELECT * FROM adminUsers,users,userGroups" 
      + " WHERE adminUsers.ausrLogCode = users.logCode"
      + " AND usrAccessLevel = userGroups.userGroupId" 
      + " AND usrName = ?"
      + " AND usrPasswd = ?"
      + " AND userGroups.userGroupGrantLogin = '1'"
      + " AND (dateLastUsed IS NULL OR dateLastUsed > '" + now + "')";

    database = bean.getDBConnection(databaseId);

    DbRet dbRet = null;

    dbRet = database.beginTransaction(Database.TRANS_ISO_1);

    int prevTransIsolation = dbRet.getRetInt();
    
    try {
      ps = database.createPreparedStatement(query);
      ps.setString(1, empUsername);
      ps.setString(2, empPassword);
      
      resultSet = ps.executeQuery();
          
      if (resultSet.next() == true) {
        // username & password pair valid
        AuthEmp authEmp = new AuthEmp(empUsername,
                                      empPassword,
                                      resultSet.getInt("usrAccessLevel"),
                                      databaseId);

        // πρόσθεσε στο hashtable τον χρήστη
        bean.addAuthUser(databaseId,authEmp);

        //System.out.println("addAuthUser executed!!!");

        HttpSession session = null;

        session = request.getSession();

        session.setAttribute(databaseId + ".unbindObject", authEmp);
        
        // το username & password που θα συνοδεύει τον χρήστη
        // κατα την διάρκεια του session του
        session.setAttribute(databaseId + ".authUsername",empUsername);
        session.setAttribute(databaseId + ".authPassword",empPassword);
        session.setAttribute(databaseId + ".authGrantLogin","1");
        
        AUTHENTICATED = true;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      try { if (resultSet != null) resultSet.close(); } catch (Exception e) { }
      try { if (ps != null) ps.close(); } catch (Exception e) { }
    }

    database.commitTransaction(1,prevTransIsolation);

    bean.freeDBConnection(databaseId, database);
    
    return AUTHENTICATED;
  }
}