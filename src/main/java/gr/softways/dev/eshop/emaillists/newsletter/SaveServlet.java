package gr.softways.dev.eshop.emaillists.newsletter;

import java.io.*;
import java.util.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class SaveServlet extends HttpServlet {

  private Director _director;
  
  private String _charset = null;
  
  private String _databaseId = null;
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    _databaseId = SwissKnife.jndiLookup("swconf/databaseId");
    
    _director = Director.getInstance();
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    PrintWriter out = null;
    
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    DbRet dbRet = new DbRet();
    
    String authUsername = SwissKnife.getSessionAttr(_databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(_databaseId + ".authPassword",request);

    int auth = _director.auth(_databaseId, authUsername, authPassword,
        "Newsletter", Director.AUTH_INSERT);
    
    if (auth != Director.AUTH_OK) dbRet.setNoError(0);
    else dbRet = doSave(request);
    
    try {
      out = response.getWriter();

      out.println(dbRet.getNoError());

      out.close();
    }
    catch (Exception e) {
    }
  }
  
  private DbRet doSave(HttpServletRequest request) {
    DbRet dbRet = null;
    
    String NWLR_Code = null,
        NWLR_Title = request.getParameter("NWLR_Title"),
        NWLR_From = request.getParameter("from"),
        NWLR_Subject = request.getParameter("subject"),
        NWLR_ContentType = request.getParameter("mailContent"),
        NWLR_Charset = request.getParameter("mailCharset"),
        NWLR_Message = request.getParameter("body");
    
    Timestamp NWLR_Date = SwissKnife.currentDate();
            
    PreparedStatement ps = null;
    
    // get database connection
    Database database = _director.getDBConnection(_databaseId);

    // begin transaction
    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);
    
    int prevTransIsolation = dbRet.getRetInt();
    
    String query = null;
    
    SQLHelper2 sql = new SQLHelper2();
    
    dbRet = sql.getSQL(database, "SELECT NWLR_Code FROM Newsletter WHERE NWLR_Title = '" + SwissKnife.sqlEncode(NWLR_Title) + "'");
    if (dbRet.getNoError() == 1 && dbRet.getRetInt() > 0) NWLR_Code = sql.getColumn("NWLR_Code");
    sql.closeResources();
    
    if (NWLR_Code == null) {
      query = "INSERT INTO Newsletter ("
          + "NWLR_Code,NWLR_Date,NWLR_Title,NWLR_From,NWLR_Subject"
          + ",NWLR_ContentType,NWLR_Charset,NWLR_Message"
          + ") VALUES ("
          + "?,?,?,?,?,?,?,?"
          + ")";
      
      try {
        ps = database.createPreparedStatement(query);
        
        ps.setString(1, SwissKnife.buildPK());
        ps.setTimestamp(2, NWLR_Date);
        ps.setString(3, SwissKnife.sqlEncode(NWLR_Title));
        ps.setString(4, SwissKnife.sqlEncode(NWLR_From));
        ps.setString(5, SwissKnife.sqlEncode(NWLR_Subject));
        ps.setString(6, SwissKnife.sqlEncode(NWLR_ContentType));
        ps.setString(7, SwissKnife.sqlEncode(NWLR_Charset));
        ps.setString(8, SwissKnife.sqlEncode(NWLR_Message));
        
        ps.executeUpdate();
      }
      catch (Exception e) {
        e.printStackTrace();
        dbRet.setNoError(0);
      }
      finally {
        try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
      }
    }
    else {
      query = "UPDATE Newsletter SET"
          + " NWLR_Date = ?"
          + ",NWLR_Title = ?"
          + ",NWLR_From = ?"
          + ",NWLR_Subject = ?"
          + ",NWLR_ContentType = ?"
          + ",NWLR_Charset = ?"
          + ",NWLR_Message = ?"
          + " WHERE NWLR_Code = ?";
      
      try {
        ps = database.createPreparedStatement(query);
        
        ps.setTimestamp(1, NWLR_Date);
        ps.setString(2, SwissKnife.sqlEncode(NWLR_Title));
        ps.setString(3, SwissKnife.sqlEncode(NWLR_From));
        ps.setString(4, SwissKnife.sqlEncode(NWLR_Subject));
        ps.setString(5, SwissKnife.sqlEncode(NWLR_ContentType));
        ps.setString(6, SwissKnife.sqlEncode(NWLR_Charset));
        ps.setString(7, SwissKnife.sqlEncode(NWLR_Message));
        
        ps.setString(8, SwissKnife.sqlEncode(NWLR_Code));
        
        ps.executeUpdate();
      }
      catch (Exception e) {
        e.printStackTrace();
        dbRet.setNoError(0);
      }
      finally {
        try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
      }
    }
    
    // End transaction (commit or rollback)
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    _director.freeDBConnection(_databaseId,database);

    return dbRet;
  }
}