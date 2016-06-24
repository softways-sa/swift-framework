package gr.softways.dev.eshop.countryzones.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class doAction extends HttpServlet {

  private Director bean;
    
  private String _charset = null;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    bean = Director.getInstance();
  }

  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
         throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1"),
           databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId"),
           urlSuccess = request.getParameter("urlSuccess") == null ? "" : request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");

    int status = Director.STATUS_OK;

    if (databaseId.equals(""))
      status = Director.STATUS_ERROR;
    else if (action.equals("INSERT"))
      status = doInsert(request, databaseId);
    else if (action.equals("UPDATE"))
      status = doUpdate(request, databaseId);
    else if (action.equals("DELETE"))
      status = doDelete(request, databaseId);

    if (status < 0) {
      response.sendRedirect(urlNoAccess);
    }
    else if (status == Director.STATUS_OK)
      response.sendRedirect(urlSuccess);
    else
      response.sendRedirect(urlFailure);
  }

  private int doInsert(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "countryZones", Director.AUTH_INSERT);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String CZCode = SwissKnife.sqlEncode(request.getParameter("CZCode")),
           CZName = SwissKnife.sqlEncode(request.getParameter("CZName"));

    if (CZCode.equals("")) return Director.STATUS_ERROR;

    String query = "INSERT INTO countryZones " +
      " (CZCode,CZName) " +
      " VALUES (" +
      "'" + CZCode + "'," +
      "'" + CZName + "'" +
      ")";

    return executeQuery(databaseId, query);
  }

  private int doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "countryZones", Director.AUTH_UPDATE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String CZCode = SwissKnife.sqlEncode(request.getParameter("CZCode")),
           CZName = SwissKnife.sqlEncode(request.getParameter("CZName"));

    if (CZCode.equals("")) return Director.STATUS_ERROR;
    
    String query = "UPDATE countryZones SET " +
      " CZName = '" + CZName + "'" +
      " WHERE CZCode = '" + CZCode + "'";

    return executeQuery(databaseId, query);
  }

  private int doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "countryZones", Director.AUTH_DELETE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }
    
    String CZCode = SwissKnife.sqlEncode(request.getParameter("CZCode"));

    String query = "DELETE FROM countryZones"
                 + " WHERE CZCode = '" + CZCode + "'";

    return executeQuery(databaseId, query);
  }

  /**
   *  �������� query ��' ������� ���� ����.
   *
   * @param  databaseId �� ������������� ��� ����� ���
   *                    �� ��������������
   * @param  query      �� query ���� ��������
   * @return            ��� ������ ����������
   */
  private int executeQuery(String databaseId, String query) {
    Database database = bean.getDBConnection(databaseId);

    int status = Director.STATUS_OK;
    
    DbRet dbRet = null;

    dbRet = database.execQuery(query);

    if (dbRet.getNoError() == 0) {
      status = Director.STATUS_ERROR;
    }

    bean.freeDBConnection(databaseId,database);

    return status;
  }
}