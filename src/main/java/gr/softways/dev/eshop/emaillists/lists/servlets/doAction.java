package gr.softways.dev.eshop.emaillists.lists.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

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
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1");
    
    if ("EXCEL".equals(action)) {
      response.setContentType("application/vnd.ms-excel; charset=" + _charset);
    }
    else {
      request.setCharacterEncoding(_charset);
      response.setContentType("text/html; charset=" + _charset);
    }
    
    String databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId").trim(),
           urlSuccess = request.getParameter("urlSuccess") == null ? "" : request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");

    int status = Director.STATUS_OK;

    if (databaseId.equals("")) status = Director.STATUS_ERROR;
    else if (action.equals("INSERT")) status = doInsert(request, databaseId);
    else if (action.equals("UPDATE")) status = doUpdate(request,databaseId);
    else if (action.equals("DELETE")) status = doDelete(request,databaseId);
    else if (action.equals("EXCEL")) doExportEXCEL(request,response,databaseId);

    if (status < 0) {
      response.sendRedirect(urlNoAccess);
    }
    else if (status == Director.STATUS_OK)
      response.sendRedirect(urlSuccess);
    else
      response.sendRedirect(urlFailure);
  }

  /**
    *  ���������� ���� ��� RDBMS.
    *
    * @param  request    �� HttpServletRequest ��� ��� ������
    * @param  databaseId �� ������������� ��� ����� ���
    *                    �� ��������������
    * @return            ������ ����������
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
    *  �������� ��� RDBMS.
    *
    * @param  request    �� HttpServletRequest ��� ��� ������
    * @param  databaseId �� ������������� ��� ����� ���
    *                    �� ��������������
    * @return            ������ ����������
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
   * ��������
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
  
  private DbRet doExportEXCEL(HttpServletRequest request,HttpServletResponse response,String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

    int auth = bean.auth(databaseId,authUsername,authPassword,"emailListTab",Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();
    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      return dbRet;
    }
    
    String EMLTCode = SwissKnife.sqlEncode( SwissKnife.grEncode( request.getParameter("EMLTCode") ) );
    
    StringBuilder query = new StringBuilder();
    query.append("SELECT EMLMEmail,EMLTName FROM emailListMember,emailListReg,emailListTab where EMLMCode = EMLRMemberCode AND EMLTCode = EMLRListCode AND EMLMActive = '1'");
    if (EMLTCode.length() > 0) {
      query.append(" AND EMLTCode = '").append(EMLTCode).append("'");
    }
    query.append(" ORDER BY EMLTName, EMLMEmail");
    
    ServletOutputStream out = null;
    
    String promptFilename = "newsletter_list_" + EMLTCode;
    response.addHeader("content-disposition","attachment; filename=" + promptFilename + ".xls");

    WritableWorkbook workbook = null;
    WritableSheet sheet = null;
    
    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();
    
    QueryDataSet queryDataSet = null;
    
    try {
      out = response.getOutputStream();
      
      workbook = Workbook.createWorkbook(out);
      sheet = workbook.createSheet("list",0);
      
      int excelRow = 0, index = -1;
      
      sheet.addCell(new Label(++index, excelRow, "�����"));
      sheet.addCell(new Label(++index, excelRow, "EMAIL"));
      
      queryDataSet = new QueryDataSet();
      
      queryDataSet.setQuery(new QueryDescriptor(database,query.toString(),null,true,Load.UNCACHED));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();
      
      while (queryDataSet.inBounds() == true) {
        excelRow++;
        
        index = -1;
        
        sheet.addCell(new Label(++index, excelRow, SwissKnife.sqlDecode(queryDataSet.getString("EMLTName"))));
        sheet.addCell(new Label(++index, excelRow, SwissKnife.sqlDecode(queryDataSet.getString("EMLMEmail"))));
        
        queryDataSet.next();
      }
      
      workbook.write();
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      
      e.printStackTrace();
    }
    finally {
      try { workbook.close(); } catch (Exception e) { e.printStackTrace(); }
      try { out.close(); } catch (Exception e) { e.printStackTrace(); }
      
      if (queryDataSet != null) try { queryDataSet.close(); } catch (Exception e) { e.printStackTrace(); }
    }
    
    database.commitTransaction(1,prevTransIsolation);
    
    director.freeDBConnection(databaseId,database);
    
    return new DbRet();
  }
}
