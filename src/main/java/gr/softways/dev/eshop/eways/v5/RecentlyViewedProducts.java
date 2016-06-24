/*
 * Product.java
 *
 * Created on 19 Νοέμβριος 2003, 10:58 πμ
 */

package gr.softways.dev.eshop.eways.v5;

import gr.softways.dev.eshop.eways.v2.Product;

import java.util.Vector;

/**
 *
 * @author  minotauros
 */
public class RecentlyViewedProducts {
  
  public RecentlyViewedProducts(int maxProducts) {
    this.maxProducts = maxProducts;
    
    recentlyViewedProducts = new Vector(this.maxProducts);
  }
  
  public void addProduct(Product product) {
    int indexOf = 0;
    
    if ((indexOf = contains(product)) >= 0) {
      recentlyViewedProducts.remove(indexOf);
      
      recentlyViewedProducts.add(0, product);
    }
    else {
      try { recentlyViewedProducts.remove(maxProducts-1); } catch (Exception e) { };
      recentlyViewedProducts.add(0, product);
    }
  }
  
  public Product getProductAt(int index) {
    Product product = null;
    
    try {
      product = (Product) recentlyViewedProducts.get(index);
    }
    catch (Exception e) {
      product = null;
    }
    
    return product;
  }
  
  public int getSize() {
    return recentlyViewedProducts.size();
  }
  
  public int getCapacity() {
    return recentlyViewedProducts.capacity();
  }
  
  private int contains(Product product) {
    int indexOf = -1;
    
    for (int i=0; i<recentlyViewedProducts.size(); i++) {
      Product p  = (Product) recentlyViewedProducts.get(i);
      
      if (p.getPrdId().equals(product.getPrdId())) {
        indexOf = i;
        break;
      }
    }
    
    return indexOf;
  }
  
  private Vector recentlyViewedProducts = null;
  private int maxProducts = 0;
}