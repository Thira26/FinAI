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

import com.example.finai.databinding.FragmentRewardsBinding;
import com.example.finai.data.RewardsRepository;

public class RewardsFragment extends Fragment {

    private FragmentRewardsBinding binding;
    private BadgeAdapter badgeAdapter;
    private VoucherAdapter voucherAdapter;
    private RewardsRepository rewardsRepo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRewardsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        rewardsRepo = new RewardsRepository(requireContext());
        
        badgeAdapter = new BadgeAdapter();
        binding.badgesList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.badgesList.setAdapter(badgeAdapter);
        
        voucherAdapter = new VoucherAdapter();
        binding.vouchersList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.vouchersList.setAdapter(voucherAdapter);
        
        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null && getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        
        refreshRewards();
    }

    private void refreshRewards() {
        badgeAdapter.submit(rewardsRepo.getAllBadges());
        voucherAdapter.submit(rewardsRepo.getAllVouchers());
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshRewards();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

