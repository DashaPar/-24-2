package com.workscheduler.model;

import java.time.LocalDateTime;

public class Task {
    private int id;
    private int userId;
    private String title;
    private String description;
    private String category;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int estimatedMinutes;
    private int actualMinutes;
    private String status; // PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    private long createdAt;

    public Task(int id, int userId, String title, String description, String category,
                LocalDateTime startTime, LocalDateTime endTime, int estimatedMinutes,
                int actualMinutes, String status, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.startTime = startTime;
        this.endTime = endTime;
        this.estimatedMinutes = estimatedMinutes;
        this.actualMinutes = actualMinutes;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public int getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
    public int getActualMinutes() { return actualMinutes; }
    public void setActualMinutes(int actualMinutes) { this.actualMinutes = actualMinutes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
}