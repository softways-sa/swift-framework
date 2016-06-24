package gr.softways.dev.eshop.filetemplate;

import java.io.*;
import java.util.*;
import java.math.*;

import java.sql.*;
import java.text.SimpleDateFormat;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

public class TXTFileTemplate implements FileTemplate {

  FileTemplateFormat _fileTemplateFormat = null;
  Director director = Director.getInstance();

  private int ERROR_COLUMNS_MISMATCH = 1;
  private int ERROR_COLUMNS_RANGE = 2;

  private String _fileCharset = "8859_7";

  public TXTFileTemplate() {
  }

  public DbRet doAction(String operation, FileTemplateFormat fileTemplateFormat) {
    _fileTemplateFormat = fileTemplateFormat;
    DbRet dbRet = null;

    //System.out.println(operation + " txt implementation");

    if (TEMPLATE_OP_IN.equals(operation)) {
      dbRet = doIN();
    }
    else if (TEMPLATE_OP_OUT.equals(operation)) {
      dbRet = doOUT();
    }
    else if (TEMPLATE_OP_INNew.equals(operation)) {
      dbRet = doINNew();
    }

    return dbRet;
  }

  /**
   * Ανάγνωση από legacy σύστημα και ανανέωση της βάσης δεδομένων
   * βάση της επιλεγμένης γραμμογράφησης.
   * Τα columns που αποτελούν το primary key μπορεί να είναι μέχρι 3.
   *
   */
  protected DbRet doIN() {
    DbRet dbRet = new DbRet();

    Database database = null;
    String databaseId = _fileTemplateFormat.getDatabaseId();
    PreparedStatement preparedStatement = null;
    StringBuffer query = new StringBuffer();

    SimpleDateFormat dateFormatter =
                new java.text.SimpleDateFormat();

    String[] FTeFColPK = {"", "", ""};
    String[] typePK = {"", "", ""};
    int[] ordinalPK = new int[3];
    int indexPK = -1;

    String filename = null, delimiter = null;

    BufferedReader file = null;

    int colCount = 0;

    database = director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);

    int prevTransIsolation = dbRet.getRetInt();

    int line = 0;

    try {
      filename = _fileTemplateFormat.getInPath()
               + _fileTemplateFormat.getFilename();

      //System.out.println("filename = " + filename);

      delimiter = _fileTemplateFormat.getDelimiter();

      colCount = _fileTemplateFormat.getColumnCount();

      
      _fileTemplateFormat.first();

      query.append("UPDATE ");
      query.append(_fileTemplateFormat.getTablename());
      query.append(" SET");

      // init columns & find primary keys
      for (int i=0; i<colCount; i++) {
        if (_fileTemplateFormat.isColPK() == true && indexPK<3) {
          indexPK++;
          ordinalPK[indexPK] = i;
          typePK[indexPK] = _fileTemplateFormat.getColumnType();
          FTeFColPK[indexPK] = _fileTemplateFormat.getColumnname();
        }

        // <<χτίσιμο>> του prepared statement
        if (i==0) query.append(" ");
        else query.append(", ");
        query.append(_fileTemplateFormat.getColumnname());
        query.append(" = ? ");

        _fileTemplateFormat.next();
      }

      query.append(" WHERE ");
      // προσθήκη primary keys στο prepared statement
      for (int i=0; i<=indexPK; i++) {
        if (i>0)
          query.append(" AND ");
        query.append(FTeFColPK[i]);
        query.append(" = ?");
      }

      //System.out.println(query.toString());
      
      preparedStatement = database.createPreparedStatement(query.toString());

      file = new BufferedReader(new InputStreamReader(new FileInputStream(filename),
                                                      _fileCharset));

      // τα δεδομένα των columns
      String[] columnDataArray = new String[colCount];

      String lineStr = null;

      // ανάγνωση στοιχείων και εκτέλεση prepared statement
      while ( (lineStr = file.readLine()) != null && dbRet.getNoError() == 1) {
        line++;

        _fileTemplateFormat.first();

        dbRet = parseLine(_fileTemplateFormat, lineStr, columnDataArray);

        if (dbRet.getNoError() == 1) {
          _fileTemplateFormat.first();

          for (int i=0; i<colCount; i++) {
            if (TYPE_STRING.equals(_fileTemplateFormat.getColumnType())) {
              preparedStatement.setString(i+1,SwissKnife.sqlEncode(columnDataArray[i]));
            }
            else if (TYPE_INT.equals(_fileTemplateFormat.getColumnType())) {
              preparedStatement.setInt(i+1,Integer.parseInt(columnDataArray[i]));
            }
            else if (TYPE_BIGDECIMAL.equals(_fileTemplateFormat.getColumnType())) {
              preparedStatement.setBigDecimal(i+1,new BigDecimal(columnDataArray[i]));
            }
            else if (TYPE_DATE.equals(_fileTemplateFormat.getColumnType())) {
              dateFormatter.applyPattern(_fileTemplateFormat.getColumnFormat());
              if (columnDataArray[i].trim().equals(""))
                preparedStatement.setNull(i+1, Types.TIMESTAMP);
              else
                preparedStatement.setTimestamp(i+1,new Timestamp( dateFormatter.parse(columnDataArray[i]).getTime() ) );
            }

            _fileTemplateFormat.next();

          }
          // τοποθέτηση των primary keys
          for (int i=0; i<=indexPK; i++) {
            if (TYPE_STRING.equals(typePK[i])) {
              preparedStatement.setString(colCount+i+1,SwissKnife.sqlEncode(columnDataArray[ordinalPK[i]]));
            }
            else if (TYPE_INT.equals(typePK[i])) {
              preparedStatement.setInt(colCount+i+1,Integer.parseInt(columnDataArray[ordinalPK[i]]));
            }
            else if (TYPE_BIGDECIMAL.equals(typePK[i])) {
              preparedStatement.setBigDecimal(colCount+i+1,new BigDecimal(columnDataArray[ordinalPK[i]]));
            }
            else if (TYPE_DATE.equals(_fileTemplateFormat.getColumnType())) {
              dateFormatter.applyPattern(_fileTemplateFormat.getColumnFormat());
              if (columnDataArray[i].trim().equals(""))
                preparedStatement.setNull(i+1,Types.TIMESTAMP);
              else
                preparedStatement.setTimestamp(i+1,new Timestamp( dateFormatter.parse(columnDataArray[i]).getTime() ) );
            }
          }

          preparedStatement.executeUpdate();
        }
      }
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      System.err.println("TXTFileTemplate: (line : " + line
                       + ") The query that went wrong is " + query.toString());
      e.printStackTrace();
    }
    finally {
      try { file.close(); } catch (IOException ioe) { ioe.printStackTrace(); }
    }

    if (dbRet.getNoError() == 0 && dbRet.getRetInt() == ERROR_COLUMNS_MISMATCH) {
      System.err.println("TXTFileTemplate: (line : " + line
                       + ") Columns mismatch.");
    }
    if (dbRet.getNoError() == 0 && dbRet.getRetInt() == ERROR_COLUMNS_RANGE) {
      System.err.println("TXTFileTemplate: (line : " + line
                       + ") Columns range error.");
    }

    dbRet = database.commitTransaction(dbRet.getNoError(),prevTransIsolation);

    director.freeDBConnection(databaseId, database);

    return dbRet;
  }

  /**
   * Εξαγωγή από τη βάση δεδομένων προς legacy συστήματα.
   *
   */
  protected DbRet doOUT() {
    DbRet dbRet = new DbRet();
    dbRet.setNoError(1);

    SimpleDateFormat dateFormatter =
                new java.text.SimpleDateFormat();

    Database database = null;
    String databaseId = _fileTemplateFormat.getDatabaseId();
    QueryDataSet queryDataSet = new QueryDataSet();
    String query = "SELECT * FROM ";

    String filename = null, delimiter = null;

    File outFileHandle = null;
    
    //RandomAccessFile outFile = null; FileOutputStream
    BufferedWriter outFile = null;
    
    
    
    int colCount = 0;

    database = director.getDBConnection(databaseId);

    boolean ok = true;

    try {
      filename = _fileTemplateFormat.getOutPath()
               + _fileTemplateFormat.getFilename();

      outFileHandle = new File(filename);
      if (outFileHandle.exists()) outFileHandle.delete();
      
      //outFile = new RandomAccessFile(outFileHandle,"rw");
      outFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileHandle), "UTF-8"));

      delimiter = _fileTemplateFormat.getDelimiter();

      colCount = _fileTemplateFormat.getColumnCount();

      query += _fileTemplateFormat.getTablename();

      queryDataSet.setQuery(new QueryDescriptor(database, query, null, 
                                                true, Load.UNCACHED));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      queryDataSet.refresh();

      StringBuffer line = new StringBuffer();
      
      //for (int i=0; i<queryDataSetCount; i++) {
      while (queryDataSet.inBounds()==true) {
        _fileTemplateFormat.first();
        
        for (int x=0; x<colCount; x++) {
          if (TYPE_STRING.equals(_fileTemplateFormat.getColumnType())) {
            //line.append(padd(SwissKnife.sqlDecode(queryDataSet.format(_fileTemplateFormat.getColumnname())),Integer.parseInt(_fileTemplateFormat.getRemColumnFormat()) - _fileTemplateFormat.getColumnLength()));
            line.append(SwissKnife.sqlDecode(queryDataSet.format(_fileTemplateFormat.getColumnname())));
          }
          else if (TYPE_INT.equals(_fileTemplateFormat.getColumnType())) {
            //line.append(padd(String.valueOf(queryDataSet.getInt(_fileTemplateFormat.getColumnname())),Integer.parseInt(_fileTemplateFormat.getRemColumnFormat()) - _fileTemplateFormat.getColumnLength()));
            line.append(String.valueOf(queryDataSet.getInt(_fileTemplateFormat.getColumnname())));
          }
          else if (TYPE_BIGDECIMAL.equals(_fileTemplateFormat.getColumnType())) {
            //line.append(padd(queryDataSet.getBigDecimal(_fileTemplateFormat.getColumnname()).toString(),Integer.parseInt(_fileTemplateFormat.getRemColumnFormat()) - _fileTemplateFormat.getColumnLength()));
            line.append(queryDataSet.getBigDecimal(_fileTemplateFormat.getColumnname()).toString());
          }
          else if (TYPE_DATE.equals(_fileTemplateFormat.getColumnType())) {
            dateFormatter.applyPattern(_fileTemplateFormat.getColumnFormat());
            if (queryDataSet.getTimestamp(_fileTemplateFormat.getColumnname()) == null)
              //line.append(padd("", Integer.parseInt(_fileTemplateFormat.getRemColumnFormat()) - _fileTemplateFormat.getColumnLength()));
              line.append("");
            else
              line.append(dateFormatter.format(queryDataSet.getTimestamp(_fileTemplateFormat.getColumnname())).toString());
          }
          
          if (_fileTemplateFormat.next() == true) {
            line.append(_fileTemplateFormat.getDelimiter());
          }
        }
        line.append("\n");

        queryDataSet.next();
      }

      //outFile.write(line.toString().getBytes());
      outFile.write(line.toString());
      
      queryDataSet.close();
    }
    catch (Exception e) {
      ok = false;
      e.printStackTrace();
    }
    finally {
      try {
        outFile.close();
      }
      catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }

    director.freeDBConnection(databaseId, database);

    if (ok == false)
      dbRet.setNoError(0);

    return dbRet;
  }

  /**
   * (Προσθέτει τα απαραίτητα κενά σε περίπτωση που η συμβολοσειρά είναι
   * μικρότερη του μέγιστου μήκους.)
   *
   */
  protected String padd(String str, int length) {
    if (str == null || str.equals("null")) str = "";

    int spaces = length - str.length();

    if (spaces <= 0) return str;

    char[] fillSpaces = new char[spaces];

    for (int i=0; i<spaces; i++)
      fillSpaces[i] = ' ';

    str += new String(fillSpaces);

    return str;
  }

  /**
   * Ανάγνωση από legacy σύστημα και εισαγωγή νέων row
   * βάση της επιλεγμένης γραμμογράφησης.
   *
   */
  protected DbRet doINNew() {
    DbRet dbRet = new DbRet();
    dbRet.setNoError(1);

    Database database = null;
    String databaseId = _fileTemplateFormat.getDatabaseId();
    PreparedStatement preparedStatement = null;
    StringBuffer query = new StringBuffer();

    SimpleDateFormat dateFormatter =
                new java.text.SimpleDateFormat();

    String filename = null, delimiter = null;

    BufferedReader file = null;

    int colCount = 0;

    database = director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);

    int prevTransIsolation = dbRet.getRetInt();

    int line = 0;

    try {
      filename = _fileTemplateFormat.getInPath()
               + _fileTemplateFormat.getFilename();

      //System.out.println("filename = " + filename);

      delimiter = _fileTemplateFormat.getDelimiter();

      colCount = _fileTemplateFormat.getColumnCount();

      _fileTemplateFormat.first();

      query.append("INSERT INTO ");
      query.append(_fileTemplateFormat.getTablename());
      query.append(" (");

      // <<χτίσιμο>> του prepared statement
      for (int i=0; i<colCount; i++) {
        if (i>0) query.append(", ");
        query.append(_fileTemplateFormat.getColumnname());
        _fileTemplateFormat.next();
      }
      query.append(") VALUES (");

      for (int i=0; i<colCount; i++) {
        if (i>0) query.append(", ");
        query.append("?");
      }
      query.append(")");

      //System.out.println("query = " + query.toString());

      preparedStatement = database.createPreparedStatement(query.toString());

      file = new BufferedReader(new InputStreamReader(new FileInputStream(filename),
                                                      _fileCharset));

      // τα δεδομένα των columns
      String[] columnDataArray = new String[colCount];

      String lineStr = null;

      // ανάγνωση στοιχείων και εκτέλεση prepared statement
      while ( (lineStr = file.readLine()) != null && dbRet.getNoError() == 1) {
        line++;

        _fileTemplateFormat.first();

        dbRet = parseLine(_fileTemplateFormat, lineStr, columnDataArray);

        if (dbRet.getNoError() == 1) {
          _fileTemplateFormat.first();

          for (int i=0; i<colCount; i++) {
            if (TYPE_STRING.equals(_fileTemplateFormat.getColumnType())) {
              preparedStatement.setString(i+1,SwissKnife.sqlEncode(columnDataArray[i]));
            }
            else if (TYPE_INT.equals(_fileTemplateFormat.getColumnType())) {
              preparedStatement.setInt(i+1,Integer.parseInt(columnDataArray[i]));
            }
            else if (TYPE_BIGDECIMAL.equals(_fileTemplateFormat.getColumnType())) {
              preparedStatement.setBigDecimal(i+1,new BigDecimal(columnDataArray[i]));
            }
            else if (TYPE_DATE.equals(_fileTemplateFormat.getColumnType())) {
              dateFormatter.applyPattern(_fileTemplateFormat.getColumnFormat());

              if (columnDataArray[i].trim().equals(""))
                preparedStatement.setNull(i+1, Types.TIMESTAMP);
              else
                preparedStatement.setTimestamp(i+1,new Timestamp( dateFormatter.parse(columnDataArray[i]).getTime() ) );
            }

            _fileTemplateFormat.next();
          }

          preparedStatement.executeUpdate();
        }
      }
    }
    catch (Exception e) {
      dbRet.setNoError(0);

      System.err.println("TXTFileTemplate: (line : " + line
                       + ") The query that went wrong is " + query.toString());

      e.printStackTrace();
    }
    finally {
      try { file.close(); } catch (Exception ioe) { ioe.printStackTrace(); }
    }

    if (dbRet.getNoError() == 0 && dbRet.getRetInt() == ERROR_COLUMNS_MISMATCH) {
      System.err.println("TXTFileTemplate: (line : " + line
                       + ") Columns mismatch.");
    }
    if (dbRet.getNoError() == 0 && dbRet.getRetInt() == ERROR_COLUMNS_RANGE) {
      System.err.println("TXTFileTemplate: (line : " + line
                       + ") Columns range error.");
    }

    dbRet = database.commitTransaction(dbRet.getNoError(),prevTransIsolation);

    director.freeDBConnection(databaseId, database);

    return dbRet;
  }

  private DbRet parseLine(FileTemplateFormat ftf, String line,
                          String[] colData) {

    DbRet dbRet = new DbRet();

    String delimiter = ftf.getDelimiter();

    int colCount = ftf.getColumnCount();

    if (delimiter == null || delimiter.length() == 0) {
      // PARSE WITH STARTCOL & ENDCOL

      for (int i=0; i<colCount; i++) {
        try {
          colData[i] = line.substring(ftf.getColumnLength(),
                                      Integer.parseInt(ftf.getRemColumnFormat()));

          ftf.next();
        }
        catch (Exception e) {
          e.printStackTrace();

          dbRet.setNoError(0);
          dbRet.setRetInt(ERROR_COLUMNS_RANGE);

          return dbRet;
        }
      }
    }
    else {
      // PARSE WITH DELIMITER
      
      StrTokenizer lineTokenizer = new StrTokenizer(line, delimiter.toCharArray()[0]);
      //StringTokenizer lineTokenizer = new StringTokenizer(line, delimiter);

      if (colCount != lineTokenizer.countTokens()) {
        dbRet.setNoError(0);
        dbRet.setRetInt(ERROR_COLUMNS_MISMATCH);

        return dbRet;
      }

      for (int i=0; i<colCount; i++) {
        colData[i] = lineTokenizer.nextToken();

        ftf.next();
      }
    }

    return dbRet;
  }
}