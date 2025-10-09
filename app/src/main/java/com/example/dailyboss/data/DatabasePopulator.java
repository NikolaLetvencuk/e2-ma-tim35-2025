package com.example.dailyboss.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.UUID;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.Random;

// Uvozimo vaše postojeće enume
import com.example.dailyboss.domain.enums.FrequencyUnit;
import com.example.dailyboss.domain.enums.TaskDifficulty;
import com.example.dailyboss.domain.enums.TaskImportance;
import com.example.dailyboss.domain.enums.TaskStatus;

public class DatabasePopulator {

    private final DatabaseHelper dbHelper;
    private final Random random = new Random();

    public DatabasePopulator(Context context) {
        dbHelper = new DatabaseHelper(context);
    }
    public static final int DATABASE_VERSION = 13;

    public void populateDatabase() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        Log.d("TAG", "populateDatabase: ");
        try {

            // 1. Popunjavanje USERS tabele
            String userId1 = UUID.randomUUID().toString();
            String userId2 = UUID.randomUUID().toString();

            insertUser(db, userId1, "john_doe", "john.doe@example.com", "password123", "avatar_john.png", true, System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30));
            insertUser(db, userId2, "jane_smith", "jane.smith@example.com", "securepass", "avatar_jane.png", true, System.currentTimeMillis() - TimeUnit.DAYS.toMillis(15));
            insertUser(db, UUID.randomUUID().toString(), "guest_user", "guest@example.com", "guestpass", null, false, System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5));

            // 2. Popunjavanje CATEGORIES tabele
            String categoryId1 = UUID.randomUUID().toString(); // Posao
            String categoryId2 = UUID.randomUUID().toString(); // Fitnes
            String categoryId3 = UUID.randomUUID().toString(); // Hobiji
            String categoryId4 = UUID.randomUUID().toString(); // Učenje

            insertCategory(db, categoryId1, "Posao", "#FF6F61", userId1);
            insertCategory(db, categoryId2, "Fitnes", "#6B5B95", userId1);
            insertCategory(db, categoryId3, "Hobiji", "#88B04B", userId2);
            insertCategory(db, categoryId4, "Učenje", "#F7CAC9", userId2);
            insertCategory(db, UUID.randomUUID().toString(), "Kućni Poslovi", "#92A8D1", userId1);


            // 3. Popunjavanje TASK_TEMPLATES tabele
            String templateId1 = UUID.randomUUID().toString();
            String templateId2 = UUID.randomUUID().toString();
            String templateId3 = UUID.randomUUID().toString();
            String templateId4 = UUID.randomUUID().toString();
            String templateId5 = UUID.randomUUID().toString();


            insertTaskTemplate(db, templateId1, categoryId1, userId1, "Završi projekat X", "Kompletan projekat DailyBoss", "10:00", 7, FrequencyUnit.DAY, createDate(2023, 0, 1), 0, TaskDifficulty.HARD, TaskImportance.EXTREMELY_IMPORTANT, true);
            insertTaskTemplate(db, templateId2, categoryId2, userId1, "Vežbaj 30 minuta", "Jutarnje vežbe snage", "07:00", 1, FrequencyUnit.DAY, createDate(2023, 1, 1), 0, TaskDifficulty.EASY, TaskImportance.IMPORTANT, true);
            insertTaskTemplate(db, templateId3, categoryId3, userId2, "Čitaj knjigu", "Pročitaj poglavlje knjige 'Zelena milja'", "21:00", 3, FrequencyUnit.WEEK, createDate(2023, 0, 10), 0, TaskDifficulty.VERY_EASY, TaskImportance.NORMAL, true);
            insertTaskTemplate(db, templateId4, categoryId4, userId2, "Uči Java", "Završi online kurs Java programiranja", "18:00", 5, FrequencyUnit.DAY, createDate(2023, 2, 1), createDate(2023, 4, 30), TaskDifficulty.EXTREME, TaskImportance.SPECIAL, true);
            insertTaskTemplate(db, templateId5, categoryId1, userId1, "Sastanak sa timom", "Dnevni stand-up sastanak", "09:00", 1, FrequencyUnit.DAY, createDate(2023, 3, 1), 0, TaskDifficulty.EASY, TaskImportance.NORMAL, true);


            // 4. Popunjavanje TASK_INSTANCES tabele
            long today = System.currentTimeMillis();
            long yesterday = today - TimeUnit.DAYS.toMillis(1);
            long twoDaysAgo = today - TimeUnit.DAYS.toMillis(2);

            insertTaskInstance(db, UUID.randomUUID().toString(), today, TaskStatus.ACTIVE, templateId1, userId1);
            insertTaskInstance(db, UUID.randomUUID().toString(), today, TaskStatus.UNDONE, templateId2, userId1);
            insertTaskInstance(db, UUID.randomUUID().toString(), yesterday, TaskStatus.DONE, templateId1, userId1);
            insertTaskInstance(db, UUID.randomUUID().toString(), yesterday, TaskStatus.CANCELLED, templateId2, userId1);
            insertTaskInstance(db, UUID.randomUUID().toString(), twoDaysAgo, TaskStatus.DONE, templateId1, userId1);
            insertTaskInstance(db, UUID.randomUUID().toString(), twoDaysAgo, TaskStatus.DONE, templateId2, userId1);
            insertTaskInstance(db, UUID.randomUUID().toString(), today, TaskStatus.ACTIVE, templateId3, userId2);
            insertTaskInstance(db, UUID.randomUUID().toString(), today, TaskStatus.PAUSED, templateId4, userId2);


            // 5. Popunjavanje USER_PROFILE tabele
            String userProfileId1 = UUID.randomUUID().toString();
            String userProfileId2 = UUID.randomUUID().toString();

            insertUserProfile(db, userProfileId1, userId1, 10, 2, 5, 1200, 3, System.currentTimeMillis());
            insertUserProfile(db, userProfileId2, userId2, 7, 1, 3, 800, 2, System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5));

            // 6. Popunjavanje BADGES tabele
            String badgeId1 = UUID.randomUUID().toString(); // Prvi uspeh
            String badgeId2 = UUID.randomUUID().toString(); // Serijski zadaci
            String badgeId3 = UUID.randomUUID().toString(); // Majstor kategorije

            insertBadge(db, badgeId1, "Prvi koraci", "Završi svoj prvi zadatak.", "badge_first_task.png", 1);
            insertBadge(db, badgeId2, "Dnevni Heroj", "Završi 7 zadataka za redom.", "badge_daily_hero.png", 7);
            insertBadge(db, badgeId3, "Profesor", "Završi 10 zadataka u kategoriji 'Učenje'.", "badge_professor.png", 10);

            // 7. Popunjavanje USER_BADGES tabele
            insertUserBadge(db, UUID.randomUUID().toString(), userId1, badgeId1, System.currentTimeMillis() - TimeUnit.DAYS.toMillis(25));
            insertUserBadge(db, UUID.randomUUID().toString(), userId1, badgeId2, System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10));
            insertUserBadge(db, UUID.randomUUID().toString(), userId2, badgeId1, System.currentTimeMillis() - TimeUnit.DAYS.toMillis(12));

            // 8. Popunjavanje EQUIPMENT tabele
            String equipmentId1 = UUID.randomUUID().toString(); // Eliksir
            String equipmentId2 = UUID.randomUUID().toString(); // Štit
            String equipmentId3 = UUID.randomUUID().toString(); // Mač

            insertEquipment(db, equipmentId1, "Eliksir Motivacije", "Povećava XP poene za 20% na 3 dana.", "elixir_icon.png", "POTION", "XP_BONUS", 0.20, 0, 3, 500, true, false);
            insertEquipment(db, equipmentId2, "Štit Otpornosti", "Smanjuje šansu za neuspeh zadatka za 5% u 10 borbi.", "shield_icon.png", "ARMOR", "FAILURE_REDUCTION", 0.05, 10, 0, 1200, false, false);
            insertEquipment(db, equipmentId3, "Mač Fokusa", "Povećava efikasnost zadatka za 10% trajno.", "sword_icon.png", "WEAPON", "EFFICIENCY_BONUS", 0.10, 0, 0, 2000, false, false);


            // 9. Popunjavanje USER_EQUIPMENT tabele
            insertUserEquipment(db, UUID.randomUUID().toString(), userId1, equipmentId1, 2, false, 0, 0, 0.0); // John ima 2 eliksira, neaktivna
            insertUserEquipment(db, UUID.randomUUID().toString(), userId1, equipmentId2, 1, true, 7, System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1), 0.05); // John koristi štit
            insertUserEquipment(db, UUID.randomUUID().toString(), userId2, equipmentId3, 1, true, 0, 0, 0.10); // Jane koristi mač


            // 10. Popunjavanje USER_STATISTICS tabele
            String userStatId1 = UUID.randomUUID().toString();
            String userStatId2 = UUID.randomUUID().toString();

            insertUserStatistic(db, userStatId1, userId1, 25, 30, 15, 5, 7, 3, 2, 1, 6, System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1), 50, 2.4);
            insertUserStatistic(db, userStatId2, userId2, 10, 12, 7, 2, 4, 1, 1, 1, 2, System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1), 60, 15.0);

            // 11. Popunjavanje USER_CATEGORY_STATISTICS tabele
            insertUserCategoryStatistic(db, UUID.randomUUID().toString(), userStatId1, categoryId1, 8, "Posao");
            insertUserCategoryStatistic(db, UUID.randomUUID().toString(), userStatId1, categoryId2, 5, "Fitnes");
            insertUserCategoryStatistic(db, UUID.randomUUID().toString(), userStatId2, categoryId3, 3, "Hobiji");
            insertUserCategoryStatistic(db, UUID.randomUUID().toString(), userStatId2, categoryId4, 4, "Učenje");


            db.setTransactionSuccessful();
            System.out.println("Baza podataka uspešno popunjena primerima podataka.");
            // Log.d("DatabasePopulator", "Baza podataka uspešno popunjena primerima podataka.");
        } catch (Exception e) {
            System.err.println("Greška prilikom popunjavanja baze podataka: " + e.getMessage());
            // Log.e("DatabasePopulator", "Greška prilikom popunjavanja baze podataka", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private void insertUser(SQLiteDatabase db, String id, String username, String email, String password, String avatar, boolean isActive, long registrationTimestamp) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_ID, id);
        values.put(DatabaseHelper.COL_USER_USERNAME, username);
        values.put(DatabaseHelper.COL_USER_EMAIL, email);
        values.put(DatabaseHelper.COL_USER_PASSWORD, password);
        values.put(DatabaseHelper.COL_USER_AVATAR, avatar);
        values.put(DatabaseHelper.COL_USER_IS_ACTIVE, isActive ? 1 : 0);
        values.put(DatabaseHelper.COL_USER_REG_TIMESTAMP, registrationTimestamp);
        db.insert(DatabaseHelper.TABLE_USERS, null, values);
    }

    private void insertCategory(SQLiteDatabase db, String id, String name, String color, String userId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ID, id);
        values.put(DatabaseHelper.COL_NAME, name);
        values.put(DatabaseHelper.COL_COLOR, color);
        values.put(DatabaseHelper.COL_CATEGORY_USER_ID, userId);
        db.insert(DatabaseHelper.TABLE_CATEGORIES, null, values);
    }

    private void insertTaskTemplate(SQLiteDatabase db, String templateId, String categoryId, String userId, String name, String description,
                                    String executionTime, int frequencyInterval, FrequencyUnit frequencyUnit, long startDate, long endDate,
                                    TaskDifficulty difficulty, TaskImportance importance, boolean isRecurring) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_TEMPLATE_ID, templateId);
        values.put(DatabaseHelper.COL_TEMPLATE_CATEGORY_ID, categoryId);
        values.put(DatabaseHelper.COL_TEMPLATE_USER_ID, userId);
        values.put(DatabaseHelper.COL_TEMPLATE_NAME, name);
        values.put(DatabaseHelper.COL_TEMPLATE_DESCRIPTION, description);
        values.put(DatabaseHelper.COL_TEMPLATE_EXECUTION_TIME, executionTime);
        values.put(DatabaseHelper.COL_TEMPLATE_FREQUENCY_INTERVAL, frequencyInterval);
        values.put(DatabaseHelper.COL_TEMPLATE_FREQUENCY_UNIT, frequencyUnit.name()); // Pohranjujemo kao String
        values.put(DatabaseHelper.COL_TEMPLATE_START_DATE, startDate);
        values.put(DatabaseHelper.COL_TEMPLATE_END_DATE, endDate);
        values.put(DatabaseHelper.COL_TEMPLATE_DIFFICULTY, difficulty.name()); // Pohranjujemo kao String
        values.put(DatabaseHelper.COL_TEMPLATE_IMPORTANCE, importance.name()); // Pohranjujemo kao String
        values.put(DatabaseHelper.COL_TEMPLATE_IS_RECURRING, isRecurring ? 1 : 0);
        db.insert(DatabaseHelper.TABLE_TASK_TEMPLATES, null, values);
    }

    private void insertTaskInstance(SQLiteDatabase db, String instanceId, long instanceDate, TaskStatus status, String templateId, String userId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_INSTANCE_ID, instanceId);
        values.put(DatabaseHelper.COL_INSTANCE_DATE, instanceDate);
        values.put(DatabaseHelper.COL_INSTANCE_STATUS, status.name()); // Pohranjujemo kao String
        values.put(DatabaseHelper.COL_INSTANCE_TEMPLATE_ID, templateId);
        values.put(DatabaseHelper.COL_INSTANCE_USER_ID, userId);
        db.insert(DatabaseHelper.TABLE_TASK_INSTANCES, null, values);
    }

    private void insertUserProfile(SQLiteDatabase db, String id, String userId, int completedTasks, int failedTasks,
                                   int level, int experiencePoints, int winStreak, long lastActiveTimestamp) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_PROFILE_ID, id);
        values.put(DatabaseHelper.COL_PROFILE_USER_ID, userId);
        values.put(DatabaseHelper.COL_PROFILE_COMPLETED_TASKS, completedTasks);
        values.put(DatabaseHelper.COL_PROFILE_FAILED_TASKS, failedTasks);
        values.put(DatabaseHelper.COL_PROFILE_LEVEL, level);
        values.put(DatabaseHelper.COL_PROFILE_EXP_POINTS, experiencePoints);
        values.put(DatabaseHelper.COL_PROFILE_WIN_STREAK, winStreak);
        values.put(DatabaseHelper.COL_PROFILE_LAST_ACTIVE, lastActiveTimestamp);
        db.insert(DatabaseHelper.TABLE_USER_PROFILE, null, values);
    }

    private void insertBadge(SQLiteDatabase db, String id, String name, String description, String iconPath, int requiredCompletions) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_BADGE_ID, id);
        values.put(DatabaseHelper.COL_BADGE_NAME, name);
        values.put(DatabaseHelper.COL_BADGE_DESCRIPTION, description);
        values.put(DatabaseHelper.COL_BADGE_ICON_PATH, iconPath);
        values.put(DatabaseHelper.COL_BADGE_REQUIRED_COMPLETIONS, requiredCompletions);
        db.insert(DatabaseHelper.TABLE_BADGES, null, values);
    }

    private void insertUserBadge(SQLiteDatabase db, String id, String userId, String badgeId, long dateAchieved) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_BADGE_ID, id);
        values.put(DatabaseHelper.COL_USER_BADGE_USER_ID, userId);
        values.put(DatabaseHelper.COL_USER_BADGE_BADGE_ID, badgeId);
        values.put(DatabaseHelper.COL_USER_BADGE_DATE_ACHIEVED, dateAchieved);
        db.insert(DatabaseHelper.TABLE_USER_BADGES, null, values);
    }

    private void insertEquipment(SQLiteDatabase db, String id, String name, String description, String iconPath,
                                 String type, String bonusType, double bonusValue, int durationBattles,
                                 int durationDays, int basePriceCoins, boolean isConsumable, boolean isStackable) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_EQUIPMENT_ID, id);
        values.put(DatabaseHelper.COL_EQUIPMENT_NAME, name);
        values.put(DatabaseHelper.COL_EQUIPMENT_DESCRIPTION, description);
        values.put(DatabaseHelper.COL_EQUIPMENT_ICON_PATH, iconPath);
        values.put(DatabaseHelper.COL_EQUIPMENT_TYPE, type);
        values.put(DatabaseHelper.COL_EQUIPMENT_BONUS_TYPE, bonusType);
        values.put(DatabaseHelper.COL_EQUIPMENT_BONUS_VALUE, bonusValue);
        values.put(DatabaseHelper.COL_EQUIPMENT_DURATION_BATTLES, durationBattles);
        values.put(DatabaseHelper.COL_EQUIPMENT_DURATION_DAYS, durationDays);
        values.put(DatabaseHelper.COL_EQUIPMENT_BASE_PRICE_COINS, basePriceCoins);
        values.put(DatabaseHelper.COL_EQUIPMENT_IS_CONSUMABLE, isConsumable ? 1 : 0);
        values.put(DatabaseHelper.COL_EQUIPMENT_IS_STACKABLE, isStackable ? 1 : 0);
        db.insert(DatabaseHelper.TABLE_EQUIPMENT, null, values);
    }

    private void insertUserEquipment(SQLiteDatabase db, String id, String userId, String equipmentId, int quantity,
                                     boolean isActive, int remainingDurationBattles, long activationTimestamp, double currentBonusValue) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_EQUIPMENT_ID, id);
        values.put(DatabaseHelper.COL_USER_EQUIPMENT_USER_ID, userId);
        values.put(DatabaseHelper.COL_USER_EQUIPMENT_EQUIPMENT_ID, equipmentId);
        values.put(DatabaseHelper.COL_USER_EQUIPMENT_QUANTITY, quantity);
        values.put(DatabaseHelper.COL_USER_EQUIPMENT_IS_ACTIVE, isActive ? 1 : 0);
        values.put(DatabaseHelper.COL_USER_EQUIPMENT_REMAINING_DURATION_BATTLES, remainingDurationBattles);
        values.put(DatabaseHelper.COL_USER_EQUIPMENT_ACTIVATION_TIMESTAMP, activationTimestamp);
        values.put(DatabaseHelper.COL_USER_EQUIPMENT_CURRENT_BONUS_VALUE, currentBonusValue);
        db.insert(DatabaseHelper.TABLE_USER_EQUIPMENT, null, values);
    }

    private void insertUserStatistic(SQLiteDatabase db, String id, String userId, int activeDaysCount, int totalCreatedTasks,
                                     int totalCompletedTasks, int totalFailedTasks, int totalCancelledTasks,
                                     int longestTaskStreak, int currentTaskStreak, int totalSpecialMissionsStarted,
                                     int totalSpecialMissionsCompleted, long lastStreakUpdateTimestamp,
                                     int totalExpPoints, double averageExpEarned) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_STAT_ID, id);
        values.put(DatabaseHelper.COL_STAT_USER_ID, userId);
        values.put(DatabaseHelper.COL_STAT_ACTIVE_DAYS_COUNT, activeDaysCount);
        values.put(DatabaseHelper.COL_STAT_TOTAL_CREATED_TASKS, totalCreatedTasks);
        values.put(DatabaseHelper.COL_STAT_TOTAL_COMPLETED_TASKS, totalCompletedTasks);
        values.put(DatabaseHelper.COL_STAT_TOTAL_FAILED_TASKS, totalFailedTasks);
        values.put(DatabaseHelper.COL_STAT_TOTAL_CANCELLED_TASKS, totalCancelledTasks);
        values.put(DatabaseHelper.COL_STAT_LONGEST_TASK_STREAK, longestTaskStreak);
        values.put(DatabaseHelper.COL_STAT_CURRENT_TASK_STREAK, currentTaskStreak);
        values.put(DatabaseHelper.COL_STAT_TOTAL_SPECIAL_MISSIONS_STARTED, totalSpecialMissionsStarted);
        values.put(DatabaseHelper.COL_STAT_TOTAL_SPECIAL_MISSIONS_COMPLETED, totalSpecialMissionsCompleted);
        values.put(DatabaseHelper.COL_STAT_LAST_STREAK_UPDATE_TIMESTAMP, lastStreakUpdateTimestamp);
        values.put(DatabaseHelper.COL_STAT_TOTAL_EXP_POINTS, totalExpPoints);
        values.put(DatabaseHelper.COL_STAT_AVERAGE_EXP_EARNED, averageExpEarned);
        db.insert(DatabaseHelper.TABLE_USER_STATISTICS, null, values);
    }

    private void insertUserCategoryStatistic(SQLiteDatabase db, String id, String userStatisticId, String categoryId, int completedCount, String categoryName) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_U_CAT_STAT_ID, id);
        values.put(DatabaseHelper.COL_U_CAT_STAT_USER_STAT_ID, userStatisticId);
        values.put(DatabaseHelper.COL_U_CAT_STAT_CATEGORY_ID, categoryId);
        values.put(DatabaseHelper.COL_U_CAT_STAT_COMPLETED_COUNT, completedCount);
        values.put(DatabaseHelper.COL_U_CAT_STAT_CATEGORY_NAME, categoryName);
        db.insert(DatabaseHelper.TABLE_USER_CATEGORY_STATISTICS, null, values);
    }

    /**
     * Helper metoda za kreiranje vremenske oznake za određeni datum.
     * Meseci su 0-bazirani (januar je 0).
     */
    private long createDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0); // Ponoć tog dana
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}