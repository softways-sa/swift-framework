package gr.softways.dev.eshop.filetemplate.servlets;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

import gr.softways.dev.eshop.filetemplate.FileTemplate;
import gr.softways.dev.eshop.filetemplate.FileTemplateFormat;

public class doAction extends HttpServlet {

  private Director bean;

  private String _charset = null;
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    bean = Director.getInstance();
  }

  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
              throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1").trim(),
           databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId").trim(),
           className = request.getParameter("className") == null ? "" : request.getParameter("className").trim(),
           FTemCode = request.getParameter("FTemCode") == null ? "" : request.getParameter("FTemCode").trim(),
           inPath = request.getParameter("inPath") == null ? "" : request.getParameter("inPath").trim(),
           outPath = request.getParameter("outPath") == null ? "" : request.getParameter("outPath").trim(),
           urlSuccess = request.getParameter("urlSuccess") == null ? "" : request.getParameter("urlSuccess").trim(),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure").trim(),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess").trim();

    FileTemplate fileTemplate = null;
    FileTemplateFormat fileTemplateFormat = null;
    DbRet dbRet = null;

    int status = Director.STATUS_ERROR;

    if (databaseId.equals("") || FTemCode.equals("") || className.equals(""))
     status = Director.STATUS_ERROR;
    else {
      try {
        fileTemplateFormat = new FileTemplateFormat(bean, databaseId,
                                                    FTemCode, inPath, outPath);

        /** instantiation του αντίστοιχου implementation TXT,XML etc. */
        fileTemplate = (FileTemplate)Class.forName(className).newInstance();

        dbRet = fileTemplate.doAction(action, fileTemplateFormat);
        if (dbRet.getNoError() == 1)
          status = Director.STATUS_OK;
      }
      catch (ClassNotFoundException cnfe) {
        status = Director.STATUS_ERROR;
        cnfe.printStackTrace();
      }
      catch (IllegalAccessException iae) {
        status = Director.STATUS_ERROR;
        iae.printStackTrace();
      }
      catch (InstantiationException ie) {
        status = Director.STATUS_ERROR;
        ie.printStackTrace();
      }
    }

    if (status < 0) {
      response.sendRedirect(urlNoAccess);
    }
    else if (status == Director.STATUS_OK) {
      response.sendRedirect(urlSuccess);
    }
    else {
      response.sendRedirect(urlFailure);
    }
  }
}