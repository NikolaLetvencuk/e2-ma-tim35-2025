package com.example.dailyboss.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.adapters.TaskAdapter;
import com.example.dailyboss.dto.TaskItemDto;
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

        // Početno učitavanje jednokratnih zadataka
        filterTasks(false);

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
        for (TaskInstance instance : allInstances) {
            TaskTemplate template = templatesMap.get(instance.getTemplateId());
            if (template != null) {
                String color = categoryService.getColorById(template.getCategoryId());
                TaskItemDto dto = new TaskItemDto(
                        template.getName(),
                        template.getDescription(),
                        instance.getInstanceDate(),
                        instance.getStatus().toString(),
                        color,
                        template.isRecurring()
                );
                // Dodavanje informacije o tome da li je task ponavljajući
                allTasks.add(dto);
            }
        }
    }

    private void filterTasks(boolean isRepeating) {
        List<TaskItemDto> filteredList = allTasks.stream()
                .filter(task -> task.isRepeating() == isRepeating)
                .collect(Collectors.toList());

        adapter = new TaskAdapter(filteredList, task -> {
            Toast.makeText(getContext(), "Task Clicked: " + task.getTitle(), Toast.LENGTH_SHORT).show();
            // Implementirati otvaranje detalja zadatka, ako je potrebno
        });
        rvAllTasks.setAdapter(adapter);
    }
}