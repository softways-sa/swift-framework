package gr.softways.dev.eshop.custprczone.servlets;

import java.io.*;
import java.util.*;

import java.math.BigDecimal;

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
                         "prcZone", Director.AUTH_INSERT);

    if (auth != Director.AUTH_OK) {
      return auth;
    }
    
    String localeLanguage = request.getParameter("localeLanguage"),
           localeCountry = request.getParameter("localeCountry");

    String prcZoneId = request.getParameter("prcZoneId"),
           prcZoneName = SwissKnife.sqlEncode(request.getParameter("prcZoneName"));
    
    BigDecimal upLimitEU = SwissKnife.parseBigDecimal(request.getParameter("upLimitEU"),
                                                      localeLanguage, localeCountry),
               downLimitEU = SwissKnife.parseBigDecimal(request.getParameter("downLimitEU"),
                                                        localeLanguage, localeCountry);

    if (prcZoneId == null || prcZoneId.equals("")
      || prcZoneName.equals("") 
      || downLimitEU == null || upLimitEU == null)
      return Director.STATUS_ERROR;
    
    String query = "INSERT INTO prcZone (" 
                 + "prcZoneId,prcZoneName,downLimitEU,upLimitEU" 
                 + ") VALUES (" 
                 + prcZoneId 
                 + ",'" + prcZoneName + "'"
                 + "," + downLimitEU
                 + "," + upLimitEU 
                 + ")";

    return executeQuery(databaseId, query);
  }

  private int doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "prcZone", Director.AUTH_UPDATE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String localeLanguage = request.getParameter("localeLanguage"),
           localeCountry = request.getParameter("localeCountry");
    
    String prcZoneId = request.getParameter("prcZoneId"),
           prcZoneName = SwissKnife.sqlEncode(request.getParameter("prcZoneName"));
    
    BigDecimal upLimitEU = SwissKnife.parseBigDecimal(request.getParameter("upLimitEU"),
                                                      localeLanguage, localeCountry),
               downLimitEU = SwissKnife.parseBigDecimal(request.getParameter("downLimitEU"),
                                                        localeLanguage, localeCountry);
    int status = Director.STATUS_OK;

    if (prcZoneId == null || prcZoneId.equals("")
      || prcZoneName.equals("") 
      || downLimitEU.equals("") || upLimitEU == null)
      return Director.STATUS_ERROR;
    
    String query = "UPDATE prcZone SET" 
                 + " prcZoneName = '" + prcZoneName + "'"
                 + ",downLimitEU = " + downLimitEU
                 + ",upLimitEU = " + upLimitEU + " "
                 + " WHERE prcZoneId = " + prcZoneId;

    return executeQuery(databaseId, query);
  }

  private int doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "prcZone", Director.AUTH_DELETE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String prcZoneId = request.getParameter("prcZoneId");

    if (prcZoneId == null || prcZoneId.equals("")) return Director.STATUS_ERROR;
    
    String query = "DELETE FROM prcZone" 
                 + " WHERE prcZoneId = " + prcZoneId;

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