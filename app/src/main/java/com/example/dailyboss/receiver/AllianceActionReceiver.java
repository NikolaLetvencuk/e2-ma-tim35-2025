package com.example.dailyboss.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.dailyboss.data.repository.AllianceRepository;
import com.example.dailyboss.data.repository.UserRepository;
import com.example.dailyboss.domain.model.Alliance;
import com.example.dailyboss.domain.model.User;
import com.example.dailyboss.service.AllianceNotificationHelper;
import com.google.android.gms.tasks.Task;

public class AllianceActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        final PendingResult pendingResult = goAsync();

        final String action = intent.getAction();
        final int notificationId = intent.getIntExtra("NOTIFICATION_ID", -1);
        final String allianceId = intent.getStringExtra("ALLIANCE_ID");
        final String invitationId = intent.getStringExtra("INVITATION_ID");
        String receiverId = intent.getStringExtra("RECIVER_ID");

        if (notificationId == -1 || allianceId == null) {
            Log.e("ActionReceiver", "Nedostaju podaci (ID notifikacije ili Saveza).");
            pendingResult.finish();
            return;
        }

        if (receiverId == null) {
            Log.e("ActionReceiver", "Korisnik nije ulogovan. Ne može obaviti akciju.");
            pendingResult.finish();
            return;
        }

        AllianceRepository allianceRepository = new AllianceRepository(context);
        UserRepository userRepository = new UserRepository(context);

        Task<?> repositoryTask = null;

        if ("ACTION_ACCEPT_INVITE".equals(action)) {
            Log.d("ActionReceiver", "Pozvana akcija: PRIHVATI za Savez ID: " + allianceId + "idinv: " + invitationId);
            repositoryTask = allianceRepository.acceptInvitation(invitationId, receiverId, allianceId);
        } else if ("ACTION_REJECT_INVITE".equals(action)) {
            Log.d("ActionReceiver", "Pozvana akcija: ODBIJ za Savez ID: " + allianceId);

            repositoryTask = allianceRepository.rejectInvitation(invitationId);
        }

        if (repositoryTask != null) {
            repositoryTask.addOnCompleteListener(task -> {
                final NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                try {
                    if (task.isSuccessful()) {
                        Log.d("ActionReceiver", "Uspeh! Akcija " + action + " završena.");
                        notificationManager.cancel(notificationId);

                        if ("ACTION_ACCEPT_INVITE".equals(action)) {

                            if (task.getResult() instanceof Alliance) {
                                Alliance acceptedAlliance = (Alliance) task.getResult();

                                userRepository.getUserData(receiverId, new UserRepository.UserDataListener() {
                                    @Override
                                    public void onSuccess(User user) {
                                        String acceptedUsername = user.getUsername();

                                        AllianceNotificationHelper helper = new AllianceNotificationHelper(context);
                                        helper.notifyLeaderOfAcceptance(
                                                acceptedAlliance.getName(),
                                                acceptedUsername
                                        );

                                        pendingResult.finish();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e("ActionReceiver", "Greška pri dohvaćanju username-a: " + e.getMessage());

                                        pendingResult.finish();
                                    }
                                });
                                return;
                            }
                        }

                        pendingResult.finish();

                    } else {
                        Log.e("ActionReceiver", "Greška pri obavljanju akcije: ", task.getException());
                        pendingResult.finish();
                    }
                } catch (Exception e) {
                    Log.e("ActionReceiver", "Neočekivana greška: ", e);
                    pendingResult.finish();
                }
            });
        } else {
            pendingResult.finish();
        }
    }
}