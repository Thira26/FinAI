package com.example.finai.model;

public class VoucherModel {
    public String id;
    public String title;
    public String description;
    public String discount; // e.g., "10% OFF", "₹100 OFF"
    public int streakRequired;
    public boolean unlocked;
    public boolean used;
    public long unlockedAt;
    public String merchant; // e.g., "Swiggy", "Amazon"
    
    public VoucherModel() {}
    
    public VoucherModel(String id, String title, String description, String discount, int streakRequired, String merchant, boolean unlocked, boolean used, long unlockedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.discount = discount;
        this.streakRequired = streakRequired;
        this.merchant = merchant;
        this.unlocked = unlocked;
        this.used = used;
        this.unlockedAt = unlockedAt;
    }
}

