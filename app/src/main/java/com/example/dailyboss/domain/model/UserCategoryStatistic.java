package com.example.dailyboss.domain.model;

public class UserCategoryStatistic {
    private String id; // Primarni ključ
    private String userStatisticId; // FK na UserStatistic
    private String categoryId; // FK na Category
    private int completedCount;
    private String categoryName;

    public UserCategoryStatistic() {}

    public UserCategoryStatistic(String id, String userStatisticId, String categoryId, int completedCount, String categoryName) {
        this.id = id;
        this.userStatisticId = userStatisticId;
        this.categoryId = categoryId;
        this.completedCount = completedCount;
        this.categoryName = categoryName;
    }

    // Getteri i setteri
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserStatisticId() { return userStatisticId; }
    public void setUserStatisticId(String userStatisticId) { this.userStatisticId = userStatisticId; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public int getCompletedCount() { return completedCount; }
    public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void incrementCompletedTasks() {
        this.completedCount++;
    }
}