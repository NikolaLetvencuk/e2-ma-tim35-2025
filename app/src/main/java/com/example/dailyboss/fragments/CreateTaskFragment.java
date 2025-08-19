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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        categoryService = new CategoryService(getContext());
        taskTemplateService = new TaskTemplateService(getContext());
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

            int selectedUnitId = etRepeatUnit.getCheckedRadioButtonId();
            FrequencyUnit frequencyUnit = FrequencyUnit.DAY; // default
            if (selectedUnitId != -1) {
                RadioButton rb = etRepeatUnit.findViewById(selectedUnitId);
                String text = rb.getText().toString().toLowerCase();
                if (text.contains("day")) frequencyUnit = FrequencyUnit.DAY;
                else if (text.contains("week")) frequencyUnit = FrequencyUnit.WEEK;
            }

            Calendar startDateCalendar = getDateFromString(etStartDate.getText().toString());
            long startDateMillis = startDateCalendar != null ? startDateCalendar.getTimeInMillis() : 0;

            long endDateMillis = startDateMillis;
            if (!etEndDate.getText().toString().isEmpty()) {
                Calendar endDateCalendar = getDateFromString(etEndDate.getText().toString());
                if (endDateCalendar != null) {
                    endDateMillis = endDateCalendar.getTimeInMillis();
                }
            }

            int selectedDifficultyId = radioGroupDifficulty.getCheckedRadioButtonId();
            TaskDifficulty difficulty = TaskDifficulty.EASY;
            if (selectedDifficultyId != -1) {
                RadioButton rb = radioGroupDifficulty.findViewById(selectedDifficultyId);
                String text = rb.getText().toString().toLowerCase();
                if (text.contains("veoma")) difficulty = TaskDifficulty.VERY_EASY;
                else if (text.contains("lak")) difficulty = TaskDifficulty.EASY;
                else if (text.contains("težak") && !text.contains("ekstremno")) difficulty = TaskDifficulty.HARD;
                else if (text.contains("ekstremno")) difficulty = TaskDifficulty.EXTREME;
            }

            int selectedImportanceId = radioGroupImportance.getCheckedRadioButtonId();
            TaskImportance importance = TaskImportance.NORMAL;
            if (selectedImportanceId != -1) {
                RadioButton rb = radioGroupImportance.findViewById(selectedImportanceId);
                String text = rb.getText().toString().toLowerCase();
                if (text.contains("normalan")) importance = TaskImportance.NORMAL;
                else if (text.contains("važan")) importance = TaskImportance.IMPORTANT;
                else if (text.contains("ekstremno")) importance = TaskImportance.EXTREMELY_IMPORTANT;
                else if (text.contains("specijalan")) importance = TaskImportance.SPECIAL;
            }

            boolean success = taskTemplateService.addTaskTemplate(
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
        });
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