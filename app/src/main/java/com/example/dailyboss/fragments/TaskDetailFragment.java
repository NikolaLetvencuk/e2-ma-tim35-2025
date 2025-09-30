package com.example.dailyboss.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.dailyboss.R;
import com.example.dailyboss.data.TaskInstanceDao;
import com.example.dailyboss.dto.TaskDetailDto;
import com.example.dailyboss.enums.TaskDifficulty;
import com.example.dailyboss.enums.TaskImportance;
import com.example.dailyboss.enums.TaskStatus;

public class TaskDetailFragment extends Fragment {

    private TextView tvName, tvDescription, tvCategory, tvTime, tvXP, tvFrequency, tvDifficulty, tvImportance, tvStatus;
    private TaskInstanceDao dao;
    private Button btnDone, btnPaused, btnCancelled, btnEdit, btnDelete;

    public TaskDetailFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_detail, container, false);

        tvName = view.findViewById(R.id.tvDetailTitle);
        tvDescription = view.findViewById(R.id.tvDetailDescription);
        tvCategory = view.findViewById(R.id.tvDetailCategory);
        tvTime = view.findViewById(R.id.tvDetailExecutionTime);
        tvXP = view.findViewById(R.id.tvDetailValue);
        tvFrequency = view.findViewById(R.id.tvDetailRecurrence);
        tvDifficulty = view.findViewById(R.id.tvDetailDifficulty);
        tvImportance = view.findViewById(R.id.tvDetailImportance);
        tvStatus = view.findViewById(R.id.tvDetailStatus);

        btnDone = view.findViewById(R.id.btnMarkDone);
        btnPaused = view.findViewById(R.id.btnMarkPaused);
        btnCancelled = view.findViewById(R.id.btnMarkCancelled);
        btnEdit = view.findViewById(R.id.btnEditTask);
        btnDelete = view.findViewById(R.id.btnDeleteTask);

        dao = new TaskInstanceDao(requireContext());

        if (getArguments() != null) {
            String taskId = getArguments().getString("TASK_INSTANCE_ID");
            TaskDetailDto taskDetail = dao.findTaskDetailById(taskId);

            if (taskDetail != null) {
                tvName.setText(taskDetail.getName());
                tvStatus.setText(formatEnum(taskDetail.getStatus().toString()));
                tvDescription.setText(taskDetail.getDescription());
                tvCategory.setText(taskDetail.getCategoryName());
                tvTime.setText(taskDetail.getExecutionTime());
                tvDifficulty.setText(formatEnum(taskDetail.getDifficulty().toString()));
                tvImportance.setText(formatEnum(taskDetail.getImportance().toString()));

                int xp = calculateXP(taskDetail.getDifficulty(), taskDetail.getImportance());
                tvXP.setText("" + xp);

                if (taskDetail.isRecurring()) {
                    tvFrequency.setText("Repeats every " + taskDetail.getFrequencyInterval() + " " + formatEnum(taskDetail.getFrequencyUnit().toString()));
                } else {
                    tvFrequency.setText("One-time task");
                }
            }

            applyButtonStates(taskDetail);

            btnDone.setOnClickListener(v -> updateTaskStatus(taskDetail, TaskStatus.DONE));
            btnPaused.setOnClickListener(v -> togglePauseStatus(taskDetail));
            btnCancelled.setOnClickListener(v -> updateTaskStatus(taskDetail, TaskStatus.CANCELLED));

            btnEdit.setOnClickListener(v -> openEditTaskFragment(taskDetail));
            btnDelete.setOnClickListener(v -> deleteTask(taskDetail));
        }
        return view;
    }

    private int calculateXP(TaskDifficulty difficulty, TaskImportance importance) {
        int diffXP = 0;
        switch (difficulty) {
            case VERY_EASY:
                diffXP = 1;
                break;
            case EASY:
                diffXP = 3;
                break;
            case HARD:
                diffXP = 7;
                break;
            case EXTREME:
                diffXP = 20;
                break;
        }

        int impXP = 0;
        switch (importance) {
            case NORMAL:
                impXP = 1;
                break;
            case IMPORTANT:
                impXP = 3;
                break;
            case EXTREMELY_IMPORTANT:
                impXP = 10;
                break;
            case SPECIAL:
                impXP = 100;
                break;
        }

        return diffXP + impXP;
    }

    private String formatEnum(String enumValue) {
        String formatted = enumValue.toLowerCase().replace("_", " ");
        return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
    }

    private void updateTaskStatus(TaskDetailDto task, TaskStatus newStatus) {
        task.setStatus(newStatus);
        dao.updateTaskStatus(task.getInstanceId(), newStatus);
        tvStatus.setText(formatEnum(newStatus.toString()));
        applyButtonStates(task);
    }

    private void togglePauseStatus(TaskDetailDto task) {
        TaskStatus currentStatus = task.getStatus();
        TaskStatus newStatus;
        String newButtonText;
        int newButtonColorResId;

        if (currentStatus == TaskStatus.PAUSED) {

            newStatus = TaskStatus.ACTIVE;
            newButtonText = "Pause";
            newButtonColorResId = R.color.yellow2;
        } else {
            newStatus = TaskStatus.PAUSED;
            newButtonText = "Active";
            newButtonColorResId = R.color.green;
        }

        updateTaskStatus(task, newStatus);

        btnPaused.setText(newButtonText);

        int color = ContextCompat.getColor(requireContext(), newButtonColorResId);
        btnPaused.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private void openEditTaskFragment(TaskDetailDto taskDetailDto) {

        CreateTaskFragment editFragment = CreateTaskFragment.newInstance(taskDetailDto);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, editFragment)
                .addToBackStack(null)
                .commit();
    }

    private void deleteTask(TaskDetailDto task) {

        boolean isRecurring = task.isRecurring();
        String templateId = task.getTemplateId();
        String instanceId = task.getInstanceId();
        long instanceDate = task.getInstanceDate();

        if (isRecurring) {
            dao.deleteFutureInstancesFromDate(templateId, instanceDate);
        } else {
            dao.deleteById(instanceId);
        }

        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void applyButtonStates(TaskDetailDto taskDetail) {
        TaskStatus status = taskDetail.getStatus();
        boolean isRecurring = taskDetail.isRecurring();

        long currentTime = System.currentTimeMillis();
        boolean isPast = taskDetail.getInstanceDate() < currentTime;

        boolean isEditDisabled = (status == TaskStatus.DONE || status == TaskStatus.CANCELLED || status == TaskStatus.UNDONE || isPast);

        boolean isDeleteDisabled = (status == TaskStatus.DONE || status == TaskStatus.CANCELLED || status == TaskStatus.UNDONE);

        boolean isStatusActionDisabled = (status == TaskStatus.DONE || status == TaskStatus.CANCELLED || status == TaskStatus.UNDONE);

        boolean isPausedDisabled = !isRecurring || isStatusActionDisabled;

        if (status == TaskStatus.PAUSED) {
            btnPaused.setText("Active");
            btnPaused.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.green)
            ));
            setButtonState(btnDone, true);
            setButtonState(btnCancelled, true);
            setButtonState(btnPaused, isPausedDisabled);
        } else {
            btnPaused.setText("Pause");
            btnPaused.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.yellow2)
            ));
            setButtonState(btnDone, isStatusActionDisabled);
            setButtonState(btnCancelled, isStatusActionDisabled);
            setButtonState(btnPaused, isPausedDisabled || isStatusActionDisabled);
        }

        int disabledColor = ContextCompat.getColor(requireContext(), R.color.disabled_text_grey);
        int enabledColor = ContextCompat.getColor(requireContext(), R.color.calendar_black);
        int whiteColor = ContextCompat.getColor(requireContext(), android.R.color.white);

        setButtonStateWithColors(btnEdit, isEditDisabled, enabledColor, disabledColor, whiteColor);
        setButtonStateWithColors(btnDelete, isDeleteDisabled, enabledColor, disabledColor, whiteColor);
    }

    private void setButtonState(Button button, boolean isDisabled) {
        button.setEnabled(!isDisabled);
        button.setAlpha(isDisabled ? 0.5f : 1.0f);
    }

    private void setButtonStateWithColors(Button button, boolean isDisabled, int enabledBgColor, int disabledBgColor, int enabledTextColor) {
        if (isDisabled) {
            button.setEnabled(false);
            button.setBackgroundTintList(ColorStateList.valueOf(disabledBgColor));
            button.setTextColor(ColorStateList.valueOf(enabledBgColor));
        } else {
            button.setEnabled(true);
            if (button != btnPaused) {
                button.setBackgroundTintList(ColorStateList.valueOf(enabledBgColor));
            }
            button.setTextColor(ColorStateList.valueOf(enabledTextColor));
        }
    }
}