package com.example.dailyboss.model;

import com.example.dailyboss.enums.FrequencyUnit;
import com.example.dailyboss.enums.TaskDifficulty;
import com.example.dailyboss.enums.TaskImportance;

public class TaskTemplate {
    private String templateId;
    private String categoryId;
    private String name;
    private String description;
    private String executionTime;
    private int frequencyInterval;
    private FrequencyUnit frequencyUnit;
    private long startDate;
    private Long endDate;
    private TaskDifficulty difficulty;
    private TaskImportance importance;
    private boolean isRecurring;

    public TaskTemplate(String templateId, String categoryId, String name, String description, String executionTime, int frequencyInterval, FrequencyUnit frequencyUnit, long startDate, Long endDate, TaskDifficulty difficulty, TaskImportance importance, boolean isRecurring) {
        this.templateId = templateId;
        this.categoryId = categoryId;
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

    public TaskTemplate() {
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public FrequencyUnit getFrequencyUnit() {
        return frequencyUnit;
    }

    public void setFrequencyUnit(FrequencyUnit frequencyUnit) {
        this.frequencyUnit = frequencyUnit;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
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