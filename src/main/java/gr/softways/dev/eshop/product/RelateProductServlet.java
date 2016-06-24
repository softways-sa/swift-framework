package gr.softways.dev.eshop.product;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class RelateProductServlet extends HttpServlet {

  private Director _director;
    
  private String _charset = null;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    _director = Director.getInstance();
  }

  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
         throws ServletException, IOException {
    DbRet dbRet = new DbRet();
    
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1"),
           databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId"),
           urlSuccess = request.getParameter("urlSuccess") == null ? "" :  request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");
    
    if (action.equals("RELATE")) {
      dbRet = doRelate(request,databaseId);
    }
    else if (action.equals("UNRELATE")) {
      dbRet = doUnrelate(request,databaseId);
    }
            
    if (dbRet.getNoError() == 1) {   
      response.sendRedirect(urlSuccess);
    }
    else {
      if (dbRet.getAuthError() == 1)
        response.sendRedirect(urlNoAccess + "?authError=" + dbRet.getAuthErrorCode());
      else if (dbRet.get_validError() == 1)
        response.sendRedirect(urlFailure + "?validField=" + dbRet.getRetStr() + "&validError=" + dbRet.get_validErrorCode());
      else if (dbRet.getDbErrorCode() == 1)
        response.sendRedirect(urlFailure + "?dbMethod=" + dbRet.getRetStr() + "&dbError=" + dbRet.getDbErrorCode());
      else response.sendRedirect(urlFailure);
    }
 }

  private DbRet doRelate(HttpServletRequest request, String databaseId) {  
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId, authUsername, authPassword, "product", Director.AUTH_INSERT);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
     
    String PRDRP_prdId = SwissKnife.sqlEncode(request.getParameter("PRDRP_prdId")),
           PRDRP_relPrdId = SwissKnife.sqlEncode(request.getParameter("PRDRP_relPrdId"));
    
    if (PRDRP_prdId.length() == 0 || PRDRP_relPrdId.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    
    String query = "INSERT INTO prdRelatedProducts ("
                 + "PRDRP_Code,PRDRP_prdId,PRDRP_relPrdId"
                 + ") VALUES (" 
                 + "'" + SwissKnife.buildPK() + "'"
                 + ",'" + PRDRP_prdId + "'"
                 + ",'" + PRDRP_relPrdId + "'"
                 + ")";
                 
    dbRet = database.execQuery(query);
    
    query = "INSERT INTO prdRelatedProducts ("
          + "PRDRP_Code,PRDRP_prdId,PRDRP_relPrdId"
          + ") VALUES (" 
          + "'" + SwissKnife.buildPK() + "'"
          + ",'" + PRDRP_relPrdId + "'"
          + ",'" + PRDRP_prdId + "'"
          + ")";
          
    dbRet = database.execQuery(query);
    
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  private DbRet doUnrelate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId, authUsername, authPassword, "product", Director.AUTH_DELETE);

    DbRet dbRet = new DbRet();
    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }

    String PRDRP_prdId = SwissKnife.sqlEncode(request.getParameter("PRDRP_prdId")),
           PRDRP_relPrdId = SwissKnife.sqlEncode(request.getParameter("PRDRP_relPrdId"));
    
    if (PRDRP_prdId.length() == 0 || PRDRP_relPrdId.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);

    String query = "DELETE FROM prdRelatedProducts" 
                 + " WHERE PRDRP_prdId = '" + PRDRP_prdId + "'"
                 + " AND PRDRP_relPrdId = '" + PRDRP_relPrdId + "'";
                 
    dbRet = database.execQuery(query);
    
    query = "DELETE FROM prdRelatedProducts" 
          + " WHERE PRDRP_prdId = '" + PRDRP_relPrdId + "'"
          + " AND PRDRP_relPrdId = '" + PRDRP_prdId + "'";
    
    dbRet = database.execQuery(query);

    _director.freeDBConnection(databaseId,database);

    return dbRet;
  }
}