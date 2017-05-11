package gr.softways.dev.eshop.product.v2.servlets;

import java.io.*;
import java.util.*;
import java.math.*;
import java.sql.*;
import java.text.SimpleDateFormat;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class ProductServlet2_3 extends HttpServlet {
  
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
    else if (action.equals("UPDATE_STOCKQUA")) {
      dbRet = doUpdateStockQua(request, databaseId, multi, denyFilesExtTokenizer, uploadPath);
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

    int auth = bean.auth(databaseId,authUsername,authPassword,"product",Director.AUTH_INSERT);

    if (auth != Director.AUTH_OK) {
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      dbRet.setNoError(0);
      
      return dbRet;
    }

    SimpleDateFormat fixedDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    String localeLanguage = multi.getParameter("localeLanguage"),
           localeCountry = multi.getParameter("localeCountry");
    
    String catId = "", prdLock = "0";
    
    BigDecimal inQua = _zero, outQua = _zero,
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
           prd_SCCode = SwissKnife.sqlEncode(multi.getParameter("prd_SCCode")),
           nameLG1 = SwissKnife.sqlEncode(multi.getParameter("nameLG1")),
           descrLG1 = SwissKnife.sqlEncode(multi.getParameter("descrLG1")),
           mUnitLG1 = SwissKnife.sqlEncode(multi.getParameter("mUnitLG1")),
           packageNameLG1 = SwissKnife.sqlEncode(multi.getParameter("packageNameLG1")),
           nameLG2 = SwissKnife.sqlEncode(multi.getParameter("nameLG2")),
           descrLG2 = SwissKnife.sqlEncode(multi.getParameter("descrLG2")),
           mUnitLG2 = SwissKnife.sqlEncode(multi.getParameter("mUnitLG2")),
           packageNameLG2 = SwissKnife.sqlEncode(multi.getParameter("packageNameLG2")),
           nameLG3 = SwissKnife.sqlEncode(multi.getParameter("nameLG3")),
           descrLG3 = SwissKnife.sqlEncode(multi.getParameter("descrLG3")),
           mUnitLG3 = SwissKnife.sqlEncode(multi.getParameter("mUnitLG3")),
           packageNameLG3 = SwissKnife.sqlEncode(multi.getParameter("packageNameLG3")),
           PRD_GiftWrapAvail = multi.getParameter("PRD_GiftWrapAvail"),
           PRD_VAT_ID = multi.getParameter("PRD_VAT_ID");
          
    String specs = SwissKnife.sqlEncode(multi.getParameter("specs")),
           specsLG = SwissKnife.sqlEncode(multi.getParameter("specsLG")),
           specsLG1 = SwissKnife.sqlEncode(multi.getParameter("specsLG1")),
           specsLG2 = SwissKnife.sqlEncode(multi.getParameter("specsLG2")),
           specsLG3 = SwissKnife.sqlEncode(multi.getParameter("specsLG3"));
        
    String prdImageCaption = SwissKnife.sqlEncode(multi.getParameter("prdImageCaption")),
           prdImageCaptionLG = SwissKnife.sqlEncode(multi.getParameter("prdImageCaptionLG")),
           prdImageCaptionLG1 = SwissKnife.sqlEncode(multi.getParameter("prdImageCaptionLG1")),
           prdImageCaptionLG2 = SwissKnife.sqlEncode(multi.getParameter("prdImageCaptionLG2")),
           prdImageCaptionLG3 = SwissKnife.sqlEncode(multi.getParameter("prdImageCaptionLG3"));
    
    String prdAvailability = SwissKnife.sqlEncode(multi.getParameter("prdAvailability"));
    
    String nameUp = SwissKnife.searchConvert(name),
           nameUpLG = SwissKnife.searchConvert(nameLG),
           nameUpLG1 = SwissKnife.searchConvert(nameLG1),
           nameUpLG2 = SwissKnife.searchConvert(nameLG2),
           nameUpLG3 = SwissKnife.searchConvert(nameLG3),
           mUnitUp = SwissKnife.searchConvert(mUnit),
           mUnitUpLG = SwissKnife.searchConvert(mUnitLG),
           mUnitUpLG1 = SwissKnife.searchConvert(mUnitLG1),
           mUnitUpLG2 = SwissKnife.searchConvert(mUnitLG2),
           mUnitUpLG3 = SwissKnife.searchConvert(mUnitLG3);
    
    String nameLG4 = multi.getParameter("nameLG4"),
           descrLG4 = multi.getParameter("descrLG4"),
           mUnitLG4 = multi.getParameter("mUnitLG4"),
           packageNameLG4 = multi.getParameter("packageNameLG4"),
           prdImageCaptionLG4 = multi.getParameter("prdImageCaptionLG4"),
           specsLG4 = multi.getParameter("specsLG4"),
           nameLG5 = multi.getParameter("nameLG5"),
           descrLG5 = multi.getParameter("descrLG5"),
           mUnitLG5 = multi.getParameter("mUnitLG5"),
           packageNameLG5 = multi.getParameter("packageNameLG5"),
           prdImageCaptionLG5 = multi.getParameter("prdImageCaptionLG5"),
           specsLG5 = multi.getParameter("specsLG5"),
           nameLG6 = multi.getParameter("nameLG6"),
           descrLG6 = multi.getParameter("descrLG6"),
           mUnitLG6 = multi.getParameter("mUnitLG6"),
           packageNameLG6 = multi.getParameter("packageNameLG6"),
           prdImageCaptionLG6 = multi.getParameter("prdImageCaptionLG6"),
           specsLG6 = multi.getParameter("specsLG6"),
           nameLG7 = multi.getParameter("nameLG7"),
           descrLG7 = multi.getParameter("descrLG7"),
           mUnitLG7 = multi.getParameter("mUnitLG7"),
           packageNameLG7 = multi.getParameter("packageNameLG7"),
           prdImageCaptionLG7 = multi.getParameter("prdImageCaptionLG7"),
           specsLG7 = multi.getParameter("specsLG7");
    
    String nameUpLG4 = SwissKnife.searchConvert(nameLG4),
        mUnitUpLG4 = SwissKnife.searchConvert(mUnitLG4),
        nameUpLG5 = SwissKnife.searchConvert(nameLG5),
        mUnitUpLG5 = SwissKnife.searchConvert(mUnitLG5),
        nameUpLG6 = SwissKnife.searchConvert(nameLG6),
        mUnitUpLG6 = SwissKnife.searchConvert(mUnitLG6),
        nameUpLG7 = SwissKnife.searchConvert(nameLG7),
        mUnitUpLG7 = SwissKnife.searchConvert(mUnitLG7);
    
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
    
    String text1Title = multi.getParameter("text1Title"),
      text1 = multi.getParameter("text1"),
      text1TitleLG = multi.getParameter("text1TitleLG"),
      text1LG = multi.getParameter("text1LG"),
      text2Title = multi.getParameter("text2Title"),
      text2 = multi.getParameter("text2"),
      text2TitleLG = multi.getParameter("text2TitleLG"),
      text2LG = multi.getParameter("text2LG"),
      text3Title = multi.getParameter("text3Title"),
      text3 = multi.getParameter("text3"),
      text3TitleLG = multi.getParameter("text3TitleLG"),
      text3LG = multi.getParameter("text3LG"),
      text4Title = multi.getParameter("text4Title"),
      text4 = multi.getParameter("text4"),
      text4TitleLG = multi.getParameter("text4TitleLG"),
      text4LG = multi.getParameter("text4LG"),
      text5Title = multi.getParameter("text5Title"),
      text5 = multi.getParameter("text5"),
      text5TitleLG = multi.getParameter("text5TitleLG"),
      text5LG = multi.getParameter("text5LG");

    String text1TitleLG1 = multi.getParameter("text1TitleLG1"),
      text1LG1 = multi.getParameter("text1LG1"),
      text2TitleLG1 = multi.getParameter("text2TitleLG1"),
      text2LG1 = multi.getParameter("text2LG1"),
      text3TitleLG1 = multi.getParameter("text3TitleLG1"),
      text3LG1 = multi.getParameter("text3LG1"),
      text4TitleLG1 = multi.getParameter("text4TitleLG1"),
      text4LG1 = multi.getParameter("text4LG1"),
      text5TitleLG1 = multi.getParameter("text5TitleLG1"),
      text5LG1 = multi.getParameter("text5LG1");
    
    String text1TitleLG2 = multi.getParameter("text1TitleLG2"),
      text1LG2 = multi.getParameter("text1LG2"),
      text2TitleLG2 = multi.getParameter("text2TitleLG2"),
      text2LG2 = multi.getParameter("text2LG2"),
      text3TitleLG2 = multi.getParameter("text3TitleLG2"),
      text3LG2 = multi.getParameter("text3LG2"),
      text4TitleLG2 = multi.getParameter("text4TitleLG2"),
      text4LG2 = multi.getParameter("text4LG2"),
      text5TitleLG2 = multi.getParameter("text5TitleLG2"),
      text5LG2 = multi.getParameter("text5LG2");
    
    String text1TitleLG3 = multi.getParameter("text1TitleLG3"),
      text1LG3 = multi.getParameter("text1LG3"),
      text2TitleLG3 = multi.getParameter("text2TitleLG3"),
      text2LG3 = multi.getParameter("text2LG3"),
      text3TitleLG3 = multi.getParameter("text3TitleLG3"),
      text3LG3 = multi.getParameter("text3LG3"),
      text4TitleLG3 = multi.getParameter("text4TitleLG3"),
      text4LG3 = multi.getParameter("text4LG3"),
      text5TitleLG3 = multi.getParameter("text5TitleLG3"),
      text5LG3 = multi.getParameter("text5LG3");
    
    String text1TitleLG4 = multi.getParameter("text1TitleLG4"),
      text1LG4 = multi.getParameter("text1LG4"),
      text2TitleLG4 = multi.getParameter("text2TitleLG4"),
      text2LG4 = multi.getParameter("text2LG4"),
      text3TitleLG4 = multi.getParameter("text3TitleLG4"),
      text3LG4 = multi.getParameter("text3LG4"),
      text4TitleLG4 = multi.getParameter("text4TitleLG4"),
      text4LG4 = multi.getParameter("text4LG4"),
      text5TitleLG4 = multi.getParameter("text5TitleLG4"),
      text5LG4 = multi.getParameter("text5LG4");
    
    String text1TitleLG5 = multi.getParameter("text1TitleLG5"),
      text1LG5 = multi.getParameter("text1LG5"),
      text2TitleLG5 = multi.getParameter("text2TitleLG5"),
      text2LG5 = multi.getParameter("text2LG5"),
      text3TitleLG5 = multi.getParameter("text3TitleLG5"),
      text3LG5 = multi.getParameter("text3LG5"),
      text4TitleLG5 = multi.getParameter("text4TitleLG5"),
      text4LG5 = multi.getParameter("text4LG5"),
      text5TitleLG5 = multi.getParameter("text5TitleLG5"),
      text5LG5 = multi.getParameter("text5LG5");
      
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
        
    BigDecimal stockQua = SwissKnife.parseBigDecimal(multi.getParameter("stockQua"), localeLanguage, localeCountry);
    
    if (stockQua == null) stockQua = _zero;
        
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
    
    Timestamp prdEntryDate = null;
    try { prdEntryDate = new Timestamp(fixedDateFormat.parse(multi.getParameter("prdEntryDate")).getTime()); } catch (Exception e) { prdEntryDate = null; }
    
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
                 + ",prdImageCaption,prdImageCaptionLG,prdEntryDate"
                 + ",nameLG1,nameUpLG1,descrLG1,mUnitLG1,mUnitUpLG1,packageLG1"
                 + ",nameLG2,nameUpLG2,descrLG2,mUnitLG2,mUnitUpLG2,packageLG2"
                 + ",nameLG3,nameUpLG3,descrLG3,mUnitLG3,mUnitUpLG3,packageLG3"
                 + ",prdImageCaptionLG1,prdImageCaptionLG2,prdImageCaptionLG3"
                 + ",specs,specsLG,specsLG1,specsLG2,specsLG3"
                 + ",prdAvailability";
    
    if (nameLG4 != null) {
      query += ",nameLG4,nameUpLG4,descrLG4,mUnitLG4,mUnitUpLG4,packageLG4,specsLG4,prdImageCaptionLG4";
    }
    if (nameLG5 != null) {
      query += ",nameLG5,nameUpLG5,descrLG5,mUnitLG5,mUnitUpLG5,packageLG5,specsLG5,prdImageCaptionLG5";
    }
    if (nameLG6 != null) {
      query += ",nameLG6,nameUpLG6,descrLG6,mUnitLG6,mUnitUpLG6,packageLG6,specsLG6,prdImageCaptionLG6";
    }
    if (nameLG7 != null) {
      query += ",nameLG7,nameUpLG7,descrLG7,mUnitLG7,mUnitUpLG7,packageLG7,specsLG7,prdImageCaptionLG7";
    }
    
    query += ",text1Title,text1,text1TitleLG,text1LG,text2Title,text2,text2TitleLG,text2LG,text3Title,text3"
        + ",text3TitleLG,text3LG,text4Title,text4,text4TitleLG,text4LG,text5Title,text5"
        + ",text5TitleLG,text5LG";
    
    query += ",PRD_GiftWrapAvail,PRD_VAT_ID";
    
    if (text1TitleLG1 != null) {
      query += ",text1TitleLG1,text1LG1,text2TitleLG1,text2LG1,text3TitleLG1,text3LG1,text4TitleLG1,text4LG1,text5TitleLG1,text5LG1";
    }
    if (text1TitleLG2 != null) {
      query += ",text1TitleLG2,text1LG2,text2TitleLG2,text2LG2,text3TitleLG2,text3LG2,text4TitleLG2,text4LG2,text5TitleLG2,text5LG2";
    }
    if (text1TitleLG3 != null) {
      query += ",text1TitleLG3,text1LG3,text2TitleLG3,text2LG3,text3TitleLG3,text3LG3,text4TitleLG3,text4LG3,text5TitleLG3,text5LG3";
    }
    if (text1TitleLG4 != null) {
      query += ",text1TitleLG4,text1LG4,text2TitleLG4,text2LG4,text3TitleLG4,text3LG4,text4TitleLG4,text4LG4,text5TitleLG4,text5LG4";
    }
    if (text1TitleLG5 != null) {
      query += ",text1TitleLG5,text1LG5,text2TitleLG5,text2LG5,text3TitleLG5,text3LG5,text4TitleLG5,text4LG5,text5TitleLG5,text5LG5";
    }
    
    query += ") VALUES ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?"
        + ",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?"
        + ",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?"
        + ",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?"
        + ",?,?,?,?,?,?,?,?,?,?,?,?,?,?"
        + ",?,?"
        + ",?,?,?,?,?,?,?,?,?,?"
        + ",?,?,?,?,?,?,?,?,?"
        + ",?,?,?,?,?,?,?,?"
        + ",?,?";
    
    if (nameLG4 != null) {
      query += ",?,?,?,?,?,?,?,?";
    }
    if (nameLG5 != null) {
      query += ",?,?,?,?,?,?,?,?";
    }
    if (nameLG6 != null) {
      query += ",?,?,?,?,?,?,?,?";
    }
    if (nameLG7 != null) {
      query += ",?,?,?,?,?,?,?,?";
    }
    
    query += ",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";
    
    query += ",?";
    
    if (text1TitleLG1 != null) {
      query += ",?,?,?,?,?,?,?,?,?,?";
    }
    if (text1TitleLG2 != null) {
      query += ",?,?,?,?,?,?,?,?,?,?";
    }
    if (text1TitleLG3 != null) {
      query += ",?,?,?,?,?,?,?,?,?,?";
    }
    if (text1TitleLG4 != null) {
      query += ",?,?,?,?,?,?,?,?,?,?";
    }
    if (text1TitleLG5 != null) {
      query += ",?,?,?,?,?,?,?,?,?,?";
    }
    
    query += ")";
    
    int colIndex = 0;
    
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
        
        if (prdEntryDate == null) {
          ps.setNull(97, Types.TIMESTAMP);
        }
        else {
          ps.setTimestamp(97, prdEntryDate);
        }
        ps.setString(98, nameLG1);
        ps.setString(99, nameUpLG1);
        ps.setString(100, descrLG1);
        ps.setString(101, mUnitLG1);
        ps.setString(102, mUnitUpLG1);
        ps.setString(103, packageNameLG1);
        
        ps.setString(104, nameLG2);
        ps.setString(105, nameUpLG2);
        ps.setString(106, descrLG2);
        ps.setString(107, mUnitLG2);
        ps.setString(108, mUnitUpLG2);
        ps.setString(109, packageNameLG2);
        
        ps.setString(110, nameLG3);
        ps.setString(111, nameUpLG3);
        ps.setString(112, descrLG3);
        ps.setString(113, mUnitLG3);
        ps.setString(114, mUnitUpLG3);
        ps.setString(115, packageNameLG3);
        
        ps.setString(116, prdImageCaptionLG1);
        ps.setString(117, prdImageCaptionLG2);
        ps.setString(118, prdImageCaptionLG3);
        ps.setString(119, specs);
        ps.setString(120, specsLG);
        ps.setString(121, specsLG1);
        ps.setString(122, specsLG2);
        ps.setString(123, specsLG3);
        
        ps.setString(124, prdAvailability);
        colIndex = 124;
        
        if (nameLG4 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameUpLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(descrLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(mUnitLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(mUnitUpLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(packageNameLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(specsLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(prdImageCaptionLG4));
        }
        if (nameLG5 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameUpLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(descrLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(mUnitLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(mUnitUpLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(packageNameLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(specsLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(prdImageCaptionLG5));
        }
        if (nameLG6 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameUpLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(descrLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(mUnitLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(mUnitUpLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(packageNameLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(specsLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(prdImageCaptionLG6));
        }
        if (nameLG7 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameUpLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(descrLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(mUnitLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(mUnitUpLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(packageNameLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(specsLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(prdImageCaptionLG7));
        }
        
        ps.setString(++colIndex, SwissKnife.sqlEncode(text1Title));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text1));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text1TitleLG));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text1LG));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text2Title));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text2));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text2TitleLG));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text2LG));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text3Title));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text3));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text3TitleLG));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text3LG));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text4Title));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text4));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text4TitleLG));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text4LG));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text5Title));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text5));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text5TitleLG));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text5LG));
        
        ps.setString(++colIndex, SwissKnife.sqlEncode(PRD_GiftWrapAvail));
        ps.setString(++colIndex, SwissKnife.sqlEncode(PRD_VAT_ID));
        
        if (text1TitleLG1 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(text1TitleLG1));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text1LG1));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text2TitleLG1));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text2LG1));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text3TitleLG1));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text3LG1));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text4TitleLG1));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text4LG1));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text5TitleLG1));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text5LG1));
        }
        if (text1TitleLG2 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(text1TitleLG2));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text1LG2));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text2TitleLG2));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text2LG2));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text3TitleLG2));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text3LG2));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text4TitleLG2));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text4LG2));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text5TitleLG2));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text5LG2));
        }
        if (text1TitleLG3 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(text1TitleLG3));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text1LG3));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text2TitleLG3));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text2LG3));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text3TitleLG3));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text3LG3));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text4TitleLG3));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text4LG3));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text5TitleLG3));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text5LG3));
        }
        if (text1TitleLG4 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(text1TitleLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text1LG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text2TitleLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text2LG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text3TitleLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text3LG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text4TitleLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text4LG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text5TitleLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text5LG4));
        }
        if (text1TitleLG5 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(text1TitleLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text1LG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text2TitleLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text2LG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text3TitleLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text3LG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text4TitleLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text4LG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text5TitleLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text5LG5));
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

    int auth = bean.auth(databaseId,authUsername,authPassword,"product",Director.AUTH_UPDATE);

    if (auth != Director.AUTH_OK) {
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    SimpleDateFormat fixedDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    
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
           prd_SCCode = SwissKnife.sqlEncode(multi.getParameter("prd_SCCode")),
           nameLG1 = SwissKnife.sqlEncode(multi.getParameter("nameLG1")),
           descrLG1 = SwissKnife.sqlEncode(multi.getParameter("descrLG1")),
           mUnitLG1 = SwissKnife.sqlEncode(multi.getParameter("mUnitLG1")),
           packageNameLG1 = SwissKnife.sqlEncode(multi.getParameter("packageNameLG1")),
           nameLG2 = SwissKnife.sqlEncode(multi.getParameter("nameLG2")),
           descrLG2 = SwissKnife.sqlEncode(multi.getParameter("descrLG2")),
           mUnitLG2 = SwissKnife.sqlEncode(multi.getParameter("mUnitLG2")),
           packageNameLG2 = SwissKnife.sqlEncode(multi.getParameter("packageNameLG2")),
           nameLG3 = SwissKnife.sqlEncode(multi.getParameter("nameLG3")),
           descrLG3 = SwissKnife.sqlEncode(multi.getParameter("descrLG3")),
           mUnitLG3 = SwissKnife.sqlEncode(multi.getParameter("mUnitLG3")),
           packageNameLG3 = SwissKnife.sqlEncode(multi.getParameter("packageNameLG3")),
           PRD_GiftWrapAvail = multi.getParameter("PRD_GiftWrapAvail"),
           PRD_VAT_ID = multi.getParameter("PRD_VAT_ID");
           
    String specs = SwissKnife.sqlEncode(multi.getParameter("specs")),
           specsLG = SwissKnife.sqlEncode(multi.getParameter("specsLG")),
           specsLG1 = SwissKnife.sqlEncode(multi.getParameter("specsLG1")),
           specsLG2 = SwissKnife.sqlEncode(multi.getParameter("specsLG2")),
           specsLG3 = SwissKnife.sqlEncode(multi.getParameter("specsLG3"));
        
    String prdImageCaption = SwissKnife.sqlEncode(multi.getParameter("prdImageCaption")),
           prdImageCaptionLG = SwissKnife.sqlEncode(multi.getParameter("prdImageCaptionLG")),
           prdImageCaptionLG1 = SwissKnife.sqlEncode(multi.getParameter("prdImageCaptionLG1")),
           prdImageCaptionLG2 = SwissKnife.sqlEncode(multi.getParameter("prdImageCaptionLG2")),
           prdImageCaptionLG3 = SwissKnife.sqlEncode(multi.getParameter("prdImageCaptionLG3"));
    
    String prdAvailability = SwissKnife.sqlEncode(multi.getParameter("prdAvailability"));
        
    String nameUp = SwissKnife.searchConvert(name),
           nameUpLG = SwissKnife.searchConvert(nameLG),
           nameUpLG1 = SwissKnife.searchConvert(nameLG1),
           nameUpLG2 = SwissKnife.searchConvert(nameLG2),
           nameUpLG3 = SwissKnife.searchConvert(nameLG3),
           mUnitUp = SwissKnife.searchConvert(mUnit),
           mUnitUpLG = SwissKnife.searchConvert(mUnitLG),
           mUnitUpLG1 = SwissKnife.searchConvert(mUnitLG1),
           mUnitUpLG2 = SwissKnife.searchConvert(mUnitLG2),
           mUnitUpLG3 = SwissKnife.searchConvert(mUnitLG3);
    
    String nameLG4 = multi.getParameter("nameLG4"),
           descrLG4 = multi.getParameter("descrLG4"),
           mUnitLG4 = multi.getParameter("mUnitLG4"),
           packageNameLG4 = multi.getParameter("packageNameLG4"),
           prdImageCaptionLG4 = multi.getParameter("prdImageCaptionLG4"),
           specsLG4 = multi.getParameter("specsLG4"),
           nameLG5 = multi.getParameter("nameLG5"),
           descrLG5 = multi.getParameter("descrLG5"),
           mUnitLG5 = multi.getParameter("mUnitLG5"),
           packageNameLG5 = multi.getParameter("packageNameLG5"),
           prdImageCaptionLG5 = multi.getParameter("prdImageCaptionLG5"),
           specsLG5 = multi.getParameter("specsLG5"),
           nameLG6 = multi.getParameter("nameLG6"),
           descrLG6 = multi.getParameter("descrLG6"),
           mUnitLG6 = multi.getParameter("mUnitLG6"),
           packageNameLG6 = multi.getParameter("packageNameLG6"),
           prdImageCaptionLG6 = multi.getParameter("prdImageCaptionLG6"),
           specsLG6 = multi.getParameter("specsLG6"),
           nameLG7 = multi.getParameter("nameLG7"),
           descrLG7 = multi.getParameter("descrLG7"),
           mUnitLG7 = multi.getParameter("mUnitLG7"),
           packageNameLG7 = multi.getParameter("packageNameLG7"),
           prdImageCaptionLG7 = multi.getParameter("prdImageCaptionLG7"),
           specsLG7 = multi.getParameter("specsLG7");
    
    String nameUpLG4 = SwissKnife.searchConvert(nameLG4),
        mUnitUpLG4 = SwissKnife.searchConvert(mUnitLG4),
        nameUpLG5 = SwissKnife.searchConvert(nameLG5),
        mUnitUpLG5 = SwissKnife.searchConvert(mUnitLG5),
        nameUpLG6 = SwissKnife.searchConvert(nameLG6),
        mUnitUpLG6 = SwissKnife.searchConvert(mUnitLG6),
        nameUpLG7 = SwissKnife.searchConvert(nameLG7),
        mUnitUpLG7 = SwissKnife.searchConvert(mUnitLG7);
    
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
    
    String text1Title = multi.getParameter("text1Title"),
      text1 = multi.getParameter("text1"),
      text1TitleLG = multi.getParameter("text1TitleLG"),
      text1LG = multi.getParameter("text1LG"),
      text2Title = multi.getParameter("text2Title"),
      text2 = multi.getParameter("text2"),
      text2TitleLG = multi.getParameter("text2TitleLG"),
      text2LG = multi.getParameter("text2LG"),
      text3Title = multi.getParameter("text3Title"),
      text3 = multi.getParameter("text3"),
      text3TitleLG = multi.getParameter("text3TitleLG"),
      text3LG = multi.getParameter("text3LG"),
      text4Title = multi.getParameter("text4Title"),
      text4 = multi.getParameter("text4"),
      text4TitleLG = multi.getParameter("text4TitleLG"),
      text4LG = multi.getParameter("text4LG"),
      text5Title = multi.getParameter("text5Title"),
      text5 = multi.getParameter("text5"),
      text5TitleLG = multi.getParameter("text5TitleLG"),
      text5LG = multi.getParameter("text5LG");
    
    String text1TitleLG1 = multi.getParameter("text1TitleLG1"),
      text1LG1 = multi.getParameter("text1LG1"),
      text2TitleLG1 = multi.getParameter("text2TitleLG1"),
      text2LG1 = multi.getParameter("text2LG1"),
      text3TitleLG1 = multi.getParameter("text3TitleLG1"),
      text3LG1 = multi.getParameter("text3LG1"),
      text4TitleLG1 = multi.getParameter("text4TitleLG1"),
      text4LG1 = multi.getParameter("text4LG1"),
      text5TitleLG1 = multi.getParameter("text5TitleLG1"),
      text5LG1 = multi.getParameter("text5LG1");
    
    String text1TitleLG2 = multi.getParameter("text1TitleLG2"),
      text1LG2 = multi.getParameter("text1LG2"),
      text2TitleLG2 = multi.getParameter("text2TitleLG2"),
      text2LG2 = multi.getParameter("text2LG2"),
      text3TitleLG2 = multi.getParameter("text3TitleLG2"),
      text3LG2 = multi.getParameter("text3LG2"),
      text4TitleLG2 = multi.getParameter("text4TitleLG2"),
      text4LG2 = multi.getParameter("text4LG2"),
      text5TitleLG2 = multi.getParameter("text5TitleLG2"),
      text5LG2 = multi.getParameter("text5LG2");
    
    String text1TitleLG3 = multi.getParameter("text1TitleLG3"),
      text1LG3 = multi.getParameter("text1LG3"),
      text2TitleLG3 = multi.getParameter("text2TitleLG3"),
      text2LG3 = multi.getParameter("text2LG3"),
      text3TitleLG3 = multi.getParameter("text3TitleLG3"),
      text3LG3 = multi.getParameter("text3LG3"),
      text4TitleLG3 = multi.getParameter("text4TitleLG3"),
      text4LG3 = multi.getParameter("text4LG3"),
      text5TitleLG3 = multi.getParameter("text5TitleLG3"),
      text5LG3 = multi.getParameter("text5LG3");
    
    String text1TitleLG4 = multi.getParameter("text1TitleLG4"),
      text1LG4 = multi.getParameter("text1LG4"),
      text2TitleLG4 = multi.getParameter("text2TitleLG4"),
      text2LG4 = multi.getParameter("text2LG4"),
      text3TitleLG4 = multi.getParameter("text3TitleLG4"),
      text3LG4 = multi.getParameter("text3LG4"),
      text4TitleLG4 = multi.getParameter("text4TitleLG4"),
      text4LG4 = multi.getParameter("text4LG4"),
      text5TitleLG4 = multi.getParameter("text5TitleLG4"),
      text5LG4 = multi.getParameter("text5LG4");
    
    String text1TitleLG5 = multi.getParameter("text1TitleLG5"),
      text1LG5 = multi.getParameter("text1LG5"),
      text2TitleLG5 = multi.getParameter("text2TitleLG5"),
      text2LG5 = multi.getParameter("text2LG5"),
      text3TitleLG5 = multi.getParameter("text3TitleLG5"),
      text3LG5 = multi.getParameter("text3LG5"),
      text4TitleLG5 = multi.getParameter("text4TitleLG5"),
      text4LG5 = multi.getParameter("text4LG5"),
      text5TitleLG5 = multi.getParameter("text5TitleLG5"),
      text5LG5 = multi.getParameter("text5LG5");
    
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
    
    Timestamp prdEntryDate = null;
    try { prdEntryDate = new Timestamp(fixedDateFormat.parse(multi.getParameter("prdEntryDate")).getTime()); } catch (Exception e) { prdEntryDate = null; }
    
    String[] facetValues = multi.getParameterValues("facet_val_id");
    System.out.println("facetValues: " + Arrays.toString(facetValues));
    
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
                 + ",prdEntryDate = ?"
                 + ",nameLG1 = ?"
                 + ",nameUpLG1 = ?"
                 + ",descrLG1 = ?"
                 + ",mUnitLG1 = ?"
                 + ",mUnitUpLG1 = ?"
                 + ",packageLG1 = ?"
                 + ",nameLG2 = ?"
                 + ",nameUpLG2 = ?"
                 + ",descrLG2 = ?"
                 + ",mUnitLG2 = ?"
                 + ",mUnitUpLG2 = ?"
                 + ",packageLG2 = ?"
                 + ",nameLG3 = ?"
                 + ",nameUpLG3 = ?"
                 + ",descrLG3 = ?"
                 + ",mUnitLG3 = ?"
                 + ",mUnitUpLG3 = ?"
                 + ",packageLG3 = ?"
                 + ",prdImageCaptionLG1 = ?"
                 + ",prdImageCaptionLG2 = ?"
                 + ",prdImageCaptionLG3 = ?"
                 + ",specs = ?"
                 + ",specsLG = ?"
                 + ",specsLG1 = ?"
                 + ",specsLG2 = ?"
                 + ",specsLG3 = ?"
                 + ",prdAvailability = ?";
    
    if (nameLG4 != null) {
      query += ",nameLG4 = ?, nameUpLG4 = ?, descrLG4 = ?, mUnitLG4 = ?, mUnitUpLG4 = ?, packageLG4 = ?, specsLG4 = ?, prdImageCaptionLG4 = ?";
    }
    if (nameLG5 != null) {
      query += ",nameLG5 = ?, nameUpLG5 = ?, descrLG5 = ?, mUnitLG5 = ?, mUnitUpLG5 = ?, packageLG5 = ?, specsLG5 = ?, prdImageCaptionLG5 = ?";
    }
    if (nameLG6 != null) {
      query += ",nameLG6 = ?, nameUpLG6 = ?, descrLG6 = ?, mUnitLG6 = ?, mUnitUpLG6 = ?, packageLG6 = ?, specsLG6 = ?, prdImageCaptionLG6 = ?";
    }
    if (nameLG7 != null) {
      query += ",nameLG7 = ?, nameUpLG7 = ?, descrLG7 = ?, mUnitLG7 = ?, mUnitUpLG7 = ?, packageLG7 = ?, specsLG7 = ?, prdImageCaptionLG7 = ?";
    }
    
    query += ",text1Title = ?, text1 = ?, text1TitleLG = ?, text1LG = ?, text2Title = ?, text2 = ?, text2TitleLG = ?, text2LG = ?, text3Title = ?"
        + ",text3 = ?, text3TitleLG = ?, text3LG = ?, text4Title = ?, text4 = ?, text4TitleLG = ?, text4LG = ?, text5Title = ?, text5 = ?"
        + ",text5TitleLG = ?, text5LG = ?";
    
    query += ", PRD_GiftWrapAvail = ?, PRD_VAT_ID = ?";
    
    if (text1TitleLG1 != null) {
      query += ", text1TitleLG1 = ?, text1LG1 = ?, text2TitleLG1 = ?, text2LG1 = ?, text3TitleLG1 = ?, text3LG1 = ?, text4TitleLG1 = ?, text4LG1 = ?, text5TitleLG1 = ?, text5LG1 = ?";
    }
    if (text1TitleLG2 != null) {
      query += ", text1TitleLG2 = ?, text1LG2 = ?, text2TitleLG2 = ?, text2LG2 = ?, text3TitleLG2 = ?, text3LG2 = ?, text4TitleLG2 = ?, text4LG2 = ?, text5TitleLG2 = ?, text5LG2 = ?";
    }
    if (text1TitleLG3 != null) {
      query += ", text1TitleLG3 = ?, text1LG3 = ?, text2TitleLG3 = ?, text2LG3 = ?, text3TitleLG3 = ?, text3LG3 = ?, text4TitleLG3 = ?, text4LG3 = ?, text5TitleLG3 = ?, text5LG3 = ?";
    }
    if (text1TitleLG4 != null) {
      query += ", text1TitleLG4 = ?, text1LG4 = ?, text2TitleLG4 = ?, text2LG4 = ?, text3TitleLG4 = ?, text3LG4 = ?, text4TitleLG4 = ?, text4LG4 = ?, text5TitleLG4 = ?, text5LG4 = ?";
    }
    if (text1TitleLG5 != null) {
      query += ", text1TitleLG5 = ?, text1LG5 = ?, text2TitleLG5 = ?, text2LG5 = ?, text3TitleLG5 = ?, text3LG5 = ?, text4TitleLG5 = ?, text4LG5 = ?, text5TitleLG5 = ?, text5LG5 = ?";
    }
    
    query += " WHERE prdId = ?";

    int colIndex = 0;
    
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
        
        if (prdEntryDate == null) {
          ps.setNull(83, Types.TIMESTAMP);
        }
        else {
          ps.setTimestamp(83, prdEntryDate);
        }
        ps.setString(84, nameLG1);
        ps.setString(85, nameUpLG1);
        ps.setString(86, descrLG1);
        ps.setString(87, mUnitLG1);
        ps.setString(88, mUnitUpLG1);
        ps.setString(89, packageNameLG1);
        
        ps.setString(90, nameLG2);
        ps.setString(91, nameUpLG2);
        ps.setString(92, descrLG2);
        ps.setString(93, mUnitLG2);
        ps.setString(94, mUnitUpLG2);
        ps.setString(95, packageNameLG2);
        
        ps.setString(96, nameLG3);
        ps.setString(97, nameUpLG3);
        ps.setString(98, descrLG3);
        ps.setString(99, mUnitLG3);
        ps.setString(100, mUnitUpLG3);
        ps.setString(101, packageNameLG3);
        
        ps.setString(102, prdImageCaptionLG1);
        ps.setString(103, prdImageCaptionLG2);
        ps.setString(104, prdImageCaptionLG3);
        ps.setString(105, specs);
        ps.setString(106, specsLG);
        ps.setString(107, specsLG1);
        ps.setString(108, specsLG2);
        ps.setString(109, specsLG3);
        
        ps.setString(110, prdAvailability);
                 
        colIndex = 110;
        
        if (nameLG4 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameUpLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(descrLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(mUnitLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(mUnitUpLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(packageNameLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(specsLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(prdImageCaptionLG4));
        }
        if (nameLG5 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameUpLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(descrLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(mUnitLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(mUnitUpLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(packageNameLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(specsLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(prdImageCaptionLG5));
        }
        if (nameLG6 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameUpLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(descrLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(mUnitLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(mUnitUpLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(packageNameLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(specsLG6));
          ps.setString(++colIndex, SwissKnife.sqlEncode(prdImageCaptionLG6));
        }
        if (nameLG7 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(nameUpLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(descrLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(mUnitLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(mUnitUpLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(packageNameLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(specsLG7));
          ps.setString(++colIndex, SwissKnife.sqlEncode(prdImageCaptionLG7));
        }
        
        ps.setString(++colIndex, SwissKnife.sqlEncode(text1Title));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text1));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text1TitleLG));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text1LG));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text2Title));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text2));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text2TitleLG));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text2LG));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text3Title));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text3));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text3TitleLG));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text3LG));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text4Title));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text4));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text4TitleLG));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text4LG));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text5Title));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text5));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text5TitleLG));
        ps.setString(++colIndex, SwissKnife.sqlEncode(text5LG));
        
        ps.setString(++colIndex, SwissKnife.sqlEncode(PRD_GiftWrapAvail));
        ps.setString(++colIndex, SwissKnife.sqlEncode(PRD_VAT_ID));
        
        if (text1TitleLG1 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(text1TitleLG1));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text1LG1));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text2TitleLG1));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text2LG1));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text3TitleLG1));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text3LG1));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text4TitleLG1));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text4LG1));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text5TitleLG1));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text5LG1));
        }
        if (text1TitleLG2 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(text1TitleLG2));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text1LG2));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text2TitleLG2));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text2LG2));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text3TitleLG2));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text3LG2));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text4TitleLG2));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text4LG2));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text5TitleLG2));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text5LG2));
        }
        if (text1TitleLG3 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(text1TitleLG3));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text1LG3));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text2TitleLG3));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text2LG3));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text3TitleLG3));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text3LG3));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text4TitleLG3));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text4LG3));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text5TitleLG3));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text5LG3));
        }
        if (text1TitleLG4 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(text1TitleLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text1LG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text2TitleLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text2LG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text3TitleLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text3LG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text4TitleLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text4LG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text5TitleLG4));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text5LG4));
        }
        if (text1TitleLG5 != null) {
          ps.setString(++colIndex, SwissKnife.sqlEncode(text1TitleLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text1LG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text2TitleLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text2LG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text3TitleLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text3LG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text4TitleLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text4LG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text5TitleLG5));
          ps.setString(++colIndex, SwissKnife.sqlEncode(text5LG5));
        }
        
        ps.setString(++colIndex, prdId);

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
  
  private DbRet doUpdateStockQua(HttpServletRequest request,String databaseId,MultiRequest multi,StrTokenizer denyFilesExtTokenizer,String uploadPath) {
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
    
    String prdId = SwissKnife.sqlEncode(multi.getParameter("prdId"));
        
    BigDecimal stockQua = SwissKnife.parseBigDecimal(multi.getParameter("stockQua"), localeLanguage, localeCountry);
    
    if (stockQua == null) {
      dbRet.setNoError(0);
      
      return dbRet;
    }
    
    Database database = null;
    
    PreparedStatement ps = null;
   
    database = bean.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    
    int prevTransIsolation = dbRet.getRetInt();
    
    String query = "UPDATE product SET"
                 + " stockQua = ?"
                 + " WHERE prdId = ?";

    if (dbRet.getNoError() == 1) {
      try {
        ps = database.createPreparedStatement(query);
      
        ps.setBigDecimal(1, stockQua);
        ps.setString(2, prdId);
        
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
    
    return dbRet;
  }
}