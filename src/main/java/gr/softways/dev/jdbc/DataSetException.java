/*
 * DataSetException.java
 *
 * Created on 30 Ιανουάριος 2003, 10:28 πμ
 */

package gr.softways.dev.jdbc;

import java.sql.SQLException;

/**
 *
 * @author  minotauros
 */
public class DataSetException extends RuntimeException {
  
  public DataSetException(String msg) {
    super(msg);
  }
  
  public DataSetException(int errorCode, Throwable throwable) {
    super(throwable);
    this.errorCode = errorCode;
  }
      
  public DataSetException(int errorCode, String s1) {
    super(s1);
    this.errorCode = errorCode;
  }
  
  public static void SQLException(SQLException sqlException) {
    throw new DataSetException(SQL_ERROR, sqlException);
  }
  
  public static void throwException(Exception exception) {
    throw new DataSetException(GENERIC_ERROR, exception);
  }
  
  public static void closeDataSetFirst() {
    throw new DataSetException(DATASET_OPEN, "DataSet is already open, close it first.");
  }

  public static void needProvider() {
    throw new DataSetException(CANNOT_REFRESH, "Provider is not set, can not refresh.");
  }
    
  public static void unknownColumnName(String columnName) {
    throw new DataSetException(UNKNOWN_COLUMN_NAME, "Unknown column name : " + columnName);
  }

  public static void nullColumnList() {
    throw new DataSetException(EMPTY_COLUMN_NAMES, "ColumnList is null.");
  }
    
  public static void unrecognizedColumnType(String columnName, int columnType) {
    throw new DataSetException(UNRECOGNIZED_DATA_TYPE, "Unrecognized column type in column " + columnName + ". Column type is " + columnType);
  }
  
  public static void unrecognizedDataType(int dataType) {
    throw new DataSetException(UNRECOGNIZED_DATA_TYPE, "Unrecognized data type. Data type is " + dataType);
  }
  
  public static void unrecognizedSQLColumnType(int sqlColumnType) {
    throw new DataSetException(UNRECOGNIZED_DATA_TYPE, "Unrecognized SQL column type. Column type is " + sqlColumnType);
  }
  
  public static void invalidColumnType(String methodName, String columnTypeName) {
    throw new DataSetException(INVALID_COLUMN_TYPE, "Attempt to use " + methodName + " to retrieve value from a " + columnTypeName + " value.");
  }
  
  public static void openDataSetFirst() {
    throw new DataSetException(DATASET_NOT_OPEN, "Operation failed. DataSet is not open.");
  }  
    
  public int getErrorCode() {
    return errorCode;
  }
 
  public static final int INVALID_SORT_AS_INSERTED = 111;
  public static final int NO_PRIMARY_KEY = 111;
  public static final int DUPLICATE_PRIMARY = 110;
  public static final int INVALID_ITERATOR_USE = 109;
  public static final int INVALID_STORE_CLASS = 108;
  public static final int NEEDS_RECALC = 107;
  public static final int INVALID_CLASS = 106;
  public static final int DELETE_DUPLICATES = 105;
  public static final int INVALID_STORE_NAME = 104;
  public static final int PROCEDURE_FAILED = 103;
  public static final int MISSING_REPLACESTOREROW = 102;
  public static final int READ_ONLY_STORE = 101;
  public static final int NO_PRIOR_ORIGINAL_ROW = 100;
  public static final int CLASS_NOT_FOUND_ERROR = 99;
  public static final int UNKNOWN_DETAIL_NAME = 98;
  public static final int NO_DATABASE_TO_RESOLVE = 97;
  public static final int NEED_PROCEDUREPROVIDER = 96;
  public static final int NEED_QUERYPROVIDER = 95;
  public static final int PROCEDURE_IN_PROCESS = 94;
  public static final int MISMATCH_PARAM_RESULT = 93;
  public static final int NO_CALC_AGG_FIELDS = 92;
  public static final int WRONG_DATABASE = 91;
  public static final int NEED_STORAGEDATASET = 90;
  public static final int BAD_PROCEDURE_PROPERTIES = 89;
  public static final int PROVIDER_OWNED = 88;
  public static final int PROVIDER_FAILED = 87;
  public static final int LINKFIELD_IN_USERPARAMETERS = 86;
  public static final int URL_NOT_FOUND_IN_DESIGN = 85;
  public static final int URL_NOT_FOUND = 84;
  public static final int DRIVER_NOT_LOADED_AT_RUNTIME = 83;
  public static final int DRIVER_NOT_LOADED_IN_DESIGN = 82;
  public static final int FIELD_POST_ERROR = 81;
  public static final int CONNECTION_NOT_CLOSED = 80;
  public static final int IO_ERROR = 79;
  public static final int NO_RESULT_SET = 78;
  public static final int CANNOT_REFRESH = 77;
  public static final int CANNOT_SAVE_CHANGES = 76;
  public static final int REFRESHROW_NOT_SUPPORTED = 75;
  public static final int NON_EXISTENT_ROWID = 74;
  public static final int INSUFFICIENT_ROWID = 73;
  public static final int ONEPASS_INPUT_STREAM = 72;
  public static final int INVALID_FORMAT = 71;
  public static final int CANNOT_CHANGE_COLUMN = 70;
  public static final int RESOLVE_IN_PROGRESS = 69;
  public static final int NULL_COLUMN_NAME = 68;
  public static final int NOT_DATABASE_RESOLVER = 67;
  public static final int SQL_ERROR = 66;
  public static final int MISSING_MASTER_DATASET = 65;
  public static final int MISSING_RESOLVER = 64;
  public static final int RESOLVE_FAILED = 63;
  public static final int QUERY_FAILED = 62;
  public static final int REOPEN_FAILURE = 61;
  public static final int DATA_FILE_LOAD_FAILED = 60;
  public static final int NO_ROWS_AFFECTED = 51;
  public static final int INVALID_SCHEMA_FILE = 50;
  public static final int INVALID_COLUMN_TYPE = 49;
  public static final int INVALID_SORT_COLUMN = 48;
  public static final int EXCEPTION_CHAIN = 47;
  public static final int INVALID_AGG_DESCRIPTOR = 46;
  public static final int CONNECTION_DESCRIPTOR_NOT_SET = 45;
  public static final int NO_CALC_FIELDS = 44;
  public static final int MASTER_NAVIGATION_ERROR = 43;
  public static final int DATASET_NOT_OPEN = 42;
  public static final int DATASET_HAS_NO_ROWS = 41;
  public static final int NO_WHERE_CLAUSE = 40;
  public static final int DATASET_OPEN = 39;
  public static final int MULTIPLE_ROWS_AFFECTED = 38;
  public static final int NOT_UPDATEABLE = 37;
  public static final int DUPLICATE_COLUMN_NAME = 36;
  public static final int DATASET_CORRUPT = 35;
  public static final int MASTER_DETAIL_VIEW_ERROR = 34;
  public static final int LINK_COLUMNS_ERROR = 33;
  public static final int INVALID_DATA_FILE_FORMAT = 32;
  public static final int INCOMPATIBLE_DATA_ROW = 31;
  public static final int CANNOT_UPDATE_SCOPED_DATA_ROW = 30;
  public static final int LOADING_NOT_STARTED = 29;
  public static final int ALREADY_LOADING = 28;
  public static final int RESTRUCTURE_IN_PROGRESS = 27;
  public static final int QUERY_IN_PROCESS = 26;
  public static final int NEED_LOCATE_START_OPTION = 24;
  public static final int PARTIAL_SEARCH_FOR_STRING = 23;
  public static final int TRANSACTION_ISOLATION_LEVEL_NOT_SUPPORTED = 22;
  public static final int NO_NON_BLOB_COLUMNS = 21;
  public static final int EMPTY_COLUMN_NAMES = 20;
  public static final int COLUMN_NOT_IN_ROW = 19;
  public static final int DATASET_HAS_NO_TABLES = 18;
  public static final int UNRECOGNIZED_DATA_TYPE = 16;
  public static final int SET_CALCULATED_FAILURE = 15;
  public static final int CANNOT_CHANGE_COLUMN_DATA_TYPE = 14;
  public static final int COLUMN_ALREADY_BOUND = 13;
  public static final int INVALID_COLUMN_POSITION = 12;
  public static final int COLUMN_TYPE_CONFLICT = 11;
  public static final int UNKNOWN_COLUMN_NAME = 10;
  public static final int CANNOT_IMPORT_NULL_DATASET = 9;
  public static final int NO_UPDATABLE_COLUMNS = 8;
  public static final int CANNOT_FIND_TABLE_NAME = 7;
  public static final int PARAMETER_COUNT_MISMATCH = 6;
  public static final int MISMATCHED_PARAMETER_FORMAT = 5;
  public static final int UNKNOWN_PARAM_NAME = 4;
  public static final int UNEXPECTED_END_OF_QUERY = 3;
  public static final int NOT_SELECT_QUERY = 2;
  public static final int BAD_QUERY_PROPERTIES = 1;
  public static final int GENERIC_ERROR = 0;
  
  protected int errorCode;
}