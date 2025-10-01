package com.example.dailyboss.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.dailyboss.data.DatabaseHelper;
import com.example.dailyboss.domain.enums.FrequencyUnit;
import com.example.dailyboss.domain.enums.TaskDifficulty;
import com.example.dailyboss.domain.enums.TaskImportance;
import com.example.dailyboss.domain.model.TaskTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TaskTemplateDao {

    private final DatabaseHelper dbHelper;

    public TaskTemplateDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public boolean insert(TaskTemplate taskTemplate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_TEMPLATE_ID, taskTemplate.getTemplateId());
        values.put(DatabaseHelper.COL_TEMPLATE_CATEGORY_ID, taskTemplate.getCategoryId());
        values.put(DatabaseHelper.COL_TEMPLATE_NAME, taskTemplate.getName());
        values.put(DatabaseHelper.COL_TEMPLATE_DESCRIPTION, taskTemplate.getDescription());
        values.put(DatabaseHelper.COL_TEMPLATE_EXECUTION_TIME, taskTemplate.getExecutionTime());
        values.put(DatabaseHelper.COL_TEMPLATE_FREQUENCY_INTERVAL, taskTemplate.getFrequencyInterval());
        values.put(DatabaseHelper.COL_TEMPLATE_FREQUENCY_UNIT, taskTemplate.getFrequencyUnit().name());
        values.put(DatabaseHelper.COL_TEMPLATE_START_DATE, taskTemplate.getStartDate());
        values.put(DatabaseHelper.COL_TEMPLATE_END_DATE, taskTemplate.getEndDate());
        values.put(DatabaseHelper.COL_TEMPLATE_DIFFICULTY, taskTemplate.getDifficulty().name());
        values.put(DatabaseHelper.COL_TEMPLATE_IMPORTANCE, taskTemplate.getImportance().name());
        values.put(DatabaseHelper.COL_TEMPLATE_IS_RECURRING, taskTemplate.isRecurring() ? 1 : 0);

        long result = db.insert(DatabaseHelper.TABLE_TASK_TEMPLATES, null, values);
        db.close();
        return result != -1;
    }

    public List<TaskTemplate> getAll() {
        List<TaskTemplate> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_TASK_TEMPLATES,
                null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String templateId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_ID));
                String categoryId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_CATEGORY_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_DESCRIPTION));
                String executionTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_EXECUTION_TIME));
                int frequencyInterval = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_FREQUENCY_INTERVAL));
                FrequencyUnit frequencyUnit = FrequencyUnit.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_FREQUENCY_UNIT)));
                long startDate = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_START_DATE));
                Long endDate = cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_END_DATE)) ? null : cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_END_DATE));
                TaskDifficulty difficulty = TaskDifficulty.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_DIFFICULTY)));
                TaskImportance importance = TaskImportance.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_IMPORTANCE)));
                boolean isRecurring = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_IS_RECURRING)) == 1;

                list.add(new TaskTemplate(templateId, categoryId, name, description, executionTime, frequencyInterval, frequencyUnit, startDate, endDate, difficulty, importance, isRecurring));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public boolean update(TaskTemplate taskTemplate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_TEMPLATE_CATEGORY_ID, taskTemplate.getCategoryId());
        values.put(DatabaseHelper.COL_TEMPLATE_NAME, taskTemplate.getName());
        values.put(DatabaseHelper.COL_TEMPLATE_DESCRIPTION, taskTemplate.getDescription());
        values.put(DatabaseHelper.COL_TEMPLATE_EXECUTION_TIME, taskTemplate.getExecutionTime());
        values.put(DatabaseHelper.COL_TEMPLATE_FREQUENCY_INTERVAL, taskTemplate.getFrequencyInterval());
        values.put(DatabaseHelper.COL_TEMPLATE_FREQUENCY_UNIT, taskTemplate.getFrequencyUnit().name());
        values.put(DatabaseHelper.COL_TEMPLATE_START_DATE, taskTemplate.getStartDate());
        values.put(DatabaseHelper.COL_TEMPLATE_END_DATE, taskTemplate.getEndDate());
        values.put(DatabaseHelper.COL_TEMPLATE_DIFFICULTY, taskTemplate.getDifficulty().name());
        values.put(DatabaseHelper.COL_TEMPLATE_IMPORTANCE, taskTemplate.getImportance().name());
        values.put(DatabaseHelper.COL_TEMPLATE_IS_RECURRING, taskTemplate.isRecurring() ? 1 : 0);

        int updated = db.update(DatabaseHelper.TABLE_TASK_TEMPLATES, values,
                DatabaseHelper.COL_TEMPLATE_ID + " = ?", new String[]{taskTemplate.getTemplateId()});
        db.close();
        return updated > 0;
    }

    public boolean deleteById(String templateId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deleted = db.delete(DatabaseHelper.TABLE_TASK_TEMPLATES,
                DatabaseHelper.COL_TEMPLATE_ID + " = ?", new String[]{templateId});
        db.close();
        return deleted > 0;
    }

    public List<TaskTemplate> getByCategoryId(String id) {
        List<TaskTemplate> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COL_TEMPLATE_CATEGORY_ID + " = ?";
        String[] selectionArgs = { id };

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_TASK_TEMPLATES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            do {
                String templateId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_ID));
                String categoryId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_CATEGORY_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_DESCRIPTION));
                String executionTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_EXECUTION_TIME));
                int frequencyInterval = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_FREQUENCY_INTERVAL));
                FrequencyUnit frequencyUnit = FrequencyUnit.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_FREQUENCY_UNIT)));
                long startDate = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_START_DATE));
                Long endDate = cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_END_DATE)) ? null : cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_END_DATE));
                TaskDifficulty difficulty = TaskDifficulty.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_DIFFICULTY)));
                TaskImportance importance = TaskImportance.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_IMPORTANCE)));
                boolean isRecurring = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_IS_RECURRING)) == 1;

                list.add(new TaskTemplate(templateId, categoryId, name, description, executionTime, frequencyInterval, frequencyUnit, startDate, endDate, difficulty, importance, isRecurring));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    public Map<String, TaskTemplate> getTaskTemplatesByIds(Set<String> ids) {
        Map<String, TaskTemplate> result = new HashMap<>();

        if (ids == null || ids.isEmpty()) return result;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            placeholders.append("?");
            if (i < ids.size() - 1) placeholders.append(",");
        }

        String sql = "SELECT * FROM " + DatabaseHelper.TABLE_TASK_TEMPLATES +
                " WHERE " + DatabaseHelper.COL_TEMPLATE_ID + " IN (" + placeholders + ")";
        Cursor cursor = db.rawQuery(sql, ids.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                String templateId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_ID));
                String categoryId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_CATEGORY_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_DESCRIPTION));
                String executionTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_EXECUTION_TIME));
                int frequencyInterval = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_FREQUENCY_INTERVAL));
                FrequencyUnit frequencyUnit = FrequencyUnit.valueOf(
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_FREQUENCY_UNIT))
                );
                long startDate = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_START_DATE));
                Long endDate = cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_END_DATE))
                        ? null
                        : cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_END_DATE));
                TaskDifficulty difficulty = TaskDifficulty.valueOf(
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_DIFFICULTY))
                );
                TaskImportance importance = TaskImportance.valueOf(
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_IMPORTANCE))
                );
                boolean isRecurring = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_IS_RECURRING)) == 1;

                TaskTemplate template = new TaskTemplate(templateId, categoryId, name, description, executionTime, frequencyInterval, frequencyUnit, startDate, endDate, difficulty, importance, isRecurring);

                result.put(templateId, template);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return result;
    }
}