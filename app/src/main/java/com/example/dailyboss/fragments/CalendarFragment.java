package com.example.dailyboss.fragments;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;

    private TaskInstanceService taskInstanceService;
    private TaskTemplateService taskTemplateService;
    private CategoryService categoryService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        calendarView = view.findViewById(R.id.calendarView);

        // Klik na datum
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clickedDayCalendar = eventDay.getCalendar();
            int year = clickedDayCalendar.get(Calendar.YEAR);
            int month = clickedDayCalendar.get(Calendar.MONTH) + 1;
            int day = clickedDayCalendar.get(Calendar.DAY_OF_MONTH);
            System.out.println("Izabrani datum: " + day + "." + month + "." + year);
        });

        List<EventDay> events = new ArrayList<>();

        taskTemplateService = new TaskTemplateService(getContext());
        taskInstanceService = new TaskInstanceService(getContext());
        categoryService = new CategoryService(getContext());

        long today = System.currentTimeMillis();
        long thirtyDaysLater = today + 30L * 24 * 60 * 60 * 1000;
        List<TaskInstance> todayInstances = taskInstanceService.getTasksByDateRange(today, thirtyDaysLater);

        Set<String> templateIds = new HashSet<>();
        for (TaskInstance instance : todayInstances) {
            templateIds.add(instance.getTemplateId());
        }

        Map<String, TaskTemplate> templatesMap = taskTemplateService.getTemplatesByIds(templateIds);

        List<TaskItemDto> tasks = new ArrayList<>();
        for (TaskInstance instance : todayInstances) {
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

        // Primer: Map<String, List<TaskItemDto>> gde key = "2025-08-19"
        Map<Long, List<TaskItemDto>> tasksByDay = new HashMap<>();

        for (TaskItemDto task : tasks) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(task.getStartTime());

            // resetuj vreme da bude samo dan-mesec-godina
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            long dayKey = cal.getTimeInMillis();

            if (!tasksByDay.containsKey(dayKey)) {
                tasksByDay.put(dayKey, new ArrayList<>());
            }
            tasksByDay.get(dayKey).add(task);
        }

        for (Map.Entry<Long, List<TaskItemDto>> entry : tasksByDay.entrySet()) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(entry.getKey());

            // Kreiraj custom drawable za sve taskove tog dana
            DayTasksDrawable drawable = new DayTasksDrawable(entry.getValue());

            // Dodaj EventDay u kalendar
            events.add(new EventDay(cal, drawable));
        }


        calendarView.setEvents(events);



        return view;
    }
}
