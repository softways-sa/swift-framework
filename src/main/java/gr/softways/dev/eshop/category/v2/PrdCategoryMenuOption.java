package gr.softways.dev.eshop.category.v2;

public class PrdCategoryMenuOption {
  /** Creates a new instance of Present */
  public PrdCategoryMenuOption() {
  }
  
  public void setTag(String tag) {
    _tag = tag;
  }
  public String getTag() {
    return _tag;
  }
  
  public void setCode(String code) {
    _code = code;
  }
  public String getCode() {
    return _code;
  }
  
  public void setURL(String url) {
    _url = url;
  }
  public String getURL() {
    return _url;
  }
  
  public void setTitle(String title) {
    _title = title;
  }
  public String getTitle() {
    return _title;
  }
  
  public void setParent(String parent) {
    _parent = parent;
  }
  public String getParent() {
    return _parent;
  }

  public boolean isBottom() {
    return bottom;
  }

  public void setBottom(boolean bottom) {
    this.bottom = bottom;
  }
  
  public boolean equals(Object object) {
    boolean equals = false;
    
    PrdCategoryMenuOption option = (PrdCategoryMenuOption) object;
        
    try {
      if ( getTag().equals( option.getTag()) && getCode().equals( option.getCode() ) ) equals = true;
    }
    catch (Exception e) {
      equals = false;
    }
    
    return equals;
  }

  public boolean isFirst() {
    return first;
  }

  public void setFirst(boolean first) {
    this.first = first;
  }
  
  private String _tag = null;
  private String _code = null;
  private String _url = null;
  private String _title = null;
  private String _parent = null;
  private boolean bottom = false;
  private boolean first = false;
}