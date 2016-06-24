package gr.softways.dev.eshop.category.v2.servlets;

import java.io.*;
import java.util.*;
import java.math.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class PrdCategory2_1 extends HttpServlet {
  
  private Director bean;

  private String _charset = null;
  
  private int _maxUploadSizeKB = 4 * 1024;
  
  private String _uploadPropertiesFilename = null;
  
  Properties parameters = new Properties();
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    _uploadPropertiesFilename = SwissKnife.jndiLookup("upload/properties");
    
    if (config.getInitParameter("maxUploadSizeKB") != null) {
      _maxUploadSizeKB = Integer.parseInt(config.getInitParameter("maxUploadSizeKB"));
    }
    
    bean = Director.getInstance();
  }
  
  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    DbRet dbRet = new DbRet();
    
    MultiRequest multi = null;
    
    String uploadPath = null, tmpDeny = null;
    
    StrTokenizer denyFilesExtTokenizer = null;
    
    try {
      parameters.load( new FileInputStream( _uploadPropertiesFilename ) );
             
      String tmpPath = parameters.getProperty("uploadTransientPath", "/");

      tmpDeny = parameters.getProperty("uploadDenyFilesExt");

      if (tmpDeny != null && tmpDeny.length()>0) {
       denyFilesExtTokenizer = new StrTokenizer(tmpDeny, '|');
      }
      
      multi = new MultiRequest(request, tmpPath, _maxUploadSizeKB * 1024, _charset);
    }
    catch (Exception e) {
      e.printStackTrace();
      dbRet.setNoError(0);
    }
            
    String action = multi.getParameter("action1") == null ? "" : multi.getParameter("action1"),
           databaseId = multi.getParameter("databaseId") == null ? "" : multi.getParameter("databaseId"),
           urlSuccess = multi.getParameter("urlSuccess") == null ? "" : multi.getParameter("urlSuccess"),
           urlFailure = multi.getParameter("urlFailure") == null ? "" : multi.getParameter("urlFailure"),
           urlNoAccess = multi.getParameter("urlNoAccess") == null ? "" : multi.getParameter("urlNoAccess");
           
    uploadPath = multi.getParameter("uploadPath");
         
    if (databaseId.equals("")) {
      dbRet.setNoError(0);
    }
    else if (action.equals("INSERT")) {
      dbRet = doInsert(request, databaseId, multi, denyFilesExtTokenizer, uploadPath);
    }
    else if (action.equals("UPDATE")) {
      dbRet = doUpdate(request, databaseId, multi, denyFilesExtTokenizer, uploadPath);
    }
    else if (action.equals("DELETE")) {
      dbRet = doDelete(request, databaseId, multi, uploadPath);
    }
    else if (action.equals("DELETE_IMG")){
      dbRet = doDeleteImg(request, databaseId, multi, uploadPath);
    }
    else {
      dbRet.setNoError(0);
    }
    
    if (dbRet.getNoError() == 0) {
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
        response.sendRedirect(urlFailure + "?error=" + dbRet.getRetStr());
      }
    }
    else {
      response.sendRedirect(urlSuccess);
    }
  }
  
  private DbRet doInsert(HttpServletRequest request,String databaseId,MultiRequest multi,StrTokenizer denyFilesExtTokenizer,String uploadPath) {
    DbRet dbRet = new DbRet();
    
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = bean.auth(databaseId,authUsername,authPassword,"prdCategory",Director.AUTH_INSERT);

     if (auth != Director.AUTH_OK) {
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      dbRet.setNoError(0);
      
      return dbRet;
    }

    String catId = SwissKnife.sqlEncode(multi.getParameter("catId")),
           catShowFlag = SwissKnife.sqlEncode(multi.getParameter("catShowFlag")),
           catParentFlag = SwissKnife.sqlEncode(multi.getParameter("catParentFlag")),
           name = SwissKnife.sqlEncode(multi.getParameter("catName")),
           nameLG = SwissKnife.sqlEncode(multi.getParameter("catNameLG")),
           nameLG1 = SwissKnife.sqlEncode(multi.getParameter("catNameLG1")),
           nameLG2 = SwissKnife.sqlEncode(multi.getParameter("catNameLG2")),
           nameLG3 = SwissKnife.sqlEncode(multi.getParameter("catNameLG3")),
           keywords = SwissKnife.sqlEncode(multi.getParameter("keywords")),
           keywordsLG = SwissKnife.sqlEncode(multi.getParameter("keywordsLG")),
           keywordsLG1 = SwissKnife.sqlEncode(multi.getParameter("keywordsLG1")),
           keywordsLG2 = SwissKnife.sqlEncode(multi.getParameter("keywordsLG2")),
           keywordsLG3 = SwissKnife.sqlEncode(multi.getParameter("keywordsLG3")),
           catDescr = SwissKnife.sqlEncode(multi.getParameter("catDescr")),
           catDescrLG = SwissKnife.sqlEncode(multi.getParameter("catDescrLG")),
           catDescrLG1 = SwissKnife.sqlEncode(multi.getParameter("catDescrLG1")),
           catDescrLG2 = SwissKnife.sqlEncode(multi.getParameter("catDescrLG2")),
           catDescrLG3 = SwissKnife.sqlEncode(multi.getParameter("catDescrLG3")),
           catCustomerType = SwissKnife.sqlEncode(multi.getParameter("catCustomerType"));
          
    int catRank = 0;
    
    try {
      catRank = Integer.parseInt(SwissKnife.sqlEncode(multi.getParameter("catRank")));
    }
    catch (Exception e) {
      catRank = 0;
    }
        
    String nameUp = SwissKnife.searchConvert(name),
           nameUpLG = SwissKnife.searchConvert(nameLG),
           nameUpLG1 = SwissKnife.searchConvert(nameLG1),
           nameUpLG2 = SwissKnife.searchConvert(nameLG2),
           nameUpLG3 = SwissKnife.searchConvert(nameLG3),
           keywordsUp = SwissKnife.searchConvert(keywords),
           keywordsUpLG = SwissKnife.searchConvert(keywordsLG),
           keywordsUpLG1 = SwissKnife.searchConvert(keywordsLG1),
           keywordsUpLG2 = SwissKnife.searchConvert(keywordsLG2),
           keywordsUpLG3 = SwissKnife.searchConvert(keywordsLG3);
    
    String nameLG4 = multi.getParameter("catNameLG4"),
        keywordsLG4 = multi.getParameter("keywordsLG4"),
        catDescrLG4 = multi.getParameter("catDescrLG4"),
        nameLG5 = multi.getParameter("catNameLG5"),
        keywordsLG5 = multi.getParameter("keywordsLG5"),
        catDescrLG5 = multi.getParameter("catDescrLG5"),
        nameLG6 = multi.getParameter("catNameLG6"),
        keywordsLG6 = multi.getParameter("keywordsLG6"),
        catDescrLG6 = multi.getParameter("catDescrLG6"),
        nameLG7 = multi.getParameter("catNameLG7"),
        keywordsLG7 = multi.getParameter("keywordsLG7"),
        catDescrLG7 = multi.getParameter("catDescrLG7");
    
    String nameUpLG4 = SwissKnife.searchConvert(nameLG4),
        keywordsUpLG4 = SwissKnife.searchConvert(keywordsLG4),
        nameUpLG5 = SwissKnife.searchConvert(nameLG5),
        keywordsUpLG5 = SwissKnife.searchConvert(keywordsLG5),
        nameUpLG6 = SwissKnife.searchConvert(nameLG6),
        keywordsUpLG6 = SwissKnife.searchConvert(keywordsLG6),
        nameUpLG7 = SwissKnife.searchConvert(nameLG7),
        keywordsUpLG7 = SwissKnife.searchConvert(keywordsLG7);
    
    if (catId.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    String[] image = new String[]{"",""},
	           imageUpload = new String[]{"",""};
             
    int j = -1, prevTransIsolation = 0, length = 0;
    
    Enumeration myFiles = multi.getFileNames();
    
    while(myFiles.hasMoreElements()) {
      imageUpload[++j] = myFiles.nextElement().toString();
    }
    
    MultiRequest.sort(imageUpload);
    
    length = imageUpload.length;
    
    for (int i=0; i<length; i++) {
        dbRet = MultiRequest.insertFile(multi,imageUpload[i],denyFilesExtTokenizer, uploadPath);
      
        if (dbRet.getNoError() == 1) {
            image[i] = dbRet.getRetStr();
        }
        else break;
    }
    
    Database database = null;
    
    PreparedStatement ps = null;
   
    database = bean.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    
    prevTransIsolation = dbRet.getRetInt();

    String query = "INSERT INTO prdCategory (catId,catName,catNameUp,catNameLG,catNameUpLG"
        + ",catNameLG1,catNameUpLG1,catNameLG2,catNameUpLG2,catNameLG3,catNameUpLG3,keywords"
        + ",keywordsUp,keywordsLG,keywordsUpLG,keywordsLG1,keywordsUpLG1,keywordsLG2,keywordsUpLG2"
        + ",keywordsLG3,keywordsUpLG3,catShowFlag,catParentFlag,catRank,catDescr,catDescrLG"
        + ",catDescrLG1,catDescrLG2,catDescrLG3,catImgName1,catImgName2,catCustomerType";
    
    if (nameLG4 != null) {
      query += ",catNameLG4,catNameUpLG4,keywordsLG4,keywordsUpLG4,catDescrLG4";
    }
    if (nameLG5 != null) {
      query += ",catNameLG5,catNameUpLG5,keywordsLG5,keywordsUpLG5,catDescrLG5";
    }
    if (nameLG6 != null) {
      query += ",catNameLG6,catNameUpLG6,keywordsLG6,keywordsUpLG6,catDescrLG6";
    }
    if (nameLG7 != null) {
      query += ",catNameLG7,catNameUpLG7,keywordsLG7,keywordsUpLG7,catDescrLG7";
    }
    
    query += ") VALUES ("
        + "?,?,?,?,?,?,?,?,?,?"
        + ",?,?,?,?,?,?,?,?,?,?"
        + ",?,?,?,?,?,?,?,?,?,?"
        + ",?,?";
    
    if (nameLG4 != null) {
      query += ",?,?,?,?,?";
    }
    if (nameLG5 != null) {
      query += ",?,?,?,?,?";
    }
    if (nameLG6 != null) {
      query += ",?,?,?,?,?";
    }
    if (nameLG7 != null) {
      query += ",?,?,?,?,?";
    }
    
    query += ")";
    
    int colIndex = 0;
    
    if (dbRet.getNoError() == 1) {
      try {
        ps = database.createPreparedStatement(query);
    
        ps.setString(1, catId);
        ps.setString(2, name);
        ps.setString(3, nameUp);
        ps.setString(4, nameLG);
        ps.setString(5, nameUpLG);
        ps.setString(6, nameLG1);
        ps.setString(7, nameUpLG1);
        ps.setString(8, nameLG2);
        ps.setString(9, nameUpLG2);
        ps.setString(10, nameLG3);
        ps.setString(11, nameUpLG3);
        ps.setString(12, keywords);
        ps.setString(13, keywordsUp);
        ps.setString(14, keywordsLG);
        ps.setString(15, keywordsUpLG);
        ps.setString(16, keywordsLG1);
        ps.setString(17, keywordsUpLG1);
        ps.setString(18, keywordsLG2);
        ps.setString(19, keywordsUpLG2);
        ps.setString(20, keywordsLG3);
        ps.setString(21, keywordsUpLG3);
        ps.setString(22, catShowFlag);
        ps.setString(23, catParentFlag);
        ps.setInt(24, catRank);
        ps.setString(25, catDescr);
        ps.setString(26, catDescrLG);
        ps.setString(27, catDescrLG1);
        ps.setString(28, catDescrLG2);
        ps.setString(29, catDescrLG3);
        ps.setString(30, image[0]);
        ps.setString(31, image[1]);
        ps.setString(32, catCustomerType);
        
        colIndex = 32;
        
        if (nameLG4 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameUpLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(keywordsLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(keywordsUpLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(catDescrLG4));
        }
        if (nameLG5 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameUpLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(keywordsLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(keywordsUpLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(catDescrLG5));
        }
        if (nameLG6 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameUpLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(keywordsLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(keywordsUpLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(catDescrLG6));
        }
        if (nameLG7 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameUpLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(keywordsLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(keywordsUpLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(catDescrLG7));
        }
        
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

    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    bean.freeDBConnection(databaseId,database);
    
    if (dbRet.getNoError() == 0) {
      MultiRequest.deleteFiles(uploadPath, image);
    }

    return dbRet;
  }

  private DbRet doUpdate(HttpServletRequest request,String databaseId,MultiRequest multi,StrTokenizer denyFilesExtTokenizer,String uploadPath) {
    DbRet dbRet = new DbRet();
    
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = bean.auth(databaseId,authUsername,authPassword,"prdCategory",Director.AUTH_UPDATE);

     if (auth != Director.AUTH_OK) {
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      dbRet.setNoError(0);
      
      return dbRet;
    }

    String catId = SwissKnife.sqlEncode(multi.getParameter("catId")),
           catShowFlag = SwissKnife.sqlEncode(multi.getParameter("catShowFlag")),
           catParentFlag = SwissKnife.sqlEncode(multi.getParameter("catParentFlag")),
           name = SwissKnife.sqlEncode(multi.getParameter("catName")),
           nameLG = SwissKnife.sqlEncode(multi.getParameter("catNameLG")),
           nameLG1 = SwissKnife.sqlEncode(multi.getParameter("catNameLG1")),
           nameLG2 = SwissKnife.sqlEncode(multi.getParameter("catNameLG2")),
           nameLG3 = SwissKnife.sqlEncode(multi.getParameter("catNameLG3")),
           keywords = SwissKnife.sqlEncode(multi.getParameter("keywords")),
           keywordsLG = SwissKnife.sqlEncode(multi.getParameter("keywordsLG")),
           keywordsLG1 = SwissKnife.sqlEncode(multi.getParameter("keywordsLG1")),
           keywordsLG2 = SwissKnife.sqlEncode(multi.getParameter("keywordsLG2")),
           keywordsLG3 = SwissKnife.sqlEncode(multi.getParameter("keywordsLG3")),
           catDescr = SwissKnife.sqlEncode(multi.getParameter("catDescr")),
           catDescrLG = SwissKnife.sqlEncode(multi.getParameter("catDescrLG")),
           catDescrLG1 = SwissKnife.sqlEncode(multi.getParameter("catDescrLG1")),
           catDescrLG2 = SwissKnife.sqlEncode(multi.getParameter("catDescrLG2")),
           catDescrLG3 = SwissKnife.sqlEncode(multi.getParameter("catDescrLG3")),
           catCustomerType = SwissKnife.sqlEncode(multi.getParameter("catCustomerType"));
          
    String catImgName1 = SwissKnife.sqlEncode(multi.getParameter("img01")),
           catImgName2 = SwissKnife.sqlEncode(multi.getParameter("img02"));
    
    int catRank = 0;
    
    try {
      catRank = Integer.parseInt(SwissKnife.sqlEncode(multi.getParameter("catRank")));
    }
    catch (Exception e) {
      catRank = 0;
    }
    
    String nameUp = SwissKnife.searchConvert(name),
           nameUpLG = SwissKnife.searchConvert(nameLG),
           nameUpLG1 = SwissKnife.searchConvert(nameLG1),
           nameUpLG2 = SwissKnife.searchConvert(nameLG2),
           nameUpLG3 = SwissKnife.searchConvert(nameLG3),
           keywordsUp = SwissKnife.searchConvert(keywords),
           keywordsUpLG = SwissKnife.searchConvert(keywordsLG),
           keywordsUpLG1 = SwissKnife.searchConvert(keywordsLG1),
           keywordsUpLG2 = SwissKnife.searchConvert(keywordsLG2),
           keywordsUpLG3 = SwissKnife.searchConvert(keywordsLG3);
    
    String nameLG4 = multi.getParameter("catNameLG4"),
        keywordsLG4 = multi.getParameter("keywordsLG4"),
        catDescrLG4 = multi.getParameter("catDescrLG4"),
        nameLG5 = multi.getParameter("catNameLG5"),
        keywordsLG5 = multi.getParameter("keywordsLG5"),
        catDescrLG5 = multi.getParameter("catDescrLG5"),
        nameLG6 = multi.getParameter("catNameLG6"),
        keywordsLG6 = multi.getParameter("keywordsLG6"),
        catDescrLG6 = multi.getParameter("catDescrLG6"),
        nameLG7 = multi.getParameter("catNameLG7"),
        keywordsLG7 = multi.getParameter("keywordsLG7"),
        catDescrLG7 = multi.getParameter("catDescrLG7");
    
    String nameUpLG4 = SwissKnife.searchConvert(nameLG4),
        keywordsUpLG4 = SwissKnife.searchConvert(keywordsLG4),
        nameUpLG5 = SwissKnife.searchConvert(nameLG5),
        keywordsUpLG5 = SwissKnife.searchConvert(keywordsLG5),
        nameUpLG6 = SwissKnife.searchConvert(nameLG6),
        keywordsUpLG6 = SwissKnife.searchConvert(keywordsLG6),
        nameUpLG7 = SwissKnife.searchConvert(nameLG7),
        keywordsUpLG7 = SwissKnife.searchConvert(keywordsLG7);
    
    if (catId.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
                                                       
    String[] imageOld = new String[]{catImgName1,catImgName2},
             image = new String[]{catImgName1,catImgName2},
             imageUpload = new String[]{"",""};
    
    int j = -1, prevTransIsolation = 0;
    
    Enumeration myFiles = multi.getFileNames();
    
    while(myFiles.hasMoreElements()) {
      imageUpload[++j] = myFiles.nextElement().toString();
    }
    
    MultiRequest.sort(imageUpload);
    
    for (int k=0; k<imageUpload.length; k++) {
      dbRet = MultiRequest.updateFile(image[k],multi,imageUpload[k],denyFilesExtTokenizer, uploadPath);
      
      if (dbRet.getNoError() == 1) {
        image[k] = dbRet.getRetStr();
      }
      else break;
    }
    
    Database database = null;
    
    PreparedStatement ps = null;
   
    database = bean.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    
    prevTransIsolation = dbRet.getRetInt();
    
    String query = "UPDATE prdCategory SET"
        + " catName = ?"
        + ",catNameUp = ?"
        + ",catNameLG = ?"
        + ",catNameUpLG = ?"
        + ",catNameLG1 = ?"
        + ",catNameUpLG1 = ?"
        + ",catNameLG2 = ?"
        + ",catNameUpLG2 = ?"
        + ",catNameLG3 = ?"
        + ",catNameUpLG3 = ?"
        + ",keywords = ?"
        + ",keywordsUp = ?"
        + ",keywordsLG = ?"
        + ",keywordsUpLG = ?"
        + ",keywordsLG1 = ?"
        + ",keywordsUpLG1 = ?"
        + ",keywordsLG2 = ?"
        + ",keywordsUpLG2 = ?"
        + ",keywordsLG3 = ?"
        + ",keywordsUpLG3 = ?"
        + ",catShowFlag = ?"
        + ",catParentFlag = ?"
        + ",catRank = ?"
        + ",catDescr = ?"
        + ",catDescrLG = ?"
        + ",catDescrLG1 = ?"
        + ",catDescrLG2 = ?"
        + ",catDescrLG3 = ?"
        + ",catImgName1 = ?"
        + ",catImgName2 = ?"
        + ",catCustomerType = ?";
    
    if (nameLG4 != null) {
      query += ",catNameLG4 = ?, catNameUpLG4 = ?, keywordsLG4 = ?, keywordsUpLG4 = ?, catDescrLG4 = ?";
    }
    if (nameLG5 != null) {
      query += ",catNameLG5 = ?, catNameUpLG5 = ?, keywordsLG5 = ?, keywordsUpLG5 = ?, catDescrLG5 = ?";
    }
    if (nameLG6 != null) {
      query += ",catNameLG6 = ?, catNameUpLG6 = ?, keywordsLG6 = ?, keywordsUpLG6 = ?, catDescrLG6 = ?";
    }
    if (nameLG7 != null) {
      query += ",catNameLG7 = ?, catNameUpLG7 = ?, keywordsLG7 = ?, keywordsUpLG7 = ?, catDescrLG7 = ?";
    }
    
    query += " WHERE catId = ?";

    int colIndex = 0;
    
    if (dbRet.getNoError() == 1) {
      try {
        ps = database.createPreparedStatement(query);
      
        ps.setString(1, name);
        ps.setString(2, nameUp);
        ps.setString(3, nameLG);
        ps.setString(4, nameUpLG);
        ps.setString(5, nameLG1);
        ps.setString(6, nameUpLG1);
        ps.setString(7, nameLG2);
        ps.setString(8, nameUpLG2);
        ps.setString(9, nameLG3);
        ps.setString(10, nameUpLG3);
        
        ps.setString(11, keywords);
        ps.setString(12, keywordsUp);
        ps.setString(13, keywordsLG);
        ps.setString(14, keywordsUpLG);
        ps.setString(15, keywordsLG1);
        ps.setString(16, keywordsUpLG1);
        ps.setString(17, keywordsLG2);
        ps.setString(18, keywordsUpLG2);
        ps.setString(19, keywordsLG3);
        ps.setString(20, keywordsUpLG3);
        ps.setString(21, catShowFlag);
        
        ps.setString(22, catParentFlag);
        ps.setInt(23, catRank);
        ps.setString(24, catDescr);
        ps.setString(25, catDescrLG);
        ps.setString(26, catDescrLG1);
        ps.setString(27, catDescrLG2);
        ps.setString(28, catDescrLG3);
        ps.setString(29, image[0]);
        ps.setString(30, image[1]);
        ps.setString(31, catCustomerType);
        
        colIndex = 31;
        
        if (nameLG4 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameUpLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(keywordsLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(keywordsUpLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(catDescrLG4));
        }
        if (nameLG5 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameUpLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(keywordsLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(keywordsUpLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(catDescrLG5));
        }
        if (nameLG6 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameUpLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(keywordsLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(keywordsUpLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(catDescrLG6));
        }
        if (nameLG7 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameUpLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(keywordsLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(keywordsUpLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(catDescrLG7));
        }
        
        ps.setString(++colIndex, catId);
        
        ps.executeUpdate();
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
      finally {
        try { if (ps != null) ps.close(); } catch (Exception e) { }
      }
    }
     
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    bean.freeDBConnection(databaseId,database);
    
    if (dbRet.getNoError() == 1) {
      MultiRequest.deleteUpdatedFiles(uploadPath,imageOld,image);
    }
    else {
      MultiRequest.deleteUpdatedFiles(uploadPath,image,imageOld);
    }
    
    return dbRet;
  }
  
  private DbRet doDelete(HttpServletRequest request,String databaseId,MultiRequest multi,String uploadPath) {
    DbRet dbRet = new DbRet();
    
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = bean.auth(databaseId,authUsername,authPassword,"prdCategory",Director.AUTH_DELETE);

     if (auth != Director.AUTH_OK) {
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    String catId = SwissKnife.sqlEncode(multi.getParameter("catId")),
           catImgName1 = SwissKnife.sqlEncode(multi.getParameter("img01")),
           catImgName2 = SwissKnife.sqlEncode(multi.getParameter("img02"));
    
    if (catId.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = null;
   
    database = bean.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    
    int prevTransIsolation = dbRet.getRetInt();

    String query = "DELETE FROM prdCategory WHERE catId = '" + catId + "'";

    dbRet = database.execQuery(query);

    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    bean.freeDBConnection(databaseId,database);
    
    File file = null;
    
    if (catImgName1.length()>0 && dbRet.getNoError() == 1) {
      file = new File(uploadPath, catImgName1);
             
      if (file.exists()) file.delete();
    }
     
    if (catImgName2.length()>0 && dbRet.getNoError() == 1) {
      file = new File(uploadPath, catImgName2);
             
      if (file.exists()) file.delete();
    }
    
    return dbRet;
  }
  
  private DbRet doDeleteImg(HttpServletRequest request,String databaseId,MultiRequest multi,String uploadPath) {
    DbRet dbRet = new DbRet();
       
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = bean.auth(databaseId,authUsername,authPassword,"prdCategory",Director.AUTH_DELETE);

     if (auth != Director.AUTH_OK) {
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    String catId = SwissKnife.sqlEncode(multi.getParameter("catId")),
           flag = SwissKnife.sqlEncode(multi.getParameter("flag")),
           catImgName = "";
    
    if (flag.equals("1")) {
      catImgName = SwissKnife.sqlEncode(multi.getParameter("img01"));
    }
    else if (flag.equals("2")) {
      catImgName = SwissKnife.sqlEncode(multi.getParameter("img02"));
    }
    
    boolean delFile = false;
    
    if (catId.length() == 0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }

    if (catImgName.length()>0) {
      File file = new File(uploadPath,catImgName);
           
      if (file.exists()) delFile= file.delete();

      if (!delFile) dbRet.setNoError(0);
    }
     
    Database database = null;
    database = bean.getDBConnection(databaseId);
        
    String query = "";
    
    if (flag.equals("1")) {
      query = "UPDATE prdCategory SET" 
            + " catImgName1 = ''" 
            + " WHERE catId = '" + catId + "'";
    }
    else if (flag.equals("2")) {
      query = "UPDATE prdCategory SET" 
            + " catImgName2 = ''" 
            + " WHERE catId = '" + catId + "'";
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(query);
    }
    
    bean.freeDBConnection(databaseId,database);
    
    return dbRet;
  }
}