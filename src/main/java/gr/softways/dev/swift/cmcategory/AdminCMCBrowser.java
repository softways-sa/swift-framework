/*
 * AdminCMCBrowser.java
 *
 * Created on 7 Απρίλιος 2006, 4:57 μμ
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package gr.softways.dev.swift.cmcategory;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;
/**
 *
 * @author haris
 */
public class AdminCMCBrowser  extends JSPBean {
  private Object[] _CMCategories = null;
  
  private int _CMCStepSize = 2;
  
  private int _size = 0;

  private int _index = -1;  
  /** Creates a new instance of AdminCMCBrowser */
  public AdminCMCBrowser() {
  }
  
  public DbRet fetchCMCategories(boolean openAllCat, String CMRCode, 
                               boolean fetchRelated) {
    Director director = Director.getInstance();
    
    DbRet dbRet = new DbRet();
    
    CMRCode = SwissKnife.sqlEncode(CMRCode);
    
    QueryDataSet relQueryDataSet = new QueryDataSet();
    
    int auth = director.auth(databaseId, authUsername, authPassword,
                             "CMCategory", Director.AUTH_READ);

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
            
    String query = "SELECT * FROM CMCategory";
    
    if (fetchRelated == false && CMRCode != null && CMRCode.length()>0) {
      
          query  += " WHERE CMCCode NOT IN (" 
                 + " SELECT CMCRelCMR.CCCR_CMCCode FROM CMCRelCMR" 
                 + " WHERE CCCR_CMRCode IN ('" + CMRCode + "') )"; 
    }
        
    query += " ORDER BY CMCCode";
    
    String relquery = "";
     
    if (fetchRelated == true && CMRCode != null && CMRCode.length()>0) {
      
       relquery = " SELECT CMCRelCMR.CCCR_CMCCode FROM CMCRelCMR" 
                + " WHERE CCCR_CMRCode IN ('" + CMRCode + "')";
    }
     
    database = director.getDBConnection(databaseId);
                
    try {
      // κλείσε το query ώστε να το ανοίξουμε ξανά
      if (queryDataSet.isOpen()) queryDataSet.close();
      
      queryDataSet.setQuery(new QueryDescriptor(database, query, null, true, Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();
      
      _size = queryDataSet.getRowCount();
      
      if (relquery.length()>0) {
        relQueryDataSet.setQuery(new QueryDescriptor(database, relquery, null, true, Load.ALL));
        relQueryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

        relQueryDataSet.refresh();
      }
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();      
    }
    finally {      
      director.freeDBConnection(databaseId,database);
    }
    
    if (dbRet.getNoError() == 1 && _size>0) {
      CMCategory CMCat = null;
      
      _CMCategories = new Object[_size];      
                  
      
      try {                
        for (int i=0; i<_size; i++) {
          CMCat = new CMCategory();

          CMCat.setCMCCode(getColumn("CMCCode"));
          CMCat.setCMCName(getColumn("CMCName"));          
          CMCat.setCMCShowFlag(getColumn("CMCShowFlag"));          
          CMCat.setCMCParentFlag(getColumn("CMCParentFlag"));
          CMCat.setCMCDepth( getColumn("CMCCode").trim().length() / _CMCStepSize);

          if (fetchRelated == true && CMRCode != null && CMRCode.length()>0) {
            if (SwissKnife.locateOneRow("CCCR_CMCCode", getColumn("CMCCode"),
                                        relQueryDataSet)) {
              CMCat.setIsRelated(true);
            }
            else {
              CMCat.setIsRelated(false);
            }
          }
          
          if (CMCat.getCMCDepth() == 1) {
            CMCat.setVisible(true);
          }
          else {
            CMCat.setVisible(openAllCat);
          }
          
          _CMCategories[i] = CMCat;
          
          queryDataSet.next();
        }
      }
      catch (Exception e) {
        e.printStackTrace();
        dbRet.setNoError(0);
      }      
    }
    
    if (queryDataSet != null && queryDataSet.isOpen()) {
      queryDataSet.close();
    }
    if (relQueryDataSet != null && relQueryDataSet.isOpen()) {
      relQueryDataSet.close();
    }
    
    return dbRet;
  }
  
  public void switchCMCategory(String CMCCode) {
    CMCategory CMCat = null;
    
    String tmpCMCCode = null;
        
    boolean expand = false;
    
    for (int i=0; i<_size; i++) {                         
      CMCat = (CMCategory)_CMCategories[i];
      
      tmpCMCCode = CMCat.getCMCCode();
      
      if (tmpCMCCode != null && tmpCMCCode.startsWith(CMCCode)             
            && tmpCMCCode.length() == CMCCode.length() + _CMCStepSize) {
        expand = !CMCat.isVisible();
        break;
      }
    }
    
    for (int i=0; i<_size; i++) {                         
      CMCat = (CMCategory)_CMCategories[i];
      
      tmpCMCCode = CMCat.getCMCCode();
            
      if (tmpCMCCode != null && expand == true
            && tmpCMCCode.startsWith(CMCCode)             
            && tmpCMCCode.length() == CMCCode.length() + _CMCStepSize) {
        // set visible only direct subcategories
        CMCat.setVisible(true);
      }
      else if (tmpCMCCode != null && expand == false
            && tmpCMCCode.startsWith(CMCCode)             
            && tmpCMCCode.length() > CMCCode.length()) {
        // hide all subcategories
        CMCat.setVisible(false);
      }
    }
        
    CMCat = null;
  }

  public CMCategory nextCMCategory() {
    CMCategory CMCat = null;
    
    _index++;
    
    return (CMCategory)_CMCategories[_index];
  }
  
  public int getSize() {
    return _size;
  }  
  
  public void firstCMCategory() {
    _index = -1;
  }
  
  public CMCategory findCMCategory(String CMCCode) {
    CMCategory CMCat = null;
    
    CMCCode = SwissKnife.sqlEncode(CMCCode);
    
    for (int i=0; i<_size; i++) {
      CMCat = (CMCategory)_CMCategories[i];
      
      if (CMCat.getCMCCode().equals(CMCCode)) {
        break;
      }
      else {
        CMCat = null;
      }
    }
      
    return CMCat;
  }  
}
