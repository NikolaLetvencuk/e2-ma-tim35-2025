// CreateTaskFragment.java
package com.example.dailyboss.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.example.dailyboss.dto.TaskDetailDto;
import com.example.dailyboss.enums.FrequencyUnit;
import com.example.dailyboss.enums.TaskDifficulty;
import com.example.dailyboss.enums.TaskImportance;
import com.example.dailyboss.model.Category;
import com.example.dailyboss.model.TaskTemplate;
import com.example.dailyboss.service.CategoryService;
import com.example.dailyboss.service.TaskTemplateService;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dailyboss.R;

public class CreateTaskFragment extends Fragment {

    private EditText etStartDate, etEndDate, etStartTime, etRepeatInterval;
    private RadioGroup etRepeatUnit;
    private Calendar calendar;
    private Spinner spinnerCategory;
    private CategoryService categoryService;
    private TaskTemplateService taskTemplateService;

    private static final String ARG_TASK_TEMPLATE = "task_template";
    private TaskDetailDto taskToEdit = null;

    public static CreateTaskFragment newInstance(TaskDetailDto taskDetailDto) {
        CreateTaskFragment fragment = new CreateTaskFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TASK_TEMPLATE, taskDetailDto);
        fragment.setArguments(args);
        return fragment;
    }

    public static CreateTaskFragment newInstance() {
        return new CreateTaskFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        categoryService = new CategoryService(getContext());
        taskTemplateService = new TaskTemplateService(getContext());

        if (getArguments() != null && getArguments().containsKey(ARG_TASK_TEMPLATE)) {
            taskToEdit = (TaskDetailDto) getArguments().getSerializable(ARG_TASK_TEMPLATE);
        }
        return inflater.inflate(R.layout.fragment_create_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        populateCategorySpinner();

        EditText etTaskTitle = view.findViewById(R.id.editTextTitle);
        EditText etTaskDescription = view.findViewById(R.id.editTextDescription);
        Button btnCreateTask = view.findViewById(R.id.btnCreateTask);
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        etStartDate = view.findViewById(R.id.editTextStartDate);
        etEndDate = view.findViewById(R.id.editTextEndDate);
        etStartTime = view.findViewById(R.id.editTextStartTime);
        CheckBox checkBoxIsRepeated = view.findViewById(R.id.checkBoxIsRepeated);
        LinearLayout layoutRepeatOptions = view.findViewById(R.id.layoutRepeatOptions);
        etRepeatInterval = view.findViewById(R.id.editTextRepeatInterval);
        etRepeatUnit = view.findViewById(R.id.radioGroupRepeatUnit);
        RadioGroup radioGroupDifficulty = view.findViewById(R.id.radioGroupDifficulty);
        RadioGroup radioGroupImportance = view.findViewById(R.id.radioGroupImportance);

        calendar = Calendar.getInstance();

       if (taskToEdit != null) {
            toolbar.setTitle("Izmeni Zadatak");
            btnCreateTask.setText("Sačuvaj izmene");

            etTaskTitle.setText(taskToEdit.getName());
            etTaskDescription.setText(taskToEdit.getDescription());
            etStartTime.setText(taskToEdit.getExecutionTime());

            String startDateStr = formatDate(taskToEdit.getStartDate());
            etStartDate.setText(startDateStr);

            checkBoxIsRepeated.setChecked(taskToEdit.isRecurring());
            if (taskToEdit.isRecurring()) {
                layoutRepeatOptions.setVisibility(View.VISIBLE);

                if (taskToEdit.getEndDate() > 0) {
                    String endDateStr = formatDate(taskToEdit.getEndDate());
                    etEndDate.setText(endDateStr);
                }

                etRepeatInterval.setText(String.valueOf(taskToEdit.getFrequencyInterval()));

                if (taskToEdit.getFrequencyUnit() == FrequencyUnit.WEEK) {
                    ((RadioButton) view.findViewById(R.id.radioWeek)).setChecked(true);
                } else {
                    ((RadioButton) view.findViewById(R.id.radioDay)).setChecked(true);
                }
            }

            setRadioGroupSelection(radioGroupDifficulty, taskToEdit.getDifficulty());
            setRadioGroupSelection(radioGroupImportance, taskToEdit.getImportance());
            setSelectedCategory(taskToEdit.getCategoryId());

            spinnerCategory.setEnabled(false);
            spinnerCategory.setClickable(false);

            etStartDate.setEnabled(false);
            etEndDate.setEnabled(false);

            checkBoxIsRepeated.setEnabled(false);

            if (taskToEdit.isRecurring()) {
                etRepeatInterval.setEnabled(false);
                for (int i = 0; i < etRepeatUnit.getChildCount(); i++) {
                    etRepeatUnit.getChildAt(i).setEnabled(false);
                }
            }
        } else {
            toolbar.setTitle("Novi Zadatak");
            btnCreateTask.setText("Kreiraj Zadatak");
        }

        etStartDate.setOnClickListener(v -> {
            Calendar maxDate = null;
            if (etEndDate.getText().length() > 0) {
                maxDate = getDateFromString(etEndDate.getText().toString());
            }
            showDatePicker(etStartDate, null, maxDate);
        });

        etEndDate.setOnClickListener(v -> {
            Calendar minDate = null;
            if (etStartDate.getText().length() > 0) {
                minDate = getDateFromString(etStartDate.getText().toString());
            }
            showDatePicker(etEndDate, minDate, null);
        });

        etStartTime.setOnClickListener(v -> showTimePicker());

        checkBoxIsRepeated.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layoutRepeatOptions.setVisibility(View.VISIBLE);
            } else {
                layoutRepeatOptions.setVisibility(View.GONE);
            }
        });

        toolbar.setNavigationOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        btnCreateTask.setOnClickListener(v -> {
            Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
            String title = etTaskTitle.getText().toString();
            String description = etTaskDescription.getText().toString();
            String executionTime = etStartTime.getText().toString();

            int frequencyInterval = 1;
            try {
                frequencyInterval = Integer.parseInt(etRepeatInterval.getText().toString());
            } catch (NumberFormatException e) {
                frequencyInterval = 1;
            }

            FrequencyUnit frequencyUnit = getSelectedFrequencyUnit(etRepeatUnit);

            Calendar startDateCalendar = getDateFromString(etStartDate.getText().toString());
            long startDateMillis = startDateCalendar != null ? startDateCalendar.getTimeInMillis() : 0;

            long endDateMillis = 0;
            if (!etEndDate.getText().toString().isEmpty()) {
                Calendar endDateCalendar = getDateFromString(etEndDate.getText().toString());
                if (endDateCalendar != null) {
                    endDateMillis = endDateCalendar.getTimeInMillis();
                }
            }

            TaskDifficulty difficulty = getSelectedDifficulty(radioGroupDifficulty);
            TaskImportance importance = getSelectedImportance(radioGroupImportance);

            boolean success;

            if (taskToEdit != null) {
                success = taskTemplateService.updateTaskTemplate(
                        taskToEdit.getTemplateId(),
                        selectedCategory.getId(),
                        title,
                        description,
                        executionTime,
                        frequencyInterval,
                        frequencyUnit,
                        startDateMillis,
                        endDateMillis,
                        difficulty,
                        importance,
                        checkBoxIsRepeated.isChecked()
                );

                if (success) {
                    Log.d("TaskTemplate", "TaskTemplate ažuriran uspešno!");
                } else {
                    Log.e("TaskTemplate", "Greška pri ažuriranju TaskTemplate-a");
                }

            } else {
                success = taskTemplateService.addTaskTemplate(
                        selectedCategory.getId(),
                        title,
                        description,
                        executionTime,
                        frequencyInterval,
                        frequencyUnit,
                        startDateMillis,
                        endDateMillis,
                        difficulty,
                        importance,
                        checkBoxIsRepeated.isChecked()
                );

                if (success) {
                    Log.d("TaskTemplate", "TaskTemplate i TaskInstance kreirani uspešno!");
                } else {
                    Log.e("TaskTemplate", "Greška pri kreiranju TaskTemplate-a");
                }
            }

            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }

    private TaskDifficulty getSelectedDifficulty(RadioGroup radioGroupDifficulty) {
        int selectedDifficultyId = radioGroupDifficulty.getCheckedRadioButtonId();
        TaskDifficulty difficulty = TaskDifficulty.EASY; // default

        if (selectedDifficultyId != -1) {
            if (selectedDifficultyId == R.id.radioDifficultyVeryEasy) difficulty = TaskDifficulty.VERY_EASY;
            else if (selectedDifficultyId == R.id.radioDifficultyEasy) difficulty = TaskDifficulty.EASY;
            else if (selectedDifficultyId == R.id.radioDifficultyHard) difficulty = TaskDifficulty.HARD;
            else if (selectedDifficultyId == R.id.radioDifficultyExtreme) difficulty = TaskDifficulty.EXTREME;
        }
        return difficulty;
    }

    private TaskImportance getSelectedImportance(RadioGroup radioGroupImportance) {
        int selectedImportanceId = radioGroupImportance.getCheckedRadioButtonId();
        TaskImportance importance = TaskImportance.NORMAL; // default

        if (selectedImportanceId != -1) {
            if (selectedImportanceId == R.id.radioImportanceNormal) importance = TaskImportance.NORMAL;
            else if (selectedImportanceId == R.id.radioImportanceImportant) importance = TaskImportance.IMPORTANT;
            else if (selectedImportanceId == R.id.radioImportanceExtreme) importance = TaskImportance.EXTREMELY_IMPORTANT;
            else if (selectedImportanceId == R.id.radioImportanceSpecial) importance = TaskImportance.SPECIAL;
        }
        return importance;
    }

    private FrequencyUnit getSelectedFrequencyUnit(RadioGroup etRepeatUnit) {
        int selectedUnitId = etRepeatUnit.getCheckedRadioButtonId();
        if (selectedUnitId != -1 && selectedUnitId == R.id.radioWeek) {
            return FrequencyUnit.WEEK;
        }
        return FrequencyUnit.DAY;
    }

    private String formatDate(long millis) {
        if (millis <= 0) return "";
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return String.format(Locale.getDefault(), "%02d/%02d/%d",
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR));
    }

    private void setRadioGroupSelection(RadioGroup group, Enum<?> value) {
        int idToSelect = -1;
        String enumName = value.name();

        if (group.getId() == R.id.radioGroupDifficulty) {
            if (enumName.equals(TaskDifficulty.VERY_EASY.name())) idToSelect = R.id.radioDifficultyVeryEasy;
            else if (enumName.equals(TaskDifficulty.EASY.name())) idToSelect = R.id.radioDifficultyEasy;
            else if (enumName.equals(TaskDifficulty.HARD.name())) idToSelect = R.id.radioDifficultyHard;
            else if (enumName.equals(TaskDifficulty.EXTREME.name())) idToSelect = R.id.radioDifficultyExtreme;
        } else if (group.getId() == R.id.radioGroupImportance) {
            if (enumName.equals(TaskImportance.NORMAL.name())) idToSelect = R.id.radioImportanceNormal;
            else if (enumName.equals(TaskImportance.IMPORTANT.name())) idToSelect = R.id.radioImportanceImportant;
            else if (enumName.equals(TaskImportance.EXTREMELY_IMPORTANT.name())) idToSelect = R.id.radioImportanceExtreme; // Proverite da li je ID radioImportanceExtreme
            else if (enumName.equals(TaskImportance.SPECIAL.name())) idToSelect = R.id.radioImportanceSpecial;
        }

        if (idToSelect != -1) {
            group.check(idToSelect);
        }
    }

    private void setSelectedCategory(String categoryId) {
        ArrayAdapter<Category> adapter = (ArrayAdapter<Category>) spinnerCategory.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                Category category = adapter.getItem(i);
                if (category != null && category.getId().equals(categoryId)) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }
    }
    private Calendar getDateFromString(String dateString) {
        try {
            String[] dateParts = dateString.split("/");
            int day = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1;
            int year = Integer.parseInt(dateParts[2]);

            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.set(year, month, day);
            return dateCalendar;
        } catch (Exception e) {
            return null;
        }
    }

    private void populateCategorySpinner() {
        List<Category> categories = categoryService.getAllCategories();

        ArrayAdapter<Category> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCategory.setAdapter(adapter);
    }

    private void showDatePicker(final EditText editText, @Nullable Calendar minDate, @Nullable Calendar maxDate) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    editText.setText(selectedDate);
                }, year, month, day);

        if (minDate != null) {
            datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        }

        if (maxDate != null) {
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        }

        datePickerDialog.show();
    }

    private void showTimePicker() {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, selectedHour, selectedMinute) -> {
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    etStartTime.setText(selectedTime);
                }, hour, minute, true);

        timePickerDialog.show();
    }
}