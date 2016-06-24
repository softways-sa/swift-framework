package gr.softways.dev.eshop.filetemplate;

import java.io.*;
import java.util.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class FileTemplatePacket {
  private Director _dbBean;

  private Database _database;
  
  //private QueryDataSet _queryDataSet;

  private String _databaseId;

  private String _outPath;
  private String _inPath;

  private String _FTemCode;

  public FileTemplatePacket() {
  }

  public FileTemplatePacket(Director bean, String databaseId, String FTemCode,
                            String inPath, String outPath) {
    _dbBean = bean;
    _databaseId = databaseId;

    _database = _dbBean.getDBConnection(_databaseId);
    _FTemCode = FTemCode;
  }

  public Database getDatabase() {
    return _database;
  }
  
  public void setDatabase(Database database) {
    _database = database;
  }
} 