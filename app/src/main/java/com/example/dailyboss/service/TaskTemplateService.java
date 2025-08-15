package com.example.dailyboss.service;

import android.content.Context;

import com.example.dailyboss.data.TaskTemplateDao;
import com.example.dailyboss.enums.FrequencyUnit;
import com.example.dailyboss.enums.TaskDifficulty;
import com.example.dailyboss.enums.TaskImportance;
import com.example.dailyboss.model.TaskTemplate;

import java.util.List;
import java.util.UUID;

public class TaskTemplateService {

    private final TaskTemplateDao taskTemplateDao;

    public TaskTemplateService(Context context) {
        this.taskTemplateDao = new TaskTemplateDao(context);
    }

    public boolean addTaskTemplate(String categoryId, String name, String description, String executionTime, int frequencyInterval, FrequencyUnit frequencyUnit, long startDate, Long endDate, TaskDifficulty difficulty, TaskImportance importance, boolean isRecurring) {
        String id = UUID.randomUUID().toString();
        TaskTemplate taskTemplate = new TaskTemplate(id, categoryId, name, description, executionTime, frequencyInterval, frequencyUnit, startDate, endDate, difficulty, importance, isRecurring);
        return  taskTemplateDao.insert(taskTemplate);
    }

    public List<TaskTemplate> getAllTaskTemplates() {
        return taskTemplateDao.getAll();
    }

    private boolean deleteById(String id) {
        return taskTemplateDao.deleteById(id);
    }

    private boolean updateTask(TaskTemplate taskTemplate) {
        return taskTemplateDao.update(taskTemplate);
    }

    private List<TaskTemplate> getByCategoryId(String categoryId) {
        return taskTemplateDao.getByCategoryId(categoryId);
    }
}
