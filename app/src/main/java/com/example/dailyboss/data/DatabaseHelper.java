package com.example.dailyboss.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "dailyboss.db";
    // 💥 AŽURIRANO: Povećajte verziju da bi se pozvao onUpgrade i kreirala tabela USERS
    private static final int DATABASE_VERSION = 7;

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

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_USER_ID + " TEXT PRIMARY KEY," +
                    COL_USER_USERNAME + " TEXT NOT NULL," +
                    COL_USER_EMAIL + " TEXT NOT NULL," +
                    COL_USER_PASSWORD + " TEXT NOT NULL," +
                    COL_USER_AVATAR + " TEXT," +
                    COL_USER_IS_ACTIVE + " INTEGER NOT NULL," +
                    COL_USER_REG_TIMESTAMP + " INTEGER NOT NULL" +
                    ");";

    public static final String TABLE_USER_STATISTICS = "user_statistics";

    public static final String COL_STAT_ID = "id";
    public static final String COL_STAT_USER_ID = "userId";
    public static final String COL_STAT_COMPLETED_TASKS = "completedTasks";
    public static final String COL_STAT_FAILED_TASKS = "failedTasks";
    public static final String COL_STAT_LEVEL = "level";
    public static final String COL_STAT_EXP_POINTS = "experiencePoints";
    public static final String COL_STAT_WIN_STREAK = "winStreak";
    public static final String COL_STAT_LAST_ACTIVE = "lastActiveTimestamp";

    private static final String CREATE_TABLE_USER_STATISTICS =
            "CREATE TABLE " + TABLE_USER_STATISTICS + " (" +
                    COL_STAT_ID + " TEXT PRIMARY KEY," +
                    COL_STAT_USER_ID + " TEXT NOT NULL," +
                    COL_STAT_COMPLETED_TASKS + " INTEGER DEFAULT 0," +
                    COL_STAT_FAILED_TASKS + " INTEGER DEFAULT 0," +
                    COL_STAT_LEVEL + " INTEGER DEFAULT 1," +
                    COL_STAT_EXP_POINTS + " INTEGER DEFAULT 0," +
                    COL_STAT_WIN_STREAK + " INTEGER DEFAULT 0," +
                    COL_STAT_LAST_ACTIVE + " INTEGER," +
                    "FOREIGN KEY(" + COL_STAT_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")" +
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

        // Kreiranje nove šeme koja sada uključuje USERS
        onCreate(db);
    }
}