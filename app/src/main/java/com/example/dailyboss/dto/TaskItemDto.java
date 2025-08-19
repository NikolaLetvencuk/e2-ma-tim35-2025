package com.example.dailyboss.dto;

public class TaskItemDto {
    private String title;
    private String description;
    private long startTime;
    private String status;
    private String color;


    public TaskItemDto(String title, String description, long startTime, String status, String color) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.status = status;
        this.color = color;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getStartTime() { return startTime; }
    public String getStatus() { return status; }
    public String getColor() { return color; }
}