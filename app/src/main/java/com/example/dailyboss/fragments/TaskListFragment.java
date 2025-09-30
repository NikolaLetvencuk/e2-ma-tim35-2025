package com.example.dailyboss.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
        adapter = new TaskAdapter(new ArrayList<>());
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
        List<TaskInstance> allInstances = taskInstanceService.getAllTaskInstances();

        List<TaskTemplate> allTemplates = taskTemplateService.getAllTaskTemplates();
        Map<String, TaskTemplate> templatesMap = allTemplates.stream()
                .collect(Collectors.toMap(TaskTemplate::getTemplateId, template -> template));

        allTasks.clear();

        long now = System.currentTimeMillis();

        for (TaskInstance instance : allInstances) {
            TaskTemplate template = templatesMap.get(instance.getTemplateId());
            if (template != null) {
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

        adapter = new TaskAdapter(requireContext(), allTasks, task -> {
            Bundle bundle = new Bundle();
            bundle.putString("TASK_INSTANCE_ID", task.getInstanceId());

            TaskDetailFragment detailFragment = new TaskDetailFragment();
            detailFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });
        rvAllTasks.setAdapter(adapter);
    }

    private void filterTasks(boolean isRepeatingClicked) {
        if (isRepeatingClicked) {
            repeating = !repeating;
        } else {
            oneTime = !oneTime;
        }

        List<TaskItemDto> filteredList = allTasks.stream()
                .filter(task -> {
                    if (task.isRepeating() && repeating) return true;
                    if (!task.isRepeating() && oneTime) return true;
                    return false;
                })
                .collect(Collectors.toList());

        if (repeating && oneTime) {
            filteredList = new ArrayList<>(allTasks);
        }

        adapter = new TaskAdapter(requireContext(), filteredList, task -> {
            Bundle bundle = new Bundle();
            bundle.putString("TASK_INSTANCE_ID", task.getInstanceId());

            TaskDetailFragment detailFragment = new TaskDetailFragment();
            detailFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });
        rvAllTasks.setAdapter(adapter);

        updateButtonsState();
    }

    private void updateButtonsState() {
        int activeTextColor = getResources().getColor(R.color.white);
        int inactiveTextColor = getResources().getColor(R.color.white);

        ColorStateList activeTint = ContextCompat.getColorStateList(getContext(), R.color.calendar_black);

        ColorStateList inactiveTint = ContextCompat.getColorStateList(getContext(), android.R.color.darker_gray);

        if (oneTime) {
            btnOneTimeTasks.setBackgroundTintList(activeTint);
            btnOneTimeTasks.setTextColor(activeTextColor);
        } else {
            btnOneTimeTasks.setBackgroundTintList(inactiveTint);
            btnOneTimeTasks.setTextColor(inactiveTextColor);
        }

        if (repeating) {
            btnRepeatingTasks.setBackgroundTintList(activeTint);
            btnRepeatingTasks.setTextColor(activeTextColor);
        } else {
            btnRepeatingTasks.setBackgroundTintList(inactiveTint);
            btnRepeatingTasks.setTextColor(inactiveTextColor);
        }
    }
}