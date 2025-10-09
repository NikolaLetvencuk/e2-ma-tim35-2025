// Putanja: com.example.dailyboss.domain.usecase/UpdateActiveDaysUseCase.java
package com.example.dailyboss.domain.usecase;

import com.example.dailyboss.data.repository.UserStatisticRepository;
import com.example.dailyboss.domain.model.UserStatistic;
import com.google.firebase.auth.FirebaseAuth;
import android.util.Log;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class UpdateActiveDaysUseCase {

    private final UserStatisticRepository statisticRepository;
    private final FirebaseAuth firebaseAuth;
    private static final String TAG = "UpdateActiveDaysUC";

    public UpdateActiveDaysUseCase(UserStatisticRepository statisticRepository) {
        this.statisticRepository = statisticRepository;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    public void execute() {
        String userId = firebaseAuth.getUid();
        if (userId == null) {
            Log.w(TAG, "Korisnik nije ulogovan. Ne mogu ažurirati uzastopne dane.");
            return;
        }

        statisticRepository.getUserStatistic(userId, new UserStatisticRepository.UserStatisticDataListener() {
            @Override
            public void onSuccess(UserStatistic statistic) {
                long currentTime = System.currentTimeMillis();
                long lastUpdate = statistic.getLastStreakUpdateTimestamp();

                // --- INICIJALIZACIJA KALENDARA ZA PROVERU DANA ---
                Calendar lastActiveCal = Calendar.getInstance();
                lastActiveCal.setTimeInMillis(lastUpdate);

                Calendar todayCal = Calendar.getInstance();
                todayCal.setTimeInMillis(currentTime);
                // ------------------------------------------------

                // Kreiranje kalendara za JUCERAŠNJI dan (za proveru niza)
                Calendar yesterdayCal = (Calendar) todayCal.clone();
                yesterdayCal.add(Calendar.DAY_OF_YEAR, -1);

                // Provera da li je poslednji put bilo DANAS (isti kalendarski dan)
                boolean wasActiveToday = (lastActiveCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                        lastActiveCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR));

                // Provera da li je poslednji put bilo JUČE (uzastopni niz)
                boolean wasActiveYesterday = (lastActiveCal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR) &&
                        lastActiveCal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR));

                // --- LOGIKA UZASTOPNOG NIZA ---

                if (wasActiveToday) {
                    // Slučaj 1: Već je zabeležen za DANAS.
                    Log.d(TAG, "Aktivnost za tekući dan je već zabeležena. Uzastopni niz se ne menja.");
                    statistic.setActiveDaysCount(1);

                    // Ažuriraj timestamp na TRENUTNO VREME
                    statistic.setLastStreakUpdateTimestamp(currentTime);

                    Log.w(TAG, "Uzastopni niz PREKINUT/POČETAK NOVOG. ActiveDaysCount resetovan na 1.");
                    saveStatistic(statistic);
                } else if (wasActiveYesterday) {
                    // Slučaj 2: Perfektan niz! Poslednja aktivnost je bila JUČE.

                    statistic.incrementActiveDaysCount(); // Povećaj uzastopni niz

                    statistic.setLastStreakUpdateTimestamp(currentTime);

                    Log.d(TAG, "Uzastopni niz produžen. ActiveDaysCount: " + statistic.getActiveDaysCount());
                    saveStatistic(statistic);

                } else {
                    // Slučaj 3: Niz prekinut (nije bilo ni danas ni juče, ili je ovo PRVO logovanje)

                    // Resetuj ActiveDaysCount na 1 (jer je korisnik aktivan DANAS, počinje novi niz)
                    statistic.setActiveDaysCount(1);

                    // Ažuriraj timestamp na TRENUTNO VREME
                    statistic.setLastStreakUpdateTimestamp(currentTime);

                    Log.w(TAG, "Uzastopni niz PREKINUT/POČETAK NOVOG. ActiveDaysCount resetovan na 1.");
                    saveStatistic(statistic);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Greška pri dohvatanju statistike: " + e.getMessage());
            }
        });
    }

    /**
     * Pomoćna metoda za čuvanje statistike.
     */
    private void saveStatistic(UserStatistic statistic) {
        statisticRepository.upsertUserStatistic(statistic, new UserStatisticRepository.UserStatisticDataListener() {
            @Override
            public void onSuccess(UserStatistic s) {
                Log.d(TAG, "Uspešno sačuvana ažurirana statistika.");
            }
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Greška pri čuvanju ažurirane statistike: " + e.getMessage());
            }
        });
    }
}