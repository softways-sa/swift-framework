package gr.softways.dev.eshop.news.servlets;

import java.io.*;
import java.util.*;
import java.math.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class doAction extends HttpServlet {

  private Director bean;

  private String _charset = null;
  
  private String _uploadPropertiesFilename = null;
  
  Properties parameters = new Properties();
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    _uploadPropertiesFilename = SwissKnife.jndiLookup("upload/properties");
    
    bean = Director.getInstance();
  }
    
  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
              throws ServletException, IOException {
    DbRet dbRet = new DbRet();
    
    MultiRequest multi = null;
        
    String uploadPath = null, 
           tmpDeny = null;
    
    StrTokenizer denyFilesExtTokenizer = null;
    
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    try {
      parameters.load( new FileInputStream( _uploadPropertiesFilename ) );
             
      String tmpPath = parameters.getProperty("uploadTransientPath", "/");

      tmpDeny = parameters.getProperty("uploadDenyFilesExt");

      if (tmpDeny != null && tmpDeny.length()>0) {
       denyFilesExtTokenizer = new StrTokenizer(tmpDeny, '|');
      }
      
      multi = new MultiRequest(request, tmpPath, 14 * 1024 * 1024, _charset);
    }
    catch (Exception e) {
      e.printStackTrace();
      dbRet.setNoError(0);
    }
            
    String action = multi.getParameter("action1") == null ? "" : multi.getParameter("action1"),
           databaseId = multi.getParameter("databaseId") == null ? "" : multi.getParameter("databaseId").trim(),
           urlSuccess = multi.getParameter("urlSuccess") == null ? "" : multi.getParameter("urlSuccess"),
           urlFailure = multi.getParameter("urlFailure") == null ? "" : multi.getParameter("urlFailure"),
           urlNoAccess = multi.getParameter("urlNoAccess") == null ? "" : multi.getParameter("urlNoAccess");
           
    uploadPath = multi.getParameter("uploadPath");
         
    if (databaseId.equals(""))
      dbRet.setNoError(0);
    else if (action.equals("INSERT"))
      dbRet = doInsert(request, databaseId, multi, denyFilesExtTokenizer, uploadPath);
    else if (action.equals("UPDATE"))
      dbRet = doUpdate(request, databaseId, multi, denyFilesExtTokenizer, uploadPath);
    else if (action.equals("DELETE")){
      dbRet = doDelete(request, databaseId, multi, uploadPath);
    }
    else if (action.equals("DELETE_IMG")){
      dbRet = doDeleteImg(request, databaseId, multi, uploadPath);
    }
    
    if (dbRet.getNoError() == 0) {
      if (dbRet.getAuthError() == 1)
        response.sendRedirect(urlFailure + "?authError=" + dbRet.getAuthErrorCode());
      else
        response.sendRedirect(urlFailure);
    }
    else {
      response.sendRedirect(urlSuccess);
    }
  }

  private DbRet doInsert(HttpServletRequest request, String databaseId, 
                         MultiRequest multi, StrTokenizer denyFilesExtTokenizer,
                         String uploadPath) {
    DbRet dbRet = new DbRet();
    
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "newsTab", Director.AUTH_INSERT);

     if (auth != Director.AUTH_OK) {
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      dbRet.setNoError(0);
      
      return dbRet;
    }

    Database database = null;        
    
    String newsTitle = SwissKnife.sqlEncode(  multi.getParameter("newsTitle") ),
           newsTitleLG = SwissKnife.sqlEncode( multi.getParameter("newsTitleLG") ),
           newsSummary = SwissKnife.sqlEncode( multi.getParameter("newsSummary") ),
           newsSummaryLG = SwissKnife.sqlEncode( multi.getParameter("newsSummaryLG") ),
           newsFile = SwissKnife.sqlEncode( multi.getParameter("newsFile") ).trim(),
           newsFileLG = SwissKnife.sqlEncode( multi.getParameter("newsFileLG") ).trim(),
           newsText = SwissKnife.sqlEncode( multi.getParameter("newsText") ),
           newsTextLG = SwissKnife.sqlEncode( multi.getParameter("newsTextLG") ),
           newsHasFile = SwissKnife.sqlEncode( multi.getParameter("newsHasFile") ).trim(),
           newsType = SwissKnife.sqlEncode( multi.getParameter("newsType") ).trim(),
           newsTypeLG = SwissKnife.sqlEncode( multi.getParameter("newsTypeLG") ).trim() ,
           newsUrl = SwissKnife.sqlEncode( multi.getParameter("newsUrl") ).trim(),
           newsImg3 = SwissKnife.sqlEncode( multi.getParameter("newsImg3") ).trim(),
           newsImg4 = SwissKnife.sqlEncode( multi.getParameter("newsImg4") ).trim();

    String newsCode = "";

    String newsTitleUp = SwissKnife.searchConvert(newsTitle),
           newsTitleUpLG = SwissKnife.searchConvert(newsTitleLG);
	   
    Timestamp newsDay = 
        SwissKnife.buildTimestamp(multi.getParameter("newsDayDate"),
                                  multi.getParameter("newsDayMonth"),
                                  multi.getParameter("newsDayYear"));
    
    String[] image = new String[]{"",""},
	     imageUpload = new String[]{"",""};
    
    int j = -1, length = 0, prevTransIsolation = 0, retries = 0;
    
    PreparedStatement ps = null;
    
    Enumeration myFiles = multi.getFileNames();
    
    database = bean.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    
    prevTransIsolation = dbRet.getRetInt();
    
    while(myFiles.hasMoreElements()) {
      imageUpload[++j] = myFiles.nextElement().toString();     
    }
    
    sort(imageUpload);
    
    length = imageUpload.length;
    
    for (int i = 0; i < length; i++){
      if (i == 0) 
	dbRet = insertImage("",multi,imageUpload[i],denyFilesExtTokenizer, 
                            uploadPath);
      else
	dbRet = insertImage(image[i-1],multi,imageUpload[i], denyFilesExtTokenizer,
                            uploadPath);
      
      if(dbRet.getNoError() == 1){	
	image[i] = dbRet.getRetStr();
      }    
      else {	
	break;
      }	
    }                            

    String query = "INSERT INTO newsTab (newsCode,newsDay,newsTitle,newsTitleLG"
		    + ", newsTitleUp,newsTitleUpLG,newsSummary,newsSummaryLG"
                    + ",newsText,newsTextLG,newsFile,newsFileLG,newsType"
		    + ",newsTypeLG,newsHasFile,newsUrl,newsImg,newsImg2,newsImg3"
		    + ", newsImg4) VALUES ("
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
   
   if (dbRet.getNoError() == 1) {
     try{
	ps = database.createPreparedStatement(query);
      }
      catch (Exception e) {
	e.printStackTrace();
	dbRet.setNoError(0);
      }

      for (dbRet.setRetry(1); dbRet.getRetry() == 1 && retries < 10 ; retries++) {
	newsCode = SwissKnife.buildPK().trim();

	try {
	  ps.setString(1, newsCode);
          
          if (newsDay == null) {
            ps.setNull(2, Types.TIMESTAMP);
          }
          else {
            ps.setTimestamp(2, newsDay);
          }
          
	  ps.setString(3, newsTitle);
	  ps.setString(4, newsTitleLG);
	  ps.setString(5, newsTitleUp);
	  ps.setString(6, newsTitleUpLG);
	  ps.setString(7, newsSummary);
	  ps.setString(8, newsSummaryLG);
	  ps.setString(9, newsText);
	  ps.setString(10, newsTextLG);
	  ps.setString(11, newsFile);
	  ps.setString(12, newsFileLG);
	  ps.setString(13, newsType);
	  ps.setString(14, newsTypeLG);
	  ps.setString(15, newsHasFile);
	  ps.setString(16, newsUrl);
	  ps.setString(17, image[0]);
	  ps.setString(18, image[1]);
	  ps.setString(19, newsImg3);
	  ps.setString(20, newsImg4);  

	  ps.executeUpdate();

	  dbRet.setRetry(0);
	  dbRet.setNoError(1);
	}
	catch (Exception e) {
	  dbRet.setNoError(0);
	  dbRet.setRetry(1);

          e.printStackTrace();
	}        
      }

      try {
	ps.close();

	ps = null;
      }
      catch (Exception e) {
        dbRet.setNoError(0);
	e.printStackTrace();
      }  
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    bean.freeDBConnection(databaseId,database);
    
    if (dbRet.getNoError() == 0){
      deleteImages(uploadPath,image);
    }  	
    
    return dbRet;
  }
  
  private DbRet insertImage(String name1,MultiRequest multi, String fname,
                            StrTokenizer denyFilesExtTokenizer, 
                            String uploadPath) {
    DbRet dbRet = new DbRet();
    String name = "", docType = "", ext = "", oldFilename = "", g = "";
    
    File oldF = null;
    boolean renamef = false;
    
    try{           
      oldFilename = multi.getFilesystemName(fname);

      if (oldFilename != null) {
             
        oldF = multi.getFile(fname);      
      
        g = oldF.getName();                      
      
        if (g.lastIndexOf(".")!=-1) {
          ext = g.substring(g.lastIndexOf("."),g.length());
          docType = ext.substring(1,ext.length());
        }
        else
          docType="generic";
      
        if (denyFilesExtTokenizer != null) {
          while (denyFilesExtTokenizer.hasMoreTokens()) {
            if ( docType.equals( denyFilesExtTokenizer.nextToken() ) ) {
              oldF.delete();
              throw new Exception(docType + " extension is forbidden...");
            }
          }
        }
      
        name = SwissKnife.buildPK().trim() + ext;
        while(!name.equals("") &&  name1.equals(name)){ 
           name = SwissKnife.buildPK().trim() + ext;
        }  
          
        File nf = new File(uploadPath, name);
       
        if (nf.exists())
          nf.delete();            
      
        renamef = oldF.renameTo(nf);
          
          if(!renamef){
            dbRet.setNoError(0);
            oldF.delete();
          }
      }
      dbRet.setRetStr(name);
   }  
   catch (Exception e) {
     e.printStackTrace();     
     dbRet.setNoError(0);
   }   
   
    return dbRet;
  }
  
  
  private DbRet doUpdate(HttpServletRequest request, String databaseId, 
                         MultiRequest multi, StrTokenizer denyFilesExtTokenizer,
                         String uploadPath) {
    DbRet dbRet = new DbRet();
    
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "newsTab", Director.AUTH_UPDATE);

     if (auth != Director.AUTH_OK) {
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      dbRet.setNoError(0);
      
      return dbRet;
    }

    Database database = null;
    
    String newsCode = SwissKnife.sqlEncode( multi.getParameter("newsCode") ).trim(),
           newsTitle = SwissKnife.sqlEncode( multi.getParameter("newsTitle") ).trim(),
           newsTitleLG = SwissKnife.sqlEncode( multi.getParameter("newsTitleLG") ),
           newsSummary = SwissKnife.sqlEncode( multi.getParameter("newsSummary") ),
           newsSummaryLG = SwissKnife.sqlEncode( multi.getParameter("newsSummaryLG") ),
           newsFile = SwissKnife.sqlEncode( multi.getParameter("newsFile") ).trim(),
           newsFileLG = SwissKnife.sqlEncode( multi.getParameter("newsFileLG") ).trim(),
           newsText = SwissKnife.sqlEncode( multi.getParameter("newsText") ),
           newsTextLG = SwissKnife.sqlEncode( multi.getParameter("newsTextLG") ),
           newsHasFile = SwissKnife.sqlEncode( multi.getParameter("newsHasFile") ).trim(),
           newsType = SwissKnife.sqlEncode( multi.getParameter("newsType") ).trim(),
           newsTypeLG = SwissKnife.sqlEncode( multi.getParameter("newsTypeLG") ).trim(),
           newsUrl = SwissKnife.sqlEncode( multi.getParameter("newsUrl") ).trim(),
           newsUrlLG = SwissKnife.sqlEncode(  multi.getParameter("newsUrlLG") ).trim(),
           newsImg = SwissKnife.sqlEncode( multi.getParameter("newsImg") ).trim(),
           newsImg2 = SwissKnife.sqlEncode( multi.getParameter("newsImg2") ).trim(),
           newsImg3 = SwissKnife.sqlEncode( multi.getParameter("newsImg3") ).trim(),
           newsImg4 = SwissKnife.sqlEncode( multi.getParameter("newsImg4") ).trim();

    String newsTitleUp = SwissKnife.searchConvert(newsTitle),
           newsTitleUpLG = SwissKnife.searchConvert(newsTitleLG);

   Timestamp newsDay = 
        SwissKnife.buildTimestamp(multi.getParameter("newsDayDate"),
                                  multi.getParameter("newsDayMonth"),
                                  multi.getParameter("newsDayYear"));
       
    if (newsCode.length()==0) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
   
    String[] imageOld = new String[]{newsImg,newsImg2},
	     image = new String[]{newsImg,newsImg2},
             imageUpload = new String[]{"",""};
    
    int prevTransIsolation = 0, j = -1;    
    
    Enumeration myFiles = multi.getFileNames();
    
    PreparedStatement ps = null;
    
    database = bean.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    
    prevTransIsolation = dbRet.getRetInt();
    
    while(myFiles.hasMoreElements()) {
      imageUpload[++j] = myFiles.nextElement().toString();
    }
    
    sort(imageUpload);
    
    for (int k = 0; k < imageUpload.length; k++) {

      if (k == 0){
	dbRet = updateImage("",image[k],multi,imageUpload[k], 
                            denyFilesExtTokenizer, uploadPath);
      }
      else {
	dbRet = updateImage(image[k-1],image[k],multi,imageUpload[k], 
                            denyFilesExtTokenizer, uploadPath);
      }
      
      if(dbRet.getNoError() == 1){
        image[k] = dbRet.getRetStr();
      }   
      else {
	break;
      }
    }         
       
    String query = "UPDATE newsTab SET "
                  + " newsDay = ?"
                  + ",newsTitle = ?"
                  + ",newsTitleLG = ?"
                  + ",newsTitleUp = ?"
                  + ",newsTitleUpLG = ?"
                  + ",newsSummary = ?"
                  + ",newsSummaryLG = ?"
                  + ",newsFile = ?"
                  + ",newsFileLG = ?"
                  + ",newsText = ?"
                  + ",newsTextLG = ?"
                  + ",newsType = ?"
                  + ",newsTypeLG = ?"
                  + ",newsHasFile = ?"
                  + ",newsUrl = ?"
                  + ",newsImg = ?"
                  + ",newsImg2 = ?"
                  + ",newsImg3 = ?"
                  + ",newsImg4 = ?"
                  + " WHERE newsCode = ?";
		 
    if (dbRet.getNoError() == 1) {
      try {
	ps = database.createPreparedStatement(query);
	
        if (newsDay == null) {
          ps.setNull(1, Types.TIMESTAMP);
        }
        else {
          ps.setTimestamp(1, newsDay);
        }        
	ps.setString(2, newsTitle);
	ps.setString(3, newsTitleLG);
	ps.setString(4, newsTitleUp);
	ps.setString(5, newsTitleUpLG);
	ps.setString(6, newsSummary);
	ps.setString(7, newsSummaryLG);      
	ps.setString(8, newsFile);
	ps.setString(9, newsFileLG);
	ps.setString(10, newsText);
	ps.setString(11, newsTextLG);
	ps.setString(12, newsType);
	ps.setString(13, newsTypeLG);
	ps.setString(14, newsHasFile);
	ps.setString(15, newsUrl);
	ps.setString(16, image[0]);
	ps.setString(17, image[1]);
	ps.setString(18, newsImg3);
	ps.setString(19, newsImg4);      
	ps.setString(20, newsCode);

	ps.executeUpdate();     
      }
      catch (Exception e) {
        dbRet.setNoError(0);
	e.printStackTrace();
      }
      finally {
        if (ps != null) {
          try { ps.close(); } catch (Exception e) { }
        }
      }
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    bean.freeDBConnection(databaseId,database);
    
    if (dbRet.getNoError() == 1){
      deleteImages(uploadPath,imageOld,image);
    }
    else {
      deleteImages(uploadPath,image,imageOld);
    }
    
    return dbRet;
  }

  private DbRet doDelete(HttpServletRequest request, String databaseId, 
                         MultiRequest multi, String uploadPath) {

    DbRet dbRet = new DbRet();

    int prevTransIsolation = 0;

    String authUsername = SwissKnife.getSessionAttr(databaseId +  ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "newsTab", Director.AUTH_DELETE);

     if (auth != Director.AUTH_OK) {
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      dbRet.setNoError(0);
      
      return dbRet;
    }

    String newsCode = SwissKnife.sqlEncode( multi.getParameter("newsCode") ),
	   newsImg = SwissKnife.sqlEncode( multi.getParameter("newsImg") ),
           newsImg2 = SwissKnife.sqlEncode( multi.getParameter("newsImg2") );
    
    if (newsCode.length()==0) {
      dbRet.setNoError(0);
      return dbRet;
    }

    Database database = bean.getDBConnection(databaseId);

    String query = "DELETE FROM newsTab" 
                 + " WHERE newsCode = '" + newsCode + "'";

    if (dbRet.getNoError() == 1)
      dbRet = database.execQuery(query);

    bean.freeDBConnection(databaseId,database);
    
    File file = null;
    
    if (!newsImg.equals("") && dbRet.getNoError() == 1 ) {
      file = new File(uploadPath,newsImg);
             
      if (file.exists()) {
        file.delete();
      }
    }
     
    if (!newsImg2.equals("")) {  
      file = new File(uploadPath,newsImg2);

      if (file.exists()){
        file.delete();            
      }
    }
    
    file = null;
      
    return dbRet;
  }  
  
   private DbRet doDeleteImg(HttpServletRequest request, String databaseId, 
                             MultiRequest multi, String uploadPath) {
    
    DbRet dbRet = new DbRet();
       
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", 
                                                    request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", 
                                                    request);

    int auth = bean.auth(databaseId, authUsername, authPassword,
                         "newsTab", Director.AUTH_DELETE);

     if (auth != Director.AUTH_OK) {
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    String newsImg = "";
    String newsCode = SwissKnife.sqlEncode( multi.getParameter("newsCode") ),
           flag = SwissKnife.sqlEncode( multi.getParameter("flag") );
    
    if (flag.equals("1")){
      newsImg =  SwissKnife.sqlEncode( multi.getParameter("newsImg") );
    } 
    else if (flag.equals("2")){
      newsImg = SwissKnife.sqlEncode( multi.getParameter("newsImg2") );
    }  
    
    boolean delFile = false;
    
    if ( newsCode.equals("") ) {
      dbRet.setNoError(0);
      return dbRet;
    }

    if (!newsImg.equals("")) {      
      File file = new File(uploadPath,newsImg);
           
      if (file.exists() ){
        delFile= file.delete();            
      }
      
      if (!delFile) dbRet.setNoError(0);
    }
     
    Database database = bean.getDBConnection(databaseId);        
    
    String query = "";
    
    if (flag.equals("1")) {
      query = "UPDATE newsTab SET " +
              " newsImg = ''" +   
              " WHERE newsCode = '" + newsCode + "'";
    }
    else if (flag.equals("2")) {
      query = "UPDATE newsTab SET " +
              " newsImg2 = '' " +   
              " WHERE newsCode = '" +  newsCode + "'";
    }   
        
    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(query);
    }
    
    bean.freeDBConnection(databaseId,database);               
    
    return dbRet;
  }
  
   
  private DbRet updateImage(String imageBefore,String PLImage, 
                            MultiRequest multi,String fname,
                            StrTokenizer denyFilesExtTokenizer,
                            String uploadPath){ 
    
    String name = "", docType = "", ext = "", oldFilename = "", g = "";
    boolean renamef = false;
    
    DbRet dbRet = new DbRet();
    
    try{
      oldFilename = multi.getFilesystemName(fname);                         
      
      if (oldFilename != null) {
              
        File oldF = multi.getFile(fname);      
      
        g = oldF.getName();                      
     
        if (g.lastIndexOf(".")!=-1) {
          ext = g.substring(g.lastIndexOf("."),g.length());
          docType  = ext.substring(1,ext.length());
        }
        else
          docType="generic";          
      
        if (denyFilesExtTokenizer != null) {
          while (denyFilesExtTokenizer.hasMoreTokens()) {
            if ( docType.equals( denyFilesExtTokenizer.nextToken() ) ) {
              oldF.delete();
              throw new Exception(docType + " extension is forbidden...");
            }
          }
        }                  
      
        name = SwissKnife.buildPK().trim() + ext;
        while(!name.equals("") && imageBefore.equals(name)){ 
           name = SwissKnife.buildPK().trim() + ext;
        }  
	
        File nf = new File(uploadPath, name);            
      
        if (nf.exists())
          nf.delete();            
      
        renamef = oldF.renameTo(nf);
        
        if(!renamef){
          dbRet.setNoError(0);
        }  
      }
      else{
        name = PLImage;
      }
      dbRet.setRetStr(name);
    }
    
    catch (Exception e) {
      e.printStackTrace();
      dbRet.setNoError(0);
    }
    
    return dbRet;
  }
  
  public void sort(String[] tableName) {
    int min, length = tableName.length;
    String tmp;
    
    for (int i = 0; i < length - 1; i ++){
      min = i;
      
      for (int pos = i + 1; pos < length; pos ++){
          if(! tableName[pos].trim().equals("") ){
            if (tableName[pos].compareTo(tableName[min]) < 0 || tableName[min].trim().equals("")){
              min = pos;
            }
          }  
      }
      tmp = tableName[min];
      tableName[min] = tableName[i];
      tableName[i] = tmp;
    
    }  
  }
  
  public void deleteImages(String uploadPath, String[] tableName) {
    File nf = null;
    int length = 0;
    
    length = tableName.length;
    
    for (int i = 0; i < length; i++) {
      if (!tableName[i].equals("")) {
	nf = new File(uploadPath, tableName[i]);
	if (nf.exists()) nf.delete();
      }  
    }    
  }
  
  public void deleteImages(String uploadPath, String[] tableName1, 
                           String[] tableName2) {
    File nf = null;
    int length = 0;
    
    length = tableName1.length;
    
    for (int i = 0; i < length; i++) {
      if (!tableName1[i].equals("") && !tableName1[i].equals(tableName2[i])) {
	nf = new File(uploadPath, tableName1[i]);
	if (nf.exists()) nf.delete();   
      }
    }
  }
}