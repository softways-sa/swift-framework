package gr.softways.dev.swift.cmrow;

import com.mortennobel.imagescaling.*;
import gr.softways.dev.util.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.*;
import org.apache.commons.fileupload.servlet.*;

public class GalleryImageScaleServlet extends HttpServlet {

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    DbRet dbRet = new DbRet();
    
    HashMap<String, String> params = new HashMap();
        
    // Create a factory for disk-based file items
    FileItemFactory factory = new DiskFileItemFactory();

    // Create a new file upload handler
    ServletFileUpload upload = new ServletFileUpload(factory);
    upload.setSizeMax(DEFAULT_MAX_FILE_SIZE);
    upload.setFileSizeMax(DEFAULT_MAX_FILE_SIZE);
    
    List items = null;
    
    try {
      // Parse the request
      items = upload.parseRequest(request);
    }
    catch (Exception e) {
      response.sendRedirect("/admin/problem.jsp");
      return;
    }
    
    processParams(params, items);
    
    String urlNoAccess = params.get("urlNoAccess"),
        urlSuccess = params.get("urlSuccess"),
        urlFailure = params.get("urlFailure"),
        uploadPath = params.get("uploadPath");
    
    String CMRCode = params.get("CMRCode"), bgColor = params.get("bgColor");
    
    int imageMaxWidth = 0, imageMaxHeight = 0;
    
    try {
      imageMaxWidth = Integer.parseInt( params.get("imageMaxWidth") );
      imageMaxHeight = Integer.parseInt( params.get("imageMaxHeight") );
    }
    catch (Exception  e) {
    }

    String authUsername = SwissKnife.getSessionAttr(databaseId + ".authUsername",request),
            authPassword = SwissKnife.getSessionAttr(databaseId + ".authPassword",request);

    int auth = director.auth(databaseId,authUsername,authPassword,"uploadFiles",Director.AUTH_UPDATE);

    if (auth != Director.AUTH_OK) {
      response.sendRedirect(urlNoAccess);
      return;
    }
    
    // Process the uploaded items
    Iterator<FileItem> iter = items.iterator();

    while (iter.hasNext()) {
      FileItem item = iter.next();

      //if (item.isFormField()) System.out.println(item.getFieldName() + " = " + item.getString());
      //else System.out.println(item.getFieldName() + " = " + item.getName());
      
      if (!item.isFormField() && item.getName() != null && item.getName().length() > 0) {
        processUploadedFile(item, uploadPath + "/" + CMRCode + "-" + item.getFieldName() + ".jpg", imageMaxWidth, imageMaxHeight, bgColor);
        item.delete();
      }
    }
    
    if (dbRet.getNoError() == 1) response.sendRedirect(urlSuccess);
    else response.sendRedirect(urlFailure);
  }
  
  private DbRet processUploadedFile(FileItem item, String filename, int maxWidth, int maxHeight, String bgColor) {
    DbRet dbRet = new DbRet();
    
    BufferedImage resizedImage = null, image = null, canvas = null;
    
    Graphics2D graphics2D = null;
    
    File resizedImgFile = null;
        
    int w = maxWidth, h = maxHeight;
        
    int x = 0, y = 0;
    
    try {
      image = (BufferedImage) ImageIO.read(item.getInputStream());
      
      ResampleOp resampleOp = new ResampleOp(DimensionConstrain.createMaxDimension(w, h));
      resizedImage = resampleOp.filter(image, null);

      x = (int)((w - resizedImage.getWidth()) / 2);
      y = (int)((h - resizedImage.getHeight()) / 2);

      // Draw the scaled image
      canvas = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

      graphics2D = canvas.createGraphics();
      graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);  
      graphics2D.setColor(Color.decode(bgColor));
      graphics2D.fillRect(0, 0, w, h);
      graphics2D.drawImage(resizedImage, x, y, null);

      image.flush();
      image = null;

      resizedImgFile = new File(filename);

      ImageIO.write(canvas, "jpg", resizedImgFile);
    }
    catch (Exception e) {
      e.printStackTrace();
      
      dbRet.setNoError(0);
    }
    
    return dbRet;
  }
  
  private void processParams(HashMap<String, String> params, List items) {
    Iterator<FileItem> iter = items.iterator();

    while (iter.hasNext()) {
      FileItem item = iter.next();

      if (item.isFormField()) {
        params.put(item.getFieldName(), item.getString());
      }
    }
  }
  
  private static String databaseId = SwissKnife.jndiLookup("swconf/databaseId");
  private static Director director = Director.getInstance();
  
  private static final int DEFAULT_MAX_FILE_SIZE = 3145728; // 3 MB
}