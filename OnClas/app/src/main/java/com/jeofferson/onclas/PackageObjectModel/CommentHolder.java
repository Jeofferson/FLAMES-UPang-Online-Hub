package com.jeofferson.onclas.PackageObjectModel;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class CommentHolder {


    private String objectId;

    private String commentOnId;
    private String commentId;
    private String commenter1Id;
    private String commenter2Id;
    private String commenterPicture;
    private String commenter1Name;
    private String commenter2Name;
    private String commentContent;
    private @ServerTimestamp Date dateCreated;
    private String commentPicture;


    public CommentHolder() {}
    public CommentHolder(String commentOnId, String commentId, String commenter1Id, String commenter2Id, String commenterPicture, String commenter1Name, String commenter2Name, String commentContent, Date dateCreated, String commentPicture) {
        this.commentOnId = commentOnId;
        this.commentId = commentId;
        this.commenter1Id = commenter1Id;
        this.commenter2Id = commenter2Id;
        this.commenterPicture = commenterPicture;
        this.commenter1Name = commenter1Name;
        this.commenter2Name = commenter2Name;
        this.commentContent = commentContent;
        this.dateCreated = dateCreated;
        this.commentPicture = commentPicture;
    }


    @Exclude
    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getCommentOnId() {
        return commentOnId;
    }

    public void setCommentOnId(String commentOnId) {
        this.commentOnId = commentOnId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommenter1Id() {
        return commenter1Id;
    }

    public void setCommenter1Id(String commenter1Id) {
        this.commenter1Id = commenter1Id;
    }

    public String getCommenter2Id() {
        return commenter2Id;
    }

    public void setCommenter2Id(String commenter2Id) {
        this.commenter2Id = commenter2Id;
    }

    public String getCommenterPicture() {
        return commenterPicture;
    }

    public void setCommenterPicture(String commenterPicture) {
        this.commenterPicture = commenterPicture;
    }

    public String getCommenter1Name() {
        return commenter1Name;
    }

    public void setCommenter1Name(String commenter1Name) {
        this.commenter1Name = commenter1Name;
    }

    public String getCommenter2Name() {
        return commenter2Name;
    }

    public void setCommenter2Name(String commenter2Name) {
        this.commenter2Name = commenter2Name;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getCommentPicture() {
        return commentPicture;
    }

    public void setCommentPicture(String commentPicture) {
        this.commentPicture = commentPicture;
    }


}
