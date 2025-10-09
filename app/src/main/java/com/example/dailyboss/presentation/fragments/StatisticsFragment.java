package com.example.dailyboss.presentation.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dailyboss.R; // Ensure this is correct
import com.example.dailyboss.data.dao.UserProfileDao;
import com.example.dailyboss.data.repository.TaskInstanceRepositoryImpl;
import com.example.dailyboss.data.repository.UserCategoryStatisticRepository;
import com.example.dailyboss.data.repository.UserStatisticRepository;
import com.example.dailyboss.domain.model.UserCategoryStatistic;
import com.example.dailyboss.domain.model.UserProfile;
import com.example.dailyboss.domain.model.UserStatistic;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth; // Assuming Firebase Auth for user ID

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    private TextView tvActiveDaysCount;
    private TextView tvLongestTaskStreak;
    private TextView tvAverageTaskDifficulty;
    private TextView tvSpecialMissions;
    private PieChart pieChartTaskStatus;
    private BarChart barChartCategories;
    private LineChart lineChartAverageDifficulty;
    private LineChart lineChartXpProgress;
    private UserStatisticRepository userStatisticRepository;
    private UserCategoryStatisticRepository userCategoryStatisticRepository;
    private TaskInstanceRepositoryImpl taskInstanceRepository;

    private String currentUserId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            currentUserId = "guest_user";
            Toast.makeText(getContext(), "Korisnik nije prijavljen. Statistika možda neće biti dostupna.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        userStatisticRepository = new UserStatisticRepository(requireContext());
        userCategoryStatisticRepository = new UserCategoryStatisticRepository(requireContext());
        taskInstanceRepository = new TaskInstanceRepositoryImpl(requireContext());
        // Initialize UI elements
        tvActiveDaysCount = view.findViewById(R.id.tvActiveDaysCount);
        tvLongestTaskStreak = view.findViewById(R.id.tvLongestTaskStreak);
        tvAverageTaskDifficulty = view.findViewById(R.id.tvAverageTaskDifficulty);
        tvSpecialMissions = view.findViewById(R.id.tvSpecialMissions);
        pieChartTaskStatus = view.findViewById(R.id.pieChartTaskStatus);
        barChartCategories = view.findViewById(R.id.barChartCategories);
        lineChartAverageDifficulty = view.findViewById(R.id.lineChartAverageDifficulty);
        lineChartXpProgress = view.findViewById(R.id.lineChartXpProgress);

        if (currentUserId != null && !currentUserId.equals("guest_user")) {
            loadStatistics();
        } else {
            Toast.makeText(getContext(), "Nije moguće učitati statistiku bez prijave.", Toast.LENGTH_LONG).show();
            tvActiveDaysCount.setText("N/A");
            tvLongestTaskStreak.setText("N/A");
            tvSpecialMissions.setText("N/A");
            tvAverageTaskDifficulty.setText("N/A");
            pieChartTaskStatus.clear();
            barChartCategories.clear();
            lineChartAverageDifficulty.clear();
            lineChartXpProgress.clear();
        }
        return view;
    }

    private void loadStatistics() {
        // Load UserStatistic
        userStatisticRepository.getUserStatistic(currentUserId, new UserStatisticRepository.UserStatisticDataListener() { // CORRECTED LISTENER NAME
            @Override
            public void onSuccess(UserStatistic statistic) {
                if (isAdded()) {
                    updateUserStatisticUI(statistic);
                    loadCategoryStatistics(statistic.getUserId());
                    setupAverageDifficultyDisplayAndGraph(statistic);
                    setupLineChartXpProgress(taskInstanceRepository.getDailyXPProgress());
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Greška pri dohvatanju statistike korisnika: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadCategoryStatistics(String userStatisticId) {
        userCategoryStatisticRepository.getAllCategoryStatisticsForUser(userStatisticId, new UserCategoryStatisticRepository.UserCategoryStatisticListListener() {
            @Override
            public void onSuccess(List<UserCategoryStatistic> statistics) {
                if (isAdded()) {
                    updateCategoryStatisticsUI(statistics);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Greška pri dohvatanju statistike kategorija: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupAverageDifficultyDisplayAndGraph(UserStatistic statistic) {
        String userId = statistic.getUserId();
        String avgDescription = taskInstanceRepository.getAverageDifficultyLevelDescription(userId);
        tvAverageTaskDifficulty.setText(avgDescription);
        tvAverageTaskDifficulty.setTextSize(20f);

        try {
            List<Entry> difficultyEntries = taskInstanceRepository.getAverageDifficultyTrend(userId);

            setupLineChartAverageDifficulty(difficultyEntries);

        } catch (Exception e) {
            Log.e("StatsFragment", "Greška pri učitavanju trenda prosečne težine: " + e.getMessage());
            Toast.makeText(requireContext(), "Greška pri učitavanju trenda težine.", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateUserStatisticUI(UserStatistic statistic) {
        if (statistic == null) {
            tvActiveDaysCount.setText("0 dana");
            tvLongestTaskStreak.setText("0 dana zaredom");
            tvSpecialMissions.setText("Započeto: 0, Završeno: 0");
            setupPieChartTaskStatus(0, 0, 0, 0);
            return;
        }

        tvActiveDaysCount.setText(statistic.getActiveDaysCount() + " dana");
        tvLongestTaskStreak.setText(statistic.getLongestTaskStreak() + "");
        tvSpecialMissions.setText("Započeto: " + statistic.getTotalSpecialMissionsStarted() + ", Završeno: " + statistic.getTotalSpecialMissionsCompleted());

        int totalCreated = statistic.getTotalCreatedTasks();
        int completed = statistic.getTotalCompletedTasks();
        int failed = statistic.getTotalFailedTasks();
        int cancelled = statistic.getTotalCancelledTasks();

        setupPieChartTaskStatus(completed, failed, cancelled, totalCreated);
    }

    private void updateCategoryStatisticsUI(List<UserCategoryStatistic> categoryStatistics) {
        if (categoryStatistics == null || categoryStatistics.isEmpty()) {
            setupBarChartCategories(new HashMap<>());
            return;
        }

        Map<String, Integer> completedTasksByCategory = new HashMap<>();

        for (UserCategoryStatistic stat : categoryStatistics) {
            String categoryName = stat.getCategoryName();
            completedTasksByCategory.put(categoryName, stat.getCompletedCount());
        }
        setupBarChartCategories(completedTasksByCategory);
    }


    private void setupPieChartTaskStatus(int completed, int failed, int cancelled, int createdButPending) {
        pieChartTaskStatus.setUsePercentValues(true);
        pieChartTaskStatus.getDescription().setEnabled(false);
        pieChartTaskStatus.setExtraOffsets(5, 10, 5, 5);

        pieChartTaskStatus.setDragDecelerationFrictionCoef(0.95f);

        pieChartTaskStatus.setDrawHoleEnabled(true);
        pieChartTaskStatus.setHoleColor(Color.WHITE);
        pieChartTaskStatus.setTransparentCircleColor(Color.WHITE);
        pieChartTaskStatus.setTransparentCircleAlpha(110);
        pieChartTaskStatus.setHoleRadius(58f);
        pieChartTaskStatus.setTransparentCircleRadius(61f);

        pieChartTaskStatus.setDrawCenterText(true);
        pieChartTaskStatus.setCenterText("Status Zadataka");
        pieChartTaskStatus.setCenterTextSize(14f);

        pieChartTaskStatus.setRotationAngle(0);
        pieChartTaskStatus.setRotationEnabled(true);
        pieChartTaskStatus.setHighlightPerTapEnabled(true);

        ArrayList<PieEntry> entries = new ArrayList<>();
        if (completed > 0) entries.add(new PieEntry(completed, "Završeni"));
        if (failed > 0) entries.add(new PieEntry(failed, "Neuspeli"));
        if (cancelled > 0) entries.add(new PieEntry(cancelled, "Otkazani"));
        if (createdButPending > 0) entries.add(new PieEntry(createdButPending, "Kreirani"));


        PieDataSet dataSet = new PieDataSet(entries, "Status Zadataka");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // Define colors
        final int[] MY_COLORS = {
                Color.rgb(124, 252, 0),
                Color.rgb(255, 69, 0),
                Color.rgb(105, 105, 105),
                Color.rgb(30, 144, 255)
        };
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : MY_COLORS) colors.add(c);
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChartTaskStatus));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);

        pieChartTaskStatus.setData(data);
        pieChartTaskStatus.invalidate();

        Legend l = pieChartTaskStatus.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
    }

    private void setupBarChartCategories(Map<String, Integer> categoryData) {
        barChartCategories.getDescription().setEnabled(false);
        barChartCategories.setDrawGridBackground(false);

        XAxis xAxis = barChartCategories.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(categoryData.size());
        xAxis.setLabelRotationAngle(-45);

        ArrayList<BarEntry> entries = new ArrayList<>();
        final ArrayList<String> categoryLabels = new ArrayList<>();

        int i = 0;
        for (Map.Entry<String, Integer> entry : categoryData.entrySet()) {
            entries.add(new BarEntry(i, entry.getValue()));
            categoryLabels.add(entry.getKey());
            i++;
        }

        xAxis.setValueFormatter(new IndexAxisValueFormatter(categoryLabels));

        BarDataSet dataSet = new BarDataSet(entries, "Završeni Zadaci po Kategoriji");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setDrawValues(true);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);
        barChartCategories.setData(data);
        barChartCategories.setFitBars(true);
        barChartCategories.invalidate();

        barChartCategories.getAxisRight().setEnabled(false);
        barChartCategories.getAxisLeft().setAxisMinimum(0f);
        barChartCategories.animateY(1000);
    }

    private void setupLineChartAverageDifficulty(List<Entry> entries) {
        lineChartAverageDifficulty.getDescription().setEnabled(false);
        lineChartAverageDifficulty.setTouchEnabled(true);
        lineChartAverageDifficulty.setDragEnabled(true);
        lineChartAverageDifficulty.setScaleEnabled(true);
        lineChartAverageDifficulty.setPinchZoom(true);

        LineDataSet dataSet = new LineDataSet(entries, "Prosečna Težina Zadataka (5 dana)");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.LTGRAY);

        LineData lineData = new LineData(dataSet);
        lineChartAverageDifficulty.setData(lineData);
        lineChartAverageDifficulty.invalidate();

        List<String> dayLabels = new ArrayList<>();

        for (int i = 0; i < entries.size(); i++) {
            int daysAgo = entries.size() - 1 - i;
            if (daysAgo == 0) {
                dayLabels.add("Danas");
            } else if (daysAgo == 1) {
                dayLabels.add("Juče");
            } else {
                dayLabels.add("Pre " + daysAgo + "d");
            }
        }

        XAxis xAxis = lineChartAverageDifficulty.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dayLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(entries.size());
        lineChartAverageDifficulty.getAxisRight().setEnabled(false);

        lineChartAverageDifficulty.getAxisLeft().setAxisMinimum(0f);

        lineChartAverageDifficulty.animateX(1500);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setAxisLineColor(Color.WHITE);
        xAxis.setGridColor(Color.DKGRAY);

        YAxis yAxisLeft = lineChartAverageDifficulty.getAxisLeft();
        yAxisLeft.setTextColor(Color.WHITE);
        yAxisLeft.setAxisLineColor(Color.WHITE);
        yAxisLeft.setGridColor(Color.DKGRAY);

        Legend legend = lineChartAverageDifficulty.getLegend();
        legend.setTextColor(Color.WHITE);

    }

    private void setupLineChartXpProgress(List<Entry> entries) {
        if (entries.isEmpty()) {
            Log.d("StatsFragment", "Nema XP podataka za poslednjih 7 dana.");
            lineChartXpProgress.clear();
            lineChartXpProgress.invalidate();
            return;
        }

        lineChartXpProgress.getDescription().setEnabled(false);
        lineChartXpProgress.setTouchEnabled(true);
        lineChartXpProgress.setDragEnabled(true);
        lineChartXpProgress.setScaleEnabled(true);
        lineChartXpProgress.setPinchZoom(true);

        LineDataSet dataSet = new LineDataSet(entries, "XP Osvojenih");
        dataSet.setColor(Color.parseColor("#00ADB5"));
        dataSet.setCircleColor(Color.parseColor("#00ADB5"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#6600ADB5"));

        LineData lineData = new LineData(dataSet);
        lineChartXpProgress.setData(lineData);

        List<String> dayLabels = new ArrayList<>();
        int totalDays = entries.size();

        for (int i = 0; i < totalDays; i++) {
            int daysAgo = totalDays - 1 - i;
            if (daysAgo == 0) {
                dayLabels.add("Danas");
            } else if (daysAgo == 1) {
                dayLabels.add("Juče");
            } else {
                dayLabels.add("Pre " + daysAgo + "d");
            }
        }

        XAxis xAxis = lineChartXpProgress.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dayLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(totalDays);

        lineChartXpProgress.getAxisRight().setEnabled(false);
        lineChartXpProgress.getAxisLeft().setAxisMinimum(0f);
        lineChartXpProgress.animateX(1500);

        xAxis.setTextColor(Color.WHITE);
        xAxis.setAxisLineColor(Color.WHITE);
        xAxis.setGridColor(Color.DKGRAY);

        YAxis yAxisLeft = lineChartXpProgress.getAxisLeft();
        yAxisLeft.setTextColor(Color.WHITE);
        yAxisLeft.setAxisLineColor(Color.WHITE);
        yAxisLeft.setGridColor(Color.DKGRAY);

        Legend legend = lineChartXpProgress.getLegend();
        legend.setTextColor(Color.WHITE);

        lineChartXpProgress.invalidate();
    }
}