package com.example.dailyboss.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.dailyboss.data.SharedPreferencesHelper;
import com.example.dailyboss.data.repository.AllianceRepository;
import com.example.dailyboss.data.repository.UserRepository;
import com.example.dailyboss.domain.model.Alliance;
import com.example.dailyboss.domain.model.User;
import com.example.dailyboss.service.AllianceNotificationHelper;
import com.google.android.gms.tasks.Task; // Korišćenje ispravnog uvoza

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

        // 💡 KLJUČNA IZMENA: Koristimo Task<?> (Džoker tip)
        Task<?> repositoryTask = null;

        if ("ACTION_ACCEPT_INVITE".equals(action)) {
            Log.d("ActionReceiver", "Pozvana akcija: PRIHVATI za Savez ID: " + allianceId + "idinv: " + invitationId);

            // Vaš acceptInvitation sada ima logičan potpis: acceptInvitation(invitationId, receiverId)
            // Pretpostavljamo da ste refaktorisali acceptInvitation да користи само два аргумента,
            // као што је предложено у претходном одговору.
            // *AKO NISTE refaktorisali, koristite stari poziv:*
            // repositoryTask = allianceRepository.acceptInvitation(invitationId, receiverId, allianceId);

            repositoryTask = allianceRepository.acceptInvitation(invitationId, receiverId, allianceId); // Koristite trenutni potpis
        } else if ("ACTION_REJECT_INVITE".equals(action)) {
            Log.d("ActionReceiver", "Pozvana akcija: ODBIJ za Savez ID: " + allianceId);

            repositoryTask = allianceRepository.rejectInvitation(invitationId);
        }

        if (repositoryTask != null) {
            repositoryTask.addOnCompleteListener(task -> {
                // Zadržite NotificationManager, NotificationHelper, pendingResult.finish() u finaly bloku
                final NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                try {
                    if (task.isSuccessful()) {
                        Log.d("ActionReceiver", "Uspeh! Akcija " + action + " završena.");
                        notificationManager.cancel(notificationId);

                        if ("ACTION_ACCEPT_INVITE".equals(action)) {

                            if (task.getResult() instanceof Alliance) {
                                Alliance acceptedAlliance = (Alliance) task.getResult();

                                // 1. DOHVATANJE USERNAME-A ONOGA KO JE PRIHVATIO (asinhrono)
                                userRepository.getUserData(receiverId, new UserRepository.UserDataListener() {
                                    @Override
                                    public void onSuccess(User user) {
                                        // 2. OVO SE IZVRŠAVA NAKON DOHVATANJA USERNAME-A
                                        String acceptedUsername = user.getUsername();

                                        // Stvarna notifikacija vođi
                                        AllianceNotificationHelper helper = new AllianceNotificationHelper(context);
                                        helper.notifyLeaderOfAcceptance(
                                                acceptedAlliance.getName(),
                                                acceptedUsername
                                        );

                                        // 💡 BITNO: OBAVEZNO ZAVRŠITI pendingResult NAKON SVIH ASINHRONIH POZIVA
                                        // U ovom složenom slučaju, najbolje je da pendingResult.finish()
                                        // pozovete tek u poslednjem listeneru.
                                        // Međutim, zbog prirode BroadcastReceiver-a i Task-ova,
                                        // najsigurnije je pozvati ga OVDE, NAKON POSLEDNJE USPEŠNE AKCIJE.
                                        pendingResult.finish();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e("ActionReceiver", "Greška pri dohvaćanju username-a: " + e.getMessage());

                                        // Moramo zatvoriti Receiver čak i ako ovo ne uspe
                                        pendingResult.finish();
                                    }
                                });
                                // ❌ NEMOJTE OVDE ZVATI pendingResult.finish() jer je gornji poziv asinhron!
                                // Umesto toga, pendingResult.finish() se premešta u onSuccess/onFailure bloka.
                                return; // Završi izvršavanje ovog dela, čekajući na gornji listener
                            }
                        }

                        // Ako nije ACTION_ACCEPT_INVITE ili nema Alliance objekta, završavamo ovde.
                        pendingResult.finish();

                    } else {
                        Log.e("ActionReceiver", "Greška pri obavljanju akcije: ", task.getException());
                        // U slučaju neuspeha, završavamo odmah
                        pendingResult.finish();
                    }
                } catch (Exception e) {
                    Log.e("ActionReceiver", "Neočekivana greška: ", e);
                    pendingResult.finish();
                }
            });
        } else {
            // Ako repositoryTask nije ni pokrenut, završavamo odmah
            pendingResult.finish();
        }
    }
}