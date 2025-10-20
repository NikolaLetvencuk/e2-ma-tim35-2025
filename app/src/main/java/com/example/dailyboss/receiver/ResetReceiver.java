package com.example.dailyboss.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.dailyboss.data.dao.UserStatisticDao;
import com.example.dailyboss.domain.model.UserStatistic;
import com.example.dailyboss.utils.StreakTracker;

import java.util.Set;

public class ResetReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        UserStatisticDao userStatisticDao = new UserStatisticDao(context);

        Set<String> failedUsers = StreakTracker.getUsersWhoDidNotFinishAnyTask();

        for (String userId : failedUsers) {
            UserStatistic stat = userStatisticDao.getUserStatistic(userId);
            if (stat != null) {
                if (stat.getLongestTaskStreak() < stat.getCurrentTaskStreak()) {
                    stat.setLongestTaskStreak(stat.getCurrentTaskStreak());
                }
                stat.setCurrentTaskStreak(0);
                userStatisticDao.upsert(stat);
            }
        }
        Log.d("EE", "Morning2");

        // Resetuj tracker za sledeći dan
        StreakTracker.resetDailyTracking();
    }
}