package gr.softways.dev.eshop.eways;

public abstract class Order {

  public static String PAY_TYPE_CREDIT_CARD = "1";
  public static String PAY_TYPE_ON_DELIVERY = "2";

  public static String DOCUMENT_TYPE_RECEIPT = "1";
  public static String DOCUMENT_TYPE_INVOICE = "2";
  
  public static String STATUS_PENDING = "1";
  public static String STATUS_COMPLETED = "5";
  public static String STATUS_CANCELED = "7";
}