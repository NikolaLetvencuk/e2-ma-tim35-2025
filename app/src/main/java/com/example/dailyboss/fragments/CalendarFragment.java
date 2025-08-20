package com.example.dailyboss.fragments;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.example.dailyboss.R;
import com.example.dailyboss.dto.TaskItemDto;
import com.example.dailyboss.model.TaskInstance;
import com.example.dailyboss.model.TaskTemplate;
import com.example.dailyboss.service.CategoryService;
import com.example.dailyboss.service.TaskInstanceService;
import com.example.dailyboss.service.TaskTemplateService;
import com.example.dailyboss.utils.DayTasksDrawable;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private View weekCalendarRoot; // fragment_week_calendar.xml
    private Button btnDay, btnWeek, btnMonth;

    private TaskInstanceService taskInstanceService;
    private TaskTemplateService taskTemplateService;
    private CategoryService categoryService;

    private List<TaskItemDto> tasks;

    private static final int PIXELS_PER_HOUR = 60;
    private static final int TASK_HEIGHT = 50;

    private LocalDate weekStart; // trenutno prikazana nedelja

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = root.findViewById(R.id.calendarView);
        btnDay = root.findViewById(R.id.btnDay);
        btnWeek = root.findViewById(R.id.btnWeek);
        btnMonth = root.findViewById(R.id.btnMonth);

        taskTemplateService = new TaskTemplateService(getContext());
        taskInstanceService = new TaskInstanceService(getContext());
        categoryService = new CategoryService(getContext());

        loadTasks();
        setupMonthView();

        // inflamo custom week layout
        weekCalendarRoot = inflater.inflate(R.layout.fragment_week_calendar, container, false);
        ((ViewGroup) root).addView(weekCalendarRoot);
        weekCalendarRoot.setVisibility(View.GONE);

        // inicijalni start nedelje (ponedeljak)
        weekStart = LocalDate.now().with(DayOfWeek.MONDAY);

        // podešavamo strelice
        ImageButton btnPrev = weekCalendarRoot.findViewById(R.id.btnPrevWeek);
        ImageButton btnNext = weekCalendarRoot.findViewById(R.id.btnNextWeek);

        btnPrev.setOnClickListener(v -> {
            weekStart = weekStart.minusWeeks(1);
            populateWeekView(weekStart);
        });

        btnNext.setOnClickListener(v -> {
            weekStart = weekStart.plusWeeks(1);
            populateWeekView(weekStart);
        });

        btnDay.setOnClickListener(v -> switchToDayView());
        btnWeek.setOnClickListener(v -> switchToWeekView());
        btnMonth.setOnClickListener(v -> switchToMonthView());

        return root;
    }

    private void populateWeekView(LocalDate startOfWeek) {
        LinearLayout daysContainer = weekCalendarRoot.findViewById(R.id.daysContainer);
        LinearLayout daysColumnsContainer = weekCalendarRoot.findViewById(R.id.daysColumnsContainer);

        daysContainer.removeAllViews();
        daysColumnsContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);

            // header (Mon 19)
            TextView dayLabel = new TextView(getContext());
            dayLabel.setText(date.getDayOfWeek().toString().substring(0, 3) + " " + date.getDayOfMonth());
            dayLabel.setPadding(16, 8, 16, 8);
            daysContainer.addView(dayLabel);

            // kolona za taskove
            FrameLayout dayColumn = new FrameLayout(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(200, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.setMargins(2, 2, 2, 2);
            dayColumn.setLayoutParams(lp);
            dayColumn.setBackgroundColor(Color.parseColor("#f8f8f8"));
            daysColumnsContainer.addView(dayColumn);

            // renderuj taskove za taj dan
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

    private void switchToDayView() {
        calendarView.setVisibility(View.GONE);
        weekCalendarRoot.setVisibility(View.GONE);
    }

    private void switchToWeekView() {
        calendarView.setVisibility(View.GONE);
        weekCalendarRoot.setVisibility(View.VISIBLE);
        populateWeekView(weekStart);
    }

    private void switchToMonthView() {
        calendarView.setVisibility(View.VISIBLE);
        weekCalendarRoot.setVisibility(View.GONE);
    }

    private void loadTasks() {
        long today = System.currentTimeMillis();
        long thirtyDaysLater = today + 30L * 24 * 60 * 60 * 1000;
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
}
