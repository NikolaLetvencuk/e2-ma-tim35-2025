package com.example.dailyboss.data.repository;

import android.content.Context;

import com.example.dailyboss.data.dao.TaskInstanceDao;
import com.example.dailyboss.data.dao.TaskTemplateDao;
import com.example.dailyboss.domain.enums.TaskStatus;
import com.example.dailyboss.domain.model.TaskInstance;
import com.example.dailyboss.domain.model.TaskTemplate;
import com.example.dailyboss.domain.repository.ITaskInstanceRepository;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class TaskInstanceRepositoryImpl implements ITaskInstanceRepository {

    private final TaskInstanceDao taskInstanceDAO;
    private final TaskTemplateDao taskTemplateDao;

    public TaskInstanceRepositoryImpl(Context context) {
        this.taskInstanceDAO = new TaskInstanceDao(context);
        this.taskTemplateDao = new TaskTemplateDao(context);
    }

    @Override
    public boolean addTaskInstance(String taskId, long instanceDate, TaskStatus status, String templateId) {
        String instanceId = UUID.randomUUID().toString();
        TaskInstance taskInstance = new TaskInstance(instanceId, instanceDate, status, templateId);
        return taskInstanceDAO.insert(taskInstance);
    }

    @Override
    public List<TaskInstance> getAllTaskInstances() {
        return taskInstanceDAO.getAll();
    }

    @Override
    public boolean deleteById(String instanceId) {
        return taskInstanceDAO.deleteById(instanceId);
    }

    @Override
    public boolean updateTaskInstance(TaskInstance taskInstance) {
        return taskInstanceDAO.update(taskInstance);
    }

    @Override
    public int countByCategoyId(String categoryId) {
        List<TaskTemplate> taskTemplates = taskTemplateDao.getByCategoryId(categoryId);
        int count = 0;
        for (TaskTemplate task : taskTemplates) {
            count += taskInstanceDAO.getByTaskId(task.getTemplateId());
        }
        return count;
    }

    @Override
    public List<TaskInstance> getTasksByDateRange(long startTimestamp, long endTimestamp) {
        long startOfDay = getStartOfDay(startTimestamp);
        long endOfDay = getEndOfDay(endTimestamp);
        return taskInstanceDAO.getTasksByDateRange(startOfDay, endOfDay);
    }

    // Helper metode
    private long getStartOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getEndOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }
}