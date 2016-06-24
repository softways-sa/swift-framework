package gr.softways.dev.swift.cmrow.servlets;

import java.io.*;
import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.swift.cmrow.CMRowAttribs;
import gr.softways.dev.util.*;
import java.sql.Timestamp;

import gr.softways.dev.swift.emailspooler.EmailSpooler;

public class doAction extends HttpServlet {

  private Director _director;
    
  private String _charset = null;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    _charset = SwissKnife.jndiLookup("swconf/charset");
    if (_charset == null) _charset = SwissKnife.DEFAULT_CHARSET;
    
    _director = Director.getInstance();
  }

  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding(_charset);
    response.setContentType("text/html; charset=" + _charset);
    
    String action = request.getParameter("action1") == null ? "" : request.getParameter("action1"),
           databaseId = request.getParameter("databaseId") == null ? "" : request.getParameter("databaseId"),
           urlSuccess = request.getParameter("urlSuccess") == null ? "" : request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");

    int status = Director.STATUS_ERROR;

    if (databaseId.equals("")) status = Director.STATUS_ERROR;
    else if (action.equals("INSERT")) status = doInsert(request, databaseId);
    else if (action.equals("UPDATE")) status = doUpdate(request, databaseId);
    else if (action.equals("DELETE")) status = doDelete(request, databaseId);
    else if (action.equals("SENDTOEMAILLIST")) status = doSendToEmailList(request, databaseId);    
    else status = Director.STATUS_ERROR;

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

  /**
   *  Καταχώρηση νέου άρθρου στο RDBMS.
   *
   * @param  request    το HttpServletRequest από την σελίδα
   * @param  databaseId το αναγνωριστικό της βάσης που
   *                    θα χρησιμοποιηθεί
   * @return            κωδικό κατάστασης
   */
  private int doInsert(HttpServletRequest request, String databaseId) {

    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"CMRow",Director.AUTH_INSERT);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String CMRCode = null,
           CMRTitle = SwissKnife.sqlEncode(request.getParameter("CMRTitle")),
           CMRTitleLG = SwissKnife.sqlEncode(request.getParameter("CMRTitleLG")),
           CMRKeyWords = SwissKnife.sqlEncode(request.getParameter("CMRKeyWords")),
           CMRKeyWordsLG = SwissKnife.sqlEncode(request.getParameter("CMRKeyWordsLG")),
           CMRSummary = SwissKnife.sqlEncode(request.getParameter("CMRSummary")),
           CMRSummaryLG = SwissKnife.sqlEncode(request.getParameter("CMRSummaryLG")),
           CMRText = SwissKnife.sqlEncode(request.getParameter("CMRText")),
           CMRTextLG = SwissKnife.sqlEncode(request.getParameter("CMRTextLG")),
           CMRIsSticky = SwissKnife.sqlEncode(request.getParameter("CMRIsSticky")),
           CMCCode = SwissKnife.sqlEncode(request.getParameter("CMCCode")),
           CCCRRank = SwissKnife.sqlEncode(request.getParameter("CCCRRank")),
           CCCRIsHidden = SwissKnife.sqlEncode(request.getParameter("CCCRIsHidden")),
           CMRDateCreatedDay=null, CMRDateCreatedMonth=null, CMRDateCreatedYear=null,
           CMRDateUpdatedDay=null, CMRDateUpdatedMonth=null, CMRDateUpdatedYear=null;

    String CMRIsProtected = request.getParameter("CMRIsProtected");
    
    String CMRHeadHTML = request.getParameter("CMRHeadHTML"),
        CMRBodyHTML = request.getParameter("CMRBodyHTML"),
        CMRAttribs = null;
    
    String CMRDateCreatedHour = "0", CMRDateCreatedMin = "0", CMRDateCreatedSec = "0", CMRDateCreatedMil = "0";
    String CMRDateUpdatedHour = "0", CMRDateUpdatedMin = "0", CMRDateUpdatedSec = "0", CMRDateUpdatedMil = "0";
        
    Timestamp CMRDateCreated = null, CMRDateUpdated = null;

    int rank = 0;
    try {
      rank = Integer.parseInt(CCCRRank);
    }
    catch (Exception e){
      rank = 0;
    }
    
    if ( (CMRDateCreatedDay = request.getParameter("CMRDateCreatedDay")) == null ) CMRDateCreatedDay = "";
    if ( (CMRDateCreatedMonth = request.getParameter("CMRDateCreatedMonth")) == null ) CMRDateCreatedMonth = "";
    if ( (CMRDateCreatedYear = request.getParameter("CMRDateCreatedYear")) == null ) CMRDateCreatedYear = "";
    if (request.getParameter("CMRDateCreatedHour") != null) CMRDateCreatedHour = request.getParameter("CMRDateCreatedHour");
    if (request.getParameter("CMRDateCreatedMin") != null) CMRDateCreatedMin = request.getParameter("CMRDateCreatedMin");
    if (request.getParameter("CMRDateCreatedSec") != null) CMRDateCreatedSec = request.getParameter("CMRDateCreatedSec");
    if (request.getParameter("CMRDateCreatedMil") != null) CMRDateCreatedMil = request.getParameter("CMRDateCreatedMil");
    CMRDateCreated = SwissKnife.buildTimestamp(CMRDateCreatedDay,CMRDateCreatedMonth,CMRDateCreatedYear,CMRDateCreatedHour,CMRDateCreatedMin,CMRDateCreatedSec,CMRDateCreatedMil);
    
    if ( (CMRDateUpdatedDay = request.getParameter("CMRDateUpdatedDay")) == null ) CMRDateUpdatedDay = "";
    if ( (CMRDateUpdatedMonth = request.getParameter("CMRDateUpdatedMonth")) == null ) CMRDateUpdatedMonth = "";
    if ( (CMRDateUpdatedYear = request.getParameter("CMRDateUpdatedYear")) == null ) CMRDateUpdatedYear = "";
    if (request.getParameter("CMRDateUpdatedHour") != null) CMRDateUpdatedHour = request.getParameter("CMRDateUpdatedHour");
    if (request.getParameter("CMRDateUpdatedMin") != null) CMRDateUpdatedMin = request.getParameter("CMRDateUpdatedMin");
    if (request.getParameter("CMRDateUpdatedSec") != null) CMRDateUpdatedSec = request.getParameter("CMRDateUpdatedSec");
    if (request.getParameter("CMRDateUpdatedMil") != null) CMRDateUpdatedMil = request.getParameter("CMRDateUpdatedMil");
    CMRDateUpdated = SwissKnife.buildTimestamp(CMRDateUpdatedDay,CMRDateUpdatedMonth,CMRDateUpdatedYear,CMRDateUpdatedHour,CMRDateUpdatedMin,CMRDateUpdatedSec,CMRDateUpdatedMil);
    
    String stmp = null;
    stmp = CMRTitle.length() <= 80 ? CMRTitle.substring(0, CMRTitle.length()) : CMRTitle.substring(0, 80);
    String CMRTitle1Up = SwissKnife.searchConvert(stmp);
    stmp = CMRTitle.length() <= 80 ? "" : CMRTitle.substring(80, CMRTitle.length());
    String CMRTitle2Up = SwissKnife.searchConvert(stmp);
    
    stmp = CMRTitleLG.length() <= 80 ? CMRTitleLG.substring(0, CMRTitleLG.length()) : CMRTitleLG.substring(0, 80);
    String CMRTitle1UpLG = SwissKnife.searchConvert(stmp);
    stmp = CMRTitleLG.length() <= 80 ? "" : CMRTitleLG.substring(80, CMRTitleLG.length());
    String CMRTitle2UpLG = SwissKnife.searchConvert(stmp);
    
    stmp = CMRKeyWords.length() <= 80 ? CMRKeyWords.substring(0, CMRKeyWords.length()) : CMRKeyWords.substring(0, 80);
    String CMRKeyWords1Up = SwissKnife.searchConvert(stmp);
    stmp = CMRKeyWords.length() <= 80 ? "" : CMRKeyWords.substring(80, CMRKeyWords.length());
    String CMRKeyWords2Up = SwissKnife.searchConvert(stmp);
    
    stmp = CMRKeyWordsLG.length() <= 80 ? CMRKeyWordsLG.substring(0, CMRKeyWordsLG.length()) : CMRKeyWordsLG.substring(0, 80);
    String CMRKeyWords1UpLG = SwissKnife.searchConvert(stmp);
    stmp = CMRKeyWordsLG.length() <= 80 ? "" : CMRKeyWordsLG.substring(80, CMRKeyWordsLG.length());
    String CMRKeyWords2UpLG = SwissKnife.searchConvert(stmp);
    
    String CMRTitleLG1 = request.getParameter("CMRTitleLG1"),
           CMRTitleLG2 = request.getParameter("CMRTitleLG2"),
           CMRTitleLG3 = request.getParameter("CMRTitleLG3"),
           CMRTitleLG4 = request.getParameter("CMRTitleLG4"),
           CMRTitleLG5 = request.getParameter("CMRTitleLG5"),
           CMRTitleLG6 = request.getParameter("CMRTitleLG6"),
           CMRTitleLG7 = request.getParameter("CMRTitleLG7"),
           CMRKeyWordsLG1 = request.getParameter("CMRKeyWordsLG1"),
           CMRKeyWordsLG2 = request.getParameter("CMRKeyWordsLG2"),
           CMRKeyWordsLG3 = request.getParameter("CMRKeyWordsLG3"),
           CMRKeyWordsLG4 = request.getParameter("CMRKeyWordsLG4"),
           CMRKeyWordsLG5 = request.getParameter("CMRKeyWordsLG5"),
           CMRKeyWordsLG6 = request.getParameter("CMRKeyWordsLG6"),
           CMRKeyWordsLG7 = request.getParameter("CMRKeyWordsLG7"),
           CMRSummaryLG1 = request.getParameter("CMRSummaryLG1"),
           CMRSummaryLG2 = request.getParameter("CMRSummaryLG2"),
           CMRSummaryLG3 = request.getParameter("CMRSummaryLG3"),
           CMRSummaryLG4 = request.getParameter("CMRSummaryLG4"),
           CMRSummaryLG5 = request.getParameter("CMRSummaryLG5"),
           CMRSummaryLG6 = request.getParameter("CMRSummaryLG6"),
           CMRSummaryLG7 = request.getParameter("CMRSummaryLG7"),
           CMRTextLG1 = request.getParameter("CMRTextLG1"),
           CMRTextLG2 = request.getParameter("CMRTextLG2"),
           CMRTextLG3 = request.getParameter("CMRTextLG3"),
           CMRTextLG4 = request.getParameter("CMRTextLG4"),
           CMRTextLG5 = request.getParameter("CMRTextLG5"),
           CMRTextLG6 = request.getParameter("CMRTextLG6"),
           CMRTextLG7 = request.getParameter("CMRTextLG7");
           
    String CMRTitle1UpLG1 = null, CMRTitle2UpLG1 = null, CMRTitle1UpLG2 = null, CMRTitle2UpLG2 = null,
           CMRTitle1UpLG3 = null, CMRTitle2UpLG3 = null, CMRTitle1UpLG4 = null, CMRTitle2UpLG4 = null,
           CMRTitle1UpLG5 = null, CMRTitle2UpLG5 = null, CMRTitle1UpLG6 = null, CMRTitle2UpLG6 = null,
           CMRTitle1UpLG7 = null, CMRTitle2UpLG7 = null;
    
    String CMRKeyWords1UpLG1 = null, CMRKeyWords2UpLG1 = null, CMRKeyWords1UpLG2 = null, CMRKeyWords2UpLG2 = null,
           CMRKeyWords1UpLG3 = null, CMRKeyWords2UpLG3 = null, CMRKeyWords1UpLG4 = null, CMRKeyWords2UpLG4 = null,
           CMRKeyWords1UpLG5 = null, CMRKeyWords2UpLG5 = null, CMRKeyWords1UpLG6 = null, CMRKeyWords2UpLG6 = null,
           CMRKeyWords1UpLG7 = null, CMRKeyWords2UpLG7 = null;
    
    if (CMRTitleLG1 != null) {
      stmp = CMRTitleLG1.length() <= 80 ? CMRTitleLG1.substring(0, CMRTitleLG1.length()) : CMRTitleLG1.substring(0, 80);
      CMRTitle1UpLG1 = SwissKnife.searchConvert(stmp);
      stmp = CMRTitleLG1.length() <= 80 ? "" : CMRTitleLG1.substring(80, CMRTitleLG1.length());
      CMRTitle2UpLG1 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRTitleLG2 != null) {
      stmp = CMRTitleLG2.length() <= 80 ? CMRTitleLG2.substring(0, CMRTitleLG2.length()) : CMRTitleLG2.substring(0, 80);
      CMRTitle1UpLG2 = SwissKnife.searchConvert(stmp);
      stmp = CMRTitleLG2.length() <= 80 ? "" : CMRTitleLG2.substring(80, CMRTitleLG2.length());
      CMRTitle2UpLG2 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRTitleLG3 != null) {
      stmp = CMRTitleLG3.length() <= 80 ? CMRTitleLG3.substring(0, CMRTitleLG3.length()) : CMRTitleLG3.substring(0, 80);
      CMRTitle1UpLG3 = SwissKnife.searchConvert(stmp);
      stmp = CMRTitleLG3.length() <= 80 ? "" : CMRTitleLG3.substring(80, CMRTitleLG3.length());
      CMRTitle2UpLG3 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRTitleLG4 != null) {
      stmp = CMRTitleLG4.length() <= 80 ? CMRTitleLG4.substring(0, CMRTitleLG4.length()) : CMRTitleLG4.substring(0, 80);
      CMRTitle1UpLG4 = SwissKnife.searchConvert(stmp);
      stmp = CMRTitleLG4.length() <= 80 ? "" : CMRTitleLG4.substring(80, CMRTitleLG4.length());
      CMRTitle2UpLG4 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRTitleLG5 != null) {
      stmp = CMRTitleLG5.length() <= 80 ? CMRTitleLG5.substring(0, CMRTitleLG5.length()) : CMRTitleLG5.substring(0, 80);
      CMRTitle1UpLG5 = SwissKnife.searchConvert(stmp);
      stmp = CMRTitleLG5.length() <= 80 ? "" : CMRTitleLG5.substring(80, CMRTitleLG5.length());
      CMRTitle2UpLG5 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRTitleLG6 != null) {
      stmp = CMRTitleLG6.length() <= 80 ? CMRTitleLG6.substring(0, CMRTitleLG6.length()) : CMRTitleLG6.substring(0, 80);
      CMRTitle1UpLG6 = SwissKnife.searchConvert(stmp);
      stmp = CMRTitleLG6.length() <= 80 ? "" : CMRTitleLG6.substring(80, CMRTitleLG6.length());
      CMRTitle2UpLG6 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRTitleLG7 != null) {
      stmp = CMRTitleLG7.length() <= 80 ? CMRTitleLG7.substring(0, CMRTitleLG7.length()) : CMRTitleLG7.substring(0, 80);
      CMRTitle1UpLG7 = SwissKnife.searchConvert(stmp);
      stmp = CMRTitleLG7.length() <= 80 ? "" : CMRTitleLG7.substring(80, CMRTitleLG7.length());
      CMRTitle2UpLG7 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRKeyWordsLG1 != null) {
      stmp = CMRKeyWordsLG1.length() <= 80 ? CMRKeyWordsLG1.substring(0, CMRKeyWordsLG1.length()) : CMRKeyWordsLG1.substring(0, 80);
      CMRKeyWords1UpLG1 = SwissKnife.searchConvert(stmp);
      stmp = CMRKeyWordsLG1.length() <= 80 ? "" : CMRKeyWordsLG1.substring(80, CMRKeyWordsLG1.length());
      CMRKeyWords2UpLG1 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRKeyWordsLG2 != null) {
      stmp = CMRKeyWordsLG2.length() <= 80 ? CMRKeyWordsLG2.substring(0, CMRKeyWordsLG2.length()) : CMRKeyWordsLG2.substring(0, 80);
      CMRKeyWords1UpLG2 = SwissKnife.searchConvert(stmp);
      stmp = CMRKeyWordsLG2.length() <= 80 ? "" : CMRKeyWordsLG2.substring(80, CMRKeyWordsLG2.length());
      CMRKeyWords2UpLG2 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRKeyWordsLG3 != null) {
      stmp = CMRKeyWordsLG3.length() <= 80 ? CMRKeyWordsLG3.substring(0, CMRKeyWordsLG3.length()) : CMRKeyWordsLG3.substring(0, 80);
      CMRKeyWords1UpLG3 = SwissKnife.searchConvert(stmp);
      stmp = CMRKeyWordsLG3.length() <= 80 ? "" : CMRKeyWordsLG3.substring(80, CMRKeyWordsLG3.length());
      CMRKeyWords2UpLG3 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRKeyWordsLG4 != null) {
      stmp = CMRKeyWordsLG4.length() <= 80 ? CMRKeyWordsLG4.substring(0, CMRKeyWordsLG4.length()) : CMRKeyWordsLG4.substring(0, 80);
      CMRKeyWords1UpLG4 = SwissKnife.searchConvert(stmp);
      stmp = CMRKeyWordsLG4.length() <= 80 ? "" : CMRKeyWordsLG4.substring(80, CMRKeyWordsLG4.length());
      CMRKeyWords2UpLG4 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRKeyWordsLG5 != null) {
      stmp = CMRKeyWordsLG5.length() <= 80 ? CMRKeyWordsLG5.substring(0, CMRKeyWordsLG5.length()) : CMRKeyWordsLG5.substring(0, 80);
      CMRKeyWords1UpLG5 = SwissKnife.searchConvert(stmp);
      stmp = CMRKeyWordsLG5.length() <= 80 ? "" : CMRKeyWordsLG5.substring(80, CMRKeyWordsLG5.length());
      CMRKeyWords2UpLG5 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRKeyWordsLG6 != null) {
      stmp = CMRKeyWordsLG6.length() <= 80 ? CMRKeyWordsLG6.substring(0, CMRKeyWordsLG6.length()) : CMRKeyWordsLG6.substring(0, 80);
      CMRKeyWords1UpLG6 = SwissKnife.searchConvert(stmp);
      stmp = CMRKeyWordsLG6.length() <= 80 ? "" : CMRKeyWordsLG6.substring(80, CMRKeyWordsLG6.length());
      CMRKeyWords2UpLG6 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRKeyWordsLG7 != null) {
      stmp = CMRKeyWordsLG7.length() <= 80 ? CMRKeyWordsLG7.substring(0, CMRKeyWordsLG7.length()) : CMRKeyWordsLG7.substring(0, 80);
      CMRKeyWords1UpLG7 = SwissKnife.searchConvert(stmp);
      stmp = CMRKeyWordsLG7.length() <= 80 ? "" : CMRKeyWordsLG7.substring(80, CMRKeyWordsLG7.length());
      CMRKeyWords2UpLG7 = SwissKnife.searchConvert(stmp);
    }
    
    CMRAttribs = buildCMRAttribs(request);
    
    PreparedStatement ps = null;

    CMRCode = SwissKnife.buildPK();
    
    String insCMRow = "INSERT INTO CMRow " +
                   " (CMRCode,CMRDateCreated,CMRDateUpdated,CMRTitle,CMRTitle1Up," +
                   "  CMRTitle2Up,CMRTitleLG,CMRTitle1UpLG,CMRTitle2UpLG,CMRKeyWords," +
                   "  CMRKeyWords1Up,CMRKeyWords2Up,CMRKeyWordsLG,CMRKeyWords1UpLG," +
                   "  CMRKeyWords2UpLG,CMRSummary,CMRSummaryLG,CMRText,CMRTextLG,CMRIsSticky";

    int insCMRowCounter = 20;
    
    if (CMRIsProtected != null) {
      insCMRow += ",CMRIsProtected";
      insCMRowCounter++;
    }
    
    if (CMRTitleLG1 != null) {
      insCMRow += ",CMRTitleLG1,CMRTitle1UpLG1,CMRTitle2UpLG1";
      insCMRowCounter += 3;
    }
    if (CMRTitleLG2 != null) {
      insCMRow += ",CMRTitleLG2,CMRTitle1UpLG2,CMRTitle2UpLG2";
      insCMRowCounter += 3;
    }
    if (CMRTitleLG3 != null) {
      insCMRow += ",CMRTitleLG3,CMRTitle1UpLG3,CMRTitle2UpLG3";
      insCMRowCounter += 3;
    }
    if (CMRTitleLG4 != null) {
      insCMRow += ",CMRTitleLG4,CMRTitle1UpLG4,CMRTitle2UpLG4";
      insCMRowCounter += 3;
    }
    if (CMRTitleLG5 != null) {
      insCMRow += ",CMRTitleLG5,CMRTitle1UpLG5,CMRTitle2UpLG5";
      insCMRowCounter += 3;
    }
    if (CMRTitleLG6 != null) {
      insCMRow += ",CMRTitleLG6,CMRTitle1UpLG6,CMRTitle2UpLG6";
      insCMRowCounter += 3;
    }
    if (CMRTitleLG7 != null) {
      insCMRow += ",CMRTitleLG7,CMRTitle1UpLG7,CMRTitle2UpLG7";
      insCMRowCounter += 3;
    }
    
    if (CMRKeyWordsLG1 != null) {
      insCMRow += ",CMRKeyWordsLG1,CMRKeyWords1UpLG1,CMRKeyWords2UpLG1";
      insCMRowCounter += 3;
    }
    if (CMRKeyWordsLG2 != null) {
      insCMRow += ",CMRKeyWordsLG2,CMRKeyWords1UpLG2,CMRKeyWords2UpLG2";
      insCMRowCounter += 3;
    }
    if (CMRKeyWordsLG3 != null) {
      insCMRow += ",CMRKeyWordsLG3,CMRKeyWords1UpLG3,CMRKeyWords2UpLG3";
      insCMRowCounter += 3;
    }
    if (CMRKeyWordsLG4 != null) {
      insCMRow += ",CMRKeyWordsLG4,CMRKeyWords1UpLG4,CMRKeyWords2UpLG4";
      insCMRowCounter += 3;
    }
    if (CMRKeyWordsLG5 != null) {
      insCMRow += ",CMRKeyWordsLG5,CMRKeyWords1UpLG5,CMRKeyWords2UpLG5";
      insCMRowCounter += 3;
    }
    if (CMRKeyWordsLG6 != null) {
      insCMRow += ",CMRKeyWordsLG6,CMRKeyWords1UpLG6,CMRKeyWords2UpLG6";
      insCMRowCounter += 3;
    }
    if (CMRKeyWordsLG7 != null) {
      insCMRow += ",CMRKeyWordsLG7,CMRKeyWords1UpLG7,CMRKeyWords2UpLG7";
      insCMRowCounter += 3;
    }
    
    if (CMRSummaryLG1 != null) {
      insCMRow += ",CMRSummaryLG1";
      insCMRowCounter++;
    }
    if (CMRSummaryLG2 != null) {
      insCMRow += ",CMRSummaryLG2";
      insCMRowCounter++;
    }
    if (CMRSummaryLG3 != null) {
      insCMRow += ",CMRSummaryLG3";
      insCMRowCounter++;
    }
    if (CMRSummaryLG4 != null) {
      insCMRow += ",CMRSummaryLG4";
      insCMRowCounter++;
    }
    if (CMRSummaryLG5 != null) {
      insCMRow += ",CMRSummaryLG5";
      insCMRowCounter++;
    }
    if (CMRSummaryLG6 != null) {
      insCMRow += ",CMRSummaryLG6";
      insCMRowCounter++;
    }
    if (CMRSummaryLG7 != null) {
      insCMRow += ",CMRSummaryLG7";
      insCMRowCounter++;
    }
    
    if (CMRTextLG1 != null) {
      insCMRow += ",CMRTextLG1";
      insCMRowCounter++;
    }
    if (CMRTextLG2 != null) {
      insCMRow += ",CMRTextLG2";
      insCMRowCounter++;
    }
    if (CMRTextLG3 != null) {
      insCMRow += ",CMRTextLG3";
      insCMRowCounter++;
    }
    if (CMRTextLG4 != null) {
      insCMRow += ",CMRTextLG4";
      insCMRowCounter++;
    }
    if (CMRTextLG5 != null) {
      insCMRow += ",CMRTextLG5";
      insCMRowCounter++;
    }
    if (CMRTextLG6 != null) {
      insCMRow += ",CMRTextLG6";
      insCMRowCounter++;
    }
    if (CMRTextLG7 != null) {
      insCMRow += ",CMRTextLG7";
      insCMRowCounter++;
    }
    
    if (CMRHeadHTML != null) {
      insCMRow += ",CMRHeadHTML";
      insCMRowCounter++;
    }
    if (CMRBodyHTML != null) {
      insCMRow += ",CMRBodyHTML";
      insCMRowCounter++;
    }
    if (CMRAttribs != null) {
      insCMRow += ",CMRAttribs";
      insCMRowCounter++;
    }
    
    insCMRow += ") VALUES (";
    
    for (int i=1; i<insCMRowCounter; i++) {
      insCMRow += "?,";
    }
    insCMRow += "?)";
    
    String insCMCRelCMR = "INSERT INTO CMCRelCMR " +
                   " (CCCRCode,CCCR_CMCCode,CCCR_CMRCode,CCCRPrimary," +
                   "  CCCRRank,CCCRIsHidden)" +
                   " VALUES (" +
                   "'" + CMRCode + "'," +
                   "'" + CMCCode + "'," +
                   "'" + CMRCode + "'," +
                   "'1'," +
                   rank + ",'" + CCCRIsHidden + "')";
    
    Database database = _director.getDBConnection(databaseId);
    DbRet dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    
    int prevTransIsolation = dbRet.getRetInt();
    
    int rowsAffected = 0;
    
    if (dbRet.getNoError() == 1) {
      try {
        ps = database.createPreparedStatement(insCMRow);
        
        ps.setString(1, CMRCode);
        ps.setTimestamp(2, CMRDateCreated);
        ps.setTimestamp(3, CMRDateUpdated);
        ps.setString(4, CMRTitle);
        ps.setString(5, CMRTitle1Up);
        ps.setString(6, CMRTitle2Up);
        ps.setString(7, CMRTitleLG);
        ps.setString(8, CMRTitle1UpLG);
        ps.setString(9, CMRTitle2UpLG);
        ps.setString(10, CMRKeyWords);
        ps.setString(11, CMRKeyWords1Up);
        ps.setString(12, CMRKeyWords2Up);
        ps.setString(13, CMRKeyWordsLG);
        ps.setString(14, CMRKeyWords1UpLG);
        ps.setString(15, CMRKeyWords2UpLG);
        ps.setString(16, CMRSummary);
        ps.setString(17, CMRSummaryLG);
        ps.setString(18, CMRText);
        ps.setString(19, CMRTextLG);
        ps.setString(20, CMRIsSticky);
        
        insCMRowCounter = 20;
        
        if (CMRIsProtected != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, CMRIsProtected);
        }
        
        if (CMRTitleLG1 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitleLG1));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle1UpLG1));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle2UpLG1));
        }
        if (CMRTitleLG2 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitleLG2));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle1UpLG2));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle2UpLG2));
        }
        if (CMRTitleLG3 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitleLG3));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle1UpLG3));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle2UpLG3));
        }
        if (CMRTitleLG4 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitleLG4));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle1UpLG4));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle2UpLG4));
        }
        if (CMRTitleLG5 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitleLG5));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle1UpLG5));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle2UpLG5));
        }
        if (CMRTitleLG6 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitleLG6));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle1UpLG6));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle2UpLG6));
        }
        if (CMRTitleLG7 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitleLG7));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle1UpLG7));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle2UpLG7));
        }
        
        if (CMRKeyWordsLG1 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWordsLG1));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords1UpLG1));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords2UpLG1));
        }
        if (CMRKeyWordsLG2 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWordsLG2));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords1UpLG2));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords2UpLG2));
        }
        if (CMRKeyWordsLG3 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWordsLG3));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords1UpLG3));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords2UpLG3));
        }
        if (CMRKeyWordsLG4 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWordsLG4));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords1UpLG4));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords2UpLG4));
        }
        if (CMRKeyWordsLG5 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWordsLG5));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords1UpLG5));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords2UpLG5));
        }
        if (CMRKeyWordsLG6 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWordsLG6));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords1UpLG6));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords2UpLG6));
        }
        if (CMRKeyWordsLG7 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWordsLG7));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords1UpLG7));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords2UpLG7));
        }
        
        if (CMRSummaryLG1 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRSummaryLG1));
        }
        if (CMRSummaryLG2 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRSummaryLG2));
        }
        if (CMRSummaryLG3 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRSummaryLG3));
        }
        if (CMRSummaryLG4 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRSummaryLG4));
        }
        if (CMRSummaryLG5 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRSummaryLG5));
        }
        if (CMRSummaryLG6 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRSummaryLG6));
        }
        if (CMRSummaryLG7 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRSummaryLG7));
        }
        
        if (CMRTextLG1 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTextLG1));
        }
        if (CMRTextLG2 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTextLG2));
        }
        if (CMRTextLG3 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTextLG3));
        }
        if (CMRTextLG4 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTextLG4));
        }
        if (CMRTextLG5 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTextLG5));
        }
        if (CMRTextLG6 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTextLG6));
        }
        if (CMRTextLG7 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTextLG7));
        }
        
        if (CMRHeadHTML != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRHeadHTML));
        }
        if (CMRBodyHTML != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRBodyHTML));
        }
        if (CMRAttribs != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRAttribs));
        }
        
        rowsAffected = ps.executeUpdate();
      }
      catch (Exception eee) {
        dbRet.setNoError(0);
        eee.printStackTrace();
      }
      finally {
        try { if (ps != null) ps.close(); } catch (Exception e) { }
      }
    }
    
    if (dbRet.getNoError() == 1) {
      dbRet = database.execQuery(insCMCRelCMR);
    }
    
    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    _director.freeDBConnection(databaseId,database);
    
    if (dbRet.getNoError() == 1) return Director.STATUS_OK;
    else return Director.STATUS_ERROR;
  }
  
  private int doUpdate(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

    int auth = _director.auth(databaseId,authUsername,authPassword,"CMRow",Director.AUTH_UPDATE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String CMRCode = SwissKnife.sqlEncode(request.getParameter("CMRCode")),
           CMRTitle = SwissKnife.sqlEncode(request.getParameter("CMRTitle")),
           CMRTitleLG = SwissKnife.sqlEncode(request.getParameter("CMRTitleLG")), 
           CMRKeyWords = SwissKnife.sqlEncode(request.getParameter("CMRKeyWords")),
           CMRKeyWordsLG = SwissKnife.sqlEncode(request.getParameter("CMRKeyWordsLG")), 
           CMRSummary = SwissKnife.sqlEncode(request.getParameter("CMRSummary")),
           CMRSummaryLG = SwissKnife.sqlEncode(request.getParameter("CMRSummaryLG")),
           CMRText = SwissKnife.sqlEncode(request.getParameter("CMRText")),
           CMRTextLG = SwissKnife.sqlEncode(request.getParameter("CMRTextLG")),
           CMRIsSticky = SwissKnife.sqlEncode(request.getParameter("CMRIsSticky")),
           CMRDateCreatedDay=null, CMRDateCreatedMonth=null, CMRDateCreatedYear=null,
           CMRDateUpdatedDay=null, CMRDateUpdatedMonth=null, CMRDateUpdatedYear=null;
    
    String CMRIsProtected = request.getParameter("CMRIsProtected");
    
    String CMRHeadHTML = request.getParameter("CMRHeadHTML"),
        CMRBodyHTML = request.getParameter("CMRBodyHTML"),
        CMRAttribs = null;
    
    String CMRDateCreatedHour = "0", CMRDateCreatedMin = "0", CMRDateCreatedSec = "0", CMRDateCreatedMil = "0";
    String CMRDateUpdatedHour = "0", CMRDateUpdatedMin = "0", CMRDateUpdatedSec = "0", CMRDateUpdatedMil = "0";
    
    Timestamp CMRDateCreated=null, CMRDateUpdated=null;
    
    if ( (CMRDateCreatedDay = request.getParameter("CMRDateCreatedDay")) == null ) CMRDateCreatedDay = "";
    if ( (CMRDateCreatedMonth = request.getParameter("CMRDateCreatedMonth")) == null ) CMRDateCreatedMonth = "";
    if ( (CMRDateCreatedYear = request.getParameter("CMRDateCreatedYear")) == null ) CMRDateCreatedYear = "";
    if (request.getParameter("CMRDateCreatedHour") != null) CMRDateCreatedHour = request.getParameter("CMRDateCreatedHour");
    if (request.getParameter("CMRDateCreatedMin") != null) CMRDateCreatedMin = request.getParameter("CMRDateCreatedMin");
    if (request.getParameter("CMRDateCreatedSec") != null) CMRDateCreatedSec = request.getParameter("CMRDateCreatedSec");
    if (request.getParameter("CMRDateCreatedMil") != null) CMRDateCreatedMil = request.getParameter("CMRDateCreatedMil");
    CMRDateCreated = SwissKnife.buildTimestamp(CMRDateCreatedDay,CMRDateCreatedMonth,CMRDateCreatedYear,CMRDateCreatedHour,CMRDateCreatedMin,CMRDateCreatedSec,CMRDateCreatedMil);
    
    if ( (CMRDateUpdatedDay = request.getParameter("CMRDateUpdatedDay")) == null ) CMRDateUpdatedDay = "";
    if ( (CMRDateUpdatedMonth = request.getParameter("CMRDateUpdatedMonth")) == null ) CMRDateUpdatedMonth = "";
    if ( (CMRDateUpdatedYear = request.getParameter("CMRDateUpdatedYear")) == null ) CMRDateUpdatedYear = "";
    if ( (CMRDateUpdatedDay = request.getParameter("CMRDateUpdatedDay")) == null ) CMRDateUpdatedDay = "";
    if ( (CMRDateUpdatedMonth = request.getParameter("CMRDateUpdatedMonth")) == null ) CMRDateUpdatedMonth = "";
    if ( (CMRDateUpdatedYear = request.getParameter("CMRDateUpdatedYear")) == null ) CMRDateUpdatedYear = "";
    if (request.getParameter("CMRDateUpdatedHour") != null) CMRDateUpdatedHour = request.getParameter("CMRDateUpdatedHour");
    if (request.getParameter("CMRDateUpdatedMin") != null) CMRDateUpdatedMin = request.getParameter("CMRDateUpdatedMin");
    if (request.getParameter("CMRDateUpdatedSec") != null) CMRDateUpdatedSec = request.getParameter("CMRDateUpdatedSec");
    if (request.getParameter("CMRDateUpdatedMil") != null) CMRDateUpdatedMil = request.getParameter("CMRDateUpdatedMil");
    CMRDateUpdated = SwissKnife.buildTimestamp(CMRDateUpdatedDay,CMRDateUpdatedMonth,CMRDateUpdatedYear,CMRDateUpdatedHour,CMRDateUpdatedMin,CMRDateUpdatedSec,CMRDateUpdatedMil);
    
    String stmp = null;
    stmp = CMRTitle.length() <= 80 ? CMRTitle.substring(0, CMRTitle.length()) : CMRTitle.substring(0, 80);
    String CMRTitle1Up = SwissKnife.searchConvert(stmp);
    stmp = CMRTitle.length() <= 80 ? "" : CMRTitle.substring(80, CMRTitle.length());    
    String CMRTitle2Up = SwissKnife.searchConvert(stmp);    
    
    stmp = CMRTitleLG.length() <= 80 ? CMRTitleLG.substring(0, CMRTitleLG.length()) : CMRTitleLG.substring(0, 80);    
    String CMRTitle1UpLG = SwissKnife.searchConvert(stmp);
    stmp = CMRTitleLG.length() <= 80 ? "" : CMRTitleLG.substring(80, CMRTitleLG.length());    
    String CMRTitle2UpLG = SwissKnife.searchConvert(stmp);      
    
    stmp = CMRKeyWords.length() <= 80 ? CMRKeyWords.substring(0, CMRKeyWords.length()) : CMRKeyWords.substring(0, 80);        
    String CMRKeyWords1Up = SwissKnife.searchConvert(stmp);    
    stmp = CMRKeyWords.length() <= 80 ? "" : CMRKeyWords.substring(80, CMRKeyWords.length());        
    String CMRKeyWords2Up = SwissKnife.searchConvert(stmp);
    
    stmp = CMRKeyWordsLG.length() <= 80 ? CMRKeyWordsLG.substring(0, CMRKeyWordsLG.length()) : CMRKeyWordsLG.substring(0, 80);            
    String CMRKeyWords1UpLG = SwissKnife.searchConvert(stmp);    
    stmp = CMRKeyWordsLG.length() <= 80 ? "" : CMRKeyWordsLG.substring(80, CMRKeyWordsLG.length());            
    String CMRKeyWords2UpLG = SwissKnife.searchConvert(stmp);    
    
    String CMRTitleLG1 = request.getParameter("CMRTitleLG1"),
           CMRTitleLG2 = request.getParameter("CMRTitleLG2"),
           CMRTitleLG3 = request.getParameter("CMRTitleLG3"),
           CMRTitleLG4 = request.getParameter("CMRTitleLG4"),
           CMRTitleLG5 = request.getParameter("CMRTitleLG5"),
           CMRTitleLG6 = request.getParameter("CMRTitleLG6"),
           CMRTitleLG7 = request.getParameter("CMRTitleLG7"),
           CMRKeyWordsLG1 = request.getParameter("CMRKeyWordsLG1"),
           CMRKeyWordsLG2 = request.getParameter("CMRKeyWordsLG2"),
           CMRKeyWordsLG3 = request.getParameter("CMRKeyWordsLG3"),
           CMRKeyWordsLG4 = request.getParameter("CMRKeyWordsLG4"),
           CMRKeyWordsLG5 = request.getParameter("CMRKeyWordsLG5"),
           CMRKeyWordsLG6 = request.getParameter("CMRKeyWordsLG6"),
           CMRKeyWordsLG7 = request.getParameter("CMRKeyWordsLG7"),
           CMRSummaryLG1 = request.getParameter("CMRSummaryLG1"),
           CMRSummaryLG2 = request.getParameter("CMRSummaryLG2"),
           CMRSummaryLG3 = request.getParameter("CMRSummaryLG3"),
           CMRSummaryLG4 = request.getParameter("CMRSummaryLG4"),
           CMRSummaryLG5 = request.getParameter("CMRSummaryLG5"),
           CMRSummaryLG6 = request.getParameter("CMRSummaryLG6"),
           CMRSummaryLG7 = request.getParameter("CMRSummaryLG7"),
           CMRTextLG1 = request.getParameter("CMRTextLG1"),
           CMRTextLG2 = request.getParameter("CMRTextLG2"),
           CMRTextLG3 = request.getParameter("CMRTextLG3"),
           CMRTextLG4 = request.getParameter("CMRTextLG4"),
           CMRTextLG5 = request.getParameter("CMRTextLG5"),
           CMRTextLG6 = request.getParameter("CMRTextLG6"),
           CMRTextLG7 = request.getParameter("CMRTextLG7");
           
    String CMRTitle1UpLG1 = null, CMRTitle2UpLG1 = null, CMRTitle1UpLG2 = null, CMRTitle2UpLG2 = null,
           CMRTitle1UpLG3 = null, CMRTitle2UpLG3 = null, CMRTitle1UpLG4 = null, CMRTitle2UpLG4 = null,
           CMRTitle1UpLG5 = null, CMRTitle2UpLG5 = null, CMRTitle1UpLG6 = null, CMRTitle2UpLG6 = null,
           CMRTitle1UpLG7 = null, CMRTitle2UpLG7 = null;
    
    String CMRKeyWords1UpLG1 = null, CMRKeyWords2UpLG1 = null, CMRKeyWords1UpLG2 = null, CMRKeyWords2UpLG2 = null,
           CMRKeyWords1UpLG3 = null, CMRKeyWords2UpLG3 = null, CMRKeyWords1UpLG4 = null, CMRKeyWords2UpLG4 = null,
           CMRKeyWords1UpLG5 = null, CMRKeyWords2UpLG5 = null, CMRKeyWords1UpLG6 = null, CMRKeyWords2UpLG6 = null,
           CMRKeyWords1UpLG7 = null, CMRKeyWords2UpLG7 = null;
    
    if (CMRTitleLG1 != null) {
      stmp = CMRTitleLG1.length() <= 80 ? CMRTitleLG1.substring(0, CMRTitleLG1.length()) : CMRTitleLG1.substring(0, 80);
      CMRTitle1UpLG1 = SwissKnife.searchConvert(stmp);
      stmp = CMRTitleLG1.length() <= 80 ? "" : CMRTitleLG1.substring(80, CMRTitleLG1.length());
      CMRTitle2UpLG1 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRTitleLG2 != null) {
      stmp = CMRTitleLG2.length() <= 80 ? CMRTitleLG2.substring(0, CMRTitleLG2.length()) : CMRTitleLG2.substring(0, 80);
      CMRTitle1UpLG2 = SwissKnife.searchConvert(stmp);
      stmp = CMRTitleLG2.length() <= 80 ? "" : CMRTitleLG2.substring(80, CMRTitleLG2.length());
      CMRTitle2UpLG2 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRTitleLG3 != null) {
      stmp = CMRTitleLG3.length() <= 80 ? CMRTitleLG3.substring(0, CMRTitleLG3.length()) : CMRTitleLG3.substring(0, 80);
      CMRTitle1UpLG3 = SwissKnife.searchConvert(stmp);
      stmp = CMRTitleLG3.length() <= 80 ? "" : CMRTitleLG3.substring(80, CMRTitleLG3.length());
      CMRTitle2UpLG3 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRTitleLG4 != null) {
      stmp = CMRTitleLG4.length() <= 80 ? CMRTitleLG4.substring(0, CMRTitleLG4.length()) : CMRTitleLG4.substring(0, 80);
      CMRTitle1UpLG4 = SwissKnife.searchConvert(stmp);
      stmp = CMRTitleLG4.length() <= 80 ? "" : CMRTitleLG4.substring(80, CMRTitleLG4.length());
      CMRTitle2UpLG4 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRTitleLG5 != null) {
      stmp = CMRTitleLG5.length() <= 80 ? CMRTitleLG5.substring(0, CMRTitleLG5.length()) : CMRTitleLG5.substring(0, 80);
      CMRTitle1UpLG5 = SwissKnife.searchConvert(stmp);
      stmp = CMRTitleLG5.length() <= 80 ? "" : CMRTitleLG5.substring(80, CMRTitleLG5.length());
      CMRTitle2UpLG5 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRTitleLG6 != null) {
      stmp = CMRTitleLG6.length() <= 80 ? CMRTitleLG6.substring(0, CMRTitleLG6.length()) : CMRTitleLG6.substring(0, 80);
      CMRTitle1UpLG6 = SwissKnife.searchConvert(stmp);
      stmp = CMRTitleLG6.length() <= 80 ? "" : CMRTitleLG6.substring(80, CMRTitleLG6.length());
      CMRTitle2UpLG6 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRTitleLG7 != null) {
      stmp = CMRTitleLG7.length() <= 80 ? CMRTitleLG7.substring(0, CMRTitleLG7.length()) : CMRTitleLG7.substring(0, 80);
      CMRTitle1UpLG7 = SwissKnife.searchConvert(stmp);
      stmp = CMRTitleLG7.length() <= 80 ? "" : CMRTitleLG7.substring(80, CMRTitleLG7.length());
      CMRTitle2UpLG7 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRKeyWordsLG1 != null) {
      stmp = CMRKeyWordsLG1.length() <= 80 ? CMRKeyWordsLG1.substring(0, CMRKeyWordsLG1.length()) : CMRKeyWordsLG1.substring(0, 80);
      CMRKeyWords1UpLG1 = SwissKnife.searchConvert(stmp);
      stmp = CMRKeyWordsLG1.length() <= 80 ? "" : CMRKeyWordsLG1.substring(80, CMRKeyWordsLG1.length());
      CMRKeyWords2UpLG1 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRKeyWordsLG2 != null) {
      stmp = CMRKeyWordsLG2.length() <= 80 ? CMRKeyWordsLG2.substring(0, CMRKeyWordsLG2.length()) : CMRKeyWordsLG2.substring(0, 80);
      CMRKeyWords1UpLG2 = SwissKnife.searchConvert(stmp);
      stmp = CMRKeyWordsLG2.length() <= 80 ? "" : CMRKeyWordsLG2.substring(80, CMRKeyWordsLG2.length());
      CMRKeyWords2UpLG2 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRKeyWordsLG3 != null) {
      stmp = CMRKeyWordsLG3.length() <= 80 ? CMRKeyWordsLG3.substring(0, CMRKeyWordsLG3.length()) : CMRKeyWordsLG3.substring(0, 80);
      CMRKeyWords1UpLG3 = SwissKnife.searchConvert(stmp);
      stmp = CMRKeyWordsLG3.length() <= 80 ? "" : CMRKeyWordsLG3.substring(80, CMRKeyWordsLG3.length());
      CMRKeyWords2UpLG3 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRKeyWordsLG4 != null) {
      stmp = CMRKeyWordsLG4.length() <= 80 ? CMRKeyWordsLG4.substring(0, CMRKeyWordsLG4.length()) : CMRKeyWordsLG4.substring(0, 80);
      CMRKeyWords1UpLG4 = SwissKnife.searchConvert(stmp);
      stmp = CMRKeyWordsLG4.length() <= 80 ? "" : CMRKeyWordsLG4.substring(80, CMRKeyWordsLG4.length());
      CMRKeyWords2UpLG4 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRKeyWordsLG5 != null) {
      stmp = CMRKeyWordsLG5.length() <= 80 ? CMRKeyWordsLG5.substring(0, CMRKeyWordsLG5.length()) : CMRKeyWordsLG5.substring(0, 80);
      CMRKeyWords1UpLG5 = SwissKnife.searchConvert(stmp);
      stmp = CMRKeyWordsLG5.length() <= 80 ? "" : CMRKeyWordsLG5.substring(80, CMRKeyWordsLG5.length());
      CMRKeyWords2UpLG5 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRKeyWordsLG6 != null) {
      stmp = CMRKeyWordsLG6.length() <= 80 ? CMRKeyWordsLG6.substring(0, CMRKeyWordsLG6.length()) : CMRKeyWordsLG6.substring(0, 80);
      CMRKeyWords1UpLG6 = SwissKnife.searchConvert(stmp);
      stmp = CMRKeyWordsLG6.length() <= 80 ? "" : CMRKeyWordsLG6.substring(80, CMRKeyWordsLG6.length());
      CMRKeyWords2UpLG6 = SwissKnife.searchConvert(stmp);
    }
    
    if (CMRKeyWordsLG7 != null) {
      stmp = CMRKeyWordsLG7.length() <= 80 ? CMRKeyWordsLG7.substring(0, CMRKeyWordsLG7.length()) : CMRKeyWordsLG7.substring(0, 80);
      CMRKeyWords1UpLG7 = SwissKnife.searchConvert(stmp);
      stmp = CMRKeyWordsLG7.length() <= 80 ? "" : CMRKeyWordsLG7.substring(80, CMRKeyWordsLG7.length());
      CMRKeyWords2UpLG7 = SwissKnife.searchConvert(stmp);
    }
    
    CMRAttribs = buildCMRAttribs(request);
    
    String query = "UPDATE CMRow SET " +
                   " CMRDateCreated = ?," +
                   " CMRDateUpdated = ?," +
                   " CMRTitle = ?," +
                   " CMRTitle1Up = ?," +
                   " CMRTitle2Up = ?," +
                   " CMRTitleLG = ?," +
                   " CMRTitle1UpLG = ?," +
                   " CMRTitle2UpLG = ?," +
                   " CMRKeyWords = ?," +
                   " CMRKeyWords1Up = ?," +
                   " CMRKeyWords2Up = ?," +
                   " CMRKeyWordsLG = ?," +
                   " CMRKeyWords1UpLG = ?," +
                   " CMRKeyWords2UpLG = ?," +
                   " CMRSummary = ?," +
                   " CMRSummaryLG = ?," +
                   " CMRText = ?," +
                   " CMRTextLG = ?," +
                   " CMRIsSticky = ?";
                   
    if (CMRIsProtected != null) {
      query += ", CMRIsProtected = ?";
    }
    
    if (CMRTitleLG1 != null) {
      query += ", CMRTitleLG1 = ?, CMRTitle1UpLG1 = ?, CMRTitle2UpLG1 = ?";
    }
    if (CMRTitleLG2 != null) {
      query += ", CMRTitleLG2 = ?, CMRTitle1UpLG2 = ?, CMRTitle2UpLG2 = ?";
    }
    if (CMRTitleLG3 != null) {
      query += ", CMRTitleLG3 = ?, CMRTitle1UpLG3 = ?, CMRTitle2UpLG3 = ?";
    }
    if (CMRTitleLG4 != null) {
      query += ", CMRTitleLG4 = ?, CMRTitle1UpLG4 = ?, CMRTitle2UpLG4 = ?";
    }
    if (CMRTitleLG5 != null) {
      query += ", CMRTitleLG5 = ?, CMRTitle1UpLG5 = ?, CMRTitle2UpLG5 = ?";
    }
    if (CMRTitleLG6 != null) {
      query += ", CMRTitleLG6 = ?, CMRTitle1UpLG6 = ?, CMRTitle2UpLG6 = ?";
    }
    if (CMRTitleLG7 != null) {
      query += ", CMRTitleLG7 = ?, CMRTitle1UpLG7 = ?, CMRTitle2UpLG7 = ?";
    }
    
    if (CMRKeyWordsLG1 != null) {
      query += ", CMRKeyWordsLG1 = ?, CMRKeyWords1UpLG1 = ?, CMRKeyWords2UpLG1 = ?";
    }
    if (CMRKeyWordsLG2 != null) {
      query += ",CMRKeyWordsLG2 = ?, CMRKeyWords1UpLG2 = ?, CMRKeyWords2UpLG2 = ?";
    }
    if (CMRKeyWordsLG3 != null) {
      query += ", CMRKeyWordsLG3 = ?, CMRKeyWords1UpLG3 = ?, CMRKeyWords2UpLG3 = ?";
    }
    if (CMRKeyWordsLG4 != null) {
      query += ",CMRKeyWordsLG4 = ?, CMRKeyWords1UpLG4 = ?, CMRKeyWords2UpLG4 = ?";
    }
    if (CMRKeyWordsLG5 != null) {
      query += ", CMRKeyWordsLG5 = ?, CMRKeyWords1UpLG5 = ?, CMRKeyWords2UpLG5 = ?";
    }
    if (CMRKeyWordsLG6 != null) {
      query += ", CMRKeyWordsLG6 = ?, CMRKeyWords1UpLG6 = ?, CMRKeyWords2UpLG6 = ?";
    }
    if (CMRKeyWordsLG7 != null) {
      query += ", CMRKeyWordsLG7 = ?, CMRKeyWords1UpLG7 = ?, CMRKeyWords2UpLG7 = ?";
    }
    
    if (CMRSummaryLG1 != null) {
      query += ", CMRSummaryLG1 = ?";
    }
    if (CMRSummaryLG2 != null) {
      query += ", CMRSummaryLG2 = ?";
    }
    if (CMRSummaryLG3 != null) {
      query += ", CMRSummaryLG3 = ?";
    }
    if (CMRSummaryLG4 != null) {
      query += ", CMRSummaryLG4 = ?";
    }
    if (CMRSummaryLG5 != null) {
      query += ", CMRSummaryLG5 = ?";
    }
    if (CMRSummaryLG6 != null) {
      query += ", CMRSummaryLG6 = ?";
    }
    if (CMRSummaryLG7 != null) {
      query += ", CMRSummaryLG7 = ?";
    }
    
    if (CMRTextLG1 != null) {
      query += ", CMRTextLG1 = ?";
    }
    if (CMRTextLG2 != null) {
      query += ", CMRTextLG2 = ?";
    }
    if (CMRTextLG3 != null) {
      query += ", CMRTextLG3 = ?";
    }
    if (CMRTextLG4 != null) {
      query += ", CMRTextLG4 = ?";
    }
    if (CMRTextLG5 != null) {
      query += ", CMRTextLG5 = ?";
    }
    if (CMRTextLG6 != null) {
      query += ", CMRTextLG6 = ?";
    }
    if (CMRTextLG7 != null) {
      query += ", CMRTextLG7 = ?";
    }
    
    if (CMRHeadHTML != null) {
      query += ", CMRHeadHTML = ?";
    }
    if (CMRBodyHTML != null) {
      query += ", CMRBodyHTML = ?";
    }
    if (CMRAttribs != null) {
      query += ", CMRAttribs = ?";
    }
    
    query += " WHERE CMRCode = ?";

    Database database = _director.getDBConnection(databaseId);
    DbRet dbRet = database.beginTransaction(Database.TRANS_ISO_1);
    
    int prevTransIsolation = dbRet.getRetInt();
    
    PreparedStatement ps = null;
    
    int rowsAffected = 0;
    
    if (dbRet.getNoError() == 1) {
      try {
        ps = database.createPreparedStatement(query);
        
        ps.setTimestamp(1, CMRDateCreated);
        ps.setTimestamp(2, CMRDateUpdated);
        ps.setString(3, CMRTitle);
        ps.setString(4, CMRTitle1Up);
        ps.setString(5, CMRTitle2Up);
        ps.setString(6, CMRTitleLG);
        ps.setString(7, CMRTitle1UpLG);
        ps.setString(8, CMRTitle2UpLG);
        ps.setString(9, CMRKeyWords);
        ps.setString(10, CMRKeyWords1Up);
        ps.setString(11, CMRKeyWords2Up);
        ps.setString(12, CMRKeyWordsLG);
        ps.setString(13, CMRKeyWords1UpLG);
        ps.setString(14, CMRKeyWords2UpLG);
        ps.setString(15, CMRSummary);
        ps.setString(16, CMRSummaryLG);
        ps.setString(17, CMRText);
        ps.setString(18, CMRTextLG);
        ps.setString(19, CMRIsSticky);
        
        int insCMRowCounter = 19;
        
        if (CMRIsProtected != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, CMRIsProtected);
        }
        
        if (CMRTitleLG1 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitleLG1));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle1UpLG1));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle2UpLG1));
        }
        if (CMRTitleLG2 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitleLG2));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle1UpLG2));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle2UpLG2));
        }
        if (CMRTitleLG3 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitleLG3));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle1UpLG3));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle2UpLG3));
        }
        if (CMRTitleLG4 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitleLG4));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle1UpLG4));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle2UpLG4));
        }
        if (CMRTitleLG5 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitleLG5));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle1UpLG5));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle2UpLG5));
        }
        if (CMRTitleLG6 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitleLG6));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle1UpLG6));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle2UpLG6));
        }
        if (CMRTitleLG7 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitleLG7));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle1UpLG7));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTitle2UpLG7));
        }
        
        if (CMRKeyWordsLG1 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWordsLG1));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords1UpLG1));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords2UpLG1));
        }
        if (CMRKeyWordsLG2 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWordsLG2));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords1UpLG2));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords2UpLG2));
        }
        if (CMRKeyWordsLG3 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWordsLG3));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords1UpLG3));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords2UpLG3));
        }
        if (CMRKeyWordsLG4 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWordsLG4));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords1UpLG4));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords2UpLG4));
        }
        if (CMRKeyWordsLG5 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWordsLG5));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords1UpLG5));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords2UpLG5));
        }
        if (CMRKeyWordsLG6 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWordsLG6));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords1UpLG6));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords2UpLG6));
        }
        if (CMRKeyWordsLG7 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWordsLG7));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords1UpLG7));
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRKeyWords2UpLG7));
        }
        
        if (CMRSummaryLG1 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRSummaryLG1));
        }
        if (CMRSummaryLG2 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRSummaryLG2));
        }
        if (CMRSummaryLG3 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRSummaryLG3));
        }
        if (CMRSummaryLG4 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRSummaryLG4));
        }
        if (CMRSummaryLG5 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRSummaryLG5));
        }
        if (CMRSummaryLG6 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRSummaryLG6));
        }
        if (CMRSummaryLG7 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRSummaryLG7));
        }
        
        if (CMRTextLG1 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTextLG1));
        }
        if (CMRTextLG2 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTextLG2));
        }
        if (CMRTextLG3 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTextLG3));
        }
        if (CMRTextLG4 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTextLG4));
        }
        if (CMRTextLG5 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTextLG5));
        }
        if (CMRTextLG6 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTextLG6));
        }
        if (CMRTextLG7 != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRTextLG7));
        }
        
        if (CMRHeadHTML != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRHeadHTML));
        }
        if (CMRBodyHTML != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRBodyHTML));
        }
        if (CMRAttribs != null) {
          insCMRowCounter++;
          ps.setString(insCMRowCounter, SwissKnife.sqlEncode(CMRAttribs));
        }
        
        insCMRowCounter++;
        ps.setString(insCMRowCounter, CMRCode);
        
        rowsAffected = ps.executeUpdate();
      }
      catch (Exception eee) {
        dbRet.setNoError(0);
        eee.printStackTrace();
      }
      finally {
        try { if (ps != null) ps.close(); } catch (Exception e) { }
      }
    }
    
    database.commitTransaction(dbRet.getNoError(),prevTransIsolation);
    _director.freeDBConnection(databaseId,database);
    
    if (dbRet.getNoError() == 1) return Director.STATUS_OK;
    else return Director.STATUS_ERROR;
  
  }

  /**
   * Διαγραφή κατηγορίας
   */
  private int doDelete(HttpServletRequest request, String databaseId) {
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

    int auth = 0;

    auth = _director.auth(databaseId,authUsername,authPassword,"CMRow",Director.AUTH_DELETE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String CMRCode = SwissKnife.sqlEncode(request.getParameter("CMRCode")),
        uploadPath = request.getParameter("guploadPath");

    if (CMRCode.equals("")) return Director.STATUS_ERROR;

    DbRet dbRet = null;
    
    // get database connection
    Database database = _director.getDBConnection(databaseId);

    // begin transaction
    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();

    String query = "";

    if (dbRet.getNoError() == 1) {
      query = "DELETE FROM CMRow WHERE CMRCode = '" + CMRCode + "'";

      dbRet = database.execQuery(query);
    }
    
    if (dbRet.getNoError() == 1 && uploadPath != null) {
      File file = null;
      
      for (int i=1; i<=20; i++) {
        file = new File(uploadPath,CMRCode + "-" + i + ".jpg");
        
        if (file.exists()) file.delete();
      }
      
      file = null;
    }

    // End transaction (commit or rollback)
    dbRet = database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    _director.freeDBConnection(databaseId,database);

    if (dbRet.getNoError() == 1)
      return Director.STATUS_OK;
    else
      return Director.STATUS_ERROR;
  }

  private int doSendToEmailList(HttpServletRequest request, String databaseId) {
    
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
           authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);
    int auth = 0;
    auth = _director.auth(databaseId,authUsername,authPassword,"CMRow",Director.AUTH_UPDATE);

    if (auth != Director.AUTH_OK) {
      return auth;
    }

    String ELCode = SwissKnife.sqlEncode(request.getParameter("ELCode"));
    if (ELCode.equals("")) return Director.STATUS_ERROR;
    String CMRCode = SwissKnife.sqlEncode(request.getParameter("CMRCode"));
    if (CMRCode.equals("")) return Director.STATUS_ERROR;    
    
    DbRet dbRet = null;
    dbRet = sendToEmailList(databaseId, ELCode, CMRCode);
   
    if (dbRet.getNoError() == 1)
      return Director.STATUS_OK;
    else
      return Director.STATUS_ERROR;    
  }  

  private DbRet sendToEmailList(String databaseId, String ELCode, String CMRCode) {
    DbRet dbRet = new DbRet();
    Director director = Director.getInstance();
    Database database = null;
    QueryDataSet membersDataSet = null;
    Timestamp today = SwissKnife.currentDate();
    
    String select = "SELECT RMCode,RMEmail,RMFirstName,RMLang,ELSmtpServer,ELConfRegFrom,ELTopHTML,ELTopHTMLLG,ELBottomHTML,ELBottomHTMLLG FROM registeredMember,emailList,ELRelRM"
                  + " WHERE ELCode = '" + ELCode + "'"
                  + "   AND RMIsActive = '1'"
                  + "   AND RMCode = ELRM_RMCode"
                  + "   AND ELCode = ELRM_ELCode";
    
    String RMCode = null, RMEmail = null, RMFirstName = null, RMLang = null;
    String ELSmtpServer = null, ELConfRegFrom = null, ELTopHTML = null, ELBottomHTML = null;
    
    
    try {
      database = director.getDBConnection(databaseId);
      membersDataSet = new QueryDataSet();
      membersDataSet.setQuery(new QueryDescriptor(database,select,null,true,Load.UNCACHED));
      membersDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);
      membersDataSet.refresh();
      
      while (membersDataSet.inBounds() == true) {
        
        try {
          RMCode = SwissKnife.sqlDecode(membersDataSet.getString("RMCode"));
          RMLang = SwissKnife.sqlDecode(membersDataSet.getString("RMLang"));
          RMEmail = SwissKnife.sqlDecode(membersDataSet.getString("RMEmail"));
          RMFirstName = SwissKnife.sqlDecode(membersDataSet.getString("RMFirstName"));
          
          ELSmtpServer = SwissKnife.sqlDecode(membersDataSet.getString("ELSmtpServer"));
          ELConfRegFrom = SwissKnife.sqlDecode(membersDataSet.getString("ELConfRegFrom"));
          ELTopHTML = SwissKnife.sqlDecode(membersDataSet.getString("ELTopHTML" + RMLang));
          ELBottomHTML = SwissKnife.sqlDecode(membersDataSet.getString("ELBottomHTML" + RMLang));
        
          dbRet = buildEmail(database,CMRCode,RMLang,RMEmail,RMFirstName,ELSmtpServer,ELConfRegFrom,ELTopHTML,ELBottomHTML);
        }
        catch (Exception e) {
          dbRet.setNoError(0);
          
          e.printStackTrace();
        }
        membersDataSet.next();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      try { membersDataSet.close(); } catch (Exception e) { }
      director.freeDBConnection(databaseId,database);
    }
    
    return dbRet;
  }
  
  private DbRet buildEmail(Database database,String CMRCode,String RMLang,String RMEmail,
                                String RMFirstName,String ELSmtpServer,String ELConfRegFrom,String ELTopHTML,String ELBottomHTML) {
    DbRet dbRet = null;
    
    QueryDataSet queryDataSet = null;
    
    String CMRText = null;
    
    String select = "SELECT CMRText" + RMLang + ",CMRTitle" + RMLang + " FROM CMRow"
                  + " WHERE CMRCode = '" + CMRCode + "'";
                  
    try {
      queryDataSet = new QueryDataSet();
      
      queryDataSet.setQuery(new QueryDescriptor(database,select,null,true,Load.ALL));
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();
      
      CMRText = SwissKnife.sqlDecode(queryDataSet.getString("CMRText" + RMLang)).replaceAll("\\^FIRSTNAME\\^", RMFirstName);
      
      dbRet = EmailSpooler.spoolEmail(database,RMEmail,ELConfRegFrom,SwissKnife.sqlDecode(queryDataSet.getString("CMRTitle" + RMLang)),ELTopHTML + " " + CMRText + " " + ELBottomHTML,ELSmtpServer,"text/html","UTF-8");
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      
      e.printStackTrace();
    }
    finally {
      try { queryDataSet.close(); } catch (Exception e) { }
    }
    
    return dbRet;
  }
  
  
  /**
   *  Εκτέλεση query απ' ευθείας στην βάση.
   *
   * @param  databaseId το αναγνωριστικό της βάσης που
   *                    θα χρησιμοποιηθεί
   * @param  query      το query προς εκτέλεση
   * @return            τον κωδικό κατάστασης
   */
  private int executeQuery(String databaseId, String query) {
    Database database = _director.getDBConnection(databaseId);

    int status = Director.STATUS_OK;
    
    DbRet dbRet = null;

    dbRet = database.execQuery(query);

    if (dbRet.getNoError() == 0) {
      status = Director.STATUS_ERROR;
    }

    _director.freeDBConnection(databaseId,database);

    return status;
  }
  
  private String buildCMRAttribs(HttpServletRequest request) {
    String CMRAttribs = null;
    
    Enumeration paramNames = request.getParameterNames();
    while(paramNames.hasMoreElements()) {
      String s = (String)paramNames.nextElement();
      
      if (s.startsWith(CMRowAttribs.PREFIX)) {
        if (CMRAttribs == null) CMRAttribs = "";
        CMRAttribs += s.substring(CMRowAttribs.PREFIX.length()) + CMRowAttribs.KEY_VALUE_SEP + request.getParameter(s) + CMRowAttribs.ATTRIB_SEP;
      }
    }
    
    return CMRAttribs;
  }
}