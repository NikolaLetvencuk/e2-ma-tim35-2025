// TasksFragment.java
package com.example.dailyboss.fragments;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.adapters.TaskAdapter;
import com.example.dailyboss.dto.TaskItemDto;
import com.example.dailyboss.model.TaskInstance;
import com.example.dailyboss.model.TaskTemplate;
import com.example.dailyboss.service.TaskInstanceService;
import com.example.dailyboss.service.TaskTemplateService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TasksFragment extends Fragment {

    private TaskInstanceService taskInstanceService;
    private TaskTemplateService taskTemplateService;
    private TaskAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_task, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rvTasks1);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.category_item_spacing);
        int extraBottomMargin = 100;

        Button btnNewTask = view.findViewById(R.id.btnNewTask);
        btnNewTask.setOnClickListener(v -> {
            Log.d("TasksFragment", "Kliknuto na Add New Task dugme!");
            Toast.makeText(getContext(), "Button clicked!", Toast.LENGTH_SHORT).show();

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CreateTaskFragment())
                    .addToBackStack(null) // Dodajemo u back stack za navigaciju nazad
                    .commit();
        });


        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                int itemCount = parent.getAdapter().getItemCount();
                int spanCount = layoutManager.getSpanCount();

                outRect.set(spacingInPixels, spacingInPixels, spacingInPixels, spacingInPixels);
                outRect.bottom = extraBottomMargin;
            }
        });

        taskTemplateService = new TaskTemplateService(getContext());
        taskInstanceService = new TaskInstanceService(getContext());

        long today = System.currentTimeMillis();
        List<TaskInstance> todayInstances = taskInstanceService.getTasksByDateRange(today, today);

        Set<String> templateIds = new HashSet<>();
        for (TaskInstance instance : todayInstances) {
            templateIds.add(instance.getTemplateId());
        }

        Map<String, TaskTemplate> templatesMap = taskTemplateService.getTemplatesByIds(templateIds);

        List<TaskItemDto> tasks = new ArrayList<>();
        for (TaskInstance instance : todayInstances) {
            TaskTemplate template = templatesMap.get(instance.getTemplateId());
            TaskItemDto dto = new TaskItemDto(
                    template.getName(),
                    template.getDescription(),
                    instance.getInstanceDate(),
                    instance.getStatus().toString()
            );
            tasks.add(dto);
        }

        adapter = new TaskAdapter(tasks);
        recyclerView.setAdapter(adapter);

        return view;
    }
}