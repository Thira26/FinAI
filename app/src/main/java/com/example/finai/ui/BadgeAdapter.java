package com.example.finai.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finai.R;
import com.example.finai.model.BadgeModel;

import java.util.ArrayList;
import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.VH> {

    private final List<BadgeModel> data = new ArrayList<>();

    public void submit(List<BadgeModel> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_badge, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        BadgeModel b = data.get(i);
        h.badgeName.setText(b.name);
        h.badgeDescription.setText(b.description);
        h.badgeRequirement.setText("Requires " + b.streakRequired + " streaks");
        
        if (b.unlocked) {
            h.unlockedBadge.setText("🏆");
            h.unlockedBadge.setVisibility(View.VISIBLE);
        } else {
            h.unlockedBadge.setText("🔒");
            h.unlockedBadge.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView badgeName, badgeDescription, badgeRequirement, unlockedBadge;
        
        VH(@NonNull View itemView) {
            super(itemView);
            badgeName = itemView.findViewById(R.id.badgeName);
            badgeDescription = itemView.findViewById(R.id.badgeDescription);
            badgeRequirement = itemView.findViewById(R.id.badgeRequirement);
            unlockedBadge = itemView.findViewById(R.id.unlockedBadge);
        }
    }
}

