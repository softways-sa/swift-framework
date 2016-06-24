/*
 * CategoryBrowser.java
 *
 * Created on 19 Ιούνιος 2002, 5:38 μμ
 */

package gr.softways.dev.eshop.category;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

/**
 *
 * @author  minotauros
 * @version 
 */
public class AdminCatBrowser extends JSPBean {

  private Object[] _categories = null;
  
  private int _catDepth = 0;
  
  private int _size = 0;

  private int _index = -1;
  
  public AdminCatBrowser() {
  }
  
  public synchronized DbRet fetchCategories(boolean openAllCat, String prdId, 
                               boolean fetchRelated) {
    Director director = Director.getInstance();
    
    DbRet dbRet = new DbRet();
    
    QueryDataSet relQueryDataSet = new QueryDataSet();
    
    int auth = director.auth(databaseId, authUsername, authPassword,
                             "prdCategory", Director.AUTH_READ);

    if (auth != Director.AUTH_OK) {
      dbRet.setNoError(0);
      
      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
            
    String query = "SELECT * FROM prdCategory";
    
    if (fetchRelated == false && prdId != null && prdId.length()>0) {
      
          query  += " WHERE catId NOT IN (" 
                 + " SELECT prdInCatTab.PINCCatId FROM prdInCatTab" 
                 + " WHERE PINCPrdId IN ('" + prdId + "') )"; 
    }
        
    query += " ORDER BY catId";
    
    String relquery = "";
     
    if (fetchRelated == true && prdId != null && prdId.length()>0) {
      
       relquery = " SELECT prdInCatTab.PINCCatId FROM prdInCatTab" 
                + " WHERE PINCPrdId IN ('" + prdId + "')";
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
      Category category = null;
      
      _categories = new Object[_size];      
                  
      _catDepth = Integer.parseInt(director.getPoolAttr(databaseId + ".catDepth"));
      
      try {
        for (int i=0; i<_size; i++) {
          category = new Category();

          category.setCatId(getColumn("catId"));
          category.setCatName(getColumn("catName"));          
          category.setCatShowFlag(getColumn("catShowFlag"));          
          category.setCatParentFlag(getColumn("catParentFlag"));
          category.setCatDepth( getColumn("catId").trim().length() / _catDepth);

          if (fetchRelated == true && prdId != null && prdId.length()>0) {
            if (SwissKnife.locateOneRow("PINCCatId", getColumn("catId"),
                                        relQueryDataSet)) {
              category.setIsRelated(true);
            }
            else {
              category.setIsRelated(false);
            }
          }
          
          if (category.getCatDepth() == 1) {
            category.setVisible(true);
          }
          else {
            category.setVisible(openAllCat);
          }
          
          _categories[i] = category;
          
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
  
  public void switchCategory(String catId) {
    Category category = null;
    
    String tmpCatId = null;
        
    boolean expand = false;
    
    for (int i=0; i<_size; i++) {
      category = (Category)_categories[i];
      
      tmpCatId = category.getCatId();
      
      if (tmpCatId != null && tmpCatId.startsWith(catId)             
            && tmpCatId.length() == catId.length() + _catDepth) {
        expand = !category.isVisible();
        break;
      }
    }
    
    for (int i=0; i<_size; i++) {                         
      category = (Category)_categories[i];
      
      tmpCatId = category.getCatId();
            
      if (tmpCatId != null && expand == true
            && tmpCatId.startsWith(catId)             
            && tmpCatId.length() == catId.length() + _catDepth) {
        // set visible only direct subcategories
        category.setVisible(true);
      }
      else if (tmpCatId != null && expand == false
            && tmpCatId.startsWith(catId)             
            && tmpCatId.length() > catId.length()) {
        // hide all subcategories
        category.setVisible(false);
      }
    }
        
    category = null;
  }

  public Category nextCategory() {
    Category category = null;
    
    _index++;
    
    return (Category)_categories[_index];
  }
  
  public int getSize() {
    return _size;
  }  
  
  public void firstCategory() {
    _index = -1;
  }
  
  public Category findCategory(String catId) {
    Category category = null;
    
    for (int i=0; i<_size; i++) {
      category = (Category)_categories[i];
      
      if (category.getCatId().equals(catId)) {
        break;
      }
      else {
        category = null;
      }
    }
      
    return category;
  }
}