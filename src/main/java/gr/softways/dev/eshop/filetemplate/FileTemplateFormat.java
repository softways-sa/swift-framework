package gr.softways.dev.eshop.filetemplate;

import java.io.*;
import java.util.*;
import java.math.*;

import java.sql.*;
import java.text.*;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class FileTemplateFormat {

  private Director _db;
  private String _databaseId;
  private QueryDataSet _queryDataSet = new QueryDataSet();

  /** � ������� ������������ �������������� */
  private String _FTemCode;

  /** Directory path for IN (������ ��� legacy, upload) */
  private String _inPath;

  /** Directory path for OUT (������ ���� legacy, download) */
  private String _outPath;

  public FileTemplateFormat(Director db, String databaseId, String FTemCode,
                            String inPath, String outPath) {
    _db = db;
    _databaseId = databaseId;

    _FTemCode = FTemCode;

    _inPath = inPath;
    _outPath = outPath;

    init();
  }

  public FileTemplateFormat(Director db, Database database, String FTemCode,
                            String inPath, String outPath) {
    _db = db;

    _FTemCode = FTemCode;

    _inPath = inPath;
    _outPath = outPath;

    initDB(database);
  }


  /**
   * Init with a Database object already initialized.
   *
   */
  private void initDB(Database database) {
    StringBuffer query = new StringBuffer();

    try {
      // ������ �� query ���� �� �� ��������� ����
      if (_queryDataSet.isOpen()) _queryDataSet.close();

      query.append("SELECT * FROM fileTemplate,fileTemplateFormat");
      query.append(" WHERE FTemCode = FTeFFileTemCode");
      query.append(" AND FTemCode = '");
      query.append(_FTemCode);
      query.append("' ORDER BY FTeFColOrder");

      // �������� �� query
      _queryDataSet.setQuery(new QueryDescriptor(database, query.toString(), 
                                                 null, true, Load.ALL));
      _queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      _queryDataSet.refresh();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void init() {
    Database database = null;
    StringBuffer query = new StringBuffer();

    database = _db.getDBConnection(_databaseId);

    try {
      // ������ �� query ���� �� �� ��������� ����
      if (_queryDataSet.isOpen()) _queryDataSet.close();

      query.append("SELECT * FROM fileTemplate,fileTemplateFormat");
      query.append(" WHERE FTemCode = FTeFFileTemCode");
      query.append(" AND FTemCode = '");
      query.append(_FTemCode);
      query.append("' ORDER BY FTeFColOrder");

      // �������� �� query
      _queryDataSet.setQuery(new QueryDescriptor(database, query.toString(), 
                                                 null, true, Load.ALL));
      _queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      _queryDataSet.refresh();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    _db.freeDBConnection(_databaseId, database);
  }

  /**
   * �� ����� ��� table ��� ����� ����������� � ������������
   * �������������.
   *
   */
  public String getTablename() throws DataSetException {
    return _queryDataSet.getString("FTemTablename");
  }

  public String getRemTablename() throws DataSetException {
    return _queryDataSet.getString("FTemRemTablename");
  }

  /**
   * ���������� �� ����� ��� ������ (column) ��� current
   * row.
   *
   */
  public String getColumnname() throws DataSetException {
    return SwissKnife.sqlDecode(_queryDataSet.getString("FTeFColName"));
  }

  public String getRemColumnname() throws DataSetException {
    return SwissKnife.sqlDecode(_queryDataSet.getString("FTeFRemColName"));
  }

  /**
   * � ����� ��� ������ (String,Date etc)
   *
   */
  public String getColumnType() throws DataSetException {
    return _queryDataSet.getString("FTeFColType");
  }

  public String getRemColumnType() throws DataSetException {
    return _queryDataSet.getString("FTeFRemColType");
  }

  /**
   * To format ��� ������
   *
   */
  public String getColumnFormat() throws DataSetException {
    return _queryDataSet.getString("FTeFColFormat");
  }

  /**
   * ������� ��������������� ��� �� ������� ��� ��������� �� �����.
   */
  public String getRemColumnFormat() throws DataSetException {
    return _queryDataSet.getString("FTeFRemColFormat");
  }

  /**
   * ������� ��������������� ��� �� ������� ��� ��� ������ �� �����.
   *
   */
  public int getColumnLength() throws DataSetException {
    return _queryDataSet.getInt("FTeFColLength");
  }

  public int getRemColumnLength() throws DataSetException {
    return _queryDataSet.getInt("FTeFRemColLength");
  }

  /**
   * ���������� ��� ������ ��� ����� row.
   *
   */
  public void first() throws DataSetException {
    _queryDataSet.first();
  }

  /**
   * �� ������� row.
   *
   */
  public boolean next() throws DataSetException {
    return _queryDataSet.next();
  }

  /**
   * �� �������� ���� rows.
   *
   */
  public boolean hasMore() throws DataSetException {
    return _queryDataSet.inBounds();
  }

  /**
   * �� ����������� row.
   *
   */
  public boolean previous() throws DataSetException {
    return _queryDataSet.prior();
  }

  /**
   * �� ������ ��� rows
   */
  public int getColumnCount() throws DataSetException {
    return _queryDataSet.getRowCount();
  }

  /**
   * Directory path for IN (������ ��� legacy, upload)
   *
   */
  public String getInPath() {
    return _inPath;
  }

  /**
   * Directory path for OUT (������ ���� legacy, download)
   *
   */
  public String getOutPath() {
    return _outPath;
  }

  /**
   * ������ ����� ��� ������� ��� �������������� �����
   * ��������� ����� ��������.
   *
   */
  public String getFilename() throws DataSetException {
    return _queryDataSet.getString("FTemFilename");
  }

  public boolean isColPK() throws DataSetException {
    if (_queryDataSet.getString("FTeFColPKFlag").equals("1"))
      return true;
    else return false;
  }

  public boolean isRemColPK() throws DataSetException {
    if (_queryDataSet.getString("FTeFRemColPKFlag").equals("1"))
      return true;
    else return false;
  }

  public String getDelimiter() throws DataSetException {
    return _queryDataSet.getString("FTemDelimiter");
  }

  public String getDatabaseId() {
    return _databaseId;
  }

  public Director getDb() {
    return _db;
  }

  public String getFTemCode() {
    return _FTemCode;
  }
}