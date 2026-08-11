package com.example.finai.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finai.R;
import com.example.finai.model.VoucherModel;

import java.util.ArrayList;
import java.util.List;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VH> {

    private final List<VoucherModel> data = new ArrayList<>();

    public void submit(List<VoucherModel> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        VoucherModel v = data.get(i);
        h.voucherTitle.setText(v.title);
        h.voucherDescription.setText(v.description);
        h.voucherDiscount.setText(v.discount);
        h.voucherMerchant.setText(v.merchant);
        h.voucherRequirement.setText("Requires " + v.streakRequired + " streaks");
        
        if (v.unlocked) {
            if (v.used) {
                h.voucherStatus.setText("Used");
                h.voucherStatus.setTextColor(h.itemView.getContext().getResources().getColor(R.color.md_onSurfaceVariant));
            } else {
                h.voucherStatus.setText("Unlocked");
                h.voucherStatus.setTextColor(h.itemView.getContext().getResources().getColor(R.color.neon_blue));
            }
        } else {
            h.voucherStatus.setText("Locked");
            h.voucherStatus.setTextColor(h.itemView.getContext().getResources().getColor(R.color.md_onSurfaceVariant));
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView voucherTitle, voucherDescription, voucherDiscount, voucherMerchant, voucherRequirement, voucherStatus;
        
        VH(@NonNull View itemView) {
            super(itemView);
            voucherTitle = itemView.findViewById(R.id.voucherTitle);
            voucherDescription = itemView.findViewById(R.id.voucherDescription);
            voucherDiscount = itemView.findViewById(R.id.voucherDiscount);
            voucherMerchant = itemView.findViewById(R.id.voucherMerchant);
            voucherRequirement = itemView.findViewById(R.id.voucherRequirement);
            voucherStatus = itemView.findViewById(R.id.voucherStatus);
        }
    }
}

