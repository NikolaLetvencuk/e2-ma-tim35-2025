package com.example.dailyboss.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.adapters.TaskAdapter;
import com.example.dailyboss.dto.TaskItemDto;
import com.example.dailyboss.enums.TaskStatus;
import com.example.dailyboss.model.TaskInstance;
import com.example.dailyboss.model.TaskTemplate;
import com.example.dailyboss.service.CategoryService;
import com.example.dailyboss.service.TaskInstanceService;
import com.example.dailyboss.service.TaskTemplateService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskListFragment extends Fragment {

    private RecyclerView rvAllTasks;
    private TaskAdapter adapter;
    private Button btnOneTimeTasks;
    private Button btnRepeatingTasks;

    private TaskInstanceService taskInstanceService;
    private TaskTemplateService taskTemplateService;
    private CategoryService categoryService;

    private boolean oneTime = true;
    private boolean repeating = true;

    private List<TaskItemDto> allTasks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        rvAllTasks = view.findViewById(R.id.rvAllTasks);
        btnOneTimeTasks = view.findViewById(R.id.btnOneTimeTasks);
        btnRepeatingTasks = view.findViewById(R.id.btnRepeatingTasks);

        rvAllTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskAdapter(new ArrayList<>()); // Inicijalizacija sa praznom listom
        rvAllTasks.setAdapter(adapter);

        taskTemplateService = new TaskTemplateService(getContext());
        taskInstanceService = new TaskInstanceService(getContext());
        categoryService = new CategoryService(getContext());

        loadAllTasks();

        btnOneTimeTasks.setOnClickListener(v -> filterTasks(false));
        btnRepeatingTasks.setOnClickListener(v -> filterTasks(true));

        return view;
    }

    private void loadAllTasks() {
        // Učitavanje svih task instanci
        List<TaskInstance> allInstances = taskInstanceService.getAllTaskInstances();

        // Učitavanje svih template-a (za naziv, opis, i categoryId)
        List<TaskTemplate> allTemplates = taskTemplateService.getAllTaskTemplates();
        Map<String, TaskTemplate> templatesMap = allTemplates.stream()
                .collect(Collectors.toMap(TaskTemplate::getTemplateId, template -> template));

        allTasks.clear();

        long now = System.currentTimeMillis(); // trenutni timestamp

        for (TaskInstance instance : allInstances) {
            TaskTemplate template = templatesMap.get(instance.getTemplateId());
            if (template != null) {
                // Provera: datum taska je sada ili u budućnosti, i status je aktivan
                if (instance.getInstanceDate() >= now || instance.getStatus() == TaskStatus.ACTIVE) {

                    String color = categoryService.getColorById(template.getCategoryId());
                    TaskItemDto dto = new TaskItemDto(
                            instance.getInstanceId(),
                            template.getName(),
                            template.getDescription(),
                            instance.getInstanceDate(),
                            instance.getStatus().toString(),
                            color,
                            template.isRecurring()
                    );
                    allTasks.add(dto);
                }
            }
        }

        adapter = new TaskAdapter(this.getContext(), allTasks, task -> {
            Toast.makeText(getContext(), "Task Clicked: " + task.getTitle(), Toast.LENGTH_SHORT).show();
        });
        rvAllTasks.setAdapter(adapter);
    }

    private void filterTasks(boolean isRepeatingClicked) {
        // Toggle stanje
        if (isRepeatingClicked) {
            repeating = !repeating;
        } else {
            oneTime = !oneTime;
        }

        // Filtriraj na osnovu trenutnog stanja
        List<TaskItemDto> filteredList = allTasks.stream()
                .filter(task -> {
                    if (task.isRepeating() && repeating) return true;
                    if (!task.isRepeating() && oneTime) return true;
                    return false;
                })
                .collect(Collectors.toList());

        // Ako su oba true, prikazi sve
        if (repeating && oneTime) {
            filteredList = new ArrayList<>(allTasks);
        }

        adapter = new TaskAdapter(this.getContext(), filteredList, task -> {
            Toast.makeText(getContext(), "Task Clicked: " + task.getTitle(), Toast.LENGTH_SHORT).show();
        });
        rvAllTasks.setAdapter(adapter);

        updateButtonsState();
    }

    private void updateButtonsState() {
        // Direktno koristi R.color umesto getColor()
        int activeTextColor = getResources().getColor(R.color.white);
        int inactiveTextColor = getResources().getColor(R.color.white);

        // Aktivna boja (npr. crna pozadina)
        ColorStateList activeTint = ContextCompat.getColorStateList(getContext(), R.color.calendar_black);

        // Neaktivna boja (npr. siva pozadina)
        ColorStateList inactiveTint = ContextCompat.getColorStateList(getContext(), android.R.color.darker_gray);

        // One-Time dugme
        if (oneTime) {
            btnOneTimeTasks.setBackgroundTintList(activeTint);
            btnOneTimeTasks.setTextColor(activeTextColor);
        } else {
            btnOneTimeTasks.setBackgroundTintList(inactiveTint);
            btnOneTimeTasks.setTextColor(inactiveTextColor);
        }

        // Repeating dugme
        if (repeating) {
            btnRepeatingTasks.setBackgroundTintList(activeTint);
            btnRepeatingTasks.setTextColor(activeTextColor);
        } else {
            btnRepeatingTasks.setBackgroundTintList(inactiveTint);
            btnRepeatingTasks.setTextColor(inactiveTextColor);
        }
    }
}