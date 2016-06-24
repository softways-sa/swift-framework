package gr.softways.dev.eshop.orders;

import gr.softways.dev.eshop.eways.v2.Order;
import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;
import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import jxl.*;
import jxl.write.*;

public class ExportServlet extends HttpServlet {

  private Director _director;

  private String _charset = null;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    
    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    _director = Director.getInstance();
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request,response);
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    
    String action = SwissKnife.grEncode(request.getParameter("action1")),
           databaseId = SwissKnife.grEncode(request.getParameter("databaseId"));

    DbRet dbRet = new DbRet();

    if (databaseId.equals("")) {
      dbRet.setNoError(0);
    }
    else if (action.equals("ASCII")) {
    }
    else if (action.equals("EXCEL")) {
      dbRet = doExportEXCEL(request, response, databaseId);
    }
    else {
      dbRet.setNoError(0);
    }
  }
  
  private DbRet doExportEXCEL(HttpServletRequest request,HttpServletResponse response,String databaseId) {
    response.setContentType("application/vnd.ms-excel; charset=" + _charset);
    
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"orders",Director.AUTH_UPDATE);

    DbRet dbRet = new DbRet();
    
    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      return dbRet;
    }
    
    String query = buildQueryString(request);
    
    ServletOutputStream out = null;
    
    Timestamp currentDate = SwissKnife.currentDate();
    
    String promptFilename = "orders_" + SwissKnife.formatDate(currentDate,"dd-MM-yyyy-HHmm");
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
      sheet = workbook.createSheet("orders",0);
      
      int excelRow = 0, index = -1;
      
      sheet.addCell(new Label(++index, excelRow, "κωδικός παραγγελίας"));
      sheet.addCell(new Label(++index, excelRow, "ημ/νία"));
      sheet.addCell(new Label(++index, excelRow, "κατάσταση"));
      sheet.addCell(new Label(++index, excelRow, "τρόπος πληρωμής"));
      
      sheet.addCell(new Label(++index, excelRow, "κωδικός πελάτη"));
      sheet.addCell(new Label(++index, excelRow, "email πελάτη"));
      sheet.addCell(new Label(++index, excelRow, "όνομα πελάτη"));
      sheet.addCell(new Label(++index, excelRow, "επώνυμο πελάτη"));
      sheet.addCell(new Label(++index, excelRow, "δ/νση πελάτη"));
      sheet.addCell(new Label(++index, excelRow, "τ.κ. πελάτη"));
      sheet.addCell(new Label(++index, excelRow, "πόλη πελάτη"));
      sheet.addCell(new Label(++index, excelRow, "χώρα πελάτη"));
      sheet.addCell(new Label(++index, excelRow, "τηλέφωνο πελάτη"));
      sheet.addCell(new Label(++index, excelRow, "εταιρία"));
      sheet.addCell(new Label(++index, excelRow, "Α.Φ.Μ."));
      sheet.addCell(new Label(++index, excelRow, "Δ.Ο.Υ."));
      sheet.addCell(new Label(++index, excelRow, "δραστηριότητα"));
      
      sheet.addCell(new Label(++index, excelRow, "κωδικός είδους"));
      sheet.addCell(new Label(++index, excelRow, "ποσότητα είδους"));
      sheet.addCell(new Label(++index, excelRow, "αξία μονάδος προ ΦΠΑ"));
      sheet.addCell(new Label(++index, excelRow, "αξία συνόλου προ ΦΠΑ"));
      
      sheet.addCell(new Label(++index, excelRow, "έξοδα αποστολής προ ΦΠΑ"));
      
      String orderId = null, status = null, payType = null;
      
      DateFormat dateFormat = new DateFormat("dd/mm/yyyy hh:mm:ss");
      WritableCellFormat dateCellFormat = new WritableCellFormat(dateFormat);
      
      Calendar cal = Calendar.getInstance();
      
      queryDataSet = new QueryDataSet();
      
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.UNCACHED));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();
      
      while (queryDataSet.inBounds() == true) {
        excelRow++;
        
        if (excelRow > 1 && !queryDataSet.getString("orderId").equals(orderId)) {
          // skip one row
          excelRow++;
        }
        
        index = -1;
        
        status = queryDataSet.getString("status");
        if (status.equals(Order.STATUS_PENDING)) status = "Εκκρεμεί";
        else if (status.equals(Order.STATUS_PENDING_PAYMENT)) status = "Εκκρεμεί επαλήθευση πληρωμής";
        else if (status.equals(Order.STATUS_SHIPPED)) status = "Απεστάλη";
        else if (status.equals(Order.STATUS_COMPLETED)) status = "Διεκπεραιωμένη";
        else if (status.equals(Order.STATUS_CANCELED)) status = "Ακυρώθηκε";
        else if (status.equals(Order.STATUS_PENDING_DEPOSIT)) status = "Εκκρεμεί κατάθεση";
        else if (status.equals(Order.STATUS_PENDING_PAYPAL)) status = "Εκκρεμεί PayPal πληρωμή";
        
        payType = queryDataSet.getString("ordPayWay");
        if (payType.equals(Order.PAY_TYPE_CREDIT_CARD)) {
          payType = "ΠΙΣΤΩΤΙΚΗ ΚΑΡΤΑ";
        }
        else if (payType.equals(Order.PAY_TYPE_DEPOSIT)) {
          payType = "ΚΑΤΑΘΕΣΗ";
        }
        else if (payType.equals(Order.PAY_TYPE_ON_DELIVERY)) {
          payType = "ΑΝΤΙΚΑΤΑΒΟΛΗ";
        }
        else if (payType.equals(Order.PAY_TYPE_PAYPAL)) {
          payType = "PayPal";
        }
        else if (payType.equals(Order.PAY_TYPE_CASH)) {
          payType = "ΜΕΤΡΗΤΑ";
        }
        
        sheet.addCell(new Label(++index, excelRow, SwissKnife.sqlDecode(queryDataSet.getString("orderId"))));
        
        cal.setTimeInMillis(queryDataSet.getTimestamp("orderDate").getTime());
        sheet.addCell(new DateTime(++index, excelRow, cal.getTime(), dateCellFormat));
        
        sheet.addCell(new Label(++index, excelRow, status));
        sheet.addCell(new Label(++index, excelRow, payType));
        
        sheet.addCell(new Label(++index, excelRow, SwissKnife.sqlDecode(queryDataSet.getString("customerId"))));
        sheet.addCell(new Label(++index, excelRow, SwissKnife.sqlDecode(queryDataSet.getString("email"))));
        sheet.addCell(new Label(++index, excelRow, SwissKnife.sqlDecode(queryDataSet.getString("firstname"))));
        sheet.addCell(new Label(++index, excelRow, SwissKnife.sqlDecode(queryDataSet.getString("lastname"))));
        sheet.addCell(new Label(++index, excelRow, SwissKnife.sqlDecode(queryDataSet.getString("billingAddress"))));
        sheet.addCell(new Label(++index, excelRow, SwissKnife.sqlDecode(queryDataSet.getString("billingZipCode"))));
        sheet.addCell(new Label(++index, excelRow, SwissKnife.sqlDecode(queryDataSet.getString("billingCity"))));
        sheet.addCell(new Label(++index, excelRow, SwissKnife.sqlDecode(queryDataSet.getString("billingCountry"))));
        sheet.addCell(new Label(++index, excelRow, SwissKnife.sqlDecode(queryDataSet.getString("billingPhone"))));
        sheet.addCell(new Label(++index, excelRow, SwissKnife.sqlDecode(queryDataSet.getString("companyName"))));
        sheet.addCell(new Label(++index, excelRow, SwissKnife.sqlDecode(queryDataSet.getString("afm"))));
        sheet.addCell(new Label(++index, excelRow, SwissKnife.sqlDecode(queryDataSet.getString("doy"))));
        sheet.addCell(new Label(++index, excelRow, SwissKnife.sqlDecode(queryDataSet.getString("profession"))));
        
        sheet.addCell(new Label(++index, excelRow, SwissKnife.sqlDecode(queryDataSet.getString("prdId"))));
        sheet.addCell(new jxl.write.Number(++index, excelRow, queryDataSet.getBigDecimal("quantity1").setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue()));
        sheet.addCell(new jxl.write.Number(++index, excelRow, queryDataSet.getBigDecimal("unitPrcEU").setScale(curr1Scale, BigDecimal.ROUND_HALF_UP).doubleValue()));
        sheet.addCell(new jxl.write.Number(++index, excelRow, queryDataSet.getBigDecimal("valueEU1").setScale(curr1Scale, BigDecimal.ROUND_HALF_UP).doubleValue()));
        
        sheet.addCell(new jxl.write.Number(++index, excelRow, queryDataSet.getBigDecimal("shippingValueEU").doubleValue()));
        
        orderId = queryDataSet.getString("orderId");
        
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
  
  private String buildQueryString(HttpServletRequest request) {
    String query = "", orderId = "", email = "";
    
    String[] orderStatus = null;
    
    if ((orderId = request.getParameter("orderId")) == null) orderId = "";
    if ((email = request.getParameter("email")) == null) email = "";
    
    orderStatus = request.getParameterValues("orderStatus");
    
    Timestamp orderDateApo = SwissKnife.buildTimestamp(request.getParameter("orderDateDayApo"),request.getParameter("orderDateMonthApo"),request.getParameter("orderDateYearApo"), "0", "0", "0", "0"),
        orderDateEos = SwissKnife.buildTimestamp(request.getParameter("orderDateDayEos"),request.getParameter("orderDateMonthEos"),request.getParameter("orderDateYearEos"), "0", "0", "0", "0");
        
    query = "SELECT orders.*,transactions.*,customer.customerId,customer.email FROM orders,transactions,customer WHERE orders.orderId = transactions.orderId AND orders.customerId = customer.customerId";
    
    if (orderStatus != null && orderStatus.length > 0) {
      query += " AND (";
      
      for (int i=0; i<orderStatus.length; i++) {
        query += "orders.status = '" + SwissKnife.sqlEncode(orderStatus[i]) + "'";
        
        if ((i+1) < orderStatus.length) query += " OR ";
      }
      
      query += ")";
    }
    
    if (orderId.length()>0) {
      query += " AND orders.orderId = '" + SwissKnife.sqlEncode(orderId) + "'";
    }
    
    if (orderDateApo != null) {
      query += " AND orders.orderDate >= '" + orderDateApo + "'";
    }
    
    if (orderDateEos != null) {
      query += " AND orders.orderDate < '" + orderDateEos + "'";
    }
    
    if (email.length()>0) {
      query += " AND orders.email LIKE '%" + SwissKnife.sqlEncode(email) + "%'";
    }
    
    query += " ORDER BY orders.orderId, transactions.transId ASC";
    
    return query;
  }
  
  private BigDecimal _zero = new BigDecimal("0");
  
  private int curr1Scale = Integer.parseInt(SwissKnife.jndiLookup("swconf/curr1Scale"));
}