package com.jeofferson.onclas.PackageObjectModel;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class NotificationHolder {


    private String objectId;

    private String fromId;
    private String toId;
    private String fromPicture;
    private String fromName;
    private String toPicture;
    private String toName;
    private String notificationType;
    private String notificationContent;
    private String notificationSpecificContent;
    private @ServerTimestamp Date dateCreated;
    private String destinationPostHolder;
    private String destinationPost;
    private String destinationCommentHolder;
    private String destinationComment;
    private String destinationReplyHolder;


    public NotificationHolder() {}
    public NotificationHolder(String fromId, String toId, String fromPicture, String fromName, String toPicture, String toName, String notificationType, String notificationContent, String notificationSpecificContent, Date dateCreated, String destinationPostHolder, String destinationPost, String destinationCommentHolder, String destinationComment, String destinationReplyHolder) {
        this.fromId = fromId;
        this.toId = toId;
        this.fromPicture = fromPicture;
        this.fromName = fromName;
        this.toPicture = toPicture;
        this.toName = toName;
        this.notificationType = notificationType;
        this.notificationContent = notificationContent;
        this.notificationSpecificContent = notificationSpecificContent;
        this.dateCreated = dateCreated;
        this.destinationPostHolder = destinationPostHolder;
        this.destinationPost = destinationPost;
        this.destinationCommentHolder = destinationCommentHolder;
        this.destinationComment = destinationComment;
        this.destinationReplyHolder = destinationReplyHolder;
    }


    @Exclude
    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getFromPicture() {
        return fromPicture;
    }

    public void setFromPicture(String fromPicture) {
        this.fromPicture = fromPicture;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getToPicture() {
        return toPicture;
    }

    public void setToPicture(String toPicture) {
        this.toPicture = toPicture;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getNotificationContent() {
        return notificationContent;
    }

    public void setNotificationContent(String notificationContent) {
        this.notificationContent = notificationContent;
    }

    public String getNotificationSpecificContent() {
        return notificationSpecificContent;
    }

    public void setNotificationSpecificContent(String notificationSpecificContent) {
        this.notificationSpecificContent = notificationSpecificContent;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDestinationPostHolder() {
        return destinationPostHolder;
    }

    public void setDestinationPostHolder(String destinationPostHolder) {
        this.destinationPostHolder = destinationPostHolder;
    }

    public String getDestinationPost() {
        return destinationPost;
    }

    public void setDestinationPost(String destinationPost) {
        this.destinationPost = destinationPost;
    }

    public String getDestinationCommentHolder() {
        return destinationCommentHolder;
    }

    public void setDestinationCommentHolder(String destinationCommentHolder) {
        this.destinationCommentHolder = destinationCommentHolder;
    }

    public String getDestinationComment() {
        return destinationComment;
    }

    public void setDestinationComment(String destinationComment) {
        this.destinationComment = destinationComment;
    }

    public String getDestinationReplyHolder() {
        return destinationReplyHolder;
    }

    public void setDestinationReplyHolder(String destinationReplyHolder) {
        this.destinationReplyHolder = destinationReplyHolder;
    }


}
