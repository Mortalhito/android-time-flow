package com.example.timeflow;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.timeflow.ui.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private CountdownFragment countdownFragment;
    private HabitFragment habitFragment;
    private HomeFragment homeFragment;
    private FocusFragment focusFragment;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFragments();
        setupBottomNavigation();

        // 默认显示首页
        showFragment(homeFragment);
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private void initFragments() {
        countdownFragment = new CountdownFragment();
        habitFragment = new HabitFragment();
        homeFragment = new HomeFragment();
        focusFragment = new FocusFragment();
        profileFragment = new ProfileFragment();
    }

    private void setupBottomNavigation() {
        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_countdown) {
                showFragment(countdownFragment);
                return true;
            } else if (item.getItemId() == R.id.nav_habit) {
                showFragment(habitFragment);
                return true;
            } else if (item.getItemId() == R.id.nav_home) {
                showFragment(homeFragment);
                return true;
            } else if (item.getItemId() == R.id.nav_focus) {
                showFragment(focusFragment);
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                showFragment(profileFragment);
                return true;
            }
            return false;
        });
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}