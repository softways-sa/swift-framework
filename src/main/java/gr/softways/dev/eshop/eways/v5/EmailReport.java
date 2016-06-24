package gr.softways.dev.eshop.eways.v5;

import java.util.Hashtable;
import java.math.BigDecimal;

import gr.softways.dev.util.*;

import gr.softways.dev.eshop.eways.v2.Order;
    
/**
 *
 * @author  Administrator
 * @version 
 */
public class EmailReport extends JSPBean {
  private BigDecimal _zero = new BigDecimal("0");
  
  static Hashtable lb = new Hashtable();
  
  String[] values = null;
  
  static {
    lb.put("subject", "Η παραγγελία σας από το");
    lb.put("subjectLG", "Your order from");
    
    lb.put("orderId", "Κωδ. Παραγγελίας: ");
    lb.put("orderIdLG", "Order ID: ");
    lb.put("date", "Ημ/νία: ");
    lb.put("dateLG", "Date: ");
    
    lb.put("customerData", "ΣΤΟΙΧΕΙΑ ΧΡΕΩΣΗΣ");
    lb.put("customerDataLG", "BILLING ADDRESS");
    lb.put("shipAddress", "ΣΤΟΙΧΕΙΑ ΑΠΟΣΤΟΛΗΣ");
    lb.put("shipAddressLG", "SHIPPING ADDRESS");
    
    lb.put("prds", "ΠΡΟΪΟΝΤΑ");
    lb.put("prdsLG", "ITEMS");
    
    lb.put("vatWord", "ΦΠΑ");
    lb.put("vatWordLG", "VAT");
    
    lb.put("totalPrds", "Μερικό Σύνολο: ");
    lb.put("totalPrdsLG", "Subtotal: ");
    lb.put("shipCost","Έξοδα αποστολής: ");
    lb.put("shipCostLG","Shipping: ");
    lb.put("totalOrderPrice","Σύνολο: ");
    lb.put("totalOrderPriceLG","Order Total: ");
    
    lb.put("extraDetails","Προτιμήσεις/Παρατηρήσεις");
    lb.put("extraDetailsLG","Preferences/Notes");
    
    lb.put("thankYou", "Σας ευχαριστούμε που επιλέξατε το ηλεκτρονικό μας κατάστημα.");
    lb.put("thankYouLG", "Thank you for choosing us.");
    
    lb.put("giftWrap", "συσκευασία δώρου");
    lb.put("giftWrapLG", "gift wrap");
    
    lb.put("paytype","Τρόπος πληρωμής:");
    lb.put("paytypeLG","Payment method:");
    
    lb.put("shipMethod","Τρόπος αποστολής:");
    lb.put("shipMethodLG","Shipping:");
    
    lb.put("paytype." + Order.PAY_TYPE_CREDIT_CARD,"Πιστωτική κάρτα");
    lb.put("paytype." + Order.PAY_TYPE_CREDIT_CARD + "LG","Credit card");
    
    lb.put("paytype." + Order.PAY_TYPE_ON_DELIVERY,"Αντικαταβολή");
    lb.put("paytype." + Order.PAY_TYPE_ON_DELIVERY + "LG","On delivery");
    
    lb.put("paytype." + Order.PAY_TYPE_DEPOSIT,"Κατάθεση σε τραπεζικό λογαριασμό");
    lb.put("paytype." + Order.PAY_TYPE_DEPOSIT + "LG","Bank deposit");
    
    lb.put("paytype." + Order.PAY_TYPE_PAYPAL,"PayPal");
    lb.put("paytype." + Order.PAY_TYPE_PAYPAL + "LG","PayPal");
    
    lb.put("paytype." + Order.PAY_TYPE_VIVA,"VIVA");
    lb.put("paytype." + Order.PAY_TYPE_VIVA + "LG","VIVA");
    
    lb.put("paytype." + Order.PAY_TYPE_CASH,"Μετρητά");
    lb.put("paytype." + Order.PAY_TYPE_CASH + "LG","Cash");
  }
  
  public EmailReport() {
    values = Configuration.getValues(new String[] {"shopName","smtpServer","shopEmailFrom","shopEmailTo"});
    
    String tmp = SwissKnife.jndiLookup("swconf/emailReportCurrSymbol");
    if (tmp != null && tmp.length() > 0) currSymbol = tmp;
  }

  public DbRet sendClientReport(String orderId) {
    DbRet dbRet = new DbRet();
    
    StringBuffer body = new StringBuffer();
    
    String ls = "\r\n";

    int rows = 0;
    
    String smtpServer = values[1],
        eshopSalesEmail = values[2];
    
    SQLHelper2 helperBean = new SQLHelper2();
    helperBean.initBean(_databaseId, null, null, null, null);
    
    dbRet = helperBean.getSQL("SELECT orders.*,transactions.*,product.name,product.nameLG,product.nameLG1,product.nameLG2 FROM orders,transactions,product WHERE orders.orderId = transactions.orderId AND product.prdId = transactions.prdId AND orders.orderId = '" + SwissKnife.sqlEncode(orderId) + "'");

    rows = dbRet.getRetInt();
    
    if (dbRet.getNoError() == 0 || rows <= 0) {
      dbRet.setNoError(0);
      helperBean.closeResources();
     
      dbRet.setNoError(0);
      return dbRet;
    }
    
    String lang = helperBean.getColumn("ordLang");
    
    String subject = lb.get("subject" + lang).toString() + " " + values[0];
    
    EMail email = new EMail(helperBean.getColumn("email"),eshopSalesEmail,subject,"",smtpServer,"text/plain","UTF-8",null);
    
    String localeLanguage = "el", localeCountry = "GR";
    if (lang.equals("LG")) {
        localeLanguage = "en";
        localeCountry = "UK";
    }

    body.append(lb.get("orderId" + lang).toString() + orderId + ls + ls);
    
    body.append(lb.get("date" + lang).toString() + SwissKnife.formatDate(helperBean.getTimestamp("orderDate"), "dd/MM/yyyy") + ls + ls);
    
    body.append(lb.get("customerData" + lang).toString() + ls);
    body.append(helperBean.getColumn("firstname") + " " + helperBean.getColumn("lastname") + ls);
    body.append(helperBean.getColumn("billingAddress") + ls);
    body.append(helperBean.getColumn("billingZipCode") + " " + helperBean.getColumn("billingCity") + ls);
    body.append(helperBean.getColumn("billingCountry") + ls);
    if (helperBean.getColumn("billingPhone").length() > 0) {
      body.append(helperBean.getColumn("billingPhone") + ls);
    }
    if (helperBean.getColumn("companyName").length()>0 && helperBean.getColumn("afm").length()>0) {
      body.append(helperBean.getColumn("companyName") + ls);
      body.append(helperBean.getColumn("afm") + ls);
      if (helperBean.getColumn("doy").length()>0) body.append(helperBean.getColumn("doy") + ls);
      if (helperBean.getColumn("profession").length()>0) body.append(helperBean.getColumn("profession") + ls);
    }
    
    if (helperBean.getColumn("ordPrefNotes").length()>0) {
      body.append(ls + lb.get("extraDetails" + lang).toString() + ls);
      body.append(helperBean.getColumn("ordPrefNotes") + ls);
    }
    
    body.append(ls);
    
    body.append(lb.get("shipAddress" + lang).toString() + ls);
    body.append(helperBean.getColumn("shippingName") + ls);
    body.append(helperBean.getColumn("shippingAddress") + ls);
    body.append(helperBean.getColumn("shippingZipCode") + " " + helperBean.getColumn("shippingCity") + ls);
    body.append(helperBean.getColumn("shippingCountry") + ls);
    if (helperBean.getColumn("shippingPhone").length() > 0) body.append(helperBean.getColumn("shippingPhone") + ls);
    
    body.append(ls);
    
    body.append(lb.get("prds" + lang).toString() + ls);
    
    while (helperBean.inBounds() == true) {
      body.append(SwissKnife.formatNumber(helperBean.getBig("quantity1"),"el","GR",0,0));
      body.append(" x '" + helperBean.getColumn("name" + lang));
      if (helperBean.getColumn("transPO_Name" + lang).length() > 0) {
        body.append(" - " + helperBean.getColumn("transPO_Name" + lang));
      }
      if ("1".equals(helperBean.getColumn("transPRD_GiftWrap"))) {
        body.append(" + " + lb.get("giftWrap" + lang).toString());
      }
      
      body.append("' + " + lb.get("vatWord" + lang).toString() + " = ");
      body.append(SwissKnife.formatNumber(helperBean.getBig("valueEU1").add(helperBean.getBig("vatValEU1")),localeLanguage,localeCountry,2,2) + " " + currSymbol + ls);
          
      helperBean.nextRow();
    }
    
    BigDecimal orderPrice = helperBean.getBig("valueEU").add( helperBean.getBig("vatValEU") ),
        shippingPrice = helperBean.getBig("shippingValueEU").add( helperBean.getBig("shippingVatValEU") );

    if (shippingPrice.compareTo(_zero) > 0) {
      body.append(ls);
      body.append(lb.get("shipCost" + lang).toString() + SwissKnife.formatNumber(shippingPrice,localeLanguage,localeCountry,2,2) + " " + currSymbol);
    }
    
    body.append(ls);
    body.append(lb.get("totalOrderPrice" + lang).toString() + SwissKnife.formatNumber(orderPrice.add(shippingPrice),localeLanguage,localeCountry,2,2) + " " + currSymbol);
    
    body.append(ls + ls);
    body.append(lb.get("paytype" + lang).toString() + " " + lb.get("paytype." + helperBean.getColumn("ordPayWay") + lang).toString());
    
    body.append(ls + ls);
    body.append(lb.get("shipMethod" + lang).toString() + " " + helperBean.getColumn("ord_ShipMethodTitle" + lang));
    
    dbRet = helperBean.getSQL("SELECT * FROM CMRow,CMCRelCMR WHERE CCCR_CMRCode = CMRCode AND CCCR_CMCCode = '0199'");
    rows = dbRet.getRetInt();
    
    if (rows > 0) {
      body.append(ls + ls);
      body.append(helperBean.getColumn("CMRText" + lang));
    }
    
    body.append(ls + ls);
    body.append(lb.get("thankYou" + lang).toString());
    
    email.setBody( body.toString() );

    boolean sent = SendMail.sendMessage(email);

    if (sent == false) dbRet.setNoError(0);
    else dbRet.setNoError(1);
    
    helperBean.closeResources();
        
    return dbRet;
  }
  
  // notify eshopAdminEmail about the order
  public DbRet sendAdminReport(String orderId) {
    DbRet dbRet = new DbRet();
    
    EMail email = null;
 
    String smtpServer = values[1],
        shopEmailFrom = values[2],
        shopEmailTo = values[3];
    
    email = new EMail(shopEmailTo,shopEmailFrom,
        "Παραγγελία στο ηλεκτρονικό κατάστημα",
        "Πραγματοποιήθηκε παραγγελία στο ηλεκτρονικό σας κατάστημα με κωδικό " + orderId + ".",
        smtpServer,"text/plain","ISO-8859-7",null);
    
    boolean sent = SendMail.sendMessage(email);

    if (sent == false) dbRet.setNoError(0);
    else dbRet.setNoError(1);
    
    return dbRet;
  }
  
  String _databaseId = SwissKnife.jndiLookup("swconf/databaseId");
  
  private String currSymbol = "EURO";
}