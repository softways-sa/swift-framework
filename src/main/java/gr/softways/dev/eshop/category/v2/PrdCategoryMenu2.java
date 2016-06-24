package gr.softways.dev.eshop.category.v2;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;
import java.util.HashMap;
import java.util.Vector;

/**
 *
 * @author  minotauros
 */
public class PrdCategoryMenu2 extends JSPBean {
  
  /** Creates a new instance of Present */
  public PrdCategoryMenu2() {
  }
  
  public int getMenuLength() {
    try {
      return _v.size();
    }
    catch (Exception e) {
      throw new NullPointerException(databaseId);
    }
  }
  
  public PrdCategoryMenuOption2 getMenuOption(int i) {
    return (PrdCategoryMenuOption2)_v.get(i);
  }
  
  public DbRet getMenu(String catId, String lang) {
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

    database.setReadOnly(true);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    int prevTransIsolation = dbRet.getRetInt();
    
    _v = new Vector();
    
    dbRet = getMenuTree(lang, catId, database);

    database.commitTransaction(dbRet.getNoError(), prevTransIsolation);
    database.setReadOnly(false);

    director.freeDBConnection(databaseId, database);

    if (dbRet.getNoError() == 1) {
      for (int i=1; i<=12; i++) {
        sortRank(2 * i);
      }
      
      try {
        for (int i=1; i<=(12 - (catId.length() / 2)); i++) {
          sortCode(catId.length() + (i * 2));
        }
      }
      catch (Exception e) {
        System.out.println(e);
        dbRet.setNoError(0);
      }
      
      if (dbRet.getNoError() == 1) buildMenu(0,"");
    }
    
    return dbRet;
  }
  
  private int buildMenu(int index, String rootCode) {
    PrdCategoryMenuOption2 menuOption = null;
    
    int lenD = menuTree.length;
    
    String pushedRootCode = rootCode;
    
    while (index < lenD) {
      
      menuOption = new PrdCategoryMenuOption2();
      menuOption.setTag("<li>");
      menuOption.setCode(SwissKnife.sqlDecode(menuTree[index][0]));
      _v.add(menuOption);

      menuOption = new PrdCategoryMenuOption2();
      menuOption.setTag("<a>");
      menuOption.setCode(menuTree[index][0]);
      menuOption.setTitle(menuTree[index][1]);
      menuOption.setSefFullPath(menuTree[index][5]);
      menuOption.setParent(menuTree[index][2]);
      if (menuTree[index][3] != null && menuTree[index][3].length()>0) menuOption.setURL(menuTree[index][3]);
      _v.add(menuOption);

      if (menuTree[index][2].equals("1")) {
        rootCode = menuTree[index][0];
        
        menuOption = new PrdCategoryMenuOption2();
        menuOption.setTag("<ul>");
        _v.add(menuOption);

        index = buildMenu(++index, rootCode) - 1;

        menuOption = new PrdCategoryMenuOption2();
        menuOption.setTag("</ul>");
        _v.add(menuOption);
        
        rootCode = pushedRootCode;
      }

      index++;

      menuOption = new PrdCategoryMenuOption2();
      menuOption.setTag("</li>");
      
      _v.add(menuOption);

      if (index < lenD && rootCode.length() > 0 && !menuTree[index][0].startsWith(rootCode)) break;
    }
    
    return index;
  }
  
  private DbRet getMenuTree(String lang, String catId, Database database) {
    DbRet dbRet = new DbRet();
    
    QueryDataSet queryDataSet = null;
    
    HashMap<String, String> categoryTree = new HashMap<String, String>();
    
    String query = "SELECT catId,catName" + lang + ",catParentFlag,catRank FROM prdCategory WHERE catId LIKE '" + SwissKnife.sqlEncode(catId) + "%' AND catId NOT IN ('" + SwissKnife.sqlEncode(catId) + "') AND catShowFlag = '1' ORDER BY catId ASC";
    
    try {
      queryDataSet = new QueryDataSet();
      
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.ALL));
      
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();
      int qCount = queryDataSet.getRowCount();
      
      menuTree = new String[qCount][6];

      for (int i=0; i<qCount; i++) {
        categoryTree.put(queryDataSet.getString("catId"), SwissKnife.sqlDecode(queryDataSet.getString("catName" + lang)));
        queryDataSet.next();
      }
      
      queryDataSet.first();
      
      for (int i=0; i<qCount; i++) {
        String path = "";
        
        String catIdTmp = queryDataSet.getString("catId");
        if (catIdTmp.length() >= 4 && categoryTree.get(catIdTmp.substring(0, catIdTmp.length() - 2)) == null) System.out.println(databaseId + " - PrdCategoryMenu2 orphan catId = " + catIdTmp);
        
        menuTree[i][0] = SwissKnife.sqlDecode(queryDataSet.getString("catId"));
        menuTree[i][1] = SwissKnife.sqlDecode(queryDataSet.getString("catName" + lang));
        menuTree[i][2] = SwissKnife.sqlDecode(queryDataSet.getString("catParentFlag"));
        menuTree[i][3] = "";
        menuTree[i][4] = String.valueOf( queryDataSet.getInt("catRank") );
        
        for (int x=1; x<=(queryDataSet.getString("catId").length() / 2); x++) {
          if (x > 1) path += "/";

          path += SwissKnife.sefEncode( categoryTree.get(queryDataSet.getString("catId").substring(0, x*2)) );
        }
        menuTree[i][5] = path;
        
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
  
  private void sortCode(int rootDepth) throws Exception {
    int lenD = menuTree.length;
    
    String[][] menuTreeTmp = new String[lenD][menuTree[0].length];
        
    String tmpCode = null;
    
    int counter = 0;
    
    for (int i=0; i<lenD; i++) {
      
      tmpCode = menuTree[i][0];
      if (tmpCode == null) throw new Exception("PrdCategoryMenu2 tmpCode null encountered.");
      
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