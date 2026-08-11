package com.example.finai.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finai.R;
import com.example.finai.model.StreakModel;

import java.util.ArrayList;
import java.util.List;

public class StreakHistoryAdapter extends RecyclerView.Adapter<StreakHistoryAdapter.VH> {

    private final List<StreakModel> data = new ArrayList<>();

    public void submit(List<StreakModel> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_streak_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        StreakModel s = data.get(i);
        h.dateLabel.setText(s.dateLabel != null ? s.dateLabel : s.id);
        
        // Hide period type label since we're already filtering by tabs
        h.periodType.setVisibility(View.GONE);
        
        h.spentAmount.setText("₹" + (long)s.spent);
        h.budgetAmount.setText("₹" + (long)s.budget);
        h.streakAfter.setText(String.valueOf(s.streakAfter));
        
        // Set streak change with color
        if (s.streakChange > 0) {
            h.streakChange.setText("+" + s.streakChange);
            h.streakChange.setTextColor(h.itemView.getContext().getResources().getColor(R.color.neon_blue));
        } else if (s.streakChange < 0) {
            h.streakChange.setText(String.valueOf(s.streakChange));
            h.streakChange.setTextColor(h.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
        } else {
            h.streakChange.setText("0");
            h.streakChange.setTextColor(h.itemView.getContext().getResources().getColor(R.color.md_onSurfaceVariant));
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView dateLabel, periodType, spentAmount, budgetAmount, streakChange, streakAfter;
        
        VH(@NonNull View itemView) {
            super(itemView);
            dateLabel = itemView.findViewById(R.id.dateLabel);
            periodType = itemView.findViewById(R.id.periodType);
            spentAmount = itemView.findViewById(R.id.spentAmount);
            budgetAmount = itemView.findViewById(R.id.budgetAmount);
            streakChange = itemView.findViewById(R.id.streakChange);
            streakAfter = itemView.findViewById(R.id.streakAfter);
        }
    }
}

