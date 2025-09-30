package com.example.dailyboss.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.dailyboss.dto.TaskDetailDto;
import com.example.dailyboss.enums.FrequencyUnit;
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

    public int deleteFutureInstancesFromDate(String templateId, long dateBoundary) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String whereClause = DatabaseHelper.COL_INSTANCE_TEMPLATE_ID + " = ? AND " +
                DatabaseHelper.COL_INSTANCE_DATE + " >= ? AND " +
                DatabaseHelper.COL_INSTANCE_STATUS + " != ?";

        String[] whereArgs = {
                templateId,
                String.valueOf(dateBoundary),
                TaskStatus.DONE.name()
        };

        int deletedRows = db.delete(DatabaseHelper.TABLE_TASK_INSTANCES, whereClause, whereArgs);
        db.close();

        return deletedRows;
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

    public TaskDetailDto findTaskDetailById(String instanceId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT ti.*, tt." +
                DatabaseHelper.COL_TEMPLATE_CATEGORY_ID + ", tt." +
                DatabaseHelper.COL_TEMPLATE_NAME + ", tt." +
                DatabaseHelper.COL_TEMPLATE_DESCRIPTION + ", tt." +
                DatabaseHelper.COL_TEMPLATE_EXECUTION_TIME + ", tt." +
                DatabaseHelper.COL_TEMPLATE_FREQUENCY_INTERVAL + ", tt." +
                DatabaseHelper.COL_TEMPLATE_FREQUENCY_UNIT + ", tt." +
                DatabaseHelper.COL_TEMPLATE_START_DATE + ", tt." +
                DatabaseHelper.COL_TEMPLATE_END_DATE + ", tt." +
                DatabaseHelper.COL_TEMPLATE_DIFFICULTY + ", tt." +
                DatabaseHelper.COL_TEMPLATE_IMPORTANCE + ", tt." +
                DatabaseHelper.COL_TEMPLATE_IS_RECURRING + ", " +
                "c." + DatabaseHelper.COL_NAME + " AS categoryName " +
                "FROM " + DatabaseHelper.TABLE_TASK_INSTANCES + " ti " +
                "JOIN " + DatabaseHelper.TABLE_TASK_TEMPLATES + " tt " +
                "ON ti." + DatabaseHelper.COL_INSTANCE_TEMPLATE_ID + " = tt." + DatabaseHelper.COL_TEMPLATE_ID + " " +
                "JOIN " + DatabaseHelper.TABLE_CATEGORIES + " c " +
                "ON tt." + DatabaseHelper.COL_TEMPLATE_CATEGORY_ID + " = c." + DatabaseHelper.COL_ID + " " +
                "WHERE ti." + DatabaseHelper.COL_INSTANCE_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{instanceId});

        TaskDetailDto taskDetail = null;
        if (cursor.moveToFirst()) {
            taskDetail = new TaskDetailDto(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_DATE)),
                    TaskStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_STATUS))),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_TEMPLATE_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_CATEGORY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow("categoryName")),  // ovo je novo
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_EXECUTION_TIME)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_FREQUENCY_INTERVAL)),
                    FrequencyUnit.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_FREQUENCY_UNIT))),
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_START_DATE)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_END_DATE)),
                    TaskDifficulty.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_DIFFICULTY))),
                    TaskImportance.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_IMPORTANCE))),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_IS_RECURRING)) == 1
            );
        }

        cursor.close();
        db.close();
        return taskDetail;
    }

    public boolean updateTaskStatus(String instanceId, TaskStatus newStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_INSTANCE_STATUS, newStatus.name());

        int updated = db.update(DatabaseHelper.TABLE_TASK_INSTANCES, values,
                DatabaseHelper.COL_INSTANCE_ID + " = ?", new String[]{instanceId});

        db.close();
        return updated > 0;
    }

    public List<TaskInstance> getFutureActiveInstances(String templateId, long dateBoundary) {
        List<TaskInstance> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM " + DatabaseHelper.TABLE_TASK_INSTANCES +
                " WHERE " + DatabaseHelper.COL_INSTANCE_TEMPLATE_ID + " = ? " +
                " AND " + DatabaseHelper.COL_INSTANCE_DATE + " >= ? " +
                " AND " + DatabaseHelper.COL_INSTANCE_STATUS + " != ?";

        String[] selectionArgs = {
                templateId,
                String.valueOf(dateBoundary),
                TaskStatus.DONE.name()
        };

        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor.moveToFirst()) {
            do {
                String instanceId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_ID));
                long instanceDate = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_DATE));
                TaskStatus status = TaskStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_STATUS)));
                String tempId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTANCE_TEMPLATE_ID));

                list.add(new TaskInstance(instanceId, instanceDate, status, tempId));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}