package gr.softways.dev.swift.cmrow;

import java.util.HashMap;
import java.util.Map;

public class CMRowAttribs {

  private Map<String, String> attribs = new HashMap();
  
  public CMRowAttribs() {
  }
  public CMRowAttribs(String attribs) {
    String[] attrs = attribs.split(ATTRIB_SEP);
    
    for (int i=0; i<attrs.length; i++) {
      String[] pairs = attrs[i].split("\\" + KEY_VALUE_SEP, 2);
      
      this.attribs.put(pairs[0], pairs.length == 1 ? "" : pairs[1]);
    }
  }
  
  public String getValue(String key) {
    String value;
    
    value = attribs.get(key);
    
    return value;
  }
  
  public static final String PREFIX = "CMRAttribs_";
  public static final String ATTRIB_SEP = "\n";
  public static final String KEY_VALUE_SEP = "|";
}