package com.example.finai.ui;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.finai.databinding.FragmentStreaksBinding;
import com.example.finai.R;
import com.example.finai.data.LocalTransactionsRepository;
import com.example.finai.data.StreakRepository;
import com.example.finai.model.TransactionModel;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class StreaksFragment extends Fragment {

    private FragmentStreaksBinding binding;
    private StreakRepository streakRepo;
    private LocalTransactionsRepository txRepo;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        streakRepo = new StreakRepository(requireContext());
        txRepo = new LocalTransactionsRepository(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStreaksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClickListeners();
        refreshDashboard();
        updateStreaksFromTransactions();
    }

    private void setupClickListeners() {
        if (binding == null) return;

        View.OnClickListener editBudgetListener = v -> {
            String type = (v == binding.btnEditDailyBudget) ? "daily" : "weekly";
            showBudgetDialog(type);
        };

        binding.btnEditDailyBudget.setOnClickListener(editBudgetListener);
        binding.btnEditWeeklyBudget.setOnClickListener(editBudgetListener);

        binding.btnViewHistory.setOnClickListener(v -> navigateToFragment(StreakHistoryFragment.class));
        binding.btnViewRewards.setOnClickListener(v -> navigateToFragment(RewardsFragment.class));
    }

    private void navigateToFragment(Class<? extends Fragment> fragmentClass) {
        if (getActivity() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, fragmentClass, null)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void showBudgetDialog(String type) {
        if (getContext() == null) return;

        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        double currentBudget = type.equals("daily") ?
                streakRepo.getDailyBudget() : streakRepo.getWeeklyBudget();
        input.setText(String.valueOf((long) currentBudget));
        input.setHint("Enter budget amount (₹)");

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit " + (type.equals("daily") ? "Daily" : "Weekly") + " Budget")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> saveBudget(type, input.getText().toString()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveBudget(String type, String amountStr) {
        try {
            double budget = Double.parseDouble(amountStr);
            if (budget <= 0) {
                showToast("Budget must be greater than 0");
                return;
            }

            double oldDailyBudget = streakRepo.getDailyBudget();
            double oldWeeklyBudget = streakRepo.getWeeklyBudget();

            if (type.equals("daily")) {
                streakRepo.setDailyBudget(budget);
            } else {
                streakRepo.setWeeklyBudget(budget);
            }

            // Recalculate all streaks with new budgets
            streakRepo.recalculateStreaksWithNewBudgets(
                    requireContext(),
                    streakRepo.getDailyBudget(),
                    streakRepo.getWeeklyBudget()
            );

            refreshDashboard();
            showSnackbar("Budget updated and streaks recalculated");

        } catch (NumberFormatException e) {
            showToast("Invalid amount");
        }
    }

    private void refreshDashboard() {
        if (binding == null) return;

        int currentStreak = streakRepo.getCurrentStreak();
        binding.currentStreakNumber.setText(String.valueOf(currentStreak));

        // Update motivational message
        updateMotivationalMessage(currentStreak);

        // Update daily budget progress
        updateBudgetProgress("daily");

        // Update weekly budget progress
        updateBudgetProgress("weekly");
    }

    private void updateBudgetProgress(String type) {
        if (binding == null) return;

        boolean isDaily = type.equals("daily");
        double budget = isDaily ? streakRepo.getDailyBudget() : streakRepo.getWeeklyBudget();
        double spent = isDaily ? calculateDailySpend() : calculateWeeklySpend();

        if (isDaily) {
            binding.dailyBudget.setText(String.format(Locale.US, "₹%d", (long) budget));
            binding.dailySpent.setText(String.format(Locale.US, "₹%d", (long) spent));
            int progress = budget > 0 ? (int) Math.min(100, Math.round((spent / budget) * 100)) : 0;
            binding.dailyProgressBar.setProgress(progress);
        } else {
            binding.weeklyBudget.setText(String.format(Locale.US, "₹%d", (long) budget));
            binding.weeklySpent.setText(String.format(Locale.US, "₹%d", (long) spent));
            int progress = budget > 0 ? (int) Math.min(100, Math.round((spent / budget) * 100)) : 0;
            binding.weeklyProgressBar.setProgress(progress);
        }
    }

    private double calculateDailySpend() {
        try {
            String today = DATE_FORMAT.format(new Date());
            List<TransactionModel> transactions = txRepo.getAll();

            return transactions.stream()
                    .filter(Objects::nonNull)
                    .filter(t -> t.type != null && !t.type.equalsIgnoreCase("credit"))
                    .filter(t -> today.equals(t.dateIso))
                    .mapToDouble(t -> Math.max(0, t.amount))
                    .sum();

        } catch (Exception e) {
            return 0;
        }
    }

    private double calculateWeeklySpend() {
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            // Get start of week (Monday)
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int daysFromMonday = (dayOfWeek == Calendar.SUNDAY ? 6 : dayOfWeek - Calendar.MONDAY);
            cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday);

            long weekStart = cal.getTimeInMillis();
            long weekEnd = weekStart + (7 * 24 * 60 * 60 * 1000L);

            return txRepo.getAll().stream()
                    .filter(Objects::nonNull)
                    .filter(t -> t.type != null && !t.type.equalsIgnoreCase("credit"))
                    .filter(t -> isDateInRange(t.dateIso, weekStart, weekEnd))
                    .mapToDouble(t -> Math.max(0, t.amount))
                    .sum();

        } catch (Exception e) {
            return 0;
        }
    }

    private boolean isDateInRange(String dateIso, long startTime, long endTime) {
        if (dateIso == null) return false;
        try {
            Date date = DATE_FORMAT.parse(dateIso);
            return date != null && date.getTime() >= startTime && date.getTime() < endTime;
        } catch (ParseException e) {
            return false;
        }
    }

    private void updateStreaksFromTransactions() {
        String today = DATE_FORMAT.format(new Date());
        String lastUpdate = streakRepo.getLastUpdateDate();

        if (!today.equals(lastUpdate)) {
            updateDailyStreak(today);
            updateWeeklyStreak();
            streakRepo.setLastUpdateDate(today);
        }

        refreshDashboard();
    }

    private void updateDailyStreak(String today) {
        if (streakRepo.getStreakEntry(today, "daily") == null) {
            double dailySpent = calculateDailySpend();
            double dailyBudget = streakRepo.getDailyBudget();
            int streakChange = streakRepo.updateStreakForPeriod(today, "daily", dailySpent, dailyBudget);
            updateMotivationalMessage(streakChange);
        }
    }

    private void updateWeeklyStreak() {
        Calendar cal = Calendar.getInstance();
        String weekId = cal.get(Calendar.YEAR) + "-W" + cal.get(Calendar.WEEK_OF_YEAR);

        if (streakRepo.getStreakEntry(weekId, "weekly") == null) {
            double weeklySpent = calculateWeeklySpend();
            double weeklyBudget = streakRepo.getWeeklyBudget();
            streakRepo.updateStreakForPeriod(weekId, "weekly", weeklySpent, weeklyBudget);
        }
    }

    private void updateMotivationalMessage(int currentStreak) {
        if (binding == null) return;

        String message;
        if (currentStreak == 0) {
            message = "Start your savings journey!";
        } else if (currentStreak < 5) {
            message = "Keep it up! You're doing great!";
        } else if (currentStreak < 15) {
            message = "Great! +" + currentStreak + " streak!";
        } else if (currentStreak < 30) {
            message = "Amazing dedication! Keep going!";
        } else {
            message = "Legendary streak! You're unstoppable!";
        }
        binding.motivationalMessage.setText(message);
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void showSnackbar(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshDashboard();
        updateStreaksFromTransactions();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
