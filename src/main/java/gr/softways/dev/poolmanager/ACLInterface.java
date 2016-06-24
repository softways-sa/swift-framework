package gr.softways.dev.poolmanager;

public interface ACLInterface {
  // permissions for authentication
  public static final int AUTH_READ = 1;
  public static final int AUTH_INSERT = 2;
  public static final int AUTH_UPDATE = 4;
  public static final int AUTH_DELETE = 8;

  // επιστροφή του authentication
  public static final int AUTH_OK = 1;
  
  public static final int AUTH_NOACCESS = -1;
  public static final int AUTH_PASSWORD_MISMATCH = -2;
  public static final int AUTH_NOUSER = -3;
  public static final int AUTH_NOPOOL = -4;
  
  // legacy reasons for some servlets
  public static final int STATUS_OK = 1;
  public static final int STATUS_ERROR = 2;
}