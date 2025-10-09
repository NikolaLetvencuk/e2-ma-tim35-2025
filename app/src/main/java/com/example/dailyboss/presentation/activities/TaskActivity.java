package com.example.dailyboss.presentation.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.dailyboss.R;
import com.example.dailyboss.presentation.fragments.TasksFragment; // Pretpostavljam da je ovo fragment za listu zadataka

public class TaskActivity extends AppCompatActivity {

    private boolean isCalendarView = true;
    private ImageButton btnToggleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        // Uklonite isCalendarView i btnToggleView logiku

        ImageButton btnProfile = findViewById(R.id.btnProfile);

        // Uklonite dugmad za prebacivanje iz TaskActivity, jer je logika u TasksFragment-u
        findViewById(R.id.btnCalendarView).setVisibility(View.GONE);
        findViewById(R.id.btnListView).setVisibility(View.GONE);

        // Postavljanje početnog fragmenta - TasksFragment sada rukuje prebacivanjem
        if (savedInstanceState == null) {
            loadFragment(new TasksFragment());
        }

        // Logika za Back dugme

        // Logika za Profile dugme
        btnProfile.setOnClickListener(v -> {
            // TODO: Implementiraj logiku za otvaranje profila
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}