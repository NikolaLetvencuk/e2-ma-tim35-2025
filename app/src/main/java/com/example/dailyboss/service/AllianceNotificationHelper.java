package com.example.dailyboss.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.example.dailyboss.R;
import com.example.dailyboss.receiver.AllianceActionReceiver;

public class AllianceNotificationHelper {

    private final Context context;
    private final NotificationManager notificationManager;
    private static final String CHANNEL_ID = "alliance_invitations";
    private static final String CHANNEL_NAME = "Pozivi u Savez";
    private static final int NOTIFICATION_ID = 101;
    private static final int LEADER_NOTIFICATION_ID = 102;

    public AllianceNotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Sistemski pozivi za ulazak u savez.");
            notificationManager.createNotificationChannel(channel);
        }
    }


    public void showAllianceInvitation(String invitationId, String allianceId, String allianceName, String senderUsername, String reciverId) {

        Intent intentData = new Intent()
                .putExtra("ALLIANCE_ID", allianceId)
                .putExtra("NOTIFICATION_ID", NOTIFICATION_ID)
                .putExtra("INVITATION_ID", invitationId)
                .putExtra("RECIVER_ID", reciverId);
        Intent acceptIntent = new Intent(context, AllianceActionReceiver.class)
                .setAction("ACTION_ACCEPT_INVITE")
                .putExtras(intentData);

        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(
                context,
                (int) System.currentTimeMillis() + 1,
                acceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0) // ISPRAVLJENO: 4 ARGUMENTA
        );


        Intent rejectIntent = new Intent(context, AllianceActionReceiver.class)
                .setAction("ACTION_REJECT_INVITE")
                .putExtras(intentData);

        PendingIntent rejectPendingIntent = PendingIntent.getBroadcast(
                context,
                (int) System.currentTimeMillis() + 2,
                rejectIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Poziv za savez!")
                .setContentText(senderUsername + " te poziva u savez " + allianceName + ".")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setAutoCancel(false)
                .addAction(R.drawable.ic_notification, "Prihvati", acceptPendingIntent)
                .addAction(R.drawable.ic_notification, "Odbij", rejectPendingIntent);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void notifyLeaderOfAcceptance(String allianceName, String acceptedUsername) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(acceptedUsername + " se pridružio!")
                .setContentText(acceptedUsername + " je prihvatio poziv i pridružio se savezu " + allianceName + ".")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(LEADER_NOTIFICATION_ID, builder.build());
    }
}