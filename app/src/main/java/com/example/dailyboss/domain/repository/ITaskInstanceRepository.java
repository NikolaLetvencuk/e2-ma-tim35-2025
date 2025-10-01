package com.example.dailyboss.domain.repository;

import com.example.dailyboss.domain.enums.TaskStatus;
import com.example.dailyboss.domain.model.TaskInstance;

import java.util.List;

public interface ITaskInstanceRepository {

    boolean addTaskInstance(String taskId, long instanceDate, TaskStatus status, String templateId);

    List<TaskInstance> getAllTaskInstances();

    boolean deleteById(String instanceId);

    boolean updateTaskInstance(TaskInstance taskInstance);

    int countByCategoyId(String categoryId);

    List<TaskInstance> getTasksByDateRange(long startTimestamp, long endTimestamp);
}