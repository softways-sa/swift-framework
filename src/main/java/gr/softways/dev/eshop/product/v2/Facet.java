package gr.softways.dev.eshop.product.v2;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Panos
 */
public class Facet {
  public int id;
  public String name;
  
  public List<FacetValue> facetValues = new LinkedList<FacetValue>();

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 97 * hash + this.id;
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Facet other = (Facet) obj;
    if (this.id != other.id) {
      return false;
    }
    return true;
  }
  
}