package com.example.dailyboss.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dailyboss.R;
import com.example.dailyboss.fragments.TasksFragment;

public class TaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new TasksFragment())
                    .commit();
        }
    }
}