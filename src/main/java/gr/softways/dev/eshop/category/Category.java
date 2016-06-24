/*
 * Category.java
 *
 * Created on 19 Ιούνιος 2002, 5:55 μμ
 */

package gr.softways.dev.eshop.category;

/**
 *
 * @author  minotauros
 * @version 
 */
public class Category {
 
  public Category() {
  }

  public String getCatId() {
    return _catId;
  }  
  public void setCatId(String catId) {
    _catId = catId;
  }
  
  public String getCatName() {
    return _catName;
  }  
  public void setCatName(String catName) {
    _catName = catName;
  }
  
  public String getCatShowFlag() {
    return _catShowFlag;
  }  
  public void setCatShowFlag(String catShowFlag) {
    _catShowFlag = catShowFlag;
  }
  
  public String getCatParentFlag() {
    return _catParentFlag;
  }  
  public void setCatParentFlag(String catParentFlag) {
    _catParentFlag = catParentFlag;
  }
  
  public boolean isVisible() {
    return _isVisible;
  }  
  public void setVisible(boolean isVisible) {
    _isVisible = isVisible;
  }
  
  public int getCatDepth() {
    return _catDepth;
  }  
  public void setCatDepth(int catDepth) {
    _catDepth = catDepth;
  }
  
  public boolean isRelated() {
    return _isRelated;
  }
  public void setIsRelated(boolean isRelated) {
    _isRelated = isRelated;
  }
  
  private String _catId;
  private String _catName;
  private String _catShowFlag;
  private String _catParentFlag;
  
  private boolean _isVisible = false;
  private boolean _isRelated = false;
  
  private int _catDepth;
}