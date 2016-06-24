package gr.softways.dev.poolmanager;

import javax.servlet.http.*;

import java.io.Serializable;

import gr.softways.dev.util.Director;

public class AuthEmp implements Serializable,HttpSessionBindingListener {

public AuthEmp() {
    this.username = "";
    this.password = "";
    this.accessLevel = 0;
    this.databaseId = "";
    this.userCounter = 1;
  }

  public AuthEmp(String username, String password, int accessLevel, String databaseId) {
    this.username = username;
    this.password = password;
    this.accessLevel = accessLevel;
    this.databaseId = databaseId;
    this.userCounter = 1;
  }

  public AuthEmp(String username, String password, int accessLevel) {
    this.username = username;
    this.password = password;
    this.accessLevel = accessLevel;
    this.userCounter = 1;
  }

  private String username;

  public void setUsername(String username) {
    this.username = username;
  }

  public String getUsername() {
    return this.username;
  }

  private String password;

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPassword() {
    return this.password;
  }

  private int accessLevel;

  public void setAccessLevel(int accessLevel) {
    this.accessLevel = accessLevel;
  }

  public int getAccessLevel() {
    return this.accessLevel;
  }

  private String databaseId;
  public void setDatabaseId(String databaseId) {
    this.databaseId = databaseId;
  }
  public String getDatabaseId() {
    return this.databaseId;
  }

  private int userCounter;
  public void setUserCounter(int userCounter) {
    this.userCounter = userCounter;
  }
  public int getUserCounter() {
    return this.userCounter;
  }
  public void increaseUserCounter() {
    this.userCounter++;
  }
  public void decreaseUserCounter() {
    this.userCounter--;
  }

  public void valueBound(HttpSessionBindingEvent event) {
  }

  public void valueUnbound(HttpSessionBindingEvent event) {
    //dbBean bean = new dbBean();

    //bean.poolManager.removeAuthUser(this.getDatabaseId(), this);
    Director.getInstance().removeAuthUser(this.getDatabaseId(), this);
    
    //System.out.println(this.username + " from " + this.getDatabaseId() + " sessioned out.");

    //bean = null;
  }
}