package gr.softways.dev.eshop.orders.v2.chart;

import gr.softways.dev.eshop.eways.Customer;
import gr.softways.dev.jdbc.Database;
import gr.softways.dev.jdbc.Load;
import gr.softways.dev.jdbc.MetaDataUpdate;
import gr.softways.dev.jdbc.QueryDataSet;
import gr.softways.dev.jdbc.QueryDescriptor;
import gr.softways.dev.util.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.*;
import javax.servlet.http.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;


public class ChartServlet extends HttpServlet {

  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    charset = SwissKnife.jndiLookup("swconf/charset");
    if (charset == null) {
      charset = SwissKnife.DEFAULT_CHARSET;
    }
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException, IOException {
    request.setCharacterEncoding(charset);

    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername", request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword", request);

    int auth = director.auth(databaseId,authUsername,authPassword,"orders",Director.AUTH_DELETE);

    if (auth != Director.AUTH_OK) {
      throw new ServletException();
    }
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1");
    
    try {
      if (action.equals("chartdata")) {
        chartData(request, response);
      }
      else {
        throw new Exception();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new ServletException();
    }
  }
  
  private DbRet chartData(HttpServletRequest request, HttpServletResponse response) throws Exception {
    DbRet dbRet = new DbRet();
    
    response.setContentType("application/json; charset=" + charset);
    
    PrintWriter out = null;
    
    JSONObject json = new JSONObject();
    JSONArray series = new JSONArray();
    
    JSONObject serie = null;
    JSONArray data = null;
    
    DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yyyy"),
        ftmFriendly = DateTimeFormat.forPattern("E, dd MMM yyyy");
    
    LocalDate from = fmt.parseLocalDate(request.getParameter("from"));
    LocalDate to = fmt.parseLocalDate(request.getParameter("to"));
    
    LocalDate compare_from = null, compare_to = null;
    try {
      compare_from = fmt.parseLocalDate(request.getParameter("compare_from"));
      compare_to = fmt.parseLocalDate(request.getParameter("compare_to"));
    }
    catch (Exception e) {
      compare_from = null;
      compare_to = null;
    }
    
    String compare = request.getParameter("compare"),
        chartConcept = request.getParameter("chartConcept");
    
    List<Order> orders = findByDates(from, to);
    
    serie = new JSONObject();
    data = new JSONArray();
    for (Order order : orders) {
      JSONArray dataEntry = new JSONArray();
      
      if ("1".equals(chartConcept)) {
        dataEntry.put(order.getOrderDate().toString(ftmFriendly));
        dataEntry.put(order.getValue());
        
        data.put(dataEntry);
      }
      else if ("2".equals(chartConcept)) {
        dataEntry.put(order.getOrderDate().toString(ftmFriendly));
        dataEntry.put(order.getOrders());
        
        data.put(dataEntry);
      }
    }
    serie.put("name", from.toString(fmt) + " - " + to.toString(fmt));
    serie.put("data", data);
    serie.put("pointStart", new DateTime(from.getYear(),
          from.getMonthOfYear(),
          from.getDayOfMonth(),
          0,
          0,
          DateTimeZone.UTC).getMillis());
    series.put(serie);
    
    if (compare != null && "1".equals(compare) && compare_from != null && compare_to != null) {
      compare = "1";
      orders = findByDates(compare_from, compare_to);
      
      serie = new JSONObject();
      data = new JSONArray();
      for (Order order : orders) {
        JSONArray dataEntry = new JSONArray();
        
        if ("1".equals(chartConcept)) {
          dataEntry.put(order.getOrderDate().toString(ftmFriendly));
          dataEntry.put(order.getValue());
          
          data.put(dataEntry);
        }
        else if ("2".equals(chartConcept)) {
          dataEntry.put(order.getOrderDate().toString(ftmFriendly));
          dataEntry.put(order.getOrders());
          
          data.put(dataEntry);
        }
      }
      serie.put("name", compare_from.toString(fmt) + " - " + compare_to.toString(fmt));
      serie.put("data", data);
      serie.put("pointStart", new DateTime(from.getYear(),
          from.getMonthOfYear(),
          from.getDayOfMonth(),
          0,
          0,
          DateTimeZone.UTC).getMillis());
      series.put(serie);
    }
    
    json.put("series", series);
    
    String yAxisTitle = "";
    if ("1".equals(chartConcept)) {
      yAxisTitle = "Τζίρος";
    }
    else {
      yAxisTitle = "Παραγγελίες";
    }
    json.put("yAxisTitle", yAxisTitle);
    
    JSONArray overviews = new JSONArray();
    
    JSONObject overview = null;
    
    Order order1 = findSumByDates(from, to),
        order2 = null;
    
    if ("1".equals(compare)) order2 = findSumByDates(compare_from, compare_to);
    
    overview = new JSONObject();
    overview.put("title", "Τζίρος");
    overview.put("total1", "€" + SwissKnife.formatNumber( order1.getValue(),"el","gr",2,2 ));
    if ("1".equals(compare)) {
      overview.put("total2", "€" + SwissKnife.formatNumber( order2.getValue(),"el","gr",2,2 ));
      overview.put("total_dif_pct", order1.getValue().divide(order2.getValue(), 4, RoundingMode.HALF_UP).subtract(one).multiply(oneHundred));
    }
    overviews.put(overview);
    
    overview = new JSONObject();
    overview.put("title", "Παραγγελίες");
    overview.put("total1", SwissKnife.formatNumber( order1.getOrders(),"el","gr",0,0 ));
    if ("1".equals(compare)) {
      overview.put("total2", SwissKnife.formatNumber( order2.getOrders(),"el","gr",0,0 ));
      
      BigDecimal rcpts1 = new BigDecimal(order1.getOrders()),
          rcpts2 = new BigDecimal(order2.getOrders());
      
      overview.put("total_dif_pct", rcpts1.divide(rcpts2, 4, RoundingMode.HALF_UP).subtract(one).multiply(oneHundred));
    }
    overviews.put(overview);
    
    json.put("overviews", overviews);
    
    out = response.getWriter();
    json.write(out);
      
    out.close();
      
    return dbRet;
  }
  
  private static List<Order> findByDates(LocalDate from, LocalDate to) {
    List<Order> orders = new ArrayList<Order>();
    
    Database database = null;
    QueryDataSet queryDataSet = null;
    
    DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
    
    String sql = "SELECT CAST(orderDate AS DATE) AS ordersDates,sum(valueEU + vatValEU) AS totalValue, COUNT(orderId) AS totalOrders FROM orders"
        + " WHERE status NOT IN ('" + gr.softways.dev.eshop.eways.Order.STATUS_CANCELED + "')"
        + " AND CAST(orderDate AS DATE) >= '" + from.toString(fmt) + "' AND CAST(orderDate AS DATE) <= '" + to.toString(fmt) + "'"
        + " GROUP BY ordersDates"
        + " ORDER BY ordersDates ASC";
    
    database = director.getDBConnection(databaseId);
    
    try {
      queryDataSet = new QueryDataSet();
      queryDataSet.setQuery(new QueryDescriptor(database,sql,null,true,Load.UNCACHED));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();

      while (queryDataSet.inBounds() == true) {
        Order order = new Order();

        order.setOrderDate(new LocalDate(queryDataSet.getDate("ordersDates")));
        order.setValue(queryDataSet.getBigDecimal("totalValue"));
        order.setOrders(queryDataSet.getInt("totalOrders"));

        orders.add(order);
        
        queryDataSet.next();
      }
    }
    finally {
      if (queryDataSet != null) queryDataSet.close();
      
      director.freeDBConnection(databaseId, database);
    }
    
    return orders;
  }
  
  private static Order findSumByDates(LocalDate from, LocalDate to) {
    Database database = null;
    QueryDataSet queryDataSet = null;
    
    DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
    
    Order order = new Order();
    
    String sql = "SELECT SUM(valueEU + vatValEU) AS totalValue, COUNT(orderId) AS totalOrders FROM orders"
        + " WHERE status NOT IN ('" + gr.softways.dev.eshop.eways.Order.STATUS_CANCELED + "')"
        + " AND CAST(orderDate AS DATE) >= '" + from.toString(fmt) + "' AND CAST(orderDate AS DATE) <= '" + to.toString(fmt) + "'";
    
    database = director.getDBConnection(databaseId);
    
    try {
      queryDataSet = new QueryDataSet();
      queryDataSet.setQuery(new QueryDescriptor(database,sql,null,true,Load.UNCACHED));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();

      if (queryDataSet.getBigDecimal("totalValue") != null) order.setValue(queryDataSet.getBigDecimal("totalValue"));
      else order.setValue(zero);
      order.setOrders(queryDataSet.getInt("totalOrders"));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (queryDataSet != null) queryDataSet.close();
      
      director.freeDBConnection(databaseId, database);
    }
    
    return order;
  }
  
  private String charset;
  
  private static Director director = Director.getInstance();
  
  private static final String databaseId = SwissKnife.jndiLookup("swconf/databaseId");
  
  private static BigDecimal zero = new BigDecimal("0.00"),
        oneHundred = new BigDecimal("100.00"),
        one = new BigDecimal("1.00");
}