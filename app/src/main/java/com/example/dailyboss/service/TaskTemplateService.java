package com.example.dailyboss.service;

import android.content.Context;

import com.example.dailyboss.data.TaskInstanceDao;
import com.example.dailyboss.data.TaskTemplateDao;
import com.example.dailyboss.enums.FrequencyUnit;
import com.example.dailyboss.enums.TaskDifficulty;
import com.example.dailyboss.enums.TaskImportance;
import com.example.dailyboss.enums.TaskStatus;
import com.example.dailyboss.model.TaskInstance;
import com.example.dailyboss.model.TaskTemplate;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TaskTemplateService {

    private final TaskTemplateDao taskTemplateDao;
    private final TaskInstanceDao taskInstanceDao;

    public TaskTemplateService(Context context) {
        this.taskTemplateDao = new TaskTemplateDao(context);
        this.taskInstanceDao = new TaskInstanceDao(context);
    }

    public boolean addTaskTemplate(String categoryId, String name, String description, String executionTime, int frequencyInterval, FrequencyUnit frequencyUnit, long startDate, Long endDate, TaskDifficulty difficulty, TaskImportance importance, boolean isRecurring) {
        String id = UUID.randomUUID().toString();
        TaskTemplate taskTemplate = new TaskTemplate(id, categoryId, name, description, executionTime, frequencyInterval, frequencyUnit, startDate, endDate, difficulty, importance, isRecurring);

        if (!isRecurring) {
            TaskInstance taskInstance = new TaskInstance(UUID.randomUUID().toString(), startDate, TaskStatus.TO_DO, id);
            taskInstanceDao.insert(taskInstance);
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(startDate);
            long date = calendar.getTimeInMillis();

            do {
                TaskInstance taskInstance = new TaskInstance(UUID.randomUUID().toString(), date, TaskStatus.TO_DO, id);
                taskInstanceDao.insert(taskInstance);

                switch (frequencyUnit) {
                    case DAY:
                        calendar.add(Calendar.DAY_OF_YEAR, frequencyInterval);
                        break;
                    case WEEK:
                        calendar.add(Calendar.WEEK_OF_YEAR, frequencyInterval);
                        break;
                }
                date = calendar.getTimeInMillis();
            } while (endDate == null || date <= endDate);
        }
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

    public Map<String, TaskTemplate> getTemplatesByIds(Set<String> ids) {
        return taskTemplateDao.getTaskTemplatesByIds(ids);
    }
}
