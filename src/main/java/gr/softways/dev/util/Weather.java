// http://wow.weather.com/weather/wow/rebuildmodule/GRXX0019?config=SZ=180x150*WX=HWF*LNK=SSNL*UNT=C*BGC=f8f6e7*MAP=null|null*DN=softways.gr*TIER=0*PID=1288915335*MD5=aa32cd5d6e942a08cadafc7224388f1c&proto=http&target=wx_module_1379
// http://imawow.weather.com/web/common/wxicons/36/0.gif

package gr.softways.dev.util;

import java.io.*;
import java.net.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
  
/**
 *
 * @author  minotauros
 */
public class Weather {
  
  protected Weather() {
  }
  
  /**
   * thessaloniki weather
   */
  public static DbRet getWeather() {
    DbRet dbRet = new DbRet();
    
    HttpURLConnection con = null;
    BufferedReader in = null;
    
    try {
      con = (HttpURLConnection) new URL("http://wow.weather.com/weather/wow/rebuildmodule/GRXX0019?config=SZ=180x150*WX=HWF*LNK=SSNL*UNT=C*BGC=f8f6e7*MAP=null|null*DN=softways.gr*TIER=0*PID=1288915335*MD5=aa32cd5d6e942a08cadafc7224388f1c&proto=http&target=wx_module_1379").openConnection();
      con.setRequestMethod("GET");
      con.setUseCaches(false);

      in = new BufferedReader(new InputStreamReader(con.getInputStream()));

      String result = "", line = null;
      while((line = in.readLine()) != null) {
        result += line;
      }
      
      Pattern pattern = Pattern.compile("<img\\s+src=.*/36/([^\"]+).gif", Pattern.DOTALL | Pattern.UNIX_LINES | Pattern.CASE_INSENSITIVE);
      Matcher m = pattern.matcher(result);
      if (m.find()) {
        dbRet.setRetInt( Integer.parseInt(m.group(1)) );
        
        pattern = Pattern.compile("<b>(.*)&deg;", Pattern.DOTALL | Pattern.UNIX_LINES | Pattern.CASE_INSENSITIVE);
        m = pattern.matcher(result);
        if (m.find()) {
          dbRet.setRetStr( m.group(1) );
        }
      }
    }
    catch (Exception e) {
      dbRet.setNoError(0);
    }
    
    return dbRet;
  }
}