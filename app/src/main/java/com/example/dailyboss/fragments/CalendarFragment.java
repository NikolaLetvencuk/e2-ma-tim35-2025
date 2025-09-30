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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.example.dailyboss.R;
// Predpostavlja se da imate TaskCalendarAdapter
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

        // Učitavanje i dodavanje Week/Day kontejnera
        weekCalendarRoot = inflater.inflate(R.layout.fragment_week_calendar, container, false);
        ((ViewGroup) root).addView(weekCalendarRoot);
        weekCalendarRoot.setVisibility(View.GONE);

        weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        dayView = LocalDate.now();

        calendarView.setOnForwardPageChangeListener(this::updateTasksForCurrentMonth);
        calendarView.setOnPreviousPageChangeListener(this::updateTasksForCurrentMonth);

        // Inicijalizacija GestureDetector-a za Day/Week Swipe
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
                        if (diffX > 0) { // Swipe Right (Previous)
                            if (isWeek) {
                                Log.d("EE", "onFling: swipe right (week)");
                                weekStart = weekStart.minusWeeks(1);
                                populateWeekView(weekStart);
                            } else {
                                Log.d("EE", "onFling: swipe right (day)");
                                dayView = dayView.minusDays(1);
                                populateDayView(dayView);
                            }
                        } else { // Swipe Left (Next)
                            if (isWeek) {
                                Log.d("EE", "onFling: swipe left (week)");
                                weekStart = weekStart.plusWeeks(1);
                                populateWeekView(weekStart);
                            } else {
                                Log.d("EE", "onFling: swipe left (day)");
                                dayView = dayView.plusDays(1);
                                populateDayView(dayView);
                            }
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        weekCalendarRoot.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        LinearLayout daysColumnsContainer = weekCalendarRoot.findViewById(R.id.daysColumnsContainer);
        if (daysColumnsContainer != null) {
            daysColumnsContainer.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                return false; // Vraca false da bi HorizontalScrollView i dalje radio scroll
            });
        }

        // Click Listeneri za dugmad
        btnDay.setOnClickListener(v -> switchToDayView());
        btnWeek.setOnClickListener(v -> switchToWeekView());
        btnMonth.setOnClickListener(v -> switchToMonthView());

        // Inicijalni prikaz je Month View, možete promeniti ovde ako želite Week/Day
        switchToMonthView();

        // NOTE: Zakomentarisao sam logiku za TaskCalendarAdapter jer ona duplira
        // logiku koju ste vratili u populateDayView i populateWeekView.
        // Ako TaskCalendarAdapter radi Day/Week prikaz, potrebno je njega koristiti
        // umesto da se logika duplira ovde. Trenutno koristimo logiku iz ovog fragmenta.
        /*
        TaskCalendarAdapter adapter = new TaskCalendarAdapter(getContext(), tasks);
        LinearLayout daysContainer = weekCalendarRoot.findViewById(R.id.daysContainer);
        LinearLayout timeScaleColumn = weekCalendarRoot.findViewById(R.id.timeScaleColumn);
        adapter.setOnDateChangeListener(new TaskCalendarAdapter.OnDateChangeListener() { ... });
        adapter.setupSwipeGestures(daysColumnsContainer, isWeek);
        */

        return root;
    }

    // --- LOGIKA ZA PRIKAZ ---
    private static final int COLOR_SELECTED = Color.parseColor("#424242"); // Tamno siva/Pepeljasta (skoro crna)
    private static final int COLOR_UNSELECTED = Color.parseColor("#A9A9A9"); // Svetlo siva za neselektovanoivate static final int COLOR_UNSELECTED = Color.parseColor("#CCCCCC"); // Svetlo siva za neselektovano

    private void switchToDayView() {
        calendarView.setVisibility(View.GONE);
        weekCalendarRoot.setVisibility(View.VISIBLE);
        populateDayView(dayView);
        isWeek = false;

        // NOVE BOJE ZA DUGMAD
        btnDay.setBackgroundColor(COLOR_SELECTED);
        btnWeek.setBackgroundColor(COLOR_UNSELECTED);
        btnMonth.setBackgroundColor(COLOR_UNSELECTED);
    }

    private void switchToWeekView() {
        calendarView.setVisibility(View.GONE);
        weekCalendarRoot.setVisibility(View.VISIBLE);
        populateWeekView(weekStart);
        isWeek = true;

        // NOVE BOJE ZA DUGMAD
        btnDay.setBackgroundColor(COLOR_UNSELECTED);
        btnWeek.setBackgroundColor(COLOR_SELECTED);
        btnMonth.setBackgroundColor(COLOR_UNSELECTED);
    }

    private void switchToMonthView() {
        calendarView.setVisibility(View.VISIBLE);
        weekCalendarRoot.setVisibility(View.GONE);

        // NOVE BOJE ZA DUGMAD
        btnDay.setBackgroundColor(COLOR_UNSELECTED);
        btnWeek.setBackgroundColor(COLOR_UNSELECTED);
        btnMonth.setBackgroundColor(COLOR_SELECTED);
    }

    // --- POPUNJAVANJE WEEK VIEW-a (Vaša originalna Week logika) ---
    private void populateWeekView(LocalDate startOfWeek) {
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        long startMillis = startOfWeek.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli();

        long endMillis = endOfWeek.atTime(23, 59, 59)
                .atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli();

        loadTasks(startMillis, endMillis);

        LinearLayout daysContainer = weekCalendarRoot.findViewById(R.id.daysContainer);
        LinearLayout daysColumnsContainer = weekCalendarRoot.findViewById(R.id.daysColumnsContainer);
        HorizontalScrollView daysScrollView = root.findViewById(R.id.daysScrollView);

        // Postavljanje gesta za scroll view (kao što je bilo)
        if (daysScrollView != null) {
            daysScrollView.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                return false; // false da i dalje radi normalan scroll
            });
        }


        daysContainer.removeAllViews();
        daysColumnsContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());

        // Računanje širine dana, u Week view je obično jednaka podela
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;

        // Uzimamo u obzir timeScaleColumn
        LinearLayout timeScaleColumn = weekCalendarRoot.findViewById(R.id.timeScaleColumn);
        int timeScaleWidth = timeScaleColumn != null ? timeScaleColumn.getWidth() : 0;
        int dayWidth = (screenWidth - timeScaleWidth) / 7;

        for (int i = 0; i < 7; i++) {
            FrameLayout dayColumn = new FrameLayout(getContext());

            LocalDate date = startOfWeek.plusDays(i);

            // Day Label
            TextView dayLabel = (TextView) inflater.inflate(R.layout.item_day_label, daysContainer, false);
            dayLabel.setText(date.getDayOfWeek().toString().substring(0, 3) + "\n" + date.getDayOfMonth());
            LinearLayout.LayoutParams labelParams =
                    new LinearLayout.LayoutParams(dayWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            dayLabel.setLayoutParams(labelParams);
            daysContainer.addView(dayLabel);

            // Vertical Divider
            if (i < 6) {
                View verticalDivider = new View(getContext());
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                        2, // širina linije
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                verticalDivider.setLayoutParams(dividerParams);
                verticalDivider.setBackgroundColor(Color.parseColor("#FFFFFF"));
                daysContainer.addView(verticalDivider);
            }

            // Day Column (FrameLayout for tasks)
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dayWidth, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.setMargins(2, 2, 2, 2);
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

    // --- POPUNJAVANJE DAY VIEW-a (Vaša originalna Day logika sa boljim prikazom) ---
    private void populateDayView(LocalDate date) {
        loadTasks(date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

        LinearLayout daysContainer = weekCalendarRoot.findViewById(R.id.daysContainer);
        LinearLayout daysColumnsContainer = weekCalendarRoot.findViewById(R.id.daysColumnsContainer);
        LinearLayout timeScaleColumn = weekCalendarRoot.findViewById(R.id.timeScaleColumn);
        HorizontalScrollView daysScrollView = root.findViewById(R.id.daysScrollView);

        if (daysScrollView != null) {
            daysScrollView.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                return false; // false da i dalje radi normalan scroll
            });
        }

        daysContainer.removeAllViews();
        daysColumnsContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());

        // ScrollView za vertikalni scroll (MATCH_PARENT da zauzme dostupan prostor)
        ScrollView verticalScrollView = new ScrollView(getContext());
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        verticalScrollView.setLayoutParams(scrollParams);
        verticalScrollView.setFillViewport(true);

        // FrameLayout za pozicioniranje taskova (ili LinearLayout za listanje, zavisno od potrebe)
        // Vraćamo LinearLayout jer je vaša originalna logika koristila LinearLayout za dodavanje task kartica.
        LinearLayout dayColumnLayout = new LinearLayout(getContext());
        dayColumnLayout.setOrientation(LinearLayout.VERTICAL);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;

        // Širina kolone za taskove je cela širina minus širina timeScaleColumn
        int timeScaleWidth = timeScaleColumn != null ? timeScaleColumn.getWidth() : 0;
        int dayWidth = screenWidth - timeScaleWidth;

        // Ako koristite LinearLayout, visina je WRAP_CONTENT
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dayWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(2, 2, 2, 2);
        dayColumnLayout.setLayoutParams(lp);
        dayColumnLayout.setBackgroundColor(Color.parseColor("#ffffff"));

        verticalScrollView.addView(dayColumnLayout);

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
        if (timeScaleColumn != null) {
            timeScaleColumn.setMinimumHeight(24 * PIXELS_PER_HOUR); // 24h * visina po satu
        }

        // Filtriraj taskove za ovaj dan i sortiraj po startTime
        List<TaskItemDto> dayTasks = new ArrayList<>();
        for (TaskItemDto task : tasks) {
            LocalDate taskDate = Instant.ofEpochMilli(task.getStartTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            if (taskDate.equals(date)) dayTasks.add(task);
        }
        dayTasks.sort((t1, t2) -> Long.compare(t1.getStartTime(), t2.getStartTime()));

        // Dodavanje taskova u Day Column Layout
        // U Day View-u, koristite CardView i LinearLayout da zauzmu više prostora (veći prikaz)
        for (TaskItemDto task : dayTasks) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(task.getStartTime());
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);

            // U Day View-u, pozicija je manje bitna jer koristimo vertikalni scroll
            // Ali možete dodati prazan prostor za "vremensku" poziciju

            // Vaša originalna logika:
            View taskView = LayoutInflater.from(getContext()).inflate(R.layout.item_task, dayColumnLayout, false);

            MaterialCardView card = taskView.findViewById(R.id.cardTask);
            TextView tvTitle = taskView.findViewById(R.id.tvTaskTitle);
            TextView tvDesc = taskView.findViewById(R.id.tvTaskDesc);
            TextView tvTime = taskView.findViewById(R.id.tvTaskTime);

            tvTitle.setText(task.getTitle());
            tvDesc.setText(task.getDescription());
            tvTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));

            card.setCardBackgroundColor(Color.parseColor(task.getColor()));

            // Ovi LayoutParams diktiraju veličinu i margine vaše CardView kartice.
            // Zbog LinearLayout-a, vertikalno se nižu jedan ispod drugog.
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT // Koristite WRAP_CONTENT umesto 400 za dinamičku visinu
            );

            params.setMargins(30, 20, 30, 20); // Margine između kartica

            dayColumnLayout.addView(taskView, params);
        }
    }

    // --- POMOĆNE METODE ---

    private void loadTasks(long startDate, long endDate) {
        List<TaskInstance> instances = taskInstanceService.getTasksByDateRange(startDate, endDate);

        Set<String> templateIds = new HashSet<>();
        for (TaskInstance instance : instances) templateIds.add(instance.getTemplateId());

        Map<String, TaskTemplate> templatesMap = taskTemplateService.getTemplatesByIds(templateIds);

        tasks = new ArrayList<>();
        for (TaskInstance instance : instances) {
            TaskTemplate template = templatesMap.get(instance.getTemplateId());
            String color = categoryService.getColorById(template.getCategoryId());

            // NAPOMENA: Dodao sam template.isRecurring() jer je TaskItemDto uvek primao 6 parametara
            // (Ili se TaskItemDto mora prilagoditi da prima 5)
            tasks.add(new TaskItemDto(
                    instance.getInstanceId(),
                    template.getName(),
                    template.getDescription(),
                    instance.getInstanceDate(),
                    instance.getStatus().toString(),
                    color,
                    template.isRecurring() // Dodato kao pretpostavljeni 6. parametar
            ));
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