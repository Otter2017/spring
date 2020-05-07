package cn.gigahome.web.entity;

import java.util.HashMap;
import java.util.List;

public class User {
    private String userID;

    private String userName;

    private String password;

    private HashMap<String, Boolean> operatePrivileges;

    private Region region;

    private boolean isActive;

    private boolean isDelete;

    private List<String> departments;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public HashMap<String, Boolean> getOperatePrivileges() {
        return operatePrivileges;
    }

    public void setOperatePrivileges(HashMap<String, Boolean> operatePrivileges) {
        this.operatePrivileges = operatePrivileges;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public List<String> getDepartments() {
        return departments;
    }

    public void setDepartments(List<String> departments) {
        this.departments = departments;
    }
}
