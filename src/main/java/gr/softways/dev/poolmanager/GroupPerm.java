package gr.softways.dev.poolmanager;

public class GroupPerm {

public GroupPerm() {
    this.userGroupId = "";
    this.SPObject = "";
    this.SPPerm = 0;
  }

  public GroupPerm(String userGroupId, String SPObject, int SPPerm) {
    this.userGroupId = userGroupId;
    this.SPObject = SPObject;
    this.SPPerm = SPPerm;
  }

  private String userGroupId;

  public void setUserGroupId(String userGroupId) {
    this.userGroupId = userGroupId;
  }

  public String getUserGroupId() {
    return this.userGroupId;
  }

  private String SPObject;

  public void setSPObject(String SPObject) {
    this.SPObject = SPObject;
  }

  public String getSPObject() {
    return this.SPObject;
  }

  private int SPPerm;

  public void setSPPerm(int SPPerm) {
    this.SPPerm = SPPerm;
  }

  public int getSPPerm() {
    return this.SPPerm;
  }
} 