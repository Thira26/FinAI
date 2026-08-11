package com.example.finai.model;

public class StreakModel {
    public String id; // date string (yyyy-MM-dd) or week identifier
    public String periodType; // "daily" or "weekly"
    public double spent;
    public double budget;
    public int streakChange; // +1 or -1
    public int streakAfter; // streak count after this period
    public long timestamp;
    public String dateLabel; // human-readable date
    
    public StreakModel() {}
    
    public StreakModel(String id, String periodType, double spent, double budget, int streakChange, int streakAfter, long timestamp, String dateLabel) {
        this.id = id;
        this.periodType = periodType;
        this.spent = spent;
        this.budget = budget;
        this.streakChange = streakChange;
        this.streakAfter = streakAfter;
        this.timestamp = timestamp;
        this.dateLabel = dateLabel;
    }
}

