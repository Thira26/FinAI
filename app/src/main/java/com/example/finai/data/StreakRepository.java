package com.example.finai.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.finai.model.StreakModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StreakRepository {
    private static final String PREF = "finai_local";
    private static final String KEY_STREAKS = "streaks";
    private static final String KEY_CURRENT_STREAK = "current_streak";
    private static final String KEY_DAILY_BUDGET = "daily_budget";
    private static final String KEY_WEEKLY_BUDGET = "weekly_budget";
    private static final String KEY_LAST_UPDATE_DATE = "last_update_date";

    private final SharedPreferences sp;

    public StreakRepository(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public int getCurrentStreak() {
        return sp.getInt(KEY_CURRENT_STREAK, 0);
    }

    public void setCurrentStreak(int streak) {
        sp.edit().putInt(KEY_CURRENT_STREAK, Math.max(0, streak)).apply();
    }

    public double getDailyBudget() {
        return Double.longBitsToDouble(sp.getLong(KEY_DAILY_BUDGET, Double.doubleToLongBits(1000.0)));
    }

    public void setDailyBudget(double budget) {
        sp.edit().putLong(KEY_DAILY_BUDGET, Double.doubleToLongBits(budget)).apply();
    }

    public double getWeeklyBudget() {
        return Double.longBitsToDouble(sp.getLong(KEY_WEEKLY_BUDGET, Double.doubleToLongBits(7000.0)));
    }

    public void setWeeklyBudget(double budget) {
        sp.edit().putLong(KEY_WEEKLY_BUDGET, Double.doubleToLongBits(budget)).apply();
    }

    public String getLastUpdateDate() {
        return sp.getString(KEY_LAST_UPDATE_DATE, "");
    }

    public void setLastUpdateDate(String date) {
        sp.edit().putString(KEY_LAST_UPDATE_DATE, date).apply();
    }

    public void addStreakEntry(StreakModel entry) {
        try {
            JSONArray arr = new JSONArray(sp.getString(KEY_STREAKS, "[]"));
            JSONObject o = new JSONObject();
            o.put("id", entry.id);
            o.put("periodType", entry.periodType);
            o.put("spent", entry.spent);
            o.put("budget", entry.budget);
            o.put("streakChange", entry.streakChange);
            o.put("streakAfter", entry.streakAfter);
            o.put("timestamp", entry.timestamp);
            o.put("dateLabel", entry.dateLabel);
            arr.put(o);
            sp.edit().putString(KEY_STREAKS, arr.toString()).apply();
        } catch (JSONException ignored) {}
    }

    public List<StreakModel> getAllStreakEntries() {
        List<StreakModel> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(sp.getString(KEY_STREAKS, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                StreakModel s = new StreakModel();
                s.id = o.optString("id");
                s.periodType = o.optString("periodType", "daily");
                s.spent = o.optDouble("spent", 0);
                s.budget = o.optDouble("budget", 0);
                s.streakChange = o.optInt("streakChange", 0);
                s.streakAfter = o.optInt("streakAfter", 0);
                s.timestamp = o.optLong("timestamp", 0);
                s.dateLabel = o.optString("dateLabel", "");
                list.add(s);
            }
        } catch (JSONException ignored) {}
        // Sort by timestamp descending (newest first)
        list.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
        return list;
    }

    public List<StreakModel> getStreakEntriesByPeriod(String periodType) {
        List<StreakModel> all = getAllStreakEntries();
        List<StreakModel> filtered = new ArrayList<>();
        for (StreakModel s : all) {
            if (s.periodType.equals(periodType)) {
                filtered.add(s);
            }
        }
        return filtered;
    }

    // Get streak entry for a specific date/week
    public StreakModel getStreakEntry(String id, String periodType) {
        List<StreakModel> all = getAllStreakEntries();
        for (StreakModel s : all) {
            if (s.id.equals(id) && s.periodType.equals(periodType)) {
                return s;
            }
        }
        return null;
    }

    // Calculate and update streak based on spend vs budget
    // Returns the streak change that occurred
    public int updateStreakForPeriod(String periodId, String periodType, double spent, double budget) {
        int currentStreak = getCurrentStreak();
        int streakChange;
        
        if (spent <= budget) {
            streakChange = 1;
            currentStreak += 1;
        } else {
            streakChange = -1;
            currentStreak = Math.max(0, currentStreak - 1);
        }
        
        setCurrentStreak(currentStreak);
        
        // Save entry
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();
        String dateLabel;
        if (periodType.equals("daily")) {
            dateLabel = new SimpleDateFormat("MMM d, yyyy", Locale.US).format(new Date());
        } else {
            // Calculate week date range
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int daysFromMonday = (dayOfWeek == Calendar.SUNDAY ? 6 : dayOfWeek - Calendar.MONDAY);
            cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday);
            Date weekStart = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, 6);
            Date weekEnd = cal.getTime();
            SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
            dateLabel = monthDayFormat.format(weekStart) + " – " + monthDayFormat.format(weekEnd);
        }
        
        StreakModel entry = new StreakModel(
            periodId,
            periodType,
            spent,
            budget,
            streakChange,
            currentStreak,
            System.currentTimeMillis(),
            dateLabel
        );
        addStreakEntry(entry);
        
        return streakChange;
    }

    // Recalculate all streak entries with new budget values
    public void recalculateStreaksWithNewBudgets(Context ctx, double newDailyBudget, double newWeeklyBudget) {
        List<StreakModel> allEntries = getAllStreakEntries();
        if (allEntries.isEmpty()) return;
        
        // Clear existing streaks and recalculate from scratch
        sp.edit().remove(KEY_STREAKS).putInt(KEY_CURRENT_STREAK, 0).apply();
        
        LocalTransactionsRepository txRepo = new LocalTransactionsRepository(ctx);
        List<com.example.finai.model.TransactionModel> transactions = txRepo.getAll();
        
        // Recalculate daily streaks
        Map<String, Double> dailySpend = new HashMap<>();
        Map<String, String> dailyDateLabels = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        
        try {
            for (com.example.finai.model.TransactionModel t : transactions) {
                if (t == null || t.dateIso == null) continue;
                if (t.type != null && t.type.equalsIgnoreCase("credit")) continue;
                
                String dateId = t.dateIso;
                dailySpend.put(dateId, dailySpend.getOrDefault(dateId, 0.0) + Math.max(0, t.amount));
                Date txDate = sdf.parse(t.dateIso);
                if (txDate != null) {
                    SimpleDateFormat labelFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
                    dailyDateLabels.put(dateId, labelFormat.format(txDate));
                }
            }
            
            // Recalculate weekly streaks
            Calendar cal = Calendar.getInstance();
            Map<String, Double> weeklySpend = new HashMap<>();
            Map<String, String> weeklyDateLabels = new HashMap<>();
            
            for (com.example.finai.model.TransactionModel t : transactions) {
                if (t == null || t.dateIso == null) continue;
                if (t.type != null && t.type.equalsIgnoreCase("credit")) continue;
                
                try {
                    Date txDate = sdf.parse(t.dateIso);
                    if (txDate == null) continue;
                    
                    cal.setTime(txDate);
                    int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
                    int year = cal.get(Calendar.YEAR);
                    String weekId = year + "-W" + weekOfYear;
                    
                    weeklySpend.put(weekId, weeklySpend.getOrDefault(weekId, 0.0) + Math.max(0, t.amount));
                    
                    // Calculate week date range
                    if (!weeklyDateLabels.containsKey(weekId)) {
                        cal.setTime(txDate);
                        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                        int daysFromMonday = (dayOfWeek == Calendar.SUNDAY ? 6 : dayOfWeek - Calendar.MONDAY);
                        cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday);
                        Date weekStart = cal.getTime();
                        cal.add(Calendar.DAY_OF_MONTH, 6);
                        Date weekEnd = cal.getTime();
                        
                        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
                        weeklyDateLabels.put(weekId, monthDayFormat.format(weekStart) + " – " + monthDayFormat.format(weekEnd));
                    }
                } catch (Exception ignored) {}
            }
            
            // Recalculate all daily streaks in chronological order
            List<String> sortedDailyDates = new ArrayList<>(dailySpend.keySet());
            sortedDailyDates.sort(String::compareTo);
            
            for (String dateId : sortedDailyDates) {
                double spent = dailySpend.get(dateId);
                String dateLabel = dailyDateLabels.getOrDefault(dateId, dateId);
                updateStreakForPeriodWithLabel(dateId, "daily", spent, newDailyBudget, dateLabel);
            }
            
            // Recalculate all weekly streaks in chronological order
            List<String> sortedWeeks = new ArrayList<>(weeklySpend.keySet());
            sortedWeeks.sort(String::compareTo);
            
            for (String weekId : sortedWeeks) {
                double spent = weeklySpend.get(weekId);
                String dateLabel = weeklyDateLabels.getOrDefault(weekId, weekId);
                updateStreakForPeriodWithLabel(weekId, "weekly", spent, newWeeklyBudget, dateLabel);
            }
        } catch (Exception ignored) {}
    }
    
    private int updateStreakForPeriodWithLabel(String periodId, String periodType, double spent, double budget, String dateLabel) {
        int currentStreak = getCurrentStreak();
        int streakChange;
        
        if (spent <= budget) {
            streakChange = 1;
            currentStreak += 1;
        } else {
            streakChange = -1;
            currentStreak = Math.max(0, currentStreak - 1);
        }
        
        setCurrentStreak(currentStreak);
        
        StreakModel entry = new StreakModel(
            periodId,
            periodType,
            spent,
            budget,
            streakChange,
            currentStreak,
            System.currentTimeMillis(),
            dateLabel
        );
        addStreakEntry(entry);
        
        return streakChange;
    }

    // Remove a specific streak entry
    public void removeStreakEntry(String id, String periodType) {
        try {
            JSONArray arr = new JSONArray(sp.getString(KEY_STREAKS, "[]"));
            JSONArray newArr = new JSONArray();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String entryId = o.optString("id");
                String entryType = o.optString("periodType", "daily");
                if (!entryId.equals(id) || !entryType.equals(periodType)) {
                    newArr.put(o);
                }
            }
            sp.edit().putString(KEY_STREAKS, newArr.toString()).apply();
        } catch (JSONException ignored) {}
    }
}

