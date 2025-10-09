package com.example.dailyboss.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.dailyboss.data.repository.TaskInstanceRepositoryImpl;

public class MorningReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        TaskInstanceRepositoryImpl taskRepo = new TaskInstanceRepositoryImpl(context);
        taskRepo.registerUsersWithTasksForToday();
        Log.d("EE", "Morning");
    }
}

