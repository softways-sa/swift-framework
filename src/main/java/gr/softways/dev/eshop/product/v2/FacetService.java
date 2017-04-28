package gr.softways.dev.eshop.product.v2;

import gr.softways.dev.jdbc.Database;
import gr.softways.dev.jdbc.Load;
import gr.softways.dev.jdbc.MetaDataUpdate;
import gr.softways.dev.jdbc.QueryDataSet;
import gr.softways.dev.jdbc.QueryDescriptor;
import gr.softways.dev.util.DbRet;
import gr.softways.dev.util.Director;
import gr.softways.dev.util.SwissKnife;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Panos
 */
public class FacetService {
  
  public static List<Facet> getFacets(String categoryId) {
    if (categoryId == null) throw new RuntimeException();
    
    List<Facet> facets = new ArrayList<Facet>();
    
    String databaseId = SwissKnife.jndiLookup("swconf/databaseId");
    
    String query = "SELECT facet.id AS facet_id, facet.NAME AS facet_name, product_facet_val.FACET_VALUES_ID AS val_id, facet_values.NAME AS val_name, count(product.prdid) AS prd_count FROM product JOIN prdInCatTab ON product.PRDID = prdInCatTab.PINCPRDID JOIN product_facet_val ON product_facet_val.PRODUCT_ID = product.PRDID JOIN facet_values ON facet_values.id = product_facet_val.FACET_VALUES_ID JOIN facet ON facet.id = facet_values.FACET_ID WHERE prdInCatTab.PINCCATID = '" + SwissKnife.sqlEncode(categoryId) + "' GROUP BY facet.id, facet.NAME, product_facet_val.FACET_VALUES_ID, facet_values.NAME;";
    
    Director director = Director.getInstance();
    
    Database database = director.getDBConnection(databaseId);
    QueryDataSet queryDataSet = null;
    
    DbRet dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();
    
    try {
      queryDataSet = new QueryDataSet();
      queryDataSet.setQuery(new QueryDescriptor(database,query,null,true,Load.UNCACHED));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();
      
      Facet facet = null;
      FacetValue facetValue = null;
      
      while (queryDataSet.inBounds() == true) {
        facet = new Facet();
        facet.id = queryDataSet.getInt("facet_id");
        facet.name = queryDataSet.getString("facet_name");
        
        int idx = facets.indexOf(facet);
        if (idx != -1) {
          facet = facets.get(idx);
        }
        
        facetValue = new FacetValue();
        facetValue.id = queryDataSet.getInt("val_id");
        facetValue.name = queryDataSet.getString("val_name");
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
  
}
