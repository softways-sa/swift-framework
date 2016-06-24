package gr.softways.dev.swift.cmcategory.v1;

import java.util.Vector;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

/**
 *
 * @author  minotauros
 */
public class Menu extends JSPBean {
  
  /** Creates a new instance of Present */
  public Menu() {
  }
  
  public int getMenuLength() {
    try {
      return _v.size();
    }
    catch (Exception e) {
      throw new NullPointerException(databaseId);
    }
  }
  
  public MenuOption getMenuOption(int i) {
    return (MenuOption)_v.get(i);
  }
  
  public DbRet getMenu(String CMCCode, String orderBy) {
    return getMenu(CMCCode,orderBy, "");
  }
  
  public DbRet getMenu(String CMCCode, String orderBy, String lang) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,authUsername,authPassword,"CMCategory",Director.AUTH_READ);

    if (auth != AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
      
      return dbRet;
    }
    
    database = director.getDBConnection(databaseId);

    database.setReadOnly(true);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    _v = new Vector();
    
    getMenu(lang, _v, CMCCode, orderBy, database);
    
    database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    database.setReadOnly(false);

    director.freeDBConnection(databaseId, database);
    
    return dbRet;
  }
  
  /**
   * This method assumes that catId length equals to 25 and that
   * catDepth is 2.
   */
  private DbRet getMenu(String lang, Vector v, String CMCCode, String orderBy, Database database) {
    DbRet dbRet = new DbRet();
    
    QueryDataSet queryDataSet = null;
    
    MenuOption menuOption = null, searchMenuOption = null;
    
    String s = CMCCode + "__";
    
    for (int i=0; i<25-s.length(); i++) s = s + " ";
    
    String query = "SELECT CMCCode,CMCName" + lang + ",CMCParentFlag,CMCURL" + lang + " FROM CMCategory WHERE CMCCode LIKE '" + SwissKnife.sqlEncode(s) + "%' AND CMCCode NOT IN ('" + SwissKnife.sqlEncode(CMCCode) + "') AND CMCShowFlag = '1'";

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
        menuOption = new MenuOption();
        menuOption.setTag("<li>");
        menuOption.setCode(SwissKnife.sqlDecode(queryDataSet.getString("CMCCode")));
        if (firstOption == true) {
          menuOption.setFirst(true);
          firstOption = false;
        }
        _v.add(menuOption);
        
        //System.out.println("<a href=\"swift.jsp\">" + SwissKnife.sqlDecode(queryDataSet.getString("CMCName")) + "</a>");
        menuOption = new MenuOption();
        menuOption.setTag("<a>");
        menuOption.setCode(SwissKnife.sqlDecode(queryDataSet.getString("CMCCode")));
        menuOption.setTitle(SwissKnife.sqlDecode(queryDataSet.getString("CMCName" + lang)));
        menuOption.setParent(SwissKnife.sqlDecode(queryDataSet.getString("CMCParentFlag")));
        if (queryDataSet.getString("CMCURL" + lang) != null && queryDataSet.getString("CMCURL" + lang).length()>0) menuOption.setURL(SwissKnife.sqlDecode(queryDataSet.getString("CMCURL" + lang)));
        _v.add(menuOption);
        
        searchMenuOption = new MenuOption();
        searchMenuOption.setTag("<li>");
        searchMenuOption.setCode(SwissKnife.sqlDecode(queryDataSet.getString("CMCCode")));
        
        if (queryDataSet.getString("CMCParentFlag").equals("1")) {
          //System.out.println("<ul>");
          menuOption = new MenuOption();
          menuOption.setTag("<ul>");
          _v.add(menuOption);
        
          getMenu(lang, _v, queryDataSet.getString("CMCCode"), orderBy, database);
          
          //System.out.println("</ul>");
          menuOption = new MenuOption();
          menuOption.setTag("</ul>");
          _v.add(menuOption);
        }
        
        queryDataSet.next();
        
        //System.out.println("</li>");
        menuOption = new MenuOption();
        menuOption.setTag("</li>");
        if (queryDataSet.inBounds() == false) {
          menuOption.setBottom(true);
        }
        _v.add(menuOption);
        
        if (queryDataSet.inBounds() == false) {
          menuOption = (MenuOption) _v.get( _v.indexOf(searchMenuOption) );
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