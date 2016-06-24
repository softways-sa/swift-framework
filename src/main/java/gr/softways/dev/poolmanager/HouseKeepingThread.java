package gr.softways.dev.poolmanager;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.LogWriter;
import java.util.*;

public class HouseKeepingThread extends TimerTask {
  
  AppConnPool appConnPool = null;
  Vector v = null;
  
  public HouseKeepingThread(AppConnPool appConnPool) {
    this.appConnPool = appConnPool;
  }
  
  public void run() {
    appConnPool.logWriter.log("HouseKeepingThread - start",LogWriter.DEBUG);
    
    try {
      doTask();
    }
    catch (Exception e) {
      appConnPool.logWriter.log("HouseKeepingThread - error",LogWriter.ERROR);
      e.printStackTrace();
    }
    
    appConnPool.logWriter.log("HouseKeepingThread - finish",LogWriter.DEBUG);
  }
  
  private void doTask() {
    synchronized (appConnPool) {
      
      int totalDatabaseConnections = appConnPool.getTotalDatabaseConnections();
      
      if (totalDatabaseConnections > appConnPool.getMaxSpareDBConn()) {
        long nowMillis = System.currentTimeMillis();
        long maxIdleTimeMillis = appConnPool.getMaxIdleTime() * 60 * 1000;
      
        for (int i=0; i<appConnPool.getMaxConns(); i++) {
          if ( appConnPool.getDBConnections(i,0) != null  
              && ((String)appConnPool.getDBConnections(i,1)).equals("1") ) {
            
            if ((nowMillis - ((Long)appConnPool.getDBConnections(i,2)).longValue()) >= maxIdleTimeMillis
                || appConnPool.isConnectionOK((Database)appConnPool.getDBConnections(i,0)) == false) {
              appConnPool.logWriter.log("HouseKeepingThread - mark connection for close",LogWriter.INFO);
              appConnPool.releaseDBConnection( (Database)appConnPool.getDBConnections(i,0) );

              totalDatabaseConnections--;

              if (totalDatabaseConnections <= appConnPool.getMaxSpareDBConn()) break;
            }
            
          }
        }
      }
      
    }
  }
}