package com.example.dailyboss.fragments;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.example.dailyboss.R;
import com.example.dailyboss.adapters.TaskCalendarAdapter;
import com.example.dailyboss.dto.TaskItemDto;
import com.example.dailyboss.model.TaskInstance;
import com.example.dailyboss.model.TaskTemplate;
import com.example.dailyboss.service.CategoryService;
import com.example.dailyboss.service.TaskInstanceService;
import com.example.dailyboss.service.TaskTemplateService;
import com.example.dailyboss.utils.DayTasksDrawable;
import com.google.android.material.card.MaterialCardView;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private View weekCalendarRoot;
    private Button btnDay, btnWeek, btnMonth;

    private TaskInstanceService taskInstanceService;
    private TaskTemplateService taskTemplateService;
    private CategoryService categoryService;

    private List<TaskItemDto> tasks;

    private static final int PIXELS_PER_HOUR = 60;
    private static final int TASK_HEIGHT = 50;
    private boolean isWeek = true;

    private GestureDetector gestureDetector;
    private View root;

    private LocalDate weekStart;
    private LocalDate dayView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = root.findViewById(R.id.calendarView);
        btnDay = root.findViewById(R.id.btnDay);
        btnWeek = root.findViewById(R.id.btnWeek);
        btnMonth = root.findViewById(R.id.btnMonth);

        taskTemplateService = new TaskTemplateService(getContext());
        taskInstanceService = new TaskInstanceService(getContext());
        categoryService = new CategoryService(getContext());

        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastDayOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        long startMillis = firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endMillis = lastDayOfMonth.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        loadTasks(startMillis, endMillis);
        setupMonthView();


        weekCalendarRoot = inflater.inflate(R.layout.fragment_week_calendar, container, false);
        ((ViewGroup) root).addView(weekCalendarRoot);
        weekCalendarRoot.setVisibility(View.GONE);

        weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        dayView = LocalDate.now();

        calendarView.setOnForwardPageChangeListener(() -> {
            updateTasksForCurrentMonth();
        });

        calendarView.setOnPreviousPageChangeListener(() -> {
            updateTasksForCurrentMonth();
        });

        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
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
                        if (diffX > 0 && isWeek) {
                            Log.d("EE", "onFling: swipe right");
                            weekStart = weekStart.minusWeeks(1);
                            populateWeekView(weekStart);
                        } else if(diffX < 0 && isWeek) {
                            Log.d("EE", "onFling: swipe left");
                            weekStart = weekStart.plusWeeks(1);
                            populateWeekView(weekStart);
                        } else if(diffX > 0) {
                            dayView = dayView.minusDays(1);
                            populateDayView(dayView);
                        } else {
                            dayView = dayView.plusDays(1);
                            populateDayView(dayView);
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        weekCalendarRoot.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        LinearLayout daysColumnsContainer = weekCalendarRoot.findViewById(R.id.daysColumnsContainer);
        daysColumnsContainer.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false; // vrati false da bi i dalje mogao normalno da skrola
        });


        btnDay.setOnClickListener(v -> switchToDayView());
        btnWeek.setOnClickListener(v -> switchToWeekView());
        btnMonth.setOnClickListener(v -> switchToMonthView());

        TaskCalendarAdapter adapter = new TaskCalendarAdapter(getContext(), tasks);
        LinearLayout daysContainer = weekCalendarRoot.findViewById(R.id.daysContainer);
        LinearLayout timeScaleColumn = weekCalendarRoot.findViewById(R.id.timeScaleColumn);

        adapter.setOnDateChangeListener(new TaskCalendarAdapter.OnDateChangeListener() {
            @Override
            public void onNextDay() {
                dayView = dayView.plusDays(1);
                adapter.populateDayView(dayView, daysContainer, daysColumnsContainer, timeScaleColumn);
            }

            @Override
            public void onPreviousDay() {
                dayView = dayView.minusDays(1);
                adapter.populateDayView(dayView, daysContainer, daysColumnsContainer, timeScaleColumn);
            }

            @Override
            public void onNextWeek() {
                weekStart = weekStart.plusWeeks(1);
                adapter.populateWeekView(weekStart, daysContainer, daysColumnsContainer);
            }

            @Override
            public void onPreviousWeek() {
                weekStart = weekStart.minusWeeks(1);
                adapter.populateWeekView(weekStart, daysContainer, daysColumnsContainer);
            }
        });
        adapter.setupSwipeGestures(daysColumnsContainer, isWeek);


        return root;
    }

    private void populateWeekView(LocalDate startOfWeek) {
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        long startMillis = startOfWeek.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli();

        long endMillis = endOfWeek.atTime(23, 59, 59)
                .atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli();

        loadTasks(startMillis, endMillis);        LinearLayout daysContainer = weekCalendarRoot.findViewById(R.id.daysContainer);
        LinearLayout daysColumnsContainer = weekCalendarRoot.findViewById(R.id.daysColumnsContainer);
        HorizontalScrollView daysScrollView = root.findViewById(R.id.daysScrollView);
        daysScrollView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false; // false da i dalje radi normalan scroll
        });

        daysContainer.removeAllViews();
        daysColumnsContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (int i = 0; i < 7; i++) {
            FrameLayout dayColumn = new FrameLayout(getContext());
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int screenWidth = displayMetrics.widthPixels;
            int dayWidth = screenWidth / 7;

            LocalDate date = startOfWeek.plusDays(i);
            TextView dayLabel = (TextView) inflater.inflate(R.layout.item_day_label, daysContainer, false);
            dayLabel.setText(date.getDayOfWeek().toString().substring(0, 3) + "\n" + date.getDayOfMonth());
            LinearLayout.LayoutParams labelParams =
                    new LinearLayout.LayoutParams(dayWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            dayLabel.setLayoutParams(labelParams);
            daysContainer.addView(dayLabel);
            if (i < 6) {
                View verticalDivider = new View(getContext());
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                        2, // širina linije
                        ViewGroup.LayoutParams.MATCH_PARENT // ide od vrha do dna
                );
                verticalDivider.setLayoutParams(dividerParams);
                verticalDivider.setBackgroundColor(Color.parseColor("#FFFFFF")); // boja linije
                daysContainer.addView(verticalDivider);
            }


            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dayWidth, ViewGroup.LayoutParams.MATCH_PARENT);            lp.setMargins(2, 2, 2, 2);
            dayColumn.setLayoutParams(lp);
            dayColumn.setBackgroundColor(Color.parseColor("#f8f8f8"));
            daysColumnsContainer.addView(dayColumn);


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

                TextView taskView = new TextView(getContext());
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

    private void populateDayView(LocalDate date) {
        loadTasks(date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        LinearLayout daysContainer = weekCalendarRoot.findViewById(R.id.daysContainer);
        LinearLayout daysColumnsContainer = weekCalendarRoot.findViewById(R.id.daysColumnsContainer);
        LinearLayout timeScaleColumn = weekCalendarRoot.findViewById(R.id.timeScaleColumn);
        HorizontalScrollView daysScrollView = root.findViewById(R.id.daysScrollView);
        daysScrollView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false; // false da i dalje radi normalan scroll
        });
        daysContainer.removeAllViews();
        daysColumnsContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());

        // ScrollView za vertikalni scroll
        ScrollView verticalScrollView = new ScrollView(getContext());
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        verticalScrollView.setLayoutParams(scrollParams);
        verticalScrollView.setFillViewport(true);

        // FrameLayout u ScrollView za postavljanje taskova
        LinearLayout dayColumn = new LinearLayout(getContext());
        dayColumn.setOrientation(LinearLayout.VERTICAL);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int dayWidth = screenWidth - timeScaleColumn.getWidth();

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dayWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(2, 2, 2, 2);
        dayColumn.setLayoutParams(lp);
        dayColumn.setBackgroundColor(Color.parseColor("#ffffff"));
        verticalScrollView.addView(dayColumn);

        // Dodaj ScrollView u daysColumnsContainer
        daysColumnsContainer.addView(verticalScrollView);

        // Labela dana
        TextView dayLabel = (TextView) inflater.inflate(R.layout.item_day_label, daysContainer, false);
        dayLabel.setText(date.getDayOfWeek().toString() + "\n" + date.getDayOfMonth());
        dayLabel.setPadding(16, 8, 16, 8);
        dayLabel.setWidth(dayWidth);
        dayLabel.setGravity(Gravity.CENTER);
        daysContainer.addView(dayLabel);

        // Povećanje visine skale (opciono)
        timeScaleColumn.setMinimumHeight(24 * PIXELS_PER_HOUR); // 24h * visina po satu

        // Filtriraj taskove za ovaj dan i sortiraj po startTime
        List<TaskItemDto> dayTasks = new ArrayList<>();
        for (TaskItemDto task : tasks) {
            LocalDate taskDate = Instant.ofEpochMilli(task.getStartTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            if (taskDate.equals(date)) dayTasks.add(task);
        }
        dayTasks.sort((t1, t2) -> Long.compare(t1.getStartTime(), t2.getStartTime()));

        // Dodavanje taskova u FrameLayout
        for (TaskItemDto task : dayTasks) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(task.getStartTime());
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);

            float yPos = (hour + minute / 60f) * PIXELS_PER_HOUR;

            View taskView = LayoutInflater.from(getContext()).inflate(R.layout.item_task, dayColumn, false);

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

    private void switchToDayView() {
        calendarView.setVisibility(View.GONE);
        weekCalendarRoot.setVisibility(View.VISIBLE);
        populateDayView(dayView);
        isWeek = false;
    }

    private void switchToWeekView() {
        calendarView.setVisibility(View.GONE);
        weekCalendarRoot.setVisibility(View.VISIBLE);
        populateWeekView(weekStart);
        isWeek = true;
    }

    private void switchToMonthView() {
        calendarView.setVisibility(View.VISIBLE);
        weekCalendarRoot.setVisibility(View.GONE);
    }

    private void loadTasks(long startDate, long endDate) {
        long today = startDate;
        long thirtyDaysLater = endDate;
        List<TaskInstance> instances = taskInstanceService.getTasksByDateRange(today, thirtyDaysLater);

        Set<String> templateIds = new HashSet<>();
        for (TaskInstance instance : instances) templateIds.add(instance.getTemplateId());

        Map<String, TaskTemplate> templatesMap = taskTemplateService.getTemplatesByIds(templateIds);

        tasks = new ArrayList<>();
        for (TaskInstance instance : instances) {
            TaskTemplate template = templatesMap.get(instance.getTemplateId());
            String color = categoryService.getColorById(template.getCategoryId());

            TaskItemDto dto = new TaskItemDto(
                    template.getName(),
                    template.getDescription(),
                    instance.getInstanceDate(),
                    instance.getStatus().toString(),
                    color
            );
            tasks.add(dto);
        }
    }

    private void setupMonthView() {
        List<EventDay> events = new ArrayList<>();
        Map<Long, List<TaskItemDto>> tasksByDay = new HashMap<>();

        for (TaskItemDto task : tasks) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(task.getStartTime());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            long key = cal.getTimeInMillis();
            tasksByDay.computeIfAbsent(key, k -> new ArrayList<>()).add(task);
        }

        for (Map.Entry<Long, List<TaskItemDto>> entry : tasksByDay.entrySet()) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(entry.getKey());

            DayTasksDrawable drawable = new DayTasksDrawable(entry.getValue());
            events.add(new EventDay(cal, drawable));
        }

        calendarView.setEvents(events);
    }

    private void updateTasksForCurrentMonth() {
        Calendar currentPageDate = calendarView.getCurrentPageDate();

        LocalDate firstDay = LocalDate.of(
                currentPageDate.get(Calendar.YEAR),
                currentPageDate.get(Calendar.MONTH) + 1,
                1
        );
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        long firstDayMillis = firstDay.atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        long lastDayMillis = lastDay.atTime(23, 59, 59)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        loadTasks(firstDayMillis, lastDayMillis);
        setupMonthView();
    }
}