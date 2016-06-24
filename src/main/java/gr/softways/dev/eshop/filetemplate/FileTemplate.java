package gr.softways.dev.eshop.filetemplate;

import gr.softways.dev.util.DbRet;

import gr.softways.dev.eshop.filetemplate.FileTemplateFormat;

public interface FileTemplate {
  public static String TEMPLATE_OP_IN = "IN";
  public static String TEMPLATE_OP_OUT = "OUT";
  public static String TEMPLATE_OP_INNew = "INNew";

  public static final String TYPE_STRING = "1";
  public static final String TYPE_BIGDECIMAL = "2";
  public static final String TYPE_DATE = "3";
  public static final String TYPE_INT = "4";
  
  public DbRet doAction(String operation, FileTemplateFormat fileTemplateFormat);
}