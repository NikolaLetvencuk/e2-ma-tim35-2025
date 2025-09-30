package com.example.dailyboss.dto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskItemDto {
    private String instanceId;
    private String title;
    private String description;
    private long startTime;
    private String status;
    private String color;
    private boolean isRepeating;


    public TaskItemDto(String instanceId, String title, String description, long startTime, String status, String color, boolean isRepeating) {
        this.instanceId = instanceId;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.status = status;
        this.color = color;
        this.isRepeating = isRepeating;
    }

    public String getInstanceId() { return instanceId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getStartTime() { return startTime; }
    public String getStatus() { return status; }
    public String getColor() { return color; }
    public boolean isRepeating() { return isRepeating; }
    public void setRepeating(boolean repeating) { isRepeating = repeating; }
    public void setStatus(String status) { this.status = status; }

    public String getFormattedTimeStatus() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(startTime)) + " - " + status;
    }

}