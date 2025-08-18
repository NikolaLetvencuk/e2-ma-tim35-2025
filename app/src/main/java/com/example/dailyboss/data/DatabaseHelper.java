package com.example.dailyboss.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "dailyboss.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_CATEGORIES = "categories";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_COLOR = "color";

    public static final String TABLE_TASK_TEMPLATES = "task_templates";
    public static final String COL_TEMPLATE_ID = "templateId";
    public static final String COL_TEMPLATE_CATEGORY_ID = "categoryId";
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

    private static final String CREATE_TABLE_CATEGORIES =
            "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                    COL_ID + " TEXT PRIMARY KEY," +
                    COL_NAME + " TEXT NOT NULL," +
                    COL_COLOR + " TEXT NOT NULL" +
                    ");";

    private static final String CREATE_TABLE_TASK_TEMPLATES =
            "CREATE TABLE " + TABLE_TASK_TEMPLATES + " (" +
                    COL_TEMPLATE_ID + " TEXT PRIMARY KEY," +
                    COL_TEMPLATE_CATEGORY_ID + " TEXT," +
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
                    COL_INSTANCE_TEMPLATE_ID + " TEXT" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_TASK_TEMPLATES);
        db.execSQL(CREATE_TABLE_TASK_INSTANCES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK_TEMPLATES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK_INSTANCES);
        onCreate(db);
    }
}