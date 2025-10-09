package com.example.dailyboss.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "dailyboss.db";
    private static final int DATABASE_VERSION = 18;

    public static final String TABLE_CATEGORIES = "categories";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_COLOR = "color";
    public static final String COL_CATEGORY_USER_ID = "userId";
    public static final String TABLE_TASK_TEMPLATES = "task_templates";
    public static final String COL_TEMPLATE_ID = "templateId";
    public static final String COL_TEMPLATE_CATEGORY_ID = "categoryId";
    public static final String COL_TEMPLATE_USER_ID = "userId";
    public static final String COL_TEMPLATE_NAME = "name";
    public static final String COL_TEMPLATE_DESCRIPTION = "description";
    public static final String COL_TEMPLATE_EXECUTION_TIME = "executionTime";
    public static final String COL_TEMPLATE_FREQUENCY_INTERVAL = "frequencyInterval";
    public static final String COL_TEMPLATE_FREQUENCY_UNIT = "frequencyUnit";
    public static final String COL_TEMPLATE_START_DATE = "startDate";
    public static final String COL_TEMPLATE_END_DATE = "endDate";
    public static final String COL_TEMPLATE_DIFFICULTY = "difficulty";
    public static final String COL_TEMPLATE_IMPORTANCE = "importance";
    public static final String COL_TEMPLATE_IS_RECURRING = "isRecurring";

    public static final String TABLE_TASK_INSTANCES = "task_instances";
    public static final String COL_INSTANCE_ID = "instanceId";
    public static final String COL_INSTANCE_DATE = "instanceDate";
    public static final String COL_INSTANCE_STATUS = "status";
    public static final String COL_INSTANCE_TEMPLATE_ID = "templateId";
    public static final String COL_INSTANCE_USER_ID = "userId";

    private static final String CREATE_TABLE_CATEGORIES =
            "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                    COL_ID + " TEXT PRIMARY KEY," +
                    COL_NAME + " TEXT NOT NULL," +
                    COL_COLOR + " TEXT NOT NULL, " +
                    COL_CATEGORY_USER_ID + " TEXT NOT NULL " +
                    ");";

    private static final String CREATE_TABLE_TASK_TEMPLATES =
            "CREATE TABLE " + TABLE_TASK_TEMPLATES + " (" +
                    COL_TEMPLATE_ID + " TEXT PRIMARY KEY," +
                    COL_TEMPLATE_CATEGORY_ID + " TEXT," +
                    COL_TEMPLATE_USER_ID + " TEXT, " +
                    COL_TEMPLATE_NAME + " TEXT NOT NULL," +
                    COL_TEMPLATE_DESCRIPTION + " TEXT," +
                    COL_TEMPLATE_EXECUTION_TIME + " TEXT," +
                    COL_TEMPLATE_FREQUENCY_INTERVAL + " INTEGER," +
                    COL_TEMPLATE_FREQUENCY_UNIT + " TEXT," +
                    COL_TEMPLATE_START_DATE + " INTEGER NOT NULL," +
                    COL_TEMPLATE_END_DATE + " INTEGER," +
                    COL_TEMPLATE_DIFFICULTY + " TEXT," +
                    COL_TEMPLATE_IMPORTANCE + " TEXT," +
                    COL_TEMPLATE_IS_RECURRING + " INTEGER NOT NULL" +
                    ");";

    private static final String CREATE_TABLE_TASK_INSTANCES =
            "CREATE TABLE " + TABLE_TASK_INSTANCES + " (" +
                    COL_INSTANCE_ID + " TEXT PRIMARY KEY," +
                    COL_INSTANCE_DATE + " INTEGER NOT NULL," +
                    COL_INSTANCE_STATUS + " TEXT," +
                    COL_INSTANCE_TEMPLATE_ID + " TEXT," +
                    COL_INSTANCE_USER_ID + " TEXT " +
                    ");";

    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_USERNAME = "username";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PASSWORD = "password";
    public static final String COL_USER_AVATAR = "avatar";
    public static final String COL_USER_IS_ACTIVE = "isActive";
    public static final String COL_USER_REG_TIMESTAMP = "registrationTimestamp";
    public static final String COL_USER_ALLIANCE_ID = "allianceId";

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_USER_ID + " TEXT PRIMARY KEY," +
                    COL_USER_USERNAME + " TEXT NOT NULL," +
                    COL_USER_EMAIL + " TEXT NOT NULL," +
                    COL_USER_PASSWORD + " TEXT NOT NULL," +
                    COL_USER_AVATAR + " TEXT," +
                    COL_USER_IS_ACTIVE + " INTEGER NOT NULL," +
                    COL_USER_REG_TIMESTAMP + " INTEGER NOT NULL," +
                    COL_USER_ALLIANCE_ID + " TEXT" +
                    ");";

    public static final String TABLE_USER_PROFILE = "user_profile";

    public static final String COL_PROFILE_ID = "id";
    public static final String COL_PROFILE_USER_ID = "userId";
    public static final String COL_PROFILE_COMPLETED_TASKS = "completedTasks";
    public static final String COL_PROFILE_FAILED_TASKS = "failedTasks";
    public static final String COL_PROFILE_LEVEL = "level";
    public static final String COL_PROFILE_EXP_POINTS = "experiencePoints";
    public static final String COL_PROFILE_WIN_STREAK = "winStreak";
    public static final String COL_PROFILE_LAST_ACTIVE = "lastActiveTimestamp";

    private static final String CREATE_TABLE_USER_PROFILE =
            "CREATE TABLE " + TABLE_USER_PROFILE + " (" +
                    COL_PROFILE_ID + " TEXT PRIMARY KEY," +
                    COL_PROFILE_USER_ID + " TEXT NOT NULL," +
                    COL_PROFILE_COMPLETED_TASKS + " INTEGER DEFAULT 0," +
                    COL_PROFILE_FAILED_TASKS + " INTEGER DEFAULT 0," +
                    COL_PROFILE_LEVEL + " INTEGER DEFAULT 1," +
                    COL_PROFILE_EXP_POINTS + " INTEGER DEFAULT 0," +
                    COL_PROFILE_WIN_STREAK + " INTEGER DEFAULT 0," +
                    COL_PROFILE_LAST_ACTIVE + " INTEGER," +
                    "FOREIGN KEY(" + COL_PROFILE_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")" +
                    ");";


    public static final String TABLE_BADGES = "badges";
    public static final String COL_BADGE_ID = "id";
    public static final String COL_BADGE_NAME = "name";
    public static final String COL_BADGE_DESCRIPTION = "description";
    public static final String COL_BADGE_ICON_PATH = "iconPath";
    public static final String COL_BADGE_REQUIRED_COMPLETIONS = "requiredCompletions";

    private static final String CREATE_TABLE_BADGES =
            "CREATE TABLE " + TABLE_BADGES + " (" +
                    COL_BADGE_ID + " TEXT PRIMARY KEY," +
                    COL_BADGE_NAME + " TEXT NOT NULL," +
                    COL_BADGE_DESCRIPTION + " TEXT," +
                    COL_BADGE_ICON_PATH + " TEXT," +
                    COL_BADGE_REQUIRED_COMPLETIONS + " INTEGER NOT NULL" +
                    ");";

    public static final String TABLE_USER_BADGES = "user_badges";
    public static final String COL_USER_BADGE_ID = "id";
    public static final String COL_USER_BADGE_USER_ID = "userId";
    public static final String COL_USER_BADGE_BADGE_ID = "badgeId";
    public static final String COL_USER_BADGE_DATE_ACHIEVED = "dateAchieved";

    private static final String CREATE_TABLE_USER_BADGES =
            "CREATE TABLE " + TABLE_USER_BADGES + " (" +
                    COL_USER_BADGE_ID + " TEXT PRIMARY KEY," +
                    COL_USER_BADGE_USER_ID + " TEXT NOT NULL," +
                    COL_USER_BADGE_BADGE_ID + " TEXT NOT NULL," +
                    COL_USER_BADGE_DATE_ACHIEVED + " INTEGER NOT NULL," +
                    "FOREIGN KEY(" + COL_USER_BADGE_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")," +
                    "FOREIGN KEY(" + COL_USER_BADGE_BADGE_ID + ") REFERENCES " + TABLE_BADGES + "(" + COL_BADGE_ID + ")" +
                    ");";


    // Tabela za opremu
    public static final String TABLE_EQUIPMENT = "equipment";
    public static final String COL_EQUIPMENT_ID = "id";
    public static final String COL_EQUIPMENT_NAME = "name";
    public static final String COL_EQUIPMENT_DESCRIPTION = "description";
    public static final String COL_EQUIPMENT_ICON_PATH = "iconPath";
    public static final String COL_EQUIPMENT_TYPE = "type";
    public static final String COL_EQUIPMENT_BONUS_TYPE = "bonusType";
    public static final String COL_EQUIPMENT_BONUS_VALUE = "bonusValue";
    public static final String COL_EQUIPMENT_DURATION_BATTLES = "durationBattles";
    public static final String COL_EQUIPMENT_DURATION_DAYS = "durationDays";
    public static final String COL_EQUIPMENT_BASE_PRICE_COINS = "basePriceCoins";
    public static final String COL_EQUIPMENT_IS_CONSUMABLE = "isConsumable";
    public static final String COL_EQUIPMENT_IS_STACKABLE = "isStackable";

    private static final String CREATE_TABLE_EQUIPMENT =
            "CREATE TABLE " + TABLE_EQUIPMENT + " (" +
                    COL_EQUIPMENT_ID + " TEXT PRIMARY KEY," +
                    COL_EQUIPMENT_NAME + " TEXT NOT NULL," +
                    COL_EQUIPMENT_DESCRIPTION + " TEXT," +
                    COL_EQUIPMENT_ICON_PATH + " TEXT," +
                    COL_EQUIPMENT_TYPE + " TEXT," +
                    COL_EQUIPMENT_BONUS_TYPE + " TEXT," +
                    COL_EQUIPMENT_BONUS_VALUE + " REAL," +
                    COL_EQUIPMENT_DURATION_BATTLES + " INTEGER," +
                    COL_EQUIPMENT_DURATION_DAYS + " INTEGER," +
                    COL_EQUIPMENT_BASE_PRICE_COINS + " INTEGER," +
                    COL_EQUIPMENT_IS_CONSUMABLE + " INTEGER," +
                    COL_EQUIPMENT_IS_STACKABLE + " INTEGER" +
                    ");";

    // Tabela za korisničku opremu
    public static final String TABLE_USER_EQUIPMENT = "user_equipment";
    public static final String COL_USER_EQUIPMENT_ID = "id";
    public static final String COL_USER_EQUIPMENT_USER_ID = "userId";
    public static final String COL_USER_EQUIPMENT_EQUIPMENT_ID = "equipmentId";
    public static final String COL_USER_EQUIPMENT_QUANTITY = "quantity";
    public static final String COL_USER_EQUIPMENT_IS_ACTIVE = "isActive";
    public static final String COL_USER_EQUIPMENT_REMAINING_DURATION_BATTLES = "remainingDurationBattles";
    public static final String COL_USER_EQUIPMENT_ACTIVATION_TIMESTAMP = "activationTimestamp";
    public static final String COL_USER_EQUIPMENT_CURRENT_BONUS_VALUE = "currentBonusValue";

    private static final String CREATE_TABLE_USER_EQUIPMENT =
            "CREATE TABLE " + TABLE_USER_EQUIPMENT + " (" +
                    COL_USER_EQUIPMENT_ID + " TEXT PRIMARY KEY," +
                    COL_USER_EQUIPMENT_USER_ID + " TEXT NOT NULL," +
                    COL_USER_EQUIPMENT_EQUIPMENT_ID + " TEXT NOT NULL," +
                    COL_USER_EQUIPMENT_QUANTITY + " INTEGER," +
                    COL_USER_EQUIPMENT_IS_ACTIVE + " INTEGER," +
                    COL_USER_EQUIPMENT_REMAINING_DURATION_BATTLES + " INTEGER," +
                    COL_USER_EQUIPMENT_ACTIVATION_TIMESTAMP + " INTEGER," +
                    COL_USER_EQUIPMENT_CURRENT_BONUS_VALUE + " REAL," +
                    "FOREIGN KEY(" + COL_USER_EQUIPMENT_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")," +
                    "FOREIGN KEY(" + COL_USER_EQUIPMENT_EQUIPMENT_ID + ") REFERENCES " + TABLE_EQUIPMENT + "(" + COL_EQUIPMENT_ID + ")" +
                    ");";


    public static final String TABLE_USER_STATISTICS = "user_statistics";
    public static final String COL_STAT_ID = "id";
    public static final String COL_STAT_USER_ID = "user_id";
    public static final String COL_STAT_ACTIVE_DAYS_COUNT = "active_days_count";
    public static final String COL_STAT_TOTAL_CREATED_TASKS = "total_created_tasks";
    public static final String COL_STAT_TOTAL_COMPLETED_TASKS = "total_completed_tasks";
    public static final String COL_STAT_TOTAL_FAILED_TASKS = "total_failed_tasks";
    public static final String COL_STAT_TOTAL_CANCELLED_TASKS = "total_cancelled_tasks";
    public static final String COL_STAT_LONGEST_TASK_STREAK = "longest_task_streak";
    public static final String COL_STAT_CURRENT_TASK_STREAK = "current_task_streak";
    // public static final String COL_STAT_COMPLETED_TASKS_BY_CATEGORY = "completed_tasks_by_category"; // UKLONI OVO
    public static final String COL_STAT_TOTAL_SPECIAL_MISSIONS_STARTED = "total_special_missions_started";
    public static final String COL_STAT_TOTAL_SPECIAL_MISSIONS_COMPLETED = "total_special_missions_completed";
    public static final String COL_STAT_LAST_STREAK_UPDATE_TIMESTAMP = "last_streak_update_timestamp";
    public static final String COL_STAT_TOTAL_EXP_POINTS = "total_exp_points";
    public static final String COL_STAT_AVERAGE_EXP_EARNED = "average_exp_earned";

    public static final String COL_STAT_POWER_POINTS = "powerPoints";
    public static final String COL_STAT_COINS = "coins";
    public static final String COL_STAT_TITLE = "title";
    // SQL za kreiranje tabele user_statistics
    private static final String CREATE_TABLE_USER_STATISTICS =
            "CREATE TABLE " + TABLE_USER_STATISTICS + " (" +
                    COL_STAT_ID + " TEXT PRIMARY KEY," +
                    COL_STAT_USER_ID + " TEXT NOT NULL UNIQUE," +
                    COL_STAT_ACTIVE_DAYS_COUNT + " INTEGER DEFAULT 0," +
                    COL_STAT_TOTAL_CREATED_TASKS + " INTEGER DEFAULT 0," +
                    COL_STAT_TOTAL_COMPLETED_TASKS + " INTEGER DEFAULT 0," +
                    COL_STAT_TOTAL_FAILED_TASKS + " INTEGER DEFAULT 0," +
                    COL_STAT_TOTAL_CANCELLED_TASKS + " INTEGER DEFAULT 0," +
                    COL_STAT_LONGEST_TASK_STREAK + " INTEGER DEFAULT 0," +
                    COL_STAT_CURRENT_TASK_STREAK + " INTEGER DEFAULT 0," +
                    COL_STAT_TOTAL_SPECIAL_MISSIONS_STARTED + " INTEGER DEFAULT 0," +
                    COL_STAT_TOTAL_SPECIAL_MISSIONS_COMPLETED + " INTEGER DEFAULT 0," +
                    COL_STAT_LAST_STREAK_UPDATE_TIMESTAMP + " INTEGER DEFAULT 0," +
                    COL_STAT_TOTAL_EXP_POINTS + " INTEGER DEFAULT 0," +
                    COL_STAT_AVERAGE_EXP_EARNED + " INTEGER DEFAULT 0," +
                    COL_STAT_POWER_POINTS + " INTEGER DEFAULT 0," +
                    COL_STAT_COINS + " INTEGER DEFAULT 0," +
                    COL_STAT_TITLE + " TEXT," +

                    "FOREIGN KEY(" + COL_STAT_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE" +
                    ");";

    // --- Nova tabela za UserCategoryStatistics ---
    public static final String TABLE_USER_CATEGORY_STATISTICS = "user_category_statistics";
    public static final String COL_U_CAT_STAT_ID = "id"; // Može biti auto-generated
    public static final String COL_U_CAT_STAT_USER_STAT_ID = "user_statistic_id";
    public static final String COL_U_CAT_STAT_CATEGORY_ID = "category_id";
    public static final String COL_U_CAT_STAT_COMPLETED_COUNT = "completed_count";
    public static final String COL_U_CAT_STAT_CATEGORY_NAME = "category_name";

    private static final String CREATE_TABLE_USER_CATEGORY_STATISTICS =
            "CREATE TABLE " + TABLE_USER_CATEGORY_STATISTICS + " (" +
                    COL_U_CAT_STAT_ID + " TEXT PRIMARY KEY," + // UUID ili slično
                    COL_U_CAT_STAT_USER_STAT_ID + " TEXT NOT NULL," +
                    COL_U_CAT_STAT_CATEGORY_ID + " TEXT NOT NULL," +
                    COL_U_CAT_STAT_CATEGORY_NAME + " TEXT NOT NULL, " +
                    COL_U_CAT_STAT_COMPLETED_COUNT + " INTEGER DEFAULT 0," +
                    "FOREIGN KEY(" + COL_U_CAT_STAT_USER_STAT_ID + ") REFERENCES " + TABLE_USER_STATISTICS + "(" + COL_STAT_ID + ") ON DELETE CASCADE," +
                    "FOREIGN KEY(" + COL_U_CAT_STAT_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COL_ID + ") ON DELETE CASCADE," +
                    "UNIQUE(" + COL_U_CAT_STAT_USER_STAT_ID + ", " + COL_U_CAT_STAT_CATEGORY_ID + ")" + // Jedinstven par
                    ");";

    public static final String TABLE_FRIENDSHIPS = "friendships";
    public static final String COL_FRIENDSHIP_ID = "id";
    public static final String COL_FRIENDSHIP_SENDER_ID = "senderId";
    public static final String COL_FRIENDSHIP_RECEIVER_ID = "receiverId";
    public static final String COL_FRIENDSHIP_TIMESTAMP = "timestamp";
    public static final String COL_FRIENDSHIP_STATUS = "status";

    private static final String CREATE_TABLE_FRIENDSHIPS =
            "CREATE TABLE " + TABLE_FRIENDSHIPS + " (" +
                    COL_FRIENDSHIP_ID + " TEXT PRIMARY KEY," +
                    COL_FRIENDSHIP_SENDER_ID + " TEXT NOT NULL," +
                    COL_FRIENDSHIP_RECEIVER_ID + " TEXT NOT NULL," +
                    COL_FRIENDSHIP_TIMESTAMP + " INTEGER NOT NULL," +
                    COL_FRIENDSHIP_STATUS + " TEXT NOT NULL," +
                    "FOREIGN KEY(" + COL_FRIENDSHIP_SENDER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")," +
                    "FOREIGN KEY(" + COL_FRIENDSHIP_RECEIVER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")" +
                    ");";

    // --- NOVE TABELE ZA SAVEZ (Alliance) ---

    // Tabela za Savez (Alliance)
    public static final String TABLE_ALLIANCES = "alliances";
    public static final String COL_ALLIANCE_ID = "id";
    public static final String COL_ALLIANCE_NAME = "name";
    public static final String COL_ALLIANCE_LEADER_ID = "leaderId";
    public static final String COL_ALLIANCE_CREATED_AT = "createdAt";
    public static final String COL_ALLIANCE_MISSION_ACTIVE = "isMissionActive";
    public static final String COL_ALLIANCE_STATUS = "status";

    // Tabela za Pozive u Savez (AllianceInvitation)
    public static final String TABLE_ALLIANCE_INVITATIONS = "alliance_invitations";
    public static final String COL_INVITATION_ID = "id";
    public static final String COL_INVITATION_ALLIANCE_ID = "allianceId";
    public static final String COL_INVITATION_ALLIANCE_NAME = "allianceName";
    public static final String COL_INVITATION_SENDER_ID = "senderId";
    public static final String COL_INVITATION_RECEIVER_ID = "receiverId";
    public static final String COL_INVITATION_SENT_AT = "sentAt";
    public static final String COL_INVITATION_STATUS = "status";

    // Tabela za Poruke Saveza (AllianceMessage)
    public static final String TABLE_ALLIANCE_MESSAGES = "alliance_messages";
    public static final String COL_MESSAGE_ID = "id";
    public static final String COL_MESSAGE_ALLIANCE_ID = "allianceId";
    public static final String COL_MESSAGE_SENDER_ID = "senderId";
    public static final String COL_MESSAGE_SENDER_USERNAME = "sender_username";
    public static final String COL_MESSAGE_CONTENT = "content";
    public static final String COL_MESSAGE_TIMESTAMP = "timestamp";

    // SQL za kreiranje tabele ALLIANCES
    private static final String CREATE_TABLE_ALLIANCES =
            "CREATE TABLE " + TABLE_ALLIANCES + " (" +
                    COL_ALLIANCE_ID + " TEXT PRIMARY KEY," +
                    COL_ALLIANCE_NAME + " TEXT NOT NULL," +
                    COL_ALLIANCE_LEADER_ID + " TEXT NOT NULL," +
                    COL_ALLIANCE_CREATED_AT + " INTEGER NOT NULL," +
                    COL_ALLIANCE_MISSION_ACTIVE + " INTEGER NOT NULL," + // 0=false, 1=true
                    COL_ALLIANCE_STATUS + " TEXT NOT NULL," +
                    "FOREIGN KEY(" + COL_ALLIANCE_LEADER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE" +
                    ");";

    // SQL za kreiranje tabele ALLIANCE_INVITATIONS
    private static final String CREATE_TABLE_ALLIANCE_INVITATIONS =
            "CREATE TABLE " + TABLE_ALLIANCE_INVITATIONS + " (" +
                    COL_INVITATION_ID + " TEXT PRIMARY KEY," +
                    COL_INVITATION_ALLIANCE_ID + " TEXT NOT NULL," +
                    COL_INVITATION_ALLIANCE_NAME + " TEXT NOT NULL," +
                    COL_INVITATION_SENDER_ID + " TEXT NOT NULL," +
                    COL_INVITATION_RECEIVER_ID + " TEXT NOT NULL," +
                    COL_INVITATION_SENT_AT + " INTEGER NOT NULL," +
                    COL_INVITATION_STATUS + " TEXT NOT NULL," + // "Pending", "Accepted", "Rejected"
                    "FOREIGN KEY(" + COL_INVITATION_ALLIANCE_ID + ") REFERENCES " + TABLE_ALLIANCES + "(" + COL_ALLIANCE_ID + ") ON DELETE CASCADE," +
                    "FOREIGN KEY(" + COL_INVITATION_RECEIVER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE" +
                    ");";

    // SQL za kreiranje tabele ALLIANCE_MESSAGES
    private static final String CREATE_TABLE_ALLIANCE_MESSAGES =
            "CREATE TABLE " + TABLE_ALLIANCE_MESSAGES + " (" +
                    COL_MESSAGE_ID + " TEXT PRIMARY KEY," +
                    COL_MESSAGE_ALLIANCE_ID + " TEXT NOT NULL," +
                    COL_MESSAGE_SENDER_ID + " TEXT NOT NULL," +
                    COL_MESSAGE_SENDER_USERNAME + " TEXT NOT NULL," +
                    COL_MESSAGE_CONTENT + " TEXT NOT NULL," +
                    COL_MESSAGE_TIMESTAMP + " INTEGER NOT NULL," +
                    "FOREIGN KEY(" + COL_MESSAGE_ALLIANCE_ID + ") REFERENCES " + TABLE_ALLIANCES + "(" + COL_ALLIANCE_ID + ") ON DELETE CASCADE," +
                    "FOREIGN KEY(" + COL_MESSAGE_SENDER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_TASK_TEMPLATES);
        db.execSQL(CREATE_TABLE_TASK_INSTANCES);
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_USER_STATISTICS);
        db.execSQL(CREATE_TABLE_BADGES);
        db.execSQL(CREATE_TABLE_USER_BADGES);
        db.execSQL(CREATE_TABLE_EQUIPMENT);
        db.execSQL(CREATE_TABLE_USER_EQUIPMENT);
        db.execSQL(CREATE_TABLE_USER_PROFILE);
        db.execSQL(CREATE_TABLE_USER_CATEGORY_STATISTICS);
        db.execSQL(CREATE_TABLE_FRIENDSHIPS);
        db.execSQL(CREATE_TABLE_ALLIANCES);
        db.execSQL(CREATE_TABLE_ALLIANCE_INVITATIONS);
        db.execSQL(CREATE_TABLE_ALLIANCE_MESSAGES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Brisanje stare šeme
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK_TEMPLATES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK_INSTANCES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_STATISTICS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BADGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_BADGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EQUIPMENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_EQUIPMENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_PROFILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_CATEGORY_STATISTICS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDSHIPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALLIANCE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALLIANCE_INVITATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALLIANCES);

        onCreate(db);
    }
}