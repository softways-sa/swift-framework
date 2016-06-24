package gr.softways.dev.eshop.product.v2.servlets;

import java.io.*;
import java.util.*;
import java.math.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class ProductServlet2_1 extends HttpServlet {
  
  private Director bean;

  private String _charset = null;
  
  private int _maxUploadSizeKB = 4 * 1024;
  
  private String _uploadPropertiesFilename = null;
  
  Properties parameters = new Properties();

  private BigDecimal _zero = new BigDecimal("0");
  
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
  
  private DbRet doInsert(HttpServletRequest request,String databaseId,MultiRequest multi,
                         StrTokenizer denyFilesExtTokenizer,String uploadPath) {
    DbRet dbRet = new DbRet();
    
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = bean.auth(databaseId,authUsername,authPassword,"product",Director.AUTH_INSERT);

     if (auth != Director.AUTH_OK) {
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      dbRet.setNoError(0);
      
      return dbRet;
    }

    String localeLanguage = multi.getParameter("localeLanguage"),
           localeCountry = multi.getParameter("localeCountry");
    
    String catId = "", prdLock = "0";
    
    BigDecimal stockQua = _zero,
               inQua = _zero, outQua = _zero,
               inVal = _zero, inValEU = _zero,
               inValCUR1 = _zero, inValCUR2 = _zero,
               outVal = _zero, outValEU = _zero,
               outValCUR1 = _zero, outValCUR2 = _zero;
    
    String prdId = SwissKnife.sqlEncode(multi.getParameter("prdId")),
           prdId2 = SwissKnife.sqlEncode(multi.getParameter("prdId2")),
           name = SwissKnife.sqlEncode(multi.getParameter("name")),
           nameLG = SwissKnife.sqlEncode(multi.getParameter("nameLG")),
           descr = SwissKnife.sqlEncode(multi.getParameter("descr")),
           descrLG = SwissKnife.sqlEncode(multi.getParameter("descrLG")),
           supplierId = SwissKnife.sqlEncode(multi.getParameter("supplierId")),
           prdManufactId = SwissKnife.sqlEncode(multi.getParameter("prdManufactId")),
           mUnit = SwissKnife.sqlEncode(multi.getParameter("mUnit")),
           mUnitLG = SwissKnife.sqlEncode(multi.getParameter("mUnitLG")),
           packageName = SwissKnife.sqlEncode(multi.getParameter("packageName")),
           packageNameLG = SwissKnife.sqlEncode(multi.getParameter("packageNameLG")),
           barcode = SwissKnife.sqlEncode(multi.getParameter("barcode")),
           prdHomePageLink = SwissKnife.sqlEncode(multi.getParameter("prdHomePageLink")),
           shippingKindZone = SwissKnife.sqlEncode(multi.getParameter("shippingKindZone")),
           prd_SCCode = SwissKnife.sqlEncode(multi.getParameter("prd_SCCode"));
          
    String prdImageCaption = SwissKnife.sqlEncode(multi.getParameter("prdImageCaption")),
           prdImageCaptionLG = SwissKnife.sqlEncode(multi.getParameter("prdImageCaptionLG"));
    
    String nameUp = SwissKnife.searchConvert(name),
           nameUpLG = SwissKnife.searchConvert(nameLG),
           mUnitUp = SwissKnife.searchConvert(mUnit),
           mUnitUpLG = SwissKnife.searchConvert(mUnitLG);
           
    // various flags
    String hdStockFlag = null, hdStockFlagW = null,
           hotdealFlag = null, hotdealFlagW = null,
           salesFlag = null, salesFlagW = null,
           prdHideFlag = null, prdHideFlagW = null,
           prdNewColl = null, 
           prdCompFlag = null,
           prdHasAttributes = null,
           prdStopSalesGlobal = null,
           prdStartSales = null;

    if ((hdStockFlag = multi.getParameter("hdStockFlag")) == null) hdStockFlag = "0";
    if ((hdStockFlagW = multi.getParameter("hdStockFlagW")) == null) hdStockFlagW = "0";
    if ((hotdealFlag = multi.getParameter("hotdealFlag")) == null) hotdealFlag = "0";
    if ((hotdealFlagW = multi.getParameter("hotdealFlagW")) == null) hotdealFlagW = "0";
    if ((salesFlag = multi.getParameter("salesFlag")) == null) salesFlag = "0";
    if ((salesFlagW = multi.getParameter("salesFlagW")) == null) salesFlagW = "0";
    if ((prdHideFlag = multi.getParameter("prdHideFlag")) == null) prdHideFlag = "0";
    if ((prdHideFlagW = multi.getParameter("prdHideFlagW")) == null) prdHideFlagW = "0";
    if ((prdNewColl = multi.getParameter("prdNewColl")) == null) prdNewColl = "0";
    if ((prdCompFlag = multi.getParameter("prdCompFlag")) == null) prdCompFlag = "0";
    if ((prdHasAttributes = multi.getParameter("prdHasAttributes")) == null) prdHasAttributes = "0";
    if ((prdStopSalesGlobal = multi.getParameter("prdStopSalesGlobal")) == null) prdStopSalesGlobal = "0";
    if ((prdStartSales = multi.getParameter("prdStartSales")) == null) prdStartSales = "0";
        
    BigDecimal vatPct = SwissKnife.parseBigDecimal(multi.getParameter("vatPct"), localeLanguage, localeCountry),
               wholesalePrc = SwissKnife.parseBigDecimal(multi.getParameter("wholesalePrc"), localeLanguage, localeCountry),
               wholesalePrcEU = SwissKnife.parseBigDecimal(multi.getParameter("wholesalePrcEU"), localeLanguage, localeCountry),
               wholesalePrcCUR1 = SwissKnife.parseBigDecimal(multi.getParameter("wholesalePrcCUR1"), localeLanguage, localeCountry),
               wholesalePrcCUR2 = SwissKnife.parseBigDecimal(multi.getParameter("wholesalePrcCUR2"), localeLanguage, localeCountry),
               retailPrc = SwissKnife.parseBigDecimal(multi.getParameter("retailPrc"), localeLanguage, localeCountry),
               retailPrcEU = SwissKnife.parseBigDecimal(multi.getParameter("retailPrcEU"), localeLanguage, localeCountry),
               retailPrcCUR1 = SwissKnife.parseBigDecimal(multi.getParameter("retailPrcCUR1"), localeLanguage, localeCountry),
               retailPrcCUR2 = SwissKnife.parseBigDecimal(multi.getParameter("retailPrcCUR2"), localeLanguage, localeCountry),
               slWholesalePrc = SwissKnife.parseBigDecimal(multi.getParameter("slWholesalePrc"), localeLanguage, localeCountry),
               slWholesalePrcEU = SwissKnife.parseBigDecimal(multi.getParameter("slWholesalePrcEU"), localeLanguage, localeCountry),
               slWholesalePrcCUR1 = SwissKnife.parseBigDecimal(multi.getParameter("slWholesalePrcCUR1"), localeLanguage, localeCountry),
               slWholesalePrcCUR2 = SwissKnife.parseBigDecimal(multi.getParameter("slWholesalePrcCUR2"), localeLanguage, localeCountry),
               slRetailPrc = SwissKnife.parseBigDecimal(multi.getParameter("slRetailPrc"), localeLanguage, localeCountry),
               slRetailPrcEU = SwissKnife.parseBigDecimal(multi.getParameter("slRetailPrcEU"), localeLanguage, localeCountry),
               slRetailPrcCUR1 = SwissKnife.parseBigDecimal(multi.getParameter("slRetailPrcCUR1"), localeLanguage, localeCountry),
               slRetailPrcCUR2 = SwissKnife.parseBigDecimal(multi.getParameter("slRetailPrcCUR2"), localeLanguage, localeCountry),
               hdWholesalePrc = SwissKnife.parseBigDecimal(multi.getParameter("hdWholesalePrc"), localeLanguage, localeCountry),
               hdWholesalePrcEU = SwissKnife.parseBigDecimal(multi.getParameter("hdWholesalePrcEU"), localeLanguage, localeCountry),
               hdWholesalePrcCUR1 = SwissKnife.parseBigDecimal(multi.getParameter("hdWholesalePrcCUR1"), localeLanguage, localeCountry),
               hdWholesalePrcCUR2 = SwissKnife.parseBigDecimal(multi.getParameter("hdWholesalePrcCUR2"), localeLanguage, localeCountry),
               hdRetailPrc = SwissKnife.parseBigDecimal(multi.getParameter("hdRetailPrc"), localeLanguage, localeCountry),
               hdRetailPrcEU = SwissKnife.parseBigDecimal(multi.getParameter("hdRetailPrcEU"), localeLanguage, localeCountry),
               hdRetailPrcCUR1 = SwissKnife.parseBigDecimal(multi.getParameter("hdRetailPrcCUR1"), localeLanguage, localeCountry),
               hdRetailPrcCUR2 = SwissKnife.parseBigDecimal(multi.getParameter("hdRetailPrcCUR2"), localeLanguage, localeCountry),
               giftPrc = SwissKnife.parseBigDecimal(multi.getParameter("giftPrc"), localeLanguage, localeCountry),
               giftPrcEU = SwissKnife.parseBigDecimal(multi.getParameter("giftPrcEU"), localeLanguage, localeCountry),
               giftPrcCUR1 = SwissKnife.parseBigDecimal(multi.getParameter("giftPrcCUR1"), localeLanguage, localeCountry),
               giftPrcCUR2 = SwissKnife.parseBigDecimal(multi.getParameter("giftPrcCUR2"), localeLanguage, localeCountry),
               weight = SwissKnife.parseBigDecimal(multi.getParameter("weight"), localeLanguage, localeCountry),
               cubemeter = SwissKnife.parseBigDecimal(multi.getParameter("cubemeter"), localeLanguage, localeCountry),
               minOrderQua = SwissKnife.parseBigDecimal(multi.getParameter("minOrderQua"), localeLanguage, localeCountry),
               maxOrderQua = SwissKnife.parseBigDecimal(multi.getParameter("maxOrderQua"), localeLanguage, localeCountry),
               safetyStockQua = SwissKnife.parseBigDecimal(multi.getParameter("safetyStockQua"), localeLanguage, localeCountry),
               shippingValue = SwissKnife.parseBigDecimal(multi.getParameter("shippingValue"), localeLanguage, localeCountry),
               shippingValueEU = SwissKnife.parseBigDecimal(multi.getParameter("shippingValueEU"), localeLanguage, localeCountry),
               shippingValueCUR1 = SwissKnife.parseBigDecimal(multi.getParameter("shippingValueCUR1"), localeLanguage, localeCountry),
               shippingValueCUR2 = SwissKnife.parseBigDecimal(multi.getParameter("shippingValueCUR2"), localeLanguage, localeCountry),
               shippingVatPct = SwissKnife.parseBigDecimal(multi.getParameter("shippingVatPct"), localeLanguage, localeCountry);
    
    if (vatPct == null) vatPct = _zero;
    if (wholesalePrc == null) wholesalePrc = _zero;
    if (wholesalePrcEU == null) wholesalePrcEU = _zero;
    if (wholesalePrcCUR1 == null) wholesalePrcCUR1 = _zero;
    if (wholesalePrcCUR2 == null) wholesalePrcCUR2 = _zero;
    if (retailPrc == null) retailPrc = _zero;
    if (retailPrcEU == null) retailPrcEU = _zero;
    if (retailPrcCUR1 == null) retailPrcCUR1 = _zero;
    if (retailPrcCUR2 == null) retailPrcCUR2 = _zero;
    if (slWholesalePrc == null) slWholesalePrc = _zero;
    if (slWholesalePrcEU == null) slWholesalePrcEU = _zero;
    if (slWholesalePrcCUR1 == null) slWholesalePrcCUR1 = _zero;
    if (slWholesalePrcCUR2 == null) slWholesalePrcCUR2 = _zero;
    if (slRetailPrc == null) slRetailPrc = _zero;
    if (slRetailPrcEU == null) slRetailPrcEU = _zero;
    if (slRetailPrcCUR1 == null) slRetailPrcCUR1 = _zero;
    if (slRetailPrcCUR2 == null) slRetailPrcCUR2 = _zero;
    if (hdWholesalePrc == null) hdWholesalePrc = _zero;
    if (hdWholesalePrcEU == null) hdWholesalePrcEU = _zero;
    if (hdWholesalePrcCUR1 == null) hdWholesalePrcCUR1 = _zero;
    if (hdWholesalePrcCUR2 == null) hdWholesalePrcCUR2 = _zero;
    if (hdRetailPrc == null) hdRetailPrc = _zero;
    if (hdRetailPrcEU == null) hdRetailPrcEU = _zero;
    if (hdRetailPrcCUR1 == null) hdRetailPrcCUR1 = _zero;
    if (hdRetailPrcCUR2 == null) hdRetailPrcCUR2 = _zero;
    if (giftPrc == null) giftPrc = _zero;
    if (giftPrcEU == null) giftPrcEU = _zero;
    if (giftPrcCUR1 == null) giftPrcCUR1 = _zero;
    if (giftPrcCUR2 == null) giftPrcCUR2 = _zero;
    if (weight == null) weight = _zero;
    if (cubemeter == null) cubemeter = _zero;
    if (minOrderQua == null) minOrderQua = _zero;
    if (maxOrderQua == null) maxOrderQua = _zero;
    if (safetyStockQua == null) safetyStockQua = _zero;
    if (shippingValue == null) shippingValue = _zero;
    if (shippingValueEU == null) shippingValueEU = _zero;
    if (shippingValueCUR1 == null) shippingValueCUR1 = _zero;
    if (shippingValueCUR2 == null) shippingValueCUR2 = _zero;
    if (shippingVatPct == null) shippingVatPct = _zero;

    String deliveryDays = null;
    if ((deliveryDays = multi.getParameter("deliveryDays")) == null) deliveryDays = "0";
    
    Timestamp hdBeginDate = SwissKnife.buildTimestamp(multi.getParameter("hdBeginDateDay"),
                                                      multi.getParameter("hdBeginDateMonth"),
                                                      multi.getParameter("hdBeginDateYear"));

    Timestamp hdEndDate = SwissKnife.buildTimestamp(multi.getParameter("hdEndDateDay"),
                                                    multi.getParameter("hdEndDateMonth"),
                                                    multi.getParameter("hdEndDateYear"));
    
    Timestamp hdBeginDateW = SwissKnife.buildTimestamp(multi.getParameter("hdBeginDateWDay"),
                                                       multi.getParameter("hdBeginDateWMonth"),
                                                       multi.getParameter("hdBeginDateWYear"));

    Timestamp hdEndDateW = SwissKnife.buildTimestamp(multi.getParameter("hdEndDateWDay"),
                                                     multi.getParameter("hdEndDateWMonth"),
                                                     multi.getParameter("hdEndDateWYear"));
    
    String[] image = new String[]{"","","",""},   
	     imageUpload = new String[]{"","","",""};
    
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

    String query = "INSERT INTO product (prdId,prdId2,name,nameUp,nameLG"
                 + ",nameUpLG,descr,descrLG,supplierId"
                 + ",vatPct,mUnit,mUnitLG,wholesalePrc,wholesalePrcEU"
                 + ",wholesalePrcCUR1,wholesalePrcCUR2"
                 + ",retailPrc,retailPrcEU"
                 + ",retailPrcCUR1,retailPrcCUR2"
                 + ",slWholesalePrc,slWholesalePrcEU"
                 + ",slWholesalePrcCUR1,slWholesalePrcCUR2"
                 + ",slRetailPrc,slRetailPrcEU"
                 + ",slRetailPrcCUR1,slRetailPrcCUR2"
                 + ",hdWholesalePrc,hdWholesalePrcEU"
                 + ",hdWholesalePrcCUR1,hdWholesalePrcCUR2"
                 + ",hdRetailPrc,hdRetailPrcEU"
                 + ",hdRetailPrcCUR1,hdRetailPrcCUR2"
                 + ",giftPrc,giftPrcEU"
                 + ",giftPrcCUR1,giftPrcCUR2"
                 + ",weight,cubemeter,minOrderQua"
                 + ",maxOrderQua,stockQua,safetyStockQua"
                 + ",package,packageLG,img"
                 + ",img2,img3,img4,barcode,deliveryDays,catId"
                 + ",hdStockFlag,hdStockFlagW"
                 + ",hotdealFlag,hotdealFlagW,salesFlag,salesFlagW"
                 + ",shippingKindZone,shippingValue,shippingValueEU"
                 + ",shippingValueCUR1,shippingValueCUR2,shippingVatPct"
                 + ",inQua,outQua"
                 + ",inVal,inValEU,inValCUR1,inValCUR2"
                 + ",outVal,outValEU,outValCUR1,outValCUR2"
                 + ",prdLock,prdManufactId,prdHideFlag,prdHideFlagW"
                 + ",prdNewColl,prdHomePageLink,prdCompFlag,prdHasAttributes"
                 + ",prdStopSalesGlobal,prdStartSales,hdBeginDate,hdEndDate"
                 + ",hdBeginDateW,hdEndDateW,mUnitUp,mUnitUpLG,prd_SCCode"
                 + ",prdImageCaption,prdImageCaptionLG"
                 + ") VALUES ("
                 + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?"
                 + ",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?"
                 + ",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?"
                 + ",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?"
                 + ",?,?,?,?,?,?,?,?,?,?,?,?,?,?"
                 + ",?,?"
                 + ")";
    
    if (dbRet.getNoError() == 1) {
      try {
        ps = database.createPreparedStatement(query);
    
        ps.setString(1, prdId);
        ps.setString(2, prdId2);
        ps.setString(3, name);
        ps.setString(4, nameUp);
        ps.setString(5, nameLG);
        ps.setString(6, nameUpLG);
        ps.setString(7, descr);
        ps.setString(8, descrLG);
        
        if (supplierId == null || supplierId.length() == 0) {
          ps.setNull(9, Types.CHAR);
        }
        else {
          ps.setString(9, supplierId);
        }
        
        ps.setBigDecimal(10, vatPct);
        ps.setString(11, mUnit);
        ps.setString(12, mUnitLG);
        ps.setBigDecimal(13, wholesalePrc);
        ps.setBigDecimal(14, wholesalePrcEU);
        ps.setBigDecimal(15, wholesalePrcCUR1);
        ps.setBigDecimal(16, wholesalePrcCUR2);
        ps.setBigDecimal(17, retailPrc);
        ps.setBigDecimal(18, retailPrcEU);
        ps.setBigDecimal(19, retailPrcCUR1);
        ps.setBigDecimal(20, retailPrcCUR2);
        ps.setBigDecimal(21, slWholesalePrc);
        ps.setBigDecimal(22, slWholesalePrcEU);
        ps.setBigDecimal(23, slWholesalePrcCUR1);
        ps.setBigDecimal(24, slWholesalePrcCUR2);
        ps.setBigDecimal(25, slRetailPrc);
        ps.setBigDecimal(26, slRetailPrcEU);
        ps.setBigDecimal(27, slRetailPrcCUR1);
        ps.setBigDecimal(28, slRetailPrcCUR2);
        ps.setBigDecimal(29, hdWholesalePrc);
        ps.setBigDecimal(30, hdWholesalePrcEU);
        ps.setBigDecimal(31, hdWholesalePrcCUR1);
        ps.setBigDecimal(32, hdWholesalePrcCUR2);
        ps.setBigDecimal(33, hdRetailPrc);
        ps.setBigDecimal(34, hdRetailPrcEU);
        ps.setBigDecimal(35, hdRetailPrcCUR1);
        ps.setBigDecimal(36, hdRetailPrcCUR2);
        ps.setBigDecimal(37, giftPrc);
        ps.setBigDecimal(38, giftPrcEU);
        ps.setBigDecimal(39, giftPrcCUR1);
        ps.setBigDecimal(40, giftPrcCUR2);
        ps.setBigDecimal(41, weight);
        ps.setBigDecimal(42, cubemeter);
        ps.setBigDecimal(43, minOrderQua);
        ps.setBigDecimal(44, maxOrderQua);
        ps.setBigDecimal(45, stockQua);
        ps.setBigDecimal(46, safetyStockQua);
        ps.setString(47, packageName);
        ps.setString(48, packageNameLG);
        ps.setString(49, image[0]);
        ps.setString(50, image[1]);
        ps.setString(51, image[2]);
        ps.setString(52, image[3]);
        ps.setString(53, barcode);
        ps.setInt(54, Integer.parseInt(deliveryDays));
        ps.setString(55, catId);
        ps.setString(56, hdStockFlag);
        ps.setString(57, hdStockFlagW);
        ps.setString(58, hotdealFlag);
        ps.setString(59, hotdealFlagW);
        ps.setString(60, salesFlag);
        ps.setString(61, salesFlagW);
        ps.setString(62, shippingKindZone);
        ps.setBigDecimal(63, shippingValue);
        ps.setBigDecimal(64, shippingValueEU);
        ps.setBigDecimal(65, shippingValueCUR1);
        ps.setBigDecimal(66, shippingValueCUR2);
        ps.setBigDecimal(67, shippingVatPct);
        ps.setBigDecimal(68, inQua);
        ps.setBigDecimal(69, outQua);
        ps.setBigDecimal(70, inVal);
        ps.setBigDecimal(71, inValEU);
        ps.setBigDecimal(72, inValCUR1);
        ps.setBigDecimal(73, inValCUR2);
        ps.setBigDecimal(74, outVal);
        ps.setBigDecimal(75, outValEU);
        ps.setBigDecimal(76, outValCUR1);
        ps.setBigDecimal(77, outValCUR2);
        ps.setString(78, prdLock);
        
        if (prdManufactId == null || prdManufactId.length() == 0) {
          ps.setNull(79, Types.CHAR);
        }
        else {
          ps.setString(79, prdManufactId);
        }
        
        ps.setString(80, prdHideFlag);
        ps.setString(81, prdHideFlagW);
        ps.setString(82, prdNewColl);
        ps.setString(83, prdHomePageLink);
        ps.setString(84, prdCompFlag);
        ps.setString(85, prdHasAttributes);
        ps.setString(86, prdStopSalesGlobal);
        ps.setString(87, prdStartSales);
        
        if (hdBeginDate == null) {
          ps.setNull(88, Types.TIMESTAMP);
        }
        else {
          ps.setTimestamp(88, hdBeginDate);
        }

        if (hdEndDate == null) {
          ps.setNull(89, Types.TIMESTAMP);
        }
        else {
          ps.setTimestamp(89, hdEndDate);
        }

        if (hdBeginDateW == null) {
          ps.setNull(90, Types.TIMESTAMP);
        }
        else {
          ps.setTimestamp(90, hdBeginDateW);
        }

        if (hdEndDateW == null) {
          ps.setNull(91, Types.TIMESTAMP);
        }
        else {
          ps.setTimestamp(91, hdEndDateW);
        }
        
        ps.setString(92, mUnitUp);
        ps.setString(93, mUnitUpLG);
        
        if (prd_SCCode == null || prd_SCCode.length() == 0) {
          ps.setNull(94, Types.VARCHAR);
        }
        else {
          ps.setString(94, prd_SCCode);
        }
        
        ps.setString(95, prdImageCaption);
        ps.setString(96, prdImageCaptionLG);

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

    /** stored procedures for product monthly */
    
    /**String procParams[] = new String[2], procParamDlm[] = new String[2];

    int year = SwissKnife.getTDateInt(SwissKnife.currentDate(),"year");
    procParams[0] = prdId;
    procParamDlm[0] = "'";
    procParams[1] = String.valueOf(year);
    procParamDlm[1] = "";
    if (dbRet.getNoError() == 1) {
      dbRet = database.execProcedure("newPrdMonthly", procParams, procParamDlm);
    }
    year++;
    procParams[0] = prdId;
    procParamDlm[0] = "'";
    procParams[1] = String.valueOf(year);
    procParamDlm[1] = "";
    
    if (dbRet.getNoError() == 1) {
      dbRet = database.execProcedure("newPrdMonthly", procParams, procParamDlm);
    } **/
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    bean.freeDBConnection(databaseId,database);
    
    if (dbRet.getNoError() == 0) {
      MultiRequest.deleteFiles(uploadPath, image);
    }

    return dbRet;
  }

  private DbRet doUpdate(HttpServletRequest request,String databaseId,MultiRequest multi,
                         StrTokenizer denyFilesExtTokenizer,String uploadPath) {
    DbRet dbRet = new DbRet();
    
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = bean.auth(databaseId,authUsername,authPassword,"product",Director.AUTH_UPDATE);

     if (auth != Director.AUTH_OK) {
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      dbRet.setNoError(0);
      
      return dbRet;
    }

    String localeLanguage = multi.getParameter("localeLanguage"),
           localeCountry = multi.getParameter("localeCountry");
    
    String catId = "", prdLock = "0";
    
    String prdId = SwissKnife.sqlEncode(multi.getParameter("prdId")),
           prdId2 = SwissKnife.sqlEncode(multi.getParameter("prdId2")),
           name = SwissKnife.sqlEncode(multi.getParameter("name")),
           nameLG = SwissKnife.sqlEncode(multi.getParameter("nameLG")),
           descr = SwissKnife.sqlEncode(multi.getParameter("descr")),
           descrLG = SwissKnife.sqlEncode(multi.getParameter("descrLG")),
           supplierId = SwissKnife.sqlEncode(multi.getParameter("supplierId")),
           prdManufactId = SwissKnife.sqlEncode(multi.getParameter("prdManufactId")),
           mUnit = SwissKnife.sqlEncode(multi.getParameter("mUnit")),
           mUnitLG = SwissKnife.sqlEncode(multi.getParameter("mUnitLG")),
           packageName = SwissKnife.sqlEncode(multi.getParameter("packageName")),
           packageNameLG = SwissKnife.sqlEncode(multi.getParameter("packageNameLG")),
           barcode = SwissKnife.sqlEncode(multi.getParameter("barcode")),
           prdHomePageLink = SwissKnife.sqlEncode(multi.getParameter("prdHomePageLink")),
           shippingKindZone = SwissKnife.sqlEncode(multi.getParameter("shippingKindZone")),
           img = SwissKnife.sqlEncode(multi.getParameter("img01")),
           img2 = SwissKnife.sqlEncode(multi.getParameter("img02")),
           img3 = SwissKnife.sqlEncode(multi.getParameter("img03")),
           img4 = SwissKnife.sqlEncode(multi.getParameter("img04")),
           prd_SCCode = SwissKnife.sqlEncode(multi.getParameter("prd_SCCode"));

    String prdImageCaption = SwissKnife.sqlEncode(multi.getParameter("prdImageCaption")),
           prdImageCaptionLG = SwissKnife.sqlEncode(multi.getParameter("prdImageCaptionLG"));
           
    String nameUp = SwissKnife.searchConvert(name),
           nameUpLG = SwissKnife.searchConvert(nameLG),
           mUnitUp = SwissKnife.searchConvert(mUnit),
           mUnitUpLG = SwissKnife.searchConvert(mUnitLG);
    
    // various flags
    String hdStockFlag = null, hdStockFlagW = null,
           hotdealFlag = null, hotdealFlagW = null,
           salesFlag = null, salesFlagW = null,
           prdHideFlag = null, prdHideFlagW = null,
           prdNewColl = null, 
           prdCompFlag = null,
           prdHasAttributes = null,
           prdStopSalesGlobal = null,
           prdStartSales = null;
    
    if ((hdStockFlag = multi.getParameter("hdStockFlag")) == null) hdStockFlag = "0";
    if ((hdStockFlagW = multi.getParameter("hdStockFlagW")) == null) hdStockFlagW = "0";
    if ((hotdealFlag = multi.getParameter("hotdealFlag")) == null) hotdealFlag = "0";
    if ((hotdealFlagW = multi.getParameter("hotdealFlagW")) == null) hotdealFlagW = "0";
    if ((salesFlag = multi.getParameter("salesFlag")) == null) salesFlag = "0";
    if ((salesFlagW = multi.getParameter("salesFlagW")) == null) salesFlagW = "0";
    if ((prdHideFlag = multi.getParameter("prdHideFlag")) == null) prdHideFlag = "0";
    if ((prdHideFlagW = multi.getParameter("prdHideFlagW")) == null) prdHideFlagW = "0";
    if ((prdNewColl = multi.getParameter("prdNewColl")) == null) prdNewColl = "0";
    if ((prdCompFlag = multi.getParameter("prdCompFlag")) == null) prdCompFlag = "0";
    if ((prdHasAttributes = multi.getParameter("prdHasAttributes")) == null) prdHasAttributes = "0";
    if ((prdStopSalesGlobal = multi.getParameter("prdStopSalesGlobal")) == null) prdStopSalesGlobal = "0";
    if ((prdStartSales = multi.getParameter("prdStartSales")) == null) prdStartSales = "0";
        
    BigDecimal vatPct = SwissKnife.parseBigDecimal(multi.getParameter("vatPct"), localeLanguage, localeCountry),
               wholesalePrc = SwissKnife.parseBigDecimal(multi.getParameter("wholesalePrc"), localeLanguage, localeCountry),
               wholesalePrcEU = SwissKnife.parseBigDecimal(multi.getParameter("wholesalePrcEU"), localeLanguage, localeCountry),
               wholesalePrcCUR1 = SwissKnife.parseBigDecimal(multi.getParameter("wholesalePrcCUR1"), localeLanguage, localeCountry),
               wholesalePrcCUR2 = SwissKnife.parseBigDecimal(multi.getParameter("wholesalePrcCUR2"), localeLanguage, localeCountry),
               retailPrc = SwissKnife.parseBigDecimal(multi.getParameter("retailPrc"), localeLanguage, localeCountry),
               retailPrcEU = SwissKnife.parseBigDecimal(multi.getParameter("retailPrcEU"), localeLanguage, localeCountry),
               retailPrcCUR1 = SwissKnife.parseBigDecimal(multi.getParameter("retailPrcCUR1"), localeLanguage, localeCountry),
               retailPrcCUR2 = SwissKnife.parseBigDecimal(multi.getParameter("retailPrcCUR2"), localeLanguage, localeCountry),
               slWholesalePrc = SwissKnife.parseBigDecimal(multi.getParameter("slWholesalePrc"), localeLanguage, localeCountry),
               slWholesalePrcEU = SwissKnife.parseBigDecimal(multi.getParameter("slWholesalePrcEU"), localeLanguage, localeCountry),
               slWholesalePrcCUR1 = SwissKnife.parseBigDecimal(multi.getParameter("slWholesalePrcCUR1"), localeLanguage, localeCountry),
               slWholesalePrcCUR2 = SwissKnife.parseBigDecimal(multi.getParameter("slWholesalePrcCUR2"), localeLanguage, localeCountry),
               slRetailPrc = SwissKnife.parseBigDecimal(multi.getParameter("slRetailPrc"), localeLanguage, localeCountry),
               slRetailPrcEU = SwissKnife.parseBigDecimal(multi.getParameter("slRetailPrcEU"), localeLanguage, localeCountry),
               slRetailPrcCUR1 = SwissKnife.parseBigDecimal(multi.getParameter("slRetailPrcCUR1"), localeLanguage, localeCountry),
               slRetailPrcCUR2 = SwissKnife.parseBigDecimal(multi.getParameter("slRetailPrcCUR2"), localeLanguage, localeCountry),
               hdWholesalePrc = SwissKnife.parseBigDecimal(multi.getParameter("hdWholesalePrc"), localeLanguage, localeCountry),
               hdWholesalePrcEU = SwissKnife.parseBigDecimal(multi.getParameter("hdWholesalePrcEU"), localeLanguage, localeCountry),
               hdWholesalePrcCUR1 = SwissKnife.parseBigDecimal(multi.getParameter("hdWholesalePrcCUR1"), localeLanguage, localeCountry),
               hdWholesalePrcCUR2 = SwissKnife.parseBigDecimal(multi.getParameter("hdWholesalePrcCUR2"), localeLanguage, localeCountry),
               hdRetailPrc = SwissKnife.parseBigDecimal(multi.getParameter("hdRetailPrc"), localeLanguage, localeCountry),
               hdRetailPrcEU = SwissKnife.parseBigDecimal(multi.getParameter("hdRetailPrcEU"), localeLanguage, localeCountry),
               hdRetailPrcCUR1 = SwissKnife.parseBigDecimal(multi.getParameter("hdRetailPrcCUR1"), localeLanguage, localeCountry),
               hdRetailPrcCUR2 = SwissKnife.parseBigDecimal(multi.getParameter("hdRetailPrcCUR2"), localeLanguage, localeCountry),
               giftPrc = SwissKnife.parseBigDecimal(multi.getParameter("giftPrc"), localeLanguage, localeCountry),
               giftPrcEU = SwissKnife.parseBigDecimal(multi.getParameter("giftPrcEU"), localeLanguage, localeCountry),
               giftPrcCUR1 = SwissKnife.parseBigDecimal(multi.getParameter("giftPrcCUR1"), localeLanguage, localeCountry),
               giftPrcCUR2 = SwissKnife.parseBigDecimal(multi.getParameter("giftPrcCUR2"), localeLanguage, localeCountry),
               weight = SwissKnife.parseBigDecimal(multi.getParameter("weight"), localeLanguage, localeCountry),
               cubemeter = SwissKnife.parseBigDecimal(multi.getParameter("cubemeter"), localeLanguage, localeCountry),
               minOrderQua = SwissKnife.parseBigDecimal(multi.getParameter("minOrderQua"), localeLanguage, localeCountry),
               maxOrderQua = SwissKnife.parseBigDecimal(multi.getParameter("maxOrderQua"), localeLanguage, localeCountry),
               safetyStockQua = SwissKnife.parseBigDecimal(multi.getParameter("safetyStockQua"), localeLanguage, localeCountry),
               shippingValue = SwissKnife.parseBigDecimal(multi.getParameter("shippingValue"), localeLanguage, localeCountry),
               shippingValueEU = SwissKnife.parseBigDecimal(multi.getParameter("shippingValueEU"), localeLanguage, localeCountry),
               shippingValueCUR1 = SwissKnife.parseBigDecimal(multi.getParameter("shippingValueCUR1"), localeLanguage, localeCountry),
               shippingValueCUR2 = SwissKnife.parseBigDecimal(multi.getParameter("shippingValueCUR2"), localeLanguage, localeCountry),
               shippingVatPct = SwissKnife.parseBigDecimal(multi.getParameter("shippingVatPct"), localeLanguage, localeCountry);
    
    if (vatPct == null) vatPct = _zero;
    if (wholesalePrc == null) wholesalePrc = _zero;
    if (wholesalePrcEU == null) wholesalePrcEU = _zero;
    if (wholesalePrcCUR1 == null) wholesalePrcCUR1 = _zero;
    if (wholesalePrcCUR2 == null) wholesalePrcCUR2 = _zero;
    if (retailPrc == null) retailPrc = _zero;
    if (retailPrcEU == null) retailPrcEU = _zero;
    if (retailPrcCUR1 == null) retailPrcCUR1 = _zero;
    if (retailPrcCUR2 == null) retailPrcCUR2 = _zero;
    if (slWholesalePrc == null) slWholesalePrc = _zero;
    if (slWholesalePrcEU == null) slWholesalePrcEU = _zero;
    if (slWholesalePrcCUR1 == null) slWholesalePrcCUR1 = _zero;
    if (slWholesalePrcCUR2 == null) slWholesalePrcCUR2 = _zero;
    if (slRetailPrc == null) slRetailPrc = _zero;
    if (slRetailPrcEU == null) slRetailPrcEU = _zero;
    if (slRetailPrcCUR1 == null) slRetailPrcCUR1 = _zero;
    if (slRetailPrcCUR2 == null) slRetailPrcCUR2 = _zero;
    if (hdWholesalePrc == null) hdWholesalePrc = _zero;
    if (hdWholesalePrcEU == null) hdWholesalePrcEU = _zero;
    if (hdWholesalePrcCUR1 == null) hdWholesalePrcCUR1 = _zero;
    if (hdWholesalePrcCUR2 == null) hdWholesalePrcCUR2 = _zero;
    if (hdRetailPrc == null) hdRetailPrc = _zero;
    if (hdRetailPrcEU == null) hdRetailPrcEU = _zero;
    if (hdRetailPrcCUR1 == null) hdRetailPrcCUR1 = _zero;
    if (hdRetailPrcCUR2 == null) hdRetailPrcCUR2 = _zero;
    if (giftPrc == null) giftPrc = _zero;
    if (giftPrcEU == null) giftPrcEU = _zero;
    if (giftPrcCUR1 == null) giftPrcCUR1 = _zero;
    if (giftPrcCUR2 == null) giftPrcCUR2 = _zero;
    if (weight == null) weight = _zero;
    if (cubemeter == null) cubemeter = _zero;
    if (minOrderQua == null) minOrderQua = _zero;
    if (maxOrderQua == null) maxOrderQua = _zero;
    if (safetyStockQua == null) safetyStockQua = _zero;
    if (shippingValue == null) shippingValue = _zero;
    if (shippingValueEU == null) shippingValueEU = _zero;
    if (shippingValueCUR1 == null) shippingValueCUR1 = _zero;
    if (shippingValueCUR2 == null) shippingValueCUR2 = _zero;
    if (shippingVatPct == null) shippingVatPct = _zero;

    String deliveryDays = null;
    if ((deliveryDays = multi.getParameter("deliveryDays")) == null) deliveryDays = "0";
    
    Timestamp hdBeginDate = SwissKnife.buildTimestamp(multi.getParameter("hdBeginDateDay"),
                                                      multi.getParameter("hdBeginDateMonth"),
                                                      multi.getParameter("hdBeginDateYear"));

    Timestamp hdEndDate = SwissKnife.buildTimestamp(multi.getParameter("hdEndDateDay"),
                                                    multi.getParameter("hdEndDateMonth"),
                                                    multi.getParameter("hdEndDateYear"));
    
    Timestamp hdBeginDateW = SwissKnife.buildTimestamp(multi.getParameter("hdBeginDateWDay"),
                                                       multi.getParameter("hdBeginDateWMonth"),
                                                       multi.getParameter("hdBeginDateWYear"));

    Timestamp hdEndDateW = SwissKnife.buildTimestamp(multi.getParameter("hdEndDateWDay"),
                                                     multi.getParameter("hdEndDateWMonth"),
                                                     multi.getParameter("hdEndDateWYear"));
    
    String[] imageOld = new String[]{img,img2,img3,img4},
	  image = new String[]{img,img2,img3,img4},
            imageUpload = new String[]{"","","",""};
    
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
    
    String query = "UPDATE product SET"
                 + " prdId2 = ?"
                 + ",name = ?"
                 + ",nameUp = ?"
                 + ",nameLG = ?"
                 + ",nameUpLG = ?"
                 + ",descr = ?"
                 + ",descrLG = ?"
                 + ",supplierId = ?"
                 + ",vatPct = ?"
                 + ",mUnit = ?"
                 + ",mUnitLG = ?"
                 + ",wholesalePrc = ?"
                 + ",wholesalePrcEU = ?"
                 + ",wholesalePrcCUR1 = ?"
                 + ",wholesalePrcCUR2 = ?"
                 + ",retailPrc = ?"
                 + ",retailPrcEU = ?"
                 + ",retailPrcCUR1 = ?"
                 + ",retailPrcCUR2 = ?"
                 + ",slWholesalePrc = ?"
                 + ",slWholesalePrcEU = ?"
                 + ",slWholesalePrcCUR1 = ?"
                 + ",slWholesalePrcCUR2 = ?"
                 + ",slRetailPrc = ?"
                 + ",slRetailPrcEU = ?"
                 + ",slRetailPrcCUR1 = ?"
                 + ",slRetailPrcCUR2 = ?"
                 + ",hdWholesalePrc = ?"
                 + ",hdWholesalePrcEU = ?"
                 + ",hdWholesalePrcCUR1 = ?"
                 + ",hdWholesalePrcCUR2 = ?"
                 + ",hdRetailPrc = ?"
                 + ",hdRetailPrcEU = ?"
                 + ",hdRetailPrcCUR1 = ?"
                 + ",hdRetailPrcCUR2 = ?"
                 + ",giftPrc = ?"
                 + ",giftPrcEU = ?"
                 + ",giftPrcCUR1 = ?"
                 + ",giftPrcCUR2 = ?"
                 + ",weight = ?"
                 + ",cubemeter = ?"
                 + ",minOrderQua = ?"
                 + ",maxOrderQua = ?"
                 + ",safetyStockQua = ?"
                 + ",package = ?"
                 + ",packageLG = ?"
                 + ",img = ?"
                 + ",img2 = ?"
                 + ",img3 = ?"
                 + ",img4 = ?"
                 + ",barcode = ?"
                 + ",deliveryDays = ?"
                 + ",catId = ?"
                 + ",hdStockFlag = ?"
                 + ",hdStockFlagW = ?"
                 + ",hotdealFlag = ?"
                 + ",hotdealFlagW = ?"
                 + ",shippingKindZone = ?"
                 + ",shippingValue = ?"
                 + ",shippingValueEU = ?"
                 + ",shippingValueCUR1 = ?"
                 + ",shippingValueCUR2 = ?"
                 + ",shippingVatPct = ?"
                 + ",prdLock = ?"
                 + ",prdManufactId = ?"
                 + ",prdHideFlag = ?"
                 + ",prdHideFlagW = ?"
                 + ",prdNewColl = ?"
                 + ",prdHomePageLink = ?"
                 + ",prdCompFlag = ?"
                 + ",prdHasAttributes = ?"
                 + ",prdStopSalesGlobal = ?"
                 + ",prdStartSales = ?"
                 + ",hdBeginDate = ?"
                 + ",hdEndDate = ?"
                 + ",hdBeginDateW = ?"
                 + ",hdEndDateW = ?"
                 + ",mUnitUp = ?"
                 + ",mUnitUpLG = ?"
                 + ",prd_SCCode = ?"
                 + ",prdImageCaption = ?"
                 + ",prdImageCaptionLG = ?"
                 + " WHERE prdId = ?";

    if (dbRet.getNoError() == 1) {
      try {
        ps = database.createPreparedStatement(query);
      
        ps.setString(1, prdId2);
        ps.setString(2, name);
        ps.setString(3, nameUp);
        ps.setString(4, nameLG);
        ps.setString(5, nameUpLG);
        ps.setString(6, descr);
        ps.setString(7, descrLG);
        
        if (supplierId == null || supplierId.length() == 0) {
          ps.setNull(8, Types.CHAR);
        }
        else {
          ps.setString(8, supplierId);
        }
        
        ps.setBigDecimal(9, vatPct);
        ps.setString(10, mUnit);
        ps.setString(11, mUnitLG);
        ps.setBigDecimal(12, wholesalePrc);
        ps.setBigDecimal(13, wholesalePrcEU);
        ps.setBigDecimal(14, wholesalePrcCUR1);
        ps.setBigDecimal(15, wholesalePrcCUR2);
        ps.setBigDecimal(16, retailPrc);
        ps.setBigDecimal(17, retailPrcEU);
        ps.setBigDecimal(18, retailPrcCUR1);
        ps.setBigDecimal(19, retailPrcCUR2);
        ps.setBigDecimal(20, slWholesalePrc);
        ps.setBigDecimal(21, slWholesalePrcEU);
        ps.setBigDecimal(22, slWholesalePrcCUR1);
        ps.setBigDecimal(23, slWholesalePrcCUR2);
        ps.setBigDecimal(24, slRetailPrc);
        ps.setBigDecimal(25, slRetailPrcEU);
        ps.setBigDecimal(26, slRetailPrcCUR1);
        ps.setBigDecimal(27, slRetailPrcCUR2);
        ps.setBigDecimal(28, hdWholesalePrc);
        ps.setBigDecimal(29, hdWholesalePrcEU);
        ps.setBigDecimal(30, hdWholesalePrcCUR1);
        ps.setBigDecimal(31, hdWholesalePrcCUR2);
        ps.setBigDecimal(32, hdRetailPrc);
        ps.setBigDecimal(33, hdRetailPrcEU);
        ps.setBigDecimal(34, hdRetailPrcCUR1);
        ps.setBigDecimal(35, hdRetailPrcCUR2);
        ps.setBigDecimal(36, giftPrc);
        ps.setBigDecimal(37, giftPrcEU);
        ps.setBigDecimal(38, giftPrcCUR1);
        ps.setBigDecimal(39, giftPrcCUR2);
        ps.setBigDecimal(40, weight);
        ps.setBigDecimal(41, cubemeter);
        ps.setBigDecimal(42, minOrderQua);
        ps.setBigDecimal(43, maxOrderQua);
        ps.setBigDecimal(44, safetyStockQua);
        ps.setString(45, packageName);
        ps.setString(46, packageNameLG);
        ps.setString(47, image[0]);
        ps.setString(48, image[1]);
        ps.setString(49, image[2]);
        ps.setString(50, image[3]);
        ps.setString(51, barcode);
        ps.setInt(52, Integer.parseInt(deliveryDays));
        ps.setString(53, catId);
        ps.setString(54, hdStockFlag);
        ps.setString(55, hdStockFlagW);
        ps.setString(56, hotdealFlag);
        ps.setString(57, hotdealFlagW);
        ps.setString(58, shippingKindZone);
        ps.setBigDecimal(59, shippingValue);
        ps.setBigDecimal(60, shippingValueEU);
        ps.setBigDecimal(61, shippingValueCUR1);
        ps.setBigDecimal(62, shippingValueCUR2);
        ps.setBigDecimal(63, shippingVatPct);
        ps.setString(64, prdLock);
        
        if (prdManufactId == null || prdManufactId.length() == 0) {
          ps.setNull(65, Types.CHAR);
        }
        else {
          ps.setString(65, prdManufactId);
        }
        
        ps.setString(66, prdHideFlag);
        ps.setString(67, prdHideFlagW);
        ps.setString(68, prdNewColl);
        ps.setString(69, prdHomePageLink);
        ps.setString(70, prdCompFlag);
        ps.setString(71, prdHasAttributes);
        ps.setString(72, prdStopSalesGlobal);
        ps.setString(73, prdStartSales);
      
        if (hdBeginDate == null) {
          ps.setNull(74, Types.TIMESTAMP);
        }
        else {
          ps.setTimestamp(74, hdBeginDate);
        }
      
        if (hdEndDate == null) {
          ps.setNull(75, Types.TIMESTAMP);
        }
        else {
          ps.setTimestamp(75, hdEndDate);
        }
      
        if (hdBeginDateW == null) {
          ps.setNull(76, Types.TIMESTAMP);
        }
        else {
          ps.setTimestamp(76, hdBeginDateW);
        }
      
        if (hdEndDateW == null) {
          ps.setNull(77, Types.TIMESTAMP);
        }
        else {
          ps.setTimestamp(77, hdEndDateW);
        }
        
        ps.setString(78, mUnitUp);
        ps.setString(79, mUnitUpLG);
        
        if (prd_SCCode == null || prd_SCCode.length() == 0) {
          ps.setNull(80, Types.VARCHAR);
        }
        else {
          ps.setString(80, prd_SCCode);
        }
        
        ps.setString(81, prdImageCaption);
        ps.setString(82, prdImageCaptionLG);
        
        ps.setString(83, prdId);

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

    int auth = bean.auth(databaseId,authUsername,authPassword,"product",Director.AUTH_DELETE);

     if (auth != Director.AUTH_OK) {
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    String prdId = SwissKnife.sqlEncode(multi.getParameter("prdId")),
           img = SwissKnife.sqlEncode(multi.getParameter("img01")),
           img2 = SwissKnife.sqlEncode(multi.getParameter("img02")),
           img3 = SwissKnife.sqlEncode(multi.getParameter("img03")),
           img4 = SwissKnife.sqlEncode(multi.getParameter("img04"));
    
    if (prdId.equals("")) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = null;
   
    database = bean.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    
    int prevTransIsolation = dbRet.getRetInt();

    String query = "DELETE FROM prdMonthly WHERE prdMId = '" + prdId + "'";

    dbRet = database.execQuery(query);

    /**
    if (dbRet.getNoError() == 1) {
      query = "DELETE FROM prdCompTab WHERE pcomPrdCode = '" + prdId + "'";
      
      dbRet = database.execQuery(query);
    }
    if (dbRet.getNoError() == 1) {
      query = "DELETE FROM prdAttributes WHERE prdAPrdId = '" + prdId + "'";

      dbRet = database.execQuery(query);
    }
    if (dbRet.getNoError() == 1) {
      query = "DELETE FROM PILines WHERE PILPrdId = '" + prdId + "'";
      
      dbRet = database.execQuery(query);
    }
    if (dbRet.getNoError() == 1) {
      query = "DELETE FROM prdImports WHERE prdId = '" + prdId + "'";
      
      dbRet = database.execQuery(query);
    }
    if (dbRet.getNoError() == 1) {
      query = "DELETE FROM prdInter WHERE PIPrdId = '" + prdId + "'";
      
      dbRet = database.execQuery(query);
    }
    if (dbRet.getNoError() == 1) {
      query = "DELETE FROM attributeTab WHERE attPrdId = '" + prdId + "'";
      
      dbRet = database.execQuery(query);
    }
    if (dbRet.getNoError() == 1) {
      query = "DELETE FROM attributeTab2 WHERE att2PrdId = '" + prdId + "'";
      
      dbRet = database.execQuery(query);
    }
    **/

    if (dbRet.getNoError() == 1) {
      query = "DELETE FROM prdInCatTab WHERE PINCPrdId = '" + prdId + "'";
      
      dbRet = database.execQuery(query);
    }

    if (dbRet.getNoError() == 1) {
      query = "DELETE FROM product WHERE prdId = '" + prdId + "'";
      
      dbRet = database.execQuery(query);
    }
    
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    bean.freeDBConnection(databaseId,database);
    
    File file = null;
    
    if (img.length()>0 && dbRet.getNoError() == 1) {
      file = new File(uploadPath, img);
             
      if (file.exists()) file.delete();
    }
     
    if (img2.length()>0 && dbRet.getNoError() == 1) {
      file = new File(uploadPath, img2);
             
      if (file.exists()) file.delete();
    }
    
    if (img3.length()>0 && dbRet.getNoError() == 1) {
      file = new File(uploadPath, img3);
             
      if (file.exists()) file.delete();
    }
    
    if (img4.length()>0 && dbRet.getNoError() == 1) {
      file = new File(uploadPath, img4);
             
      if (file.exists()) file.delete();
    }
    
    return dbRet;
  }
  
  private DbRet doDeleteImg(HttpServletRequest request,String databaseId,MultiRequest multi,String uploadPath) {
    DbRet dbRet = new DbRet();
       
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = bean.auth(databaseId,authUsername,authPassword,"product",Director.AUTH_DELETE);

     if (auth != Director.AUTH_OK) {
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    String prdId = SwissKnife.sqlEncode(multi.getParameter("prdId")),
           flag = SwissKnife.sqlEncode(multi.getParameter("flag")),
           img = "";
    
    if (flag.equals("1")) {
      img = SwissKnife.sqlEncode(multi.getParameter("img01"));
    }
    else if (flag.equals("2")) {
      img = SwissKnife.sqlEncode(multi.getParameter("img02"));
    }
    else if (flag.equals("3")) {
      img = SwissKnife.sqlEncode(multi.getParameter("img03"));
    }
    else if (flag.equals("4")) {
      img = SwissKnife.sqlEncode(multi.getParameter("img04"));
    }
    
    boolean delFile = false;
    
    if (prdId.equals("")) {
      dbRet.setNoError(0);
      
      return dbRet;
    }

    if (img.length()>0) {
      File file = new File(uploadPath,img);
           
      if (file.exists()) delFile= file.delete();

      if (!delFile) dbRet.setNoError(0);
    }
     
    Database database = null;
    database = bean.getDBConnection(databaseId);
        
    String query = "";
    
    if (flag.equals("1")) {
      query = "UPDATE product SET" 
            + " img = ''" 
            + " WHERE prdId = '" + prdId + "'";
    }
    else if (flag.equals("2")) {
      query = "UPDATE product SET" 
            + " img2 = ''" 
            + " WHERE prdId = '" + prdId + "'";
    }
    else if (flag.equals("3")) {
      query = "UPDATE product SET" 
            + " img3 = ''" 
            + " WHERE prdId = '" + prdId + "'";
    }
    else if (flag.equals("4")) {
      query = "UPDATE product SET" 
            + " img4 = ''" 
            + " WHERE prdId = '" + prdId + "'";
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(query);
    }
    
    bean.freeDBConnection(databaseId,database);
    
    return dbRet;
  }
}
