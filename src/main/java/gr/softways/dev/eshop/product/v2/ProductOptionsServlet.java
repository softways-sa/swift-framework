package gr.softways.dev.eshop.product.v2;

import java.io.*;
import java.util.*;
import java.math.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class ProductOptionsServlet extends HttpServlet {

  private Director _director;

  private String _charset = null;
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    _director = Director.getInstance();
  }

  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    DbRet dbRet = new DbRet();
    
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1"),
           databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId"),
           urlSuccess = request.getParameter("urlSuccess") == null ? "" :  request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");

    if (databaseId.equals("")) {
      dbRet.setNoError(0);
    }
    else if (action.equals("INSERT")) {
      dbRet = doInsert(request,databaseId);
    }
    else if (action.equals("UPDATE")) {
      dbRet = doUpdate(request,databaseId);
    }
    else if (action.equals("DELETE")) {
      dbRet = doDelete(request,databaseId);
    }
    else if (action.equals("UPDATE_CLOTH_ATTRS")) {
      dbRet = doUpdateClothAttrs(request,databaseId);
    }
    else {
      dbRet.setNoError(0);
    }
    
    if (dbRet.getNoError() == 1) {
       response.sendRedirect(urlSuccess);
    }
    else {
      if (dbRet.getAuthError() == 1) {
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
  }

  private DbRet doInsert(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"ProductOptions",Director.AUTH_INSERT);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String localeLanguage = request.getParameter("localeLanguage"),
           localeCountry = request.getParameter("localeCountry");
    
    String PO_Code = request.getParameter("PO_Code"),
           PO_prdId = request.getParameter("PO_prdId"),
           PO_Name = request.getParameter("PO_Name"),
           PO_NameLG = request.getParameter("PO_NameLG"),
           PO_NameLG1 = request.getParameter("PO_NameLG1"),
           PO_NameLG2 = request.getParameter("PO_NameLG2"),
           PO_Enabled = request.getParameter("PO_Enabled");
        
    if  (PO_Code == null || PO_Code.length() == 0) {
      PO_Code = SwissKnife.buildPK();
    }
    
    BigDecimal PO_RetailPrcEU = SwissKnife.parseBigDecimal(request.getParameter("PO_RetailPrcEU"), localeLanguage, localeCountry),
        PO_WholesalePrcEU = SwissKnife.parseBigDecimal(request.getParameter("PO_WholesalePrcEU"), localeLanguage, localeCountry),
        PO_RetailOfferPrcEU = SwissKnife.parseBigDecimal(request.getParameter("PO_RetailOfferPrcEU"), localeLanguage, localeCountry),
        PO_WholesaleOfferPrcEU = SwissKnife.parseBigDecimal(request.getParameter("PO_WholesaleOfferPrcEU"), localeLanguage, localeCountry);
    
    if (PO_RetailPrcEU == null) PO_RetailPrcEU = _zero;
    if (PO_WholesalePrcEU == null) PO_WholesalePrcEU = _zero;
    if (PO_RetailOfferPrcEU == null) PO_RetailOfferPrcEU = _zero;
    if (PO_WholesaleOfferPrcEU == null) PO_WholesaleOfferPrcEU = _zero;
    
    int PO_Order = 0;
    
    try {
      PO_Order = Integer.parseInt(request.getParameter("PO_Order"));
    }
    catch (Exception e) {
      PO_Order = 0;
    }
    
    if (localeLanguage == null || localeLanguage.length() == 0 || localeCountry == null || localeCountry.length() == 0 
        || PO_prdId == null || PO_prdId.length() == 0 || PO_Name == null || PO_Name.length() == 0) {
      dbRet.setNoError(0);
      return dbRet;
    }
    
    PreparedStatement ps = null;
    
    Database database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    String query = "INSERT INTO ProductOptions ("
                 + " PO_Code,PO_prdId,PO_Name,PO_NameLG,PO_NameLG1,PO_NameLG2,PO_RetailPrcEU,PO_WholesalePrcEU,PO_RetailOfferPrcEU"
                 + ",PO_WholesaleOfferPrcEU,PO_Order,PO_Enabled"
                 + ") VALUES ("
                 + "?,?,?,?,?,?,?,?,?,?,?,?"
                 + ")";
                 
    if (dbRet.getNoError() == 1) {
      try {
        ps = database.createPreparedStatement(query);
        
        ps.setString(1, SwissKnife.sqlEncode(PO_Code));
        ps.setString(2, SwissKnife.sqlEncode(PO_prdId));
        ps.setString(3, SwissKnife.sqlEncode(PO_Name));
        ps.setString(4, SwissKnife.sqlEncode(PO_NameLG));
        ps.setString(5, SwissKnife.sqlEncode(PO_NameLG1));
        ps.setString(6, SwissKnife.sqlEncode(PO_NameLG2));
        
        ps.setBigDecimal(7, PO_RetailPrcEU);
        ps.setBigDecimal(8, PO_WholesalePrcEU);
        ps.setBigDecimal(9, PO_RetailOfferPrcEU);
        ps.setBigDecimal(10, PO_WholesaleOfferPrcEU);
        
        ps.setInt(11, PO_Order);
        
        ps.setString(12, SwissKnife.sqlEncode(PO_Enabled));
        
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
    
    dbRet = database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    _director.freeDBConnection(databaseId,database);
    
    return dbRet;
  }
  
  private DbRet doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"ProductOptions",Director.AUTH_DELETE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String PO_Code = request.getParameter("PO_Code"),
        PO_prdId = request.getParameter("PO_prdId");
    
    if (PO_Code == null || PO_Code.length() == 0 || PO_prdId == null || PO_prdId.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    String query = null;
    
    if (dbRet.getNoError() == 1) {
      query = "DELETE FROM ProductOptions WHERE PO_Code = '" + SwissKnife.sqlEncode(PO_Code) + "' AND PO_prdId = '" + SwissKnife.sqlEncode(PO_prdId) + "'";
      
      dbRet = database.execQuery(query);
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    _director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  private DbRet doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"ProductOptions",Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String localeLanguage = request.getParameter("localeLanguage"),
           localeCountry = request.getParameter("localeCountry");
    
    String PO_Code = request.getParameter("PO_Code"),
        PO_prdId = request.getParameter("PO_prdId"),
        PO_Name = request.getParameter("PO_Name"),
        PO_NameLG = request.getParameter("PO_NameLG"),
        PO_NameLG1 = request.getParameter("PO_NameLG1"),
        PO_NameLG2 = request.getParameter("PO_NameLG2"),
        PO_Enabled = request.getParameter("PO_Enabled");
    
    BigDecimal PO_RetailPrcEU = SwissKnife.parseBigDecimal(request.getParameter("PO_RetailPrcEU"), localeLanguage, localeCountry),
        PO_WholesalePrcEU = SwissKnife.parseBigDecimal(request.getParameter("PO_WholesalePrcEU"), localeLanguage, localeCountry),
        PO_RetailOfferPrcEU = SwissKnife.parseBigDecimal(request.getParameter("PO_RetailOfferPrcEU"), localeLanguage, localeCountry),
        PO_WholesaleOfferPrcEU = SwissKnife.parseBigDecimal(request.getParameter("PO_WholesaleOfferPrcEU"), localeLanguage, localeCountry);
    
    if (PO_RetailPrcEU == null) PO_RetailPrcEU = _zero;
    if (PO_WholesalePrcEU == null) PO_WholesalePrcEU = _zero;
    if (PO_RetailOfferPrcEU == null) PO_RetailOfferPrcEU = _zero;
    if (PO_WholesaleOfferPrcEU == null) PO_WholesaleOfferPrcEU = _zero;
    
    int PO_Order = 0;
    
    try {
      PO_Order = Integer.parseInt(request.getParameter("PO_Order"));
    }
    catch (Exception e) {
      PO_Order = 0;
    }
    
    if (localeLanguage == null || localeLanguage.length() == 0 || localeCountry == null || localeCountry.length() == 0 
        || PO_Code == null || PO_Code.length() == 0 || PO_prdId == null || PO_prdId.length() == 0
        || PO_Name == null || PO_Name.length() == 0) {
      dbRet.setNoError(0);
      return dbRet;
    }
    
    PreparedStatement ps = null;
    
    Database database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    String query = "UPDATE ProductOptions SET"
        + " PO_Name = ?"
        + ",PO_NameLG = ?"
        + ",PO_NameLG1 = ?"
        + ",PO_NameLG2 = ?"
        + ",PO_RetailPrcEU = ?"
        + ",PO_WholesalePrcEU = ?"
        + ",PO_RetailOfferPrcEU = ?"
        + ",PO_WholesaleOfferPrcEU = ?"
        + ",PO_Order = ?"
        + ",PO_Enabled = ?"
        + " WHERE PO_Code = ? AND PO_prdId = ?";
    
    if (dbRet.getNoError() == 1) {
      try {
        ps = database.createPreparedStatement(query);
        
        ps.setString(1, SwissKnife.sqlEncode(PO_Name));
        ps.setString(2, SwissKnife.sqlEncode(PO_NameLG));
        ps.setString(3, SwissKnife.sqlEncode(PO_NameLG1));
        ps.setString(4, SwissKnife.sqlEncode(PO_NameLG2));
        ps.setBigDecimal(5, PO_RetailPrcEU);
        ps.setBigDecimal(6, PO_WholesalePrcEU);
        ps.setBigDecimal(7, PO_RetailOfferPrcEU);
        ps.setBigDecimal(8, PO_WholesaleOfferPrcEU);
        ps.setInt(9, PO_Order);
        ps.setString(10, SwissKnife.sqlEncode(PO_Enabled));
        
        ps.setString(11, SwissKnife.sqlEncode(PO_Code));
        ps.setString(12, SwissKnife.sqlEncode(PO_prdId));
        
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
    
    dbRet = database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    _director.freeDBConnection(databaseId,database);
    
    return dbRet;
  }
  
  private DbRet doUpdateClothAttrs(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"ProductOptions",Director.AUTH_INSERT);

    DbRet dbRet = new DbRet();

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    String prdId = request.getParameter("prdId");
    
    if (prdId == null || prdId.length() == 0) {
      dbRet.setNoError(0);
      return dbRet;
    }
    
    int clothsRowCount = 0;
    
    try {
      clothsRowCount = Integer.parseInt(request.getParameter("clothsRowCount"));
    }
    catch (Exception e) {
      clothsRowCount = 0;
    }
    
    String CLAT_Code = null;
    
    PreparedStatement ps = null, clothPs = null;
    
    QueryDataSet queryDataSet = null, colorDataSet = null, sizeDataSet = null;
    
    Database database = _director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    String query = "INSERT INTO ProductOptions ("
                 + " PO_Code,PO_prdId,PO_Name,PO_NameLG,PO_NameLG1,PO_NameLG2,PO_RetailPrcEU,PO_WholesalePrcEU,PO_RetailOfferPrcEU"
                 + ",PO_WholesaleOfferPrcEU,PO_Order,PO_Enabled"
                 + ") VALUES ("
                 + "?,?,?,?,?,?,?,?,?,?,?,?"
                 + ")";
    
    String clothQuery = "INSERT INTO ProductOptionsCloth ("
                 + " POC_Code,POC_prdId,POC_CLAT_Code"
                 + ") VALUES ("
                 + "?,?,?"
                 + ")";
                 
    if (dbRet.getNoError() == 1) {
      try {
        database.execQuery("DELETE FROM ProductOptionsCloth WHERE POC_prdId ='" + SwissKnife.sqlEncode(prdId) + "'");
        database.execQuery("DELETE FROM ProductOptions WHERE PO_prdId = '" + SwissKnife.sqlEncode(prdId) + "'");
            
        ps = database.createPreparedStatement(query);
        clothPs = database.createPreparedStatement(clothQuery);
        
        queryDataSet = new QueryDataSet();
        queryDataSet.setQuery(new QueryDescriptor(database,"SELECT * FROM ClothAttrs",null,true,Load.UNCACHED));
        queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
        queryDataSet.refresh();
        
        while (queryDataSet.inBounds() == true) {
          CLAT_Code = request.getParameter("CLAT_Code" + queryDataSet.getString("CLAT_Code").trim());
          
          if (CLAT_Code != null && CLAT_Code.length()>0) {
            clothPs.setString(1, SwissKnife.buildPK());
            clothPs.setString(2, SwissKnife.sqlEncode(prdId));
            clothPs.setString(3, queryDataSet.getString("CLAT_Code"));
            clothPs.executeUpdate();
          }
          
          queryDataSet.next();
        }
        
        colorDataSet = new QueryDataSet();
        colorDataSet.setQuery(new QueryDescriptor(database,"SELECT CLAT_Name,CLAT_NameLG,CLAT_NameLG1,CLAT_NameLG2,CLAT_Order,CLAT_AttrCode FROM ClothAttrs,ProductOptionsCloth WHERE POC_CLAT_Code = CLAT_Code AND CLAT_GID='1' AND POC_prdId ='" + SwissKnife.sqlEncode(prdId) + "'",null,true,Load.UNCACHED));
        colorDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
        colorDataSet.refresh();
        
        sizeDataSet = new QueryDataSet();
        sizeDataSet.setQuery(new QueryDescriptor(database,"SELECT CLAT_Name,CLAT_NameLG,CLAT_NameLG1,CLAT_NameLG2,CLAT_Order,CLAT_AttrCode FROM ClothAttrs,ProductOptionsCloth WHERE POC_CLAT_Code = CLAT_Code AND CLAT_GID='2' AND POC_prdId ='" + SwissKnife.sqlEncode(prdId) + "'",null,true,Load.ALL));
        sizeDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
        sizeDataSet.refresh();
        
        while (colorDataSet.inBounds() == true) {
          
          sizeDataSet.goToRow(0);
          
          while (sizeDataSet.inBounds() == true) {
            ps.setString(1, colorDataSet.getString("CLAT_AttrCode").trim() + sizeDataSet.getString("CLAT_AttrCode").trim());
            ps.setString(2, SwissKnife.sqlEncode(prdId));
            ps.setString(3, colorDataSet.getString("CLAT_Name") + " - " + sizeDataSet.getString("CLAT_Name"));
            ps.setString(4, colorDataSet.getString("CLAT_NameLG") + " - " + sizeDataSet.getString("CLAT_NameLG"));
            ps.setString(5, colorDataSet.getString("CLAT_NameLG1") + " - " + sizeDataSet.getString("CLAT_NameLG1"));
            ps.setString(6, colorDataSet.getString("CLAT_NameLG2") + " - " + sizeDataSet.getString("CLAT_NameLG2"));
            ps.setBigDecimal(7, _zero);
            ps.setBigDecimal(8, _zero);
            ps.setBigDecimal(9, _zero);
            ps.setBigDecimal(10, _zero);
            ps.setInt(11, queryDataSet.getInt("CLAT_Order"));
            ps.setString(12, "1");
            ps.executeUpdate();

            sizeDataSet.next();
          }
          
          colorDataSet.next();
        }
      }
      catch (Exception e) {
        e.printStackTrace();
        dbRet.setNoError(0);
      }
      finally {
        try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
        try { if (clothPs != null) clothPs.close(); } catch (Exception e) { e.printStackTrace(); }
        try { if (queryDataSet != null) queryDataSet.close(); } catch (Exception e) { e.printStackTrace(); }
        try { if (colorDataSet != null) colorDataSet.close(); } catch (Exception e) { e.printStackTrace(); }
        try { if (sizeDataSet != null) sizeDataSet.close(); } catch (Exception e) { e.printStackTrace(); }
      }
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    _director.freeDBConnection(databaseId,database);
    
    return dbRet;
  }
  
  private BigDecimal _zero = new BigDecimal("0");
}