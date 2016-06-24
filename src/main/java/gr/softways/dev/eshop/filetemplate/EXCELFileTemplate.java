/*
 * EXCELFileTemplate.java
 *
 * Created on 21 Φεβρουάριος 2003, 11:56 πμ
 */

package gr.softways.dev.eshop.filetemplate;

import java.io.*;
import java.util.*;
import java.math.*;

import java.sql.*;
import java.sql.Types;

import java.text.SimpleDateFormat;

import gr.softways.dev.util.*;
import gr.softways.dev.jdbc.*;

import jxl.Workbook;
import jxl.Sheet;

/**
 *
 * @author  minotauros
 */
public class EXCELFileTemplate implements FileTemplate {
  
  FileTemplateFormat _fileTemplateFormat = null;
  Director director = Director.getInstance();

  private int ERROR_COLUMNS_MISMATCH = 1;
  private int ERROR_COLUMNS_RANGE = 2;

  public EXCELFileTemplate() {
  }
  
  public DbRet doAction(String operation, FileTemplateFormat fileTemplateFormat) {
    DbRet dbRet = null;
    
    _fileTemplateFormat = fileTemplateFormat;
    
    if (TEMPLATE_OP_INNew.equals(operation)) {
      dbRet = doInsertNew();
    }
    else {
      dbRet = new DbRet();
      
      dbRet.setNoError(0);
      dbRet.setRetStr("Not implemented.");
    }

    return dbRet;
  }
  
  private DbRet doInsertNew() {
    DbRet dbRet = new DbRet();
   
    Database database = null;
    String databaseId = _fileTemplateFormat.getDatabaseId();
    PreparedStatement preparedStatement = null;
    
    StringBuffer query = new StringBuffer();
    
    SimpleDateFormat dateFormatter =
                new java.text.SimpleDateFormat();

    String filename = null;
    
    File excelInputFile = null;
    Workbook workbook = null;
    Sheet sheet = null;
    
    int colCount = 0;

    database = director.getDBConnection(databaseId);
    dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    
    int prevTransIsolation = dbRet.getRetInt();
    
    int currentExcelRow = 0;

    try {
      filename = _fileTemplateFormat.getInPath()
               + _fileTemplateFormat.getFilename();

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
      
      preparedStatement = database.createPreparedStatement(query.toString());

      excelInputFile = new File(filename);
      
      workbook = Workbook.getWorkbook(excelInputFile);
      sheet = workbook.getSheet(0);
      
      int excelRows = sheet.getRows();
      if (excelRows == 0) {
        throw new Exception("There are no rows in the EXCEL file.");
      }

      // ανάγνωση στοιχείων και εκτέλεση prepared statement {
      for (currentExcelRow=0; currentExcelRow<excelRows; currentExcelRow++) {
        _fileTemplateFormat.first();
        
        for (int i=0; i<colCount; i++) {
          if (TYPE_STRING.equals(_fileTemplateFormat.getColumnType())) {
            preparedStatement.setString(i+1,SwissKnife.sqlEncode(sheet.getCell(i, currentExcelRow).getContents()));
          }
          else if (TYPE_INT.equals(_fileTemplateFormat.getColumnType())) {
            preparedStatement.setInt(i+1,Integer.parseInt(sheet.getCell(i, currentExcelRow).getContents()));
          }
          else if (TYPE_BIGDECIMAL.equals(_fileTemplateFormat.getColumnType())) {
            preparedStatement.setBigDecimal(i+1,new BigDecimal(sheet.getCell(i, currentExcelRow).getContents()));
          }
          else if (TYPE_DATE.equals(_fileTemplateFormat.getColumnType())) {
            dateFormatter.applyPattern(_fileTemplateFormat.getColumnFormat());

            if (sheet.getCell(i, currentExcelRow).getContents().trim().equals(""))
              preparedStatement.setNull(i+1, Types.TIMESTAMP);
            else
              preparedStatement.setTimestamp(i+1,new Timestamp( dateFormatter.parse(sheet.getCell(i, currentExcelRow).getContents()).getTime() ) );
          }

          _fileTemplateFormat.next();
        }
        
        preparedStatement.executeUpdate();
      }
      // } ανάγνωση στοιχείων και εκτέλεση prepared statement      
    }
    catch (Exception e) {
      dbRet.setNoError(0);

      System.err.println("EXCELFileTemplate: (Row : " + currentExcelRow
                       + ") The query that went wrong is " + query.toString());

      e.printStackTrace();
    }
    finally {
      excelInputFile = null;

      if (workbook != null) workbook.close();
    }
     
    dbRet = database.commitTransaction(dbRet.getNoError(),prevTransIsolation);

    director.freeDBConnection(databaseId, database);
      
    return dbRet;
  }
}