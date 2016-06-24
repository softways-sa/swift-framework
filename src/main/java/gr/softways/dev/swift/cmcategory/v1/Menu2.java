package gr.softways.dev.swift.cmcategory.v1;

import java.util.Vector;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

/**
 *
 * @author  minotauros
 */
public class Menu2 extends JSPBean {
  
  /** Creates a new instance of Present */
  public Menu2() {
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
  
  public DbRet getMenu(String CMCCode, String lang) {
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
    
    dbRet = getMenuTree(lang, CMCCode, database);

    database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    database.setReadOnly(false);

    director.freeDBConnection(databaseId, database);

    if (dbRet.getNoError() == 1) {
      for (int i=1; i<=12; i++) {
        sortRank(2 * i);
      }
      for (int i=1; i<=(12 - (CMCCode.length() / 2)); i++) {
        sortCode(CMCCode.length() + (i * 2));
      }
      buildMenu(0,"");
    }
    
    return dbRet;
  }
  
  private int buildMenu(int index, String rootCode) {
    MenuOption menuOption = null;
    
    int lenD = menuTree.length;
    
    String pushedRootCode = rootCode;
    
    boolean firstOption = true;
    while (index < lenD) {
      
      menuOption = new MenuOption();
      menuOption.setTag("<li>");
      menuOption.setCode(SwissKnife.sqlDecode(menuTree[index][0]));
      if (firstOption == true) {
        menuOption.setFirst(true);
        firstOption = false;
      }
      _v.add(menuOption);

      menuOption = new MenuOption();
      menuOption.setTag("<a>");
      menuOption.setCode(menuTree[index][0]);
      menuOption.setTitle(menuTree[index][1]);
      menuOption.setParent(menuTree[index][2]);
      if (menuTree[index][3] != null && menuTree[index][3].length()>0) menuOption.setURL(menuTree[index][3]);
      _v.add(menuOption);

      if (menuTree[index][2].equals("1")) {
        rootCode = menuTree[index][0];
        
        menuOption = new MenuOption();
        menuOption.setTag("<ul>");
        _v.add(menuOption);

        index = buildMenu(++index, rootCode) - 1;

        menuOption = new MenuOption();
        menuOption.setTag("</ul>");
        _v.add(menuOption);
        
        rootCode = pushedRootCode;
      }

      index++;

      menuOption = new MenuOption();
      menuOption.setTag("</li>");
      
      _v.add(menuOption);

      if (index < lenD && rootCode.length() > 0 && !menuTree[index][0].startsWith(rootCode)) break;
    }
    
    return index;
  }
  
  private DbRet getMenuTree(String lang, String CMCCode, Database database) {
    DbRet dbRet = new DbRet();
    
    QueryDataSet queryDataSet = null;
    
    String query = "SELECT CMCCode,CMCRank,CMCName" + lang + ",CMCParentFlag,CMCURL" + lang + " FROM CMCategory WHERE CMCCode LIKE '" + SwissKnife.sqlEncode(CMCCode) + "%' AND CMCCode NOT IN ('" + SwissKnife.sqlEncode(CMCCode) + "') AND CMCShowFlag = '1' ORDER BY CMCCode ASC";

    try {
      queryDataSet = new QueryDataSet();
      
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();
      int qCount = queryDataSet.getRowCount();
      
      menuTree = new String[qCount][5];

      for (int i=0; i<qCount; i++) {
        menuTree[i][0] = SwissKnife.sqlDecode(queryDataSet.getString("CMCCode"));
        menuTree[i][1] = SwissKnife.sqlDecode(queryDataSet.getString("CMCName" + lang));
        menuTree[i][2] = SwissKnife.sqlDecode(queryDataSet.getString("CMCParentFlag"));
        menuTree[i][3] = SwissKnife.sqlDecode(queryDataSet.getString("CMCURL" + lang));
        menuTree[i][4] = String.valueOf( queryDataSet.getInt("CMCRank") );
        
        queryDataSet.next();
      }
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }

    if (queryDataSet != null) queryDataSet.close();
    
    return dbRet;
  }
  
  private void sortRank(int cDepth) {
    int lenD = menuTree.length;
    
    String tmp = null;
    
    for (int i=0; i<lenD; i++) {
      
      for (int j=(lenD-1); j>=(i+1); j--) {
        if (menuTree[j][0].length() != cDepth) continue;
        else if (menuTree[j-1][0].length() < cDepth) continue;
        else if (menuTree[j-1][0].length() > cDepth) swap(j, j-1);
        else if (Integer.valueOf(menuTree[j][4]) > Integer.valueOf(menuTree[j-1][4])) swap(j, j-1);
      }
      
    }
  }
  
  private void sortCode(int rootDepth) {
    int lenD = menuTree.length;
    
    String[][] menuTreeTmp = new String[lenD][menuTree[0].length];
        
    String tmpCode = null;
    
    int counter = 0;
    
    for (int i=0; i<lenD; i++) {
            
      tmpCode = menuTree[i][0];
      
      if (tmpCode.length() > rootDepth) continue;
      else if (tmpCode.length() < rootDepth) {
        for (int x=0; x<menuTree[i].length; x++) menuTreeTmp[counter][x] = menuTree[i][x];
        counter++;
      }
      else if (tmpCode.length() == rootDepth) {
        for (int j=i; j<lenD; j++) {
          if (menuTree[j][0].startsWith(tmpCode)) {
            for (int x=0; x<menuTree[i].length; x++) menuTreeTmp[counter][x] = menuTree[j][x];
            counter++;
          }
        }
      }
      
    }
    
    System.arraycopy(menuTreeTmp, 0, menuTree, 0, lenD);
  }
  
  private void swap(int x, int c) {
    String tmp = null;
    
    for (int i=0; i<menuTree[x].length; i++) {
      tmp = menuTree[x][i];
      menuTree[x][i] = menuTree[c][i];
      menuTree[c][i] = tmp;
    }
  }
  
  private void printMenuTree() {
    System.out.println("---");
    for (int i=0; i<menuTree.length; i++) System.out.println(menuTree[i][0] + " | " + menuTree[i][4]);
    System.out.println("---");
  }
  
  private Vector _v = null;
  
  private String[][] menuTree = null;
}