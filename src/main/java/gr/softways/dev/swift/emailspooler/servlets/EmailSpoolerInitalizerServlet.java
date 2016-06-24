package gr.softways.dev.swift.emailspooler.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gr.softways.dev.swift.emailspooler.*;

import gr.softways.dev.jdbc.*;
import gr.softways.dev.util.*;

public class EmailSpoolerInitalizerServlet extends HttpServlet {

  private Timer _timer = null;
  
  // default interval period time before execution in milliseconds
  private int _intervalTime = 1000 * 60 * 1;
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    
    boolean isDaemon = true;
    
    if (config.getInitParameter("intervalTime") != null) {
      _intervalTime = Integer.parseInt(config.getInitParameter("intervalTime"));
    }
    
    _timer = new Timer(isDaemon);
    _timer.schedule(new EmailSpoolerTask(),1000 * 60 * 1,_intervalTime);
  }
  
  public void destroy() {
    _timer.cancel();
  }
}
