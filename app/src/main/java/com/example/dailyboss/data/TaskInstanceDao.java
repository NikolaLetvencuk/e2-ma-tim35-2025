package com.example.dailyboss.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.dailyboss.enums.TaskDifficulty;
import com.example.dailyboss.enums.TaskImportance;
import com.example.dailyboss.enums.TaskStatus;
import com.example.dailyboss.model.TaskInstance;

import java.util.ArrayList;
import java.util.List;

public class TaskInstanceDao {

    private final DatabaseHelper dbHelper;

    public TaskInstanceDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public boolean insert(TaskInstance taskInstance) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_INSTANCE_ID, taskInstance.getInstanceId());
        values.put(DatabaseHelper.COL_INSTANCE_TASK_ID, taskInstance.getTaskId());
        values.put(DatabaseHelper.COL_INSTANCE_DATE, taskInstance.getInstanceDate());
        values.put(DatabaseHelper.COL_INSTANCE_STATUS, taskInstance.getStatus().name());
        values.put(DatabaseHelper.COL_INSTANCE_TEMPLATE_ID, taskInstance.getTemplateId());
        values.put(DatabaseHelper.COL_INSTANCE_CATEGORY_ID, taskInstance.getCategoryId());
        values.put(DatabaseHelper.COL_INSTANCE_NAME, taskInstance.getName());
        values.put(DatabaseHelper.COL_INSTANCE_DESCRIPTION, taskInstance.getDescription());
        values.put(DatabaseHelper.COL_INSTANCE_DIFFICULTY, taskInstance.getDifficulty().name());
        values.put(DatabaseHelper.COL_INSTANCE_IMPORTANCE, taskInstance.getImportance().name());
        values.put(DatabaseHelper.COL_INSTANCE_XP_VALUE, taskInstance.getXpValue());

        long result = db.insert(DatabaseHelper.TABLE_TASK_INSTANCES, null, values);
        db.close();
        return result != -1;
    }

    public List<TaskInstance> getAll() {
        List<TaskInstance> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_TASK_INSTANCES,
                null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String instanceId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_ID));
                String taskId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_TASK_ID));
                long instanceDate = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_DATE));
                TaskStatus status = TaskStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_STATUS)));
                String templateId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_TEMPLATE_ID));
                String categoryId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_CATEGORY_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_DESCRIPTION));
                TaskDifficulty difficulty = TaskDifficulty.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_DIFFICULTY)));
                TaskImportance importance = TaskImportance.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_IMPORTANCE)));
                int xpValue = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_XP_VALUE));

                list.add(new TaskInstance(instanceId, taskId, instanceDate, status, templateId, categoryId, name, description, difficulty, importance, xpValue));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public boolean update(TaskInstance taskInstance) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_INSTANCE_TASK_ID, taskInstance.getTaskId());
        values.put(DatabaseHelper.COL_INSTANCE_DATE, taskInstance.getInstanceDate());
        values.put(DatabaseHelper.COL_INSTANCE_STATUS, taskInstance.getStatus().name());
        values.put(DatabaseHelper.COL_INSTANCE_TEMPLATE_ID, taskInstance.getTemplateId());
        values.put(DatabaseHelper.COL_INSTANCE_CATEGORY_ID, taskInstance.getCategoryId());
        values.put(DatabaseHelper.COL_INSTANCE_NAME, taskInstance.getName());
        values.put(DatabaseHelper.COL_INSTANCE_DESCRIPTION, taskInstance.getDescription());
        values.put(DatabaseHelper.COL_INSTANCE_DIFFICULTY, taskInstance.getDifficulty().name());
        values.put(DatabaseHelper.COL_INSTANCE_IMPORTANCE, taskInstance.getImportance().name());
        values.put(DatabaseHelper.COL_INSTANCE_XP_VALUE, taskInstance.getXpValue());

        int updated = db.update(DatabaseHelper.TABLE_TASK_INSTANCES, values,
                DatabaseHelper.COL_INSTANCE_ID + " = ?", new String[]{taskInstance.getInstanceId()});
        db.close();
        return updated > 0;
    }

    public boolean deleteById(String instanceId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deleted = db.delete(DatabaseHelper.TABLE_TASK_INSTANCES,
                DatabaseHelper.COL_INSTANCE_ID + " = ?", new String[]{instanceId});
        db.close();
        return deleted > 0;
    }
}