package com.example.finai.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.finai.model.BadgeModel;
import com.example.finai.model.VoucherModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RewardsRepository {
    private static final String PREF = "finai_local";
    private static final String KEY_BADGES = "badges";
    private static final String KEY_VOUCHERS = "vouchers";

    private final SharedPreferences sp;
    private final StreakRepository streakRepo;

    public RewardsRepository(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        streakRepo = new StreakRepository(ctx);
    }

    public List<BadgeModel> getAllBadges() {
        List<BadgeModel> badges = new ArrayList<>();
        
        // Predefined badges - every 5 streaks
        int[] streakMilestones = {5, 10, 15, 20, 25, 30, 50, 100};
        String[] badgeNames = {
            "Saver Starter", "Budget Master", "Smart Spender", "Money Wizard",
            "Thrifty Titan", "Cash Champion", "Economy Expert", "Wealth Warrior"
        };
        String[] badgeDescriptions = {
            "You're getting started!", "Great progress!", "You're doing well!", "Impressive!",
            "Amazing dedication!", "Outstanding!", "Exceptional!", "Legendary!"
        };
        
        int currentStreak = streakRepo.getCurrentStreak();
        
        try {
            JSONArray saved = new JSONArray(sp.getString(KEY_BADGES, "[]"));
            for (int i = 0; i < streakMilestones.length; i++) {
                BadgeModel badge = findBadgeInArray(saved, String.valueOf(streakMilestones[i]));
                if (badge == null) {
                    badge = new BadgeModel();
                    badge.id = String.valueOf(streakMilestones[i]);
                    badge.name = badgeNames[i];
                    badge.description = badgeDescriptions[i];
                    badge.streakRequired = streakMilestones[i];
                    badge.unlocked = currentStreak >= streakMilestones[i];
                    badge.unlockedAt = badge.unlocked ? System.currentTimeMillis() : 0;
                } else {
                    // Update unlocked status based on current streak
                    if (!badge.unlocked && currentStreak >= streakMilestones[i]) {
                        badge.unlocked = true;
                        badge.unlockedAt = System.currentTimeMillis();
                        saveBadge(badge);
                    }
                }
                badges.add(badge);
            }
        } catch (JSONException e) {
            // Initialize badges if none exist
            for (int i = 0; i < streakMilestones.length; i++) {
                BadgeModel badge = new BadgeModel();
                badge.id = String.valueOf(streakMilestones[i]);
                badge.name = badgeNames[i];
                badge.description = badgeDescriptions[i];
                badge.streakRequired = streakMilestones[i];
                badge.unlocked = currentStreak >= streakMilestones[i];
                badge.unlockedAt = badge.unlocked ? System.currentTimeMillis() : 0;
                badges.add(badge);
                saveBadge(badge);
            }
        }
        
        return badges;
    }

    public List<VoucherModel> getAllVouchers() {
        List<VoucherModel> vouchers = new ArrayList<>();
        
        // Predefined vouchers - every 15 streaks
        int[] streakMilestones = {15, 30, 45, 60, 90};
        String[] voucherTitles = {
            "Food Delivery Discount", "Shopping Spree", "Entertainment Bundle",
            "Travel Voucher", "Premium Savings"
        };
        String[] voucherDescriptions = {
            "Enjoy your meals with savings!", "Shop smart with discounts!", "Relax with entertainment deals!",
            "Travel more for less!", "Exclusive premium offers!"
        };
        String[] discounts = {
            "10% OFF", "₹200 OFF", "15% OFF", "₹500 OFF", "20% OFF"
        };
        String[] merchants = {
            "Swiggy/Zomato", "Amazon/Flipkart", "Netflix/Spotify", "Ola/Uber", "Various"
        };
        
        int currentStreak = streakRepo.getCurrentStreak();
        
        try {
            JSONArray saved = new JSONArray(sp.getString(KEY_VOUCHERS, "[]"));
            for (int i = 0; i < streakMilestones.length; i++) {
                VoucherModel voucher = findVoucherInArray(saved, String.valueOf(streakMilestones[i]));
                if (voucher == null) {
                    voucher = new VoucherModel();
                    voucher.id = String.valueOf(streakMilestones[i]);
                    voucher.title = voucherTitles[i];
                    voucher.description = voucherDescriptions[i];
                    voucher.discount = discounts[i];
                    voucher.merchant = merchants[i];
                    voucher.streakRequired = streakMilestones[i];
                    voucher.unlocked = currentStreak >= streakMilestones[i];
                    voucher.used = false;
                    voucher.unlockedAt = voucher.unlocked ? System.currentTimeMillis() : 0;
                } else {
                    // Update unlocked status based on current streak
                    if (!voucher.unlocked && currentStreak >= streakMilestones[i]) {
                        voucher.unlocked = true;
                        voucher.unlockedAt = System.currentTimeMillis();
                        saveVoucher(voucher);
                    }
                }
                vouchers.add(voucher);
            }
        } catch (JSONException e) {
            // Initialize vouchers if none exist
            for (int i = 0; i < streakMilestones.length; i++) {
                VoucherModel voucher = new VoucherModel();
                voucher.id = String.valueOf(streakMilestones[i]);
                voucher.title = voucherTitles[i];
                voucher.description = voucherDescriptions[i];
                voucher.discount = discounts[i];
                voucher.merchant = merchants[i];
                voucher.streakRequired = streakMilestones[i];
                voucher.unlocked = currentStreak >= streakMilestones[i];
                voucher.used = false;
                voucher.unlockedAt = voucher.unlocked ? System.currentTimeMillis() : 0;
                vouchers.add(voucher);
                saveVoucher(voucher);
            }
        }
        
        return vouchers;
    }

    private BadgeModel findBadgeInArray(JSONArray arr, String id) {
        try {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                if (id.equals(o.optString("id"))) {
                    BadgeModel b = new BadgeModel();
                    b.id = o.optString("id");
                    b.name = o.optString("name");
                    b.description = o.optString("description");
                    b.streakRequired = o.optInt("streakRequired", 0);
                    b.unlocked = o.optBoolean("unlocked", false);
                    b.unlockedAt = o.optLong("unlockedAt", 0);
                    return b;
                }
            }
        } catch (JSONException ignored) {}
        return null;
    }

    private VoucherModel findVoucherInArray(JSONArray arr, String id) {
        try {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                if (id.equals(o.optString("id"))) {
                    VoucherModel v = new VoucherModel();
                    v.id = o.optString("id");
                    v.title = o.optString("title");
                    v.description = o.optString("description");
                    v.discount = o.optString("discount");
                    v.merchant = o.optString("merchant");
                    v.streakRequired = o.optInt("streakRequired", 0);
                    v.unlocked = o.optBoolean("unlocked", false);
                    v.used = o.optBoolean("used", false);
                    v.unlockedAt = o.optLong("unlockedAt", 0);
                    return v;
                }
            }
        } catch (JSONException ignored) {}
        return null;
    }

    private void saveBadge(BadgeModel badge) {
        try {
            JSONArray arr = new JSONArray(sp.getString(KEY_BADGES, "[]"));
            // Remove existing if any
            JSONArray newArr = new JSONArray();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                if (!badge.id.equals(o.optString("id"))) {
                    newArr.put(o);
                }
            }
            // Add updated badge
            JSONObject o = new JSONObject();
            o.put("id", badge.id);
            o.put("name", badge.name);
            o.put("description", badge.description);
            o.put("streakRequired", badge.streakRequired);
            o.put("unlocked", badge.unlocked);
            o.put("unlockedAt", badge.unlockedAt);
            newArr.put(o);
            sp.edit().putString(KEY_BADGES, newArr.toString()).apply();
        } catch (JSONException ignored) {}
    }

    private void saveVoucher(VoucherModel voucher) {
        try {
            JSONArray arr = new JSONArray(sp.getString(KEY_VOUCHERS, "[]"));
            // Remove existing if any
            JSONArray newArr = new JSONArray();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                if (!voucher.id.equals(o.optString("id"))) {
                    newArr.put(o);
                }
            }
            // Add updated voucher
            JSONObject o = new JSONObject();
            o.put("id", voucher.id);
            o.put("title", voucher.title);
            o.put("description", voucher.description);
            o.put("discount", voucher.discount);
            o.put("merchant", voucher.merchant);
            o.put("streakRequired", voucher.streakRequired);
            o.put("unlocked", voucher.unlocked);
            o.put("used", voucher.used);
            o.put("unlockedAt", voucher.unlockedAt);
            newArr.put(o);
            sp.edit().putString(KEY_VOUCHERS, newArr.toString()).apply();
        } catch (JSONException ignored) {}
    }

    public void markVoucherAsUsed(String voucherId) {
        List<VoucherModel> vouchers = getAllVouchers();
        for (VoucherModel v : vouchers) {
            if (v.id.equals(voucherId)) {
                v.used = true;
                saveVoucher(v);
                break;
            }
        }
    }
}

