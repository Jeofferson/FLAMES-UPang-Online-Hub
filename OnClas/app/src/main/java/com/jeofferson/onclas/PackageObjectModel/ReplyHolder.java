package com.jeofferson.onclas.PackageObjectModel;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class ReplyHolder {


    private String objectId;

    private String replyOnId;
    private String replier1Id;
    private String replier2Id;
    private String replierPicture;
    private String replier1Name;
    private String replier2Name;
    private String replyContent;
    private @ServerTimestamp Date dateCreated;
    private String replyPicture;


    public ReplyHolder() {}
    public ReplyHolder(String replyOnId, String replier1Id, String replier2Id, String replierPicture, String replier1Name, String replier2Name, String replyContent, Date dateCreated, String replyPicture) {
        this.replyOnId = replyOnId;
        this.replier1Id = replier1Id;
        this.replier2Id = replier2Id;
        this.replierPicture = replierPicture;
        this.replier1Name = replier1Name;
        this.replier2Name = replier2Name;
        this.replyContent = replyContent;
        this.dateCreated = dateCreated;
        this.replyPicture = replyPicture;
    }


    @Exclude
    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getReplyOnId() {
        return replyOnId;
    }

    public void setReplyOnId(String replyOnId) {
        this.replyOnId = replyOnId;
    }

    public String getReplier1Id() {
        return replier1Id;
    }

    public void setReplier1Id(String replier1Id) {
        this.replier1Id = replier1Id;
    }

    public String getReplier2Id() {
        return replier2Id;
    }

    public void setReplier2Id(String replier2Id) {
        this.replier2Id = replier2Id;
    }

    public String getReplierPicture() {
        return replierPicture;
    }

    public void setReplierPicture(String replierPicture) {
        this.replierPicture = replierPicture;
    }

    public String getReplier1Name() {
        return replier1Name;
    }

    public void setReplier1Name(String replier1Name) {
        this.replier1Name = replier1Name;
    }

    public String getReplier2Name() {
        return replier2Name;
    }

    public void setReplier2Name(String replier2Name) {
        this.replier2Name = replier2Name;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getReplyPicture() {
        return replyPicture;
    }

    public void setReplyPicture(String replyPicture) {
        this.replyPicture = replyPicture;
    }


}
