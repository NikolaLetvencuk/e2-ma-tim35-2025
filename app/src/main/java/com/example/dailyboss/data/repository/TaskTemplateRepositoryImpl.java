package com.example.dailyboss.data.repository;

import android.content.Context;

import com.example.dailyboss.data.dao.TaskInstanceDao;
import com.example.dailyboss.data.dao.TaskTemplateDao;
import com.example.dailyboss.domain.enums.FrequencyUnit;
import com.example.dailyboss.domain.enums.TaskDifficulty;
import com.example.dailyboss.domain.enums.TaskImportance;
import com.example.dailyboss.domain.enums.TaskStatus;
import com.example.dailyboss.domain.model.TaskInstance;
import com.example.dailyboss.domain.model.TaskTemplate;
import com.example.dailyboss.domain.repository.ITaskTemplateRepository;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TaskTemplateRepositoryImpl implements ITaskTemplateRepository {

    private final TaskTemplateDao taskTemplateDao;
    private final TaskInstanceDao taskInstanceDao;

    public TaskTemplateRepositoryImpl(Context context) {
        this.taskTemplateDao = new TaskTemplateDao(context);
        this.taskInstanceDao = new TaskInstanceDao(context);
    }

    @Override
    public long combineDateAndTime(long startDate, String executionTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(startDate);

        try {
            String[] parts = executionTime.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cal.getTimeInMillis();
    }

    @Override
    public boolean addTaskTemplate(String categoryId, String name, String description, String executionTime,
                                   int frequencyInterval, FrequencyUnit frequencyUnit,
                                   long startDate, long endDate, TaskDifficulty difficulty,
                                   TaskImportance importance, boolean isRecurring) {
        String id = UUID.randomUUID().toString();
        TaskTemplate taskTemplate = new TaskTemplate(id, categoryId, name, description, executionTime,
                frequencyInterval, frequencyUnit, startDate, endDate, difficulty, importance, isRecurring);

        long combined = combineDateAndTime(startDate, executionTime);
        if (!isRecurring) {
            TaskInstance taskInstance = new TaskInstance(UUID.randomUUID().toString(), combined, TaskStatus.ACTIVE, id);
            taskInstanceDao.insert(taskInstance);
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(startDate);
            long date = calendar.getTimeInMillis();

            do {
                combined = combineDateAndTime(date, executionTime);
                TaskInstance taskInstance = new TaskInstance(UUID.randomUUID().toString(), combined, TaskStatus.ACTIVE, id);
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
            } while (date <= endDate);
        }
        return taskTemplateDao.insert(taskTemplate);
    }

    @Override
    public boolean updateTaskTemplate(String templateId, String categoryId, String name, String description,
                                      String executionTime, int frequencyInterval, FrequencyUnit frequencyUnit,
                                      long startDate, long endDate, TaskDifficulty difficulty, TaskImportance importance,
                                      boolean isRecurring) {

        TaskTemplate taskTemplate = new TaskTemplate(templateId, categoryId, name, description, executionTime,
                frequencyInterval, frequencyUnit, startDate, endDate, difficulty, importance, isRecurring);

        String newTemplateId = UUID.randomUUID().toString();
        taskTemplate.setTemplateId(newTemplateId);
        boolean templateSuccess = taskTemplateDao.insert(taskTemplate);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long todayMillis = today.getTimeInMillis();

        List<TaskInstance> futureInstances = taskInstanceDao.getFutureActiveInstances(templateId, todayMillis);

        if (futureInstances != null && !futureInstances.isEmpty()) {
            for (TaskInstance instance : futureInstances) {
                instance.setTemplateId(newTemplateId);
                Calendar instanceCal = Calendar.getInstance();
                instanceCal.setTimeInMillis(instance.getInstanceDate());
                instanceCal.set(Calendar.HOUR_OF_DAY, 0);
                instanceCal.set(Calendar.MINUTE, 0);
                instanceCal.set(Calendar.SECOND, 0);
                instanceCal.set(Calendar.MILLISECOND, 0);
                long dateOnlyMillis = instanceCal.getTimeInMillis();
                long newCombinedTime = combineDateAndTime(dateOnlyMillis, executionTime);

                instance.setInstanceDate(newCombinedTime);
                taskInstanceDao.update(instance);
            }
        }

        return templateSuccess;
    }

    @Override
    public List<TaskTemplate> getAllTaskTemplates() {
        return taskTemplateDao.getAll();
    }

    @Override
    public boolean deleteById(String id) {
        return taskTemplateDao.deleteById(id);
    }

    @Override
    public boolean updateTask(TaskTemplate taskTemplate) {
        return taskTemplateDao.update(taskTemplate);
    }

    @Override
    public List<TaskTemplate> getByCategoryId(String categoryId) {
        return taskTemplateDao.getByCategoryId(categoryId);
    }

    @Override
    public Map<String, TaskTemplate> getTemplatesByIds(Set<String> ids) {
        return taskTemplateDao.getTaskTemplatesByIds(ids);
    }
}