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

public class ExportXMLServlet extends HttpServlet {

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
    
    query.append("SELECT product.prdId,product.prdHomePageLink,product.barcode,prdCategory.catId,product.name,product.img,product.hotdealFlag,");
    query.append("product.retailPrcEU,product.hdRetailPrcEU,product.vatPct,product.hdBeginDate,product.hdEndDate,");
    query.append("product.stockQua,product.prdAvailability,prdInCatTab.PINCCatId,VAT.*");
    query.append(" FROM product");
    query.append(" JOIN prdInCatTab ON prdInCatTab.PINCPrdId = product.prdId");
    query.append(" JOIN prdCategory ON prdInCatTab.PINCCatId = prdCategory.catId");
    query.append(" JOIN VAT ON VAT.VAT_ID = product.PRD_VAT_ID");
    query.append(" WHERE prdHideFlag != '1'");
    query.append(" AND catShowFlag = '1'");
    query.append(" AND PINCPrimary = '1'");
    
    PrintWriter out = null;
    
    String[] configurationValues = Configuration.getValues(new String[] {"useSSL", "excludeFromProductsFeed"});
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
      
      doExport(out, request, response, queryDataSet, categoryTree, uriScheme);
    }
    finally {
      if (queryDataSet != null) try { queryDataSet.close(); } catch (Exception e) { e.printStackTrace(); }
      database.commitTransaction(1,prevTransIsolation);
      director.freeDBConnection(databaseId,database);
      
      if (out != null) out.close();
    }
  }
  
  private DbRet doExport(PrintWriter out, HttpServletRequest request, HttpServletResponse response, 
      QueryDataSet queryDataSet, HashMap<String, String> categoryTree, String uriScheme) {
    String catId = null;
    
    DbRet dbRet = new DbRet();
    
    String server = uriScheme + request.getServerName() + "/";
      
    out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
    out.println("<webstore>");
    out.println("<created_at>" + SwissKnife.formatDate(SwissKnife.currentDate(),"yyyy-MM-dd HH:mm") + "</created_at>");
    out.println("<products>");

    while (queryDataSet.inBounds() == true) {
      out.println("<product>");

      out.println("<id>" + SwissKnife.sqlDecode( queryDataSet.getString("prdId") ) + "</id>");

      out.println("<name><![CDATA[" + SwissKnife.sqlDecode(queryDataSet.getString("name")) + "]]></name>");

      out.println("<manufacturer><![CDATA[" + SwissKnife.sqlDecode( queryDataSet.getString("prdHomePageLink") ) + "]]></manufacturer>");

      out.println("<mpn><![CDATA[" + SwissKnife.sqlDecode( queryDataSet.getString("barcode") ) + "]]></mpn>");

      out.println("<link><![CDATA[" + server + "product_detail.jsp?prdId=" + SwissKnife.sqlDecode( queryDataSet.getString("prdId") ) + "]]></link>");

      String prd_img = "";
      if (SwissKnife.fileExists(wwwrootPath + "/prd_images/" + SwissKnife.sqlDecode( queryDataSet.getString("prdId") ) + "-1.jpg")) {
        prd_img = "prd_images/" + SwissKnife.sqlDecode( queryDataSet.getString("prdId") ) + "-1.jpg";

        out.println("<image><![CDATA[" + server + prd_img + "]]></image>");
      }
      else out.println("<image></image>");

      out.println("<price_with_vat>" + PriceChecker.calcPrd(one,queryDataSet,gr.softways.dev.eshop.eways.Customer.CUSTOMER_TYPE_RETAIL,PriceChecker.isOffer(queryDataSet,gr.softways.dev.eshop.eways.Customer.CUSTOMER_TYPE_RETAIL),zero).getUnitGrossCurr1().setScale(2,BigDecimal.ROUND_HALF_UP) + "</price_with_vat>");

      String s_instock = "", s_availability = "";
      String prdAvailability = SwissKnife.sqlDecode( queryDataSet.getString("prdAvailability") );
      if ("1".equals(prdAvailability)) {
        s_instock = "Y";
        s_availability = "�� �������";
      }
      else {
        s_instock = "N";
        
        if ("2".equals(prdAvailability)) s_availability = "1 ��� 3 ������";
        else if ("3".equals(prdAvailability)) s_availability = "4 ��� 7 ������";
        else if ("4".equals(prdAvailability)) s_availability = "7+ ������";
        else if ("5".equals(prdAvailability)) s_availability = "������� �����������";
        else if ("6".equals(prdAvailability)) s_availability = "���-����������";
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

      out.println("</product>");

      queryDataSet.next();
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