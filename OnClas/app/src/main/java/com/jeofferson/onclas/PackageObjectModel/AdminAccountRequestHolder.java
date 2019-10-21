package com.jeofferson.onclas.PackageObjectModel;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class AdminAccountRequestHolder {


    private String objectId;

    private DocumentReference userReference;
    private DocumentReference generalSearchItemReference;
    private String userPicture;
    private String userFullName;
    private String fullType;
    private @ServerTimestamp Date dateCreated;
    private boolean isDecided;


    public AdminAccountRequestHolder() {}
    public AdminAccountRequestHolder(DocumentReference userReference, DocumentReference generalSearchItemReference, String userPicture, String userFullName, String fullType, Date dateCreated, boolean isDecided) {
        this.userReference = userReference;
        this.generalSearchItemReference = generalSearchItemReference;
        this.userPicture = userPicture;
        this.userFullName = userFullName;
        this.fullType = fullType;
        this.dateCreated = dateCreated;
        this.isDecided = isDecided;
    }


    @Exclude
    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public DocumentReference getUserReference() {
        return userReference;
    }

    public void setUserReference(DocumentReference userReference) {
        this.userReference = userReference;
    }

    public DocumentReference getGeneralSearchItemReference() {
        return generalSearchItemReference;
    }

    public void setGeneralSearchItemReference(DocumentReference generalSearchItemReference) {
        this.generalSearchItemReference = generalSearchItemReference;
    }

    public String getUserPicture() {
        return userPicture;
    }

    public void setUserPicture(String userPicture) {
        this.userPicture = userPicture;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getFullType() {
        return fullType;
    }

    public void setFullType(String fullType) {
        this.fullType = fullType;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean isDecided() {
        return isDecided;
    }

    public void setDecided(boolean decided) {
        this.isDecided = decided;
    }


}
