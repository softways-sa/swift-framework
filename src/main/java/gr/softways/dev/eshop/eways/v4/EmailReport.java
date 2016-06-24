package gr.softways.dev.eshop.eways.v4;

import java.util.Hashtable;
import java.util.Vector;
import java.math.BigDecimal;

import gr.softways.dev.util.*;

import gr.softways.dev.eshop.eways.v2.Product;
import gr.softways.dev.eshop.eways.v2.PrdPrice;
import gr.softways.dev.eshop.eways.v2.TotalPrice;
import gr.softways.dev.eshop.eways.v2.ProductAttribute;
import gr.softways.dev.eshop.product.v2.ProductOptionsValue;

/**
 *
 * @author  Administrator
 * @version 
 */
public class EmailReport extends JSPBean {
  private BigDecimal _zero = new BigDecimal("0");
  
  static Hashtable lb = new Hashtable();
  
  static {
    lb.put("subject", "Η παραγγελία σας από το");
    lb.put("subjectLG", "Your order from");
    
    lb.put("orderId", "Κωδ. Παραγγελίας: ");
    lb.put("orderIdLG", "Order ID: ");
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
    
    lb.put("paytype","Τρόπος πληρωμής: ");
    lb.put("paytypeLG","Payment method: ");
    
    lb.put("PAY_TYPE_CREDIT_CARD","Πιστωτική κάρτα");
    lb.put("PAY_TYPE_CREDIT_CARDLG","Credit card");
    lb.put("PAY_TYPE_ON_DELIVERY","Αντικαταβολή");
    lb.put("PAY_TYPE_ON_DELIVERYLG","On delivery");
    lb.put("PAY_TYPE_DEPOSIT","Κατάθεση σε τραπεζικό λογαριασμό");
    lb.put("PAY_TYPE_DEPOSITLG","Bank deposit");
    lb.put("PAY_TYPE_PAYPAL","PayPal");
    lb.put("PAY_TYPE_PAYPALLG","PayPal");
  }
  
  public EmailReport() {
  }

  public DbRet sendClientReport(Customer customer) {
    DbRet dbRet = new DbRet();

    String[] values = Configuration.getValues(new String[] {"shopName","smtpServer","shopEmailFrom"});
    String subject = lb.get("subject" + customer.getCustLang()).toString() + " " + values[0],
        smtpServer = values[1],
        eshopSalesEmail = values[2];

    EMail email = new EMail(customer.getEmail(),eshopSalesEmail,subject,"",smtpServer,"text/plain","UTF-8",null);

    email.setBody( createCustomerReport(customer) );

    boolean sent = SendMail.sendMessage(email);

    if (sent == false) {
     dbRet.setNoError(0);
    }

    return dbRet;
  }
  
  protected String createCustomerReport(Customer customer) {
    StringBuffer body = new StringBuffer();
    
    String ls = "\r\n";
    
    Order order = customer.getOrder();
    
    Product product = null;

    PrdPrice prdPrice = null;

    ProductOptionsValue productOptionsValue = null;
    
    order = customer.getOrder();
    
    String lang = customer.getCustLang();

    body.append(lb.get("orderId" + lang).toString() + order.getOrderId() + ls + ls);
    
    body.append(lb.get("customerData" + lang).toString() + ls);
    body.append(customer.getFirstname() + " " + customer.getLastname() + ls);
    body.append(customer.getBillingAddress() + ls);
    body.append(customer.getBillingZipCode() + " " + customer.getBillingCity() + ls);
    body.append(customer.getBillingCountry() + ls);
    if (customer.getBillingPhone().length() > 0) {
      body.append(customer.getBillingPhone() + ls);
    }
    if (customer.getBillingName().length()>0 && customer.getBillingAfm().length()>0) {
      body.append(customer.getBillingName() + ls);
      body.append(customer.getBillingAfm() + ls);
      if (customer.getBillingDoy().length()>0) body.append(customer.getBillingDoy() + ls);
      if (customer.getBillingProfession().length()>0) body.append(customer.getBillingProfession() + ls);
    }
    
    if (customer.getOrdPrefNotes() != null && customer.getOrdPrefNotes().length()>0) {
      body.append(ls + lb.get("extraDetails" + lang).toString() + ls);
      body.append(customer.getOrdPrefNotes() + ls);
    }
    
    body.append(ls);
    
    body.append(lb.get("shipAddress" + lang).toString() + ls);
    body.append(customer.getShippingName() + ls);
    body.append(customer.getShippingAddress() + ls);
    body.append(customer.getShippingZipCode() + " " + customer.getShippingCity() + ls);
    body.append(customer.getShippingCountry() + ls);
    body.append(customer.getShippingPhone() + ls);
    
    body.append(ls);
    
    body.append(lb.get("prds" + lang).toString() + ls);
    int orderLines = order.getOrderLines();
    
    Vector PAVector = null;
    ProductAttribute pa = null;
    
    for (int i=0; i<orderLines; i++) {
      product = order.getProductAt(i);
      prdPrice = product.getPrdPrice();
      productOptionsValue = product.getProductOptionsValue();
      
      PAVector = product.getPAVector();
      
      body.append(product.getQuantity());
      body.append(" x '" + product.getPrdName());
      if (productOptionsValue != null) {
        body.append(" - " + productOptionsValue.getValue("PO_Name" + lang));
      }
      for (int j=0; j<PAVector.size(); j++) {
        pa = (ProductAttribute)PAVector.elementAt(j);
        body.append(" - " + pa.getAtrName() + ": " + pa.getATVAValue());
      }
      
      body.append("' + " + lb.get("vatWord" + lang).toString() + " = ");
      body.append(SwissKnife.formatNumber(prdPrice.getTotalGrossCurr1(),customer.getLocaleLanguage(),customer.getLocaleCountry(),customer.getMinCurr1DispFractionDigits(),customer.getCurr1DisplayScale()) + " EURO" + ls);
    }
    
    TotalPrice orderPrice = order.getOrderPrice();
    
    TotalPrice shippingPrice = order.getShippingPrice();

    if (shippingPrice.getGrossCurr1().compareTo(_zero) > 0) {
      body.append(ls);
      body.append(lb.get("shipCost" + lang).toString() + SwissKnife.formatNumber(shippingPrice.getGrossCurr1().setScale(customer.getCurr1DisplayScale(), BigDecimal.ROUND_HALF_UP),customer.getLocaleLanguage(),customer.getLocaleCountry(),customer.getMinCurr1DispFractionDigits(),customer.getCurr1DisplayScale()) + " EURO");
    }
    
    body.append(ls);
    body.append(lb.get("totalOrderPrice" + lang).toString() + SwissKnife.formatNumber(orderPrice.getGrossCurr1().add(shippingPrice.getGrossCurr1()).setScale(customer.getCurr1DisplayScale(), BigDecimal.ROUND_HALF_UP),customer.getLocaleLanguage(),customer.getLocaleCountry(),customer.getMinCurr1DispFractionDigits(),customer.getCurr1DisplayScale()) + " EURO");
    
    body.append(ls + ls);
    body.append(lb.get("thankYou" + lang).toString());
    
    return body.toString();
  }
}