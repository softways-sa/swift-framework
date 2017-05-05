package gr.softways.dev.eshop.facet;

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class FacetServlet extends HttpServlet {

  private Director _director;

  private String _charset = null;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) {
      _charset = SwissKnife.DEFAULT_CHARSET;
    }

    _director = Director.getInstance();
  }

  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    DbRet dbRet = new DbRet();

    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);

    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1"),
        databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId"),
        urlSuccess = request.getParameter("urlSuccess") == null ? "" : request.getParameter("urlSuccess"),
        urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
        urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");

    if (databaseId.equals("")) {
      dbRet.setNoError(0);
    }
    else if (action.equals("INSERT")) {
      dbRet = doInsert(request, databaseId);
    }
    else if (action.equals("UPDATE")) {
      dbRet = doUpdate(request, databaseId);
    }
    else if (action.equals("DELETE")) {
      dbRet = doDelete(request, databaseId);
    }
    else {
      dbRet.setNoError(0);
    }

    if (dbRet.getNoError() == 1) {
      response.sendRedirect(urlSuccess);
    }
    else if (dbRet.getAuthError() == 1) {
      response.sendRedirect(urlNoAccess + "?authError=" + dbRet.getAuthErrorCode());
    }
    else if (dbRet.get_validError() == 1) {
      response.sendRedirect(urlFailure + "?validField=" + dbRet.getRetStr() + "&validError=" + dbRet.get_validErrorCode());
    }
    else if (dbRet.getDbErrorCode() == 1) {
      response.sendRedirect(urlFailure + "?dbMethod=" + dbRet.getRetStr() + "&dbError=" + dbRet.getDbErrorCode());
    }
    else {
      response.sendRedirect(urlFailure);
    }
  }

  private DbRet doInsert(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
        authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId, authUsername, authPassword, "Configuration", Director.AUTH_INSERT);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);

      return dbRet;
    }

    String name = request.getParameter("name"),
        nameLG = request.getParameter("nameLG");

    int display_order = 0;

    try {
      display_order = Integer.parseInt(request.getParameter("display_order"));
    }
    catch (Exception e) {
      display_order = 0;
    }

    if (name == null || name.length() == 0) {
      dbRet.setNoError(0);
      return dbRet;
    }

    PreparedStatement ps = null;

    Database database = _director.getDBConnection(databaseId);

    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();

    String query = "INSERT INTO facet (id,name,nameLG,display_order) VALUES (NEXT VALUE FOR facet_id_sequence,?,?,?)";

    if (dbRet.getNoError() == 1) {
      try {
        ps = database.createPreparedStatement(query);

        ps.setString(1, SwissKnife.sqlEncode(name));
        ps.setString(2, SwissKnife.sqlEncode(nameLG));
        ps.setInt(3, display_order);

        ps.executeUpdate();
      }
      catch (Exception e) {
        e.printStackTrace();
        dbRet.setNoError(0);
      }
      finally {
        try {
          if (ps != null) {
            ps.close();
          }
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    _director.freeDBConnection(databaseId, database);

    return dbRet;
  }

  private DbRet doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
        authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId, authUsername, authPassword, "facet", Director.AUTH_DELETE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);

      return dbRet;
    }

    int id = 0;

    try {
      id = Integer.parseInt(request.getParameter("id"));
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      return dbRet;
    }

    Database database = _director.getDBConnection(databaseId);

    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();

    String query = null;

    if (dbRet.getNoError() == 1) {
      query = "DELETE FROM facet WHERE id = '" + id + "'";

      dbRet = database.execQuery(query);
    }

    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    _director.freeDBConnection(databaseId, database);

    return dbRet;
  }

  private DbRet doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
        authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId, authUsername, authPassword, "facet", Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);

      return dbRet;
    }

    String name = request.getParameter("name"),
        nameLG = request.getParameter("nameLG");

    int display_order = 0;

    try {
      display_order = Integer.parseInt(request.getParameter("display_order"));
    }
    catch (Exception e) {
      display_order = 0;
    }

    int id = 0;

    try {
      id = Integer.parseInt(request.getParameter("id"));
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      return dbRet;
    }

    if (name == null || name.length() == 0) {
      dbRet.setNoError(0);
      return dbRet;
    }

    PreparedStatement ps = null;

    Database database = _director.getDBConnection(databaseId);

    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();

    String query = "UPDATE facet SET name = ?, nameLG = ?, display_order = ? WHERE id = ?";

    if (dbRet.getNoError() == 1) {
      try {
        ps = database.createPreparedStatement(query);

        ps.setString(1, SwissKnife.sqlEncode(name));
        ps.setString(2, SwissKnife.sqlEncode(nameLG));
        ps.setInt(3, display_order);
        ps.setInt(4, id);

        ps.executeUpdate();
      }
      catch (Exception e) {
        e.printStackTrace();
        dbRet.setNoError(0);
      }
      finally {
        try {
          if (ps != null) {
            ps.close();
          }
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    _director.freeDBConnection(databaseId, database);

    return dbRet;
  }
}
