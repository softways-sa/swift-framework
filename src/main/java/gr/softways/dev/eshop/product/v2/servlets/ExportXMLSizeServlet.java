package gr.softways.dev.eshop.product.v2.servlets;

import gr.softways.dev.eshop.eways.v5.PriceChecker;
import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.lang3.StringUtils;

/**
 * XML feed that handles product options as 'SIZE' following
 * skroutz specs.
 * 
 * @author konstpan
 */
public class ExportXMLSizeServlet extends HttpServlet {

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    
    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    wwwrootPath = getServletContext().getRealPath("");
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request,response);
  }
  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    
    DbRet dbRet;

    HashMap<String, String> categoryTree;
    
    String uriScheme;
    
    StringBuilder query = new StringBuilder();
    
    boolean useZoom = false;
    
    query.append("SELECT product.prdId,product.prdHomePageLink,product.barcode,prdCategory.catId,product.name,product.img,product.hotdealFlag,");
    query.append("product.retailPrcEU,product.hdRetailPrcEU,product.vatPct,product.hdBeginDate,product.hdEndDate,");
    query.append("product.stockQua,product.prdAvailability,prdInCatTab.PINCCatId,VAT.*,ProductOptions.PO_Name");
    query.append(" FROM product ");
    query.append(" LEFT JOIN ProductOptions ON ProductOptions.PO_prdId = product.prdId");
    query.append(" JOIN prdInCatTab ON prdInCatTab.PINCPrdId = product.prdId");
    query.append(" JOIN prdCategory ON prdInCatTab.PINCCatId = prdCategory.catId");
    query.append(" JOIN VAT ON VAT.VAT_ID = product.PRD_VAT_ID");
    query.append(" WHERE prdHideFlag != '1'");
    query.append(" AND catShowFlag = '1'");
    query.append(" AND PINCPrimary = '1'");
    query.append(" ORDER BY product.prdId");
    
    PrintWriter out = null;
    
    String[] configurationValues = Configuration.getValues(new String[] {"useSSL", "excludeFromProductsFeed", "useZoomProductsFeed"});
    if (configurationValues[0] != null && "1".equals(configurationValues[0])) {
      uriScheme = "https://";
    }
    else {
      uriScheme = "http://";
    }
    
    if (StringUtils.isNotEmpty(configurationValues[1])) {
      String[] tokens = configurationValues[1].split(",");
      for (String cid : tokens) {
         query.append(" AND prdInCatTab.PINCCatId NOT LIKE '").append(SwissKnife.sqlEncode(cid)).append("%'");
      }
    }
    
    if ("1".equals(configurationValues[2])) {
      useZoom = true;
    }
    
    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(databaseId);
    
    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();
    
    categoryTree = getCategoryTree(database);
    
    QueryDataSet queryDataSet = null;
    
    try {
      response.setContentType("text/xml; charset=" + _charset);
      out = response.getWriter();
      
      queryDataSet = new QueryDataSet();
      
      queryDataSet.setQuery(new QueryDescriptor(database,query.toString(),null,true,Load.UNCACHED));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();
      
      dbRet = doExport(out, request, response, queryDataSet, categoryTree, uriScheme, useZoom);
    }
    finally {
      try { out.close(); } catch (Exception e) { e.printStackTrace(); }
      
      if (queryDataSet != null) try { queryDataSet.close(); } catch (Exception e) { e.printStackTrace(); }
      
      database.commitTransaction(1,prevTransIsolation);
    
      director.freeDBConnection(databaseId,database);
    }
  }
  
  private DbRet doExport(PrintWriter out, HttpServletRequest request, HttpServletResponse response, 
      QueryDataSet queryDataSet, HashMap<String, String> categoryTree, String uriScheme, boolean useZoom) {
    
    String prdId;
    String catId;
    
    DbRet dbRet = new DbRet();
    
    String server = uriScheme + request.getServerName() + "/";
      
    out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
    out.println("<webstore>");
    out.println("<created_at>" + SwissKnife.formatDate(SwissKnife.currentDate(),"yyyy-MM-dd HH:mm") + "</created_at>");
    out.println("<products>");

    while (queryDataSet.inBounds() == true) {
      prdId = queryDataSet.getString("prdId");
      out.println("<product>");

      out.println("<id>" + SwissKnife.sqlDecode(prdId) + "</id>");

      out.println("<name><![CDATA[" + SwissKnife.sqlDecode(queryDataSet.getString("name")) + "]]></name>");

      out.println("<manufacturer><![CDATA[" + SwissKnife.sqlDecode( queryDataSet.getString("prdHomePageLink") ) + "]]></manufacturer>");

      out.println("<mpn><![CDATA[" + SwissKnife.sqlDecode( queryDataSet.getString("barcode") ) + "]]></mpn>");

      out.println("<link><![CDATA[" + server + "product_detail.jsp?prdId=" + SwissKnife.sqlDecode( queryDataSet.getString("prdId") ) + "]]></link>");

      String prd_img = "", postfix_prd_img = "-1.jpg";
      if (useZoom) postfix_prd_img = "-1z.jpg";
      if (SwissKnife.fileExists(wwwrootPath + "/prd_images/" + SwissKnife.sqlDecode(prdId) + "-1.jpg")) {
        prd_img = "prd_images/" + SwissKnife.sqlDecode(prdId) + postfix_prd_img;

        out.println("<image><![CDATA[" + server + prd_img + "]]></image>");
      }
      else out.println("<image></image>");

      out.println("<price_with_vat>" + PriceChecker.calcPrd(one,queryDataSet,gr.softways.dev.eshop.eways.Customer.CUSTOMER_TYPE_RETAIL,PriceChecker.isOffer(queryDataSet,gr.softways.dev.eshop.eways.Customer.CUSTOMER_TYPE_RETAIL),zero).getUnitGrossCurr1().setScale(2,BigDecimal.ROUND_HALF_UP) + "</price_with_vat>");

      String s_instock = "", s_availability = "";
      String prdAvailability = SwissKnife.sqlDecode( queryDataSet.getString("prdAvailability") );
      if ("1".equals(prdAvailability)) {
        s_instock = "Y";
        s_availability = "Σε απόθεμα";
      }
      else {
        s_instock = "N";
        
        if ("2".equals(prdAvailability)) s_availability = "1 έως 3 ημέρες";
        else if ("3".equals(prdAvailability)) s_availability = "4 έως 7 ημέρες";
        else if ("4".equals(prdAvailability)) s_availability = "7+ ημέρες";
        else if ("5".equals(prdAvailability)) s_availability = "Κατόπιν Παραγγελίας";
        else if ("6".equals(prdAvailability)) s_availability = "Προ-παραγγελία";
        else s_availability = "";
      }
      
      out.println("<instock>"+ s_instock + "</instock>");
      out.println("<availability>" + s_availability + "</availability>");

      catId = SwissKnife.sqlDecode( queryDataSet.getString("catId") );

      out.print("<category><![CDATA[");
      for (int i=1; i<=(catId.length() / 2); i++) {
        if (i > 1) out.print(" > ");

        String catName = categoryTree.get(catId.substring(0, i*2));

        out.print(catName);
      }
      out.println("]]></category>");
      out.println("<category_id>" + catId + "</category_id>");
      
      boolean hasSize = false;
      if (queryDataSet.getString("PO_Name") != null) {
        hasSize = true;
        out.print("<size>");
        
        boolean firstIter = true;
        while (queryDataSet.inBounds() && prdId.equals(queryDataSet.getString("prdId"))) {
          if (!firstIter) out.print(",");
          out.print(queryDataSet.getString("PO_Name"));
          prdId = queryDataSet.getString("prdId");
          
          firstIter = false;
          queryDataSet.next();
        }
        
        out.println("</size>");
      }
      
      out.println("</product>");

      if (hasSize == false) queryDataSet.next();
    }
      
    out.println("</products>");
    out.println("</webstore>");
     
    return dbRet;
  }
  
  private HashMap<String, String> getCategoryTree(Database database) {
    HashMap categoryTree = new HashMap<String, String>();
    
    QueryDataSet queryDataSet = new QueryDataSet();
    
    String query = "SELECT catId,catName FROM prdCategory";
    
    queryDataSet = new QueryDataSet();

    queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.UNCACHED));
    queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

    queryDataSet.refresh();
    
    while (queryDataSet.inBounds() == true) {
      categoryTree.put(SwissKnife.sqlDecode(queryDataSet.getString("catId")), SwissKnife.sqlDecode(queryDataSet.getString("catName")));
      queryDataSet.next();
    }
    
    queryDataSet.close();
    
    return categoryTree;
  }
  
  private String _charset = null;
  
  private final BigDecimal zero = new BigDecimal("0");
  private final BigDecimal one = new BigDecimal("1");
  
  private final String databaseId = SwissKnife.jndiLookup("swconf/databaseId");
  
  private String wwwrootPath = "";
}