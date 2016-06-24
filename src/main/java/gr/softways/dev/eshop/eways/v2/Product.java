/*
 * Product.java
 *
 * Created on 19 Νοέμβριος 2003, 10:58 πμ
 */

package gr.softways.dev.eshop.eways.v2;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Vector;

import gr.softways.dev.eshop.product.v2.ProductOptionsValue;

/**
 *
 * @author  minotauros
 */
public class Product {
  
  static public String HOTDEAL_FLAG_ALWAYS = "1";
  static public String HOTDEAL_FLAG_DATE = "2";
  static public String HOTDEAL_FLAG_DATE_STOCK = "3";
  static public String HOTDEAL_FLAG_STOCK = "4";

  public Product(String prdId, BigDecimal quantity, PrdPrice prdPrice, boolean isOffer) {
    _prdId = prdId;
    
    _quantity = quantity;
    
    _prdPrice = prdPrice;
    
    _isOffer = isOffer;
    
    _PAVector = new Vector();
  }
  public Product(String prdId, Vector PAVector, BigDecimal quantity,PrdPrice prdPrice,boolean isOffer) {
    _prdId = prdId;
    
    _quantity = quantity;
    
    _prdPrice = prdPrice;
    
    _isOffer = isOffer;
    
    _PAVector = PAVector;
  }
  
  public String getPrdId() {
    return _prdId;
  }
  
  public String getPrdName() {
    return _name;
  }
  public void setPrdName(String name) {
    _name = name;
  }
  
  public void setQuantity(BigDecimal quantity) {
    _quantity = quantity;
    
    _prdPrice.setQuantity(_quantity);
  }
  
  public void setDiscountPct(BigDecimal discountPct) {
    _prdPrice.setDiscountPct(discountPct);
  }
  
  public BigDecimal getQuantity() {
    return _quantity;
  }
  
  public void setWeight(BigDecimal weight) {
    _weight = weight;
  }
  
  public BigDecimal getWeight() {
    return _weight;
  }
  
  public void setMinOrderQua(BigDecimal minOrderQua) {
    _minOrderQua = minOrderQua;
  }
  
  public BigDecimal getMinOrderQua() {
    return _minOrderQua;
  }
  
  public PrdPrice getPrdPrice() {
    return _prdPrice;
  }
  
  public boolean isOffer() {
    return _isOffer;
  }
  
  public boolean hasAttributes() {
    return _hasAttributes;
  }
  public void setAttributes(boolean hasAttributes) {
    _hasAttributes = hasAttributes;
  }
  
  public String getAttAttCode() {
    return _attAttCode;
  }
  public void setAttAttCode(String attAttCode) {
    _attAttCode = attAttCode;
  }
  
  public String getAttName() {
    return _attName;
  }
  public void setAttName(String attName) {
    _attName = attName;
  }
  
  public String getAtt2AttCode() {
    return _att2AttCode;
  }
  public void setAtt2AttCode(String att2AttCode) {
    _att2AttCode = att2AttCode;
  }
  
  public String getAtt2Name() {
    return _att2Name;
  }
  public void setAtt2Name(String att2Name) {
    _att2Name = att2Name;
  }
  
  public String getStamp() {
    return _stamp;
  }
  public void setStamp(String stamp) {
    _stamp = stamp;
  }

  public String getImg() {
    return _img;
  }
  public void setImg(String img) {
    _img = img;
  }
  
  public String getImg2() {
    return _img2;
  }
  public void setImg2(String img2) {
    _img2 = img2;
  }
  
  public String getImg3() {
    return _img3;
  }
  public void setImg3(String img3) {
    _img3 = img3;
  }
  
  public String getSCCode() {
    return _SCCode;
  }
  public void setSCCode(String SCCode) {
    _SCCode = SCCode;
  }
  
  public void setVATPct(BigDecimal vatPct) {
    _prdPrice.setVATPct(vatPct);
  }
  
  public boolean isGiftCard() {
    return _giftCard;
  }
  public void setGiftCard(boolean giftCard) {
    _giftCard = giftCard;
  }
  
  public Vector getPAVector() {
    return _PAVector;
  }
  public void setPAVector(Vector PAVector) {
    _PAVector = PAVector;
  }
  
  public boolean PAVectorCompare(Vector PAVector) {
    ProductAttribute pa1 = null, pa2 = null;
    
    boolean seemEqual = true;
    
    if (_PAVector.size() == 0 && PAVector.size() == 0) return true;
    else if (_PAVector.size() != PAVector.size()) return false;
    else {
      for (int i=0; i<_PAVector.size(); i++) {
        pa1 = (ProductAttribute)_PAVector.elementAt(i);
        pa2 = (ProductAttribute)PAVector.elementAt(i);
        if (!pa1.getPMAVCode().equals(pa2.getPMAVCode())) {
          seemEqual = false;
          break;
        }
      }
    }
    
    return seemEqual;
  }
  
  public ProductAttribute getProductAttributeAt(int index) {
    ProductAttribute pa = null;

    try {
      pa = (ProductAttribute)_PAVector.elementAt(index);
    }
    catch (ArrayIndexOutOfBoundsException e) {
    }

    return pa;
  }
  
  public ProductOptionsValue getProductOptionsValue() {
    return _productOptionsValue;
  }
  public void setProductOptionsValue(ProductOptionsValue productOptionsValue) {
    _productOptionsValue = productOptionsValue;
  }
  
  public String getPO_Code() {
    if (_productOptionsValue == null) return "";
    else return _productOptionsValue.getPO_Code();
  }

  public boolean isGiftWrap() {
    return giftWrap;
  }

  public void setGiftWrap(boolean giftWrap) {
    this.giftWrap = giftWrap;
  }

  public BigDecimal getFixedShipPrice() {
    return fixedShipPrice;
  }

  public void setFixedShipPrice(BigDecimal fixedShipPrice) {
    this.fixedShipPrice = fixedShipPrice;
  }
  
  private String _prdId = null;
  private String _name = null;

  private String _img = "";
  private String _img2 = "";
  private String _img3 = "";
  
  private boolean _hasAttributes = false;
  
  private String _attAttCode = "";
  private String _attName = "";
  private String _att2AttCode = "";
  private String _att2Name = "";
  
  private String _stamp = "";
  
  private String _SCCode = "";
  
  private BigDecimal _weight = null;
  private BigDecimal _quantity = null;
  private BigDecimal _minOrderQua = null;
  
  private BigDecimal fixedShipPrice = null;
  
  private PrdPrice _prdPrice = null;
  
  private boolean _giftCard = false;
  
  boolean _isOffer = false;
  
  private Vector _PAVector = null;
  
  private ProductOptionsValue _productOptionsValue = null;
  
  private boolean giftWrap = false;
}