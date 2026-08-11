package com.example.finai.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finai.databinding.FragmentStreakHistoryBinding;
import com.example.finai.data.StreakRepository;
import com.example.finai.model.StreakModel;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class StreakHistoryFragment extends Fragment {

    private FragmentStreakHistoryBinding binding;
    private StreakHistoryAdapter adapter;
    private StreakRepository streakRepo;
    private String currentFilter = "daily"; // Default to daily

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStreakHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        streakRepo = new StreakRepository(requireContext());
        adapter = new StreakHistoryAdapter();
        binding.historyList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.historyList.setAdapter(adapter);
        
        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null && getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        
        // Setup filter chips
        binding.chipDaily.setChecked(true);
        binding.chipDaily.setOnCheckedChangeListener((chip, isChecked) -> {
            if (isChecked) {
                currentFilter = "daily";
                refreshHistory();
            }
        });
        
        binding.chipWeekly.setOnCheckedChangeListener((chip, isChecked) -> {
            if (isChecked) {
                currentFilter = "weekly";
                refreshHistory();
            }
        });
        
        refreshHistory();
    }

    private void refreshHistory() {
        List<StreakModel> allEntries = streakRepo.getAllStreakEntries();
        List<StreakModel> filteredEntries = new ArrayList<>();
        
        // Filter by current selection
        for (StreakModel entry : allEntries) {
            if (entry.periodType != null && entry.periodType.equals(currentFilter)) {
                filteredEntries.add(entry);
            }
        }
        
        // Sort filtered entries appropriately
        if (currentFilter.equals("weekly")) {
            // Sort weekly entries by week ID (e.g., "2025-W50") in descending order
            filteredEntries.sort((a, b) -> {
                // Extract year and week from IDs like "2025-W50"
                try {
                    String[] aParts = a.id.split("-W");
                    String[] bParts = b.id.split("-W");
                    if (aParts.length == 2 && bParts.length == 2) {
                        int aYear = Integer.parseInt(aParts[0]);
                        int bYear = Integer.parseInt(bParts[0]);
                        if (aYear != bYear) {
                            return Integer.compare(bYear, aYear); // Descending by year
                        }
                        int aWeek = Integer.parseInt(aParts[1]);
                        int bWeek = Integer.parseInt(bParts[1]);
                        return Integer.compare(bWeek, aWeek); // Descending by week
                    }
                } catch (Exception ignored) {}
                // Fallback to timestamp comparison
                return Long.compare(b.timestamp, a.timestamp);
            });
        } else {
            // For daily entries, sort by date ID (yyyy-MM-dd) in descending order
            filteredEntries.sort((a, b) -> {
                // Date format is "yyyy-MM-dd", so string comparison works for descending
                return b.id.compareTo(a.id);
            });
        }
        
        adapter.submit(filteredEntries);
        
        if (filteredEntries == null || filteredEntries.isEmpty()) {
            String emptyMessage = currentFilter.equals("daily") 
                ? "No daily streak history yet" 
                : "No weekly streak history yet";
            binding.emptyText.setText(emptyMessage);
            binding.emptyText.setVisibility(View.VISIBLE);
            binding.historyList.setVisibility(View.GONE);
        } else {
            binding.emptyText.setVisibility(View.GONE);
            binding.historyList.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshHistory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

