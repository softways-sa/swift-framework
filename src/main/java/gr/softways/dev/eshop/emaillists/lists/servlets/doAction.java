package gr.softways.dev.eshop.emaillists.lists.servlets;

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
           databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId").trim(),
           urlSuccess = request.getParameter("urlSuccess") == null ? "" : request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");

    int status = Director.STATUS_OK;

    if (databaseId.equals(""))
      status = Director.STATUS_ERROR;
    else if (action.equals("INSERT"))
      status = doInsert(request, databaseId);
    else if (action.equals("UPDATE"))
      status = doUpdate(request,databaseId);
    else if (action.equals("DELETE"))
      status = doDelete(request,databaseId);

    if (status < 0) {
      response.sendRedirect(urlNoAccess);
    }
    else if (status == Director.STATUS_OK)
      response.sendRedirect(urlSuccess);
    else
      response.sendRedirect(urlFailure);
  }

  /**
    *  Καταχώρηση νέου στο RDBMS.
    *
    * @param  request    το HttpServletRequest από την σελίδα
    * @param  databaseId το αναγνωριστικό της βάσης που
    *                    θα χρησιμοποιηθεί
    * @return            κωδικό κατάστασης
   */
  public int doInsert(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);
    
    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "emailListTab", bean.AUTH_INSERT);

    if (auth < 0) {
      return auth;
    }

    DbRet dbRet = null;

    String EMLTName = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLTName") ) ),
           EMLTDescr = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLTDescr") ) ),
           EMLTTo = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLTTo") ) ),
           EMLTActive = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLTActive") ) ),
           EMLTField1 = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLTField1") ) ),
           EMLTField2 = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLTField2") ) );

    String EMLTCode = SwissKnife.buildPK();

    if (EMLTName.trim().equals(""))
      return Director.STATUS_ERROR;

    String EMLTNameUp = SwissKnife.searchConvert(EMLTName);

    Database database = bean.getDBConnection(databaseId);

    int status = Director.STATUS_OK;

    String insert = "INSERT INTO emailListTab (EMLTCode,EMLTName"
                    + ",EMLTNameUp,EMLTDescr,EMLTTo,EMLTActive"
                    + ",EMLTField1,EMLTField2"
                    + ")"
                    + " VALUES ("
                    + "'"  + EMLTCode            + "'"
                    + ",'" + EMLTName  + "'"
                    + ",'" + EMLTNameUp  + "'"
                    + ",'" + EMLTDescr  + "'"
                    + ",'" + EMLTTo  + "'"
                    + ",'" + EMLTActive  + "'"
                    + ",'"  + EMLTField1 + "'"
                    + ",'"  + EMLTField2 + "'"
                    + ")";

    dbRet = database.execQuery(insert);

    if (dbRet.getNoError() == 0) {
      status = Director.STATUS_ERROR;
    }

    bean.freeDBConnection(databaseId,database);
    return status;
  }

  /**
    *  Μεταβολή στο RDBMS.
    *
    * @param  request    το HttpServletRequest από την σελίδα
    * @param  databaseId το αναγνωριστικό της βάσης που
    *                    θα χρησιμοποιηθεί
    * @return            κωδικό κατάστασης
   */
  public int doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "emailListTab", bean.AUTH_UPDATE);

    if (auth < 0) {
      return auth;
    }

    String EMLTCode = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLTCode") ) ),
           EMLTName = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLTName") ) ),
           EMLTDescr = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLTDescr") ) ),
           EMLTTo = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLTTo") ) ),
           EMLTActive = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLTActive") ) ),
           EMLTField1 = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLTField1") ) ),
           EMLTField2 = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLTField2") ) );

    if (EMLTName.trim().equals(""))
      return Director.STATUS_ERROR;

    String EMLTNameUp = SwissKnife.searchConvert(EMLTName);
    
    Database database = bean.getDBConnection(databaseId);
    DbRet dbRet = null;
    
    int status = Director.STATUS_OK;

    String update = "UPDATE emailListTab SET "
                  + "EMLTName = '"  + EMLTName + "'"
                  + ",EMLTNameUp = '" + EMLTNameUp + "'"
                  + ",EMLTDescr = '" + EMLTDescr + "'"
                  + ",EMLTTo = '" + EMLTTo + "'"
                  + ",EMLTActive = '" + EMLTActive + "'"
                  + ",EMLTField1 = '" + EMLTField1 + "'"
                  + ",EMLTField2 = '"  + EMLTField2 + "'"
                  + " WHERE EMLTCode = '" + EMLTCode + "'";

    dbRet = database.execQuery(update);
    
    if (dbRet.getNoError() == 0) {
      status = Director.STATUS_ERROR;
    }

    bean.freeDBConnection(databaseId,database);
    return status;
  }


  /**
   * Διαγραφή
   *
   */
  public int doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int authStatus = 0;

    authStatus = bean.auth(databaseId,authUsername,authPassword,
                           "emailListTab", bean.AUTH_DELETE);
 
    if (authStatus < 0) {
      return authStatus;
    }

    String EMLTCode = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLTCode") ) );

    if (EMLTCode.trim().equals(""))
      return Director.STATUS_ERROR;

    Database database = bean.getDBConnection(databaseId);

    int status = Director.STATUS_OK;

    DbRet dbRet = null;

    String delete = "DELETE FROM emailListTab"
                  + " WHERE EMLTCode = '" + EMLTCode + "'";

    dbRet = database.execQuery(delete);

    if (dbRet.getNoError() == 0) {
      status = Director.STATUS_ERROR;
    }

    bean.freeDBConnection(databaseId,database);
    return status;
  }
}
