package com.jeofferson.onclas.PackageObjectModel;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class UserPhoto {


    private String objectId;

    private String userId;
    private String userPhotoDownloadUrl;
    private @ServerTimestamp Date dateCreated;


    public UserPhoto() {}
    public UserPhoto(String userId, String userPhotoDownloadUrl, Date dateCreated) {
        this.userId = userId;
        this.userPhotoDownloadUrl = userPhotoDownloadUrl;
        this.dateCreated = dateCreated;
    }


    @Exclude
    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPhotoDownloadUrl() {
        return userPhotoDownloadUrl;
    }

    public void setUserPhotoDownloadUrl(String userPhotoDownloadUrl) {
        this.userPhotoDownloadUrl = userPhotoDownloadUrl;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }


}
