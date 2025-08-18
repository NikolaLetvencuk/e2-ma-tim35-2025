package com.example.dailyboss.model;

import com.example.dailyboss.enums.TaskDifficulty;
import com.example.dailyboss.enums.TaskImportance;
import com.example.dailyboss.enums.TaskStatus;

public class TaskInstance {
    private String instanceId;
    private long instanceDate;
    private TaskStatus status;
    private String templateId;

    public TaskInstance(String instanceId, long instanceDate, TaskStatus status, String templateId) {
        this.instanceId = instanceId;
        this.instanceDate = instanceDate;
        this.status = status;
        this.templateId = templateId;
    }

    public TaskInstance() {
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
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
}