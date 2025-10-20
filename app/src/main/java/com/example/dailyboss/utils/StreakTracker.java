package com.example.dailyboss.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class StreakTracker {

    private static final Set<String> usersWithPendingTasks =
            Collections.synchronizedSet(new HashSet<>());

    public static void registerUserWithTasks(String userId) {
        usersWithPendingTasks.add(userId);
    }

    public static void markUserAsCompleted(String userId) {
        usersWithPendingTasks.remove(userId);
    }

    public static Set<String> getUsersWhoDidNotFinishAnyTask() {
        return new HashSet<>(usersWithPendingTasks);
    }

    public static void resetDailyTracking() {
        usersWithPendingTasks.clear();
    }
}