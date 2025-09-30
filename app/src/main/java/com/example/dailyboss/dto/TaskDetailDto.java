package com.example.dailyboss.dto;

import com.example.dailyboss.enums.FrequencyUnit;
import com.example.dailyboss.enums.TaskDifficulty;
import com.example.dailyboss.enums.TaskImportance;
import com.example.dailyboss.enums.TaskStatus;

import java.io.Serializable;

public class TaskDetailDto implements Serializable {
    private String instanceId;
    private long instanceDate;
    private TaskStatus status;

    private String templateId;
    private String categoryId;
    private String name;
    private String description;
    private String executionTime;
    private int frequencyInterval;
    private FrequencyUnit frequencyUnit;
    private long startDate;
    private long endDate;
    private TaskDifficulty difficulty;
    private TaskImportance importance;
    private boolean isRecurring;
    private String categoryName;

    public TaskDetailDto() {}

    public TaskDetailDto(
            String instanceId, long instanceDate, TaskStatus status,
            String templateId, String categoryId, String categoryName,
            String name, String description, String executionTime,
            int frequencyInterval, FrequencyUnit frequencyUnit, long startDate, long endDate,
            TaskDifficulty difficulty, TaskImportance importance, boolean isRecurring) {

        this.instanceId = instanceId;
        this.instanceDate = instanceDate;
        this.status = status;
        this.templateId = templateId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.name = name;
        this.description = description;
        this.executionTime = executionTime;
        this.frequencyInterval = frequencyInterval;
        this.frequencyUnit = frequencyUnit;
        this.startDate = startDate;
        this.endDate = endDate;
        this.difficulty = difficulty;
        this.importance = importance;
        this.isRecurring = isRecurring;
    }

    public long getInstanceDate() {
        return instanceDate;
    }

    public void setInstanceDate(long instanceDate) {
        this.instanceDate = instanceDate;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
    }

    public int getFrequencyInterval() {
        return frequencyInterval;
    }

    public void setFrequencyInterval(int frequencyInterval) {
        this.frequencyInterval = frequencyInterval;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FrequencyUnit getFrequencyUnit() {
        return frequencyUnit;
    }

    public void setFrequencyUnit(FrequencyUnit frequencyUnit) {
        this.frequencyUnit = frequencyUnit;
    }

    public long getEndDate() {
        return endDate;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public TaskDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(TaskDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public TaskImportance getImportance() {
        return importance;
    }

    public void setImportance(TaskImportance importance) {
        this.importance = importance;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }
}