package com.example.dailyboss;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import com.example.dailyboss.fragments.CategoryListFragment;
import com.example.dailyboss.fragments.HomeFragment;
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

            for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
                MenuItem menuItem = bottomNavigationView.getMenu().getItem(i);
                View icon = bottomNavigationView.findViewById(menuItem.getItemId());

                if (menuItem.getItemId() == item.getItemId()) {
                    icon.setScaleX(1.3f);
                    icon.setScaleY(1.3f);
                } else {
                    icon.setScaleX(1f);
                    icon.setScaleY(1f);
                }
            }

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
            }

            return false;
        });
    }
}
