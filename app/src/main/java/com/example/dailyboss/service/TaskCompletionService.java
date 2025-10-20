package com.example.dailyboss.service;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.dailyboss.data.SharedPreferencesHelper;
import com.example.dailyboss.data.dao.TaskInstanceDao;
import com.example.dailyboss.data.dao.UserProfileDao;
import com.example.dailyboss.data.repository.TaskTemplateRepositoryImpl;
import com.example.dailyboss.data.repository.UserStatisticRepository;
import com.example.dailyboss.domain.enums.TaskStatus;
import com.example.dailyboss.domain.model.TaskInstance;
import com.example.dailyboss.domain.model.TaskTemplate;
import com.example.dailyboss.domain.model.UserProfile;
import com.example.dailyboss.domain.model.UserStatistic;


public class TaskCompletionService {

    private static final String TAG = "TaskCompletionService";

    private final Context context;
    private final TaskInstanceDao taskInstanceDao;
    private final TaskTemplateRepositoryImpl taskTemplateRepository;
    private final UserStatisticRepository userStatisticRepository;
    private final UserProfileDao userProfileDao;
    private final LevelingService levelingService;
    private final SharedPreferencesHelper prefs;
    
    public interface LevelUpCallback {
        void onLevelUp(int newLevel, String newTitle);
    }
    
    private LevelUpCallback levelUpCallback;
    
    public void setLevelUpCallback(LevelUpCallback callback) {
        this.levelUpCallback = callback;
    }

    public TaskCompletionService(Context context) {
        this.context = context.getApplicationContext();
        this.taskInstanceDao = new TaskInstanceDao(context);
        this.taskTemplateRepository = new TaskTemplateRepositoryImpl(context);
        this.userStatisticRepository = new UserStatisticRepository(context);
        this.userProfileDao = new UserProfileDao(context);
        this.levelingService = new LevelingService(context);
        this.prefs = new SharedPreferencesHelper(context);

        LevelingService.testPPFormula();
    }

    public static class TaskCompletionResult {
        public boolean success;
        public int xpGained;
        public boolean leveledUp;
        public int newLevel;
        public String newTitle;

        public TaskCompletionResult(boolean success, int xpGained, boolean leveledUp, int newLevel, String newTitle) {
            this.success = success;
            this.xpGained = xpGained;
            this.leveledUp = leveledUp;
            this.newLevel = newLevel;
            this.newTitle = newTitle;
        }
    }


    public TaskCompletionResult completeTaskWithResult(String instanceId, TaskStatus newStatus) {
        Log.d(TAG, "=== START completeTaskWithResult ===");
        Log.d(TAG, "instanceId: " + instanceId + ", newStatus: " + newStatus);
        
        TaskInstance instance = taskInstanceDao.findTaskById(instanceId);
        if (instance == null) {
            Log.e(TAG, "Task instance not found: " + instanceId);
            return new TaskCompletionResult(false, 0, false, 0, "");
        }
        Log.d(TAG, "Task instance found: " + instance.getInstanceId());

        TaskTemplate template = taskTemplateRepository.getAllTaskTemplates()
                .stream()
                .filter(t -> t.getTemplateId().equals(instance.getTemplateId()))
                .findFirst()
                .orElse(null);

        if (template == null) {
            Log.e(TAG, "Task template not found for instance: " + instanceId);
            return new TaskCompletionResult(false, 0, false, 0, "");
        }
        Log.d(TAG, "Task template found: " + template.getName() + ", Difficulty: " + template.getDifficulty() + ", Importance: " + template.getImportance());

        String userId = prefs.getLoggedInUserId();
        if (userId == null) {
            Log.e(TAG, "No logged in user");
            return new TaskCompletionResult(false, 0, false, 0, "");
        }
        Log.d(TAG, "User ID: " + userId);

        instance.setStatus(newStatus);
        boolean updated = taskInstanceDao.update(instance);

        if (!updated) {
            Log.e(TAG, "Failed to update task instance status");
            return new TaskCompletionResult(false, 0, false, 0, "");
        }

        if (newStatus == TaskStatus.DONE) {
            Log.d(TAG, "Status is DONE, adding XP...");
            
            UserStatistic stats = userStatisticRepository.getUserStatistic(userId);
            UserProfile userProfile = userProfileDao.getByUserId(userId);
            if (stats == null) {
                Log.e(TAG, "User statistics not found for userId: " + userId);
                Log.d(TAG, "Creating new UserStatistic for user: " + userId);
                
                stats = new UserStatistic(
                        java.util.UUID.randomUUID().toString(),
                        userId,
                        0,
                        0,
                        1,
                        0,
                        50,
                        0,
                        0,
                        0,
                        0,
                        System.currentTimeMillis(),
                        0,
                        0,
                        0,
                        300,
                        "Novajlija"
                );
                
                boolean created = userStatisticRepository.saveOrUpdate(stats);
                if (!created) {
                    Log.e(TAG, "Failed to create UserStatistic");
                    return new TaskCompletionResult(false, 0, false, 0, "");
                }
                Log.d(TAG, "UserStatistic created successfully");
            }
            Log.d(TAG, "User stats found - Level: " + userProfile.getLevel() + ", Current XP: " + userProfile.getExperiencePoints());

            int xpGained = LevelingService.calculateTotalTaskXP(
                    template.getDifficulty(),
                    template.getImportance(),
                    userProfile.getLevel()
            );

            Log.d(TAG, String.format("Task completed: %s, XP gained: %d (before level: %d)", template.getName(), xpGained, userProfile.getLevel()));

            boolean leveledUp = levelingService.addExperiencePoints(userId, xpGained);
            Log.d(TAG, "addExperiencePoints result - leveledUp: " + leveledUp + ", xpGained: " + xpGained);

            stats = userStatisticRepository.getUserStatistic(userId); // Refresh stats
            Log.d(TAG, "Refreshed stats - level: " + stats.getLevel() + ", XP: " + stats.getTotalXPPoints() + ", PP: " + stats.getPowerPoints());
            userProfile.setCompletedTasks(userProfile.getCompletedTasks() + 1);
            boolean saved = userStatisticRepository.saveOrUpdate(stats);
            Log.d(TAG, "Stats saved: " + saved + ", New completed tasks: " + userProfile.getCompletedTasks() + ", New XP: " + userProfile.getExperiencePoints());

            if (leveledUp) {
                LevelingService.LevelInfo levelInfo = levelingService.getLevelInfo(userId);
                if (levelInfo != null) {
                    String message = String.format("🎉 Nivo %d dostignut! Nova titula: %s (+%d PP)",
                            levelInfo.currentLevel,
                            levelInfo.title,
                            LevelingService.calculatePPRewardForLevel(levelInfo.currentLevel));
                    if (leveledUp) {
                        userProfile.setLastActiveTimestamp(System.currentTimeMillis()); // Set timestamp on level up
                        Log.d(TAG, "User leveled up! Updated UserProfile.lastActiveTimestamp to " + userProfile.getLastActiveTimestamp());
                    }
                    android.os.Handler handler = new android.os.Handler(context.getMainLooper());
                    handler.post(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
                    
                    // Pozovi callback ako postoji
                    if (levelUpCallback != null) {
                        handler.post(() -> levelUpCallback.onLevelUp(levelInfo.currentLevel, levelInfo.title));
                    }
                    
                    return new TaskCompletionResult(true, xpGained, true, levelInfo.currentLevel, levelInfo.title);
                }
            }

            return new TaskCompletionResult(true, xpGained, false, userProfile.getLevel(), stats.getTitle());
        } else if (newStatus == TaskStatus.CANCELLED) {
            // Uvećaj broj neuspešnih zadataka
            UserStatistic stats = userStatisticRepository.getUserStatistic(userId);
            if (stats != null) {
                stats.incrementTotalFailedTasks();
                userStatisticRepository.saveOrUpdate(stats);
            }
        }

        return new TaskCompletionResult(true, 0, false, 0, "");
    }


    @Deprecated
    public boolean completeTask(String instanceId, TaskStatus newStatus) {
        return completeTaskWithResult(instanceId, newStatus).success;
    }

    public interface TaskCompletionListener {
        void onTaskCompleted(boolean leveledUp, int xpGained);
        void onError(String errorMessage);
    }

    public void completeTaskAsync(String instanceId, TaskStatus newStatus, TaskCompletionListener listener) {
        new Thread(() -> {
            try {
                boolean success = completeTask(instanceId, newStatus);
                
                android.os.Handler handler = new android.os.Handler(context.getMainLooper());
                handler.post(() -> {
                    if (success) {
                        // Možete dodati dodatnu logiku za callback
                        listener.onTaskCompleted(false, 0); // Implementirajte vraćanje pravih vrednosti
                    } else {
                        listener.onError("Greška pri završavanju zadatka");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error completing task: " + e.getMessage());
                android.os.Handler handler = new android.os.Handler(context.getMainLooper());
                handler.post(() -> listener.onError(e.getMessage()));
            }
        }).start();
    }
}