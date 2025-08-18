package com.example.dailyboss.dto;

public class TaskItemDto {
    private String title;
    private String description;
    private long startTime;
    private String status;

    public TaskItemDto(String title, String description, long startTime, String status) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.status = status;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getStartTime() { return startTime; }
    public String getStatus() { return status; }
}