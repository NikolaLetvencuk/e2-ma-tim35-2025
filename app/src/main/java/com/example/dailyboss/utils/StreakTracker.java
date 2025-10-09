package com.example.dailyboss.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class StreakTracker {

    // Thread-safe skup korisnika koji imaju taskove danas, a nisu završili nijedan
    private static final Set<String> usersWithPendingTasks =
            Collections.synchronizedSet(new HashSet<>());

    /**
     * Dodaj usera koji danas ima taskove (poziva se ujutru kad se taskovi generišu)
     */
    public static void registerUserWithTasks(String userId) {
        usersWithPendingTasks.add(userId);
    }

    /**
     * Kad user završi neki task, brišemo ga iz liste
     */
    public static void markUserAsCompleted(String userId) {
        usersWithPendingTasks.remove(userId);
    }

    /**
     * Vrati sve korisnike koji nisu završili nijedan task
     */
    public static Set<String> getUsersWhoDidNotFinishAnyTask() {
        return new HashSet<>(usersWithPendingTasks);
    }

    /**
     * Resetuje listu za novi dan
     */
    public static void resetDailyTracking() {
        usersWithPendingTasks.clear();
    }
}