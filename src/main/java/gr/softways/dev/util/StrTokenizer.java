package gr.softways.dev.util;

import java.lang.*;
import java.io.*;
import java.util.*;

public class StrTokenizer {

  private Vector v = new Vector();

  private int _count = 0;
  private int _currentTokenIndex = 0;

  public StrTokenizer(String s, char d) {
    String temp = "";
    
    if (s == null) {
        s = "";
    }
    
    int length = s.length();

    for (int i = 0; i < length; i++){
      if (!(s.charAt(i) == d) ){
        temp += s.charAt(i);
      }
      else {
        v.addElement(temp);
        temp = "";
      }
    }
    
    v.addElement(temp);
    
    _count = v.size();
  }

  public int countTokens() {    
    return _count;
  }

  public String nextToken() {

    String temp = (String)v.elementAt(_currentTokenIndex);

    _currentTokenIndex += 1;

    return temp;
  }

  public boolean hasMoreTokens(){
    return  _currentTokenIndex < _count;
  }

  /**
   * Επαναφορά του index στο πρώτο token.
   */
  public void resetTokenizer() {
    _currentTokenIndex = 0;
  }
  
  public static void main(String args[]) {
    String s1=";chr;ist;  j;ινα;";
    char d1 = ';';
    
    StrTokenizer str = new StrTokenizer(s1, d1);
    
    int countTokens = str.countTokens();
    
    for (int i=0; i<countTokens; i++) {
      System.out.println(str.nextToken());
    }
    
    if (str.hasMoreTokens() == false) {
      str.resetTokenizer();
    }
    
    while (str.hasMoreTokens()) {
      System.out.println(str.nextToken());
    }
    
    System.out.println(str.nextToken());
  }
}