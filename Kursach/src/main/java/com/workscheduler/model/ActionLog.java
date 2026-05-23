package com.workscheduler.model;

public class ActionLog {
    private int id;
    private int userId;
    private String username;
    private String action;
    private String details;
    private long timestamp;

    public ActionLog(int id, int userId, String username, String action, String details, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.details = details;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public long getTimestamp() { return timestamp; }
}