/*
 * CMCategory.java
 *
 * Created on 7 Απρίλιος 2006, 4:39 μμ
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package gr.softways.dev.swift.cmcategory;

/**
 *
 * @author haris
 */
  
public class CMCategory {
 
  public CMCategory() {
  }

  public String getCMCCode() {
    return _CMCCode;
  }  
  public void setCMCCode(String CMCCode) {
    _CMCCode = CMCCode;
  }
  
  public String getCMCName() {
    return _CMCName;
  }  
  public void setCMCName(String CMCName) {
    _CMCName = CMCName;
  }
  
  public String getCMCShowFlag() {
    return _CMCShowFlag;
  }  
  public void setCMCShowFlag(String CMCShowFlag) {
    _CMCShowFlag = CMCShowFlag;
  }
  
  public String getCMCParentFlag() {
    return _CMCParentFlag;
  }  
  public void setCMCParentFlag(String CMCParentFlag) {
    _CMCParentFlag = CMCParentFlag;
  }
  
  public boolean isVisible() {
    return _isVisible;
  }  
  public void setVisible(boolean isVisible) {
    _isVisible = isVisible;
  }
  
  public int getCMCDepth() {
    return _CMCDepth;
  }  
  public void setCMCDepth(int CMCDepth) {
    _CMCDepth = CMCDepth;
  }
  
  public boolean isRelated() {
    return _isRelated;
  }
  public void setIsRelated(boolean isRelated) {
    _isRelated = isRelated;
  }
  
  private String _CMCCode;
  private String _CMCName;
  private String _CMCShowFlag;
  private String _CMCParentFlag;
  
  private boolean _isVisible = false;
  private boolean _isRelated = false;
  
  private int _CMCDepth;
}  
  

