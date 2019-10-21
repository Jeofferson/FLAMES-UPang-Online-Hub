package com.jeofferson.onclas.PackageObjectModel;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

public class User {


    private String objectId;

    private String username;
    private String userCover;
    private String userPicture;
    private String firstName;
    private String lastName;
    private String userFullName;
    private String userBio;
    private String gender;
    private long birthday;
    private String type;
    private List<String> departments = new ArrayList<>();
    private List<String> year = new ArrayList<>();
    private String fullType;
    private boolean isAuthorized;


    public User() {}
    public User(String username, String userCover, String userPicture, String firstName, String lastName, String userFullName, String userBio, String gender, long birthday, String type, List<String> departments, List<String> year, String fullType, boolean isAuthorized) {
        this.username = username;
        this.userCover = userCover;
        this.userPicture = userPicture;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userFullName = userFullName;
        this.userBio = userBio;
        this.gender = gender;
        this.birthday = birthday;
        this.type = type;
        this.departments = departments;
        this.year = year;
        this.fullType = fullType;
        this.isAuthorized = isAuthorized;
    }


    @Exclude
    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserCover() {
        return userCover;
    }

    public void setUserCover(String userCover) {
        this.userCover = userCover;
    }

    public String getUserPicture() {
        return userPicture;
    }

    public void setUserPicture(String userPicture) {
        this.userPicture = userPicture;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getUserBio() {
        return userBio;
    }

    public void setUserBio(String userBio) {
        this.userBio = userBio;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public long getBirthday() {
        return birthday;
    }

    public void setBirthday(long birthday) {
        this.birthday = birthday;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getDepartments() {
        return departments;
    }

    public void setDepartments(List<String> departments) {
        this.departments = departments;
    }

    public List<String> getYear() {
        return year;
    }

    public void setYear(List<String> year) {
        this.year = year;
    }

    public String getFullType() {
        return fullType;
    }

    public void setFullType(String fullType) {
        this.fullType = fullType;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public void setAuthorized(boolean authorized) {
        isAuthorized = authorized;
    }


}
