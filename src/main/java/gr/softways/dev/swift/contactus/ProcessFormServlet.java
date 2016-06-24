package gr.softways.dev.swift.contactus;

import gr.softways.dev.util.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class ProcessFormServlet extends HttpServlet {
  
  //Initialize global variables
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    
    servletContext = getServletContext();
    
    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    _databaseId = SwissKnife.jndiLookup("swconf/databaseId");
    if (_databaseId == null) throw new ServletException("swconf/databaseId not found in jndi.");
  }
  
  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    RequestDispatcher requestDispatcher = null;
    
    DbRet dbRet = new DbRet();
    
    Map<String, List<String>> formFields = new LinkedHashMap<String, List<String>>();
    
    dbRet = process(formFields, request);
    
    if (dbRet.getNoError() == 1) request.setAttribute("regForm", "OK");
    else request.setAttribute("regForm", "ERROR");
    
    String returnurl = getParameter(formFields, "returnurl__hidden");
    
    if (returnurl != null) {
      requestDispatcher = request.getRequestDispatcher(returnurl);
      requestDispatcher.forward(request, response);
    }
    else {
      requestDispatcher = request.getRequestDispatcher("/problem.jsp");
      requestDispatcher.forward(request, response);
    }
  }
  
  private DbRet process(Map<String, List<String>> formFields, HttpServletRequest request) {
    DbRet dbRet = new DbRet();
    
    String id = SwissKnife.buildPK();
    
    String br = "<br/>";
    
    StringBuilder message = new StringBuilder();
    
    // Create a factory for disk-based file items
    FileItemFactory factory = new DiskFileItemFactory();

    // Create a new file upload handler
    ServletFileUpload upload = new ServletFileUpload(factory);
    upload.setHeaderEncoding(_charset);
    upload.setSizeMax(DEFAULT_MAX_FILE_SIZE);
    upload.setFileSizeMax(DEFAULT_MAX_FILE_SIZE);
    
    List<File> uploadedFiles =  new ArrayList<File>();
    
    List<FileItem> items = null;
    
    String fromAddress = null, subject = null, copyReq = null;
    
    try {
      items = upload.parseRequest(request);
      convertToMap(formFields, items);
      
      request.setAttribute("regFormFields", formFields);
      
      fromAddress = getParameter(formFields, "email");
      
      boolean isCaptchaResponseCorrect = false;
    
      String captcha_response = getParameter(formFields, "captcha_response__hidden");

      try {
        String captcha_expected = (String) request.getSession().getAttribute(com.google.code.kaptcha.Constants.KAPTCHA_SESSION_KEY);
        request.getSession().setAttribute(com.google.code.kaptcha.Constants.KAPTCHA_SESSION_KEY,SwissKnife.buildPK());

        if (captcha_response == null || !captcha_response.equalsIgnoreCase(captcha_expected)) isCaptchaResponseCorrect = false;
        else isCaptchaResponseCorrect = true;
      }
      catch (Exception e) {
      }
    
      if (isCaptchaResponseCorrect == false) {
        request.setAttribute("regFormCaptchaError", "1");
        
        throw new Exception("Wrong captcha.");
      }
      
      String[] values = Configuration.getValues(new String[] {"smtpServer",getParameter(formFields, "formRecipientKey__hidden")});
      String smtpServer = values[0], toAddress = values[1];
    
      fromAddress = getParameter(formFields, "email");
      subject = getParameter(formFields, "subject__hidden");
      copyReq = getParameter(formFields, "copyReq__hidden");
      
      message.append("<style>body {font-family:Verdana,Arial, Helvetica, sans-serif; font-size:12px;} div {margin-bottom: 5px;}</style>");
    
      message.append("<span style='font-size:16px; font-weight:bold;'>");
      message.append(subject);
      message.append("</span>");
      message.append(br);
      message.append(br);
      
      // Parse the request
      Iterator<FileItem> iter = items.iterator();
      while (iter.hasNext()) {
        FileItem item = iter.next();

        if (!item.isFormField() && item.getName() != null && item.getName().length() > 0) {
          String ext = "";
          
          int i = item.getName().lastIndexOf('.');
          if (i > 0) {
            ext = item.getName().substring(i+1);
          }

          File saveTo = new File(servletContext.getRealPath("") + "/temp_upload/" +  item.getFieldName() + "_contactus_" + id + "." + ext);
          item.write(saveTo);
          
          uploadedFiles.add(saveTo);
        }
        else if (item.getString().length() > 0 && !item.getFieldName().endsWith("__label") && !item.getFieldName().endsWith("__hidden")) {
          message.append("<div><span style='font-weight:bold;'>");
          message.append( getParameter(formFields, item.getFieldName() + "__label") );
          message.append("</span>: ");
          message.append(item.getString(_charset));
          message.append("</div>");
          message.append(br);
        }
      }
      
      EmailUtility.sendEmailWithAttachment(smtpServer, fromAddress, toAddress, subject, message.toString(), uploadedFiles);
      
      if (copyReq != null && "1".equals(copyReq)) EmailUtility.sendEmailWithAttachment(smtpServer, fromAddress, fromAddress, subject, message.toString(), uploadedFiles);
    }
    catch (Exception e) {
      dbRet.setNoError(0);
    }
    finally {
      deleteUploadFiles(uploadedFiles);
    }
    
    return dbRet;
  }
  
  public String getParameter(Map<String, List<String>> formFields, String aName) {
    String result = null;
    
    List<String> values = formFields.get(aName);
    
    if (values == null){
      //you might try the wrappee, to see if it has a value 
    }
    else if (values.isEmpty()) {
      //param name known, but no values present
      result = "";
    }
    else {
      //return first value in list
      result = values.get(FIRST_VALUE);
    }
    
    return result;
  }
  
  /**
  * Return the parameter values. Applies only to regular parameters, 
  * not to file upload parameters.
  */
  public String[] getParameterValues(Map<String, List<String>> formFields,String aName) {
    String[] result = null;
    
    List<String> values = formFields.get(aName);
    
    if (values != null) {
      result = values.toArray(new String[values.size()]);
    }
    
    return result;
  }
  
  /**
   * Deletes all uploaded files, should be called after the email was sent.
   */
  private void deleteUploadFiles(List<File> listFiles) {
    if (listFiles != null && listFiles.size() > 0) {
      for (File aFile : listFiles) {
        aFile.delete();
      }
    }
  }
  
  private void convertToMap(Map<String, List<String>> formFields, List<FileItem> listFiles) throws UnsupportedEncodingException {
    for (FileItem item: listFiles) {
      if (item.isFormField()) {
        if (alreadyHasValue(formFields, item)) {
          addMultivaluedItem(formFields, item);
        }
        else {
          addSingleValueItem(formFields, item);
        }
      }
    }
  }
  
  private boolean alreadyHasValue(Map<String, List<String>> formFields, FileItem aItem) {
    return formFields.get(aItem.getFieldName()) != null;
  }
  
  private void addSingleValueItem(Map<String, List<String>> formFields, FileItem aItem) throws UnsupportedEncodingException  {
    List<String> list = new ArrayList<String>();
    
    list.add(aItem.getString(_charset));
    formFields.put(aItem.getFieldName(), list);
  }
  
  private void addMultivaluedItem(Map<String, List<String>> formFields, FileItem aItem) throws UnsupportedEncodingException {
    List<String> values = formFields.get(aItem.getFieldName());
    
    values.add(aItem.getString(_charset));
  }
  
  private static final int FIRST_VALUE = 0;
  
  private ServletContext servletContext;
  
  private String _charset = null;
  
  private String _databaseId = null;
  
  private Director _director = Director.getInstance();
  
  private static final int DEFAULT_MAX_FILE_SIZE = 2621440; // 2,5 MB
}