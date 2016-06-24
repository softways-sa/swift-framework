package gr.softways.dev.eshop.eways.v2;

public abstract class Order {
  public static String PAY_TYPE_CREDIT_CARD = "1";
  public static String PAY_TYPE_ON_DELIVERY = "2";
  public static String PAY_TYPE_DEPOSIT = "8";
  public static String PAY_TYPE_PAYPAL = "9";
  public static String PAY_TYPE_CREDIT = "7";
  public static String PAY_TYPE_CASH = "6";
  public static String PAY_TYPE_VIVA = "5";

  public static String DOCUMENT_TYPE_RECEIPT = "1";
  public static String DOCUMENT_TYPE_INVOICE = "2";
  
  public static String STATUS_PENDING = "1";
  public static String STATUS_COMPLETED = "5";
  public static String STATUS_CANCELED = "7";
  public static String STATUS_SHIPPED = "3";
  public static String STATUS_PENDING_DEPOSIT = "6";
  public static String STATUS_PENDING_PAYPAL = "8";
  public static String STATUS_HANDLING = "2";
  public static String STATUS_PENDING_CC = "9";
  public static String STATUS_PENDING_PAYMENT = "4";
  
  public static String STOCK_INVENTORY = "1";
  public static String PROGRESSIVE_INVENTORY = "2";
}
