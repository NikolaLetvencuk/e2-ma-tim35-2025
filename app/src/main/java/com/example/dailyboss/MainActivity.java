package com.example.dailyboss;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dailyboss.fragments.CategoryListFragment;
import com.example.dailyboss.fragments.CreateTaskFragment;
import com.example.dailyboss.fragments.HomeFragment;
import com.example.dailyboss.fragments.TasksFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
                return true;
            } else if (item.getItemId() == R.id.nav_categories) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new CategoryListFragment())
                        .commit();
                return true;
            } else if (item.getItemId() == R.id.activity_tasks) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TasksFragment())
                        .commit();
                return true;
            }
            return false;
        });
    }
}
