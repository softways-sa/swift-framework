package gr.softways.dev.eshop.eways.v4;

import java.util.Vector;
import java.util.Enumeration;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.servlet.http.*;

import gr.softways.dev.util.*;

import gr.softways.dev.eshop.eways.v2.Product;
import gr.softways.dev.eshop.eways.v2.PrdPrice;
import gr.softways.dev.eshop.eways.v2.TotalPrice;
import gr.softways.dev.eshop.eways.v2.ProductAttribute;
import gr.softways.dev.eshop.product.v2.ProductOptionsValue;

import gr.softways.dev.eshop.product.v2.Present;

public class Order {

  public Order(Customer customer) {
    _customer = customer;
    
    _inventoryType = SwissKnife.jndiLookup("swconf/inventoryType");
    if (_inventoryType == null || _inventoryType.equals("")) {
      System.err.println("[" + _customer.getDatabaseId() + "] _inventoryType not found in jndi lookup!!!");
    }
  }

  /**
   * Iterate the products to get the total order price. 
   * (no shipping included)
   *
   */
  public TotalPrice getOrderPrice() {
    TotalPrice orderPrice = new TotalPrice();
    
    Product product = null;

    int productCount = getOrderLines();
    
    for (int i=0; i<productCount; i++) {
      product = getProductAt(i);

      orderPrice.add(product.getPrdPrice());
    }
    
    return orderPrice;
  }
  
  public void setVATPct(BigDecimal vatPct) {
    _vatPct = vatPct;
    
    Product product = null;

    int productCount = getOrderLines();
    
    for (int i=0; i<productCount; i++) {
      getProductAt(i).setVATPct(vatPct);
    }
  }
  
  public void setDiscountPct(BigDecimal discountPct) {
    Product product = null;

    int productCount = getOrderLines();
    
    for (int i=0; i<productCount; i++) {
      getProductAt(i).setDiscountPct(discountPct);
    }
  }

  private TotalPrice _shipPrice = new TotalPrice();
  public TotalPrice getShippingPrice() {
    return _shipPrice;
  }
  public void setShippingPrice(TotalPrice shipPrice) {
    _shipPrice = shipPrice;
  }

  protected Vector _products = new Vector();

  public int getPrdOffersCount() {
    int counter = 0;

    Product product = null;

    Enumeration productEnumeration = getProductEnumeration();
    
    while (productEnumeration.hasMoreElements()) {
      product = (Product)productEnumeration.nextElement();
      
      if (product.isOffer() == true) {
        counter++;
      }
    }

    productEnumeration = null;

    return counter;
  }

  public void addProduct(Product product) {
    _products.addElement(product);
  }

  /**
   * Επιστρέφει το product με κωδικό prdId αν υπάρχει
   * ή null σε διαφορετική περίπτωση.
   */
  public Product existsProduct(String prdId, String PO_Code, String stamp) {
    if (prdId == null) return null;
    
    if (PO_Code == null) PO_Code = "";
    
    if (stamp == null) stamp = "";
    
    Product product = null;

    int productCount = _products.size();

    for (int i=0; i<productCount; i++) {
      product = (Product)_products.elementAt(i);

      if (prdId.equals(product.getPrdId()) && PO_Code.equals(product.getPO_Code())
            && stamp.equals(product.getStamp())) {
        break;
      }
      else product = null;
    }

    return product;
  }

  /**
   * Προσθήκη object (@param1 product) στον vector,
   * στη θέση που δείχνει ο index (@param1 index), διαγράφοντας
   * αυτό που υπάρχει σε αυτή τη θέση.
  */
  public boolean replaceProductAt(Product product, int index) {
    boolean replace = true;

    try {
      _products.setElementAt(product, index);
    }
    catch (ArrayIndexOutOfBoundsException e) {
      replace = false;
    }

    return replace;
  }

  /**
   * Επιστρέφει true αν πραγματικά διέγραψε το specified
   * product.
   */
  public boolean removeProductAt(int index) {
    boolean remove = true;

    try {
      _products.removeElementAt(index);
    }
    catch (ArrayIndexOutOfBoundsException e) {
      remove = false;
    }

    return remove;
  }

  /**
   * Διαγραφή όλων των products.
   */
  public void removeAllProducts() {
    _products.clear();
  }

  /**
   * Επιστρέφει το product στη θέση index ή null σε περίπτωση
   * που δεν υπάρχει product σε αυτή τη θέση.
   */
  public Product getProductAt(int index) {
    Product product = null;

    try {
      product = (Product)_products.elementAt(index);
    }
    catch (ArrayIndexOutOfBoundsException e) {
    }

    return product;
  }

  /**
   * Το πλήθος των προϊόντων που βρίσκονται στο order.
   */
  public BigDecimal getProductCount() {
    BigDecimal productCount = new BigDecimal(0);

    Product product = null;
    Enumeration productEnumeration = getProductEnumeration();

    while (productEnumeration.hasMoreElements()) {
      product = (Product)productEnumeration.nextElement();

      productCount = productCount.add(product.getQuantity());
    }

    productEnumeration = null;
    
    return productCount;
  }

  /**
   * Πόσα προϊόντα υπάρχουν στο καλάθι χωρις να λαμβάνει
   * υπόψην το quantity του καθενός.
   */
  public int getOrderLines() {
    return _products.size();
  }

  public Enumeration getProductEnumeration() {
    return _products.elements();
  }

  public DbRet processRequest(HttpServletRequest request) {
    DbRet dbRet = new DbRet();
    
    ProductAttribute productAttribute = null;
    Vector PAVector = new Vector();
    String PMAVCode = null;
    int attributeCounter = 0;
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1");
    
    String localeLanguage = request.getParameter("localeLanguage"),
           localeCountry = request.getParameter("localeCountry");
           
    HttpSession session = request.getSession();
    
    if (action.equals("CART_ADD")) {
      BigDecimal quantity = new BigDecimal("1");
      String q = null;
      
      if ( (q=request.getParameter("quantity")) != null) {
        quantity = new BigDecimal(q);
      }
      
      dbRet = doCartAdd(request.getParameter("prdId"),request.getParameter("PO_Code"),request.getParameter("stamp"),quantity,session);
    }
    else if (action.equals("CART_REMOVE")) {
      try {
        dbRet = doCartRemove(Integer.parseInt(request.getParameter("prdIndex")));
      }
      catch (Exception e) {
        dbRet.setNoError(0);
        e.printStackTrace();
      }
    }
    else if (action.equals("CART_RECALC")) {
      int productCount = getOrderLines();
      
      for (int i=0; i<productCount; i++) {
        try {
          int q = Integer.parseInt(request.getParameter("quantity_" + i));
          
          if (q > 0) {
            getProductAt(i).setQuantity(new BigDecimal(String.valueOf(q)));
          }
        }
        catch (Exception e) {
        }
      }
    }
    
    return dbRet;
  }
  
  public void resetOrder() {
    removeAllProducts();
    
    setOrderId("");
    setOrdBankTran("");
    setOrderDate(null);
    
    setOrdPayWay("");
    setCountryZone("");
    setDeliveryType("");
    
    setDocumentType("");
    
    setRGCode("");
  }
  
  public DbRet doCartRemove(int prdIndex) {
    DbRet dbRet = new DbRet();
    
    if (removeProductAt(prdIndex) == false) {
      dbRet.setNoError(0);
    }
    
    return dbRet;
  }
  
  public DbRet doCartAdd(String prdId, BigDecimal amount) {
    String stamp = "";
    
    BigDecimal quantity = new BigDecimal("1");
    
    boolean isGiftCard = true;
    
    return doCartAdd(prdId,"",stamp,quantity,isGiftCard,amount,null);
  }
  
  public DbRet doCartAdd(String prdId, String PO_Code, String stamp, BigDecimal quantity, HttpSession session) {
    boolean isGiftCard = false;
    BigDecimal amount = null;
    
    return doCartAdd(prdId,PO_Code,stamp,quantity,isGiftCard,amount,session);
  }
  
  public DbRet doCartAdd(String prdId,String PO_Code,String stamp,BigDecimal quantity,boolean isGiftCard,BigDecimal amount,HttpSession session) {
    DbRet dbRet = null;
    
    Present prdManager = new Present();
    prdManager.setDatabaseId(_customer.getDatabaseId());
    prdManager.setAuthUsername(_customer.getAuthUsername());
    prdManager.setAuthPassword(_customer.getAuthPassword());
    prdManager.setSession(session);
    
    Product product = null;
    PrdPrice prdPrice = null;
    
    BigDecimal exchangeRate = new BigDecimal("1");

    boolean stampExtraCost = false;
    if (stamp != null && stamp.length()>0) {
      stampExtraCost = true;
    }

    boolean checkForPrdHideFlag = true, checkForPrdCategory = true;
    
    if (isGiftCard == true) {
      checkForPrdHideFlag = false;
      checkForPrdCategory = false;
    }

    dbRet = prdManager.getPrd(prdId,_inventoryType,checkForPrdHideFlag,checkForPrdCategory);
    
    if (dbRet.getNoError() == 1 && dbRet.getRetInt() > 0) {
      product = existsProduct(prdId, PO_Code, stamp);
        
      if (product == null) {
        // new product
        boolean isOffer = false, giftCard = false;
        
        ProductOptionsValue productOptionsValue = null;
        
        if (PO_Code != null && PO_Code.length() > 0) {
          productOptionsValue = ProductOptionsValue.getProductOptionsValue(PO_Code);
        }
        
        if (PriceChecker.isOffer(prdManager.getQueryDataSet(),_customer.getCustomerType())) {
            isOffer = true;
            
            if (productOptionsValue == null) prdPrice = PriceChecker.calcPrd(quantity,prdManager.getQueryDataSet(),_customer.getCustomerType(),isOffer,_customer.getDiscountPct());
            else prdPrice = PriceChecker.calcPrd(quantity,prdManager.getQueryDataSet(),productOptionsValue,_customer.getCustomerType(),isOffer,_customer.getDiscountPct());
        }
        else {
            if (productOptionsValue == null) prdPrice = PriceChecker.calcPrd(quantity,prdManager.getQueryDataSet(),_customer.getCustomerType(),isOffer,_customer.getDiscountPct());
            else prdPrice = PriceChecker.calcPrd(quantity,prdManager.getQueryDataSet(),productOptionsValue,_customer.getCustomerType(),isOffer,_customer.getDiscountPct());
        }

        product = new Product(prdId,quantity,prdPrice,isOffer);
        product.setPrdName(prdManager.getColumn("name" + _customer.getCustLang()));
        product.setWeight(prdManager.getBig("weight"));
        product.setImg(prdManager.getColumn("img"));
        product.setImg2(prdManager.getColumn("img2"));
        
        product.setSCCode(prdManager.getColumn("prd_SCCode"));

        if (stamp != null) product.setStamp(stamp);
        
        if (productOptionsValue != null) product.setProductOptionsValue(productOptionsValue);

        product.setGiftCard(giftCard);
        addProduct(product);
      }
      else {
        // product allready exists
        product.setQuantity( product.getQuantity().add(quantity) );
      }
    }
    
    prdManager.closeResources();
    
    return dbRet;
  }
  
  public int getIntFromString(String s) {
    int n = 0;
    try {
      n = Integer.parseInt(s);
    }
    catch(Exception e){
      n = 0;
    }
    return n;
  }

  public void setOrderId(String orderId){
    _orderId = orderId;
  }

  public String getOrderId(){
    return _orderId;
  }

  public void setOrdBankTran(String ordBankTran){
    _ordBankTran = ordBankTran;
  }

  public String getOrdBankTran(){
    return _ordBankTran;
  }
  
  public void setOrderDate(Timestamp orderDate){
    _orderDate = orderDate;
  }

  public Timestamp getOrderDate(){
    return _orderDate;
  }

  public void setOrdPayWay(String ordPayWay) {
    _ordPayWay = ordPayWay;
  }
  
  public String getOrdPayWay() {
    return _ordPayWay;
  }
  
  public void setCountryZone(String countryZone) {
    _countryZone = countryZone;
  }
  
  public String getCountryZone() {
    return _countryZone;
  }
  
  public void setDeliveryType(String deliveryType) {
    _deliveryType = deliveryType;
  }
  
  public String getDeliveryType() {
    return _deliveryType;
  }
  
  public void setDocumentType(String documentType) {
    _documentType = documentType;
  }

  public String getDocumentType() {
    return _documentType;
  }

  public String getShippingWay() {
    return _shippingWay;
  }
  public void setShippingWay(String shippingWay) {
    _shippingWay = shippingWay;
  }
  
  public void setRGCode(String RGCode) {
    _RGCode = RGCode;
  }
  public String getRGCode() {
    return _RGCode;
  }
  
  private Customer _customer = null;
  
  private String _inventoryType = null;
  
  protected String _orderId;
  protected String _ordBankTran;
  protected Timestamp _orderDate;
  
  protected String _ordPayWay;
  protected String _deliveryType;
  protected String _countryZone;
  
  protected String _documentType;
  
  protected String _shippingWay = "";
  
  protected String _RGCode = "";
  
  private BigDecimal _vatPct = new BigDecimal("0.19");
  
  private BigDecimal _zero = new BigDecimal("0");
}