package com.example.finai.model;

public class BadgeModel {
    public String id;
    public String name;
    public String description;
    public int streakRequired;
    public boolean unlocked;
    public long unlockedAt;
    
    public BadgeModel() {}
    
    public BadgeModel(String id, String name, String description, int streakRequired, boolean unlocked, long unlockedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.streakRequired = streakRequired;
        this.unlocked = unlocked;
        this.unlockedAt = unlockedAt;
    }
}

