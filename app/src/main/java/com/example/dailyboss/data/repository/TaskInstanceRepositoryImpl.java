package com.example.dailyboss.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.dailyboss.data.SharedPreferencesHelper;
import com.example.dailyboss.data.dao.TaskInstanceDao;
import com.example.dailyboss.data.dao.TaskTemplateDao;
import com.example.dailyboss.data.dao.UserCategoryStatisticDao;
import com.example.dailyboss.data.dao.UserStatisticDao;
import com.example.dailyboss.data.dto.TaskDetailDto;
import com.example.dailyboss.domain.enums.TaskImportance;
import com.example.dailyboss.domain.enums.TaskStatus;
import com.example.dailyboss.domain.model.TaskInstance;
import com.example.dailyboss.domain.model.TaskTemplate;
import com.example.dailyboss.domain.model.UserCategoryStatistic;
import com.example.dailyboss.domain.model.UserStatistic;
import com.example.dailyboss.domain.repository.ITaskInstanceRepository;
import com.example.dailyboss.utils.StreakTracker;
import com.github.mikephil.charting.data.Entry;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TaskInstanceRepositoryImpl implements ITaskInstanceRepository {

    private final TaskInstanceDao taskInstanceDAO;
    private final TaskTemplateDao taskTemplateDao;
    private final UserStatisticDao userStatisticDao;
    private final UserCategoryStatisticDao userCategoryStatisticDao;
    private final Context context;
    private final SharedPreferencesHelper prefs;
    private String userId;
    public TaskInstanceRepositoryImpl(Context context) {
        this.taskInstanceDAO = new TaskInstanceDao(context);
        this.taskTemplateDao = new TaskTemplateDao(context);
        this.userStatisticDao = new UserStatisticDao(context);
        this.userCategoryStatisticDao = new UserCategoryStatisticDao(context);
        this.prefs = new SharedPreferencesHelper(context);
        this.context = context.getApplicationContext();
        this.userId = prefs.getLoggedInUserId();
    }

    @Override
    public boolean addTaskInstance(String taskId, long instanceDate, TaskStatus status, String templateId) {
        String instanceId = UUID.randomUUID().toString();
        TaskInstance taskInstance = new TaskInstance(instanceId, instanceDate, status, templateId, userId);
        return taskInstanceDAO.insert(taskInstance);
    }

    @Override
    public List<TaskInstance> getAllTaskInstances() {
        // 🔒 Vraćaj samo task instance trenutno ulogovanog korisnika
        return taskInstanceDAO.getAllByUserId(userId);
    }

    @Override
    public boolean deleteById(String instanceId) {
        return taskInstanceDAO.deleteById(instanceId);
    }

    @Override
    public boolean updateTaskInstance(TaskInstance taskInstance) {
        return taskInstanceDAO.update(taskInstance);
    }

    @Override
    public int countByCategoyId(String categoryId) {
        List<TaskTemplate> taskTemplates = taskTemplateDao.getByCategoryId(categoryId);
        int count = 0;
        for (TaskTemplate task : taskTemplates) {
            count += taskInstanceDAO.getByTaskId(task.getTemplateId());
        }
        return count;
    }

    @Override
    public List<TaskInstance> getTasksByDateRange(long startTimestamp, long endTimestamp) {
        long startOfDay = getStartOfDay(startTimestamp);
        long endOfDay = getEndOfDay(endTimestamp);
        // 🔒 Vraćaj samo task instance trenutno ulogovanog korisnika u datumu
        return taskInstanceDAO.getTasksByDateRangeAndUserId(startOfDay, endOfDay, userId);
    }

    // Helper metode
    private long getStartOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getEndOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    public TaskDetailDto findTaskDetailById(String instanceId) {
        return taskInstanceDAO.findTaskDetailById(instanceId);
    }

    public boolean updateTaskStatus(String instanceId, TaskStatus newStatus, String categoryId) {
        UserStatistic userStatistic = userStatisticDao.getUserStatistic(userId);
        switch (newStatus) {
            case UNDONE:
                userStatistic.incrementTotalFailedTasks();
                break;
            case DONE:
                userStatistic.incrementTotalCompletedTasks();
                UserCategoryStatistic userCategoryStatistic = userCategoryStatisticDao.getCategoryStatistic(userId, categoryId);
                userCategoryStatistic.incrementCompletedTasks();
                userStatistic.incrementCurrentStrak();
                if(userStatistic.getLongestTaskStreak() < userStatistic.getCurrentTaskStreak()) {
                    userStatistic.setLongestTaskStreak(userStatistic.getCurrentTaskStreak());
                }
                if(isSpecialMission(instanceId))
                    userStatistic.incrementTotalSpecialMissionsCompleted();

                userStatistic = updateAverageXPEarned(userStatistic, instanceId);

                StreakTracker.markUserAsCompleted(userId);
                userCategoryStatisticDao.upsert(userCategoryStatistic);
                break;
            case CANCELLED:
                userStatistic.incrementTotalCancelledTasks();
                break;
            case PAUSED:
                break;
            case ACTIVE:
                if(isSpecialMission(instanceId))
                    userStatistic.incrementTotalSpecialMissionsStarted();
                break;
            default:
                throw new IllegalArgumentException("Unknown task status: " + newStatus);
        }
        userStatisticDao.upsert(userStatistic);

        return taskInstanceDAO.updateTaskStatus(instanceId, newStatus);
    }

    private boolean isSpecialMission(String instanceId) {
        TaskInstance taskInstance = taskInstanceDAO.findTaskById(instanceId);
        TaskTemplate taskTemplate = taskTemplateDao.getById(taskInstance.getTemplateId());
        if (taskTemplate.getImportance() == TaskImportance.SPECIAL) {
            return true;
        }
        return  false;
    }

    public int deleteFutureInstancesFromDate(String templateId, long dateBoundary) {
        return taskInstanceDAO.deleteFutureInstancesFromDate(templateId, dateBoundary);
    }

    public void registerUsersWithTasksForToday() {
        List<String> usersWithTasksToday = taskInstanceDAO.getUsersWithTasksForToday();

        for (String userId : usersWithTasksToday) {
            StreakTracker.registerUserWithTasks(userId);
        }
    }

    private UserStatistic updateAverageXPEarned(UserStatistic statistic, String instanceId) {
        TaskInstance taskInstance = taskInstanceDAO.findTaskById(instanceId);
        TaskTemplate taskTemplate = taskTemplateDao.getById(taskInstance.getTemplateId());

        int xpEarned = taskTemplate.getDifficulty().getXpValue();

        int totalCompleted = statistic.getTotalCompletedTasks();
        double oldAverageXP = statistic.getAverageXPEarned() * (totalCompleted - 1);
        double newAverageXP = (oldAverageXP + xpEarned) / totalCompleted;

        statistic.setTotalXPPoints(statistic.getTotalXPPoints() + xpEarned);

        statistic.setAverageXPEarned(newAverageXP);
        return  statistic;
    }

    public String getAverageDifficultyLevelDescription(String userId) {
        UserStatistic statistic = userStatisticDao.getUserStatistic(userId);
        if (statistic == null || statistic.getTotalCompletedTasks() == 0) {
            return "Nema dovoljno podataka za proračun.";
        }

        double averageXP = statistic.getAverageXPEarned();

        if (averageXP >= 14.0) {
            return "Uglavnom rešavate EKSTREMNO TEŠKE zadatke. 👑 Prosečno XP " + averageXP;
        } else if (averageXP >= 5.0) {
            return "Uglavnom rešavate TEŠKE zadatke. 💪 Prosečno XP " + averageXP;
        } else if (averageXP >= 2.0) {
            return "Uglavnom rešavate zadatke NORMALNE TEŽINE. ⭐ Prosečno XP " + averageXP;
        } else {
            return "Uglavnom rešavate LAKE zadatke. 😊 Prosečno XP " + averageXP;
        }
    }

    private double calculateDailyAverageXP(String userId, long dateTimestamp) {
        long startOfDay = getStartOfDay(dateTimestamp);
        long endOfDay = getEndOfDay(dateTimestamp);

        List<TaskInstance> completedInstances = taskInstanceDAO.getCompletedTasksForDay(userId, startOfDay, endOfDay);

        if (completedInstances.isEmpty()) {
            return 0.0;
        }

        double totalXPSum = 0;

        Map<String, TaskTemplate> templatesCache = new HashMap<>();

        for (TaskInstance instance : completedInstances) {
            String templateId = instance.getTemplateId();

            TaskTemplate template = templatesCache.get(templateId);
            if (template == null) {
                template = taskTemplateDao.getById(templateId);
                if (template != null) {
                    templatesCache.put(templateId, template);
                }
            }

            if (template != null && template.getDifficulty() != null) {
                totalXPSum += template.getDifficulty().getXpValue();
            }
        }

        return totalXPSum / completedInstances.size();
    }

    public List<Entry> getAverageDifficultyTrend(String userId) {
        List<Entry> entries = new ArrayList<>();

        Calendar cal = Calendar.getInstance();

        for (int i = 4; i >= 0; i--) {
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.add(Calendar.DAY_OF_YEAR, -i);

            int xIndex = 4 - i;

            double averageXP = calculateDailyAverageXP(userId, cal.getTimeInMillis());

            entries.add(new Entry(xIndex, (float) averageXP));
        }

        return entries;
    }

    public List<Entry> getDailyXPProgress() {
        final int numberOfDays = 7;

        List<Entry> entries = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        Map<String, TaskTemplate> templatesCache = new HashMap<>();

        for (int i = numberOfDays - 1; i >= 0; i--) {
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.add(Calendar.DAY_OF_YEAR, -i);

            long dayTimestamp = cal.getTimeInMillis();
            long startOfDay = getStartOfDay(dayTimestamp);
            long endOfDay = getEndOfDay(dayTimestamp);

            if (userId == null) {
                Log.e("XPProgress", "User ID is null. Cannot fetch XP progress.");
                break;
            }

            List<TaskInstance> completedInstances = taskInstanceDAO.getCompletedTasksForDay(userId, startOfDay, endOfDay);

            int totalXPSum = 0;

            for (TaskInstance instance : completedInstances) {
                String templateId = instance.getTemplateId();

                TaskTemplate template = templatesCache.get(templateId);
                if (template == null) {
                    template = taskTemplateDao.getById(templateId);
                    if (template != null) {
                        templatesCache.put(templateId, template);
                    }
                }

                if (template != null && template.getDifficulty() != null) {
                    totalXPSum += template.getDifficulty().getXpValue();
                }
            }

            int xIndex = numberOfDays - 1 - i;

            entries.add(new Entry(xIndex, (float) totalXPSum));
        }

        return entries;
    }
}