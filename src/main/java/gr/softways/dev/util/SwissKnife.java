/*
 * SwissKnife.java
 *
 * Created on 8 Ιούλιος 2003, 11:33 πμ
 */

package gr.softways.dev.util;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.poolmanager.*;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.StringTokenizer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author  minotauros
 */
public class SwissKnife {
  
  public static String DEFAULT_CHARSET = "UTF-8";
  
  public static Locale Locale_GR = new Locale("el", "GR");

  protected SwissKnife() {
  }
  
  public static String buildQueryString(HttpServletRequest request,String query,
                                        String order, String[] pNames1,
                                        String[] pNames2, String[] pNames3,
                                        String[] qParts, String[] pTypes,
                                        String[] heads, String[] feet,
                                        String clause, String oper, int pCnt) {
    String[][] sPms = new String[pCnt][3];

    try {
      for (int i=0; i<pCnt; i++) {
        fillParam(request, sPms, i, pNames1[i], pNames2[i], pNames3[i],
                  qParts[i], pTypes[i], heads[i], feet[i]);
      }
    }
    catch (Exception e) { e.printStackTrace(); }

    return constructQuery(sPms, pCnt, query, clause, oper, order);
  }

  private static void fillParam(HttpServletRequest request, String [][] sPms,
                         int i, String pName, String pName2,
                         String pName3, String qPart, String pType,
                         String head, String foot) {
    String p1 = "", p2 = "", p3 = "", pall = "";
    int m = 0;
    if ( (p1 = request.getParameter(pName)) != null) {
      p1 = sqlEncode(p1.trim());
    }
    else p1 = "";
    
    if (pType.equalsIgnoreCase("UP") || pType.equalsIgnoreCase("KWORDS_UP")) {
      p1 = searchConvert(p1);
    }

    StringTokenizer st = null;
    if (pType.equalsIgnoreCase("KWORDS") 
          || pType.equalsIgnoreCase("KWORDS_UP") && p1.length()>0) {
      st = new StringTokenizer(p1, " ");
      p1 = "";

      while (st.hasMoreTokens() == true) {
        p1 += head + st.nextToken() + foot;
        if (st.hasMoreTokens() == true)
          p1 += "~@~";
      }
    }

    if (pType.equals("D")) {
      if ((p2 = request.getParameter(pName2)) != null && !p2.equals(""))
        p2= "-" + p2.trim() + "-";
      else {
        if (!p1.equals("")) {
          if (qPart.indexOf(">=") != -1)
            p2 = "-01-";
          else if (qPart.indexOf("<=") != -1)
            p2 = "-12-";
        }
        else
          p2= "--";          
      }
      if ((p3 = request.getParameter(pName3)) != null && !p3.equals(""))
        p3= p3.trim();
      else {
        if (!p2.equals("--")) {
          if (qPart.indexOf(">=") != -1)
            p3 = "01";
          else if (qPart.indexOf("<=") != -1) {
            try {
              m = Integer.parseInt(p2.substring(1,3));
            }
            catch(Exception e) {
              try {
                m = Integer.parseInt(p2.substring(1,2));           
              }
              catch(Exception ee) {
                m = 2;
              }
            }                       
            if (m==4 || m==6 || m==9 || m==11)  
              p3 = "30";
            else if (m==2)
              p3 = "28";
            else
              p3 = "31";
          }
        }
        else
          p3= "";
      }  
     
      if (!p3.equals("") && qPart.indexOf(">=") != -1) {
        p3 += " 00:00:00.0";
      }
      else if (!p3.equals("") && qPart.indexOf("<=") != -1) {
        p3 += " 23:59:59.9";
      }
    }

    pall = p1 + p2 + p3;

    if (pall.equals("") || (pall.equals("--") && pType.equals("D"))) {
      head = "";
      foot = "";
    }

    if (pType.equalsIgnoreCase("KWORDS") || pType.equalsIgnoreCase("KWORDS_UP")) {
      sPms[i][0] = pall;
      sPms[i][1] = qPart;
      sPms[i][2] = pType;
    }
    else {
      sPms[i][0] = head + pall + foot;
      sPms[i][1] = qPart;
      sPms[i][2] = pType;
    }
  }
  
  private static String constructQuery(String [][] sPms, int pCnt, String query,
                                       String clause, String oper,
                                       String order) {
    boolean first = true;

    for (int i=0; i< pCnt; i++)
      if (sPms[i][2].equalsIgnoreCase("C") || sPms[i][2].equalsIgnoreCase("UP")) {
        if (!sPms[i][0].equals("")) {
          if (first) query += clause;
          else query += oper;

          query += sPms[i][1] + " " + sPms[i][0];
          first = false;
        }
      }
      else if (sPms[i][2].equalsIgnoreCase("KWORDS") || sPms[i][2].equalsIgnoreCase("KWORDS_UP")) {
        if (!sPms[i][0].equals("")) {
          if (first) query += clause;
          else query += oper;

          StringTokenizer st = new StringTokenizer(sPms[i][0], "~@~");

          query += " (";
          while (st.hasMoreTokens() == true) {
            query += sPms[i][1] + " " + st.nextToken() + " ";
            if (st.hasMoreTokens() == true) query += " OR ";
          }
          query += ")";

          first = false;
        }
      }
      else if (sPms[i][2].equalsIgnoreCase("D")) {
        if (!sPms[i][0].equals("--")) {
          if (first) query += clause;
          else query += oper;

          query += sPms[i][1] + " " + sPms[i][0];
          first = false;
        }
      }
      else if (sPms[i][2].equalsIgnoreCase("N")) {
        if (!sPms[i][0].equals("")) {
          if (first) query += clause;
          else query += oper;

          query += sPms[i][1] + " " + sPms[i][0];
          first = false;
        }
      }

    query += order;
    return query;
  }
  
  /**
   *
   */
  public static String sqlEncode(String str) {
    // escape character used ^
    if (str == null) return "";

    int strLength = str.length();
    StringBuffer e = new StringBuffer(strLength);

    for (int i=0; i<strLength; i++) {
      char c = str.charAt(i);
      if ( "^'".indexOf(c) >= 0) {
        e.append("^");
        e.append(Integer.toString(c,16));
      }
      else e.append(c);
    }
    return e.toString();
  }
  
  /**
   *
   */
  public static String sqlDecode(String str) {
    // escape character used ^
    if (str == null) return "";

    int strLength = str.length();
    StringBuffer d = new StringBuffer(strLength);
    String hex = "";

    for (int i=0; i<strLength; i++) {
      char c = str.charAt(i);
      if (c == '^') {
        hex = str.substring(i+1,i+3);
        char dec = (char) Integer.parseInt(hex,16);
        d.append(dec);
        i += 2;
      }
      else d.append(c);
    }
    return d.toString();
  }
  
  /**
   *  Μετάτρεψε το string σε case-insensitive και τόνο
   * insensitive.
   */
  public static String searchConvert(String stringToConvert) {
    if (stringToConvert == null || stringToConvert.equals(""))
      return "";

    int charsLength = stringToConvert.length();

    // πριν τοποθετήσεις το string στον πίνακα χαρακτήρων
    // μετάτρεψε τους χαρακτήρες σε κεφαλαίους
    char[] chars = stringToConvert.toUpperCase().toCharArray();

    StringBuffer string = new StringBuffer(charsLength);

    for (int i=0; i<charsLength; i++) {
      switch (chars[i]) {
        case 'Ά' : string.append('Α');
                   break;
        case 'Έ' : string.append('Ε');
                   break;
        case 'Ή' : string.append('Η');
                   break;
        case 'Ϊ' :
        case 'Ί' : string.append('Ι');
                   break;
        case 'ό' :
        case 'Ό' : string.append('Ο');
                   break;
        case 'Ϋ' :
        case 'Ύ' : string.append('Υ');
                   break;
        case 'Ώ' : string.append('Ω');
                   break;
        default: string.append(chars[i]);
      }
    }
    
    return string.toString();
  }

  
  private static Context _envCtx = null;
  private static Object _envCtxLock = new Object();
  static {
    try {
      _envCtx = (Context) new InitialContext().lookup("java:comp/env");
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  public static String jndiLookup(String name) {
    String value = null;
    
    try {
      synchronized (_envCtxLock) {
        value = (String) _envCtx.lookup(name);
      }
    }
    catch (Exception e) {
      value = null;
      //e.printStackTrace();
    }
    
    return value;
  }
  
  /**
   *
   */
  public static Timestamp currentDate() {
    return new Timestamp(System.currentTimeMillis());
  }
  
  /**
   * Κατασκευή primary key βάση της ημ/νίας. Never give same key twice.
   */
  //private static SimpleDateFormat _unique_pk_format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
  private static String _unique_pk = null;
  
  public static synchronized String buildPK() {
    String PK = null;
    
    do {
      PK = Long.toString(System.currentTimeMillis(), 32).trim().toUpperCase();
    }
    while (PK.equals(_unique_pk));
    
    _unique_pk = PK;
    
    return _unique_pk;
  }
  
  private static Object _fixedDateFormatLock = new Object();
  private static SimpleDateFormat _fixedDateFormat = new SimpleDateFormat("d M yyyy H m s S");
  public static Timestamp buildTimestamp(String day, String month, String year) {
    return buildTimestamp(day, month, year, null, null, null, null);
  }
  /**
   * Hour must be between 0-23
   */
  public static Timestamp buildTimestamp(String day, String month, String year, 
                                         String hour, String minutes, 
                                         String seconds, String milliseconds) {
    Timestamp timestamp = null;
    
    if (hour == null || hour.equals("")) {
      hour = "0";
    }
    if (minutes == null || minutes.equals("")) {
      minutes = "0";
    }
    if (seconds == null || seconds.equals("")) {
      seconds = "0";
    }
    if (milliseconds == null || milliseconds.equals("")) {
      milliseconds = "0";
    }
    
    try {
      synchronized (_fixedDateFormatLock) {
        timestamp = new Timestamp(_fixedDateFormat.parse(day + " " 
                                                       + month + " "
                                                       + year + " "
                                                       + hour + " "
                                                       + minutes + " "
                                                       + seconds + " "
                                                       + milliseconds + " "
                                                        ).getTime());
      }
    }
    catch (Exception e) {
      timestamp = null;
    }
    
    return timestamp;
  }
  
  private static Object _genericDateFormatLock = new Object();
  private static SimpleDateFormat _genericDateFormat = new SimpleDateFormat();
  public static String formatDate(Timestamp timestamp, String format) {
    try {
      synchronized (_genericDateFormatLock) {
        _genericDateFormat.applyPattern(format);
        return _genericDateFormat.format(timestamp);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }
  
  private static Calendar _calendar = Calendar.getInstance();
  
  public static String getTDateStr(Timestamp timestamp, String dateField) {
    _calendar.setTime(timestamp);

    String date = "";

    if (dateField.equalsIgnoreCase("WEEKDAY"))
      date = String.valueOf( _calendar.get(Calendar.DAY_OF_WEEK) );
    else if (dateField.equalsIgnoreCase("DAY"))
      date = String.valueOf( _calendar.get(Calendar.DAY_OF_MONTH) );
    else if (dateField.equalsIgnoreCase("MONTH"))
      date = String.valueOf( _calendar.get(Calendar.MONTH) + 1);
    else if (dateField.equalsIgnoreCase("YEAR"))
      date = String.valueOf( _calendar.get(Calendar.YEAR) );
    else if (dateField.equalsIgnoreCase("HOUR"))
      date = String.valueOf( _calendar.get(Calendar.HOUR) );
    else if (dateField.equalsIgnoreCase("HOUR_OF_DAY"))
      date = String.valueOf( _calendar.get(Calendar.HOUR_OF_DAY) );
    else if (dateField.equalsIgnoreCase("AM_PM"))
      date = String.valueOf( _calendar.get(Calendar.AM_PM) );
    else if (dateField.equalsIgnoreCase("MINUTE"))
      date = String.valueOf( _calendar.get(Calendar.MINUTE) );
    else if (dateField.equalsIgnoreCase("SECONDS"))
      date = String.valueOf( _calendar.get(Calendar.SECOND) );

    return date;
  }

  public static int getTDateInt(Timestamp timestamp, String dateField) {
    _calendar.setTime(timestamp);
    
    int date = 0;

    if (dateField.equalsIgnoreCase("WEEKDAY"))
      date = _calendar.get(Calendar.DAY_OF_WEEK);
    else if (dateField.equalsIgnoreCase("DAY_OF_YEAR"))
      date = _calendar.get(Calendar.DAY_OF_YEAR);
    else if (dateField.equalsIgnoreCase("WEEK_OF_YEAR"))
      date = _calendar.get(Calendar.WEEK_OF_YEAR);
    else if (dateField.equalsIgnoreCase("DAY_OF_WEEK_IN_MONTH"))
      date = _calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH);
    else if (dateField.equalsIgnoreCase("WEEK_OF_MONTH"))
      date = _calendar.get(Calendar.WEEK_OF_MONTH);
    else if (dateField.equalsIgnoreCase("DAY"))
      date = _calendar.get(Calendar.DAY_OF_MONTH);
    else if (dateField.equalsIgnoreCase("MONTH"))
      date = _calendar.get(Calendar.MONTH) + 1;
    else if (dateField.equalsIgnoreCase("YEAR"))
      date = _calendar.get(Calendar.YEAR);
    else if (dateField.equalsIgnoreCase("AM_PM"))
      date = _calendar.get(Calendar.AM_PM);
    else if (dateField.equalsIgnoreCase("HOUR"))
      date = _calendar.get(Calendar.HOUR);
    else if (dateField.equalsIgnoreCase("HOUR_OF_DAY"))
      date = _calendar.get(Calendar.HOUR_OF_DAY);
    else if (dateField.equalsIgnoreCase("MINUTE"))
      date = _calendar.get(Calendar.MINUTE);
    else if (dateField.equalsIgnoreCase("SECONDS"))
      date = _calendar.get(Calendar.SECOND);

    return date;
  }
  
  /**
   *
   */
  public static String hexEscape(String str) {
    int stringLength = str.length();
    
    StringBuffer stringBuffer = new StringBuffer();

    char c;
    for (int i=0; i<stringLength; i++) {
      c = str.charAt(i);
      if ( c == '&') {
        stringBuffer.append("%26");
      }
      else if (c == '+') {
        stringBuffer.append("%2B");
      }
      else if (c == '%') {
        stringBuffer.append("%25");
      }
      else if (c == '#') {
        stringBuffer.append("%23");
      }
      else if (c == '=') {
        stringBuffer.append("%3D");
      }
      else if (c == ' ') {
        stringBuffer.append("%20");
      }
      else if (c == '\"') {
        stringBuffer.append("%22");
      }
      else if (c == '\'') {
        stringBuffer.append("%27");
      }
      else if (c == '?') {
        stringBuffer.append("%3F");
      }
      else if (c == '/') {
        stringBuffer.append("%2F");
      }
      else if (c == '!') {
        stringBuffer.append("%21");
      }
      else if (c == '*') {
        stringBuffer.append("%2A");
      }
      else if (c == '(') {
        stringBuffer.append("%28");
      }
      else if (c == ')') {
        stringBuffer.append("%29");
      }
      else if (c == ';') {
        stringBuffer.append("%3B");
      }
      else if (c == ':') {
        stringBuffer.append("%3A");
      }
      else if (c == '@') {
        stringBuffer.append("%40");
      }
      else if (c == '$') {
        stringBuffer.append("%24");
      }
      else if (c == ',') {
        stringBuffer.append("%2C");
      }
      else if (c == '[') {
        stringBuffer.append("%5B");
      }
      else if (c == ']') {
        stringBuffer.append("%5D");
      }
      else {
        stringBuffer.append(c);
      }
    }
    
    return stringBuffer.toString();
  }
  
  public static String getSessionAttr(String name, HttpServletRequest request) {
    String value = "";
    
    HttpSession s = request.getSession(false);
    
    if (s != null && s.getAttribute(name) != null) value = s.getAttribute(name).toString();
    
    return value;
  }
  
  public static Object getSessionAttr(HttpServletRequest request, String name) {
    Object value = "";
    
    HttpSession s = request.getSession(false);
    
    if (s != null && s.getAttribute(name) != null) value = s.getAttribute(name);
    
    return value;
  }
  
  public static String formatHTML(String text) {
    int length = text.length();
    
    StringBuffer buffer = new StringBuffer();
    
    for (int i=0; i<length; i++) {
      if (text.charAt(i) == '\n') {
        buffer.append("<BR>");
      }
      else {
        buffer.append(text.charAt(i));
      }
    }
    
    return buffer.toString();
  }
  
  public static String grEncode(String str) {
    if (str == null) str = "";
    
    return str;
  }
  
  public static boolean locateOneRow(String columnName, String columnValue, 
                                     QueryDataSet queryDataSet) {
    boolean found = false;
    
    try {
      DataRow row = new DataRow(queryDataSet, columnName);
      row.setString(columnName, columnValue);

      found = queryDataSet.locate(row, Locate.FIRST);
    }
    catch (Exception e) {
      found = false;
      e.printStackTrace();
    }
    
    return found;
  }
  
  /*
   * Επιστρέφει τον αριθμό των αρχείων που περιέχονται
   * μέσα στο directory.
   *
   * @param dirPath  το path του directory
  */
  public static int getDirListLength(String dirPath){
    File file = new File(dirPath);

    File[] tableFile = file.listFiles();

    int fileNumber = 0, tableSize = 0;

    if (tableFile != null){
      tableSize = tableFile.length;
      for (int i = 0; i < tableSize; i++){
        if (tableFile[i].isFile())
          fileNumber++;
      }
    }

    file = null;
    tableFile = null;
    
    return fileNumber;
  }

  public static boolean fileExists(String FilePath) {
    File file = new File(FilePath);

    if (file.exists()) {
      return true;
    }
    else {
      return false;
    }
  }
  
  public static boolean fileExists(String filePath, String filename) {
    File file = new File(filePath + filename);

    try {
      if (file.exists() == true && filename.equals( file.getCanonicalFile().getName() ) == true) return true;
      else return false;
    }
    catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
  
  public static String formatNumber(Object number, 
                                    String lang, String country,
                                    int minFractionDigits,
                                    int maxFractionDigits) {
    Locale locale = null;
    
    String s = null;
    
    if (country != null && country.equals("GR")) {
      locale = Locale_GR;
    }
    else if (country != null && country.equals("UK")) {
      locale = Locale.UK;
    }
    else if (country != null && country.equals("US")) {
      locale = Locale.US;
    }
    else {
      locale = new Locale(lang, country);
    }
    
    NumberFormat nf = NumberFormat.getNumberInstance(locale);
    
    nf.setMinimumFractionDigits(minFractionDigits);
    nf.setMaximumFractionDigits(maxFractionDigits);
    
    s = nf.format(number);
    
    nf = null;
    
    return s;
  }
  
  public static BigDecimal parseBigDecimal(String s, 
                                       String lang, String country) {
    if (s == null || country == null || lang == null) return null;
    
    Locale locale = null;
    
    BigDecimal big = null;
    
    if (country != null && country.equals("GR")) {
      locale = Locale_GR;
    }
    else if (country != null && country.equals("UK")) {
      locale = Locale.UK;
    }
    else if (country != null && country.equals("US")) {
      locale = Locale.US;
    }
    else {
      locale = new Locale(lang, country);
    }
    
    NumberFormat nf = NumberFormat.getNumberInstance(locale);
    
    try {
      big = new BigDecimal( String.valueOf(nf.parse(s).doubleValue()) );
    }
    catch (Exception e) {
    }
    
    nf = null;
    
    return big;
  }
  
  public static DbRet authenticate(String databaseId,
                                   String authUsername, String authPassword,
                                   String securityObject,
                                   int permissions) {
    DbRet dbRet = new DbRet();
    
    Director director = Director.getInstance();
    
    int auth = director.auth(databaseId,
                             authUsername, authPassword,
                             securityObject, permissions);

    if (auth != ACLInterface.AUTH_OK) {
      dbRet.setNoError(0);

      dbRet.setAuthError(1);
      dbRet.setAuthErrorCode(auth);
    }
    
    director = null;
    
    return dbRet;
  }
  
  public static int strToInt(String s) {
    int i = 0;
    
    try {
      i = Integer.parseInt(s);
    }
    catch (Exception e) {
    }
    
    return i;
  }
  
  public static String sefEncode(String url) {
    String sefURL = "";
    
    if (url == null) return sefURL;
    
    sefURL = url.replace("!", "-").replace("'", "-").replace("\"", "-").replace(" ","-").replace("&", "-").replace(",","-").replace(".","-").replace("(","-").replace(")","-").replace("#","-").replace("`","-").replace("%","-").replace("$","-").replace("^","-").replace("*","-").replace("+","-").replace("=","-").replace(";","-").replace(":","-").replace("<","-").replace(">","-").replace("?","-").replace("|","-").replace("{","-").replace("}","-").replace("@","-").replace("~","-").replace("+","-").replace("_","-").replace("/","-").replace("\\","-").replace("[","-").replace("]","-").replace("\u00A0", "-");
    
    return sefURL;
  }
}