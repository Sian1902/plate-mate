package com.example.plate_mate;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.plate_mate.presentation.mealdetails.view.MealDetailsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements MealDetailsFragment.NavVisibilityCallback {
    private NavController navController;
    private BottomNavigationView bottomNav;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            bottomNav = findViewById(R.id.bottomNavigation);
            toolbar = findViewById(R.id.mainToolbar);
            NavigationUI.setupWithNavController(bottomNav, navController);
        }
    }
    @Override
    public void setNavigationVisibility(boolean isVisible) {
        runOnUiThread(() -> {
            if (bottomNav != null) {
                bottomNav.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            }

            if (toolbar != null) {
                toolbar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp();
    }


}