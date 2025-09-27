package com.example.dailyboss.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.dailyboss.R;
import com.example.dailyboss.dto.TaskItemDto;
import com.example.dailyboss.fragments.CalendarFragment;
import com.google.android.material.card.MaterialCardView;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskCalendarAdapter {

    public interface OnDateChangeListener {
        void onNextDay();
        void onPreviousDay();
        void onNextWeek();
        void onPreviousWeek();
    }
    private OnDateChangeListener dateChangeListener;

    public void setOnDateChangeListener(OnDateChangeListener listener) {
        this.dateChangeListener = listener;
    }

    private static final int PIXELS_PER_HOUR = 60;
    private static final int TASK_HEIGHT = 50;

    private List<TaskItemDto> tasks;
    private LayoutInflater inflater;

    public TaskCalendarAdapter(Context context, List<TaskItemDto> tasks) {
        this.tasks = tasks;
        this.inflater = LayoutInflater.from(context);
    }

    public void populateWeekView(LocalDate startOfWeek, LinearLayout daysContainer, LinearLayout daysColumnsContainer) {
        daysContainer.removeAllViews();
        daysColumnsContainer.removeAllViews();

        DisplayMetrics displayMetrics = inflater.getContext().getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int dayWidth = screenWidth / 7;

        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);

            // Labela dana
            TextView dayLabel = (TextView) inflater.inflate(R.layout.item_day_label, daysContainer, false);
            dayLabel.setText(date.getDayOfWeek().toString().substring(0, 3) + "\n" + date.getDayOfMonth());
            dayLabel.setLayoutParams(new LinearLayout.LayoutParams(dayWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            daysContainer.addView(dayLabel);

            // Vertical divider
            if (i < 6) {
                View divider = new View(inflater.getContext());
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(2, ViewGroup.LayoutParams.MATCH_PARENT);
                divider.setLayoutParams(dividerParams);
                divider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                daysContainer.addView(divider);
            }

            // Column za taskove
            FrameLayout dayColumn = new FrameLayout(inflater.getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dayWidth, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.setMargins(2, 2, 2, 2);
            dayColumn.setLayoutParams(lp);
            dayColumn.setBackgroundColor(Color.parseColor("#f8f8f8"));
            daysColumnsContainer.addView(dayColumn);

            // Taskovi za taj dan
            for (TaskItemDto task : tasks) {
                LocalDate taskDate = Instant.ofEpochMilli(task.getStartTime())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                if (!taskDate.equals(date)) continue;

                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(task.getStartTime());
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);

                float yPos = (hour + minute / 60f) * PIXELS_PER_HOUR;

                TextView taskView = new TextView(inflater.getContext());
                taskView.setText(task.getTitle());
                taskView.setBackgroundColor(Color.parseColor(task.getColor()));
                taskView.setTextSize(12);
                taskView.setPadding(4, 4, 4, 4);

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        TASK_HEIGHT
                );
                params.topMargin = (int) yPos;
                dayColumn.addView(taskView, params);
            }
        }
    }

    private GestureDetector gestureDetector;

    public void setupSwipeGestures(View targetView, boolean isWeekView) {
        gestureDetector = new GestureDetector(targetView.getContext(), new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(e2.getY() - e1.getY())) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) { // swipe right
                            if (isWeekView && dateChangeListener != null) dateChangeListener.onPreviousWeek();
                            else if (!isWeekView && dateChangeListener != null) dateChangeListener.onPreviousDay();
                        } else { // swipe left
                            if (isWeekView && dateChangeListener != null) dateChangeListener.onNextWeek();
                            else if (!isWeekView && dateChangeListener != null) dateChangeListener.onNextDay();
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        targetView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    public void populateDayView(LocalDate date, LinearLayout daysContainer, LinearLayout daysColumnsContainer, LinearLayout timeScaleColumn) {
        daysContainer.removeAllViews();
        daysColumnsContainer.removeAllViews();

        LayoutInflater inflater = this.inflater;

        // ScrollView za vertikalni scroll
        ScrollView verticalScrollView = new ScrollView(inflater.getContext());
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        verticalScrollView.setLayoutParams(scrollParams);
        verticalScrollView.setFillViewport(true);

        // Column u ScrollView
        LinearLayout dayColumn = new LinearLayout(inflater.getContext());
        dayColumn.setOrientation(LinearLayout.VERTICAL);
        DisplayMetrics displayMetrics = inflater.getContext().getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int dayWidth = screenWidth - timeScaleColumn.getWidth();

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dayWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(2, 2, 2, 2);
        dayColumn.setLayoutParams(lp);
        dayColumn.setBackgroundColor(Color.parseColor("#ffffff"));
        verticalScrollView.addView(dayColumn);

        // Dodaj ScrollView u container
        daysColumnsContainer.addView(verticalScrollView);

        // Labela dana
        TextView dayLabel = (TextView) inflater.inflate(R.layout.item_day_label, daysContainer, false);
        dayLabel.setText(date.getDayOfWeek().toString() + "\n" + date.getDayOfMonth());
        dayLabel.setPadding(16, 8, 16, 8);
        dayLabel.setWidth(dayWidth);
        dayLabel.setGravity(android.view.Gravity.CENTER);
        daysContainer.addView(dayLabel);

        // Povećanje visine skale (opciono)
        timeScaleColumn.setMinimumHeight(24 * PIXELS_PER_HOUR);

        // Filtriraj i sortiraj taskove za dan
        List<TaskItemDto> dayTasks = new ArrayList<>();
        for (TaskItemDto task : tasks) {
            LocalDate taskDate = Instant.ofEpochMilli(task.getStartTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            if (taskDate.equals(date)) dayTasks.add(task);
        }
        dayTasks.sort((t1, t2) -> Long.compare(t1.getStartTime(), t2.getStartTime()));

        // Dodavanje taskova u column
        for (TaskItemDto task : dayTasks) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(task.getStartTime());
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);

            float yPos = (hour + minute / 60f) * PIXELS_PER_HOUR;

            View taskView = inflater.inflate(R.layout.item_task, dayColumn, false);

            MaterialCardView card = taskView.findViewById(R.id.cardTask);
            TextView tvTitle = taskView.findViewById(R.id.tvTaskTitle);
            TextView tvDesc = taskView.findViewById(R.id.tvTaskDesc);
            TextView tvTime = taskView.findViewById(R.id.tvTaskTime);

            tvTitle.setText(task.getTitle());
            tvDesc.setText(task.getDescription());
            tvTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));

            card.setCardBackgroundColor(Color.parseColor(task.getColor()));

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    400
            );
            params.setMargins(30, 20, 30, 20);

            dayColumn.addView(taskView, params);
        }
    }
}
