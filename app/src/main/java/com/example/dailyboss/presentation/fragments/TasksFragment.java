package com.example.dailyboss.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dailyboss.R;

public class TasksFragment extends Fragment {

    private boolean isCalendarView = false;

    private Fragment calendarFragment;
    private Fragment listFragment;

    private Button btnCalendarView;
    private Button btnListView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_task, container, false);

        calendarFragment = new CalendarFragment();
        listFragment = new TaskListFragment();


        btnCalendarView = view.findViewById(R.id.btnCalendarView);
        btnListView = view.findViewById(R.id.btnListView);

        if (savedInstanceState == null) {
            loadFragment(listFragment);
            updateButtonState(false);
        }

        btnCalendarView.setOnClickListener(v -> {
            loadFragment(calendarFragment);
            updateButtonState(true);
        });

        btnListView.setOnClickListener(v -> {
            loadFragment(listFragment);
            updateButtonState(false);
        });


        Button btnNewTask = view.findViewById(R.id.btnNewTask);

        if (btnNewTask != null) {
            btnNewTask.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new CreateTaskFragment())
                        .addToBackStack(null) // Dodaje transakciju na back stack
                        .commit();
            });
        }
        updateButtonState(false);


        return view;
    }

    private void loadFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        if (fragment instanceof CalendarFragment) {
            Toast.makeText(getContext(), "Prikaz Kalendara", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Prikaz Liste", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateButtonState(boolean isCalendarActive) {
        int activeColor = getResources().getColor(R.color.calendar_black);
        int inactiveColor = getResources().getColor(android.R.color.darker_gray);

        if (isCalendarActive) {
            btnCalendarView.setTextColor(activeColor);
            btnListView.setTextColor(inactiveColor);

            btnCalendarView.setBackgroundResource(R.drawable.bottom_border_active_ripple);
            btnListView.setBackgroundResource(R.drawable.bottom_border_inactive_ripple);

        } else {
            btnCalendarView.setTextColor(inactiveColor);
            btnListView.setTextColor(activeColor);

            btnCalendarView.setBackgroundResource(R.drawable.bottom_border_inactive_ripple);
            btnListView.setBackgroundResource(R.drawable.bottom_border_active_ripple);
        }
    }

}