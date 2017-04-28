package gr.softways.dev.eshop.product.v2;

/**
 *
 * @author Panos
 */
public class FacetValue {
  public int id;
  public String name;
  public Facet facet;

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 37 * hash + this.id;
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
    final FacetValue other = (FacetValue) obj;
    if (this.id != other.id) {
      return false;
    }
    return true;
  }

}