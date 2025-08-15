package com.example.dailyboss.model;

import com.example.dailyboss.enums.TaskDifficulty;
import com.example.dailyboss.enums.TaskImportance;
import com.example.dailyboss.enums.TaskStatus;

public class TaskInstance {
    private String instanceId;
    private String taskId;
    private long instanceDate;
    private TaskStatus status;
    private String templateId;
    private String categoryId;
    private String name;
    private String description;
    private TaskDifficulty difficulty;
    private TaskImportance importance;
    private int xpValue;

    public TaskInstance(String instanceId, String taskId, long instanceDate, TaskStatus status, String templateId, String categoryId, String name, String description, TaskDifficulty difficulty, TaskImportance importance, int xpValue) {
        this.instanceId = instanceId;
        this.taskId = taskId;
        this.instanceDate = instanceDate;
        this.status = status;
        this.templateId = templateId;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.importance = importance;
        this.xpValue = xpValue;
    }

    public TaskInstance() {
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public long getInstanceDate() {
        return instanceDate;
    }

    public void setInstanceDate(long instanceDate) {
        this.instanceDate = instanceDate;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
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

    public int getXpValue() {
        return xpValue;
    }

    public void setXpValue(int xpValue) {
        this.xpValue = xpValue;
    }
}