package com.example.dailyboss.domain.repository;

import com.example.dailyboss.domain.enums.FrequencyUnit;
import com.example.dailyboss.domain.enums.TaskDifficulty;
import com.example.dailyboss.domain.enums.TaskImportance;
import com.example.dailyboss.domain.enums.TaskStatus;
import com.example.dailyboss.domain.model.TaskInstance;
import com.example.dailyboss.domain.model.TaskTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ITaskTemplateRepository {

    long combineDateAndTime(long startDate, String executionTime);

    boolean addTaskTemplate(
            String categoryId,
            String name,
            String description,
            String executionTime,
            int frequencyInterval,
            FrequencyUnit frequencyUnit,
            long startDate,
            long endDate,
            TaskDifficulty difficulty,
            TaskImportance importance,
            boolean isRecurring
    );

    boolean updateTaskTemplate(
            String templateId,
            String categoryId,
            String name,
            String description,
            String executionTime,
            int frequencyInterval,
            FrequencyUnit frequencyUnit,
            long startDate,
            long endDate,
            TaskDifficulty difficulty,
            TaskImportance importance,
            boolean isRecurring
    );

    List<TaskTemplate> getAllTaskTemplates();

    boolean deleteById(String id);

    boolean updateTask(TaskTemplate taskTemplate);

    List<TaskTemplate> getByCategoryId(String categoryId);

    Map<String, TaskTemplate> getTemplatesByIds(Set<String> ids);
}