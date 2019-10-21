package com.jeofferson.onclas.PackageOthers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.jeofferson.onclas.R;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String messageTitle = remoteMessage.getNotification().getTitle();
        String messageBody = remoteMessage.getNotification().getBody();

        String clickAction = remoteMessage.getNotification().getClickAction();

        String notificationType = remoteMessage.getData().get("notificationType");
        String destinationPostHolder = remoteMessage.getData().get("destinationPostHolder");
        String destinationPost = remoteMessage.getData().get("destinationPost");
        String destinationCommentHolder = remoteMessage.getData().get("destinationCommentHolder");
        String destinationComment = remoteMessage.getData().get("destinationComment");
        String destinationReplyHolder = remoteMessage.getData().get("destinationReplyHolder");

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(messageTitle)
                    .setContentText(messageBody)
                    .setAutoCancel(true);

        Intent intentResult = new Intent(clickAction);
        intentResult.putExtra("notificationType", notificationType);
        intentResult.putExtra("destinationPostHolder", destinationPostHolder);
        intentResult.putExtra("destinationPost", destinationPost);
        intentResult.putExtra("destinationCommentHolder", destinationCommentHolder);
        intentResult.putExtra("destinationComment", destinationComment);
        intentResult.putExtra("destinationReplyHolder", destinationReplyHolder);

        PendingIntent pendingIntent = PendingIntent.getActivity(FirebaseMessagingService.this, 0, intentResult, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        int notificationId = (int) System.currentTimeMillis();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 26) {

            String channelId = "Your_channel_id";

            NotificationChannel notificationChannel = new NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
            builder.setChannelId(channelId);

        }

        notificationManager.notify(notificationId, builder.build());

    }


}
