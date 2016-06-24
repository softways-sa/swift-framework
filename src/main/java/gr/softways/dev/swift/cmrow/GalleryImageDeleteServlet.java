package gr.softways.dev.swift.cmrow;

import gr.softways.dev.eshop.product.v2.*;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.util.*;

public class GalleryImageDeleteServlet extends HttpServlet {
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    DbRet dbRet = new DbRet();
    
    String urlSuccess = request.getParameter("urlSuccess") == null ? "" : request.getParameter("urlSuccess"),
           urlFailure = request.getParameter("urlFailure") == null ? "" : request.getParameter("urlFailure"),
           urlNoAccess = request.getParameter("urlNoAccess") == null ? "" : request.getParameter("urlNoAccess");
    
    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
            authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);
    
    int auth = director.auth(databaseId,authUsername,authPassword,"uploadFiles",Director.AUTH_UPDATE);

    if (auth != Director.AUTH_OK) {
      response.sendRedirect(urlNoAccess);
      return;
    }
    
    String uploadPath = request.getParameter("uploadPath"),
        CMRCode = request.getParameter("CMRCode"),
        slotNum = request.getParameter("slotNum"),
        prdImage = "";
    
    prdImage = uploadPath + "/" + CMRCode + "-" + slotNum + ".jpg";
    doDeleteImg(prdImage);
    
    prdImage = uploadPath + "/" + CMRCode + "-" + slotNum + "z.jpg";
    doDeleteImg(prdImage);
    
    response.sendRedirect(urlSuccess);
  }
  
  private DbRet doDeleteImg(String prdImage) {
    DbRet dbRet = new DbRet();

    boolean delFile = false;
    
    File file = new File(prdImage);
           
    if (file.exists()) delFile = file.delete();
    
    if (!delFile) dbRet.setNoError(0);
    
    return dbRet;
  }
  
  private static String databaseId = SwissKnife.jndiLookup("swconf/databaseId");
  private static Director director = Director.getInstance();
}