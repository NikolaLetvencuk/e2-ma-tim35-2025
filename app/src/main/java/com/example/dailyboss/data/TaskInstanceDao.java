package com.example.dailyboss.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
        values.put(DatabaseHelper.COL_INSTANCE_DATE, taskInstance.getInstanceDate());
        values.put(DatabaseHelper.COL_INSTANCE_STATUS, taskInstance.getStatus().name());
        values.put(DatabaseHelper.COL_INSTANCE_TEMPLATE_ID, taskInstance.getTemplateId());

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
                long instanceDate = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_DATE));
                TaskStatus status = TaskStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_STATUS)));
                String templateId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_TEMPLATE_ID));

                list.add(new TaskInstance(instanceId, instanceDate, status, templateId));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public boolean update(TaskInstance taskInstance) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_INSTANCE_DATE, taskInstance.getInstanceDate());
        values.put(DatabaseHelper.COL_INSTANCE_STATUS, taskInstance.getStatus().name());
        values.put(DatabaseHelper.COL_INSTANCE_TEMPLATE_ID, taskInstance.getTemplateId());

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

    public int getByTaskId(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_TASK_INSTANCES +
                " WHERE " + DatabaseHelper.COL_INSTANCE_TEMPLATE_ID + " = ?";

        String[] selectionArgs = { id };

        Cursor cursor = db.rawQuery(query, selectionArgs);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();

        return count;
    }

    public List<TaskInstance> getTasksByDateRange(long start, long end) {
        List<TaskInstance> taskInstances = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COL_INSTANCE_DATE + " BETWEEN ? AND ?";
        String[] selectionArgs = { String.valueOf(start), String.valueOf(end) };

        Cursor cursor = db.query(DatabaseHelper.TABLE_TASK_INSTANCES, null, selection, selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            taskInstances.add(new TaskInstance(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_DATE)),
                    TaskStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_STATUS))),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_TEMPLATE_ID))
            ));
        }

        cursor.close();
        db.close();
        return taskInstances;
    }

    public TaskInstance findTaskById(String instanceId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COL_INSTANCE_ID + " = ?";
        String[] selectionArgs = { instanceId };

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_TASK_INSTANCES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        TaskInstance taskInstance = null;

        if (cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_ID));
            long date = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_DATE));
            TaskStatus status = TaskStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_STATUS)));
            String templateId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_TEMPLATE_ID));

            taskInstance = new TaskInstance(id, date, status, templateId);
        }

        cursor.close();
        db.close();

        return taskInstance;
    }
}