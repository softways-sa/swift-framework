package gr.softways.dev.eshop.filetemplate;

import java.io.*;
import java.util.*;

import gr.softways.dev.eshop.filetemplate.FileTemplate;
import gr.softways.dev.eshop.filetemplate.FileTemplateFormat;

import gr.softways.dev.util.DbRet;

public class XMLFileTemplate implements FileTemplate {

  FileTemplateFormat _fileTemplateFormat = null;

  public XMLFileTemplate() {
  }

  public DbRet doAction(String operation, FileTemplateFormat fileTemplateFormat) {
    _fileTemplateFormat = fileTemplateFormat;
    
    if (TEMPLATE_OP_IN.equals(operation)) {
      ;
    }

    return new DbRet();
  }
}