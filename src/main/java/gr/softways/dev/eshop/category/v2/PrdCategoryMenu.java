package gr.softways.dev.eshop.category.v2;

import java.util.Vector;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

/**
 *
 * @author  minotauros
 */
public class PrdCategoryMenu extends JSPBean {
  
  /** Creates a new instance of Present */
  public PrdCategoryMenu() {
  }
  
  public int getMenuLength() {
    try {
      return _v.size();
    }
    catch (Exception e) {
      throw new NullPointerException(databaseId);
    }
  }
  
  public PrdCategoryMenuOption getMenuOption(int i) {
    return (PrdCategoryMenuOption)_v.get(i);
  }
  
  public DbRet getMenu(String CMCCode, String orderBy) {
    return getMenu(CMCCode,orderBy, "");
  }
  
  public DbRet getMenu(String CMCCode, String orderBy, String lang) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"prdCategory",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    database = director.getDBConnection(databaseId);

    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();
    
    _v = new Vector();
    
    getMenu(lang, _v, CMCCode, orderBy, database);
    
    database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  /**
   * This method assumes that catId length equals to 25 and that
   * catDepth is 2.
   */
  private DbRet getMenu(String lang, Vector v, String catId, String orderBy, Database database) {
    DbRet dbRet = new DbRet();
    
    QueryDataSet queryDataSet = null;
    
    PrdCategoryMenuOption menuOption = null, searchMenuOption = null;
    
    String s = catId + "__";
    
    for (int i=0; i<25-s.length(); i++) s = s + " ";
    
    String query = "SELECT * FROM prdCategory WHERE catId LIKE '" + SwissKnife.sqlEncode(s) + "%' AND catId NOT IN ('" + SwissKnife.sqlEncode(catId) + "') AND catShowFlag = '1'";

    if (orderBy != null && orderBy.length()>0) {
      query += " ORDER BY " + orderBy;
    }
    
    try {
      queryDataSet = new QueryDataSet();
      
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();

      boolean firstOption = true;
      while (queryDataSet.inBounds() == true) {
        //System.out.println("<li>");
        menuOption = new PrdCategoryMenuOption();
        menuOption.setTag("<li>");
        menuOption.setCode(SwissKnife.sqlDecode(queryDataSet.getString("catId")));
        if (firstOption == true) {
          menuOption.setFirst(true);
          firstOption = false;
        }
        _v.add(menuOption);
        
        //System.out.println("<a href=\"swift.jsp\">" + SwissKnife.sqlDecode(queryDataSet.getString("CMCName")) + "</a>");
        menuOption = new PrdCategoryMenuOption();
        menuOption.setTag("<a>");
        menuOption.setCode(SwissKnife.sqlDecode(queryDataSet.getString("catId")));
        menuOption.setTitle(SwissKnife.sqlDecode(queryDataSet.getString("catName" + lang)));
        menuOption.setParent(SwissKnife.sqlDecode(queryDataSet.getString("catParentFlag")));
        //if (queryDataSet.getString("CMCURL" + lang) != null && queryDataSet.getString("CMCURL" + lang).length()>0) menuOption.setURL(SwissKnife.sqlDecode(queryDataSet.getString("CMCURL" + lang)));
        _v.add(menuOption);
        
        searchMenuOption = new PrdCategoryMenuOption();
        searchMenuOption.setTag("<li>");
        searchMenuOption.setCode(SwissKnife.sqlDecode(queryDataSet.getString("catId")));
        
        if (queryDataSet.getString("catParentFlag").equals("1")) {
          //System.out.println("<ul>");
          menuOption = new PrdCategoryMenuOption();
          menuOption.setTag("<ul>");
          _v.add(menuOption);
        
          getMenu(lang, _v, queryDataSet.getString("catId"), orderBy, database);
          
          //System.out.println("</ul>");
          menuOption = new PrdCategoryMenuOption();
          menuOption.setTag("</ul>");
          _v.add(menuOption);
        }
        
        queryDataSet.next();
        
        //System.out.println("</li>");
        menuOption = new PrdCategoryMenuOption();
        menuOption.setTag("</li>");
        if (queryDataSet.inBounds() == false) {
          menuOption.setBottom(true);
        }
        _v.add(menuOption);
        
        if (queryDataSet.inBounds() == false) {
          menuOption = (PrdCategoryMenuOption) _v.get( _v.indexOf(searchMenuOption) );
          menuOption.setBottom(true);
        }
      }
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }
    
    if (queryDataSet != null) queryDataSet.close();

    return dbRet;
  }
  
  private Vector _v = null;
}