package com.jeofferson.onclas.PackageObjectModel;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

public class GeneralSearchItem {


    private String objectId;

    private String itemId;
    private String itemPicture;
    private String itemName;
    private String type;
    private List<String> departments = new ArrayList<>();
    private List<String> year = new ArrayList<>();
    private String fullType;
    private String itemType;
    private boolean isAuthorized;


    public GeneralSearchItem() {}
    public GeneralSearchItem(String itemId, String itemPicture, String itemName, String type, List<String> departments, List<String> year, String fullType, String itemType, boolean isAuthorized) {
        this.itemId = itemId;
        this.itemPicture = itemPicture;
        this.itemName = itemName;
        this.type = type;
        this.departments = departments;
        this.year = year;
        this.fullType = fullType;
        this.itemType = itemType;
        this.isAuthorized = isAuthorized;
    }


    @Exclude
    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemPicture() {
        return itemPicture;
    }

    public void setItemPicture(String itemPicture) {
        this.itemPicture = itemPicture;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
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

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public void setAuthorized(boolean authorized) {
        isAuthorized = authorized;
    }


}
