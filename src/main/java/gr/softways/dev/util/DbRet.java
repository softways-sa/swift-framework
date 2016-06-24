package gr.softways.dev.util;

import java.sql.Timestamp;
import java.math.BigDecimal;

public class DbRet {
  public int retry = 0;
  public int unknownError = 0;
  public int dbErrorCode = 0;
  public int authError = 0;
  public int authErrorCode = 0;
  private int _validError = 0;
  private int _validErrorCode = 0;
  public int rowsFound = 0;
  public int noError = 1;
  public int retInt = 0;
  public BigDecimal retBig = null;
  public String retStr = "";
  public Timestamp retTs = null;

  public DbRet() {
  }
  public int getRetry() {
    return retry;
  }
  public void setRetry(int retryVal) {
    retry = retryVal;
  }
  public int getUnknownError() {
    return unknownError;
  }
  public void setUnknownError(int unknownErrorVal) {
    unknownError = unknownErrorVal;
  }
  public int getDbErrorCode() {
    return dbErrorCode;
  }
  public void setDbErrorCode(int dbErrorCodeVal) {
    dbErrorCode = dbErrorCodeVal;
  }
  public int getAuthError() {
    return authError;
  }
  public void setAuthError(int authErrorVal) {
    authError = authErrorVal;
  }
  public int getAuthErrorCode() {
    return authErrorCode;
  }
  public void setAuthErrorCode(int authErrorCodeVal) {
    authErrorCode = authErrorCodeVal;
  }
   public int get_validError() {
    return _validError;
  }
  public void set_validError(int _validErrorVal) {
    _validError = _validErrorVal;
  }
  public int get_validErrorCode() {
    return _validErrorCode;
  }
  public void set_validErrorCode(int _validErrorCodeVal) {
    _validErrorCode = _validErrorCodeVal;
  }
  public int getRowsFound() {
    return rowsFound;
  }
  public void setRowsFound(int rowsFoundVal) {
    rowsFound = rowsFoundVal;
  }
  public int getNoError() {
    return noError;
  }
  public void setNoError(int noErrorVal) {
    noError = noErrorVal;
  }
  public int getRetInt() {
    return retInt;
  }
  public void setRetInt(int retIntVal) {
    retInt = retIntVal;
  }
   public BigDecimal getRetBig() {
    return retBig;
  }
  public void setRetBig(BigDecimal retBigVal) {
    retBig = retBigVal;
  }
   public String getRetStr() {
    return retStr;
  }
  public void setRetStr(String retStrVal) {
    retStr = retStrVal;
  }
   public Timestamp getRetTs() {
    return retTs;
  }
  public void setRetTs(Timestamp retTsVal) {
    retTs = retTsVal;
  }
}