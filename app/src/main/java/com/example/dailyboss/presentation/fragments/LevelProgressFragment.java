package com.example.dailyboss.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dailyboss.R;
import com.example.dailyboss.data.SharedPreferencesHelper;
import com.example.dailyboss.service.LevelingService;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Locale;


public class LevelProgressFragment extends Fragment {

    private TextView tvLevel;
    private TextView tvTitle;
    private TextView tvPowerPoints;
    private TextView tvCoins;
    private TextView tvCurrentXP;
    private TextView tvRequiredXP;
    private TextView tvProgressPercentage;
    private ProgressBar progressBarLevel;
    private MaterialToolbar toolbar;

    private LevelingService levelingService;
    private SharedPreferencesHelper prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_level_progress, container, false);

        levelingService = new LevelingService(requireContext());
        prefs = new SharedPreferencesHelper(requireContext());

        initViews(view);
        setupToolbar();
        loadLevelInfo();

        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        tvLevel = view.findViewById(R.id.tvLevel);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvPowerPoints = view.findViewById(R.id.tvPowerPoints);
        tvCoins = view.findViewById(R.id.tvCoins);
        tvCurrentXP = view.findViewById(R.id.tvCurrentXP);
        tvRequiredXP = view.findViewById(R.id.tvRequiredXP);
        tvProgressPercentage = view.findViewById(R.id.tvProgressPercentage);
        progressBarLevel = view.findViewById(R.id.progressBarLevel);
    }

    private void setupToolbar() {
        toolbar.setTitle("Napredovanje");
        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    private void loadLevelInfo() {
        String userId = prefs.getLoggedInUserId();
        if (userId == null) {
            return;
        }

        LevelingService.LevelInfo levelInfo = levelingService.getLevelInfo(userId);
        if (levelInfo == null) {
            return;
        }

        tvLevel.setText(String.format(Locale.getDefault(), "Nivo %d", levelInfo.currentLevel));
        tvTitle.setText(levelInfo.title);
        tvPowerPoints.setText(String.format(Locale.getDefault(), "%d PP", levelInfo.currentPP));
        tvCoins.setText(String.format(Locale.getDefault(), "%d novčića", levelInfo.currentCoins));

        int xpInCurrentLevel = levelInfo.currentXP - levelInfo.xpRequiredForCurrentLevel;
        int xpNeededForLevel = levelInfo.xpRequiredForNextLevel - levelInfo.xpRequiredForCurrentLevel;

        tvCurrentXP.setText(String.format(Locale.getDefault(), "%d XP", xpInCurrentLevel));
        tvRequiredXP.setText(String.format(Locale.getDefault(), "%d XP", xpNeededForLevel));
        tvProgressPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", levelInfo.progressPercentage));

        progressBarLevel.setMax(xpNeededForLevel);
        progressBarLevel.setProgress(xpInCurrentLevel);

        displayNextLevelInfo(levelInfo.currentLevel + 1);
    }

    private void displayNextLevelInfo(int nextLevel) {

        int nextLevelPP = LevelingService.calculatePPRewardForLevel(nextLevel);
        String nextLevelTitle = LevelingService.getTitleForLevel(nextLevel);

    }

    @Override
    public void onResume() {
        super.onResume();
        loadLevelInfo();
    }
}

