package gr.softways.dev.eshop.product.v2;

import gr.softways.dev.eshop.eways.Customer;
import gr.softways.dev.jdbc.Database;
import gr.softways.dev.jdbc.Load;
import gr.softways.dev.jdbc.MetaDataUpdate;
import gr.softways.dev.jdbc.QueryDataSet;
import gr.softways.dev.jdbc.QueryDescriptor;
import gr.softways.dev.util.DbRet;
import gr.softways.dev.util.Director;
import gr.softways.dev.util.SwissKnife;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Panos
 */
public class FacetService {
  
  public static List<Facet> getProductFacets(String prdId) {
    List<Facet> facets = new ArrayList<Facet>();
    
    String databaseId = SwissKnife.jndiLookup("swconf/databaseId");
    String lang = "";
    
    StringBuilder query = new StringBuilder();
    
    query.append("SELECT facet.id AS facet_id, facet.NAME").append(lang).append(" AS facet_name,");
    query.append(" facet.display_order AS facet_order, facet_values.id AS val_id, facet_values.NAME").append(lang);
    query.append(" AS val_name");
    query.append(" FROM product_facet_val JOIN facet_values ON product_facet_val.facet_values_id = facet_values.id");
    query.append(" JOIN facet ON facet.id = facet_values.FACET_ID");
    query.append(" WHERE product_facet_val.product_id = '").append(SwissKnife.sqlEncode(prdId)).append("'");
    query.append(" ORDER BY facet_values.NAME").append(lang);
    
    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(databaseId);
    QueryDataSet queryDataSet = null;
    
    DbRet dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();
    
    try {
      queryDataSet = new QueryDataSet();
      queryDataSet.setQuery(new QueryDescriptor(database, query.toString(), null, true, Load.UNCACHED));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();
      
      Facet facet = null;
      FacetValue facetValue = null;
      
      while (queryDataSet.inBounds() == true) {
        facet = new Facet();
        facet.id = queryDataSet.getInt("facet_id");
        facet.name = queryDataSet.getString("facet_name");
        facet.displayOrder = queryDataSet.getInt("facet_order");
        
        int idx = facets.indexOf(facet);
        if (idx != -1) {
          facet = facets.get(idx);
        }
        
        facetValue = new FacetValue();
        facetValue.id = queryDataSet.getInt("val_id");
        facetValue.name = queryDataSet.getString("val_name");
        facetValue.facet = facet;
        facet.facetValues.add(facetValue);
        
        if (idx != -1) {
          facets.set(idx, facet);
        }
        else {
          facets.add(facet);
        }
         
        queryDataSet.next();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (queryDataSet != null) queryDataSet.close();
    }

    database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    director.freeDBConnection(databaseId, database);
    
    Collections.sort(facets, new Comparator<Facet>() {
      @Override
      public int compare(Facet o1, Facet o2) {
        return o1.displayOrder.compareTo(o2.displayOrder);
      }
    });
    
    return facets;
  }
  
  public static List<Facet> getAdminFacets() {
    List<Facet> facets = new ArrayList<Facet>();
    
    String databaseId = SwissKnife.jndiLookup("swconf/databaseId");
    String lang = "";
    
    StringBuilder query = new StringBuilder();
    
    query.append("SELECT facet.id AS facet_id, facet.NAME").append(lang).append(" AS facet_name,");
    query.append(" facet.display_order AS facet_order, facet_values.id AS val_id, facet_values.NAME").append(lang);
    query.append(" AS val_name");
    query.append(" FROM facet_values JOIN facet ON facet.id = facet_values.FACET_ID");
    query.append(" ORDER BY facet_values.NAME").append(lang);
    
    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(databaseId);
    QueryDataSet queryDataSet = null;
    
    DbRet dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();
    
    try {
      queryDataSet = new QueryDataSet();
      queryDataSet.setQuery(new QueryDescriptor(database, query.toString(), null, true, Load.UNCACHED));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();
      
      Facet facet = null;
      FacetValue facetValue = null;
      
      while (queryDataSet.inBounds() == true) {
        facet = new Facet();
        facet.id = queryDataSet.getInt("facet_id");
        facet.name = queryDataSet.getString("facet_name");
        facet.displayOrder = queryDataSet.getInt("facet_order");
        
        int idx = facets.indexOf(facet);
        if (idx != -1) {
          facet = facets.get(idx);
        }
        
        facetValue = new FacetValue();
        facetValue.id = queryDataSet.getInt("val_id");
        facetValue.name = queryDataSet.getString("val_name");
        facetValue.facet = facet;
        facet.facetValues.add(facetValue);
        
        if (idx != -1) {
          facets.set(idx, facet);
        }
        else {
          facets.add(facet);
        }
         
        queryDataSet.next();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (queryDataSet != null) queryDataSet.close();
    }

    database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    director.freeDBConnection(databaseId, database);
    
    Collections.sort(facets, new Comparator<Facet>() {
      @Override
      public int compare(Facet o1, Facet o2) {
        return o1.displayOrder.compareTo(o2.displayOrder);
      }
    });
    
    return facets;
  }
  
  public static List<Facet> getFacets(String categoryId, HttpServletRequest request) {
    if (categoryId == null) throw new RuntimeException();
    
    List<Facet> facets = new ArrayList<Facet>();
    
    String databaseId = SwissKnife.jndiLookup("swconf/databaseId");
    String lang = SwissKnife.getSessionAttr(databaseId + ".lang", request);
    
    int customerType = 0;
    try {
      if (SwissKnife.getSessionAttr(databaseId + ".front_end.customerType", request) != null) {
        customerType = Integer.parseInt(SwissKnife.getSessionAttr(databaseId + ".front_end.customerType", request));
      }
      else {
        customerType = Customer.CUSTOMER_TYPE_RETAIL;
      }
    }
    catch (Exception e) {
      customerType = Customer.CUSTOMER_TYPE_RETAIL;
    }

    StringBuilder query = new StringBuilder();
    
    query.append("SELECT facet.id AS facet_id, facet.NAME").append(lang).append(" AS facet_name,");
    query.append(" facet.display_order AS facet_order, product_facet_val.FACET_VALUES_ID AS val_id, facet_values.NAME").append(lang);
    query.append(" AS val_name, count(product.prdid) AS prd_count");
    query.append(" FROM product JOIN prdInCatTab ON product.PRDID = prdInCatTab.PINCPRDID");
    query.append(" JOIN product_facet_val ON product_facet_val.PRODUCT_ID = product.PRDID");
    query.append(" JOIN facet_values ON facet_values.id = product_facet_val.FACET_VALUES_ID");
    query.append(" JOIN facet ON facet.id = facet_values.FACET_ID");
    query.append(" WHERE prdInCatTab.PINCCATID = '").append(SwissKnife.sqlEncode(categoryId)).append("'");
    //query.append(" AND prdCategory.catShowFlag = '1'");
    if (customerType == Customer.CUSTOMER_TYPE_WHOLESALE) {
      query.append(" AND product.prdHideFlagW = '0'");
    }
    else {
      query.append(" AND product.prdHideFlag = '0'");
    }
    query.append(" GROUP BY facet.id, facet.NAME").append(lang).append(", facet.display_order, product_facet_val.FACET_VALUES_ID, facet_values.NAME").append(lang);
    query.append(" ORDER BY facet_values.NAME").append(lang);
    
    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(databaseId);
    QueryDataSet queryDataSet = null;
    
    DbRet dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();
    
    try {
      queryDataSet = new QueryDataSet();
      queryDataSet.setQuery(new QueryDescriptor(database, query.toString(), null, true, Load.UNCACHED));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();
      
      Facet facet = null;
      FacetValue facetValue = null;
      
      while (queryDataSet.inBounds() == true) {
        facet = new Facet();
        facet.id = queryDataSet.getInt("facet_id");
        facet.name = queryDataSet.getString("facet_name");
        facet.displayOrder = queryDataSet.getInt("facet_order");
        
        int idx = facets.indexOf(facet);
        if (idx != -1) {
          facet = facets.get(idx);
        }
        
        facetValue = new FacetValue();
        facetValue.id = queryDataSet.getInt("val_id");
        facetValue.name = queryDataSet.getString("val_name");
        facetValue.facet = facet;
        facet.facetValues.add(facetValue);
        
        if (idx != -1) {
          facets.set(idx, facet);
        }
        else {
          facets.add(facet);
        }
         
        queryDataSet.next();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (queryDataSet != null) queryDataSet.close();
    }

    database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    director.freeDBConnection(databaseId, database);
    
    Collections.sort(facets, new Comparator<Facet>() {
      @Override
      public int compare(Facet o1, Facet o2) {
        return o1.displayOrder.compareTo(o2.displayOrder);
      }
    });
    
    return facets;
  }
  
  public static List<FacetValue> getFacetValuesFromQuery(String query) {
    if (StringUtils.isBlank(query)) throw new IllegalArgumentException();
    
    List<FacetValue> facetValues = new ArrayList<FacetValue>();
    
    for (String q : query.split(",")) {
      String[] parts = q.split("@");
      if (parts.length == 2) {
        Facet facet = new Facet();
        facet.id = Integer.parseInt(parts[0]);
        
        FacetValue facetValue = new FacetValue();
        facetValue.id = Integer.parseInt(parts[1]);
        
        facetValues.add(facetValue);
      }
    }
    
    return facetValues;
  }
  
  public static String addFacetValueToQuery(String query, FacetValue facetValue) {
    query += facetValue.facet.id + "@" + facetValue.id + ",";
    
    return query;
  }
  
  public static String removeFacetValueFromQuery(String query, FacetValue facetValue) {
    return query.replace(facetValue.facet.id + "@" + facetValue.id + ",", "");
  }
  
}