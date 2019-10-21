package com.jeofferson.onclas.PackageObjectModel;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post {


    private String objectId;

    private String poster1Id;
    private String poster2Id;
    private String posterPicture;
    private String poster1Name;
    private String poster2Name;
    private String postContent;
    private @ServerTimestamp Date dateCreated;
    private String postPicture;
    private String postType;


    public Post() {}
    public Post(String poster1Id, String poster2Id, String posterPicture, String poster1Name, String poster2Name, String postContent, Date dateCreated, String postPicture, String postType) {
        this.poster1Id = poster1Id;
        this.poster2Id = poster2Id;
        this.posterPicture = posterPicture;
        this.poster1Name = poster1Name;
        this.poster2Name = poster2Name;
        this.postContent = postContent;
        this.dateCreated = dateCreated;
        this.postPicture = postPicture;
        this.postType = postType;
    }


    @Exclude
    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getPoster1Id() {
        return poster1Id;
    }

    public void setPoster1Id(String poster1Id) {
        this.poster1Id = poster1Id;
    }

    public String getPoster2Id() {
        return poster2Id;
    }

    public void setPoster2Id(String poster2Id) {
        this.poster2Id = poster2Id;
    }

    public String getPosterPicture() {
        return posterPicture;
    }

    public void setPosterPicture(String posterPicture) {
        this.posterPicture = posterPicture;
    }

    public String getPoster1Name() {
        return poster1Name;
    }

    public void setPoster1Name(String poster1Name) {
        this.poster1Name = poster1Name;
    }

    public String getPoster2Name() {
        return poster2Name;
    }

    public void setPoster2Name(String poster2Name) {
        this.poster2Name = poster2Name;
    }
    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getPostPicture() {
        return postPicture;
    }

    public void setPostPicture(String postPicture) {
        this.postPicture = postPicture;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }


}
